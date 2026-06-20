package com.example.gocode.lessons.lesson

import com.example.gocode.lessons.LessonStep

object JavaLessonsRepository {

    fun getSteps(nodeId: String): List<LessonStep> {
        return when (nodeId) {
            "java_u1_l2" -> getLesson2Steps()
            "java_u2_l1" -> getSection2Lesson1Steps()
            "java_u2_l2" -> getSection2Lesson2Steps()
            "java_u3_l1" -> getSection3Lesson1Steps()
            "java_u3_l2" -> getSection3Lesson2Steps()
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
}
