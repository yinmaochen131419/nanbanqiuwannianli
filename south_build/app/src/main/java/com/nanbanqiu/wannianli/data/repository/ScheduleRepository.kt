/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data.repository

import android.content.Context
import com.nanbanqiu.wannianli.data.model.ScheduleEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ScheduleRepository(context: Context) {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("wannianli_schedule", Context.MODE_PRIVATE)
    private val keyEvents = "events_list"

    private fun loadAll(): MutableList<ScheduleEvent> {
        val json = prefs.getString(keyEvents, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<ScheduleEvent>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun saveAll(events: List<ScheduleEvent>) {
        prefs.edit().putString(keyEvents, gson.toJson(events)).apply()
    }

    fun getAll(): List<ScheduleEvent> = loadAll().sortedBy { countdownDays(it) }

    fun getByType(type: Int): List<ScheduleEvent> =
        loadAll().filter { it.type == type }.sortedBy { countdownDays(it) }

    fun getUpcoming(limit: Int = 5): List<ScheduleEvent> {
        val today = LocalDate.now()
        return loadAll()
            .filter { countdownDays(it) >= 0 }
            .sortedBy { countdownDays(it) }
            .take(limit)
    }

    fun countdownDays(event: ScheduleEvent): Long {
        val today = LocalDate.now()
        if (event.isLunarDate) {
            if (!event.recurring) {
                val solar = event.toSolarDate(event.year)
                if (solar != null) {
                    val target = LocalDate.of(solar.first, solar.second, solar.third)
                    return ChronoUnit.DAYS.between(today, target)
                }
            }
            val thisYearSolar = event.toSolarDate(today.year)
            if (thisYearSolar != null) {
                val thisYearDate = LocalDate.of(thisYearSolar.first, thisYearSolar.second, thisYearSolar.third)
                if (thisYearDate >= today) return ChronoUnit.DAYS.between(today, thisYearDate)
            }
            for (y in today.year + 1..today.year + 3) {
                val solar = event.toSolarDate(y)
                if (solar != null) {
                    val date = LocalDate.of(solar.first, solar.second, solar.third)
                    return ChronoUnit.DAYS.between(today, date)
                }
            }
            val target = LocalDate.of(event.year, event.month, event.day)
            return ChronoUnit.DAYS.between(today, target)
        }
        if (!event.recurring) {
            val target = LocalDate.of(event.year, event.month, event.day)
            return ChronoUnit.DAYS.between(today, target)
        }
        val thisYear = LocalDate.of(today.year, event.month, event.day)
        return if (thisYear >= today) {
            ChronoUnit.DAYS.between(today, thisYear)
        } else {
            val nextYear = LocalDate.of(today.year + 1, event.month, event.day)
            ChronoUnit.DAYS.between(today, nextYear)
        }
    }

    fun save(event: ScheduleEvent) {
        val events = loadAll()
        val idx = events.indexOfFirst { it.id == event.id }
        if (idx >= 0) events[idx] = event else events.add(event)
        saveAll(events)
    }

    fun delete(id: Long) {
        val events = loadAll()
        events.removeAll { it.id == id }
        saveAll(events)
    }

    fun getById(id: Long): ScheduleEvent? = loadAll().find { it.id == id }
}