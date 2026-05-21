package com.example.wannianli.data.model

data class SolarTermInfo(
    val name: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val isCurrent: Boolean = false
) {
    val fullDateTime: String
        get() = "${year}年${month}月${day}日 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:${second.toString().padStart(2, '0')}"
}

data class FourPillars(
    val yearPillar: String,
    val monthPillar: String,
    val dayPillar: String,
    val hourPillar: String
)

data class LunarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean = false,
    val yearName: String = "",
    val monthName: String = "",
    val dayName: String = "",
    val yearGanZhi: String = "",
    val monthGanZhi: String = "",
    val dayGanZhi: String = "",
    val hourGanZhi: String = "",
    val shengXiao: String = ""
)

data class CalendarDay(
    val gregorianYear: Int,
    val gregorianMonth: Int,
    val gregorianDay: Int,
    val weekday: String,
    val lunarDate: LunarDate,
    val currentSolarTerm: SolarTermInfo?,
    val nextSolarTerm: SolarTermInfo?,
    val daysUntilNextTerm: Long = 0
)

data class ChangelogEntry(
    val version: String,
    val date: String,
    val content: String
)