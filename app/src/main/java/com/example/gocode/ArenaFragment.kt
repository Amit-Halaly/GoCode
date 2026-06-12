package com.example.gocode

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gocode.repositories.AvatarRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class ArenaFragment : Fragment() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private lateinit var statusLabel: TextView
    private lateinit var ratingText: TextView
    private lateinit var rankText: TextView
    private lateinit var languageText: TextView
    private lateinit var startButton: MaterialButton
    private lateinit var battleTabButton: MaterialButton
    private lateinit var leaderboardTabButton: MaterialButton
    private lateinit var battleContent: View
    private lateinit var leaderboardContent: View

    private lateinit var matchmakingPanel: View
    private lateinit var searchingText: TextView
    private lateinit var searchingProgress: ProgressBar
    private lateinit var playerSearchAvatar: ImageView
    private lateinit var opponentSearchAvatar: ImageView
    private lateinit var searchVsBadge: TextView
    private lateinit var opponentNameText: TextView
    private lateinit var opponentMetaText: TextView
    private lateinit var opponentRatingText: TextView

    private lateinit var matchPanel: View
    private lateinit var playerAvatarImage: ImageView
    private lateinit var opponentAvatarImage: ImageView
    private lateinit var playerCardName: TextView
    private lateinit var opponentCardName: TextView
    private lateinit var playerCardRank: TextView
    private lateinit var opponentCardRank: TextView
    private lateinit var roundText: TextView
    private lateinit var timerText: TextView
    private lateinit var timerProgress: ProgressBar
    private lateinit var scoreText: TextView
    private lateinit var questionCourseText: TextView
    private lateinit var questionPromptText: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var feedbackText: TextView

    private lateinit var resultPanel: View
    private lateinit var resultTitle: TextView
    private lateinit var resultDetails: TextView
    private lateinit var ratingDeltaText: TextView
    private lateinit var playAgainButton: MaterialButton

    private lateinit var globalLeaderboard: TextView
    private lateinit var localLeaderboard: TextView
    private lateinit var friendsLeaderboard: TextView
    private lateinit var questionIntroOverlay: View
    private lateinit var questionIntroTitle: TextView
    private lateinit var questionIntroSubtitle: TextView

    private var playerName = "You"
    private var playerRating = 1000
    private var playerLanguages = listOf("Java")
    private var playerAvatarResId = 0
    private var activeOpponent: ArenaOpponent? = null
    private var activeQuestions = emptyList<ArenaQuestion>()
    private var questionIndex = 0
    private var playerScore = 0
    private var opponentScore = 0
    private var questionStartMs = 0L
    private var playerAnswered = false
    private var opponentAnswered = false
    private var currentCorrectIndex = -1

    private var matchmakingJob: Job? = null
    private var searchAnimationJob: Job? = null
    private var opponentJob: Job? = null
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_arena, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        renderIdle()
        loadPlayerProfile()

        startButton.setOnClickListener { startMatchmaking() }
        playAgainButton.setOnClickListener { startMatchmaking() }
        battleTabButton.setOnClickListener { showArenaTab(ArenaTab.BATTLE) }
        leaderboardTabButton.setOnClickListener { showArenaTab(ArenaTab.LEADERBOARD) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        matchmakingJob?.cancel()
        searchAnimationJob?.cancel()
        opponentJob?.cancel()
        timer?.cancel()
    }

    private fun bindViews(view: View) {
        statusLabel = view.findViewById(R.id.arenaStatusLabel)
        ratingText = view.findViewById(R.id.arenaRating)
        rankText = view.findViewById(R.id.arenaRank)
        languageText = view.findViewById(R.id.arenaLanguages)
        startButton = view.findViewById(R.id.startArenaButton)
        battleTabButton = view.findViewById(R.id.battleTabButton)
        leaderboardTabButton = view.findViewById(R.id.leaderboardTabButton)
        battleContent = view.findViewById(R.id.battleContent)
        leaderboardContent = view.findViewById(R.id.leaderboardContent)

        matchmakingPanel = view.findViewById(R.id.matchmakingPanel)
        searchingText = view.findViewById(R.id.searchingText)
        searchingProgress = view.findViewById(R.id.searchingProgress)
        playerSearchAvatar = view.findViewById(R.id.playerSearchAvatar)
        opponentSearchAvatar = view.findViewById(R.id.opponentSearchAvatar)
        searchVsBadge = view.findViewById(R.id.searchVsBadge)
        opponentNameText = view.findViewById(R.id.opponentName)
        opponentMetaText = view.findViewById(R.id.opponentMeta)
        opponentRatingText = view.findViewById(R.id.opponentRating)

        matchPanel = view.findViewById(R.id.matchPanel)
        playerAvatarImage = view.findViewById(R.id.playerAvatarImage)
        opponentAvatarImage = view.findViewById(R.id.opponentAvatarImage)
        playerCardName = view.findViewById(R.id.playerCardName)
        opponentCardName = view.findViewById(R.id.opponentCardName)
        playerCardRank = view.findViewById(R.id.playerCardRank)
        opponentCardRank = view.findViewById(R.id.opponentCardRank)
        roundText = view.findViewById(R.id.roundText)
        timerText = view.findViewById(R.id.timerText)
        timerProgress = view.findViewById(R.id.timerProgress)
        scoreText = view.findViewById(R.id.scoreText)
        questionCourseText = view.findViewById(R.id.questionCourse)
        questionPromptText = view.findViewById(R.id.questionPrompt)
        optionsContainer = view.findViewById(R.id.optionsContainer)
        feedbackText = view.findViewById(R.id.feedbackText)

        resultPanel = view.findViewById(R.id.resultPanel)
        resultTitle = view.findViewById(R.id.resultTitle)
        resultDetails = view.findViewById(R.id.resultDetails)
        ratingDeltaText = view.findViewById(R.id.ratingDeltaText)
        playAgainButton = view.findViewById(R.id.playAgainButton)

        globalLeaderboard = view.findViewById(R.id.globalLeaderboard)
        localLeaderboard = view.findViewById(R.id.localLeaderboard)
        friendsLeaderboard = view.findViewById(R.id.friendsLeaderboard)
        questionIntroOverlay = view.findViewById(R.id.questionIntroOverlay)
        questionIntroTitle = view.findViewById(R.id.questionIntroTitle)
        questionIntroSubtitle = view.findViewById(R.id.questionIntroSubtitle)
    }

    @SuppressLint("SetTextI18n")
    private fun loadPlayerProfile() {
        val user = auth.currentUser
        if (user == null) {
            renderProfile()
            renderLeaderboards()
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                playerName = doc.getString("username")?.takeIf { it.isNotBlank() }
                    ?: user.displayName
                    ?: "You"
                playerRating = (doc.getLong("rating") ?: 1000L).toInt()
                val avatarId = doc.getString("avatarId")
                playerAvatarResId = avatarDrawableForId(avatarId, R.drawable.avatar_robot)
                val primaryLanguage = doc.getString("primaryLanguage") ?: "Java"
                val knownLanguages = doc.get("knownLanguages") as? List<*>
                playerLanguages = (knownLanguages?.filterIsInstance<String>().orEmpty() + primaryLanguage)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .ifEmpty { listOf("Java") }
                renderProfile()
                renderLeaderboards()
            }
            .addOnFailureListener {
                renderProfile()
                renderLeaderboards()
            }
    }

    private fun renderIdle() {
        showArenaTab(ArenaTab.BATTLE)
        matchmakingPanel.visibility = View.GONE
        matchPanel.visibility = View.GONE
        resultPanel.visibility = View.GONE
        startButton.visibility = View.VISIBLE
        statusLabel.text = "Ready for a ranked code duel"
    }

    @SuppressLint("SetTextI18n")
    private fun renderProfile() {
        ratingText.text = "$playerRating"
        rankText.text = rankName(playerRating)
        languageText.text = playerLanguages.joinToString(separator = " / ")
        playerCardName.text = playerName
        playerCardRank.text = "${rankName(playerRating)} - $playerRating"
        val avatar = playerAvatarResId.takeIf { it != 0 } ?: R.drawable.avatar_robot
        playerAvatarImage.setImageResource(avatar)
        playerSearchAvatar.setImageResource(avatar)
    }

    private fun showArenaTab(tab: ArenaTab) {
        battleContent.visibility = if (tab == ArenaTab.BATTLE) View.VISIBLE else View.GONE
        leaderboardContent.visibility = if (tab == ArenaTab.LEADERBOARD) View.VISIBLE else View.GONE
        battleTabButton.alpha = if (tab == ArenaTab.BATTLE) 1f else 0.58f
        leaderboardTabButton.alpha = if (tab == ArenaTab.LEADERBOARD) 1f else 0.58f
        startButton.visibility = if (tab == ArenaTab.BATTLE && isIdleBattleState()) View.VISIBLE else View.GONE
    }

    private fun isIdleBattleState(): Boolean {
        return matchmakingPanel.visibility != View.VISIBLE &&
                matchPanel.visibility != View.VISIBLE &&
                resultPanel.visibility != View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun startMatchmaking() {
        timer?.cancel()
        matchmakingJob?.cancel()
        searchAnimationJob?.cancel()
        opponentJob?.cancel()

        startButton.visibility = View.GONE
        resultPanel.visibility = View.GONE
        matchPanel.visibility = View.GONE
        matchmakingPanel.visibility = View.VISIBLE
        searchingProgress.visibility = View.VISIBLE
        opponentNameText.text = "Searching..."
        opponentMetaText.text = "Matching course, language and rating"
        opponentRatingText.text = "Ranked"
        searchingText.text = "Finding opponent"
        statusLabel.text = "Searching arena"

        matchmakingPanel.popIn()
        startSearchAnimation()

        matchmakingJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(950)
            searchingText.text = "Checking shared languages"
            delay(900)
            val opponent = findOpponent()
            activeOpponent = opponent
            opponentNameText.text = opponent.name
            opponentMetaText.text = "${opponent.languages.joinToString(" / ")} - ${rankName(opponent.rating)}"
            opponentRatingText.text = "${opponent.rating}"
            opponentSearchAvatar.setImageResource(opponent.avatarRes)
            opponentAvatarImage.setImageResource(opponent.avatarRes)
            opponentCardName.text = opponent.name
            opponentCardRank.text = "${rankName(opponent.rating)} - ${opponent.rating}"
            searchingProgress.visibility = View.GONE
            searchingText.text = "Match found"
            stopSearchAnimation()
            opponentSearchAvatar.popIn()
            searchVsBadge.pulse()
            matchmakingPanel.popIn()
            delay(900)
            startRound(opponent)
        }
    }

    private fun findOpponent(): ArenaOpponent {
        val possible = arenaOpponents.filter { opponent ->
            opponent.languages.any { lang -> playerLanguages.any { it.equals(lang, ignoreCase = true) } }
        }.ifEmpty { arenaOpponents }

        return possible.minByOrNull { kotlin.math.abs(it.rating - playerRating) }
            ?: arenaOpponents.random()
    }

    private fun startRound(opponent: ArenaOpponent) {
        val sharedLanguages = playerLanguages.filter { playerLang ->
            opponent.languages.any { it.equals(playerLang, ignoreCase = true) }
        }.ifEmpty { playerLanguages }

        activeQuestions = arenaQuestions
            .filter { question -> sharedLanguages.any { it.equals(question.language, ignoreCase = true) } }
            .ifEmpty { arenaQuestions }
            .shuffled()
            .take(QUESTION_COUNT)

        questionIndex = 0
        playerScore = 0
        opponentScore = 0

        matchmakingPanel.visibility = View.GONE
        matchPanel.visibility = View.VISIBLE
        resultPanel.visibility = View.GONE
        statusLabel.text = "Ranked match live"
        matchPanel.popIn()
        animateVersusEntry()
        showQuestion()
    }

    @SuppressLint("SetTextI18n")
    private fun showQuestion() {
        timer?.cancel()
        opponentJob?.cancel()

        val question = activeQuestions.getOrNull(questionIndex) ?: run {
            finishMatch()
            return
        }

        playerAnswered = false
        opponentAnswered = false
        currentCorrectIndex = question.correctIndex
        feedbackText.text = ""
        optionsContainer.removeAllViews()

        roundText.text = "Question ${questionIndex + 1}/$QUESTION_COUNT"
        scoreText.text = "$playerName $playerScore  -  ${activeOpponent?.name ?: "Opponent"} $opponentScore"
        questionCourseText.text = "${question.language} - ${question.course}"
        questionPromptText.text = question.prompt
        questionPromptText.alpha = 0f
        questionPromptText.translationY = 18f

        question.options.forEachIndexed { index, option ->
            val button = MaterialButton(requireContext()).apply {
                text = option
                isAllCaps = false
                textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                setBackgroundResource(R.drawable.bg_arena_option)
                minHeight = resources.getDimensionPixelSize(R.dimen.arena_option_min_height)
                setPadding(22, 12, 22, 12)
                setOnClickListener { submitPlayerAnswer(index) }
                alpha = 0f
                translationY = 20f
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.arena_option_gap)
            }
            optionsContainer.addView(button, params)
        }

        showQuestionIntro(question) {
            questionStartMs = System.currentTimeMillis()
            animateQuestionEntry()
            startQuestionTimer()
            scheduleOpponentAnswer(question)
        }
    }

    private fun showQuestionIntro(question: ArenaQuestion, onDone: () -> Unit) {
        questionIntroTitle.text = "WHAT IS THE OUTPUT?"
        questionIntroSubtitle.text = "${question.language} - ${question.course}"
        questionIntroOverlay.visibility = View.VISIBLE
        questionIntroOverlay.alpha = 0f
        questionIntroTitle.scaleX = 0.72f
        questionIntroTitle.scaleY = 0.72f
        questionIntroSubtitle.translationY = 22f
        questionIntroSubtitle.alpha = 0f

        questionIntroOverlay.animate()
            .alpha(1f)
            .setDuration(140)
            .withEndAction {
                questionIntroTitle.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(270)
                    .setInterpolator(OvershootInterpolator())
                    .withEndAction {
                        questionIntroSubtitle.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(170)
                            .withEndAction {
                                questionIntroOverlay.postDelayed({
                                    questionIntroOverlay.animate()
                                        .alpha(0f)
                                        .setDuration(180)
                                        .withEndAction {
                                            questionIntroOverlay.visibility = View.GONE
                                            onDone()
                                        }
                                        .start()
                                }, 420L)
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun startQuestionTimer() {
        timerProgress.max = QUESTION_TIME_MS.toInt()
        timerProgress.progress = QUESTION_TIME_MS.toInt()
        timerText.text = "${QUESTION_TIME_MS / 1000}s"

        timer = object : CountDownTimer(QUESTION_TIME_MS, 100L) {
            override fun onTick(millisUntilFinished: Long) {
                timerProgress.progress = millisUntilFinished.toInt()
                timerText.text = "${(millisUntilFinished / 1000f).roundToInt()}s"
                if (millisUntilFinished < 3_500L) {
                    timerText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
                    timerText.pulse()
                }
            }

            override fun onFinish() {
                timerProgress.progress = 0
                timerText.text = "0s"
                if (!playerAnswered) submitPlayerAnswer(selectedIndex = -1)
            }
        }.start()
    }

    private fun scheduleOpponentAnswer(question: ArenaQuestion) {
        val delayMs = Random.nextLong(1600L, QUESTION_TIME_MS - 900L)
        opponentJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(delayMs)
            val skillRoll = Random.nextInt(100)
            val selectedIndex = if (skillRoll < activeOpponent.orDefaultSkill()) {
                question.correctIndex
            } else {
                question.options.indices.filter { it != question.correctIndex }.random()
            }
            submitOpponentAnswer(selectedIndex, delayMs)
        }
    }

    private fun submitPlayerAnswer(selectedIndex: Int) {
        if (playerAnswered) return
        playerAnswered = true

        optionsContainer.childrenAsButtons().forEach { it.isEnabled = false }

        val elapsed = System.currentTimeMillis() - questionStartMs
        val correct = selectedIndex == currentCorrectIndex
        val delta = scoreForAnswer(correct, elapsed)
        playerScore += delta

        colorAnswerButtons(selectedIndex)
        scoreText.popIn()
        feedbackText.text = when {
            correct -> "+$delta quick points"
            selectedIndex == -1 -> "$delta timeout"
            else -> "$delta wrong answer"
        }

        maybeAdvanceQuestion()
    }

    private fun submitOpponentAnswer(selectedIndex: Int, elapsed: Long) {
        if (opponentAnswered) return
        opponentAnswered = true

        val correct = selectedIndex == currentCorrectIndex
        opponentScore += scoreForAnswer(correct, elapsed)
        opponentAvatarImage.pulse()
        maybeAdvanceQuestion()
    }

    private fun maybeAdvanceQuestion() {
        if (!playerAnswered || !opponentAnswered) return

        timer?.cancel()
        scoreText.text = "$playerName $playerScore  -  ${activeOpponent?.name ?: "Opponent"} $opponentScore"

        viewLifecycleOwner.lifecycleScope.launch {
            delay(850)
            questionIndex++
            if (questionIndex >= activeQuestions.size) finishMatch() else showQuestion()
        }
    }

    private fun colorAnswerButtons(selectedIndex: Int) {
        optionsContainer.childrenAsButtons().forEachIndexed { index, button ->
            when (index) {
                currentCorrectIndex -> button.setBackgroundResource(R.drawable.bg_arena_option_correct)
                selectedIndex -> button.setBackgroundResource(R.drawable.bg_arena_option_wrong)
                else -> button.alpha = 0.56f
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun finishMatch() {
        timer?.cancel()
        opponentJob?.cancel()

        val won = playerScore > opponentScore
        val tied = playerScore == opponentScore
        val ratingDelta = when {
            won -> 28
            tied -> 4
            else -> -18
        }

        playerRating = (playerRating + ratingDelta).coerceAtLeast(0)
        renderProfile()
        updateArenaStats(ratingDelta, won)
        renderLeaderboards()

        matchPanel.visibility = View.GONE
        resultPanel.visibility = View.VISIBLE
        startButton.visibility = View.GONE
        statusLabel.text = if (won) "Victory in the arena" else if (tied) "Arena draw" else "Defeated, but sharper"
        resultTitle.text = if (won) "Victory" else if (tied) "Draw" else "Defeat"
        resultDetails.text = "$playerName $playerScore - ${activeOpponent?.name ?: "Opponent"} $opponentScore"
        ratingDeltaText.text = if (ratingDelta >= 0) "+$ratingDelta rating" else "$ratingDelta rating"
        ratingDeltaText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (ratingDelta >= 0) android.R.color.holo_green_light else android.R.color.holo_red_light
            )
        )
        resultPanel.popIn()
    }

    private fun updateArenaStats(ratingDelta: Int, won: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>(
            "rating" to playerRating,
            "arenaMatches" to FieldValue.increment(1),
            "arenaRatingDeltaTotal" to FieldValue.increment(ratingDelta.toLong())
        )
        if (won) updates["arenaWins"] = FieldValue.increment(1)

        db.collection("users").document(uid).update(updates)
    }

    private fun renderLeaderboards() {
        val global = (arenaOpponents + ArenaOpponent(playerName, playerRating, playerLanguages, 72, playerAvatarResId))
            .sortedByDescending { it.rating }
            .take(5)
            .mapIndexed { index, player ->
                val medal = when (index) {
                    0 -> "01"
                    1 -> "02"
                    2 -> "03"
                    else -> "0${index + 1}"
                }
                "$medal  ${player.name.padEnd(14).take(14)}  ${player.rating}  ${rankName(player.rating)}"
            }
            .joinToString("\n")

        globalLeaderboard.text = "GLOBAL RANK\n$global"
        localLeaderboard.text = listOf(
            "LOCAL LADDER",
            "01  ${playerName.padEnd(14).take(14)}  $playerRating  ${rankName(playerRating)}",
            "02  ByteRunner      1180  Silver",
            "03  LoopMage        1095  Silver"
        ).joinToString("\n")
        friendsLeaderboard.text = listOf(
            "FRIENDS",
            "01  Aviv            1220  Silver",
            "02  ${playerName.padEnd(14).take(14)}  $playerRating  ${rankName(playerRating)}",
            "03  Ben              970  Bronze"
        ).joinToString("\n")
    }

    private fun scoreForAnswer(correct: Boolean, elapsedMs: Long): Int {
        if (!correct) return WRONG_ANSWER_PENALTY
        val speedBonus = ((QUESTION_TIME_MS - elapsedMs).coerceAtLeast(0) / 90L).toInt()
        return 420 + speedBonus
    }

    private fun rankName(rating: Int): String {
        return when {
            rating >= 1800 -> "Legend"
            rating >= 1500 -> "Diamond"
            rating >= 1250 -> "Gold"
            rating >= 1000 -> "Silver"
            else -> "Bronze"
        }
    }

    private fun ArenaOpponent?.orDefaultSkill(): Int = this?.skill ?: 68

    private fun View.popIn() {
        alpha = 0f
        scaleX = 0.96f
        scaleY = 0.96f
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(230L)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    private fun startSearchAnimation() {
        searchAnimationJob?.cancel()
        playerSearchAvatar.scaleX = 1f
        playerSearchAvatar.scaleY = 1f
        opponentSearchAvatar.scaleX = 1f
        opponentSearchAvatar.scaleY = 1f
        searchVsBadge.scaleX = 1f
        searchVsBadge.scaleY = 1f
        searchAnimationJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                playerSearchAvatar.floatBeat(direction = -1f)
                searchVsBadge.pulse()
                delay(360L)
                opponentSearchAvatar.floatBeat(direction = 1f)
                delay(420L)
            }
        }
    }

    private fun stopSearchAnimation() {
        searchAnimationJob?.cancel()
        listOf(playerSearchAvatar, opponentSearchAvatar, searchVsBadge).forEach { view ->
            view.animate().cancel()
            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0f)
                .alpha(1f)
                .setDuration(160L)
                .start()
        }
    }

    private fun animateVersusEntry() {
        playerAvatarImage.translationX = -90f
        opponentAvatarImage.translationX = 90f
        playerAvatarImage.alpha = 0f
        opponentAvatarImage.alpha = 0f
        playerAvatarImage.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(360L)
            .setInterpolator(OvershootInterpolator())
            .start()
        opponentAvatarImage.animate()
            .alpha(1f)
            .translationX(0f)
            .setStartDelay(90L)
            .setDuration(360L)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    private fun animateQuestionEntry() {
        questionPromptText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(240L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        optionsContainer.childrenAsButtons().forEachIndexed { index, button ->
            button.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(80L + index * 65L)
                .setDuration(210L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun View.floatBeat(direction: Float) {
        animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .translationX(direction * 8f)
            .setDuration(150L)
            .withEndAction {
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationX(0f)
                    .setDuration(180L)
                    .start()
            }
            .start()
    }

    private fun View.pulse() {
        animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(110L)
            .withEndAction {
                animate().scaleX(1f).scaleY(1f).setDuration(110L).start()
            }
            .start()
    }

    private fun avatarDrawableForId(avatarId: String?, fallback: Int): Int {
        val avatar = AvatarRepository.load(requireContext()).firstOrNull { it.id == avatarId }
        if (avatar != null) {
            val resId = AvatarRepository.resolveDrawableResId(requireContext(), avatar.drawableName)
            if (resId != 0) return resId
        }
        return fallback
    }

    private fun LinearLayout.childrenAsButtons(): List<MaterialButton> {
        return (0 until childCount).mapNotNull { getChildAt(it) as? MaterialButton }
    }

    private data class ArenaQuestion(
        val language: String,
        val course: String,
        val prompt: String,
        val options: List<String>,
        val correctIndex: Int
    )

    private data class ArenaOpponent(
        val name: String,
        val rating: Int,
        val languages: List<String>,
        val skill: Int,
        val avatarRes: Int
    )

    private enum class ArenaTab {
        BATTLE,
        LEADERBOARD
    }

    private companion object {
        private const val QUESTION_COUNT = 5
        private const val QUESTION_TIME_MS = 12_000L
        private const val WRONG_ANSWER_PENALTY = -180

        private val arenaOpponents = listOf(
            ArenaOpponent("NullPointer", 1260, listOf("Java", "C"), 74, R.drawable.avatar_alien),
            ArenaOpponent("StackQueen", 1420, listOf("Java", "Python"), 78, R.drawable.avatar_witch),
            ArenaOpponent("ByteRunner", 1180, listOf("C", "Python"), 66, R.drawable.avatar_robot),
            ArenaOpponent("LoopMage", 1095, listOf("Java"), 62, R.drawable.avatar_owl),
            ArenaOpponent("AlgoNinja", 1610, listOf("Python", "Java", "C"), 84, R.drawable.avatar_ninja)
        )

        private val arenaQuestions = listOf(
            ArenaQuestion(
                language = "Java",
                course = "Variables",
                prompt = "What is the output?\nint x = 4;\nSystem.out.println(x++);",
                options = listOf("4", "5", "3", "Compilation error"),
                correctIndex = 0
            ),
            ArenaQuestion(
                language = "Java",
                course = "Strings",
                prompt = "What is printed?\nString s = \"Go\";\nSystem.out.println(s + 2 + 3);",
                options = listOf("Go5", "Go23", "5Go", "Compilation error"),
                correctIndex = 1
            ),
            ArenaQuestion(
                language = "Java",
                course = "Loops",
                prompt = "How many times does this loop run?\nfor (int i = 0; i < 3; i++)",
                options = listOf("2", "3", "4", "Infinite"),
                correctIndex = 1
            ),
            ArenaQuestion(
                language = "Python",
                course = "Lists",
                prompt = "What is the output?\nnums = [1, 2, 3]\nprint(nums[1])",
                options = listOf("1", "2", "3", "Error"),
                correctIndex = 1
            ),
            ArenaQuestion(
                language = "Python",
                course = "Operators",
                prompt = "What is printed?\nprint(7 // 2)",
                options = listOf("3.5", "4", "3", "2"),
                correctIndex = 2
            ),
            ArenaQuestion(
                language = "Python",
                course = "Booleans",
                prompt = "What is printed?\nprint(True and False)",
                options = listOf("True", "False", "0", "Error"),
                correctIndex = 1
            ),
            ArenaQuestion(
                language = "C",
                course = "Pointers",
                prompt = "What does &x represent in C?",
                options = listOf("The value of x", "The address of x", "A copy of x", "The size of x"),
                correctIndex = 1
            ),
            ArenaQuestion(
                language = "C",
                course = "Arrays",
                prompt = "What is the first index in a C array?",
                options = listOf("0", "1", "-1", "Depends on compiler"),
                correctIndex = 0
            ),
            ArenaQuestion(
                language = "C",
                course = "Operators",
                prompt = "What is printed?\nprintf(\"%d\", 5 / 2);",
                options = listOf("2.5", "3", "2", "Compilation error"),
                correctIndex = 2
            )
        )
    }
}
