package com.example.wannianli.engine

import kotlin.math.*

object SolarTermCalculator {
    private data class TermInfo(val month: Int, val day: Int, val hour: Int, val minute: Int)

    private val solarTermData: Map<Int, List<TermInfo>> = mapOf(
        2020 to listOf(
            TermInfo(2,4,17,3), TermInfo(2,19,12,57), TermInfo(3,5,10,57), TermInfo(3,20,11,50),
            TermInfo(4,4,15,38), TermInfo(4,19,22,45), TermInfo(5,5,8,51), TermInfo(5,20,21,49),
            TermInfo(6,5,12,58), TermInfo(6,21,5,44), TermInfo(7,6,23,14), TermInfo(7,22,16,37),
            TermInfo(8,7,9,6), TermInfo(8,22,23,45), TermInfo(9,7,12,8), TermInfo(9,22,21,31),
            TermInfo(10,8,3,55), TermInfo(10,23,7,0), TermInfo(11,7,7,14), TermInfo(11,22,4,40),
            TermInfo(12,7,0,10), TermInfo(12,21,18,2), TermInfo(1,5,23,39), TermInfo(1,20,17,28)
        ),
        2021 to listOf(
            TermInfo(2,3,22,59), TermInfo(2,18,18,44), TermInfo(3,5,16,54), TermInfo(3,20,17,37),
            TermInfo(4,4,21,35), TermInfo(4,20,4,33), TermInfo(5,5,14,47), TermInfo(5,21,3,37),
            TermInfo(6,5,18,52), TermInfo(6,21,11,32), TermInfo(7,7,5,5), TermInfo(7,22,22,26),
            TermInfo(8,7,14,54), TermInfo(8,23,5,35), TermInfo(9,7,17,53), TermInfo(9,23,3,21),
            TermInfo(10,8,9,39), TermInfo(10,23,12,51), TermInfo(11,7,12,59), TermInfo(11,22,10,34),
            TermInfo(12,7,5,57), TermInfo(12,21,23,59), TermInfo(1,5,17,14), TermInfo(1,20,10,39)
        ),
        2022 to listOf(
            TermInfo(2,4,4,51), TermInfo(2,19,0,43), TermInfo(3,5,22,44), TermInfo(3,20,23,33),
            TermInfo(4,5,3,20), TermInfo(4,20,10,24), TermInfo(5,5,20,26), TermInfo(5,21,9,23),
            TermInfo(6,6,0,26), TermInfo(6,21,17,14), TermInfo(7,7,10,38), TermInfo(7,23,4,7),
            TermInfo(8,7,20,29), TermInfo(8,23,11,16), TermInfo(9,7,23,32), TermInfo(9,23,9,4),
            TermInfo(10,8,15,22), TermInfo(10,23,18,36), TermInfo(11,7,18,45), TermInfo(11,22,16,20),
            TermInfo(12,7,11,46), TermInfo(12,22,5,48), TermInfo(1,5,23,5), TermInfo(1,20,16,30)
        ),
        2023 to listOf(
            TermInfo(2,4,10,42), TermInfo(2,19,6,34), TermInfo(3,6,4,36), TermInfo(3,21,5,24),
            TermInfo(4,5,9,13), TermInfo(4,20,16,13), TermInfo(5,6,2,19), TermInfo(5,21,15,9),
            TermInfo(6,6,6,18), TermInfo(6,21,22,58), TermInfo(7,7,16,31), TermInfo(7,23,9,50),
            TermInfo(8,8,2,23), TermInfo(8,23,17,1), TermInfo(9,8,5,27), TermInfo(9,23,14,50),
            TermInfo(10,8,21,16), TermInfo(10,24,0,21), TermInfo(11,8,0,36), TermInfo(11,22,22,3),
            TermInfo(12,7,17,33), TermInfo(12,22,11,27), TermInfo(1,6,4,50), TermInfo(1,20,22,7)
        ),
        2024 to listOf(
            TermInfo(2,4,16,27), TermInfo(2,19,12,13), TermInfo(3,5,10,23), TermInfo(3,20,11,6),
            TermInfo(4,4,15,2), TermInfo(4,19,22,0), TermInfo(5,5,8,10), TermInfo(5,20,21,0),
            TermInfo(6,5,12,10), TermInfo(6,21,4,51), TermInfo(7,6,22,20), TermInfo(7,22,15,44),
            TermInfo(8,7,8,9), TermInfo(8,22,22,55), TermInfo(9,7,11,11), TermInfo(9,22,20,44),
            TermInfo(10,8,3,0), TermInfo(10,23,6,15), TermInfo(11,7,6,20), TermInfo(11,22,3,56),
            TermInfo(12,6,23,17), TermInfo(12,21,17,21), TermInfo(1,5,10,33), TermInfo(1,20,4,0)
        ),
        2025 to listOf(
            TermInfo(2,3,22,10), TermInfo(2,18,18,7), TermInfo(3,5,16,7), TermInfo(3,20,17,1),
            TermInfo(4,4,20,49), TermInfo(4,20,3,56), TermInfo(5,5,13,57), TermInfo(5,21,2,55),
            TermInfo(6,5,17,57), TermInfo(6,21,10,42), TermInfo(7,7,4,5), TermInfo(7,22,21,29),
            TermInfo(8,7,13,52), TermInfo(8,23,4,34), TermInfo(9,7,16,52), TermInfo(9,23,2,19),
            TermInfo(10,8,8,43), TermInfo(10,23,11,51), TermInfo(11,7,12,4), TermInfo(11,22,9,35),
            TermInfo(12,7,5,4), TermInfo(12,21,23,3), TermInfo(1,5,16,23), TermInfo(1,20,9,39)
        ),
        2026 to listOf(
            TermInfo(2,4,4,2), TermInfo(2,18,23,44), TermInfo(3,5,21,37), TermInfo(3,20,22,26),
            TermInfo(4,5,2,2), TermInfo(4,20,9,12), TermInfo(5,5,19,0), TermInfo(5,21,7,56),
            TermInfo(6,5,22,57), TermInfo(6,21,15,42), TermInfo(7,7,9,3), TermInfo(7,23,2,25),
            TermInfo(8,7,18,48), TermInfo(8,23,9,29), TermInfo(9,7,21,46), TermInfo(9,23,7,14),
            TermInfo(10,8,13,36), TermInfo(10,23,16,46), TermInfo(11,7,16,57), TermInfo(11,22,14,30),
            TermInfo(12,7,9,56), TermInfo(12,22,3,56), TermInfo(1,5,21,12), TermInfo(1,20,14,32)
        ),
        2027 to listOf(
            TermInfo(2,4,9,46), TermInfo(2,19,5,33), TermInfo(3,6,3,30), TermInfo(3,21,4,25),
            TermInfo(4,5,8,5), TermInfo(4,20,15,17), TermInfo(5,6,1,10), TermInfo(5,21,13,39),
            TermInfo(6,6,4,34), TermInfo(6,21,21,11), TermInfo(7,7,14,37), TermInfo(7,23,7,55),
            TermInfo(8,8,0,27), TermInfo(8,23,15,14), TermInfo(9,8,3,28), TermInfo(9,23,13,5),
            TermInfo(10,8,19,17), TermInfo(10,23,22,33), TermInfo(11,7,22,38), TermInfo(11,22,20,17),
            TermInfo(12,7,15,38), TermInfo(12,22,9,43), TermInfo(1,6,2,50), TermInfo(1,20,20,22)
        ),
        2028 to listOf(
            TermInfo(2,4,15,31), TermInfo(2,19,11,27), TermInfo(3,5,9,28), TermInfo(3,20,10,17),
            TermInfo(4,4,13,59), TermInfo(4,19,21,9), TermInfo(5,5,7,3), TermInfo(5,20,19,34),
            TermInfo(6,5,10,30), TermInfo(6,21,3,7), TermInfo(7,6,20,32), TermInfo(7,22,13,55),
            TermInfo(8,7,6,21), TermInfo(8,22,21,9), TermInfo(9,7,9,22), TermInfo(9,22,18,58),
            TermInfo(10,8,1,19), TermInfo(10,23,4,29), TermInfo(11,7,4,41), TermInfo(11,22,2,13),
            TermInfo(12,6,21,43), TermInfo(12,21,15,40), TermInfo(1,5,8,56), TermInfo(1,20,2,14)
        ),
        2029 to listOf(
            TermInfo(2,3,21,20), TermInfo(2,18,17,8), TermInfo(3,5,15,6), TermInfo(3,20,15,59),
            TermInfo(4,4,19,44), TermInfo(4,20,2,55), TermInfo(5,5,12,52), TermInfo(5,21,1,27),
            TermInfo(6,5,16,27), TermInfo(6,21,9,5), TermInfo(7,7,2,31), TermInfo(7,22,19,53),
            TermInfo(8,7,12,22), TermInfo(8,23,3,8), TermInfo(9,7,15,27), TermInfo(9,23,1,0),
            TermInfo(10,8,7,26), TermInfo(10,23,10,35), TermInfo(11,7,10,49), TermInfo(11,22,8,22),
            TermInfo(12,7,3,53), TermInfo(12,21,21,48), TermInfo(1,5,15,9), TermInfo(1,20,8,25)
        ),
    )

    fun calculateSolarTerms(year: Int): List<SolarTermResult> {
        solarTermData[year]?.let { data ->
            return data.mapIndexed { index, info ->
                val name = CalendarConstants.SOLAR_TERM_NAMES[index]
                SolarTermResult(name, year, info.month, info.day, info.hour, info.minute, 0)
            }
        }
        return calculateSolarTermsFallback(year)
    }

    fun calculateSolarTermJD(year: Int, termIndex: Int): Double {
        solarTermData[year]?.let { data ->
            val info = data[termIndex]
            return gregorianToJD(year, info.month, info.day) +
                    info.hour / 24.0 + info.minute / 1440.0
        }
        return calculateSolarTermJDFallback(year, termIndex)
    }

    private fun calculateSolarTermsFallback(year: Int): List<SolarTermResult> {
        val terms = mutableListOf<SolarTermResult>()
        for (i in 0..23) {
            val angle = i * 15.0
            val baseJD = getApproximateTermJD(year, i)
            val jd = iterateToTerm(baseJD, angle)
            val cal = jdToCalendar(jd)
            terms.add(SolarTermResult(CalendarConstants.SOLAR_TERM_NAMES[i],
                cal[0], cal[1], cal[2], cal[3], cal[4], cal[5]))
        }
        return terms
    }

    private fun calculateSolarTermJDFallback(year: Int, termIndex: Int): Double {
        val angle = termIndex * 15.0
        val baseJD = getApproximateTermJD(year, termIndex)
        return iterateToTerm(baseJD, angle)
    }

    private fun getApproximateTermJD(year: Int, termIndex: Int): Double {
        val y = year.toDouble()
        val startJD = gregorianToJD(year, 1, 1)
        val anglePerDay = 360.0 / 365.2422
        val approxDays = (termIndex * 15.0) / anglePerDay + 35.0
        return startJD + approxDays
    }

    private fun iterateToTerm(jd: Double, targetAngle: Double): Double {
        var t = (jd - CalendarConstants.J2000) / 36525.0
        var correction = 0.0
        for (i in 0 until 5) {
            val currentT = t + correction / 36525.0
            val sunLon = getSunApparentLongitude(currentT)
            var diff = targetAngle - sunLon
            while (diff > 180.0) diff -= 360.0
            while (diff < -180.0) diff += 360.0
            if (abs(diff) < 0.00001) break
            correction += diff * 365.2422 / 360.0 * 1.2
        }
        return jd + correction
    }

    private fun getSunApparentLongitude(t: Double): Double {
        val L0 = normalizeAngle360(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        val M = normalizeAngle360(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
        val C = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(radians(M)) +
                (0.019993 - 0.000101 * t) * sin(radians(2 * M)) +
                0.000289 * sin(radians(3 * M))
        val sunLon = L0 + C
        val omega = normalizeAngle360(125.04 - 1934.136 * t)
        val lambda = sunLon - 0.00569 - 0.00478 * sin(radians(omega))
        return normalizeAngle360(lambda)
    }

    fun gregorianToJD(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
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

    private fun normalizeAngle360(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun radians(deg: Double) = deg * PI / 180.0

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