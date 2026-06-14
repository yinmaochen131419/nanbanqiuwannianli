/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.ui.screen

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material.icons.filled.Check

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.nanbanqiu.wannianli.data.AntipodalInfo

import com.nanbanqiu.wannianli.data.CityDataSource

import com.nanbanqiu.wannianli.data.model.CityInfo

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.nanbanqiu.wannianli.R
import com.nanbanqiu.wannianli.util.LanguageHelper

@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun SettingsScreen(

    onBack: () -> Unit,

    northCityId: String,

    onNorthCityChanged: (String) -> Unit

) {

    var showNorthDialog by remember { mutableStateOf(false) }

    val northCity = CityDataSource.getCityById(northCityId) ?: CityDataSource.defaultNorthCity

    val antipodalInfo = remember(northCityId) { CityDataSource.getAntipodalInfo(northCity) }

    Scaffold(

        topBar = {

            TopAppBar(

                title = { Text(stringResource(R.string.title_settings), fontWeight = FontWeight.Bold) },

                navigationIcon = {

                    IconButton(onClick = onBack) {

                        Icon(Icons.Default.ArrowBack, stringResource(R.string.common_back))

                    }

                },

                colors = TopAppBarDefaults.topAppBarColors(

                    containerColor = Color(0xFFE3F2FD),

                    titleContentColor = Color(0xFF1565C0),

                    navigationIconContentColor = Color(0xFF1565C0)

                )

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier

                .fillMaxSize()

                .padding(padding)

                .verticalScroll(rememberScrollState())

                .padding(12.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp)

        ) {

            CitySelectCard(

                title = stringResource(R.string.settings_timezone_select),

                city = northCity,

                onClick = { showNorthDialog = true }

            )

            AntipodalInfoCard(

                antipodalInfo = antipodalInfo,

                northCity = northCity

            )

        }

    }

    if (showNorthDialog) {

        CitySelectDialog(

            title = stringResource(R.string.settings_select_north_timezone),

            cities = CityDataSource.northCities,

            selectedId = northCityId,

            onSelect = {

                onNorthCityChanged(it)

                showNorthDialog = false

            },

            onDismiss = { showNorthDialog = false }

        )

    }

}

@Composable

private fun CitySelectCard(

    title: String,

    city: CityInfo,

    onClick: () -> Unit

) {

    val context = LocalContext.current
    val langCode = LanguageHelper.getSavedLanguage(context)

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(10.dp),

        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)

    ) {

        Column(modifier = Modifier.padding(14.dp)) {

            Text(

                text = title,

                fontSize = 16.sp,

                fontWeight = FontWeight.Bold,

                color = Color(0xFF1565C0)

            )

            Spacer(Modifier.height(8.dp))

            Text(

                text = getCityDisplayName(city, langCode),

                fontSize = 15.sp,

                fontWeight = FontWeight.Medium,

                color = Color(0xFF424242)

            )

            Spacer(Modifier.height(4.dp))

            Text(

                text = "${formatLatitude(city.latitude, context)}  ${formatLongitude(city.longitude, context)}",

                fontSize = 13.sp,

                color = Color(0xFF757575)

            )

            Spacer(Modifier.height(2.dp))

            Text(

                text = formatUtcOffset(city.utcOffsetMinutes),

                fontSize = 13.sp,

                color = Color(0xFF757575)

            )

        }

    }

}

@Composable

private fun AntipodalInfoCard(

    antipodalInfo: AntipodalInfo,

    northCity: CityInfo

) {
    val context = LocalContext.current
    val langCode = LanguageHelper.getSavedLanguage(context)
    val antipodalCity = CityDataSource.getCityById(antipodalInfo.antipodalZoneId)
    val antipodalDisplayName = antipodalCity?.let { getCityDisplayName(it, langCode) } ?: antipodalInfo.displayNameZh

    Card(

        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(10.dp),

        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)

    ) {

        Column(modifier = Modifier.padding(14.dp)) {

            Text(

                text = stringResource(R.string.settings_antipode),

                fontSize = 16.sp,

                fontWeight = FontWeight.Bold,

                color = Color(0xFF0D47A1)

            )

            Spacer(Modifier.height(8.dp))

            Text(

                text = antipodalDisplayName,

                fontSize = 15.sp,

                fontWeight = FontWeight.Medium,

                color = Color(0xFF424242)

            )

            Spacer(Modifier.height(4.dp))

            Text(

                text = "${formatLatitude(antipodalInfo.lat, context)}  ${formatLongitude(antipodalInfo.lon, context)}",

                fontSize = 13.sp,

                color = Color(0xFF757575)

            )

            Spacer(Modifier.height(2.dp))

            Text(

                text = "${stringResource(R.string.settings_timezone_label)} $antipodalDisplayName (${formatUtcOffset(antipodalInfo.antipodalUtcOffsetMinutes)})",

                fontSize = 13.sp,

                color = Color(0xFF757575)

            )

            Spacer(Modifier.height(6.dp))

            Text(

                text = stringResource(R.string.settings_auto_calculated, getCityDisplayName(northCity, langCode)),

                fontSize = 11.sp,

                color = Color(0xFF9E9E9E)

            )

        }

    }

}

@Composable

private fun CitySelectDialog(

    title: String,

    cities: List<CityInfo>,

    selectedId: String,

    onSelect: (String) -> Unit,

    onDismiss: () -> Unit

) {
    val context = LocalContext.current
    val langCode = LanguageHelper.getSavedLanguage(context)

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {

            Text(

                text = title,

                fontWeight = FontWeight.Bold,

                color = Color(0xFF1565C0)

            )

        },

        text = {

            Column(

                modifier = Modifier.verticalScroll(rememberScrollState())

            ) {

                cities.forEach { city ->

                    val isSelected = city.id == selectedId && city.nameZh == CityDataSource.getCityById(selectedId)?.nameZh

                    val antipodalPreview = CityDataSource.getAntipodalInfo(city)

                    Row(

                        modifier = Modifier

                            .fillMaxWidth()

                            .clickable { onSelect(city.id) }

                            .padding(vertical = 8.dp),

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Column(modifier = Modifier.weight(1f)) {

                            Text(

                                text = getCityDisplayName(city, langCode),

                                fontSize = 15.sp,

                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,

                                color = if (isSelected) Color(0xFF1565C0) else Color(0xFF424242)

                            )

                            Text(

                                text = "→ ${CityDataSource.getCityById(antipodalPreview.antipodalZoneId)?.let { getCityDisplayName(it, langCode) } ?: antipodalPreview.displayNameZh}",

                                fontSize = 12.sp,

                                color = Color(0xFF0D47A1)

                            )

                            Text(

                                text = "${formatLatitude(city.latitude, context)}  ${formatLongitude(city.longitude, context)}  ${formatUtcOffset(city.utcOffsetMinutes)}",

                                fontSize = 11.sp,

                                color = Color(0xFF757575)

                            )

                        }

                        if (isSelected) {

                            Icon(

                                Icons.Default.Check,

                                contentDescription = stringResource(R.string.settings_selected),

                                tint = Color(0xFF1565C0),

                                modifier = Modifier.size(20.dp)

                            )

                        }

                    }

                    if (city != cities.last()) {

                        Divider(color = Color(0xFFE0E0E0))

                    }

                }

            }

        },

        confirmButton = {

            TextButton(onClick = onDismiss) {

                Text(stringResource(R.string.settings_close))

            }

        }

    )

}

private fun getCityDisplayName(city: CityInfo, langCode: String): String {
    return when (langCode) {
        LanguageHelper.LANG_EN -> city.nameEn
        LanguageHelper.LANG_ES -> city.nameEs
        LanguageHelper.LANG_ZH_TW -> city.nameZh
        else -> city.nameZh
    }
}

private fun formatLatitude(lat: Double, context: Context): String {
    val direction = if (lat >= 0) context.getString(R.string.settings_north_lat) else context.getString(R.string.settings_south_lat)
    return "$direction${String.format("%.1f", kotlin.math.abs(lat))}°"
}

private fun formatLongitude(lng: Double, context: Context): String {
    val direction = if (lng >= 0) context.getString(R.string.settings_east_lon) else context.getString(R.string.settings_west_lon)
    return "$direction${String.format("%.1f", kotlin.math.abs(lng))}°"
}

private fun formatUtcOffset(minutes: Int): String {

    val sign = if (minutes >= 0) "+" else "-"

    val absMinutes = kotlin.math.abs(minutes)

    val hours = absMinutes / 60

    val mins = absMinutes % 60

    return if (mins == 0) {

        "UTC$sign$hours"

    } else {

        "UTC$sign${hours}:${mins.toString().padStart(2, '0')}"

    }

}
