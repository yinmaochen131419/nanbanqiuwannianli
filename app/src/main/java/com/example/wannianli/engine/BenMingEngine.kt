package com.example.wannianli.engine

data class BenMingResult(
    val benMingYear: Int,
    val benMingGanZhi: String,
    val benMingGan: Int,
    val benMingZhi: Int,
    val usageLabel: String,
    val dayGanZhi: String,
    val hourGanZhi: String,
    val clashWithDay: Boolean,
    val punishmentWithDay: Boolean,
    val harmWithDay: Boolean,
    val clashWithHour: Boolean,
    val punishmentWithHour: Boolean,
    val harmWithHour: Boolean,
    val dayHourSupport: Boolean,
    val dayHourDrain: Boolean,
    val benMingSanSha: Boolean,
    val benMingSuiPo: Boolean,
    val verdict: String,
    val verdictLabel: String,
    val verdictColor: Long,
    val analysis: String,
    val suitableYears: String,
    val unsuitableYears: String
)

object BenMingEngine {

    private val XING_PAIRS = setOf(
        setOf(0, 3), setOf(2, 5, 8), setOf(1, 7, 10),
        setOf(4, 6, 9, 11)
    )

    private val LIU_HAI = intArrayOf(7, 6, 5, 4, 3, 2, 1, 0, 11, 10, 9, 8)

    fun evaluate(
        benMingYear: Int,
        benMingGanZhi: String,
        dayGanZhi: String,
        hourGanZhi: String,
        usageLabel: String,
        dayGan: Int,
        dayZhi: Int,
        hourGan: Int,
        hourZhi: Int
    ): BenMingResult {
        val benMingGan = CalendarConstants.TIAN_GAN.indexOf(benMingGanZhi[0].toString())
        val benMingZhi = CalendarConstants.DI_ZHI.indexOf(benMingGanZhi[1].toString())

        val clashWithDay = benMingZhi == (dayZhi + 6) % 12
        val punishmentWithDay = checkXing(benMingZhi, dayZhi)
        val harmWithDay = LIU_HAI[benMingZhi] == dayZhi

        val clashWithHour = benMingZhi == (hourZhi + 6) % 12
        val punishmentWithHour = checkXing(benMingZhi, hourZhi)
        val harmWithHour = LIU_HAI[benMingZhi] == hourZhi

        val benMingWx = CalendarConstants.GAN_WUXING[benMingGan]
        val dayWx = CalendarConstants.GAN_WUXING[dayGan]
        val hourWx = CalendarConstants.GAN_WUXING[hourGan]
        val daySupport = isSheng(dayWx, benMingWx)
        val hourSupport = isSheng(hourWx, benMingWx)
        val dayHourSupport = daySupport || hourSupport
        val dayKe = isKe(dayWx, benMingWx)
        val hourKe = isKe(hourWx, benMingWx)
        val dayHourDrain = dayKe || hourKe

        val benMingSanSha = checkBenMingSanSha(benMingZhi, dayZhi, hourZhi)
        val benMingSuiPo = clashWithDay

        val hasFatal = clashWithDay || punishmentWithDay || benMingSuiPo
        val hasWarning = harmWithDay || clashWithHour || punishmentWithHour || harmWithHour || benMingSanSha || dayHourDrain

        val verdict: String
        val verdictLabel: String
        val verdictColor: Long
        val analysis: StringBuilder = StringBuilder()

        if (hasFatal) {
            verdict = "绝对禁用"
            verdictLabel = "绝对禁用"
            verdictColor = 0xFFC62828
            analysis.append("本命${benMingGanZhi}年，与")
            val reasons = mutableListOf<String>()
            if (clashWithDay) reasons.add("日柱${dayGanZhi}六冲")
            if (punishmentWithDay) reasons.add("日柱${dayGanZhi}相刑")
            if (benMingSuiPo) reasons.add("犯本命岁破")
            analysis.append(reasons.joinToString("、"))
            analysis.append("，此乃死刑级禁忌，")
        } else if (hasWarning) {
            verdict = "谨慎使用"
            verdictLabel = "谨慎使用"
            verdictColor = 0xFFF9A825
            analysis.append("本命${benMingGanZhi}年，")
            val warnings = mutableListOf<String>()
            if (harmWithDay) warnings.add("与日柱${dayGanZhi}六害")
            if (clashWithHour) warnings.add("与时柱${hourGanZhi}六冲（可换时辰）")
            if (punishmentWithHour) warnings.add("与时柱${hourGanZhi}相刑")
            if (harmWithHour) warnings.add("与时柱${hourGanZhi}六害")
            if (benMingSanSha) warnings.add("犯本命三煞")
            if (dayHourDrain) warnings.add("日时五行克泄年命")
            analysis.append(warnings.joinToString("；"))
            analysis.append("。")
        } else {
            verdict = "大吉可用"
            verdictLabel = "大吉可用"
            verdictColor = 0xFF2E7D32
            analysis.append("本命${benMingGanZhi}年，")
            if (dayHourSupport) {
                analysis.append("日时五行生扶本命，大利家运。")
            } else {
                analysis.append("无冲刑害，日课可用。")
            }
        }

        val suitableYears = buildSuitableYears(benMingZhi)
        val unsuitableYears = buildUnsuitableYears(benMingZhi)

        if (hasFatal) {
            analysis.append("此${usageLabel}不宜用此日课，${unsuitableYears}年命亦绝对禁用。")
        } else if (hasWarning) {
            analysis.append("得天德月德可化解轻微刑害，${usageLabel}用事需谨慎。适配${suitableYears}年命者最吉。")
        } else {
            analysis.append("${usageLabel}用事大吉，${suitableYears}年命最适配此日课。")
        }

        return BenMingResult(
            benMingYear = benMingYear,
            benMingGanZhi = benMingGanZhi,
            benMingGan = benMingGan,
            benMingZhi = benMingZhi,
            usageLabel = usageLabel,
            dayGanZhi = dayGanZhi,
            hourGanZhi = hourGanZhi,
            clashWithDay = clashWithDay,
            punishmentWithDay = punishmentWithDay,
            harmWithDay = harmWithDay,
            clashWithHour = clashWithHour,
            punishmentWithHour = punishmentWithHour,
            harmWithHour = harmWithHour,
            dayHourSupport = dayHourSupport,
            dayHourDrain = dayHourDrain,
            benMingSanSha = benMingSanSha,
            benMingSuiPo = benMingSuiPo,
            verdict = verdict,
            verdictLabel = verdictLabel,
            verdictColor = verdictColor,
            analysis = analysis.toString(),
            suitableYears = suitableYears,
            unsuitableYears = unsuitableYears
        )
    }

    private fun checkXing(zhiA: Int, zhiB: Int): Boolean {
        for (pair in XING_PAIRS) {
            if (pair.size == 2 && pair.contains(zhiA) && pair.contains(zhiB) && zhiA != zhiB) return true
            if (pair.size == 3 && pair.contains(zhiA) && pair.contains(zhiB) && zhiA != zhiB) return true
            if (pair.size >= 4 && zhiA == zhiB && pair.contains(zhiA)) return true
        }
        return false
    }

    private fun isSheng(a: Int, b: Int): Boolean {
        return (a == 0 && b == 1) || (a == 1 && b == 2) || (a == 2 && b == 3) ||
                (a == 3 && b == 4) || (a == 4 && b == 0)
    }

    private fun isKe(a: Int, b: Int): Boolean {
        return (a == 0 && b == 2) || (a == 2 && b == 4) || (a == 4 && b == 1) ||
                (a == 1 && b == 3) || (a == 3 && b == 0)
    }

    private fun checkBenMingSanSha(benMingZhi: Int, dayZhi: Int, hourZhi: Int): Boolean {
        val sanShaTriple = when (benMingZhi) {
            1, 5, 9 -> 2
            0, 4, 8 -> 6
            3, 7, 11 -> 10
            else -> 2
        }
        val sanShaZhi = sanShaTriple
        val sanShaAt = listOf(sanShaZhi, (sanShaZhi + 1) % 12, (sanShaZhi + 2) % 12)
        return sanShaAt.contains(dayZhi) || sanShaAt.contains(hourZhi)
    }

    private fun buildSuitableYears(benMingZhi: Int): String {
        val liuHe = (benMingZhi + 1) % 12
        val sanHe1 = (benMingZhi + 4) % 12
        val sanHe2 = (benMingZhi + 8) % 12
        val names = listOf(liuHe, sanHe1, sanHe2).map { CalendarConstants.DI_ZHI[it] }
        return "${names.joinToString("、")}年命最吉"
    }

    private fun buildUnsuitableYears(benMingZhi: Int): String {
        val chong = (benMingZhi + 6) % 12
        val hai = LIU_HAI[benMingZhi]
        val chongName = CalendarConstants.DI_ZHI[chong]
        val haiName = CalendarConstants.DI_ZHI[hai]
        return "${chongName}、${haiName}年命"
    }
}