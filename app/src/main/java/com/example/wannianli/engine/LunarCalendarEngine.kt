package com.example.wannianli.engine

import com.nlf.calendar.Solar
import kotlin.math.abs

object LunarCalendarEngine {
    fun solarToLunar(year: Int, month: Int, day: Int): LunarResult {
        val lunar = Solar.fromYmd(year, month, day).lunar
        return LunarResult(
            year = lunar.year,
            month = abs(lunar.month),
            day = lunar.day,
            isLeapMonth = lunar.toString().contains("闰")
        )
    }

    fun gregorianToJD(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = Math.floor(y / 100.0)
        val b = 2 - a + Math.floor(a / 4.0)
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    data class LunarResult(
        val year: Int,
        val month: Int,
        val day: Int,
        val isLeapMonth: Boolean
    )
}