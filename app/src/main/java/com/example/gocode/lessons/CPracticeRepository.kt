package com.example.gocode.lessons

object CPracticeRepository {

    fun getQuestions(nodeId: String): List<PracticeQuestion> {
        val unit = Regex("""c_u(\d+)_""").find(nodeId)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
        val lessonPart = when {
            nodeId.contains("_p2") -> 2
            nodeId.contains("_q1") -> 3
            else -> 1
        }
        return questionsFor(unit, lessonPart)
    }

    private fun questionsFor(unit: Int, part: Int): List<PracticeQuestion> {
        val base = when (unit) {
            1 -> listOf(
                mc(unit, 1, "Which header gives C access to printf?", listOf("#include <stdio.h>", "#include <print.h>", "import stdio", "using System"), "#include <stdio.h>", "printf is declared in stdio.h."),
                drag(unit, 2, "Complete the first C print.", "#include <stdio.h>\n\nint main() {\n    ______(\"Hello C\\n\");\n    return ______;\n}", listOf("printf", "0", "print", "main"), listOf("printf", "0"), "printf prints text, and return 0 marks success."),
                mc(unit, 3, "Which type stores a whole number?", listOf("int", "double", "char", "string"), "int", "int stores whole numbers in C."),
                drag(unit, 4, "Match printf specifiers to values.", "int age = 14;\nchar grade = 'A';\nprintf(\"______ ______\\n\", age, grade);", listOf("%d", "%c", "%s", "%f"), listOf("%d", "%c"), "%d prints int and %c prints char.")
            )
            2 -> listOf(
                mc(unit, 1, "Which operator checks equality?", listOf("==", "=", "!=", ">="), "==", "== compares values. = assigns a value."),
                drag(unit, 2, "Complete the age check.", "if (age ______ 13) {\n    printf(\"Welcome\\n\");\n}", listOf(">=", "<=", "=", "else"), listOf(">="), "age >= 13 checks age 13 or older."),
                mc(unit, 3, "What does && mean?", listOf("Both conditions must be true", "Either condition can be true", "Assign a value", "End the program"), "Both conditions must be true", "&& is logical AND."),
                drag(unit, 4, "Complete the if / else shape.", "______ (score >= 75) {\n    printf(\"Passed\\n\");\n} ______ {\n    printf(\"Try again\\n\");\n}", listOf("if", "else", "while", "return"), listOf("if", "else"), "if starts the true branch; else handles false.")
            )
            3 -> listOf(
                mc(unit, 1, "Which loop is best when counting 1 to 5 in one line?", listOf("for", "char", "include", "return"), "for", "A for loop is compact for counting."),
                drag(unit, 2, "Complete the for loop.", "for (int i = 1; i ______ 5; i______ ) {\n    printf(\"%d\\n\", i);\n}", listOf("<=", "++", "--", "=="), listOf("<=", "++"), "The loop runs while i <= 5 and increments with i++."),
                mc(unit, 3, "What must a while loop eventually do?", listOf("Make its condition false", "Remove main", "Ignore braces", "Use strings"), "Make its condition false", "Without progress toward false, the loop can run forever."),
                fill(unit, 4, "Complete the increment operator.", "count______;", "++", "count++ adds one.")
            )
            4 -> listOf(
                mc(unit, 1, "What is the first index in a C array?", listOf("0", "1", "-1", "size"), "0", "C arrays start at index 0."),
                drag(unit, 2, "Complete the array loop.", "for (int i = 0; i < ______; i++) {\n    printf(\"%d\\n\", scores[______]);\n}", listOf("size", "i", "0", "score"), listOf("size", "i"), "Use the known size as the limit and i as the current index."),
                mc(unit, 3, "What is unsafe in C arrays?", listOf("Reading outside the array", "Using printf", "Declaring int", "Using return 0"), "Reading outside the array", "C will not protect you from every invalid index."),
                fill(unit, 4, "If an array has 4 items, the last index is ______.", null, "3", "The last index is size - 1.")
            )
            5 -> listOf(
                mc(unit, 1, "What is a function?", listOf("A named reusable block of code", "Only a variable", "Only a header", "An array index"), "A named reusable block of code", "Functions organize reusable logic."),
                drag(unit, 2, "Complete the function that returns double.", "______ doubleNumber(int n) {\n    ______ n * 2;\n}", listOf("int", "return", "void", "printf"), listOf("int", "return"), "The function returns an int, so it uses int and return."),
                mc(unit, 3, "What does void mean?", listOf("No return value", "A string", "A pointer", "A compiler"), "No return value", "void functions do not send back a value."),
                fill(unit, 4, "Complete the call: sayHello______;", null, "()", "Function calls use parentheses.")
            )
            6 -> listOf(
                mc(unit, 1, "Which function reads formatted input in C?", listOf("scanf", "printf", "println", "input"), "scanf", "scanf reads input using format specifiers."),
                drag(unit, 2, "Complete reading an int.", "int age;\nscanf(\"______\", ______age);", listOf("%d", "&", "%s", "*"), listOf("%d", "&"), "scanf needs %d and the address of age."),
                mc(unit, 3, "Why use & with scanf for an int?", listOf("To pass the variable address", "To print a new line", "To create main", "To compare strings"), "To pass the variable address", "scanf stores input at the address you provide."),
                drag(unit, 4, "Read age and decide.", "scanf(\"%d\", &age);\nif (age ______ 13) {\n    printf(\"Welcome\\n\");\n}", listOf(">=", "==", "<", "else"), listOf(">="), "Compare the input after reading it.")
            )
            7 -> listOf(
                mc(unit, 1, "What is a beginner C string?", listOf("A char array", "Only an int", "A for loop", "A return value"), "A char array", "C strings are stored in char arrays."),
                drag(unit, 2, "Complete the string include and print.", "#include <______>\nchar name[] = \"Leo\";\nprintf(\"______\\n\", name);", listOf("string.h", "%s", "%d", "stdio.c"), listOf("string.h", "%s"), "string.h has string helpers, and %s prints strings."),
                mc(unit, 3, "Which function compares two C strings?", listOf("strcmp", "==", "equals", "compareTo"), "strcmp", "strcmp compares string text."),
                fill(unit, 4, "strcmp returns ______ when strings are equal.", null, "0", "strcmp returns 0 for equal strings.")
            )
            8 -> listOf(
                mc(unit, 1, "What does a pointer store?", listOf("An address", "Only text", "Only a loop", "A compiler warning"), "An address", "Pointers store memory addresses."),
                drag(unit, 2, "Complete pointer creation.", "int score = 90;\nint *ptr = ______score;\nprintf(\"%d\\n\", ______ptr);", listOf("&", "*", "%", "return"), listOf("&", "*"), "& gets the address, * reads the value through the pointer."),
                mc(unit, 3, "What does &score mean?", listOf("The address of score", "The text score", "The first array item", "A newline"), "The address of score", "& gets a variable address."),
                mc(unit, 4, "Why pass a pointer to a function?", listOf("So it can change the original value", "So printf stops", "So main disappears", "So arrays become strings"), "So it can change the original value", "Pointers let functions work with caller data.")
            )
            9 -> listOf(
                mc(unit, 1, "What should you read first in a compiler error?", listOf("Line number and message", "Only app color", "Only the keyboard", "Nothing"), "Line number and message", "The line and message are the best starting point."),
                mc(unit, 2, "Which missing symbol often breaks C statements?", listOf("Semicolon", "Emoji", "Package", "Class"), "Semicolon", "Most C statements need semicolons."),
                drag(unit, 3, "Complete the safe division check.", "if (b ______ 0) {\n    printf(\"%d\\n\", a / b);\n}", listOf("!=", "==", "=", "<"), listOf("!="), "Check b != 0 before dividing."),
                mc(unit, 4, "Why do C warnings matter?", listOf("They can reveal unsafe code", "They always mean success", "They change colors", "They skip tests"), "They can reveal unsafe code", "Warnings often point to bugs before they hurt.")
            )
            else -> listOf(
                mc(unit, 1, "Which tool repeats code?", listOf("loop", "char", "include", "return"), "loop", "Loops repeat code."),
                mc(unit, 2, "Which tool stores many same-type values?", listOf("array", "if", "printf", "main"), "array", "Arrays store multiple values of the same type."),
                mc(unit, 3, "Which tool organizes reusable code?", listOf("function", "index", "warning", "specifier"), "function", "Functions organize reusable code."),
                drag(unit, 4, "Complete the mixed condition.", "if (scores[i] ______ best) {\n    best = scores[i];\n}", listOf(">", "<", "==", "return"), listOf(">"), "Use > when looking for a larger score.")
            )
        }

        return when (part) {
            1 -> base.take(3)
            2 -> base.drop(1).take(3)
            else -> base
        }
    }

    private fun mc(unit: Int, number: Int, question: String, options: List<String>, answer: String, explanation: String) =
        PracticeQuestion("c_u${unit}_mc_$number", PracticeQuestionType.MULTIPLE_CHOICE, "Question $number", question, options = options, correctAnswer = answer, explanation = explanation)

    private fun drag(unit: Int, number: Int, question: String, code: String, options: List<String>, answers: List<String>, explanation: String) =
        PracticeQuestion("c_u${unit}_drag_$number", PracticeQuestionType.DRAG_FILL_BLANK, "Question $number", question, code = code.trimIndent(), options = options, correctAnswer = answers.first(), correctAnswers = answers, explanation = explanation)

    private fun fill(unit: Int, number: Int, question: String, code: String?, answer: String, explanation: String) =
        PracticeQuestion("c_u${unit}_fill_$number", PracticeQuestionType.FILL_BLANK, "Question $number", question, code = code?.trimIndent(), correctAnswer = answer, explanation = explanation)
}
