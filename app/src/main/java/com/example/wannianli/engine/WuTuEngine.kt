package com.example.wannianli.engine

data class WuTuResult(
    val sunYearZhi: String,
    val sunMountain: String,
    val sunDirection: String,
    val sunArrivesAtShan: Boolean,
    val sunArrivesAtXiang: Boolean,
    val moonMonthZhi: String,
    val moonMountain: String,
    val moonDirection: String,
    val moonArrivesAtShan: Boolean,
    val moonArrivesAtXiang: Boolean,
    val hasFatalSha: Boolean,
    val fatalShaNames: List<String>,
    val verdict: String,
    val verdictBgColor: Long,
    val verdictTextColor: Long,
    val verdictSubText: String,
    val sunDetail: String,
    val moonDetail: String,
    val overallAnalysis: String,
    val ancientRule: String
)

object WuTuEngine {

    private val BRANCH_ZHI_NAMES = arrayOf(
        "子", "丑", "寅", "卯", "辰", "巳",
        "午", "未", "申", "酉", "戌", "亥"
    )

    private fun branchToMountainIndex(zhiIndex: Int): Int = zhiIndex * 2 + 1

    private fun mountainToXiangIndex(mountainIndex: Int): Int = (mountainIndex + 12) % 24

    fun evaluate(
        yearZhi: Int,
        monthZhi: Int,
        sittingMountainIndex: Int,
        shanShaResult: ShanShaResult
    ): WuTuResult {
        val sunMountainIdx = branchToMountainIndex(yearZhi)
        val sunXiangIdx = mountainToXiangIndex(sunMountainIdx)
        val sunMountainName = ShanShaEngine.MOUNTAIN_NAMES[sunMountainIdx] + "山"
        val sunXiangName = ShanShaEngine.MOUNTAIN_NAMES[sunXiangIdx] + "山"

        val moonMountainIdx = branchToMountainIndex(monthZhi)
        val moonXiangIdx = mountainToXiangIndex(moonMountainIdx)
        val moonMountainName = ShanShaEngine.MOUNTAIN_NAMES[moonMountainIdx] + "山"
        val moonXiangName = ShanShaEngine.MOUNTAIN_NAMES[moonXiangIdx] + "山"

        val sunArrivesAtShan = sittingMountainIndex == sunMountainIdx
        val sunArrivesAtXiang = sittingMountainIndex == sunXiangIdx
        val moonArrivesAtShan = sittingMountainIndex == moonMountainIdx
        val moonArrivesAtXiang = sittingMountainIndex == moonXiangIdx

        val fatalShaNames = shanShaResult.criticalViolations
            .filter { it.name in setOf("三煞", "岁破", "阴府", "戊己都天", "巡山罗睺") }
            .map { it.name }
        val hasFatalSha = fatalShaNames.isNotEmpty()

        val sittingName = ShanShaEngine.MOUNTAIN_NAMES[sittingMountainIndex] + "山"

        val sunDetail: String
        val moonDetail: String
        val overallAnalysis: String
        val verdict: String
        val verdictBgColor: Long
        val verdictTextColor: Long
        val verdictSubText: String

        if (hasFatalSha) {
            if (sunArrivesAtShan || sunArrivesAtXiang) {
                verdict = "慎用・可化煞"
                verdictBgColor = 0xFFF9A825
                verdictTextColor = 0xFF795548
                val targetLabel = if (sunArrivesAtShan) "到山" else "到向"
                verdictSubText = "太阳${targetLabel}，山家凶煞可化，动土安葬可择时辰慎用，日常喜事禁用"

                val shaList = fatalShaNames.joinToString("、")
                sunDetail = "太阳${targetLabel}・化煞：${sittingName}太阳${targetLabel}，可化解${shaList}大部分凶气，此课仅可用于阴宅安葬、阳宅修方动土，慎用；嫁娶、入宅、开业仍忌破日，不可用。"

                if (moonArrivesAtShan || moonArrivesAtXiang) {
                    val moonTarget = if (moonArrivesAtShan) "到山" else "到向"
                    moonDetail = "太阴${moonTarget}・催丁：太阴${moonTarget}，可助太阳化煞，催丁稳宅亦得加持。"
                } else {
                    moonDetail = "太阴不到・不催丁：太阴未到山向，催丁稳宅力量弱，仅得太阳地气加持。"
                }

                overallAnalysis = "太阳${targetLabel}，山家凶煞可化，此日可用作动土、修方、安葬，配合时辰择吉用事。嫁娶入宅开业仍忌。"
            } else {
                verdict = "大凶・禁用"
                verdictBgColor = 0xFFC62828
                verdictTextColor = 0xFFFFFFFF.toLong()
                verdictSubText = "太阳太阴不到，山家大煞无化解，绝对禁用"

                val shaList = fatalShaNames.joinToString("、")
                sunDetail = "太阳不到・无化解：${sittingName}犯${shaList}大煞，太阳未到山向南，煞气无法化解。"
                moonDetail = "太阴不到・无助：太阴未到山向，无任何加持之力。"
                overallAnalysis = "${sittingName}犯山家${shaList}大煞，太阳太阴双双不到，煞气无法化解，此日绝对禁用。动土安葬嫁娶皆忌。"
            }
        } else {
            val sunArrives = sunArrivesAtShan || sunArrivesAtXiang
            val moonArrives = moonArrivesAtShan || moonArrivesAtXiang

            if (sunArrives || moonArrives) {
                verdict = "大吉・加倍催旺"
                verdictBgColor = 0xFF2E7D32
                verdictTextColor = 0xFFFFFFFF.toLong()
                verdictSubText = "乌兔吉星到山到向，龙气加倍催旺，大吉大利"

                if (sunArrivesAtShan) {
                    sunDetail = "太阳到山・大吉：太阳到${sittingName}，凶煞消散，加倍催旺龙气。宜建房、安葬、修方、动土，百事大吉。"
                } else if (sunArrivesAtXiang) {
                    sunDetail = "太阳到向・大吉：太阳到${sittingName}（向），催福绵长。宜阳宅修造、开门放水、纳气催财。"
                } else {
                    sunDetail = "太阳不到：太阳不到山向，但仍得年乌兔之气，平稳无碍。"
                }

                if (moonArrivesAtShan) {
                    moonDetail = "太阴到山・催丁：太阴到${sittingName}，催丁稳宅，家宅安宁，人丁兴旺。"
                } else if (moonArrivesAtXiang) {
                    moonDetail = "太阴到向・催财：太阴到${sittingName}（向），财禄自来，纳福催财。"
                } else {
                    moonDetail = "太阴不到：太阴不到山向，催丁催财力弱，但得太阳之力已足。"
                }

                overallAnalysis = "山家无煞，乌兔吉星到位，龙气加倍催旺。此课宜建房、安葬、修方、动土，大吉大利。"
            } else {
                verdict = "平稳・无加持"
                verdictBgColor = 0xFF757575
                verdictTextColor = 0xFFFFFFFF.toLong()
                verdictSubText = "乌兔吉星不到，无加持之力，平稳可用"

                sunDetail = "太阳不到：太阳不到山向，无催旺化煞之力。"
                moonDetail = "太阴不到：太阴不到山向，无催丁催财之力。"
                overallAnalysis = "山家无凶煞，乌兔吉星未到山向，无额外加持。此课可用，但无乌兔催旺之效。"
            }
        }

        val ancientRule = "古法铁律：太阳到山到向，可解山家诸煞；日家破日、本命冲克，吉神无力化解。乌兔只化解山家地气煞。"

        return WuTuResult(
            sunYearZhi = BRANCH_ZHI_NAMES[yearZhi],
            sunMountain = sunMountainName,
            sunDirection = sunXiangName,
            sunArrivesAtShan = sunArrivesAtShan,
            sunArrivesAtXiang = sunArrivesAtXiang,
            moonMonthZhi = BRANCH_ZHI_NAMES[monthZhi],
            moonMountain = moonMountainName,
            moonDirection = moonXiangName,
            moonArrivesAtShan = moonArrivesAtShan,
            moonArrivesAtXiang = moonArrivesAtXiang,
            hasFatalSha = hasFatalSha,
            fatalShaNames = fatalShaNames,
            verdict = verdict,
            verdictBgColor = verdictBgColor,
            verdictTextColor = verdictTextColor,
            verdictSubText = verdictSubText,
            sunDetail = sunDetail,
            moonDetail = moonDetail,
            overallAnalysis = overallAnalysis,
            ancientRule = ancientRule
        )
    }
}