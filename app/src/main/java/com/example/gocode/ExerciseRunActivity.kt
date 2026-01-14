package com.example.gocode

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.gocode.network.ApiClient
import com.example.gocode.network.models.lintModels.LintRequest
import com.example.gocode.network.models.runModels.RunRequest
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula
import io.github.rosemoe.sora.widget.schemes.SchemeEclipse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExerciseRunActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("gocode_prefs", MODE_PRIVATE) }
    private var isDark = true

    private lateinit var editor: CodeEditor
    private lateinit var etInput: EditText
    private lateinit var tvResult: TextView

    private val ui = Handler(Looper.getMainLooper())
    private var pollRunnable: Runnable? = null
    private var lastSeenText: String? = null
    private var lintJob: Job? = null

    private var lastLintMessage: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_run)

        editor = findViewById(R.id.codeEditor)
        etInput = findViewById(R.id.etInput)
        val btnRun = findViewById<Button>(R.id.btnRun)
        val btnTheme = findViewById<Button>(R.id.btnTheme)
        val btnClear = findViewById<Button>(R.id.btnClear)
        tvResult = findViewById(R.id.tvResult)

        editor.setEditorLanguage(JavaLanguage())
        editor.setTextSize(14f)
        editor.isLineNumberEnabled = true
        editor.isHighlightCurrentLine = true
        editor.isUndoEnabled = true
        editor.isWordwrap = true

        val savedCode = prefs.getString("draft_code", null) ?: defaultJavaTemplate()
        val savedInput = prefs.getString("draft_input", "") ?: ""
        isDark = prefs.getBoolean("editor_dark", true)

        editor.setText(savedCode)
        etInput.setText(savedInput)
        applyTheme(isDark)

        startPollingForChanges()

        btnTheme.setOnClickListener {
            isDark = !isDark
            prefs.edit { putBoolean("editor_dark", isDark) }
            applyTheme(isDark)
        }

        btnClear.setOnClickListener {
            val fresh = defaultJavaTemplate()
            editor.setText(fresh)
            etInput.setText("")
            tvResult.text = "—"
            lastLintMessage = null

            prefs.edit {
                putString("draft_code", fresh)
                putString("draft_input", "")
            }
        }

        btnRun.setOnClickListener {
            val code = editor.text.toString()
            val input = etInput.text.toString()

            prefs.edit {
                putString("draft_code", code)
                putString("draft_input", input)
            }

            btnRun.isEnabled = false
            tvResult.text = "Running..."

            lifecycleScope.launch {
                try {
                    val res = ApiClient.execApi.run(
                        RunRequest(language = "java", code = code, input = input)
                    )

                    tvResult.text = buildString {
                        appendLine("exitCode: ${res.exitCode}")
                        appendLine("output:")
                        appendLine(res.output)
                        appendLine("error:")
                        appendLine(res.error)
                    }
                } catch (e: Exception) {
                    tvResult.text = "Run failed: ${e.message}"
                } finally {
                    btnRun.isEnabled = true
                }
            }
        }
    }

    private fun startPollingForChanges() {
        pollRunnable?.let { ui.removeCallbacks(it) }

        val r = object : Runnable {
            override fun run() {
                val text = editor.text.toString()
                if (text != lastSeenText) {
                    lastSeenText = text
                    scheduleLint(text)
                }
                ui.postDelayed(this, 400L)
            }
        }

        pollRunnable = r
        ui.post(r)
    }

    private fun scheduleLint(code: String) {
        lintJob?.cancel()
        lintJob = lifecycleScope.launch {
            delay(500L)
            runLint(code)
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun runLint(code: String) {
        try {
            val res = ApiClient.execApi.lint(LintRequest(code = code))

            if (res.errors.isNotEmpty()) {
                val e = res.errors.first()
                val msg = "Lint: line ${e.line}${if (e.col != null) ", col ${e.col}" else ""} — ${e.message}"
                lastLintMessage = msg

                if (tvResult.text.isNullOrBlank() || tvResult.text == "—" || tvResult.text.startsWith("Lint:")) {
                    tvResult.text = msg
                }
            } else {
                lastLintMessage = null
                if (tvResult.text.startsWith("Lint:")) {
                    tvResult.text = "Lint: OK ✅"
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun applyTheme(dark: Boolean) {
        editor.colorScheme = if (dark) SchemeDarcula() else SchemeEclipse()
    }

    override fun onDestroy() {
        super.onDestroy()
        pollRunnable?.let { ui.removeCallbacks(it) }
        lintJob?.cancel()
        runCatching { editor.release() }
    }

    private fun defaultJavaTemplate(): String = """
        public class Main {
            public static void main(String[] args) {
                System.out.println("Hello GoCode!");
            }
        }
    """.trimIndent()
}
