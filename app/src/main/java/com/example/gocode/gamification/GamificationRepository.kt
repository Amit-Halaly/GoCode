package com.example.gocode.gamification

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class RewardGrant(
    val xp: Long,
    val coins: Long,
    val title: String
)

data class GamificationResult(
    val reward: RewardGrant,
    val levelBefore: Long,
    val levelAfter: Long,
    val bonusCoins: Long,
    val newAchievements: List<AchievementReward>
) {
    val leveledUp: Boolean get() = levelAfter > levelBefore
}

data class AchievementReward(
    val id: String,
    val title: String,
    val description: String
)

object GamificationRepository {
    private const val PREFS_NAME = "gamification_local"
    private const val KEY_LEVEL = "level"
    private const val KEY_XP = "xp"
    private const val KEY_XP_TO_NEXT = "xpToNext"
    private const val KEY_COINS = "coins"
    private const val KEY_REWARDED_NODES = "rewardedNodeIds"
    private const val KEY_ACHIEVEMENTS = "achievementIds"

    fun awardNodeCompleted(
        context: Context,
        nodeId: String,
        onResult: (GamificationResult?) -> Unit = {}
    ) {
        val reward = rewardForNode(nodeId)
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            onResult(applyLocalReward(context, nodeId, reward))
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user.uid)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val rewardedNodeIds = snapshot.stringList(KEY_REWARDED_NODES)
            if (nodeId in rewardedNodeIds) {
                return@runTransaction null
            }

            val levelBefore = snapshot.getLong(KEY_LEVEL) ?: 1L
            var level = levelBefore
            var xp = snapshot.getLong(KEY_XP) ?: 0L
            var coins = snapshot.getLong(KEY_COINS) ?: 0L
            var xpToNext = xpRequiredForLevel(level)
            val achievements = snapshot.stringList(KEY_ACHIEVEMENTS).toMutableSet()

            xp += reward.xp
            coins += reward.coins

            var bonusCoins = 0L
            while (xp >= xpToNext) {
                xp -= xpToNext
                level += 1
                val levelBonus = levelRewardCoins(level)
                coins += levelBonus
                bonusCoins += levelBonus
                xpToNext = xpRequiredForLevel(level)
            }

            val nextRewardedNodes = rewardedNodeIds + nodeId
            val newAchievements = unlockedAchievements(
                nodeId = nodeId,
                level = level,
                coins = coins,
                rewardedNodeIds = nextRewardedNodes,
                existingAchievements = achievements
            )
            achievements.addAll(newAchievements.map { it.id })

            val updates = mutableMapOf<String, Any>(
                KEY_LEVEL to level,
                KEY_XP to xp,
                KEY_XP_TO_NEXT to xpToNext,
                KEY_COINS to coins,
                KEY_REWARDED_NODES to nextRewardedNodes,
                KEY_ACHIEVEMENTS to achievements.toList(),
                "completedNodeCount" to nextRewardedNodes.size.toLong(),
                "lessonNodesCompleted" to nextRewardedNodes.count { it.contains("_l") }.toLong(),
                "practiceNodesCompleted" to nextRewardedNodes.count { it.contains("_p") }.toLong(),
                "quizNodesCompleted" to nextRewardedNodes.count { it.contains("_q") }.toLong(),
                "codeNodesCompleted" to nextRewardedNodes.count { it.contains("_c") }.toLong()
            )

            if (!snapshot.exists()) {
                updates["createdFromGamification"] = true
            }

            transaction.set(userRef, updates, SetOptions.merge())

            GamificationResult(
                reward = reward,
                levelBefore = levelBefore,
                levelAfter = level,
                bonusCoins = bonusCoins,
                newAchievements = newAchievements
            )
        }.addOnSuccessListener { result ->
            onResult(result)
        }.addOnFailureListener {
            onResult(applyLocalReward(context, nodeId, reward))
        }
    }

    fun rewardForNode(nodeId: String): RewardGrant {
        return when {
            nodeId.contains("_q") -> RewardGrant(80, 50, "Quiz complete")
            nodeId.contains("_c") -> RewardGrant(100, 75, "Code challenge complete")
            nodeId.contains("_p") -> RewardGrant(50, 25, "Practice complete")
            else -> RewardGrant(35, 15, "Lesson complete")
        }
    }

    fun spendCoins(
        context: Context,
        amount: Long,
        onResult: (Boolean) -> Unit = {}
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onResult(spendLocalCoins(context, amount))
            return
        }

        val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val coins = snapshot.getLong(KEY_COINS) ?: 0L
            if (coins < amount) return@runTransaction false

            transaction.set(userRef, mapOf(KEY_COINS to coins - amount), SetOptions.merge())
            true
        }.addOnSuccessListener { success ->
            onResult(success)
        }.addOnFailureListener {
            onResult(false)
        }
    }

    fun resetProgress(
        context: Context,
        onComplete: (Boolean) -> Unit = {}
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onComplete(true)
            return
        }

        val resetData = mapOf(
            KEY_LEVEL to 1L,
            KEY_XP to 0L,
            KEY_XP_TO_NEXT to xpRequiredForLevel(1L),
            KEY_COINS to 0L,
            KEY_REWARDED_NODES to emptyList<String>(),
            KEY_ACHIEVEMENTS to emptyList<String>(),
            "completedNodeCount" to 0L,
            "lessonNodesCompleted" to 0L,
            "practiceNodesCompleted" to 0L,
            "quizNodesCompleted" to 0L,
            "codeNodesCompleted" to 0L,
            "arenaWins" to 0L
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .set(resetData, SetOptions.merge())
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun applyLocalReward(
        context: Context,
        nodeId: String,
        reward: RewardGrant
    ): GamificationResult? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rewardedNodes = prefs.getStringSet(KEY_REWARDED_NODES, emptySet()).orEmpty()
        if (nodeId in rewardedNodes) return null

        val levelBefore = prefs.getLong(KEY_LEVEL, 1L)
        var level = levelBefore
        var xp = prefs.getLong(KEY_XP, 0L) + reward.xp
        var coins = prefs.getLong(KEY_COINS, 0L) + reward.coins
        var xpToNext = xpRequiredForLevel(level)

        var bonusCoins = 0L
        while (xp >= xpToNext) {
            xp -= xpToNext
            level += 1
            val levelBonus = levelRewardCoins(level)
            coins += levelBonus
            bonusCoins += levelBonus
            xpToNext = xpRequiredForLevel(level)
        }

        val nextRewardedNodes = rewardedNodes + nodeId
        val existingAchievements = prefs.getStringSet(KEY_ACHIEVEMENTS, emptySet()).orEmpty()
            .toMutableSet()
        val newAchievements = unlockedAchievements(
            nodeId = nodeId,
            level = level,
            coins = coins,
            rewardedNodeIds = nextRewardedNodes.toList(),
            existingAchievements = existingAchievements
        )
        existingAchievements.addAll(newAchievements.map { it.id })

        prefs.edit()
            .putLong(KEY_LEVEL, level)
            .putLong(KEY_XP, xp)
            .putLong(KEY_XP_TO_NEXT, xpToNext)
            .putLong(KEY_COINS, coins)
            .putStringSet(KEY_REWARDED_NODES, nextRewardedNodes)
            .putStringSet(KEY_ACHIEVEMENTS, existingAchievements)
            .apply()

        return GamificationResult(reward, levelBefore, level, bonusCoins, newAchievements)
    }

    private fun spendLocalCoins(context: Context, amount: Long): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val coins = prefs.getLong(KEY_COINS, 0L)
        if (coins < amount) return false

        prefs.edit()
            .putLong(KEY_COINS, coins - amount)
            .apply()
        return true
    }

    private fun unlockedAchievements(
        nodeId: String,
        level: Long,
        coins: Long,
        rewardedNodeIds: List<String>,
        existingAchievements: Set<String>
    ): List<AchievementReward> {
        val lessonCount = rewardedNodeIds.count { it.contains("_l") }
        val practiceCount = rewardedNodeIds.count { it.contains("_p") }
        val quizCount = rewardedNodeIds.count { it.contains("_q") }
        val codeCount = rewardedNodeIds.count { it.contains("_c") }

        val candidates = buildList {
            if (nodeId.contains("_l")) {
                add(AchievementReward("first_lesson", "First Lesson", "You completed your first lesson."))
            }
            if (lessonCount >= 5) {
                add(AchievementReward("lesson_runner_5", "Lesson Runner", "You completed 5 lessons."))
            }
            if (lessonCount >= 10) {
                add(AchievementReward("lesson_master_10", "Lesson Master", "You completed 10 lessons."))
            }
            if (nodeId.contains("_p")) {
                add(AchievementReward("first_practice", "First Practice", "You finished your first practice session."))
            }
            if (practiceCount >= 5) {
                add(AchievementReward("practice_streak_5", "Practice Streak", "You completed 5 practice sessions."))
            }
            if (practiceCount >= 10) {
                add(AchievementReward("practice_grinder_10", "Practice Grinder", "You completed 10 practice sessions."))
            }
            if (nodeId.contains("_q")) {
                add(AchievementReward("first_quiz", "First Quiz", "You completed your first section quiz."))
            }
            if (quizCount >= 3) {
                add(AchievementReward("quiz_climber_3", "Quiz Climber", "You completed 3 section quizzes."))
            }
            if (quizCount >= 7) {
                add(AchievementReward("quiz_champion_7", "Quiz Champion", "You completed 7 section quizzes."))
            }
            if (nodeId.contains("_c")) {
                add(AchievementReward("first_code", "First Code Challenge", "You completed your first coding challenge."))
            }
            if (codeCount >= 3) {
                add(AchievementReward("code_builder_3", "Code Builder", "You completed 3 coding challenges."))
            }
            if (codeCount >= 8) {
                add(AchievementReward("code_crafter_8", "Code Crafter", "You completed 8 coding challenges."))
            }
            if (quizCount >= 1) {
                add(AchievementReward("first_section", "Section Cleared", "You cleared your first learning section."))
            }
            if (quizCount >= 3) {
                add(AchievementReward("three_sections", "Three Sections Down", "You cleared 3 learning sections."))
            }
            if (quizCount >= 5) {
                add(AchievementReward("half_path", "Halfway Through Java", "You cleared 5 Java sections."))
            }
            if (level >= 2) {
                add(AchievementReward("level_2", "Level 2", "You reached level 2."))
            }
            if (level >= 3) {
                add(AchievementReward("level_3", "Rising Coder", "You reached level 3."))
            }
            if (level >= 5) {
                add(AchievementReward("level_5", "Level 5", "You reached level 5."))
            }
            if (level >= 10) {
                add(AchievementReward("level_10", "Level 10", "You reached level 10."))
            }
            if (coins >= 100) {
                add(AchievementReward("coin_100", "First Coins", "You collected 100 coins."))
            }
            if (coins >= 500) {
                add(AchievementReward("coin_500", "Coin Collector", "You collected 500 coins."))
            }
            if (coins >= 1000) {
                add(AchievementReward("coin_1000", "Treasure Stack", "You collected 1000 coins."))
            }
            if ("java_u10_q1" in rewardedNodeIds) {
                add(AchievementReward("java_path_complete", "Java Path Complete", "You completed the Java fundamentals path."))
            }
            if ("c_u10_q1" in rewardedNodeIds) {
                add(AchievementReward("c_path_complete", "C Path Complete", "You completed the C fundamentals path."))
            }
        }

        return candidates.distinctBy { it.id }
            .filterNot { it.id in existingAchievements }
    }

    private fun xpRequiredForLevel(level: Long): Long {
        return 120L + ((level - 1L).coerceAtLeast(0L) * 40L)
    }

    private fun levelRewardCoins(level: Long): Long {
        return 50L + (level * 10L)
    }

    private fun DocumentSnapshot.stringList(field: String): List<String> {
        return (get(field) as? List<*>)
            .orEmpty()
            .mapNotNull { it as? String }
    }
}
