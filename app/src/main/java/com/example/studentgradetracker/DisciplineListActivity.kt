package com.example.studentgradetracker

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.studentgradetracker.data.database.AppDatabase

class DisciplineListActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discipline_list)

        db = AppDatabase.getDatabase(this)
    val addButton = findViewById<Button>(R.id.addDisciplineButton)

        addButton.setOnClickListener {
            showAddDisciplineDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDisciplines()
    }

    private fun showAddDisciplineDialog() {
        val input = EditText(this)
        input.hint = "Название дисциплины"

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Добавить дисциплину")
            .setView(input)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(
                android.app.AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val name = input.text.toString().trim()

                if (name.isEmpty()) {
                    input.error = "Введите название дисциплины"
                } else {
                    saveDiscipline(name)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun saveDiscipline(name: String) {
        lifecycleScope.launch {
            try {
                db.disciplineDao().insert(
                    com.example.studentgradetracker.data.entity.Discipline(
                        name = name
                    )
                )

                loadDisciplines()

                Toast.makeText(
                    this@DisciplineListActivity,
                    "Дисциплина добавлена",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@DisciplineListActivity,
                    "Ошибка: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadDisciplines() {
        lifecycleScope.launch {
            val disciplines = db.disciplineDao().getAll()
            val grades = db.gradeDao().getAll()

            val container =
                findViewById<LinearLayout>(R.id.disciplineList)

            val emptyText =
                findViewById<TextView>(R.id.emptyDisciplinesText)

            container.removeAllViews()

            emptyText.visibility =
                if (disciplines.isEmpty()) View.VISIBLE
                else View.GONE

            disciplines.forEach { discipline ->

                val subjectGrades = grades.filter {
                    it.disciplineId == discipline.id
                }

                val risk = RiskCalculator.calculate(
                    grades = subjectGrades,
                    threshold = 3.5,
                    minimumGrades = 3
                )

                val average = if (subjectGrades.isNotEmpty()) {
                    subjectGrades.map { it.value }.average()
                } else {
                    0.0
                }

                // Создаем карточку
                val card = LinearLayout(this@DisciplineListActivity)
                card.orientation = LinearLayout.HORIZONTAL
                card.gravity = Gravity.CENTER_VERTICAL
                card.setPadding(20, 25, 20, 25)
                card.setBackgroundColor(risk.color)

                // Текст (слева)
                val textContainer = LinearLayout(this@DisciplineListActivity)
                textContainer.orientation = LinearLayout.VERTICAL
                val textParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textContainer.layoutParams = textParams

                val nameText = TextView(this@DisciplineListActivity)
                nameText.text = discipline.name
                nameText.textSize = 18f
                nameText.setTextColor(Color.BLACK)
                nameText.setTypeface(null, android.graphics.Typeface.BOLD)

                val averageText = TextView(this@DisciplineListActivity)
                averageText.text = if (average == 0.0) "Нет оценок" else "Средний балл: %.2f".format(average)
                averageText.textSize = 14f
                averageText.setTextColor(Color.DKGRAY)

                val riskText = TextView(this@DisciplineListActivity)
                riskText.text = risk.title + "\n" + risk.reasons.joinToString("\n")
                riskText.textSize = 12f
                riskText.setTextColor(Color.DKGRAY)
                riskText.setPadding(0, 8, 0, 0)

                textContainer.addView(nameText)
                textContainer.addView(averageText)
                textContainer.addView(riskText)

                card.addView(textContainer)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = 12
                container.addView(card, params)

                // Обычное нажатие - переход в дисциплину
                card.setOnClickListener {
                    val intent = Intent(
                        this@DisciplineListActivity,
                        DisciplineActivity::class.java
                    )

                    intent.putExtra("disciplineId", discipline.id)
                    intent.putExtra("disciplineName", discipline.name)

                    startActivity(intent)
                }

                // ДОЛГОЕ НАЖАТИЕ - УДАЛЕНИЕ ДИСЦИПЛИНЫ
                card.setOnLongClickListener {
                    showDeleteConfirmation(discipline.name, discipline.id)
                    true
                }
            }
        }
    }

    // Функция для подтверждения удаления
    private fun showDeleteConfirmation(name: String, id: Int) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Удалить дисциплину?")
            .setMessage("Вы уверены, что хотите удалить «$name»? Все оценки по этой дисциплине будут безвозвратно удалены.")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    // Находим дисциплину по ID и удаляем её.
                    // Благодаря CASCADE, оценки удалятся сами.
                    val disciplines = db.disciplineDao().getAll()
                    val disciplineToDelete = disciplines.find { it.id == id }

                    if (disciplineToDelete != null) {
                        db.disciplineDao().delete(disciplineToDelete)
                        loadDisciplines() // Обновляем список
                        Toast.makeText(this@DisciplineListActivity, "Дисциплина удалена", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}