package com.example.wannianli.engine

import kotlin.math.floor

data class ZeRiResult(
    val dayGanZhi: String,
    val jianchu: String,
    val jianchuJiXiong: String,
    val twentyEightMansion: String,
    val mansionJiXiong: String,
    val sanSha: Boolean,
    val suiPo: Boolean,
    val yuePo: Boolean,
    val jieSha: Boolean,
    val zaiSha: Boolean,
    val tianDe: Boolean,
    val yueDe: Boolean,
    val wuXingScore: String,
    val overallScore: Int,
    val overallVerdict: String,
    val suggestions: List<String>,
    val taboos: List<String>,
    val wuXingDetail: String
)

data class MountainAnalysisResult(
    val fourPillarWuXing: String,
    val mountainWuXingMatch: String,
    val overriddenScore: Int,
    val overriddenVerdict: String,
    val overriddenReason: String,
    val shanShaPriorityText: String,
    val mountainYi: List<String>,
    val mountainJi: List<String>
)

data class ZeRiMonthResult(
    val year: Int,
    val month: Int,
    val days: List<ZeRiResult>,
    val bestDays: List<ZeRiResult>,
    val goodDays: List<ZeRiResult>
)

object ZeRiEngine {

    fun evaluateDay(
        year: Int,
        month: Int,
        day: Int,
        yearGanZhi: String,
        monthGanZhi: String,
        dayGanZhi: String
    ): ZeRiResult {
        val yearGan = CalendarConstants.TIAN_GAN.indexOf(yearGanZhi[0].toString())
        val yearZhi = CalendarConstants.DI_ZHI.indexOf(yearGanZhi[1].toString())
        val monthZhi = CalendarConstants.DI_ZHI.indexOf(monthGanZhi[1].toString())
        val dayGan = CalendarConstants.TIAN_GAN.indexOf(dayGanZhi[0].toString())
        val dayZhi = CalendarConstants.DI_ZHI.indexOf(dayGanZhi[1].toString())

        val jianchuIndex = ((monthZhi % 12) + (dayZhi - monthZhi + 12) % 12) % 12
        val jianchuName = CalendarConstants.JIANCHU_NAMES[jianchuIndex]
        val jianchuJiXiong = if (CalendarConstants.JIANCHU_JIXIONG[jianchuIndex] > 0) "吉" else if (CalendarConstants.JIANCHU_JIXIONG[jianchuIndex] < 0) "凶" else "平"

        val mansionIndex = calcTwentyEightMansion(year, month, day)
        val mansionName = CalendarConstants.TWENTY_EIGHT_MANSIONS[mansionIndex]
        val mansionJiXiong = if (CalendarConstants.TWENTY_EIGHT_JIXIONG[mansionIndex] > 0) "吉" else "凶"

        val sanShaZhi = CalendarConstants.getSanSha(yearZhi)
        val isSanSha = sanShaZhi.contains(dayZhi)

        val isSuiPo = dayZhi == CalendarConstants.getSuiPo(yearZhi)

        val isYuePo = dayZhi == CalendarConstants.getYuePo(monthZhi)

        val isJieSha = dayZhi == CalendarConstants.getJieSha(yearZhi)

        val isZaiSha = dayZhi == CalendarConstants.getZaiSha(yearZhi)

        val tianDeGan = CalendarConstants.TIAN_DE_MONTH[monthZhi] ?: -1
        val isTianDe = dayGan == tianDeGan

        val yueDeGan = CalendarConstants.YUE_DE_MONTH[monthZhi] ?: -1
        val isYueDe = dayGan == yueDeGan

        val dayWuXing = CalendarConstants.GAN_WUXING[dayGan]
        val yearWuXing = CalendarConstants.GAN_WUXING[yearGan]
        val monthWuXing = CalendarConstants.GAN_WUXING[(CalendarConstants.TIAN_GAN.indexOf(monthGanZhi[0].toString()))]
        val wuXingScore = calcWuXingScore(dayWuXing, yearWuXing, monthWuXing)
        val wuXingDetail = buildWuXingDetail(dayGan, dayWuXing, yearWuXing, monthWuXing)

        val suggestions = mutableListOf<String>()
        val taboos = mutableListOf<String>()
        var score = 60

        if (isSanSha) { score -= 25; taboos.add("犯年三煞（大凶，诸事不宜）") }
        if (isSuiPo) { score -= 20; taboos.add("岁破日（与太岁相冲，不宜用事）") }
        if (isYuePo) { score -= 15; taboos.add("月破日（月令相冲，不宜用事）") }
        if (isJieSha) { score -= 10; taboos.add("劫煞日（慎用）") }
        if (isZaiSha) { score -= 10; taboos.add("灾煞日（慎用）") }
        if (jianchuName == "破") { score -= 10; taboos.add("破日（大凶，百事不宜）") }
        if (jianchuName == "闭") { score -= 5; taboos.add("闭日（不宜兴造嫁娶）") }

        if (jianchuName == "开" || jianchuName == "成") { score += 10; suggestions.add("${jianchuName}日（大吉，百事皆宜）") }
        if (jianchuName == "除" || jianchuName == "定" || jianchuName == "执" || jianchuName == "危") {
            score += 5; suggestions.add("${jianchuName}日（吉利）")
        }
        if (isTianDe) { score += 12; suggestions.add("天德日（上天之德，诸事大吉）") }
        if (isYueDe) { score += 10; suggestions.add("月德日（月中之德，百福并集）") }
        if (CalendarConstants.TWENTY_EIGHT_JIXIONG[mansionIndex] > 0) {
            score += 5; suggestions.add("值日星宿：${mansionName}（吉）")
        } else {
            score -= 3; taboos.add("值日星宿：${mansionName}（凶）")
        }

        score += wuXingScore

        score = score.coerceIn(0, 100)

        val verdict = when {
            score >= 80 -> "大吉"
            score >= 65 -> "吉"
            score >= 50 -> "平"
            score >= 35 -> "小凶"
            else -> "大凶"
        }

        return ZeRiResult(
            dayGanZhi = dayGanZhi,
            jianchu = jianchuName,
            jianchuJiXiong = jianchuJiXiong,
            twentyEightMansion = mansionName,
            mansionJiXiong = mansionJiXiong,
            sanSha = isSanSha,
            suiPo = isSuiPo,
            yuePo = isYuePo,
            jieSha = isJieSha,
            zaiSha = isZaiSha,
            tianDe = isTianDe,
            yueDe = isYueDe,
            wuXingScore = buildWuXingScoreText(wuXingScore),
            overallScore = score,
            overallVerdict = verdict,
            suggestions = suggestions.toList(),
            taboos = taboos.toList(),
            wuXingDetail = wuXingDetail
        )
    }

    fun evaluateMonth(
        year: Int,
        month: Int,
        toJD: (Int, Int, Int) -> Double,
        calcDayPillar: (Int, Int, Int) -> String,
        calcYearPillar: (Int, Int) -> String,
        calcMonthPillar: (Int, Int, Int, List<SolarTermCalculator.SolarTermResult>) -> String,
        getMonthDayCount: (Int, Int) -> Int
    ): ZeRiMonthResult {
        val allTermsThisYear = SolarTermCalculator.calculateSolarTerms(year).map {
            val actualYear = if (it.month == 1) year + 1 else year
            SolarTermCalculator.SolarTermResult(it.name, actualYear, it.month, it.day, it.hour, it.minute, it.second)
        }
        val allTermsLastYear = SolarTermCalculator.calculateSolarTerms(year - 1).map {
            val actualYear = if (it.month == 1) year else year - 1
            SolarTermCalculator.SolarTermResult(it.name, actualYear, it.month, it.day, it.hour, it.minute, it.second)
        }
        val combinedTerms = allTermsLastYear + allTermsThisYear

        val yearGanZhi = calcYearPillar(year, month)

        val dayCount = getMonthDayCount(year, month)
        val results = mutableListOf<ZeRiResult>()

        for (d in 1..dayCount) {
            val dayGanZhi = calcDayPillar(year, month, d)
            val monthGanZhi = calcMonthPillar(year, month, d, combinedTerms)
            val result = evaluateDay(year, month, d, yearGanZhi, monthGanZhi, dayGanZhi)
            results.add(result)
        }

        val bestDays = results.filter { it.overallScore >= 80 }.sortedByDescending { it.overallScore }
        val goodDays = results.filter { it.overallScore in 65..79 }.sortedByDescending { it.overallScore }

        return ZeRiMonthResult(year, month, results, bestDays, goodDays)
    }

    private fun calcTwentyEightMansion(year: Int, month: Int, day: Int): Int {
        val jd = gregorianToJD(year, month, day)
        val baseJd = gregorianToJD(2025, 1, 1)
        val dayDiff = (jd - baseJd).toLong()
        val mansionIndex = ((dayDiff % 28 + 28) % 28).toInt()
        return mansionIndex
    }

    private fun calcWuXingScore(dayWuXing: Int, yearWuXing: Int, monthWuXing: Int): Int {
        var score = 0
        if (isWuXingSheng(yearWuXing, dayWuXing)) score += 5
        if (isWuXingSheng(monthWuXing, dayWuXing)) score += 5
        if (dayWuXing == yearWuXing) score += 3
        if (dayWuXing == monthWuXing) score += 3
        if (isWuXingKe(yearWuXing, dayWuXing)) score -= 5
        if (isWuXingKe(monthWuXing, dayWuXing)) score -= 5
        return score
    }

    private fun isWuXingSheng(a: Int, b: Int): Boolean {
        return (a == 0 && b == 1) || (a == 1 && b == 2) || (a == 2 && b == 3) || (a == 3 && b == 4) || (a == 4 && b == 0)
    }

    private fun isWuXingKe(a: Int, b: Int): Boolean {
        return (a == 0 && b == 2) || (a == 2 && b == 4) || (a == 4 && b == 1) || (a == 1 && b == 3) || (a == 3 && b == 0)
    }

    private fun buildWuXingDetail(dayGan: Int, dayWuXing: Int, yearWuXing: Int, monthWuXing: Int): String {
        val dayWxName = CalendarConstants.WUXING[dayWuXing]
        val yearWxName = CalendarConstants.WUXING[yearWuXing]
        val monthWxName = CalendarConstants.WUXING[monthWuXing]
        val dayGanName = CalendarConstants.TIAN_GAN[dayGan]

        val sb = StringBuilder("日干${dayGanName}属${dayWxName}，")
        if (isWuXingSheng(yearWuXing, dayWuXing)) sb.append("年干${yearWxName}生${dayWxName}（吉），")
        if (isWuXingSheng(monthWuXing, dayWuXing)) sb.append("月干${monthWxName}生${dayWxName}（吉），")
        if (dayWuXing == yearWuXing) sb.append("与年干同为${dayWxName}（比和），")
        if (dayWuXing == monthWuXing) sb.append("与月干同为${dayWxName}（比和），")
        if (isWuXingKe(yearWuXing, dayWuXing)) sb.append("年干${yearWxName}克${dayWxName}（不吉），")
        if (isWuXingKe(monthWuXing, dayWuXing)) sb.append("月干${monthWxName}克${dayWxName}（不吉），")
        return sb.toString().trimEnd(',')
    }

    private fun buildWuXingScoreText(score: Int): String {
        return when {
            score > 0 -> "五行和合（+${score}）"
            score < 0 -> "五行不合（${score}）"
            else -> "五行中和（0）"
        }
    }

    fun buildMountainAnalysis(
        yearGanZhi: String,
        monthGanZhi: String,
        dayGanZhi: String,
        hourGanZhi: String,
        sittingMountainIndex: Int,
        baseResult: ZeRiResult,
        shanShaResult: ShanShaResult
    ): MountainAnalysisResult {
        val fourPillarWuXing = buildFourPillarWuXingDetail(yearGanZhi, monthGanZhi, dayGanZhi, hourGanZhi)
        val mountainWuXingMatch = buildMountainWuXingMatch(
            yearGanZhi, monthGanZhi, dayGanZhi, hourGanZhi,
            sittingMountainIndex
        )

        val criticalNames = shanShaResult.criticalViolations.map { it.name }.toSet()
        val hasFatalSha = criticalNames.any { it in setOf("三煞", "岁破", "阴府", "戊己都天") }
        val criticalCount = shanShaResult.criticalViolations.size

        val overriddenScore: Int
        val overriddenVerdict: String
        val overriddenReason: String

        if (hasFatalSha) {
            overriddenScore = when {
                criticalCount >= 3 -> 5
                criticalCount >= 2 -> 15
                else -> 25
            }
            overriddenVerdict = "大凶禁用"
            val shaNames = shanShaResult.criticalViolations.joinToString("、") { it.name }
            overriddenReason = "五行虽吉，但犯山家${shaNames}等大煞，建房、安葬、修造绝对禁用。日课再好，犯山家大煞一律作废。"
        } else if (criticalCount > 0) {
            overriddenScore = when {
                criticalCount >= 2 -> 30
                else -> 40
            }
            overriddenVerdict = "慎用"
            val shaNames = shanShaResult.criticalViolations.joinToString("、") { it.name }
            overriddenReason = "犯山家${shaNames}，虽非四大凶煞，但仍需谨慎。建议另择吉日。"
        } else {
            overriddenScore = -1
            overriddenVerdict = ""
            overriddenReason = ""
        }

        val shanShaPriorityText = "日家吉神（天德、月德）只能化解破日、白虎等日常小煞，绝对化解不了山家岁破、三煞、阴府等地气大煞。山家犯大煞，吉神无力回天，动土安葬必凶。"

        val mountainYi = buildMountainYi(baseResult, shanShaResult, sittingMountainIndex)
        val mountainJi = buildMountainJi(baseResult, shanShaResult, sittingMountainIndex)

        return MountainAnalysisResult(
            fourPillarWuXing = fourPillarWuXing,
            mountainWuXingMatch = mountainWuXingMatch,
            overriddenScore = overriddenScore,
            overriddenVerdict = overriddenVerdict,
            overriddenReason = overriddenReason,
            shanShaPriorityText = shanShaPriorityText,
            mountainYi = mountainYi,
            mountainJi = mountainJi
        )
    }

    private fun buildFourPillarWuXingDetail(
        yearGanZhi: String, monthGanZhi: String, dayGanZhi: String, hourGanZhi: String
    ): String {
        val pillars = listOf(yearGanZhi, monthGanZhi, dayGanZhi, hourGanZhi)
        val wxCount = IntArray(5)

        for (p in pillars) {
            val g = CalendarConstants.TIAN_GAN.indexOf(p[0].toString())
            val z = CalendarConstants.DI_ZHI.indexOf(p[1].toString())
            if (g >= 0) wxCount[CalendarConstants.GAN_WUXING[g]]++
            if (z >= 0) wxCount[CalendarConstants.ZHI_WUXING[z]]++
        }

        val wxNames = CalendarConstants.WUXING
        val strengthDesc = mutableListOf<String>()
        for (i in 0..4) {
            val desc = when {
                wxCount[i] >= 4 -> "${wxNames[i]}极旺"
                wxCount[i] >= 3 -> "${wxNames[i]}旺"
                wxCount[i] >= 2 -> "${wxNames[i]}平"
                wxCount[i] >= 1 -> "${wxNames[i]}弱"
                else -> "${wxNames[i]}缺"
            }
            strengthDesc.add(desc)
        }

        val sb = StringBuilder("四柱干支五行：${pillars.joinToString(" ")} → ")
        sb.append(strengthDesc.joinToString("、"))
        return sb.toString()
    }

    private fun buildMountainWuXingMatch(
        yearGanZhi: String, monthGanZhi: String, dayGanZhi: String, hourGanZhi: String,
        sittingMountainIndex: Int
    ): String {
        val palaceIdx = ShanShaEngine.MOUNTAIN_PALACE[sittingMountainIndex]
        val palaceName = ShanShaEngine.PALACE_NAMES[palaceIdx]
        val mountainName = ShanShaEngine.MOUNTAIN_NAMES[sittingMountainIndex]

        val palaceWxIndex = PALACE_WUXING[palaceIdx]
        val palaceWxName = CalendarConstants.WUXING[palaceWxIndex]

        val pillars = listOf(yearGanZhi, monthGanZhi, dayGanZhi, hourGanZhi)
        val wxCount = IntArray(5)
        for (p in pillars) {
            val g = CalendarConstants.TIAN_GAN.indexOf(p[0].toString())
            val z = CalendarConstants.DI_ZHI.indexOf(p[1].toString())
            if (g >= 0) wxCount[CalendarConstants.GAN_WUXING[g]]++
            if (z >= 0) wxCount[CalendarConstants.ZHI_WUXING[z]]++
        }

        val maxWx = (0..4).maxByOrNull { wxCount[it] } ?: 0
        val maxWxName = CalendarConstants.WUXING[maxWx]

        val sb = StringBuilder("${mountainName}山属${palaceName}宫${palaceWxName}。")

        if (isWuXingSheng(maxWx, palaceWxIndex)) {
            sb.append("此课${maxWxName}最旺，生扶坐山${palaceWxName}，五行相生，大利扶山。")
        } else if (isWuXingKe(maxWx, palaceWxIndex)) {
            sb.append("此课${maxWxName}极旺，克坐山${palaceWxName}，五行克山，大凶。扶山为择日第一要义，五行克山则吉神无用。")
        } else if (maxWx == palaceWxIndex) {
            sb.append("此课${maxWxName}旺，与坐山同为${palaceWxName}，比和助旺，扶山有力。")
        } else if (isWuXingKe(palaceWxIndex, maxWx)) {
            sb.append("坐山${palaceWxName}生此课${maxWxName}，泄山之气，扶山力弱。")
        } else {
            sb.append("此课${maxWxName}偏旺，与坐山${palaceWxName}无直接生克，扶山之力平平。")
        }

        return sb.toString()
    }

    private fun buildMountainYi(
        baseResult: ZeRiResult,
        shanShaResult: ShanShaResult,
        sittingMountainIndex: Int
    ): List<String> {
        val mountainName = ShanShaEngine.MOUNTAIN_NAMES[sittingMountainIndex]
        val hasFatalSha = shanShaResult.criticalViolations.any {
            it.name in setOf("三煞", "岁破", "阴府", "戊己都天") && it.severity >= 3
        }
        val hasCritical = shanShaResult.criticalViolations.isNotEmpty()

        val yi = mutableListOf<String>()

        if (hasFatalSha) {
            yi.add("仅宜日常小事（求财、签约、出行访友、祭祀祈福）")
            if (baseResult.tianDe || baseResult.yueDe) {
                yi.add("天德月德护身，小事可用，绝不碰动土安葬")
            }
        } else if (hasCritical) {
            yi.add("求财、签约、出行、祭祀")
            if (baseResult.jianchuJiXiong == "吉" || baseResult.jianchu == "成" || baseResult.jianchu == "开") {
                yi.add("${baseResult.jianchu}日小吉，日常事务可用")
            }
        } else {
            yi.add("${mountainName}山建房、安葬、修方、动土")
            if (baseResult.jianchuJiXiong == "吉") {
                yi.add("嫁娶、开业、入宅、出行")
            }
            if (baseResult.tianDe) yi.add("天德日，${mountainName}山诸事大吉")
            if (baseResult.yueDe) yi.add("月德日，${mountainName}山百福并集")
        }

        return yi.distinct()
    }

    private fun buildMountainJi(
        baseResult: ZeRiResult,
        shanShaResult: ShanShaResult,
        sittingMountainIndex: Int
    ): List<String> {
        val mountainName = ShanShaEngine.MOUNTAIN_NAMES[sittingMountainIndex]
        val hasFatalSha = shanShaResult.criticalViolations.any {
            it.name in setOf("三煞", "岁破", "阴府", "戊己都天") && it.severity >= 3
        }

        val ji = mutableListOf<String>()

        if (hasFatalSha) {
            ji.add("${mountainName}山建房、安葬、修方、动土（犯山家大煞，绝对禁用）")
            ji.add("嫁娶、入宅、开业（山家犯大煞，大事不宜）")
            val shaNames = shanShaResult.criticalViolations.joinToString("、") { it.name }
            ji.add("犯${shaNames}，不可用！择日第一铁则：山家犯大煞，五行再好直接作废。")
        } else {
            if (baseResult.jianchu == "破") ji.add("破日大忌：嫁娶、入宅、开业、安葬")
            if (baseResult.jianchu == "闭") ji.add("闭日忌：开业、出行、动土")
            if (baseResult.jianchu == "建") ji.add("建日忌：安葬、破土、开仓")

            val criticalNames = shanShaResult.criticalViolations.map { it.name }
            if (criticalNames.isNotEmpty()) {
                ji.add("${mountainName}山犯${criticalNames.joinToString("、")}，建议另择吉日")
            }
            val warningNames = shanShaResult.warningViolations.map { it.name }
            if (warningNames.isNotEmpty()) {
                ji.add("${mountainName}山犯${warningNames.joinToString("、")}，用事需谨慎")
            }
        }

        return ji.distinct()
    }

    val PALACE_WUXING = intArrayOf(4, 2, 0, 0, 1, 2, 3, 3)

    private fun gregorianToJD(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }
}