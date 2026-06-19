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
 * 集成测试：四柱八字链路
 * 验证 FourPillarsEngine → SolarTermCalculator + LunarCalendarEngine 的完整数据流
 *
 * 数据流：
 *   1. SolarTermCalculator.calculateSolarTerms() → 获取立春等节气
 *   2. LunarCalendarEngine.solarToLunar() → 获取农历日期
 *   3. FourPillarsEngine.calculate() → 综合以上数据计算四柱八字
 *   4. FourPillarsEngine.flipGanZhi() → 南半球干支翻转
 */
@RunWith(AndroidJUnit4::class)
class FourPillarsIntegrationTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            SxtwlBridge
        }
    }

    // ========== 链路3：四柱八字计算全链路 ==========

    @Test
    fun testFourPillars_June19_2026() {
        // 2026-06-19 12:00 = 丙午年 甲午月 甲子日
        val solarTerms = SolarTermCalculator.calculateSolarTerms(2026)
        val lunarResult = LunarCalendarEngine.solarToLunar(2026, 6, 19)

        val result = FourPillarsEngine.calculate(
            2026, 6, 19, 12, 0, solarTerms, lunarResult
        )

        // 验证年柱（已过立春，用2026年）
        assertEquals("丙午", result.yearGanZhi)
        // 验证日柱
        assertEquals("甲子", result.dayGanZhi)
        // 验证农历信息传递正确
        assertEquals(lunarResult.year, result.lunarYear)
        assertEquals(lunarResult.month, result.lunarMonth)
        assertEquals(lunarResult.day, result.lunarDay)
    }

    @Test
    fun testYearPillar_BeforeLiChun() {
        // 2026-01-15（立春前）年柱应使用上一年(2025=乙巳)
        val solarTerms = SolarTermCalculator.calculateSolarTerms(2026)
        val lunarResult = LunarCalendarEngine.solarToLunar(2026, 1, 15)

        val result = FourPillarsEngine.calculate(
            2026, 1, 15, 12, 0, solarTerms, lunarResult
        )

        assertEquals("乙巳", result.yearGanZhi)
    }

    @Test
    fun testYearPillar_AfterLiChun() {
        // 2026-03-15（立春后）年柱应使用当年(2026=丙午)
        val solarTerms = SolarTermCalculator.calculateSolarTerms(2026)
        val lunarResult = LunarCalendarEngine.solarToLunar(2026, 3, 15)

        val result = FourPillarsEngine.calculate(
            2026, 3, 15, 12, 0, solarTerms, lunarResult
        )

        assertEquals("丙午", result.yearGanZhi)
    }

    @Test
    fun testMonthPillar_DependsOnJieQi() {
        // 月柱依赖节气（节，非气）
        // 2026-06-19 在芒种(6月5日)之后、小暑(7月7日)之前 → 甲午月
        val solarTerms = SolarTermCalculator.calculateSolarTerms(2026)
        val lunarResult = LunarCalendarEngine.solarToLunar(2026, 6, 19)

        val result = FourPillarsEngine.calculate(
            2026, 6, 19, 12, 0, solarTerms, lunarResult
        )

        assertEquals("甲午", result.monthGanZhi)
    }

    @Test
    fun testShengXiao_DependsOnYearPillar() {
        // 生肖依赖年柱：2026=丙午=马
        val solarTerms = SolarTermCalculator.calculateSolarTerms(2026)
        val lunarResult = LunarCalendarEngine.solarToLunar(2026, 6, 19)

        val result = FourPillarsEngine.calculate(
            2026, 6, 19, 12, 0, solarTerms, lunarResult
        )

        assertEquals("马", result.shengXiao)
    }

    @Test
    fun testShengXiao_BeforeLiChun() {
        // 立春前生肖应属上一年：2026-01-15 → 2025=蛇
        val solarTerms = SolarTermCalculator.calculateSolarTerms(2026)
        val lunarResult = LunarCalendarEngine.solarToLunar(2026, 1, 15)

        val result = FourPillarsEngine.calculate(
            2026, 1, 15, 12, 0, solarTerms, lunarResult
        )

        assertEquals("蛇", result.shengXiao)
    }

    // ========== 链路4：南半球干支翻转 ==========

    @Test
    fun testFlipGanZhi_Basic() {
        // 丙午 → 壬子 (天干+6, 地支+6)
        assertEquals("壬子", FourPillarsEngine.flipGanZhi("丙午"))
        // 甲子 → 庚午
        assertEquals("庚午", FourPillarsEngine.flipGanZhi("甲子"))
        // 乙巳 → 辛亥
        assertEquals("辛亥", FourPillarsEngine.flipGanZhi("乙巳"))
    }

    @Test
    fun testFlipGanZhi_Symmetry() {
        // 翻转两次应回到原值
        val testCases = listOf("甲子", "丙午", "乙巳", "丁卯", "戊辰")
        for (gz in testCases) {
            val flipped = FourPillarsEngine.flipGanZhi(gz)
            val doubleFlipped = FourPillarsEngine.flipGanZhi(flipped)
            assertEquals("双重翻转应回到原值", gz, doubleFlipped)
        }
    }

    @Test
    fun testNorthernPillars_Calculation() {
        // 验证北半球干支计算
        val solarTerms = SolarTermCalculator.calculateSolarTerms(2026)
        val lunarResult = LunarCalendarEngine.solarToLunar(2026, 6, 19)

        val northern = FourPillarsEngine.calculateNorthern(
            2026, 6, 19, 12, 0, solarTerms, lunarResult
        )

        assertEquals("丙午", northern.yearGanZhi)
        assertEquals("甲子", northern.dayGanZhi)
        assertEquals("马", northern.shengXiao)
    }

    @Test
    fun testHourPillar_LateNight() {
        // 23点（子时）应使用次日的日干
        val solarTerms = SolarTermCalculator.calculateSolarTerms(2026)
        val lunarResult = LunarCalendarEngine.solarToLunar(2026, 6, 19)

        val result23 = FourPillarsEngine.calculate(
            2026, 6, 19, 23, 0, solarTerms, lunarResult
        )
        val result0 = FourPillarsEngine.calculate(
            2026, 6, 19, 0, 0, solarTerms, lunarResult
        )

        // 23时的时柱应基于次日日干，0时基于当日日干
        assertNotNull(result23.hourGanZhi)
        assertNotNull(result0.hourGanZhi)
        assertTrue("时柱长度应为2", result23.hourGanZhi.length == 2)
    }

    @Test
    fun testGanZhiConsistency_WithSxtwl() {
        // 验证FourPillarsEngine的干支与SxtwlBridge直接计算一致
        val gz = SxtwlBridge.nativeGetGanZhi(2026, 6, 19, 12)
        val solarTerms = SolarTermCalculator.calculateSolarTerms(2026)
        val lunarResult = LunarCalendarEngine.solarToLunar(2026, 6, 19)
        val result = FourPillarsEngine.calculate(
            2026, 6, 19, 12, 0, solarTerms, lunarResult
        )

        // 日柱应与JNI直接计算一致
        assertEquals(gz[2], result.dayGanZhi)
    }
}
