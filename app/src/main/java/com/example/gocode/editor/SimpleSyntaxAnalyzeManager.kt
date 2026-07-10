package com.example.gocode.editor

import io.github.rosemoe.sora.lang.analysis.SimpleAnalyzeManager
import io.github.rosemoe.sora.lang.styling.MappedSpans
import io.github.rosemoe.sora.lang.styling.SpanFactory
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.lang.styling.TextStyle
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

class SimpleSyntaxAnalyzeManager private constructor(
    private val spec: SyntaxSpec
) : SimpleAnalyzeManager<Unit>() {

    override fun analyze(
        text: StringBuilder,
        delegate: SimpleAnalyzeManager<Unit>.Delegate<Unit>
    ): Styles {
        val lines = text.toString().split('\n').map { line ->
            if (line.endsWith('\r')) line.dropLast(1) else line
        }
        val builder = MappedSpans.Builder(lines.size.coerceAtLeast(1))
        var inBlockComment = false
        var tripleString: String? = null

        lines.forEachIndexed { lineIndex, line ->
            if (delegate.isCancelled) return@forEachIndexed

            val writer = LineSpanWriter(builder, lineIndex, line.length)
            var column = 0

            while (column < line.length) {
                val activeTriple = tripleString
                if (activeTriple != null) {
                    val close = line.indexOf(activeTriple, column)
                    val end = if (close >= 0) close + activeTriple.length else line.length
                    writer.token(column, end, LITERAL_STYLE)
                    column = end
                    if (close >= 0) tripleString = null
                    continue
                }

                if (inBlockComment) {
                    val close = line.indexOf("*/", column)
                    val end = if (close >= 0) close + 2 else line.length
                    writer.token(column, end, COMMENT_STYLE)
                    column = end
                    if (close >= 0) inBlockComment = false
                    continue
                }

                if (spec.isPreprocessorLine(line, column)) {
                    val end = scanIdentifier(line, column + 1)
                    writer.token(column, end.coerceAtLeast(column + 1), KEYWORD_STYLE)
                    column = end
                    continue
                }

                val lineComment = spec.lineComment
                if (lineComment != null && line.startsWith(lineComment, column)) {
                    writer.token(column, line.length, COMMENT_STYLE)
                    column = line.length
                    continue
                }

                if (spec.hasBlockComments && line.startsWith("/*", column)) {
                    val close = line.indexOf("*/", column + 2)
                    val end = if (close >= 0) close + 2 else line.length
                    writer.token(column, end, COMMENT_STYLE)
                    column = end
                    if (close < 0) inBlockComment = true
                    continue
                }

                val quote = line[column]
                if (spec.supportsTripleStrings && (line.startsWith("\"\"\"", column) || line.startsWith("'''", column))) {
                    val delimiter = line.substring(column, column + 3)
                    val close = line.indexOf(delimiter, column + 3)
                    val end = if (close >= 0) close + 3 else line.length
                    writer.token(column, end, LITERAL_STYLE)
                    column = end
                    if (close < 0) tripleString = delimiter
                    continue
                }
                if (quote == '"' || quote == '\'') {
                    val end = scanString(line, column, quote)
                    writer.token(column, end, LITERAL_STYLE)
                    column = end
                    continue
                }

                if (quote == '@' && spec.decorators && column + 1 < line.length && isIdentifierStart(line[column + 1])) {
                    val end = scanIdentifier(line, column + 1)
                    writer.token(column, end, ANNOTATION_STYLE)
                    column = end
                    continue
                }

                if (quote.isDigit() || (quote == '.' && column + 1 < line.length && line[column + 1].isDigit())) {
                    val end = scanNumber(line, column)
                    writer.token(column, end, LITERAL_STYLE)
                    column = end
                    continue
                }

                if (isIdentifierStart(quote)) {
                    val end = scanIdentifier(line, column)
                    val word = line.substring(column, end)
                    val style = spec.styleFor(word, line.nextMeaningfulChar(end))
                    if (style != NORMAL_STYLE) {
                        writer.token(column, end, style)
                    }
                    column = end
                    continue
                }

                if (quote in OPERATOR_CHARS) {
                    writer.token(column, column + 1, OPERATOR_STYLE)
                }
                column++
            }

            writer.finish()
        }

        builder.determine((lines.size - 1).coerceAtLeast(0))
        return Styles(builder.build()).also { it.finishBuilding() }
    }

    private class LineSpanWriter(
        private val builder: MappedSpans.Builder,
        private val line: Int,
        private val lineLength: Int
    ) {
        private var hasSpan = false
        private var lastStyle = Long.MIN_VALUE
        private var styledUntil = 0

        fun token(start: Int, end: Int, style: Long) {
            if (start >= end) return
            if (!hasSpan && start > 0) add(0, NORMAL_STYLE)
            if (hasSpan && styledUntil < start && lastStyle != NORMAL_STYLE) add(styledUntil, NORMAL_STYLE)
            add(start, style)
            styledUntil = end
        }

        fun finish() {
            if (!hasSpan) add(0, NORMAL_STYLE)
            if (styledUntil < lineLength && lastStyle != NORMAL_STYLE) add(styledUntil, NORMAL_STYLE)
        }

        private fun add(column: Int, style: Long) {
            if (hasSpan && lastStyle == style) return
            builder.add(line, SpanFactory.obtain(column, style))
            hasSpan = true
            lastStyle = style
        }
    }

    private data class SyntaxSpec(
        val keywords: Set<String>,
        val types: Set<String>,
        val literals: Set<String>,
        val builtins: Set<String>,
        val lineComment: String?,
        val hasBlockComments: Boolean,
        val supportsTripleStrings: Boolean = false,
        val decorators: Boolean = false,
        val preprocessor: Boolean = false
    ) {
        fun styleFor(word: String, nextMeaningfulChar: Char?): Long {
            return when {
                word in keywords || word in types -> KEYWORD_STYLE
                word in literals -> LITERAL_STYLE
                word in builtins || nextMeaningfulChar == '(' -> FUNCTION_STYLE
                else -> NORMAL_STYLE
            }
        }

        fun isPreprocessorLine(line: String, column: Int): Boolean {
            if (!preprocessor || line[column] != '#') return false
            return line.substring(0, column).isBlank()
        }
    }

    companion object {
        fun create(language: String): SimpleSyntaxAnalyzeManager? {
            val spec = when (language) {
                "python" -> pythonSpec()
                "c", "clang" -> cSpec()
                "cpp", "c++", "cplusplus" -> cppSpec()
                "csharp", "c#", "cs" -> csharpSpec()
                else -> null
            }
            return spec?.let(::SimpleSyntaxAnalyzeManager)
        }

        private val NORMAL_STYLE = TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL)
        private val KEYWORD_STYLE = TextStyle.makeStyle(EditorColorScheme.KEYWORD, 0, true, false, false)
        private val COMMENT_STYLE = TextStyle.makeStyle(EditorColorScheme.COMMENT, 0, false, true, false, true)
        private val LITERAL_STYLE = TextStyle.makeStyle(EditorColorScheme.LITERAL, true)
        private val FUNCTION_STYLE = TextStyle.makeStyle(EditorColorScheme.FUNCTION_NAME)
        private val OPERATOR_STYLE = TextStyle.makeStyle(EditorColorScheme.OPERATOR)
        private val ANNOTATION_STYLE = TextStyle.makeStyle(EditorColorScheme.ANNOTATION)

        private val OPERATOR_CHARS = "{}[]();,+-*/%=&|!<>?:.^~".toSet()

        private fun pythonSpec() = SyntaxSpec(
            keywords = setOf(
                "and", "as", "assert", "async", "await", "break", "class", "continue", "def",
                "del", "elif", "else", "except", "finally", "for", "from", "global", "if",
                "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise",
                "return", "try", "while", "with", "yield"
            ),
            types = setOf("bool", "dict", "float", "int", "list", "set", "str", "tuple"),
            literals = setOf("False", "None", "True"),
            builtins = setOf(
                "abs", "append", "enumerate", "input", "len", "max", "min", "print", "range",
                "round", "sorted", "sum", "type"
            ),
            lineComment = "#",
            hasBlockComments = false,
            supportsTripleStrings = true,
            decorators = true
        )

        private fun cSpec() = SyntaxSpec(
            keywords = setOf(
                "auto", "break", "case", "const", "continue", "default", "do", "else", "enum",
                "extern", "for", "goto", "if", "register", "return", "sizeof", "static",
                "struct", "switch", "typedef", "union", "volatile", "while"
            ),
            types = setOf(
                "char", "double", "float", "int", "long", "short", "signed", "unsigned", "void"
            ),
            literals = setOf("NULL"),
            builtins = setOf("fgets", "free", "malloc", "printf", "puts", "scanf", "strlen"),
            lineComment = "//",
            hasBlockComments = true,
            preprocessor = true
        )

        private fun cppSpec() = cSpec().copy(
            keywords = cSpec().keywords + setOf(
                "alignas", "alignof", "catch", "class", "constexpr", "delete", "explicit",
                "export", "friend", "namespace", "new", "noexcept", "operator", "private",
                "protected", "public", "template", "this", "throw", "try", "typename", "using",
                "virtual"
            ),
            types = cSpec().types + setOf("bool", "string", "vector", "auto"),
            literals = cSpec().literals + setOf("false", "nullptr", "true"),
            builtins = cSpec().builtins + setOf("cin", "cout", "endl", "push_back", "size")
        )

        private fun csharpSpec() = SyntaxSpec(
            keywords = setOf(
                "abstract", "as", "base", "break", "case", "catch", "checked", "class", "const",
                "continue", "default", "delegate", "do", "else", "enum", "event", "explicit",
                "extern", "finally", "fixed", "for", "foreach", "goto", "if", "implicit", "in",
                "interface", "internal", "is", "lock", "namespace", "new", "operator", "out",
                "override", "params", "private", "protected", "public", "readonly", "ref",
                "return", "sealed", "sizeof", "stackalloc", "static", "struct", "switch", "this",
                "throw", "try", "typeof", "unchecked", "unsafe", "using", "virtual", "volatile",
                "while"
            ),
            types = setOf(
                "bool", "byte", "char", "decimal", "double", "float", "int", "long", "object",
                "sbyte", "short", "string", "uint", "ulong", "ushort", "var", "void"
            ),
            literals = setOf("false", "null", "true"),
            builtins = setOf("Console", "Main", "ReadLine", "Write", "WriteLine", "Parse", "ToString"),
            lineComment = "//",
            hasBlockComments = true,
            decorators = true
        )

        private fun CharSequence.nextMeaningfulChar(start: Int): Char? {
            var index = start
            while (index < length) {
                val char = this[index]
                if (!char.isWhitespace()) return char
                index++
            }
            return null
        }

        private fun isIdentifierStart(char: Char): Boolean = char == '_' || char.isLetter()

        private fun isIdentifierPart(char: Char): Boolean = char == '_' || char.isLetterOrDigit()

        private fun scanIdentifier(line: String, start: Int): Int {
            var index = start
            while (index < line.length && isIdentifierPart(line[index])) index++
            return index
        }

        private fun scanNumber(line: String, start: Int): Int {
            var index = start
            while (index < line.length && (line[index].isLetterOrDigit() || line[index] in "._")) index++
            return index
        }

        private fun scanString(line: String, start: Int, quote: Char): Int {
            var index = start + 1
            var escaped = false
            while (index < line.length) {
                val char = line[index]
                if (escaped) {
                    escaped = false
                } else if (char == '\\') {
                    escaped = true
                } else if (char == quote) {
                    return index + 1
                }
                index++
            }
            return line.length
        }
    }
}
