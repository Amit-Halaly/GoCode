package com.example.gocode.lessons

object CLessonsRepository {

    fun getSteps(nodeId: String): List<LessonStep> {
        return when (nodeId) {
            "c_u1_l1" -> lesson("c_s1_l1", "#include + printf", listOf(
                Step("C starts from main", "A C program starts running inside main().\n\nFor console output, include stdio.h and call printf().", "#include <stdio.h>\n\nint main() {\n    printf(\"Hello C\\n\");\n    return 0;\n}", "return 0 tells the operating system the program ended successfully."),
                Step("printf prints text", "Text goes inside double quotes. Use \\n when you want a new line.", "printf(\"GoCode\\n\");", "\\n is one character: a newline."),
                Step("Semicolons matter", "Most C statements end with a semicolon. Missing one is a common beginner compile error.", "printf(\"Ready\\n\");")
            ))
            "c_u1_l2" -> lesson("c_s1_l2", "Variables + Types", listOf(
                Step("Variables store values", "C needs a type before each variable name.", "int age = 14;\ndouble price = 19.99;\nchar grade = 'A';"),
                Step("Print variables", "printf uses format specifiers as placeholders.", "int score = 90;\nprintf(\"%d\\n\", score);", "%d prints an int."),
                Step("Common specifiers", "%d is for int, %f is for double, %c is for char, and %s is for strings.", "printf(\"%c %d\\n\", 'A', 100);")
            ))
            "c_u2_l1" -> lesson("c_s2_l1", "Comparisons + If", listOf(
                Step("Conditions are true or false", "C uses comparisons like >, <, >=, <=, ==, and != inside if statements.", "int score = 90;\nif (score >= 75) {\n    printf(\"Passed\\n\");\n}"),
                Step("Use == to compare", "A single = assigns a value. Double == checks equality.", "if (age == 13) {\n    printf(\"Teen\\n\");\n}"),
                Step("Blocks use braces", "The code inside braces runs only when the condition is true.")
            ))
            "c_u2_l2" -> lesson("c_s2_l2", "Else + Logic", listOf(
                Step("else handles false", "Use else when you need a second path.", "if (age >= 13) {\n    printf(\"Welcome\\n\");\n} else {\n    printf(\"Too young\\n\");\n}"),
                Step("&& means AND", "Use && when both conditions must be true.", "if (age >= 13 && hasPassword) {\n    printf(\"Access granted\\n\");\n}"),
                Step("|| means OR", "Use || when at least one condition is enough.", "if (isAdmin || isTeacher) {\n    printf(\"Allowed\\n\");\n}")
            ))
            "c_u3_l1" -> lesson("c_s3_l1", "While Loops", listOf(
                Step("while repeats code", "A while loop repeats while its condition stays true.", "int count = 1;\nwhile (count <= 3) {\n    printf(\"%d\\n\", count);\n    count++;\n}"),
                Step("Update the counter", "A loop needs something that moves it toward stopping.", "count++;", "count++ adds 1."),
                Step("Avoid infinite loops", "If the condition never becomes false, the program keeps running.")
            ))
            "c_u3_l2" -> lesson("c_s3_l2", "For Loops", listOf(
                Step("for loops are compact", "A for loop puts start, condition, and update in one line.", "for (int i = 1; i <= 5; i++) {\n    printf(\"%d\\n\", i);\n}"),
                Step("Count down", "Use -- to subtract one each repeat.", "for (int i = 3; i >= 1; i--) {\n    printf(\"%d\\n\", i);\n}"),
                Step("Loops and if work together", "You can place decisions inside loops to react on each repeat.")
            ))
            "c_u4_l1" -> lesson("c_s4_l1", "Arrays", listOf(
                Step("Arrays store many values", "An array keeps several values of the same type.", "int scores[] = {90, 75, 88};"),
                Step("Read by index", "Indexes start at 0.", "printf(\"%d\\n\", scores[0]);", "This prints the first score."),
                Step("Loop through an array", "C arrays do not remember their length automatically, so keep track of the size.", "int size = 3;\nfor (int i = 0; i < size; i++) {\n    printf(\"%d\\n\", scores[i]);\n}")
            ))
            "c_u4_l2" -> lesson("c_s4_l2", "Array Indexes", listOf(
                Step("Last index", "If an array has 3 values, the last index is 2.", "scores[2]"),
                Step("Change a value", "Assign to an index to update one item.", "scores[1] = 80;"),
                Step("Stay inside bounds", "Reading outside the array is unsafe in C. Always keep indexes between 0 and size - 1.")
            ))
            "c_u5_l1" -> lesson("c_s5_l1", "Functions", listOf(
                Step("Functions organize code", "A function is a named block of reusable code.", "void sayHello() {\n    printf(\"Hello\\n\");\n}"),
                Step("Call a function", "A function runs when you call it by name.", "sayHello();"),
                Step("void means no return value", "Use void when the function does work but does not send back a value.")
            ))
            "c_u5_l2" -> lesson("c_s5_l2", "Parameters + Return", listOf(
                Step("Parameters receive values", "Write the type and name inside the parentheses.", "void greet(char name[]) {\n    printf(\"Hello %s\\n\", name);\n}"),
                Step("Return a value", "Use return when a function calculates a result.", "int doubleNumber(int n) {\n    return n * 2;\n}"),
                Step("Store returned values", "Returned values can be assigned to variables.", "int result = doubleNumber(4);")
            ))
            "c_u6_l1" -> lesson("c_s6_l1", "scanf Input", listOf(
                Step("Read input with scanf", "scanf reads values from standard input.", "int age;\nscanf(\"%d\", &age);", "The & gives scanf the address where the value should be stored."),
                Step("Match the format", "Use the right specifier for the variable type.", "double price;\nscanf(\"%lf\", &price);"),
                Step("Input makes programs interactive", "After reading input, use the variable like any other value.")
            ))
            "c_u6_l2" -> lesson("c_s6_l2", "Input Decisions", listOf(
                Step("Read and decide", "Combine scanf with if / else.", "int age;\nscanf(\"%d\", &age);\n\nif (age >= 13) {\n    printf(\"Welcome\\n\");\n}"),
                Step("Prompts help users", "Print a short message before reading input.", "printf(\"Enter age: \");\nscanf(\"%d\", &age);")
            ))
            "c_u7_l1" -> lesson("c_s7_l1", "Strings as Char Arrays", listOf(
                Step("C strings are char arrays", "A beginner C string is usually a char array ending with a special null character.", "char name[] = \"Leo\";"),
                Step("Print strings with %s", "Use %s to print a string.", "printf(\"%s\\n\", name);"),
                Step("Include string.h for string tools", "String helper functions live in string.h.", "#include <string.h>")
            ))
            "c_u7_l2" -> lesson("c_s7_l2", "String Checks", listOf(
                Step("Compare strings with strcmp", "Do not use == to compare string text in C. Use strcmp().", "if (strcmp(name, \"Leo\") == 0) {\n    printf(\"Found Leo\\n\");\n}"),
                Step("String length", "strlen() counts characters before the null ending.", "printf(\"%zu\\n\", strlen(name));")
            ))
            "c_u8_l1" -> lesson("c_s8_l1", "Pointers", listOf(
                Step("A pointer stores an address", "A pointer remembers where another value lives in memory.", "int score = 90;\nint *ptr = &score;"),
                Step("& gets an address", "Use & before a variable to get its address.", "&score"),
                Step("* reads through a pointer", "Use * to access the value at the address.", "printf(\"%d\\n\", *ptr);")
            ))
            "c_u8_l2" -> lesson("c_s8_l2", "Pointers + Arrays", listOf(
                Step("Arrays and pointers are connected", "An array name points to the first value.", "int scores[] = {10, 20, 30};\nprintf(\"%d\\n\", scores[0]);"),
                Step("Pass addresses to functions", "Functions can change caller variables when they receive pointers.", "void addOne(int *n) {\n    (*n)++;\n}")
            ))
            "c_u9_l1" -> lesson("c_s9_l1", "Reading Errors", listOf(
                Step("Compiler errors are clues", "C compiler messages usually include a file, line, and explanation."),
                Step("Common C mistakes", "Check semicolons, braces, format specifiers, and missing includes first."),
                Step("Warnings matter", "C warnings can point to unsafe code even when compilation succeeds.")
            ))
            "c_u9_l2" -> lesson("c_s9_l2", "Debugging C", listOf(
                Step("Print values to debug", "A focused printf can reveal what a variable contains.", "printf(\"age = %d\\n\", age);"),
                Step("Check before dividing", "Avoid dividing by zero by checking first.", "if (b != 0) {\n    printf(\"%d\\n\", a / b);\n}"),
                Step("Fix one issue at a time", "Start with the first compiler message, then run again.")
            ))
            "c_u10_l1" -> lesson("c_s10_l1", "Final Review", listOf(
                Step("You know the core C pieces", "You learned program shape, variables, conditions, loops, arrays, functions, input, strings, pointers, and debugging."),
                Step("Think in memory and values", "C rewards careful thinking: which value do I have, and where is it stored?"),
                Step("Build in small steps", "Write a little, run, read the result, then continue.")
            ))
            "c_u10_l2" -> lesson("c_s10_l2", "Build Confidence", listOf(
                Step("C is precise", "Small syntax details matter, but the patterns repeat."),
                Step("Ready for more", "After this path, you are ready for structs, files, dynamic memory, and deeper systems programming.", tip = "The final quiz mixes ideas from the whole C path.")
            ))
            else -> getSteps("c_u1_l1")
        }
    }

    private fun lesson(prefix: String, title: String, steps: List<Step>): List<LessonStep> {
        return steps.mapIndexed { index, step ->
            LessonStep(
                id = "${prefix}_s${index + 1}",
                title = if (index == 0) title else step.title,
                body = step.body,
                code = step.code,
                tip = step.tip
            )
        }
    }

    private data class Step(
        val title: String,
        val body: String,
        val code: String? = null,
        val tip: String? = null
    )
}
