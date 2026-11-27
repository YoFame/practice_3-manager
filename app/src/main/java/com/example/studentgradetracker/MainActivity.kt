package com.example.studentgradetracker

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.studentgradetracker.data.database.AppDatabase
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)

        findViewById<Button>(R.id.openDisciplinesButton)
            .setOnClickListener {
                startActivity(
                    Intent(this, DisciplineListActivity::class.java)
                )
            }
    }

    override fun onResume() {
        super.onResume()
        loadHomeData()
    }

    private fun loadHomeData() {
        lifecycleScope.launch {

            val disciplines = db.disciplineDao().getAll()
            val grades = db.gradeDao().getAll()

            // Рассчитываем общий средний балл
            val average = if (grades.isNotEmpty()) {
                grades.map { it.value }.average()
            } else {
                0.0
            }

            findViewById<TextView>(R.id.averageText).text =
                String.format(Locale.getDefault(), "%.1f", average)

            findViewById<TextView>(R.id.countText).text =
                "На основе ${grades.size} оценок"

            // Показываем дисциплины
            val container =
                findViewById<LinearLayout>(R.id.homeDisciplineList)

            container.removeAllViews()

            disciplines.take(disciplines.size).forEach { discipline ->

                val subjectGrades = grades.filter {
                    it.disciplineId == discipline.id
                }

                val subjectAverage = if (subjectGrades.isNotEmpty()) {
                    subjectGrades.map { it.value }.average()
                } else {
                    0.0
                }

                val card = createDisciplineCard(
                    discipline.name,
                    subjectAverage
                )

                card.setOnClickListener {
                    val intent = Intent(
                        this@MainActivity,
                        DisciplineActivity::class.java
                    )

                    intent.putExtra("disciplineId", discipline.id)
                    intent.putExtra("disciplineName", discipline.name)

                    startActivity(intent)
                }

                container.addView(card)
            }
        }
    }

    // ОБНОВЛЕННАЯ ФУНКЦИЯ: Теперь название слева, оценка справа
    private fun createDisciplineCard(
        name: String,
        average: Double


    ): LinearLayout {

        // 1. Создаем главный контейнер (строка)
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(24, 32, 24, 32) // Внутренние отступы карточки
        row.setBackgroundColor(Color.WHITE) // Белый фон

        // 2. Название дисциплины (слева)
        val nameText = TextView(this)
        nameText.text = name
        nameText.textSize = 16f
        nameText.setTextColor(Color.BLACK)
        // weight=1 заставляет этот текст занять всё свободное место, прижимая оценку вправо
        val nameParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        nameText.layoutParams = nameParams

        // 3. Оценка (справа)
        val averageText = TextView(this)
        averageText.text = String.format(Locale.getDefault(), "%.1f", average)
        averageText.textSize = 20f
        averageText.setTextColor(Color.parseColor("#000000"))
        averageText.setTypeface(null, Typeface.NORMAL)

        // Добавляем тексты в строку
        row.addView(nameText)
        row.addView(averageText)

        // 4. Задаем отступы для самой карточки (чтобы не прилипали друг к другу)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 12
        row.layoutParams = params

        return row
    }
}