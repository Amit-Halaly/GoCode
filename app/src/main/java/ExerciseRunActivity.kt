package com.example.gocode

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.gocode.network.ApiClient
import com.example.gocode.network.RunRequest
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

class ExerciseRunActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("gocode_prefs", Context.MODE_PRIVATE) }

    private var isDark = true

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_run)

        val codeEditor = findViewById<CodeEditor>(R.id.codeEditor)
        val etInput = findViewById<EditText>(R.id.etInput)
        val btnRun = findViewById<Button>(R.id.btnRun)
        val btnTheme = findViewById<Button>(R.id.btnTheme)
        val btnClear = findViewById<Button>(R.id.btnClear)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        codeEditor.setEditorLanguage(JavaLanguage())

        codeEditor.isWordwrap = true
        codeEditor.setTextSize(14f)

        val savedCode = prefs.getString("draft_code", null) ?: defaultJavaTemplate()
        val savedInput = prefs.getString("draft_input", "") ?: ""
        isDark = prefs.getBoolean("editor_dark", true)

        codeEditor.setText(savedCode)
        etInput.setText(savedInput)
        applyTheme(codeEditor, isDark)

        btnTheme.setOnClickListener {
            isDark = !isDark
            prefs.edit { putBoolean("editor_dark", isDark) }
            applyTheme(codeEditor, isDark)
        }

        btnClear.setOnClickListener {
            codeEditor.setText(defaultJavaTemplate())
            etInput.setText("")
            tvResult.text = "—"
            prefs.edit {
                putString("draft_code", defaultJavaTemplate())
                putString("draft_input", "")
            }
        }

        btnRun.setOnClickListener {
            val code = codeEditor.text.toString()
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

                    val firstErrLine = parseFirstJavaErrorLine(res.error)

                    tvResult.text = buildString {
                        appendLine("exitCode: ${res.exitCode}")
                        if (firstErrLine != null) appendLine("firstErrorLine: $firstErrLine")
                        appendLine("output:")
                        appendLine(res.output)
                        appendLine("error:")
                        appendLine(res.error)
                    }

                    if (firstErrLine != null) {
                        jumpToLine(codeEditor, firstErrLine)
                        tryMarkErrorLine(codeEditor, firstErrLine)
                    }

                } catch (e: Exception) {
                    tvResult.text = "Request failed: ${e.message}"
                } finally {
                    btnRun.isEnabled = true
                }
            }
        }
    }

    private fun applyTheme(editor: CodeEditor, dark: Boolean) {
        val scheme = EditorColorScheme()

        if (dark) {
            scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, "#0F111A".toColorInt())
            scheme.setColor(EditorColorScheme.TEXT_NORMAL, "#E6E6E6".toColorInt())
            scheme.setColor(EditorColorScheme.LINE_NUMBER, "#8A8A8A".toColorInt())
            safeSet(scheme, "LINE_NUMBER_BACKGROUND", "#0F111A".toColorInt())
            safeSet(scheme, "CURRENT_LINE", "#151A24".toColorInt())
        } else {
            scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, Color.WHITE)
            scheme.setColor(EditorColorScheme.TEXT_NORMAL, "#111111".toColorInt())
            scheme.setColor(EditorColorScheme.LINE_NUMBER, "#666666".toColorInt())
            safeSet(scheme, "LINE_NUMBER_BACKGROUND", "#F4F4F4".toColorInt())
            safeSet(scheme, "CURRENT_LINE", "#EFF3FF".toColorInt())
        }

        editor.colorScheme = scheme
    }

    private fun safeSet(scheme: EditorColorScheme, fieldName: String, color: Int) {
        try {
            val field = EditorColorScheme::class.java.getField(fieldName)
            val key = field.getInt(null)
            scheme.setColor(key, color)
        } catch (_: Throwable) {
        }
    }

    private fun parseFirstJavaErrorLine(stderr: String?): Int? {
        if (stderr.isNullOrBlank()) return null
        val regex = Regex("""Main\.java:(\d+):""")
        val m = regex.find(stderr) ?: return null
        return m.groupValues[1].toIntOrNull()
    }

    private fun jumpToLine(editor: CodeEditor, oneBasedLine: Int) {
        val line = (oneBasedLine - 1).coerceAtLeast(0)
        invokeEditor(editor, "setSelection", line, 0)
        invokeEditor(editor, "setCursorPosition", line, 0)
        invokeEditor(editor, "jumpToLine", oneBasedLine)
        invokeEditor(editor, "scrollToLine", oneBasedLine)
    }

    @SuppressLint("UseKtx")
    private fun tryMarkErrorLine(editor: CodeEditor, oneBasedLine: Int) {
        val line = (oneBasedLine - 1).coerceAtLeast(0)
        invokeEditor(editor, "setLineBackground", line, "#33FF0000".toColorInt())
        invokeEditor(editor, "markLineError", line)
        invokeEditor(editor, "setErrorLine", line)
    }

    private fun invokeEditor(target: Any, methodName: String, vararg args: Any) {
        try {
            val methods = target.javaClass.methods.filter { it.name == methodName }
            val m = methods.firstOrNull { it.parameterTypes.size == args.size } ?: return
            m.isAccessible = true
            m.invoke(target, *args)
        } catch (_: Throwable) {
        }
    }

    private fun defaultJavaTemplate(): String = """
        public class Main {
            public static void main(String[] args) {
                System.out.println("Hello GoCode!");
            }
        }
    """.trimIndent()
}
