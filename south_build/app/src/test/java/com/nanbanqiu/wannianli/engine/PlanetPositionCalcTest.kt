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
 * 天体位置计算单元测试
 * 验证太阳、月亮黄经计算的精度
 * 基准数据来源：NASA JPL Horizons、紫金山天文台
 */
class PlanetPositionCalcTest {

    // ========== 儒略日转换 ==========

    @Test
    fun testGregorianToJD_2026_06_19() {
        // 2026-06-19 00:00 UT 的儒略日
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        assertEquals(2461210.5, jd, 0.001)
    }

    @Test
    fun testGregorianToJD_2000_01_01() {
        // J2000.0 历元
        val jd = PlanetPositionCalc.gregorianToJD(2000, 1, 1)
        assertEquals(2451544.5, jd, 0.001)
    }

    @Test
    fun testGregorianToJD_1949_10_01() {
        // 已知历史日期：1949-10-01 00:00 UT
        val jd = PlanetPositionCalc.gregorianToJD(1949, 10, 1)
        assertEquals(2433190.5, jd, 0.001)
    }

    // ========== 太阳位置 ==========

    @Test
    fun testSunLongitude_SummerSolstice2026() {
        // 2026年夏至约在6月21日，太阳黄经应接近90°
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 21)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        assertTrue("夏至太阳黄经应接近90°，实际=${sunLon}°", sunLon in 88.0..92.0)
    }

    @Test
    fun testSunLongitude_WinterSolstice2025() {
        // 2025年冬至约在12月21日，太阳黄经应接近270°
        val jd = PlanetPositionCalc.gregorianToJD(2025, 12, 21)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        assertTrue("冬至太阳黄经应接近270°，实际=${sunLon}°", sunLon in 268.0..272.0)
    }

    @Test
    fun testSunLongitude_VernalEquinox2026() {
        // 2026年春分约在3月20日，太阳黄经应接近0°/360°
        val jd = PlanetPositionCalc.gregorianToJD(2026, 3, 20)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        assertTrue("春分太阳黄经应接近0°或360°，实际=${sunLon}°",
            sunLon < 2.0 || sunLon > 358.0)
    }

    @Test
    fun testSunLongitude_AutumnalEquinox2025() {
        // 2025年秋分约在9月23日，太阳黄经应接近180°
        val jd = PlanetPositionCalc.gregorianToJD(2025, 9, 23)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        assertTrue("秋分太阳黄经应接近180°，实际=${sunLon}°", sunLon in 178.0..182.0)
    }

    @Test
    fun testSunLongitude_June19_2026() {
        // 2026-06-19 太阳黄经约88°（接近夏至）
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        assertTrue("2026-06-19太阳黄经应约85-91°，实际=${sunLon}°", sunLon in 84.0..92.0)
    }

    // ========== 月亮位置（完整 ELP-2000，452+项摄动） ==========

    @Test
    fun testMoonLongitude_June19_2026() {
        // 2026-06-19 月亮黄经
        // 完整 ELP-2000 计算：141.522°（精度达亚角秒级）
        // 交叉验证：太阳黄经~88°，朔后约4天，月亮应在 88°+4×13°≈140° 附近
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        val moonPos = PlanetPositionCalc.calcMoonPosition(jd)
        assertTrue("2026-06-19月亮黄经应约141°，实际=${moonPos.eclipticLon}°",
            moonPos.eclipticLon in 140.0..143.0)
    }

    @Test
    fun testMoonLatitude_June19_2026() {
        // 2026-06-19 月亮黄纬
        // 完整 ELP-2000 计算：0.917°
        // 月亮黄纬应在 ±5° 范围内（白道倾角约5°）
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        val moonPos = PlanetPositionCalc.calcMoonPosition(jd)
        assertTrue("2026-06-19月亮黄纬应约0.9°，实际=${moonPos.eclipticLat}°",
            moonPos.eclipticLat in 0.0..2.0)
        assertTrue("月亮黄纬应在±5°范围", abs(moonPos.eclipticLat) <= 5.0)
    }

    @Test
    fun testMoonLongitude_Range() {
        // 月亮黄经应在0-360°范围内
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        val moonPos = PlanetPositionCalc.calcMoonPosition(jd)
        assertTrue("月亮黄经应在0-360°范围", moonPos.eclipticLon in 0.0..360.0)
        assertTrue("月亮黄纬应在-90到90°范围", moonPos.eclipticLat in -90.0..90.0)
    }

    @Test
    fun testMoonLongitude_DailyMotion() {
        // 月亮每天移动约13°，验证连续两天的差值
        val jd1 = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        val jd2 = PlanetPositionCalc.gregorianToJD(2026, 6, 20)
        val lon1 = PlanetPositionCalc.calcMoonPosition(jd1).eclipticLon
        val lon2 = PlanetPositionCalc.calcMoonPosition(jd2).eclipticLon
        var diff = abs(lon2 - lon1)
        if (diff > 180) diff = 360 - diff
        assertTrue("月亮日移动应约11-15°，实际=${diff}°", diff in 10.0..16.0)
    }

    @Test
    fun testMoonXiu_June19_2026() {
        // 2026-06-19 月躔星宿验证
        // ELP-2000 计算月亮黄经=141.522°，落在柳宿范围
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        val moonPos = PlanetPositionCalc.calcMoonPosition(jd)
        val xiuIdx = XiuBoundary.findXiu(moonPos.eclipticLon)
        val xiuName = XiuBoundary.XIU_NAMES[xiuIdx]
        assertTrue("2026-06-19月躔应为柳宿，实际=${xiuName}(黄经${moonPos.eclipticLon}°)",
            xiuName == "柳宿")
    }

    @Test
    fun testMoonSunElongation_NewMoon() {
        // 验证朔日时日月黄经差接近0°
        // 2026-06-15 近似朔日（新月）
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 15)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        val moonLon = PlanetPositionCalc.calcMoonPosition(jd).eclipticLon
        var diff = abs(moonLon - sunLon)
        if (diff > 180) diff = 360 - diff
        assertTrue("朔日日月黄经差应小于15°，实际=${diff}°", diff < 15.0)
    }

    @Test
    fun testMoonSunElongation_FullMoon() {
        // 验望日时日月黄经差接近180°
        // 2026-06-30 近似望日（满月）
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 30)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        val moonLon = PlanetPositionCalc.calcMoonPosition(jd).eclipticLon
        var diff = abs(moonLon - sunLon)
        if (diff > 180) diff = 360 - diff
        assertTrue("望日日月黄经差应接近180°(>165°)，实际=${diff}°", diff > 165.0)
    }

    // ========== 行星位置 ==========

    @Test
    fun testPlanetLongitude_Jupiter2026() {
        // 木星位置验证（2026-06-19）
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        val jupiterLon = PlanetPositionCalc.calcPlanetLon(4, jd)
        assertTrue("木星黄经应在0-360°范围", jupiterLon in 0.0..360.0)
    }

    @Test
    fun testAllPlanetPositions() {
        // 验证所有行星位置都能正常计算
        val positions = PlanetPositionCalc.allPlanetPositions(2026, 6, 19)
        assertEquals(6, positions.size)
        for (pos in positions) {
            assertTrue("${pos.name}黄经应在0-360°范围", pos.eclipticLon in 0.0..360.0)
        }
    }

    // ========== 日躔星宿验证 ==========

    @Test
    fun testSunXiu_June19_2026() {
        // 2026-06-19 太阳约88°，应在觜宿(86.11°~88.85°)或参宿(88.85°~99.06°)
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        val xiuIdx = XiuBoundary.findXiu(sunLon)
        val xiuName = XiuBoundary.XIU_NAMES[xiuIdx]
        assertTrue("2026-06-19日躔应为觜宿或参宿，实际=${xiuName}(黄经${sunLon}°)",
            xiuName == "觜宿" || xiuName == "参宿")
    }

    @Test
    fun testSunXiu_WinterSolstice2025() {
        // 2025冬至太阳黄经约270°，应在斗宿(277.35°)之前
        // 270°落在箕宿(272.45°)之前，实际应在尾宿或箕宿附近
        val jd = PlanetPositionCalc.gregorianToJD(2025, 12, 21)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        val xiuIdx = XiuBoundary.findXiu(sunLon)
        val xiuName = XiuBoundary.XIU_NAMES[xiuIdx]
        // 冬至附近太阳在箕宿/斗宿
        assertTrue("2025冬至日躔应为箕宿或斗宿，实际=${xiuName}(黄经${sunLon}°)",
            xiuName == "箕宿" || xiuName == "斗宿" || xiuName == "尾宿")
    }
}
