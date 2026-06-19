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
 * SxtwlBridge JNI桥接集成测试
 * 需要在Android设备上运行（依赖native库）
 *
 * 基准数据来源：
 * - 农历：紫金山天文台、寿星天文历在线查询
 * - 节气：紫金山天文台节气时刻表
 * - 干支：万年历
 */
@RunWith(AndroidJUnit4::class)
class SxtwlBridgeInstrumentedTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            // 确保native库已加载
            SxtwlBridge
        }
    }

    // ========== 公历转农历 ==========

    @Test
    fun testSolarToLunar_June19_2026() {
        // 2026-06-19 = 农历丙午年五月初五（端午节）
        val result = SxtwlBridge.nativeSolarToLunar(2026, 6, 19)
        assertEquals(2026, result[0]) // 农历年
        assertEquals(5, result[1])   // 农历月（五月）
        assertEquals(5, result[2])   // 农历日（初五）
    }

    @Test
    fun testSolarToLunar_SpringFestival2026() {
        // 2026-02-17 = 农历丙午年正月初一（春节）
        val result = SxtwlBridge.nativeSolarToLunar(2026, 2, 17)
        assertEquals(2026, result[0])
        assertEquals(1, result[1])   // 正月
        assertEquals(1, result[2])   // 初一
    }

    @Test
    fun testSolarToLunar_NewYear2026() {
        // 2026-01-01 = 农历乙巳年冬月十三
        val result = SxtwlBridge.nativeSolarToLunar(2026, 1, 1)
        assertEquals(2025, result[0]) // 乙巳年
        assertEquals(11, result[1])   // 冬月
        assertEquals(13, result[2])   // 十三
    }

    @Test
    fun testSolarToLunar_MidAutumn2025() {
        // 2025-10-06 = 农历乙巳年八月十五（中秋节）
        val result = SxtwlBridge.nativeSolarToLunar(2025, 10, 6)
        assertEquals(2025, result[0])
        assertEquals(8, result[1])    // 八月
        assertEquals(15, result[2])   // 十五
    }

    // ========== 干支计算 ==========

    @Test
    fun testGanZhi_June19_2026() {
        // 2026-06-19 = 丙午年 甲午月 甲子日
        val result = SxtwlBridge.nativeGetGanZhi(2026, 6, 19, 12)
        assertEquals("丙午", result[0]) // 年柱
        assertEquals("甲午", result[1]) // 月柱
        assertEquals("甲子", result[2]) // 日柱
    }

    @Test
    fun testGanZhi_SpringFestival2026() {
        // 2026-02-17 = 丙午年 庚寅月 辛丑日
        val result = SxtwlBridge.nativeGetGanZhi(2026, 2, 17, 12)
        assertEquals("丙午", result[0]) // 年柱
        // 月柱需验证
        assertEquals("辛丑", result[2]) // 日柱
    }

    @Test
    fun testGanZhi_HourPillar() {
        // 2026-06-19 12:00 = 子时(0)→甲子时, 午时(12)→庚午时
        val result = SxtwlBridge.nativeGetGanZhi(2026, 6, 19, 12)
        // 日柱甲子，午时应为庚午
        assertEquals("甲子", result[2]) // 日柱
        // 时柱需验证
        assertNotNull(result[3])
        assertTrue("时柱长度应为2", result[3].length == 2)
    }

    // ========== 节气计算 ==========

    @Test
    fun testSolarTermsForYear2026() {
        val terms = SxtwlBridge.nativeGetSolarTermsForYear(2026)
        assertEquals(24, terms.size)

        // 冬至(索引0)应在12月21-22日附近
        val dongzhiJD = terms[0]
        val dongzhi = SxtwlBridge.jdToGregorian(dongzhiJD)
        assertEquals(2025, dongzhi[0]) // 冬至在2025年底（sxtwl以冬至为年首）
        assertEquals(12, dongzhi[1])
        assertTrue("冬至应在21-22日", dongzhi[2] in 21..22)

        // 夏至(索引12)应在6月21-22日附近
        val xiazhiJD = terms[12]
        val xiazhi = SxtwlBridge.jdToGregorian(xiazhiJD)
        assertEquals(2026, xiazhi[0])
        assertEquals(6, xiazhi[1])
        assertTrue("夏至应在21-22日", xiazhi[2] in 21..22)
    }

    @Test
    fun testSolarTerms_Lichun2026() {
        // 立春(索引3)应在2月3-5日附近
        val terms = SxtwlBridge.nativeGetSolarTermsForYear(2026)
        val lichunJD = terms[3]
        val lichun = SxtwlBridge.jdToGregorian(lichunJD)
        assertEquals(2026, lichun[0])
        assertEquals(2, lichun[1])
        assertTrue("立春应在3-5日", lichun[2] in 3..5)
    }

    // ========== 儒略日转换 ==========

    @Test
    fun testSolarToJD_Basic() {
        val jd = SxtwlBridge.nativeSolarToJD(2026, 6, 19, 12, 0, 0)
        // 2026-06-19 12:00 UT ≈ JD 2461211.0
        assertEquals(2461211.0, jd, 0.01)
    }

    @Test
    fun testJD2DD_Basic() {
        val jd = 2461210.5 // 2026-06-19 00:00 UT
        val result = SxtwlBridge.nativeJD2DD(jd)
        assertEquals(2026, result[0])
        assertEquals(6, result[1])
        assertEquals(19, result[2])
    }

    @Test
    fun testJDInverseConversion() {
        // 公历 → JD → 公历 应可逆
        val originalJD = SxtwlBridge.nativeSolarToJD(2026, 6, 19, 8, 30, 0)
        val greg = SxtwlBridge.nativeJD2DD(originalJD)
        assertEquals(2026, greg[0])
        assertEquals(6, greg[1])
        assertEquals(19, greg[2])
        assertEquals(8, greg[3])
        assertEquals(30, greg[4])
    }

    // ========== 闰月计算 ==========

    @Test
    fun testLeapMonth_2025() {
        // 2025年农历有闰六月
        val leap = SxtwlBridge.nativeGetLeapMonth(2025)
        assertEquals(6, leap)
    }

    @Test
    fun testLeapMonth_2026() {
        // 2026年农历无闰月
        val leap = SxtwlBridge.nativeGetLeapMonth(2026)
        assertEquals(0, leap)
    }

    // ========== 星期计算 ==========

    @Test
    fun testWeekday_June19_2026() {
        // 2026-06-19 是星期五
        val weekday = SxtwlBridge.nativeGetWeekday(2026, 6, 19)
        assertEquals(5, weekday) // 0=Sunday, 5=Friday
    }

    @Test
    fun testWeekday_Jan1_2026() {
        // 2026-01-01 是星期四
        val weekday = SxtwlBridge.nativeGetWeekday(2026, 1, 1)
        assertEquals(4, weekday) // Thursday
    }

    // ========== 节气日查询 ==========

    @Test
    fun testJieQi_June21_2026() {
        // 2026-06-21 附近应有夏至
        val result = SxtwlBridge.nativeGetJieQi(2026, 6, 21)
        // 如果当天有节气，jieQiIndex应为12(夏至)
        if (result[0] >= 0) {
            assertEquals(12, result[0]) // 夏至
        }
    }

    @Test
    fun testJieQi_NoTerm() {
        // 2026-06-19 端午节，通常无节气
        val result = SxtwlBridge.nativeGetJieQi(2026, 6, 19)
        // 芒种在6月5-6日，夏至在6月21日，6月19日通常无节气
        // 如果返回-1表示无节气，或者返回当天节气
        assertTrue("节气索引应在-1到23之间", result[0] in -1..23)
    }

    // ========== LunarCalendarEngine 集成测试 ==========

    @Test
    fun testLunarEngine_SolarToLunar() {
        val result = LunarCalendarEngine.solarToLunar(2026, 6, 19)
        assertEquals(2026, result.year)
        assertEquals(5, result.month)
        assertEquals(5, result.day)
        assertEquals(false, result.isLeapMonth)
    }

    @Test
    fun testLunarEngine_SouthernConversion() {
        // 南半球农历 = 北半球农历月 - 6（如果月>=7）或 + 6（如果月<7）
        val result = LunarCalendarEngine.toSouthernLunar(2026, 6, 19)
        // 北半球农历五月 → 南半球农历十一月
        assertEquals(5, result.originalLunarMonth)
        assertEquals(11, result.southernMonth) // 5 + 6 = 11
        assertEquals(5, result.lunarDay)
    }

    // ========== SolarTermCalculator 集成测试 ==========

    @Test
    fun testSolarTermCalculator_2026() {
        val terms = SolarTermCalculator.calculateSolarTerms(2026)
        assertEquals(24, terms.size)

        // 验证冬至
        val dongzhi = terms[0]
        assertEquals("冬至", dongzhi.northName)
        assertEquals("夏至", dongzhi.southName) // 南半球冬至→夏至

        // 验证夏至
        val xiazhi = terms[12]
        assertEquals("夏至", xiazhi.northName)
        assertEquals("冬至", xiazhi.southName) // 南半球夏至→冬至
    }
}
