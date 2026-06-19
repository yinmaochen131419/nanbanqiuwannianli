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
 * 集成测试：农历转换链路
 * 验证 LunarCalendarEngine → SxtwlBridge → JNI 的完整数据流
 *
 * 数据流：公历日期 → SxtwlBridge.nativeSolarToLunar() → LunarCalendarEngine.solarToLunar()
 *        → LunarCalendarEngine.toSouthernLunar() → 南半球农历结果
 */
@RunWith(AndroidJUnit4::class)
class LunarConversionIntegrationTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            SxtwlBridge // 触发native库加载
        }
    }

    // ========== 链路1：公历→农历→南半球农历 ==========

    @Test
    fun testSolarToLunarToSouthern_Chain() {
        // 2026-06-19 端午节
        // 链路：SxtwlBridge.nativeSolarToLunar → LunarCalendarEngine.solarToLunar → toSouthernLunar

        // 第一步：JNI层
        val jniResult = SxtwlBridge.nativeSolarToLunar(2026, 6, 19)
        assertEquals(2026, jniResult[0])
        assertEquals(5, jniResult[1])   // 五月
        assertEquals(5, jniResult[2])   // 初五

        // 第二步：LunarCalendarEngine封装层
        val lunarResult = LunarCalendarEngine.solarToLunar(2026, 6, 19)
        assertEquals(jniResult[0], lunarResult.year)
        assertEquals(kotlin.math.abs(jniResult[1]), lunarResult.month)
        assertEquals(jniResult[2], lunarResult.day)
        assertEquals(jniResult[1] < 0, lunarResult.isLeapMonth)

        // 第三步：南半球转换
        val southern = LunarCalendarEngine.toSouthernLunar(2026, 6, 19)
        assertEquals(lunarResult.year, southern.originalLunarYear)
        assertEquals(lunarResult.month, southern.originalLunarMonth)
        assertEquals(lunarResult.day, southern.lunarDay)
        // 北半球五月 → 南半球十一月 (5+6=11)
        assertEquals(11, southern.southernMonth)
    }

    @Test
    fun testSpringFestival_Chain() {
        // 2026-02-17 春节
        val lunar = LunarCalendarEngine.solarToLunar(2026, 2, 17)
        assertEquals(1, lunar.month)  // 正月
        assertEquals(1, lunar.day)    // 初一

        val southern = LunarCalendarEngine.toSouthernLunar(2026, 2, 17)
        // 北半球正月 → 南半球七月
        assertEquals(7, southern.southernMonth)
    }

    @Test
    fun testMidAutumn_Chain() {
        // 2025-10-06 中秋节
        val lunar = LunarCalendarEngine.solarToLunar(2025, 10, 6)
        assertEquals(8, lunar.month)   // 八月
        assertEquals(15, lunar.day)    // 十五

        val southern = LunarCalendarEngine.toSouthernLunar(2025, 10, 6)
        // 北半球八月 → 南半球二月
        assertEquals(2, southern.southernMonth)
    }

    @Test
    fun testLeapMonth_Chain() {
        // 2025年有闰六月
        val leapMonth = SxtwlBridge.nativeGetLeapMonth(2025)
        assertEquals(6, leapMonth)

        // 南半球闰月 = (北半球闰月 + 6 - 1) % 12 + 1 = (6+5)%12+1 = 12
        val southLeap = if (leapMonth > 0) (leapMonth + 6 - 1) % 12 + 1 else 0
        assertEquals(12, southLeap)
    }

    @Test
    fun testSouthernLunarMonthConsistency() {
        // 验证南半球月份始终在1-12范围内
        for (month in 1..12) {
            for (day in 1..28) {
                val southern = LunarCalendarEngine.toSouthernLunar(2026, month, day)
                assertTrue("南半球月份应在1-12，实际=${southern.southernMonth}",
                    southern.southernMonth in 1..12)
            }
        }
    }

    @Test
    fun testJDConversion_Chain() {
        // 链路：SxtwlBridge.nativeSolarToJD → nativeJD2DD 应可逆
        val originalY = 2026
        val originalM = 6
        val originalD = 19
        val originalH = 14
        val originalMin = 30

        val jd = SxtwlBridge.nativeSolarToJD(originalY, originalM, originalD, originalH, originalMin, 0)
        val greg = SxtwlBridge.nativeJD2DD(jd)

        assertEquals(originalY, greg[0])
        assertEquals(originalM, greg[1])
        assertEquals(originalD, greg[2])
        assertEquals(originalH, greg[3])
        assertEquals(originalMin, greg[4])
    }
}
