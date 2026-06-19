/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.floor

/**
 * 节气精度验证表（JVM 简化版）
 *
 * 使用纯 Kotlin 简化 VSOP87 太阳黄经算法（PlanetPositionCalc.calcSunLon）
 * 通过二分法精确求解太阳到达目标黄经的时刻，与紫金山天文台权威数据对比。
 *
 * 权威数据来源：紫金山天文台 2026 年二十四节气交节时刻
 * 数据采集：911cha.com（标注"由紫金山天文台测定"）
 *
 * 注意：本测试使用简化轨道要素（仅线性变化率），精度约 ±0.4°（≈±9小时）。
 * 生产环境使用 sxtwl_cpp（完整 VSOP87 + ELP-2000），精度达分钟级。
 * 严格精度验证请见 Android 仪器化测试 SolarTermPrecisionInstrumentedTest。
 *
 * 精度目标：与权威数据偏差 < 12 小时（43200 秒）—— 简化算法限制
 * 表格中 PASS/FAIL 判定标准与断言标准一致（43200 秒）
 */
class SolarTermPrecisionTest {

    // ========== 节气定义 ==========
    // 24 节气对应的太阳黄经（从春分=0°开始，每15°一个）
    data class SolarTermDef(
        val name: String,
        val targetLon: Double,       // 太阳目标黄经（度）
        val refYear: Int,            // 权威数据年份
        val refMonth: Int,           // 权威数据月份
        val refDay: Int,             // 权威数据日
        val refHour: Int,            // 权威数据时
        val refMinute: Int,          // 权威数据分
        val refSecond: Int           // 权威数据秒
    )

    /**
     * 2026 年二十四节气权威数据（紫金山天文台）
     * 按公历日期排列（1月~12月）
     */
    private val SOLAR_TERMS_2026 = listOf(
        SolarTermDef("小寒",  285.0, 2026, 1,  5, 16, 22, 53),
        SolarTermDef("大寒",  300.0, 2026, 1, 20,  9, 44, 39),
        SolarTermDef("立春",  315.0, 2026, 2,  4,  4,  1, 51),
        SolarTermDef("雨水",  330.0, 2026, 2, 18, 23, 51, 39),
        SolarTermDef("惊蛰",  345.0, 2026, 3,  5, 21, 58, 43),
        SolarTermDef("春分",    0.0, 2026, 3, 20, 22, 45, 42),
        SolarTermDef("清明",   15.0, 2026, 4,  5,  2, 39, 43),
        SolarTermDef("谷雨",   30.0, 2026, 4, 20,  9, 38, 51),
        SolarTermDef("立夏",   45.0, 2026, 5,  5, 19, 48, 27),
        SolarTermDef("小满",   60.0, 2026, 5, 21,  8, 36, 28),
        SolarTermDef("芒种",   75.0, 2026, 6,  5, 23, 48,  4),
        SolarTermDef("夏至",   90.0, 2026, 6, 21, 16, 24, 12),
        SolarTermDef("小暑",  105.0, 2026, 7,  7,  9, 56, 40),
        SolarTermDef("大暑",  120.0, 2026, 7, 23,  3, 12, 48),
        SolarTermDef("立秋",  135.0, 2026, 8,  7, 19, 42, 26),
        SolarTermDef("处暑",  150.0, 2026, 8, 23, 10, 18, 31),
        SolarTermDef("白露",  165.0, 2026, 9,  7, 22, 40, 59),
        SolarTermDef("秋分",  180.0, 2026, 9, 23,  8,  4, 56),
        SolarTermDef("寒露",  195.0, 2026, 10,  8, 14, 28, 59),
        SolarTermDef("霜降",  210.0, 2026, 10, 23, 17, 37, 39),
        SolarTermDef("立冬",  225.0, 2026, 11,  7, 17, 51, 46),
        SolarTermDef("小雪",  240.0, 2026, 11, 22, 15, 23,  3),
        SolarTermDef("大雪",  255.0, 2026, 12,  7, 10, 52, 14),
        SolarTermDef("冬至",  270.0, 2026, 12, 22,  4, 49, 55)
    )

    // ========== 纯 Kotlin 节气计算（VSOP87 二分法） ==========

    /**
     * 使用二分法求解太阳到达目标黄经的精确时刻
     *
     * @param targetLon 目标黄经（度，0-360）
     * @param year 年份
     * @param approxMonth 预期月份（用于缩小搜索范围）
     * @param approxDay 预期日（用于缩小搜索范围）
     * @return 儒略日（JD）
     */
    private fun findSolarTermJD(targetLon: Double, year: Int, approxMonth: Int, approxDay: Int): Double {
        // 搜索范围：预期日期 ±5 天
        val jdStart = PlanetPositionCalc.gregorianToJD(year, approxMonth, approxDay) - 5.0
        val jdEnd = PlanetPositionCalc.gregorianToJD(year, approxMonth, approxDay) + 5.0

        // 归一化目标黄经到 [0, 360)
        val target = ((targetLon % 360.0) + 360.0) % 360.0

        // 二分法：找到太阳黄经从 < target 变为 >= target 的时刻
        var lo = jdStart
        var hi = jdEnd

        for (i in 0 until 100) {
            val mid = (lo + hi) / 2.0
            val lonMid = PlanetPositionCalc.calcSunLon(mid)
            val lonLo = PlanetPositionCalc.calcSunLon(lo)

            // 判断 target 是否在 [lonLo, lonMid) 区间内（考虑环绕）
            if (angleInRange(lonLo, lonMid, target)) {
                hi = mid
            } else {
                lo = mid
            }

            if (hi - lo < 1e-10) break
        }

        return (lo + hi) / 2.0
    }

    /**
     * 判断目标角度是否在 [a, b) 区间内（考虑 0°/360° 环绕）
     */
    private fun angleInRange(a: Double, b: Double, target: Double): Boolean {
        val aMod = ((a % 360.0) + 360.0) % 360.0
        val bMod = ((b % 360.0) + 360.0) % 360.0
        val tMod = ((target % 360.0) + 360.0) % 360.0

        // 处理环绕情况
        if (aMod <= bMod) {
            return tMod >= aMod && tMod < bMod
        } else {
            // 跨越 0° 的情况
            return tMod >= aMod || tMod < bMod
        }
    }

    /**
     * JD 转公历时间（北京时间 UTC+8）
     * @return [year, month, day, hour, minute, second]
     */
    private fun jdToBeijingTime(jd: Double): IntArray {
        // JD 是 UT 时间，北京时间 = UT + 8小时
        val jdBeijing = jd + 8.0 / 24.0

        val z = floor(jdBeijing + 0.5)
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
        val month = if (e < 14) (e - 1).toInt() else (e - 13).toInt()
        val year = if (month > 2) (c - 4716).toInt() else (c - 4715).toInt()

        // 计算时分秒
        val dayFrac = jdBeijing + 0.5 - z
        val totalSec = (dayFrac * 86400.0 + 0.5).toInt()
        val hour = totalSec / 3600
        val minute = (totalSec % 3600) / 60
        val second = totalSec % 60

        return intArrayOf(year, month, day, hour, minute, second)
    }

    /**
     * 计算权威数据对应的 JD（北京时间 → UT → JD）
     */
    private fun refToJD(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Double {
        // 北京时间转 UT：减 8 小时
        val utHour = hour - 8
        val jd = PlanetPositionCalc.gregorianToJD(year, month, day)
        return jd + (utHour + minute / 60.0 + second / 3600.0) / 24.0
    }

    /**
     * 计算两个 JD 之间的时间差（秒）
     */
    private fun jdDiffSeconds(jd1: Double, jd2: Double): Double {
        return (jd1 - jd2) * 86400.0
    }

    // ========== 精度验证测试 ==========

    @Test
    fun testSolarTermPrecisionTable() {
        println()
        println("╔══════════════════════════════════════════════════════════════════════════════════════════════════╗")
        println("║                    节气精度验证表 — 2026年（紫金山天文台权威数据对比）                              ║")
        println("╠══════╦═════════════════════╦═════════════════════╦═════════════════════╦═══════════╦═════════════╣")
        println("║ 节气 ║   权威时间(北京)    ║   计算时间(北京)    ║   太阳黄经(计算)    ║  偏差(秒)  ║   结果      ║")
        println("╠══════╬═════════════════════╬═════════════════════╬═════════════════════╬═══════════╬═════════════╣")

        var maxDiff = 0.0
        var maxDiffTerm = ""
        var totalDiff = 0.0
        var passCount = 0

        for (term in SOLAR_TERMS_2026) {
            // 计算节气 JD
            val calcJD = findSolarTermJD(term.targetLon, term.refYear, term.refMonth, term.refDay)
            // 权威数据 JD
            val refJD = refToJD(term.refYear, term.refMonth, term.refDay, term.refHour, term.refMinute, term.refSecond)
            // 时间差（秒）
            val diffSec = jdDiffSeconds(calcJD, refJD)
            val absDiff = abs(diffSec)

            // 计算时间（北京时间）
            val calcTime = jdToBeijingTime(calcJD)
            // 计算太阳黄经（验证）—— 归一化到 [0, 360)
            val calcLon = ((PlanetPositionCalc.calcSunLon(calcJD) % 360.0) + 360.0) % 360.0

            // 精度判定：简化算法 < 43200秒（12小时）为通过
            val result = if (absDiff < 43200.0) "✓ PASS" else "✗ FAIL"
            if (absDiff < 43200.0) passCount++

            if (absDiff > maxDiff) {
                maxDiff = absDiff
                maxDiffTerm = term.name
            }
            totalDiff += absDiff

            println("║ ${term.name.padEnd(4)} ║ " +
                "${term.refYear}-${term.refMonth.toString().padStart(2,'0')}-${term.refDay.toString().padStart(2,'0')} " +
                "${term.refHour.toString().padStart(2,'0')}:${term.refMinute.toString().padStart(2,'0')}:${term.refSecond.toString().padStart(2,'0')} " +
                "║ ${calcTime[0]}-${calcTime[1].toString().padStart(2,'0')}-${calcTime[2].toString().padStart(2,'0')} " +
                "${calcTime[3].toString().padStart(2,'0')}:${calcTime[4].toString().padStart(2,'0')}:${calcTime[5].toString().padStart(2,'0')} " +
                "║ ${calcLon.toFixed(4).padStart(8)}° ║ " +
                "${diffSec.toInt().toString().padStart(8)} ║ $result ║")
        }

        println("╠══════╩═════════════════════╩═════════════════════╩═════════════════════╩═══════════╩═════════════╣")
        println("║ 统计：通过 ${passCount}/24 | 最大偏差 ${maxDiff.toInt()}秒 (${maxDiffTerm}) | 平均偏差 ${(totalDiff/24).toInt()}秒                          ║")
        println("╚══════════════════════════════════════════════════════════════════════════════════════════════════╝")
        println()

        // 整体精度断言：简化算法偏差应 < 12 小时（43200 秒）
        assertTrue("节气精度验证失败：最大偏差 ${maxDiff.toInt()}秒 (${maxDiffTerm})，应 < 43200秒",
            maxDiff < 43200.0)
    }

    // ========== 分季节验证 ==========

    @Test
    fun testSpringTerms() {
        // 春季节气：立春~谷雨（太阳黄经 315°~30°）
        val springTerms = SOLAR_TERMS_2026.filter { it.targetLon in listOf(315.0, 330.0, 345.0, 0.0, 15.0, 30.0) }
        verifyTerms(springTerms, "春季")
    }

    @Test
    fun testSummerTerms() {
        // 夏季节气：立夏~大暑（太阳黄经 45°~120°）
        val summerTerms = SOLAR_TERMS_2026.filter { it.targetLon in listOf(45.0, 60.0, 75.0, 90.0, 105.0, 120.0) }
        verifyTerms(summerTerms, "夏季")
    }

    @Test
    fun testAutumnTerms() {
        // 秋季节气：立秋~霜降（太阳黄经 135°~210°）
        val autumnTerms = SOLAR_TERMS_2026.filter { it.targetLon in listOf(135.0, 150.0, 165.0, 180.0, 195.0, 210.0) }
        verifyTerms(autumnTerms, "秋季")
    }

    @Test
    fun testWinterTerms() {
        // 冬季节气：立冬~大寒（太阳黄经 225°~300°）
        val winterTerms = SOLAR_TERMS_2026.filter { it.targetLon in listOf(225.0, 240.0, 255.0, 270.0, 285.0, 300.0) }
        verifyTerms(winterTerms, "冬季")
    }

    private fun verifyTerms(terms: List<SolarTermDef>, season: String) {
        var maxDiff = 0.0
        var maxTerm = ""
        for (term in terms) {
            val calcJD = findSolarTermJD(term.targetLon, term.refYear, term.refMonth, term.refDay)
            val refJD = refToJD(term.refYear, term.refMonth, term.refDay, term.refHour, term.refMinute, term.refSecond)
            val diffSec = abs(jdDiffSeconds(calcJD, refJD))
            if (diffSec > maxDiff) { maxDiff = diffSec; maxTerm = term.name }
        }
        assertTrue("${season}节气精度验证失败：最大偏差 ${maxDiff.toInt()}秒 (${maxTerm})，应 < 43200秒",
            maxDiff < 43200.0)
    }

    // ========== 关键节气验证 ==========

    @Test
    fun testVernalEquinox2026() {
        // 春分：太阳黄经 = 0°，2026-03-20 22:45:42 北京时间
        val calcJD = findSolarTermJD(0.0, 2026, 3, 20)
        val refJD = refToJD(2026, 3, 20, 22, 45, 42)
        val diffSec = abs(jdDiffSeconds(calcJD, refJD))
        println("春分 2026: 偏差 ${diffSec.toInt()} 秒 (${(diffSec/3600).toFixed(1)} 小时)")
        assertTrue("春分偏差应 < 43200秒，实际=${diffSec.toInt()}秒", diffSec < 43200.0)
    }

    @Test
    fun testSummerSolstice2026() {
        // 夏至：太阳黄经 = 90°，2026-06-21 16:24:12 北京时间
        val calcJD = findSolarTermJD(90.0, 2026, 6, 21)
        val refJD = refToJD(2026, 6, 21, 16, 24, 12)
        val diffSec = abs(jdDiffSeconds(calcJD, refJD))
        println("夏至 2026: 偏差 ${diffSec.toInt()} 秒 (${(diffSec/3600).toFixed(1)} 小时)")
        assertTrue("夏至偏差应 < 43200秒，实际=${diffSec.toInt()}秒", diffSec < 43200.0)
    }

    @Test
    fun testAutumnalEquinox2026() {
        // 秋分：太阳黄经 = 180°，2026-09-23 08:04:56 北京时间
        val calcJD = findSolarTermJD(180.0, 2026, 9, 23)
        val refJD = refToJD(2026, 9, 23, 8, 4, 56)
        val diffSec = abs(jdDiffSeconds(calcJD, refJD))
        println("秋分 2026: 偏差 ${diffSec.toInt()} 秒 (${(diffSec/3600).toFixed(1)} 小时)")
        assertTrue("秋分偏差应 < 43200秒，实际=${diffSec.toInt()}秒", diffSec < 43200.0)
    }

    @Test
    fun testWinterSolstice2026() {
        // 冬至：太阳黄经 = 270°，2026-12-22 04:49:55 北京时间
        val calcJD = findSolarTermJD(270.0, 2026, 12, 22)
        val refJD = refToJD(2026, 12, 22, 4, 49, 55)
        val diffSec = abs(jdDiffSeconds(calcJD, refJD))
        println("冬至 2026: 偏差 ${diffSec.toInt()} 秒 (${(diffSec/3600).toFixed(1)} 小时)")
        assertTrue("冬至偏差应 < 43200秒，实际=${diffSec.toInt()}秒", diffSec < 43200.0)
    }

    @Test
    fun testLiChun2026() {
        // 立春：太阳黄经 = 315°，2026-02-04 04:01:51 北京时间
        val calcJD = findSolarTermJD(315.0, 2026, 2, 4)
        val refJD = refToJD(2026, 2, 4, 4, 1, 51)
        val diffSec = abs(jdDiffSeconds(calcJD, refJD))
        println("立春 2026: 偏差 ${diffSec.toInt()} 秒 (${(diffSec/3600).toFixed(1)} 小时)")
        assertTrue("立春偏差应 < 43200秒，实际=${diffSec.toInt()}秒", diffSec < 43200.0)
    }

    // ========== 辅助：Double 格式化 ==========
    private fun Double.toFixed(decimals: Int = 2): String {
        val factor = Math.pow(10.0, decimals.toDouble())
        return (Math.round(this * factor) / factor).toString()
    }
}
