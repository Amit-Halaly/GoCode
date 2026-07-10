package com.example.gocode.lessons

object CppLessonsRepository {

    fun getSteps(nodeId: String): List<LessonStep> {
        val unit = Regex("""cpp_u(\d+)_""").find(nodeId)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
        val isSecondLesson = nodeId.contains("_l2")
        val content = units.getOrElse(unit - 1) { units.first() }
        val lesson = if (isSecondLesson) content.secondLesson else content.firstLesson
        val prefix = "cpp_s${unit}_${if (isSecondLesson) "l2" else "l1"}"
        return lesson(prefix, lesson.title, lesson.steps)
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

    private data class UnitContent(
        val firstLesson: LessonContent,
        val secondLesson: LessonContent
    )

    private data class LessonContent(
        val title: String,
        val steps: List<Step>
    )

    private data class Step(
        val title: String,
        val body: String,
        val code: String? = null,
        val tip: String? = null
    )

    private val units = listOf(
        UnitContent(
            LessonContent("iostream + cout", listOf(
                Step("C++ starts from main", "A C++ program starts running inside main(). Include iostream to print to the console.", "#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << \"Hello C++\" << endl;\n    return 0;\n}"),
                Step("cout sends output", "Use cout with << to send text and values to the console.", "cout << \"GoCode\" << endl;", "endl moves to a new line."),
                Step("Semicolons still matter", "Most C++ statements end with a semicolon.")
            )),
            LessonContent("Variables + Types", listOf(
                Step("Typed variables", "C++ variables need a type before the name.", "int age = 14;\ndouble price = 19.99;\nchar grade = 'A';\nbool ready = true;"),
                Step("Strings need <string>", "Use string for text and include the string header.", "#include <string>\nstring name = \"Leo\";"),
                Step("Print variables", "cout can print variables and text in one line.", "cout << name << \" scored \" << score << endl;")
            ))
        ),
        UnitContent(
            LessonContent("Comparisons + if", listOf(
                Step("Conditions choose paths", "C++ uses comparisons like >, <, >=, <=, ==, and != inside if statements.", "if (score >= 75) {\n    cout << \"Passed\" << endl;\n}"),
                Step("Compare with ==", "A single = assigns. Double == compares.", "if (age == 13) {\n    cout << \"Teen\" << endl;\n}"),
                Step("Blocks use braces", "The code inside braces runs only when the condition is true.")
            )),
            LessonContent("else + logic", listOf(
                Step("else handles false", "Use else when the program needs a fallback path.", "if (age >= 13) {\n    cout << \"Welcome\" << endl;\n} else {\n    cout << \"Too young\" << endl;\n}"),
                Step("&& means AND", "Use && when both conditions must be true.", "if (age >= 13 && hasPassword) {\n    cout << \"Access granted\" << endl;\n}"),
                Step("|| means OR", "Use || when either condition is enough.")
            ))
        ),
        UnitContent(
            LessonContent("while loops", listOf(
                Step("while repeats", "A while loop repeats while its condition remains true.", "int count = 1;\nwhile (count <= 3) {\n    cout << count << endl;\n    count++;\n}"),
                Step("Update the counter", "A loop needs a change that moves it toward stopping.", "count++;"),
                Step("Watch for endless loops", "If the condition never becomes false, the program keeps running.")
            )),
            LessonContent("for loops", listOf(
                Step("for is compact", "A for loop keeps the start, condition, and update together.", "for (int i = 1; i <= 5; i++) {\n    cout << i << endl;\n}"),
                Step("Loop over containers", "A range-based for loop reads every value in a vector.", "for (int score : scores) {\n    cout << score << endl;\n}"),
                Step("Loops combine with if", "Use decisions inside loops to filter values.")
            ))
        ),
        UnitContent(
            LessonContent("Vectors", listOf(
                Step("Vectors store many values", "A vector is a resizable list of values with the same type.", "#include <vector>\nvector<int> scores = {90, 75, 88};"),
                Step("Read by index", "Indexes start at 0.", "cout << scores[0] << endl;"),
                Step("Count with size", "Use size() to know how many values are inside.", "cout << scores.size() << endl;")
            )),
            LessonContent("Vector changes", listOf(
                Step("Add with push_back", "push_back adds a value to the end of a vector.", "scores.push_back(95);"),
                Step("Loop through a vector", "Use a range-based for loop when you only need each value.", "for (int score : scores) {\n    cout << score << endl;\n}"),
                Step("Stay inside bounds", "Only read indexes from 0 to size() - 1.")
            ))
        ),
        UnitContent(
            LessonContent("Functions", listOf(
                Step("Functions organize code", "A function is a named reusable block.", "void sayHello() {\n    cout << \"Hello\" << endl;\n}"),
                Step("Call a function", "A function runs when you call it by name.", "sayHello();"),
                Step("void means no returned value", "Use void when the function performs an action.")
            )),
            LessonContent("Parameters + return", listOf(
                Step("Parameters receive values", "Write the type and name inside the parentheses.", "void greet(string name) {\n    cout << \"Hello \" << name << endl;\n}"),
                Step("Return a value", "Use return when a function calculates a result.", "int doubleNumber(int n) {\n    return n * 2;\n}"),
                Step("Store returned values", "Returned values can be assigned to variables.", "int result = doubleNumber(4);")
            ))
        ),
        UnitContent(
            LessonContent("cin input", listOf(
                Step("Read input with cin", "cin reads values from standard input using >>.", "int age;\ncin >> age;"),
                Step("Read text", "For one word, cin can read into a string.", "string name;\ncin >> name;"),
                Step("Input becomes data", "After reading input, use the variable like any other value.")
            )),
            LessonContent("Input decisions", listOf(
                Step("Read and decide", "Combine cin with if / else to make interactive programs.", "int age;\ncin >> age;\nif (age >= 13) {\n    cout << \"Welcome\" << endl;\n}"),
                Step("Prompts help users", "Print a short message before reading input.", "cout << \"Enter age: \";\ncin >> age;")
            ))
        ),
        UnitContent(
            LessonContent("String tools", listOf(
                Step("Strings have helpers", "C++ string values can report their length and access characters.", "string name = \"Leo\";\ncout << name.length() << endl;"),
                Step("Compare strings", "Use == to compare string text.", "if (name == \"Leo\") {\n    cout << \"Found Leo\" << endl;\n}"),
                Step("Read characters", "Use indexes to read characters in a string.", "cout << name[0] << endl;")
            )),
            LessonContent("String checks", listOf(
                Step("Find text", "find returns the position of text, or string::npos when not found.", "if (email.find(\"@\") != string::npos) {\n    cout << \"Valid\" << endl;\n}"),
                Step("Build text", "Use + to combine strings.", "string message = \"Hello \" + name;")
            ))
        ),
        UnitContent(
            LessonContent("Classes", listOf(
                Step("Classes describe objects", "A class groups data and behavior into a custom type.", "class Student {\npublic:\n    string name;\n    int age;\n};"),
                Step("Create an object", "An object is a value made from a class.", "Student student;\nstudent.name = \"Maya\";"),
                Step("public exposes members", "Beginner examples often use public so main can access fields.")
            )),
            LessonContent("Methods", listOf(
                Step("Methods belong to classes", "A method is a function inside a class.", "class Student {\npublic:\n    string name;\n    void introduce() {\n        cout << name << endl;\n    }\n};"),
                Step("Call with dot", "Use the dot to call a method on an object.", "student.introduce();")
            ))
        ),
        UnitContent(
            LessonContent("Reading errors", listOf(
                Step("Compiler errors are clues", "C++ compiler messages usually include a file, line, and explanation."),
                Step("Common mistakes", "Check includes, semicolons, braces, and type names first."),
                Step("Warnings matter", "Warnings can point to risky code even when compilation succeeds.")
            )),
            LessonContent("try / catch", listOf(
                Step("Handle runtime errors", "try / catch lets a program respond to risky operations.", "try {\n    int number = stoi(text);\n    cout << number << endl;\n} catch (exception& e) {\n    cout << \"Invalid number\" << endl;\n}"),
                Step("Debug with output", "A focused cout can reveal what a value contains.", "cout << \"age = \" << age << endl;")
            ))
        ),
        UnitContent(
            LessonContent("Final review", listOf(
                Step("You know the core C++ pieces", "You learned output, variables, decisions, loops, vectors, functions, input, strings, classes, and debugging."),
                Step("Think in types and flow", "Ask what type each value has, what repeats, and which path should run.")
            )),
            LessonContent("Build confidence", listOf(
                Step("C++ rewards precision", "Small syntax details matter, but the patterns repeat."),
                Step("Ready for more", "After this path, you are ready for references, files, inheritance, and modern C++ projects.", tip = "The final quiz mixes the whole C++ path.")
            ))
        )
    )
}
