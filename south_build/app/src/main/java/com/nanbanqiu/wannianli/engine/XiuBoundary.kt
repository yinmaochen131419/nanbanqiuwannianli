/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

object XiuBoundary {

    val XIU_NAMES = arrayOf(
        "角宿", "亢宿", "氐宿", "房宿", "心宿", "尾宿", "箕宿",
        "斗宿", "牛宿", "女宿", "虚宿", "危宿", "室宿", "壁宿",
        "奎宿", "娄宿", "胃宿", "昴宿", "毕宿", "觜宿", "参宿",
        "井宿", "鬼宿", "柳宿", "星宿", "张宿", "翼宿", "轸宿"
    )

    val XIU_ELEMENTS = arrayOf(
        "木", "金", "土", "日", "月", "火", "水",
        "水", "金", "土", "日", "月", "火", "水",
        "木", "金", "土", "日", "月", "火", "水",
        "水", "金", "土", "日", "月", "火", "水"
    )

    val XIU_ANIMALS = arrayOf(
        "蛟", "龙", "貉", "兔", "狐", "虎", "豹",
        "獬", "牛", "蝠", "鼠", "燕", "猪", "貐",
        "狼", "狗", "雉", "鸡", "乌", "猴", "猿",
        "犴", "羊", "獐", "马", "鹿", "蛇", "蚓"
    )

    val GROUP_NAMES = arrayOf("东方青龙", "北方玄武", "西方白虎", "南方朱雀")

    data class XiuInfo(
        val index: Int,
        val name: String,
        val element: String,
        val animal: String,
        val group: String,
        val startLon: Double,
        val endLon: Double
    )

    data class XiuStarData(val index: Int, val name: String, val starName: String, val hip: Int, val eclipticLonJ2000: Double)

    val XIU_STARS = listOf(
        XiuStarData(0, "角宿", "角宿一", 65474, 203.80),
        XiuStarData(1, "亢宿", "亢宿一", 69427, 217.12),
        XiuStarData(2, "氐宿", "氐宿一", 72622, 224.80),
        XiuStarData(3, "房宿", "房宿一", 78265, 242.86),
        XiuStarData(4, "心宿", "心宿一", 80112, 247.55),
        XiuStarData(5, "尾宿", "尾宿一", 82514, 254.38),
        XiuStarData(6, "箕宿", "箕宿一", 88635, 272.45),
        XiuStarData(7, "斗宿", "斗宿一", 90185, 277.35),
        XiuStarData(8, "牛宿", "牛宿一", 100345, 307.91),
        XiuStarData(9, "女宿", "女宿一", 102618, 315.33),
        XiuStarData(10, "虚宿", "虚宿一", 106278, 325.46),
        XiuStarData(11, "危宿", "危宿一", 109074, 334.11),
        XiuStarData(12, "室宿", "室宿一", 113963, 348.10),
        XiuStarData(13, "壁宿", "壁宿一", 1067, 4.86),
        XiuStarData(14, "奎宿", "奎宿一", 4463, 17.29),
        XiuStarData(15, "娄宿", "娄宿一", 8903, 31.60),
        XiuStarData(16, "胃宿", "胃宿一", 9153, 42.87),
        XiuStarData(17, "昴宿", "昴宿一", 10628, 53.62),
        XiuStarData(18, "毕宿", "毕宿一", 16411, 70.84),
        XiuStarData(19, "觜宿", "觜宿一", 26207, 86.11),
        XiuStarData(20, "参宿", "参宿一", 26727, 88.85),
        XiuStarData(21, "井宿", "井宿一", 30343, 99.06),
        XiuStarData(22, "鬼宿", "鬼宿一", 41307, 131.69),
        XiuStarData(23, "柳宿", "柳宿一", 42313, 137.25),
        XiuStarData(24, "星宿", "星宿一", 46390, 145.13),
        XiuStarData(25, "张宿", "张宿一", 48356, 153.34),
        XiuStarData(26, "翼宿", "翼宿一", 55687, 169.90),
        XiuStarData(27, "轸宿", "轸宿一", 59803, 187.47)
    )

    fun findXiu(eclipticLon: Double): Int {
        val raw = (eclipticLon % 360.0 + 360.0) % 360.0
        // 归一化到6位小数，消除浮点运算精度误差
        val n = Math.round(raw * 1e6) / 1e6
        for (i in 0 until 28) {
            val start = XIU_STARS[i].eclipticLonJ2000
            val end = XIU_STARS[(i + 1) % 28].eclipticLonJ2000
            val adjEnd = if (end < start) end + 360.0 else end
            if (n >= start && n < adjEnd) return i
            if (n + 360.0 >= start && n + 360.0 < adjEnd) return i
        }
        return 0
    }

    fun getXiuInfo(eclipticLon: Double): XiuInfo {
        val idx = findXiu(eclipticLon)
        val start = XIU_STARS[idx].eclipticLonJ2000
        val end = XIU_STARS[(idx + 1) % 28].eclipticLonJ2000
        val adjEnd = if (end < start) end + 360.0 else end
        val groupIdx = idx / 7
        return XiuInfo(
            index = idx,
            name = XIU_NAMES[idx],
            element = XIU_ELEMENTS[idx],
            animal = XIU_ANIMALS[idx],
            group = GROUP_NAMES[groupIdx],
            startLon = start,
            endLon = adjEnd
        )
    }

    fun getXiuByIndex(index: Int): XiuInfo {
        val i = (index % 28 + 28) % 28
        val start = XIU_STARS[i].eclipticLonJ2000
        val end = XIU_STARS[(i + 1) % 28].eclipticLonJ2000
        val adjEnd = if (end < start) end + 360.0 else end
        val groupIdx = i / 7
        return XiuInfo(
            index = i,
            name = XIU_NAMES[i],
            element = XIU_ELEMENTS[i],
            animal = XIU_ANIMALS[i],
            group = GROUP_NAMES[groupIdx],
            startLon = start,
            endLon = adjEnd
        )
    }
}
