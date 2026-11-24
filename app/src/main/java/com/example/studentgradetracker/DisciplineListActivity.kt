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

class DisciplineListActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discipline_list)

        db = AppDatabase.getDatabase(this)
    }

    override fun onResume() {
        super.onResume()
        loadDisciplines()
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
                if (disciplines.isEmpty()) android.view.View.VISIBLE
                else android.view.View.GONE

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