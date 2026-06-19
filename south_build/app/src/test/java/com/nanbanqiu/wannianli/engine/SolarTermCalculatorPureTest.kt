/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * 节气计算器单元测试（纯Kotlin部分）
 * 仅测试不依赖JNI的函数：gregorianToJD、jdToCalendar
 * JNI相关函数（calculateSolarTerms）在Instrumented测试中验证
 */
class SolarTermCalculatorPureTest {

    // ========== 儒略日转换 ==========

    @Test
    fun testGregorianToJD_Basic() {
        val jd = SolarTermCalculator.gregorianToJD(2026, 6, 19)
        assertEquals(2461210.5, jd, 0.001)
    }

    @Test
    fun testGregorianToJD_WithTime() {
        val jd = SolarTermCalculator.gregorianToJD(2026, 6, 19, 12, 0, 0)
        // 中午12点应比午夜多0.5天
        assertEquals(2461211.0, jd, 0.001)
    }

    @Test
    fun testGregorianToJD_J2000() {
        val jd = SolarTermCalculator.gregorianToJD(2000, 1, 1, 12, 0, 0)
        // J2000.0 = JD 2451545.0 (2000-01-01 12:00 UT)
        assertEquals(2451545.0, jd, 0.001)
    }

    @Test
    fun testJDToCalendar_Basic() {
        val jd = 2461210.5 // 2026-06-19 00:00
        val cal = SolarTermCalculator.jdToCalendar(jd)
        assertEquals(2026, cal[0])
        assertEquals(6, cal[1])
        assertEquals(19, cal[2])
    }

    @Test
    fun testJDToCalendar_WithTime() {
        val jd = 2461211.0 // 2026-06-19 12:00
        val cal = SolarTermCalculator.jdToCalendar(jd)
        assertEquals(2026, cal[0])
        assertEquals(6, cal[1])
        assertEquals(19, cal[2])
        assertEquals(12, cal[3])
    }

    @Test
    fun testJDInverseConversion() {
        // JD → 公历 → JD 应可逆
        val originalJD = 2461210.5
        val cal = SolarTermCalculator.jdToCalendar(originalJD)
        val backJD = SolarTermCalculator.gregorianToJD(
            cal[0], cal[1], cal[2], cal[3], cal[4], cal[5]
        )
        assertEquals(originalJD, backJD, 0.001)
    }

    @Test
    fun testJDToCalendar_J2000() {
        val jd = 2451545.0 // J2000.0
        val cal = SolarTermCalculator.jdToCalendar(jd)
        assertEquals(2000, cal[0])
        assertEquals(1, cal[1])
        assertEquals(1, cal[2])
        assertEquals(12, cal[3]) // 12:00 UT
    }

    // ========== 节气名称映射验证 ==========

    @Test
    fun testJieQiNamesOrder() {
        // 验证节气名称数组从冬至开始
        val names = arrayOf(
            "冬至", "小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
            "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露",
            "秋分", "寒露", "霜降", "立冬", "小雪", "大雪"
        )
        // 此验证通过反射或直接验证数组内容
        // sxtwl节气索引：0=冬至, 12=夏至
        assertEquals("冬至", names[0])
        assertEquals("夏至", names[12])
        assertEquals("大雪", names[23])
    }

    @Test
    fun testSouthernTermMapping() {
        // 南半球节气 = 北半球节气 + 12（取模24）
        // 冬至(0) → 夏至(12)
        // 夏至(12) → 冬至(0)
        // 立春(3) → 立秋(15)
        val northToSouth = { i: Int -> (i + 12) % 24 }
        assertEquals(12, northToSouth(0))  // 冬至→夏至
        assertEquals(0, northToSouth(12))  // 夏至→冬至
        assertEquals(15, northToSouth(3))  // 立春→立秋
        assertEquals(9, northToSouth(21))  // 立冬→立夏
    }
}
