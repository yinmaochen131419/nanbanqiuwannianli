/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data.local

import android.content.Context
import android.content.SharedPreferences

class CityPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("city_prefs", Context.MODE_PRIVATE)

    fun getNorthCityId(): String =
        prefs.getString("north_city_id", "Asia/Shanghai") ?: "Asia/Shanghai"

    fun setNorthCityId(id: String) = prefs.edit().putString("north_city_id", id).apply()
}
