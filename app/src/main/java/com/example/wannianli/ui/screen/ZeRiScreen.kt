package com.example.wannianli.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wannianli.engine.*
import com.example.wannianli.viewmodel.MainViewModel
import java.util.Calendar

data class DayZeRiInfo(
    val year: Int,
    val month: Int,
    val day: Int,
    val weekday: String,
    val lunarMonthName: String,
    val lunarDayName: String,
    val lunarMonth: Int,
    val lunarDay: Int,
    val isLeapMonth: Boolean,
    val yearGanZhi: String,
    val monthGanZhi: String,
    val dayGanZhi: String,
    val hourGanZhi: String,
    val zeRiResult: ZeRiResult,
    val yi: List<String>,
    val ji: List<String>
)

@Composable
fun ZeRiScreen(viewModel: MainViewModel) {
    val repository = viewModel.getRepository()
    val viewYear by viewModel.viewYear.collectAsState()
    val viewMonth by viewModel.viewMonth.collectAsState()

    val today = remember { Calendar.getInstance() }

    var currentYear by remember { mutableStateOf(today.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(today.get(Calendar.MONTH) + 1) }
    var currentDay by remember { mutableStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    var currentDayInfo by remember { mutableStateOf<DayZeRiInfo?>(null) }
    var monthDayList by remember { mutableStateOf<List<DayZeRiInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedSchoolTab by remember { mutableStateOf(0) }
    var selectedSubTab by remember { mutableStateOf(0) }
    var sittingMountainIndex by remember { mutableStateOf(1) }
    var showMountainPicker by remember { mutableStateOf(false) }
    var cachedSolarTerms by remember { mutableStateOf<List<SolarTermCalculator.SolarTermResult>>(emptyList()) }
    var benMingUsageType by remember { mutableStateOf("嫁娶") }
    var maleBenMingYear by remember { mutableStateOf(1994) }
    var femaleBenMingYear by remember { mutableStateOf(1995) }
    var ownerBenMingYear by remember { mutableStateOf(1980) }
    var personBenMingYear by remember { mutableStateOf(1990) }
    var showBenMingSelector by remember { mutableStateOf(false) }

    LaunchedEffect(currentYear, currentMonth) {
        isLoading = true
        monthDayList = evaluateAllDays(currentYear, currentMonth, repository)
        val allTermsThisYear = repository.getSolarTermsForYear(currentYear)
        val allTermsLastYear = repository.getSolarTermsForYear(currentYear - 1)
        cachedSolarTerms = allTermsLastYear + allTermsThisYear
        val dayIdx = currentDay - 1
        currentDayInfo = monthDayList.getOrNull(dayIdx)
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DayNavigator(
            year = currentYear,
            month = currentMonth,
            day = currentDay,
            onPrevDay = {
                val prev = prevDay(currentYear, currentMonth, currentDay)
                currentYear = prev.first
                currentMonth = prev.second
                currentDay = prev.third
                currentDayInfo = monthDayList.getOrNull(prev.third - 1)
            },
            onNextDay = {
                val next = nextDay(currentYear, currentMonth, currentDay)
                currentYear = next.first
                currentMonth = next.second
                currentDay = next.third
                currentDayInfo = monthDayList.getOrNull(next.third - 1)
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                ZeRiDayHeader(currentDayInfo)

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedSchoolTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = selectedSchoolTab == 0,
                        onClick = { selectedSchoolTab = 0 },
                        text = { Text("正体五行") }
                    )
                    Tab(
                        selected = selectedSchoolTab == 1,
                        onClick = { selectedSchoolTab = 1 },
                        text = { Text("斗首择日") }
                    )
                    Tab(
                        selected = selectedSchoolTab == 2,
                        onClick = { selectedSchoolTab = 2 },
                        text = { Text("河洛日课") }
                    )
                    Tab(
                        selected = selectedSchoolTab == 3,
                        onClick = { selectedSchoolTab = 3 },
                        text = { Text("通天窍") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                BenMingSelectorCard(
                    benMingUsageType = benMingUsageType,
                    onUsageTypeChange = { benMingUsageType = it },
                    maleYear = maleBenMingYear,
                    onMaleYearChange = { maleBenMingYear = it },
                    femaleYear = femaleBenMingYear,
                    onFemaleYearChange = { femaleBenMingYear = it },
                    ownerYear = ownerBenMingYear,
                    onOwnerYearChange = { ownerBenMingYear = it },
                    personYear = personBenMingYear,
                    onPersonYearChange = { personBenMingYear = it }
                )

                val sharedBenMingResults = remember(
                    currentDayInfo?.dayGanZhi ?: "", currentDayInfo?.hourGanZhi ?: "",
                    benMingUsageType, maleBenMingYear, femaleBenMingYear, ownerBenMingYear, personBenMingYear
                ) {
                    val info = currentDayInfo
                    if (info == null) emptyList()
                    else {
                        val dg = CalendarConstants.TIAN_GAN.indexOf(info.dayGanZhi[0].toString())
                        val dz = CalendarConstants.DI_ZHI.indexOf(info.dayGanZhi[1].toString())
                        val hg = CalendarConstants.TIAN_GAN.indexOf(info.hourGanZhi[0].toString())
                        val hz = CalendarConstants.DI_ZHI.indexOf(info.hourGanZhi[1].toString())
                        when (benMingUsageType) {
                            "嫁娶" -> listOf(
                                BenMingEngine.evaluate(maleBenMingYear, yearToGanZhi(maleBenMingYear), info.dayGanZhi, info.hourGanZhi, "男主", dg, dz, hg, hz),
                                BenMingEngine.evaluate(femaleBenMingYear, yearToGanZhi(femaleBenMingYear), info.dayGanZhi, info.hourGanZhi, "女主", dg, dz, hg, hz)
                            )
                            "入宅" -> listOf(
                                BenMingEngine.evaluate(ownerBenMingYear, yearToGanZhi(ownerBenMingYear), info.dayGanZhi, info.hourGanZhi, "宅主", dg, dz, hg, hz)
                            )
                            else -> listOf(
                                BenMingEngine.evaluate(personBenMingYear, yearToGanZhi(personBenMingYear), info.dayGanZhi, info.hourGanZhi, "主事人", dg, dz, hg, hz)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedSchoolTab) {
                    0 -> {
                        MountainSelector(
                            selectedIndex = sittingMountainIndex,
                            onSelect = { sittingMountainIndex = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (currentDayInfo != null) {
                            val info = currentDayInfo!!
                            val shaResult: ShanShaResult = remember(
                                info.yearGanZhi, info.monthGanZhi, info.dayGanZhi, sittingMountainIndex
                            ) {
                                val yg = CalendarConstants.TIAN_GAN.indexOf(info.yearGanZhi[0].toString())
                                val yz = CalendarConstants.DI_ZHI.indexOf(info.yearGanZhi[1].toString())
                                val mz = CalendarConstants.DI_ZHI.indexOf(info.monthGanZhi[1].toString())
                                val dz = CalendarConstants.DI_ZHI.indexOf(info.dayGanZhi[1].toString())
                                ShanShaEngine.evaluate(currentYear, currentMonth, currentDay, yg, yz, mz, dz, sittingMountainIndex)
                            }

                            val mountainAnalysis: MountainAnalysisResult = remember(
                                info.yearGanZhi, info.monthGanZhi, info.dayGanZhi, info.hourGanZhi,
                                sittingMountainIndex, shaResult
                            ) {
                                ZeRiEngine.buildMountainAnalysis(
                                    yearGanZhi = info.yearGanZhi,
                                    monthGanZhi = info.monthGanZhi,
                                    dayGanZhi = info.dayGanZhi,
                                    hourGanZhi = info.hourGanZhi,
                                    sittingMountainIndex = sittingMountainIndex,
                                    baseResult = info.zeRiResult,
                                    shanShaResult = shaResult
                                )
                            }

                            ZeRiDayDetailWithYiJi(info, mountainAnalysis)

                            if (sharedBenMingResults.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                sharedBenMingResults.forEach { bm ->
                                    BenMingResultCard(bm)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val coreTabooResult: CoreTabooResult = remember(
                                info.yearGanZhi, info.monthGanZhi, info.dayGanZhi,
                                info.lunarMonth, info.lunarDay, sittingMountainIndex,
                                cachedSolarTerms.hashCode()
                            ) {
                                val yg = CalendarConstants.TIAN_GAN.indexOf(info.yearGanZhi[0].toString())
                                val yz = CalendarConstants.DI_ZHI.indexOf(info.yearGanZhi[1].toString())
                                val mz = CalendarConstants.DI_ZHI.indexOf(info.monthGanZhi[1].toString())
                                val dz = CalendarConstants.DI_ZHI.indexOf(info.dayGanZhi[1].toString())
                                val dg = CalendarConstants.TIAN_GAN.indexOf(info.dayGanZhi[0].toString())
                                CoreTabooEngine.evaluate(
                                    currentYear, currentMonth, currentDay,
                                    yg, yz, mz, dz, dg,
                                    sittingMountainIndex,
                                    info.lunarMonth, info.lunarDay,
                                    cachedSolarTerms
                                )
                            }

                            val wuTuResult: WuTuResult = remember(
                                info.yearGanZhi, info.monthGanZhi, sittingMountainIndex, coreTabooResult
                            ) {
                                val yz = CalendarConstants.DI_ZHI.indexOf(info.yearGanZhi[1].toString())
                                val mz = CalendarConstants.DI_ZHI.indexOf(info.monthGanZhi[1].toString())
                                WuTuEngine.evaluate(yz, mz, sittingMountainIndex, coreTabooResult.shanShaResult)
                            }

                            ShanShaSection(
                                shaResult = coreTabooResult.shanShaResult,
                                mountainAnalysis = mountainAnalysis,
                                year = currentYear,
                                month = currentMonth,
                                day = currentDay
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            WuTuSection(wuTuResult)

                            Spacer(modifier = Modifier.height(8.dp))

                            CoreTabooSection(coreTabooResult, info.zeRiResult)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (currentDayInfo != null) {
                            JianchuMansionDeepCard(currentDayInfo!!)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TabRow(
                            selectedTabIndex = selectedSubTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Tab(
                                selected = selectedSubTab == 0,
                                onClick = { selectedSubTab = 0 },
                                text = { Text("详情分析") }
                            )
                            Tab(
                                selected = selectedSubTab == 1,
                                onClick = { selectedSubTab = 1 },
                                text = { Text("月份概览") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when (selectedSubTab) {
                            0 -> {
                                if (currentDayInfo != null) {
                                    val info = currentDayInfo!!
                                    val shaResult: ShanShaResult = remember(
                                        info.yearGanZhi, info.monthGanZhi, info.dayGanZhi, sittingMountainIndex
                                    ) {
                                        val yg = CalendarConstants.TIAN_GAN.indexOf(info.yearGanZhi[0].toString())
                                        val yz = CalendarConstants.DI_ZHI.indexOf(info.yearGanZhi[1].toString())
                                        val mz = CalendarConstants.DI_ZHI.indexOf(info.monthGanZhi[1].toString())
                                        val dz = CalendarConstants.DI_ZHI.indexOf(info.dayGanZhi[1].toString())
                                        ShanShaEngine.evaluate(currentYear, currentMonth, currentDay, yg, yz, mz, dz, sittingMountainIndex)
                                    }
                                    val mountainAnalysis: MountainAnalysisResult = remember(
                                        info.yearGanZhi, info.monthGanZhi, info.dayGanZhi, info.hourGanZhi,
                                        sittingMountainIndex, shaResult
                                    ) {
                                        ZeRiEngine.buildMountainAnalysis(
                                            yearGanZhi = info.yearGanZhi,
                                            monthGanZhi = info.monthGanZhi,
                                            dayGanZhi = info.dayGanZhi,
                                            hourGanZhi = info.hourGanZhi,
                                            sittingMountainIndex = sittingMountainIndex,
                                            baseResult = info.zeRiResult,
                                            shanShaResult = shaResult
                                        )
                                    }
                                    ZeRiAnalysisDetail(info, mountainAnalysis)
                                }
                            }
                            1 -> {
                                ZeRiMonthCalendarGrid(
                                    year = currentYear,
                                    month = currentMonth,
                                    dayList = monthDayList,
                                    currentDay = currentDay,
                                    onDayClick = { dayIdx ->
                                        currentDay = dayIdx + 1
                                        currentDayInfo = monthDayList.getOrNull(dayIdx)
                                        selectedSubTab = 0
                                    }
                                )
                            }
                        }
                    }
                    1 -> {
                        if (currentDayInfo != null) {
                            DouShouDayDetail(currentDayInfo!!, sittingMountainIndex, sharedBenMingResults)
                        }
                    }
                    2 -> {
                        if (currentDayInfo != null) {
                            HeLuoDayDetail(currentDayInfo!!, sharedBenMingResults)
                        }
                    }
                    3 -> {
                        if (currentDayInfo != null) {
                            TongTianQiaoSection(
                                info = currentDayInfo!!,
                                sittingMountainIndex = sittingMountainIndex,
                                currentYear = currentYear,
                                currentMonth = currentMonth,
                                currentDay = currentDay,
                                sharedBenMingResults = sharedBenMingResults
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ZeRiPrincipleCard(selectedSchoolTab)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun WuTuSection(result: WuTuResult) {
    val verdictBgColor = Color(result.verdictBgColor.toInt())
    val verdictTextColor = Color(result.verdictTextColor.toInt())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "乌兔太阳太阴·山家催旺化煞分析",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A148C)
                )
                Surface(
                    color = verdictBgColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = result.verdict,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = verdictBgColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = verdictBgColor.copy(alpha = 0.06f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = result.verdictSubText,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = verdictBgColor,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "太阳（年乌兔）：${result.sunYearZhi}年太阳到${result.sunMountain}，到向${result.sunDirection}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = if (result.sunArrivesAtShan) "  太阳到山，凶煞消散，加倍催旺龙气"
                else if (result.sunArrivesAtXiang) "  太阳到向，催福绵长"
                else "  太阳不到山向",
                style = MaterialTheme.typography.bodySmall,
                color = if (result.sunArrivesAtShan || result.sunArrivesAtXiang) Color(0xFF2E7D32)
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "太阴（月乌兔）：${result.moonMonthZhi}月太阴到${result.moonMountain}，到向${result.moonDirection}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = if (result.moonArrivesAtShan) "  太阴到山，催丁稳宅"
                else if (result.moonArrivesAtXiang) "  太阴到向，财禄自来"
                else "  太阴不到山向",
                style = MaterialTheme.typography.bodySmall,
                color = if (result.moonArrivesAtShan || result.moonArrivesAtXiang) Color(0xFF2E7D32)
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "乌兔吉凶深度解析",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A148C)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = verdictBgColor.copy(alpha = 0.06f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = result.sunDetail,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = Color(0xFF1B5E20).copy(alpha = 0.04f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = result.moonDetail,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.overallAnalysis,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF4A148C).copy(alpha = 0.06f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = result.ancientRule,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4A148C),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun CoreTabooSection(result: CoreTabooResult, zeRiResult: ZeRiResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "四大类神煞优先级分析",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A148C)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "先避山家地气煞，再避年家天地煞，再避当日气场煞，最后看日常小事",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            result.priorityGroups.forEach { group ->
                val titleColor = when (group.priority) {
                    1 -> Color(0xFFC62828)
                    2 -> Color(0xFFE65100)
                    3 -> Color(0xFF6A1B9A)
                    else -> Color(0xFF1565C0)
                }
                val bgColor = when {
                    group.anyViolated && group.priority == 1 -> Color(0xFFC62828).copy(alpha = 0.06f)
                    group.anyViolated && group.priority == 3 -> Color(0xFF6A1B9A).copy(alpha = 0.05f)
                    group.anyViolated -> titleColor.copy(alpha = 0.05f)
                    else -> Color(0xFF2E7D32).copy(alpha = 0.03f)
                }

                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = titleColor,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "优先${group.priority}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = group.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = titleColor
                                )
                            }
                            Surface(
                                color = if (group.anyViolated) titleColor.copy(alpha = 0.12f)
                                else Color(0xFF2E7D32).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = group.verdict,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (group.anyViolated) titleColor else Color(0xFF2E7D32)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = group.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        if (group.anyViolated) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = group.prohibition,
                                style = MaterialTheme.typography.labelSmall,
                                color = titleColor,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        group.checks.forEach { check ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = if (check.isViolated) "!" else "O",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (check.isViolated) titleColor else Color(0xFF2E7D32),
                                    modifier = Modifier.width(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = check.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = check.detail,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (check.isViolated) titleColor.copy(alpha = 0.8f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                    if (check.isViolated) {
                                        Text(
                                            text = "禁忌：${check.prohibition}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = titleColor.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShenShaConflictSection(result: ZeRiResult) {
    val conflicts = mutableListOf<String>()

    if (result.jianchu == "破" && (result.tianDe || result.yueDe)) {
        conflicts.add("破日为凶，但天德月德可化解其煞气。此日可用于求财、签约，但仍忌嫁娶入宅安葬。")
    }
    if (result.jianchu == "建" && result.tianDe) {
        conflicts.add("建日犯阳刚过盛，得天德化解，可用作修造，但仍忌安葬破土。")
    }
    if (result.jianchu == "危" && (result.tianDe || result.yueDe)) {
        conflicts.add("危日有险，天德月德护持转危为安。可用于祈福祭祀，忌远行。")
    }
    if (result.jianchu == "收" && result.tianDe) {
        conflicts.add("收日主收敛，天德可化解部分收敛之气，但仍忌疗病安葬。")
    }
    if (result.jianchu == "闭" && result.yueDe) {
        conflicts.add("闭日闭塞不通，月德可稍解其弊，祭祀纳财可用，开业出行仍忌。")
    }

    if (result.twentyEightMansion.contains("亢金") || result.twentyEightMansion.contains("心月") ||
        result.twentyEightMansion.contains("奎木") || result.twentyEightMansion.contains("鬼金")) {
        if (result.tianDe || result.yueDe) {
            conflicts.add("值日星宿虽凶，但天德月德可压制凶宿之力，可酌情用事。")
        } else {
            conflicts.add("值日星宿为凶宿，又无天德月德化解，慎用此日。")
        }
    }

    if (result.jianchu == "成" || result.jianchu == "开") {
        if (result.sanSha || result.suiPo) {
            conflicts.add("建除虽吉但犯年家重煞，吉不抵凶，不可用。天德月德再吉，犯年煞照样凶！")
        }
    }

    if (conflicts.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "神煞冲突深度解析",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE65100)
        )
        Spacer(modifier = Modifier.height(4.dp))
        conflicts.forEach { c ->
            Surface(
                color = Color(0xFFE65100).copy(alpha = 0.06f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = c,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun JianchuMansionDeepCard(info: DayZeRiInfo) {
    val result = info.zeRiResult

    val jianchuIndex = CalendarConstants.JIANCHU_NAMES.indexOf(result.jianchu)
    val jianchuDetail = if (jianchuIndex in 0..11) CalendarConstants.JIANCHU_DETAIL[jianchuIndex] else ""
    val jianchuYi = if (jianchuIndex in 0..11) CalendarConstants.JIANCHU_YI[jianchuIndex] else emptyList()
    val jianchuJi = if (jianchuIndex in 0..11) CalendarConstants.JIANCHU_JI[jianchuIndex] else emptyList()

    val mansionIndex = CalendarConstants.TWENTY_EIGHT_MANSIONS.indexOfFirst { it == result.twentyEightMansion }
    val mansionDetail = if (mansionIndex in 0..27) CalendarConstants.MANSION_DETAIL[mansionIndex] else ""
    val mansionYi = if (mansionIndex in 0..27) CalendarConstants.MANSION_YI[mansionIndex] else emptyList()
    val mansionJi = if (mansionIndex in 0..27) CalendarConstants.MANSION_JI[mansionIndex] else emptyList()

    val jianchuColor = if (result.jianchuJiXiong == "吉") Color(0xFF2E7D32) else if (result.jianchuJiXiong == "凶") Color(0xFFC62828) else Color(0xFFF9A825)
    val mansionColor = if (result.mansionJiXiong == "吉") Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "十二建除：",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = jianchuColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${result.jianchu}日",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = jianchuColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = jianchuDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                if (jianchuYi.isNotEmpty() || jianchuJi.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (jianchuYi.isNotEmpty()) {
                            Text(
                                text = "宜：",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = jianchuYi.joinToString("、"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (jianchuJi.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "忌：",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = jianchuJi.joinToString("、"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "二十八宿：",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = mansionColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = result.twentyEightMansion,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = mansionColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mansionDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                if (mansionYi.isNotEmpty() || mansionJi.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    if (mansionYi.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "宜：",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mansionYi.joinToString("、"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (mansionJi.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "忌：",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mansionJi.joinToString("、"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MountainSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val palaceGroups = listOf(
        "坎宫(北)" to listOf(0, 1, 2),
        "艮宫(东北)" to listOf(3, 4, 5),
        "震宫(东)" to listOf(6, 7, 8),
        "巽宫(东南)" to listOf(9, 10, 11),
        "离宫(南)" to listOf(12, 13, 14),
        "坤宫(西南)" to listOf(15, 16, 17),
        "兑宫(西)" to listOf(18, 19, 20),
        "乾宫(西北)" to listOf(21, 22, 23)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "当前坐山：${ShanShaEngine.MOUNTAIN_NAMES[selectedIndex]}山",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            palaceGroups.forEach { (palace, indices) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = palace,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(88.dp)
                    )
                    indices.forEach { idx ->
                        val isSelected = idx == selectedIndex
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .clickable { onSelect(idx) }
                        ) {
                            Text(
                                text = "${ShanShaEngine.MOUNTAIN_NAMES[idx]}山",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShanShaSection(
    shaResult: ShanShaResult,
    mountainAnalysis: MountainAnalysisResult,
    year: Int,
    month: Int,
    day: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (shaResult.overallSafe) Color(0xFF2E7D32).copy(alpha = 0.06f)
                             else Color(0xFFC62828).copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "山家煞分析",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = if (shaResult.overallSafe) Color(0xFF2E7D32).copy(alpha = 0.15f)
                            else Color(0xFFC62828).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = shaResult.overallVerdict,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (shaResult.overallSafe) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = shaResult.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (shaResult.criticalViolations.isNotEmpty() || shaResult.warningViolations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                shaResult.criticalViolations.forEach { c ->
                    ShaViolationChip(c, true)
                }
                shaResult.warningViolations.forEach { w ->
                    ShaViolationChip(w, false)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = mountainAnalysis.shanShaPriorityText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ShaViolationChip(check: ShaCheck, isCritical: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = if (isCritical) Color(0xFFC62828).copy(alpha = 0.1f)
                    else Color(0xFFE65100).copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = check.name,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCritical) Color(0xFFC62828) else Color(0xFFE65100)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = check.detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DayNavigator(
    year: Int,
    month: Int,
    day: Int,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevDay) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "前一天")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount > 50) onPrevDay()
                            else if (dragAmount < -50) onNextDay()
                        }
                    }
            ) {
                Text(
                    text = "${year}年${month}月${day}日",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            IconButton(onClick = onNextDay) {
                Icon(Icons.Default.ChevronRight, contentDescription = "后一天")
            }
        }
    }
}

@Composable
private fun ZeRiDayHeader(info: DayZeRiInfo?) {
    if (info == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = info.weekday,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "农历：${if (info.isLeapMonth) "闰" else ""}${info.lunarMonthName}${info.lunarDayName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PillarChip("年柱", info.yearGanZhi)
                PillarChip("月柱", info.monthGanZhi)
                PillarChip("日柱", info.dayGanZhi)
                PillarChip("时柱", info.hourGanZhi)
            }
        }
    }
}

@Composable
private fun PillarChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ZeRiDayDetailWithYiJi(info: DayZeRiInfo, mountainAnalysis: MountainAnalysisResult) {
    val result = info.zeRiResult
    val isOverridden = mountainAnalysis.overriddenScore > 0
    val displayScore = if (isOverridden) mountainAnalysis.overriddenScore else result.overallScore
    val displayVerdict = if (isOverridden) mountainAnalysis.overriddenVerdict else result.overallVerdict
    val displayYi = if (isOverridden && mountainAnalysis.mountainYi.isNotEmpty()) mountainAnalysis.mountainYi else info.yi
    val displayJi = if (isOverridden && mountainAnalysis.mountainJi.isNotEmpty()) mountainAnalysis.mountainJi else info.ji

    val scoreColor = if (isOverridden) {
        Color(0xFFC62828)
    } else {
        when {
            result.overallScore >= 80 -> Color(0xFF2E7D32)
            result.overallScore >= 65 -> Color(0xFF558B2F)
            result.overallScore >= 50 -> Color(0xFFF9A825)
            result.overallScore >= 35 -> Color(0xFFE65100)
            else -> Color(0xFFC62828)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = result.dayGanZhi,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = scoreColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = displayVerdict,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = if (isOverridden) "${displayScore}分（山家大煞，禁用）" else "${displayScore}分",
                            style = MaterialTheme.typography.labelSmall,
                            color = scoreColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (isOverridden) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFC62828).copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mountainAnalysis.overriddenReason,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ZeRiInfoChip("十二建除", result.jianchu, if (result.jianchuJiXiong == "吉") Color(0xFF2E7D32) else if (result.jianchuJiXiong == "凶") Color(0xFFC62828) else Color(0xFFF9A825))
                ZeRiInfoChip("二十八宿", result.twentyEightMansion.takeLast(3), if (result.mansionJiXiong == "吉") Color(0xFF2E7D32) else Color(0xFFC62828))
                ZeRiInfoChip("五行", result.wuXingScore, if (result.wuXingScore.contains("和合")) Color(0xFF2E7D32) else if (result.wuXingScore.contains("不合")) Color(0xFFC62828) else Color(0xFF757575))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (result.tianDe) ShenShaChip("天德", false)
                if (result.yueDe) ShenShaChip("月德", false)
                if (result.sanSha) ShenShaChip("三煞", true)
                if (result.suiPo) ShenShaChip("岁破", true)
                if (result.yuePo) ShenShaChip("月破", true)
                if (result.jieSha) ShenShaChip("劫煞", true)
                if (result.zaiSha) ShenShaChip("灾煞", true)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "宜",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.06f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (displayYi.isEmpty()) {
                                Text(
                                    text = "—",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                displayYi.forEach { item ->
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(
                                            text = "+ ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "忌",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFC62828).copy(alpha = 0.06f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (displayJi.isEmpty()) {
                                Text(
                                    text = "—",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                displayJi.forEach { item ->
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(
                                            text = "- ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFC62828),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZeRiAnalysisDetail(info: DayZeRiInfo, mountainAnalysis: MountainAnalysisResult) {
    val result = info.zeRiResult

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "五行分析",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.wuXingDetail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "四柱五行旺衰",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = Color(0xFF1565C0).copy(alpha = 0.06f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = mountainAnalysis.fourPillarWuXing,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    color = Color(0xFF1565C0)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "扶山补龙分析",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(4.dp))
            val mtMatchColor = if (mountainAnalysis.mountainWuXingMatch.contains("克山") || mountainAnalysis.mountainWuXingMatch.contains("大凶"))
                Color(0xFFC62828) else if (mountainAnalysis.mountainWuXingMatch.contains("生扶") || mountainAnalysis.mountainWuXingMatch.contains("比和"))
                Color(0xFF2E7D32) else Color(0xFF6D4C41)
            Surface(
                color = mtMatchColor.copy(alpha = 0.06f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = mountainAnalysis.mountainWuXingMatch,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    color = mtMatchColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color(0xFFC62828).copy(alpha = 0.06f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "神煞优先级",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mountainAnalysis.shanShaPriorityText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "吉神宜趋",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (result.suggestions.isEmpty()) {
                Text(
                    text = "无特别吉神",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                result.suggestions.forEach { s ->
                    Text(
                        text = s,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "凶神宜忌",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (result.taboos.isEmpty()) {
                Text(
                    text = "无特别凶煞",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                result.taboos.forEach { t ->
                    Text(
                        text = t,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ShenShaConflictSection(result)
        }
    }
}

@Composable
private fun ZeRiMonthCalendarGrid(
    year: Int,
    month: Int,
    dayList: List<DayZeRiInfo>,
    currentDay: Int,
    onDayClick: (Int) -> Unit
) {
    val dayCount = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
        else -> 30
    }

    val firstDayWeekday = remember(year, month) {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                val headers = arrayOf("日", "一", "二", "三", "四", "五", "六")
                for (h in headers) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = h,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (h == "日" || h == "六") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            val totalCells = firstDayWeekday + dayCount
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayWeekday + 1

                        if (day in 1..dayCount) {
                            val info = dayList.getOrNull(day - 1)
                            val isCurrent = day == currentDay
                            ZeRiDayCell(
                                day = day,
                                result = info?.zeRiResult,
                                isCurrentDay = isCurrent,
                                modifier = Modifier.weight(1f),
                                onClick = { onDayClick(day - 1) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(0.85f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("大吉", Color(0xFF2E7D32))
                LegendItem("吉", Color(0xFF558B2F))
                LegendItem("平", Color(0xFFF9A825))
                LegendItem("小凶", Color(0xFFE65100))
                LegendItem("大凶", Color(0xFFC62828))
            }
        }
    }
}

@Composable
private fun ZeRiDayCell(
    day: Int,
    result: ZeRiResult?,
    isCurrentDay: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val scoreColor = if (result != null) {
        when {
            result.overallScore >= 80 -> Color(0xFF2E7D32)
            result.overallScore >= 65 -> Color(0xFF558B2F)
            result.overallScore >= 50 -> Color(0xFFF9A825)
            else -> Color(0xFFC62828)
        }
    } else Color.Unspecified

    val bgColor = when {
        isCurrentDay -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        result != null -> scoreColor.copy(alpha = 0.12f)
        else -> Color.Transparent
    }

    val borderColor = if (isCurrentDay) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.background(borderColor, CircleShape).padding(2.dp)
                else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$day",
                fontSize = 13.sp,
                fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Medium,
                color = if (result != null) scoreColor else MaterialTheme.colorScheme.onSurface
            )
            if (result != null) {
                Text(
                    text = result.jianchu,
                    fontSize = 9.sp,
                    color = scoreColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ZeRiInfoChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Surface(
            color = color.copy(alpha = 0.12f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ShenShaChip(name: String, isXiong: Boolean) {
    Surface(
        color = if (isXiong) Color(0xFFC62828).copy(alpha = 0.1f) else Color(0xFF2E7D32).copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (isXiong) Color(0xFFC62828) else Color(0xFF2E7D32),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ZeRiPrincipleCard(schoolTab: Int) {
    var expanded by remember { mutableStateOf(false) }

    val isZhengTi = schoolTab == 0
    val isDouShou = schoolTab == 1
    val isHeLuo = schoolTab == 2

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isZhengTi) "正体五行择日原理" else if (isDouShou) "斗首择日原理" else "河洛纳卦择日原理",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isZhengTi) zhengTiPrinciple else if (isDouShou) douShouPrinciple else heLuoPrinciple,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

private fun prevDay(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
    if (day > 1) return Triple(year, month, day - 1)
    return if (month > 1) {
        val prevMonthDays = when (month - 1) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
            else -> 30
        }
        Triple(year, month - 1, prevMonthDays)
    } else {
        Triple(year - 1, 12, 31)
    }
}

private fun nextDay(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
    val maxDay = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
        else -> 30
    }
    if (day < maxDay) return Triple(year, month, day + 1)
    return if (month < 12) {
        Triple(year, month + 1, 1)
    } else {
        Triple(year + 1, 1, 1)
    }
}

private fun evaluateAllDays(
    year: Int,
    month: Int,
    repository: com.example.wannianli.data.repository.CalendarRepository
): List<DayZeRiInfo> {
    val allTermsThisYear = repository.getSolarTermsForYear(year)
    val allTermsLastYear = repository.getSolarTermsForYear(year - 1)
    val combinedTerms = allTermsLastYear + allTermsThisYear

    val dayCount = repository.getMonthDayCount(year, month)
    val results = mutableListOf<DayZeRiInfo>()

    for (d in 1..dayCount) {
        val weekdayIndex = getWeekday(year, month, d)
        val weekdayName = "周${CalendarConstants.WEEKDAY_NAMES[weekdayIndex]}"

        val lunarResult = LunarCalendarEngine.solarToLunar(year, month, d)
        val lunarMonthName = CalendarConstants.LUNAR_MONTH_NAMES.getOrElse(lunarResult.month - 1) { "${lunarResult.month}月" }
        val lunarDayName = CalendarConstants.LUNAR_DAY_NAMES.getOrElse(lunarResult.day - 1) { "${lunarResult.day}" }

        val dayPillarIndices = FourPillarsEngine.calcDayPillar(year, month, d)
        val dayGanZhi = "${CalendarConstants.TIAN_GAN[dayPillarIndices.dayGanIndex]}${CalendarConstants.DI_ZHI[dayPillarIndices.dayZhiIndex]}"
        val yearGanZhi = calcYearPillarForZeRi(year, month, d, allTermsThisYear)
        val monthGanZhi = calcMonthPillarForZeRi(year, month, d, combinedTerms)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val hourZhi = when (hour) {
            23, 0 -> 0; 1, 2 -> 1; 3, 4 -> 2; 5, 6 -> 3; 7, 8 -> 4
            9, 10 -> 5; 11, 12 -> 6; 13, 14 -> 7; 15, 16 -> 8
            17, 18 -> 9; 19, 20 -> 10; 21, 22 -> 11; else -> 0
        }
        val startIdx = CalendarConstants.ganIndexToStartIndex(dayPillarIndices.dayGanIndex)
        val hourStartGan = CalendarConstants.FIVE_RAT_HOUR_GAN_START[startIdx]
        val hourGan = (hourStartGan + hourZhi) % 10
        val hourGanZhi = "${CalendarConstants.TIAN_GAN[hourGan]}${CalendarConstants.DI_ZHI[hourZhi]}"

        val zeRiResult = ZeRiEngine.evaluateDay(year, month, d, yearGanZhi, monthGanZhi, dayGanZhi)
        val yi = getYiList(zeRiResult)
        val ji = getJiList(zeRiResult)

        results.add(
            DayZeRiInfo(
                year = year, month = month, day = d,
                weekday = weekdayName,
                lunarMonthName = lunarMonthName,
                lunarDayName = lunarDayName,
                lunarMonth = lunarResult.month,
                lunarDay = lunarResult.day,
                isLeapMonth = lunarResult.isLeapMonth,
                yearGanZhi = yearGanZhi,
                monthGanZhi = monthGanZhi,
                dayGanZhi = dayGanZhi,
                hourGanZhi = hourGanZhi,
                zeRiResult = zeRiResult,
                yi = yi,
                ji = ji
            )
        )
    }
    return results
}

private fun getWeekday(year: Int, month: Int, day: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, day)
    return cal.get(Calendar.DAY_OF_WEEK) - 1
}

private fun getYiList(result: ZeRiResult): List<String> {
    val yi = mutableListOf<String>()

    when (result.jianchu) {
        "建" -> { yi.add("出行"); yi.add("赴任") }
        "除" -> { yi.add("除旧布新"); yi.add("治病求医"); yi.add("扫舍") }
        "满" -> { yi.add("祭祀"); yi.add("祈福"); yi.add("求嗣") }
        "平" -> { yi.add("修造"); yi.add("入宅"); yi.add("嫁娶") }
        "定" -> { yi.add("订婚"); yi.add("开业"); yi.add("交易") }
        "执" -> { yi.add("捕猎"); yi.add("讨债") }
        "破" -> { /* 诸事不宜 */ }
        "危" -> { yi.add("祭祀"); yi.add("安床"); yi.add("祈福") }
        "成" -> { yi.add("嫁娶"); yi.add("开市"); yi.add("入宅"); yi.add("出行") }
        "收" -> { yi.add("纳财"); yi.add("入学"); yi.add("进人口") }
        "开" -> { yi.add("开市"); yi.add("出行"); yi.add("嫁娶"); yi.add("动土") }
        "闭" -> { yi.add("祭祀"); yi.add("纳财"); yi.add("埋葬") }
    }

    if (result.tianDe) yi.add("天德日，百事大吉")
    if (result.yueDe) yi.add("月德日，百福并集")
    if (result.jianchuJiXiong == "吉" && result.overallScore >= 65) {
        if (!yi.contains("嫁娶")) yi.add("嫁娶")
        if (!yi.contains("出行")) yi.add("出行")
    }

    return yi.distinct()
}

private fun getJiList(result: ZeRiResult): List<String> {
    val ji = mutableListOf<String>()

    when (result.jianchu) {
        "建" -> { ji.add("破土"); ji.add("安葬"); ji.add("开仓") }
        "除" -> { ji.add("求官"); ji.add("上任") }
        "满" -> { ji.add("动土"); ji.add("迁徙"); ji.add("栽种") }
        "平" -> { /* 无大忌 */ }
        "定" -> { ji.add("诉讼"); ji.add("出行远归") }
        "执" -> { ji.add("开市"); ji.add("交易") }
        "破" -> { ji.add("百事不宜") }
        "危" -> { ji.add("出行远归"); ji.add("动土") }
        "成" -> { /* 百事大吉 */ }
        "收" -> { ji.add("安葬"); ji.add("疗病"); ji.add("出行") }
        "开" -> { /* 百事大吉 */ }
        "闭" -> { ji.add("开业"); ji.add("出行"); ji.add("动土") }
    }

    if (result.sanSha) ji.add("犯三煞，诸事不宜")
    if (result.suiPo) ji.add("岁破日，不宜用事")
    if (result.yuePo) ji.add("月破日，不宜用事")
    if (result.jieSha) ji.add("忌远行大事")
    if (result.zaiSha) ji.add("忌动土兴造")

    return ji.distinct()
}

private fun calcYearPillarForZeRi(
    year: Int, month: Int, day: Int,
    terms: List<SolarTermCalculator.SolarTermResult>
): String {
    val liChun = terms.find { it.name == "立春" && it.year == year }
    if (liChun != null) {
        val isBeforeLiChun = month < liChun.month || (month == liChun.month && day < liChun.day)
        val effectiveYear = if (isBeforeLiChun) year - 1 else year
        val ganIndex = ((effectiveYear - 4) % 10 + 10) % 10
        val zhiIndex = ((effectiveYear - 4) % 12 + 12) % 12
        return "${CalendarConstants.TIAN_GAN[ganIndex]}${CalendarConstants.DI_ZHI[zhiIndex]}"
    }
    val ganIndex = ((year - 4) % 10 + 10) % 10
    val zhiIndex = ((year - 4) % 12 + 12) % 12
    return "${CalendarConstants.TIAN_GAN[ganIndex]}${CalendarConstants.DI_ZHI[zhiIndex]}"
}

private fun calcMonthPillarForZeRi(
    year: Int, month: Int, day: Int,
    terms: List<SolarTermCalculator.SolarTermResult>
): String {
    val jieTerms = terms.filter { CalendarConstants.JIE_NAMES.contains(it.name) }
        .sortedWith(compareBy({ it.year }, { it.month }, { it.day }))

    var monthIndex = 0
    for (jie in jieTerms) {
        if (jie.year > year) continue
        if (month > jie.month || (month == jie.month && day >= jie.day)) {
            monthIndex = CalendarConstants.JIE_NAMES.indexOf(jie.name)
        }
    }

    val yearGanZhi = calcYearPillarForZeRi(year, month, day, terms)
    val yearGanIndex = CalendarConstants.TIAN_GAN.indexOf(yearGanZhi[0].toString())
    val startIndex = CalendarConstants.ganIndexToStartIndex(yearGanIndex)
    val januaryStartGan = CalendarConstants.FIVE_TIGER_MONTH_GAN_START[startIndex]
    val monthGanIndex = (januaryStartGan + monthIndex) % 10
    val monthZhiIndex = CalendarConstants.MONTH_ZHI_INDEX[monthIndex]

    return "${CalendarConstants.TIAN_GAN[monthGanIndex]}${CalendarConstants.DI_ZHI[monthZhiIndex]}"
}

@Composable
private fun DouShouDayDetail(info: DayZeRiInfo, sittingMountainIndex: Int = -1, sharedBenMingResults: List<BenMingResult> = emptyList()) {
    val douShouResult = remember(info.yearGanZhi, info.monthGanZhi, info.dayGanZhi, info.hourGanZhi, sittingMountainIndex) {
        DouShouEngine.evaluate(info.yearGanZhi, info.monthGanZhi, info.dayGanZhi, info.hourGanZhi, sittingMountainIndex)
    }

    val starColor = DouShouEngine.STAR_COLORS[douShouResult.dayStar]
    val bgColor = Color(starColor).copy(alpha = 0.08f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "年柱斗首五行",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = douShouResult.yearDouShouWuxing,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${douShouResult.dayStarName}日",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(starColor)
                        )
                        Text(
                            text = douShouResult.dayStarJiXiong,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(starColor).copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "四柱斗首五星",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            douShouResult.pillars.forEach { pillar ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pillar.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = pillar.ganZhi,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = pillar.douShouWuxing,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val pStarColor = DouShouEngine.STAR_COLORS[pillar.star]
                        Surface(
                            color = Color(pStarColor).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = DouShouEngine.STAR_NAMES[pillar.star],
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(pStarColor)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "催福力度分析",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DouShouStrengthCard(
                    label = "催财",
                    strength = douShouResult.caiStrength,
                    modifier = Modifier.weight(1f)
                )
                DouShouStrengthCard(
                    label = "催丁",
                    strength = douShouResult.dingStrength,
                    modifier = Modifier.weight(1f)
                )
                DouShouStrengthCard(
                    label = "催贵",
                    strength = douShouResult.guiStrength,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "斗首分析",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (douShouResult.authenticityCheck.isNotEmpty()) {
                Surface(
                    color = Color(0xFF1A237E).copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = douShouResult.authenticityCheck,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1A237E),
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = douShouResult.principle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = douShouResult.analysis,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = douShouResult.fuShanAnalysis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = DouShouEngine.STAR_DESCRIPTIONS[douShouResult.dayStar],
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(starColor),
                    lineHeight = 18.sp
                )
            }

            if (douShouResult.globalPattern.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "斗首全局格局",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFF1B5E20).copy(alpha = 0.06f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = douShouResult.globalPattern,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            if (douShouResult.mountainMatch.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "斗首与坐山匹配分析",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFF6A1B9A).copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = douShouResult.mountainMatch,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        color = Color(0xFF6A1B9A)
                    )
                }
            }

            if (douShouResult.taboos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "禁忌事项",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                douShouResult.taboos.forEach { taboo ->
                    Surface(
                        color = Color(0xFFC62828).copy(alpha = 0.06f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "!",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = taboo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (douShouResult.benMingReference.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF4A148C).copy(alpha = 0.04f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = douShouResult.benMingReference,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4A148C),
                        lineHeight = 16.sp
                    )
                }
            }
            if (sharedBenMingResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                sharedBenMingResults.forEach { bm ->
                    BenMingBottomReference(bm)
                }
            }
        }
    }
}

@Composable
private fun DouShouStrengthCard(
    label: String,
    strength: String,
    modifier: Modifier
) {
    val isStrong = strength.contains("旺")
    val isMedium = strength.contains("中")
    val isBad = strength.contains("损") || strength.contains("破军")
    val color = when {
        isStrong -> Color(0xFF2E7D32)
        isMedium -> Color(0xFF558B2F)
        isBad -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val icon = when {
                isStrong -> "✦✦✦"
                isMedium -> "✦✦"
                isBad -> "✕"
                else -> "✦"
            }
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = strength,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

private fun yearToGanZhi(year: Int): String {
    val ganIndex = ((year - 4) % 10 + 10) % 10
    val zhiIndex = ((year - 4) % 12 + 12) % 12
    return "${CalendarConstants.TIAN_GAN[ganIndex]}${CalendarConstants.DI_ZHI[zhiIndex]}"
}

@Composable
private fun BenMingSelectorCard(
    benMingUsageType: String,
    onUsageTypeChange: (String) -> Unit,
    maleYear: Int, onMaleYearChange: (Int) -> Unit,
    femaleYear: Int, onFemaleYearChange: (Int) -> Unit,
    ownerYear: Int, onOwnerYearChange: (Int) -> Unit,
    personYear: Int, onPersonYearChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "本命相主",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A148C)
                )
                Row {
                    listOf("嫁娶" to 0xFFC62828L, "入宅" to 0xFF00695CL, "修造" to 0xFF4A148CL, "通用" to 0xFF2E7D32L)
                        .forEach { (label, clrLong) ->
                            val clr = Color(clrLong)
                            val isSelected = benMingUsageType == label
                            Surface(
                                color = if (isSelected) clr.copy(alpha = 0.15f) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clickable { onUsageTypeChange(label) }
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) clr else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (benMingUsageType) {
                "嫁娶" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BenMingYearPicker(
                            label = "男主年命",
                            year = maleYear,
                            onYearChange = onMaleYearChange,
                            modifier = Modifier.weight(1f)
                        )
                        BenMingYearPicker(
                            label = "女主年命",
                            year = femaleYear,
                            onYearChange = onFemaleYearChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                "入宅" -> {
                    BenMingYearPicker(
                        label = "宅主年命",
                        year = ownerYear,
                        onYearChange = onOwnerYearChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    BenMingYearPicker(
                        label = "主事人年命",
                        year = personYear,
                        onYearChange = onPersonYearChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun BenMingYearPicker(
    label: String, year: Int, onYearChange: (Int) -> Unit, modifier: Modifier
) {
    val ganzhi = yearToGanZhi(year)
    Surface(
        color = Color(0xFF4A148C).copy(alpha = 0.04f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4A148C)
                )
                Text(
                    text = "${year}年（${ganzhi}）",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onYearChange(maxOf(year - 1, 1924)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("−", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = { onYearChange(minOf(year + 1, 2043)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BenMingResultCard(result: BenMingResult) {
    val verdictColor = Color(result.verdictColor)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${result.benMingGanZhi}年 ${result.usageLabel}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = verdictColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = result.verdictLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = verdictColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = result.analysis,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )

            if (result.dayHourSupport || result.dayHourDrain || result.clashWithDay || result.clashWithHour) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (result.dayHourSupport) {
                        BenMingChip("日时生扶", Color(0xFF2E7D32))
                    }
                    if (result.dayHourDrain) {
                        BenMingChip("日时克泄", Color(0xFFC62828))
                    }
                    if (result.clashWithDay) {
                        BenMingChip("冲克日柱", Color(0xFFC62828))
                    }
                    if (result.clashWithHour) {
                        BenMingChip("冲克时柱", Color(0xFFC62828))
                    }
                    if (result.punishmentWithDay) {
                        BenMingChip("日柱相刑", Color(0xFFC62828))
                    }
                    if (result.harmWithDay) {
                        BenMingChip("日柱六害", Color(0xFFE65100))
                    }
                    if (result.benMingSanSha) {
                        BenMingChip("本命三煞", Color(0xFFC62828))
                    }
                    if (result.benMingSuiPo) {
                        BenMingChip("本命岁破", Color(0xFFC62828))
                    }
                }
            }

            if (result.suitableYears.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.suitableYears,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun BenMingChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BenMingBottomReference(benMingResult: BenMingResult?) {
    if (benMingResult == null) return
    val color = Color(benMingResult.verdictColor)
    Surface(
        color = Color(0xFF4A148C).copy(alpha = 0.04f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
            Text(
                text = "本命：",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A148C)
            )
            Column {
                Text(
                    text = "${benMingResult.benMingGanZhi}年${benMingResult.usageLabel} — ${benMingResult.verdict}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = benMingResult.analysis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
private fun TongTianQiaoSection(
    info: DayZeRiInfo,
    sittingMountainIndex: Int,
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    sharedBenMingResults: List<BenMingResult> = emptyList()
) {
    val result = info.zeRiResult
    val dayGanZhi = info.dayGanZhi

    val dayZhi = CalendarConstants.DI_ZHI.indexOf(dayGanZhi[1].toString())

    val shaResult: ShanShaResult = remember(
        info.yearGanZhi, info.monthGanZhi, info.dayGanZhi, sittingMountainIndex
    ) {
        val yg = CalendarConstants.TIAN_GAN.indexOf(info.yearGanZhi[0].toString())
        val yz = CalendarConstants.DI_ZHI.indexOf(info.yearGanZhi[1].toString())
        val mz = CalendarConstants.DI_ZHI.indexOf(info.monthGanZhi[1].toString())
        val dz = CalendarConstants.DI_ZHI.indexOf(info.dayGanZhi[1].toString())
        ShanShaEngine.evaluate(currentYear, currentMonth, currentDay, yg, yz, mz, dz, sittingMountainIndex)
    }

    val fatalShaNames = shaResult.criticalViolations
        .filter { it.name in setOf("三煞", "岁破", "阴府", "戊己都天", "巡山罗睺") }
        .map { it.name }
    val hasMountainFatalSha = fatalShaNames.isNotEmpty()
    val mtName = ShanShaEngine.MOUNTAIN_NAMES[sittingMountainIndex]

    val dailyTaboos = mutableListOf<String>()
    if (result.jianchu == "破") dailyTaboos.add("破日")
    val yangGongDays = mapOf(1 to 13, 2 to 11, 3 to 9, 4 to 7, 5 to 5, 6 to 3, 7 to 1, 8 to 27, 9 to 25, 10 to 23, 11 to 21, 12 to 19)
    val lm = info.lunarMonth; val ld = info.lunarDay
    if (yangGongDays[lm] == ld || (lm == 7 && ld == 29)) dailyTaboos.add("杨公忌日")

    val primaryBenMing = sharedBenMingResults.firstOrNull()

    val tqResult: TongTianQiaoResult = remember(
        dayGanZhi, sittingMountainIndex, hasMountainFatalSha, fatalShaNames, dailyTaboos, sharedBenMingResults.hashCode()
    ) {
        TongTianQiaoEngine.evaluate(
            dayZhi = dayZhi,
            dayGanZhi = dayGanZhi,
            sittingMountainIndex = sittingMountainIndex,
            hasMountainFatalSha = hasMountainFatalSha,
            fatalShaNames = fatalShaNames,
            benMingResult = primaryBenMing,
            caiStrength = "",
            dingStrength = "",
            guiStrength = "",
            dailyTaboos = dailyTaboos
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "通天窍择时",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${info.yearGanZhi}年 ${info.monthGanZhi}月 ${dayGanZhi}日",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val bestLabel = when {
                        hasMountainFatalSha -> "山家有煞"
                        tqResult.recommendedHour != null -> "最优时辰"
                        else -> "一般"
                    }
                    val bestLabelColor = when {
                        hasMountainFatalSha -> Color(0xFFC62828)
                        tqResult.recommendedHour != null -> Color(0xFF2E7D32)
                        else -> Color(0xFFF9A825)
                    }
                    Surface(
                        color = bestLabelColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = bestLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = bestLabelColor
                        )
                    }
                }

                if (tqResult.recommendedHour != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "首选",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "【${tqResult.recommendedHour!!.hourZhi}时·${tqResult.recommendedHour!!.shenSha}】",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tqResult.recommendedHour!!.coreUsage,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                if (tqResult.secondaryHour != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFF1565C0).copy(alpha = 0.06f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "次选",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "【${tqResult.secondaryHour!!.hourZhi}时·${tqResult.secondaryHour!!.shenSha}】",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tqResult.secondaryHour!!.coreUsage,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1565C0).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                if (tqResult.caiWealthHour != null || tqResult.marriageHour != null || tqResult.repairHour != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(14.dp)
                                .background(Color(0xFF6A1B9A), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "按用途推荐时辰",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                    }
                }
                if (tqResult.caiWealthHour != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFFF6F00).copy(alpha = 0.06f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "\uD83D\uDCB0",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "求财",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6F00)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "【${tqResult.caiWealthHour!!.hourZhi}时\u00B7${tqResult.caiWealthHour!!.shenSha}】",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6F00)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "匹配斗首武曲催财",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF6F00).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                if (tqResult.marriageHour != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFC62828).copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "\uD83D\uDC92",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "嫁娶安家",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "【${tqResult.marriageHour!!.hourZhi}时\u00B7${tqResult.marriageHour!!.shenSha}】",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "匹配河洛催丁催贵",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC62828).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                if (tqResult.repairHour != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFF4A148C).copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "\uD83D\uDD28",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "修造安葬",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A148C)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "【${tqResult.repairHour!!.hourZhi}时\u00B7${tqResult.repairHour!!.shenSha}】",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A148C)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "需结合乌兔太阳太阴",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4A148C).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

            if (hasMountainFatalSha) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFFC62828).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "!",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "坐山联动：${mtName}山犯${fatalShaNames.joinToString("、")}大煞",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    text = "山家犯大煞时，所有吉时一律禁用。动土安葬绝对不可用此日课。日常小事可酌情择吉时慎用。",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFC62828).copy(alpha = 0.85f),
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                if (!hasMountainFatalSha) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.05f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "坐山联动：${mtName}山不犯山家大煞，吉时可放心择用。",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "十二时辰神煞吉凶",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                tqResult.hours.forEach { hour ->
                    val hourBgColor = when {
                        hour.isBestHour -> Color(0xFF2E7D32).copy(alpha = 0.06f)
                        hour.isJi -> Color(0xFF2E7D32).copy(alpha = 0.03f)
                        else -> Color(0xFFC62828).copy(alpha = 0.03f)
                    }
                    val accentColor = when {
                        hour.isBestHour -> Color(0xFF2E7D32)
                        hour.isJi -> Color(0xFF558B2F)
                        else -> Color(0xFFC62828)
                    }

                    Surface(
                        color = hourBgColor,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = accentColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = hour.hourZhi + "时",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = hour.shenSha,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (hour.isBlockedByBenMing) {
                                        Text(
                                            text = "冲命",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFC62828),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Surface(
                                        color = if (hour.isJi) Color(0xFF2E7D32).copy(alpha = 0.1f) else Color(0xFFC62828).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (hour.isJi) "吉" else "凶",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (hour.isJi) Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = hour.coreUsage,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "综合总结",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFF6A1B9A).copy(alpha = 0.04f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = tqResult.summary,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }

                if (tqResult.dailyTabooText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFC62828).copy(alpha = 0.06f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "\u26A0",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "本日为${tqResult.dailyTabooNames.joinToString("、")}，虽有黄道吉时，但嫁娶、入宅、开业仍需谨慎，动土安葬绝对禁用。",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC62828),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "六大黄道吉时",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("金匮" to Color(0xFFFF6F00), "天德" to Color(0xFF0D47A1),
                        "青龙" to Color(0xFF1B5E20), "明堂" to Color(0xFF4A148C),
                        "玉堂" to Color(0xFF00695C), "司命" to Color(0xFFBF360C)).forEach { (name, clr) ->
                        Surface(
                            color = clr.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = name,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = clr,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = Color(0xFFF9A825).copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "神煞优先级：山家煞 ＞ 本命冲克 ＞ 时辰吉凶。通天窍择时以十二神煞定吉凶；需避开山家大煞、本命冲克；适合日常小事、嫁娶、开业，动土安葬需结合乌兔太阳太阴。",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF9A825),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "通天窍择日原理",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "通天窍择时以黄道十二神煞定吉凶，按日支起青龙顺排十二时辰。核心口诀：青龙明堂金匮天德玉堂司命为六大黄道吉时。配合坐山、本命相主联动，是民间实战派日常小事择日首选。",
                modifier = Modifier.padding(10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1A237E),
                lineHeight = 18.sp
            )
        }
    }

    if (sharedBenMingResults.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        sharedBenMingResults.forEach { bm ->
            BenMingBottomReference(bm)
        }
    }
}

private val zhengTiPrinciple = "正体五行择日以天地干支的五行生克为核心，综合运用以下法则评判日课吉凶：\n\n" +
        "1. 十二建除：以月建地支为基准，逐日排定建除满平定执破危成收开闭十二神，成、开、除、定、执、危为吉。\n\n" +
        "2. 三煞：年支三合局所冲之方为三煞，大凶，百事不宜。寅午戌年煞在北（亥子丑），申子辰年煞在南（巳午未），巳酉丑年煞在东（寅卯辰），亥卯未年煞在西（申酉戌）。\n\n" +
        "3. 岁破/月破：与年支（或月支）相冲之日，大事不宜。\n\n" +
        "4. 天德/月德：月令所值的天德和月德星，乃上吉之神。\n\n" +
        "5. 二十八宿：每日值日星宿，分吉宿和凶宿。\n\n" +
        "6. 五行生克：日干五行得年月相生为吉（扶山），被年月所克为不吉。"

private val douShouPrinciple = "斗首择日以天干化气五行为核心，不同於正体五行。其原理如下：\n\n" +
        "1. 天干化气：甲己化土、乙庚化金、丙辛化水、丁壬化木、戊癸化火。此斗首五行之根基。\n\n" +
        "2. 定元辰：以年柱天干化气五行为元辰（主星），作为评判日课吉凶的基准。\n\n" +
        "3. 辨五星：以月、日、时柱天干化气五行与元辰比较，分出五星星性——\n" +
        "   · 同我者为「元辰」（主星，最吉）\n" +
        "   · 我生者为「廉贞」（子孙星，利人丁）\n" +
        "   · 生我者为「贪狼」（官贵星，利功名）\n" +
        "   · 我克者为「武曲」（妻财星，利财富）\n" +
        "   · 克我者为「破军」（鬼贼星，大凶）\n\n" +
        "4. 扶山补龙：斗首择日须配合山家坐向和命主年庚，五星得令生扶则吉，被克泄则凶。\n\n" +
        "5. 五星旺衰：旺相之五星催福力强，休囚之五星催福力弱，破军当令则百事不宜。"

private val heLuoPrinciple = "河洛纳卦择日，以河图洛书之数理、伏羲先天八卦与文王后天八卦之象义为根基，择日层次最高。其原理如下：\n\n" +
        "1. 纳甲配卦：十天干依京房纳甲法配以八卦（乾纳甲壬、坤纳乙癸、震纳庚、巽纳辛、坎纳戊、离纳己、艮纳丙、兑纳丁），十二地支依纳支法配以八卦。\n\n" +
        "2. 合重卦：每柱干支取干纳卦为上卦、支纳卦为下卦，合成六十四重卦之一。\n\n" +
        "3. 河洛数理：每卦配以洛书数（坎1坤2震3巽4乾6兑7艮8离9），考究数理和合。\n\n" +
        "4. 卦气通气：四卦同宫或五行相生，则卦气贯通，天人合一。同宫越多，力量越纯。\n\n" +
        "5. 阴阳相配：上卦下卦阴阳二气均衡则刚柔并济，大吉之象。\n\n" +
        "6. 六十四卦吉凶：每卦各有吉凶休咎，得吉卦者百事亨通，得凶卦者慎用。"

@Composable
private fun HeLuoDayDetail(info: DayZeRiInfo, sharedBenMingResults: List<BenMingResult> = emptyList()) {
    val heLuoResult = remember(info.yearGanZhi, info.monthGanZhi, info.dayGanZhi, info.hourGanZhi) {
        HeLuoEngine.evaluate(info.yearGanZhi, info.monthGanZhi, info.dayGanZhi, info.hourGanZhi)
    }

    val scoreColor = when {
        heLuoResult.overallScore >= 85 -> Color(0xFF1B5E20)
        heLuoResult.overallScore >= 70 -> Color(0xFF2E7D32)
        heLuoResult.overallScore >= 55 -> Color(0xFF558B2F)
        heLuoResult.overallScore >= 40 -> Color(0xFFF9A825)
        heLuoResult.overallScore >= 25 -> Color(0xFFE65100)
        else -> Color(0xFFC62828)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "洛书数理",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val luoShuNums = heLuoResult.pillars.map { it.luoShuNumber }.joinToString(" · ")
                    Text(
                        text = luoShuNums,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    color = scoreColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = heLuoResult.overallVerdict,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = "${heLuoResult.overallScore}分",
                            style = MaterialTheme.typography.labelSmall,
                            color = scoreColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "纳甲配卦",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            heLuoResult.pillars.forEach { pillar ->
                val guaColor = when {
                    pillar.guaVerdict.contains("吉") && !pillar.guaVerdict.contains("小凶") -> Color(0xFF2E7D32)
                    pillar.guaVerdict.contains("小凶") -> Color(0xFFE65100)
                    else -> Color(0xFF757575)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = guaColor.copy(alpha = 0.06f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = pillar.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pillar.ganZhi,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "上卦${pillar.upperGua} · 下卦${pillar.lowerGua}  →  ${pillar.guaName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = guaColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = pillar.guaVerdict,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = guaColor
                            )
                            Text(
                                text = "洛数 ${pillar.luoShuNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "卦气五行分析",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeLuoAnalysisCard(
                    label = "卦气通气",
                    analysis = heLuoResult.sameGongAnalysis,
                    modifier = Modifier.weight(1f)
                )
                HeLuoAnalysisCard(
                    label = "五行相生",
                    analysis = heLuoResult.generateAnalysis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeLuoAnalysisCard(
                    label = "阴阳相配",
                    analysis = heLuoResult.yinYangAnalysis,
                    modifier = Modifier.weight(1f)
                )
                HeLuoAnalysisCard(
                    label = "洛书数理",
                    analysis = heLuoResult.luoShuAnalysis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "催福维度评估",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeLuoStrengthCard(
                    label = "催丁",
                    strength = heLuoResult.dingStrength,
                    icon = "人",
                    modifier = Modifier.weight(1f)
                )
                HeLuoStrengthCard(
                    label = "催贵",
                    strength = heLuoResult.guiStrength,
                    icon = "官",
                    modifier = Modifier.weight(1f)
                )
                HeLuoStrengthCard(
                    label = "稳大局",
                    strength = heLuoResult.stability,
                    icon = "稳",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "日课综合分析",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = heLuoResult.guaAnalysis,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = heLuoResult.energyAnalysis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = Color(0xFF1A237E).copy(alpha = 0.06f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "河洛纳卦为择日最高层次，重卦气贯通与阴阳平衡。此日课若卦气相合、洛数和合，则天人相应，吉应深远，适合建房、祖坟、重大喜事等长久布局。",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1A237E),
                    lineHeight = 18.sp
                )
            }
            if (sharedBenMingResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                sharedBenMingResults.forEach { bm ->
                    BenMingBottomReference(bm)
                }
            }
        }
    }
}

@Composable
private fun HeLuoAnalysisCard(
    label: String,
    analysis: String,
    modifier: Modifier
) {
    val isPositive = analysis.contains("极强") || analysis.contains("大吉") || analysis.contains("和合") ||
            analysis.contains("相生") || analysis.contains("平衡") || analysis.contains("多卦")

    val color = if (isPositive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = color.copy(alpha = 0.06f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = analysis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun HeLuoStrengthCard(
    label: String,
    strength: String,
    icon: String,
    modifier: Modifier
) {
    val isStrong = strength.contains("旺") || strength.contains("极稳")
    val isMedium = strength.contains("中") || strength.contains("稳") && !strength.contains("极稳")
    val color = when {
        isStrong -> Color(0xFF2E7D32)
        isMedium -> Color(0xFF558B2F)
        else -> Color(0xFFF9A825)
    }

    val starCount = when {
        isStrong -> "✦✦✦"
        isMedium -> "✦✦"
        else -> "✦"
    }

    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = starCount,
                style = MaterialTheme.typography.titleLarge,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = strength,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}