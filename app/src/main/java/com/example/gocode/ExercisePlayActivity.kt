package com.example.gocode

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.gocode.editor.GoCodeLanguage
import com.example.gocode.gamification.GamificationRepository
import com.example.gocode.lessons.CCodeExerciseRepository
import com.example.gocode.lessons.CodeExercise
import com.example.gocode.lessons.CppCodeExerciseRepository
import com.example.gocode.lessons.JavaCodeExerciseRepository
import com.example.gocode.lessons.LanguagePathFragment
import com.example.gocode.lessons.LessonProgressStore
import com.example.gocode.lessons.PythonCodeExerciseRepository
import com.airbnb.lottie.LottieAnimationView
import com.example.gocode.network.ApiClient
import com.example.gocode.network.models.hintModels.HintRequest
import com.example.gocode.network.models.lintModels.LintRequest
import com.example.gocode.network.models.runModels.RunRequest
import com.example.gocode.network.models.runModels.RunResponse
import com.example.gocode.network.models.runModels.RunTestCase
import com.google.android.material.button.MaterialButton
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.SymbolInputView
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.getComponent
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula
import io.github.rosemoe.sora.widget.schemes.SchemeEclipse
import io.github.rosemoe.sora.widget.subscribeAlways
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class ExercisePlayActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    private lateinit var editor: CodeEditor
    private lateinit var symbolInput: SymbolInputView
    private lateinit var inputField: EditText

    private lateinit var outputCard: View
    private lateinit var outputTitle: TextView
    private lateinit var outputText: TextView
    private lateinit var statusPill: TextView
    private lateinit var runProgress: ProgressBar

    private lateinit var leoTipGroup: View
    private lateinit var tvTipTitle: TextView
    private lateinit var tvTipText: TextView
    private lateinit var editorTouchOverlay: View

    private lateinit var resultLottie: LottieAnimationView
    private lateinit var backButton: MaterialButton
    private lateinit var runButton: MaterialButton
    private lateinit var themeButton: MaterialButton
    private lateinit var resetButton: MaterialButton
    private lateinit var answerButton: MaterialButton

    private lateinit var introOverlay: View
    private lateinit var introReadyText: TextView
    private lateinit var introTaskText: TextView

    private lateinit var inputPromptOverlay: View
    private lateinit var inputPromptCard: View
    private lateinit var inputPromptTitle: TextView
    private lateinit var inputPromptMessage: TextView
    private lateinit var inputPromptField: EditText
    private lateinit var inputPromptButton: MaterialButton
    private lateinit var inputPromptCancel: MaterialButton

    private var isDarkTheme = true
    private var pendingRunCode: String = ""
    private var pendingInputCount = 0
    private val promptedInputs = mutableListOf<String>()

    private var lintJob: Job? = null
    private var runJob: Job? = null
    private var hintJob: Job? = null

    private var hintLoadedForThisRun = false
    private var hintRequestInFlight = false
    private var lastRun: RunResponse? = null
    private var answerUnlocked = false

    private var nodeId: String = "java_u1_c1"
    private lateinit var currentExercise: CodeExercise

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        isDarkTheme = prefs.getBoolean(KEY_DARK, true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_play)

        nodeId = intent.getStringExtra(LanguagePathFragment.EXTRA_NODE_ID) ?: "java_u1_c1"
        currentExercise = when {
            nodeId.startsWith("cpp_") -> CppCodeExerciseRepository.getExercise(nodeId)
            nodeId.startsWith("c_") -> CCodeExerciseRepository.getExercise(nodeId)
            nodeId.startsWith("py_") -> PythonCodeExerciseRepository.getExercise(nodeId)
            else -> JavaCodeExerciseRepository.getExercise(nodeId)
        }

        findViewById<TextView>(R.id.taskTitle).text = currentExercise.title
        findViewById<TextView>(R.id.taskSubtitle).text = currentExercise.subtitle

        editor = findViewById(R.id.codeEditor)
        symbolInput = findViewById(R.id.symbolInput)
        inputField = findViewById(R.id.inputField)

        outputCard = findViewById(R.id.outputCard)
        outputTitle = findViewById(R.id.outputTitle)
        outputText = findViewById(R.id.outputView)
        statusPill = findViewById(R.id.statusPill)
        runProgress = findViewById(R.id.runProgress)

        leoTipGroup = findViewById(R.id.leoTipGroup)
        tvTipTitle = findViewById(R.id.tvTipTitle)
        tvTipText = findViewById(R.id.tvTipText)
        editorTouchOverlay = findViewById(R.id.editorTouchOverlay)

        resultLottie = findViewById(R.id.resultLottie)
        backButton = findViewById(R.id.backButton)
        runButton = findViewById(R.id.runButton)
        themeButton = findViewById(R.id.themeButton)
        resetButton = findViewById(R.id.resetButton)
        answerButton = findViewById(R.id.answerButton)

        introOverlay = findViewById(R.id.introOverlay)
        introReadyText = findViewById(R.id.introReadyText)
        introTaskText = findViewById(R.id.introTaskText)

        inputPromptOverlay = findViewById(R.id.inputPromptOverlay)
        inputPromptCard = findViewById(R.id.inputPromptCard)
        inputPromptTitle = findViewById(R.id.inputPromptTitle)
        inputPromptMessage = findViewById(R.id.inputPromptMessage)
        inputPromptField = findViewById(R.id.inputPromptField)
        inputPromptButton = findViewById(R.id.inputPromptButton)
        inputPromptCancel = findViewById(R.id.inputPromptCancel)

        setupEditor()
        setupSymbolBar()
        updateThemeButtonIcon()

        val savedCode = savedInstanceState?.getString(STATE_CODE) ?: prefs.getString(codeKey(), null)
        val savedInput = savedInstanceState?.getString(STATE_INPUT) ?: prefs.getString(inputKey(), "") ?: ""
        answerUnlocked = prefs.getBoolean(answerUnlockedKey(), false)

        editor.setText(savedCode ?: currentExercise.template)
        inputField.setText(savedInput.ifBlank { currentExercise.defaultInput })
        updateAnswerButton()

        hideLeoHard()
        hideLottie(immediate = true)
        setStatus(Status.READY)
        renderReadyState()

        setupHideLeoOnEditorTouch()

        runButton.setOnClickListener { runSolution() }
        backButton.setOnClickListener { finish() }
        resetButton.setOnClickListener { resetExercise() }
        themeButton.setOnClickListener { toggleTheme() }
        leoTipGroup.setOnClickListener { requestHint() }
        answerButton.setOnClickListener { showUnlockAnswerDialog() }
        inputPromptButton.setOnClickListener { submitPromptedInput() }
        inputPromptCancel.setOnClickListener { cancelPromptedRun() }

        editor.subscribeAlways<ContentChangeEvent> {
            setStatus(Status.READY)
            scheduleLint()
        }
        scheduleLint()

        if (savedInstanceState == null) {
            introOverlay.post { showIntroOverlay() }
        }
    }

    private fun setupSymbolBar() {
        symbolInput.bindEditor(editor)

        val display = arrayOf("TAB", "{", "}", "(", ")", "[", "]", ";")
        val insert = arrayOf("\t", "{", "}", "(", ")", "[", "]", ";")

        symbolInput.removeSymbols()
        symbolInput.addSymbols(display, insert)
        symbolInput.visibility = View.GONE
        symbolInput.elevation = 24f
        symbolInput.translationZ = 24f

        observeKeyboardForSymbolBar()
    }

    private fun observeKeyboardForSymbolBar() {
        val root = findViewById<View>(android.R.id.content)
        root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            root.getWindowVisibleDisplayFrame(rect)

            val screenHeight = root.rootView.height
            val keyboardHeight = screenHeight - rect.bottom
            val keyboardOpen = keyboardHeight > screenHeight * 0.15

            symbolInput.visibility = if (keyboardOpen) View.VISIBLE else View.GONE
            if (keyboardOpen) {
                symbolInput.bringToFront()
                symbolInput.post { alignSymbolBarToKeyboard(rect.bottom) }
                if (leoTipGroup.visibility == View.VISIBLE) hideLeoHard()
            } else {
                symbolInput.translationY = 0f
            }
        }
    }

    private fun alignSymbolBarToKeyboard(keyboardTopOnScreen: Int) {
        symbolInput.translationY = 0f
        val location = IntArray(2)
        symbolInput.getLocationOnScreen(location)

        val symbolBottomOnScreen = location[1] + symbolInput.height
        symbolInput.translationY = (keyboardTopOnScreen - symbolBottomOnScreen).toFloat()
    }

    override fun onResume() {
        super.onResume()
        hideLeoHard()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_CODE, editor.text.toString())
        outState.putString(STATE_INPUT, inputField.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        persistDraft()
    }

    override fun onDestroy() {
        super.onDestroy()
        lintJob?.cancel()
        runJob?.cancel()
        hintJob?.cancel()
        runCatching { editor.release() }
    }

    private fun setupEditor() {
        editor.setEditorLanguage(GoCodeLanguage(currentExercise.language))
        editor.setTextSize(12f)
        editor.isLineNumberEnabled = true
        editor.isHighlightCurrentLine = true
        editor.isUndoEnabled = true
        editor.isWordwrap = true
        editor.getComponent<EditorAutoCompletion>().setEnabledAnimation(true)

        editor.typefaceText = runCatching {
            Typeface.createFromAsset(assets, "JetBrainsMono-Regular.ttf")
        }.getOrElse { Typeface.MONOSPACE }

        applyThemeToEditor()
        editor.diagnostics = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupHideLeoOnEditorTouch() {
        editorTouchOverlay.visibility = View.GONE
        editorTouchOverlay.setOnTouchListener { v, _ ->
            if (leoTipGroup.visibility == View.VISIBLE) hideLeoHard()
            v.performClick()
            false
        }
        inputField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && leoTipGroup.visibility == View.VISIBLE) hideLeoHard()
        }
    }

    private fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        prefs.edit { putBoolean(KEY_DARK, isDarkTheme) }
        updateThemeButtonIcon()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }

    private fun updateThemeButtonIcon() {
        val icon = if (isDarkTheme) R.drawable.ic_sun else R.drawable.ic_moon
        themeButton.icon = ContextCompat.getDrawable(this, icon)
    }

    private fun applyThemeToEditor() {
        editor.colorScheme = if (isDarkTheme) SchemeDarcula() else SchemeEclipse()
        editor.invalidate()
    }

    private fun persistDraft() {
        prefs.edit {
            putString(codeKey(), editor.text.toString())
            putString(inputKey(), inputField.text.toString())
        }
    }

    private fun setBusy(busy: Boolean) {
        runButton.isEnabled = !busy
        backButton.isEnabled = !busy
        resetButton.isEnabled = !busy
        themeButton.isEnabled = !busy
        answerButton.isEnabled = !busy
        leoTipGroup.isEnabled = !busy
        runProgress.visibility = if (busy) View.VISIBLE else View.GONE
        runButton.text = if (busy) "Running" else "Run"
    }

    private fun resetExercise() {
        lintJob?.cancel()
        runJob?.cancel()
        hintJob?.cancel()
        lastRun = null
        hintLoadedForThisRun = false
        hintRequestInFlight = false

        editor.setText(currentExercise.template)
        inputField.setText(currentExercise.defaultInput)

        renderReadyState()
        clearDiagnostics()
        hideLottie(immediate = true)
        hideLeoHard()
        setBusy(false)
        setStatus(Status.READY)
        persistDraft()
        scheduleLint()
    }

    private fun runSolution() {
        runJob?.cancel()
        hintJob?.cancel()
        hideLeoHard()
        hideLottie(immediate = true)
        hintLoadedForThisRun = false
        hintRequestInFlight = false

        setStatus(Status.RUNNING)
        outputTitle.text = "Running"
        outputText.text = "Checking your solution behind the scenes..."
        setBusy(true)

        pendingRunCode = editor.text.toString()
        pendingInputCount = detectInputReadCount(pendingRunCode)
        promptedInputs.clear()
        persistDraft()
        if (pendingInputCount > 0) {
            showInputPrompt(step = 0)
        } else {
            executeRun(
                code = pendingRunCode,
                fallbackInput = currentExercise.defaultInput,
                fallbackExpectedOutput = currentExercise.tests.first().expectedOutput,
                tests = currentExercise.tests
            )
        }
    }

    private fun executeRun(
        code: String,
        fallbackInput: String,
        fallbackExpectedOutput: String,
        tests: List<RunTestCase>
    ) {
        runJob = lifecycleScope.launch {
            val res = runCatching {
                ApiClient.execApi.run(
                    RunRequest(
                        language = currentExercise.language,
                        code = code,
                        input = fallbackInput,
                        expectedOutput = fallbackExpectedOutput,
                        compareMode = currentExercise.compareMode,
                        testCases = tests
                    )
                )
            }.getOrElse { null }

            if (res == null) {
                lastRun = null
                setStatus(Status.FAILED)
                outputTitle.text = "Error"
                outputText.text = "Server error"
                showFailThenLeo()
                setBusy(false)
                return@launch
            }

            lastRun = res
            renderRunResult(res)

            val runtimeError = (res.exitCode != 0) || res.error.trim().isNotEmpty()
            val passed = (res.passed == true) && !runtimeError

            setStatus(if (passed) Status.PASSED else Status.FAILED)
            if (passed) {
                LessonProgressStore.saveProgress(this@ExercisePlayActivity, nodeId, 100)
                if (!answerUnlocked) {
                    GamificationRepository.awardNodeCompleted(this@ExercisePlayActivity, nodeId)
                }
                playResultAnimation(pass = true) { finish() }
            } else if (isCServerMismatch(res)) {
                hideLeoHard()
            } else {
                showFailThenLeo()
            }
            setBusy(false)
        }
    }

    private fun renderRunResult(res: RunResponse?) {
        if (res == null) {
            renderReadyState()
            return
        }

        val out = res.output.trim()
        val err = res.error.trim()
        val hasError = (res.exitCode != 0) || err.isNotBlank()
        val passed = res.passed == true && !hasError
        outputTitle.text = if (passed) "Great work" else "Needs work"
        outputText.text = when {
            isCServerMismatch(res) -> "The app sent this as C, but the execution server handled it as Java. Deploy the latest server build with C support, then run again."
            hasError -> err.ifBlank { "Runtime error" }
            passed -> "Your solution passed the checks."
            out.isNotEmpty() -> "Your output:\n$out"
            else -> "Your program finished without output."
        }
    }

    private fun renderReadyState() {
        outputTitle.text = "Console"
        outputText.text = "Press Run. If the program needs input, GoCode will ask for it step by step."
    }

    private fun showIntroOverlay() {
        introTaskText.text = currentExercise.title
        introOverlay.visibility = View.VISIBLE
        introOverlay.alpha = 0f
        introReadyText.scaleX = 0.72f
        introReadyText.scaleY = 0.72f
        introTaskText.alpha = 0f
        introTaskText.translationY = 26f

        introOverlay.animate()
            .alpha(1f)
            .setDuration(180)
            .withEndAction {
                introReadyText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(OvershootInterpolator())
                    .setDuration(360)
                    .withEndAction {
                        introTaskText.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(260)
                            .withEndAction {
                                introOverlay.postDelayed({ hideIntroOverlay() }, 650L)
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun hideIntroOverlay() {
        introOverlay.animate()
            .alpha(0f)
            .translationY(-36f)
            .setDuration(260)
            .withEndAction {
                introOverlay.visibility = View.GONE
                introOverlay.translationY = 0f
            }
            .start()
    }

    private fun showInputPrompt(step: Int) {
        inputPromptTitle.text = "Input ${step + 1} of $pendingInputCount"
        inputPromptMessage.text = "Enter value ${step + 1} for the next input read in your code."
        inputPromptField.setText("")
        inputPromptField.error = null
        inputPromptOverlay.visibility = View.VISIBLE
        inputPromptOverlay.alpha = 0f
        inputPromptCard.scaleX = 0.94f
        inputPromptCard.scaleY = 0.94f

        inputPromptOverlay.animate().alpha(1f).setDuration(150).start()
        inputPromptCard.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(OvershootInterpolator())
            .setDuration(220)
            .start()
        inputPromptField.requestFocus()
    }

    private fun submitPromptedInput() {
        val value = inputPromptField.text.toString()
        if (value.isEmpty()) {
            inputPromptField.error = "Enter a value"
            return
        }

        promptedInputs += value
        if (promptedInputs.size < pendingInputCount) {
            showInputPrompt(step = promptedInputs.size)
            return
        }

        inputPromptOverlay.animate()
            .alpha(0f)
            .setDuration(130)
            .withEndAction {
                inputPromptOverlay.visibility = View.GONE
                val inputText = promptedInputs.joinToString(separator = " ")
                inputField.setText(inputText)
                executeRun(
                    code = pendingRunCode,
                    fallbackInput = "$inputText\n",
                    fallbackExpectedOutput = currentExercise.tests.first().expectedOutput,
                    tests = currentExercise.tests
                )
            }
            .start()
    }

    private fun cancelPromptedRun() {
        inputPromptOverlay.visibility = View.GONE
        pendingRunCode = ""
        promptedInputs.clear()
        setBusy(false)
        setStatus(Status.READY)
        renderReadyState()
    }

    private fun detectInputReadCount(code: String): Int {
        val pythonInputReads = Regex("""\binput\s*\(""").findAll(code).count()
        val scannerReads = Regex(
            """\.\s*next(?:Int|Long|Double|Float|Short|Byte|Line|Boolean|BigInteger|BigDecimal)?\s*\("""
        ).findAll(code).count()
        val bufferedReaderReads = Regex("""\.\s*readLine\s*\(""").findAll(code).count()
        val streamReads = Regex("""(?:System\s*\.\s*in|[\w.]+)\s*\.\s*read\s*\(""").findAll(code).count()
        val scanfReads = Regex("""\bscanf\s*\(""").findAll(code).count()
        val cinReads = Regex("""\bcin\s*>>""").findAll(code).count()
        val passwordReads = Regex("""\.\s*readPassword\s*\(""").findAll(code).count()
        val dataInputReads = Regex(
            """\.\s*read(?:Int|Long|Double|Float|Short|Byte|Boolean|Char|UTF|Fully)\s*\("""
        ).findAll(code).count()
        return pythonInputReads + scannerReads + bufferedReaderReads + streamReads + scanfReads + cinReads + passwordReads + dataInputReads
    }

    private fun showUnlockAnswerDialog() {
        if (answerUnlocked) {
            showUnlockedAnswer()
            return
        }

        val dialog = Dialog(this)
        val content = layoutInflater.inflate(R.layout.dialog_unlock_answer, null)
        dialog.setContentView(content)

        content.findViewById<MaterialButton>(R.id.unlockNoButton).setOnClickListener {
            dialog.dismiss()
        }
        content.findViewById<MaterialButton>(R.id.unlockYesButton).setOnClickListener {
            dialog.dismiss()
            unlockAnswer()
        }
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun unlockAnswer() {
        answerButton.isEnabled = false
        GamificationRepository.spendCoins(this, ANSWER_UNLOCK_COST.toLong()) { success ->
            answerButton.isEnabled = true
            if (success) {
                answerUnlocked = true
                prefs.edit { putBoolean(answerUnlockedKey(), true) }
                updateAnswerButton()
                showUnlockedAnswer()
            } else {
                showNotEnoughCoinsDialog()
            }
        }
    }

    private fun showNotEnoughCoinsDialog() {
        val dialog = Dialog(this)
        val content = layoutInflater.inflate(R.layout.dialog_not_enough_coins, null)
        dialog.setContentView(content)

        content.findViewById<MaterialButton>(R.id.notEnoughCoinsOkButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showUnlockedAnswer() {
        editor.setText(currentExercise.answer)
        persistDraft()
        scheduleLint()
        outputTitle.text = "Unlocked answer"
        outputText.text = currentExercise.answer
        shakeOutputCard()
    }

    private fun updateAnswerButton() {
        answerButton.text = if (answerUnlocked) "Answer" else "Unlock"
    }

    private fun shakeOutputCard() {
        outputCard.animate()
            .translationY(-8f)
            .setDuration(110)
            .withEndAction {
                outputCard.animate().translationY(0f).setDuration(110).start()
            }
            .start()
    }

    private fun setStatus(status: Status) {
        val (label, background) = when (status) {
            Status.READY -> "Ready" to R.drawable.bg_status_idle
            Status.RUNNING -> "Running tests" to R.drawable.bg_status_running
            Status.PASSED -> "All tests passed" to R.drawable.bg_status_pass
            Status.FAILED -> "Needs work" to R.drawable.bg_status_fail
        }
        statusPill.text = label
        statusPill.setBackgroundResource(background)
        statusPill.animate()
            .scaleX(1.04f)
            .scaleY(1.04f)
            .setDuration(110)
            .withEndAction {
                statusPill.animate().scaleX(1f).scaleY(1f).setDuration(110).start()
            }
            .start()
    }

    private fun showFailThenLeo() = playResultAnimation(pass = false) { showLeoPrompt() }

    private fun showLeoPrompt() {
        tvTipTitle.text = "AI hint"
        tvTipText.text = "Tap for a focused clue."

        leoTipGroup.visibility = View.VISIBLE
        leoTipGroup.alpha = 0f
        leoTipGroup.translationY = 42f
        editorTouchOverlay.visibility = View.VISIBLE

        leoTipGroup.animate()
            .alpha(1f)
            .translationY(0f)
            .setInterpolator(OvershootInterpolator())
            .setDuration(240)
            .start()
    }

    private fun hideLeoHard() {
        leoTipGroup.animate().cancel()
        leoTipGroup.visibility = View.GONE
        leoTipGroup.alpha = 0f
        leoTipGroup.translationY = 0f
        hintLoadedForThisRun = false
        hintRequestInFlight = false
        editorTouchOverlay.visibility = View.GONE
    }

    private fun requestHint() {
        val res = lastRun ?: return
        if (isCServerMismatch(res)) return
        val failed = (res.passed == false) || (res.exitCode != 0) ||
                res.error.trim().isNotEmpty() || res.output.trim().isEmpty()
        if (!failed || hintRequestInFlight || hintLoadedForThisRun) return

        hintRequestInFlight = true
        tvTipTitle.text = "AI hint"
        tvTipText.text = "Thinking..."

        hintJob?.cancel()
        hintJob = lifecycleScope.launch {
            runCatching {
                ApiClient.execApi.hint(
                    HintRequest(
                        task = "${currentExercise.title}. ${currentExercise.subtitle}",
                        language = currentExercise.language,
                        code = editor.text.toString(),
                        input = inputField.text.toString(),
                        output = res.summary ?: res.output,
                        error = res.error,
                        exitCode = res.exitCode,
                        passed = res.passed,
                        expectedOutput = res.expectedOutput,
                        actualOutput = res.actualOutput,
                        compareMode = currentExercise.compareMode
                    )
                )
            }.onSuccess { hintRes ->
                hintLoadedForThisRun = true
                tvTipText.text = hintRes.hint
            }.onFailure {
                hintLoadedForThisRun = true
                tvTipText.text = "Check how your values are combined."
            }
            hintRequestInFlight = false
        }
    }

    private fun playResultAnimation(pass: Boolean, onDone: () -> Unit) {
        val anim = if (pass) R.raw.lottie_pass else R.raw.lottie_fail
        runCatching { resultLottie.removeAllAnimatorListeners() }
        runCatching { resultLottie.cancelAnimation() }

        resultLottie.visibility = View.VISIBLE
        resultLottie.bringToFront()
        resultLottie.translationZ = 100f
        resultLottie.setAnimation(anim)
        resultLottie.repeatCount = 0
        resultLottie.progress = 0f

        resultLottie.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                hideLottie(immediate = true)
                onDone()
            }
        })
        resultLottie.playAnimation()
    }

    private fun hideLottie(immediate: Boolean = false) {
        runCatching { resultLottie.removeAllAnimatorListeners() }
        runCatching { resultLottie.cancelAnimation() }
        if (immediate) {
            resultLottie.visibility = View.GONE
            resultLottie.alpha = 1f
            return
        }
        if (resultLottie.visibility != View.VISIBLE) return
        resultLottie.animate().alpha(0f).setDuration(120)
            .withEndAction {
                resultLottie.visibility = View.GONE
                resultLottie.alpha = 1f
            }.start()
    }

    private fun scheduleLint() {
        lintJob?.cancel()
        lintJob = lifecycleScope.launch {
            delay(LINT_DEBOUNCE_MS)
            runLint()
        }
    }

    private suspend fun runLint() {
        runCatching {
            ApiClient.execApi.lint(
                LintRequest(language = currentExercise.language, code = editor.text.toString())
            )
        }.onSuccess { res ->
            val first = res.errors.firstOrNull() ?: run {
                clearDiagnostics()
                return
            }
            applyLineDiagnostic(findLineRegion((first.line - 1).coerceAtLeast(0)))
        }.onFailure { clearDiagnostics() }
    }

    private fun isCServerMismatch(res: RunResponse): Boolean {
        return currentExercise.language == "c" && res.error.contains("Main.java")
    }

    private fun applyLineDiagnostic(region: Pair<Int, Int>?) {
        if (region == null) {
            clearDiagnostics()
            return
        }
        val (start, end) = region
        if (end <= start) {
            clearDiagnostics()
            return
        }
        editor.diagnostics = DiagnosticsContainer().apply {
            addDiagnostic(DiagnosticRegion(start, end, DiagnosticRegion.SEVERITY_ERROR))
        }
        editor.invalidate()
    }

    private fun findLineRegion(lineZeroBased: Int): Pair<Int, Int>? {
        val text = editor.text
        if (text.lineCount <= 0) return null
        val line = lineZeroBased.coerceIn(0, text.lineCount - 1)
        var len = text.getColumnCount(line).coerceAtLeast(1)
        var start = text.getCharIndex(line, 0)
        for (c in text.getLineString(line)) {
            if (c == ' ' || c == '\t') {
                start++
                len--
            } else {
                break
            }
        }
        if (len <= 0) return null
        return start to (start + len)
    }

    private fun clearDiagnostics() {
        editor.diagnostics = null
        editor.invalidate()
    }

    private fun codeKey(): String = "${KEY_CODE}_$nodeId"

    private fun inputKey(): String = "${KEY_INPUT}_$nodeId"

    private fun answerUnlockedKey(): String = "${KEY_ANSWER_UNLOCKED}_$nodeId"

    private enum class Status {
        READY,
        RUNNING,
        PASSED,
        FAILED
    }

    private companion object {
        private const val PREFS_NAME = "goCode_prefs"
        private const val KEY_CODE = "exercise_code"
        private const val KEY_INPUT = "exercise_input"
        private const val KEY_ANSWER_UNLOCKED = "exercise_answer_unlocked"
        private const val KEY_DARK = "exercise_dark"
        private const val STATE_CODE = "state_code"
        private const val STATE_INPUT = "state_input"
        private const val LINT_DEBOUNCE_MS = 850L
        private const val ANSWER_UNLOCK_COST = 50
    }
}
