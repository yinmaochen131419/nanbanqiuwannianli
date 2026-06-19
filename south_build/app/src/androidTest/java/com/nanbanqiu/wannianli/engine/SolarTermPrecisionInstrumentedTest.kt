/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * 节气精度验证表 — Android 仪器化测试（严格精度）
 *
 * 使用 sxtwl_cpp 原生库（完整 VSOP87 + ELP-2000）计算节气交节时刻，
 * 与紫金山天文台权威数据对比，验证生产环境精度。
 *
 * 权威数据来源：紫金山天文台 2026 年二十四节气交节时刻
 * 数据采集：911cha.com（标注"由紫金山天文台测定"）
 *
 * 精度目标：与权威数据偏差 < 5 分钟（300 秒）
 *
 * 运行方式：需 Android 设备/模拟器
 * ./gradlew :app:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class SolarTermPrecisionInstrumentedTest {

    // ========== 权威数据（紫金山天文台 2026 年） ==========
    data class RefTerm(
        val name: String,
        val year: Int, val month: Int, val day: Int,
        val hour: Int, val minute: Int, val second: Int
    )

    /**
     * 2026 年二十四节气权威数据（按公历日期排列）
     * 来源：紫金山天文台
     */
    private val REF_TERMS_2026 = listOf(
        RefTerm("小寒", 2026, 1,  5, 16, 22, 53),
        RefTerm("大寒", 2026, 1, 20,  9, 44, 39),
        RefTerm("立春", 2026, 2,  4,  4,  1, 51),
        RefTerm("雨水", 2026, 2, 18, 23, 51, 39),
        RefTerm("惊蛰", 2026, 3,  5, 21, 58, 43),
        RefTerm("春分", 2026, 3, 20, 22, 45, 42),
        RefTerm("清明", 2026, 4,  5,  2, 39, 43),
        RefTerm("谷雨", 2026, 4, 20,  9, 38, 51),
        RefTerm("立夏", 2026, 5,  5, 19, 48, 27),
        RefTerm("小满", 2026, 5, 21,  8, 36, 28),
        RefTerm("芒种", 2026, 6,  5, 23, 48,  4),
        RefTerm("夏至", 2026, 6, 21, 16, 24, 12),
        RefTerm("小暑", 2026, 7,  7,  9, 56, 40),
        RefTerm("大暑", 2026, 7, 23,  3, 12, 48),
        RefTerm("立秋", 2026, 8,  7, 19, 42, 26),
        RefTerm("处暑", 2026, 8, 23, 10, 18, 31),
        RefTerm("白露", 2026, 9,  7, 22, 40, 59),
        RefTerm("秋分", 2026, 9, 23,  8,  4, 56),
        RefTerm("寒露", 2026, 10,  8, 14, 28, 59),
        RefTerm("霜降", 2026, 10, 23, 17, 37, 39),
        RefTerm("立冬", 2026, 11,  7, 17, 51, 46),
        RefTerm("小雪", 2026, 11, 22, 15, 23,  3),
        RefTerm("大雪", 2026, 12,  7, 10, 52, 14),
        RefTerm("冬至", 2026, 12, 22,  4, 49, 55)
    )

    // sxtwl 节气索引顺序（从冬至开始）
    private val SXTWL_NAMES = arrayOf(
        "冬至", "小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
        "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露",
        "秋分", "寒露", "霜降", "立冬", "小雪", "大雪"
    )

    /**
     * 从 sxtwl 获取 2026 年所有节气（北京时间）
     * sxtwl 返回的节气从冬至开始，所以需要合并 2026 和 2027 年的数据
     */
    private fun get2026SolarTerms(): Map<String, RefTerm> {
        val result = mutableMapOf<String, RefTerm>()

        // 2026 年节气（冬至~大雪），其中冬至是 2025-12 的
        val terms2026 = SolarTermCalculator.calculateSolarTerms(2026)
        for (i in 0 until 24) {
            val term = terms2026[i]
            // 只取 2026 日历年的节气
            if (term.year == 2026) {
                result[SXTWL_NAMES[i]] = RefTerm(
                    SXTWL_NAMES[i], term.year, term.month, term.day,
                    term.hour, term.minute, term.second
                )
            }
        }

        // 2027 年的冬至（即 2026-12 的冬至）
        val terms2027 = SolarTermCalculator.calculateSolarTerms(2027)
        val dongzhi2027 = terms2027[0]
        if (dongzhi2027.year == 2026) {
            result["冬至"] = RefTerm(
                "冬至", dongzhi2027.year, dongzhi2027.month, dongzhi2027.day,
                dongzhi2027.hour, dongzhi2027.minute, dongzhi2027.second
            )
        }

        return result
    }

    /**
     * 计算两个时间点之间的差值（秒）
     */
    private fun timeDiffSeconds(
        y1: Int, m1: Int, d1: Int, h1: Int, mi1: Int, s1: Int,
        y2: Int, m2: Int, d2: Int, h2: Int, mi2: Int, s2: Int
    ): Double {
        val jd1 = SolarTermCalculator.gregorianToJD(y1, m1, d1, h1, mi1, s1)
        val jd2 = SolarTermCalculator.gregorianToJD(y2, m2, d2, h2, mi2, s2)
        return (jd1 - jd2) * 86400.0
    }

    // ========== 精度验证测试 ==========

    @Test
    fun testSolarTermPrecisionTable() {
        val calcTerms = get2026SolarTerms()

        println()
        println("╔══════════════════════════════════════════════════════════════════════════════════════════════════╗")
        println("║          节气精度验证表（sxtwl_cpp 完整 VSOP87）— 2026年（紫金山天文台权威数据对比）                ║")
        println("╠══════╦═════════════════════╦═════════════════════╦═══════════╦═════════════════════════════════╣")
        println("║ 节气 ║   权威时间(北京)    ║   计算时间(北京)    ║  偏差(秒)  ║           结果                  ║")
        println("╠══════╬═════════════════════╬═════════════════════╬═══════════╬═════════════════════════════════╣")

        var maxDiff = 0.0
        var maxDiffTerm = ""
        var totalDiff = 0.0
        var passCount = 0

        for (ref in REF_TERMS_2026) {
            val calc = calcTerms[ref.name]
            if (calc == null) {
                println("║ ${ref.name.padEnd(4)} ║ ${formatTime(ref)} ║       (未找到)       ║    N/A    ║ ✗ FAIL — 计算数据缺失           ║")
                continue
            }

            val diffSec = timeDiffSeconds(
                calc.year, calc.month, calc.day, calc.hour, calc.minute, calc.second,
                ref.year, ref.month, ref.day, ref.hour, ref.minute, ref.second
            )
            val absDiff = abs(diffSec)

            val result = if (absDiff < 300.0) "✓ PASS" else "✗ FAIL"
            if (absDiff < 300.0) passCount++

            if (absDiff > maxDiff) {
                maxDiff = absDiff
                maxDiffTerm = ref.name
            }
            totalDiff += absDiff

            println("║ ${ref.name.padEnd(4)} ║ ${formatTime(ref)} ║ ${formatTime(calc)} ║ ${diffSec.toInt().toString().padStart(8)} ║ $result" +
                " 偏差${(absDiff/60).toInt()}分${(absDiff%60).toInt()}秒".padEnd(32) + "║")
        }

        println("╠══════╩═════════════════════╩═════════════════════╩═══════════╬═════════════════════════════════╣")
        println("║ 统计：通过 ${passCount}/24 | 最大偏差 ${maxDiff.toInt()}秒 (${maxDiffTerm}) | 平均偏差 ${(totalDiff/24).toInt()}秒".padEnd(95) + "║")
        println("╚══════════════════════════════════════════════════════════════════════════════════════════════════╝")
        println()

        assertTrue("节气精度验证失败：最大偏差 ${maxDiff.toInt()}秒 (${maxDiffTerm})，应 < 300秒",
            maxDiff < 300.0)
    }

    // ========== 关键节气验证 ==========

    @Test
    fun testLiChun() {
        // 立春：2026-02-04 04:01:51
        val calcTerms = get2026SolarTerms()
        val calc = calcTerms["立春"]!!
        val diff = abs(timeDiffSeconds(
            calc.year, calc.month, calc.day, calc.hour, calc.minute, calc.second,
            2026, 2, 4, 4, 1, 51
        ))
        println("立春 2026: 偏差 ${diff.toInt()} 秒")
        assertTrue("立春偏差应 < 300秒，实际=${diff.toInt()}秒", diff < 300.0)
    }

    @Test
    fun testChunFen() {
        // 春分：2026-03-20 22:45:42
        val calcTerms = get2026SolarTerms()
        val calc = calcTerms["春分"]!!
        val diff = abs(timeDiffSeconds(
            calc.year, calc.month, calc.day, calc.hour, calc.minute, calc.second,
            2026, 3, 20, 22, 45, 42
        ))
        println("春分 2026: 偏差 ${diff.toInt()} 秒")
        assertTrue("春分偏差应 < 300秒，实际=${diff.toInt()}秒", diff < 300.0)
    }

    @Test
    fun testXiaZhi() {
        // 夏至：2026-06-21 16:24:12
        val calcTerms = get2026SolarTerms()
        val calc = calcTerms["夏至"]!!
        val diff = abs(timeDiffSeconds(
            calc.year, calc.month, calc.day, calc.hour, calc.minute, calc.second,
            2026, 6, 21, 16, 24, 12
        ))
        println("夏至 2026: 偏差 ${diff.toInt()} 秒")
        assertTrue("夏至偏差应 < 300秒，实际=${diff.toInt()}秒", diff < 300.0)
    }

    @Test
    fun testQiuFen() {
        // 秋分：2026-09-23 08:04:56
        val calcTerms = get2026SolarTerms()
        val calc = calcTerms["秋分"]!!
        val diff = abs(timeDiffSeconds(
            calc.year, calc.month, calc.day, calc.hour, calc.minute, calc.second,
            2026, 9, 23, 8, 4, 56
        ))
        println("秋分 2026: 偏差 ${diff.toInt()} 秒")
        assertTrue("秋分偏差应 < 300秒，实际=${diff.toInt()}秒", diff < 300.0)
    }

    @Test
    fun testDongZhi() {
        // 冬至：2026-12-22 04:49:55
        val calcTerms = get2026SolarTerms()
        val calc = calcTerms["冬至"]!!
        val diff = abs(timeDiffSeconds(
            calc.year, calc.month, calc.day, calc.hour, calc.minute, calc.second,
            2026, 12, 22, 4, 49, 55
        ))
        println("冬至 2026: 偏差 ${diff.toInt()} 秒")
        assertTrue("冬至偏差应 < 300秒，实际=${diff.toInt()}秒", diff < 300.0)
    }

    // ========== 辅助函数 ==========

    private fun formatTime(t: RefTerm): String {
        return "${t.year}-${t.month.toString().padStart(2,'0')}-${t.day.toString().padStart(2,'0')} " +
               "${t.hour.toString().padStart(2,'0')}:${t.minute.toString().padStart(2,'0')}:${t.second.toString().padStart(2,'0')}"
    }
}
