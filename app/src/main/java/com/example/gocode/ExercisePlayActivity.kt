package com.example.gocode

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.gocode.network.ApiClient
import com.example.gocode.network.models.hintModels.HintRequest
import com.example.gocode.network.models.lintModels.LintRequest
import com.example.gocode.network.models.runModels.RunRequest
import com.example.gocode.network.models.runModels.RunResponse
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

    private lateinit var leoTipGroup: View
    private lateinit var tvTipTitle: TextView
    private lateinit var tvTipText: TextView

    private lateinit var resultLottie: LottieAnimationView

    private lateinit var runButton: MaterialButton
    private lateinit var themeButton: MaterialButton
    private lateinit var resetButton: MaterialButton

    private var isDarkTheme = false

    private var lintJob: Job? = null
    private var runJob: Job? = null
    private var hintJob: Job? = null

    private var lastRun: RunResponse? = null
    private var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    // TODO: Replace with real task payload
    private val taskTitleText = "Print Hello World"
    private val expectedOutput = "Hello World"

    override fun onCreate(savedInstanceState: Bundle?) {
        isDarkTheme = prefs.getBoolean(KEY_DARK, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_play)

        findViewById<TextView>(R.id.taskTitle).text = taskTitleText

        editor = findViewById(R.id.codeEditor)
        symbolInput = findViewById(R.id.symbolInput)
        inputField = findViewById(R.id.inputField)

        outputCard = findViewById(R.id.outputCard)
        outputTitle = findViewById(R.id.outputTitle)
        outputText = findViewById(R.id.outputView)

        leoTipGroup = findViewById(R.id.leoTipGroup)
        tvTipTitle = findViewById(R.id.tvTipTitle)
        tvTipText = findViewById(R.id.tvTipText)

        resultLottie = findViewById(R.id.resultLottie)

        runButton = findViewById(R.id.runButton)
        themeButton = findViewById(R.id.themeButton)
        resetButton = findViewById(R.id.resetButton)

        setupEditor()
        setupSymbolBar()

        val restoredCode = savedInstanceState?.getString(STATE_CODE)
        val restoredInput = savedInstanceState?.getString(STATE_INPUT)
        val savedCode = prefs.getString(KEY_CODE, null)
        val savedInput = prefs.getString(KEY_INPUT, "") ?: ""

        editor.setText(restoredCode ?: savedCode ?: defaultTemplate())
        inputField.setText(restoredInput ?: savedInput)

        outputCard.visibility = View.GONE
        hideLeo(immediate = true)
        hideLottie(immediate = true)

        runButton.setOnClickListener { runSolution() }
        resetButton.setOnClickListener { resetExercise() }
        themeButton.setOnClickListener { toggleTheme() }
        leoTipGroup.setOnClickListener { requestHint() }

        editor.subscribeAlways<ContentChangeEvent> { scheduleLint() }
        scheduleLint()
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

        val root = findViewById<View>(android.R.id.content)
        keyboardListener?.let { root.viewTreeObserver.removeOnGlobalLayoutListener(it) }
        keyboardListener = null

        runCatching { editor.release() }
    }

    private fun setupEditor() {
        editor.setEditorLanguage(JavaLanguage())
        editor.setTextSize(14f)
        editor.isLineNumberEnabled = true
        editor.isHighlightCurrentLine = true
        editor.isUndoEnabled = true
        editor.isWordwrap = true
        editor.getComponent<EditorAutoCompletion>().setEnabledAnimation(true)

        editor.typefaceText = runCatching {
            Typeface.createFromAsset(assets, "JetBrainsMono-Regular.ttf")
        }.getOrElse {
            Typeface.MONOSPACE
        }

        applyThemeToEditor()
        editor.diagnostics = null
    }

    private fun setupSymbolBar() {
        symbolInput.bindEditor(editor)

        val display = arrayOf(
            "Tab", "{", "}", "(", ")", "[", "]",
            ";", "\"", "=", "==", "!=", "<", ">",
            "+", "-", "*", "/", "&&", "||",
            "System.out.println()", "return"
        )
        val insert = arrayOf(
            "\t", "{", "}", "(", ")", "[", "]",
            ";", "\"", "=", "==", "!=", "<", ">",
            "+", "-", "*", "/", "&&", "||",
            "System.out.println()", "return "
        )

        symbolInput.removeSymbols()
        symbolInput.addSymbols(display, insert)

        observeKeyboard(symbolInput)
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
        leoTipGroup.isEnabled = !busy
    }

    private fun resetExercise() {
        lintJob?.cancel()
        runJob?.cancel()
        hintJob?.cancel()

        lastRun = null

        editor.setText(defaultTemplate())
        inputField.setText("")

        outputCard.visibility = View.GONE
        outputTitle.text = "Output"
        outputText.text = "—"

        clearDiagnostics()
        hideLottie(immediate = true)
        hideLeo(immediate = true)

        setBusy(false)
        persistDraft()
        scheduleLint()
    }

    private fun runSolution() {
        runJob?.cancel()
        hintJob?.cancel()

        hideLeo(immediate = true)
        hideLottie(immediate = true)

        outputCard.visibility = View.VISIBLE
        outputTitle.text = "Output"
        outputText.text = "Checking..."

        setBusy(true)

        val code = editor.text.toString()
        val input = inputField.text.toString()
        persistDraft()

        runJob = lifecycleScope.launch {
            val res = runCatching {
                ApiClient.execApi.run(
                    RunRequest(
                        language = "java",
                        code = code,
                        input = input,
                        expectedOutput = expectedOutput,
                        compareMode = "normalize"
                    )
                )
            }.getOrElse { null }

            if (res == null) {
                lastRun = null
                outputTitle.text = "Error"
                outputText.text = "Server error"
                showFailThenLeo()
                setBusy(false)
                return@launch
            }

            lastRun = res
            renderRunResult(res)

            val failedByRuntime = (res.exitCode != 0) || res.error.trim().isNotEmpty()
            val failedByWrongOutput = (res.passed == false)
            val failedByNoOutput = res.output.trim().isEmpty() && !failedByRuntime

            val passed = (res.passed == true) && !failedByRuntime

            if (passed) {
                playResultAnimation(pass = true) {
                }
            } else {
                if (failedByRuntime || failedByWrongOutput || failedByNoOutput) {
                    showFailThenLeo()
                } else {
                    showFailThenLeo()
                }
            }

            setBusy(false)
        }
    }

    private fun showFailThenLeo() {
        playResultAnimation(pass = false) {
            showLeoPrompt()
        }
    }

    private fun renderRunResult(res: RunResponse) {
        val out = res.output.trim()
        val err = res.error.trim()
        val hasError = (res.exitCode != 0) || err.isNotBlank()

        when {
            hasError -> {
                outputTitle.text = "Error"
                outputText.text = if (err.isNotBlank()) err else "Runtime error"
            }
            out.isNotEmpty() -> {
                outputTitle.text = "Output"
                outputText.text = out
            }
            else -> {
                outputTitle.text = "Output"
                outputText.text = "(no output)"
            }
        }
    }

    private fun showLeoPrompt() {
        leoTipGroup.bringToFront()
        leoTipGroup.translationZ = 50f

        tvTipTitle.text = "Tip from Leo"
        tvTipText.text = "For a hint, tap Leo"

        leoTipGroup.visibility = View.VISIBLE
        leoTipGroup.alpha = 0f
        leoTipGroup.translationY = 60f

        leoTipGroup.animate()
            .alpha(1f)
            .translationY(0f)
            .setInterpolator(OvershootInterpolator())
            .setDuration(240)
            .start()
    }

    private fun hideLeo(immediate: Boolean = false) {
        leoTipGroup.animate().cancel()

        if (immediate) {
            leoTipGroup.visibility = View.GONE
            leoTipGroup.alpha = 0f
            leoTipGroup.translationY = 0f
            return
        }

        if (leoTipGroup.visibility != View.VISIBLE) return
        leoTipGroup.animate()
            .alpha(0f)
            .translationY(40f)
            .setDuration(160)
            .withEndAction {
                leoTipGroup.visibility = View.GONE
                leoTipGroup.translationY = 0f
            }
            .start()
    }

    private fun requestHint() {
        val res = lastRun ?: return

        val failed = (res.passed == false) || (res.exitCode != 0) || res.error.trim().isNotEmpty() || res.output.trim().isEmpty()
        if (!failed) return

        hintJob?.cancel()
        tvTipTitle.text = "Tip from Leo"
        tvTipText.text = "Thinking..."

        val code = editor.text.toString()
        val input = inputField.text.toString()

        hintJob = lifecycleScope.launch {
            runCatching {
                ApiClient.execApi.hint(
                    HintRequest(
                        task = taskTitleText,
                        language = "java",
                        code = code,
                        input = input,
                        output = res.output,
                        error = res.error,
                        exitCode = res.exitCode,
                        passed = res.passed,
                        expectedOutput = res.expectedOutput,
                        actualOutput = res.actualOutput,
                        compareMode = "normalize"
                    )
                )
            }.onSuccess { hintRes ->
                tvTipText.text = hintRes.hint
            }.onFailure {
                tvTipText.text = "Try checking your output and prints."
            }
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
        resultLottie.animate()
            .alpha(0f)
            .setDuration(120)
            .withEndAction {
                resultLottie.visibility = View.GONE
                resultLottie.alpha = 1f
            }
            .start()
    }

    private fun scheduleLint() {
        lintJob?.cancel()
        lintJob = lifecycleScope.launch {
            delay(LINT_DEBOUNCE_MS)
            runLint()
        }
    }

    private suspend fun runLint() {
        val code = editor.text.toString()

        runCatching {
            ApiClient.execApi.lint(LintRequest(code = code))
        }.onSuccess { res ->
            val first = res.errors.firstOrNull()
            if (first == null) {
                clearDiagnostics()
                return
            }

            val lineZeroBased = (first.line - 1).coerceAtLeast(0)
            applyLineDiagnostic(findLineRegion(lineZeroBased))
        }.onFailure {
            clearDiagnostics()
        }
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

        val container = DiagnosticsContainer().apply {
            addDiagnostic(DiagnosticRegion(start, end, DiagnosticRegion.SEVERITY_ERROR))
        }

        editor.diagnostics = container
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

    private fun observeKeyboard(symbolBar: View) {
        val root = findViewById<View>(android.R.id.content)

        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            root.getWindowVisibleDisplayFrame(rect)

            val screenHeight = root.rootView.height
            val keyboardHeight = screenHeight - rect.bottom
            val keyboardOpen = keyboardHeight > screenHeight * 0.15f

            symbolBar.visibility = if (keyboardOpen) View.VISIBLE else View.GONE
        }

        keyboardListener = listener
        root.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    private fun defaultTemplate() = """
        public class Main {
            public static void main(String[] args) {
                System.out.println("Hello World");
            }
        }
    """.trimIndent()

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