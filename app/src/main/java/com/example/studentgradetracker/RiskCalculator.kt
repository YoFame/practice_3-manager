
package com.example.studentgradetracker

import com.example.studentgradetracker.data.entity.Grade

object RiskCalculator {

    data class RiskResult(
        val title: String,
        val reasons: List<String>,
        val color: Int
    )

    fun calculate(
        grades: List<Grade>,
        threshold: Double = 3.5,
        minimumGrades: Int = 3,
        periodStart: String? = null,
        periodEnd: String? = null
    ): RiskResult {

        val reasons = mutableListOf<String>()

        // Проверяем средний балл по всем оценкам дисциплины
        val average = if (grades.isNotEmpty()) {
            grades.map { it.value }.average()
        } else {
            null
        }

        if (average != null && average < threshold) {
            reasons.add(
                "Средний балл ниже порога ${"%.1f".format(threshold)}"
            )
        }

        // Проверяем количество оценок
        if (grades.size < minimumGrades) {
            reasons.add(
                "Мало оценок: ${grades.size} из необходимых $minimumGrades"
            )
        }

        // Проверяем наличие оценок за выбранный период
        val periodSelected = periodStart != null || periodEnd != null

        if (periodSelected) {
            val periodGrades = grades.filter { grade ->
                val afterStart = periodStart == null ||
                        grade.date >= periodStart

                val beforeEnd = periodEnd == null ||
                        grade.date <= periodEnd

                afterStart && beforeEnd
            }

            if (periodGrades.isEmpty()) {
                reasons.add("Нет оценок за выбранный период")
            }
        }

        val title = when {
            average != null && average < threshold -> "Высокий риск"
            reasons.isNotEmpty() -> "Есть риск"
            else -> "Всё хорошо"
        }

        val color = when (title) {
            "Высокий риск" -> android.graphics.Color.rgb(255, 220, 220)
            "Есть риск" -> android.graphics.Color.rgb(255, 243, 205)
            else -> android.graphics.Color.rgb(220, 245, 220)
        }

        if (reasons.isEmpty()) {
            reasons.add("Причин для беспокойства нет")
        }

        return RiskResult(
            title = title,
            reasons = reasons,
            color = color
        )
    }
}
