/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data.model

data class CityInfo(

    val id: String,              // IANA时区ID，如 "Asia/Shanghai"

    val nameZh: String,          // 中文名，如"上海"

    val nameEn: String,          // 英文名，如"Shanghai"

    val nameEs: String,          // 西班牙文名，如"Shanghai"

    val latitude: Double,        // 纬度，正为北纬，负为南纬

    val longitude: Double,       // 经度，正为东经，负为西经

    val utcOffsetMinutes: Int,   // UTC偏移分钟数，如+480(UTC+8), -180(UTC-3)

    val isNorthernHemisphere: Boolean  // 是否北半球
)
