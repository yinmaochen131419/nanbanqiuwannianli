/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data.repository

import android.content.Context
import com.nanbanqiu.wannianli.data.model.DiaryNote
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NoteRepository(private val context: Context) {
    private val gson = Gson()
    private val prefsName = "wannianli_notes"
    private val keyNotes = "notes_list"

    private fun loadNotes(): MutableList<DiaryNote> {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = prefs.getString(keyNotes, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<DiaryNote>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun saveNotes(notes: List<DiaryNote>) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().putString(keyNotes, gson.toJson(notes)).apply()
    }

    fun getAllNotes(): List<DiaryNote> {
        return loadNotes().sortedByDescending { it.date + it.time }
    }

    fun getNotesByYear(year: Int): List<DiaryNote> {
        return loadNotes().filter { it.date.startsWith("$year-") }
            .sortedByDescending { it.date + it.time }
    }

    fun getNotesByMonth(year: Int, month: Int): List<DiaryNote> {
        val prefix = "$year-${month.toString().padStart(2, '0')}"
        return loadNotes().filter { it.date.startsWith(prefix) }
            .sortedByDescending { it.date + it.time }
    }

    fun getNoteById(id: Long): DiaryNote? {
        return loadNotes().find { it.id == id }
    }

    fun saveNote(note: DiaryNote) {
        val notes = loadNotes()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            notes[index] = note
        } else {
            notes.add(note)
        }
        saveNotes(notes)
    }

    fun deleteNote(id: Long) {
        val notes = loadNotes()
        notes.removeAll { it.id == id }
        saveNotes(notes)
    }

    fun getAvailableYears(): List<Int> {
        return loadNotes().map { it.date.substring(0, 4).toInt() }.distinct().sortedDescending()
    }

    fun getAvailableMonths(year: Int): List<Int> {
        val prefix = "$year-"
        return loadNotes()
            .filter { it.date.startsWith(prefix) }
            .map { it.date.substring(5, 7).toInt() }
            .distinct()
            .sortedDescending()
    }

    fun searchNotes(keyword: String): List<DiaryNote> {
        val kw = keyword.trim().lowercase()
        if (kw.isEmpty()) return getAllNotes()
        return loadNotes().filter { note ->
            note.content.lowercase().contains(kw)
        }.sortedByDescending { it.date + it.time }
    }
}