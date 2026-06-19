/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import kotlin.math.*

object PureLunarEngine {

    private const val SYNODIC_MONTH = 29.530588

    private val DI_ZHI = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

    data class MoonPhaseInfo(
        val moonAge: Double,
        val illumination: Double,
        val phaseName: String,
        val phaseIndex: Int,
        val daysToNextNewMoon: Int,
        val daysToNextFullMoon: Int,
        val nextNewMoonDate: Triple<Int, Int, Int>,
        val nextFullMoonDate: Triple<Int, Int, Int>,
        val isFirstQuarter: Boolean,
        val isLastQuarter: Boolean,
        val monthJianZhi: String,
        val monthPoZhi: String,
        val monthPoDays: List<Int>
    )

    fun calcMoonPhase(year: Int, month: Int, day: Int): MoonPhaseInfo {
        val jd = PlanetPositionCalc.gregorianToJD(year, month, day)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        val moonPos = PlanetPositionCalc.calcMoonPosition(jd)

        val elongation = normalize360(moonPos.eclipticLon - sunLon)
        val moonAge = elongation / 360.0 * SYNODIC_MONTH
        val illumination = (1.0 - cos(elongation * PI / 180.0)) / 2.0 * 100.0

        val phaseIndex = determinePhaseIndex(elongation)
        val phaseName = PHASE_NAMES[phaseIndex]
        val isFirstQuarter = phaseIndex == 2
        val isLastQuarter = phaseIndex == 6

        val nextNewMoon = findNextNewMoon(year, month, day)
        val nextFullMoon = findNextFullMoon(year, month, day)
        val daysToNewMoon = daysBetween(year, month, day, nextNewMoon.first, nextNewMoon.second, nextNewMoon.third)
        val daysToFullMoon = daysBetween(year, month, day, nextFullMoon.first, nextFullMoon.second, nextFullMoon.third)

        val lunarRes = SxtwlBridge.nativeSolarToLunar(year, month, day)
        val lunarMonth = abs(lunarRes[1])
        val jianIdx = (lunarMonth + 1) % 12
        val monthJianZhi = DI_ZHI[jianIdx]
        val monthPoZhi = DI_ZHI[(jianIdx + 6) % 12]

        val (_, poDays) = findLunarMonthInfo(year, month, day, lunarMonth, lunarRes[0], monthPoZhi)

        return MoonPhaseInfo(
            moonAge = round(moonAge * 10.0) / 10.0,
            illumination = round(illumination * 10.0) / 10.0,
            phaseName = phaseName,
            phaseIndex = phaseIndex,
            daysToNextNewMoon = daysToNewMoon,
            daysToNextFullMoon = daysToFullMoon,
            nextNewMoonDate = nextNewMoon,
            nextFullMoonDate = nextFullMoon,
            isFirstQuarter = isFirstQuarter,
            isLastQuarter = isLastQuarter,
            monthJianZhi = monthJianZhi,
            monthPoZhi = monthPoZhi,
            monthPoDays = poDays
        )
    }

    private val PHASE_NAMES = arrayOf(
        "新月", "蛾眉月", "上弦月", "盈凸月",
        "满月", "亏凸月", "下弦月", "残月"
    )

    private fun determinePhaseIndex(elongation: Double): Int {
        return when {
            elongation < 11.25 || elongation >= 348.75 -> 0
            elongation < 78.75 -> 1
            elongation < 101.25 -> 2
            elongation < 168.75 -> 3
            elongation < 191.25 -> 4
            elongation < 258.75 -> 5
            elongation < 281.25 -> 6
            else -> 7
        }
    }

    private fun findNextNewMoon(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        val baseJd = PlanetPositionCalc.gregorianToJD(year, month, day)
        val baseSunLon = PlanetPositionCalc.calcSunLon(baseJd)
        val baseMoon = PlanetPositionCalc.calcMoonPosition(baseJd)
        val baseElongation = normalize360(baseMoon.eclipticLon - baseSunLon)

        for (offset in 1..35) {
            val checkJd = baseJd + offset.toDouble()
            val sunLon = PlanetPositionCalc.calcSunLon(checkJd)
            val moonPos = PlanetPositionCalc.calcMoonPosition(checkJd)
            val el = normalize360(moonPos.eclipticLon - sunLon)

            val prevEl = if (offset > 1) {
                val prevJd = checkJd - 1.0
                val prevSun = PlanetPositionCalc.calcSunLon(prevJd)
                val prevMoon = PlanetPositionCalc.calcMoonPosition(prevJd)
                normalize360(prevMoon.eclipticLon - prevSun)
            } else {
                baseElongation
            }

            if (prevEl > 180.0 && el < 180.0) {
                return jdToDate(checkJd)
            }
            if (el < 5.0) {
                return jdToDate(checkJd)
            }
        }
        return Triple(year, month, day)
    }

    private fun findNextFullMoon(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        val baseJd = PlanetPositionCalc.gregorianToJD(year, month, day)
        val baseSunLon = PlanetPositionCalc.calcSunLon(baseJd)
        val baseMoon = PlanetPositionCalc.calcMoonPosition(baseJd)
        val baseElongation = normalize360(baseMoon.eclipticLon - baseSunLon)

        for (offset in 1..35) {
            val checkJd = baseJd + offset.toDouble()
            val sunLon = PlanetPositionCalc.calcSunLon(checkJd)
            val moonPos = PlanetPositionCalc.calcMoonPosition(checkJd)
            val el = normalize360(moonPos.eclipticLon - sunLon)

            val prevEl = if (offset > 1) {
                val prevJd = checkJd - 1.0
                val prevSun = PlanetPositionCalc.calcSunLon(prevJd)
                val prevMoon = PlanetPositionCalc.calcMoonPosition(prevJd)
                normalize360(prevMoon.eclipticLon - prevSun)
            } else {
                baseElongation
            }

            if (prevEl < 180.0 && el > 180.0) {
                return jdToDate(checkJd)
            }
        }
        return Triple(year, month, day)
    }

    private fun jdToDate(jd: Double): Triple<Int, Int, Int> {
        val jdInt = jd + 0.5
        val z = floor(jdInt).toInt()
        val f = jdInt - z
        var a = z
        if (a >= 2299161) {
            val alpha = floor((a - 1867216.25) / 36524.25).toInt()
            a += 1 + alpha - alpha / 4
        }
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toInt()
        val d = floor(365.25 * c).toInt()
        val e = floor((b - d) / 30.6001).toInt()

        val day = b - d - floor(30.6001 * e).toInt() + f.toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715

        return Triple(year, month, day)
    }

    private fun daysBetween(y1: Int, m1: Int, d1: Int, y2: Int, m2: Int, d2: Int): Int {
        val jd1 = PlanetPositionCalc.gregorianToJD(y1, m1, d1)
        val jd2 = PlanetPositionCalc.gregorianToJD(y2, m2, d2)
        return (jd2 - jd1).toInt()
    }

    private fun findLunarMonthInfo(solarYear: Int, solarMonth: Int, solarDay: Int, targetLunarMonth: Int, targetLunarYear: Int, monthPoZhi: String): Pair<Int, List<Int>> {
        var y = solarYear
        var m = solarMonth
        var d = solarDay
        while (true) {
            val check = SxtwlBridge.nativeSolarToLunar(y, m, d)
            if (abs(check[1]) == targetLunarMonth && check[2] == 1 && check[0] == targetLunarYear) break
            d--
            if (d < 1) {
                m--
                if (m < 1) { y--; m = 12 }
                d = solarDaysInMonth(y, m)
            }
        }

        val poDays = mutableListOf<Int>()
        var dayCount = 0
        var cy = y; var cm = m; var cd = d
        while (true) {
            val check = SxtwlBridge.nativeSolarToLunar(cy, cm, cd)
            if (abs(check[1]) != targetLunarMonth && dayCount > 0) break
            dayCount++
            val gz = SxtwlBridge.nativeGetGanZhi(cy, cm, cd, 12)
            val dayZhi = gz[2].substring(1)
            if (dayZhi == monthPoZhi && abs(check[1]) == targetLunarMonth) {
                poDays.add(check[2])
            }
            cd++
            if (cd > solarDaysInMonth(cy, cm)) {
                cd = 1
                cm++
                if (cm > 12) { cy++; cm = 1 }
            }
            if (dayCount > 32) break
        }

        val maxLunarDay = dayCount
        return Pair(maxLunarDay, poDays)
    }

    private fun solarDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> 30
        }
    }

    private fun normalize360(d: Double): Double = ((d % 360.0) + 360.0) % 360.0
}