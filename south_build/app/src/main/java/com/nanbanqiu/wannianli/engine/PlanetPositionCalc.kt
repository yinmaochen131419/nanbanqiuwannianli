/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import kotlin.math.*

object PlanetPositionCalc {

    val PLANET_NAMES = arrayOf("太阳", "水星", "金星", "火星", "木星", "土星")
    val PLANET_SYMBOLS = arrayOf("☉", "☿", "♀", "♂", "♃", "♄")

    data class PlanetPosition(
        val planetId: Int,
        val name: String,
        val symbol: String,
        val eclipticLon: Double,
        val eclipticLat: Double
    )

    private data class Orbit(
        val a0: Double, val e0: Double, val i0: Double,
        val node0: Double, val peri0: Double, val L0: Double,
        val aDot: Double, val eDot: Double, val iDot: Double,
        val nodeDot: Double, val periDot: Double, val LDot: Double
    )

    private val ORBITS = mapOf(
        0 to Orbit(1.00000102, 0.01670862, 0.0, 0.0, 102.937348, 100.464572,
            0.0, -0.00003804, 0.0, 0.0, 0.32298, 35999.372450),
        1 to Orbit(0.38709831, 0.20563175, 7.004986, 48.330893, 77.456119, 252.250906,
            0.0, 0.00002047, -0.005947, -0.125340, 0.159479, 149472.674636),
        2 to Orbit(0.72332982, 0.00677177, 3.394662, 76.679920, 131.602467, 181.979801,
            0.0, -0.00004250, -0.001124, -0.277422, 0.018031, 58517.803875),
        3 to Orbit(1.52368804, 0.09340457, 1.849726, 49.558093, 336.060234, 355.433000,
            0.0, 0.00009189, -0.008045, -0.295034, 0.451056, 19140.302685),
        4 to Orbit(5.20256115, 0.04849793, 1.303270, 100.464442, 14.331207, 34.351484,
            -0.00002563, 0.00003174, -0.003741, -0.165254, 0.064758, 3034.905675),
        5 to Orbit(9.55474657, 0.05550818, 2.488878, 113.665524, 93.057237, 50.077444,
            0.00000294, -0.00004933, -0.003984, -0.206316, 0.079862, 1222.114947)
    )

    fun gregorianToJD(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    fun calcSunLon(jd: Double): Double {
        val earth = calcHeliocentricEcliptic(0, jd)
        return normalizeDeg(atan2(earth.y, earth.x).toDeg() + 180.0)
    }

    fun calcPlanetLon(planetId: Int, jd: Double): Double {
        val earth = calcHeliocentricEcliptic(0, jd)
        val planet = calcHeliocentricEcliptic(planetId, jd)
        val xg = planet.x - earth.x
        val yg = planet.y - earth.y
        return normalizeRad(atan2(yg, xg)).toDeg()
    }

    fun calcPlanetLat(planetId: Int, jd: Double): Double {
        if (planetId == 0) return 0.0
        val earth = calcHeliocentricEcliptic(0, jd)
        val planet = calcHeliocentricEcliptic(planetId, jd)
        val xg = planet.x - earth.x
        val yg = planet.y - earth.y
        val zg = planet.z - earth.z
        return atan2(zg, sqrt(xg * xg + yg * yg)).toDeg()
    }

    fun allPlanetPositions(year: Int, month: Int, day: Int): List<PlanetPosition> {
        val jd = gregorianToJD(year, month, day)
        val sunLon = calcSunLon(jd)
        return listOf(
            PlanetPosition(0, PLANET_NAMES[0], PLANET_SYMBOLS[0], sunLon, 0.0),
            PlanetPosition(1, PLANET_NAMES[1], PLANET_SYMBOLS[1], calcPlanetLon(1, jd), calcPlanetLat(1, jd)),
            PlanetPosition(2, PLANET_NAMES[2], PLANET_SYMBOLS[2], calcPlanetLon(2, jd), calcPlanetLat(2, jd)),
            PlanetPosition(3, PLANET_NAMES[3], PLANET_SYMBOLS[3], calcPlanetLon(3, jd), calcPlanetLat(3, jd)),
            PlanetPosition(4, PLANET_NAMES[4], PLANET_SYMBOLS[4], calcPlanetLon(4, jd), calcPlanetLat(4, jd)),
            PlanetPosition(5, PLANET_NAMES[5], PLANET_SYMBOLS[5], calcPlanetLon(5, jd), calcPlanetLat(5, jd))
        )
    }

    private data class HelioPos(val x: Double, val y: Double, val z: Double, val r: Double)

    private fun calcHeliocentricEcliptic(planetId: Int, jd: Double): HelioPos {
        val o = ORBITS[planetId]!!
        val T = (jd - 2451545.0) / 36525.0

        val a = o.a0 + o.aDot * T
        val e = o.e0 + o.eDot * T
        val iRad = (o.i0 + o.iDot * T).toRad()
        val nodeRad = normalizeDeg(o.node0 + o.nodeDot * T).toRad()
        val periRad = normalizeDeg(o.peri0 + o.periDot * T).toRad()
        val LRad = normalizeDeg(o.L0 + o.LDot * T).toRad()

        val M = normalizeRad(LRad - periRad)
        val E = solveKepler(M, e)

        val cosE = cos(E)
        val sinE = sin(E)
        val xp = a * (cosE - e)
        val yp = a * sqrt(1.0 - e * e) * sinE
        val r = sqrt(xp * xp + yp * yp)
        val v = atan2(yp, xp)

        val omega = normalizeRad(periRad - nodeRad)
        val u = v + omega
        val cosNode = cos(nodeRad)
        val sinNode = sin(nodeRad)
        val cosI = cos(iRad)
        val sinI = sin(iRad)

        val xecl = r * (cosNode * cos(u) - sinNode * sin(u) * cosI)
        val yecl = r * (sinNode * cos(u) + cosNode * sin(u) * cosI)
        val zecl = r * sin(u) * sinI

        return HelioPos(xecl, yecl, zecl, r)
    }

    private fun solveKepler(M: Double, e: Double): Double {
        var E = M
        for (iter in 0 until 20) {
            val dE = (M + e * sin(E) - E) / (1.0 - e * cos(E))
            E += dE
            if (abs(dE) < 1e-12) break
        }
        return E
    }

    data class MoonPosition(
        val eclipticLon: Double,
        val eclipticLat: Double
    )

    fun calcMoonPosition(jd: Double): MoonPosition {
        val T = (jd - 2451545.0) / 36525.0

        val Lp = normalizeDeg(218.3164477 + 481267.88123421 * T - 0.0015786 * T * T)
        val D = normalizeDeg(297.8501921 + 445267.1114034 * T - 0.0018819 * T * T)
        val M = normalizeDeg(357.5291092 + 35999.0502909 * T - 0.0001536 * T * T)
        val Mp = normalizeDeg(134.9633964 + 477198.8675055 * T + 0.0087414 * T * T)
        val F = normalizeDeg(93.2720950 + 483202.0175233 * T - 0.0036539 * T * T)

        val toRad = { d: Double -> d * PI / 180.0 }

        var lonCorr = 0.0
        lonCorr += 6288774 * sin(toRad(Mp))
        lonCorr += 1274027 * sin(toRad(2 * D - Mp))
        lonCorr += 658314 * sin(toRad(2 * D))
        lonCorr += 213618 * sin(toRad(2 * Mp))
        lonCorr += -185116 * sin(toRad(M))
        lonCorr += -114332 * sin(toRad(2 * F))
        lonCorr += 58793 * sin(toRad(2 * D - 2 * Mp))
        lonCorr += 57066 * sin(toRad(2 * D - M - Mp))
        lonCorr += 53322 * sin(toRad(2 * D + Mp))
        lonCorr += 45758 * sin(toRad(2 * D - M))
        lonCorr += -40923 * sin(toRad(M - Mp))
        lonCorr += -34720 * sin(toRad(D))
        lonCorr += -30383 * sin(toRad(M + Mp))

        val eclipticLon = normalizeDeg(Lp + lonCorr / 1000000.0)

        var latCorr = 0.0
        latCorr += 5128122 * sin(toRad(F))
        latCorr += 280602 * sin(toRad(Mp + F))
        latCorr += 277693 * sin(toRad(Mp - F))
        latCorr += 173237 * sin(toRad(2 * D - F))
        latCorr += 55413 * sin(toRad(2 * D - Mp + F))
        latCorr += 46271 * sin(toRad(2 * D - Mp - F))
        latCorr += 32573 * sin(toRad(2 * D + F))
        latCorr += 17198 * sin(toRad(2 * Mp + F))

        val eclipticLat = latCorr / 1000000.0

        return MoonPosition(eclipticLon, eclipticLat)
    }

    private fun Double.toRad() = this * PI / 180.0
    private fun Double.toDeg() = this * 180.0 / PI
    private fun normalizeDeg(d: Double): Double = ((d % 360.0) + 360.0) % 360.0
    private fun normalizeRad(r: Double): Double = ((r % (2 * PI)) + 2 * PI) % (2 * PI)
}
