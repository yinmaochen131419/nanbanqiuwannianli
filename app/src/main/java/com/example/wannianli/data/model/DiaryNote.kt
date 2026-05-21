package com.example.wannianli.data.model

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