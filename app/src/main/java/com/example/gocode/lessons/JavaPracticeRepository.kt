package com.example.gocode.lessons

object JavaPracticeRepository {

    fun getQuestions(nodeId: String): List<PracticeQuestion> {
        return when (nodeId) {
            "java_u1_p2" -> getPractice2Questions()
            "java_u1_q1" -> getUnit1QuizQuestions()
            "java_u2_p1" -> getSection2Practice1Questions()
            "java_u2_p2" -> getSection2Practice2Questions()
            "java_u2_q1" -> getSection2QuizQuestions()
            "java_u3_p1" -> getSection3Practice1Questions()
            "java_u3_p2" -> getSection3Practice2Questions()
            "java_u3_q1" -> getSection3QuizQuestions()
            "java_u4_p1" -> getSection4Practice1Questions()
            "java_u4_p2" -> getSection4Practice2Questions()
            "java_u4_q1" -> getSection4QuizQuestions()
            "java_u5_p1" -> getSection5Practice1Questions()
            "java_u5_p2" -> getSection5Practice2Questions()
            "java_u5_q1" -> getSection5QuizQuestions()
            "java_u6_p1" -> getSection6Practice1Questions()
            "java_u6_p2" -> getSection6Practice2Questions()
            "java_u6_q1" -> getSection6QuizQuestions()
            "java_u7_p1" -> getSection7Practice1Questions()
            "java_u7_p2" -> getSection7Practice2Questions()
            "java_u7_q1" -> getSection7QuizQuestions()
            "java_u8_p1" -> getSection8Practice1Questions()
            "java_u8_p2" -> getSection8Practice2Questions()
            "java_u8_q1" -> getSection8QuizQuestions()
            "java_u9_p1" -> getSection9Practice1Questions()
            "java_u9_p2" -> getSection9Practice2Questions()
            "java_u9_q1" -> getSection9QuizQuestions()
            "java_u10_p1" -> getSection10Practice1Questions()
            "java_u10_p2" -> getSection10Practice2Questions()
            "java_u10_q1" -> getSection10QuizQuestions()
            else -> getPractice1Questions()
        }
    }

    fun getPractice1Questions(): List<PracticeQuestion> {
        return listOf(
            PracticeQuestion(
                id = "java_p1_q1",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 1",
                question = "Which command prints text to the console in Java?",
                options = listOf(
                    "System.out.println()",
                    "print.console()",
                    "Console.write()",
                    "echo()"
                ),
                correctAnswer = "System.out.println()",
                explanation = "In Java, System.out.println() is used to print text to the console."
            ),
            PracticeQuestion(
                id = "java_p1_q2",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 2",
                question = "Drag the correct command into the blank inside the code.",
                code = """
                    public class Main {
                        public static void main(String[] args) {
                            ______("Hello World");
                        }
                    }
                """.trimIndent(),
                options = listOf(
                    "System.out.println",
                    "Console.write",
                    "print.console",
                    "echo"
                ),
                correctAnswer = "System.out.println",
                explanation = "The full command is System.out.println(\"Hello World\");"
            ),
            PracticeQuestion(
                id = "java_p1_q3",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 3",
                question = "What does println mean?",
                options = listOf(
                    "Print line",
                    "Print language",
                    "Private line",
                    "Program link"
                ),
                correctAnswer = "Print line",
                explanation = "println means print line. It prints text and moves to the next line."
            ),
            PracticeQuestion(
                id = "java_p1_q4",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 4",
                question = "Which line is the correct entry point for a Java program?",
                code = """
                    public class Main {
                        // program starts here
                    }
                """.trimIndent(),
                options = listOf(
                    "public static void main(String[] args) { }",
                    "public start main(String[] args) { }",
                    "main public static void(String[] args) { }",
                    "System.out.println(String[] args) { }"
                ),
                correctAnswer = "public static void main(String[] args) { }",
                explanation = "Java starts from public static void main(String[] args)."
            ),
            PracticeQuestion(
                id = "java_p1_q5",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 5",
                question = "Where does Java start running the program?",
                options = listOf(
                    "main()",
                    "start()",
                    "run()",
                    "print()"
                ),
                correctAnswer = "main()",
                explanation = "Java starts running the program from the main() method."
            )
        )
    }

    private fun getPractice2Questions(): List<PracticeQuestion> {
        return listOf(
            PracticeQuestion(
                id = "java_p2_q1",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 1",
                question = "Which type should you use for a whole number?",
                options = listOf("int", "String", "text", "word"),
                correctAnswer = "int",
                explanation = "int stores whole numbers such as 7, 14, or 100."
            ),
            PracticeQuestion(
                id = "java_p2_q2",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 2",
                question = "Drag each type into the matching blank inside the code.",
                code = """
                    ______ age = 14;
                    ______ name = "Leo";
                    ______ price = 19.99;
                    ______ isReady = true;
                """.trimIndent(),
                options = listOf("String", "int", "boolean", "double", "char"),
                correctAnswer = "int",
                correctAnswers = listOf("int", "String", "double", "boolean"),
                explanation = "age is int, name is String, price is double, and isReady is boolean."
            ),
            PracticeQuestion(
                id = "java_p2_q3",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 3",
                question = "Which type stores decimal numbers?",
                options = listOf("double", "int", "boolean", "char"),
                correctAnswer = "double",
                explanation = "double stores decimal values such as 3.14 or 19.99."
            ),
            PracticeQuestion(
                id = "java_p2_q4",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 4",
                question = "Which value is a boolean?",
                options = listOf("true", "\"true\"", "'t'", "10"),
                correctAnswer = "true",
                explanation = "boolean values are true or false without quotes."
            ),
            PracticeQuestion(
                id = "java_p2_q5",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 5",
                question = "Which line correctly creates a char?",
                options = listOf(
                    "char grade = 'A';",
                    "char grade = \"A\";",
                    "String grade = 'A';",
                    "boolean grade = A;"
                ),
                correctAnswer = "char grade = 'A';",
                explanation = "A char stores one character and uses single quotes."
            ),
            PracticeQuestion(
                id = "java_p2_q6",
                type = PracticeQuestionType.FILL_BLANK,
                title = "Question 6",
                question = "Complete the variable name that gets printed.",
                code = """
                    String player = "Maya";
                    System.out.println(______);
                """.trimIndent(),
                correctAnswer = "player",
                explanation = "Without quotes, println(player) prints the value stored in the variable."
            ),
            PracticeQuestion(
                id = "java_p2_q7",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 7",
                question = "Drag the correct values into the blanks.",
                code = """
                    char grade = ______;
                    boolean passed = ______;
                    String message = ______;
                """.trimIndent(),
                options = listOf("'A'", "true", "\"Great job\"", "99", "A"),
                correctAnswer = "'A'",
                correctAnswers = listOf("'A'", "true", "\"Great job\""),
                explanation = "char uses single quotes, boolean uses true or false, and String uses double quotes."
            ),
            PracticeQuestion(
                id = "java_p2_q8",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 8",
                question = "What will this code print?",
                code = """
                    int score = 0;
                    score = 10;
                    System.out.println(score);
                """.trimIndent(),
                options = listOf("10", "0", "score", "Nothing"),
                correctAnswer = "10",
                explanation = "score is updated to 10 before it is printed."
            )
        )
    }

    private fun getUnit1QuizQuestions(): List<PracticeQuestion> {
        return listOf(
            PracticeQuestion(
                id = "java_q1_q1",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 1",
                question = "What is the entry point of a Java program?",
                options = listOf("main()", "println()", "start()", "class()"),
                correctAnswer = "main()",
                explanation = "Java begins running from the main() method."
            ),
            PracticeQuestion(
                id = "java_q1_q2",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Quiz 2",
                question = "Complete the first Java program.",
                code = """
                    public class Main {
                        public static void main(String[] args) {
                            ______("Hello Java");
                        }
                    }
                """.trimIndent(),
                options = listOf("System.out.println", "print.console", "main", "String"),
                correctAnswer = "System.out.println",
                explanation = "System.out.println prints text to the console."
            ),
            PracticeQuestion(
                id = "java_q1_q3",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 3",
                question = "Which type stores text?",
                options = listOf("String", "int", "double", "boolean"),
                correctAnswer = "String",
                explanation = "String stores text values inside double quotes."
            ),
            PracticeQuestion(
                id = "java_q1_q4",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 4",
                question = "Which type stores true or false?",
                options = listOf("boolean", "char", "int", "String"),
                correctAnswer = "boolean",
                explanation = "boolean stores exactly true or false."
            ),
            PracticeQuestion(
                id = "java_q1_q5",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Quiz 5",
                question = "Match the variable types to the values.",
                code = """
                    ______ count = 3;
                    ______ letter = 'G';
                    ______ average = 4.5;
                """.trimIndent(),
                options = listOf("char", "double", "int", "String", "boolean"),
                correctAnswer = "int",
                correctAnswers = listOf("int", "char", "double"),
                explanation = "count is int, letter is char, and average is double."
            ),
            PracticeQuestion(
                id = "java_q1_q6",
                type = PracticeQuestionType.FILL_BLANK,
                title = "Quiz 6",
                question = "What variable name should be printed?",
                code = """
                    String course = "Java";
                    System.out.println(______);
                """.trimIndent(),
                correctAnswer = "course",
                explanation = "println(course) prints the value stored in course."
            ),
            PracticeQuestion(
                id = "java_q1_q7",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 7",
                question = "What will this code print?",
                code = """
                    String name = "Nora";
                    System.out.println("name");
                """.trimIndent(),
                options = listOf("name", "Nora", "\"Nora\"", "Nothing"),
                correctAnswer = "name",
                explanation = "Text inside quotes is printed exactly as written."
            ),
            PracticeQuestion(
                id = "java_q1_q8",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 8",
                question = "Which line updates an existing variable correctly?",
                code = """
                    int level = 1;
                """.trimIndent(),
                options = listOf("level = 2;", "int level = 2; int level = 3;", "2 = level;", "level int = 2;"),
                correctAnswer = "level = 2;",
                explanation = "After a variable exists, update it by writing its name, equals sign, and new value."
            )
        )
    }

    private fun getSection2Practice1Questions(): List<PracticeQuestion> {
        return listOf(
            PracticeQuestion(
                id = "java_s2_p1_q1",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 1",
                question = "What does this condition mean?",
                code = """
                    score >= 75
                """.trimIndent(),
                options = listOf(
                    "score is at least 75",
                    "score is exactly 75",
                    "score is less than 75",
                    "score becomes 75"
                ),
                correctAnswer = "score is at least 75",
                explanation = ">= means greater than or equal to."
            ),
            PracticeQuestion(
                id = "java_s2_p1_q2",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 2",
                question = "Drag the correct comparison into the condition.",
                code = """
                    int age = 16;

                    if (age ______ 13) {
                        System.out.println("You can join");
                    }
                """.trimIndent(),
                options = listOf(">=", "<=", "==", "!="),
                correctAnswer = ">=",
                explanation = "age >= 13 checks if age is 13 or older."
            ),
            PracticeQuestion(
                id = "java_s2_p1_q3",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 3",
                question = "Which operator checks if two values are equal?",
                options = listOf("==", "=", "!=", ">="),
                correctAnswer = "==",
                explanation = "== compares values. A single = assigns a value."
            ),
            PracticeQuestion(
                id = "java_s2_p1_q4",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 4",
                question = "What will this code print?",
                code = """
                    int coins = 50;

                    if (coins >= 100) {
                        System.out.println("Buy item");
                    }
                """.trimIndent(),
                options = listOf("Nothing", "Buy item", "coins", "100"),
                correctAnswer = "Nothing",
                explanation = "The condition is false because 50 is less than 100, so the if body does not run."
            ),
            PracticeQuestion(
                id = "java_s2_p1_q5",
                type = PracticeQuestionType.FILL_BLANK,
                title = "Question 5",
                question = "Complete the boolean variable used by the if statement.",
                code = """
                    boolean isReady = true;

                    if (______) {
                        System.out.println("Start");
                    }
                """.trimIndent(),
                correctAnswer = "isReady",
                explanation = "A boolean variable can be used directly as the condition."
            ),
            PracticeQuestion(
                id = "java_s2_p1_q6",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 6",
                question = "Complete the if statement.",
                code = """
                    int score = 90;

                    ______ (score >= 75) {
                        System.out.println("Passed");
                    }
                """.trimIndent(),
                options = listOf("if", "else", "boolean", "main"),
                correctAnswer = "if",
                explanation = "if starts a conditional block."
            )
        )
    }

    private fun getSection2Practice2Questions(): List<PracticeQuestion> {
        return listOf(
            PracticeQuestion(
                id = "java_s2_p2_q1",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 1",
                question = "When does else run?",
                options = listOf(
                    "When the if condition is false",
                    "Before the if condition",
                    "Every time",
                    "Only when the program ends"
                ),
                correctAnswer = "When the if condition is false",
                explanation = "else handles the path where the if condition is false."
            ),
            PracticeQuestion(
                id = "java_s2_p2_q2",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 2",
                question = "Drag the missing keywords into the code.",
                code = """
                    int score = 60;

                    ______ (score >= 75) {
                        System.out.println("Passed");
                    } ______ {
                        System.out.println("Try again");
                    }
                """.trimIndent(),
                options = listOf("if", "else", "boolean", "main"),
                correctAnswer = "if",
                correctAnswers = listOf("if", "else"),
                explanation = "The first keyword is if, and the second branch is else."
            ),
            PracticeQuestion(
                id = "java_s2_p2_q3",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 3",
                question = "What does && mean?",
                options = listOf("AND", "OR", "NOT", "EQUAL"),
                correctAnswer = "AND",
                explanation = "&& means both conditions must be true."
            ),
            PracticeQuestion(
                id = "java_s2_p2_q4",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 4",
                question = "What does || mean?",
                options = listOf("OR", "AND", "NOT", "LESS THAN"),
                correctAnswer = "OR",
                explanation = "|| means at least one condition must be true."
            ),
            PracticeQuestion(
                id = "java_s2_p2_q5",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 5",
                question = "What will this code print?",
                code = """
                    boolean gameOver = false;

                    if (!gameOver) {
                        System.out.println("Keep playing");
                    }
                """.trimIndent(),
                options = listOf("Keep playing", "gameOver", "false", "Nothing"),
                correctAnswer = "Keep playing",
                explanation = "!gameOver is true when gameOver is false."
            ),
            PracticeQuestion(
                id = "java_s2_p2_q6",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 6",
                question = "Complete the access check.",
                code = """
                    boolean isAdmin = false;
                    boolean isTeacher = true;

                    if (isAdmin ______ isTeacher) {
                        System.out.println("Access granted");
                    }
                """.trimIndent(),
                options = listOf("||", "&&", "==", "!="),
                correctAnswer = "||",
                explanation = "Use || because either admin or teacher should be enough."
            ),
            PracticeQuestion(
                id = "java_s2_p2_q7",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 7",
                question = "Which branch runs first when its condition is true?",
                code = """
                    if (score >= 90) {
                        System.out.println("Excellent");
                    } else if (score >= 75) {
                        System.out.println("Passed");
                    }
                """.trimIndent(),
                options = listOf("The first true branch", "Every branch", "Only else", "No branch"),
                correctAnswer = "The first true branch",
                explanation = "Java checks from top to bottom and stops at the first true condition."
            )
        )
    }

    private fun getSection2QuizQuestions(): List<PracticeQuestion> {
        return listOf(
            PracticeQuestion(
                id = "java_s2_q1",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 1",
                question = "Which operator means not equal?",
                options = listOf("!=", "==", "=", ">="),
                correctAnswer = "!=",
                explanation = "!= checks that two values are not equal."
            ),
            PracticeQuestion(
                id = "java_s2_q2",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Quiz 2",
                question = "Complete the pass/fail decision.",
                code = """
                    int score = 72;

                    if (score ______ 75) {
                        System.out.println("Passed");
                    } ______ {
                        System.out.println("Try again");
                    }
                """.trimIndent(),
                options = listOf(">=", "else", "if", "&&", "=="),
                correctAnswer = ">=",
                correctAnswers = listOf(">=", "else"),
                explanation = "score >= 75 checks the passing score. else handles the failing path."
            ),
            PracticeQuestion(
                id = "java_s2_q3",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 3",
                question = "What does this condition require?",
                code = """
                    age >= 13 && hasTicket
                """.trimIndent(),
                options = listOf(
                    "Age is at least 13 and hasTicket is true",
                    "Only one side must be true",
                    "Age becomes 13",
                    "hasTicket becomes false"
                ),
                correctAnswer = "Age is at least 13 and hasTicket is true",
                explanation = "&& requires both sides to be true."
            ),
            PracticeQuestion(
                id = "java_s2_q4",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 4",
                question = "What does || require?",
                options = listOf(
                    "At least one condition is true",
                    "Both conditions are true",
                    "Both conditions are false",
                    "The variable is a String"
                ),
                correctAnswer = "At least one condition is true",
                explanation = "|| is OR, so one true side is enough."
            ),
            PracticeQuestion(
                id = "java_s2_q5",
                type = PracticeQuestionType.FILL_BLANK,
                title = "Quiz 5",
                question = "Complete the condition that checks if the game is not over.",
                code = """
                    boolean gameOver = false;

                    if (______) {
                        System.out.println("Keep playing");
                    }
                """.trimIndent(),
                correctAnswer = "!gameOver",
                explanation = "! flips the boolean value, so !gameOver is true when gameOver is false."
            ),
            PracticeQuestion(
                id = "java_s2_q6",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 6",
                question = "What will this code print?",
                code = """
                    int coins = 120;

                    if (coins >= 100) {
                        System.out.println("Buy sword");
                    } else {
                        System.out.println("Collect more");
                    }
                """.trimIndent(),
                options = listOf("Buy sword", "Collect more", "coins", "Nothing"),
                correctAnswer = "Buy sword",
                explanation = "120 is greater than 100, so the if branch runs."
            ),
            PracticeQuestion(
                id = "java_s2_q7",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Quiz 7",
                question = "Complete the multi-choice decision.",
                code = """
                    if (score >= 90) {
                        System.out.println("Excellent");
                    } ______ if (score >= 75) {
                        System.out.println("Passed");
                    } ______ {
                        System.out.println("Try again");
                    }
                """.trimIndent(),
                options = listOf("else", "if", "&&", "||"),
                correctAnswer = "else",
                correctAnswers = listOf("else", "else"),
                explanation = "Use else if for the second condition, and else for the final fallback."
            )
        )
    }

    private fun getSection3Practice1Questions(): List<PracticeQuestion> {
        return listOf(
            PracticeQuestion(
                id = "java_s3_p1_q1",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 1",
                question = "What is a loop used for?",
                options = listOf(
                    "Repeating code while a condition allows it",
                    "Creating a new class only",
                    "Printing text once",
                    "Changing Java into another language"
                ),
                correctAnswer = "Repeating code while a condition allows it",
                explanation = "A loop repeats a block of code."
            ),
            PracticeQuestion(
                id = "java_s3_p1_q2",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 2",
                question = "Drag the missing parts into the while loop.",
                code = """
                    int count = 1;

                    ______ (count <= 5) {
                        System.out.println(count);
                        ______;
                    }
                """.trimIndent(),
                options = listOf("while", "count++", "if", "count--", "else"),
                correctAnswer = "while",
                correctAnswers = listOf("while", "count++"),
                explanation = "while starts the loop, and count++ moves the counter forward."
            ),
            PracticeQuestion(
                id = "java_s3_p1_q3",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 3",
                question = "What does count++ do?",
                options = listOf(
                    "Adds 1 to count",
                    "Subtracts 1 from count",
                    "Prints count",
                    "Creates a String"
                ),
                correctAnswer = "Adds 1 to count",
                explanation = "count++ is a shortcut for increasing count by 1."
            ),
            PracticeQuestion(
                id = "java_s3_p1_q4",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 4",
                question = "What will this code print first?",
                code = """
                    int count = 1;

                    while (count <= 3) {
                        System.out.println(count);
                        count++;
                    }
                """.trimIndent(),
                options = listOf("1", "2", "3", "Nothing"),
                correctAnswer = "1",
                explanation = "The counter starts at 1, so the first printed value is 1."
            ),
            PracticeQuestion(
                id = "java_s3_p1_q5",
                type = PracticeQuestionType.FILL_BLANK,
                title = "Question 5",
                question = "Complete the condition so the loop prints 1, 2, and 3.",
                code = """
                    int count = 1;

                    while (______) {
                        System.out.println(count);
                        count++;
                    }
                """.trimIndent(),
                correctAnswer = "count <= 3",
                explanation = "count <= 3 keeps the loop running for 1, 2, and 3."
            ),
            PracticeQuestion(
                id = "java_s3_p1_q6",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 6",
                question = "Why can this loop run forever?",
                code = """
                    int count = 1;

                    while (count <= 5) {
                        System.out.println(count);
                    }
                """.trimIndent(),
                options = listOf(
                    "count never changes",
                    "println is not allowed in loops",
                    "count starts too high",
                    "The class is missing"
                ),
                correctAnswer = "count never changes",
                explanation = "Without count++, the condition can stay true forever."
            )
        )
    }

    private fun getSection3Practice2Questions(): List<PracticeQuestion> {
        return listOf(
            PracticeQuestion(
                id = "java_s3_p2_q1",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 1",
                question = "Which loop is usually best when you know how many times to repeat?",
                options = listOf("for", "if", "else", "String"),
                correctAnswer = "for",
                explanation = "for loops are great for clear counting loops."
            ),
            PracticeQuestion(
                id = "java_s3_p2_q2",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 2",
                question = "Drag the missing parts into the for loop.",
                code = """
                    ______ (int i = 1; i <= 5; ______) {
                        System.out.println(i);
                    }
                """.trimIndent(),
                options = listOf("for", "i++", "while", "i--", "if"),
                correctAnswer = "for",
                correctAnswers = listOf("for", "i++"),
                explanation = "for starts the loop, and i++ increases i after each repeat."
            ),
            PracticeQuestion(
                id = "java_s3_p2_q3",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 3",
                question = "How many times does this loop run?",
                code = """
                    for (int i = 1; i <= 4; i++) {
                        System.out.println("Go");
                    }
                """.trimIndent(),
                options = listOf("4", "1", "5", "Forever"),
                correctAnswer = "4",
                explanation = "The loop runs for i values 1, 2, 3, and 4."
            ),
            PracticeQuestion(
                id = "java_s3_p2_q4",
                type = PracticeQuestionType.FILL_BLANK,
                title = "Question 4",
                question = "Complete the update that counts down.",
                code = """
                    for (int i = 5; i >= 1; ______) {
                        System.out.println(i);
                    }
                """.trimIndent(),
                correctAnswer = "i--",
                explanation = "i-- subtracts 1 after each repeat."
            ),
            PracticeQuestion(
                id = "java_s3_p2_q5",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 5",
                question = "Complete the loop and the decision inside it.",
                code = """
                    for (int i = 1; i <= 5; i++) {
                        ______ (i == 3) {
                            System.out.println("Middle");
                        }
                    }
                """.trimIndent(),
                options = listOf("if", "else", "while", "for"),
                correctAnswer = "if",
                explanation = "Use if to check a condition inside each loop repeat."
            ),
            PracticeQuestion(
                id = "java_s3_p2_q6",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 6",
                question = "What does i-- do?",
                options = listOf(
                    "Subtracts 1 from i",
                    "Adds 1 to i",
                    "Prints i twice",
                    "Stops the program"
                ),
                correctAnswer = "Subtracts 1 from i",
                explanation = "i-- decreases the counter by 1."
            )
        )
    }

    private fun getSection3QuizQuestions(): List<PracticeQuestion> {
        return listOf(
            PracticeQuestion(
                id = "java_s3_q1",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 1",
                question = "What does while check before each repeat?",
                options = listOf("A condition", "A class name", "A file name", "A String type"),
                correctAnswer = "A condition",
                explanation = "while keeps running while its condition is true."
            ),
            PracticeQuestion(
                id = "java_s3_q2",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Quiz 2",
                question = "Complete the while loop.",
                code = """
                    int count = 1;

                    while (count ______ 3) {
                        System.out.println(count);
                        ______;
                    }
                """.trimIndent(),
                options = listOf("<=", "count++", ">=", "count--", "if"),
                correctAnswer = "<=",
                correctAnswers = listOf("<=", "count++"),
                explanation = "count <= 3 prints 1 through 3, and count++ moves forward."
            ),
            PracticeQuestion(
                id = "java_s3_q3",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 3",
                question = "Which for loop prints 1 to 5?",
                options = listOf(
                    "for (int i = 1; i <= 5; i++)",
                    "for (int i = 5; i <= 1; i++)",
                    "for (int i = 1; i >= 5; i++)",
                    "for (String i = 1; i <= 5; i++)"
                ),
                correctAnswer = "for (int i = 1; i <= 5; i++)",
                explanation = "Start at 1, keep going while i <= 5, and increase i."
            ),
            PracticeQuestion(
                id = "java_s3_q4",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 4",
                question = "How many times does this loop print?",
                code = """
                    for (int i = 0; i < 3; i++) {
                        System.out.println("Java");
                    }
                """.trimIndent(),
                options = listOf("3", "2", "4", "Forever"),
                correctAnswer = "3",
                explanation = "i is 0, 1, and 2. That is three repeats."
            ),
            PracticeQuestion(
                id = "java_s3_q5",
                type = PracticeQuestionType.FILL_BLANK,
                title = "Quiz 5",
                question = "Complete the condition that finds the third repeat.",
                code = """
                    for (int i = 1; i <= 5; i++) {
                        if (______) {
                            System.out.println("Third");
                        }
                    }
                """.trimIndent(),
                correctAnswer = "i == 3",
                explanation = "Use == to compare i with 3."
            ),
            PracticeQuestion(
                id = "java_s3_q6",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Quiz 6",
                question = "Complete the countdown loop.",
                code = """
                    for (int i = 5; i ______ 1; ______) {
                        System.out.println(i);
                    }
                """.trimIndent(),
                options = listOf(">=", "i--", "<=", "i++", "while"),
                correctAnswer = ">=",
                correctAnswers = listOf(">=", "i--"),
                explanation = "A countdown keeps running while i >= 1 and uses i--."
            ),
            PracticeQuestion(
                id = "java_s3_q7",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 7",
                question = "Which problem can cause an infinite loop?",
                options = listOf(
                    "The counter is never updated",
                    "The loop prints text",
                    "The condition becomes false",
                    "The loop uses braces"
                ),
                correctAnswer = "The counter is never updated",
                explanation = "If the value in the condition never changes, the condition may never become false."
            ),
            PracticeQuestion(
                id = "java_s3_q8",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Quiz 8",
                question = "When is a for loop usually clearer than a while loop?",
                options = listOf(
                    "When you know the number of repeats",
                    "When you need a class",
                    "When you create text only",
                    "When no condition is needed"
                ),
                correctAnswer = "When you know the number of repeats",
                explanation = "for loops keep the start, condition, and update together."
            )
        )
    }

    private fun getSection4Practice1Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(
            id = "java_s4_p1_q1",
            type = PracticeQuestionType.MULTIPLE_CHOICE,
            title = "Question 1",
            question = "What does an array store?",
            options = listOf("Many values of the same type", "Only one boolean", "Only class names", "Only errors"),
            correctAnswer = "Many values of the same type",
            explanation = "An array groups multiple values under one variable name."
        ),
        PracticeQuestion(
            id = "java_s4_p1_q2",
            type = PracticeQuestionType.DRAG_FILL_BLANK,
            title = "Question 2",
            question = "Complete the int array.",
            code = """
                ______ scores = {90, 75, 88};
                System.out.println(scores[0]);
            """.trimIndent(),
            options = listOf("int[]", "String", "int", "boolean[]"),
            correctAnswer = "int[]",
            explanation = "int[] creates an array of int values."
        ),
        PracticeQuestion(
            id = "java_s4_p1_q3",
            type = PracticeQuestionType.MULTIPLE_CHOICE,
            title = "Question 3",
            question = "What does names[0] read?",
            code = """String[] names = {"Leo", "Maya"};""",
            options = listOf("Leo", "Maya", "0", "names"),
            correctAnswer = "Leo",
            explanation = "Array indexes start at 0."
        ),
        PracticeQuestion(
            id = "java_s4_p1_q4",
            type = PracticeQuestionType.FILL_BLANK,
            title = "Question 4",
            question = "Complete the property that returns the array size.",
            code = """System.out.println(scores.______);""",
            correctAnswer = "length",
            explanation = "Arrays use .length to report how many values they hold."
        )
    )

    private fun getSection4Practice2Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(
            id = "java_s4_p2_q1",
            type = PracticeQuestionType.MULTIPLE_CHOICE,
            title = "Question 1",
            question = "If an array has 4 values, what is the last index?",
            options = listOf("3", "4", "1", "length"),
            correctAnswer = "3",
            explanation = "The last index is length - 1."
        ),
        PracticeQuestion(
            id = "java_s4_p2_q2",
            type = PracticeQuestionType.DRAG_FILL_BLANK,
            title = "Question 2",
            question = "Complete the loop that prints every score.",
            code = """
                for (int i = 0; i < scores.______; i++) {
                    System.out.println(scores[______]);
                }
            """.trimIndent(),
            options = listOf("length", "i", "0", "score", "size"),
            correctAnswer = "length",
            correctAnswers = listOf("length", "i"),
            explanation = "Use scores.length for the limit and scores[i] to read the current value."
        ),
        PracticeQuestion(
            id = "java_s4_p2_q3",
            type = PracticeQuestionType.MULTIPLE_CHOICE,
            title = "Question 3",
            question = "What usually causes an array index error?",
            options = listOf("Reading an index that does not exist", "Using println", "Creating a String", "Using a semicolon"),
            correctAnswer = "Reading an index that does not exist",
            explanation = "Indexes must stay between 0 and length - 1."
        )
    )

    private fun getSection4QuizQuestions(): List<PracticeQuestion> = getSection4Practice1Questions() + listOf(
        PracticeQuestion(
            id = "java_s4_q5",
            type = PracticeQuestionType.DRAG_FILL_BLANK,
            title = "Quiz 5",
            question = "Complete the enhanced for loop.",
            code = """
                for (int score ______ scores) {
                    System.out.println(score);
                }
            """.trimIndent(),
            options = listOf(":", "in", "=", "=="),
            correctAnswer = ":",
            explanation = "Enhanced for loops use a colon: for (int score : scores)."
        )
    )

    private fun getSection5Practice1Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(
            id = "java_s5_p1_q1",
            type = PracticeQuestionType.MULTIPLE_CHOICE,
            title = "Question 1",
            question = "What is a method?",
            options = listOf("A named block of reusable code", "Only a variable", "Only an array", "A Java error"),
            correctAnswer = "A named block of reusable code",
            explanation = "Methods organize code into reusable blocks."
        ),
        PracticeQuestion(
            id = "java_s5_p1_q2",
            type = PracticeQuestionType.DRAG_FILL_BLANK,
            title = "Question 2",
            question = "Complete the method call.",
            code = """
                static void sayHello() {
                    System.out.println("Hello");
                }

                public static void main(String[] args) {
                    ______;
                }
            """.trimIndent(),
            options = listOf("sayHello()", "sayHello", "void", "method()"),
            correctAnswer = "sayHello()",
            explanation = "Call a method with its name and parentheses."
        ),
        PracticeQuestion(
            id = "java_s5_p1_q3",
            type = PracticeQuestionType.MULTIPLE_CHOICE,
            title = "Question 3",
            question = "What does void mean?",
            options = listOf("The method does not return a value", "The method returns int", "The method is an array", "The method is broken"),
            correctAnswer = "The method does not return a value",
            explanation = "void methods run code but do not send back a result."
        )
    )

    private fun getSection5Practice2Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(
            id = "java_s5_p2_q1",
            type = PracticeQuestionType.MULTIPLE_CHOICE,
            title = "Question 1",
            question = "What does a parameter do?",
            options = listOf("Lets a method receive information", "Stops a loop", "Creates an error", "Deletes an array"),
            correctAnswer = "Lets a method receive information",
            explanation = "Parameters make methods flexible."
        ),
        PracticeQuestion(
            id = "java_s5_p2_q2",
            type = PracticeQuestionType.DRAG_FILL_BLANK,
            title = "Question 2",
            question = "Complete the method that returns double the number.",
            code = """
                static ______ doubleNumber(int number) {
                    ______ number * 2;
                }
            """.trimIndent(),
            options = listOf("int", "return", "void", "String"),
            correctAnswer = "int",
            correctAnswers = listOf("int", "return"),
            explanation = "The method returns an int, so it uses int and return."
        ),
        PracticeQuestion(
            id = "java_s5_p2_q3",
            type = PracticeQuestionType.FILL_BLANK,
            title = "Question 3",
            question = "Complete the parameter type.",
            code = """static void greet(______ name) { }""",
            correctAnswer = "String",
            explanation = "A name is text, so the parameter type is String."
        )
    )

    private fun getSection5QuizQuestions(): List<PracticeQuestion> = getSection5Practice1Questions() + getSection5Practice2Questions()

    private fun getSection6Practice1Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(
            id = "java_s6_p1_q1",
            type = PracticeQuestionType.MULTIPLE_CHOICE,
            title = "Question 1",
            question = "What does Scanner help with?",
            options = listOf("Reading user input", "Creating arrays only", "Drawing graphics", "Changing colors"),
            correctAnswer = "Reading user input",
            explanation = "Scanner reads values typed into the console."
        ),
        PracticeQuestion(
            id = "java_s6_p1_q2",
            type = PracticeQuestionType.DRAG_FILL_BLANK,
            title = "Question 2",
            question = "Complete the Scanner setup.",
            code = """
                import java.util.______;

                Scanner input = new Scanner(System.in);
            """.trimIndent(),
            options = listOf("Scanner", "String", "Input", "System"),
            correctAnswer = "Scanner",
            explanation = "Scanner is imported from java.util.Scanner."
        ),
        PracticeQuestion(
            id = "java_s6_p1_q3",
            type = PracticeQuestionType.MULTIPLE_CHOICE,
            title = "Question 3",
            question = "Which Scanner method reads a whole number?",
            options = listOf("nextInt()", "nextLine()", "println()", "length()"),
            correctAnswer = "nextInt()",
            explanation = "nextInt() reads an int."
        )
    )

    private fun getSection6Practice2Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(
            id = "java_s6_p2_q1",
            type = PracticeQuestionType.DRAG_FILL_BLANK,
            title = "Question 1",
            question = "Complete the age check from input.",
            code = """
                int age = input.______();

                if (age ______ 13) {
                    System.out.println("Welcome");
                }
            """.trimIndent(),
            options = listOf("nextInt", ">=", "nextLine", "<=", "equals"),
            correctAnswer = "nextInt",
            correctAnswers = listOf("nextInt", ">="),
            explanation = "Read an int with nextInt(), then compare age >= 13."
        ),
        PracticeQuestion(
            id = "java_s6_p2_q2",
            type = PracticeQuestionType.MULTIPLE_CHOICE,
            title = "Question 2",
            question = "Why print a prompt before reading input?",
            options = listOf("So the user knows what to type", "So Java creates a class", "So arrays get longer", "So loops stop"),
            correctAnswer = "So the user knows what to type",
            explanation = "Prompts make console programs easier to use."
        )
    )

    private fun getSection6QuizQuestions(): List<PracticeQuestion> = getSection6Practice1Questions() + getSection6Practice2Questions()

    private fun getSection7Practice1Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(id = "java_s7_p1_q1", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 1", question = "What does length() return?", options = listOf("The number of characters", "The first character", "A loop", "An array"), correctAnswer = "The number of characters", explanation = "length() counts characters in a String."),
        PracticeQuestion(id = "java_s7_p1_q2", type = PracticeQuestionType.DRAG_FILL_BLANK, title = "Question 2", question = "Complete the uppercase call.", code = """String word = "java";
System.out.println(word.______());""", options = listOf("toUpperCase", "length", "contains", "equals"), correctAnswer = "toUpperCase", explanation = "toUpperCase() returns uppercase text."),
        PracticeQuestion(id = "java_s7_p1_q3", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 3", question = "What does contains() return?", options = listOf("true or false", "Only an int", "Only a char", "A class"), correctAnswer = "true or false", explanation = "contains() checks if text includes another text.")
    )

    private fun getSection7Practice2Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(id = "java_s7_p2_q1", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 1", question = "Which method should beginners use to compare String text?", options = listOf("equals()", "==", "length()", "nextInt()"), correctAnswer = "equals()", explanation = "equals() compares String text."),
        PracticeQuestion(id = "java_s7_p2_q2", type = PracticeQuestionType.DRAG_FILL_BLANK, title = "Question 2", question = "Complete the check.", code = """if (answer.______("yes")) {
    System.out.println("Confirmed");
}""", options = listOf("equals", "length", "trim", "for"), correctAnswer = "equals", explanation = "answer.equals(\"yes\") checks the text."),
        PracticeQuestion(id = "java_s7_p2_q3", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 3", question = "What does trim() remove?", options = listOf("Spaces at the start and end", "All letters", "All numbers", "The whole String"), correctAnswer = "Spaces at the start and end", explanation = "trim() cleans extra outer spaces.")
    )

    private fun getSection7QuizQuestions(): List<PracticeQuestion> = getSection7Practice1Questions() + getSection7Practice2Questions()

    private fun getSection8Practice1Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(id = "java_s8_p1_q1", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 1", question = "What is a class?", options = listOf("A blueprint for objects", "Only a loop", "Only a number", "A console input"), correctAnswer = "A blueprint for objects", explanation = "Classes describe the data and behavior objects can have."),
        PracticeQuestion(id = "java_s8_p1_q2", type = PracticeQuestionType.DRAG_FILL_BLANK, title = "Question 2", question = "Complete the field declaration.", code = """class Student {
    ______ name;
    int age;
}""", options = listOf("String", "Scanner", "void", "for"), correctAnswer = "String", explanation = "name is text, so it should be a String."),
        PracticeQuestion(id = "java_s8_p1_q3", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 3", question = "What are fields?", options = listOf("Variables that belong to a class/object", "Only imported files", "Only errors", "Only println calls"), correctAnswer = "Variables that belong to a class/object", explanation = "Fields store object data.")
    )

    private fun getSection8Practice2Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(id = "java_s8_p2_q1", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 1", question = "Which keyword creates a new object?", options = listOf("new", "class", "if", "return"), correctAnswer = "new", explanation = "new creates an object from a class."),
        PracticeQuestion(id = "java_s8_p2_q2", type = PracticeQuestionType.DRAG_FILL_BLANK, title = "Question 2", question = "Complete the object code.", code = """Student student = ______ Student();
student.______ = "Maya";""", options = listOf("new", "name", "class", "String"), correctAnswer = "new", correctAnswers = listOf("new", "name"), explanation = "Use new Student() to create the object, then student.name to set the field."),
        PracticeQuestion(id = "java_s8_p2_q3", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 3", question = "What does the dot in student.name mean?", options = listOf("Access something inside the object", "Start a loop", "Create an array", "Import Scanner"), correctAnswer = "Access something inside the object", explanation = "Dot syntax accesses fields and methods on an object.")
    )

    private fun getSection8QuizQuestions(): List<PracticeQuestion> = getSection8Practice1Questions() + getSection8Practice2Questions()

    private fun getSection9Practice1Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(id = "java_s9_p1_q1", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 1", question = "When does a compile-time error happen?", options = listOf("Before the program runs", "Only after input", "Only inside arrays", "Never"), correctAnswer = "Before the program runs", explanation = "Compile-time errors stop Java from building the program."),
        PracticeQuestion(id = "java_s9_p1_q2", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 2", question = "What is missing here?", code = """System.out.println("Hello")""", options = listOf("Semicolon", "Array", "Scanner", "Class field"), correctAnswer = "Semicolon", explanation = "Java statements usually end with ;."),
        PracticeQuestion(id = "java_s9_p1_q3", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 3", question = "What should you check first in an error message?", options = listOf("The line number and message", "Only the app color", "Only the imports", "Nothing"), correctAnswer = "The line number and message", explanation = "The line number and message are the best starting point.")
    )

    private fun getSection9Practice2Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(id = "java_s9_p2_q1", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 1", question = "What does try / catch help with?", options = listOf("Handling risky runtime code", "Creating arrays only", "Styling buttons", "Importing every class"), correctAnswer = "Handling risky runtime code", explanation = "try / catch lets code respond to runtime failures."),
        PracticeQuestion(id = "java_s9_p2_q2", type = PracticeQuestionType.DRAG_FILL_BLANK, title = "Question 2", question = "Complete the try / catch structure.", code = """______ {
    int number = Integer.parseInt(text);
} ______ (Exception e) {
    System.out.println("Invalid");
}""", options = listOf("try", "catch", "if", "for"), correctAnswer = "try", correctAnswers = listOf("try", "catch"), explanation = "Risky code goes in try, and the fallback goes in catch."),
        PracticeQuestion(id = "java_s9_p2_q3", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 3", question = "Why use debug prints?", options = listOf("To inspect values while the program runs", "To remove Java", "To skip compiling", "To create an object automatically"), correctAnswer = "To inspect values while the program runs", explanation = "Debug prints can reveal what your variables contain.")
    )

    private fun getSection9QuizQuestions(): List<PracticeQuestion> = getSection9Practice1Questions() + getSection9Practice2Questions()

    private fun getSection10Practice1Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(id = "java_s10_p1_q1", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 1", question = "Which tool repeats code?", options = listOf("loop", "String", "char", "import"), correctAnswer = "loop", explanation = "Loops repeat code."),
        PracticeQuestion(id = "java_s10_p1_q2", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 2", question = "Which tool stores many values?", options = listOf("array", "if", "void", "catch"), correctAnswer = "array", explanation = "Arrays store many values."),
        PracticeQuestion(id = "java_s10_p1_q3", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 3", question = "Which tool organizes reusable code?", options = listOf("method", "index", "input", "error"), correctAnswer = "method", explanation = "Methods organize reusable code.")
    )

    private fun getSection10Practice2Questions(): List<PracticeQuestion> = listOf(
        PracticeQuestion(id = "java_s10_p2_q1", type = PracticeQuestionType.DRAG_FILL_BLANK, title = "Question 1", question = "Complete the mixed code.", code = """for (int i = 0; i < names.______; i++) {
    if (names[i].______("Leo")) {
        System.out.println("Found");
    }
}""", options = listOf("length", "equals", "size", "==", "Scanner"), correctAnswer = "length", correctAnswers = listOf("length", "equals"), explanation = "Use length for the array size and equals() for String comparison."),
        PracticeQuestion(id = "java_s10_p2_q2", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Question 2", question = "What is the best first step when solving a coding task?", options = listOf("Break it into smaller steps", "Write random code", "Ignore errors", "Delete main"), correctAnswer = "Break it into smaller steps", explanation = "Small steps make bigger problems manageable.")
    )

    private fun getSection10QuizQuestions(): List<PracticeQuestion> = getSection10Practice1Questions() + getSection10Practice2Questions() + listOf(
        PracticeQuestion(id = "java_s10_q6", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Final Quiz 6", question = "Which keyword sends a value back from a method?", options = listOf("return", "new", "catch", "while"), correctAnswer = "return", explanation = "return sends a value back from a method."),
        PracticeQuestion(id = "java_s10_q7", type = PracticeQuestionType.MULTIPLE_CHOICE, title = "Final Quiz 7", question = "Which keyword creates an object?", options = listOf("new", "void", "else", "length"), correctAnswer = "new", explanation = "new creates an object from a class.")
    )
}
