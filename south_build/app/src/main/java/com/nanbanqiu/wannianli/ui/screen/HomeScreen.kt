/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.ui.screen

import androidx.compose.animation.AnimatedVisibility

import androidx.compose.animation.fadeIn

import androidx.compose.animation.fadeOut

import androidx.compose.foundation.background

import androidx.compose.foundation.border

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.res.stringResource

import com.nanbanqiu.wannianli.R

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.nanbanqiu.wannianli.data.AntipodalInfo

import com.nanbanqiu.wannianli.data.CityDataSource

import com.nanbanqiu.wannianli.data.model.CalendarDay

import com.nanbanqiu.wannianli.data.model.CityInfo

import com.nanbanqiu.wannianli.data.model.LunarDate

import com.nanbanqiu.wannianli.data.model.SolarTermInfo

import com.nanbanqiu.wannianli.engine.CalendarConstants

import com.nanbanqiu.wannianli.engine.PlanetPositionCalc

import com.nanbanqiu.wannianli.engine.XiuBoundary

import com.nanbanqiu.wannianli.engine.ZeRiEngine

import com.nanbanqiu.wannianli.viewmodel.MainViewModel

import com.nanbanqiu.wannianli.viewmodel.MonthDayItem

import com.nanbanqiu.wannianli.util.LanguageHelper
import com.nanbanqiu.wannianli.util.LocalizedStringProvider

import java.util.*

import java.time.LocalDate

import java.time.LocalTime

import java.time.ZoneId

import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun HomeScreen(

    viewModel: MainViewModel,

    onDrawerToggle: () -> Unit = {},

    onNavigateToZeRi: () -> Unit = {},

    onNavigateToEclipse: () -> Unit = {},

    onNavigateToSchedule: () -> Unit = {},

    onNavigateToNotes: () -> Unit = {},

) {

    val monthDays by viewModel.monthDays.collectAsState()

    val calendarDay by viewModel.calendarDay.collectAsState()

    val viewYear by viewModel.viewYear.collectAsState()

    val viewMonth by viewModel.viewMonth.collectAsState()

    val selectedYear by viewModel.selectedYear.collectAsState()

    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val selectedDay by viewModel.selectedDay.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    val northCityId by viewModel.northCityId.collectAsState()

    val northCity = remember(northCityId) { CityDataSource.getCityById(northCityId) ?: CityDataSource.defaultNorthCity }

    val antipodalInfo = remember(northCityId) { CityDataSource.getAntipodalInfo(northCity) }

    var showYearMonthPicker by remember { mutableStateOf(false) }

    var pickerYear by remember { mutableIntStateOf(viewYear) }

    var pickerMonth by remember { mutableIntStateOf(viewMonth) }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    val screenWidthDp = LocalConfiguration.current.screenWidthDp
                    val titleFontSize = ((screenWidthDp - 80) / 14f).coerceAtMost(20f).sp
                    Text(
                        stringResource(R.string.home_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                },

                navigationIcon = {

                    IconButton(onClick = onDrawerToggle) {

                        Icon(Icons.Default.Menu, stringResource(R.string.common_menu), tint = Color(0xFF1565C0))

                    }

                },

                actions = {

                    IconButton(onClick = { viewModel.loadToday() }) {

                        Icon(Icons.Default.Today, stringResource(R.string.home_today), tint = Color(0xFF1565C0))

                    }

                },

                colors = TopAppBarDefaults.topAppBarColors(

                    containerColor = Color(0xFFE3F2FD),

                    titleContentColor = Color(0xFF1565C0),

                    navigationIconContentColor = Color(0xFF1565C0)

                )

            )

        },

    ) { padding ->

        Column(

            modifier = Modifier

                .fillMaxSize()

                .padding(padding)

                .background(Color(0xFFF5F5F5))

        ) {

            MonthNavigationBar(

                viewYear = viewYear,

                viewMonth = viewMonth,

                onPrevMonth = { viewModel.navigateMonth(-1) },

                onNextMonth = { viewModel.navigateMonth(1) },

                onYearMonthClick = {

                    pickerYear = viewYear

                    pickerMonth = viewMonth

                    showYearMonthPicker = true

                }

            )

            DualClockBar(northCity, antipodalInfo)

            SolarCalendarTab(

                    monthDays = monthDays,

                    viewYear = viewYear,

                    viewMonth = viewMonth,

                    selectedYear = selectedYear,

                    selectedMonth = selectedMonth,

                    selectedDay = selectedDay,

                    onSelectDay = { y, m, d -> viewModel.selectDay(y, m, d) },

                    calendarDay = calendarDay,

                    northCity = northCity,

                    antipodalInfo = antipodalInfo,

                    northCityId = northCityId

            )

        }

    }

    if (showYearMonthPicker) {

        YearMonthPickerDialog(

            initialYear = pickerYear,

            initialMonth = pickerMonth,

            onConfirm = { y, m ->

                viewModel.navigateToYearMonth(y, m)

                showYearMonthPicker = false

            },

            onDismiss = { showYearMonthPicker = false }

        )

    }

}

@Composable

private fun MonthNavigationBar(

    viewYear: Int,

    viewMonth: Int,

    onPrevMonth: () -> Unit,

    onNextMonth: () -> Unit,

    onYearMonthClick: () -> Unit

) {

    Surface(

        color = Color.White,

        shadowElevation = 2.dp

    ) {

        Row(

            modifier = Modifier

                .fillMaxWidth()

                .padding(horizontal = 8.dp, vertical = 4.dp),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.SpaceBetween

        ) {

            IconButton(onClick = onPrevMonth) {

                Icon(Icons.Default.ChevronLeft, stringResource(R.string.home_prev_month))

            }

            TextButton(onClick = onYearMonthClick) {
                val ctx = LocalContext.current
                Text(

                    LocalizedStringProvider.formatSolarDateTitle(ctx, viewYear, viewMonth),

                    fontSize = 18.sp,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF1565C0)

                )

            }

            IconButton(onClick = onNextMonth) {

                Icon(Icons.Default.ChevronRight, stringResource(R.string.home_next_month))

            }

        }

    }

}

@Composable

private fun SolarCalendarTab(

    monthDays: List<MonthDayItem>,

    viewYear: Int,

    viewMonth: Int,

    selectedYear: Int,

    selectedMonth: Int,

    selectedDay: Int,

    onSelectDay: (Int, Int, Int) -> Unit,

    calendarDay: CalendarDay?,

    northCity: CityInfo,

    antipodalInfo: AntipodalInfo,

    northCityId: String

) {

    Column(

        modifier = Modifier

            .fillMaxSize()

            .verticalScroll(rememberScrollState())

    ) {

        WeekdayHeader()

        SolarMonthGrid(

            monthDays = monthDays,

            viewYear = viewYear,

            viewMonth = viewMonth,

            selectedYear = selectedYear,

            selectedMonth = selectedMonth,

            selectedDay = selectedDay,

            onSelectDay = onSelectDay

        )

        if (calendarDay != null) {

            Spacer(Modifier.height(12.dp))

            SolarInfoCards(calendarDay, northCity, antipodalInfo, northCityId)

            Spacer(Modifier.height(12.dp))

            SolarDateInfoCard(calendarDay, northCity, antipodalInfo, northCityId)

        }

    }

}

@Composable

private fun WeekdayHeader() {

    Row(

        modifier = Modifier

            .fillMaxWidth()

            .background(Color(0xFFE3F2FD))

            .padding(vertical = 8.dp)

    ) {

        val weekdays = listOf(stringResource(R.string.weekday_mon), stringResource(R.string.weekday_tue), stringResource(R.string.weekday_wed), stringResource(R.string.weekday_thu), stringResource(R.string.weekday_fri), stringResource(R.string.weekday_sat), stringResource(R.string.weekday_sun))

        weekdays.forEachIndexed { index, day ->

            val isWeekend = index >= 5

            Text(

                text = day,

                modifier = Modifier.weight(1f),

                textAlign = TextAlign.Center,

                fontSize = 13.sp,

                fontWeight = FontWeight.Bold,

                color = if (isWeekend) Color(0xFFD32F2F) else Color(0xFF1565C0)

            )

        }

    }

}

@Composable

private fun SolarMonthGrid(

    monthDays: List<MonthDayItem>,

    viewYear: Int,

    viewMonth: Int,

    selectedYear: Int,

    selectedMonth: Int,

    selectedDay: Int,

    onSelectDay: (Int, Int, Int) -> Unit

) {

    val chunkedDays = monthDays.chunked(7)

    Column(modifier = Modifier.fillMaxWidth()) {

        chunkedDays.forEach { row ->

            Row(modifier = Modifier.fillMaxWidth()) {

                row.forEach { item ->

                    val isSelected = item.gregorianYear == selectedYear &&

                            item.gregorianMonth == selectedMonth &&

                            item.gregorianDay == selectedDay

                    SolarDayCell(

                        item = item,

                        isSelected = isSelected,

                        onClick = { onSelectDay(item.gregorianYear, item.gregorianMonth, item.gregorianDay) }

                    )

                }

            }

        }

    }

}

@Composable

private fun RowScope.SolarDayCell(

    item: MonthDayItem,

    isSelected: Boolean,

    onClick: () -> Unit

) {

    val bgColor = when {

        isSelected -> Color(0xFFBBDEFB)

        item.isToday -> Color(0xFFFFF3E0)

        !item.isCurrentMonth -> Color(0xFFF5F5F5)

        else -> Color.White

    }

    val textColor = when {

        isSelected -> Color(0xFF1565C0)

        !item.isCurrentMonth -> Color(0xFFBDBDBD)

        item.isWeekend && !item.isToday -> Color(0xFFE53935)

        else -> Color(0xFF212121)

    }

    val seasonEmoji = getSeasonEmojiForMonth(item.gregorianMonth)

    Box(

        modifier = Modifier

            .weight(1f)

            .aspectRatio(0.85f)

            .clickable { onClick() }

            .background(bgColor),

        contentAlignment = Alignment.TopCenter

    ) {

        Column(

            horizontalAlignment = Alignment.CenterHorizontally,

            modifier = Modifier.padding(top = 2.dp)

        ) {

            if (item.isToday) {

                Box(

                    modifier = Modifier

                        .size(28.dp)

                        .clip(CircleShape)

                        .background(if (isSelected) Color.White.copy(alpha = 0.3f) else Color(0xFFE65100)),

                    contentAlignment = Alignment.Center

                ) {

                    Text(

                        text = "${item.gregorianDay}",

                        fontSize = 14.sp,

                        fontWeight = FontWeight.Bold,

                        color = if (isSelected) Color.White else Color.White

                    )

                }

            } else {

                Text(

                    text = "${item.gregorianDay}",

                    fontSize = 14.sp,

                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,

                    color = textColor

                )

            }

            if (item.isCurrentMonth && item.gregorianDay == 1 && seasonEmoji.isNotEmpty()) {

                Text(

                    text = seasonEmoji,

                    fontSize = 10.sp

                )

            }

            if (item.solarTermName != null && item.isCurrentMonth) {

                val ctx = LocalContext.current

                Text(

                    text = LocalizedStringProvider.getSolarTermByName(ctx, item.solarTermName),

                    fontSize = 8.sp,

                    color = if (isSelected) Color(0xFFFFD700) else Color(0xFF2E7D32),

                    maxLines = 1,

                    overflow = TextOverflow.Ellipsis

                )

            }

        }

    }

}

private fun getSeasonEmojiForMonth(month: Int): String {

    return when (month) {

        9, 10, 11 -> "\uD83C\uDF38"

        12, 1, 2 -> "\u2600\uFE0F"

        3, 4, 5 -> "\uD83C\uDF42"

        6, 7, 8 -> "\u2744\uFE0F"

        else -> ""

    }

}

@Composable

private fun SolarDateInfoCard(

    calendarDay: CalendarDay,

    northCity: CityInfo,

    antipodalInfo: AntipodalInfo,

    northCityId: String

) {

    Card(

        modifier = Modifier

            .fillMaxWidth()

            .padding(horizontal = 12.dp),

        colors = CardDefaults.cardColors(containerColor = Color.White),

        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),

        shape = RoundedCornerShape(12.dp)

    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            if (calendarDay.currentSolarTerm != null || calendarDay.nextSolarTerm != null) {

                DualSolarTermCard(calendarDay, northCity, antipodalInfo, northCityId)

            } else {

                InfoRow(stringResource(R.string.home_solar_term), stringResource(R.string.home_no_solar_term))

            }

        }

    }

}

@Composable

private fun SolarInfoCards(

    calendarDay: CalendarDay,

    northCity: CityInfo,

    antipodalInfo: AntipodalInfo,

    northCityId: String

) {

    val ctx = LocalContext.current

    val langCode = LanguageHelper.getSavedLanguage(ctx)

    Card(

        modifier = Modifier

            .fillMaxWidth()

            .padding(horizontal = 12.dp),

        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FFF0)),

        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),

        shape = RoundedCornerShape(12.dp)

    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            // 标题?

            Row(

                modifier = Modifier

                    .fillMaxWidth()

                    .clip(RoundedCornerShape(6.dp))

                    .background(Color(0xFFE3F2FD))

                    .padding(vertical = 4.dp),

                horizontalArrangement = Arrangement.Center

            ) {

                Text(

                    text = stringResource(R.string.home_calendar_conversion),

                    fontSize = 12.sp,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF1565C0)

                )

            }

            Spacer(Modifier.height(6.dp))

            // 标题?

            Row(

                modifier = Modifier.fillMaxWidth(),

                verticalAlignment = Alignment.Top

            ) {

                Column(modifier = Modifier.weight(1f)) {

                    Text(stringResource(R.string.home_north_hemisphere), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))

                    Text("${formatCityLocation(northCity)} ${formatCityUtc(northCity)}", fontSize = 7.sp, color = Color(0xFF757575))

                }

                Column(

                    modifier = Modifier.weight(1f),

                    horizontalAlignment = Alignment.End

                ) {

                    Text(stringResource(R.string.home_south_hemisphere), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))

                    if (getNearbyCityName(antipodalInfo, langCode).isNotEmpty()) {

                        Text("${stringResource(R.string.home_reference)}：${getNearbyCityName(antipodalInfo, langCode)}", fontSize = 7.sp, color = Color(0xFF757575))

                    }

                    Text("${formatAntipodalLocation(antipodalInfo)} ${formatAntipodalUtc(antipodalInfo)}", fontSize = 7.sp, color = Color(0xFF757575))

                }

            }

            Spacer(Modifier.height(6.dp))

            val northBg = Color(0xFFFFF3E0)

            val southBg = Color(0xFFE3F2FD)

            // 基本信息

            val ctx = LocalContext.current

            DualInfoRow(stringResource(R.string.home_solar_calendar), "${calendarDay.gregorianYear}.${calendarDay.gregorianMonth}.${calendarDay.gregorianDay}", stringResource(R.string.home_solar_calendar), "${calendarDay.southSolarYear}.${calendarDay.southSolarMonth}.${calendarDay.southSolarDay}", leftBg = northBg, rightBg = southBg)

            DualInfoRow(stringResource(R.string.home_lunar_calendar), "${LocalizedStringProvider.getNorthLunarMonthByName(ctx, calendarDay.lunarDate.monthName)}${LocalizedStringProvider.getLunarDayByName(ctx, calendarDay.lunarDate.dayName)}", stringResource(R.string.home_lunar_calendar), LocalizedStringProvider.getLocalizedSouthOppositeDay(ctx, calendarDay.southOppositeDay), leftBg = northBg, rightBg = southBg)

            DualInfoRow(stringResource(R.string.home_moon_phase), "${calendarDay.northMoonPhaseSymbol} ${LocalizedStringProvider.getMoonPhaseByName(ctx, calendarDay.moonPhaseName)}", stringResource(R.string.home_moon_phase), "${calendarDay.southMoonPhaseSymbol} ${LocalizedStringProvider.getMoonPhaseByName(ctx, calendarDay.southMoonPhaseName)}", leftBg = northBg, rightBg = southBg)

            DualInfoRow(stringResource(R.string.home_zodiac), LocalizedStringProvider.getZodiacByName(ctx, calendarDay.northShengXiao), stringResource(R.string.home_zodiac), LocalizedStringProvider.getZodiacByName(ctx, calendarDay.southShengXiao), leftBg = northBg, rightBg = southBg)

            DualInfoRow(stringResource(R.string.home_weekday), LocalizedStringProvider.getWeekdayName(ctx, weekdayToIndex(calendarDay.weekday)), stringResource(R.string.home_weekday), getSouthWeekday(calendarDay.gregorianYear, calendarDay.gregorianMonth, calendarDay.gregorianDay, northCityId, antipodalInfo.antipodalZoneId), leftBg = northBg, rightBg = southBg)

            // 值日星宿 & 日躔星宿 & 月躔星宿

            val dailyLodge = calcDailyStarLodge(calendarDay.gregorianYear, calendarDay.gregorianMonth, calendarDay.gregorianDay, ctx)

            val solarLodge = calcSolarStarLodge(calendarDay.gregorianYear, calendarDay.gregorianMonth, calendarDay.gregorianDay, ctx)

            val monthlyLodge = calcMonthlyStarLodge(calendarDay.gregorianYear, calendarDay.gregorianMonth, calendarDay.gregorianDay, ctx)

            DualInfoRow(stringResource(R.string.home_daily_star), dailyLodge.first, stringResource(R.string.home_daily_star), dailyLodge.second, leftBg = northBg, rightBg = southBg)

            DualInfoRow(stringResource(R.string.home_solar_star), solarLodge.first, stringResource(R.string.home_solar_star), solarLodge.second, leftBg = northBg, rightBg = southBg)

            DualInfoRow(stringResource(R.string.home_moon_star), monthlyLodge.first, stringResource(R.string.home_moon_star), monthlyLodge.second, leftBg = northBg, rightBg = southBg)

            Spacer(Modifier.height(6.dp))

            Divider(color = Color(0xFFE0E0E0))

            Spacer(Modifier.height(6.dp))

            // 四柱八字

            DualInfoRow(stringResource(R.string.home_year_pillar), calendarDay.northYearGanZhi, stringResource(R.string.home_year_pillar), calendarDay.southYearGanZhi, leftBg = northBg, rightBg = southBg)

            DualInfoRow(stringResource(R.string.home_month_pillar), calendarDay.northMonthGanZhi, stringResource(R.string.home_month_pillar), calendarDay.southMonthGanZhi, leftBg = northBg, rightBg = southBg)

            DualInfoRow(stringResource(R.string.home_day_pillar), calendarDay.northDayGanZhi, stringResource(R.string.home_day_pillar), calendarDay.southDayGanZhi, leftBg = northBg, rightBg = southBg)

            DualInfoRow(stringResource(R.string.home_hour_pillar), calendarDay.northHourGanZhi, stringResource(R.string.home_hour_pillar), calendarDay.southHourGanZhi, leftBg = northBg, rightBg = southBg)

        }

    }

}

@Composable

private fun DualInfoRow(

    leftLabel: String,

    leftValue: String,

    rightLabel: String,

    rightValue: String,

    leftBg: Color = Color.Transparent,

    rightBg: Color = Color.Transparent

) {

    val context = LocalContext.current

    val langCode = LanguageHelper.getSavedLanguage(context)

    val isCjk = langCode == LanguageHelper.LANG_ZH || langCode == LanguageHelper.LANG_ZH_TW

    Row(

        modifier = Modifier

            .fillMaxWidth()

            .padding(vertical = 2.dp),

        verticalAlignment = Alignment.CenterVertically

    ) {

        if (isCjk) {

            // Chinese: horizontal label: value (compact, no line break)

            Row(

                modifier = Modifier

                    .weight(1f)

                    .background(leftBg, RoundedCornerShape(4.dp))

                    .padding(horizontal = 4.dp),

                verticalAlignment = Alignment.CenterVertically

            ) {

                Text(

                    text = "$leftLabel: ",

                    fontSize = 13.sp,

                    color = Color(0xFF757575),

                    fontWeight = FontWeight.Medium

                )

                Text(

                    text = leftValue,

                    fontSize = 14.sp,

                    color = Color(0xFF212121)

                )

            }

            Row(

                modifier = Modifier

                    .weight(1f)

                    .background(rightBg, RoundedCornerShape(4.dp))

                    .padding(horizontal = 4.dp),

                horizontalArrangement = Arrangement.End,

                verticalAlignment = Alignment.CenterVertically

            ) {

                Text(

                    text = "$rightLabel: ",

                    fontSize = 13.sp,

                    color = Color(0xFF757575),

                    fontWeight = FontWeight.Medium

                )

                Text(

                    text = rightValue,

                    fontSize = 14.sp,

                    color = Color(0xFF212121)

                )

            }

        } else {

            // English/Spanish: vertical label above value (wrapping allowed)

            Column(

                modifier = Modifier

                    .weight(1f)

                    .background(leftBg, RoundedCornerShape(4.dp))

                    .padding(horizontal = 6.dp, vertical = 2.dp)

            ) {

                Text(

                    text = leftLabel,

                    fontSize = 11.sp,

                    color = Color(0xFF757575),

                    fontWeight = FontWeight.Medium,

                    maxLines = 1

                )

                Text(

                    text = leftValue,

                    fontSize = 13.sp,

                    color = Color(0xFF212121),

                    maxLines = 2

                )

            }

            Column(

                modifier = Modifier

                    .weight(1f)

                    .background(rightBg, RoundedCornerShape(4.dp))

                    .padding(horizontal = 6.dp, vertical = 2.dp)

            ) {

                Text(

                    text = rightLabel,

                    fontSize = 11.sp,

                    color = Color(0xFF757575),

                    fontWeight = FontWeight.Medium,

                    maxLines = 1

                )

                Text(

                    text = rightValue,

                    fontSize = 13.sp,

                    color = Color(0xFF212121),

                    maxLines = 2

                )

            }

        }

    }

}

@Composable

private fun DualClockBar(northCity: CityInfo, antipodalInfo: AntipodalInfo) {

    val ctx = LocalContext.current

    val langCode = LanguageHelper.getSavedLanguage(ctx)

    val northZone = remember { ZoneId.of(CityDataSource.toZoneId(northCity.id)) }

    val southZone = remember { ZoneId.of(antipodalInfo.antipodalZoneId) }

    var now by remember { mutableStateOf(java.time.LocalTime.now()) }

    LaunchedEffect(Unit) {

        while (true) {

            kotlinx.coroutines.delay(1000L)

            now = java.time.LocalTime.now()

        }

    }

    val northTime = java.time.ZonedDateTime.now(northZone)

    val southTime = java.time.ZonedDateTime.now(southZone)

    val offsetHours = (antipodalInfo.antipodalUtcOffsetMinutes - northCity.utcOffsetMinutes) / 60

    Row(

        modifier = Modifier

            .fillMaxWidth()

            .background(Color(0xFFE3F2FD))

            .padding(horizontal = 8.dp, vertical = 4.dp),

        horizontalArrangement = Arrangement.SpaceEvenly,

        verticalAlignment = Alignment.CenterVertically

    ) {

        Text(

            text = "${getCityDisplayName(northCity, langCode)} ${formatCityUtc(northCity)} ${northTime.hour.toString().padStart(2, '0')}:${northTime.minute.toString().padStart(2, '0')}",

            fontSize = 12.sp,

            fontWeight = FontWeight.Bold,

            color = Color(0xFF1565C0),

            maxLines = 1,

            overflow = TextOverflow.Ellipsis

        )

        Text(

            text = "${stringResource(R.string.home_time_diff)}${kotlin.math.abs(offsetHours)}h",

            fontSize = 10.sp,

            color = Color(0xFF1565C0).copy(alpha = 0.6f)

        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(

                text = "${getAntipodalDisplayName(antipodalInfo, langCode)} ${formatAntipodalUtc(antipodalInfo)} ${southTime.hour.toString().padStart(2, '0')}:${southTime.minute.toString().padStart(2, '0')}",

                fontSize = 12.sp,

                fontWeight = FontWeight.Bold,

                color = Color(0xFF1565C0),

                maxLines = 1,

                overflow = TextOverflow.Ellipsis

            )

            if (getNearbyCityName(antipodalInfo, langCode).isNotEmpty()) {

                Text(

                    text = "${stringResource(R.string.home_reference)}：${getNearbyCityName(antipodalInfo, langCode)}",

                    fontSize = 8.sp,

                    color = Color(0xFF1565C0).copy(alpha = 0.5f)

                )

            }

        }

    }

}

private fun weekdayToIndex(weekdayName: String): Int {
    val names = CalendarConstants.WEEKDAY_NAMES
    for (i in names.indices) {
        if (weekdayName.contains(names[i])) return i
    }
    return 0
}

@Composable
private fun formatSolarTermDateTime(term: SolarTermInfo): String {
    val ctx = LocalContext.current
    return LocalizedStringProvider.formatSolarTermDateTime(ctx, term.year, term.month, term.day, term.hour, term.minute)
}

@Composable
private fun formatSolarTermLocalDateTime(term: SolarTermInfo, northZoneId: String, southZoneId: String): String {
    val ctx = LocalContext.current
    val north = java.time.LocalDateTime.of(term.year, term.month, term.day, term.hour, term.minute, term.second)
    val south = north.atZone(java.time.ZoneId.of(northZoneId))
        .withZoneSameInstant(java.time.ZoneId.of(southZoneId))
        .toLocalDateTime()
    var southMonth = south.monthValue + 6
    val southYear = if (south.monthValue < 7) south.year - 1 else south.year
    if (southMonth > 12) {
        southMonth -= 12
    }
    val southDay = south.dayOfMonth
    return LocalizedStringProvider.formatSolarTermDateTime(ctx, southYear, southMonth, southDay, south.hour, south.minute)
}

private fun calcDailyStarLodge(year: Int, month: Int, day: Int, context: android.content.Context): Pair<String, String> {

    // 值日星宿：28宿按固定顺序逐日轮值，全球统一，不分南北半球

    val baseJd = PlanetPositionCalc.gregorianToJD(2025, 1, 1)
    val currentJd = PlanetPositionCalc.gregorianToJD(year, month, day)
    val dayDiff = (currentJd - baseJd).toLong()

    // 2025-01-01为参宿(index 20)，校准偏移量20

    val index = (((dayDiff + 20) % 28 + 28) % 28).toInt()

    val text = LocalizedStringProvider.getMansionWithSymbol(context, index)

    return Pair(text, text)

}

private fun calcMonthlyStarLodge(year: Int, month: Int, day: Int, context: android.content.Context): Pair<String, String> {

    // 月躔星宿：根据月亮实际黄经位置查宿，天球坐标全球一致，不分南北半球

    val jd = PlanetPositionCalc.gregorianToJD(year, month, day)

    val moonPos = PlanetPositionCalc.calcMoonPosition(jd)

    val index = XiuBoundary.findXiu(moonPos.eclipticLon)

    val text = LocalizedStringProvider.getMansionWithSymbol(context, index)

    return Pair(text, text)

}

private fun calcSolarStarLodge(year: Int, month: Int, day: Int, context: android.content.Context): Pair<String, String> {

    // 日躔星宿：根据太阳实际黄经位置查宿，天球坐标全球一致，不分南北半球

    val jd = PlanetPositionCalc.gregorianToJD(year, month, day)

    val sunLon = PlanetPositionCalc.calcSunLon(jd)

    val index = XiuBoundary.findXiu(sunLon)

    val text = LocalizedStringProvider.getMansionWithSymbol(context, index)

    return Pair(text, text)

}

@Composable

private fun getSouthWeekday(year: Int, month: Int, day: Int, northCityId: String, antipodalZoneId: String): String {

    val northZone = ZoneId.of(CityDataSource.toZoneId(northCityId))

    val southZone = ZoneId.of(antipodalZoneId)

    val northDate = LocalDate.of(year, month, day)

    val northZdt = ZonedDateTime.of(northDate, LocalTime.NOON, northZone)

    val southZdt = northZdt.withZoneSameInstant(southZone)

    val weekDays = arrayOf(stringResource(R.string.weekday_full_mon), stringResource(R.string.weekday_full_tue), stringResource(R.string.weekday_full_wed), stringResource(R.string.weekday_full_thu), stringResource(R.string.weekday_full_fri), stringResource(R.string.weekday_full_sat), stringResource(R.string.weekday_full_sun))

    return weekDays[southZdt.dayOfWeek.value - 1]

}

@Composable

private fun formatCityLocation(city: CityInfo): String {

    val latDir = if (city.latitude >= 0) stringResource(R.string.settings_north_lat) else stringResource(R.string.settings_south_lat)

    val lonDir = if (city.longitude >= 0) stringResource(R.string.settings_east_lon) else stringResource(R.string.settings_west_lon)

    return "${latDir}${"%.1f".format(kotlin.math.abs(city.latitude))}° ${lonDir}${"%.1f".format(kotlin.math.abs(city.longitude))}°"

}

private fun getCityDisplayName(city: CityInfo, langCode: String): String {
    return when (langCode) {
        LanguageHelper.LANG_EN -> city.nameEn
        LanguageHelper.LANG_ES -> city.nameEs
        else -> city.nameZh
    }
}

private fun getAntipodalDisplayName(info: AntipodalInfo, langCode: String): String {
    return when (langCode) {
        LanguageHelper.LANG_EN -> info.displayNameEn
        LanguageHelper.LANG_ES -> info.displayNameEn  // fallback to English for Spanish
        else -> info.displayNameZh
    }
}

private fun getNearbyCityName(info: AntipodalInfo, langCode: String): String {
    return when (langCode) {
        LanguageHelper.LANG_EN -> info.nearbyCityNameEn
        LanguageHelper.LANG_ES -> info.nearbyCityNameEn  // fallback to English for Spanish
        else -> info.nearbyCityNameZh
    }
}

private fun formatCityUtc(city: CityInfo): String {

    val sign = if (city.utcOffsetMinutes >= 0) "+" else ""

    val hours = city.utcOffsetMinutes / 60

    val minutes = city.utcOffsetMinutes % 60

    return "UTC${sign}${hours}${if (minutes != 0) ":${minutes}" else ""}"

}

@Composable

private fun formatAntipodalLocation(info: AntipodalInfo): String {

    val latDir = if (info.lat >= 0) stringResource(R.string.settings_north_lat) else stringResource(R.string.settings_south_lat)

    val lonDir = if (info.lon >= 0) stringResource(R.string.settings_east_lon) else stringResource(R.string.settings_west_lon)

    return "${latDir}${"%.1f".format(kotlin.math.abs(info.lat))}° ${lonDir}${"%.1f".format(kotlin.math.abs(info.lon))}°"

}

private fun formatAntipodalUtc(info: AntipodalInfo): String {

    val sign = if (info.antipodalUtcOffsetMinutes >= 0) "+" else ""

    val hours = info.antipodalUtcOffsetMinutes / 60

    val minutes = info.antipodalUtcOffsetMinutes % 60

    return "UTC${sign}${hours}${if (minutes != 0) ":${minutes}" else ""}"

}

@Composable

private fun DualSolarTermCard(calendarDay: CalendarDay, northCity: CityInfo, antipodalInfo: AntipodalInfo, northCityId: String) {

    val ctx = LocalContext.current

    val currentN = calendarDay.currentSolarTerm

    val nextN = calendarDay.nextSolarTerm

    val countdown = calendarDay.daysUntilNextTerm

    val countdownDays = countdown / 3600 / 24

    Card(

        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FFF0)),

        shape = RoundedCornerShape(12.dp),

        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)

    ) {

        Column(modifier = Modifier.padding(10.dp)) {

            Row(

                modifier = Modifier

                    .fillMaxWidth()

                    .clip(RoundedCornerShape(6.dp))

                    .background(Color(0xFFE3F2FD))

                    .padding(vertical = 4.dp),

                horizontalArrangement = Arrangement.Center

            ) {

                Text(

                    text = stringResource(R.string.home_solar_term_conversion),

                    fontSize = 12.sp,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF1565C0)

                )

            }

            Spacer(Modifier.height(6.dp))

            if (currentN != null) {

                Row(modifier = Modifier.fillMaxWidth()) {

                    Column(

                        modifier = Modifier

                            .weight(1f)

                            .clip(RoundedCornerShape(8.dp))

                            .background(Color(0xFFFFF3E0))

                            .padding(6.dp),

                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        Text(LocalizedStringProvider.getWeekdayName(ctx, weekdayToIndex(calendarDay.weekday)), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = LocalizedStringProvider.getSolarTermByName(ctx, currentN.northName),

                            fontSize = 18.sp,

                            fontWeight = FontWeight.Bold,

                            color = Color(0xFFE65100),

                            maxLines = 1,

                            overflow = TextOverflow.Ellipsis
                        )

                    }

                    Spacer(Modifier.width(4.dp))

                    Column(

                        modifier = Modifier

                            .weight(1f)

                            .clip(RoundedCornerShape(8.dp))

                            .background(Color(0xFFE3F2FD))

                            .padding(6.dp),

                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        Text(getSouthWeekday(calendarDay.gregorianYear, calendarDay.gregorianMonth, calendarDay.gregorianDay, northCityId, antipodalInfo.antipodalZoneId), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))

                        Spacer(Modifier.height(4.dp))

                        Text(

                            text = LocalizedStringProvider.getSolarTermByName(ctx, currentN.name),

                            fontSize = 18.sp,

                            fontWeight = FontWeight.Bold,

                            color = Color(0xFF0D47A1),

                            maxLines = 1,

                            overflow = TextOverflow.Ellipsis
                        )

                    }

                }

                Spacer(Modifier.height(4.dp))

                Row(

                    modifier = Modifier

                        .fillMaxWidth()

                        .clip(RoundedCornerShape(4.dp))

                        .background(Color(0xFFF5F5F5))

                        .padding(horizontal = 8.dp, vertical = 3.dp)

                ) {

                    Text(

                        text = formatSolarTermDateTime(currentN),

                        fontSize = 9.sp,

                        color = Color(0xFFE65100),

                        modifier = Modifier.weight(1f),

                        maxLines = 2

                    )

                    Spacer(Modifier.width(4.dp))

                    Text(

                        text = formatSolarTermLocalDateTime(currentN, CityDataSource.toZoneId(northCityId), antipodalInfo.antipodalZoneId),

                        fontSize = 9.sp,

                        color = Color(0xFF0D47A1),

                        modifier = Modifier.weight(1f),

                        maxLines = 2

                    )

                }

            }

            if (nextN != null) {

                Spacer(Modifier.height(6.dp))

                Divider(color = Color(0xFFE0E0E0))

                Spacer(Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {

                    Column(

                        modifier = Modifier

                            .weight(1f)

                            .clip(RoundedCornerShape(8.dp))

                            .background(Color(0xFFFFF8E1))

                            .padding(6.dp),

                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        Text(

                            text = LocalizedStringProvider.getSolarTermByName(ctx, nextN.northName),

                            fontSize = 15.sp,

                            fontWeight = FontWeight.Bold,

                            color = Color(0xFFE65100),

                            maxLines = 1,

                            overflow = TextOverflow.Ellipsis
                        )

                        if (countdown > 0) Text(stringResource(R.string.home_days_later, countdownDays), fontSize = 9.sp, color = Color(0xFF8D6E63))

                    }

                    Spacer(Modifier.width(4.dp))

                    Column(

                        modifier = Modifier

                            .weight(1f)

                            .clip(RoundedCornerShape(8.dp))

                            .background(Color(0xFFE8EAF6))

                            .padding(6.dp),

                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        Text(

                            text = LocalizedStringProvider.getSolarTermByName(ctx, nextN.name),

                            fontSize = 15.sp,

                            fontWeight = FontWeight.Bold,

                            color = Color(0xFF0D47A1),

                            maxLines = 1,

                            overflow = TextOverflow.Ellipsis
                        )

                        if (countdown > 0) Text(stringResource(R.string.home_days_later, countdownDays), fontSize = 9.sp, color = Color(0xFF78909C))

                    }

                }

                Spacer(Modifier.height(4.dp))

                Row(

                    modifier = Modifier

                        .fillMaxWidth()

                        .clip(RoundedCornerShape(4.dp))

                        .background(Color(0xFFF5F5F5))

                        .padding(horizontal = 8.dp, vertical = 3.dp)

                ) {

                    Text(

                        text = formatSolarTermDateTime(nextN),

                        fontSize = 9.sp,

                        color = Color(0xFFE65100),

                        modifier = Modifier.weight(1f),

                        maxLines = 2

                    )

                    Spacer(Modifier.width(4.dp))

                    Text(

                        text = formatSolarTermLocalDateTime(nextN, CityDataSource.toZoneId(northCityId), antipodalInfo.antipodalZoneId),

                        fontSize = 9.sp,

                        color = Color(0xFF0D47A1),

                        modifier = Modifier.weight(1f),

                        maxLines = 2

                    )

                }

            }

        }

    }

}

@Composable

private fun InfoRow(label: String, value: String) {

    Row(

        verticalAlignment = Alignment.CenterVertically,

        modifier = Modifier.padding(vertical = 2.dp)

    ) {

        Text(

            text = "$label: ",

            fontSize = 13.sp,

            color = Color(0xFF757575),

            fontWeight = FontWeight.Medium

        )

        Text(

            text = value,

            fontSize = 14.sp,

            color = Color(0xFF212121)

        )

    }

}

@Composable

private fun LunisolarCalendarTab(

    monthDays: List<MonthDayItem>,

    viewYear: Int,

    viewMonth: Int,

    selectedYear: Int,

    selectedMonth: Int,

    selectedDay: Int,

    onSelectDay: (Int, Int, Int) -> Unit,

    calendarDay: CalendarDay?,

    northCity: CityInfo,

    antipodalInfo: AntipodalInfo

) {

    Column(

        modifier = Modifier

            .fillMaxSize()

            .verticalScroll(rememberScrollState())

    ) {

        WeekdayHeader()

        LunisolarMonthGrid(

            monthDays = monthDays,

            viewYear = viewYear,

            viewMonth = viewMonth,

            selectedYear = selectedYear,

            selectedMonth = selectedMonth,

            selectedDay = selectedDay,

            onSelectDay = onSelectDay

        )

        if (calendarDay != null) {

            Spacer(Modifier.height(12.dp))

            LunisolarDateInfoCard(calendarDay, northCity, antipodalInfo)

        }

    }

}

@Composable

private fun LunisolarMonthGrid(

    monthDays: List<MonthDayItem>,

    viewYear: Int,

    viewMonth: Int,

    selectedYear: Int,

    selectedMonth: Int,

    selectedDay: Int,

    onSelectDay: (Int, Int, Int) -> Unit

) {

    val chunkedDays = monthDays.chunked(7)

    Column(modifier = Modifier.fillMaxWidth()) {

        chunkedDays.forEach { row ->

            Row(modifier = Modifier.fillMaxWidth()) {

                row.forEach { item ->

                    val isSelected = item.gregorianYear == selectedYear &&

                            item.gregorianMonth == selectedMonth &&

                            item.gregorianDay == selectedDay

                    LunisolarDayCell(

                        item = item,

                        isSelected = isSelected,

                        onClick = { onSelectDay(item.gregorianYear, item.gregorianMonth, item.gregorianDay) }

                    )

                }

            }

        }

    }

}

@Composable

private fun RowScope.LunisolarDayCell(

    item: MonthDayItem,

    isSelected: Boolean,

    onClick: () -> Unit

) {

    val ctx = LocalContext.current

    val bgColor = when {

        isSelected -> Color(0xFFBBDEFB)

        item.isToday -> Color(0xFFFFF3E0)

        item.solarTermName != null && item.isCurrentMonth -> Color(0xFFE8F5E9)

        !item.isCurrentMonth -> Color(0xFFF5F5F5)

        else -> Color.White

    }

    val textColor = when {

        isSelected -> Color(0xFF1565C0)

        !item.isCurrentMonth -> Color(0xFFBDBDBD)

        item.solarTermName != null -> Color(0xFF2E7D32)

        item.isWeekend && !item.isToday -> Color(0xFFE53935)

        else -> Color(0xFF212121)

    }

    Box(

        modifier = Modifier

            .weight(1f)

            .aspectRatio(0.85f)

            .clickable { onClick() }

            .background(bgColor),

        contentAlignment = Alignment.Center

    ) {

        Column(

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            if (item.isLunarNewMonth && item.isCurrentMonth) {

                Text(

                    text = LocalizedStringProvider.getNorthLunarMonthByName(ctx, item.lunarMonthName),

                    fontSize = 9.sp,

                    color = if (isSelected) Color(0xFFFFD700) else Color(0xFFE65100),

                    maxLines = 1

                )

            }

            if (item.isToday && !isSelected) {

                Box(

                    modifier = Modifier

                        .size(26.dp)

                        .clip(CircleShape)

                        .background(Color(0xFFE65100)),

                    contentAlignment = Alignment.Center

                ) {

                    Text(

                        text = LocalizedStringProvider.getLunarDayByName(ctx, item.lunarDayName),

                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1

                    )

                }

            } else {

                Text(

                    text = LocalizedStringProvider.getLunarDayByName(ctx, item.lunarDayName),

                    fontSize = 12.sp,

                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,

                    color = textColor,

                    maxLines = 1

                )

            }

            if (item.solarTermName != null && item.isCurrentMonth) {

                Text(

                    text = LocalizedStringProvider.getSolarTermByName(ctx, item.solarTermName),

                    fontSize = 8.sp,

                    color = if (isSelected) Color(0xFFFFD700) else Color(0xFF2E7D32),

                    maxLines = 1,

                    overflow = TextOverflow.Ellipsis

                )

            }

        }

    }

}

@Composable

private fun LunisolarDateInfoCard(calendarDay: CalendarDay, northCity: CityInfo, antipodalInfo: AntipodalInfo) {

    val ctx = LocalContext.current

    Card(

        modifier = Modifier

            .fillMaxWidth()

            .padding(horizontal = 12.dp),

        colors = CardDefaults.cardColors(containerColor = Color.White),

        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),

        shape = RoundedCornerShape(12.dp)

    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(

                text = "${stringResource(R.string.home_lunar_calendar)}: ${LocalizedStringProvider.getNorthLunarMonthByName(ctx, calendarDay.lunarDate.monthName)}${LocalizedStringProvider.getLunarDayByName(ctx, calendarDay.lunarDate.dayName)}",

                fontSize = 18.sp,

                fontWeight = FontWeight.Bold,

                color = Color(0xFF1565C0)

            )

            Spacer(Modifier.height(8.dp))

            Divider(color = Color(0xFFE0E0E0))

            Spacer(Modifier.height(8.dp))

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween

            ) {

                Column {

                    Text(stringResource(R.string.home_north_hemisphere), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    InfoRow(stringResource(R.string.home_ganzhi), calendarDay.northYearGanZhi)
                    InfoRow(stringResource(R.string.home_zodiac), LocalizedStringProvider.getZodiacByName(ctx, calendarDay.northShengXiao))

                }

                Column(

                    horizontalAlignment = Alignment.End

                ) {

                    Text(stringResource(R.string.home_south_hemisphere), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                    InfoRow(stringResource(R.string.home_ganzhi), calendarDay.southYearGanZhi)
                    InfoRow(stringResource(R.string.home_zodiac), LocalizedStringProvider.getZodiacByName(ctx, calendarDay.southShengXiao))

                }

            }

            if (calendarDay.lunarDate.isLeapMonth) {

                Spacer(Modifier.height(4.dp))

                InfoRow(stringResource(R.string.home_leap_month), stringResource(R.string.home_yes))

            }

            Spacer(Modifier.height(8.dp))

            Divider(color = Color(0xFFE0E0E0))

            Spacer(Modifier.height(8.dp))

            if (calendarDay.currentSolarTerm != null) {

                Spacer(Modifier.height(8.dp))

                Divider(color = Color(0xFFE0E0E0))

                Spacer(Modifier.height(8.dp))

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween

                ) {

                    InfoRow(stringResource(R.string.home_north_term), LocalizedStringProvider.getSolarTermByName(ctx, calendarDay.currentSolarTerm.northName))

                    InfoRow(stringResource(R.string.home_south_term), LocalizedStringProvider.getSolarTermByName(ctx, calendarDay.currentSolarTerm.name))

                }

            }

        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)

@Composable

private fun YearMonthPickerDialog(

    initialYear: Int,

    initialMonth: Int,

    onConfirm: (Int, Int) -> Unit,

    onDismiss: () -> Unit

) {

    var pickerYear by remember { mutableIntStateOf(initialYear) }

    var pickerMonth by remember { mutableIntStateOf(initialMonth) }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {

            Text(stringResource(R.string.home_select_year_month), fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))

        },

        text = {

            Column(

                horizontalAlignment = Alignment.CenterHorizontally,

                modifier = Modifier.fillMaxWidth()

            ) {

                Row(

                    verticalAlignment = Alignment.CenterVertically,

                    horizontalArrangement = Arrangement.Center,

                    modifier = Modifier.fillMaxWidth()

                ) {

                    IconButton(onClick = { if (pickerYear > 1900) pickerYear-- }) {

                        Icon(Icons.Default.ChevronLeft, stringResource(R.string.home_decrease))

                    }

                    Text(

                        text = LocalizedStringProvider.formatYear(LocalContext.current, pickerYear),

                        fontSize = 24.sp,

                        fontWeight = FontWeight.Bold,

                        color = Color(0xFF1565C0),

                        modifier = Modifier.width(100.dp),

                        textAlign = TextAlign.Center

                    )

                    IconButton(onClick = { if (pickerYear < 2100) pickerYear++ }) {

                        Icon(Icons.Default.ChevronRight, stringResource(R.string.home_increase))

                    }

                }

                Spacer(Modifier.height(16.dp))

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceEvenly

                ) {

                    for (row in 0..2) {

                        Column {

                            for (col in 0..3) {

                                val m = row * 4 + col + 1

                                val isSelected = m == pickerMonth

                                TextButton(

                                    onClick = { pickerMonth = m },

                                    modifier = Modifier.size(60.dp),

                                    colors = ButtonDefaults.textButtonColors(

                                        contentColor = if (isSelected) Color(0xFF1565C0) else Color(0xFF616161)

                                    )

                                ) {

                                    Text(

                                        text = LocalizedStringProvider.formatMonth(LocalContext.current, m),

                                        fontSize = if (isSelected) 16.sp else 14.sp,

                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                                    )

                                }

                            }

                        }

                    }

                }

            }

        },

        confirmButton = {

            TextButton(onClick = { onConfirm(pickerYear, pickerMonth) }) {

                Text(stringResource(R.string.common_ok), fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))

            }

        },

        dismissButton = {

            TextButton(onClick = onDismiss) {

                Text(stringResource(R.string.common_cancel), color = Color(0xFF757575))

            }

        }

    )

}