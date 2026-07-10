package com.example.gocode.lessons

import com.example.gocode.network.models.runModels.RunTestCase

object CppCodeExerciseRepository {

    fun getExercise(nodeId: String): CodeExercise {
        return exercises[nodeId] ?: exercises.getValue("cpp_u1_c1")
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
        language = "cpp",
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
            "cpp_u1_c1",
            "Print a greeting",
            "Use cout from main to print Hello GoCode.",
            """
                #include <iostream>
                using namespace std;

                int main() {
                    // TODO: Print Hello GoCode
                    return 0;
                }
            """,
            """
                #include <iostream>
                using namespace std;

                int main() {
                    cout << "Hello GoCode" << endl;
                    return 0;
                }
            """,
            "Hello GoCode"
        ),
        exercise(
            "cpp_u2_c1",
            "Access check",
            "Use if / else and logical AND to decide access.",
            """
                #include <iostream>
                using namespace std;

                int main() {
                    int age = 16;
                    bool hasPassword = true;

                    // TODO: Print Access granted only when both conditions are true.
                    // Otherwise print Access denied.
                    return 0;
                }
            """,
            """
                #include <iostream>
                using namespace std;

                int main() {
                    int age = 16;
                    bool hasPassword = true;

                    if (age >= 13 && hasPassword) {
                        cout << "Access granted" << endl;
                    } else {
                        cout << "Access denied" << endl;
                    }
                    return 0;
                }
            """,
            "Access granted"
        ),
        exercise(
            "cpp_u3_c1",
            "Count loop",
            "Use a for loop to print 1 to 5 and mark the middle value.",
            """
                #include <iostream>
                using namespace std;

                int main() {
                    // TODO: Print 1 to 5.
                    // When the number is 3, also print Middle.
                    return 0;
                }
            """,
            """
                #include <iostream>
                using namespace std;

                int main() {
                    for (int i = 1; i <= 5; i++) {
                        cout << i << endl;
                        if (i == 3) {
                            cout << "Middle" << endl;
                        }
                    }
                    return 0;
                }
            """,
            "1\n2\n3\nMiddle\n4\n5"
        ),
        exercise(
            "cpp_u4_c1",
            "Scores vector",
            "Use a vector and loop to print every score.",
            """
                #include <iostream>
                #include <vector>
                using namespace std;

                int main() {
                    vector<int> scores = {90, 75, 88};

                    // TODO: Print every score.
                    return 0;
                }
            """,
            """
                #include <iostream>
                #include <vector>
                using namespace std;

                int main() {
                    vector<int> scores = {90, 75, 88};

                    for (int score : scores) {
                        cout << score << endl;
                    }
                    return 0;
                }
            """,
            "90\n75\n88"
        ),
        exercise(
            "cpp_u5_c1",
            "Helper function",
            "Complete a reusable function that greets each name.",
            """
                #include <iostream>
                #include <string>
                using namespace std;

                void greet(string name) {
                    // TODO: Print Hello plus the name.
                }

                int main() {
                    greet("Leo");
                    greet("Maya");
                    return 0;
                }
            """,
            """
                #include <iostream>
                #include <string>
                using namespace std;

                void greet(string name) {
                    cout << "Hello " << name << endl;
                }

                int main() {
                    greet("Leo");
                    greet("Maya");
                    return 0;
                }
            """,
            "Hello Leo\nHello Maya"
        ),
        exercise(
            "cpp_u6_c1",
            "Ask age",
            "Read an age with cin and print the right message.",
            """
                #include <iostream>
                using namespace std;

                int main() {
                    int age;

                    // TODO: Read age.
                    // Print Welcome if age is at least 13, otherwise print Too young.
                    return 0;
                }
            """,
            """
                #include <iostream>
                using namespace std;

                int main() {
                    int age;
                    cin >> age;

                    if (age >= 13) {
                        cout << "Welcome" << endl;
                    } else {
                        cout << "Too young" << endl;
                    }
                    return 0;
                }
            """,
            "Welcome",
            input = "16\n",
            hiddenInput = "10\n",
            hiddenExpected = "Too young"
        ),
        exercise(
            "cpp_u7_c1",
            "Name checker",
            "Use string comparison to check a name.",
            """
                #include <iostream>
                #include <string>
                using namespace std;

                int main() {
                    string name = "Leo";

                    // TODO: If name equals Leo, print Found Leo.
                    return 0;
                }
            """,
            """
                #include <iostream>
                #include <string>
                using namespace std;

                int main() {
                    string name = "Leo";

                    if (name == "Leo") {
                        cout << "Found Leo" << endl;
                    }
                    return 0;
                }
            """,
            "Found Leo"
        ),
        exercise(
            "cpp_u8_c1",
            "Student object",
            "Complete a class method that prints object data.",
            """
                #include <iostream>
                #include <string>
                using namespace std;

                class Student {
                public:
                    string name;
                    int age;

                    void introduce() {
                        // TODO: Print name and age with a space between them.
                    }
                };

                int main() {
                    Student student;
                    student.name = "Maya";
                    student.age = 14;
                    student.introduce();
                    return 0;
                }
            """,
            """
                #include <iostream>
                #include <string>
                using namespace std;

                class Student {
                public:
                    string name;
                    int age;

                    void introduce() {
                        cout << name << " " << age << endl;
                    }
                };

                int main() {
                    Student student;
                    student.name = "Maya";
                    student.age = 14;
                    student.introduce();
                    return 0;
                }
            """,
            "Maya 14"
        ),
        exercise(
            "cpp_u9_c1",
            "Safe parse",
            "Use try / catch to parse text safely.",
            """
                #include <iostream>
                #include <string>
                using namespace std;

                int main() {
                    string text = "42";

                    // TODO: Parse text into an int.
                    // Print the number if it works, otherwise print Invalid number.
                    return 0;
                }
            """,
            """
                #include <iostream>
                #include <string>
                using namespace std;

                int main() {
                    string text = "42";

                    try {
                        int number = stoi(text);
                        cout << number << endl;
                    } catch (exception& e) {
                        cout << "Invalid number" << endl;
                    }
                    return 0;
                }
            """,
            "42"
        ),
        exercise(
            "cpp_u10_c1",
            "Final check",
            "Combine vectors, loops, functions, and conditions.",
            """
                #include <iostream>
                #include <vector>
                using namespace std;

                void printPassing(vector<int> scores) {
                    // TODO: Print only scores that are at least 75.
                }

                int main() {
                    vector<int> scores = {60, 88, 75, 42, 91};
                    printPassing(scores);
                    return 0;
                }
            """,
            """
                #include <iostream>
                #include <vector>
                using namespace std;

                void printPassing(vector<int> scores) {
                    for (int score : scores) {
                        if (score >= 75) {
                            cout << score << endl;
                        }
                    }
                }

                int main() {
                    vector<int> scores = {60, 88, 75, 42, 91};
                    printPassing(scores);
                    return 0;
                }
            """,
            "88\n75\n91"
        )
    ).associateBy { it.nodeId }
}
