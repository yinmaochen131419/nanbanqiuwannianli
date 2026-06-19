/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import kotlin.math.*

object SolarTermCalculator {

    private val JIE_QI_NAMES = arrayOf(
        "冬至", "小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
        "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露",
        "秋分", "寒露", "霜降", "立冬", "小雪", "大雪"
    )

    fun calculateSolarTerms(year: Int): List<SolarTermResult> {
        val jdValues = SxtwlBridge.nativeGetSolarTermsForYear(year)
        return (0 until 24).map { i ->
            val jd = jdValues[i]
            val gregorian = SxtwlBridge.jdToGregorian(jd)
            val northName = JIE_QI_NAMES[i]
            val southName = JIE_QI_NAMES[(i + 12) % 24]
            SolarTermResult(
                northName = northName,
                southName = southName,
                year = gregorian[0],
                month = gregorian[1],
                day = gregorian[2],
                hour = gregorian[3],
                minute = gregorian[4],
                second = gregorian[5]
            )
        }
    }

    fun calculateSolarTermJD(year: Int, termIndex: Int): Double {
        val terms = calculateSolarTerms(year)
        val term = terms[termIndex]
        return gregorianToJD(term.year, term.month, term.day, term.hour, term.minute, term.second)
    }

    fun gregorianToJD(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        var jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
        jd += (hour + minute / 60.0 + second / 3600.0) / 24.0
        return jd
    }

    fun jdToCalendar(jd: Double): IntArray {
        val z = floor(jd + 0.5)
        var a = z
        if (a >= 2299161) {
            val alpha = floor((z - 1867216.25) / 36524.25)
            a = z + 1 + alpha - floor(alpha / 4.0)
        }
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25)
        val d = floor(365.25 * c)
        val e = floor((b - d) / 30.6001)
        val day = (b - d - floor(30.6001 * e)).toInt()
        val mo = if (e < 14) (e - 1).toInt() else (e - 13).toInt()
        val yr = if (mo > 2) (c - 4716).toInt() else (c - 4715).toInt()
        val dayFrac = jd + 0.5 - z
        val totalSec = (dayFrac * 86400.0 + 0.5).toInt()
        val hour = totalSec / 3600
        val minute = (totalSec % 3600) / 60
        val second = totalSec % 60
        return intArrayOf(yr, mo, day, hour, minute, second)
    }

    data class SolarTermResult(
        val northName: String,
        val southName: String,
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int,
        val second: Int
    ) {
        val name: String get() = southName
    }
}