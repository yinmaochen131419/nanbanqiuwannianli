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

    /**
     * ELP-2000 完整版月亮位置计算
     * 使用 452+ 项摄动数据，精度远超简化版（13项）
     * 返回 Date 分点黄道坐标（度）
     */
    fun calcMoonPosition(jd: Double): MoonPosition {
        val t = (jd - 2451545.0) / 36525.0
        val lonRad = xl1Calc(0, t)
        val latRad = xl1Calc(1, t)
        val eclipticLon = normalizeDeg(lonRad * 180.0 / PI)
        val eclipticLat = latRad * 180.0 / PI
        return MoonPosition(eclipticLon, eclipticLat)
    }

    /**
     * ELP-2000 月亮坐标计算核心算法
     * 移植自寿星天文历 eph0.js 的 XL1_calc 函数
     *
     * @param zn 0=经度, 1=纬度, 2=距离
     * @param t 儒略世纪数 (J2000起算)
     * @return 经度/纬度（弧度），距离（公里）
     */
    private fun xl1Calc(zn: Int, t: Double): Double {
        val ob = Elp2000Data.XL1[zn]
        val rad = 180.0 * 3600.0 / PI  // 每弧度的角秒数 = 206264.806

        var t2 = t * t
        var t3 = t2 * t
        var t4 = t3 * t
        val t5 = t4 * t
        val tx = t - 10

        var v = 0.0

        if (zn == 0) {
            // 月球平黄经（弧度→角秒）
            v += (3.81034409 + 8399.684730072 * t - 3.319e-05 * t2 + 3.11e-08 * t3 - 2.033e-10 * t4) * rad
            // 岁差（角秒）
            v += 5028.792262 * t + 1.1124406 * t2 + 0.00007699 * t3 - 0.000023479 * t4 - 0.0000000178 * t5
            // 公元3000-5000年长期修正
            if (tx > 0) v += -0.866 + 1.43 * tx + 0.054 * tx * tx
        }

        // 时间幂次缩放
        t2 /= 1e4
        t3 /= 1e8
        t4 /= 1e8

        // 使用全部项 (n = -1 → ob[0].size)
        val n = ob[0].size

        var tn = 1.0  // t^i 系数
        for (i in ob.indices) {
            val f = ob[i]
            // 按比例计算各项数（与 eph0.js 一致）
            var nTerms = floor(n.toDouble() * f.size / ob[0].size + 0.5).toInt()
            if (i > 0) nTerms += 6
            if (nTerms >= f.size) nTerms = f.size

            var c = 0.0
            var j = 0
            while (j < nTerms) {
                c += f[j] * cos(f[j + 1] + t * f[j + 2] + t2 * f[j + 3] + t3 * f[j + 4] + t4 * f[j + 5])
                j += 6
            }
            v += c * tn
            tn *= t
        }

        // 经度和纬度：角秒→弧度
        if (zn != 2) v /= rad

        return v
    }

    private fun Double.toRad() = this * PI / 180.0
    private fun Double.toDeg() = this * 180.0 / PI
    private fun normalizeDeg(d: Double): Double = ((d % 360.0) + 360.0) % 360.0
    private fun normalizeRad(r: Double): Double = ((r % (2 * PI)) + 2 * PI) % (2 * PI)
}
