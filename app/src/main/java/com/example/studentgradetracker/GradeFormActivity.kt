package com.example.studentgradetracker

import android.os.Bundle
import android.widget.*
import android.app.DatePickerDialog
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

import com.example.studentgradetracker.data.database.AppDatabase
import com.example.studentgradetracker.data.entity.Grade

class GradeFormActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    private var gradeId = -1
    private var selectedDisciplineId = -1

    private lateinit var disciplineSpinner: Spinner
    private lateinit var gradeInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var workTypeSpinner: Spinner
    private lateinit var commentInput: EditText

    private val workTypes = listOf(
        "Контрольная",
        "Практическая",
        "Лабораторная",
        "Экзамен",
        "Зачёт",
        "Ответ на занятии"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grade_form)

        db = AppDatabase.getDatabase(this)

        gradeId = intent.getIntExtra("gradeId", -1)
        selectedDisciplineId =
            intent.getIntExtra("disciplineId", -1)

        disciplineSpinner = findViewById(R.id.disciplineSpinner)
        gradeInput = findViewById(R.id.gradeInput)
        dateInput = findViewById(R.id.dateInput)
        workTypeSpinner = findViewById(R.id.workTypeSpinner)
        commentInput = findViewById(R.id.commentInput)

        setupWorkTypes()
        setupDatePicker()
        loadDisciplines()

        findViewById<Button>(R.id.saveGradeButton)
            .setOnClickListener {
                saveGrade()
            }
    }

    private fun setupWorkTypes() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            workTypes
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        workTypeSpinner.adapter = adapter
    }

    private fun setupDatePicker() {
        dateInput.setOnClickListener {

            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    dateInput.setText(
                        String.format(
                            Locale.getDefault(),
                            "%02d.%02d.%04d",
                            day,
                            month + 1,
                            year
                        )
                    )

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun loadDisciplines() {
        lifecycleScope.launch {

            val disciplines = db.disciplineDao().getAll()

            if (disciplines.isEmpty()) {
                Toast.makeText(
                    this@GradeFormActivity,
                    "Сначала добавьте дисциплину",
                    Toast.LENGTH_LONG
                ).show()

                finish()
                return@launch
            }

            val adapter = ArrayAdapter(
                this@GradeFormActivity,
                android.R.layout.simple_spinner_item,
                disciplines.map { it.name }
            )

            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            disciplineSpinner.adapter = adapter

            val position = disciplines.indexOfFirst {
                it.id == selectedDisciplineId
            }

            if (position >= 0) {
                disciplineSpinner.setSelection(position)
            }

            // Заполняем форму при редактировании
            if (gradeId != -1) {

                gradeInput.setText(
                    intent.getIntExtra("gradeValue", 0).toString()
                )

                dateInput.setText(
                    intent.getStringExtra("gradeDate") ?: ""
                )

                commentInput.setText(
                    intent.getStringExtra("gradeComment") ?: ""
                )

                val type =
                    intent.getStringExtra("gradeType") ?: ""

                val typePosition = workTypes.indexOf(type)

                if (typePosition >= 0) {
                    workTypeSpinner.setSelection(typePosition)
                }
            }
        }
    }

    private fun saveGrade() {

        val value = gradeInput.text.toString().toIntOrNull()
        val date = dateInput.text.toString().trim()
        val comment = commentInput.text.toString().trim()

        if (value == null || value !in 2..5) {
            gradeInput.error = "Введите оценку от 2 до 5"
            return
        }

        if (date.isBlank()) {
            dateInput.error = "Выберите дату"
            return
        }

        lifecycleScope.launch {

            val disciplines = db.disciplineDao().getAll()
            val position = disciplineSpinner.selectedItemPosition

            val disciplineId =
                disciplines.getOrNull(position)?.id ?: -1

            if (disciplineId == -1) {
                Toast.makeText(
                    this@GradeFormActivity,
                    "Выберите дисциплину",
                    Toast.LENGTH_SHORT
                ).show()

                return@launch
            }

            val workType =
                workTypeSpinner.selectedItem.toString()

            if (gradeId == -1) {

                db.gradeDao().insert(
                    Grade(
                        disciplineId = disciplineId,
                        value = value,
                        date = date,
                        workType = workType,
                        comment = comment
                    )
                )

                Toast.makeText(
                    this@GradeFormActivity,
                    "Оценка добавлена",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                db.gradeDao().update(
                    Grade(
                        id = gradeId,
                        disciplineId = disciplineId,
                        value = value,
                        date = date,
                        workType = workType,
                        comment = comment
                    )
                )

                Toast.makeText(
                    this@GradeFormActivity,
                    "Оценка изменена",
                    Toast.LENGTH_SHORT
                ).show()
            }

            finish()
        }
    }
}