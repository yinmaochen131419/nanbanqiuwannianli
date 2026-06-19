/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 集成测试：节气计算链路
 * 验证 SolarTermCalculator → SxtwlBridge.nativeGetSolarTermsForYear → JD转换 的完整数据流
 *
 * 数据流：年份 → SxtwlBridge.nativeGetSolarTermsForYear() → DoubleArray[24] JD
 *        → SxtwlBridge.jdToGregorian() → SolarTermCalculator.SolarTermResult
 *        → 南北半球节气名称映射
 */
@RunWith(AndroidJUnit4::class)
class SolarTermIntegrationTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            SxtwlBridge
        }
    }

    // ========== 链路2：节气计算全链路 ==========

    @Test
    fun testSolarTermsForYear2026_Chain() {
        // 链路：JNI返回JD → jdToGregorian转换 → SolarTermResult封装
        val jdValues = SxtwlBridge.nativeGetSolarTermsForYear(2026)
        assertEquals(24, jdValues.size)

        // 验证每个JD都能正确转换为公历日期
        for (i in 0 until 24) {
            val greg = SxtwlBridge.jdToGregorian(jdValues[i])
            assertTrue("节气${i}年份应合理", greg[0] in 2025..2027)
            assertTrue("节气${i}月份应合理", greg[1] in 1..12)
            assertTrue("节气${i}日期应合理", greg[2] in 1..31)
        }
    }

    @Test
    fun testSolarTermCalculator_FullChain() {
        // 完整链路：SolarTermCalculator.calculateSolarTerms
        val terms = SolarTermCalculator.calculateSolarTerms(2026)
        assertEquals(24, terms.size)

        // 验证节气顺序：索引0=冬至，索引12=夏至
        assertEquals("冬至", terms[0].northName)
        assertEquals("夏至", terms[12].northName)

        // 验证南半球映射：冬至→夏至
        assertEquals("夏至", terms[0].southName)
        assertEquals("冬至", terms[12].southName)

        // 验证立春在2月
        val lichun = terms[3]
        assertEquals("立春", lichun.northName)
        assertEquals("立秋", lichun.southName)
        assertEquals(2026, lichun.year)
        assertEquals(2, lichun.month)
        assertTrue("立春应在3-5日", lichun.day in 3..5)
    }

    @Test
    fun testWinterSolstice2026() {
        // 冬至应在12月21-22日
        val terms = SolarTermCalculator.calculateSolarTerms(2026)
        val dongzhi = terms[0] // 冬至
        // sxtwl以冬至为年首，冬至可能在2025年底
        assertEquals("冬至", dongzhi.northName)
        assertTrue("冬至月份应为12", dongzhi.month == 12)
        assertTrue("冬至应在21-22日", dongzhi.day in 21..22)
    }

    @Test
    fun testSummerSolstice2026() {
        // 夏至应在6月21-22日
        val terms = SolarTermCalculator.calculateSolarTerms(2026)
        val xiazhi = terms[12] // 夏至
        assertEquals("夏至", xiazhi.northName)
        assertEquals(6, xiazhi.month)
        assertTrue("夏至应在21-22日", xiazhi.day in 21..22)
    }

    @Test
    fun testSolarTermSequence() {
        // 验证24节气按时间顺序排列
        val terms = SolarTermCalculator.calculateSolarTerms(2026)

        // 冬至(0)应在最前面，时间最早
        val dongzhiJD = SxtwlBridge.nativeGetSolarTermsForYear(2026)[0]
        val xiazhiJD = SxtwlBridge.nativeGetSolarTermsForYear(2026)[12]

        // 冬至JD应小于夏至JD（冬至在前）
        assertTrue("冬至JD应小于夏至JD", dongzhiJD < xiazhiJD)
    }

    @Test
    fun testSouthernTermMapping_Consistency() {
        // 验证南北半球节气对称性
        val terms = SolarTermCalculator.calculateSolarTerms(2026)

        for (i in 0 until 24) {
            val northName = terms[i].northName
            val southName = terms[i].southName
            // 南半球节气 = 北半球节气 + 12（取模24）
            val expectedSouthIndex = (i + 12) % 24
            val expectedSouthName = terms[expectedSouthIndex].northName
            assertEquals("南半球节气名称应对称", expectedSouthName, southName)
        }
    }

    @Test
    fun testSolarTermPrecision() {
        // 验证节气计算精度（寿星天文历应精确到分钟级）
        val terms = SolarTermCalculator.calculateSolarTerms(2026)
        for (term in terms) {
            // 节气时刻的小时和分钟应该是有效的
            assertTrue("${term.northName}小时应有效", term.hour in 0..23)
            assertTrue("${term.northName}分钟应有效", term.minute in 0..59)
        }
    }
}
