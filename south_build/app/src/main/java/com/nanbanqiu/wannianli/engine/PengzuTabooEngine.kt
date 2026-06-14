/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine

object PengzuTabooEngine {

    data class PengzuTaboo(
        val taboo: String,
        val explanation: String
    )

    data class DailyTaboos(
        val ganTaboo: PengzuTaboo,
        val zhiTaboo: PengzuTaboo
    )

    private val GAN_TABOOS = mapOf(
        0 to PengzuTaboo("甲不开仓", "甲日不宜开仓放粮、出货财，以防财物耗散"),
        1 to PengzuTaboo("乙不栽植", "乙日不宜栽种植株，移栽难活、扎根不深"),
        2 to PengzuTaboo("丙不修灶", "丙日不宜修砌炉灶，犯之火灾隐患"),
        3 to PengzuTaboo("丁不剃头", "丁日不宜剃头理发，犯之头生疮疖"),
        4 to PengzuTaboo("戊不受田", "戊日不宜接受田产土地，恐有争讼纠纷"),
        5 to PengzuTaboo("己不破券", "己日不宜破毁契约文书，恐伤信义损财"),
        6 to PengzuTaboo("庚不经络", "庚日不宜织布纺纱，经纬断续不成匹"),
        7 to PengzuTaboo("辛不合酱", "辛日不宜合酱制醋，发酵不成味败"),
        8 to PengzuTaboo("壬不决水", "壬日不宜开渠放水、修堤决水，恐犯水厄"),
        9 to PengzuTaboo("癸不词讼", "癸日不宜诉讼打官司，讼事不利易败诉")
    )

    private val ZHI_TABOOS = mapOf(
        0 to PengzuTaboo("子不问卜", "子日不宜占卜问卦，卦象不正易自招祸殃"),
        1 to PengzuTaboo("丑不冠带", "丑日不宜行冠礼、穿新衣戴新帽，不祥"),
        2 to PengzuTaboo("寅不祭祀", "寅日不宜祭祀祖先神明，神灵不享"),
        3 to PengzuTaboo("卯不穿井", "卯日不宜打井挖洞，凿之不透反耗人力"),
        4 to PengzuTaboo("辰不哭泣", "辰日不宜哭泣哀嚎，犯之复有丧事"),
        5 to PengzuTaboo("巳不远行", "巳日不宜远行出行，旅途不顺多阻滞"),
        6 to PengzuTaboo("午不苫盖", "午日不宜苫盖屋顶、搭棚遮阳，盖之不稳"),
        7 to PengzuTaboo("未不服药", "未日不宜服药，药力难行病难愈"),
        8 to PengzuTaboo("申不安床", "申日不宜安床铺床，卧不安寝"),
        9 to PengzuTaboo("酉不会客", "酉日不宜会客见友、宴请宾朋，宾主不欢"),
        10 to PengzuTaboo("戌不吃犬", "戌日不宜吃狗肉，犯之招口舌是非"),
        11 to PengzuTaboo("亥不嫁娶", "亥日不宜嫁娶婚配，婚姻不顺易离异")
    )

    fun getDailyTaboos(dayGanZhi: String): DailyTaboos {
        val gan = dayGanZhi[0].toString()
        val zhi = dayGanZhi[1].toString()
        val ganIdx = CalendarConstants.TIAN_GAN.indexOf(gan)
        val zhiIdx = CalendarConstants.DI_ZHI.indexOf(zhi)
        return DailyTaboos(
            ganTaboo = GAN_TABOOS[ganIdx] ?: PengzuTaboo("", ""),
            zhiTaboo = ZHI_TABOOS[zhiIdx] ?: PengzuTaboo("", "")
        )
    }
}