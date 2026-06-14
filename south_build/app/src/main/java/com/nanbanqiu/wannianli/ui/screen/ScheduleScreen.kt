/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.nanbanqiu.wannianli.R
import com.nanbanqiu.wannianli.data.model.ScheduleEvent
import com.nanbanqiu.wannianli.data.repository.ScheduleRepository
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { ScheduleRepository(context) }
    var events by remember { mutableStateOf(repository.getAll()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<ScheduleEvent?>(null) }

    val filteredEvents = when (selectedTab) {
        1 -> events.filter { it.type == ScheduleEvent.TYPE_BIRTHDAY }
        2 -> events.filter { it.type == ScheduleEvent.TYPE_ANNIVERSARY }
        3 -> events.filter { it.type == ScheduleEvent.TYPE_TODO }
        else -> events
    }

    val tabs = listOf(
        stringResource(R.string.schedule_tab_all),
        stringResource(R.string.schedule_tab_birthday),
        stringResource(R.string.schedule_tab_anniversary),
        stringResource(R.string.schedule_tab_todo)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_schedule), fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingEvent = null
                    showDialog = true
                },
                containerColor = Color(0xFF1565C0)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.schedule_add))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFE3F2FD),
                contentColor = Color(0xFF1565C0),
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFBDBDBD)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.schedule_empty), fontSize = 16.sp, color = Color(0xFF9E9E9E))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.schedule_empty_hint), fontSize = 14.sp, color = Color(0xFFBDBDBD))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            countdownDays = repository.countdownDays(event),
                            onEdit = {
                                editingEvent = event
                                showDialog = true
                            },
                            onDelete = {
                                repository.delete(event.id)
                                events = repository.getAll()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        EventDialog(
            event = editingEvent,
            onDismiss = { showDialog = false },
            onSave = { event ->
                repository.save(event)
                events = repository.getAll()
                showDialog = false
            }
        )
    }
}

@Composable
private fun EventCard(
    event: ScheduleEvent,
    countdownDays: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                event.typeIcon,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val dateStr = if (event.isLunarDate) {
                    "${stringResource(R.string.schedule_lunar_prefix)}${ScheduleEvent.LUNAR_MONTH_NAMES.getOrElse(event.month - 1) { "${event.month}${stringResource(R.string.common_month)}" }}${event.day}${stringResource(R.string.common_day)}"
                } else {
                    "${event.month}${stringResource(R.string.common_month)}${event.day}${stringResource(R.string.common_day)}"
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateStr, fontSize = 13.sp, color = Color(0xFF757575))
                    if (event.recurring) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Replay,
                            contentDescription = stringResource(R.string.schedule_recurring),
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (countdownDays >= 0) {
                    Text(
                        stringResource(R.string.schedule_countdown, countdownDays),
                        fontSize = 13.sp,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        stringResource(R.string.schedule_expired),
                        fontSize = 13.sp,
                        color = Color(0xFFE53935)
                    )
                }
                Text(
                    event.typeName,
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.common_delete), tint = Color(0xFF9E9E9E), modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.schedule_delete_confirm)) },
            text = { Text(stringResource(R.string.schedule_delete_message, event.title)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text(stringResource(R.string.common_delete), color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDialog(
    event: ScheduleEvent?,
    onDismiss: () -> Unit,
    onSave: (ScheduleEvent) -> Unit
) {
    val isEdit = event != null
    var title by remember { mutableStateOf(event?.title ?: "") }
    var type by remember { mutableIntStateOf(event?.type ?: 0) }
    var year by remember { mutableIntStateOf(event?.year ?: LocalDate.now().year) }
    var month by remember { mutableIntStateOf(event?.month ?: LocalDate.now().monthValue) }
    var day by remember { mutableIntStateOf(event?.day ?: LocalDate.now().dayOfMonth) }
    var recurring by remember { mutableStateOf(event?.recurring ?: false) }
    var isLunarDate by remember { mutableStateOf(event?.isLunarDate ?: false) }
    var note by remember { mutableStateOf(event?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) stringResource(R.string.schedule_edit) else stringResource(R.string.schedule_add), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.schedule_title_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.schedule_type_label), fontSize = 13.sp, color = Color(0xFF757575))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScheduleEvent.TYPE_NAMES.forEachIndexed { index, name ->
                        FilterChip(
                            selected = type == index,
                            onClick = { type = index },
                            label = { Text(name, fontSize = 12.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.common_year), fontSize = 12.sp, color = Color(0xFF757575))
                        var yearText by remember { mutableStateOf(year.toString()) }
                        OutlinedTextField(
                            value = yearText,
                            onValueChange = {
                                yearText = it
                                year = it.toIntOrNull() ?: year
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.common_month), fontSize = 12.sp, color = Color(0xFF757575))
                        var monthText by remember { mutableStateOf(month.toString()) }
                        OutlinedTextField(
                            value = monthText,
                            onValueChange = {
                                monthText = it
                                month = it.toIntOrNull()?.coerceIn(1, 12) ?: month
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.common_day), fontSize = 12.sp, color = Color(0xFF757575))
                        var dayText by remember { mutableStateOf(day.toString()) }
                        OutlinedTextField(
                            value = dayText,
                            onValueChange = {
                                dayText = it
                                day = it.toIntOrNull()?.coerceIn(1, 31) ?: day
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.schedule_recurring_label), fontSize = 14.sp)
                    Switch(checked = recurring, onCheckedChange = { recurring = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.schedule_lunar_date_label), fontSize = 14.sp)
                    Switch(checked = isLunarDate, onCheckedChange = { isLunarDate = it })
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.schedule_note_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) return@TextButton
                    val newEvent = ScheduleEvent(
                        id = event?.id ?: System.currentTimeMillis(),
                        type = type,
                        title = title,
                        year = year,
                        month = month,
                        day = day,
                        recurring = recurring,
                        isLunarDate = isLunarDate,
                        note = note
                    )
                    onSave(newEvent)
                },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.common_save), color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
