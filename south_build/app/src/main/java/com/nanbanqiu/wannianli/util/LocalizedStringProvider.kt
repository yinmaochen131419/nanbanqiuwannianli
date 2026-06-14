/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.util

import android.content.Context
import com.nanbanqiu.wannianli.R

/**
 * Provides localized strings for Chinese calendar terms.
 * Maps Chinese text from CalendarConstants to string resource IDs.
 */
object LocalizedStringProvider {

    // Solar Terms
    private val SOLAR_TERM_IDS = intArrayOf(
        R.string.solar_term_0, R.string.solar_term_1, R.string.solar_term_2,
        R.string.solar_term_3, R.string.solar_term_4, R.string.solar_term_5,
        R.string.solar_term_6, R.string.solar_term_7, R.string.solar_term_8,
        R.string.solar_term_9, R.string.solar_term_10, R.string.solar_term_11,
        R.string.solar_term_12, R.string.solar_term_13, R.string.solar_term_14,
        R.string.solar_term_15, R.string.solar_term_16, R.string.solar_term_17,
        R.string.solar_term_18, R.string.solar_term_19, R.string.solar_term_20,
        R.string.solar_term_21, R.string.solar_term_22, R.string.solar_term_23
    )

    fun getSolarTerm(context: Context, index: Int): String {
        if (index !in SOLAR_TERM_IDS.indices) return ""
        val result = context.getString(SOLAR_TERM_IDS[index])
        return if (result.isBlank()) {
            val terms = com.nanbanqiu.wannianli.engine.CalendarConstants.NORTH_SOLAR_TERM_NAMES
            if (index in terms.indices) terms[index] else ""
        } else result
    }

    fun getSolarTermByName(context: Context, chineseName: String): String {
        val northTerms = com.nanbanqiu.wannianli.engine.CalendarConstants.NORTH_SOLAR_TERM_NAMES
        val idx = northTerms.indexOf(chineseName)
        return if (idx >= 0) getSolarTerm(context, idx) else chineseName
    }

    // Zodiac
    private val ZODIAC_IDS = intArrayOf(
        R.string.zodiac_0, R.string.zodiac_1, R.string.zodiac_2,
        R.string.zodiac_3, R.string.zodiac_4, R.string.zodiac_5,
        R.string.zodiac_6, R.string.zodiac_7, R.string.zodiac_8,
        R.string.zodiac_9, R.string.zodiac_10, R.string.zodiac_11
    )

    fun getZodiac(context: Context, index: Int): String {
        if (index !in ZODIAC_IDS.indices) return ""
        val result = context.getString(ZODIAC_IDS[index])
        return if (result.isBlank()) {
            val zodiacs = com.nanbanqiu.wannianli.engine.CalendarConstants.SHENG_XIAO
            if (index in zodiacs.indices) zodiacs[index] else ""
        } else result
    }

    fun getZodiacByName(context: Context, chineseName: String): String {
        val zodiacs = com.nanbanqiu.wannianli.engine.CalendarConstants.SHENG_XIAO
        val idx = zodiacs.indexOf(chineseName)
        return if (idx >= 0) getZodiac(context, idx) else chineseName
    }

    // Four Symbols
    private val FOUR_SYMBOL_IDS = intArrayOf(
        R.string.four_symbol_0, R.string.four_symbol_1,
        R.string.four_symbol_2, R.string.four_symbol_3
    )

    fun getFourSymbol(context: Context, index: Int): String {
        if (index !in FOUR_SYMBOL_IDS.indices) {
            val symbols = arrayOf("青龙", "白虎", "朱雀", "玄武")
            return if (index in symbols.indices) symbols[index] else ""
        }
        val result = context.getString(FOUR_SYMBOL_IDS[index])
        return if (result.isBlank()) {
            val symbols = arrayOf("青龙", "白虎", "朱雀", "玄武")
            if (index in symbols.indices) symbols[index] else result
        } else result
    }

    // Seasons
    private val SEASON_IDS = intArrayOf(
        R.string.season_0, R.string.season_1,
        R.string.season_2, R.string.season_3
    )

    fun getSeason(context: Context, index: Int): String {
        if (index !in SEASON_IDS.indices) return ""
        val result = context.getString(SEASON_IDS[index])
        return if (result.isBlank()) {
            val seasons = arrayOf("春", "夏", "秋", "冬")
            if (index in seasons.indices) seasons[index] else ""
        } else result
    }

    fun getSeasonByName(context: Context, chineseName: String): String {
        val seasons = arrayOf("春", "夏", "秋", "冬")
        val idx = seasons.indexOf(chineseName)
        return if (idx >= 0) getSeason(context, idx) else chineseName
    }

    // Moon Phases
    private val MOON_PHASE_IDS = intArrayOf(
        R.string.moon_phase_0, R.string.moon_phase_1, R.string.moon_phase_2,
        R.string.moon_phase_3, R.string.moon_phase_4, R.string.moon_phase_5,
        R.string.moon_phase_6, R.string.moon_phase_7
    )

    fun getMoonPhase(context: Context, index: Int): String {
        if (index !in MOON_PHASE_IDS.indices) return ""
        val result = context.getString(MOON_PHASE_IDS[index])
        return if (result.isBlank()) {
            val phases = arrayOf("新月", "蛾眉月", "上弦月", "盈凸月", "满月", "亏凸月", "下弦月", "残月")
            if (index in phases.indices) phases[index] else ""
        } else result
    }

    fun getSouthMoonPhase(context: Context, phaseIndex: Int): String {
        // South hemisphere: reversed visual mapping
        val southIndices = intArrayOf(0, 7, 6, 5, 4, 3, 2, 1)
        return if (phaseIndex in southIndices.indices) getMoonPhase(context, southIndices[phaseIndex]) else ""
    }

    fun getMoonPhaseByName(context: Context, chineseName: String): String {
        val phaseNames = arrayOf("新月", "蛾眉月", "上弦月", "盈凸月", "满月", "亏凸月", "下弦月", "残月")
        val idx = phaseNames.indexOf(chineseName)
        return if (idx >= 0) getMoonPhase(context, idx) else chineseName
    }

    // 28 Mansions
    private val MANSION_IDS = intArrayOf(
        R.string.mansion_0, R.string.mansion_1, R.string.mansion_2,
        R.string.mansion_3, R.string.mansion_4, R.string.mansion_5,
        R.string.mansion_6, R.string.mansion_7, R.string.mansion_8,
        R.string.mansion_9, R.string.mansion_10, R.string.mansion_11,
        R.string.mansion_12, R.string.mansion_13, R.string.mansion_14,
        R.string.mansion_15, R.string.mansion_16, R.string.mansion_17,
        R.string.mansion_18, R.string.mansion_19, R.string.mansion_20,
        R.string.mansion_21, R.string.mansion_22, R.string.mansion_23,
        R.string.mansion_24, R.string.mansion_25, R.string.mansion_26,
        R.string.mansion_27
    )

    fun getMansion(context: Context, index: Int): String {
        if (index !in MANSION_IDS.indices) {
            val allNames = com.nanbanqiu.wannianli.engine.CalendarConstants.TWENTY_EIGHT_MANSIONS
            return if (index in allNames.indices) allNames[index] else ""
        }
        val result = context.getString(MANSION_IDS[index])
        return if (result.isBlank()) {
            val allNames = com.nanbanqiu.wannianli.engine.CalendarConstants.TWENTY_EIGHT_MANSIONS
            if (index in allNames.indices) allNames[index] else result
        } else result
    }

    fun getMansionByName(context: Context, chineseName: String): String {
        // chineseName may be like "牛宿" or "牛金牛" - extract the first character
        val allNames = com.nanbanqiu.wannianli.engine.CalendarConstants.TWENTY_EIGHT_MANSIONS
        for (i in allNames.indices) {
            if (allNames[i].startsWith(chineseName) || chineseName.startsWith(allNames[i][0].toString())) {
                return getMansion(context, i)
            }
        }
        return chineseName
    }

    // Lunar Day Names
    private val LUNAR_DAY_IDS = intArrayOf(
        R.string.lunar_day_0, R.string.lunar_day_1, R.string.lunar_day_2,
        R.string.lunar_day_3, R.string.lunar_day_4, R.string.lunar_day_5,
        R.string.lunar_day_6, R.string.lunar_day_7, R.string.lunar_day_8,
        R.string.lunar_day_9, R.string.lunar_day_10, R.string.lunar_day_11,
        R.string.lunar_day_12, R.string.lunar_day_13, R.string.lunar_day_14,
        R.string.lunar_day_15, R.string.lunar_day_16, R.string.lunar_day_17,
        R.string.lunar_day_18, R.string.lunar_day_19, R.string.lunar_day_20,
        R.string.lunar_day_21, R.string.lunar_day_22, R.string.lunar_day_23,
        R.string.lunar_day_24, R.string.lunar_day_25, R.string.lunar_day_26,
        R.string.lunar_day_27, R.string.lunar_day_28, R.string.lunar_day_29
    )

    fun getLunarDay(context: Context, index: Int): String {
        if (index !in LUNAR_DAY_IDS.indices) return ""
        val result = context.getString(LUNAR_DAY_IDS[index])
        return if (result.isBlank()) {
            val dayNames = com.nanbanqiu.wannianli.engine.CalendarConstants.LUNAR_DAY_NAMES
            if (index in dayNames.indices) dayNames[index] else ""
        } else result
    }

    fun getLunarDayByName(context: Context, chineseName: String): String {
        val dayNames = com.nanbanqiu.wannianli.engine.CalendarConstants.LUNAR_DAY_NAMES
        val idx = dayNames.indexOf(chineseName)
        return if (idx >= 0) getLunarDay(context, idx) else chineseName
    }

    // Northern Lunar Month Names
    private val NORTH_LUNAR_MONTH_IDS = intArrayOf(
        R.string.north_lunar_month_0, R.string.north_lunar_month_1,
        R.string.north_lunar_month_2, R.string.north_lunar_month_3,
        R.string.north_lunar_month_4, R.string.north_lunar_month_5,
        R.string.north_lunar_month_6, R.string.north_lunar_month_7,
        R.string.north_lunar_month_8, R.string.north_lunar_month_9,
        R.string.north_lunar_month_10, R.string.north_lunar_month_11
    )

    fun getNorthLunarMonth(context: Context, index: Int): String {
        if (index !in NORTH_LUNAR_MONTH_IDS.indices) return ""
        val result = context.getString(NORTH_LUNAR_MONTH_IDS[index])
        return if (result.isBlank()) {
            val months = com.nanbanqiu.wannianli.engine.CalendarConstants.NORTH_LUNAR_MONTH_NAMES
            if (index in months.indices) months[index] else ""
        } else result
    }

    fun getNorthLunarMonthByName(context: Context, chineseName: String): String {
        val monthNames = com.nanbanqiu.wannianli.engine.CalendarConstants.NORTH_LUNAR_MONTH_NAMES
        val idx = monthNames.indexOf(chineseName)
        return if (idx >= 0) getNorthLunarMonth(context, idx) else chineseName
    }

    // Southern Lunar Month Names
    private val SOUTH_LUNAR_MONTH_IDS = intArrayOf(
        R.string.south_lunar_month_0, R.string.south_lunar_month_1,
        R.string.south_lunar_month_2, R.string.south_lunar_month_3,
        R.string.south_lunar_month_4, R.string.south_lunar_month_5,
        R.string.south_lunar_month_6, R.string.south_lunar_month_7,
        R.string.south_lunar_month_8, R.string.south_lunar_month_9,
        R.string.south_lunar_month_10, R.string.south_lunar_month_11
    )

    fun getSouthLunarMonth(context: Context, index: Int): String {
        if (index !in SOUTH_LUNAR_MONTH_IDS.indices) return ""
        val result = context.getString(SOUTH_LUNAR_MONTH_IDS[index])
        return if (result.isBlank()) {
            val months = com.nanbanqiu.wannianli.engine.CalendarConstants.SOUTH_LUNAR_MONTH_NAMES
            if (index in months.indices) months[index] else ""
        } else result
    }

    fun getSouthLunarMonthByName(context: Context, chineseName: String): String {
        val monthNames = com.nanbanqiu.wannianli.engine.CalendarConstants.SOUTH_LUNAR_MONTH_NAMES
        val idx = monthNames.indexOf(chineseName)
        return if (idx >= 0) getSouthLunarMonth(context, idx) else chineseName
    }

    // Five Elements
    private val WUXING_IDS = intArrayOf(
        R.string.wuxing_0, R.string.wuxing_1, R.string.wuxing_2,
        R.string.wuxing_3, R.string.wuxing_4
    )

    fun getWuxing(context: Context, index: Int): String {
        if (index !in WUXING_IDS.indices) return ""
        val result = context.getString(WUXING_IDS[index])
        return if (result.isBlank()) {
            val elements = arrayOf("木", "火", "土", "金", "水")
            if (index in elements.indices) elements[index] else ""
        } else result
    }

    // Helper: get mansion with four symbol annotation
    fun getMansionWithSymbol(context: Context, mansionIndex: Int): String {
        val mansionName = getMansion(context, mansionIndex)
        val symbolIndex = mansionIndex / 7  // 0-6: 青龙, 7-13: 玄武, 14-20: 白虎, 21-27: 朱雀
        val symbolName = getFourSymbol(context, symbolIndex)
        if (mansionName.isBlank() && symbolName.isBlank()) {
            val allNames = com.nanbanqiu.wannianli.engine.CalendarConstants.TWENTY_EIGHT_MANSIONS
            val symbols = arrayOf("青龙", "玄武", "白虎", "朱雀")
            val mName = if (mansionIndex in allNames.indices) allNames[mansionIndex] else ""
            val sName = if (symbolIndex in symbols.indices) symbols[symbolIndex] else ""
            return "$mName($sName)"
        }
        if (mansionName.isBlank()) return symbolName
        if (symbolName.isBlank()) return mansionName
        return "$mansionName($symbolName)"
    }

    // Helper: get leap month display
    fun getLeapMonthDisplay(context: Context, monthName: String, isLeap: Boolean): String {
        val prefix = context.getString(R.string.leap_month_prefix)
        return if (isLeap) "$prefix$monthName" else monthName
    }

    // Helper: localize composed southOppositeDay string (e.g., "闰南十月初三" or "南十月初三")
    fun getLocalizedSouthOppositeDay(context: Context, composedStr: String): String {
        val southMonthNames = com.nanbanqiu.wannianli.engine.CalendarConstants.SOUTH_LUNAR_MONTH_NAMES
        val lunarDayNames = com.nanbanqiu.wannianli.engine.CalendarConstants.LUNAR_DAY_NAMES
        var isLeap = false
        var remaining = composedStr
        if (remaining.startsWith("闰")) {
            isLeap = true
            remaining = remaining.substring(1)
        }
        var localizedMonth = ""
        var localizedDay = ""
        for (i in southMonthNames.indices) {
            if (remaining.startsWith(southMonthNames[i])) {
                localizedMonth = getSouthLunarMonth(context, i)
                remaining = remaining.substring(southMonthNames[i].length)
                break
            }
        }
        for (i in lunarDayNames.indices) {
            if (remaining == lunarDayNames[i]) {
                localizedDay = getLunarDay(context, i)
                break
            }
        }
        val prefix = if (isLeap) context.getString(R.string.leap_month_prefix) else ""
        return "$prefix$localizedMonth$localizedDay"
    }

    // Weekday full names
    fun getWeekdayName(context: Context, weekdayIndex: Int): String {
        return when (weekdayIndex) {
            0 -> context.getString(R.string.weekday_full_sun)
            1 -> context.getString(R.string.weekday_full_mon)
            2 -> context.getString(R.string.weekday_full_tue)
            3 -> context.getString(R.string.weekday_full_wed)
            4 -> context.getString(R.string.weekday_full_thu)
            5 -> context.getString(R.string.weekday_full_fri)
            6 -> context.getString(R.string.weekday_full_sat)
            else -> ""
        }
    }

    // Weekday short names
    fun getWeekdayShort(context: Context, weekdayIndex: Int): String {
        return when (weekdayIndex) {
            0 -> context.getString(R.string.weekday_sun)
            1 -> context.getString(R.string.weekday_mon)
            2 -> context.getString(R.string.weekday_tue)
            3 -> context.getString(R.string.weekday_wed)
            4 -> context.getString(R.string.weekday_thu)
            5 -> context.getString(R.string.weekday_fri)
            6 -> context.getString(R.string.weekday_sat)
            else -> ""
        }
    }

    // Gregorian month names (localized: Chinese=数字, English=January..., Spanish=enero...)
    private val MONTH_NAME_IDS = intArrayOf(
        R.string.month_name_1, R.string.month_name_2, R.string.month_name_3,
        R.string.month_name_4, R.string.month_name_5, R.string.month_name_6,
        R.string.month_name_7, R.string.month_name_8, R.string.month_name_9,
        R.string.month_name_10, R.string.month_name_11, R.string.month_name_12
    )

    fun getMonthName(context: Context, month: Int): String {
        return if (month in 1..12) context.getString(MONTH_NAME_IDS[month - 1]) else month.toString()
    }

    // Format solar term datetime using localized format string
    fun formatSolarTermDateTime(context: Context, year: Int, month: Int, day: Int, hour: Int, minute: Int): String {
        val monthStr = getMonthName(context, month)
        return context.getString(R.string.format_solar_term_datetime, year.toString(), monthStr, day, hour, minute)
    }

    // Format year-month using localized format string
    fun formatYearMonth(context: Context, year: Int, month: Int): String {
        val monthStr = getMonthName(context, month)
        return context.getString(R.string.format_year_month, year.toString(), monthStr)
    }

    // Format year using localized format string (e.g., "2026年" or "2026")
    fun formatYear(context: Context, year: Int): String {
        return context.getString(R.string.format_year, year.toString())
    }

    // Format month using localized format string (e.g., "6月" or "June")
    fun formatMonth(context: Context, month: Int): String {
        val monthStr = getMonthName(context, month)
        return context.getString(R.string.format_month, monthStr)
    }

    // Format solar date title (e.g., "北半球阳历 2026年6月" or "Solar Norte, junio de 2026")
    fun formatSolarDateTitle(context: Context, year: Int, month: Int): String {
        val label = context.getString(R.string.home_north_solar)
        val yearMonth = formatYearMonth(context, year, month)
        return context.getString(R.string.home_north_solar_date, label, yearMonth)
    }
}
