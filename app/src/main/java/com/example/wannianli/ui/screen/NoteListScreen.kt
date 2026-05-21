package com.example.wannianli.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wannianli.data.model.DiaryNote
import com.example.wannianli.util.ExportUtil
import com.example.wannianli.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit,
    onEdit: (Long?) -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val availableYears by viewModel.availableYears.collectAsState()
    val availableMonths by viewModel.availableMonths.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showYearFilter by remember { mutableStateOf(false) }
    var showMonthFilter by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    var showExportResultDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val displayNotes = if (searchQuery.isBlank()) {
        notes
    } else {
        notes.filter { note ->
            note.content.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日课应验笔记", fontWeight = FontWeight.Bold) },
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
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = "导出",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createNewNote()
                    onEdit(null)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新增", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showYearFilter,
                    onClick = { showYearFilter = !showYearFilter; showMonthFilter = false },
                    label = { Text("按年") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = showMonthFilter,
                    onClick = { showMonthFilter = !showMonthFilter; showYearFilter = false },
                    label = { Text("按月") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { viewModel.loadAllNotes(); showYearFilter = false; showMonthFilter = false; searchQuery = "" }) {
                    Text("全部")
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("搜索笔记内容...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "搜索",
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "清除", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            if (showYearFilter && availableYears.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    items(availableYears) { year ->
                        ListItem(
                            headlineContent = { Text("${year}年") },
                            modifier = Modifier.clickable {
                                viewModel.filterByYear(year)
                                showYearFilter = false
                                showMonthFilter = true
                            }
                        )
                    }
                }
            }

            if (showMonthFilter && availableMonths.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    items(availableMonths) { month ->
                        val year = availableYears.firstOrNull() ?: return@items
                        ListItem(
                            headlineContent = { Text("${year}年${month}月") },
                            modifier = Modifier.clickable {
                                viewModel.filterByMonth(year, month)
                                showMonthFilter = false
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            if (displayNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (searchQuery.isBlank()) "暂无笔记" else "未找到匹配的笔记",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (searchQuery.isBlank()) "点击右下角 + 新增" else "尝试其他关键词",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onEdit(note.id) },
                            onDelete = { showDeleteDialog = note.id }
                        )
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            viewModel = viewModel,
            isExporting = isExporting,
            onDismiss = { showExportDialog = false },
            onExportComplete = {
                showExportDialog = false
                showExportResultDialog = true
            }
        )
    }

    if (showExportResultDialog && exportResult != null) {
        ExportResultDialog(
            result = exportResult!!,
            onDismiss = {
                showExportResultDialog = false
                viewModel.clearExportResult()
            },
            onShareToWeChat = { viewModel.shareToWeChat() },
            onShareToQQ = { viewModel.shareToQQ() },
            onShareMore = { viewModel.shareExportedFile() }
        )
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条笔记吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { viewModel.deleteNote(it) }
                    showDeleteDialog = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun NoteCard(
    note: DiaryNote,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${note.date}  ${note.time}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillarChip("年", note.yearPillar)
                PillarChip("月", note.monthPillar)
                PillarChip("日", note.dayPillar)
                PillarChip("时", note.hourPillar)
            }
            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PillarChip(label: String, value: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = "$label.$value",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun ExportDialog(
    viewModel: NoteViewModel,
    isExporting: Boolean,
    onDismiss: () -> Unit,
    onExportComplete: () -> Unit
) {
    val exportResult by viewModel.exportResult.collectAsState()

    LaunchedEffect(exportResult) {
        if (exportResult != null && exportResult!!.success) {
            onExportComplete()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出全部笔记") },
        text = {
            Column {
                if (isExporting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("正在导出，请稍候...")
                    }
                } else {
                    Text("选择导出格式（所有笔记将合并为一个文件）：")
                    Spacer(Modifier.height(12.dp))
                    ExportFormatButton("TXT 文本", Icons.Default.Description) {
                        viewModel.exportAllNotes(ExportUtil.ExportFormat.TXT)
                    }
                    Spacer(Modifier.height(8.dp))
                    ExportFormatButton("Word 文档", Icons.Default.Article) {
                        viewModel.exportAllNotes(ExportUtil.ExportFormat.WORD)
                    }
                    Spacer(Modifier.height(8.dp))
                    ExportFormatButton("Excel 表格", Icons.Default.TableChart) {
                        viewModel.exportAllNotes(ExportUtil.ExportFormat.EXCEL)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ExportResultDialog(
    result: NoteViewModel.ExportResult,
    onDismiss: () -> Unit,
    onShareToWeChat: () -> Unit,
    onShareToQQ: () -> Unit,
    onShareMore: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (result.success) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (result.success) "导出完成" else "导出失败",
                    color = if (result.success) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column {
                Text(result.message)
                if (result.success && result.file != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "文件路径：",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = result.file.absolutePath,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "选择分享方式：",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShareAppButton(
                            label = "微信",
                            color = Color(0xFF07C160),
                            onClick = onShareToWeChat,
                            modifier = Modifier.weight(1f)
                        )
                        ShareAppButton(
                            label = "QQ",
                            color = Color(0xFF12B7F5),
                            onClick = onShareToQQ,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = onShareMore,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("更多", fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (result.success) "关闭" else "确定")
            }
        }
    )
}

@Composable
private fun ShareAppButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 13.sp, color = Color.White)
    }
}

@Composable
private fun ExportFormatButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}