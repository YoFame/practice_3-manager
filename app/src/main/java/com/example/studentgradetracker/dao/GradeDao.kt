package com.example.studentgradetracker.data.dao

import androidx.room.*
import com.example.studentgradetracker.data.entity.Grade

@Dao
interface GradeDao {

    @Query("SELECT * FROM grades")
    suspend fun getAll(): List<Grade>

    @Query("SELECT * FROM grades WHERE disciplineId = :disciplineId")
    suspend fun getByDiscipline(disciplineId: Int): List<Grade>

    @Insert
    suspend fun insert(grade: Grade): Long

    @Update
    suspend fun update(grade: Grade)

    @Delete
    suspend fun delete(grade: Grade)
}