package com.example.studentgradetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disciplines")
data class Discipline(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String
)