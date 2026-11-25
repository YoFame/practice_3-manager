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

    @Query("""
    SELECT * FROM grades
    WHERE disciplineId = :disciplineId
    AND (:workType IS NULL OR workType = :workType)
    AND (:startDate IS NULL OR date >= :startDate)
    AND (:endDate IS NULL OR date <= :endDate)
    ORDER BY date DESC
""")
    suspend fun getFilteredGrades(
        disciplineId: Int,
        workType: String?,
        startDate: String?,
        endDate: String?
    ): List<Grade>

    @Query("""
    SELECT * FROM grades
    WHERE disciplineId = :disciplineId
    ORDER BY date DESC
""")
    suspend fun getGradesByDate(disciplineId: Int): List<Grade>

    @Query("""
    SELECT * FROM grades
    WHERE disciplineId = :disciplineId
    ORDER BY value DESC
""")
    suspend fun getGradesByValue(disciplineId: Int): List<Grade>
}