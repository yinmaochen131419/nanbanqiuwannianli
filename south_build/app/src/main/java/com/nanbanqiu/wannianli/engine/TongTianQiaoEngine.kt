/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

data class HourShenSha(
    val hour: Int,
    val hourZhi: String,
    val hourName: String,
    val shenSha: String,
    val isJi: Boolean,
    val isBestHour: Boolean,
    val isBlockedBySha: Boolean,
    val isBlockedByBenMing: Boolean,
    val coreUsage: String,
    val detailText: String
)

data class TongTianQiaoResult(
    val dayZhi: String,
    val hours: List<HourShenSha>,
    val bestHours: List<HourShenSha>,
    val worstHours: List<HourShenSha>,
    val mountainBlocked: Boolean,
    val mountainBlockReason: String,
    val recommendedHour: HourShenSha?,
    val secondaryHour: HourShenSha?,
    val summary: String,
    val benMingAdvisory: String,
    val caiWealthHour: HourShenSha?,
    val marriageHour: HourShenSha?,
    val repairHour: HourShenSha?,
    val dailyTabooNames: List<String>,
    val dailyTabooText: String
)

object TongTianQiaoEngine {

    private val SHEN_SHA_NAMES = arrayOf(
        "青龙", "明堂", "天刑", "朱雀",
        "金匮", "天德", "白虎", "玉堂",
        "天牢", "玄武", "司命", "勾陈"
    )

    private val SHEN_SHA_IS_JI = booleanArrayOf(
        true, true, false, false,
        true, true, false, true,
        false, false, true, false
    )

    private val SHEN_SHA_USAGE = arrayOf(
        "嫁娶、开业、出行大吉",
        "修造、安葬、签约",
        "忌动土、官司、是非",
        "忌口舌、吵架、文书纠纷",
        "求财、交易、收账、催财",
        "万能吉神，化解小凶",
        "忌血光、意外、嫁娶",
        "入宅、安家、喜事",
        "忌牢狱、纠纷、办事受阻",
        "忌破财、小人、被骗",
        "求医、祈福、出行平安",
        "忌拖延、阻滞、办事不顺"
    )

    fun evaluate(
        dayZhi: Int,
        dayGanZhi: String,
        sittingMountainIndex: Int,
        hasMountainFatalSha: Boolean,
        fatalShaNames: List<String>,
        benMingResult: BenMingResult?,
        caiStrength: String,
        dingStrength: String,
        guiStrength: String,
        dailyTaboos: List<String>
    ): TongTianQiaoResult {
        val qingLongHourZhi = (dayZhi % 6 * 2 + 8) % 12
        val startIndex = (12 - qingLongHourZhi) % 12

        val hours = mutableListOf<HourShenSha>()

        for (i in 0..11) {
            val shenShaIdx = (startIndex + i) % 12
            val hourZhiIdx = i
            val shenSha = SHEN_SHA_NAMES[shenShaIdx]
            val isJi = SHEN_SHA_IS_JI[shenShaIdx]
            val coreUsage = SHEN_SHA_USAGE[shenShaIdx]
            val hourZhi = CalendarConstants.DI_ZHI[hourZhiIdx]

            val isBlockedBySha = hasMountainFatalSha && !(shenSha == "天德" || shenSha == "司命" || shenSha == "青龙")
            val isBlockedByBenMing = if (benMingResult != null) {
                val bmZhi = benMingResult.benMingZhi
                hourZhiIdx == (bmZhi + 6) % 12
            } else false

            val isBestHour = isJi && !isBlockedBySha && !isBlockedByBenMing

            val detail = buildHourDetail(shenSha, isJi, isBlockedBySha, isBlockedByBenMing, hourZhi)

            hours.add(HourShenSha(
                hour = i,
                hourZhi = hourZhi,
                hourName = "${hourZhi}时（${when(i) { 0 -> "23-1"; 2 -> "3-5"; 4 -> "5-7"; 6 -> "7-9"; 8 -> "9-11"; 10 -> "11-13"; else -> when(i) { 1 -> "1-3"; 3 -> "3-5"; 5 -> "5-7"; 7 -> "7-9"; 9 -> "9-11"; 11 -> "21-23"; else -> "" }}}）",
                shenSha = shenSha,
                isJi = isJi,
                isBestHour = isBestHour,
                isBlockedBySha = isBlockedBySha,
                isBlockedByBenMing = isBlockedByBenMing,
                coreUsage = coreUsage,
                detailText = detail
            ))
        }

        val bestHours = hours.filter { it.isBestHour && it.isJi }
        val worstHours = hours.filter { !it.isJi || it.isBlockedBySha || it.isBlockedByBenMing }

        val recommendedHour = bestHours.firstOrNull()
        val secondaryHour = bestHours.getOrNull(1)

        val caiWealthHour = hours.find { it.isBestHour && it.shenSha == "金匮" }
            ?: hours.find { it.isJi && it.shenSha == "金匮" }
            ?: hours.find { it.isBestHour }

        val marriageHour = hours.find { it.isBestHour && (it.shenSha == "玉堂" || it.shenSha == "青龙") }
            ?: hours.find { it.isJi && (it.shenSha == "玉堂" || it.shenSha == "青龙") }
            ?: hours.find { it.isBestHour && it.shenSha == "明堂" }

        val repairHour = hours.find { it.isBestHour && it.shenSha == "明堂" }
            ?: hours.find { it.isJi && it.shenSha == "明堂" }
            ?: hours.find { it.isBestHour && it.shenSha == "天德" }
            ?: hours.find { it.isBestHour }

        val hasDailyTaboos = dailyTaboos.isNotEmpty()
        val dailyTabooText = if (hasDailyTaboos) {
            "${dailyTaboos.joinToString("、")}，虽有黄道吉时，但嫁娶、入宅、开业仍需谨慎，动土安葬绝对禁用。"
        } else ""

        val mountainBlockReason = if (hasMountainFatalSha) {
            "坐山犯${fatalShaNames.joinToString("、")}大煞，吉时亦需慎用；天德、司命、青龙时辰可酌情使用。"
        } else ""

        val summary = buildSummary(
            hasMountainFatalSha, fatalShaNames,
            recommendedHour, secondaryHour,
            caiWealthHour, marriageHour, repairHour,
            caiStrength, dingStrength, guiStrength,
            dailyTabooText
        )

        val benMingAdvisory = if (benMingResult != null) {
            val bmGz = benMingResult.benMingGanZhi
            val blockedHours = hours.filter { it.isBlockedByBenMing }
            if (blockedHours.isNotEmpty()) {
                "${bmGz}年命禁用时辰：${blockedHours.joinToString("、") { "${it.hourZhi}时" }}（冲克本命）。"
            } else {
                "${bmGz}年命与时辰无冲克，可用。"
            }
        } else ""

        return TongTianQiaoResult(
            dayZhi = CalendarConstants.DI_ZHI[dayZhi],
            hours = hours,
            bestHours = bestHours,
            worstHours = worstHours,
            mountainBlocked = hasMountainFatalSha,
            mountainBlockReason = mountainBlockReason,
            recommendedHour = recommendedHour,
            secondaryHour = secondaryHour,
            summary = summary,
            benMingAdvisory = benMingAdvisory,
            caiWealthHour = caiWealthHour,
            marriageHour = marriageHour,
            repairHour = repairHour,
            dailyTabooNames = dailyTaboos,
            dailyTabooText = dailyTabooText
        )
    }

    private fun buildHourDetail(
        shenSha: String, isJi: Boolean,
        isBlockedBySha: Boolean, isBlockedByBenMing: Boolean,
        hourZhi: String
    ): String {
        val sb = StringBuilder()
        when (shenSha) {
            "青龙" -> sb.append("青龙黄道吉时，万事大吉，利嫁娶、开业、出行签约。")
            "明堂" -> sb.append("明堂贵人时，利修造安葬、会见贵人、签约合作。")
            "天刑" -> sb.append("天刑黑道凶时，忌动土争执，大事不宜。")
            "朱雀" -> sb.append("朱雀黑道，主口舌官非，忌签约文书。")
            "金匮" -> sb.append("金匮黄道，利求财交易，是通天窍催财第一吉时。")
            "天德" -> sb.append("天德大吉时，万能吉神，可化解日常小凶煞。")
            "白虎" -> sb.append("白虎黑道，主血光意外，忌嫁娶出行。")
            "玉堂" -> sb.append("玉堂贵人时，利入宅安家、嫁娶喜事，百事吉利。")
            "天牢" -> sb.append("天牢黑道，忌诉讼办事，易受阻滞。")
            "玄武" -> sb.append("玄武黑道，主小人被骗破财，忌交易签约。")
            "司命" -> sb.append("司命吉时，利求医祈福、出行平安，天乙贵人时。")
            "勾陈" -> sb.append("勾陈黑道，主拖延阻滞，办事易受阻。")
        }
        if (isBlockedBySha) {
            sb.append("⚠ 坐山犯大煞，此时辰慎用。")
        }
        if (isBlockedByBenMing) {
            sb.append("❌ ${hourZhi}时冲克本命，不可用。")
        }
        return sb.toString()
    }

    private fun buildSummary(
        hasMountainFatalSha: Boolean,
        fatalShaNames: List<String>,
        recommendedHour: HourShenSha?,
        secondaryHour: HourShenSha?,
        caiWealthHour: HourShenSha?,
        marriageHour: HourShenSha?,
        repairHour: HourShenSha?,
        caiStrength: String,
        dingStrength: String,
        guiStrength: String,
        dailyTabooText: String
    ): String {
        val sb = StringBuilder()
        sb.append("通天窍择时以十二神煞定吉凶。")

        if (recommendedHour != null) {
            sb.append("本日首选【${recommendedHour.hourZhi}时·${recommendedHour.shenSha}】，${recommendedHour.shenSha}黄道，${recommendedHour.coreUsage}。")
        }
        if (secondaryHour != null) {
            sb.append("次选【${secondaryHour.hourZhi}时·${secondaryHour.shenSha}】。")
        }

        sb.append("用途导向：")
        if (caiWealthHour != null) {
            sb.append("求财优先${caiWealthHour.hourZhi}时${caiWealthHour.shenSha}（匹配斗首武曲催财）；")
        }
        if (marriageHour != null) {
            sb.append("嫁娶安家优先${marriageHour.hourZhi}时${marriageHour.shenSha}（匹配河洛催丁催贵）；")
        }
        if (repairHour != null) {
            sb.append("修造安葬优先${repairHour.hourZhi}时${repairHour.shenSha}（需结合乌兔太阳太阴）。")
        }

        if (dailyTabooText.isNotEmpty()) {
            sb.append("本日为${dailyTabooText}")
        }

        if (hasMountainFatalSha) {
            sb.append("因坐山犯${fatalShaNames.joinToString("、")}大煞，所有时辰需慎用，动土安葬须另择吉日。")
        } else {
            sb.append("坐山无凶煞，吉时可放心使用。")
        }

        sb.append("通天窍适合日常小事（出行、签约、办事）、嫁娶、入宅、开业；")
        sb.append("金匮、天德、青龙、明堂、玉堂、司命为六大黄道吉时，优先选用。")
        return sb.toString()
    }
}