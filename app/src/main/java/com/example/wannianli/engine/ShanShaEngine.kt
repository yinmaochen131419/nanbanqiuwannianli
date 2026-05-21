package com.example.wannianli.engine

data class ShaCheck(
    val name: String,
    val category: String,
    val isViolated: Boolean,
    val detail: String,
    val severity: Int
)

data class ShanShaResult(
    val sittingMountain: String,
    val checks: List<ShaCheck>,
    val criticalViolations: List<ShaCheck>,
    val warningViolations: List<ShaCheck>,
    val overallSafe: Boolean,
    val overallVerdict: String,
    val summary: String
)

object ShanShaEngine {

    val MOUNTAIN_NAMES = arrayOf(
        "壬", "子", "癸", "丑", "艮", "寅",
        "甲", "卯", "乙", "辰", "巽", "巳",
        "丙", "午", "丁", "未", "坤", "申",
        "庚", "酉", "辛", "戌", "乾", "亥"
    )

    val MOUNTAIN_PALACE = intArrayOf(
        0, 0, 0, 1, 1, 1,
        2, 2, 2, 3, 3, 3,
        4, 4, 4, 5, 5, 5,
        6, 6, 6, 7, 7, 7
    )

    val MOUNTAIN_BRANCH = intArrayOf(
        -1, 0, -1, 1, -1, 2,
        -1, 3, -1, 4, -1, 5,
        -1, 6, -1, 7, -1, 8,
        -1, 9, -1, 10, -1, 11
    )

    val MOUNTAIN_PALACE_BRANCH = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)

    val PALACE_SAN_SHA = arrayOf(
        intArrayOf(5, 6, 7), intArrayOf(8, 9, 10),
        intArrayOf(11, 0, 1), intArrayOf(2, 3, 4)
    )

    val PALACE_NAMES = arrayOf("坎", "艮", "震", "巽", "离", "坤", "兑", "乾")

    fun getSittingIndex(mountainName: String): Int = MOUNTAIN_NAMES.indexOf(mountainName)

    fun getPalaceName(mountainIndex: Int): String = PALACE_NAMES[MOUNTAIN_PALACE[mountainIndex]]

    fun getMountainBranch(mountainIndex: Int): Int = MOUNTAIN_BRANCH[mountainIndex]

    fun evaluate(
        year: Int, month: Int, day: Int,
        yearGan: Int, yearZhi: Int, monthZhi: Int, dayZhi: Int,
        sittingMountainIndex: Int
    ): ShanShaResult {
        val checks = mutableListOf<ShaCheck>()
        val mountainName = MOUNTAIN_NAMES[sittingMountainIndex]
        val palaceIdx = MOUNTAIN_PALACE[sittingMountainIndex]
        val palaceName = PALACE_NAMES[palaceIdx]
        val mountainBranch = MOUNTAIN_BRANCH[sittingMountainIndex]

        checkSanSha(yearZhi, palaceIdx, palaceName, mountainName, checks)

        checkSuiPo(yearZhi, mountainBranch, mountainName, checks)

        checkChongShan(dayZhi, mountainBranch, mountainName, checks)

        if (mountainBranch >= 0) {
            checkXingHai(dayZhi, mountainBranch, mountainName, checks)
        }

        checkDaJiangJun(yearZhi, mountainBranch, mountainName, checks)

        checkXunShan(yearZhi, sittingMountainIndex, mountainName, checks)

        checkWuJi(yearGan, palaceName, mountainName, checks)

        checkDaYueJian(month, palaceName, mountainName, checks)

        checkTuFu(yearZhi, palaceName, mountainName, checks)

        checkYinFu(yearGan, palaceIdx, palaceName, mountainName, checks)

        checkLuoTian(month, day, palaceIdx, palaceName, mountainName, checks)

        checkSiZhengSha(yearZhi, mountainBranch, mountainName, checks)

        val critical = checks.filter { it.severity >= 3 && it.isViolated }
        val warnings = checks.filter { it.severity in 1..2 && it.isViolated }
        val safe = critical.isEmpty()

        val verdict = if (safe) {
            if (warnings.isEmpty()) "大吉" else "可用"
        } else {
            if (critical.size >= 3) "大凶" else if (critical.size >= 2) "凶" else "慎用"
        }

        val summary = buildSummary(mountainName, palaceName, critical, warnings, safe)

        return ShanShaResult(
            sittingMountain = mountainName,
            checks = checks,
            criticalViolations = critical,
            warningViolations = warnings,
            overallSafe = safe,
            overallVerdict = verdict,
            summary = summary
        )
    }

    private fun checkSanSha(yearZhi: Int, palaceIdx: Int, palaceName: String, mt: String, checks: MutableList<ShaCheck>) {
        val sanShaGroup = when {
            yearZhi == 0 || yearZhi == 4 || yearZhi == 8 -> 2
            yearZhi == 2 || yearZhi == 6 || yearZhi == 10 -> 1
            yearZhi == 5 || yearZhi == 9 || yearZhi == 1 -> 0
            else -> 3
        }
        val violated = sanShaGroup == palaceIdx
        val detail = if (violated)
            "${mt}山（${PALACE_NAMES[palaceIdx]}宫）正犯年三煞，大凶，百事不宜"
        else "${mt}山（${PALACE_NAMES[palaceIdx]}宫）不犯三煞"
        checks.add(ShaCheck("三煞", "年家煞", violated, detail, 5))
    }

    private fun checkSuiPo(yearZhi: Int, branch: Int, mt: String, checks: MutableList<ShaCheck>) {
        if (branch < 0) { checks.add(ShaCheck("岁破", "年家煞", false, "${mt}山无支，不犯岁破", 4)); return }
        val suiPoZhi = (yearZhi + 6) % 12
        val violated = branch == suiPoZhi
        val detail = if (violated)
            "${mt}山犯岁破，与太岁正冲，大凶"
        else "${mt}山不犯岁破"
        checks.add(ShaCheck("岁破", "年家煞", violated, detail, 4))
    }

    private fun checkChongShan(dayZhi: Int, branch: Int, mt: String, checks: MutableList<ShaCheck>) {
        if (branch < 0) { checks.add(ShaCheck("冲山", "山家煞", false, "${mt}山无支，不犯冲山", 3)); return }
        val oppZhi = (dayZhi + 6) % 12
        val violated = branch == oppZhi
        val detail = if (violated)
            "${mt}山被日支${CalendarConstants.DI_ZHI[dayZhi]}所冲，大凶忌用"
        else "${mt}山不被日支所冲"
        checks.add(ShaCheck("冲山", "山家煞", violated, detail, 3))
    }

    private fun checkXingHai(dayZhi: Int, branch: Int, mt: String, checks: MutableList<ShaCheck>) {
        if (branch < 0) return
        val xingMap = mapOf(
            0 to 1, 2 to 5, 3 to 0, 5 to 8, 6 to 7, 8 to 11, 9 to 1, 11 to 2
        )
        val xingTarget = xingMap[dayZhi]
        val violated = branch == xingTarget
        if (violated) {
            checks.add(ShaCheck("坐山刑害", "山家煞", true, "${mt}山被日支所刑，不吉", 2))
        }
    }

    private fun checkDaJiangJun(yearZhi: Int, branch: Int, mt: String, checks: MutableList<ShaCheck>) {
        val djjZhi = when {
            (yearZhi == 0 || yearZhi == 4 || yearZhi == 8) -> 9
            (yearZhi == 2 || yearZhi == 6 || yearZhi == 10) -> 6
            (yearZhi == 11 || yearZhi == 3 || yearZhi == 7) -> 3
            else -> 0
        }
        if (branch < 0) {
            checks.add(ShaCheck("大将军", "年家煞", false, "${mt}山无支，不犯大将军", 2))
            return
        }
        val violated = branch == djjZhi
        val detail = if (violated) "${mt}山犯大将军，忌修造动土" else "${mt}山不犯大将军"
        checks.add(ShaCheck("大将军", "年家煞", violated, detail, 2))
    }

    private fun checkXunShan(yearZhi: Int, mtIndex: Int, mt: String, checks: MutableList<ShaCheck>) {
        val xunShanIndex = intArrayOf(2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 0)
        val violated = mtIndex == xunShanIndex[yearZhi]
        val detail = if (violated) "${mt}山犯巡山罗睺，忌用事" else "${mt}山不犯巡山罗睺"
        checks.add(ShaCheck("巡山罗睺", "年家煞", violated, detail, 2))
    }

    private fun checkWuJi(yearGan: Int, palaceName: String, mt: String, checks: MutableList<ShaCheck>) {
        val targetPalaces = when {
            yearGan == 0 || yearGan == 5 -> setOf("震", "巽")
            yearGan == 1 || yearGan == 6 -> setOf("离", "坤")
            yearGan == 2 || yearGan == 7 -> setOf("兑", "乾")
            yearGan == 3 || yearGan == 8 -> setOf("坎", "艮")
            else -> emptySet()
        }
        val violated = targetPalaces.contains(palaceName)
        val detail = if (violated)
            "${mt}山（${palaceName}宫）犯戊己都天，忌动土修造"
        else "${mt}山不犯戊己都天"
        checks.add(ShaCheck("戊己都天", "年家煞", violated, detail, 2))
    }

    private fun checkDaYueJian(month: Int, palaceName: String, mt: String, checks: MutableList<ShaCheck>) {
        val targetPalace = when {
            month in 1..3 -> "艮"
            month in 4..6 -> "巽"
            month in 7..9 -> "坤"
            else -> "乾"
        }
        val violated = palaceName == targetPalace
        val detail = if (violated)
            "${mt}山犯大月建（${month}月建${targetPalace}方），忌修造"
        else "${mt}山不犯大月建"
        checks.add(ShaCheck("大月建", "年家煞", violated, detail, 1))
    }

    private fun checkTuFu(yearZhi: Int, palaceName: String, mt: String, checks: MutableList<ShaCheck>) {
        val targetPalace = when {
            yearZhi == 0 || yearZhi == 3 || yearZhi == 6 || yearZhi == 9 -> "艮"
            yearZhi == 1 || yearZhi == 4 || yearZhi == 7 || yearZhi == 10 -> "坤"
            else -> "乾"
        }
        val violated = palaceName == targetPalace
        val detail = if (violated) "${mt}山犯土府，忌动土" else "${mt}山不犯土府"
        checks.add(ShaCheck("土府", "年家煞", violated, detail, 1))
    }

    private fun checkYinFu(yearGan: Int, palaceIdx: Int, palaceName: String, mt: String, checks: MutableList<ShaCheck>) {
        val targetPalace = when {
            yearGan == 0 || yearGan == 5 -> 1
            yearGan == 1 || yearGan == 6 -> 6
            yearGan == 2 || yearGan == 7 -> 0
            yearGan == 3 || yearGan == 8 -> 4
            else -> 2
        }
        val violated = palaceIdx == targetPalace
        val detail = if (violated)
            "${mt}山（${palaceName}宫）犯阴府煞，最忌安葬、修方"
        else "${mt}山不犯阴府"
        checks.add(ShaCheck("阴府", "山家煞", violated, detail, 3))
    }

    private fun checkLuoTian(month: Int, day: Int, palaceIdx: Int, palaceName: String, mt: String, checks: MutableList<ShaCheck>) {
        val monthDays = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val dayOfYear = monthDays[month - 1] + day
        val targetPalace = when {
            dayOfYear <= 31 -> 0
            dayOfYear <= 62 -> 1
            dayOfYear <= 93 -> 2
            dayOfYear <= 124 -> 3
            dayOfYear <= 155 -> 4
            dayOfYear <= 186 -> 5
            dayOfYear <= 217 -> 6
            dayOfYear <= 248 -> 7
            dayOfYear <= 279 -> 0
            dayOfYear <= 310 -> 1
            dayOfYear <= 341 -> 2
            else -> 3
        }
        val violated = palaceIdx == targetPalace
        val detail = if (violated)
            "${mt}山（${palaceName}宫）犯罗天大退，忌安葬、修方"
        else "${mt}山不犯罗天大退"
        checks.add(ShaCheck("罗天大退", "山家煞", violated, detail, 2))
    }

    private fun checkSiZhengSha(yearZhi: Int, branch: Int, mt: String, checks: MutableList<ShaCheck>) {
        if (branch < 0) return
        val yearTriple = when (yearZhi) {
            0, 4, 8 -> 0
            2, 6, 10 -> 1
            5, 9, 1 -> 2
            else -> 3
        }
        val shaBranches = when (yearTriple) {
            0 -> intArrayOf(5, 6, 7)
            1 -> intArrayOf(11, 0, 1)
            2 -> intArrayOf(2, 3, 4)
            else -> intArrayOf(8, 9, 10)
        }
        if (branch in shaBranches) {
            checks.add(ShaCheck("坐三煞方", "山家煞", true, "${mt}山坐三煞方，忌修造动土", 3))
        }
    }

    private fun buildSummary(
        mt: String, palace: String,
        critical: List<ShaCheck>,
        warnings: List<ShaCheck>,
        safe: Boolean
    ): String {
        if (safe && warnings.isEmpty())
            return "${mt}山（${palace}宫）—— 诸煞不犯，可用。"
        val sb = StringBuilder("${mt}山（${palace}宫）—— ")
        if (critical.isNotEmpty()) {
            sb.append("犯${critical.joinToString("、") { it.name }}（大凶），不可用。")
            if (warnings.isNotEmpty()) {
                sb.append("另犯${warnings.joinToString("、") { it.name }}。")
            }
        } else {
            sb.append("不犯大煞，")
            if (warnings.isNotEmpty()) {
                sb.append("犯${warnings.joinToString("、") { it.name }}（慎用），可酌情使用。")
            }
        }
        return sb.toString()
    }
}