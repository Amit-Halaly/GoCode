package com.example.gocode.lessons

import com.example.gocode.network.models.runModels.RunTestCase

object JavaCodeExerciseRepository {

    fun getExercise(nodeId: String): CodeExercise {
        return exercises[nodeId] ?: exercises.getValue("java_u1_c1")
    }

    fun getExercises(): Map<String, CodeExercise> = exercises

    private val exercises: Map<String, CodeExercise> = listOf(
        CodeExercise(
            nodeId = "java_u1_c1",
            title = "Print a greeting",
            subtitle = "Use the first-program lesson to print Hello GoCode! from main.",
            answer = """
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello GoCode!");
                    }
                }
            """.trimIndent(),
            tests = listOf(
                RunTestCase(name = "Greeting", expectedOutput = "Hello GoCode!"),
                RunTestCase(name = "Hidden exact line", expectedOutput = "Hello GoCode!", hidden = true)
            ),
            template = """
                public class Main {
                    public static void main(String[] args) {
                        // TODO: Print Hello GoCode!
                    }
                }
            """.trimIndent()
        ),
        CodeExercise(
            nodeId = "java_u2_c1",
            title = "Password check",
            subtitle = "Use if / else and boolean logic to decide whether access is allowed.",
            answer = """
                public class Main {
                    public static void main(String[] args) {
                        int age = 16;
                        boolean hasPassword = true;

                        if (age >= 13 && hasPassword) {
                            System.out.println("Access granted");
                        } else {
                            System.out.println("Access denied");
                        }
                    }
                }
            """.trimIndent(),
            tests = listOf(
                RunTestCase(name = "Allowed case", expectedOutput = "Access granted"),
                RunTestCase(name = "Hidden allowed case", expectedOutput = "Access granted", hidden = true)
            ),
            template = """
                public class Main {
                    public static void main(String[] args) {
                        int age = 16;
                        boolean hasPassword = true;

                        // TODO: Print "Access granted" only when both conditions are true.
                        // Otherwise print "Access denied".
                    }
                }
            """.trimIndent()
        ),
        CodeExercise(
            nodeId = "java_u3_c1",
            title = "Count loop",
            subtitle = "Use a for loop to print 1 to 5 and mark the middle value.",
            answer = """
                public class Main {
                    public static void main(String[] args) {
                        for (int i = 1; i <= 5; i++) {
                            System.out.println(i);
                            if (i == 3) {
                                System.out.println("Middle");
                            }
                        }
                    }
                }
            """.trimIndent(),
            tests = listOf(
                RunTestCase(name = "Loop output", expectedOutput = "1\n2\n3\nMiddle\n4\n5"),
                RunTestCase(name = "Hidden loop output", expectedOutput = "1\n2\n3\nMiddle\n4\n5", hidden = true)
            ),
            template = """
                public class Main {
                    public static void main(String[] args) {
                        // TODO: Use a for loop to print the numbers 1 to 5.
                        // When the number is 3, also print "Middle".
                    }
                }
            """.trimIndent()
        ),
        CodeExercise(
            nodeId = "java_u4_c1",
            title = "Favorite list",
            subtitle = "Use an array loop to print every value in order.",
            answer = """
                public class Main {
                    public static void main(String[] args) {
                        String[] favorites = {"Java", "Android", "GoCode"};

                        for (String favorite : favorites) {
                            System.out.println(favorite);
                        }
                    }
                }
            """.trimIndent(),
            tests = listOf(
                RunTestCase(name = "Array values", expectedOutput = "Java\nAndroid\nGoCode"),
                RunTestCase(name = "Hidden array values", expectedOutput = "Java\nAndroid\nGoCode", hidden = true)
            ),
            template = """
                public class Main {
                    public static void main(String[] args) {
                        String[] favorites = {"Java", "Android", "GoCode"};

                        // TODO: Use a loop to print every value in favorites.
                    }
                }
            """.trimIndent()
        ),
        CodeExercise(
            nodeId = "java_u5_c1",
            title = "Helper method",
            subtitle = "Complete a reusable method that greets each name it receives.",
            answer = """
                public class Main {
                    static void greet(String name) {
                        System.out.println("Hello " + name);
                    }

                    public static void main(String[] args) {
                        greet("Leo");
                        greet("Maya");
                    }
                }
            """.trimIndent(),
            tests = listOf(
                RunTestCase(name = "Method calls", expectedOutput = "Hello Leo\nHello Maya"),
                RunTestCase(name = "Hidden method calls", expectedOutput = "Hello Leo\nHello Maya", hidden = true)
            ),
            template = """
                public class Main {
                    static void greet(String name) {
                        // TODO: Print "Hello " plus the name.
                    }

                    public static void main(String[] args) {
                        greet("Leo");
                        greet("Maya");
                    }
                }
            """.trimIndent()
        ),
        CodeExercise(
            nodeId = "java_u6_c1",
            title = "Ask age",
            subtitle = "Use Scanner input with an if / else decision.",
            answer = """
                import java.util.Scanner;

                public class Main {
                    public static void main(String[] args) {
                        Scanner input = new Scanner(System.in);
                        int age = input.nextInt();

                        if (age >= 13) {
                            System.out.println("Welcome");
                        } else {
                            System.out.println("Too young");
                        }

                        input.close();
                    }
                }
            """.trimIndent(),
            defaultInput = "16",
            tests = listOf(
                RunTestCase(name = "Teen user", input = "16\n", expectedOutput = "Welcome"),
                RunTestCase(name = "Too young", input = "10\n", expectedOutput = "Too young"),
                RunTestCase(name = "Hidden boundary", input = "13\n", expectedOutput = "Welcome", hidden = true)
            ),
            template = """
                import java.util.Scanner;

                public class Main {
                    public static void main(String[] args) {
                        Scanner input = new Scanner(System.in);

                        // TODO: Read an age and print "Welcome" or "Too young".

                        input.close();
                    }
                }
            """.trimIndent()
        ),
        CodeExercise(
            nodeId = "java_u7_c1",
            title = "Name checker",
            subtitle = "Use trim and equals to compare String text safely.",
            answer = """
                public class Main {
                    public static void main(String[] args) {
                        String name = "  Leo  ";

                        if (name.trim().equals("Leo")) {
                            System.out.println("Found Leo");
                        }
                    }
                }
            """.trimIndent(),
            tests = listOf(
                RunTestCase(name = "Trimmed match", expectedOutput = "Found Leo"),
                RunTestCase(name = "Hidden trimmed match", expectedOutput = "Found Leo", hidden = true)
            ),
            template = """
                public class Main {
                    public static void main(String[] args) {
                        String name = "  Leo  ";

                        // TODO: Trim the name and check if it equals "Leo".
                        // If it does, print "Found Leo".
                    }
                }
            """.trimIndent()
        ),
        CodeExercise(
            nodeId = "java_u8_c1",
            title = "Student object",
            subtitle = "Complete an object method that uses the object's fields.",
            answer = """
                class Student {
                    String name;
                    int age;

                    void introduce() {
                        System.out.println(name + " is " + age);
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Student student = new Student();
                        student.name = "Maya";
                        student.age = 14;
                        student.introduce();
                    }
                }
            """.trimIndent(),
            tests = listOf(
                RunTestCase(name = "Object output", expectedOutput = "Maya is 14"),
                RunTestCase(name = "Hidden object output", expectedOutput = "Maya is 14", hidden = true)
            ),
            template = """
                class Student {
                    String name;
                    int age;

                    void introduce() {
                        // TODO: Print the student's name and age like: Maya is 14
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Student student = new Student();
                        student.name = "Maya";
                        student.age = 14;
                        student.introduce();
                    }
                }
            """.trimIndent()
        ),
        CodeExercise(
            nodeId = "java_u9_c1",
            title = "Safe parse",
            subtitle = "Use try / catch to handle risky number parsing.",
            answer = """
                public class Main {
                    public static void main(String[] args) {
                        String text = "42";

                        try {
                            int number = Integer.parseInt(text);
                            System.out.println(number);
                        } catch (Exception e) {
                            System.out.println("Invalid number");
                        }
                    }
                }
            """.trimIndent(),
            tests = listOf(
                RunTestCase(name = "Valid number", expectedOutput = "42"),
                RunTestCase(name = "Hidden valid number", expectedOutput = "42", hidden = true)
            ),
            template = """
                public class Main {
                    public static void main(String[] args) {
                        String text = "42";

                        // TODO: Use try / catch to parse text into an int.
                        // Print the number if it works, otherwise print "Invalid number".
                    }
                }
            """.trimIndent()
        ),
        CodeExercise(
            nodeId = "java_u10_c1",
            title = "Final check",
            subtitle = "Combine methods, arrays, loops, and String length.",
            answer = """
                public class Main {
                    static void printLongNames(String[] names) {
                        for (String name : names) {
                            if (name.length() > 3) {
                                System.out.println(name);
                            }
                        }
                    }

                    public static void main(String[] args) {
                        String[] names = {"Leo", "Maya", "Noam", "Dan"};
                        printLongNames(names);
                    }
                }
            """.trimIndent(),
            tests = listOf(
                RunTestCase(name = "Long names", expectedOutput = "Maya\nNoam"),
                RunTestCase(name = "Hidden long names", expectedOutput = "Maya\nNoam", hidden = true)
            ),
            template = """
                public class Main {
                    static void printLongNames(String[] names) {
                        // TODO: Print only names longer than 3 characters.
                    }

                    public static void main(String[] args) {
                        String[] names = {"Leo", "Maya", "Noam", "Dan"};
                        printLongNames(names);
                    }
                }
            """.trimIndent()
        )
    ).associateBy { it.nodeId }
}
