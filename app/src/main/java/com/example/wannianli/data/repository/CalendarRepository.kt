package com.example.wannianli.data.repository

import android.content.Context
import com.example.wannianli.data.local.ChangelogDataSource
import com.example.wannianli.data.model.*
import com.example.wannianli.engine.*
import java.util.*
import kotlin.math.floor

class CalendarRepository(private val context: Context) {
    private var cachedSolarTerms: MutableMap<Int, List<SolarTermCalculator.SolarTermResult>> = mutableMapOf()
    private var cachedPillarsResults: MutableMap<String, FourPillarsEngine.PillarsResult> = mutableMapOf()
    private var cachedHolidays: MutableMap<Int, Map<String, String>> = mutableMapOf()
    private var cachedTermDays: MutableMap<Int, Map<String, String>> = mutableMapOf()

    fun getCalendarDay(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        minute: Int = Calendar.getInstance().get(Calendar.MINUTE)
    ): CalendarDay {
        val weekdayIndex = getWeekday(year, month, day)
        val weekdayName = "星期${CalendarConstants.WEEKDAY_NAMES[weekdayIndex]}"

        val allTermsThisYear = getSolarTermsForYear(year)
        val allTermsLastYear = getSolarTermsForYear(year - 1)
        val allTermsNextYear = getSolarTermsForYear(year + 1)

        val combinedTerms = allTermsLastYear + allTermsThisYear + allTermsNextYear

        val currentTerm = findCurrentSolarTerm(year, month, day, combinedTerms)
        val nextTerm = findNextSolarTerm(year, month, day, hour, minute, combinedTerms)

        val cacheKey = "$year-$month-$day-$hour-$minute"
        val pillarsResult = cachedPillarsResults.getOrPut(cacheKey) {
            val lunarResult = LunarCalendarEngine.solarToLunar(year, month, day)
            FourPillarsEngine.calculate(year, month, day, hour, minute, allTermsThisYear, lunarResult)
        }

        val currentSolarTermInfo = currentTerm?.let {
            SolarTermInfo(it.name, it.year, it.month, it.day, it.hour, it.minute, it.second, true)
        }
        val nextSolarTermInfo = nextTerm?.let {
            SolarTermInfo(it.name, it.year, it.month, it.day, it.hour, it.minute, it.second, false)
        }

        val daysUntilNext = if (nextTerm != null && currentTerm == null) {
            computeSecondsUntil(year, month, day, hour, minute, nextTerm)
        } else 0L

        val lunarDate = LunarDate(
            year = pillarsResult.lunarYear,
            month = pillarsResult.lunarMonth,
            day = pillarsResult.lunarDay,
            isLeapMonth = pillarsResult.isLeapMonth,
            yearName = pillarsResult.ganZhiYear,
            monthName = pillarsResult.lunarMonthName,
            dayName = pillarsResult.lunarDayName,
            yearGanZhi = pillarsResult.yearGanZhi,
            monthGanZhi = pillarsResult.monthGanZhi,
            dayGanZhi = pillarsResult.dayGanZhi,
            hourGanZhi = pillarsResult.hourGanZhi,
            shengXiao = pillarsResult.shengXiao
        )

        return CalendarDay(
            gregorianYear = year,
            gregorianMonth = month,
            gregorianDay = day,
            weekday = weekdayName,
            lunarDate = lunarDate,
            currentSolarTerm = currentSolarTermInfo,
            nextSolarTerm = nextSolarTermInfo,
            daysUntilNextTerm = daysUntilNext
        )
    }

    fun getSolarTermsForYear(year: Int): List<SolarTermCalculator.SolarTermResult> {
        return cachedSolarTerms.getOrPut(year) {
            SolarTermCalculator.calculateSolarTerms(year).map { term ->
                val actualYear = if (term.month == 1) year + 1 else year
                SolarTermCalculator.SolarTermResult(
                    term.name, actualYear, term.month, term.day, term.hour, term.minute, term.second
                )
            }
        }
    }

    fun getChangelog(): List<ChangelogEntry> {
        return ChangelogDataSource.getChangelog(context)
    }

    fun addChangelogEntry(entry: ChangelogEntry) {
        ChangelogDataSource.addEntry(context, entry)
    }

    private fun findCurrentSolarTerm(
        year: Int, month: Int, day: Int,
        terms: List<SolarTermCalculator.SolarTermResult>
    ): SolarTermCalculator.SolarTermResult? {
        return terms.find { it.year == year && it.month == month && it.day == day }
    }

    private fun findNextSolarTerm(
        year: Int, month: Int, day: Int, hour: Int, minute: Int,
        terms: List<SolarTermCalculator.SolarTermResult>
    ): SolarTermCalculator.SolarTermResult? {
        val nowJd = LunarCalendarEngine.gregorianToJD(year, month, day) +
                hour / 24.0 + minute / 1440.0

        var closestTerm: SolarTermCalculator.SolarTermResult? = null
        var closestDiff = Double.MAX_VALUE

        for (term in terms) {
            val termJd = LunarCalendarEngine.gregorianToJD(term.year, term.month, term.day) +
                    term.hour / 24.0 + term.minute / 1440.0 + term.second / 86400.0
            val diff = termJd - nowJd
            if (diff > 0 && diff < closestDiff) {
                closestDiff = diff
                closestTerm = term
            }
        }

        return closestTerm
    }

    private fun computeSecondsUntil(
        year: Int, month: Int, day: Int, hour: Int, minute: Int,
        term: SolarTermCalculator.SolarTermResult
    ): Long {
        val nowJd = LunarCalendarEngine.gregorianToJD(year, month, day) +
                hour / 24.0 + minute / 1440.0
        val termJd = LunarCalendarEngine.gregorianToJD(term.year, term.month, term.day) +
                term.hour / 24.0 + term.minute / 1440.0 + term.second / 86400.0
        val diffDays = termJd - nowJd
        return (diffDays * 86400.0).toLong().coerceAtLeast(0)
    }

    private fun getWeekday(year: Int, month: Int, day: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day)
        return cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    fun getLunarDayName(year: Int, month: Int, day: Int): String {
        val result = LunarCalendarEngine.solarToLunar(year, month, day)
        val dayIndex = result.day - 1
        return if (dayIndex in 0..29) CalendarConstants.LUNAR_DAY_NAMES[dayIndex] else "${result.day}"
    }

    fun getLunarMonthName(year: Int, month: Int, day: Int): String {
        val result = LunarCalendarEngine.solarToLunar(year, month, day)
        return CalendarConstants.LUNAR_MONTH_NAMES[result.month - 1]
    }

    fun isLunarNewMonth(year: Int, month: Int, day: Int): Boolean {
        val result = LunarCalendarEngine.solarToLunar(year, month, day)
        return result.day == 1
    }

    fun isWeekend(year: Int, month: Int, day: Int): Boolean {
        val weekdayIndex = getWeekday(year, month, day)
        return weekdayIndex == 0 || weekdayIndex == 6
    }

    fun getMonthDayCount(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun getHolidayName(year: Int, month: Int, day: Int): String? {
        val holidays = cachedHolidays.getOrPut(year) { computeHolidays(year) }
        return holidays["$month-$day"]
    }

    fun getSolarTermDayName(year: Int, month: Int, day: Int): String? {
        val termDays = cachedTermDays.getOrPut(year) {
            getSolarTermsForYear(year)
                .filter { it.year == year }
                .associate { "${it.month}-${it.day}" to it.name }
        }
        return termDays["$month-$day"]
    }

    private fun computeHolidays(year: Int): Map<String, String> {
        val holidays = mutableMapOf<String, String>()

        holidays["1-1"] = "元旦"
        holidays["5-1"] = "劳动节"
        holidays["10-1"] = "国庆"
        holidays["10-2"] = "国庆"
        holidays["10-3"] = "国庆"

        for (d in 15..31) {
            val result = LunarCalendarEngine.solarToLunar(year, 1, d)
            if (result.month == 1 && result.day == 1) {
                holidays["1-$d"] = "春节"
                if (d > 1) holidays["1-${d - 1}"] = "除夕"
                else {
                    val prevResult = LunarCalendarEngine.solarToLunar(year - 1, 12, 31)
                    if (prevResult.month == 12 && prevResult.day == 30) holidays["12-31"] = "除夕"
                }
                break
            } else if (result.month == 12 && result.day == 30) {
                holidays["1-$d"] = "除夕"
            }
        }
        for (d in 1..15) {
            val result = LunarCalendarEngine.solarToLunar(year, 2, d)
            if (result.month == 1 && result.day == 1) {
                holidays["2-$d"] = "春节"
                val prevDate = getPreviousDay(year, 2, d)
                holidays["${prevDate.first}-${prevDate.second}"] = "除夕"
                break
            }
        }

        for (d in 15..31) {
            val result = LunarCalendarEngine.solarToLunar(year, 5, d)
            if (result.month == 5 && result.day == 5) {
                holidays["5-$d"] = "端午"
                break
            }
        }
        for (d in 1..15) {
            val result = LunarCalendarEngine.solarToLunar(year, 6, d)
            if (result.month == 5 && result.day == 5) {
                holidays["6-$d"] = "端午"
                break
            }
        }

        for (d in 1..31) {
            val result = LunarCalendarEngine.solarToLunar(year, 9, d)
            if (result.month == 8 && result.day == 15) {
                holidays["9-$d"] = "中秋"
                break
            }
        }
        for (d in 1..15) {
            val result = LunarCalendarEngine.solarToLunar(year, 10, d)
            if (result.month == 8 && result.day == 15) {
                holidays["10-$d"] = "中秋"
                break
            }
        }

        val terms = getSolarTermsForYear(year)
        val qingming = terms.find { it.name == "清明" && it.year == year }
        if (qingming != null) {
            holidays["${qingming.month}-${qingming.day}"] = "清明"
        }

        return holidays
    }

    private fun getPreviousDay(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        if (day > 1) return Triple(year, month, day - 1)
        if (month > 1) return Triple(year, month - 1, getMonthDayCount(year, month - 1))
        return Triple(year - 1, 12, 31)
    }

    fun clearCache() {
        cachedSolarTerms.clear()
        cachedPillarsResults.clear()
        cachedHolidays.clear()
        cachedTermDays.clear()
    }
}