package com.example.wannianli.engine

object HeLuoEngine {

    val BAGUA_NAMES = arrayOf("", "坎", "坤", "震", "巽", "", "乾", "兑", "艮", "离")

    val BAGUA_WUXING = intArrayOf(0, 4, 2, 0, 0, 0, 3, 3, 2, 1)

    val BAGUA_YINYANG = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

    val BAGUA_DIRECTION = arrayOf("", "北", "西南", "东", "东南", "", "西北", "西", "东北", "南")

    val BAGUA_ATTRIBUTE = arrayOf("", "水", "地", "雷", "风", "", "天", "泽", "山", "火")

    val BAGUA_LUOSHU = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

    val BAGUA_HETU_YANG = intArrayOf(0, 1, 5, 3, 3, 0, 4, 4, 5, 2)

    val BAGUA_HETU_YIN = intArrayOf(0, 6, 10, 8, 8, 0, 9, 9, 10, 7)

    val GAN_NA_GUA = intArrayOf(6, 8, 8, 2, 1, 9, 3, 6, 4, 2)

    val ZHI_NA_GUA = intArrayOf(1, 8, 8, 3, 4, 4, 9, 8, 8, 7, 6, 6)

    val SIXTY_FOUR_GUA_NAMES = arrayOf(
        "坤为地", "山地剥", "水地比", "风地观", "雷地豫", "火地晋", "泽地萃", "天地否",
        "地山谦", "艮为山", "水山蹇", "风山渐", "雷山小过", "火山旅", "泽山咸", "天山遁",
        "地水师", "山水蒙", "坎为水", "风水涣", "雷水解", "火水未济", "泽水困", "天水讼",
        "地风升", "山风蛊", "水风井", "巽为风", "雷风恒", "火风鼎", "泽风大过", "天风姤",
        "地雷复", "山雷颐", "水雷屯", "风雷益", "震为雷", "火雷噬嗑", "泽雷随", "天雷无妄",
        "地火明夷", "山火贲", "水火既济", "风火家人", "雷火丰", "离为火", "泽火革", "天火同人",
        "地泽临", "山泽损", "水泽节", "风泽中孚", "雷泽归妹", "火泽睽", "兑为泽", "天泽履",
        "地天泰", "山天大畜", "水天需", "风天小畜", "雷天大壮", "火天大有", "泽天夬", "乾为天"
    )

    val GUA_JIXIONG = intArrayOf(
        0, -1, 1, 0, 1, 1, 0, -1,
        0, 0, -1, 0, 0, 0, 1, 0,
        1, -1, 0, 0, 1, -1, -1, -1,
        0, -1, 0, 0, 1, 1, -1, -1,
        1, 1, -1, 1, 0, 0, 1, 1,
        -1, 0, 1, 1, 1, 0, 0, 1,
        1, 0, 1, 1, 0, -1, 0, 1,
        1, 1, 1, 1, 1, 1, 1, 0
    )

    val GUA_JIXIONG_VERDICT = arrayOf(
        "平", "小凶", "吉", "平", "吉", "吉", "平", "小凶",
        "平", "平", "小凶", "平", "平", "平", "吉", "平",
        "吉", "小凶", "平", "平", "吉", "小凶", "小凶", "小凶",
        "平", "小凶", "平", "平", "吉", "吉", "小凶", "小凶",
        "吉", "吉", "小凶", "吉", "平", "平", "吉", "吉",
        "小凶", "平", "吉", "吉", "吉", "平", "平", "吉",
        "吉", "平", "吉", "吉", "平", "小凶", "平", "吉",
        "吉", "吉", "吉", "吉", "吉", "吉", "吉", "平"
    )

    fun getStemNaGua(ganIndex: Int): Int = GAN_NA_GUA[ganIndex]
    fun getBranchNaGua(zhiIndex: Int): Int = ZHI_NA_GUA[zhiIndex]

    private val LUOSHU_TO_ARRAY_INDEX = intArrayOf(0, 2, 0, 4, 3, 0, 7, 6, 1, 5)

    fun getHexagramIndex(upperGua: Int, lowerGua: Int): Int {
        val u = LUOSHU_TO_ARRAY_INDEX[upperGua]
        val l = LUOSHU_TO_ARRAY_INDEX[lowerGua]
        return u * 8 + l
    }

    fun getHexagramName(upperGua: Int, lowerGua: Int): String {
        return SIXTY_FOUR_GUA_NAMES[getHexagramIndex(upperGua, lowerGua)]
    }

    fun getGuaJiXiong(upperGua: Int, lowerGua: Int): Int {
        return GUA_JIXIONG[getHexagramIndex(upperGua, lowerGua)]
    }

    fun getGuaVerdict(upperGua: Int, lowerGua: Int): String {
        return GUA_JIXIONG_VERDICT[getHexagramIndex(upperGua, lowerGua)]
    }

    fun evaluate(
        yearGanZhi: String,
        monthGanZhi: String,
        dayGanZhi: String,
        hourGanZhi: String,
        sittingMountainIndex: Int = -1,
        hasFatalTaboo: Boolean = false
    ): HeLuoResult {
        val yearGan = CalendarConstants.TIAN_GAN.indexOf(yearGanZhi[0].toString())
        val yearZhi = CalendarConstants.DI_ZHI.indexOf(yearGanZhi[1].toString())
        val monthGan = CalendarConstants.TIAN_GAN.indexOf(monthGanZhi[0].toString())
        val monthZhi = CalendarConstants.DI_ZHI.indexOf(monthGanZhi[1].toString())
        val dayGan = CalendarConstants.TIAN_GAN.indexOf(dayGanZhi[0].toString())
        val dayZhi = CalendarConstants.DI_ZHI.indexOf(dayGanZhi[1].toString())
        val hourGan = CalendarConstants.TIAN_GAN.indexOf(hourGanZhi[0].toString())
        val hourZhi = CalendarConstants.DI_ZHI.indexOf(hourGanZhi[1].toString())

        val yearUpperGua = getStemNaGua(yearGan)
        val yearLowerGua = getBranchNaGua(yearZhi)
        val monthUpperGua = getStemNaGua(monthGan)
        val monthLowerGua = getBranchNaGua(monthZhi)
        val dayUpperGua = getStemNaGua(dayGan)
        val dayLowerGua = getBranchNaGua(dayZhi)
        val hourUpperGua = getStemNaGua(hourGan)
        val hourLowerGua = getBranchNaGua(hourZhi)

        val yearGuaName = getHexagramName(yearUpperGua, yearLowerGua)
        val monthGuaName = getHexagramName(monthUpperGua, monthLowerGua)
        val dayGuaName = getHexagramName(dayUpperGua, dayLowerGua)
        val hourGuaName = getHexagramName(hourUpperGua, hourLowerGua)

        val yearGuaJiXiong = getGuaJiXiong(yearUpperGua, yearLowerGua)
        val monthGuaJiXiong = getGuaJiXiong(monthUpperGua, monthLowerGua)
        val dayGuaJiXiong = getGuaJiXiong(dayUpperGua, dayLowerGua)
        val hourGuaJiXiong = getGuaJiXiong(hourUpperGua, hourLowerGua)

        val yearLuoShuUpper = BAGUA_LUOSHU[yearUpperGua]
        val yearLuoShuLower = BAGUA_LUOSHU[yearLowerGua]
        val monthLuoShuUpper = BAGUA_LUOSHU[monthUpperGua]
        val monthLuoShuLower = BAGUA_LUOSHU[monthLowerGua]
        val dayLuoShuUpper = BAGUA_LUOSHU[dayUpperGua]
        val dayLuoShuLower = BAGUA_LUOSHU[dayLowerGua]
        val hourLuoShuUpper = BAGUA_LUOSHU[hourUpperGua]
        val hourLuoShuLower = BAGUA_LUOSHU[hourLowerGua]

        val yearPillar = HeLuoPillarResult(
            "年柱", yearGanZhi,
            BAGUA_NAMES[yearUpperGua], BAGUA_NAMES[yearLowerGua],
            "$yearUpperGua-$yearLowerGua",
            "$yearLuoShuUpper-$yearLuoShuLower",
            yearGuaName,
            getGuaVerdict(yearUpperGua, yearLowerGua),
            BAGUA_WUXING[yearUpperGua], BAGUA_WUXING[yearLowerGua]
        )
        val monthPillar = HeLuoPillarResult(
            "月柱", monthGanZhi,
            BAGUA_NAMES[monthUpperGua], BAGUA_NAMES[monthLowerGua],
            "$monthUpperGua-$monthLowerGua",
            "$monthLuoShuUpper-$monthLuoShuLower",
            monthGuaName,
            getGuaVerdict(monthUpperGua, monthLowerGua),
            BAGUA_WUXING[monthUpperGua], BAGUA_WUXING[monthLowerGua]
        )
        val dayPillar = HeLuoPillarResult(
            "日柱", dayGanZhi,
            BAGUA_NAMES[dayUpperGua], BAGUA_NAMES[dayLowerGua],
            "$dayUpperGua-$dayLowerGua",
            "$dayLuoShuUpper-$dayLuoShuLower",
            dayGuaName,
            getGuaVerdict(dayUpperGua, dayLowerGua),
            BAGUA_WUXING[dayUpperGua], BAGUA_WUXING[dayLowerGua]
        )
        val hourPillar = HeLuoPillarResult(
            "时柱", hourGanZhi,
            BAGUA_NAMES[hourUpperGua], BAGUA_NAMES[hourLowerGua],
            "$hourUpperGua-$hourLowerGua",
            "$hourLuoShuUpper-$hourLuoShuLower",
            hourGuaName,
            getGuaVerdict(hourUpperGua, hourLowerGua),
            BAGUA_WUXING[hourUpperGua], BAGUA_WUXING[hourLowerGua]
        )

        val pillars = listOf(yearPillar, monthPillar, dayPillar, hourPillar)

        val sameGongCount = countSameGong(pillars)
        val generateCount = countWuXingGenerate(pillars)
        val yinYangScore = calcYinYangScore(
            yearUpperGua, yearLowerGua, monthUpperGua, monthLowerGua,
            dayUpperGua, dayLowerGua, hourUpperGua, hourLowerGua
        )
        val luoShuHarmony = calcLuoShuHarmony(
            yearLuoShuUpper, yearLuoShuLower, monthLuoShuUpper, monthLuoShuLower,
            dayLuoShuUpper, dayLuoShuLower, hourLuoShuUpper, hourLuoShuLower
        )

        var score = 50

        if (sameGongCount >= 2) { score += 15; if (sameGongCount >= 3) score += 10 }
        if (sameGongCount == 0) score -= 5

        if (generateCount >= 2) { score += 12; if (generateCount >= 3) score += 8 }
        if (generateCount == 0) score -= 5

        if (yinYangScore >= 6) score += 10
        if (yinYangScore <= 2) score -= 5

        if (luoShuHarmony) score += 10

        if (dayGuaJiXiong > 0) score += 10
        if (dayGuaJiXiong < 0) score -= 10

        score = score.coerceIn(0, 100)

        val verdict = when {
            score >= 85 -> "上吉"
            score >= 70 -> "大吉"
            score >= 55 -> "吉"
            score >= 40 -> "平"
            score >= 25 -> "小凶"
            else -> "凶"
        }

        val sameGongText = when {
            sameGongCount >= 3 -> "多卦同宫，卦气纯一，力量极强"
            sameGongCount >= 2 -> "有卦同宫，卦气相通"
            sameGongCount == 1 -> "仅一卦同宫"
            else -> "四卦各异，卦气分散"
        }

        val generateText = when {
            generateCount >= 3 -> "多卦五行连环相生，气运流转不息"
            generateCount >= 2 -> "有卦五行相生，气运得助"
            generateCount == 1 -> "仅一卦相生"
            else -> "四卦五行无相生，气运不顺"
        }

        val yinYangText = when {
            yinYangScore >= 7 -> "阴阳相配，刚柔并济，大吉"
            yinYangScore >= 5 -> "阴阳基本平衡"
            yinYangScore >= 3 -> "阴阳略有偏倚"
            else -> "阴阳失衡"
        }

        val luoShuText = if (luoShuHarmony) "洛书数理和合，天人相应" else "洛书数理未合"

        val principle = buildPrinciple()
        val guaAnalysis = buildGuaAnalysis(pillars, dayPillar)
        val energyAnalysis = buildEnergyAnalysis(sameGongText, generateText, yinYangText, luoShuText)

        val dingStrength = if (sameGongCount >= 2 || generateCount >= 3) "旺" else if (sameGongCount >= 1 || generateCount >= 2) "中" else "平"
        val guiStrength = if (score >= 70 && dayGuaJiXiong > 0) "旺" else if (score >= 55) "中" else "平"
        val stability = if (sameGongCount >= 2 && yinYangScore >= 5) "极稳" else if (sameGongCount >= 1 || yinYangScore >= 5) "稳" else "一般"

        return HeLuoResult(
            pillars = pillars,
            overallScore = score,
            overallVerdict = verdict,
            sameGongAnalysis = sameGongText,
            generateAnalysis = generateText,
            yinYangAnalysis = yinYangText,
            luoShuAnalysis = luoShuText,
            principle = principle,
            guaAnalysis = guaAnalysis,
            energyAnalysis = energyAnalysis,
            dingStrength = dingStrength,
            guiStrength = guiStrength,
            stability = stability
        )
    }

    private fun countSameGong(pillars: List<HeLuoPillarResult>): Int {
        val groupings = mutableMapOf<Int, Int>()
        pillars.forEach { p ->
            val k = BAGUA_WUXING[p.upperWx]
            groupings[k] = (groupings[k] ?: 0) + 1
        }
        return groupings.values.maxOrNull() ?: 0
    }

    private fun countWuXingGenerate(pillars: List<HeLuoPillarResult>): Int {
        var count = 0
        for (i in 0 until pillars.size - 1) {
            val wx1 = pillars[i].upperWx
            val wx2 = pillars[i + 1].upperWx
            if (isSheng(wx1, wx2)) count++
        }
        return count
    }

    private fun isSheng(a: Int, b: Int): Boolean {
        return (a == 0 && b == 1) || (a == 1 && b == 2) || (a == 2 && b == 3) || (a == 3 && b == 4) || (a == 4 && b == 0)
    }

    private fun calcYinYangScore(
        yug: Int, ylg: Int, mug: Int, mlg: Int, dug: Int, dlg: Int, hug: Int, hlg: Int
    ): Int {
        val yangCount = (yug % 2) + (ylg % 2) + (mug % 2) + (mlg % 2) +
                (dug % 2) + (dlg % 2) + (hug % 2) + (hlg % 2)
        val yinCount = 8 - yangCount
        if (yangCount == 4) return 8
        return 8 - kotlin.math.abs(yangCount - yinCount)
    }

    private fun calcLuoShuHarmony(
        ylu: Int, yll: Int, mlu: Int, mll: Int, dlu: Int, dll: Int, hlu: Int, hll: Int
    ): Boolean {
        val sum = ylu + yll + mlu + mll + dlu + dll + hlu + hll
        return sum % 15 == 0 || sum % 10 == 0 || sum % 8 == 0
    }

    private fun buildPrinciple(): String {
        return "河洛纳卦择日，以河图洛书之数理、先天后天八卦之象义为根基，层次最高。\n\n" +
                "其法先将年月日时四柱干支，依纳甲纳支之法，各配以八卦（上卦取干纳甲，下卦取支纳支），合成六十四卦。\n\n" +
                "再依河图洛书之数，考究卦气是否通气、阴阳是否相配、五行是否相生。卦气通、阴阳和、五行顺者，则天人合一，吉应深远。"
    }

    private fun buildGuaAnalysis(pillars: List<HeLuoPillarResult>, dayPillar: HeLuoPillarResult): String {
        val dayGuaIdx = getHexagramIndex(
            BAGUA_NAMES.indexOf(dayPillar.upperGua),
            BAGUA_NAMES.indexOf(dayPillar.lowerGua)
        )
        val dayGuaName = SIXTY_FOUR_GUA_NAMES[dayGuaIdx]
        return "日柱重卦「${dayGuaName}」，${dayPillar.guaVerdict}。" +
                "上卦${dayPillar.upperGua}（${BAGUA_ATTRIBUTE[BAGUA_NAMES.indexOf(dayPillar.upperGua)]}），" +
                "下卦${dayPillar.lowerGua}（${BAGUA_ATTRIBUTE[BAGUA_NAMES.indexOf(dayPillar.lowerGua)]}）。" +
                "此日课核心在于四卦贯通，天地人三才合一。"
    }

    private fun buildEnergyAnalysis(
        sameGong: String, generate: String, yinYang: String, luoShu: String
    ): String {
        return "$sameGong；$generate；$yinYang；$luoShu。"
    }
}

data class HeLuoResult(
    val pillars: List<HeLuoPillarResult>,
    val overallScore: Int,
    val overallVerdict: String,
    val sameGongAnalysis: String,
    val generateAnalysis: String,
    val yinYangAnalysis: String,
    val luoShuAnalysis: String,
    val principle: String,
    val guaAnalysis: String,
    val energyAnalysis: String,
    val dingStrength: String,
    val guiStrength: String,
    val stability: String
)

data class HeLuoPillarResult(
    val label: String,
    val ganZhi: String,
    val upperGua: String,
    val lowerGua: String,
    val guaNumber: String,
    val luoShuNumber: String,
    val guaName: String,
    val guaVerdict: String,
    val upperWx: Int,
    val lowerWx: Int
)