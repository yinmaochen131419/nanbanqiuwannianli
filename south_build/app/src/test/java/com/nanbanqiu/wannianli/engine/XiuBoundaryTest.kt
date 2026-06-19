/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 二十八宿边界查找单元测试
 * 验证星宿边界划分的正确性
 */
class XiuBoundaryTest {

    @Test
    fun testFindXiuAtStarPositions() {
        // 每个宿的起始黄经应该返回该宿的索引
        for (i in 0 until 28) {
            val lon = XiuBoundary.XIU_STARS[i].eclipticLonJ2000
            val result = XiuBoundary.findXiu(lon)
            assertEquals("黄经 ${lon}° 应为 ${XiuBoundary.XIU_NAMES[i]}", i, result)
        }
    }

    @Test
    fun testFindXiuZeroDegrees() {
        // 0° 应属于室宿（室宿起始348.10°，壁宿起始4.86°，0°在室宿范围内）
        val result = XiuBoundary.findXiu(0.0)
        assertEquals(12, result) // 室宿
        assertEquals("室宿", XiuBoundary.XIU_NAMES[result])
    }

    @Test
    fun testFindXiuWrapAround() {
        // 360° 应等同于 0°
        val result360 = XiuBoundary.findXiu(360.0)
        val result0 = XiuBoundary.findXiu(0.0)
        assertEquals(result0, result360)
    }

    @Test
    fun testFindXiuNegativeDegrees() {
        // 负角度应正确归一化
        val result = XiuBoundary.findXiu(-1.0)
        // -1° 等同于 359°，应属于室宿（348.10°~364.86°）
        assertEquals(12, result)
    }

    @Test
    fun testFindXiuMidpoints() {
        // 测试各宿中间点
        // 角宿(203.80) ~ 亢宿(217.12) 中间点
        val mid1 = (203.80 + 217.12) / 2.0
        assertEquals(0, XiuBoundary.findXiu(mid1)) // 角宿

        // 参宿(88.85) ~ 井宿(99.06) 中间点
        val mid2 = (88.85 + 99.06) / 2.0
        assertEquals(20, XiuBoundary.findXiu(mid2)) // 参宿
    }

    @Test
    fun testGetXiuInfo() {
        val info = XiuBoundary.getXiuInfo(88.0)
        assertEquals(19, info.index) // 觜宿
        assertEquals("觜宿", info.name)
        assertEquals("火", info.element)
        assertEquals("猴", info.animal)
        assertEquals("西方白虎", info.group)
    }

    @Test
    fun testGetXiuByIndex() {
        val info = XiuBoundary.getXiuByIndex(0)
        assertEquals("角宿", info.name)
        assertEquals("木", info.element)
        assertEquals("蛟", info.animal)
        assertEquals("东方青龙", info.group)
    }

    @Test
    fun testGetXiuByIndexWrap() {
        // 索引28应等同于索引0
        val info28 = XiuBoundary.getXiuByIndex(28)
        val info0 = XiuBoundary.getXiuByIndex(0)
        assertEquals(info0.name, info28.name)
    }

    @Test
    fun testAll28StarsHaveValidBoundaries() {
        // 确保每个黄经都能找到对应的宿
        for (lon in 0 until 360) {
            val idx = XiuBoundary.findXiu(lon.toDouble())
            assertTrue("黄经 ${lon}° 未找到星宿", idx in 0..27)
        }
    }

    @Test
    fun testFourGroupsEachHave7Stars() {
        // 四象各7宿
        for (groupIdx in 0..3) {
            for (starIdx in 0..6) {
                val idx = groupIdx * 7 + starIdx
                val info = XiuBoundary.getXiuByIndex(idx)
                assertEquals(XiuBoundary.GROUP_NAMES[groupIdx], info.group)
            }
        }
    }
}
