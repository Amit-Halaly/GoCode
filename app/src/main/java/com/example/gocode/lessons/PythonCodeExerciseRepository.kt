package com.example.gocode.lessons

import com.example.gocode.network.models.runModels.RunTestCase

object PythonCodeExerciseRepository {

    fun getExercise(nodeId: String): CodeExercise {
        return exercises[nodeId] ?: exercises.getValue("py_u1_c1")
    }

    fun getExercises(): Map<String, CodeExercise> = exercises

    private fun exercise(
        nodeId: String,
        title: String,
        subtitle: String,
        template: String,
        answer: String,
        expected: String,
        input: String = "",
        hiddenExpected: String = expected
    ) = CodeExercise(
        nodeId = nodeId,
        language = "python",
        title = title,
        subtitle = subtitle,
        template = template.trimIndent(),
        answer = answer.trimIndent(),
        defaultInput = input,
        tests = listOf(
            RunTestCase(name = "Expected output", input = input, expectedOutput = expected),
            RunTestCase(name = "Hidden check", input = input, expectedOutput = hiddenExpected, hidden = true)
        )
    )

    private val exercises: Map<String, CodeExercise> = listOf(
        exercise(
            nodeId = "py_u1_c1",
            title = "Print a greeting",
            subtitle = "Use print() to show Hello GoCode.",
            template = """
                # TODO: Print Hello GoCode
            """,
            answer = """
                print("Hello GoCode")
            """,
            expected = "Hello GoCode"
        ),
        exercise(
            nodeId = "py_u2_c1",
            title = "Access check",
            subtitle = "Use if / else and boolean logic to decide whether access is allowed.",
            template = """
                age = 16
                has_password = True

                # TODO: Print Access granted only when both conditions are true.
                # Otherwise print Access denied.
            """,
            answer = """
                age = 16
                has_password = True

                if age >= 13 and has_password:
                    print("Access granted")
                else:
                    print("Access denied")
            """,
            expected = "Access granted"
        ),
        exercise(
            nodeId = "py_u3_c1",
            title = "Count loop",
            subtitle = "Use a for loop to print 1 to 5 and mark the middle value.",
            template = """
                # TODO: Print the numbers 1 to 5.
                # When the number is 3, also print Middle.
            """,
            answer = """
                for number in range(1, 6):
                    print(number)
                    if number == 3:
                        print("Middle")
            """,
            expected = "1\n2\n3\nMiddle\n4\n5"
        ),
        exercise(
            nodeId = "py_u4_c1",
            title = "Favorite list",
            subtitle = "Use a loop to print every value in a Python list.",
            template = """
                favorites = ["Python", "GoCode", "Arena"]

                # TODO: Print every favorite.
            """,
            answer = """
                favorites = ["Python", "GoCode", "Arena"]

                for favorite in favorites:
                    print(favorite)
            """,
            expected = "Python\nGoCode\nArena"
        ),
        exercise(
            nodeId = "py_u5_c1",
            title = "Helper function",
            subtitle = "Complete a reusable function that greets each name it receives.",
            template = """
                def greet(name):
                    # TODO: Print Hello plus the name.
                    pass

                greet("Leo")
                greet("Maya")
            """,
            answer = """
                def greet(name):
                    print("Hello " + name)

                greet("Leo")
                greet("Maya")
            """,
            expected = "Hello Leo\nHello Maya"
        ),
        exercise(
            nodeId = "py_u6_c1",
            title = "Ask age",
            subtitle = "Read an age and print the right message.",
            template = """
                # TODO: Read an age from input.
                # Print Welcome if age is at least 13, otherwise print Too young.
            """,
            answer = """
                age = int(input())

                if age >= 13:
                    print("Welcome")
                else:
                    print("Too young")
            """,
            expected = "Welcome",
            input = "16\n"
        ),
        exercise(
            nodeId = "py_u7_c1",
            title = "Name checker",
            subtitle = "Use strip and a text comparison to find Leo.",
            template = """
                name = "  Leo  "

                # TODO: Clean the name and check if it equals Leo.
                # If yes, print Found Leo.
            """,
            answer = """
                name = "  Leo  "

                if name.strip() == "Leo":
                    print("Found Leo")
            """,
            expected = "Found Leo"
        ),
        exercise(
            nodeId = "py_u8_c1",
            title = "Student dictionary",
            subtitle = "Use dictionary values to print a student summary.",
            template = """
                student = {"name": "Maya", "age": 14}

                # TODO: Print Maya is 14 using the dictionary.
            """,
            answer = """
                student = {"name": "Maya", "age": 14}

                print(student["name"] + " is " + str(student["age"]))
            """,
            expected = "Maya is 14"
        ),
        exercise(
            nodeId = "py_u9_c1",
            title = "Safe number",
            subtitle = "Use try / except to parse text safely.",
            template = """
                text = "42"

                # TODO: Try to convert text to int.
                # Print the number if it works, otherwise print Invalid number.
            """,
            answer = """
                text = "42"

                try:
                    number = int(text)
                    print(number)
                except ValueError:
                    print("Invalid number")
            """,
            expected = "42"
        ),
        exercise(
            nodeId = "py_u10_c1",
            title = "Final check",
            subtitle = "Combine functions, lists, loops, and string length.",
            template = """
                def print_long_names(names):
                    # TODO: Print only names longer than 3 characters.
                    pass

                names = ["Leo", "Maya", "Noam", "Dan"]
                print_long_names(names)
            """,
            answer = """
                def print_long_names(names):
                    for name in names:
                        if len(name) > 3:
                            print(name)

                names = ["Leo", "Maya", "Noam", "Dan"]
                print_long_names(names)
            """,
            expected = "Maya\nNoam"
        )
    ).associateBy { it.nodeId }
}
