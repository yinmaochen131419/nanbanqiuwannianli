/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

/**
 * JNI桥接类 - 连接Kotlin与sxtwl_cpp (寿星天文历C++实现)
 * 数据来源: 许剑伟寿星天文历 5.10
 * C++实现: yuangu/sxtwl_cpp (BSD-3-Clause)
 */
object SxtwlBridge {
    init {
        System.loadLibrary("sxtwl_bridge")
    }

    /**
     * 公历转农历
     * @return IntArray [lunarYear, lunarMonth(1-12, 负值=闰月), lunarDay, isLeap(0/1)]
     */
    @JvmStatic
    external fun nativeSolarToLunar(year: Int, month: Int, day: Int): IntArray

    /**
     * 获取天干地支
     * @return Array<String> [yearGZ, monthGZ, dayGZ, hourGZ] 格式如"甲子"
     */
    @JvmStatic
    external fun nativeGetGanZhi(year: Int, month: Int, day: Int, hour: Int): Array<String>

    /**
     * 获取当天节气信息
     * @return IntArray [jieQiIndex(0-23, -1=无), year, month, day, hour, minute]
     */
    @JvmStatic
    external fun nativeGetJieQi(year: Int, month: Int, day: Int): IntArray

    /**
     * 获取星期几
     * @return Int (0=Sunday, 1=Monday, ..., 6=Saturday)
     */
    @JvmStatic
    external fun nativeGetWeekday(year: Int, month: Int, day: Int): Int

    /**
     * 获取农历年闰月
     * @return Int (0=无闰月, 1-12=闰月)
     */
    @JvmStatic
    external fun nativeGetRunMonth(lunarYear: Int): Int

    /**
     * 获取农历月天数
     * @return Int (29 or 30)
     */
    @JvmStatic
    external fun nativeGetLunarMonthDays(lunarYear: Int, lunarMonth: Int, isLeap: Boolean): Int

    /**
     * 获取一年中所有节气对应的儒略日
     * @return DoubleArray [24] 节气JD值，索引0=冬至, 1=小寒, ..., 23=大雪
     */
    @JvmStatic
    external fun nativeGetSolarTermsForYear(year: Int): DoubleArray

    /**
     * 公历转儒略日
     * @return Double 儒略日
     */
    @JvmStatic
    external fun nativeSolarToJD(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Double

    /**
     * 获取当天节气的精确儒略日
     * @return Double 节气JD，-1=无节气
     */
    @JvmStatic
    external fun nativeGetSolarTermJD(year: Int, month: Int, day: Int): Double

    /**
     * 儒略日转公历
     * @return IntArray [year, month, day, hour, minute, second]
     */
    @JvmStatic
    external fun nativeJD2DD(jd: Double): IntArray

    // ========== 便捷方法 ==========

    /**
     * 儒略日转公历（便捷方法）
     */
    fun jdToGregorian(jd: Double): IntArray {
        return nativeJD2DD(jd)
    }

    /**
     * 获取农历年闰月（别名，兼容旧接口）
     */
    fun nativeGetLeapMonth(lunarYear: Int): Int {
        return nativeGetRunMonth(lunarYear)
    }
}