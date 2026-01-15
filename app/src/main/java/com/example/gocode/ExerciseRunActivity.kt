package com.example.gocode

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.gocode.network.ApiClient
import com.example.gocode.network.models.lintModels.LintRequest
import com.example.gocode.network.models.runModels.RunRequest
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.getComponent
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula
import io.github.rosemoe.sora.widget.schemes.SchemeEclipse
import io.github.rosemoe.sora.widget.subscribeAlways
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExerciseRunActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    private lateinit var editor: CodeEditor
    private lateinit var inputField: EditText
    private lateinit var lintStatus: TextView
    private lateinit var outputView: TextView

    private lateinit var runButton: Button
    private lateinit var themeButton: Button
    private lateinit var clearButton: Button

    private var isDarkTheme: Boolean = true
    private var lintJob: Job? = null
    private var runJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_run)

        editor = findViewById(R.id.codeEditor)
        inputField = findViewById(R.id.inputField)
        lintStatus = findViewById(R.id.lintStatus)
        outputView = findViewById(R.id.outputView)

        runButton = findViewById(R.id.runButton)
        themeButton = findViewById(R.id.themeButton)
        clearButton = findViewById(R.id.clearButton)

        val symbolInput = findViewById<io.github.rosemoe.sora.widget.SymbolInputView>(R.id.symbolInput)
        symbolInput.bindEditor(editor)
        symbolInput.addSymbols(
            arrayOf("&&", "{", "}", "(", ")", "||", "!", ";" ),
            arrayOf("&&", "{}", "}", "()", ")", "||", "!", ";")
        )
        observeKeyboard(symbolInput)


        val savedCode = prefs.getString(KEY_CODE, null)
        val savedInput = prefs.getString(KEY_INPUT, "") ?: ""
        isDarkTheme = prefs.getBoolean(KEY_DARK, true)

        setupEditor()
        applyTheme(isDarkTheme)

        editor.setText(savedCode ?: defaultJavaTemplate())
        inputField.setText(savedInput)

        editor.subscribeAlways<ContentChangeEvent> {
            scheduleLint()
        }

        themeButton.setOnClickListener {
            isDarkTheme = !isDarkTheme
            prefs.edit { putBoolean(KEY_DARK, isDarkTheme) }
            applyTheme(isDarkTheme)
        }

        clearButton.setOnClickListener {
            editor.setText(defaultJavaTemplate())
            inputField.setText("")
            lintStatus.text = "—"
            outputView.text = "—"
            clearDiagnostics()
            persistDraft()
        }

        runButton.setOnClickListener {
            runCode()
        }

        scheduleLint()
    }

    override fun onPause() {
        super.onPause()
        persistDraft()
    }

    override fun onDestroy() {
        super.onDestroy()
        lintJob?.cancel()
        runJob?.cancel()
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

        runCatching {
            val typeface = Typeface.createFromAsset(assets, "JetBrainsMono-Regular.ttf")
            editor.typefaceText = typeface
        }
    }

    private fun applyTheme(dark: Boolean) {
        editor.colorScheme = if (dark) SchemeDarcula() else SchemeEclipse()
        editor.invalidate()
    }

    private fun persistDraft() {
        prefs.edit {
            putString(KEY_CODE, editor.text.toString())
            putString(KEY_INPUT, inputField.text.toString())
        }
    }

    private fun scheduleLint() {
        lintJob?.cancel()
        lintJob = lifecycleScope.launch {
            delay(LINT_DEBOUNCE_MS)
            runLint()
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun runLint() {
        val code = editor.text.toString()
        runCatching {
            ApiClient.execApi.lint(LintRequest(code = code))
        }.onSuccess { res ->
            val first = res.errors.firstOrNull()
            if (first == null) {
                lintStatus.text = "Lint: OK ✅"
                clearDiagnostics()
                return
            }

            val lineZeroBased = (first.line - 1).coerceAtLeast(0)
            val msg = first.message
            lintStatus.text = "Lint: line ${lineZeroBased + 1} — $msg"
            applyLineDiagnostic(findErrorIndex(lineZeroBased))
        }.onFailure {
            lintStatus.text = "Lint: —"
        }
    }

    private fun applyLineDiagnostic(result: Pair<Int, Int>?) {
        if (result == null) {
            editor.diagnostics = null
            editor.invalidate()
            return
        }
        val (start, len) = result
        val region = DiagnosticRegion(
            start,
            len,
            DiagnosticRegion.SEVERITY_ERROR
        )

        val container = DiagnosticsContainer().apply {
            addDiagnostic(region)
        }

        editor.diagnostics = container
        editor.invalidate()
    }

    private fun findErrorIndex(lineZeroBased: Int):Pair<Int, Int>? {
        val text = editor.text
        val line = lineZeroBased.coerceIn(0, (text.lineCount - 1).coerceAtLeast(0))
        var len = text.getColumnCount(line).coerceAtLeast(1)
        var start = text.getCharIndex(line, 0)
        for (c in text.getLineString(line)){
            if(c == ' ' || c == '\t'){
                start++
                len--
            }
            else
                break
        }
        return Pair(start, start + len)
    }

    private fun clearDiagnostics() {
        editor.diagnostics = null
        editor.invalidate()
    }

    @SuppressLint("SetTextI18n")
    private fun runCode() {
        runJob?.cancel()
        runJob = lifecycleScope.launch {
            val code = editor.text.toString()
            val input = inputField.text.toString()

            persistDraft()

            runButton.isEnabled = false
            outputView.text = "Running..."

            runCatching {
                ApiClient.execApi.run(
                    RunRequest(
                        language = "java",
                        code = code,
                        input = input
                    )
                )
            }.onSuccess { res ->
                outputView.text = buildString {
                    appendLine("exitCode: ${res.exitCode}")
                    appendLine()
                    appendLine("output:")
                    appendLine(res.output)
                    appendLine()
                    appendLine("error:")
                    appendLine(res.error)
                }
            }.onFailure { e ->
                outputView.text = "Request failed: ${e.message}"
            }

            runButton.isEnabled = true
        }
    }

    private fun observeKeyboard(symbolInput: View) {
        val root = findViewById<View>(android.R.id.content)

        root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            root.getWindowVisibleDisplayFrame(rect)

            val screenHeight = root.rootView.height
            val keyboardHeight = screenHeight - rect.bottom

            val keyboardOpen = keyboardHeight > screenHeight * 0.15

            symbolInput.visibility = if (keyboardOpen) View.VISIBLE else View.GONE
        }
    }


    private fun defaultJavaTemplate(): String = """
        public class Main {
            public static void main(String[] args) {
                System.out.println("Hello GoCode!");
            }
        }
    """.trimIndent()

    companion object {
        private const val PREFS_NAME = "gocode_prefs"
        private const val KEY_CODE = "playground_code"
        private const val KEY_INPUT = "playground_input"
        private const val KEY_DARK = "playground_dark"
        private const val LINT_DEBOUNCE_MS = 450L
    }
}
