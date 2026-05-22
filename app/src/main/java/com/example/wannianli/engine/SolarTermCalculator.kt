package com.example.wannianli.engine

import com.nlf.calendar.Solar
import kotlin.math.*

object SolarTermCalculator {

    fun calculateSolarTerms(year: Int): List<SolarTermResult> {
        val tableThis = Solar.fromYmd(year, 7, 1).lunar.jieQiTable
        val tableNext = Solar.fromYmd(year + 1, 7, 1).lunar.jieQiTable
        return CalendarConstants.SOLAR_TERM_NAMES.mapIndexedNotNull { index, name ->
            val solar = if (index <= 20) tableThis[name] else tableNext[name]
            if (solar == null) {
                android.util.Log.e(
                    "SolarTermCalc",
                    "Missing key '$name' for year $year in ${if (index <= 20) "tableThis" else "tableNext"}"
                )
                null
            } else {
                SolarTermResult(name, solar.year, solar.month, solar.day, solar.hour, solar.minute, solar.second)
            }
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
        val name: String,
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int,
        val second: Int
    )
}