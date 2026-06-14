/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data.model

data class DiaryNote(
    val id: Long = System.currentTimeMillis(),
    val date: String,
    val time: String,
    val yearPillar: String,
    val monthPillar: String,
    val dayPillar: String,
    val hourPillar: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)