package com.example.gocode.lessons

object CSharpPracticeRepository {

    fun getQuestions(nodeId: String): List<PracticeQuestion> {
        val unit = Regex("""cs_u(\d+)_""").find(nodeId)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
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
                mc(unit, 1, "Which command prints a line in C#?", listOf("Console.WriteLine()", "System.out.println()", "cout", "printf"), "Console.WriteLine()", "Console.WriteLine prints output in C#."),
                drag(unit, 2, "Complete the first C# print.", "Console.______(\"Hello C#\");", listOf("WriteLine", "ReadLine", "Print", "cout"), listOf("WriteLine"), "Console.WriteLine prints a line."),
                mc(unit, 3, "Which type stores true or false?", listOf("bool", "string", "int", "char"), "bool", "bool stores true or false."),
                drag(unit, 4, "Complete the string variable.", "______ name = \"Leo\";", listOf("string", "char", "bool", "text"), listOf("string"), "string stores text in C#.")
            )
            2 -> listOf(
                mc(unit, 1, "Which operator checks equality?", listOf("==", "=", "!=", ">="), "==", "== compares values. = assigns a value."),
                drag(unit, 2, "Complete the age check.", "if (age ______ 13) {\n    Console.WriteLine(\"Welcome\");\n}", listOf(">=", "<=", "=", "else"), listOf(">="), "age >= 13 checks age 13 or older."),
                mc(unit, 3, "What does && mean?", listOf("Both conditions must be true", "Either condition can be true", "Assign a value", "End the program"), "Both conditions must be true", "&& is logical AND."),
                drag(unit, 4, "Complete the if / else shape.", "______ (score >= 75) {\n    Console.WriteLine(\"Passed\");\n} ______ {\n    Console.WriteLine(\"Try again\");\n}", listOf("if", "else", "while", "return"), listOf("if", "else"), "if starts the true branch; else handles false.")
            )
            3 -> listOf(
                mc(unit, 1, "Which loop is compact for counting?", listOf("for", "string", "using", "return"), "for", "A for loop is compact for counting."),
                drag(unit, 2, "Complete the for loop.", "for (int i = 1; i ______ 5; i______ ) {\n    Console.WriteLine(i);\n}", listOf("<=", "++", "--", "=="), listOf("<=", "++"), "The loop runs while i <= 5 and increments with i++."),
                mc(unit, 3, "Which loop reads every value in an array?", listOf("foreach", "class", "using", "catch"), "foreach", "foreach reads each item."),
                fill(unit, 4, "Complete the increment operator.", "count______;", "++", "count++ adds one.")
            )
            4 -> listOf(
                mc(unit, 1, "Which value creates an int array?", listOf("int[] scores = {1, 2};", "int scores = [1, 2];", "array scores int;", "scores = int{}"), "int[] scores = {1, 2};", "C# arrays use type[] syntax."),
                drag(unit, 2, "Complete the array loop.", "for (int i = 0; i < scores.______; i++) {\n    Console.WriteLine(scores[______]);\n}", listOf("Length", "i", "size", "score"), listOf("Length", "i"), "Use Length for the limit and i as the index."),
                mc(unit, 3, "What is the first index in an array?", listOf("0", "1", "-1", "Length"), "0", "Array indexes start at 0."),
                drag(unit, 4, "Complete the foreach loop.", "foreach (int score ______ scores) {\n    Console.WriteLine(score);\n}", listOf("in", ":", "=", "of"), listOf("in"), "C# foreach uses in.")
            )
            5 -> listOf(
                mc(unit, 1, "What is a method?", listOf("A named reusable block of code", "Only a variable", "Only a namespace", "An array index"), "A named reusable block of code", "Methods organize reusable logic."),
                drag(unit, 2, "Complete the method that returns double.", "static ______ DoubleNumber(int n) {\n    ______ n * 2;\n}", listOf("int", "return", "void", "Console"), listOf("int", "return"), "The method returns an int, so it uses int and return."),
                mc(unit, 3, "What does void mean?", listOf("No return value", "A string", "An array", "A compiler"), "No return value", "void methods do not send back a value."),
                fill(unit, 4, "Complete the call: SayHello______;", null, "()", "Method calls use parentheses.")
            )
            6 -> listOf(
                mc(unit, 1, "Which method reads console input as text?", listOf("Console.ReadLine()", "Console.WriteLine()", "input()", "cin"), "Console.ReadLine()", "Console.ReadLine reads text input."),
                drag(unit, 2, "Complete reading an int.", "int age = int.______(Console.ReadLine());", listOf("Parse", "Read", "Length", "ToString"), listOf("Parse"), "int.Parse converts text to an int."),
                mc(unit, 3, "Why print a prompt before input?", listOf("So the user knows what to type", "So arrays grow", "So Main starts", "So loops stop"), "So the user knows what to type", "Prompts make console programs clearer."),
                drag(unit, 4, "Read age and decide.", "int age = int.Parse(Console.ReadLine());\nif (age ______ 13) {\n    Console.WriteLine(\"Welcome\");\n}", listOf(">=", "==", "<", "else"), listOf(">="), "Compare the input after reading it.")
            )
            7 -> listOf(
                mc(unit, 1, "Which property counts characters in a string?", listOf("Length", "Count", "Size", "Index"), "Length", "Length returns the number of characters."),
                drag(unit, 2, "Complete the string check.", "if (name ______ \"Leo\") {\n    Console.WriteLine(\"Found\");\n}", listOf("==", "=", "!=", "Contains"), listOf("=="), "C# strings can be compared with ==."),
                mc(unit, 3, "Which method checks whether text appears inside a string?", listOf("Contains()", "Push()", "ReadLine()", "Main()"), "Contains()", "Contains checks for text inside another string."),
                fill(unit, 4, "Complete the first character access: name[______]", null, "0", "Index 0 reads the first character.")
            )
            8 -> listOf(
                mc(unit, 1, "What is a class?", listOf("A blueprint for objects", "Only a loop", "Only a number", "A console input"), "A blueprint for objects", "Classes describe data and behavior."),
                drag(unit, 2, "Complete the class field.", "class Student {\n    public ______ Name;\n}", listOf("string", "Console", "void", "for"), listOf("string"), "Name is text, so string fits."),
                mc(unit, 3, "Which keyword creates a new object?", listOf("new", "class", "if", "return"), "new", "new creates an object."),
                drag(unit, 4, "Complete the object code.", "Student student = ______ Student();\nstudent.______ = \"Maya\";", listOf("new", "Name", "class", "string"), listOf("new", "Name"), "Use new Student() and then set student.Name.")
            )
            9 -> listOf(
                mc(unit, 1, "What should you read first in a compiler error?", listOf("Line number and message", "Only app color", "Only the keyboard", "Nothing"), "Line number and message", "The line and message are the best starting point."),
                mc(unit, 2, "Which missing symbol often breaks C# statements?", listOf("Semicolon", "Package", "Question mark", "Header"), "Semicolon", "Most C# statements need semicolons."),
                drag(unit, 3, "Complete the try / catch structure.", "______ {\n    int n = int.Parse(text);\n} ______ (Exception) {\n    Console.WriteLine(\"Invalid\");\n}", listOf("try", "catch", "if", "for"), listOf("try", "catch"), "Risky code goes in try; fallback goes in catch."),
                mc(unit, 4, "Why use debug output?", listOf("To inspect values while code runs", "To skip compiling", "To create arrays automatically", "To remove Main"), "To inspect values while code runs", "Debug output reveals program state.")
            )
            else -> listOf(
                mc(unit, 1, "Which tool repeats code?", listOf("loop", "string", "bool", "using"), "loop", "Loops repeat code."),
                mc(unit, 2, "Which tool stores many same-type values?", listOf("array", "if", "return", "catch"), "array", "Arrays store many values."),
                mc(unit, 3, "Which tool groups data and behavior?", listOf("class", "index", "input", "error"), "class", "Classes group fields and methods."),
                drag(unit, 4, "Complete the mixed code.", "foreach (string name in names) {\n    if (name.______ > 3) {\n        Console.WriteLine(name);\n    }\n}", listOf("Length", "Parse", "Count", "ReadLine"), listOf("Length"), "Length checks the string size.")
            )
        }
    }

    private fun mc(unit: Int, number: Int, question: String, options: List<String>, answer: String, explanation: String) =
        PracticeQuestion("cs_u${unit}_mc_$number", PracticeQuestionType.MULTIPLE_CHOICE, "Question $number", question, options = options, correctAnswer = answer, explanation = explanation)

    private fun drag(unit: Int, number: Int, question: String, code: String, options: List<String>, answers: List<String>, explanation: String) =
        PracticeQuestion("cs_u${unit}_drag_$number", PracticeQuestionType.DRAG_FILL_BLANK, "Question $number", question, code = code.trimIndent(), options = options, correctAnswer = answers.first(), correctAnswers = answers, explanation = explanation)

    private fun fill(unit: Int, number: Int, question: String, code: String?, answer: String, explanation: String) =
        PracticeQuestion("cs_u${unit}_fill_$number", PracticeQuestionType.FILL_BLANK, "Question $number", question, code = code?.trimIndent(), correctAnswer = answer, explanation = explanation)
}
