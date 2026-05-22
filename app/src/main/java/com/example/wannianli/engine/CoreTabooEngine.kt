package com.example.wannianli.engine

data class TabooCheck(
    val name: String,
    val priority: Int,
    val groupTitle: String,
    val isViolated: Boolean,
    val detail: String,
    val prohibition: String
)

data class PriorityTabooGroup(
    val priority: Int,
    val title: String,
    val subtitle: String,
    val checks: List<TabooCheck>,
    val anyViolated: Boolean,
    val verdict: String,
    val prohibition: String
)

data class CoreTabooResult(
    val priorityGroups: List<PriorityTabooGroup>,
    val overallHasCriticalViolation: Boolean,
    val shanShaResult: ShanShaResult
)

object CoreTabooEngine {

    private val LIU_HAI = intArrayOf(7, 6, 5, 4, 3, 2, 1, 0, 11, 10, 9, 8)

    fun evaluate(
        year: Int, month: Int, day: Int,
        yearGan: Int, yearZhi: Int, monthZhi: Int, dayZhi: Int, dayGan: Int,
        sittingMountainIndex: Int,
        lunarMonth: Int, lunarDay: Int,
        solarTerms: List<SolarTermCalculator.SolarTermResult>
    ): CoreTabooResult {
        val shanShaResult = ShanShaEngine.evaluate(year, month, day, yearGan, yearZhi, monthZhi, dayZhi, sittingMountainIndex)

        val extraMountainChecks = buildExtraMountainChecks(
            year, month, day, yearZhi, monthZhi, dayZhi, sittingMountainIndex
        )

        val dailyChecks = buildDailyTabooChecks(
            year, month, day, monthZhi, dayGan, dayZhi, lunarMonth, lunarDay, solarTerms
        )

        val allChecks = organizeByPriority(shanShaResult, extraMountainChecks, dailyChecks)

        val overallHasCritical = allChecks[0].anyViolated || allChecks[2].anyViolated

        return CoreTabooResult(
            priorityGroups = allChecks,
            overallHasCriticalViolation = overallHasCritical,
            shanShaResult = shanShaResult
        )
    }

    private fun buildExtraMountainChecks(
        year: Int, month: Int, day: Int,
        yearZhi: Int, monthZhi: Int, dayZhi: Int,
        sittingMountainIndex: Int
    ): List<TabooCheck> {
        val checks = mutableListOf<TabooCheck>()
        val mtName = ShanShaEngine.MOUNTAIN_NAMES[sittingMountainIndex]
        val mtBranch = ShanShaEngine.MOUNTAIN_BRANCH[sittingMountainIndex]
        val palaceIdx = ShanShaEngine.MOUNTAIN_PALACE[sittingMountainIndex]
        val palaceName = ShanShaEngine.PALACE_NAMES[palaceIdx]

        if (mtBranch >= 0) {
            val dragonTigerZhi = LIU_HAI[yearZhi]
            val dtViolated = mtBranch == dragonTigerZhi
            val dtZhiName = CalendarConstants.DI_ZHI[dragonTigerZhi]
            checks.add(TabooCheck(
                name = "龙虎煞",
                priority = 1,
                groupTitle = "山家八煞",
                isViolated = dtViolated,
                detail = if (dtViolated) "${mtName}山犯龙虎煞（年${dtZhiName}），地气不和，家宅不宁" else "${mtName}山不犯龙虎煞",
                prohibition = "阴宅安葬、阳宅修造禁用"
            ))
        }

        if (mtBranch >= 0) {
            val xueRenZhi = (monthZhi + 11) % 12
            val xrViolated = mtBranch == xueRenZhi
            val xrZhiName = CalendarConstants.DI_ZHI[xueRenZhi]
            checks.add(TabooCheck(
                name = "血刃",
                priority = 2,
                groupTitle = "年家大煞",
                isViolated = xrViolated,
                detail = if (xrViolated) "${mtName}山犯血刃（月建${CalendarConstants.DI_ZHI[monthZhi]}），主血光意外" else "${mtName}山不犯血刃",
                prohibition = "动土、安葬、修方禁用，易引发施工事故"
            ))
        }

        if (mtBranch >= 0) {
            val baiHuZhi = (yearZhi + 4) % 12
            val bhViolated = mtBranch == baiHuZhi
            val bhZhiName = CalendarConstants.DI_ZHI[baiHuZhi]
            checks.add(TabooCheck(
                name = "年家白虎",
                priority = 2,
                groupTitle = "年家大煞",
                isViolated = bhViolated,
                detail = if (bhViolated) "${mtName}山犯年家白虎（年白虎在${bhZhiName}），主凶祸破财" else "${mtName}山不犯年家白虎",
                prohibition = "动土、安葬禁用"
            ))
        }

        val xiaoYueJianPalace = when (month) {
            1 -> 7; 2 -> 6; 3 -> 1; 4 -> 4; 5 -> 0; 6 -> 5
            7 -> 2; 8 -> 3; 9 -> 7; 10 -> 6; 11 -> 1; 12 -> 4
            else -> -1
        }
        val xyjViolated = palaceIdx == xiaoYueJianPalace
        val xyjPalaceName = if (xiaoYueJianPalace >= 0) ShanShaEngine.PALACE_NAMES[xiaoYueJianPalace] else ""
        checks.add(TabooCheck(
            name = "小月建",
            priority = 2,
            groupTitle = "年家大煞",
            isViolated = xyjViolated,
            detail = if (xyjViolated) "${mtName}山（${palaceName}宫）犯小月建（${month}月建${xyjPalaceName}方），主小事阻滞" else "${mtName}山不犯小月建",
            prohibition = "动土、修造慎用，安葬禁用"
        ))

        if (mtBranch >= 0) {
            val yearTriple = when (yearZhi) {
                0, 8 -> 0; 1, 9 -> 1; 2, 6, 10 -> 2
                else -> 3
            }
            val huangFanZhi = when (yearTriple) {
                0 -> 0; 1 -> 9; 2 -> 6; else -> 3
            }
            val baoWeiZhi = when (yearTriple) {
                0 -> 4; 1 -> 1; 2 -> 10; else -> 7
            }
            val hfViolated = mtBranch == huangFanZhi
            val bwViolated = mtBranch == baoWeiZhi
            val violated = hfViolated || bwViolated
            val label = if (hfViolated) "黄幡" else if (bwViolated) "豹尾" else ""
            checks.add(TabooCheck(
                name = "黄幡豹尾",
                priority = 2,
                groupTitle = "年家大煞",
                isViolated = violated,
                detail = if (violated) "${mtName}山犯${label}煞，主家运衰退、是非不断" else "${mtName}山不犯黄幡豹尾",
                prohibition = "安葬、修方禁用"
            ))
        }

        return checks
    }

    private fun buildDailyTabooChecks(
        year: Int, month: Int, day: Int,
        monthZhi: Int, dayGan: Int, dayZhi: Int,
        lunarMonth: Int, lunarDay: Int,
        solarTerms: List<SolarTermCalculator.SolarTermResult>
    ): List<TabooCheck> {
        val checks = mutableListOf<TabooCheck>()

        val fourLiTerms = setOf("春分", "秋分", "夏至", "冬至")
        val fourJueTerms = setOf("立春", "立夏", "立秋", "立冬")
        var isSiLi = false
        var isSiJue = false
        var liTermName = ""
        var jueTermName = ""

        for (term in solarTerms) {
            if (term.year == year && fourLiTerms.contains(term.name)) {
                val prevDay = prevDay(term.year, term.month, term.day)
                if (prevDay.first == year && prevDay.second == month && prevDay.third == day) {
                    isSiLi = true
                    liTermName = term.name
                    break
                }
            }
        }
        for (term in solarTerms) {
            if (term.year == year && fourJueTerms.contains(term.name)) {
                val prevDay = prevDay(term.year, term.month, term.day)
                if (prevDay.first == year && prevDay.second == month && prevDay.third == day) {
                    isSiJue = true
                    jueTermName = term.name
                    break
                }
            }
        }

        val siLiDayGanZhi = "${CalendarConstants.TIAN_GAN[dayGan]}${CalendarConstants.DI_ZHI[dayZhi]}"
        checks.add(TabooCheck(
            name = "四离日",
            priority = 3,
            groupTitle = "日家四大核心凶日",
            isViolated = isSiLi,
            detail = if (isSiLi) "${liTermName}前一日为四离日，${siLiDayGanZhi}日，主离散破败" else "非四离日",
            prohibition = "嫁娶、入宅、开业、安葬、动土、签约等所有大事一律禁用"
        ))

        checks.add(TabooCheck(
            name = "四绝日",
            priority = 3,
            groupTitle = "日家四大核心凶日",
            isViolated = isSiJue,
            detail = if (isSiJue) "${jueTermName}前一日为四绝日，${siLiDayGanZhi}日，主断绝消亡" else "非四绝日",
            prohibition = "安葬、动土、修方、开业、嫁娶一律禁用"
        ))

        val chongSangGan = when (lunarMonth) {
            1 -> 0; 2 -> 1; 3 -> 4; 4 -> 2; 5 -> 3; 6 -> 5
            7 -> 6; 8 -> 7; 9 -> 4; 10 -> 8; 11 -> 9; 12 -> 5
            else -> -1
        }
        val isChongSang = dayGan == chongSangGan
        checks.add(TabooCheck(
            name = "大重丧日",
            priority = 3,
            groupTitle = "日家核心凶日",
            isViolated = isChongSang,
            detail = if (isChongSang) "农历${lunarMonth}月${CalendarConstants.TIAN_GAN[dayGan]}日为大重丧日（正重丧），主横祸连丧" else "非大重丧日",
            prohibition = "嫁娶、入宅、开业、动土、安葬全大事禁用，无例外"
        ))

        val xiaoChongSangZhi = when (lunarMonth) {
            1 -> listOf(5, 11); 2 -> listOf(4, 10); 3 -> listOf(3, 9)
            4 -> listOf(2, 8); 5 -> listOf(1, 7); 6 -> listOf(0, 6)
            7 -> listOf(5, 11); 8 -> listOf(4, 10); 9 -> listOf(3, 9)
            10 -> listOf(2, 8); 11 -> listOf(1, 7); 12 -> listOf(0, 6)
            else -> emptyList()
        }
        val isXiaoChongSang = dayZhi in xiaoChongSangZhi
        checks.add(TabooCheck(
            name = "小重丧日",
            priority = 3,
            groupTitle = "日家核心凶日",
            isViolated = isXiaoChongSang,
            detail = if (isXiaoChongSang) "农历${lunarMonth}月${CalendarConstants.DI_ZHI[dayZhi]}日为小重丧日（月重丧），丧葬大忌" else "非小重丧日",
            prohibition = "安葬、修坟重大忌，其余大事慎用"
        ))

        val season = when (monthZhi) {
            2, 3, 4 -> 0
            5, 6, 7 -> 1
            8, 9, 10 -> 2
            else -> 3
        }
        val isZhengSiFei = when (season) {
            0 -> (dayGan == 6 && dayZhi == 8) || (dayGan == 7 && dayZhi == 9)
            1 -> (dayGan == 8 && dayZhi == 0) || (dayGan == 9 && dayZhi == 11)
            2 -> (dayGan == 0 && dayZhi == 2) || (dayGan == 1 && dayZhi == 3)
            else -> (dayGan == 2 && dayZhi == 6) || (dayGan == 3 && dayZhi == 5)
        }
        checks.add(TabooCheck(
            name = "正四废日",
            priority = 3,
            groupTitle = "日家核心凶日",
            isViolated = isZhengSiFei,
            detail = if (isZhengSiFei) "${siLiDayGanZhi}日为正四废日，百事无成" else "非正四废日",
            prohibition = "开业、求财、建房、嫁娶、入宅禁用"
        ))

        val yangGongDays = mapOf(
            1 to 13, 2 to 11, 3 to 9, 4 to 7, 5 to 5, 6 to 3,
            7 to 1, 8 to 27, 9 to 25, 10 to 23, 11 to 21, 12 to 19
        )
        val isYangGong = yangGongDays[lunarMonth] == lunarDay
        val isJuly29 = lunarMonth == 7 && lunarDay == 29
        val isYangGongDay = isYangGong || isJuly29
        checks.add(TabooCheck(
            name = "杨公忌日",
            priority = 4,
            groupTitle = "杨公忌日",
            isViolated = isYangGongDay,
            detail = if (isYangGongDay) "农历${lunarMonth}月${lunarDay}日为杨公忌日，大事忌用" else "非杨公忌日",
            prohibition = "嫁娶、入宅、开业、建房、安葬、动土、签约、搬家禁用；日常小事可用"
        ))

        return checks
    }

    private fun organizeByPriority(
        shanShaResult: ShanShaResult,
        extraMountainChecks: List<TabooCheck>,
        dailyChecks: List<TabooCheck>
    ): List<PriorityTabooGroup> {
        val groups = mutableListOf<PriorityTabooGroup>()

        val priority1Checks = mutableListOf<TabooCheck>()

        val existingP1Names = setOf("三煞", "岁破", "冲山", "阴府", "戊己都天")
        for (check in shanShaResult.checks) {
            when {
                check.name == "三煞" -> priority1Checks.add(TabooCheck("三煞", 1, "山家八煞", check.isViolated,
                    if (check.isViolated) "坐山犯年三煞，家宅不宁、是非官灾、破财伤人" else "不犯三煞",
                    "动土、安葬、修方绝对禁用"))
                check.name == "岁破" -> priority1Checks.add(TabooCheck("岁破", 1, "山家八煞", check.isViolated,
                    if (check.isViolated) "坐山犯岁破，家宅动荡、百事不顺、气场紊乱" else "不犯岁破",
                    "动土、安葬、入宅禁用"))
                check.name == "冲山" -> priority1Checks.add(TabooCheck("冲山", 1, "山家八煞", check.isViolated,
                    if (check.isViolated) "日支冲坐山，地气不和、施工不顺" else "不被日支所冲",
                    "动土、安葬禁用"))
                check.name == "阴府" -> priority1Checks.add(TabooCheck("阴府", 1, "山家八煞", check.isViolated,
                    if (check.isViolated) "坐山犯阴府煞，家宅有灾、破财损丁" else "不犯阴府",
                    "动土、安葬、修方禁用"))
                check.name == "戊己都天" -> priority1Checks.add(TabooCheck("戊己都天", 1, "山家八煞", check.isViolated,
                    if (check.isViolated) "坐山犯戊己都天，瘟疫灾祸、家人离散" else "不犯戊己都天",
                    "阳宅动土、阴宅安葬禁用"))
                check.name == "罗天大退" -> priority1Checks.add(TabooCheck("穿山罗天大退", 1, "山家八煞", check.isViolated,
                    if (check.isViolated) "坐山犯穿山罗天大退，家宅破败、运势倒退" else "不犯罗天大退",
                    "动土、修造、安葬禁用"))
            }
        }

        for (extra in extraMountainChecks) {
            if (extra.priority == 1) priority1Checks.add(extra)
        }

        val p1Violated = priority1Checks.any { it.isViolated }
        groups.add(PriorityTabooGroup(
            priority = 1,
            title = "山家八煞",
            subtitle = "关乎山家地气根基，是择日第一关。触发任意一项，动土安葬等与山家相关事宜一律禁用。",
            checks = priority1Checks,
            anyViolated = p1Violated,
            verdict = if (p1Violated) "绝对禁用" else "通过",
            prohibition = if (p1Violated) "动土、安葬、修方等与山家相关事宜一律不可用" else "无不犯事项"
        ))

        val priority2Checks = mutableListOf<TabooCheck>()

        for (check in shanShaResult.checks) {
            when (check.name) {
                "大将军" -> priority2Checks.add(TabooCheck("大将军", 2, "年家大煞", check.isViolated,
                    if (check.isViolated) "当年掌兵之煞，犯之主血光、是非、官灾" else "不犯大将军",
                    "动土、安葬、修方禁用"))
                "巡山罗睺" -> priority2Checks.add(TabooCheck("巡山罗睺", 2, "年家大煞", check.isViolated,
                    if (check.isViolated) "犯之主施工受阻、宅主遇险" else "不犯巡山罗睺",
                    "阴宅安葬、阳宅修造禁用"))
                "大月建" -> priority2Checks.add(TabooCheck("大月建", 2, "年家大煞", check.isViolated,
                    if (check.isViolated) "犯之主房屋根基不稳、财源耗散" else "不犯大月建",
                    "阳宅建房、修造禁用"))
                "土府" -> priority2Checks.add(TabooCheck("土府", 2, "年家大煞", check.isViolated,
                    if (check.isViolated) "犯之主施工塌方、宅基不稳" else "不犯土府",
                    "动土、安葬、修方禁用"))
            }
        }

        for (extra in extraMountainChecks) {
            if (extra.priority == 2) priority2Checks.add(extra)
        }

        val p2Violated = priority2Checks.any { it.isViolated }
        groups.add(PriorityTabooGroup(
            priority = 2,
            title = "年家大煞",
            subtitle = "当年天地气场中的大凶煞，主要影响动土、安葬、修造等大型用事。",
            checks = priority2Checks,
            anyViolated = p2Violated,
            verdict = if (p2Violated) "对应事宜禁用" else "通过",
            prohibition = if (p2Violated) "仅针对动土、安葬、修造、修方；日常小事可酌情规避" else "无不犯事项"
        ))

        val p3Violated = dailyChecks.filter { it.priority == 3 }.any { it.isViolated }
        groups.add(PriorityTabooGroup(
            priority = 3,
            title = "日家核心凶日",
            subtitle = "当日气场大凶，无论山家年家是否吉庆，人生大事一律禁用，无化解方法。",
            checks = dailyChecks.filter { it.priority == 3 },
            anyViolated = p3Violated,
            verdict = if (p3Violated) "全大事禁用" else "通过",
            prohibition = if (p3Violated) "所有人生重要事宜（嫁娶入宅开业动土安葬等）一律不可用" else "无禁忌"
        ))

        val p4Checks = dailyChecks.filter { it.priority == 4 }
        val p4Violated = p4Checks.any { it.isViolated }
        groups.add(PriorityTabooGroup(
            priority = 4,
            title = "杨公忌日",
            subtitle = "民间传统大忌日，主阻滞、破败、是非。大事严格禁用，小事可酌情使用。",
            checks = p4Checks,
            anyViolated = p4Violated,
            verdict = if (p4Violated) "大事禁用、小事慎用" else "通过",
            prohibition = if (p4Violated) "嫁娶入宅开业建房安葬动土签约搬家禁用；日常小事可用" else "无禁忌"
        ))

        return groups
    }

    private fun prevDay(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        if (day > 1) return Triple(year, month, day - 1)
        return if (month > 1) {
            val prevMonthDays = when (month - 1) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
                else -> 30
            }
            Triple(year, month - 1, prevMonthDays)
        } else {
            Triple(year - 1, 12, 31)
        }
    }
}