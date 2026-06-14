/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

object StarDict {

    val CHINESE_STAR_NAMES: Map<Int, String> = mapOf(
        65474 to "角宿一",
        67222 to "角宿二",
        69427 to "亢宿一",
        69701 to "亢宿二",
        72622 to "氐宿一",
        74785 to "氐宿四",
        78265 to "房宿一",
        80112 to "心宿一",
        80763 to "心宿二",
        82514 to "尾宿一",
        88635 to "箕宿一",
        90185 to "斗宿一",
        100345 to "牛宿一",
        102618 to "女宿一",
        106278 to "虚宿一",
        109074 to "危宿一",
        113963 to "室宿一",
        1067 to "壁宿一",
        4463 to "奎宿一",
        8903 to "娄宿一",
        9153 to "胃宿一",
        10628 to "昴宿一",
        16411 to "毕宿一",
        26207 to "觜宿一",
        26727 to "参宿一",
        30343 to "井宿一",
        41307 to "鬼宿一",
        42313 to "柳宿一",
        46390 to "星宿一",
        48356 to "张宿一",
        55687 to "翼宿一",
        59803 to "轸宿一",
        32349 to "天狼星",
        24608 to "参宿七",
        24436 to "参宿四",
        30438 to "五车二",
        37279 to "南河三",
        36850 to "北河二",
        69673 to "氐宿四",
        82363 to "大角",
        37826 to "北河三",
        97649 to "牛郎星",
        102098 to "天津四",
        91262 to "织女星",
        21421 to "毕宿五",
        15863 to "昴宿六",
        27989 to "参宿五",
        25336 to "参宿六",
        31681 to "南河二",
        33579 to "天狼增四",
        44816 to "轩辕十四",
        30324 to "水位星",
        41037 to "鬼宿四",
        76267 to "井宿三",
        61941 to "角宿增三",
        54061 to "五帝座一"
    )

    fun getChineseName(hipNumber: Int): String? = CHINESE_STAR_NAMES[hipNumber]

    fun getHipNumber(chineseName: String): Int? =
        CHINESE_STAR_NAMES.entries.find { it.value == chineseName }?.key

    fun searchByName(keyword: String): List<Pair<Int, String>> =
        CHINESE_STAR_NAMES.entries
            .filter { it.value.contains(keyword) }
            .map { it.key to it.value }
}
