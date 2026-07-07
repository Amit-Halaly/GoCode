package com.example.gocode.lessons

object PythonLessonsRepository {

    fun getSteps(nodeId: String): List<LessonStep> {
        return when (nodeId) {
            "py_u1_l1" -> lesson(
                "py_s1_l1",
                "Welcome to Python",
                listOf(
                    Step("Python runs line by line", "Python is friendly for beginners because you can write useful code with very little setup.\n\nA Python program usually starts right away from the first line.", """print("Hello GoCode")""", "Python does not need a class or main() for beginner scripts."),
                    Step("Print text", "Use print() to show text in the console.", """print("Hello Python")""", "Text goes inside quotes."),
                    Step("One line at a time", "Python reads each line from top to bottom.", """print("First")
print("Second")""", "This prints First, then Second.")
                )
            )
            "py_u1_l2" -> lesson(
                "py_s1_l2",
                "Variables",
                listOf(
                    Step("Variables store values", "A variable is a name that remembers a value.", """score = 10
name = "Leo"""", "Python figures out the type from the value."),
                    Step("Python types", "Common beginner types are int, float, str, and bool.", """age = 14
price = 19.99
name = "Maya"
is_ready = True""", "Python uses True and False with capital letters."),
                    Step("Print variables", "Use the variable name without quotes to print its value.", """name = "Leo"
print(name)""", "With quotes, Python prints the exact word.")
                )
            )
            "py_u2_l1" -> lesson(
                "py_s2_l1",
                "If Statements",
                listOf(
                    Step("Programs make decisions", "An if statement runs code only when a condition is true.", """score = 90
if score >= 75:
    print("Passed")""", "The colon starts the block."),
                    Step("Indentation matters", "Python uses indentation to know which lines belong inside the if.", """if True:
    print("Inside")
print("Outside")""", "Indented lines are part of the block."),
                    Step("Comparisons", "Use >, <, >=, <=, ==, and != to compare values.", """age = 16
print(age >= 13)""", "== compares. = assigns.")
                )
            )
            "py_u2_l2" -> lesson(
                "py_s2_l2",
                "Else + Logic",
                listOf(
                    Step("else handles the other path", "else runs when the if condition is false.", """age = 10
if age >= 13:
    print("Welcome")
else:
    print("Too young")""", "Only one branch runs."),
                    Step("elif adds more choices", "Use elif when there are more than two possible outcomes.", """score = 85
if score >= 90:
    print("Excellent")
elif score >= 75:
    print("Passed")
else:
    print("Try again")""", "Python checks from top to bottom."),
                    Step("and, or, not", "Python uses readable logic words.", """has_ticket = True
age = 16
if age >= 13 and has_ticket:
    print("Enter")""", "and means both conditions must be true.")
                )
            )
            "py_u3_l1" -> lesson(
                "py_s3_l1",
                "While Loops",
                listOf(
                    Step("Loops repeat code", "A while loop repeats while its condition is true.", """count = 1
while count <= 3:
    print(count)
    count += 1""", "count += 1 adds one."),
                    Step("Stop the loop", "A loop needs a change that eventually makes the condition false.", """lives = 3
while lives > 0:
    print("Play")
    lives -= 1""", "Missing the update can create an infinite loop.")
                )
            )
            "py_u3_l2" -> lesson(
                "py_s3_l2",
                "For Loops",
                listOf(
                    Step("for loops walk through sequences", "Use for when you want to repeat over a range or a collection.", """for i in range(3):
    print(i)""", "range(3) gives 0, 1, 2."),
                    Step("Loop through words", "A for loop can read every item in a list.", """names = ["Leo", "Maya", "Dan"]
for name in names:
    print(name)""", "The variable name changes each repeat.")
                )
            )
            "py_u4_l1" -> lesson(
                "py_s4_l1",
                "Lists",
                listOf(
                    Step("Lists store many values", "A list keeps several values in order.", """scores = [90, 75, 88]""", "Lists use square brackets."),
                    Step("Read by index", "Indexes start at 0.", """names = ["Leo", "Maya", "Dan"]
print(names[0])""", "This prints Leo."),
                    Step("List length", "Use len() to count items.", """print(len(names))""", "len() works with lists and strings.")
                )
            )
            "py_u4_l2" -> lesson(
                "py_s4_l2",
                "List Changes",
                listOf(
                    Step("Append items", "append() adds an item to the end of a list.", """items = []
items.append("Python")
print(items)""", "append changes the list."),
                    Step("Loop through a list", "Lists and for loops are a natural pair.", """for score in scores:
    print(score)""", "This reads values directly.")
                )
            )
            "py_u5_l1" -> lesson(
                "py_s5_l1",
                "Functions",
                listOf(
                    Step("Functions organize code", "A function is a named block you can call again.", """def say_hello():
    print("Hello")""", "def means define a function."),
                    Step("Call a function", "A function runs only when you call it.", """say_hello()""", "The parentheses are part of the call.")
                )
            )
            "py_u5_l2" -> lesson(
                "py_s5_l2",
                "Parameters + Return",
                listOf(
                    Step("Parameters receive values", "Parameters make functions flexible.", """def greet(name):
    print("Hello " + name)""", "name behaves like a variable inside the function."),
                    Step("return sends a value back", "Use return when a function should calculate and give back a result.", """def double(n):
    return n * 2

print(double(4))""", "This prints 8.")
                )
            )
            "py_u6_l1" -> lesson("py_s6_l1", "Input", listOf(
                Step("Read user input", "input() reads text typed by the user.", """name = input()
print(name)""", "input() returns a string."),
                Step("Convert numbers", "Use int() when input should become a whole number.", """age = int(input())""", "Conversion can fail if the text is not a number.")
            ))
            "py_u6_l2" -> lesson("py_s6_l2", "Input Decisions", listOf(
                Step("Use input in conditions", "After converting input, you can compare it.", """age = int(input())
if age >= 13:
    print("Welcome")""", "Input and if statements make programs interactive."),
                Step("Print prompts", "A prompt tells the user what to type.", """name = input("Enter name: ")""", "Prompts improve the experience.")
            ))
            "py_u7_l1" -> lesson("py_s7_l1", "String Tools", listOf(
                Step("Strings have tools", "String methods help inspect and change text.", """word = "python"
print(word.upper())""", "upper() returns uppercase text."),
                Step("Count characters", "len() counts characters in a string.", """print(len("Leo"))""", "This prints 3.")
            ))
            "py_u7_l2" -> lesson("py_s7_l2", "String Checks", listOf(
                Step("Clean text", "strip() removes spaces from the start and end.", """name = "  Leo  "
print(name.strip())""", "Useful for user input."),
                Step("Check text", "Use in to check whether text contains another piece of text.", """email = "leo@gocode.com"
print("@" in email)""", "This returns True.")
            ))
            "py_u8_l1" -> lesson("py_s8_l1", "Dictionaries", listOf(
                Step("Dictionaries store labeled data", "A dictionary connects keys to values.", """student = {"name": "Maya", "age": 14}""", "Keys help describe each value."),
                Step("Read by key", "Use the key inside square brackets.", """print(student["name"])""", "This prints Maya.")
            ))
            "py_u8_l2" -> lesson("py_s8_l2", "Dictionary Updates", listOf(
                Step("Update values", "Assign to a key to change or add data.", """student["age"] = 15""", "Dictionaries are useful for records."),
                Step("Loop through keys", "A for loop can read dictionary keys.", """for key in student:
    print(key)""", "You can use the key to reach the value.")
            ))
            "py_u9_l1" -> lesson("py_s9_l1", "Reading Errors", listOf(
                Step("Errors are clues", "Python errors tell you what happened and often where to look.", """print("Hello")""", "Start with the last line of the error."),
                Step("Syntax errors", "A syntax error means Python could not understand the code.", """if True
    print("Hi")""", "This is missing a colon.")
            ))
            "py_u9_l2" -> lesson("py_s9_l2", "try / except", listOf(
                Step("Handle risky code", "try / except lets your program respond to runtime errors.", """try:
    age = int(text)
except ValueError:
    print("Invalid number")""", "Use it for code that may fail."),
                Step("Debug with prints", "Printing values can help you understand the program state.", """print("age =", age)""", "Clean debug prints later.")
            ))
            "py_u10_l1" -> lesson("py_s10_l1", "Final Review", listOf(
                Step("You know the core pieces", "You learned printing, variables, decisions, loops, lists, functions, input, strings, dictionaries, and debugging."),
                Step("Think in small steps", "Ask: what data do I need, what repeats, and what decision should the program make?")
            ))
            "py_u10_l2" -> lesson("py_s10_l2", "Build Confidence", listOf(
                Step("Patterns matter", "You do not need to memorize everything. Learn to recognize patterns."),
                Step("Ready for more", "After this path, you are ready for files, modules, APIs, and bigger Python projects.", tip = "The final quiz mixes the whole Python path.")
            ))
            else -> getSteps("py_u1_l1")
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
