/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanbanqiu.wannianli.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_knowledge), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 南北半球历法换算原理
            KnowledgeCard(
                title = stringResource(R.string.knowledge_section_1),
                content = stringResource(R.string.knowledge_content_1)
            )

            // 2. 月相翻转·太极验证
            KnowledgeCard(
                title = stringResource(R.string.knowledge_section_2),
                content = stringResource(R.string.knowledge_content_2)
            )

            // 3. 南半球节气对应关系
            KnowledgeCard(
                title = stringResource(R.string.knowledge_section_3),
                content = stringResource(R.string.knowledge_content_3)
            )

            // 4. 南半球星象定节气
            KnowledgeCard(
                title = stringResource(R.string.knowledge_section_4),
                content = stringResource(R.string.knowledge_content_4)
            )

            // 5. 月相与南北半球
            KnowledgeCard(
                title = stringResource(R.string.knowledge_section_5),
                content = stringResource(R.string.knowledge_content_5)
            )

            // 6. 科里奥利力与旋转方向
            CoriolisCard()

            // 7. 四柱八字与时辰
            KnowledgeCard(
                title = stringResource(R.string.knowledge_section_7),
                content = stringResource(R.string.knowledge_content_7)
            )

            // 8. 值日星宿与月躔星宿
            KnowledgeCard(
                title = stringResource(R.string.knowledge_section_8),
                content = stringResource(R.string.knowledge_content_8)
            )

            // 9. 南半球历法的意义
            KnowledgeCard(
                title = stringResource(R.string.knowledge_section_9),
                content = stringResource(R.string.knowledge_content_9)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun KnowledgeCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                content,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color(0xFF424242)
            )
        }
    }
}

@Composable
private fun CoriolisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.knowledge_section_6),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.knowledge_content_6_intro),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val headerColor = Color(0xFF1565C0)
            val rowEvenColor = Color(0xFFE3F2FD)
            val rowOddColor = Color(0xFFF5F5F5)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Text(stringResource(R.string.knowledge_content_6_table_header_system), modifier = Modifier.weight(1f), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.knowledge_content_6_table_header_north), modifier = Modifier.weight(0.6f), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(stringResource(R.string.knowledge_content_6_table_header_south), modifier = Modifier.weight(0.6f), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }

            val tableData = listOf(
                Triple(stringResource(R.string.knowledge_content_6_row1_system), stringResource(R.string.knowledge_content_6_row1_north), stringResource(R.string.knowledge_content_6_row1_south)),
                Triple(stringResource(R.string.knowledge_content_6_row2_system), stringResource(R.string.knowledge_content_6_row2_north), stringResource(R.string.knowledge_content_6_row2_south)),
                Triple(stringResource(R.string.knowledge_content_6_row3_system), stringResource(R.string.knowledge_content_6_row3_north), stringResource(R.string.knowledge_content_6_row3_south)),
                Triple(stringResource(R.string.knowledge_content_6_row4_system), stringResource(R.string.knowledge_content_6_row4_north), stringResource(R.string.knowledge_content_6_row4_south))
            )

            tableData.forEachIndexed { index, (system, north, south) ->
                val bgColor = if (index % 2 == 0) rowEvenColor else rowOddColor
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Text(system, modifier = Modifier.weight(1f), fontSize = 13.sp, color = Color(0xFF424242))
                    Text(north, modifier = Modifier.weight(0.6f), fontSize = 13.sp, color = Color(0xFF1565C0), textAlign = TextAlign.Center)
                    Text(south, modifier = Modifier.weight(0.6f), fontSize = 13.sp, color = Color(0xFFC62828), textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(R.string.knowledge_content_6_math),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color(0xFF424242)
            )
        }
    }
}
