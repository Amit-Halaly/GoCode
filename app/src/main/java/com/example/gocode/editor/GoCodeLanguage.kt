package com.example.gocode.editor

import android.os.Bundle
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.QuickQuoteHandler
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.completion.CompletionHelper
import io.github.rosemoe.sora.lang.completion.CompletionItemKind
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem
import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.widget.SymbolPairMatch

class GoCodeLanguage(language: String) : Language {

    private val normalized = language.lowercase()
    private val delegate: Language = if (normalized == "java") JavaLanguage() else EmptyLanguage()
    private val fallbackQuickQuoteHandler = QuickQuoteHandler { _, _, _, _ ->
        QuickQuoteHandler.HandleResult.NOT_CONSUMED
    }
    private val snippets = snippetsFor(normalized)

    override fun getAnalyzeManager(): AnalyzeManager = delegate.analyzeManager

    override fun getInterruptionLevel(): Int = delegate.interruptionLevel

    override fun requireAutoComplete(
        content: ContentReference,
        position: CharPosition,
        publisher: CompletionPublisher,
        extraArguments: Bundle
    ) {
        delegate.requireAutoComplete(content, position, publisher, extraArguments)

        val prefix = CompletionHelper.computePrefix(content, position) { ch ->
            ch.isLetterOrDigit() || ch == '_'
        }
        if (prefix.isBlank()) return

        snippets
            .filter { it.matches(prefix) }
            .forEach { snippet ->
                publisher.addItem(
                    SimpleCompletionItem(
                        snippet.label,
                        snippet.description,
                        prefix.length,
                        snippet.body
                    ).kind(CompletionItemKind.Snippet)
                )
            }
    }

    override fun getIndentAdvance(content: ContentReference, line: Int, column: Int): Int {
        return delegate.getIndentAdvance(content, line, column)
    }

    override fun useTab(): Boolean = delegate.useTab()

    override fun getFormatter(): Formatter = delegate.formatter

    override fun getSymbolPairs(): SymbolPairMatch = delegate.symbolPairs

    override fun getNewlineHandlers(): Array<NewlineHandler> = delegate.newlineHandlers ?: emptyArray()

    override fun getQuickQuoteHandler(): QuickQuoteHandler = delegate.quickQuoteHandler ?: fallbackQuickQuoteHandler

    override fun destroy() {
        delegate.destroy()
    }

    private data class CodeSnippet(
        val triggers: List<String>,
        val label: String,
        val description: String,
        val body: String
    ) {
        fun matches(prefix: String): Boolean {
            val lower = prefix.lowercase()
            return triggers.any { it.startsWith(lower) || lower.startsWith(it) } ||
                label.lowercase().startsWith(lower)
        }
    }

    private companion object {
        private fun snippetsFor(language: String): List<CodeSnippet> {
            return when (language) {
                "python" -> pythonSnippets()
                "c", "clang" -> cSnippets()
                else -> javaSnippets()
            }
        }

        private fun s(
            triggers: String,
            label: String,
            description: String,
            body: String
        ): CodeSnippet {
            return CodeSnippet(
                triggers = triggers.split("|").map { it.trim().lowercase() }.filter { it.isNotBlank() },
                label = label,
                description = description,
                body = body.trimIndent()
            )
        }

        private fun javaSnippets() = listOf(
            s("wh|while", "while loop", "Repeat while a condition is true", "while (condition) {\n    \n}"),
            s("for|fori", "for loop", "Count with an index", "for (int i = 0; i < count; i++) {\n    \n}"),
            s("fore|foreach", "for each", "Loop over an array or collection", "for (String item : items) {\n    \n}"),
            s("if", "if block", "Run code when a condition is true", "if (condition) {\n    \n}"),
            s("ife|ifelse", "if / else", "Choose between two paths", "if (condition) {\n    \n} else {\n    \n}"),
            s("else", "else block", "Run the fallback branch", "else {\n    \n}"),
            s("sout|print|sys", "System.out.println", "Print a line", "System.out.println();"),
            s("main|psvm", "main method", "Java program entry point", "public static void main(String[] args) {\n    \n}"),
            s("met|method", "static method", "Create a reusable method", "static void methodName() {\n    \n}"),
            s("ret|return", "return", "Send a value back", "return value;"),
            s("cls|class", "class", "Define a class", "class ClassName {\n    \n}"),
            s("sc|scan|scanner", "Scanner input", "Read from input", "Scanner input = new Scanner(System.in);"),
            s("next|nextint", "nextInt", "Read an integer", "int number = input.nextInt();"),
            s("str|string", "String variable", "Create text", "String text = \"\";"),
            s("arr|array", "int array", "Create an int array", "int[] numbers = {1, 2, 3};"),
            s("list|arraylist", "ArrayList", "Create a list", "ArrayList<String> items = new ArrayList<>();"),
            s("try", "try / catch", "Handle risky code", "try {\n    \n} catch (Exception e) {\n    System.out.println(e.getMessage());\n}"),
            s("sw|switch", "switch", "Choose by value", "switch (value) {\n    case 1:\n        break;\n    default:\n        break;\n}"),
            s("bool|boolean", "boolean", "Store true or false", "boolean isReady = true;"),
            s("const|final", "final variable", "Create a constant", "final int VALUE = 0;")
        )

        private fun pythonSnippets() = listOf(
            s("wh|while", "while loop", "Repeat while a condition is true", "while condition:\n    "),
            s("for", "for loop", "Loop over a range", "for i in range(count):\n    "),
            s("fore|each", "for item", "Loop over items", "for item in items:\n    "),
            s("if", "if block", "Run code when a condition is true", "if condition:\n    "),
            s("ife|ifelse", "if / else", "Choose between two paths", "if condition:\n    \nelse:\n    "),
            s("elif", "elif", "Add another condition", "elif condition:\n    "),
            s("else", "else block", "Run the fallback branch", "else:\n    "),
            s("pr|print", "print", "Print a value", "print()"),
            s("inp|input", "input", "Read text input", "text = input(\"Enter text: \")"),
            s("inti|intinput", "int input", "Read a whole number", "number = int(input(\"Enter number: \"))"),
            s("def|fun", "function", "Define a function", "def function_name():\n    "),
            s("ret|return", "return", "Send a value back", "return value"),
            s("class|cls", "class", "Define a class", "class ClassName:\n    pass"),
            s("try", "try / except", "Handle risky code", "try:\n    \nexcept ValueError:\n    print(\"Invalid value\")"),
            s("list", "list", "Create a list", "items = []"),
            s("dict", "dictionary", "Create key-value data", "student = {\"name\": \"\", \"age\": 0}"),
            s("app|append", "append", "Add to a list", "items.append(value)"),
            s("len", "len", "Count items", "len(items)"),
            s("with", "with open", "Open a file safely", "with open(\"file.txt\") as file:\n    "),
            s("main", "main guard", "Run only as a script", "if __name__ == \"__main__\":\n    ")
        )

        private fun cSnippets() = listOf(
            s("inc|include", "include stdio", "Add printf and scanf", "#include <stdio.h>"),
            s("incs|stringh", "include string", "Add string helpers", "#include <string.h>"),
            s("main", "main function", "C program entry point", "int main() {\n    \n    return 0;\n}"),
            s("wh|while", "while loop", "Repeat while a condition is true", "while (condition) {\n    \n}"),
            s("for|fori", "for loop", "Count with an index", "for (int i = 0; i < count; i++) {\n    \n}"),
            s("if", "if block", "Run code when a condition is true", "if (condition) {\n    \n}"),
            s("ife|ifelse", "if / else", "Choose between two paths", "if (condition) {\n    \n} else {\n    \n}"),
            s("else", "else block", "Run the fallback branch", "else {\n    \n}"),
            s("pr|printf", "printf", "Print formatted output", "printf(\"%d\\n\", value);"),
            s("scan|scanf", "scanf int", "Read an integer", "scanf(\"%d\", &number);"),
            s("fun|func", "function", "Create a function", "void function_name() {\n    \n}"),
            s("ret|return", "return", "Return from a function", "return value;"),
            s("arr|array", "int array", "Create an int array", "int numbers[] = {1, 2, 3};"),
            s("str|string", "char string", "Create a C string", "char text[] = \"\";"),
            s("ptr|pointer", "pointer", "Point to a variable", "int *ptr = &value;"),
            s("sw|switch", "switch", "Choose by value", "switch (value) {\n    case 1:\n        break;\n    default:\n        break;\n}"),
            s("struct", "struct", "Create a custom data type", "struct Student {\n    char name[50];\n    int age;\n};"),
            s("do", "do while", "Run once, then check", "do {\n    \n} while (condition);"),
            s("size|sizeof", "sizeof array length", "Count array items", "sizeof(numbers) / sizeof(numbers[0])"),
            s("malloc", "malloc", "Allocate memory", "int *numbers = malloc(count * sizeof(int));")
        )
    }
}
