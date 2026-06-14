/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.nanbanqiu.wannianli.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PROHIBITED_WORDS = listOf(
    "骂", "贱", "烂", "废", "傻", "笨", "脑残", "白痴", "垃圾", "滚",
    "死", "操", "靠", "屁", "狗屎", "放屁",
    "政治", "反动", "暴力", "色情", "赌博", "毒品", "自杀", "杀人"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var feedbackText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Pre-load string resources at Composable level
    val errorEmpty = stringResource(R.string.feedback_error_empty)
    val errorProhibited = stringResource(R.string.feedback_error_prohibited)
    val errorNoEmail = stringResource(R.string.feedback_error_no_email)
    val emailSubject = stringResource(R.string.feedback_email_subject)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_feedback), fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.feedback_intro),
                fontSize = 14.sp,
                color = Color(0xFF616161),
                lineHeight = 20.sp
            )

            OutlinedTextField(
                value = feedbackText,
                onValueChange = {
                    feedbackText = it
                    showError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text(stringResource(R.string.feedback_placeholder)) },
                isError = showError,
                supportingText = if (showError) {
                    { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            Button(
                onClick = {
                    if (feedbackText.isBlank()) {
                        showError = true
                        errorMessage = errorEmpty
                        return@Button
                    }
                    val found = PROHIBITED_WORDS.firstOrNull { feedbackText.contains(it) }
                    if (found != null) {
                        showError = true
                        errorMessage = errorProhibited
                        return@Button
                    }
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:158206209@qq.com")
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                        putExtra(Intent.EXTRA_TEXT, feedbackText)
                    }
                    try {
                        context.startActivity(intent)
                        onBack()
                    } catch (e: Exception) {
                        showError = true
                        errorMessage = errorNoEmail
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.feedback_submit), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
