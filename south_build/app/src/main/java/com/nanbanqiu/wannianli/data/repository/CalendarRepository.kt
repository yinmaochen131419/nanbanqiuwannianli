/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data.repository

import android.content.Context

import com.nanbanqiu.wannianli.data.model.*

import com.nanbanqiu.wannianli.engine.*

import java.util.*

import kotlin.math.floor

class CalendarRepository(private val context: Context) {

    private var cachedSolarTerms: MutableMap<Int, List<SolarTermCalculator.SolarTermResult>> = mutableMapOf()

    private var cachedPillarsResults: MutableMap<String, FourPillarsEngine.PillarsResult> = mutableMapOf()

    private var cachedNorthernPillars: MutableMap<String, FourPillarsEngine.NorthernPillars> = mutableMapOf()

    private var cachedTermDays: MutableMap<Int, Map<String, String>> = mutableMapOf()

    fun getCalendarDay(

        year: Int,

        month: Int,

        day: Int,

        hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),

        minute: Int = Calendar.getInstance().get(Calendar.MINUTE),

        northZoneId: String = "Asia/Shanghai"

    ): CalendarDay {

        // 把北半球城市本地时间转换为北京时间，用于农历计算
        val northZone = java.time.ZoneId.of(northZoneId)

        val beijingZone = java.time.ZoneId.of("Asia/Shanghai")

        val localDateTime = java.time.LocalDateTime.of(year, month, day, hour, minute)

        val localZdt = localDateTime.atZone(northZone)

        val beijingZdt = localZdt.withZoneSameInstant(beijingZone)

        // 用北京时间计算农历
        val bjYear = beijingZdt.year

        val bjMonth = beijingZdt.monthValue

        val bjDay = beijingZdt.dayOfMonth

        val bjHour = beijingZdt.hour

        val bjMinute = beijingZdt.minute

        val weekdayIndex = getWeekday(year, month, day)

        val weekdayName = "星期${CalendarConstants.WEEKDAY_NAMES[weekdayIndex]}"

        // 农历计算用北京时间
        val allTermsThisYear = getSolarTermsForYear(bjYear)

        val allTermsLastYear = getSolarTermsForYear(bjYear - 1)

        val allTermsNextYear = getSolarTermsForYear(bjYear + 1)

        val combinedTerms = allTermsLastYear + allTermsThisYear + allTermsNextYear

        val currentTerm = findCurrentSolarTerm(bjYear, bjMonth, bjDay, combinedTerms)

        val nextTerm = findNextSolarTerm(bjYear, bjMonth, bjDay, bjHour, bjMinute, combinedTerms)

        val lunarResult = LunarCalendarEngine.solarToLunar(bjYear, bjMonth, bjDay)

        // 四柱八字用当地原始时间计算（八字描述当地天地能量，必须用当地时辰）
        val localTermsThisYear = getSolarTermsForYear(year)

        val localCacheKey = "local-$year-$month-$day-$hour-$minute"

        val northernPillars = cachedNorthernPillars.getOrPut(localCacheKey) {

            FourPillarsEngine.calculateNorthern(year, month, day, hour, minute, localTermsThisYear, lunarResult)

        }

        val pillarsResult = cachedPillarsResults.getOrPut(localCacheKey) {

            FourPillarsEngine.calculate(year, month, day, hour, minute, localTermsThisYear, lunarResult)

        }

        val currentSolarTermInfo = currentTerm?.let {

            SolarTermInfo(it.name, it.northName, it.year, it.month, it.day, it.hour, it.minute, it.second, true)

        }

        val nextSolarTermInfo = nextTerm?.let {

            SolarTermInfo(it.name, it.northName, it.year, it.month, it.day, it.hour, it.minute, it.second, false)

        }

        val daysUntilNext = if (nextTerm != null && currentTerm == null) {

            computeSecondsUntil(bjYear, bjMonth, bjDay, bjHour, bjMinute, nextTerm)

        } else 0L

        val southernLunar = LunarCalendarEngine.toSouthernLunar(bjYear, bjMonth, bjDay)

        val southernMonthName = CalendarConstants.SOUTH_LUNAR_MONTH_NAMES[southernLunar.southernMonth - 1]

        val southYearGz = FourPillarsEngine.flipGanZhi(northernPillars.yearGanZhi)

        val southMonthGz = FourPillarsEngine.flipGanZhi(northernPillars.monthGanZhi)

        val southDayGz = FourPillarsEngine.flipGanZhi(northernPillars.dayGanZhi)

        val southHourGz = FourPillarsEngine.flipGanZhi(northernPillars.hourGanZhi)

        val southSxIndex = CalendarConstants.SHENG_XIAO.indexOf(northernPillars.shengXiao)

        val southSx = if (southSxIndex >= 0) CalendarConstants.SHENG_XIAO[(southSxIndex + 6) % 12] else northernPillars.shengXiao

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

            shengXiao = pillarsResult.shengXiao,

            southernYear = southernLunar.southernYear,

            southernMonth = southernLunar.southernMonth,

            southernMonthName = southernMonthName,

            isSouthernLeapMonth = southernLunar.isSouthernLeapMonth,

            southernLeapMonth = southernLunar.southernLeapMonth

        )

        val (season, seasonEmoji) = getSouthernSeason(month)

        val moonPhase = PureLunarEngine.calcMoonPhase(year, month, day)

        val (northSymbol, southSymbol) = getMoonPhaseSymbols(moonPhase.phaseIndex)

        val southPhaseName = getSouthMoonPhaseName(moonPhase.phaseIndex)

        val oppositeDayStr = computeOppositeDay(

            lunarResult.day,

            southernLunar.southernLeapMonth, southernLunar.southernMonth,

            bjYear, bjMonth, bjDay

        )

        // 南半球阳历：月份+6，日期不变，年界7月
        val southSolarYear: Int

        val southSolarMonth: Int

        if (month >= 7) {

            southSolarYear = year

            southSolarMonth = month - 6

        } else {

            southSolarYear = year - 1

            southSolarMonth = month + 6

        }

        return CalendarDay(

            gregorianYear = year,

            gregorianMonth = month,

            gregorianDay = day,

            weekday = weekdayName,

            lunarDate = lunarDate,

            currentSolarTerm = currentSolarTermInfo,

            nextSolarTerm = nextSolarTermInfo,

            daysUntilNextTerm = daysUntilNext,

            southernSeason = season,

            southernSeasonEmoji = seasonEmoji,

            northYearGanZhi = northernPillars.yearGanZhi,

            northMonthGanZhi = northernPillars.monthGanZhi,

            northDayGanZhi = northernPillars.dayGanZhi,

            northHourGanZhi = northernPillars.hourGanZhi,

            northShengXiao = northernPillars.shengXiao,

            southYearGanZhi = southYearGz,

            southMonthGanZhi = southMonthGz,

            southDayGanZhi = southDayGz,

            southHourGanZhi = southHourGz,

            southShengXiao = southSx,

            moonPhaseName = moonPhase.phaseName,

            southMoonPhaseName = southPhaseName,

            northMoonPhaseSymbol = northSymbol,

            southMoonPhaseSymbol = southSymbol,

            southOppositeDay = oppositeDayStr,

            southSolarYear = southSolarYear,

            southSolarMonth = southSolarMonth,

            southSolarDay = day

        )

    }

    fun getSolarTermsForYear(year: Int): List<SolarTermCalculator.SolarTermResult> {

        return cachedSolarTerms.getOrPut(year) {

            SolarTermCalculator.calculateSolarTerms(year)

        }

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

        val southernMonthIndex = (result.month - 1 + 6) % 12

        return CalendarConstants.SOUTH_LUNAR_MONTH_NAMES[southernMonthIndex]

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

    fun getSolarTermDayName(year: Int, month: Int, day: Int): String? {

        val lookupYear = if (month == 1 && year > 1900) year - 1 else year

        val termDays = cachedTermDays.getOrPut(lookupYear) {

            getSolarTermsForYear(lookupYear)

                .associate { "${it.month}-${it.day}" to it.name }

        }

        return termDays["$month-$day"]

    }

    private fun getSouthernSeason(month: Int): Pair<String, String> {

        return when (month) {

            9, 10, 11 -> "春" to "\uD83C\uDF38"

            12, 1, 2 -> "夏" to "\u2600\uFE0F"

            3, 4, 5 -> "秋" to "\uD83C\uDF42"

            6, 7, 8 -> "冬" to "\u2744\uFE0F"

            else -> "" to ""

        }

    }

    private var cachedMoonXiu: MutableMap<String, String> = mutableMapOf()

    fun getMoonXiuName(year: Int, month: Int, day: Int): String {

        val key = "$year-$month-$day"

        return cachedMoonXiu.getOrPut(key) {

            val jd = PlanetPositionCalc.gregorianToJD(year, month, day)

            val moon = PlanetPositionCalc.calcMoonPosition(jd)

            val xiuIdx = XiuBoundary.findXiu(moon.eclipticLon)

            XiuBoundary.XIU_NAMES[xiuIdx]

        }

    }

    fun clearCache() {

        cachedSolarTerms.clear()

        cachedPillarsResults.clear()

        cachedNorthernPillars.clear()

        cachedTermDays.clear()

        cachedMoonXiu.clear()

    }

    private fun getMoonPhaseSymbols(phaseIndex: Int): Pair<String, String> {

        // 北半球8个月相emoji
        val northSymbols = arrayOf(

            "\uD83C\uDF11", "\uD83C\uDF12", "\uD83C\uDF13", "\uD83C\uDF14",

            "\uD83C\uDF15", "\uD83C\uDF16", "\uD83C\uDF17", "\uD83C\uDF18"

        )

        val north = northSymbols[phaseIndex]

        // 南半球视觉镜像：phaseIndex -> (8 - phaseIndex) % 8

        val south = northSymbols[(8 - phaseIndex) % 8]

        return north to south

    }

    private fun getSouthMoonPhaseName(phaseIndex: Int): String {

        // 南半球月相名称按目视反向互换：蛾眉月⇌残月、上弦月⇌下弦月、盈凸月⇌亏凸月

        val southPhaseNames = arrayOf("新月", "残月", "下弦月", "亏凸月", "满月", "盈凸月", "上弦月", "蛾眉月")

        return southPhaseNames[phaseIndex]

    }

    private fun computeOppositeDay(

        lunarDay: Int,

        southernLeapMonth: Int,

        southernMonth: Int,

        gregorianYear: Int,

        gregorianMonth: Int,

        gregorianDay: Int

    ): String {

        // 计算太极对立日：农历日+15，月份就是当前南半球月份（不再+6）
        val oppositeDay = ((lunarDay - 1 + 15) % 30) + 1

        // 获取南半球当前农历月的实际天数，防止"三十"不存在
        val maxDaysInMonth = getSouthernLunarMonthDays(

            gregorianYear, gregorianMonth, gregorianDay, southernMonth, southernLeapMonth

        )

        val actualOppositeDay = if (oppositeDay > maxDaysInMonth) maxDaysInMonth else oppositeDay

        val LUNAR_DAY_NAMES = arrayOf(

            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",

            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",

            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"

        )

        // 判断当前南半球月份是否为闰月

        val isLeap = southernLeapMonth == southernMonth

        val monthDisplayName = CalendarConstants.SOUTH_LUNAR_MONTH_NAMES[southernMonth - 1]

        val displayMonth = if (isLeap) {

            "闰$monthDisplayName"

        } else {

            monthDisplayName

        }

        return "$displayMonth${LUNAR_DAY_NAMES[actualOppositeDay - 1]}"

    }

    /**
     * 获取南半球指定农历月的实际天数
     * 通过遍历该月的公历日期来计算
     */

    private fun getSouthernLunarMonthDays(

        gregorianYear: Int, gregorianMonth: Int, gregorianDay: Int,

        southernMonth: Int, southernLeapMonth: Int

    ): Int {

        try {

            var dayCount = 0

            var foundMonth = false

            for (offset in -180..180) {

                val cal = java.util.Calendar.getInstance()

                cal.set(gregorianYear, gregorianMonth - 1, gregorianDay)

                cal.add(java.util.Calendar.DAY_OF_MONTH, offset)

                val y = cal.get(java.util.Calendar.YEAR)

                val m = cal.get(java.util.Calendar.MONTH) + 1

                val d = cal.get(java.util.Calendar.DAY_OF_MONTH)

                val lunarRes = SxtwlBridge.nativeSolarToLunar(y, m, d)
                val lunarMonth = kotlin.math.abs(lunarRes[1])
                val lunarDay = lunarRes[2]
                val isLeap = lunarRes[1] < 0

                // 计算南半球月份
                val sMonth = if (lunarMonth >= 7) lunarMonth - 6 else lunarMonth + 6

                val isSouthLeap = isLeap && southernLeapMonth == sMonth

                // 检查是否在目标月份

                val isTargetMonth = sMonth == southernMonth &&

                    (isSouthLeap == (southernLeapMonth == southernMonth))

                if (isTargetMonth && lunarDay == 1 && !foundMonth) {

                    foundMonth = true

                    dayCount = 1

                } else if (isTargetMonth && foundMonth) {

                    dayCount++

                } else if (foundMonth && !isTargetMonth) {

                    break

                }

            }

            if (dayCount > 0) return dayCount

        } catch (_: Exception) {}

        return 30

    }

}
