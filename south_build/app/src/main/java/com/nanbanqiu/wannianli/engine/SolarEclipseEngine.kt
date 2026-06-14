/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import kotlin.math.*

data class EclipseEvent(
    val year: Int,
    val month: Int,
    val day: Double,
    val type: String,
    val eclipseType: String,
    val magnitude: Double,
    val xiuName: String,
    val xiuGroup: String,
    val fenYeAncient: String,
    val fenYeModern: String,
    val description: String,
    val ancientQuote: String,
    val source: String,
    val confidence: Int
)

object SolarEclipseEngine {

    private val MONTH_NAMES = arrayOf("","一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月")

    private fun norm(a: Double): Double = ((a % 360.0) + 360.0) % 360.0
    private fun dtr(d: Double): Double = d * PI / 180.0

    data class MoonPos(val lon: Double, val lat: Double, val dist: Double)

    fun calcMoonPosition(jd: Double): MoonPos {
        val T = (jd - 2451545.0) / 36525.0
        val Lp = norm(218.3164477 + 481267.88123421 * T - 0.0015786 * T * T + T * T * T / 538841.0)
        val D  = norm(297.8501921 + 445267.1114034 * T - 0.0018819 * T * T + T * T * T / 545868.0)
        val M  = norm(357.5291092 + 35999.0502909 * T - 0.0001536 * T * T)
        val Mp = norm(134.9633964 + 477198.8675055 * T + 0.0087414 * T * T + T * T * T / 69699.0)
        val F  = norm(93.2720950 + 483202.0175233 * T - 0.0036539 * T * T - T * T * T / 3526000.0)

        val Mrad = dtr(M); val MpRad = dtr(Mp); val Drad = dtr(D); val Frad = dtr(F)

        var dLon = 0.0
        dLon += 6.289 * sin(MpRad)
        dLon += 1.274 * sin(2.0 * Drad - MpRad)
        dLon += 0.658 * sin(2.0 * Drad)
        dLon += 0.214 * sin(2.0 * MpRad)
        dLon -= 0.186 * sin(Mrad)
        dLon -= 0.114 * sin(2.0 * Frad)
        dLon += 0.059 * sin(2.0 * Drad - 2.0 * MpRad)
        dLon += 0.057 * sin(2.0 * Drad - Mrad - MpRad)
        dLon += 0.053 * sin(2.0 * Drad + MpRad)
        dLon += 0.046 * sin(2.0 * Drad - Mrad)
        dLon += 0.041 * sin(MpRad - Mrad)
        dLon -= 0.035 * sin(Drad)
        dLon -= 0.031 * sin(MpRad + Mrad)

        var dLat = 0.0
        dLat += 5.128 * sin(Frad)
        dLat += 0.281 * sin(MpRad + Frad)
        dLat += 0.278 * sin(MpRad - Frad)
        dLat += 0.173 * sin(2.0 * Drad - Frad)
        dLat += 0.055 * sin(2.0 * Drad - MpRad + Frad)
        dLat += 0.046 * sin(2.0 * Drad - MpRad - Frad)

        val lon = norm(Lp + dLon)
        val lat = dLat

        return MoonPos(lon, lat, 1.0)
    }

    fun calcLunarNode(jd: Double): Double {
        val T = (jd - 2451545.0) / 36525.0
        return norm(125.0445479 - 1934.1362891 * T + 0.0020754 * T * T + T * T * T / 467441.0 - T * T * T * T / 60616000.0)
    }

    fun generateEclipseEvents(year: Int): List<EclipseEvent> {
        val events = mutableListOf<EclipseEvent>()

        val yFrac = year + 0.5
        val kApprox = ((yFrac - 2000.0) * 12.3685).toInt()

        for (k in (kApprox - 1)..(kApprox + 14)) {
            val Tk = k / 1236.85
            val JDEmean = 2451550.09765 + 29.530588853 * k + 0.0001337 * Tk * Tk
            val Mdeg = norm(2.5534 * 180.0 / PI + 29.10535669 * k)
            val Mpdeg = norm(201.5643 + 385.81693528 * k + 0.0107438 * Tk * Tk)
            val Fdeg = norm(160.7108 + 390.67050274 * k - 0.0016341 * Tk * Tk)
            val Omdeg = norm(124.7746 - 1.56375580 * k + 0.0020691 * Tk * Tk)

            val E = 1.0 - 0.002516 * Tk - 0.0000074 * Tk * Tk

            val corSolar = -0.4075 * sin(dtr(Mpdeg)) + 0.1721 * E * sin(dtr(Mdeg))
            val JDEsolar = JDEmean + corSolar

            val Fmod = norm(Fdeg)
            val gammaSolar = abs(Fmod - if (Fmod > 180.0) 180.0 else if (Fmod > 90.0 && Fmod <= 270.0) 180.0 else 0.0).let {
                if (it > 180.0) 360.0 - it else if (it > 90.0) 180.0 - it else it
            }

            val solarPossible = gammaSolar < 21.0

            if (solarPossible) {
                val P = 0.2070 * sin(dtr(Mdeg)) - 0.0074 * sin(dtr(Mpdeg)) - 0.0052 * sin(dtr(2.0 * Mdeg)) + 0.0048 * sin(dtr(2.0 * Mpdeg))
                val Q = 5.1956 - 0.0048 * cos(dtr(Mdeg)) + 0.0020 * cos(dtr(2.0 * Mdeg)) - 0.0003 * cos(dtr(2.0 * Mpdeg))
                val W = abs(cos(dtr(Fdeg)))
                val gamma = (Fdeg - if (Fdeg <= 180.0) 0.0 else 180.0).let { abs(360.0 - Fdeg).let { if (it < abs(Fdeg - 180.0)) it else abs(Fdeg - 180.0) } }
                val gammaAbs = if (gamma > 180.0) 360.0 - gamma else if (gamma > 90.0) 180.0 - gamma else gamma
                val u = 0.0059 + 0.000046 * abs(sin(dtr(Mdeg))) - 0.000027 * sin(dtr(Mpdeg))
                val gammaNorm = gammaAbs / (1.5433 + u)

                if (gammaNorm < 1.0) {
                    val jd = JDEsolar

                    val isCentral = gammaNorm < 0.9972
                    val isTotal = isCentral && (u < 0.0047 || gammaNorm < 0.95)

                    val eclipseType = when {
                        isTotal -> "日全食"
                        isCentral -> "日环食"
                        else -> "日偏食"
                    }

                    val jdDate = jdToDate(jd)
                    val ey = jdDate.first; val em = jdDate.second; val ed = jdDate.third

                    if (ey == year) {
                        val sunLon = PlanetPositionCalc.calcSunLon(jd)
                        val xiuIdx = XiuBoundary.findXiu(sunLon)
                        val xiuName = XiuBoundary.XIU_NAMES[xiuIdx]
                        val xiuGroup = XiuBoundary.GROUP_NAMES[xiuIdx / 7]
                        val fy = getFenYe(xiuIdx)
                        val (desc, quote, src) = getEclipseJudgment(xiuIdx, eclipseType, true)

                        events.add(EclipseEvent(
                            year = ey, month = em, day = ed,
                            type = "日食",
                            eclipseType = eclipseType,
                            magnitude = "%.2f".format(1.0 - gammaNorm * 0.5 + 0.5).toDouble(),
                            xiuName = xiuName, xiuGroup = xiuGroup,
                            fenYeAncient = fy.ancientName, fenYeModern = fy.modernArea,
                            description = desc, ancientQuote = quote, source = src,
                            confidence = 92
                        ))
                    }
                }
            }

            val JDEmeanL = 2451550.09765 + 29.530588853 * (k + 0.5) + 0.0001337 * Tk * Tk
            val corLunar = -0.4065 * sin(dtr(Mpdeg)) + 0.1727 * E * sin(dtr(Mdeg))
            val JDElunar = JDEmeanL + corLunar
            val FdegL = norm(Fdeg + 180.0)
            val FmodL = norm(FdegL)
            val gammaLunar = abs(FmodL - if (FmodL > 180.0) 180.0 else if (FmodL > 90.0 && FmodL <= 270.0) 180.0 else 0.0).let {
                if (it > 180.0) 360.0 - it else if (it > 90.0) 180.0 - it else it
            }

            val lunarPossible = gammaLunar < 13.0
            if (lunarPossible) {
                val gammaNormL = gammaLunar / (1.2847 + 0.0053)
                if (gammaNormL < 1.0) {
                    val jd = JDElunar
                    val jdDate = jdToDate(jd)
                    val ey = jdDate.first; val em = jdDate.second; val ed = jdDate.third

                    if (ey == year) {
                        val moonPos = calcMoonPosition(jd)
                        val xiuIdx = XiuBoundary.findXiu(moonPos.lon)
                        val xiuName = XiuBoundary.XIU_NAMES[xiuIdx]
                        val xiuGroup = XiuBoundary.GROUP_NAMES[xiuIdx / 7]
                        val fy = getFenYe(xiuIdx)

                        val isTotalE = gammaNormL < 0.5
                        val eclipseTypeL = if (isTotalE) "月全食" else "月偏食"
                        val (desc, quote, src) = getEclipseJudgment(xiuIdx, eclipseTypeL, false)

                        events.add(EclipseEvent(
                            year = ey, month = em, day = ed,
                            type = "月食",
                            eclipseType = eclipseTypeL,
                            magnitude = "%.2f".format(1.0 + (1.0 - gammaNormL) * 0.6).toDouble(),
                            xiuName = xiuName, xiuGroup = xiuGroup,
                            fenYeAncient = fy.ancientName, fenYeModern = fy.modernArea,
                            description = desc, ancientQuote = quote, source = src,
                            confidence = 88
                        ))
                    }
                }
            }
        }

        return events.sortedWith(compareBy({ it.month }, { it.day }))
    }

    private fun jdToDate(jd: Double): Triple<Int, Int, Double> {
        val jdInt = (jd + 0.5).toLong()
        val f = jd + 0.5 - jdInt
        var a = jdInt
        if (a >= 2299161L) {
            val alpha = ((a - 1867216.25) / 36524.25).toLong()
            a += 1 + alpha - alpha / 4
        }
        val b = a + 1524
        val c = ((b - 122.1) / 365.25).toLong()
        val d = (365.25 * c).toLong()
        val e = ((b - d) / 30.6001).toLong()
        val day = b - d - (30.6001 * e).toLong() + f
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        return Triple(year.toInt(), month.toInt(), day)
    }

    fun generateDecadeEvents(startYear: Int): List<EclipseEvent> {
        val all = mutableListOf<EclipseEvent>()
        for (y in startYear until startYear + 10) all.addAll(generateEclipseEvents(y))
        return all
    }

    private fun getEclipseJudgment(xiuIdx: Int, eclipseType: String, isSolar: Boolean): Triple<String, String, String> {
        val xiuName = XiuBoundary.XIU_NAMES[xiuIdx]
        val table = SOLAR_ECLIPSE_OMENS[xiuIdx]
        val mainQuote = table.first
        val volume = if (isSolar) "卷九" else "卷十七"

        val descPart = when {
            isSolar && eclipseType == "日全食" -> "大凶之兆。"
            isSolar && eclipseType == "日环食" -> "凶兆，较全食稍轻。"
            isSolar -> "有忧，但较轻。"
            eclipseType == "月全食" -> "应在大臣、后宫。"
            else -> "应在外戚、边境。"
        }

        val shortQuote = mainQuote.ifBlank { 
            if (isSolar) "日蚀在${xiuName}，其国有忧。"
            else "月蚀在${xiuName}，大臣有忧。"
        }

        val src = if (isSolar) "《开元占经》卷九·日蚀在二十八宿" else "《开元占经》卷十七·月蚀在二十八宿"

        return Triple(descPart, shortQuote, src)
    }

    private val SOLAR_ECLIPSE_OMENS = arrayOf(
        "日蚀在角，人主忧，在宫门，其国有丧。" to "",
        "日蚀在亢，朝廷有事，大臣谋，其国饥。" to "",
        "日蚀在氐，天子失德，后宫乱，天下饥。" to "",
        "日蚀在房，王者恶之，天下不宁，地动。" to "",
        "日蚀在心，天子丧，诸侯叛，主凶。" to "",
        "日蚀在尾，后宫有火，后妃有忧。" to "",
        "日蚀在箕，大风，后妃有忧，边兵起。" to "",
        "日蚀在斗，大臣诛，天下不和，五谷不成。" to "",
        "日蚀在牛，天下有大兵，五谷不熟。" to "",
        "日蚀在女，后宫有忧，丝帛贵。" to "",
        "日蚀在虚，天下大乱，有丧，王者恶之。" to "",
        "日蚀在危，天下有兵，大臣有忧，国易政。" to "",
        "日蚀在室，大臣诛，天下有兵，后宫有忧。" to "",
        "日蚀在壁，王者恶之，天下有兵，大臣死。" to "",
        "日蚀在奎，鲁国凶，天下有兵。" to "",
        "日蚀在娄，赵魏有兵，边境不宁。" to "",
        "日蚀在胃，仓廪空虚，天下饥。" to "",
        "日蚀在昴，胡兵起，边城受围。" to "",
        "日蚀在毕，边兵起，将相有忧，刑罚失中。" to "",
        "日蚀在觜，天下有兵，大臣诛。" to "",
        "日蚀在参，益州凶，天下有兵，大将死。" to "",
        "日蚀在井，益州饥，水旱相仍，人相食。" to "",
        "日蚀在鬼，国有大丧，有土功，大臣死。" to "",
        "日蚀在柳，天下大旱，五谷不登。" to "",
        "日蚀在星，女主忧，天下有兵。" to "",
        "日蚀在张，天下大旱，五谷不成。" to "",
        "日蚀在翼，天下大旱，边兵起，后妃有忧。" to "",
        "日蚀在轸，楚国凶，天下有兵，水旱为灾。" to ""
    )
}