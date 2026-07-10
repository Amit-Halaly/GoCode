package com.example.gocode.lessons

object CSharpLessonsRepository {

    fun getSteps(nodeId: String): List<LessonStep> {
        val unit = Regex("""cs_u(\d+)_""").find(nodeId)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
        val isSecondLesson = nodeId.contains("_l2")
        val content = units.getOrElse(unit - 1) { units.first() }
        val lesson = if (isSecondLesson) content.secondLesson else content.firstLesson
        val prefix = "cs_s${unit}_${if (isSecondLesson) "l2" else "l1"}"
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
            LessonContent("Program shape + print", listOf(
                Step("C# starts from Main", "A console program starts running inside Main(). Console.WriteLine prints text.", "using System;\n\nclass Program {\n    static void Main() {\n        Console.WriteLine(\"Hello C#\");\n    }\n}"),
                Step("WriteLine prints a line", "Text goes inside double quotes. WriteLine moves to the next line automatically.", "Console.WriteLine(\"GoCode\");"),
                Step("Semicolons matter", "Most C# statements end with a semicolon.")
            )),
            LessonContent("Variables + Types", listOf(
                Step("Typed variables", "C# variables need a type before the name.", "int age = 14;\ndouble price = 19.99;\nchar grade = 'A';\nbool ready = true;"),
                Step("Strings store text", "Use string for text values.", "string name = \"Leo\";"),
                Step("Print variables", "String interpolation makes output readable.", "Console.WriteLine($\"{name} scored {score}\");")
            ))
        ),
        UnitContent(
            LessonContent("Comparisons + if", listOf(
                Step("Conditions choose paths", "C# uses comparisons like >, <, >=, <=, ==, and != inside if statements.", "if (score >= 75) {\n    Console.WriteLine(\"Passed\");\n}"),
                Step("Compare with ==", "A single = assigns. Double == compares.", "if (age == 13) {\n    Console.WriteLine(\"Teen\");\n}"),
                Step("Blocks use braces", "The code inside braces runs only when the condition is true.")
            )),
            LessonContent("else + logic", listOf(
                Step("else handles false", "Use else when the program needs a fallback path.", "if (age >= 13) {\n    Console.WriteLine(\"Welcome\");\n} else {\n    Console.WriteLine(\"Too young\");\n}"),
                Step("&& means AND", "Use && when both conditions must be true.", "if (age >= 13 && hasPassword) {\n    Console.WriteLine(\"Access granted\");\n}"),
                Step("|| means OR", "Use || when either condition is enough.")
            ))
        ),
        UnitContent(
            LessonContent("while loops", listOf(
                Step("while repeats", "A while loop repeats while its condition remains true.", "int count = 1;\nwhile (count <= 3) {\n    Console.WriteLine(count);\n    count++;\n}"),
                Step("Update the counter", "A loop needs a change that moves it toward stopping.", "count++;"),
                Step("Watch for endless loops", "If the condition never becomes false, the program keeps running.")
            )),
            LessonContent("for loops", listOf(
                Step("for is compact", "A for loop keeps the start, condition, and update together.", "for (int i = 1; i <= 5; i++) {\n    Console.WriteLine(i);\n}"),
                Step("foreach reads arrays", "A foreach loop reads each value in an array.", "foreach (int score in scores) {\n    Console.WriteLine(score);\n}"),
                Step("Loops combine with if", "Use decisions inside loops to filter values.")
            ))
        ),
        UnitContent(
            LessonContent("Arrays", listOf(
                Step("Arrays store many values", "An array keeps several values of the same type.", "int[] scores = {90, 75, 88};"),
                Step("Read by index", "Indexes start at 0.", "Console.WriteLine(scores[0]);"),
                Step("Count with Length", "Use Length to know how many values are inside.", "Console.WriteLine(scores.Length);")
            )),
            LessonContent("Array indexes", listOf(
                Step("Last index", "If an array has 3 values, the last index is 2.", "scores[2]"),
                Step("Loop through an array", "Use a for loop when you need the index.", "for (int i = 0; i < scores.Length; i++) {\n    Console.WriteLine(scores[i]);\n}"),
                Step("Stay inside bounds", "Only read indexes from 0 to Length - 1.")
            ))
        ),
        UnitContent(
            LessonContent("Methods", listOf(
                Step("Methods organize code", "A method is a named reusable block.", "static void SayHello() {\n    Console.WriteLine(\"Hello\");\n}"),
                Step("Call a method", "A method runs when you call it by name.", "SayHello();"),
                Step("void means no returned value", "Use void when the method performs an action.")
            )),
            LessonContent("Parameters + return", listOf(
                Step("Parameters receive values", "Write the type and name inside the parentheses.", "static void Greet(string name) {\n    Console.WriteLine(\"Hello \" + name);\n}"),
                Step("Return a value", "Use return when a method calculates a result.", "static int DoubleNumber(int n) {\n    return n * 2;\n}"),
                Step("Store returned values", "Returned values can be assigned to variables.", "int result = DoubleNumber(4);")
            ))
        ),
        UnitContent(
            LessonContent("Console input", listOf(
                Step("Read input with ReadLine", "Console.ReadLine reads text from standard input.", "string name = Console.ReadLine();"),
                Step("Convert numbers", "Use int.Parse when input should become a whole number.", "int age = int.Parse(Console.ReadLine());"),
                Step("Input becomes data", "After reading input, use the variable like any other value.")
            )),
            LessonContent("Input decisions", listOf(
                Step("Read and decide", "Combine Console.ReadLine with if / else.", "int age = int.Parse(Console.ReadLine());\nif (age >= 13) {\n    Console.WriteLine(\"Welcome\");\n}"),
                Step("Prompts help users", "Print a short message before reading input.", "Console.Write(\"Enter age: \");\nint age = int.Parse(Console.ReadLine());")
            ))
        ),
        UnitContent(
            LessonContent("String tools", listOf(
                Step("Strings have helpers", "String values can report length and change casing.", "string name = \"Leo\";\nConsole.WriteLine(name.Length);\nConsole.WriteLine(name.ToUpper());"),
                Step("Compare strings", "Use == to compare string text in beginner examples.", "if (name == \"Leo\") {\n    Console.WriteLine(\"Found Leo\");\n}"),
                Step("Read characters", "Use indexes to read characters in a string.", "Console.WriteLine(name[0]);")
            )),
            LessonContent("String checks", listOf(
                Step("Contains checks text", "Contains returns true when text appears inside a string.", "if (email.Contains(\"@\")) {\n    Console.WriteLine(\"Valid\");\n}"),
                Step("Trim cleans input", "Trim removes spaces from the start and end.", "name = name.Trim();")
            ))
        ),
        UnitContent(
            LessonContent("Classes", listOf(
                Step("Classes describe objects", "A class groups data and behavior into a custom type.", "class Student {\n    public string Name;\n    public int Age;\n}"),
                Step("Create an object", "An object is a value made from a class.", "Student student = new Student();\nstudent.Name = \"Maya\";"),
                Step("public exposes members", "Beginner examples often use public so Main can access fields.")
            )),
            LessonContent("Objects", listOf(
                Step("Methods belong to classes", "A method inside a class can use that object's fields.", "class Student {\n    public string Name;\n    public void Introduce() {\n        Console.WriteLine(Name);\n    }\n}"),
                Step("Call with dot", "Use the dot to call a method on an object.", "student.Introduce();")
            ))
        ),
        UnitContent(
            LessonContent("Reading errors", listOf(
                Step("Compiler errors are clues", "C# compiler messages usually include a file, line, and explanation."),
                Step("Common mistakes", "Check semicolons, braces, capitalization, and type names first."),
                Step("Runtime errors happen later", "Parsing input can fail while the program runs.")
            )),
            LessonContent("try / catch", listOf(
                Step("Handle runtime errors", "try / catch lets a program respond to risky operations.", "try {\n    int number = int.Parse(text);\n    Console.WriteLine(number);\n} catch (Exception) {\n    Console.WriteLine(\"Invalid number\");\n}"),
                Step("Debug with output", "A focused WriteLine can reveal what a value contains.", "Console.WriteLine($\"age = {age}\");")
            ))
        ),
        UnitContent(
            LessonContent("Final review", listOf(
                Step("You know the core C# pieces", "You learned output, variables, decisions, loops, arrays, methods, input, strings, classes, and debugging."),
                Step("Think in types and flow", "Ask what type each value has, what repeats, and which path should run.")
            )),
            LessonContent("Build confidence", listOf(
                Step("C# has clear patterns", "Class, method, statement, block, and expression patterns repeat throughout the language."),
                Step("Ready for more", "After this path, you are ready for lists, files, LINQ, APIs, and Unity or .NET projects.", tip = "The final quiz mixes the whole C# path.")
            ))
        )
    )
}
