package com.example.gocode.lessons

object PythonPracticeRepository {

    fun getQuestions(nodeId: String): List<PracticeQuestion> {
        return when (nodeId) {
            "py_u1_p1" -> s1p1()
            "py_u1_p2" -> s1p2()
            "py_u1_q1" -> s1p1() + s1p2()
            "py_u2_p1" -> s2p1()
            "py_u2_p2" -> s2p2()
            "py_u2_q1" -> s2p1() + s2p2()
            "py_u3_p1" -> s3p1()
            "py_u3_p2" -> s3p2()
            "py_u3_q1" -> s3p1() + s3p2()
            "py_u4_p1" -> s4p1()
            "py_u4_p2" -> s4p2()
            "py_u4_q1" -> s4p1() + s4p2()
            "py_u5_p1" -> s5p1()
            "py_u5_p2" -> s5p2()
            "py_u5_q1" -> s5p1() + s5p2()
            "py_u6_p1" -> s6p1()
            "py_u6_p2" -> s6p2()
            "py_u6_q1" -> s6p1() + s6p2()
            "py_u7_p1" -> s7p1()
            "py_u7_p2" -> s7p2()
            "py_u7_q1" -> s7p1() + s7p2()
            "py_u8_p1" -> s8p1()
            "py_u8_p2" -> s8p2()
            "py_u8_q1" -> s8p1() + s8p2()
            "py_u9_p1" -> s9p1()
            "py_u9_p2" -> s9p2()
            "py_u9_q1" -> s9p1() + s9p2()
            "py_u10_p1" -> s10p1()
            "py_u10_p2" -> s10p2()
            "py_u10_q1" -> s10p1() + s10p2() + finalQuiz()
            else -> s1p1()
        }
    }

    private fun mc(id: String, question: String, options: List<String>, answer: String, explanation: String, code: String? = null) =
        PracticeQuestion(id, PracticeQuestionType.MULTIPLE_CHOICE, "Question", question, code, options, answer, explanation = explanation)

    private fun drag(id: String, question: String, code: String, options: List<String>, answers: List<String>, explanation: String) =
        PracticeQuestion(id, PracticeQuestionType.DRAG_FILL_BLANK, "Question", question, code.trimIndent(), options, answers.first(), answers, explanation)

    private fun fill(id: String, question: String, code: String, answer: String, explanation: String) =
        PracticeQuestion(id, PracticeQuestionType.FILL_BLANK, "Question", question, code.trimIndent(), correctAnswer = answer, explanation = explanation)

    private fun s1p1() = listOf(
        mc("py_s1_p1_q1", "Which command prints text in Python?", listOf("print()", "System.out.println()", "echo()", "Console.write()"), "print()", "Python uses print() to show output."),
        drag("py_s1_p1_q2", "Drag the print command into the code.", """______("Hello Python")""", listOf("print", "println", "echo", "main"), listOf("print"), "print(\"Hello Python\") shows text."),
        mc("py_s1_p1_q3", "What is printed first?", listOf("Ready", "Go", "Both at once", "Nothing"), "Ready", "Python runs from top to bottom.", """print("Ready")
print("Go")""")
    )

    private fun s1p2() = listOf(
        mc("py_s1_p2_q1", "Which line creates a variable?", listOf("score = 10", "int score = 10;", "let score: 10", "score := int"), "score = 10", "Python creates variables with name = value."),
        drag("py_s1_p2_q2", "Drag the correct values into the blanks.", """age = ______
name = ______
is_ready = ______""", listOf("14", "\"Leo\"", "True", "'age'", "false"), listOf("14", "\"Leo\"", "True"), "Numbers, strings, and booleans use simple Python values."),
        fill("py_s1_p2_q3", "Complete the variable name that should be printed.", """player = "Maya"
print(______)""", "player", "Printing the variable name shows its value.")
    )

    private fun s2p1() = listOf(
        mc("py_s2_p1_q1", "What does >= mean?", listOf("Greater than or equal", "Exactly equal", "Less than", "Assign value"), "Greater than or equal", ">= checks a minimum value."),
        drag("py_s2_p1_q2", "Complete the if statement.", """score = 90
______ score >= 75:
    print("Passed")""", listOf("if", "else", "for", "def"), listOf("if"), "if starts a conditional block."),
        mc("py_s2_p1_q3", "What does Python need after an if condition?", listOf("A colon", "A semicolon", "A class", "Parentheses only"), "A colon", "The colon starts the indented block.")
    )

    private fun s2p2() = listOf(
        mc("py_s2_p2_q1", "When does else run?", listOf("When if is false", "Before if", "Always", "Never"), "When if is false", "else handles the false branch."),
        drag("py_s2_p2_q2", "Drag the missing logic words.", """age = 16
has_ticket = True
if age >= 13 ______ has_ticket:
    print("Enter")
______:
    print("Stop")""", listOf("and", "else", "or", "not"), listOf("and", "else"), "and requires both conditions; else handles the other path."),
        mc("py_s2_p2_q3", "Which keyword adds another condition?", listOf("elif", "elseif", "another", "case"), "elif", "Python uses elif for another branch.")
    )

    private fun s3p1() = listOf(
        mc("py_s3_p1_q1", "Which loop repeats while a condition stays true?", listOf("while", "for", "if", "def"), "while", "while loops depend on a condition."),
        drag("py_s3_p1_q2", "Complete the counter loop.", """count = 1
while count <= 3:
    print(count)
    count ______ 1""", listOf("+=", "-=", "==", "="), listOf("+="), "count += 1 moves the loop toward stopping."),
        mc("py_s3_p1_q3", "What can cause an infinite loop?", listOf("The condition never becomes false", "Using print()", "A list", "A string"), "The condition never becomes false", "A loop needs a stopping point.")
    )

    private fun s3p2() = listOf(
        mc("py_s3_p2_q1", "What does range(3) produce?", listOf("0, 1, 2", "1, 2, 3", "3 only", "0, 1, 2, 3"), "0, 1, 2", "range(3) stops before 3."),
        drag("py_s3_p2_q2", "Complete the for loop.", """for name ______ names:
    print(name)""", listOf("in", ":", "of", "at"), listOf("in"), "Python for loops use in."),
        fill("py_s3_p2_q3", "Complete the range size.", """for i in range(______):
    print(i)""", "5", "range(5) repeats five times from 0 to 4.")
    )

    private fun s4p1() = listOf(
        mc("py_s4_p1_q1", "Which value is a Python list?", listOf("[1, 2, 3]", "{1, 2, 3}", "\"1, 2, 3\"", "(name = 1)"), "[1, 2, 3]", "Lists use square brackets."),
        drag("py_s4_p1_q2", "Complete the list access.", """names = ["Leo", "Maya", "Dan"]
print(names[______])""", listOf("0", "1", "len", "\"Leo\""), listOf("0"), "Index 0 reads the first item."),
        mc("py_s4_p1_q3", "What does len(names) return?", listOf("The number of items", "The first item", "The last item", "A boolean"), "The number of items", "len() counts items.")
    )

    private fun s4p2() = listOf(
        mc("py_s4_p2_q1", "Which method adds to a list?", listOf("append()", "upper()", "strip()", "input()"), "append()", "append() adds an item to the end."),
        drag("py_s4_p2_q2", "Complete the list loop.", """for score ______ scores:
    print(______)""", listOf("in", "score", "scores", "range"), listOf("in", "score"), "Use for score in scores, then print score."),
        mc("py_s4_p2_q3", "If a list has 4 items, what is the last index?", listOf("3", "4", "1", "len"), "3", "Indexes start at 0.")
    )

    private fun s5p1() = listOf(
        mc("py_s5_p1_q1", "Which keyword defines a function?", listOf("def", "function", "void", "class"), "def", "Python uses def."),
        drag("py_s5_p1_q2", "Complete the function definition and call.", """______ say_hello():
    print("Hello")

______()""", listOf("def", "say_hello", "return", "print"), listOf("def", "say_hello"), "def defines the function; say_hello() calls it."),
        mc("py_s5_p1_q3", "When does a function run?", listOf("When it is called", "When it is defined", "Before the file starts", "Only inside lists"), "When it is called", "A function definition waits until called.")
    )

    private fun s5p2() = listOf(
        mc("py_s5_p2_q1", "What does a parameter do?", listOf("Receives a value", "Stops a loop", "Creates a list only", "Deletes output"), "Receives a value", "Parameters make functions flexible."),
        drag("py_s5_p2_q2", "Complete the function that returns double.", """def double(n):
    ______ n * 2""", listOf("return", "print", "def", "if"), listOf("return"), "return sends the calculated value back."),
        fill("py_s5_p2_q3", "Complete the parameter name.", """def greet(______):
    print("Hi " + name)""", "name", "The parameter name is used inside the function.")
    )

    private fun s6p1() = listOf(
        mc("py_s6_p1_q1", "What does input() return by default?", listOf("A string", "An int", "A boolean", "A list"), "A string", "input() reads text."),
        drag("py_s6_p1_q2", "Complete the input line.", """name = ______()
print(name)""", listOf("input", "print", "int", "read"), listOf("input"), "input() reads a value from the user."),
        mc("py_s6_p1_q3", "How do you convert input to a whole number?", listOf("int(input())", "str(input())", "bool(input())", "list(input())"), "int(input())", "int() converts text to a whole number.")
    )

    private fun s6p2() = listOf(
        drag("py_s6_p2_q1", "Complete the age check.", """age = ______(input())
if age ______ 13:
    print("Welcome")""", listOf("int", ">=", "str", "<"), listOf("int", ">="), "Convert input to int, then compare it."),
        mc("py_s6_p2_q2", "Why use a prompt?", listOf("So the user knows what to type", "So Python creates a list", "So loops stop", "So print disappears"), "So the user knows what to type", "Prompts guide the user.")
    )

    private fun s7p1() = listOf(
        mc("py_s7_p1_q1", "What does upper() do?", listOf("Returns uppercase text", "Counts items", "Reads input", "Creates a function"), "Returns uppercase text", "upper() returns uppercase text."),
        drag("py_s7_p1_q2", "Complete the uppercase call.", """word = "python"
print(word.______())""", listOf("upper", "strip", "len", "append"), listOf("upper"), "word.upper() returns PYTHON."),
        mc("py_s7_p1_q3", "What does len(\"Leo\") return?", listOf("3", "Leo", "0", "True"), "3", "len() counts characters.")
    )

    private fun s7p2() = listOf(
        mc("py_s7_p2_q1", "What does strip() remove?", listOf("Spaces at the start and end", "All letters", "All numbers", "The whole variable"), "Spaces at the start and end", "strip() cleans outer spaces."),
        drag("py_s7_p2_q2", "Complete the contains check.", """email = "leo@gocode.com"
if ______ in email:
    print("Valid")""", listOf("\"@\"", "email", "contains", "strip"), listOf("\"@\""), "The in operator checks if text is inside another string."),
        mc("py_s7_p2_q3", "Which expression checks exact text?", listOf("answer == \"yes\"", "answer = \"yes\"", "answer equals \"yes\"", "answer contains yes"), "answer == \"yes\"", "== compares values.")
    )

    private fun s8p1() = listOf(
        mc("py_s8_p1_q1", "What does a dictionary store?", listOf("Key-value pairs", "Only numbers", "Only loops", "Only errors"), "Key-value pairs", "Dictionaries connect labels to values."),
        drag("py_s8_p1_q2", "Complete the dictionary access.", """student = {"name": "Maya", "age": 14}
print(student[______])""", listOf("\"name\"", "\"age\"", "0", "name"), listOf("\"name\""), "Use the key to read the value."),
        mc("py_s8_p1_q3", "What is printed?", listOf("Maya", "name", "14", "student"), "Maya", "student[\"name\"] reads Maya.", """student = {"name": "Maya"}
print(student["name"])""")
    )

    private fun s8p2() = listOf(
        mc("py_s8_p2_q1", "Which line updates the age?", listOf("student[\"age\"] = 15", "student.age = 15", "age student 15", "student(\"age\") = 15"), "student[\"age\"] = 15", "Assign to a key to update it."),
        drag("py_s8_p2_q2", "Complete the dictionary loop.", """for key ______ student:
    print(______)""", listOf("in", "key", "student", "of"), listOf("in", "key"), "Loop through dictionary keys with in."),
        mc("py_s8_p2_q3", "Which symbol wraps a dictionary?", listOf("{ }", "[ ]", "( )", "< >"), "{ }", "Dictionaries use curly braces.")
    )

    private fun s9p1() = listOf(
        mc("py_s9_p1_q1", "What is a syntax error?", listOf("Python cannot understand the code", "The program finished", "A list is empty", "A string is uppercase"), "Python cannot understand the code", "Syntax errors happen before code can run."),
        mc("py_s9_p1_q2", "What is missing here?", listOf("Colon", "Comma", "List", "Function call"), "Colon", "if statements need a colon.", """if True
    print("Hi")"""),
        mc("py_s9_p1_q3", "Where should you start reading many Python errors?", listOf("Near the last line", "Only the first word", "In the app colors", "Nowhere"), "Near the last line", "Python tracebacks often point clearly near the end.")
    )

    private fun s9p2() = listOf(
        mc("py_s9_p2_q1", "What does try / except help with?", listOf("Handling runtime errors", "Creating only lists", "Making text uppercase", "Skipping all code"), "Handling runtime errors", "try / except catches risky runtime code."),
        drag("py_s9_p2_q2", "Complete the try / except structure.", """______:
    age = int(text)
______ ValueError:
    print("Invalid")""", listOf("try", "except", "if", "else"), listOf("try", "except"), "Risky code goes in try, fallback goes in except."),
        mc("py_s9_p2_q3", "Why use debug prints?", listOf("To inspect values while code runs", "To delete errors automatically", "To create a class", "To convert every input"), "To inspect values while code runs", "Debug prints reveal what values contain.")
    )

    private fun s10p1() = listOf(
        mc("py_s10_p1_q1", "Which tool repeats code?", listOf("loop", "string", "boolean", "prompt"), "loop", "Loops repeat code."),
        mc("py_s10_p1_q2", "Which tool stores many ordered values?", listOf("list", "if", "return", "except"), "list", "Lists store ordered values."),
        mc("py_s10_p1_q3", "Which tool organizes reusable code?", listOf("function", "index", "input", "error"), "function", "Functions organize reusable code.")
    )

    private fun s10p2() = listOf(
        drag("py_s10_p2_q1", "Complete the mixed code.", """for name in names:
    if ______(name) > 3:
        print(______)""", listOf("len", "name", "range", "input"), listOf("len", "name"), "Use len(name) for length, then print name."),
        mc("py_s10_p2_q2", "Best first step for a coding task?", listOf("Break it into smaller steps", "Write random code", "Ignore errors", "Delete input"), "Break it into smaller steps", "Small steps make problems easier.")
    )

    private fun finalQuiz() = listOf(
        mc("py_s10_q_extra1", "Which keyword sends a value back from a function?", listOf("return", "def", "except", "while"), "return", "return sends a value back."),
        mc("py_s10_q_extra2", "Which type stores True or False?", listOf("bool", "str", "list", "dict"), "bool", "Boolean values are bool values.")
    )
}
