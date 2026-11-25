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

                val average = if (subjectGrades.isNotEmpty()) {
                    subjectGrades.map { it.value }.average()
                } else {
                    0.0
                }


                val card = TextView(this@DisciplineListActivity)

                card.text = "${discipline.name}       ${
                    String.format("%.1f", average)
                }"

                card.textSize = 17f
                card.setTextColor(Color.BLACK)
                card.gravity = Gravity.CENTER_VERTICAL
                card.setPadding(20, 25, 20, 25)
                card.setBackgroundColor(Color.WHITE)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                params.bottomMargin = 12

                container.addView(card, params)

                card.setOnClickListener {
                    val intent = Intent(
                        this@DisciplineListActivity,
                        DisciplineActivity::class.java
                    )

                    intent.putExtra("disciplineId", discipline.id)
                    intent.putExtra("disciplineName", discipline.name)

                    startActivity(intent)

                }
            }
        }
    }
}
