/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data.model

import com.nlf.calendar.Lunar

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

    }

    val typeName: String get() = TYPE_NAMES.getOrElse(type) { "" }

    val typeIcon: String get() = TYPE_ICONS.getOrElse(type) { "" }

    fun toSolarDate(solarYear: Int): Triple<Int, Int, Int>? {

        if (!isLunarDate) return Triple(year, month, day)

        return try {

            val lunar = Lunar.fromYmd(solarYear, month, day)

            val solar = lunar.solar

            Triple(solar.year, solar.month, solar.day)

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
