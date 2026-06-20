package com.example.gocode.gamification

import com.example.gocode.R

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val rewardText: String,
    val iconRes: Int
)

object AchievementCatalog {
    val all: List<AchievementDefinition> = listOf(
        AchievementDefinition("first_lesson", "First Lesson", "Complete your first lesson.", "+15 coins", R.drawable.ach_first_course),
        AchievementDefinition("lesson_runner_5", "Lesson Runner", "Complete 5 lessons.", "+35 coins", R.drawable.ach_first_course),
        AchievementDefinition("lesson_master_10", "Lesson Master", "Complete 10 lessons.", "+75 coins", R.drawable.ach_first_course),
        AchievementDefinition("first_practice", "First Practice", "Finish your first practice session.", "+25 coins", R.drawable.ach_first_challenge),
        AchievementDefinition("practice_streak_5", "Practice Streak", "Complete 5 practice sessions.", "+50 coins", R.drawable.ach_challenges_10),
        AchievementDefinition("practice_grinder_10", "Practice Grinder", "Complete 10 practice sessions.", "+100 coins", R.drawable.ach_challenges_10),
        AchievementDefinition("first_quiz", "First Quiz", "Complete your first section quiz.", "+50 coins", R.drawable.trophy),
        AchievementDefinition("quiz_climber_3", "Quiz Climber", "Complete 3 section quizzes.", "+90 coins", R.drawable.trophy),
        AchievementDefinition("quiz_champion_7", "Quiz Champion", "Complete 7 section quizzes.", "+180 coins", R.drawable.trophy),
        AchievementDefinition("first_code", "First Code Challenge", "Complete your first coding challenge.", "+75 coins", R.drawable.ach_first_challenge),
        AchievementDefinition("code_builder_3", "Code Builder", "Complete 3 coding challenges.", "+120 coins", R.drawable.ach_first_challenge),
        AchievementDefinition("code_crafter_8", "Code Crafter", "Complete 8 coding challenges.", "+240 coins", R.drawable.ach_challenges_10),
        AchievementDefinition("first_section", "Section Cleared", "Clear your first learning section.", "+50 coins", R.drawable.trophy),
        AchievementDefinition("three_sections", "Three Sections Down", "Clear 3 Java sections.", "+120 coins", R.drawable.trophy),
        AchievementDefinition("half_path", "Halfway Through Java", "Clear 5 Java sections.", "+200 coins", R.drawable.trophy),
        AchievementDefinition("level_2", "Level 2", "Reach level 2.", "Level reward", R.drawable.trophy),
        AchievementDefinition("level_3", "Rising Coder", "Reach level 3.", "Level reward", R.drawable.trophy),
        AchievementDefinition("level_5", "Level 5", "Reach level 5.", "Level reward", R.drawable.trophy),
        AchievementDefinition("level_10", "Level 10", "Reach level 10.", "Level reward", R.drawable.trophy),
        AchievementDefinition("coin_100", "First Coins", "Collect 100 coins.", "+25 coins", R.drawable.coin),
        AchievementDefinition("coin_500", "Coin Collector", "Collect 500 coins.", "+100 coins", R.drawable.coin),
        AchievementDefinition("coin_1000", "Treasure Stack", "Collect 1000 coins.", "+250 coins", R.drawable.coin),
        AchievementDefinition("java_path_complete", "Java Path Complete", "Complete the Java fundamentals path.", "+500 coins", R.drawable.trophy)
    )

    fun byId(id: String): AchievementDefinition? = all.firstOrNull { it.id == id }
}
