/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data.model

data class SolarTermInfo(

    val name: String,

    val northName: String = "",

    val year: Int,

    val month: Int,

    val day: Int,

    val hour: Int,

    val minute: Int,

    val second: Int,

    val isCurrent: Boolean = false

) {

    val fullDateTime: String

        get() = "${year}年公历${month}月${day}日${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    /** 鏍规嵁鎸囧畾鏃跺尯璁＄畻鏈湴鏃堕棿 */

    fun getLocalDateTime(northZoneId: String, southZoneId: String): String {

        val north = java.time.LocalDateTime.of(year, month, day, hour, minute, second)

        val south = north.atZone(java.time.ZoneId.of(northZoneId))

            .withZoneSameInstant(java.time.ZoneId.of(southZoneId))

            .toLocalDateTime()
        // 南半球阳历：月份+6，超过12则减12；北半球月份<7时南半球年份=北半球年份-1
        var southMonth = south.monthValue + 6
        val southYear = if (south.monthValue < 7) south.year - 1 else south.year
        if (southMonth > 12) {
            southMonth -= 12
        }
        val southDay = south.dayOfMonth
        return "${southYear}年公历${southMonth}月${southDay}日${south.hour.toString().padStart(2, '0')}:${south.minute.toString().padStart(2, '0')}"

    }

    /** 鍏煎鏃ц皟鐢細榛樿浣跨敤涓婃捣鈫掑竷瀹滆鏂壘鍒╂柉 */

    val argentinaDateTime: String

        get() = getLocalDateTime("Asia/Shanghai", "America/Argentina/Buenos_Aires")

}

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

    val shengXiao: String = "",

    val southernYear: Int = 0,

    val southernMonth: Int = 0,

    val southernMonthName: String = "",

    val isSouthernLeapMonth: Boolean = false,

    val southernLeapMonth: Int = 0

)

data class CalendarDay(

    val gregorianYear: Int,

    val gregorianMonth: Int,

    val gregorianDay: Int,

    val weekday: String,

    val lunarDate: LunarDate,

    val currentSolarTerm: SolarTermInfo?,

    val nextSolarTerm: SolarTermInfo?,

    val daysUntilNextTerm: Long = 0,

    val southernSeason: String = "",

    val southernSeasonEmoji: String = "",

    val northYearGanZhi: String = "",

    val northMonthGanZhi: String = "",

    val northDayGanZhi: String = "",

    val northHourGanZhi: String = "",

    val northShengXiao: String = "",

    val southYearGanZhi: String = "",

    val southMonthGanZhi: String = "",

    val southDayGanZhi: String = "",

    val southHourGanZhi: String = "",

    val southShengXiao: String = "",

    val moonPhaseName: String = "",

    val southMoonPhaseName: String = "",

    val northMoonPhaseSymbol: String = "",

    val southMoonPhaseSymbol: String = "",

    val southOppositeDay: String = "",

    val southSolarYear: Int = 0,

    val southSolarMonth: Int = 0,

    val southSolarDay: Int = 0

)

data class ChangelogEntry(

    val version: String,

    val date: String,

    val content: String

)