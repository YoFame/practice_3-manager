package com.example.studentgradetracker

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.studentgradetracker.data.database.AppDatabase

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
                String.format("%.1f", average)

            findViewById<TextView>(R.id.countText).text =
                "На основе ${grades.size} оценок"

            // Показываем первые две дисциплины
            val container =
                findViewById<LinearLayout>(R.id.homeDisciplineList)

            container.removeAllViews()

            disciplines.take(2).forEach { discipline ->

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

    private fun createDisciplineCard(
        name: String,
        average: Double
    ): TextView {

        val card = TextView(this)

        card.text = "$name${
            String.format("%.1f", average)
            
            
        }"

        card.textSize = 16f
        card.setTextColor(Color.BLACK)
        card.gravity = Gravity.CENTER_VERTICAL
        card.setPadding(18, 22, 18, 22)
        card.setBackgroundColor(Color.WHITE)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.bottomMargin = 10
        card.layoutParams = params

        return card
    }
}