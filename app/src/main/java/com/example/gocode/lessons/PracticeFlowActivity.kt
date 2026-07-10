package com.example.gocode.lessons

import android.content.ClipData
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gocode.AchievementBottomSheet
import com.example.gocode.R
import com.example.gocode.firebase.FirebaseContentRepository
import com.example.gocode.gamification.GamificationRepository
import com.example.gocode.gamification.GamificationResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlin.math.abs

class PracticeFlowActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvQuestionCounter: TextView
    private lateinit var practiceProgress: LinearProgressIndicator
    private lateinit var tvPracticeTitle: TextView
    private lateinit var tvPracticeQuestion: TextView
    private lateinit var cardPracticeCode: MaterialCardView
    private lateinit var tvPracticeCode: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var fillBlankContainer: MaterialCardView
    private lateinit var quizResultCard: MaterialCardView
    private lateinit var etFillAnswer: EditText
    private lateinit var dragBlankContainer: LinearLayout
    private lateinit var inlineCodeContainer: LinearLayout
    private lateinit var dragOptionsContainer: LinearLayout
    private lateinit var tvFeedback: TextView
    private lateinit var tvQuizScore: TextView
    private lateinit var tvQuizResultTitle: TextView
    private lateinit var tvQuizResultBody: TextView
    private lateinit var btnCheckOrNext: MaterialButton

    private var questions: List<PracticeQuestion> = emptyList()
    private var currentIndex = 0
    private var answered = false
    private var selectedCorrect = false
    private var selectedDragAnswer: String? = null
    private var nodeId: String = "java_u1_p1"
    private var correctCount = 0
    private var showingQuizResult = false
    private val inlineBlankViews = mutableListOf<TextView>()
    private val selectedDragAnswers = mutableListOf<String?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice_flow)

        btnBack = findViewById(R.id.btnBack)
        tvQuestionCounter = findViewById(R.id.tvQuestionCounter)
        practiceProgress = findViewById(R.id.practiceProgress)
        tvPracticeTitle = findViewById(R.id.tvPracticeTitle)
        tvPracticeQuestion = findViewById(R.id.tvPracticeQuestion)
        cardPracticeCode = findViewById(R.id.cardPracticeCode)
        tvPracticeCode = findViewById(R.id.tvPracticeCode)
        optionsContainer = findViewById(R.id.optionsContainer)
        fillBlankContainer = findViewById(R.id.fillBlankContainer)
        quizResultCard = findViewById(R.id.quizResultCard)
        etFillAnswer = findViewById(R.id.etFillAnswer)
        dragBlankContainer = findViewById(R.id.dragBlankContainer)
        inlineCodeContainer = findViewById(R.id.inlineCodeContainer)
        dragOptionsContainer = findViewById(R.id.dragOptionsContainer)
        tvFeedback = findViewById(R.id.tvFeedback)
        tvQuizScore = findViewById(R.id.tvQuizScore)
        tvQuizResultTitle = findViewById(R.id.tvQuizResultTitle)
        tvQuizResultBody = findViewById(R.id.tvQuizResultBody)
        btnCheckOrNext = findViewById(R.id.btnCheckOrNext)

        nodeId = intent.getStringExtra(LanguagePathFragment.EXTRA_NODE_ID) ?: "java_u1_p1"
        questions = when {
            nodeId.startsWith("cpp_") -> CppPracticeRepository.getQuestions(nodeId)
            nodeId.startsWith("cs_") -> CSharpPracticeRepository.getQuestions(nodeId)
            nodeId.startsWith("c_") -> CPracticeRepository.getQuestions(nodeId)
            nodeId.startsWith("py_") -> PythonPracticeRepository.getQuestions(nodeId)
            else -> JavaPracticeRepository.getQuestions(nodeId)
        }

        if (questions.isEmpty()) {
            finish()
            return
        }

        FirebaseContentRepository.getQuestions(nodeId, languageForNode()) { remoteQuestions ->
            if (remoteQuestions.isNotEmpty()) {
                questions = remoteQuestions
                currentIndex = 0
                correctCount = 0
                renderQuestion()
            }
        }

        btnBack.setOnClickListener { finish() }

        btnCheckOrNext.setOnClickListener {
            if (!answered) {
                when (questions[currentIndex].type) {
                    PracticeQuestionType.FILL_BLANK -> checkFillBlankAnswer()
                    PracticeQuestionType.DRAG_FILL_BLANK -> checkDragBlankAnswer()
                    PracticeQuestionType.MULTIPLE_CHOICE -> Unit
                }
            } else {
                goNext()
            }
        }

        renderQuestion()
    }

    private fun renderQuestion() {
        val question = questions[currentIndex]

        answered = false
        selectedCorrect = false
        selectedDragAnswer = null
        showingQuizResult = false

        tvQuestionCounter.text = "${currentIndex + 1}/${questions.size}"
        val progressPercent = ((currentIndex + 1) * 100) / questions.size
        practiceProgress.progress = progressPercent
        LessonProgressStore.saveProgress(this, nodeId, progressPercent)

        tvPracticeTitle.text = question.title
        tvPracticeQuestion.text = question.question
        tvPracticeQuestion.visibility = View.VISIBLE
        quizResultCard.visibility = View.GONE

        hideFeedback()

        btnCheckOrNext.text = "Check"
        btnCheckOrNext.isEnabled = question.type != PracticeQuestionType.MULTIPLE_CHOICE

        renderCode(question)
        renderQuestionType(question)
    }

    private fun renderCode(question: PracticeQuestion) {
        inlineCodeContainer.removeAllViews()
        inlineCodeContainer.visibility = View.GONE
        tvPracticeCode.visibility = View.VISIBLE

        if (question.code.isNullOrBlank()) {
            cardPracticeCode.visibility = View.GONE
        } else {
            cardPracticeCode.visibility = View.VISIBLE
            if (question.type == PracticeQuestionType.DRAG_FILL_BLANK) {
                tvPracticeCode.visibility = View.GONE
                inlineCodeContainer.visibility = View.VISIBLE
                renderInlineCodeBlanks(question)
            } else {
                tvPracticeCode.text = question.code
            }
        }
    }

    private fun renderQuestionType(question: PracticeQuestion) {
        optionsContainer.removeAllViews()
        dragOptionsContainer.removeAllViews()

        optionsContainer.visibility = View.GONE
        fillBlankContainer.visibility = View.GONE
        dragBlankContainer.visibility = View.GONE

        when (question.type) {
            PracticeQuestionType.MULTIPLE_CHOICE -> {
                optionsContainer.visibility = View.VISIBLE
                renderOptions(question)
            }

            PracticeQuestionType.FILL_BLANK -> {
                fillBlankContainer.visibility = View.VISIBLE
                etFillAnswer.setText("")
                resetFillBlankStyle()
            }

            PracticeQuestionType.DRAG_FILL_BLANK -> {
                dragBlankContainer.visibility = View.VISIBLE
                renderDragBlank(question)
            }
        }
    }

    private fun renderOptions(question: PracticeQuestion) {
        question.options.forEach { option ->
            val button = MaterialButton(this).apply {
                text = option
                isAllCaps = false
                textSize = 15f
                minHeight = dp(56)
                gravity = Gravity.CENTER_VERTICAL
                cornerRadius = dp(18)
                insetTop = 0
                insetBottom = 0
                setPadding(dp(18), 0, dp(18), 0)
                setTextColor(getColor(R.color.gc_text_primary))
                backgroundTintList = ColorStateList.valueOf(getColor(R.color.btn_primary))
                strokeWidth = dp(1)
                strokeColor = ColorStateList.valueOf(argb("#4DA3FF", 0.45f))

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(58)
                ).apply {
                    bottomMargin = dp(10)
                }

                setOnClickListener {
                    if (!answered) {
                        checkMultipleChoiceAnswer(this, option, question)
                    }
                }
            }

            optionsContainer.addView(button)
        }
    }

    private fun renderDragBlank(question: PracticeQuestion) {
        btnCheckOrNext.isEnabled = inlineBlankViews.isEmpty()

        question.options.forEach { option ->
            var downX = 0f
            var downY = 0f
            var dragStarted = false
            val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

            val chip = TextView(this).apply {
                text = option
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
                textSize = 14f
                minWidth = dp(132)
                setPadding(dp(16), 0, dp(16), 0)
                setTextColor(getColor(R.color.gc_text_primary))
                background = roundedDrawable(
                    fillColor = getColor(R.color.btn_primary),
                    strokeColor = argb("#FFFFFF", 0.24f),
                    radius = dp(18)
                )
                elevation = dp(3).toFloat()

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(52)
                ).apply {
                    marginEnd = dp(10)
                }

                setOnTouchListener { view, event ->
                    if (answered) return@setOnTouchListener false

                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = event.rawX
                            downY = event.rawY
                            dragStarted = false
                            view.isPressed = true
                            true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val dx = event.rawX - downX
                            val dy = event.rawY - downY
                            val isVerticalDrag = abs(dy) > touchSlop && abs(dy) > abs(dx)

                            if (isVerticalDrag && !dragStarted) {
                                dragStarted = true
                                view.isPressed = false
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                                dragBlankContainer.parent?.requestDisallowInterceptTouchEvent(true)
                                val clip = ClipData.newPlainText("answer", option)
                                view.startDragAndDrop(clip, View.DragShadowBuilder(view), null, 0)
                                true
                            } else {
                                false
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            view.isPressed = false
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                            dragBlankContainer.parent?.requestDisallowInterceptTouchEvent(false)

                            val dx = abs(event.rawX - downX)
                            val dy = abs(event.rawY - downY)
                            if (!dragStarted && dx < touchSlop && dy < touchSlop) {
                                view.performClick()
                            }
                            true
                        }

                        MotionEvent.ACTION_CANCEL -> {
                            view.isPressed = false
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                            dragBlankContainer.parent?.requestDisallowInterceptTouchEvent(false)
                            false
                        }

                        else -> false
                    }
                }

                setOnClickListener {
                    if (!answered) {
                        placeAnswerInFirstOpenBlank(option)
                    }
                }
            }

            dragOptionsContainer.addView(chip)
        }
    }

    private fun checkMultipleChoiceAnswer(
        selectedButton: MaterialButton,
        selectedAnswer: String,
        question: PracticeQuestion
    ) {
        answered = true
        selectedCorrect = selectedAnswer == question.correctAnswer

        for (i in 0 until optionsContainer.childCount) {
            val btn = optionsContainer.getChildAt(i) as MaterialButton
            val isCorrect = btn.text.toString() == question.correctAnswer

            when {
                isCorrect -> styleAnswerButton(btn, AnswerState.CORRECT)
                btn == selectedButton && !selectedCorrect -> styleAnswerButton(btn, AnswerState.WRONG)
                else -> styleAnswerButton(btn, AnswerState.NEUTRAL)
            }

            btn.isClickable = false
        }

        showFeedback(selectedCorrect, question.explanation)
        recordAnswerResult()
        setAnsweredButtonState()
    }

    private fun checkFillBlankAnswer() {
        val question = questions[currentIndex]
        val answer = etFillAnswer.text.toString().trim()

        if (answer.isBlank()) return

        answered = true
        selectedCorrect = answer.equals(question.correctAnswer, ignoreCase = true)

        fillBlankContainer.setStrokeColor(
            getColor(if (selectedCorrect) R.color.practice_correct else R.color.practice_wrong)
        )
        fillBlankContainer.setCardBackgroundColor(
            argb(if (selectedCorrect) "#22C55E" else "#EF4444", 0.14f)
        )

        etFillAnswer.isEnabled = false

        showFeedback(selectedCorrect, question.explanation)
        recordAnswerResult()
        setAnsweredButtonState()
    }

    private fun checkDragBlankAnswer() {
        val question = questions[currentIndex]
        if (selectedDragAnswers.any { it.isNullOrBlank() }) return

        answered = true
        val expectedAnswers = question.expectedDragAnswers()
        selectedCorrect = selectedDragAnswers == expectedAnswers

        inlineBlankViews.forEachIndexed { index, blankView ->
            val isBlankCorrect = selectedDragAnswers.getOrNull(index) == expectedAnswers.getOrNull(index)
            styleDropTarget(blankView, if (isBlankCorrect) DropState.CORRECT else DropState.WRONG)
        }
        markDragOptions(expectedAnswers)
        showFeedback(selectedCorrect, question.explanation)
        recordAnswerResult()
        setAnsweredButtonState()
    }

    private fun markDragOptions(correctAnswers: List<String>) {
        for (i in 0 until dragOptionsContainer.childCount) {
            val chip = dragOptionsContainer.getChildAt(i) as TextView
            val chipAnswer = chip.text.toString()
            val isCorrect = chipAnswer in correctAnswers
            val isSelectedWrong = chipAnswer in selectedDragAnswers.filterNotNull() && !isCorrect

            chip.background = when {
                isCorrect -> roundedDrawable(
                    fillColor = argb("#22C55E", 0.22f),
                    strokeColor = getColor(R.color.practice_correct),
                    radius = dp(18)
                )
                isSelectedWrong -> roundedDrawable(
                    fillColor = argb("#EF4444", 0.2f),
                    strokeColor = getColor(R.color.practice_wrong),
                    radius = dp(18)
                )
                else -> roundedDrawable(
                    fillColor = argb("#0167ED", 0.42f),
                    strokeColor = argb("#FFFFFF", 0.18f),
                    radius = dp(18)
                )
            }
            chip.isClickable = false
            chip.setOnLongClickListener(null)
        }
    }

    private fun showFeedback(correct: Boolean, explanation: String) {
        tvFeedback.visibility = View.VISIBLE
        tvFeedback.text = if (correct) {
            "Correct\n$explanation"
        } else {
            "Not quite\n$explanation"
        }

        val color = getColor(if (correct) R.color.practice_correct else R.color.practice_wrong)
        val fill = argb(if (correct) "#22C55E" else "#EF4444", 0.14f)
        tvFeedback.setTextColor(getColor(R.color.gc_text_primary))
        tvFeedback.background = roundedDrawable(fill, color, dp(18))
    }

    private fun hideFeedback() {
        tvFeedback.visibility = View.GONE
        tvFeedback.text = ""
        tvFeedback.background = null
    }

    private fun goNext() {
        if (showingQuizResult) {
            finish()
            return
        }

        if (currentIndex < questions.size - 1) {
            currentIndex++
            etFillAnswer.isEnabled = true
            renderQuestion()
        } else {
            LessonProgressStore.saveProgress(this, nodeId, 100)
            if (isSummaryQuiz()) {
                awardCompletion()
                showQuizResult()
            } else {
                awardCompletionAndFinish()
            }
        }
    }

    private fun recordAnswerResult() {
        if (selectedCorrect) {
            correctCount++
        }
    }

    private fun showQuizResult() {
        showingQuizResult = true
        val score = ((correctCount * 100f) / questions.size).toInt()
        val shouldContinue = score >= 75

        tvQuestionCounter.text = "${correctCount}/${questions.size}"
        practiceProgress.progress = 100
        tvPracticeTitle.text = "Section ${sectionNumberForNode()} complete"
        tvPracticeQuestion.visibility = View.GONE
        cardPracticeCode.visibility = View.GONE
        optionsContainer.visibility = View.GONE
        fillBlankContainer.visibility = View.GONE
        dragBlankContainer.visibility = View.GONE
        hideFeedback()

        quizResultCard.visibility = View.VISIBLE
        tvQuizScore.text = "$score%"
        tvQuizResultTitle.text = if (shouldContinue) {
            "Leo says: keep going"
        } else {
            "Leo says: review first"
        }
        tvQuizResultBody.text = if (shouldContinue) {
            "Nice work. You understand enough to move into the next section. Leo still wants you to practice this section again later so the basics stay sharp."
        } else {
            if (languageForNode() == "python") {
                "You are close, but Leo recommends repeating this section before moving on. Focus on print(), indentation, variables, and the core Python pattern."
            } else if (languageForNode() == "c") {
                "You are close, but Leo recommends repeating this section before moving on. Focus on main(), printf, semicolons, and the core C pattern."
            } else if (languageForNode() == "cpp") {
                "You are close, but Leo recommends repeating this section before moving on. Focus on main(), cout, semicolons, and the core C++ pattern."
            } else if (languageForNode() == "csharp") {
                "You are close, but Leo recommends repeating this section before moving on. Focus on Main(), WriteLine, semicolons, and the core C# pattern."
            } else {
                "You are close, but Leo recommends repeating this section before moving on. Focus on main(), println, and choosing the right variable type."
            }
        }

        btnCheckOrNext.text = "Finish"
        btnCheckOrNext.isEnabled = true
    }

    private fun isSummaryQuiz(): Boolean {
        return Regex("""^(java|py|c|cpp|cs)_u(10|[1-9])_q1$""").matches(nodeId)
    }

    private fun languageForNode(): String {
        return when {
            nodeId.startsWith("cpp_") -> "cpp"
            nodeId.startsWith("cs_") -> "csharp"
            nodeId.startsWith("c_") -> "c"
            nodeId.startsWith("py_") -> "python"
            else -> "java"
        }
    }

    private fun sectionNumberForNode(): Int {
        return Regex("""_u(\d+)_""").find(nodeId)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
    }

    private fun awardCompletion() {
        GamificationRepository.awardNodeCompleted(this, nodeId) { result ->
            result?.let { showReward(it) }
        }
    }

    private fun awardCompletionAndFinish() {
        GamificationRepository.awardNodeCompleted(this, nodeId) { result ->
            if (result == null) {
                finish()
            } else {
                showReward(result) { finish() }
            }
        }
    }

    private fun showReward(result: GamificationResult, onContinue: (() -> Unit)? = null) {
        AchievementBottomSheet.newRewardInstance(result).apply {
            this.onContinue = onContinue
        }.show(supportFragmentManager, "reward_sheet")
    }

    private fun resetFillBlankStyle() {
        etFillAnswer.isEnabled = true
        fillBlankContainer.setStrokeColor(getColor(R.color.pb_bg))
        fillBlankContainer.setCardBackgroundColor(getColor(R.color.lesson_code_box))
    }

    private fun renderInlineCodeBlanks(question: PracticeQuestion) {
        inlineBlankViews.clear()
        selectedDragAnswers.clear()

        val codeLines = question.code.orEmpty().lines()
        codeLines.forEach { line ->
            val lineContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(6)
                }
            }

            val parts = line.split(BLANK_TOKEN)
            parts.forEachIndexed { partIndex, part ->
                if (part.isNotEmpty()) {
                    lineContainer.addView(codeTextView(part))
                }

                if (partIndex < parts.lastIndex) {
                    val blankIndex = inlineBlankViews.size
                    selectedDragAnswers.add(null)
                    val blankView = createInlineBlankView(blankIndex)
                    inlineBlankViews.add(blankView)
                    lineContainer.addView(blankView)
                }
            }

            inlineCodeContainer.addView(lineContainer)
        }
    }

    private fun codeTextView(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            typeface = Typeface.MONOSPACE
            textSize = 14f
            includeFontPadding = false
            setTextColor(getColor(R.color.gc_text_primary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(36)
            )
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun createInlineBlankView(blankIndex: Int): TextView {
        return TextView(this).apply {
            text = "Drop here"
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            textSize = 13f
            minWidth = dp(128)
            setPadding(dp(12), 0, dp(12), 0)
            setTextColor(getColor(R.color.gc_text_secondary))
            styleDropTarget(this, DropState.EMPTY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(40)
            ).apply {
                leftMargin = dp(4)
                rightMargin = dp(4)
            }

            setOnDragListener { view, event ->
                val target = view as TextView
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> true
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        if (!answered) styleDropTarget(target, DropState.HOVER)
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        if (!answered) {
                            val state = if (selectedDragAnswers[blankIndex] == null) {
                                DropState.EMPTY
                            } else {
                                DropState.SELECTED
                            }
                            styleDropTarget(target, state)
                        }
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        if (!answered) {
                            val draggedAnswer = event.clipData?.getItemAt(0)?.text?.toString()
                            if (!draggedAnswer.isNullOrBlank()) {
                                placeAnswerInBlank(blankIndex, draggedAnswer)
                            }
                        }
                        true
                    }
                    else -> true
                }
            }
        }
    }

    private fun placeAnswerInFirstOpenBlank(answer: String) {
        val blankIndex = selectedDragAnswers.indexOfFirst { it == null }.takeIf { it >= 0 } ?: 0
        placeAnswerInBlank(blankIndex, answer)
    }

    private fun placeAnswerInBlank(blankIndex: Int, answer: String) {
        selectedDragAnswer = answer
        selectedDragAnswers[blankIndex] = answer
        inlineBlankViews[blankIndex].apply {
            text = answer
            setTextColor(getColor(R.color.gc_text_primary))
            styleDropTarget(this, DropState.SELECTED)
        }
        btnCheckOrNext.isEnabled = selectedDragAnswers.none { it.isNullOrBlank() }
    }

    private fun styleDropTarget(target: TextView, state: DropState) {
        val (fillColor, strokeColor) = when (state) {
            DropState.EMPTY -> getColor(R.color.lesson_code_box) to getColor(R.color.pb_bg)
            DropState.HOVER -> argb("#4DA3FF", 0.18f) to getColor(R.color.titles_primary)
            DropState.SELECTED -> argb("#0167ED", 0.2f) to getColor(R.color.btn_primary)
            DropState.CORRECT -> argb("#22C55E", 0.18f) to getColor(R.color.practice_correct)
            DropState.WRONG -> argb("#EF4444", 0.18f) to getColor(R.color.practice_wrong)
        }

        target.background = roundedDrawable(fillColor, strokeColor, dp(14))
    }

    private fun styleAnswerButton(button: MaterialButton, state: AnswerState) {
        val (fillColor, strokeColor) = when (state) {
            AnswerState.CORRECT -> argb("#22C55E", 0.2f) to getColor(R.color.practice_correct)
            AnswerState.WRONG -> argb("#EF4444", 0.2f) to getColor(R.color.practice_wrong)
            AnswerState.NEUTRAL -> argb("#0167ED", 0.36f) to argb("#FFFFFF", 0.16f)
        }

        button.backgroundTintList = ColorStateList.valueOf(fillColor)
        button.strokeColor = ColorStateList.valueOf(strokeColor)
        button.strokeWidth = dp(1)
    }

    private fun setAnsweredButtonState() {
        btnCheckOrNext.text = if (currentIndex == questions.size - 1) "Finish" else "Next"
        btnCheckOrNext.isEnabled = true
    }

    private fun roundedDrawable(fillColor: Int, strokeColor: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(fillColor)
            setStroke(dp(1), strokeColor)
        }
    }

    private fun argb(hexColor: String, alpha: Float): Int {
        val rgb = hexColor.removePrefix("#").toInt(16)
        val alphaValue = (alpha.coerceIn(0f, 1f) * 255).toInt()
        return (alphaValue shl 24) or rgb
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun PracticeQuestion.expectedDragAnswers(): List<String> {
        return correctAnswers.ifEmpty { listOf(correctAnswer) }
    }

    private enum class AnswerState {
        CORRECT,
        WRONG,
        NEUTRAL
    }

    private enum class DropState {
        EMPTY,
        HOVER,
        SELECTED,
        CORRECT,
        WRONG
    }

    private companion object {
        const val BLANK_TOKEN = "______"
    }
}
