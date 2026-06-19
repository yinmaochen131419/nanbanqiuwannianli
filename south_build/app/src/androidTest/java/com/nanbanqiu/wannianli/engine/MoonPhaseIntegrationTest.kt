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
 * 集成测试：月相与月建链路
 * 验证 PureLunarEngine → PlanetPositionCalc + SxtwlBridge 的完整数据流
 *
 * 数据流：
 *   1. PlanetPositionCalc.calcSunLon() + calcMoonPosition() → 月相计算
 *   2. SxtwlBridge.nativeSolarToLunar() → 月建/破日计算
 *   3. PureLunarEngine.calcMoonPhase() → 综合输出月相信息
 */
@RunWith(AndroidJUnit4::class)
class MoonPhaseIntegrationTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            SxtwlBridge
        }
    }

    // ========== 链路5：月相计算全链路 ==========

    @Test
    fun testMoonPhase_June19_2026() {
        // 2026-06-19 = 农历五月初五（端午节）
        // 月相应接近新月（农历初一是新月，初五月相为蛾眉月）
        val moonPhase = PureLunarEngine.calcMoonPhase(2026, 6, 19)

        // 月龄应约4-5天
        assertTrue("月龄应约3-6天，实际=${moonPhase.moonAge}",
            moonPhase.moonAge in 3.0..7.0)

        // 照度应小于50%（蛾眉月）
        assertTrue("照度应小于50%，实际=${moonPhase.illumination}%",
            moonPhase.illumination < 50.0)

        // 月相名称
        assertNotNull(moonPhase.phaseName)
        assertTrue("月相名称不应为空", moonPhase.phaseName.isNotEmpty())
    }

    @Test
    fun testMoonPhase_NewMoon() {
        // 农历初一应接近新月
        // 2026-06-15 = 农历五月初一
        val moonPhase = PureLunarEngine.calcMoonPhase(2026, 6, 15)
        assertTrue("初一月龄应接近0，实际=${moonPhase.moonAge}",
            moonPhase.moonAge < 2.0 || moonPhase.moonAge > 27.0)
    }

    @Test
    fun testMoonPhase_FullMoon() {
        // 农历十五应接近满月
        // 2026-06-29 = 农历五月十五
        val moonPhase = PureLunarEngine.calcMoonPhase(2026, 6, 29)
        assertTrue("十五月龄应接近15，实际=${moonPhase.moonAge}",
            moonPhase.moonAge in 13.0..17.0)
        assertTrue("十五照度应接近100%，实际=${moonPhase.illumination}%",
            moonPhase.illumination > 90.0)
    }

    // ========== 链路6：月建/破日计算 ==========

    @Test
    fun testMonthJian_June2026() {
        // 2026-06-19 农历五月 → 月建午
        // 月建地支 = (农历月 + 1) % 12 = (5+1)%12 = 6 → 午
        val moonPhase = PureLunarEngine.calcMoonPhase(2026, 6, 19)
        assertEquals("午", moonPhase.monthJianZhi)
        // 月破 = (月建+6)%12 = (6+6)%12 = 0 → 子
        assertEquals("子", moonPhase.monthPoZhi)
    }

    @Test
    fun testMonthJian_January2026() {
        // 2026-01-15 农历冬月(11月) → 月建子
        // (11+1)%12 = 0 → 子
        val moonPhase = PureLunarEngine.calcMoonPhase(2026, 1, 15)
        assertEquals("子", moonPhase.monthJianZhi)
        // 月破 = (0+6)%12 = 6 → 午
        assertEquals("午", moonPhase.monthPoZhi)
    }

    @Test
    fun testMonthJian_ConsistencyWithLunar() {
        // 验证月建与农历月一致
        for (month in 1..12) {
            val lunar = LunarCalendarEngine.solarToLunar(2026, month, 15)
            val moonPhase = PureLunarEngine.calcMoonPhase(2026, month, 15)
            val expectedJianIdx = (lunar.month + 1) % 12
            val expectedJian = arrayOf("子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥")[expectedJianIdx]
            assertEquals("农历${lunar.month}月月建应为${expectedJian}",
                expectedJian, moonPhase.monthJianZhi)
        }
    }

    @Test
    fun testMoonPhase_NextNewMoon() {
        // 下一个新月的日期应有效
        val moonPhase = PureLunarEngine.calcMoonPhase(2026, 6, 19)
        val (y, m, d) = moonPhase.nextNewMoonDate
        assertTrue("下个新月年份应合理", y in 2026..2027)
        assertTrue("下个新月月份应合理", m in 1..12)
        assertTrue("下个新月的日期应合理", d in 1..31)
        assertTrue("距下个新月天数应为正数", moonPhase.daysToNextNewMoon >= 0)
    }

    @Test
    fun testMoonPhase_NextFullMoon() {
        // 下一个满月的日期应有效
        val moonPhase = PureLunarEngine.calcMoonPhase(2026, 6, 19)
        val (y, m, d) = moonPhase.nextFullMoonDate
        assertTrue("下个满月年份应合理", y in 2026..2027)
        assertTrue("下个满月月份应合理", m in 1..12)
        assertTrue("下个满月日期应合理", d in 1..31)
        assertTrue("距下个满月天数应为正数", moonPhase.daysToNextFullMoon >= 0)
    }

    @Test
    fun testMoonPhase_PoDays() {
        // 破日列表应非空
        val moonPhase = PureLunarEngine.calcMoonPhase(2026, 6, 19)
        assertNotNull(moonPhase.monthPoDays)
        assertTrue("破日列表应非空", moonPhase.monthPoDays.isNotEmpty())
        // 破日应在1-30范围内
        for (day in moonPhase.monthPoDays) {
            assertTrue("破日应在1-30范围", day in 1..30)
        }
    }

    @Test
    fun testMoonPhase_Consistency() {
        // 连续两天的月相应连续变化
        val phase1 = PureLunarEngine.calcMoonPhase(2026, 6, 19)
        val phase2 = PureLunarEngine.calcMoonPhase(2026, 6, 20)

        // 月龄差应约1天
        val ageDiff = phase2.moonAge - phase1.moonAge
        assertTrue("连续两天月龄差应约0.8-1.2，实际=$ageDiff",
            ageDiff in 0.5..1.5 || ageDiff in -29.5..-28.5) // 后者是跨月朔的情况
    }
}
