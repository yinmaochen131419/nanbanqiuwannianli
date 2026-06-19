/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data.model



data class ScheduleEvent(

    val id: Long = System.currentTimeMillis(),

    val type: Int = 0,

    val title: String = "",

    val year: Int = 2026,

    val month: Int = 6,

    val day: Int = 1,

    val recurring: Boolean = false,

    val isLunarDate: Boolean = false,

    val reminderDays: Int = 0,

    val note: String = ""

) {

    companion object {

        const val TYPE_BIRTHDAY = 0

        const val TYPE_ANNIVERSARY = 1

        const val TYPE_MEETING = 2

        const val TYPE_TODO = 3

        val TYPE_NAMES = listOf("生日", "纪念", "会议", "待办")

        val TYPE_ICONS = listOf("\uD83C\uDF82", "\uD83D\uDC8D", "\uD83D\uDCCB", "\u2705")

        val LUNAR_MONTH_NAMES = listOf("正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月")

        fun lunarToSolar(lunarYear: Int, lunarMonth: Int, lunarDay: Int): Triple<Int, Int, Int>? {
            for (y in lunarYear..lunarYear + 1) {
                for (m in 1..12) {
                    val dim = when (m) {
                        1, 3, 5, 7, 8, 10, 12 -> 31
                        4, 6, 9, 11 -> 30
                        2 -> if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 29 else 28
                        else -> 30
                    }
                    for (d in 1..dim) {
                        try {
                            val res = com.nanbanqiu.wannianli.engine.SxtwlBridge.nativeSolarToLunar(y, m, d)
                            val lm = kotlin.math.abs(res[1])
                            val ld = res[2]
                            val ly = res[0]
                            if (ly == lunarYear && lm == lunarMonth && ld == lunarDay) {
                                return Triple(y, m, d)
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
            return null
        }

    }

    val typeName: String get() = TYPE_NAMES.getOrElse(type) { "" }

    val typeIcon: String get() = TYPE_ICONS.getOrElse(type) { "" }

    fun toSolarDate(solarYear: Int): Triple<Int, Int, Int>? {

        if (!isLunarDate) return Triple(year, month, day)

        return try {
            val solar = lunarToSolar(solarYear, month, day)
            if (solar != null) Triple(solar.first, solar.second, solar.third) else null
        } catch (e: Exception) {
            null
        }

    }

    fun nextOccurrence(fromYear: Int, fromMonth: Int, fromDay: Int): String {

        if (!isLunarDate) {

            val fromDate = fromYear * 10000 + fromMonth * 100 + fromDay

            val thisYearDate = fromYear * 10000 + month * 100 + day

            if (thisYearDate >= fromDate) {

                return "${fromYear}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

            }

            return "${fromYear + 1}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

        }

        val solar = toSolarDate(fromYear)

        if (solar != null) {

            val solarDate = solar.first * 10000 + solar.second * 100 + solar.third

            val fromDate = fromYear * 10000 + fromMonth * 100 + fromDay

            if (solarDate >= fromDate) {

                return "${solar.first}-${solar.second.toString().padStart(2, '0')}-${solar.third.toString().padStart(2, '0')}"

            }

        }

        val nextSolar = toSolarDate(fromYear + 1)

        if (nextSolar != null) {

            return "${nextSolar.first}-${nextSolar.second.toString().padStart(2, '0')}-${nextSolar.third.toString().padStart(2, '0')}"

        }

        return "${fromYear}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

    }

}
