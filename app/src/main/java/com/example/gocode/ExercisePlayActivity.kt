package com.example.gocode

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
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
import io.github.rosemoe.sora.langs.java.JavaLanguage
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
    private val promptedInputs = mutableListOf<Int>()

    private var lintJob: Job? = null
    private var runJob: Job? = null
    private var hintJob: Job? = null

    private var hintLoadedForThisRun = false
    private var hintRequestInFlight = false
    private var lastRun: RunResponse? = null

    private val taskTitleText = "Add two numbers"
    private val taskSubtitleText = "Read two integers and print their sum."
    private val exerciseNeedsInput = true

    private val exerciseTests = listOf(
        RunTestCase(name = "Warm up", input = "1 2\n", expectedOutput = "3"),
        RunTestCase(name = "Negative values", input = "10 -4\n", expectedOutput = "6"),
        RunTestCase(name = "Hidden check", input = "41 1\n", expectedOutput = "42", hidden = true),
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        isDarkTheme = prefs.getBoolean(KEY_DARK, true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_play)

        findViewById<TextView>(R.id.taskTitle).text = taskTitleText
        findViewById<TextView>(R.id.taskSubtitle).text = taskSubtitleText

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

        val savedCode = savedInstanceState?.getString(STATE_CODE) ?: prefs.getString(KEY_CODE, null)
        val savedInput = savedInstanceState?.getString(STATE_INPUT) ?: prefs.getString(KEY_INPUT, "") ?: ""

        editor.setText(savedCode ?: defaultTemplate())
        inputField.setText(savedInput)

        hideLeoHard()
        hideLottie(immediate = true)
        setStatus(Status.READY)
        renderReadyState()

        setupHideLeoOnEditorTouch()

        runButton.setOnClickListener { runSolution() }
        resetButton.setOnClickListener { resetExercise() }
        themeButton.setOnClickListener { toggleTheme() }
        leoTipGroup.setOnClickListener { requestHint() }
        answerButton.setOnClickListener { showLockedAnswerHint() }
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            symbolInput.visibility = if (imeVisible) View.VISIBLE else View.GONE
            if (imeVisible) symbolInput.bringToFront()
            if (imeVisible && leoTipGroup.visibility == View.VISIBLE) hideLeoHard()
            insets
        }
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
        editor.setEditorLanguage(JavaLanguage())
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
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }

    private fun applyThemeToEditor() {
        editor.colorScheme = if (isDarkTheme) SchemeDarcula() else SchemeEclipse()
        editor.invalidate()
    }

    private fun persistDraft() {
        prefs.edit {
            putString(KEY_CODE, editor.text.toString())
            putString(KEY_INPUT, inputField.text.toString())
        }
    }

    private fun setBusy(busy: Boolean) {
        runButton.isEnabled = !busy
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

        editor.setText(defaultTemplate())
        inputField.setText("")

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
        promptedInputs.clear()
        persistDraft()
        if (exerciseNeedsInput) {
            showInputPrompt(step = 0)
        } else {
            executeRun(
                code = pendingRunCode,
                fallbackInput = "",
                fallbackExpectedOutput = exerciseTests.first().expectedOutput,
                tests = exerciseTests
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
                        language = "java",
                        code = code,
                        input = fallbackInput,
                        expectedOutput = fallbackExpectedOutput,
                        compareMode = "trim",
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
            if (passed) playResultAnimation(pass = true) { } else showFailThenLeo()
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
        introTaskText.text = taskTitleText
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
        val labels = listOf("first", "second")
        inputPromptTitle.text = "Input ${step + 1} of 2"
        inputPromptMessage.text = "Enter the ${labels[step]} number your program will read."
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
        val value = inputPromptField.text.toString().trim().toIntOrNull()
        if (value == null) {
            inputPromptField.error = "Enter a whole number"
            return
        }

        promptedInputs += value
        if (promptedInputs.size < 2) {
            showInputPrompt(step = promptedInputs.size)
            return
        }

        inputPromptOverlay.animate()
            .alpha(0f)
            .setDuration(130)
            .withEndAction {
                inputPromptOverlay.visibility = View.GONE
                val first = promptedInputs[0]
                val second = promptedInputs[1]
                inputField.setText("$first $second")
                executeRun(
                    code = pendingRunCode,
                    fallbackInput = "$first $second\n",
                    fallbackExpectedOutput = (first + second).toString(),
                    tests = buildRunTests(first, second)
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

    private fun buildRunTests(first: Int, second: Int): List<RunTestCase> {
        val learnerTest = RunTestCase(
            name = "Your input",
            input = "$first $second\n",
            expectedOutput = (first + second).toString()
        )
        return listOf(learnerTest) + exerciseTests.drop(1)
    }

    private fun showLockedAnswerHint() {
        outputTitle.text = "Answer locked"
        outputText.text = "The full answer is locked for a future unlock flow."
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
                        task = "$taskTitleText. $taskSubtitleText",
                        language = "java",
                        code = editor.text.toString(),
                        input = inputField.text.toString(),
                        output = res.summary ?: res.output,
                        error = res.error,
                        exitCode = res.exitCode,
                        passed = res.passed,
                        expectedOutput = res.expectedOutput,
                        actualOutput = res.actualOutput,
                        compareMode = "trim"
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
            ApiClient.execApi.lint(LintRequest(code = editor.text.toString()))
        }.onSuccess { res ->
            val first = res.errors.firstOrNull() ?: run {
                clearDiagnostics()
                return
            }
            applyLineDiagnostic(findLineRegion((first.line - 1).coerceAtLeast(0)))
        }.onFailure { clearDiagnostics() }
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

    private fun defaultTemplate() = """
        import java.util.Scanner;

        public class Main {
            public static void main(String[] args) {
                Scanner scanner = new Scanner(System.in);
                int first = scanner.nextInt();
                int second = scanner.nextInt();

                System.out.println(first + second);
            }
        }
    """.trimIndent()

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
        private const val KEY_DARK = "exercise_dark"
        private const val STATE_CODE = "state_code"
        private const val STATE_INPUT = "state_input"
        private const val LINT_DEBOUNCE_MS = 850L
    }
}
