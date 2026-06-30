package com.example.gocode

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
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
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gocode.repositories.AvatarRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

class ArenaFragment : Fragment() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private lateinit var statusLabel: TextView
    private lateinit var arenaScroll: NestedScrollView
    private lateinit var ratingText: TextView
    private lateinit var rankText: TextView
    private lateinit var languageText: TextView
    private lateinit var startButton: MaterialButton
    private lateinit var arenaBackButton: MaterialButton
    private lateinit var battleTabButton: MaterialButton
    private lateinit var leaderboardTabButton: MaterialButton
    private lateinit var battleContent: View
    private lateinit var leaderboardContent: View
    private lateinit var stageTitle: TextView
    private lateinit var stageSubtitle: TextView
    private lateinit var arenaTicker: TextView

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
    private lateinit var scoreBoard: View
    private lateinit var scoreText: TextView
    private lateinit var playerScoreValue: TextView
    private lateinit var opponentScoreValue: TextView
    private lateinit var comboText: TextView
    private lateinit var questionCourseText: TextView
    private lateinit var questionPromptText: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var feedbackText: TextView

    private lateinit var resultPanel: View
    private lateinit var resultTitle: TextView
    private lateinit var resultDetails: TextView
    private lateinit var ratingDeltaText: TextView
    private lateinit var playAgainButton: MaterialButton

    private lateinit var globalLeaderboard: LinearLayout
    private lateinit var localLeaderboard: LinearLayout
    private lateinit var friendsLeaderboard: LinearLayout
    private lateinit var globalLeaderboardButton: LinearLayout
    private lateinit var localLeaderboardButton: LinearLayout
    private lateinit var friendsLeaderboardButton: LinearLayout
    private lateinit var questionIntroOverlay: View
    private lateinit var questionIntroTitle: TextView
    private lateinit var questionIntroSubtitle: TextView
    private lateinit var questionTitleText: TextView

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
    private var correctStreak = 0
    private var opponentCorrectStreak = 0
    private var matchInProgress = false
    private var matchResolved = false

    private var matchmakingJob: Job? = null
    private var idleAnimationJob: Job? = null
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
        setMainBottomNavigationVisible(false)
        renderIdle()
        loadPlayerProfile()

        startButton.setOnClickListener { startMatchmaking() }
        playAgainButton.setOnClickListener { startMatchmaking() }
        arenaBackButton.setOnClickListener { requestExitArena() }
        battleTabButton.setOnClickListener { showArenaTab(ArenaTab.BATTLE) }
        leaderboardTabButton.setOnClickListener { showArenaTab(ArenaTab.LEADERBOARD) }
        globalLeaderboardButton.setOnClickListener { showLeaderboardScope(LeaderboardScope.GLOBAL) }
        localLeaderboardButton.setOnClickListener { showLeaderboardScope(LeaderboardScope.LOCAL) }
        friendsLeaderboardButton.setOnClickListener { showLeaderboardScope(LeaderboardScope.FRIENDS) }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requestExitArena()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        matchmakingJob?.cancel()
        idleAnimationJob?.cancel()
        searchAnimationJob?.cancel()
        opponentJob?.cancel()
        timer?.cancel()
        setMainBottomNavigationVisible(true)
    }

    private fun bindViews(view: View) {
        arenaScroll = view.findViewById(R.id.arenaScroll)
        statusLabel = view.findViewById(R.id.arenaStatusLabel)
        ratingText = view.findViewById(R.id.arenaRating)
        rankText = view.findViewById(R.id.arenaRank)
        languageText = view.findViewById(R.id.arenaLanguages)
        startButton = view.findViewById(R.id.startArenaButton)
        arenaBackButton = view.findViewById(R.id.arenaBackButton)
        battleTabButton = view.findViewById(R.id.battleTabButton)
        leaderboardTabButton = view.findViewById(R.id.leaderboardTabButton)
        battleContent = view.findViewById(R.id.battleContent)
        leaderboardContent = view.findViewById(R.id.leaderboardContent)
        stageTitle = view.findViewById(R.id.stageTitle)
        stageSubtitle = view.findViewById(R.id.stageSubtitle)
        arenaTicker = view.findViewById(R.id.arenaTicker)

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
        scoreBoard = view.findViewById(R.id.scoreBoard)
        scoreText = view.findViewById(R.id.scoreText)
        playerScoreValue = view.findViewById(R.id.playerScoreValue)
        opponentScoreValue = view.findViewById(R.id.opponentScoreValue)
        comboText = view.findViewById(R.id.comboText)
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
        globalLeaderboardButton = view.findViewById(R.id.globalLeaderboardButton)
        localLeaderboardButton = view.findViewById(R.id.localLeaderboardButton)
        friendsLeaderboardButton = view.findViewById(R.id.friendsLeaderboardButton)
        questionIntroOverlay = view.findViewById(R.id.questionIntroOverlay)
        questionIntroTitle = view.findViewById(R.id.questionIntroTitle)
        questionIntroSubtitle = view.findViewById(R.id.questionIntroSubtitle)
        questionTitleText = view.findViewById(R.id.questionTitle)
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
                val avatars = AvatarRepository.load(requireContext())
                val ownedAvatarIds = AvatarRepository.defaultOwnedAvatarIds(avatars) +
                    (doc.get("ownedAvatarIds") as? List<*>).orEmpty().mapNotNull { it as? String }
                val requestedAvatarId = doc.getString("avatarId")
                val avatarId = requestedAvatarId
                    ?.takeIf { it in ownedAvatarIds }
                    ?: AvatarRepository.DEFAULT_AVATAR_ID
                if (requestedAvatarId != avatarId) {
                    db.collection("users").document(user.uid).update("avatarId", avatarId)
                }
                playerAvatarResId = avatarDrawableForId(avatarId, R.drawable.avatar_alien)
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
        matchInProgress = false
        matchResolved = false
        showArenaTab(ArenaTab.BATTLE)
        matchmakingPanel.visibility = View.GONE
        matchPanel.visibility = View.GONE
        resultPanel.visibility = View.GONE
        startButton.visibility = View.VISIBLE
        statusLabel.text = "Ready for a ranked code duel"
        stageTitle.text = "Ranked Output Duel"
        stageSubtitle.text = "Fast answers. Clean logic. Real rank."
        arenaTicker.text = "LIVE QUEUE  ${playerLanguages.joinToString("  ")}"
        startIdleAnimation()
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
        battleTabButton.setTextColor(
            ContextCompat.getColor(requireContext(), if (tab == ArenaTab.BATTLE) android.R.color.black else android.R.color.white)
        )
        leaderboardTabButton.setTextColor(
            ContextCompat.getColor(requireContext(), if (tab == ArenaTab.LEADERBOARD) android.R.color.black else android.R.color.white)
        )
        battleTabButton.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (tab == ArenaTab.BATTLE) android.R.color.holo_green_light else android.R.color.transparent
        )
        leaderboardTabButton.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (tab == ArenaTab.LEADERBOARD) android.R.color.holo_orange_light else android.R.color.transparent
        )
        if (tab == ArenaTab.LEADERBOARD) showLeaderboardScope(LeaderboardScope.GLOBAL)
        startButton.visibility = if (tab == ArenaTab.BATTLE && isIdleBattleState()) View.VISIBLE else View.GONE
        if (tab == ArenaTab.BATTLE && isIdleBattleState()) startIdleAnimation() else stopIdleAnimation()
    }

    private fun isIdleBattleState(): Boolean {
        return matchmakingPanel.visibility != View.VISIBLE &&
                matchPanel.visibility != View.VISIBLE &&
                resultPanel.visibility != View.VISIBLE
    }

    private fun setMainBottomNavigationVisible(visible: Boolean) {
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    private fun requestExitArena() {
        if (!matchInProgress || matchResolved) {
            findNavController().navigate(R.id.homeFragment)
            return
        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_arena_exit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.74f)
        dialog.setCanceledOnTouchOutside(true)

        dialog.findViewById<TextView>(R.id.exitScoreText).text =
            "$playerName $playerScore  -  ${activeOpponent?.name ?: "Opponent"} $opponentScore"
        dialog.findViewById<TextView>(R.id.exitRatingText).text = when {
            playerScore < 0 -> "Leaving now can drop your rating because your score is below zero."
            else -> "Leaving now will not increase your rating, even if you are ahead."
        }
        dialog.findViewById<MaterialButton>(R.id.keepPlayingButton).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<MaterialButton>(R.id.exitMatchButton).setOnClickListener {
            dialog.dismiss()
            forfeitAndExit()
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    @SuppressLint("SetTextI18n")
    private fun startMatchmaking() {
        timer?.cancel()
        matchmakingJob?.cancel()
        stopIdleAnimation()
        searchAnimationJob?.cancel()
        opponentJob?.cancel()
        matchInProgress = false
        matchResolved = false

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
        stageTitle.text = "Scanning Arena"
        stageSubtitle.text = "Looking for shared language power"
        arenaTicker.text = "MATCHMAKING  Rank ${rankName(playerRating)}  Rating $playerRating"

        matchmakingPanel.popIn()
        startSearchAnimation()

        matchmakingJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(950)
            searchingText.text = "Checking shared languages"
            arenaTicker.flashText("SYNCING SKILLS  ${playerLanguages.joinToString("  ")}")
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
            stageTitle.text = "Match Found"
            stageSubtitle.text = "${playerName} vs ${opponent.name}"
            arenaTicker.flashText("LOCKED  ${opponent.languages.joinToString("  ")}  ${rankName(opponent.rating)}")
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
        correctStreak = 0
        opponentCorrectStreak = 0
        matchInProgress = true
        matchResolved = false

        matchmakingPanel.visibility = View.GONE
        matchPanel.visibility = View.VISIBLE
        resultPanel.visibility = View.GONE
        statusLabel.text = "Ranked match live"
        stageTitle.text = "Battle Live"
        stageSubtitle.text = "${playerName} vs ${opponent.name}"
        arenaTicker.text = "FIRST TO THINK FAST  Wrong answers lose ${abs(WRONG_ANSWER_PENALTY)}"
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
        comboText.text = ""
        optionsContainer.removeAllViews()

        roundText.text = "Question ${questionIndex + 1}/$QUESTION_COUNT"
        updateScoreboard()
        questionCourseText.text = "${question.language} - ${question.course}"
        val titleAndCode = question.toTitleAndCode()
        questionTitleText.text = titleAndCode.first
        questionPromptText.text = titleAndCode.second
        questionTitleText.alpha = 0f
        questionTitleText.translationY = 14f
        questionPromptText.alpha = 0f
        questionPromptText.translationY = 18f

        question.options.forEachIndexed { index, option ->
            val answerKey = ('A'.code + index).toChar()
            val button = MaterialButton(requireContext()).apply {
                text = "$answerKey    $option"
                isAllCaps = false
                textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                setBackgroundResource(R.drawable.bg_arena_option)
                backgroundTintList = null
                rippleColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                minHeight = resources.getDimensionPixelSize(R.dimen.arena_option_min_height)
                textSize = 15f
                letterSpacing = 0.02f
                insetTop = 0
                insetBottom = 0
                setPadding(26, 14, 26, 14)
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

        val startQuestion = {
            scrollToQuestion()
            questionStartMs = System.currentTimeMillis()
            animateQuestionEntry()
            startQuestionTimer()
            scheduleOpponentAnswer(question)
        }

        if (questionIndex == 0) {
            showQuestionIntro(question, startQuestion)
        } else {
            startQuestion()
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
        val delta = scoreForAnswer(
            correct = correct,
            elapsedMs = elapsed,
            streak = correctStreak,
            timeout = selectedIndex == -1
        )
        playerScore += delta
        correctStreak = if (correct) correctStreak + 1 else 0

        colorAnswerButtons(selectedIndex)
        updateScoreboard()
        scoreBoard.popIn()
        playerScoreValue.flashScore()
        comboText.text = when {
            correct && correctStreak >= 3 -> "HOT STREAK x$correctStreak"
            correct -> "CLEAN HIT"
            selectedIndex == -1 -> "TIMEOUT"
            else -> "RANK HIT"
        }
        comboText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (correct) android.R.color.holo_green_light else android.R.color.holo_red_light
            )
        )
        comboText.popIn()
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
        val delta = scoreForAnswer(
            correct = correct,
            elapsedMs = elapsed,
            streak = opponentCorrectStreak,
            timeout = false
        )
        opponentScore += delta
        opponentCorrectStreak = if (correct) opponentCorrectStreak + 1 else 0
        opponentAvatarImage.pulse()
        updateScoreboard()
        opponentScoreValue.flashScore()
        arenaTicker.flashText(if (correct) "OPPONENT SCORED" else "OPPONENT MISSED")
        maybeAdvanceQuestion()
    }

    private fun maybeAdvanceQuestion() {
        if (!playerAnswered || !opponentAnswered) return

        timer?.cancel()
        updateScoreboard()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(850)
            questionIndex++
            if (questionIndex >= activeQuestions.size) finishMatch() else showQuestion()
        }
    }

    private fun colorAnswerButtons(selectedIndex: Int) {
        optionsContainer.childrenAsButtons().forEachIndexed { index, button ->
            when (index) {
                currentCorrectIndex -> {
                    button.setBackgroundResource(R.drawable.bg_arena_option_correct)
                    button.pulse()
                }
                selectedIndex -> {
                    button.setBackgroundResource(R.drawable.bg_arena_option_wrong)
                    button.shake()
                }
                else -> button.alpha = 0.56f
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateScoreboard() {
        val opponentName = activeOpponent?.name ?: "Opponent"
        playerScoreValue.text = "${playerName.uppercase().take(10)}\n$playerScore"
        opponentScoreValue.text = "${opponentName.uppercase().take(10)}\n$opponentScore"
        scoreText.text = "VS"
    }

    @SuppressLint("SetTextI18n")
    private fun finishMatch() {
        if (matchResolved) return
        matchResolved = true
        matchInProgress = false
        timer?.cancel()
        opponentJob?.cancel()

        val won = playerScore > opponentScore
        val tied = playerScore == opponentScore
        val ratingDelta = ratingDeltaForMatch(won = won, tied = tied, forfeit = false)

        playerRating = (playerRating + ratingDelta).coerceAtLeast(0)
        renderProfile()
        updateArenaStats(ratingDelta, won, forfeit = false)
        renderLeaderboards()

        matchPanel.visibility = View.GONE
        resultPanel.visibility = View.VISIBLE
        startButton.visibility = View.GONE
        statusLabel.text = if (won) "Victory in the arena" else if (tied) "Arena draw" else "Defeated, but sharper"
        stageTitle.text = if (won) "Victory Locked" else if (tied) "Rank Draw" else "Rematch Ready"
        stageSubtitle.text = "$playerScore - $opponentScore"
        arenaTicker.flashText(if (ratingDelta >= 0) "RATING UP  +$ratingDelta" else "RATING DROP  $ratingDelta")
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

    private fun forfeitAndExit() {
        if (matchResolved) {
            findNavController().navigate(R.id.homeFragment)
            return
        }

        matchResolved = true
        matchInProgress = false
        timer?.cancel()
        opponentJob?.cancel()
        matchmakingJob?.cancel()
        stopSearchAnimation()

        val ratingDelta = ratingDeltaForMatch(won = false, tied = false, forfeit = true)
        playerRating = (playerRating + ratingDelta).coerceAtLeast(0)
        updateArenaStats(ratingDelta, won = false, forfeit = true)
        findNavController().navigate(R.id.homeFragment)
    }

    private fun ratingDeltaForMatch(
        won: Boolean,
        tied: Boolean,
        forfeit: Boolean
    ): Int {
        if (forfeit) {
            return if (playerScore < 0) {
                (-8 - (abs(playerScore) / 35)).coerceAtLeast(-24)
            } else {
                0
            }
        }

        val opponentRating = activeOpponent?.rating ?: playerRating
        val expected = 1.0 / (1.0 + 10.0.pow((opponentRating - playerRating) / 400.0))
        val actual = when {
            won -> 1.0
            tied -> 0.5
            else -> 0.0
        }
        val marginBonus = (abs(playerScore - opponentScore) / 80).coerceAtMost(6)
        val delta = (RATING_K_FACTOR * (actual - expected)).roundToInt()
        return when {
            won -> (delta + marginBonus).coerceIn(8, 32)
            tied -> delta.coerceIn(-6, 6)
            else -> (delta - marginBonus).coerceIn(-32, -8)
        }
    }

    private fun updateArenaStats(ratingDelta: Int, won: Boolean, forfeit: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>(
            "rating" to playerRating,
            "arenaMatches" to FieldValue.increment(1),
            "arenaRatingDeltaTotal" to FieldValue.increment(ratingDelta.toLong())
        )
        if (won) updates["arenaWins"] = FieldValue.increment(1)
        if (forfeit) {
            updates["arenaForfeits"] = FieldValue.increment(1)
            updates["lastOpponentRatingDelta"] = 10L
        }

        db.collection("users").document(uid).update(updates)
    }

    private fun renderLeaderboards() {
        val globalPlayers = (arenaOpponents + ArenaOpponent(playerName, playerRating, playerLanguages, 72, playerAvatarResId))
            .sortedByDescending { it.rating }
            .take(5)
        buildLeaderboardTable(
            container = globalLeaderboard,
            title = "GLOBAL",
            players = globalPlayers,
            rankLabel = "WORLD"
        )
        buildLeaderboardTable(
            container = localLeaderboard,
            title = "LOCAL",
            players = listOf(
                ArenaOpponent(playerName, playerRating, playerLanguages, 72, playerAvatarResId),
                ArenaOpponent("ByteRunner", 1180, listOf("C", "Python"), 66, R.drawable.avatar_robot),
                ArenaOpponent("LoopMage", 1095, listOf("Java"), 62, R.drawable.avatar_owl),
                ArenaOpponent("Ben", 970, listOf("Java"), 55, R.drawable.avatar_boy)
            ).sortedByDescending { it.rating },
            rankLabel = "CITY"
        )
        buildLeaderboardTable(
            container = friendsLeaderboard,
            title = "FRIENDS",
            players = listOf(
                ArenaOpponent("Aviv", 1220, listOf("Java"), 70, R.drawable.avatar_ninja),
                ArenaOpponent(playerName, playerRating, playerLanguages, 72, playerAvatarResId),
                ArenaOpponent("Ben", 970, listOf("Java"), 55, R.drawable.avatar_boy),
                ArenaOpponent("Noa", 930, listOf("Python"), 52, R.drawable.avatar_cat_woman)
            ).sortedByDescending { it.rating },
            rankLabel = "CREW"
        )
        showLeaderboardScope(LeaderboardScope.GLOBAL)
    }

    private fun showLeaderboardScope(scope: LeaderboardScope) {
        globalLeaderboard.visibility = if (scope == LeaderboardScope.GLOBAL) View.VISIBLE else View.GONE
        localLeaderboard.visibility = if (scope == LeaderboardScope.LOCAL) View.VISIBLE else View.GONE
        friendsLeaderboard.visibility = if (scope == LeaderboardScope.FRIENDS) View.VISIBLE else View.GONE

        setScopeButtonState(globalLeaderboardButton, scope == LeaderboardScope.GLOBAL)
        setScopeButtonState(localLeaderboardButton, scope == LeaderboardScope.LOCAL)
        setScopeButtonState(friendsLeaderboardButton, scope == LeaderboardScope.FRIENDS)
    }

    private fun setScopeButtonState(button: LinearLayout, selected: Boolean) {
        val active = ContextCompat.getColor(requireContext(), R.color.accent_green)
        val activeText = ContextCompat.getColor(requireContext(), android.R.color.black)
        val idleText = ContextCompat.getColor(requireContext(), android.R.color.white)
        button.setBackgroundResource(if (selected) R.drawable.bg_arena_scope_tab_active else R.drawable.bg_arena_scope_tab_idle)
        button.alpha = if (selected) 1f else 0.72f
        (0 until button.childCount).forEach { index ->
            when (val child = button.getChildAt(index)) {
                is TextView -> child.setTextColor(if (selected) activeText else idleText)
                is ImageView -> child.imageTintList = ColorStateList.valueOf(if (selected) activeText else active)
            }
        }
    }

    private fun buildLeaderboardTable(
        container: LinearLayout,
        title: String,
        players: List<ArenaOpponent>,
        rankLabel: String
    ) {
        container.removeAllViews()
        container.addView(
            leaderboardTitle("$title LEADERBOARD", "$rankLabel ranking by arena rating"),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        container.addView(
            leaderboardHeader(),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(10) }
        )
        players.take(5).forEachIndexed { index, player ->
            container.addView(
                leaderboardRow(index + 1, player),
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8) }
            )
        }
    }

    private fun leaderboardTitle(title: String, subtitle: String): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(2), dp(2), dp(2))
            addView(TextView(requireContext()).apply {
                text = title
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
            })
            addView(TextView(requireContext()).apply {
                text = subtitle
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))
                textSize = 12f
            })
        }
    }

    private fun leaderboardHeader(): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.bg_arena_leaderboard_header)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            addHeaderCell("#", 0.55f)
            addHeaderCell("Name", 1.75f)
            addHeaderCell("Rank", 1f)
            addHeaderCell("Pts", 0.8f)
        }
    }

    private fun leaderboardRow(position: Int, player: ArenaOpponent): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setBackgroundResource(if (position == 1) R.drawable.bg_arena_leaderboard_top_row else R.drawable.bg_arena_leaderboard_row)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            addValueCell("#$position", 0.55f, highlight = position == 1)
            addView(
                TextView(requireContext()).apply {
                    text = player.name
                    maxLines = 1
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                },
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.75f)
            )
            addValueCell(rankName(player.rating), 1f, highlight = false)
            addValueCell(player.rating.toString(), 0.8f, highlight = position == 1)
        }
    }

    private fun LinearLayout.addHeaderCell(value: String, weight: Float) {
        addView(
            TextView(requireContext()).apply {
                text = value
                setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
        )
    }

    private fun LinearLayout.addValueCell(value: String, weight: Float, highlight: Boolean) {
        addView(
            TextView(requireContext()).apply {
                text = value
                maxLines = 1
                gravity = android.view.Gravity.CENTER_VERTICAL
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (highlight) android.R.color.holo_orange_light else android.R.color.white
                    )
                )
                textSize = 13f
                typeface = if (highlight) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
        )
    }

    private fun ArenaQuestion.toTitleAndCode(): Pair<String, String> {
        val lines = prompt.lines()
        val title = lines.firstOrNull()?.takeIf { it.isNotBlank() } ?: "What is the output?"
        val code = lines.drop(1).joinToString("\n").takeIf { it.isNotBlank() }
            ?: prompt
        return title to code
    }

    private fun scoreForAnswer(
        correct: Boolean,
        elapsedMs: Long,
        streak: Int,
        timeout: Boolean
    ): Int {
        if (!correct) return if (timeout) TIMEOUT_PENALTY else WRONG_ANSWER_PENALTY
        val speedBonus = ((QUESTION_TIME_MS - elapsedMs).coerceAtLeast(0) / 180L)
            .toInt()
            .coerceAtMost(60)
        val streakBonus = (streak * 8).coerceAtMost(32)
        return CORRECT_ANSWER_POINTS + speedBonus + streakBonus
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

    private fun startIdleAnimation() {
        idleAnimationJob?.cancel()
        idleAnimationJob = viewLifecycleOwner.lifecycleScope.launch {
            val tickerStates = listOf(
                "LIVE QUEUE  ${playerLanguages.joinToString("  ")}",
                "OUTPUT DUELS  Speed bonus active",
                "RANKED ARENA  ${rankName(playerRating)}  $playerRating",
                "CODE IQ CHECK  Ready"
            )
            var index = 0
            while (true) {
                startButton.pulseSoft()
                searchVsBadge.pulse()
                arenaTicker.flashText(tickerStates[index % tickerStates.size])
                index++
                delay(1800L)
            }
        }
    }

    private fun stopIdleAnimation() {
        idleAnimationJob?.cancel()
        startButton.animate().cancel()
        startButton.scaleX = 1f
        startButton.scaleY = 1f
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
        questionTitleText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(180L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        questionPromptText.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(50L)
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

    private fun scrollToQuestion() {
        matchPanel.post {
            val targetY = (matchPanel.top + questionCourseText.top - dp(14)).coerceAtLeast(0)
            arenaScroll.smoothScrollTo(0, targetY)
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

    private fun View.pulseSoft() {
        animate()
            .scaleX(1.025f)
            .scaleY(1.025f)
            .setDuration(420L)
            .withEndAction {
                animate().scaleX(1f).scaleY(1f).setDuration(420L).start()
            }
            .start()
    }

    private fun TextView.flashText(value: String) {
        animate().cancel()
        animate()
            .alpha(0.35f)
            .translationY(-5f)
            .setDuration(90L)
            .withEndAction {
                text = value
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(160L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()
    }

    private fun TextView.flashScore() {
        animate().cancel()
        animate()
            .scaleX(1.04f)
            .scaleY(1.04f)
            .setDuration(100L)
            .withEndAction {
                animate().scaleX(1f).scaleY(1f).setDuration(130L).start()
            }
            .start()
    }

    private fun View.shake() {
        animate()
            .translationX(-10f)
            .setDuration(55L)
            .withEndAction {
                animate()
                    .translationX(10f)
                    .setDuration(55L)
                    .withEndAction {
                        animate().translationX(0f).setDuration(70L).start()
                    }
                    .start()
            }
            .start()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).roundToInt()
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

    private enum class LeaderboardScope {
        GLOBAL,
        LOCAL,
        FRIENDS
    }

    private companion object {
        private const val QUESTION_COUNT = 7
        private const val QUESTION_TIME_MS = 12_000L
        private const val CORRECT_ANSWER_POINTS = 100
        private const val WRONG_ANSWER_PENALTY = -35
        private const val TIMEOUT_PENALTY = -50
        private const val RATING_K_FACTOR = 28

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
                language = "Java",
                course = "Getting Started",
                prompt = "What is printed?\nSystem.out.println(\"Hello GoCode!\");",
                options = listOf("Hello GoCode!", "\"Hello GoCode!\"", "Hello Java!", "Nothing"),
                correctIndex = 0
            ),
            ArenaQuestion(
                language = "Java",
                course = "Variables",
                prompt = "What is printed?\nint age = 14;\nSystem.out.println(age + 1);",
                options = listOf("14", "15", "age1", "Compilation error"),
                correctIndex = 1
            ),
            ArenaQuestion(
                language = "Java",
                course = "Variables",
                prompt = "Which type should store true or false?\nboolean isReady = true;",
                options = listOf("int", "String", "boolean", "double"),
                correctIndex = 2
            ),
            ArenaQuestion(
                language = "Java",
                course = "If / Else",
                prompt = "What is printed?\nint score = 80;\nif (score >= 75) {\n    System.out.println(\"Pass\");\n} else {\n    System.out.println(\"Try again\");\n}",
                options = listOf("Pass", "Try again", "75", "Compilation error"),
                correctIndex = 0
            ),
            ArenaQuestion(
                language = "Java",
                course = "Loops",
                prompt = "What is printed last?\nfor (int i = 1; i <= 4; i++) {\n    System.out.println(i);\n}",
                options = listOf("1", "3", "4", "5"),
                correctIndex = 2
            ),
            ArenaQuestion(
                language = "Java",
                course = "Arrays",
                prompt = "What is printed?\nString[] names = {\"Leo\", \"Maya\", \"Dan\"};\nSystem.out.println(names[1]);",
                options = listOf("Leo", "Maya", "Dan", "1"),
                correctIndex = 1
            ),
            ArenaQuestion(
                language = "Java",
                course = "Methods",
                prompt = "What is printed?\nstatic int doubleIt(int n) {\n    return n * 2;\n}\nSystem.out.println(doubleIt(6));",
                options = listOf("6", "8", "12", "Compilation error"),
                correctIndex = 2
            ),
            ArenaQuestion(
                language = "Java",
                course = "Scanner Input",
                prompt = "If the input is 16, what is printed?\nint age = input.nextInt();\nif (age >= 13) System.out.println(\"Welcome\");\nelse System.out.println(\"Too young\");",
                options = listOf("Welcome", "Too young", "16", "Nothing"),
                correctIndex = 0
            ),
            ArenaQuestion(
                language = "Java",
                course = "String Tools",
                prompt = "What is printed?\nString name = \"  Leo  \";\nSystem.out.println(name.trim().equals(\"Leo\"));",
                options = listOf("Leo", "true", "false", "Compilation error"),
                correctIndex = 1
            ),
            ArenaQuestion(
                language = "Java",
                course = "Classes & Objects",
                prompt = "What is printed?\nStudent s = new Student();\ns.name = \"Maya\";\nSystem.out.println(s.name);",
                options = listOf("Student", "name", "Maya", "null"),
                correctIndex = 2
            ),
            ArenaQuestion(
                language = "Java",
                course = "Debugging Basics",
                prompt = "Which block handles a failed parse?\ntry {\n    int n = Integer.parseInt(text);\n} catch (NumberFormatException e) {\n    System.out.println(\"Invalid number\");\n}",
                options = listOf("try", "catch", "class", "main"),
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
