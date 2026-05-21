package com.example.wannianli.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wannianli.data.model.CalendarDay
import com.example.wannianli.viewmodel.MainViewModel
import com.example.wannianli.viewmodel.MonthDayItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onDrawerToggle: () -> Unit
) {
    val calendarDay by viewModel.calendarDay.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedTab == 0) "万年历" else "择日",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDrawerToggle) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "菜单"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadToday() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = null
                        )
                    },
                    label = { Text("首页") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedTab == 1) Icons.Filled.DateRange else Icons.Outlined.DateRange,
                            contentDescription = null
                        )
                    },
                    label = { Text("择日") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                0 -> {
                    when {
                        isLoading -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("正在加载日历数据...")
                            }
                        }
                        errorMessage != null -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = errorMessage ?: "未知错误",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loadToday() }) {
                                    Text("重试")
                                }
                            }
                        }
                        calendarDay != null -> {
                            CalendarContent(calendarDay = calendarDay!!, viewModel = viewModel)
                        }
                    }
                }
                1 -> {
                    ZeRiScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun CalendarContent(calendarDay: CalendarDay, viewModel: MainViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonthViewSection(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        GregorianLunarSection(calendarDay)

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        Spacer(modifier = Modifier.height(16.dp))

        SolarTermSection(calendarDay)
    }
}

@Composable
private fun MonthViewSection(viewModel: MainViewModel) {
    val monthDays by viewModel.monthDays.collectAsState()
    val viewYear by viewModel.viewYear.collectAsState()
    val viewMonth by viewModel.viewMonth.collectAsState()

    val monthNames = arrayOf(
        "一月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.navigateMonth(-1) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "上月"
                    )
                }

                Text(
                    text = "${viewYear}年 ${monthNames[viewMonth - 1]}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { viewModel.navigateMonth(1) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "下月"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val weekHeaders = arrayOf("一", "二", "三", "四", "五", "六", "日")
                for (header in weekHeaders) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = header,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (header == "六" || header == "日")
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val chunks = monthDays.chunked(7)
            for (row in chunks) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (item in row) {
                        MonthDayCell(
                            item = item,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.selectDay(
                                    item.gregorianYear,
                                    item.gregorianMonth,
                                    item.gregorianDay
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    item: MonthDayItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dayTextColor = when {
        item.isSelected -> MaterialTheme.colorScheme.onPrimary
        item.holidayName != null && item.isCurrentMonth -> Color(0xFFD32F2F)
        item.isWeekend -> MaterialTheme.colorScheme.error
        !item.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val dayBgColor = when {
        item.isSelected -> MaterialTheme.colorScheme.primary
        item.isToday && !item.isSelected -> Color.Transparent
        else -> Color.Transparent
    }

    val todayBorder = if (item.isToday && !item.isSelected)
        MaterialTheme.colorScheme.primary
    else
        Color.Transparent

    val subTextColor = when {
        item.isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
        !item.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        item.solarTermName != null -> Color(0xFF2E7D32)
        item.holidayName != null -> Color(0xFFD32F2F)
        item.isLunarNewMonth -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val subText: String = when {
        item.holidayName != null && item.isCurrentMonth -> item.holidayName
        item.solarTermName != null && item.isCurrentMonth -> item.solarTermName
        item.isLunarNewMonth && item.isCurrentMonth -> item.lunarMonthName
        else -> item.lunarDayName
    }

    val showSubText = item.holidayName != null || item.solarTermName != null ||
            item.isLunarNewMonth || item.isCurrentMonth

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(dayBgColor)
            .border(
                width = if (item.isToday && !item.isSelected) 2.dp else 0.dp,
                color = todayBorder,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (item.isLunarNewMonth && item.isCurrentMonth && item.holidayName == null) {
            Text(
                text = item.lunarMonthName,
                fontSize = 11.sp,
                fontWeight = if (item.isSelected || item.isToday) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${item.gregorianDay}",
                    fontSize = 13.sp,
                    fontWeight = if (item.isSelected || item.isToday) FontWeight.Bold else FontWeight.Normal,
                    color = dayTextColor,
                    maxLines = 1
                )

                if (showSubText && !item.isLunarNewMonth) {
                    Text(
                        text = subText,
                        fontSize = 9.sp,
                        color = subTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GregorianLunarSection(calendarDay: CalendarDay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "公历：${calendarDay.gregorianYear}年${calendarDay.gregorianMonth}月${calendarDay.gregorianDay}日",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "农历：",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                if (calendarDay.lunarDate.isLeapMonth) {
                    Text(
                        text = "闰",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${calendarDay.lunarDate.monthName}${calendarDay.lunarDate.dayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "干支年：${calendarDay.lunarDate.yearGanZhi}年",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PillarCard(label = "年柱", value = calendarDay.lunarDate.yearGanZhi)
                PillarCard(label = "月柱", value = calendarDay.lunarDate.monthGanZhi)
                PillarCard(label = "日柱", value = calendarDay.lunarDate.dayGanZhi)
                PillarCard(label = "时柱", value = calendarDay.lunarDate.hourGanZhi)
            }
        }
    }
}

@Composable
private fun PillarCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SolarTermSection(calendarDay: CalendarDay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AcUnit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "节气",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (calendarDay.currentSolarTerm != null) {
                Text(
                    text = calendarDay.currentSolarTerm.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = calendarDay.currentSolarTerm.fullDateTime,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            } else if (calendarDay.nextSolarTerm != null) {
                val totalSeconds = calendarDay.daysUntilNextTerm
                val days = totalSeconds / 86400
                val hours = (totalSeconds % 86400) / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                Text(
                    text = "下一个节气",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${calendarDay.nextSolarTerm.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "距今日 ${days} 天 ${hours} 小时 ${minutes} 分 ${seconds} 秒",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = calendarDay.nextSolarTerm.fullDateTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}