package com.example.gocode

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.gocode.AchievementBottomSheet
import com.example.gocode.editor.GoCodeLanguage
import com.example.gocode.firebase.FirebaseContentRepository
import com.example.gocode.gamification.GamificationRepository
import com.example.gocode.gamification.GamificationResult
import com.example.gocode.lessons.LanguagePathFragment
import com.example.gocode.lessons.LessonProgressStore
import com.example.gocode.network.ApiClient
import com.example.gocode.network.models.hintModels.HintRequest
import com.example.gocode.network.models.lintModels.LintRequest
import com.example.gocode.network.models.runModels.RunRequest
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

class ExerciseRunActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    private lateinit var editor: CodeEditor
    private lateinit var inputField: EditText

    private lateinit var lintStatus: TextView
    private lateinit var outputView: TextView

    private lateinit var runButton: Button
    private lateinit var themeButton: Button
    private lateinit var clearButton: Button

    private lateinit var taskText: TextView
    private lateinit var hintText: TextView

    private var isDarkTheme: Boolean = true
    private var lintJob: Job? = null
    private var runJob: Job? = null
    private var nodeId: String = "java_u1_c1"

    private var currentTask: String = "Task: —"

    private var expectedOutput: String? = "Hello World"

    @SuppressLint("SetTextI18n")
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

        taskText = findViewById(R.id.taskText)
        hintText = findViewById(R.id.hintText)

        nodeId = intent.getStringExtra(LanguagePathFragment.EXTRA_NODE_ID) ?: "java_u1_c1"
        currentTask = taskForNode(nodeId)
        taskText.text = "Task: $currentTask"

        setupSymbolBar()
        setupEditor()

        val savedCode = prefs.getString(codeKey(), null)
        val savedInput = prefs.getString(inputKey(), "") ?: ""
        isDarkTheme = prefs.getBoolean(KEY_DARK, true)

        applyTheme(isDarkTheme)
        editor.setText(savedCode ?: defaultJavaTemplate())
        inputField.setText(savedInput)
        FirebaseContentRepository.getCodeTask(nodeId) { task ->
            val remoteTask = task["task"]
            val remoteTemplate = task["template"]
            if (!remoteTask.isNullOrBlank()) {
                currentTask = remoteTask
                taskText.text = "Task: $currentTask"
            }
            if (savedCode == null && !remoteTemplate.isNullOrBlank()) {
                editor.setText(remoteTemplate)
            }
        }

        editor.subscribeAlways<ContentChangeEvent> { scheduleLint() }

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
            hintText.text = "—"
            hintText.visibility = View.GONE
            clearDiagnostics()
            persistDraft()
        }

        runButton.setOnClickListener { runCode() }

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
        editor.setEditorLanguage(GoCodeLanguage("java"))
        editor.setTextSize(14f)
        editor.isLineNumberEnabled = true
        editor.isHighlightCurrentLine = true
        editor.isUndoEnabled = true
        editor.isWordwrap = true
        editor.getComponent<EditorAutoCompletion>().setEnabledAnimation(true)

        runCatching {
            editor.typefaceText = Typeface.createFromAsset(assets, "JetBrainsMono-Regular.ttf")
        }
    }

    private fun setupSymbolBar() {
        val symbolInput = findViewById<SymbolInputView>(R.id.symbolInput)
        symbolInput.bindEditor(editor)

        symbolInput.addSymbols(
            arrayOf("&&", "{", "}", "(", ")", "||", ";"),
            arrayOf("&&", "{}", "}", "()", ")", "||", ";")
        )

        observeKeyboard(symbolInput)
    }

    private fun applyTheme(dark: Boolean) {
        editor.colorScheme = if (dark) SchemeDarcula() else SchemeEclipse()
        editor.invalidate()
    }

    private fun persistDraft() {
        prefs.edit {
            putString(codeKey(), editor.text.toString())
            putString(inputKey(), inputField.text.toString())
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
            clearDiagnostics()
            return
        }

        val (start, end) = result
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

    private fun findErrorIndex(lineZeroBased: Int): Pair<Int, Int>? {
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

    @SuppressLint("SetTextI18n")
    private fun runCode() {
        runJob?.cancel()
        runJob = lifecycleScope.launch {
            val code = editor.text.toString()
            val input = inputField.text.toString()
            persistDraft()

            runButton.isEnabled = false
            outputView.text = "Running..."
            hintText.text = "—"
            hintText.visibility = View.GONE

            runCatching {
                ApiClient.execApi.run(
                    RunRequest(
                        language = "java",
                        code = code,
                        input = input,
                        expectedOutput = expectedOutput,
                        compareMode = "normalize"
                    )
                )
            }.onSuccess { res ->

                val hasTest = (res.passed != null)
                val failed =
                    if (hasTest) (res.passed == false)
                    else ((res.exitCode != 0) || res.error.isNotBlank())

                outputView.text = buildString {
                    appendLine("passed: ${res.passed}")
                    appendLine("exitCode: ${res.exitCode}")
                    appendLine()
                    appendLine("output:")
                    appendLine(res.output)
                    appendLine()
                    appendLine("error:")
                    appendLine(res.error)

                    if (hasTest) {
                        appendLine()
                        appendLine("expected:")
                        appendLine(res.expectedOutput ?: "—")
                        appendLine()
                        appendLine("actual:")
                        appendLine(res.actualOutput ?: res.output)
                    }
                }

                if (failed) {
                    runCatching {
                        ApiClient.execApi.hint(
                            HintRequest(
                                task = currentTask,
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
                        hintText.text = hintRes.hint
                        hintText.visibility = View.VISIBLE
                    }.onFailure {
                        hintText.text = "—"
                        hintText.visibility = View.GONE
                    }
                } else {
                    hintText.text = "congratulations!"
                    hintText.visibility = View.VISIBLE
                    LessonProgressStore.saveProgress(this@ExerciseRunActivity, nodeId, 100)
                    awardCompletion()
                }

            }.onFailure { e ->
                outputView.text = "Request failed: ${e.message}"
                hintText.text = "—"
                hintText.visibility = View.GONE
            }

            runButton.isEnabled = true
        }
    }

    private fun observeKeyboard(symbolInput: View) {
        val root = findViewById<View>(android.R.id.content)

        root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            root.getWindowVisibleDisplayFrame(rect)

            val screenHeight = root.rootView.height
            val keyboardHeight = screenHeight - rect.bottom
            val keyboardOpen = keyboardHeight > screenHeight * 0.15

            symbolInput.visibility = if (keyboardOpen) View.VISIBLE else View.GONE
        }
    }

    private fun defaultJavaTemplate(): String {
        return when (nodeId) {
            "java_u2_c1" -> """
                public class Main {
                    public static void main(String[] args) {
                        int age = 16;
                        boolean hasPassword = true;

                        // TODO: Print "Access granted" only when age is at least 13
                        // and hasPassword is true. Otherwise print "Access denied".
                    }
                }
            """.trimIndent()
            "java_u3_c1" -> """
                public class Main {
                    public static void main(String[] args) {
                        // TODO: Use a for loop to print the numbers 1 to 5.
                        // When the number is 3, also print "Middle".
                    }
                }
            """.trimIndent()
            "java_u4_c1" -> """
                public class Main {
                    public static void main(String[] args) {
                        String[] favorites = {"Java", "Android", "GoCode"};

                        // TODO: Use a loop to print every value in favorites.
                    }
                }
            """.trimIndent()
            "java_u5_c1" -> """
                public class Main {
                    static void greet(String name) {
                        // TODO: Print "Hello " plus the name.
                    }

                    public static void main(String[] args) {
                        greet("Leo");
                        greet("Maya");
                    }
                }
            """.trimIndent()
            "java_u6_c1" -> """
                import java.util.Scanner;

                public class Main {
                    public static void main(String[] args) {
                        Scanner input = new Scanner(System.in);

                        // TODO: Ask for age, read it, and print "Welcome" if age >= 13.

                        input.close();
                    }
                }
            """.trimIndent()
            "java_u7_c1" -> """
                public class Main {
                    public static void main(String[] args) {
                        String name = "  Leo  ";

                        // TODO: Trim the name and check if it equals "Leo".
                        // If it does, print "Found Leo".
                    }
                }
            """.trimIndent()
            "java_u8_c1" -> """
                class Student {
                    String name;
                    int age;

                    void introduce() {
                        // TODO: Print the student's name and age.
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Student student = new Student();
                        student.name = "Maya";
                        student.age = 14;
                        student.introduce();
                    }
                }
            """.trimIndent()
            "java_u9_c1" -> """
                public class Main {
                    public static void main(String[] args) {
                        String text = "42";

                        // TODO: Use try / catch to parse text into an int.
                        // Print the number if it works, otherwise print "Invalid number".
                    }
                }
            """.trimIndent()
            "java_u10_c1" -> """
                public class Main {
                    static void printLongNames(String[] names) {
                        // TODO: Print only names longer than 3 characters.
                    }

                    public static void main(String[] args) {
                        String[] names = {"Leo", "Maya", "Noam", "Dan"};
                        printLongNames(names);
                    }
                }
            """.trimIndent()
            else -> """
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello GoCode!");
                    }
                }
            """.trimIndent()
        }
    }

    private fun taskForNode(nodeId: String): String {
        return when (nodeId) {
            "java_u2_c1" -> "Use if / else to check access. Print Access granted only when age >= 13 and hasPassword is true."
            "java_u3_c1" -> "Use a for loop to print the numbers 1 to 5. When the number is 3, also print Middle."
            "java_u4_c1" -> "Use a loop to print every value in the favorites array."
            "java_u5_c1" -> "Complete the greet method so it prints Hello plus the name it receives."
            "java_u6_c1" -> "Use Scanner to read an age. Print Welcome if age is at least 13, otherwise print Too young."
            "java_u7_c1" -> "Trim the name and use equals to check if it is Leo. If yes, print Found Leo."
            "java_u8_c1" -> "Complete the Student introduce method so the object prints its own name and age."
            "java_u9_c1" -> "Use try / catch to parse text into an int. Print the number or Invalid number."
            "java_u10_c1" -> "Write a method that loops through the names array and prints only names longer than 3 characters."
            else -> "Print Hello World"
        }
    }

    private fun awardCompletion() {
        GamificationRepository.awardNodeCompleted(this, nodeId) { result ->
            result?.let { showReward(it) }
        }
    }

    private fun showReward(result: GamificationResult) {
        AchievementBottomSheet.newRewardInstance(result)
            .show(supportFragmentManager, "reward_sheet")
    }

    private fun codeKey(): String = "${KEY_CODE}_$nodeId"

    private fun inputKey(): String = "${KEY_INPUT}_$nodeId"

    companion object {
        private const val PREFS_NAME = "goCode_prefs"
        private const val KEY_CODE = "playground_code"
        private const val KEY_INPUT = "playground_input"
        private const val KEY_DARK = "playground_dark"
        private const val LINT_DEBOUNCE_MS = 1000L
    }
}
