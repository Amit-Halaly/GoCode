package com.example.gocode.lessons

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gocode.R
import com.example.gocode.lessons.lesson.JavaLessonsRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator

class LessonFlowActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvStepCounter: TextView
    private lateinit var stepProgress: LinearProgressIndicator
    private lateinit var tvLessonTitle: TextView
    private lateinit var tvLessonBody: TextView
    private lateinit var cardCode: View
    private lateinit var tvLessonCode: TextView
    private lateinit var cardTip: View
    private lateinit var tvTipTitle: TextView
    private lateinit var tvTipText: TextView
    private lateinit var btnPrev: MaterialButton
    private lateinit var btnNext: MaterialButton
    private var steps: List<LessonStep> = emptyList()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson_flow)

        btnBack = findViewById(R.id.btnBack)
        tvStepCounter = findViewById(R.id.tvStepCounter)
        stepProgress = findViewById(R.id.stepProgress)

        tvLessonTitle = findViewById(R.id.tvLessonTitle)
        tvLessonBody = findViewById(R.id.tvLessonBody)

        cardCode = findViewById(R.id.cardCode)
        tvLessonCode = findViewById(R.id.tvLessonCode)

        cardTip = findViewById(R.id.cardTip)
        tvTipTitle = findViewById(R.id.tvTipTitle)
        tvTipText = findViewById(R.id.tvTipText)

        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)

        steps = JavaLessonsRepository.getLesson1Steps()

        if (steps.isEmpty()) {
            finish()
            return
        }

        btnBack.setOnClickListener { finish() }

        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                renderStep()
            }
        }

        btnNext.setOnClickListener {
            if (currentIndex < steps.size - 1) {
                currentIndex++
                renderStep()
            } else {
                finish()
            }
        }

        renderStep()
    }

    private fun renderStep() {
        val step = steps[currentIndex]

        tvLessonTitle.text = step.title
        tvLessonBody.text = step.body

        tvStepCounter.text = "${currentIndex + 1}/${steps.size}"

        val percent = ((currentIndex + 1) * 100) / steps.size
        stepProgress.progress = percent

        if (step.code.isNullOrBlank()) {
            cardCode.visibility = View.GONE
        } else {
            cardCode.visibility = View.VISIBLE
            tvLessonCode.text = step.code
        }

        if (step.tip.isNullOrBlank()) {
            cardTip.visibility = View.GONE
        } else {
            cardTip.visibility = View.VISIBLE
            tvTipTitle.text = "Tip from Leo"
            tvTipText.text = step.tip
        }

        btnPrev.isEnabled = currentIndex > 0

        if (currentIndex == steps.size - 1) {
            btnNext.text = "Finish"
        } else {
            btnNext.text = "Next"
        }
    }
}
