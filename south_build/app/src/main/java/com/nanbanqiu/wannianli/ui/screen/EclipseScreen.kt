/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.ui.screen

import androidx.compose.foundation.background

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState

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

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.nanbanqiu.wannianli.engine.EclipseEvent

import com.nanbanqiu.wannianli.engine.SolarEclipseEngine

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch

import kotlinx.coroutines.withContext

import java.util.*

@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun EclipseScreen(onBack: () -> Unit) {

    val todayYear = Calendar.getInstance().get(Calendar.YEAR)

    val decadeStart = (todayYear / 10) * 10

    var viewStartYear by remember { mutableIntStateOf(decadeStart) }

    var isLoading by remember { mutableStateOf(true) }

    var decadeEvents by remember { mutableStateOf<List<EclipseEvent>>(emptyList()) }

    var expandedIdx by remember { mutableIntStateOf(-1) }

    var selectedTab by remember { mutableIntStateOf(0) }

    fun load() {

        isLoading = true

        kotlinx.coroutines.GlobalScope.launch(Dispatchers.Default) {

            val events = withContext(Dispatchers.Default) { SolarEclipseEngine.generateDecadeEvents(viewStartYear) }

            withContext(Dispatchers.Main) { decadeEvents = events; isLoading = false }

        }

    }

    LaunchedEffect(viewStartYear) { load() }

    val stats = remember(decadeEvents) {

        val solar = decadeEvents.count { it.type == "ʳ" }

        val lunar = decadeEvents.count { it.type == "ʳ" }

        val totalP = decadeEvents.count { it.eclipseType == "ȫʳ" }

        val regions = decadeEvents.map { it.fenYeAncient }.distinct()

        Triple(solar, lunar, regions)

    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = { Text("ʳռ", fontWeight = FontWeight.Bold) },

                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "") } },

                colors = TopAppBarDefaults.topAppBarColors(

                    containerColor = Color(0xFF1A1A2E),

                    titleContentColor = Color.White,

                    navigationIconContentColor = Color.White

                )

            )

        }

    ) { padding ->

        if (isLoading) {

            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {

                CircularProgressIndicator()

            }

            return@Scaffold

        }

        Column(

            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())

        ) {

            Card(

                Modifier.fillMaxWidth().padding(12.dp),

                shape = RoundedCornerShape(12.dp),

                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))

            ) {

                Column(Modifier.padding(16.dp)) {

                    Text("${viewStartYear} C ${viewStartYear + 9}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text("ʳ", color = Color(0xFFFFD700), fontSize = 14.sp)

                            Text("${stats.first}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)

                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text("ʳ", color = Color(0xFF87CEEB), fontSize = 14.sp)

                            Text("${stats.second}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)

                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text("ӰҰ", color = Color(0xFFE57373), fontSize = 14.sp)

                            Text("${stats.third.size}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)

                        }

                    }

                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {

                        TextButton(onClick = { viewStartYear = (viewStartYear / 10) * 10 - 10 }, enabled = viewStartYear > 1900) {

                            Text("? ʮ", color = Color(0xFFFFD700))

                        }

                        TextButton(onClick = { viewStartYear = (Calendar.getInstance().get(Calendar.YEAR) / 10) * 10 }) {

                            Text("?? ǰʮ", color = Color(0xFFFFD700))

                        }

                        TextButton(onClick = { viewStartYear = (viewStartYear / 10) * 10 + 10 }, enabled = viewStartYear < 2090) {

                            Text("ʮ ?", color = Color(0xFFFFD700))

                        }

                    }

                }

            }

            if (decadeEvents.isEmpty()) {

                Card(Modifier.fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))) {

                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                        Text("??", fontSize = 48.sp)

                        Spacer(Modifier.height(8.dp))

                        Text("ʮʳ¼", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)

                    }

                }

            }

            decadeEvents.forEachIndexed { idx, event ->

                val isExpanded = expandedIdx == idx

                val bgColor = when {

                    event.eclipseType == "ȫʳ" -> Color(0xFF8B0000)

                    event.eclipseType == "ջʳ" -> Color(0xFFB22222)

                    event.type == "ʳ" -> Color(0xFFCD853F)

                    event.eclipseType == "ȫʳ" -> Color(0xFF483D8B)

                    else -> Color(0xFF4682B4)

                }

                val icon = when {

                    event.eclipseType == "ȫʳ" -> "??"

                    event.eclipseType == "ջʳ" -> "??"

                    event.type == "ʳ" -> "??"

                    event.eclipseType == "ȫʳ" -> "??"

                    else -> "??"

                }

                Card(

                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clickable { expandedIdx = if (isExpanded) -1 else idx },

                    shape = RoundedCornerShape(12.dp),

                    colors = CardDefaults.cardColors(containerColor = bgColor.copy(alpha = 0.3f))

                ) {

                    Column(Modifier.padding(12.dp)) {

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                            Text(icon, fontSize = 28.sp)

                            Spacer(Modifier.width(10.dp))

                            Column(Modifier.weight(1f)) {

                                Text("${event.year} ${event.month}${event.day.toInt()}",

                                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                                Text(event.eclipseType, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)

                            }

                            Column(horizontalAlignment = Alignment.End) {

                                Text(event.xiuName, color = Color(0xFFFFD700), fontSize = 14.sp)

                                Text(event.xiuGroup, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)

                            }

                        }

                        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {

                            Text("Ұ: ${event.fenYeAncient}${event.fenYeModern}", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)

                            Text(" ${event.confidence}%", color = Color(0xFF4CAF50), fontSize = 13.sp)

                        }

                        if (isExpanded) {

                            Divider(Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))

                            Text("͡", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            Text("${event.type}  ${event.eclipseType}ʳ ${"%.2f".format(event.magnitude)}", color = Color.White, fontSize = 14.sp)

                            Spacer(Modifier.height(6.dp))

                            Text("ޡ", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            Text("${event.xiuName}${event.xiuGroup}ޣ", color = Color.White, fontSize = 14.sp)

                            Spacer(Modifier.height(6.dp))

                            Text("ż", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            Text(event.ancientQuote, color = Color.White, fontSize = 14.sp)

                            Spacer(Modifier.height(4.dp))

                            Text("${event.source}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())

                            Spacer(Modifier.height(6.dp))

                            Text("ۺ", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            Text("${event.description}Ұ${event.fenYeAncient}${event.fenYeModern}", color = Color.White, fontSize = 14.sp)

                        }

                    }

                }

            }

            Spacer(Modifier.height(24.dp))

            Card(

                Modifier.fillMaxWidth().padding(12.dp),

                shape = RoundedCornerShape(12.dp),

                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))

            ) {

                Column(Modifier.padding(16.dp)) {

                    Text("?? ʳʳռ", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(8.dp))

                    Text("ʳʳҪ¼Ԫռ߾ƪרʴʮ壩רʴʮʮţλǷҰԫ֮ϡ", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)

                    Spacer(Modifier.height(4.dp))

                    Text("ʴҡ󳼡󹬡顷顷顷صʳش¼Ӧʼߡ", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)

                    Spacer(Modifier.height(4.dp))

                    Text("ʴ󳼡󹬡߾ʴӦڽϳһ꣩Ӱ췶ΧʴΪխȸ", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)

                    Spacer(Modifier.height(8.dp))

                    Text("㷽Meeus㷨ʳۣڼư׽⣩ղ㼶ɿʳʱ̾Լ30ӡ", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)

                }

            }

            Spacer(Modifier.height(24.dp))

        }

    }

}