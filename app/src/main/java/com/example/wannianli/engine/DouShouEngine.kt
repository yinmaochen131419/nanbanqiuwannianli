package com.example.wannianli.engine

import com.example.wannianli.engine.CalendarConstants.TIAN_GAN

object DouShouEngine {

    val GAN_DOUSHOU_WUXING = intArrayOf(2, 3, 4, 0, 1, 2, 3, 4, 0, 1)

    val WUXING_NAMES = arrayOf("木", "火", "土", "金", "水")

    val DOUSHOU_WUXING_NAMES = arrayOf("土", "金", "水", "木", "火")

    const val STAR_YUANCHEN = 0
    const val STAR_LIANZHEN = 1
    const val STAR_TANLANG = 2
    const val STAR_WUQU = 3
    const val STAR_POJUN = 4
    const val STAR_OTHER = -1

    val STAR_NAMES = arrayOf("元辰", "廉贞", "贪狼", "武曲", "破军")

    val STAR_COLORS = longArrayOf(
        0xFF2E7D32,
        0xFF558B2F,
        0xFF1565C0,
        0xFFE65100,
        0xFFC62828
    )

    val STAR_JIXIONG = intArrayOf(2, 0, 1, 1, -2)

    val STAR_DESCRIPTIONS = arrayOf(
        "元辰为斗首之主星，如君如父，最吉。元辰得令则百事大吉，主家道兴隆，人财两旺。",
        "廉贞为子孙星，主人丁繁衍。廉贞得令则子孙昌盛，利求嗣、入学、进人口。",
        "贪狼为官贵星，主功名仕途。贪狼得令则官运亨通，利求官、考试、升迁。",
        "武曲为妻财星，主财富经营。武曲得令则财源广进，利求财、嫁娶、开业。",
        "破军为鬼贼星，主口舌是非。破军当令则百事不宜，忌嫁娶、出行、动土。"
    )

    fun getDouShouGanWuXing(ganIndex: Int): Int {
        return GAN_DOUSHOU_WUXING[ganIndex]
    }

    fun getStarRelation(dayDouShouWuXing: Int, yearDouShouWuXing: Int): Int {
        return when {
            dayDouShouWuXing == yearDouShouWuXing -> STAR_YUANCHEN
            isSheng(dayDouShouWuXing, yearDouShouWuXing) -> STAR_LIANZHEN
            isSheng(yearDouShouWuXing, dayDouShouWuXing) -> STAR_TANLANG
            isKe(dayDouShouWuXing, yearDouShouWuXing) -> STAR_WUQU
            isKe(yearDouShouWuXing, dayDouShouWuXing) -> STAR_POJUN
            else -> STAR_OTHER
        }
    }

    private fun isSheng(a: Int, b: Int): Boolean {
        return (a == 0 && b == 1) || (a == 1 && b == 2) || (a == 2 && b == 3) || (a == 3 && b == 4) || (a == 4 && b == 0)
    }

    private fun isKe(a: Int, b: Int): Boolean {
        return (a == 0 && b == 2) || (a == 2 && b == 4) || (a == 4 && b == 1) || (a == 1 && b == 3) || (a == 3 && b == 0)
    }

    fun evaluate(
        yearGanZhi: String,
        monthGanZhi: String,
        dayGanZhi: String,
        hourGanZhi: String,
        sittingMountainIndex: Int = -1
    ): DouShouResult {
        val yearGan = TIAN_GAN.indexOf(yearGanZhi[0].toString())
        val yearZhi = CalendarConstants.DI_ZHI.indexOf(yearGanZhi[1].toString())
        val monthGan = TIAN_GAN.indexOf(monthGanZhi[0].toString())
        val dayGan = TIAN_GAN.indexOf(dayGanZhi[0].toString())
        val hourGan = TIAN_GAN.indexOf(hourGanZhi[0].toString())

        val yearDsWx = getDouShouGanWuXing(yearGan)
        val monthDsWx = getDouShouGanWuXing(monthGan)
        val dayDsWx = getDouShouGanWuXing(dayGan)
        val hourDsWx = getDouShouGanWuXing(hourGan)

        val yearDsWxName = DOUSHOU_WUXING_NAMES[yearDsWx]

        val monthStar = getStarRelation(monthDsWx, yearDsWx)
        val dayStar = getStarRelation(dayDsWx, yearDsWx)
        val hourStar = getStarRelation(hourDsWx, yearDsWx)

        val pillars = listOf(
            DouShouPillarResult("年柱", yearGanZhi, yearDsWxName, STAR_YUANCHEN),
            DouShouPillarResult("月柱", monthGanZhi, DOUSHOU_WUXING_NAMES[monthDsWx], monthStar),
            DouShouPillarResult("日柱", dayGanZhi, DOUSHOU_WUXING_NAMES[dayDsWx], dayStar),
            DouShouPillarResult("时柱", hourGanZhi, DOUSHOU_WUXING_NAMES[hourDsWx], hourStar)
        )

        val dayStarJiXiong = STAR_JIXIONG[dayStar]
        val overallVerdict = when {
            dayStarJiXiong >= 2 -> "大吉"
            dayStarJiXiong >= 1 -> "吉"
            dayStarJiXiong >= 0 -> "平"
            else -> "凶"
        }

        val caiStrength = calcCaiStrength(pillars)
        val dingStrength = calcDingStrength(pillars)
        val guiStrength = calcGuiStrength(pillars)

        val principle = buildPrinciple(yearGanZhi, yearDsWxName, pillars)
        val analysis = buildAnalysis(pillars, dayStar)
        val fuShan = buildFuShanAnalysis(pillars)
        val pattern = buildGlobalPattern(pillars, dayStar)
        val taboos = buildTaboos(pillars)
        val mountainMatch = buildMountainMatch(pillars, sittingMountainIndex)
        val authenticity = buildAuthenticityCheck(yearGanZhi)
        val benMingReference = buildBenMingReference(pillars)

        return DouShouResult(
            yearDouShouWuxing = yearDsWxName,
            pillars = pillars,
            dayStar = dayStar,
            dayStarName = STAR_NAMES[dayStar],
            dayStarJiXiong = overallVerdict,
            caiStrength = caiStrength,
            dingStrength = dingStrength,
            guiStrength = guiStrength,
            principle = principle,
            analysis = analysis,
            fuShanAnalysis = fuShan,
            globalPattern = pattern,
            taboos = taboos,
            mountainMatch = mountainMatch,
            authenticityCheck = authenticity,
            benMingReference = benMingReference
        )
    }

    private fun calcCaiStrength(pillars: List<DouShouPillarResult>): String {
        val count = pillars.count { it.star == STAR_WUQU }
        return when {
            pillars.any { it.star == STAR_POJUN && it.label == "日柱" } -> "破军当令，损财"
            count >= 2 -> "旺（多柱武曲，大利求财）"
            count == 1 -> "中（有武曲星，财气可催）"
            else -> "弱（无武曲星，催财力不足）"
        }
    }

    private fun calcDingStrength(pillars: List<DouShouPillarResult>): String {
        val count = pillars.count { it.star == STAR_LIANZHEN }
        return when {
            pillars.any { it.star == STAR_POJUN && it.label == "日柱" } -> "破军当令，损丁"
            count >= 2 -> "旺（多柱廉贞，大利子孙）"
            count == 1 -> {
                val pos = pillars.find { it.star == STAR_LIANZHEN }?.label ?: ""
                "中。${pos}廉贞透干，子孙星有力，催丁可用。"
            }
            else -> "平。本局无廉贞（子孙星）透出，催丁、旺子孙力量偏弱；武曲生贪狼，可间接利家运平稳。"
        }
    }

    private fun calcGuiStrength(pillars: List<DouShouPillarResult>): String {
        val count = pillars.count { it.star == STAR_TANLANG }
        val poCount = pillars.count { it.star == STAR_POJUN }
        return when {
            pillars.any { it.star == STAR_POJUN && it.label == "日柱" } -> "破军当令，损贵"
            count >= 2 -> "旺（多柱贪狼，大利功名）"
            count == 1 -> {
                val pos = pillars.find { it.star == STAR_TANLANG }?.label ?: ""
                if (poCount > 0) {
                    "中。${pos}贪狼透干，贪狼为官贵星，利职场晋升、贵人相助；有破军干扰，贵气有阻滞。"
                } else {
                    "中。${pos}贪狼透干，官贵有气，利求官、考试。"
                }
            }
            else -> "平（无贪狼星，催贵力一般）"
        }
    }

    private fun buildPrinciple(yearGanZhi: String, yearDsWx: String, pillars: List<DouShouPillarResult>): String {
        val sb = StringBuilder()
        sb.append("斗首五行以天干化气为宗，不同於正体五行。")
        sb.append("年柱${yearGanZhi}，天干化气属${yearDsWx}，定为元辰。")
        sb.append("以此元辰为基准，比较月、日、时三柱天干化气，")
        sb.append("同我者为元辰，我生者为廉贞（子孙），生我者为贪狼（官贵），")
        sb.append("我克者为武曲（妻财），克我者为破军（鬼贼）。")
        return sb.toString()
    }

    private fun buildAnalysis(pillars: List<DouShouPillarResult>, dayStar: Int): String {
        val sb = StringBuilder()

        val dayPillar = pillars.find { it.label == "日柱" }!!
        val dayStarName = STAR_NAMES[dayStar]

        sb.append("日柱${dayPillar.ganZhi}，斗首属${dayPillar.douShouWuxing}，为「${dayStarName}」星。")

        when (dayStar) {
            STAR_YUANCHEN -> sb.append("元辰当令，如君临朝，百事大吉，诸神退位。此日宜嫁娶、开业、入宅、出行。")
            STAR_LIANZHEN -> sb.append("廉贞值日，主子孙昌盛，人丁兴旺。宜求嗣、入学、祭祀。")
            STAR_TANLANG -> sb.append("贪狼值日，主官贵亨通，仕途顺遂。宜求官、赴任、考试。")
            STAR_WUQU -> sb.append("武曲值日，主财源广进，利经营交易。宜开业、嫁娶、求财。")
            STAR_POJUN -> sb.append("破军值日，鬼贼当道，口舌是非多。百事不宜，慎用此日。")
        }

        return sb.toString()
    }

    private fun buildFuShanAnalysis(pillars: List<DouShouPillarResult>): String {
        val sb = StringBuilder()
        val yearPillar = pillars.find { it.label == "年柱" }!!
        val monthPillar = pillars.find { it.label == "月柱" }!!
        val dayPillar = pillars.find { it.label == "日柱" }!!
        val hourPillar = pillars.find { it.label == "时柱" }!!

        sb.append("年月日时四柱斗首分别为：")
        sb.append("年柱${yearPillar.ganZhi}（${yearPillar.douShouWuxing}，元辰）、")
        sb.append("月柱${monthPillar.ganZhi}（${monthPillar.douShouWuxing}，${STAR_NAMES[monthPillar.star]}）、")
        sb.append("日柱${dayPillar.ganZhi}（${dayPillar.douShouWuxing}，${STAR_NAMES[dayPillar.star]}）、")
        sb.append("时柱${hourPillar.ganZhi}（${hourPillar.douShouWuxing}，${STAR_NAMES[hourPillar.star]}）。")

        return sb.toString()
    }

    private fun buildGlobalPattern(pillars: List<DouShouPillarResult>, dayStar: Int): String {
        val yearPillar = pillars.find { it.label == "年柱" }!!
        val monthPillar = pillars.find { it.label == "月柱" }!!
        val dayPillar = pillars.find { it.label == "日柱" }!!
        val hourPillar = pillars.find { it.label == "时柱" }!!

        val yuanChenCount = pillars.count { it.star == STAR_YUANCHEN }
        val wuQuCount = pillars.count { it.star == STAR_WUQU }
        val tanLangCount = pillars.count { it.star == STAR_TANLANG }
        val lianZhenCount = pillars.count { it.star == STAR_LIANZHEN }
        val poJunCount = pillars.count { it.star == STAR_POJUN }

        val sb = StringBuilder()

        sb.append("年柱元辰（${yearPillar.douShouWuxing}）为君，统领全局。")

        if (poJunCount > 0 && yuanChenCount > 0) {
            val poPositions = pillars.filter { it.star == STAR_POJUN }.joinToString("、") { it.label }
            sb.append("破军在${poPositions}，但有元辰镇守，破军受制，凶中藏吉。")
        }

        if (yuanChenCount >= 3) {
            sb.append("三元辰格局：年日月时多现元辰，君临四方，大吉大利，百事亨通。")
        } else if (yuanChenCount == 2) {
            val pos = pillars.filter { it.star == STAR_YUANCHEN && it.label != "年柱" }.joinToString("") { it.label }
            sb.append("年时双元辰，上下同心，根基稳固，格局上佳。")
        }

        if (dayStar == STAR_WUQU && poJunCount > 0) {
            sb.append("日柱武曲得令，财星有力。")
            if (poJunCount == 1 && pillars.any { it.star == STAR_POJUN && it.label != "日柱" }) {
                sb.append("破军旁落，武曲当道，财可制煞。")
            }
        }

        if (dayStar == STAR_WUQU && yuanChenCount >= 2) {
            sb.append("元辰护武曲，催财格局已成，大利求财经营。")
        }

        if (dayStar == STAR_TANLANG && yuanChenCount >= 2) {
            sb.append("元辰扶贪狼，催贵格局已成，大利功名仕途。")
        }

        if (dayStar == STAR_WUQU && tanLangCount > 0) {
            sb.append("贪狼生武曲，官贵生财，权财两旺之格。")
        }

        if (poJunCount >= 2) {
            sb.append("多现破军，鬼贼猖獗，需天德月德化解方可使用。")
        }

        if (dayStar == STAR_YUANCHEN && yuanChenCount >= 2) {
            sb.append("日元辰当位，君臣同心，百事大吉。")
        }

        if (sb.length == 0 || sb.toString() == "年柱元辰（${yearPillar.douShouWuxing}）为君，统领全局。") {
            val starNames = pillars.map {
                "${it.label}${STAR_NAMES[it.star]}"
            }.joinToString("、")
            sb.append("格局：${starNames}，四柱各司其职，中平之格。")
        }

        return sb.toString()
    }

    private fun buildTaboos(pillars: List<DouShouPillarResult>): List<String> {
        val taboos = mutableListOf<String>()

        val yearPillar = pillars.find { it.label == "年柱" }!!
        val monthPo = pillars.find { it.star == STAR_POJUN && it.label == "月柱" }
        if (monthPo != null) {
            val yuanChenWx = yearPillar.douShouWuxing
            val poWx = monthPo.douShouWuxing
            val yuanZhiPo = if (yuanChenWx == "火" && poWx == "金") "年柱元辰火制破军金，凶性减弱"
            else if (yuanChenWx == "水" && poWx == "火") "年柱元辰水制破军火，凶性减弱"
            else if (yuanChenWx == "木" && poWx == "土") "年柱元辰木制破军土，凶性减弱"
            else if (yuanChenWx == "金" && poWx == "木") "年柱元辰金制破军木，凶性减弱"
            else if (yuanChenWx == "土" && poWx == "水") "年柱元辰土制破军水，凶性减弱"
            else ""
            taboos.add("破军在月柱，主是非、口舌、小人干扰。${yuanZhiPo}。得天德月德可完全化解。嫁娶、开业利财，但需防人际纠纷。")
        }

        val dayPo = pillars.find { it.star == STAR_POJUN && it.label == "日柱" }
        if (dayPo != null) {
            taboos.add("破军在日，鬼贼当道，百事不宜。忌嫁娶、出行、动土、开业。非得天德月德化解不可用。")
        }

        val hourPo = pillars.find { it.star == STAR_POJUN && it.label == "时柱" }
        if (hourPo != null) {
            taboos.add("破军在时，晚年不利，慎防口舌官非。大事宜早不宜晚。")
        }

        val yearPo = pillars.find { it.star == STAR_POJUN && it.label == "年柱" }
        if (yearPo != null) {
            taboos.add("年柱破军为岁破，年运不济，当年不宜重大决策。")
        }

        if (pillars.count { it.star == STAR_POJUN } >= 2) {
            taboos.add("多柱破军，煞气叠加。若无天德月德贵人化解，此日不可用。")
        }

        if (pillars.all { it.star == STAR_WUQU || it.star == STAR_YUANCHEN }) {
            taboos.add("全盘武元格局虽旺财，但纯阳无阴，忌安葬阴事。")
        }

        return taboos
    }

    private val DOUSHOU_TO_STANDARD_WUXING = intArrayOf(2, 3, 4, 0, 1)

    private fun buildMountainMatch(
        pillars: List<DouShouPillarResult>,
        sittingMountainIndex: Int
    ): String {
        if (sittingMountainIndex < 0) return ""
        val palaceIdx = ShanShaEngine.MOUNTAIN_PALACE[sittingMountainIndex]
        val mtName = ShanShaEngine.MOUNTAIN_NAMES[sittingMountainIndex]
        val palaceName = ShanShaEngine.PALACE_NAMES[palaceIdx]
        val mtWxIdx = ZeRiEngine.PALACE_WUXING[palaceIdx]
        val mtWxName = CalendarConstants.WUXING[mtWxIdx]

        val yearPillar = pillars.find { it.label == "年柱" }!!
        val dayPillar = pillars.find { it.label == "日柱" }!!

        val yearDsWxIdx = DOUSHOU_WUXING_NAMES.indexOf(yearPillar.douShouWuxing)
        val dayDsWxIdx = DOUSHOU_WUXING_NAMES.indexOf(dayPillar.douShouWuxing)
        val yearStdWxIdx = if (yearDsWxIdx >= 0) DOUSHOU_TO_STANDARD_WUXING[yearDsWxIdx] else -1
        val dayStdWxIdx = if (dayDsWxIdx >= 0) DOUSHOU_TO_STANDARD_WUXING[dayDsWxIdx] else -1

        val sb = StringBuilder()
        sb.append("本坐山${mtName}山属${palaceName}宫${mtWxName}。")
        sb.append("日柱${dayPillar.ganZhi}斗首属${dayPillar.douShouWuxing}（${STAR_NAMES[dayPillar.star]}），")

        if (dayStdWxIdx >= 0) {
            when {
                dayStdWxIdx == mtWxIdx -> sb.append("与坐山比和，旺财旺宅，财星得力。")
                isWuXingSheng(dayStdWxIdx, mtWxIdx) -> sb.append("生扶坐山，催旺龙气，大吉。")
                isWuXingSheng(mtWxIdx, dayStdWxIdx) -> sb.append("坐山生财星，泄山生财，利经营求财。")
                isWuXingKe(dayStdWxIdx, mtWxIdx) -> sb.append("财星克坐山，需要元辰化解或天德月德加持。")
                isWuXingKe(mtWxIdx, dayStdWxIdx) -> sb.append("坐山克财星，财气受损，催财力弱。")
                else -> sb.append("与坐山无直接生克。")
            }
        }

        if (yearStdWxIdx >= 0 && isWuXingKe(yearStdWxIdx, mtWxIdx)) {
            val yearStarName = STAR_NAMES[yearPillar.star]
            sb.append("年柱${yearStarName}（${yearPillar.douShouWuxing}）克坐山${mtWxName}，需天德月德化解。")
        }

        val wuQuCount = pillars.count { it.star == STAR_WUQU }
        val poJunCount = pillars.count { it.star == STAR_POJUN }
        if (wuQuCount > 0) sb.append("武曲为财星，此局财星有力，")
        if (poJunCount > 0 && wuQuCount > 0) sb.append("但有破军干扰，需防是非。")
        if (poJunCount == 0 && wuQuCount > 0) sb.append("催财格局可用。")

        sb.append("此斗首格局对${mtName}山——")
        if (poJunCount > 0 && wuQuCount == 0) {
            sb.append("破军当道，需慎用。")
        } else if (wuQuCount > 0 && poJunCount == 0) {
            sb.append("财星得力，可择时用事。")
        } else if (wuQuCount > 0 && poJunCount > 0) {
            sb.append("财星得力但破军干扰，需慎用。")
        } else {
            sb.append("平稳可用。")
        }

        return sb.toString()
    }

    private fun isWuXingSheng(a: Int, b: Int): Boolean {
        return (a == 0 && b == 1) || (a == 1 && b == 2) || (a == 2 && b == 3) ||
                (a == 3 && b == 4) || (a == 4 && b == 0)
    }

    private fun isWuXingKe(a: Int, b: Int): Boolean {
        return (a == 0 && b == 2) || (a == 2 && b == 4) || (a == 4 && b == 1) ||
                (a == 1 && b == 3) || (a == 3 && b == 0)
    }

    private fun buildAuthenticityCheck(yearGanZhi: String): String {
        val yearGanChar = yearGanZhi[0].toString()
        val dsWxIdx = getDouShouGanWuXing(TIAN_GAN.indexOf(yearGanChar))
        val dsWxName = DOUSHOU_WUXING_NAMES[dsWxIdx]
        return "本局斗首以年干${yearGanChar}火化气定元辰（${dsWxName}），严格遵循「年上起元辰」古法，非日上元辰，正宗无误。"
    }

    private fun buildBenMingReference(pillars: List<DouShouPillarResult>): String {
        val sb = StringBuilder()
        sb.append("本命参考：")
        val poJunPillars = pillars.filter { it.star == STAR_POJUN }
        if (poJunPillars.isNotEmpty()) {
            val poPositions = poJunPillars.joinToString("、") { it.label }
            sb.append("年命地支与${poPositions}相合者，斗首格局可用；与${poPositions}相冲者，需慎用或另择吉日。")
        } else {
            val tanLangPillars = pillars.filter { it.star == STAR_TANLANG }
            val wuQuPillars = pillars.filter { it.star == STAR_WUQU }
            if (tanLangPillars.isNotEmpty() || wuQuPillars.isNotEmpty()) {
                sb.append("年命生扶元辰（${pillars.firstOrNull { it.star == STAR_YUANCHEN }?.douShouWuxing ?: ""}）者，格局可用；年命被破军冲克者，需慎用。")
            } else {
                sb.append("年命与日柱三合六合者，斗首格局可用；年命与月柱冲害者，需慎用。")
            }
        }
        sb.append("（具体需结合命主年命干支排盘判断）。")
        return sb.toString()
    }
}

data class DouShouResult(
    val yearDouShouWuxing: String,
    val pillars: List<DouShouPillarResult>,
    val dayStar: Int,
    val dayStarName: String,
    val dayStarJiXiong: String,
    val caiStrength: String,
    val dingStrength: String,
    val guiStrength: String,
    val principle: String,
    val analysis: String,
    val fuShanAnalysis: String,
    val globalPattern: String,
    val taboos: List<String>,
    val mountainMatch: String,
    val authenticityCheck: String,
    val benMingReference: String
)

data class DouShouPillarResult(
    val label: String,
    val ganZhi: String,
    val douShouWuxing: String,
    val star: Int
)