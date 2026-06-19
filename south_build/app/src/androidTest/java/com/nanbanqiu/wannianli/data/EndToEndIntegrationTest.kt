/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nanbanqiu.wannianli.data.model.ScheduleEvent
import com.nanbanqiu.wannianli.data.repository.CalendarRepository
import com.nanbanqiu.wannianli.engine.SxtwlBridge
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 集成测试：端到端全链路
 * 验证 CalendarRepository.getCalendarDay() 的完整数据流
 *
 * 数据流：
 *   公历日期输入 → 时区转换 → SxtwlBridge(JNI) → LunarCalendarEngine(农历)
 *   → SolarTermCalculator(节气) → FourPillarsEngine(八字) → PureLunarEngine(月相)
 *   → 南半球映射(干支翻转/生肖翻转/月相对称) → CalendarDay(输出)
 */
@RunWith(AndroidJUnit4::class)
class EndToEndIntegrationTest {

    private lateinit var repository: CalendarRepository

    @Before
    fun setup() {
        SxtwlBridge // 触发native库加载
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = CalendarRepository(context)
    }

    // ========== 链路7：CalendarRepository.getCalendarDay 全链路 ==========

    @Test
    fun testGetCalendarDay_June19_2026() {
        val day = repository.getCalendarDay(2026, 6, 19, 12, 0)

        // 公历日期
        assertEquals(2026, day.gregorianYear)
        assertEquals(6, day.gregorianMonth)
        assertEquals(19, day.gregorianDay)

        // 星期
        assertEquals("星期五", day.weekday)

        // 农历日期
        assertNotNull(day.lunarDate)
        assertEquals(5, day.lunarDate.month)   // 五月
        assertEquals(5, day.lunarDate.day)    // 初五

        // 干支
        assertEquals("丙午", day.lunarDate.yearGanZhi)
        assertEquals("甲子", day.lunarDate.dayGanZhi)

        // 生肖
        assertEquals("马", day.lunarDate.shengXiao)

        // 南半球农历
        assertEquals(11, day.lunarDate.southernMonth) // 五月+6=十一月

        // 月相
        assertTrue("月相名称不应为空", day.moonPhaseName.isNotEmpty())

        // 南半球干支（翻转后）
        assertEquals("壬子", day.southYearGanZhi)  // 丙午→壬子
        assertEquals("庚午", day.southDayGanZhi)   // 甲子→庚午
    }

    @Test
    fun testGetCalendarDay_SpringFestival() {
        // 2026-02-17 春节
        val day = repository.getCalendarDay(2026, 2, 17, 12, 0)

        assertEquals(1, day.lunarDate.month)   // 正月
        assertEquals(1, day.lunarDate.day)    // 初一
        assertEquals("丙午", day.lunarDate.yearGanZhi)
    }

    @Test
    fun testGetCalendarDay_BeforeLiChun() {
        // 2026-01-15 立春前 → 年柱应为乙巳
        val day = repository.getCalendarDay(2026, 1, 15, 12, 0)

        assertEquals("乙巳", day.lunarDate.yearGanZhi)
        assertEquals("蛇", day.lunarDate.shengXiao)
    }

    @Test
    fun testGetCalendarDay_SouthernHemisphereMapping() {
        // 验证南半球干支翻转
        val day = repository.getCalendarDay(2026, 6, 19, 12, 0)

        // 北半球干支
        val northYear = day.lunarDate.yearGanZhi
        val northDay = day.lunarDate.dayGanZhi

        // 南半球干支（天干+6, 地支+6）
        assertEquals("南半球年柱应为翻转值",
            com.nanbanqiu.wannianli.engine.FourPillarsEngine.flipGanZhi(northYear),
            day.southYearGanZhi)
        assertEquals("南半球日柱应为翻转值",
            com.nanbanqiu.wannianli.engine.FourPillarsEngine.flipGanZhi(northDay),
            day.southDayGanZhi)
    }

    @Test
    fun testGetCalendarDay_SouthernShengXiao() {
        // 南半球生肖 = 北半球生肖 + 6（取模12）
        val day = repository.getCalendarDay(2026, 6, 19, 12, 0)

        val northSx = day.lunarDate.shengXiao
        val southSx = day.southShengXiao

        val sxList = listOf("鼠","牛","虎","兔","龙","蛇","马","羊","猴","鸡","狗","猪")
        val northIdx = sxList.indexOf(northSx)
        val southIdx = sxList.indexOf(southSx)

        assertTrue("北半球生肖应有效", northIdx >= 0)
        assertTrue("南半球生肖应有效", southIdx >= 0)
        assertEquals("南半球生肖应偏移6位",
            (northIdx + 6) % 12, southIdx)
    }

    @Test
    fun testGetCalendarDay_SolarTerms() {
        // 验证节气信息
        val day = repository.getCalendarDay(2026, 6, 19, 12, 0)

        // 6月19日在芒种(6月5日)之后，夏至(6月21日)之前
        // 当前节气应为芒种
        assertNotNull("应有当前节气", day.currentSolarTerm)
        // 下一个节气应为夏至
        assertNotNull("应有下一节气", day.nextSolarTerm)
    }

    @Test
    fun testGetCalendarDay_MultipleDates() {
        // 验证多个日期都能正常计算
        val testDates = listOf(
            Triple(2026, 1, 1),   // 元旦
            Triple(2026, 2, 17),  // 春节
            Triple(2026, 6, 19),  // 端午
            Triple(2025, 10, 6),  // 中秋
            Triple(2025, 12, 21)  // 冬至
        )

        for ((y, m, d) in testDates) {
            val day = repository.getCalendarDay(y, m, d, 12, 0)
            assertEquals(y, day.gregorianYear)
            assertEquals(m, day.gregorianMonth)
            assertEquals(d, day.gregorianDay)
            assertTrue("星期不应为空", day.weekday.isNotEmpty())
            assertTrue("年柱不应为空", day.lunarDate.yearGanZhi.isNotEmpty())
            assertTrue("日柱不应为空", day.lunarDate.dayGanZhi.isNotEmpty())
        }
    }

    // ========== 链路8：日程农历转公历 ==========

    @Test
    fun testScheduleEvent_LunarToSolar() {
        // 创建一个农历日期的日程：2026年五月初五
        val event = ScheduleEvent(
            id = 1,
            title = "端午节",
            type = ScheduleEvent.TYPE_ANNIVERSARY,
            year = 2026,
            month = 5,
            day = 5,
            isLunarDate = true
        )

        // 农历五月初五 → 公历6月19日
        val solar = event.toSolarDate(2026)
        assertNotNull("农历转公历不应返回null", solar)
        assertEquals(2026, solar!!.first)
        assertEquals(6, solar.second)
        assertEquals(19, solar.third)
    }

    @Test
    fun testScheduleEvent_SolarDate() {
        // 非农历日期应直接返回
        val event = ScheduleEvent(
            id = 2,
            title = "测试",
            type = ScheduleEvent.TYPE_TODO,
            year = 2026,
            month = 6,
            day = 19,
            isLunarDate = false
        )

        val solar = event.toSolarDate(2026)
        assertEquals(2026, solar!!.first)
        assertEquals(6, solar.second)
        assertEquals(19, solar.third)
    }

    @Test
    fun testScheduleEvent_LunarNewYear() {
        // 农历正月初一 → 春节
        val event = ScheduleEvent(
            id = 3,
            title = "春节",
            type = ScheduleEvent.TYPE_ANNIVERSARY,
            year = 2026,
            month = 1,
            day = 1,
            isLunarDate = true
        )

        val solar = event.toSolarDate(2026)
        assertNotNull(solar)
        assertEquals(2026, solar!!.first)
        assertEquals(2, solar.second)
        assertEquals(17, solar.third)
    }
}
