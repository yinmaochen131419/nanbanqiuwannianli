/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

import com.nlf.calendar.Solar

import kotlin.math.abs

object LunarCalendarEngine {

    fun solarToLunar(year: Int, month: Int, day: Int): LunarResult {

        val lunar = Solar.fromYmd(year, month, day).lunar

        return LunarResult(

            year = lunar.year,

            month = abs(lunar.month),

            day = lunar.day,

            isLeapMonth = lunar.toString().contains("闰")
        )
    }

    fun toSouthernLunar(year: Int, month: Int, day: Int): SouthernLunarResult {
        val lunar = Solar.fromYmd(year, month, day).lunar
        val lunarMonth = abs(lunar.month)
        val lunarYear = lunar.year
        val isNorthLeap = lunar.toString().contains("闰")

        // 南半球闰月 = 北半球闰月 + 6，超12取模
        val northLeapMonth = getNorthLeapMonth(lunarYear)
        val southLeapMonth = if (northLeapMonth > 0) {
            (northLeapMonth + 6 - 1) % 12 + 1
        } else {
            0
        }

        // 计算南半球月份
        val southernYear: Int

        val southernMonth: Int

        if (lunarMonth >= 7) {

            southernYear = lunarYear

            southernMonth = lunarMonth - 6

        } else {

            southernYear = lunarYear - 1

            southernMonth = lunarMonth + 6

        }

        // 判断当前日期是否落在南半球闰月
        val isSouthLeapMonth = if (isNorthLeap) {

            val mappedSouthMonth = if (lunarMonth >= 7) lunarMonth - 6 else lunarMonth + 6

            southLeapMonth == mappedSouthMonth

        } else {

            false

        }

        return SouthernLunarResult(

            southernYear = southernYear,

            southernMonth = southernMonth,

            lunarDay = lunar.day,

            originalLunarYear = lunarYear,

            originalLunarMonth = lunarMonth,

            isLeapMonth = isNorthLeap,

            isSouthernLeapMonth = isSouthLeapMonth,

            southernLeapMonth = southLeapMonth

        )

    }

    /**

     * 鑾峰彇鍖楀崐鐞冭鍐滃巻骞寸殑闂版湀鏈堜唤

     * lunar-java涓棸鏈堢敤璐熸暟琛ㄧず锛堥棸2?-2?     * @return 闰月月份1-12，0表示无闰月     */

    private fun getNorthLeapMonth(northLunarYear: Int): Int {

        for (m in 1..12) {

            try {

                val testLunar = com.nlf.calendar.Lunar.fromYmd(northLunarYear, -m, 1)

                if (testLunar != null) return m

            } catch (_: Exception) {}

        }

        return 0

    }

    fun gregorianToJD(year: Int, month: Int, day: Int): Double {

        var y = year

        var m = month

        if (m <= 2) { y -= 1; m += 12 }

        val a = Math.floor(y / 100.0)

        val b = 2 - a + Math.floor(a / 4.0)

        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5

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
