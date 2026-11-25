package com.example.studentgradetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = Discipline::class,
            parentColumns = ["id"],
            childColumns = ["disciplineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("disciplineId")]
)
data class Grade(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val disciplineId: Int,
    val value: Int,
    val date: String,
    val workType: String,
    val comment: String = ""
)