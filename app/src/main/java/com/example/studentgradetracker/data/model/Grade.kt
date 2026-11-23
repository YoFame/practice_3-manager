package com.example.studentgradetracker.data.model

data class Grade(
    val id: Int = 0,
    val subjectId: Int,
    val value: Int,
    val date: String,
    val workType: String,
    val comment: String = ""
)
