package com.example.gocode.lessons

object JavaLessonsRepository {

    fun getSteps(nodeId: String): List<LessonStep> {
        return when (nodeId) {
            "java_u1_l2" -> getLesson2Steps()
            "java_u2_l1" -> getSection2Lesson1Steps()
            "java_u2_l2" -> getSection2Lesson2Steps()
            "java_u3_l1" -> getSection3Lesson1Steps()
            "java_u3_l2" -> getSection3Lesson2Steps()
            "java_u4_l1" -> getSection4Lesson1Steps()
            "java_u4_l2" -> getSection4Lesson2Steps()
            "java_u5_l1" -> getSection5Lesson1Steps()
            "java_u5_l2" -> getSection5Lesson2Steps()
            "java_u6_l1" -> getSection6Lesson1Steps()
            "java_u6_l2" -> getSection6Lesson2Steps()
            "java_u7_l1" -> getSection7Lesson1Steps()
            "java_u7_l2" -> getSection7Lesson2Steps()
            "java_u8_l1" -> getSection8Lesson1Steps()
            "java_u8_l2" -> getSection8Lesson2Steps()
            "java_u9_l1" -> getSection9Lesson1Steps()
            "java_u9_l2" -> getSection9Lesson2Steps()
            "java_u10_l1" -> getSection10Lesson1Steps()
            "java_u10_l2" -> getSection10Lesson2Steps()
            else -> getLesson1Steps()
        }
    }

    fun getLesson1Steps(): List<LessonStep> {
        return listOf(
            LessonStep(
                id = "java_l1_s1",
                title = "Welcome to Java",
                body = "Java is a programming language used to build apps, games, and big systems.\n\nLet's write your first real Java program step-by-step."
            ),
            LessonStep(
                id = "java_l1_s2",
                title = "A Java program has a class",
                body = "In Java, your code lives inside a class.\n\nMost beginners start with a class called Main:",
                code = """
                    public class Main {
                    }
                """.trimIndent(),
                tip = "Do not worry about the word 'public' yet. You will understand it soon."
            ),
            LessonStep(
                id = "java_l1_s3",
                title = "main() starts the program",
                body = "Java starts running your code from a special method called main().\n\nThis is your program entry point:",
                code = """
                    public class Main {
                        public static void main(String[] args) {
                        }
                    }
                """.trimIndent(),
                tip = "Think of main() as the START button of your program."
            ),
            LessonStep(
                id = "java_l1_s4",
                title = "Print to the console",
                body = "To show text on the screen, we use:",
                code = """
                    System.out.println("Hello World");
                """.trimIndent(),
                tip = "println means: print line. It prints text and then moves to the next line."
            ),
            LessonStep(
                id = "java_l1_s5",
                title = "Your first Java program",
                body = "Here is the full working program:\n\nRead it once and make sure it makes sense.",
                code = """
                    public class Main {
                        public static void main(String[] args) {
                            System.out.println("Hello World");
                        }
                    }
                """.trimIndent(),
                tip = "In the next step, you will practice with questions and fill in the blanks."
            )
        )
    }

    private fun getLesson2Steps(): List<LessonStep> {
        return listOf(
            LessonStep(
                id = "java_l2_s1",
                title = "Variables store values",
                body = "A variable is a named box for data.\n\nInstead of writing the same value again and again, you give it a name and reuse it."
            ),
            LessonStep(
                id = "java_l2_s2",
                title = "The variable pattern",
                body = "Most Java variables follow the same pattern:\n\ntype name = value;\n\nThe type tells Java what kind of data the variable can hold.",
                code = """
                    int score = 10;
                    String name = "Maya";
                """.trimIndent(),
                tip = "The semicolon at the end tells Java: this instruction is complete."
            ),
            LessonStep(
                id = "java_l2_s3",
                title = "int stores whole numbers",
                body = "Use int for whole numbers: ages, scores, counts, levels, and points.\n\nAn int does not store decimal values.",
                code = """
                    int age = 14;
                    int coins = 250;
                """.trimIndent(),
                tip = "Good variable names explain the value: coins is clearer than x."
            ),
            LessonStep(
                id = "java_l2_s4",
                title = "double stores decimals",
                body = "Use double for numbers with a decimal point: prices, measurements, averages, and percentages.",
                code = """
                    double price = 19.99;
                    double height = 1.75;
                """.trimIndent(),
                tip = "If the value can have a decimal point, double is usually the beginner-friendly choice."
            ),
            LessonStep(
                id = "java_l2_s5",
                title = "boolean stores true or false",
                body = "Use boolean when something can be only true or false.\n\nThis is useful for conditions like: is the player alive? is the user logged in?",
                code = """
                    boolean isLoggedIn = true;
                    boolean gameOver = false;
                """.trimIndent(),
                tip = "Boolean variable names often start with is, has, or can."
            ),
            LessonStep(
                id = "java_l2_s6",
                title = "char stores one character",
                body = "Use char for exactly one character.\n\nA char uses single quotes, not double quotes.",
                code = """
                    char grade = 'A';
                    char firstLetter = 'J';
                """.trimIndent(),
                tip = "Use 'A' for char, but \"A\" for String."
            ),
            LessonStep(
                id = "java_l2_s7",
                title = "String stores text",
                body = "Use String for text: names, messages, titles, and sentences.\n\nString starts with a capital S, and the text goes inside double quotes.",
                code = """
                    String name = "Leo";
                    String message = "Welcome back!";
                """.trimIndent(),
                tip = "In Java, capitalization matters. String is correct, string is not."
            ),
            LessonStep(
                id = "java_l2_s8",
                title = "Print variables",
                body = "You can print a variable by putting its name inside println.\n\nUse quotes for exact text. Use no quotes when you want the variable's value.",
                code = """
                    String name = "Leo";
                    System.out.println(name);
                    System.out.println("name");
                """.trimIndent(),
                tip = "The first print shows Leo. The second print shows the word name."
            ),
            LessonStep(
                id = "java_l2_s9",
                title = "Change a variable",
                body = "After a variable exists, you can give it a new value.\n\nYou only write the type when you create the variable the first time.",
                code = """
                    int score = 0;
                    score = 10;
                    System.out.println(score);
                """.trimIndent(),
                tip = "This prints 10, because score was updated before printing."
            )
        )
    }

    private fun getSection2Lesson1Steps(): List<LessonStep> {
        return listOf(
            LessonStep(
                id = "java_s2_l1_s1",
                title = "Programs make decisions",
                body = "So far, our programs run every line in order.\n\nNow we will teach Java to choose what to do based on a condition."
            ),
            LessonStep(
                id = "java_s2_l1_s2",
                title = "A condition is true or false",
                body = "A condition is a question Java can answer with true or false.\n\nExample: is the score high enough?",
                code = """
                    int score = 90;
                    score >= 75
                """.trimIndent(),
                tip = "This condition is true because 90 is greater than or equal to 75."
            ),
            LessonStep(
                id = "java_s2_l1_s3",
                title = "Comparison operators",
                body = "Use comparison operators to compare values.\n\nThese are the most common ones:",
                code = """
                    >   greater than
                    <   less than
                    >=  greater than or equal
                    <=  less than or equal
                    ==  equal to
                    !=  not equal to
                """.trimIndent(),
                tip = "Use == to compare values. A single = is for assigning a value."
            ),
            LessonStep(
                id = "java_s2_l1_s4",
                title = "The if statement",
                body = "An if statement runs code only when its condition is true.",
                code = """
                    int score = 90;

                    if (score >= 75) {
                        System.out.println("Passed");
                    }
                """.trimIndent(),
                tip = "The code inside the braces runs only if the condition is true."
            ),
            LessonStep(
                id = "java_s2_l1_s5",
                title = "If with booleans",
                body = "Boolean variables already store true or false, so they fit naturally inside if statements.",
                code = """
                    boolean isLoggedIn = true;

                    if (isLoggedIn) {
                        System.out.println("Welcome back");
                    }
                """.trimIndent(),
                tip = "You do not need to write isLoggedIn == true. The variable is already a condition."
            ),
            LessonStep(
                id = "java_s2_l1_s6",
                title = "If can protect code",
                body = "Use if when code should run only in the right situation.\n\nThis is one of the most important ideas in programming.",
                code = """
                    int age = 16;

                    if (age >= 13) {
                        System.out.println("You can join");
                    }
                """.trimIndent(),
                tip = "Next, you will practice choosing the correct condition."
            )
        )
    }

    private fun getSection2Lesson2Steps(): List<LessonStep> {
        return listOf(
            LessonStep(
                id = "java_s2_l2_s1",
                title = "else handles the other path",
                body = "if handles what happens when a condition is true.\n\nelse handles what happens when it is false."
            ),
            LessonStep(
                id = "java_s2_l2_s2",
                title = "if / else",
                body = "Use if / else when there are two possible paths.",
                code = """
                    int score = 60;

                    if (score >= 75) {
                        System.out.println("Passed");
                    } else {
                        System.out.println("Try again");
                    }
                """.trimIndent(),
                tip = "Only one branch runs: either the if branch or the else branch."
            ),
            LessonStep(
                id = "java_s2_l2_s3",
                title = "else if adds more choices",
                body = "Use else if when there are more than two possible outcomes.",
                code = """
                    int score = 85;

                    if (score >= 90) {
                        System.out.println("Excellent");
                    } else if (score >= 75) {
                        System.out.println("Passed");
                    } else {
                        System.out.println("Try again");
                    }
                """.trimIndent(),
                tip = "Java checks from top to bottom and stops at the first true condition."
            ),
            LessonStep(
                id = "java_s2_l2_s4",
                title = "&& means AND",
                body = "Use && when two conditions must both be true.",
                code = """
                    int age = 16;
                    boolean hasTicket = true;

                    if (age >= 13 && hasTicket) {
                        System.out.println("Enter");
                    }
                """.trimIndent(),
                tip = "Both sides of && must be true."
            ),
            LessonStep(
                id = "java_s2_l2_s5",
                title = "|| means OR",
                body = "Use || when at least one condition needs to be true.",
                code = """
                    boolean isAdmin = false;
                    boolean isTeacher = true;

                    if (isAdmin || isTeacher) {
                        System.out.println("Access granted");
                    }
                """.trimIndent(),
                tip = "Only one side of || needs to be true."
            ),
            LessonStep(
                id = "java_s2_l2_s6",
                title = "! flips a boolean",
                body = "Use ! to mean not.\n\nIt turns true into false, and false into true.",
                code = """
                    boolean gameOver = false;

                    if (!gameOver) {
                        System.out.println("Keep playing");
                    }
                """.trimIndent(),
                tip = "!gameOver means: gameOver is not true."
            ),
            LessonStep(
                id = "java_s2_l2_s7",
                title = "Decisions make programs feel smart",
                body = "With if, else, comparisons, and boolean logic, your programs can react to different situations.",
                code = """
                    int coins = 120;

                    if (coins >= 100) {
                        System.out.println("You can buy the sword");
                    } else {
                        System.out.println("Collect more coins");
                    }
                """.trimIndent(),
                tip = "Next, the quiz will check the full decisions section."
            )
        )
    }

    private fun getSection3Lesson1Steps(): List<LessonStep> {
        return listOf(
            LessonStep(
                id = "java_s3_l1_s1",
                title = "Loops repeat code",
                body = "A loop lets your program repeat work without copying the same line again and again.\n\nThis is useful for counting, retrying, checking items, and building patterns."
            ),
            LessonStep(
                id = "java_s3_l1_s2",
                title = "The while loop",
                body = "A while loop keeps running as long as its condition is true.",
                code = """
                    while (condition) {
                        // repeat this code
                    }
                """.trimIndent(),
                tip = "Read it like English: while this is true, keep doing the block."
            ),
            LessonStep(
                id = "java_s3_l1_s3",
                title = "Start with a counter",
                body = "Many beginner loops use a counter variable.\n\nThe counter remembers which repeat we are on.",
                code = """
                    int count = 1;

                    while (count <= 5) {
                        System.out.println(count);
                        count++;
                    }
                """.trimIndent(),
                tip = "count++ means: add 1 to count."
            ),
            LessonStep(
                id = "java_s3_l1_s4",
                title = "The condition controls the loop",
                body = "The loop checks the condition before every repeat.\n\nWhen count becomes 6, count <= 5 is false, so the loop stops.",
                code = """
                    int count = 1;

                    while (count <= 5) {
                        System.out.println("Loop");
                        count++;
                    }
                """.trimIndent(),
                tip = "A loop needs a clear stopping point."
            ),
            LessonStep(
                id = "java_s3_l1_s5",
                title = "Avoid infinite loops",
                body = "If the condition never becomes false, the loop will not stop.\n\nThat is called an infinite loop.",
                code = """
                    int count = 1;

                    while (count <= 5) {
                        System.out.println(count);
                        // missing count++
                    }
                """.trimIndent(),
                tip = "If count never changes, count <= 5 stays true forever."
            ),
            LessonStep(
                id = "java_s3_l1_s6",
                title = "Loops work with decisions",
                body = "You can put if statements inside loops.\n\nThis lets the program react during each repeat.",
                code = """
                    int count = 1;

                    while (count <= 5) {
                        if (count == 3) {
                            System.out.println("Middle");
                        }
                        count++;
                    }
                """.trimIndent(),
                tip = "This combines what you learned in Section 2 with loops."
            )
        )
    }

    private fun getSection3Lesson2Steps(): List<LessonStep> {
        return listOf(
            LessonStep(
                id = "java_s3_l2_s1",
                title = "for loops are compact",
                body = "A for loop is a cleaner way to write many counting loops.\n\nIt puts the start, condition, and update in one line."
            ),
            LessonStep(
                id = "java_s3_l2_s2",
                title = "The for loop pattern",
                body = "A for loop has three parts:\n\nstart; keep going while; update",
                code = """
                    for (int i = 1; i <= 5; i++) {
                        System.out.println(i);
                    }
                """.trimIndent(),
                tip = "i is a common short name for a loop counter."
            ),
            LessonStep(
                id = "java_s3_l2_s3",
                title = "Part 1: start",
                body = "The first part creates the counter before the loop begins.",
                code = """
                    for (int i = 1; i <= 5; i++) {
                        System.out.println(i);
                    }
                """.trimIndent(),
                tip = "Here, int i = 1 means the loop starts counting from 1."
            ),
            LessonStep(
                id = "java_s3_l2_s4",
                title = "Part 2: condition",
                body = "The second part decides if the loop should keep running.",
                code = """
                    for (int i = 1; i <= 5; i++) {
                        System.out.println(i);
                    }
                """.trimIndent(),
                tip = "i <= 5 means the loop runs while i is 1, 2, 3, 4, or 5."
            ),
            LessonStep(
                id = "java_s3_l2_s5",
                title = "Part 3: update",
                body = "The third part runs after each repeat.\n\nIt usually changes the counter.",
                code = """
                    for (int i = 1; i <= 5; i++) {
                        System.out.println(i);
                    }
                """.trimIndent(),
                tip = "i++ adds 1 after each loop repeat."
            ),
            LessonStep(
                id = "java_s3_l2_s6",
                title = "Count down",
                body = "A for loop can also count down by using -- and a greater-than condition.",
                code = """
                    for (int i = 5; i >= 1; i--) {
                        System.out.println(i);
                    }
                """.trimIndent(),
                tip = "i-- subtracts 1 after each repeat."
            ),
            LessonStep(
                id = "java_s3_l2_s7",
                title = "When to use each loop",
                body = "Use for when you know how many times you want to repeat.\n\nUse while when the loop depends on a changing condition.",
                code = """
                    for (int i = 1; i <= 3; i++) {
                        System.out.println("Practice");
                    }
                """.trimIndent(),
                tip = "Next, you will practice building loops yourself."
            )
        )
    }

    private fun getSection4Lesson1Steps(): List<LessonStep> = listOf(
        LessonStep(
            id = "java_s4_l1_s1",
            title = "Arrays store many values",
            body = "An array is one variable that holds a list of values of the same type.\n\nInstead of creating score1, score2, and score3, you can store all scores together."
        ),
        LessonStep(
            id = "java_s4_l1_s2",
            title = "Create an array",
            body = "Use square brackets after the type to create an array.",
            code = """
                int[] scores = {90, 75, 88};
                String[] names = {"Leo", "Maya", "Noam"};
            """.trimIndent(),
            tip = "Every value in one array should match the array type."
        ),
        LessonStep(
            id = "java_s4_l1_s3",
            title = "Read from an array",
            body = "Use an index inside square brackets to read one value from the array.",
            code = """
                String[] names = {"Leo", "Maya", "Noam"};
                System.out.println(names[0]);
            """.trimIndent(),
            tip = "This prints Leo because arrays start counting from 0."
        ),
        LessonStep(
            id = "java_s4_l1_s4",
            title = "Array length",
            body = "Use .length to know how many values are inside an array.",
            code = """
                int[] scores = {90, 75, 88};
                System.out.println(scores.length);
            """.trimIndent(),
            tip = "This prints 3."
        ),
        LessonStep(
            id = "java_s4_l1_s5",
            title = "Arrays and loops work together",
            body = "Loops are perfect for reading every value in an array.",
            code = """
                int[] scores = {90, 75, 88};

                for (int i = 0; i < scores.length; i++) {
                    System.out.println(scores[i]);
                }
            """.trimIndent(),
            tip = "i starts at 0 because the first array index is 0."
        )
    )

    private fun getSection4Lesson2Steps(): List<LessonStep> = listOf(
        LessonStep(
            id = "java_s4_l2_s1",
            title = "Indexes start at zero",
            body = "The first value in an array is index 0, the second is index 1, and so on.\n\nThis is one of the most important array rules."
        ),
        LessonStep(
            id = "java_s4_l2_s2",
            title = "Change an array value",
            body = "You can update one place in the array by using its index.",
            code = """
                int[] scores = {90, 75, 88};
                scores[1] = 80;
            """.trimIndent(),
            tip = "scores[1] changes the second value."
        ),
        LessonStep(
            id = "java_s4_l2_s3",
            title = "Last index",
            body = "If an array has 3 values, the last index is 2.\n\nIn general, the last index is length - 1.",
            code = """
                String[] names = {"Leo", "Maya", "Noam"};
                System.out.println(names[names.length - 1]);
            """.trimIndent(),
            tip = "This prints the last name safely."
        ),
        LessonStep(
            id = "java_s4_l2_s4",
            title = "Avoid index mistakes",
            body = "Trying to read an index that does not exist causes an error.",
            code = """
                int[] scores = {90, 75, 88};
                System.out.println(scores[3]); // error
            """.trimIndent(),
            tip = "scores[3] is outside the array because the last index is 2."
        ),
        LessonStep(
            id = "java_s4_l2_s5",
            title = "Enhanced for loop",
            body = "When you only need the values, an enhanced for loop is very readable.",
            code = """
                for (int score : scores) {
                    System.out.println(score);
                }
            """.trimIndent(),
            tip = "Read it as: for each score in scores."
        )
    )

    private fun getSection5Lesson1Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s5_l1_s1", title = "Methods organize code", body = "A method is a named block of code.\n\nMethods help you reuse logic and keep main() easier to read."),
        LessonStep(
            id = "java_s5_l1_s2",
            title = "Create a method",
            body = "For now, place beginner methods inside the Main class, outside main().",
            code = """
                static void sayHello() {
                    System.out.println("Hello");
                }
            """.trimIndent(),
            tip = "void means the method does not return a value."
        ),
        LessonStep(
            id = "java_s5_l1_s3",
            title = "Call a method",
            body = "A method runs only when you call it by name.",
            code = """
                public static void main(String[] args) {
                    sayHello();
                }
            """.trimIndent(),
            tip = "The parentheses are part of the method call."
        ),
        LessonStep(
            id = "java_s5_l1_s4",
            title = "Reuse the same method",
            body = "You can call the same method many times.",
            code = """
                sayHello();
                sayHello();
                sayHello();
            """.trimIndent(),
            tip = "One method definition can power many calls."
        )
    )

    private fun getSection5Lesson2Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s5_l2_s1", title = "Parameters pass information", body = "A parameter is a value a method receives.\n\nIt makes methods flexible."),
        LessonStep(
            id = "java_s5_l2_s2",
            title = "Method with a parameter",
            body = "Write the parameter type and name inside the parentheses.",
            code = """
                static void greet(String name) {
                    System.out.println("Hello " + name);
                }
            """.trimIndent(),
            tip = "name behaves like a variable inside the method."
        ),
        LessonStep(
            id = "java_s5_l2_s3",
            title = "Return a value",
            body = "A method can calculate a value and send it back with return.",
            code = """
                static int doubleNumber(int number) {
                    return number * 2;
                }
            """.trimIndent(),
            tip = "int before the method name means this method returns an int."
        ),
        LessonStep(
            id = "java_s5_l2_s4",
            title = "Store returned values",
            body = "You can store a returned value in a variable.",
            code = """
                int result = doubleNumber(4);
                System.out.println(result);
            """.trimIndent(),
            tip = "This prints 8."
        )
    )

    private fun getSection6Lesson1Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s6_l1_s1", title = "Programs can read input", body = "Scanner lets a console program read what the user types.\n\nThis makes programs interactive."),
        LessonStep(
            id = "java_s6_l1_s2",
            title = "Import Scanner",
            body = "Scanner comes from java.util, so it needs an import at the top of the file.",
            code = """
                import java.util.Scanner;
            """.trimIndent(),
            tip = "Imports go before public class Main."
        ),
        LessonStep(
            id = "java_s6_l1_s3",
            title = "Create Scanner",
            body = "Create a Scanner connected to System.in.",
            code = """
                Scanner input = new Scanner(System.in);
            """.trimIndent(),
            tip = "System.in means keyboard input."
        ),
        LessonStep(
            id = "java_s6_l1_s4",
            title = "Read text and numbers",
            body = "Use nextLine() for text and nextInt() for whole numbers.",
            code = """
                String name = input.nextLine();
                int age = input.nextInt();
            """.trimIndent(),
            tip = "Choose the Scanner method that matches the value you need."
        )
    )

    private fun getSection6Lesson2Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s6_l2_s1", title = "Use input in decisions", body = "After reading input, you can use it with if / else just like any other variable."),
        LessonStep(
            id = "java_s6_l2_s2",
            title = "Ask and check",
            body = "This program asks for age and reacts to the answer.",
            code = """
                int age = input.nextInt();

                if (age >= 13) {
                    System.out.println("Welcome");
                } else {
                    System.out.println("Too young");
                }
            """.trimIndent(),
            tip = "Input becomes more powerful when combined with conditions."
        ),
        LessonStep(
            id = "java_s6_l2_s3",
            title = "Prompts help the user",
            body = "Print a short question before reading input.",
            code = """
                System.out.println("Enter your age:");
                int age = input.nextInt();
            """.trimIndent(),
            tip = "Good prompts make console programs easier to use."
        ),
        LessonStep(
            id = "java_s6_l2_s4",
            title = "Close Scanner at the end",
            body = "When the program is done reading, you can close the Scanner.",
            code = """
                input.close();
            """.trimIndent(),
            tip = "Close it near the end of main()."
        )
    )

    private fun getSection7Lesson1Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s7_l1_s1", title = "Strings have useful tools", body = "String is more than text storage.\n\nJava gives strings methods that can measure, compare, and change text."),
        LessonStep(id = "java_s7_l1_s2", title = "length()", body = "Use length() to count characters.", code = """String name = "Leo";
System.out.println(name.length());""", tip = "Leo has 3 characters."),
        LessonStep(id = "java_s7_l1_s3", title = "toUpperCase()", body = "Use toUpperCase() to create an uppercase version of text.", code = """String word = "java";
System.out.println(word.toUpperCase());""", tip = "This prints JAVA."),
        LessonStep(id = "java_s7_l1_s4", title = "contains()", body = "Use contains() to check if text includes another piece of text.", code = """String email = "leo@gocode.com";
System.out.println(email.contains("@"));""", tip = "contains() returns true or false.")
    )

    private fun getSection7Lesson2Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s7_l2_s1", title = "Compare strings safely", body = "Use equals() to compare String values.\n\nDo not use == for beginner string comparison."),
        LessonStep(id = "java_s7_l2_s2", title = "equals()", body = "equals() checks if two strings have the same text.", code = """String answer = "yes";

if (answer.equals("yes")) {
    System.out.println("Confirmed");
}""", tip = "This checks the text, not just the variable reference."),
        LessonStep(id = "java_s7_l2_s3", title = "equalsIgnoreCase()", body = "Use equalsIgnoreCase() when capital letters should not matter.", code = """String answer = "YES";
System.out.println(answer.equalsIgnoreCase("yes"));""", tip = "This prints true."),
        LessonStep(id = "java_s7_l2_s4", title = "trim()", body = "trim() removes extra spaces from the start and end.", code = """String name = "  Leo  ";
System.out.println(name.trim());""", tip = "This is useful for user input.")
    )

    private fun getSection8Lesson1Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s8_l1_s1", title = "Classes describe things", body = "A class is a blueprint for a kind of object.\n\nFor example, a Student class can describe student data."),
        LessonStep(id = "java_s8_l1_s2", title = "Fields store object data", body = "Fields are variables that belong to the class.", code = """class Student {
    String name;
    int age;
}""", tip = "Fields describe what each Student can remember."),
        LessonStep(id = "java_s8_l1_s3", title = "Classes can have methods", body = "A class can also contain methods that use its fields.", code = """class Student {
    String name;

    void introduce() {
        System.out.println("Hi, I am " + name);
    }
}""", tip = "Object methods describe behavior.")
    )

    private fun getSection8Lesson2Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s8_l2_s1", title = "Objects are real examples", body = "If a class is a blueprint, an object is one real thing created from it."),
        LessonStep(id = "java_s8_l2_s2", title = "Create an object", body = "Use new to create an object.", code = """Student student = new Student();""", tip = "This creates one Student object."),
        LessonStep(id = "java_s8_l2_s3", title = "Set fields", body = "Use dot syntax to set or read object fields.", code = """student.name = "Maya";
student.age = 14;""", tip = "The dot means: inside this object."),
        LessonStep(id = "java_s8_l2_s4", title = "Call object methods", body = "Use dot syntax to call a method on an object.", code = """student.introduce();""", tip = "The method uses the object's own data.")
    )

    private fun getSection9Lesson1Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s9_l1_s1", title = "Errors are information", body = "Errors are not a sign that you failed.\n\nThey are messages that help you find what Java could not understand or could not run."),
        LessonStep(id = "java_s9_l1_s2", title = "Compile-time errors", body = "A compile-time error happens before the program runs.", code = """System.out.println("Hello")""", tip = "This line is missing a semicolon."),
        LessonStep(id = "java_s9_l1_s3", title = "Runtime errors", body = "A runtime error happens while the program is running.", code = """int[] scores = {90, 80};
System.out.println(scores[5]);""", tip = "Index 5 does not exist."),
        LessonStep(id = "java_s9_l1_s4", title = "Read the line number", body = "Error messages often include a line number.\n\nStart there, then check nearby lines too.", tip = "Many mistakes are one line above the reported line.")
    )

    private fun getSection9Lesson2Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s9_l2_s1", title = "try / catch handles risky code", body = "Some code can fail at runtime.\n\ntry / catch lets your program respond instead of crashing."),
        LessonStep(id = "java_s9_l2_s2", title = "Basic try / catch", body = "Put risky code inside try and the fallback inside catch.", code = """try {
    int number = Integer.parseInt("abc");
} catch (Exception e) {
    System.out.println("Invalid number");
}""", tip = "parseInt tries to turn text into an int."),
        LessonStep(id = "java_s9_l2_s3", title = "Use helpful debug prints", body = "Printing important values can help you understand what the program is doing.", code = """System.out.println("age = " + age);""", tip = "Remove or clean debug prints when the code is finished."),
        LessonStep(id = "java_s9_l2_s4", title = "Fix one error at a time", body = "When you see many errors, start with the first one.\n\nFix it, run again, then continue.", tip = "One missing brace can create many confusing errors.")
    )

    private fun getSection10Lesson1Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s10_l1_s1", title = "You now know the core pieces", body = "You learned program structure, variables, conditions, loops, arrays, methods, input, strings, objects, and debugging."),
        LessonStep(id = "java_s10_l1_s2", title = "Think in steps", body = "When solving a problem, break it down:\n\nWhat data do I need?\nWhat decision do I need?\nWhat repeats?\nWhat can become a method?"),
        LessonStep(id = "java_s10_l1_s3", title = "Combine tools", body = "Real programs combine many small ideas.", code = """for (String name : names) {
    if (name.length() > 3) {
        greet(name);
    }
}""", tip = "This combines arrays, loops, strings, if, and methods.")
    )

    private fun getSection10Lesson2Steps(): List<LessonStep> = listOf(
        LessonStep(id = "java_s10_l2_s1", title = "Practice builds speed", body = "At this point, the goal is not memorizing everything.\n\nThe goal is recognizing patterns and knowing where to look."),
        LessonStep(id = "java_s10_l2_s2", title = "Common beginner checklist", body = "Check semicolons, braces, variable names, types, and indexes.\n\nMost beginner bugs live there."),
        LessonStep(id = "java_s10_l2_s3", title = "Ready for the next level", body = "After this review, you are ready for deeper Java topics like constructors, lists, files, and Android-specific code.", tip = "The final quiz will mix ideas from the whole Java path.")
    )
}
