/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import com.nlf.calendar.Solar

import kotlin.math.*

object PureLunarCalendarEngine {

    private val MONTH_NAMES = arrayOf(

        "正月", "二月", "三月", "四月", "五月", "六月",

        "七月", "八月", "九月", "十月", "十一月", "十二月"

    )

    private val DAY_NAMES = arrayOf(

        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",

        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",

        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"

    )

    private val PHASE_NAMES = arrayOf(

        "新月", "蛾眉月", "上弦月", "盈凸月",

        "满月", "亏凸月", "下弦月", "残月"

    )

    data class PureLunarMonth(

        val monthNumber: Int,

        val monthName: String,

        val startGregorianDate: Triple<Int, Int, Int>,

        val endGregorianDate: Triple<Int, Int, Int>,

        val dayCount: Int

    )

    data class PureLunarDate(

        val year: Int,

        val month: Int,

        val day: Int,

        val monthName: String,

        val dayName: String,

        val isNewMoon: Boolean,

        val isFullMoon: Boolean,

        val moonPhase: String

    )

    fun getPureLunarYearMonths(year: Int): List<PureLunarMonth> {

        val liQiu = findLiQiu(year)

        val liQiuJd = gregorianToJD(liQiu.first, liQiu.second, liQiu.third)

        val firstNewMoonJd = findNewMoonAtOrAfter(liQiuJd)

        val newMoonJds = mutableListOf<Double>()

        newMoonJds.add(firstNewMoonJd)

        var currentJd = firstNewMoonJd + 1.0

        while (newMoonJds.size < 13) {

            val nextJd = findNextNewMoon(currentJd)

            newMoonJds.add(nextJd)

            currentJd = nextJd + 1.0

        }

        val months = mutableListOf<PureLunarMonth>()

        for (i in 0 until 12) {

            val startJd = newMoonJds[i]

            val nextJd = newMoonJds[i + 1]

            val startDate = jdToCalendarDate(startJd)

            val nextStartDate = jdToCalendarDate(nextJd)

            val endDate = jdToCalendarDate(nextJd - 1.0)

            val dayCount = daysBetween(startDate, nextStartDate)

            months.add(

                PureLunarMonth(

                    monthNumber = i + 1,

                    monthName = MONTH_NAMES[i],

                    startGregorianDate = startDate,

                    endGregorianDate = endDate,

                    dayCount = dayCount

                )

            )

        }

        return months

    }

    fun getPureLunarDate(year: Int, month: Int, day: Int): PureLunarDate? {

        val targetJd = gregorianToJD(year, month, day)

        for (tryYear in listOf(year, year - 1)) {

            val months = getPureLunarYearMonths(tryYear)

            for (m in months) {

                val startJd = gregorianToJD(

                    m.startGregorianDate.first,

                    m.startGregorianDate.second,

                    m.startGregorianDate.third

                )

                val endJd = gregorianToJD(

                    m.endGregorianDate.first,

                    m.endGregorianDate.second,

                    m.endGregorianDate.third

                ) + 1.0

                if (targetJd >= startJd - 0.5 && targetJd < endJd - 0.5) {

                    val dayInMonth = (targetJd - startJd).toInt() + 1

                    val sunLon = calcSunLon(targetJd)

                    val moonPos = calcMoonPosition(targetJd)

                    val elongation = normalize360(moonPos.first - sunLon)

                    val isNewMoon = dayInMonth == 1

                    val isFullMoon = elongation > 165.0 && elongation < 195.0

                    val phaseName = getPhaseName(elongation)

                    return PureLunarDate(

                        year = tryYear,

                        month = m.monthNumber,

                        day = dayInMonth,

                        monthName = m.monthName,

                        dayName = DAY_NAMES.getOrElse(dayInMonth - 1) { "${dayInMonth}" },

                        isNewMoon = isNewMoon,

                        isFullMoon = isFullMoon,

                        moonPhase = phaseName

                    )

                }

            }

        }

        return null

    }

    private fun findLiQiu(year: Int): Triple<Int, Int, Int> {

        val jieQiTable = Solar.fromYmd(year, 7, 1).lunar.jieQiTable

        val solar = jieQiTable["立秋"]

        if (solar != null) {

            return Triple(solar.year, solar.month, solar.day)

        }

        return Triple(year, 8, 7)

    }

    private fun findNewMoonAtOrAfter(jd: Double): Double {

        val sunLon = calcSunLon(jd)

        val moonPos = calcMoonPosition(jd)

        val el = normalize360(moonPos.first - sunLon)

        if (el < 15.0) {

            return jd

        }

        var prevJd = jd - 1.0

        var prevEl = normalize360(calcMoonPosition(prevJd).first - calcSunLon(prevJd))

        for (offset in 0..40) {

            val checkJd = jd + offset

            val checkSun = calcSunLon(checkJd)

            val checkMoon = calcMoonPosition(checkJd)

            val checkEl = normalize360(checkMoon.first - checkSun)

            if (prevEl > 300.0 && checkEl < 60.0) {

                val frac = (360.0 - prevEl) / (360.0 - prevEl + checkEl)

                return prevJd + frac

            }

            if (checkEl < 5.0 && offset > 0) {

                return checkJd

            }

            prevJd = checkJd

            prevEl = checkEl

        }

        return jd + 15.0

    }

    private fun findNextNewMoon(fromJd: Double): Double {

        var prevJd = fromJd - 1.0

        var prevEl = normalize360(calcMoonPosition(prevJd).first - calcSunLon(prevJd))

        for (offset in 0..40) {

            val checkJd = fromJd + offset

            val checkSun = calcSunLon(checkJd)

            val checkMoon = calcMoonPosition(checkJd)

            val checkEl = normalize360(checkMoon.first - checkSun)

            if (prevEl > 300.0 && checkEl < 60.0) {

                val frac = (360.0 - prevEl) / (360.0 - prevEl + checkEl)

                return prevJd + frac

            }

            if (checkEl < 5.0 && offset > 0) {

                return checkJd

            }

            prevJd = checkJd

            prevEl = checkEl

        }

        return fromJd + 30.0

    }

    private fun getPhaseName(elongation: Double): String {

        return when {

            elongation < 11.25 || elongation >= 348.75 -> PHASE_NAMES[0]

            elongation < 78.75 -> PHASE_NAMES[1]

            elongation < 101.25 -> PHASE_NAMES[2]

            elongation < 168.75 -> PHASE_NAMES[3]

            elongation < 191.25 -> PHASE_NAMES[4]

            elongation < 258.75 -> PHASE_NAMES[5]

            elongation < 281.25 -> PHASE_NAMES[6]

            else -> PHASE_NAMES[7]

        }

    }

    private fun daysBetween(

        start: Triple<Int, Int, Int>,

        end: Triple<Int, Int, Int>

    ): Int {

        val jd1 = gregorianToJD(start.first, start.second, start.third)

        val jd2 = gregorianToJD(end.first, end.second, end.third)

        return (jd2 - jd1).toInt()

    }

    private fun gregorianToJD(year: Int, month: Int, day: Int): Double {

        var y = year

        var m = month

        if (m <= 2) { y -= 1; m += 12 }

        val a = floor(y / 100.0)

        val b = 2 - a + floor(a / 4.0)

        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

    }

    private fun jdToCalendarDate(jd: Double): Triple<Int, Int, Int> {

        val jdInt = jd + 0.5

        val z = floor(jdInt).toInt()

        var a = z

        if (a >= 2299161) {

            val alpha = floor((a - 1867216.25) / 36524.25).toInt()

            a += 1 + alpha - alpha / 4

        }

        val b = a + 1524

        val c = floor((b - 122.1) / 365.25).toInt()

        val d = floor(365.25 * c).toInt()

        val e = floor((b - d) / 30.6001).toInt()

        val day = (b - d - floor(30.6001 * e).toInt()).toInt()

        val month = if (e < 14) e - 1 else e - 13

        val year = if (month > 2) c - 4716 else c - 4715

        return Triple(year, month, day)

    }

    fun calcSunLon(jd: Double): Double {

        val T = (jd - 2451545.0) / 36525.0

        val L0 = normalizeDeg(280.46646 + 36000.76983 * T + 0.0003032 * T * T)

        val M = normalizeDeg(357.52911 + 35999.05029 * T - 0.0001537 * T * T)

        val C = (1.914602 - 0.004817 * T - 0.000014 * T * T) * sin(M.toRad()) +

                (0.019993 - 0.000101 * T) * sin(2 * M.toRad()) +

                0.000289 * sin(3 * M.toRad())

        return normalizeDeg(L0 + C)

    }

    fun calcMoonPosition(jd: Double): Pair<Double, Double> {

        val T = (jd - 2451545.0) / 36525.0

        val Lp = normalizeDeg(218.3164477 + 481267.88123421 * T - 0.0015786 * T * T)

        val D = normalizeDeg(297.8501921 + 445267.1114034 * T - 0.0018819 * T * T)

        val M = normalizeDeg(357.5291092 + 35999.0502909 * T - 0.0001536 * T * T)

        val Mp = normalizeDeg(134.9633964 + 477198.8675055 * T + 0.0087414 * T * T)

        val F = normalizeDeg(93.2720950 + 483202.0175233 * T - 0.0036539 * T * T)

        var lonCorr = 0.0

        lonCorr += 6288774 * sin(Mp.toRad())

        lonCorr += 1274027 * sin((2 * D - Mp).toRad())

        lonCorr += 658314 * sin((2 * D).toRad())

        lonCorr += 213618 * sin((2 * Mp).toRad())

        lonCorr += -185116 * sin(M.toRad())

        lonCorr += -114332 * sin((2 * F).toRad())

        lonCorr += 58793 * sin((2 * D - 2 * Mp).toRad())

        lonCorr += 57066 * sin((2 * D - M - Mp).toRad())

        lonCorr += 53322 * sin((2 * D + Mp).toRad())

        lonCorr += 45758 * sin((2 * D - M).toRad())

        lonCorr += -40923 * sin((M - Mp).toRad())

        lonCorr += -34720 * sin(D.toRad())

        lonCorr += -30383 * sin((M + Mp).toRad())

        val eclipticLon = normalizeDeg(Lp + lonCorr / 1000000.0)

        var latCorr = 0.0

        latCorr += 5128122 * sin(F.toRad())

        latCorr += 280602 * sin((Mp + F).toRad())

        latCorr += 277693 * sin((Mp - F).toRad())

        latCorr += 173237 * sin((2 * D - F).toRad())

        latCorr += 55413 * sin((2 * D - Mp + F).toRad())

        latCorr += 46271 * sin((2 * D - Mp - F).toRad())

        latCorr += 32573 * sin((2 * D + F).toRad())

        latCorr += 17198 * sin((2 * Mp + F).toRad())

        val eclipticLat = latCorr / 1000000.0

        return Pair(eclipticLon, eclipticLat)

    }

    private fun Double.toRad(): Double = this * PI / 180.0

    private fun normalizeDeg(d: Double): Double = ((d % 360.0) + 360.0) % 360.0

    private fun normalize360(d: Double): Double = ((d % 360.0) + 360.0) % 360.0

}
