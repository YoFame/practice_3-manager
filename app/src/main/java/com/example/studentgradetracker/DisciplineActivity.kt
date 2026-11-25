package com.example.studentgradetracker

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.app.AlertDialog
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.studentgradetracker.data.database.AppDatabase
import com.example.studentgradetracker.data.entity.Grade

class DisciplineActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private var disciplineId = -1
    private var disciplineName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discipline)

        db = AppDatabase.getDatabase(this)

        disciplineId = intent.getIntExtra("disciplineId", -1)
        disciplineName =
            intent.getStringExtra("disciplineName") ?: "Дисциплина"

        findViewById<TextView>(R.id.disciplineTitle).text =
            disciplineName

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.addGradeButton).setOnClickListener {
            openGradeForm()
        }
    }

    override fun onResume() {
        super.onResume()
        loadGrades()
    }

    private fun openGradeForm(grade: Grade? = null) {

        val intent = Intent(this, GradeFormActivity::class.java)

        intent.putExtra("disciplineId", disciplineId)
        intent.putExtra("disciplineName", disciplineName)

        if (grade != null) {
            intent.putExtra("gradeId", grade.id)
            intent.putExtra("gradeValue", grade.value)
            intent.putExtra("gradeDate", grade.date)
            intent.putExtra("gradeType", grade.workType)
            intent.putExtra("gradeComment", grade.comment)
        }

        startActivity(intent)
    }

    private fun loadGrades() {

        lifecycleScope.launch {

            val grades = db.gradeDao().getByDiscipline(disciplineId)

            val container =
                findViewById<LinearLayout>(R.id.gradeList)

            val emptyText =
                findViewById<TextView>(R.id.emptyGradesText)

            container.removeAllViews()

            emptyText.visibility =
                if (grades.isEmpty()) View.VISIBLE else View.GONE

            grades.forEach { grade ->

                val card = LinearLayout(this@DisciplineActivity)
                card.orientation = LinearLayout.VERTICAL
                card.setPadding(16, 15, 16, 15)
                card.setBackgroundColor(Color.WHITE)

                val value = TextView(this@DisciplineActivity)
                value.text = "${grade.value} (${gradeLabel(grade.value)})"
                value.textSize = 17f
                value.setTextColor(Color.BLACK)
                value.setTypeface(null, android.graphics.Typeface.BOLD)

                val type = TextView(this@DisciplineActivity)
                type.text = grade.workType
                type.textSize = 13f
                type.setTextColor(Color.GRAY)

                val date = TextView(this@DisciplineActivity)
                date.text = grade.date
                date.textSize = 12f
                date.setTextColor(Color.GRAY)

                val comment = TextView(this@DisciplineActivity)
                comment.text = grade.comment
                comment.textSize = 13f
                comment.setTextColor(Color.DKGRAY)

                card.addView(value)
                card.addView(type)
                card.addView(date)

                if (grade.comment.isNotBlank()) {
                    card.addView(comment)
                }

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = 10

                container.addView(card, params)

                // Обычное нажатие — редактирование
                card.setOnClickListener {
                    openGradeForm(grade)
                }

                // Долгое нажатие — удаление
                card.setOnLongClickListener {
                    confirmDelete(grade)
                    true
                }
            }
        }
    }

    private fun gradeLabel(value: Int): String {
        return when (value) {
            5 -> "Отлично"
            4 -> "Хорошо"
            3 -> "Удовлетворительно"
            2 -> "Неудовлетворительно"
            else -> ""
        }
    }

    private fun confirmDelete(grade: Grade) {

        AlertDialog.Builder(this)
            .setTitle("Удаление оценки")
            .setMessage("Удалить оценку ${grade.value}?")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->

                lifecycleScope.launch {
                    db.gradeDao().delete(grade)
                    loadGrades()
                }
            }
            .show()
    }
}