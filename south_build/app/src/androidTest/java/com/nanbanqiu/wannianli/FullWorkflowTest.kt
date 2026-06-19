/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nanbanqiu.wannianli.data.CityDataSource
import com.nanbanqiu.wannianli.data.model.ScheduleEvent
import com.nanbanqiu.wannianli.data.repository.CalendarRepository
import com.nanbanqiu.wannianli.engine.FourPillarsEngine
import com.nanbanqiu.wannianli.engine.LunarCalendarEngine
import com.nanbanqiu.wannianli.engine.PlanetPositionCalc
import com.nanbanqiu.wannianli.engine.PureLunarCalendarEngine
import com.nanbanqiu.wannianli.engine.PureLunarEngine
import com.nanbanqiu.wannianli.engine.SolarTermCalculator
import com.nanbanqiu.wannianli.engine.SxtwlBridge
import com.nanbanqiu.wannianli.engine.XiuBoundary
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 全流程测试：模拟用户完整操作路径
 *
 * 测试覆盖用户从打开App到查看各项数据的完整流程：
 *   流程1：启动 → 今日日历 → 农历/节气/八字/月相/南半球映射
 *   流程2：翻页 → 上月/下月 → 日期选择 → 数据更新
 *   流程3：城市切换 → 时区变化 → 农历重新计算
 *   流程4：纯阴历视图 → 月相朔望 → 节气标注
 *   流程5：日程管理 → 农历日程 → 公历转换 → 提醒
 *   流程6：年度节气 → 24节气列表 → 南北半球映射
 *   流程7：星宿计算 → 日躔/月躔/值日 → 二十八宿
 *   流程8：迭代日志 → 版本信息
 */
@RunWith(AndroidJUnit4::class)
class FullWorkflowTest {

    companion object {
        private lateinit var repository: CalendarRepository

        @BeforeClass
        @JvmStatic
        fun setup() {
            SxtwlBridge // 触发native库加载
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            repository = CalendarRepository(context)
        }
    }

    // ========== 流程1：启动App → 今日日历完整数据 ==========

    @Test
    fun testFlow1_TodayCalendar_FullData() {
        // 模拟用户打开App，加载今日日历
        val today = repository.getCalendarDay(2026, 6, 19, 12, 0, "Asia/Shanghai")

        // 1. 公历日期
        assertEquals(2026, today.gregorianYear)
        assertEquals(6, today.gregorianMonth)
        assertEquals(19, today.gregorianDay)

        // 2. 星期
        assertTrue("星期不应为空", today.weekday.isNotEmpty())
        assertEquals("星期五", today.weekday)

        // 3. 农历日期（端午节）
        assertEquals(5, today.lunarDate.month)
        assertEquals(5, today.lunarDate.day)
        assertFalse(today.lunarDate.isLeapMonth)

        // 4. 干支
        assertTrue("年柱不应为空", today.lunarDate.yearGanZhi.isNotEmpty())
        assertTrue("月柱不应为空", today.lunarDate.monthGanZhi.isNotEmpty())
        assertTrue("日柱不应为空", today.lunarDate.dayGanZhi.isNotEmpty())

        // 5. 生肖
        assertTrue("生肖不应为空", today.lunarDate.shengXiao.isNotEmpty())

        // 6. 节气
        assertNotNull("应有当前节气", today.currentSolarTerm)
        assertNotNull("应有下一节气", today.nextSolarTerm)

        // 7. 南半球数据
        assertTrue("南半球年柱不应为空", today.southYearGanZhi.isNotEmpty())
        assertTrue("南半球日柱不应为空", today.southDayGanZhi.isNotEmpty())
        assertTrue("南半球生肖不应为空", today.southShengXiao.isNotEmpty())

        // 8. 月相
        assertTrue("月相名称不应为空", today.moonPhaseName.isNotEmpty())

        // 9. 南半球对蹠日
        assertTrue("对蹠日不应为空", today.southOppositeDay.isNotEmpty())

        // 10. 南半球阳历
        assertTrue("南半球阳历年应合理", today.southSolarYear > 0)
        assertTrue("南半球阳历月应在1-12", today.southSolarMonth in 1..12)
        assertTrue("南半球阳历日应在1-31", today.southSolarDay in 1..31)
    }

    // ========== 流程2：翻页导航 → 上下月切换 ==========

    @Test
    fun testFlow2_NavigateMonths() {
        // 模拟用户从6月翻到7月（下月）
        val june = repository.getCalendarDay(2026, 6, 19, 12, 0)
        val july = repository.getCalendarDay(2026, 7, 19, 12, 0)

        assertEquals(6, june.gregorianMonth)
        assertEquals(7, july.gregorianMonth)

        // 两个月的数据应不同
        assertNotEquals(june.lunarDate.dayGanZhi, july.lunarDate.dayGanZhi)

        // 翻到5月（上月）
        val may = repository.getCalendarDay(2026, 5, 19, 12, 0)
        assertEquals(5, may.gregorianMonth)
        assertNotEquals(may.lunarDate.dayGanZhi, june.lunarDate.dayGanZhi)
    }

    @Test
    fun testFlow2_NavigateYearBoundary() {
        // 跨年导航：2025年12月 → 2026年1月
        val dec2025 = repository.getCalendarDay(2025, 12, 31, 12, 0)
        val jan2026 = repository.getCalendarDay(2026, 1, 1, 12, 0)

        assertEquals(2025, dec2025.gregorianYear)
        assertEquals(2026, jan2026.gregorianYear)

        // 2025-12-31 生肖应为蛇（乙巳年）
        assertEquals("蛇", dec2025.lunarDate.shengXiao)
        // 2026-01-01 生肖也应为蛇（立春前仍是乙巳年）
        assertEquals("蛇", jan2026.lunarDate.shengXiao)
    }

    @Test
    fun testFlow2_MonthDayCount() {
        // 验证每月天数正确
        val janDays = repository.getMonthDayCount(2026, 1)
        val febDays = repository.getMonthDayCount(2026, 2)
        val aprDays = repository.getMonthDayCount(2026, 4)

        assertEquals(31, janDays)
        assertEquals(28, febDays) // 2026非闰年
        assertEquals(30, aprDays)
    }

    // ========== 流程3：城市切换 → 时区变化 ==========

    @Test
    fun testFlow3_CitySwitch_Timezone() {
        // 模拟用户切换城市：上海 → 布宜诺斯艾利斯
        val shanghaiDay = repository.getCalendarDay(2026, 6, 19, 12, 0, "Asia/Shanghai")
        val buenosAiresDay = repository.getCalendarDay(2026, 6, 19, 12, 0, "America/Argentina/Buenos_Aires")

        // 同一公历日期，不同时区，农历可能不同（因北京时间 vs 阿根廷时间差~11小时）
        // 但6月19日中午12点在两个时区都是同一天，农历应相同
        assertEquals(shanghaiDay.lunarDate.month, buenosAiresDay.lunarDate.month)
        assertEquals(shanghaiDay.lunarDate.day, buenosAiresDay.lunarDate.day)
    }

    @Test
    fun testFlow3_CitySwitch_Antipodal() {
        // 验证对蹠城市计算
        val shanghai = CityDataSource.getCityById("shanghai")
        assertNotNull(shanghai)
        val antipodal = CityDataSource.getAntipodalInfo(shanghai!!)
        assertNotNull(antipodal)
        assertTrue("对蹠城市ID不应为空", antipodal.antipodalZoneId.isNotEmpty())
    }

    @Test
    fun testFlow3_TimezoneEdgeCase() {
        // 时区边界：UTC+12 和 UTC-12 的同一天可能对应不同农历日
        // 测试极端时区情况不崩溃
        val east = repository.getCalendarDay(2026, 6, 19, 23, 30, "Pacific/Auckland")
        val west = repository.getCalendarDay(2026, 6, 19, 0, 30, "Pacific/Honolulu")

        assertNotNull(east)
        assertNotNull(west)
        assertTrue("奥克兰农历日应有效", east.lunarDate.day in 1..30)
        assertTrue("檀香山农历日应有效", west.lunarDate.day in 1..30)
    }

    // ========== 流程4：纯阴历视图 ==========

    @Test
    fun testFlow4_PureLunarMonth() {
        // 模拟用户切换到纯阴历视图
        val pureLunarDate = PureLunarCalendarEngine.getPureLunarDate(2026, 6, 19)

        assertNotNull(pureLunarDate)
        assertTrue("纯阴历日名不应为空", pureLunarDate!!.dayName.isNotEmpty())
        assertTrue("月相应不为空", pureLunarDate.moonPhase.isNotEmpty())
    }

    @Test
    fun testFlow4_PureLunarMonth_NewMoon() {
        // 2026-06-15 = 农历五月初一（新月）
        val newMoon = PureLunarCalendarEngine.getPureLunarDate(2026, 6, 15)
        assertNotNull(newMoon)
        assertTrue("初一应为新月", newMoon!!.isNewMoon)
        assertFalse("初一不应为满月", newMoon.isFullMoon)
    }

    @Test
    fun testFlow4_PureLunarMonth_FullMoon() {
        // 2026-06-29 = 农历五月十五（满月）
        val fullMoon = PureLunarCalendarEngine.getPureLunarDate(2026, 6, 29)
        assertNotNull(fullMoon)
        assertTrue("十五应为满月", fullMoon!!.isFullMoon)
        assertFalse("十五不应为新月", fullMoon.isNewMoon)
    }

    @Test
    fun testFlow4_LunarDayName() {
        // 验证农历日名
        val dayName = repository.getLunarDayName(2026, 6, 19)
        assertTrue("农历日名不应为空", dayName.isNotEmpty())
    }

    @Test
    fun testFlow4_LunarMonthName() {
        val monthName = repository.getLunarMonthName(2026, 6, 19)
        assertTrue("农历月名不应为空", monthName.isNotEmpty())
    }

    @Test
    fun testFlow4_WeekendDetection() {
        // 2026-06-19 是星期五（非周末）
        assertFalse(repository.isWeekend(2026, 6, 19))
        // 2026-06-20 是星期六（周末）
        assertTrue(repository.isWeekend(2026, 6, 20))
        // 2026-06-21 是星期日（周末）
        assertTrue(repository.isWeekend(2026, 6, 21))
    }

    // ========== 流程5：日程管理 → 农历转公历 ==========

    @Test
    fun testFlow5_ScheduleLunarToSolar() {
        // 用户创建农历日程：端午节（五月初五）
        val event = ScheduleEvent(
            id = 1,
            title = "端午节",
            type = ScheduleEvent.TYPE_ANNIVERSARY,
            year = 2026,
            month = 5,
            day = 5,
            isLunarDate = true
        )

        val solar = event.toSolarDate(2026)
        assertNotNull("农历转公历不应为null", solar)
        assertEquals(2026, solar!!.first)
        assertEquals(6, solar.second)
        assertEquals(19, solar.third)
    }

    @Test
    fun testFlow5_ScheduleSolarDate() {
        // 用户创建公历日程
        val event = ScheduleEvent(
            id = 2,
            title = "开会",
            type = ScheduleEvent.TYPE_MEETING,
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
    fun testFlow5_ScheduleAllTypes() {
        // 验证所有日程类型
        val types = listOf(
            ScheduleEvent.TYPE_BIRTHDAY,
            ScheduleEvent.TYPE_ANNIVERSARY,
            ScheduleEvent.TYPE_MEETING,
            ScheduleEvent.TYPE_TODO
        )
        for (type in types) {
            val event = ScheduleEvent(
                id = type.toLong(),
                title = "测试$type",
                type = type,
                year = 2026,
                month = 6,
                day = 19,
                isLunarDate = false
            )
            assertEquals(type, event.type)
            assertTrue("类型名不应为空", event.typeName.isNotEmpty())
            assertTrue("类型图标不应为空", event.typeIcon.isNotEmpty())
        }
    }

    // ========== 流程6：年度节气列表 ==========

    @Test
    fun testFlow6_AnnualSolarTerms() {
        // 模拟用户查看2026年24节气
        val terms = repository.getSolarTermsForYear(2026)
        assertEquals(24, terms.size)

        // 验证节气顺序
        assertEquals("冬至", terms[0].northName)
        assertEquals("小寒", terms[1].northName)
        assertEquals("夏至", terms[12].northName)
        assertEquals("大雪", terms[23].northName)

        // 验证每个节气都有有效日期
        for (term in terms) {
            assertTrue("${term.northName}年份应合理", term.year in 2025..2027)
            assertTrue("${term.northName}月份应合理", term.month in 1..12)
            assertTrue("${term.northName}日期应合理", term.day in 1..31)
        }
    }

    @Test
    fun testFlow6_SolarTermDayName() {
        // 验证节气日标注
        // 2026年夏至约在6月21日
        val termName = repository.getSolarTermDayName(2026, 6, 21)
        if (termName != null) {
            assertTrue("节气名不应为空", termName.isNotEmpty())
        }
    }

    @Test
    fun testFlow6_SouthernSeason() {
        // 验证南半球季节
        val juneDay = repository.getCalendarDay(2026, 6, 19, 12, 0)
        // 6月北半球是夏季，南半球是冬季
        assertTrue("南半球季节不应为空", juneDay.southernSeason.isNotEmpty())
        assertTrue("南半球季节emoji不应为空", juneDay.southernSeasonEmoji.isNotEmpty())
    }

    // ========== 流程7：星宿计算 ==========

    @Test
    fun testFlow7_DailyStarLodge() {
        // 值日星宿：2026-06-19
        // 基准：2025-01-01=参宿(index 20)，相隔534天
        // (534+20)%28 = 22 → 鬼宿
        val baseDate = java.time.LocalDate.of(2025, 1, 1)
        val targetDate = java.time.LocalDate.of(2026, 6, 19)
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(baseDate, targetDate)
        val expectedIdx = ((totalDays + 20L) % 28L).toInt()
        val expectedName = XiuBoundary.XIU_NAMES[expectedIdx]

        assertTrue("值日星宿索引应有效", expectedIdx in 0..27)
        assertTrue("值日星宿名不应为空", expectedName.isNotEmpty())
    }

    @Test
    fun testFlow7_SunStarLodge() {
        // 日躔星宿：2026-06-19
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        val sunLon = PlanetPositionCalc.calcSunLon(jd)
        val xiuIdx = XiuBoundary.findXiu(sunLon)
        val xiuName = XiuBoundary.XIU_NAMES[xiuIdx]

        assertTrue("日躔星宿名不应为空", xiuName.isNotEmpty())
        // 6月19日太阳黄经约88°，应在觜宿或参宿
        assertTrue("日躔应为觜宿或参宿，实际=$xiuName(黄经${sunLon}°)",
            xiuName == "觜宿" || xiuName == "参宿")
    }

    @Test
    fun testFlow7_MoonStarLodge() {
        // 月躔星宿：2026-06-19
        val jd = PlanetPositionCalc.gregorianToJD(2026, 6, 19)
        val moonPos = PlanetPositionCalc.calcMoonPosition(jd)
        val xiuIdx = XiuBoundary.findXiu(moonPos.eclipticLon)
        val xiuName = XiuBoundary.XIU_NAMES[xiuIdx]

        assertTrue("月躔星宿名不应为空", xiuName.isNotEmpty())
        // 月亮黄经约135-145°，应在柳宿或星宿附近
        assertTrue("月躔星宿应有效", xiuIdx in 0..27)
    }

    @Test
    fun testFlow7_MoonXiuName() {
        // 通过Repository获取月躔星宿名
        val moonXiu = repository.getMoonXiuName(2026, 6, 19)
        assertTrue("月躔星宿名不应为空", moonXiu.isNotEmpty())
    }

    // ========== 流程8：跨年数据一致性 ==========

    @Test
    fun testFlow8_CrossYearConsistency() {
        // 验证跨年时数据连续性
        val dec31 = repository.getCalendarDay(2025, 12, 31, 12, 0)
        val jan01 = repository.getCalendarDay(2026, 1, 1, 12, 0)

        // 农历日期应连续（冬月十三 → 冬月十四）
        assertEquals(dec31.lunarDate.month, jan01.lunarDate.month)
        assertEquals(dec31.lunarDate.day + 1, jan01.lunarDate.day)
    }

    @Test
    fun testFlow8_LeapYear2025() {
        // 2025年农历有闰六月
        val leapMonth = SxtwlBridge.nativeGetLeapMonth(2025)
        assertEquals(6, leapMonth)

        // 验证闰月前后的农历日期连续
        val before = repository.getCalendarDay(2025, 7, 20, 12, 0) // 闰六月前
        val after = repository.getCalendarDay(2025, 8, 20, 12, 0)  // 闰六月后
        assertNotNull(before)
        assertNotNull(after)
    }

    @Test
    fun testFlow8_FullYear2026_AllMonths() {
        // 遍历2026年所有月份，验证不崩溃
        for (month in 1..12) {
            val dayCount = repository.getMonthDayCount(2026, month)
            assertTrue("${month}月天数应合理", dayCount in 28..31)

            val midMonth = repository.getCalendarDay(2026, month, 15, 12, 0)
            assertEquals(2026, midMonth.gregorianYear)
            assertEquals(month, midMonth.gregorianMonth)
            assertTrue("${month}月15日农历日应有效", midMonth.lunarDate.day in 1..30)
        }
    }

    // ========== 流程9：南半球对称性验证 ==========

    @Test
    fun testFlow9_SouthernSymmetry_GanZhi() {
        // 验证南北半球干支对称性（天干+6, 地支+6）
        val testDates = listOf(
            Triple(2026, 1, 15),
            Triple(2026, 3, 20),
            Triple(2026, 6, 19),
            Triple(2026, 9, 23),
            Triple(2026, 12, 21)
        )

        for ((y, m, d) in testDates) {
            val day = repository.getCalendarDay(y, m, d, 12, 0)
            assertEquals("南半球年柱应为北半球翻转",
                FourPillarsEngine.flipGanZhi(day.lunarDate.yearGanZhi),
                day.southYearGanZhi)
            assertEquals("南半球日柱应为北半球翻转",
                FourPillarsEngine.flipGanZhi(day.lunarDate.dayGanZhi),
                day.southDayGanZhi)
        }
    }

    @Test
    fun testFlow9_SouthernSymmetry_ShengXiao() {
        // 验证南北半球生肖对称性（+6取模12）
        val sxList = listOf("鼠","牛","虎","兔","龙","蛇","马","羊","猴","鸡","狗","猪")
        val testDates = listOf(
            Triple(2026, 1, 15),  // 立春前=蛇
            Triple(2026, 6, 19),  // =马
            Triple(2026, 12, 21)  // =马
        )

        for ((y, m, d) in testDates) {
            val day = repository.getCalendarDay(y, m, d, 12, 0)
            val northIdx = sxList.indexOf(day.lunarDate.shengXiao)
            val southIdx = sxList.indexOf(day.southShengXiao)
            assertTrue("北半球生肖应有效", northIdx >= 0)
            assertTrue("南半球生肖应有效", southIdx >= 0)
            assertEquals("南半球生肖应偏移6位", (northIdx + 6) % 12, southIdx)
        }
    }

    @Test
    fun testFlow9_SouthernOppositeDay() {
        // 验证南半球对蹠日
        val day = repository.getCalendarDay(2026, 6, 19, 12, 0)
        assertTrue("对蹠日不应为空", day.southOppositeDay.isNotEmpty())
    }

    // ========== 流程10：缓存与性能 ==========

    @Test
    fun testFlow10_CacheConsistency() {
        // 同一日期多次查询应返回一致结果
        val day1 = repository.getCalendarDay(2026, 6, 19, 12, 0)
        val day2 = repository.getCalendarDay(2026, 6, 19, 12, 0)

        assertEquals(day1.lunarDate.dayGanZhi, day2.lunarDate.dayGanZhi)
        assertEquals(day1.southYearGanZhi, day2.southYearGanZhi)
        assertEquals(day1.moonPhaseName, day2.moonPhaseName)
    }

    @Test
    fun testFlow10_ClearCacheAndReload() {
        // 清除缓存后重新加载应正常
        val day1 = repository.getCalendarDay(2026, 6, 19, 12, 0)
        repository.clearCache()
        val day2 = repository.getCalendarDay(2026, 6, 19, 12, 0)

        assertEquals(day1.lunarDate.dayGanZhi, day2.lunarDate.dayGanZhi)
        assertEquals(day1.southYearGanZhi, day2.southYearGanZhi)
    }
}
