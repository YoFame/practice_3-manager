package com.example.studentgradetracker

import com.example.studentgradetracker.data.entity.Grade

object GradeCalculator {

    // Средний балл по списку оценок
    fun calculateAverage(grades: List<Grade>): Double? {
        if (grades.isEmpty()) return null
        return grades.map { it.value }.average()
    }

    // Общий средний балл
    fun calculateOverallAverage(grades: List<Grade>): Double? {
        return calculateAverage(grades)
    }

    // Средний балл в удобном формате
    fun formatAverage(grades: List<Grade>): String {
        val average = calculateAverage(grades)
        return if (average == null) {
            "Нет оценок"
        } else {
            String.format("%.2f", average)
        }
    }

    // Количество оценок
    fun countGrades(grades: List<Grade>): Int {
        return grades.size
    }
}