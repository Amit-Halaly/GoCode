package com.example.gocode.lessons

object CppPracticeRepository {

    fun getQuestions(nodeId: String): List<PracticeQuestion> {
        val unit = Regex("""cpp_u(\d+)_""").find(nodeId)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
        val part = when {
            nodeId.contains("_p2") -> 2
            nodeId.contains("_q1") -> 3
            else -> 1
        }
        val base = questionsFor(unit)
        return when (part) {
            1 -> base.take(3)
            2 -> base.drop(1).take(3)
            else -> base
        }
    }

    private fun questionsFor(unit: Int): List<PracticeQuestion> {
        return when (unit) {
            1 -> listOf(
                mc(unit, 1, "Which header gives C++ access to cout?", listOf("#include <iostream>", "#include <cout>", "import Console", "#include <stdio.h>"), "#include <iostream>", "cout is declared through iostream."),
                drag(unit, 2, "Complete the first C++ print.", "#include <iostream>\nusing namespace std;\n\nint main() {\n    ______ << \"Hello C++\" << ______;\n    return 0;\n}", listOf("cout", "endl", "print", "cin"), listOf("cout", "endl"), "cout prints and endl moves to a new line."),
                mc(unit, 3, "Which type stores true or false?", listOf("bool", "string", "int", "char"), "bool", "bool stores true or false."),
                drag(unit, 4, "Complete the string variable.", "#include <string>\n______ name = \"Leo\";", listOf("string", "char", "bool", "text"), listOf("string"), "string stores text in C++.")
            )
            2 -> listOf(
                mc(unit, 1, "Which operator checks equality?", listOf("==", "=", "!=", ">="), "==", "== compares values. = assigns a value."),
                drag(unit, 2, "Complete the age check.", "if (age ______ 13) {\n    cout << \"Welcome\" << endl;\n}", listOf(">=", "<=", "=", "else"), listOf(">="), "age >= 13 checks age 13 or older."),
                mc(unit, 3, "What does && mean?", listOf("Both conditions must be true", "Either condition can be true", "Assign a value", "End the program"), "Both conditions must be true", "&& is logical AND."),
                drag(unit, 4, "Complete the if / else shape.", "______ (score >= 75) {\n    cout << \"Passed\" << endl;\n} ______ {\n    cout << \"Try again\" << endl;\n}", listOf("if", "else", "while", "return"), listOf("if", "else"), "if starts the true branch; else handles false.")
            )
            3 -> listOf(
                mc(unit, 1, "Which loop is compact for counting?", listOf("for", "string", "include", "return"), "for", "A for loop is compact for counting."),
                drag(unit, 2, "Complete the for loop.", "for (int i = 1; i ______ 5; i______ ) {\n    cout << i << endl;\n}", listOf("<=", "++", "--", "=="), listOf("<=", "++"), "The loop runs while i <= 5 and increments with i++."),
                mc(unit, 3, "What can cause an infinite loop?", listOf("The condition never becomes false", "Using cout", "A vector", "A string"), "The condition never becomes false", "A loop needs a stopping point."),
                fill(unit, 4, "Complete the increment operator.", "count______;", "++", "count++ adds one.")
            )
            4 -> listOf(
                mc(unit, 1, "Which container is a resizable list in C++?", listOf("vector", "if", "cout", "bool"), "vector", "vector stores many values and can grow."),
                drag(unit, 2, "Complete the vector code.", "vector<int> scores = {90, 75};\nscores.______(88);\ncout << scores.______() << endl;", listOf("push_back", "size", "length", "append"), listOf("push_back", "size"), "push_back adds and size() counts items."),
                mc(unit, 3, "What is the first index in a vector?", listOf("0", "1", "-1", "size"), "0", "Vector indexes start at 0."),
                drag(unit, 4, "Complete the range loop.", "for (int score ______ scores) {\n    cout << score << endl;\n}", listOf(":", "in", "=", "of"), listOf(":"), "Range-based for loops use a colon.")
            )
            5 -> listOf(
                mc(unit, 1, "What is a function?", listOf("A named reusable block of code", "Only a variable", "Only a header", "A vector index"), "A named reusable block of code", "Functions organize reusable logic."),
                drag(unit, 2, "Complete the function that returns double.", "______ doubleNumber(int n) {\n    ______ n * 2;\n}", listOf("int", "return", "void", "cout"), listOf("int", "return"), "The function returns an int, so it uses int and return."),
                mc(unit, 3, "What does void mean?", listOf("No return value", "A string", "A vector", "A compiler"), "No return value", "void functions do not send back a value."),
                fill(unit, 4, "Complete the call: sayHello______;", null, "()", "Function calls use parentheses.")
            )
            6 -> listOf(
                mc(unit, 1, "Which object reads input in beginner C++?", listOf("cin", "cout", "println", "input"), "cin", "cin reads input from standard input."),
                drag(unit, 2, "Complete reading an int.", "int age;\n______ >> age;", listOf("cin", "cout", "scanf", "read"), listOf("cin"), "cin >> age reads a value into age."),
                mc(unit, 3, "Why print a prompt before input?", listOf("So the user knows what to type", "So vectors grow", "So main starts", "So loops stop"), "So the user knows what to type", "Prompts make console programs clearer."),
                drag(unit, 4, "Read age and decide.", "cin >> age;\nif (age ______ 13) {\n    cout << \"Welcome\" << endl;\n}", listOf(">=", "==", "<", "else"), listOf(">="), "Compare the input after reading it.")
            )
            7 -> listOf(
                mc(unit, 1, "Which method counts characters in a string?", listOf("length()", "push_back()", "cin()", "main()"), "length()", "length() returns the number of characters."),
                drag(unit, 2, "Complete the string check.", "if (name ______ \"Leo\") {\n    cout << \"Found\" << endl;\n}", listOf("==", "=", "!=", "find"), listOf("=="), "C++ strings can be compared with ==."),
                mc(unit, 3, "What does find return when text is missing?", listOf("string::npos", "0", "true", "cout"), "string::npos", "find returns string::npos when the text is not found."),
                fill(unit, 4, "Complete the first character access: name[______]", null, "0", "Index 0 reads the first character.")
            )
            8 -> listOf(
                mc(unit, 1, "What is a class?", listOf("A blueprint for objects", "Only a loop", "Only a number", "A console input"), "A blueprint for objects", "Classes describe data and behavior."),
                drag(unit, 2, "Complete the class field.", "class Student {\npublic:\n    ______ name;\n};", listOf("string", "cin", "void", "for"), listOf("string"), "name is text, so string fits."),
                mc(unit, 3, "How do you access an object's field?", listOf("student.name", "student->name only", "student name", "name.student"), "student.name", "Dot syntax accesses members on an object."),
                drag(unit, 4, "Complete the method call.", "Student student;\nstudent.______();", listOf("introduce", "class", "public", "return"), listOf("introduce"), "Call a method with object.method().")
            )
            9 -> listOf(
                mc(unit, 1, "What should you read first in a compiler error?", listOf("Line number and message", "Only app color", "Only the keyboard", "Nothing"), "Line number and message", "The line and message are the best starting point."),
                mc(unit, 2, "Which missing symbol often breaks C++ statements?", listOf("Semicolon", "Package", "Question mark", "Class file"), "Semicolon", "Most C++ statements need semicolons."),
                drag(unit, 3, "Complete the try / catch structure.", "______ {\n    int n = stoi(text);\n} ______ (exception& e) {\n    cout << \"Invalid\" << endl;\n}", listOf("try", "catch", "if", "for"), listOf("try", "catch"), "Risky code goes in try; fallback goes in catch."),
                mc(unit, 4, "Why use debug output?", listOf("To inspect values while code runs", "To skip compiling", "To create vectors automatically", "To remove main"), "To inspect values while code runs", "Debug output reveals program state.")
            )
            else -> listOf(
                mc(unit, 1, "Which tool repeats code?", listOf("loop", "string", "bool", "include"), "loop", "Loops repeat code."),
                mc(unit, 2, "Which tool stores many values?", listOf("vector", "if", "return", "catch"), "vector", "Vectors store many values."),
                mc(unit, 3, "Which tool groups data and behavior?", listOf("class", "index", "input", "error"), "class", "Classes group fields and methods."),
                drag(unit, 4, "Complete the mixed code.", "for (string name : names) {\n    if (name.______() > 3) {\n        cout << name << endl;\n    }\n}", listOf("length", "push_back", "size", "cin"), listOf("length"), "length() checks the string size.")
            )
        }
    }

    private fun mc(unit: Int, number: Int, question: String, options: List<String>, answer: String, explanation: String) =
        PracticeQuestion("cpp_u${unit}_mc_$number", PracticeQuestionType.MULTIPLE_CHOICE, "Question $number", question, options = options, correctAnswer = answer, explanation = explanation)

    private fun drag(unit: Int, number: Int, question: String, code: String, options: List<String>, answers: List<String>, explanation: String) =
        PracticeQuestion("cpp_u${unit}_drag_$number", PracticeQuestionType.DRAG_FILL_BLANK, "Question $number", question, code = code.trimIndent(), options = options, correctAnswer = answers.first(), correctAnswers = answers, explanation = explanation)

    private fun fill(unit: Int, number: Int, question: String, code: String?, answer: String, explanation: String) =
        PracticeQuestion("cpp_u${unit}_fill_$number", PracticeQuestionType.FILL_BLANK, "Question $number", question, code = code?.trimIndent(), correctAnswer = answer, explanation = explanation)
}
