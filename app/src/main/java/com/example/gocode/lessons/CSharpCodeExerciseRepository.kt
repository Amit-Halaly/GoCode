package com.example.gocode.lessons

import com.example.gocode.network.models.runModels.RunTestCase

object CSharpCodeExerciseRepository {

    fun getExercise(nodeId: String): CodeExercise {
        return exercises[nodeId] ?: exercises.getValue("cs_u1_c1")
    }

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
        language = "csharp",
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
            "cs_u1_c1",
            "Print a greeting",
            "Use Console.WriteLine from Main to print Hello GoCode.",
            """
                using System;

                class Program {
                    static void Main() {
                        // TODO: Print Hello GoCode
                    }
                }
            """,
            """
                using System;

                class Program {
                    static void Main() {
                        Console.WriteLine("Hello GoCode");
                    }
                }
            """,
            "Hello GoCode"
        ),
        exercise(
            "cs_u2_c1",
            "Access check",
            "Use if / else and logical AND to decide access.",
            """
                using System;

                class Program {
                    static void Main() {
                        int age = 16;
                        bool hasPassword = true;

                        // TODO: Print Access granted only when both conditions are true.
                        // Otherwise print Access denied.
                    }
                }
            """,
            """
                using System;

                class Program {
                    static void Main() {
                        int age = 16;
                        bool hasPassword = true;

                        if (age >= 13 && hasPassword) {
                            Console.WriteLine("Access granted");
                        } else {
                            Console.WriteLine("Access denied");
                        }
                    }
                }
            """,
            "Access granted"
        ),
        exercise(
            "cs_u3_c1",
            "Count loop",
            "Use a for loop to print 1 to 5 and mark the middle value.",
            """
                using System;

                class Program {
                    static void Main() {
                        // TODO: Print 1 to 5.
                        // When the number is 3, also print Middle.
                    }
                }
            """,
            """
                using System;

                class Program {
                    static void Main() {
                        for (int i = 1; i <= 5; i++) {
                            Console.WriteLine(i);
                            if (i == 3) {
                                Console.WriteLine("Middle");
                            }
                        }
                    }
                }
            """,
            "1\n2\n3\nMiddle\n4\n5"
        ),
        exercise(
            "cs_u4_c1",
            "Scores array",
            "Use an array and loop to print every score.",
            """
                using System;

                class Program {
                    static void Main() {
                        int[] scores = {90, 75, 88};

                        // TODO: Print every score.
                    }
                }
            """,
            """
                using System;

                class Program {
                    static void Main() {
                        int[] scores = {90, 75, 88};

                        foreach (int score in scores) {
                            Console.WriteLine(score);
                        }
                    }
                }
            """,
            "90\n75\n88"
        ),
        exercise(
            "cs_u5_c1",
            "Helper method",
            "Complete a reusable method that greets each name.",
            """
                using System;

                class Program {
                    static void Greet(string name) {
                        // TODO: Print Hello plus the name.
                    }

                    static void Main() {
                        Greet("Leo");
                        Greet("Maya");
                    }
                }
            """,
            """
                using System;

                class Program {
                    static void Greet(string name) {
                        Console.WriteLine("Hello " + name);
                    }

                    static void Main() {
                        Greet("Leo");
                        Greet("Maya");
                    }
                }
            """,
            "Hello Leo\nHello Maya"
        ),
        exercise(
            "cs_u6_c1",
            "Ask age",
            "Read an age with Console.ReadLine and print the right message.",
            """
                using System;

                class Program {
                    static void Main() {
                        // TODO: Read age.
                        // Print Welcome if age is at least 13, otherwise print Too young.
                    }
                }
            """,
            """
                using System;

                class Program {
                    static void Main() {
                        int age = int.Parse(Console.ReadLine());

                        if (age >= 13) {
                            Console.WriteLine("Welcome");
                        } else {
                            Console.WriteLine("Too young");
                        }
                    }
                }
            """,
            "Welcome",
            input = "16\n",
            hiddenInput = "10\n",
            hiddenExpected = "Too young"
        ),
        exercise(
            "cs_u7_c1",
            "Name checker",
            "Use string comparison to check a name.",
            """
                using System;

                class Program {
                    static void Main() {
                        string name = "Leo";

                        // TODO: If name equals Leo, print Found Leo.
                    }
                }
            """,
            """
                using System;

                class Program {
                    static void Main() {
                        string name = "Leo";

                        if (name == "Leo") {
                            Console.WriteLine("Found Leo");
                        }
                    }
                }
            """,
            "Found Leo"
        ),
        exercise(
            "cs_u8_c1",
            "Student object",
            "Complete a class method that prints object data.",
            """
                using System;

                class Student {
                    public string Name;
                    public int Age;

                    public void Introduce() {
                        // TODO: Print Name and Age with a space between them.
                    }
                }

                class Program {
                    static void Main() {
                        Student student = new Student();
                        student.Name = "Maya";
                        student.Age = 14;
                        student.Introduce();
                    }
                }
            """,
            """
                using System;

                class Student {
                    public string Name;
                    public int Age;

                    public void Introduce() {
                        Console.WriteLine(Name + " " + Age);
                    }
                }

                class Program {
                    static void Main() {
                        Student student = new Student();
                        student.Name = "Maya";
                        student.Age = 14;
                        student.Introduce();
                    }
                }
            """,
            "Maya 14"
        ),
        exercise(
            "cs_u9_c1",
            "Safe parse",
            "Use try / catch to parse text safely.",
            """
                using System;

                class Program {
                    static void Main() {
                        string text = "42";

                        // TODO: Parse text into an int.
                        // Print the number if it works, otherwise print Invalid number.
                    }
                }
            """,
            """
                using System;

                class Program {
                    static void Main() {
                        string text = "42";

                        try {
                            int number = int.Parse(text);
                            Console.WriteLine(number);
                        } catch (Exception) {
                            Console.WriteLine("Invalid number");
                        }
                    }
                }
            """,
            "42"
        ),
        exercise(
            "cs_u10_c1",
            "Final check",
            "Combine arrays, loops, methods, and conditions.",
            """
                using System;

                class Program {
                    static void PrintPassing(int[] scores) {
                        // TODO: Print only scores that are at least 75.
                    }

                    static void Main() {
                        int[] scores = {60, 88, 75, 42, 91};
                        PrintPassing(scores);
                    }
                }
            """,
            """
                using System;

                class Program {
                    static void PrintPassing(int[] scores) {
                        foreach (int score in scores) {
                            if (score >= 75) {
                                Console.WriteLine(score);
                            }
                        }
                    }

                    static void Main() {
                        int[] scores = {60, 88, 75, 42, 91};
                        PrintPassing(scores);
                    }
                }
            """,
            "88\n75\n91"
        )
    ).associateBy { it.nodeId }
}
