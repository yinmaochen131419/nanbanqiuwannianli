/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import com.nanbanqiu.wannianli.engine.SxtwlBridge
import kotlin.math.abs

object LunarCalendarEngine {

    fun solarToLunar(year: Int, month: Int, day: Int): LunarResult {
        val result = SxtwlBridge.nativeSolarToLunar(year, month, day)
        val lunarMonth = result[1]
        return LunarResult(
            year = result[0],
            month = abs(lunarMonth),
            day = result[2],
            isLeapMonth = lunarMonth < 0
        )
    }

    fun toSouthernLunar(year: Int, month: Int, day: Int): SouthernLunarResult {
        val northern = solarToLunar(year, month, day)
        val lunarMonth = northern.month
        val lunarYear = northern.year
        val isNorthLeap = northern.isLeapMonth

        val northLeapMonth = getNorthLeapMonth(lunarYear)
        val southLeapMonth = if (northLeapMonth > 0) {
            (northLeapMonth + 6 - 1) % 12 + 1
        } else {
            0
        }

        val southernYear: Int
        val southernMonth: Int
        if (lunarMonth >= 7) {
            southernYear = lunarYear
            southernMonth = lunarMonth - 6
        } else {
            southernYear = lunarYear - 1
            southernMonth = lunarMonth + 6
        }

        val isSouthLeapMonth = if (isNorthLeap) {
            val mappedSouthMonth = if (lunarMonth >= 7) lunarMonth - 6 else lunarMonth + 6
            southLeapMonth == mappedSouthMonth
        } else {
            false
        }

        return SouthernLunarResult(
            southernYear = southernYear,
            southernMonth = southernMonth,
            lunarDay = northern.day,
            originalLunarYear = lunarYear,
            originalLunarMonth = lunarMonth,
            isLeapMonth = isNorthLeap,
            isSouthernLeapMonth = isSouthLeapMonth,
            southernLeapMonth = southLeapMonth
        )
    }

    /**
     * 获取北半球该农历年的闰月月份
     * @return 闰月月份1-12，0表示无闰月
     */
    private fun getNorthLeapMonth(northLunarYear: Int): Int {
        return SxtwlBridge.nativeGetLeapMonth(northLunarYear)
    }

    fun gregorianToJD(year: Int, month: Int, day: Int): Double {
        return SxtwlBridge.nativeSolarToJD(year, month, day, 12, 0, 0)
    }

    data class LunarResult(
        val year: Int,
        val month: Int,
        val day: Int,
        val isLeapMonth: Boolean
    )

    data class SouthernLunarResult(
        val southernYear: Int,
        val southernMonth: Int,
        val lunarDay: Int,
        val originalLunarYear: Int,
        val originalLunarMonth: Int,
        val isLeapMonth: Boolean,
        val isSouthernLeapMonth: Boolean = false,
        val southernLeapMonth: Int = 0
    )
}