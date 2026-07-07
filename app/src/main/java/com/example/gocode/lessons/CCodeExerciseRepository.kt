package com.example.gocode.lessons

import com.example.gocode.network.models.runModels.RunTestCase

object CCodeExerciseRepository {

    fun getExercise(nodeId: String): CodeExercise {
        return exercises[nodeId] ?: exercises.getValue("c_u1_c1")
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
        hiddenInput: String = input,
        hiddenExpected: String = expected,
    ) = CodeExercise(
        nodeId = nodeId,
        language = "c",
        title = title,
        subtitle = subtitle,
        template = template.trimIndent(),
        answer = answer.trimIndent(),
        defaultInput = input,
        compareMode = "trim",
        tests = listOf(
            RunTestCase(name = "Expected output", input = input, expectedOutput = expected),
            RunTestCase(name = "Hidden check", input = hiddenInput, expectedOutput = hiddenExpected, hidden = true)
        )
    )

    private val exercises: Map<String, CodeExercise> = listOf(
        exercise(
            nodeId = "c_u1_c1",
            title = "Print a greeting",
            subtitle = "Use printf from main to print Hello GoCode.",
            template = """
                #include <stdio.h>

                int main() {
                    // TODO: Print Hello GoCode
                    return 0;
                }
            """,
            answer = """
                #include <stdio.h>

                int main() {
                    printf("Hello GoCode\n");
                    return 0;
                }
            """,
            expected = "Hello GoCode"
        ),
        exercise(
            nodeId = "c_u2_c1",
            title = "Access check",
            subtitle = "Use if / else and logical AND to decide access.",
            template = """
                #include <stdio.h>

                int main() {
                    int age = 16;
                    int hasPassword = 1;

                    // TODO: Print Access granted only when both conditions are true.
                    // Otherwise print Access denied.
                    return 0;
                }
            """,
            answer = """
                #include <stdio.h>

                int main() {
                    int age = 16;
                    int hasPassword = 1;

                    if (age >= 13 && hasPassword) {
                        printf("Access granted\n");
                    } else {
                        printf("Access denied\n");
                    }
                    return 0;
                }
            """,
            expected = "Access granted"
        ),
        exercise(
            nodeId = "c_u3_c1",
            title = "Count loop",
            subtitle = "Use a for loop to print 1 to 5 and mark the middle value.",
            template = """
                #include <stdio.h>

                int main() {
                    // TODO: Print 1 to 5.
                    // When the number is 3, also print Middle.
                    return 0;
                }
            """,
            answer = """
                #include <stdio.h>

                int main() {
                    for (int i = 1; i <= 5; i++) {
                        printf("%d\n", i);
                        if (i == 3) {
                            printf("Middle\n");
                        }
                    }
                    return 0;
                }
            """,
            expected = "1\n2\n3\nMiddle\n4\n5"
        ),
        exercise(
            nodeId = "c_u4_c1",
            title = "Scores list",
            subtitle = "Use an array and loop to print every score.",
            template = """
                #include <stdio.h>

                int main() {
                    int scores[] = {90, 75, 88};
                    int size = 3;

                    // TODO: Print every score.
                    return 0;
                }
            """,
            answer = """
                #include <stdio.h>

                int main() {
                    int scores[] = {90, 75, 88};
                    int size = 3;

                    for (int i = 0; i < size; i++) {
                        printf("%d\n", scores[i]);
                    }
                    return 0;
                }
            """,
            expected = "90\n75\n88"
        ),
        exercise(
            nodeId = "c_u5_c1",
            title = "Helper function",
            subtitle = "Complete a reusable function that greets each name.",
            template = """
                #include <stdio.h>

                void greet(char name[]) {
                    // TODO: Print Hello plus the name.
                }

                int main() {
                    greet("Leo");
                    greet("Maya");
                    return 0;
                }
            """,
            answer = """
                #include <stdio.h>

                void greet(char name[]) {
                    printf("Hello %s\n", name);
                }

                int main() {
                    greet("Leo");
                    greet("Maya");
                    return 0;
                }
            """,
            expected = "Hello Leo\nHello Maya"
        ),
        exercise(
            nodeId = "c_u6_c1",
            title = "Ask age",
            subtitle = "Read an age with scanf and print the right message.",
            template = """
                #include <stdio.h>

                int main() {
                    int age;

                    // TODO: Read age.
                    // Print Welcome if age is at least 13, otherwise print Too young.
                    return 0;
                }
            """,
            answer = """
                #include <stdio.h>

                int main() {
                    int age;
                    scanf("%d", &age);

                    if (age >= 13) {
                        printf("Welcome\n");
                    } else {
                        printf("Too young\n");
                    }
                    return 0;
                }
            """,
            expected = "Welcome",
            input = "16\n",
            hiddenInput = "10\n",
            hiddenExpected = "Too young"
        ),
        exercise(
            nodeId = "c_u7_c1",
            title = "Name checker",
            subtitle = "Use strcmp to compare C string text.",
            template = """
                #include <stdio.h>
                #include <string.h>

                int main() {
                    char name[] = "Leo";

                    // TODO: If name equals Leo, print Found Leo.
                    return 0;
                }
            """,
            answer = """
                #include <stdio.h>
                #include <string.h>

                int main() {
                    char name[] = "Leo";

                    if (strcmp(name, "Leo") == 0) {
                        printf("Found Leo\n");
                    }
                    return 0;
                }
            """,
            expected = "Found Leo"
        ),
        exercise(
            nodeId = "c_u8_c1",
            title = "Swap values",
            subtitle = "Use pointers so a function can swap two variables.",
            template = """
                #include <stdio.h>

                void swap(int *a, int *b) {
                    // TODO: Swap the values pointed to by a and b.
                }

                int main() {
                    int x = 3;
                    int y = 7;
                    swap(&x, &y);
                    printf("%d %d\n", x, y);
                    return 0;
                }
            """,
            answer = """
                #include <stdio.h>

                void swap(int *a, int *b) {
                    int temp = *a;
                    *a = *b;
                    *b = temp;
                }

                int main() {
                    int x = 3;
                    int y = 7;
                    swap(&x, &y);
                    printf("%d %d\n", x, y);
                    return 0;
                }
            """,
            expected = "7 3"
        ),
        exercise(
            nodeId = "c_u9_c1",
            title = "Safe divide",
            subtitle = "Check before dividing so the program avoids division by zero.",
            template = """
                #include <stdio.h>

                int main() {
                    int a = 12;
                    int b = 0;

                    // TODO: Print Cannot divide by zero when b is 0.
                    // Otherwise print a / b.
                    return 0;
                }
            """,
            answer = """
                #include <stdio.h>

                int main() {
                    int a = 12;
                    int b = 0;

                    if (b == 0) {
                        printf("Cannot divide by zero\n");
                    } else {
                        printf("%d\n", a / b);
                    }
                    return 0;
                }
            """,
            expected = "Cannot divide by zero"
        ),
        exercise(
            nodeId = "c_u10_c1",
            title = "Final check",
            subtitle = "Combine arrays, loops, functions, and conditions.",
            template = """
                #include <stdio.h>

                void printPassing(int scores[], int size) {
                    // TODO: Print only scores that are at least 75.
                }

                int main() {
                    int scores[] = {60, 88, 75, 42, 91};
                    printPassing(scores, 5);
                    return 0;
                }
            """,
            answer = """
                #include <stdio.h>

                void printPassing(int scores[], int size) {
                    for (int i = 0; i < size; i++) {
                        if (scores[i] >= 75) {
                            printf("%d\n", scores[i]);
                        }
                    }
                }

                int main() {
                    int scores[] = {60, 88, 75, 42, 91};
                    printPassing(scores, 5);
                    return 0;
                }
            """,
            expected = "88\n75\n91"
        )
    ).associateBy { it.nodeId }
}
