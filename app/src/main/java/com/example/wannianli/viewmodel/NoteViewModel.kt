package com.example.wannianli.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wannianli.data.model.DiaryNote
import com.example.wannianli.data.repository.NoteRepository
import com.example.wannianli.util.ExportUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NoteRepository(application)
    private val context = application

    private val _notes = MutableStateFlow<List<DiaryNote>>(emptyList())
    val notes: StateFlow<List<DiaryNote>> = _notes.asStateFlow()

    private val _availableYears = MutableStateFlow<List<Int>>(emptyList())
    val availableYears: StateFlow<List<Int>> = _availableYears.asStateFlow()

    private val _availableMonths = MutableStateFlow<List<Int>>(emptyList())
    val availableMonths: StateFlow<List<Int>> = _availableMonths.asStateFlow()

    private val _currentNote = MutableStateFlow<DiaryNote?>(null)
    val currentNote: StateFlow<DiaryNote?> = _currentNote.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportResult = MutableStateFlow<ExportResult?>(null)
    val exportResult: StateFlow<ExportResult?> = _exportResult.asStateFlow()

    private var currentFilterYear: Int? = null
    private var currentFilterMonth: Int? = null

    init {
        loadAllNotes()
    }

    fun loadAllNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            currentFilterYear = null
            currentFilterMonth = null
            _notes.value = repository.getAllNotes()
            _availableYears.value = repository.getAvailableYears()
            _availableMonths.value = emptyList()
        }
    }

    fun filterByYear(year: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            currentFilterYear = year
            currentFilterMonth = null
            _notes.value = repository.getNotesByYear(year)
            _availableYears.value = repository.getAvailableYears()
            _availableMonths.value = repository.getAvailableMonths(year)
        }
    }

    fun filterByMonth(year: Int, month: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            currentFilterYear = year
            currentFilterMonth = month
            _notes.value = repository.getNotesByMonth(year, month)
            _availableYears.value = repository.getAvailableYears()
            _availableMonths.value = repository.getAvailableMonths(year)
        }
    }

    fun loadNote(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentNote.value = repository.getNoteById(id)
        }
    }

    fun saveNote(note: DiaryNote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveNote(note)
            refreshCurrentFilter()
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(id)
            refreshCurrentFilter()
        }
    }

    fun createNewNote() {
        _currentNote.value = null
    }

    private fun refreshCurrentFilter() {
        if (currentFilterYear != null && currentFilterMonth != null) {
            filterByMonth(currentFilterYear!!, currentFilterMonth!!)
        } else if (currentFilterYear != null) {
            filterByYear(currentFilterYear!!)
        } else {
            loadAllNotes()
        }
    }

    fun exportAllNotes(format: ExportUtil.ExportFormat) {
        viewModelScope.launch(Dispatchers.IO) {
            _isExporting.value = true
            try {
                val notesToExport = repository.getAllNotes()
                if (notesToExport.isEmpty()) {
                    _exportResult.value = ExportResult(false, "没有可导出的笔记")
                    _isExporting.value = false
                    return@launch
                }

                val label = "全部"
                val file = ExportUtil.exportNotes(context, notesToExport, format, label)

                _exportResult.value = ExportResult(
                    success = true,
                    message = "导出成功：共 ${notesToExport.size} 条笔记",
                    file = file,
                    mimeType = when (format) {
                        ExportUtil.ExportFormat.TXT -> "text/plain"
                        ExportUtil.ExportFormat.WORD -> "application/msword"
                        ExportUtil.ExportFormat.EXCEL -> "text/csv"
                    }
                )
            } catch (e: Exception) {
                _exportResult.value = ExportResult(false, "导出失败: ${e.message}")
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun clearExportResult() {
        _exportResult.value = null
    }

    fun shareExportedFile() {
        val result = _exportResult.value ?: return
        if (result.success && result.file != null) {
            viewModelScope.launch(Dispatchers.Main) {
                ExportUtil.shareFile(context, result.file, result.mimeType ?: "text/plain")
            }
        }
    }

    fun shareToWeChat() {
        val result = _exportResult.value ?: return
        if (result.success && result.file != null) {
            viewModelScope.launch(Dispatchers.Main) {
                ExportUtil.shareToWeChat(context, result.file, result.mimeType ?: "text/plain")
            }
        }
    }

    fun shareToQQ() {
        val result = _exportResult.value ?: return
        if (result.success && result.file != null) {
            viewModelScope.launch(Dispatchers.Main) {
                ExportUtil.shareToQQ(context, result.file, result.mimeType ?: "text/plain")
            }
        }
    }

    private fun buildExportLabel(): String {
        return when {
            currentFilterYear != null && currentFilterMonth != null ->
                "${currentFilterYear}年${currentFilterMonth.toString().padStart(2, '0')}月"
            currentFilterYear != null -> "${currentFilterYear}年"
            else -> "全部"
        }
    }

    data class ExportResult(
        val success: Boolean,
        val message: String,
        val file: File? = null,
        val mimeType: String? = null
    )
}