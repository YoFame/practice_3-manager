package com.example.studentgradetracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.studentgradetracker.data.dao.DisciplineDao
import com.example.studentgradetracker.data.dao.GradeDao
import com.example.studentgradetracker.data.entity.Discipline
import com.example.studentgradetracker.data.entity.Grade

@Database(
    entities = [Discipline::class, Grade::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun disciplineDao(): DisciplineDao
    abstract fun gradeDao(): GradeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}