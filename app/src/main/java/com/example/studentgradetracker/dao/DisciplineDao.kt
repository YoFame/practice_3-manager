package com.example.studentgradetracker.data.dao

import androidx.room.*
import com.example.studentgradetracker.data.entity.Discipline

@Dao
interface DisciplineDao {

    @Query("SELECT * FROM disciplines")
    suspend fun getAll(): List<Discipline>

    @Insert
    suspend fun insert(discipline: Discipline): Long

    @Update
    suspend fun update(discipline: Discipline)

    @Delete
    suspend fun delete(discipline: Discipline)
}