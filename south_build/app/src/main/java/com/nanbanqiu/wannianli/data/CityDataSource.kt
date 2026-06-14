/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data

import com.nanbanqiu.wannianli.data.model.CityInfo
import kotlin.math.sqrt

data class AntipodalInfo(
    val lat: Double,
    val lon: Double,
    val antipodalUtcOffsetMinutes: Int,
    val antipodalZoneId: String,
    val displayNameZh: String,
    val displayNameEn: String,
    val nearbyCityNameZh: String,
    val nearbyCityNameEn: String,
    val nearbyCityDistance: Double
)

object CityDataSource {

    // 北半球城市：按UTC偏移从大到小排列（从东到西）
    val northCities: List<CityInfo> = listOf(
        // UTC+12
        CityInfo("Asia/Kamchatka", "俄罗斯·堪察加", "Petropavlovsk, Russia", "Petropávlovsk, Rusia", 53.0, 158.7, 720, true),
        // UTC+9
        CityInfo("Asia/Tokyo", "日本·东京", "Tokyo, Japan", "Tokio, Japón", 35.7, 139.7, 540, true),
        CityInfo("Asia/Seoul", "韩国·首尔", "Seoul, South Korea", "Seúl, Corea del Sur", 37.6, 127.0, 540, true),
        // UTC+8
        CityInfo("Asia/Shanghai", "中国·上海", "Shanghai, China", "Shanghai, China", 31.2, 121.5, 480, true),
        CityInfo("Asia/Shanghai#Beijing", "中国·北京", "Beijing, China", "Pekín, China", 39.9, 116.4, 480, true),
        CityInfo("Asia/Urumqi", "中国·乌鲁木齐", "Ürümqi, China", "Ürümqi, China", 43.8, 87.6, 480, true),
        // UTC+7
        CityInfo("Asia/Bangkok", "泰国·曼谷", "Bangkok, Thailand", "Bangkok, Tailandia", 13.8, 100.5, 420, true),
        // UTC+6
        CityInfo("Asia/Dhaka", "孟加拉·达卡", "Dhaka, Bangladesh", "Daca, Bangladés", 23.8, 90.4, 360, true),
        CityInfo("Asia/Almaty", "哈萨克斯坦·阿拉木图", "Almaty, Kazakhstan", "Almaty, Kazajistán", 43.3, 76.9, 360, true),
        // UTC+5:30
        CityInfo("Asia/Kolkata", "印度·新德里", "New Delhi, India", "Nueva Delhi, India", 28.6, 77.2, 330, true),
        // UTC+4
        CityInfo("Asia/Dubai", "阿联酋·迪拜", "Dubai, UAE", "Dubái, EAU", 25.2, 55.3, 240, true),
        // UTC+3
        CityInfo("Europe/Moscow", "俄罗斯·莫斯科", "Moscow, Russia", "Moscú, Rusia", 55.8, 37.6, 180, true),
        // UTC+2
        CityInfo("Africa/Cairo", "埃及·开罗", "Cairo, Egypt", "El Cairo, Egipto", 30.0, 31.2, 120, true),
        CityInfo("Europe/Athens", "希腊·雅典", "Athens, Greece", "Atenas, Grecia", 37.9, 23.7, 120, true),
        // UTC+1
        CityInfo("Europe/Berlin", "德国·柏林", "Berlin, Germany", "Berlín, Alemania", 52.5, 13.4, 60, true),
        // UTC+0
        CityInfo("Europe/London", "英国·伦敦", "London, UK", "Londres, Reino Unido", 51.5, -0.1, 0, true),
        // UTC-5
        CityInfo("America/New_York", "美国·纽约", "New York, USA", "Nueva York, EE.UU.", 40.7, -74.0, -300, true),
        // UTC-6
        CityInfo("America/Mexico_City", "墨西哥·墨西哥城", "Mexico City, Mexico", "Ciudad de México, México", 19.4, -99.1, -360, true),
        // UTC-7
        CityInfo("America/Denver", "美国·丹佛", "Denver, USA", "Denver, EE.UU.", 39.7, -105.0, -420, true),
        // UTC-8
        CityInfo("America/Los_Angeles", "美国·洛杉矶", "Los Angeles, USA", "Los Ángeles, EE.UU.", 34.1, -118.2, -480, true),
        // UTC-9
        CityInfo("America/Anchorage", "美国·安克雷奇", "Anchorage, USA", "Anchorage, EE.UU.", 61.2, -149.9, -540, true),
    )

    // 南半球城市：仅用于附近城市参考标注
    val southCities: List<CityInfo> = listOf(
        // UTC+12
        CityInfo("Pacific/Auckland", "新西兰·奥克兰", "Auckland, New Zealand", "Auckland, Nueva Zelanda", -36.8, 174.8, 720, false),
        // UTC+11
        CityInfo("Pacific/Noumea", "新喀里多尼亚·努美阿", "Nouméa, New Caledonia", "Numea, Nueva Caledonia", -22.3, 166.5, 660, false),
        // UTC+10
        CityInfo("Australia/Sydney", "澳大利亚·悉尼", "Sydney, Australia", "Sídney, Australia", -33.9, 151.2, 600, false),
        CityInfo("Australia/Melbourne", "澳大利亚·墨尔本", "Melbourne, Australia", "Melbourne, Australia", -37.8, 145.0, 600, false),
        // UTC+8
        CityInfo("Australia/Perth", "澳大利亚·珀斯", "Perth, Australia", "Perth, Australia", -31.9, 115.9, 480, false),
        // UTC+7
        CityInfo("Asia/Jakarta", "印尼·雅加达", "Jakarta, Indonesia", "Yakarta, Indonesia", -6.2, 106.8, 420, false),
        // UTC+4
        CityInfo("Indian/Mauritius", "毛里求斯", "Mauritius", "Mauricio", -20.3, 57.5, 240, false),
        // UTC+3
        CityInfo("Indian/Reunion", "法国·留尼汪", "Réunion, France", "Reunión, Francia", -21.1, 55.5, 180, false),
        // UTC+2
        CityInfo("Africa/Johannesburg", "南非·约翰内斯堡", "Johannesburg, South Africa", "Johannesburgo, Sudáfrica", -26.2, 28.0, 120, false),
        CityInfo("Africa/Johannesburg", "南非·开普敦", "Cape Town, South Africa", "Ciudad del Cabo, Sudáfrica", -33.9, 18.4, 120, false),
        // UTC-3
        CityInfo("America/Argentina/Buenos_Aires", "阿根廷·布宜诺斯艾利斯", "Buenos Aires, Argentina", "Buenos Aires, Argentina", -34.6, -58.4, -180, false),
        CityInfo("America/Sao_Paulo", "巴西·圣保罗", "São Paulo, Brazil", "São Paulo, Brasil", -23.5, -46.6, -180, false),
        CityInfo("America/Montevideo", "乌拉圭·蒙得维的亚", "Montevideo, Uruguay", "Montevideo, Uruguay", -34.9, -56.2, -180, false),
        // UTC-4
        CityInfo("America/Santiago", "智利·圣地亚哥", "Santiago, Chile", "Santiago, Chile", -33.4, -70.7, -240, false),
        // UTC-5
        CityInfo("America/Lima", "秘鲁·利马", "Lima, Peru", "Lima, Perú", -12.0, -77.0, -300, false),
        CityInfo("America/Bogota", "哥伦比亚·波哥大", "Bogotá, Colombia", "Bogotá, Colombia", 4.7, -74.1, -300, false),
        // UTC-6
        CityInfo("Pacific/Galapagos", "厄瓜多尔·加拉帕戈斯", "Galápagos, Ecuador", "Galápagos, Ecuador", -0.7, -90.3, -360, false),
    )

    val defaultNorthCity: CityInfo = northCities[3]  // 中国·上海

    fun getCityById(id: String): CityInfo? {
        return (northCities + southCities).find { it.id == id }
    }

    /**
     * 从城市ID提取纯IANA ZoneId（去掉#后缀）
     * 例如 "Asia/Shanghai#Beijing" -> "Asia/Shanghai"
     */
    fun toZoneId(cityId: String): String {
        val hashIndex = cityId.indexOf('#')
        return if (hashIndex >= 0) cityId.substring(0, hashIndex) else cityId
    }

    /**
     * 计算严格对跖点信息
     * UTC偏移 = 北半球UTC + 12小时，确保南北时差永远12小时（2时辰）
     * 经纬度仍按严格180°对跖计算
     */
    fun getAntipodalInfo(northCity: CityInfo): AntipodalInfo {
        // 严格对跖点坐标
        val antiLat = -northCity.latitude
        val antiLon = if (northCity.longitude > 0) northCity.longitude - 180.0 else northCity.longitude + 180.0

        // UTC偏移 = 北半球UTC + 12小时，归一化到[-12h, +14h]范围
        var antiUtcOffsetMinutes = (northCity.utcOffsetMinutes + 720) % 1440
        if (antiUtcOffsetMinutes > 720) antiUtcOffsetMinutes -= 1440

        // 生成ZoneId字符串（用于ZonedDateTime计算）
        val antiZoneId = utcOffsetToZoneId(antiUtcOffsetMinutes)

        // 在南半球城市列表中找最近的城市（仅作参考标注）
        var nearestCity: CityInfo? = null
        var minDist = Double.MAX_VALUE
        for (city in southCities) {
            val dLat = city.latitude - antiLat
            val dLon = city.longitude - antiLon
            val dist = sqrt(dLat * dLat + dLon * dLon)
            if (dist < minDist) {
                minDist = dist
                nearestCity = city
            }
        }

        // 显示名：始终用坐标格式
        val latDirZh = if (antiLat >= 0) "北纬" else "南纬"
        val lonDirZh = if (antiLon >= 0) "东经" else "西经"
        val displayNameZh = "${latDirZh}${String.format("%.1f", kotlin.math.abs(antiLat))}° ${lonDirZh}${String.format("%.1f", kotlin.math.abs(antiLon))}°"

        val latDirEn = if (antiLat >= 0) "N" else "S"
        val lonDirEn = if (antiLon >= 0) "E" else "W"
        val displayNameEn = "${String.format("%.1f", kotlin.math.abs(antiLat))}°$latDirEn ${String.format("%.1f", kotlin.math.abs(antiLon))}°$lonDirEn"

        // 附近城市参考（10°以内）
        val nearbyCityNameZh: String
        val nearbyCityNameEn: String
        if (nearestCity != null && minDist <= 10.0) {
            nearbyCityNameZh = nearestCity.nameZh
            nearbyCityNameEn = nearestCity.nameEn
        } else {
            nearbyCityNameZh = ""
            nearbyCityNameEn = ""
        }

        return AntipodalInfo(
            lat = antiLat,
            lon = antiLon,
            antipodalUtcOffsetMinutes = antiUtcOffsetMinutes,
            antipodalZoneId = antiZoneId,
            displayNameZh = displayNameZh,
            displayNameEn = displayNameEn,
            nearbyCityNameZh = nearbyCityNameZh,
            nearbyCityNameEn = nearbyCityNameEn,
            nearbyCityDistance = minDist
        )
    }

    /**
     * 将UTC偏移分钟数转为ZoneId字符串
     * 使用Java ZoneOffset格式，ZonedDateTime.now()可直接使用
     */
    private fun utcOffsetToZoneId(offsetMinutes: Int): String {
        val sign = if (offsetMinutes >= 0) "+" else "-"
        val absMinutes = kotlin.math.abs(offsetMinutes)
        val hours = absMinutes / 60
        val mins = absMinutes % 60
        return if (mins == 0) {
            if (hours == 0) "Z" else "${sign}${hours.toString().padStart(2, '0')}:00"
        } else {
            "${sign}${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}"
        }
    }
}
