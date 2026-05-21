package com.example.wannianli.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wannianli.data.model.DiaryNote
import com.example.wannianli.viewmodel.MainViewModel
import com.example.wannianli.viewmodel.NoteViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long?,
    noteViewModel: NoteViewModel,
    mainViewModel: MainViewModel,
    onBack: () -> Unit
) {
    val existingNote by noteViewModel.currentNote.collectAsState()
    val calendarDay by mainViewModel.calendarDay.collectAsState()

    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var yearPillar by remember { mutableStateOf("") }
    var monthPillar by remember { mutableStateOf("") }
    var dayPillar by remember { mutableStateOf("") }
    var hourPillar by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        if (noteId != null) {
            noteViewModel.loadNote(noteId)
        }
    }

    LaunchedEffect(existingNote) {
        if (!initialized && noteId != null && existingNote != null) {
            val n = existingNote!!
            dateText = n.date
            timeText = n.time
            yearPillar = n.yearPillar
            monthPillar = n.monthPillar
            dayPillar = n.dayPillar
            hourPillar = n.hourPillar
            content = n.content
            initialized = true
        }
    }

    LaunchedEffect(calendarDay, noteId) {
        if (!initialized && noteId == null && calendarDay != null) {
            val day = calendarDay!!
            dateText = "${day.gregorianYear}-${day.gregorianMonth.toString().padStart(2, '0')}-${day.gregorianDay.toString().padStart(2, '0')}"
            val cal = Calendar.getInstance()
            timeText = "${cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${cal.get(Calendar.MINUTE).toString().padStart(2, '0')}"
            yearPillar = day.lunarDate.yearGanZhi
            monthPillar = day.lunarDate.monthGanZhi
            dayPillar = day.lunarDate.dayGanZhi
            hourPillar = day.lunarDate.hourGanZhi
            initialized = true
        }
    }

    LaunchedEffect(dateText) {
        if (initialized && dateText.length == 10 && noteId == null) {
            try {
                val parts = dateText.split("-")
                val y = parts[0].toInt()
                val m = parts[1].toInt()
                val d = parts[2].toInt()
                mainViewModel.loadDate(y, m, d)
            } catch (_: Exception) {}
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        dateText = "${cal.get(Calendar.YEAR)}-${(cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}-${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    timeText = "${timePickerState.hour.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')}"
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (noteId == null) "新增笔记" else "编辑笔记",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    TextButton(onClick = {
                        if (dateText.isNotBlank()) {
                            val time = timeText.ifBlank { "12:00" }
                            val note = DiaryNote(
                                id = existingNote?.id ?: System.currentTimeMillis(),
                                date = dateText,
                                time = time,
                                yearPillar = yearPillar,
                                monthPillar = monthPillar,
                                dayPillar = dayPillar,
                                hourPillar = hourPillar,
                                content = content,
                                createdAt = existingNote?.createdAt ?: System.currentTimeMillis()
                            )
                            noteViewModel.saveNote(note)
                        }
                        onBack()
                    }) {
                        Text("保存", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("日期") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期")
                        }
                    },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = timeText,
                onValueChange = { timeText = it },
                label = { Text("时间") },
                placeholder = { Text("HH:mm") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "选择时间")
                    }
                },
                singleLine = true
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("四柱（根据日期自动计算）",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PillarField("年柱", yearPillar) { yearPillar = it }
                        PillarField("月柱", monthPillar) { monthPillar = it }
                        PillarField("日柱", dayPillar) { dayPillar = it }
                        PillarField("时柱", hourPillar) { hourPillar = it }
                    }
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("应验记录") },
                placeholder = { Text("记录当日的应验情况...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                maxLines = 20
            )
        }
    }
}

@Composable
private fun PillarField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(72.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}