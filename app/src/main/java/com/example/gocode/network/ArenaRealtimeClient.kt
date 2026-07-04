package com.example.gocode.network

import com.example.gocode.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ArenaRealtimeClient(
    private val listener: Listener,
) {
    interface Listener {
        fun onArenaEvent(event: ArenaEvent)
        fun onArenaError(message: String)
        fun onArenaClosed()
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    fun findMatch(profile: ArenaPlayerProfile) {
        close()
        val request = Request.Builder()
            .url(arenaWebSocketUrl())
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(profile.toFindMatchJson().toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    listener.onArenaEvent(ArenaEvent.fromJson(JSONObject(text)))
                }.onFailure {
                    listener.onArenaError(it.message ?: "Invalid arena event")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                listener.onArenaError(t.message ?: "Arena connection failed")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                listener.onArenaClosed()
            }
        })
    }

    fun submitAnswer(selectedIndex: Int) {
        send(JSONObject().put("type", "answer").put("selectedIndex", selectedIndex))
    }

    fun cancelMatchmaking() {
        send(JSONObject().put("type", "cancel_matchmaking"))
    }

    fun forfeit() {
        send(JSONObject().put("type", "forfeit"))
    }

    fun close() {
        webSocket?.close(1000, "closing arena")
        webSocket = null
    }

    private fun send(payload: JSONObject) {
        webSocket?.send(payload.toString())
    }

    private fun arenaWebSocketUrl(): String {
        val baseUrl = BuildConfig.EXEC_API_BASE_URL.trimEnd('/')
        return when {
            baseUrl.startsWith("https://") -> "wss://${baseUrl.removePrefix("https://")}/arena/ws"
            baseUrl.startsWith("http://") -> "ws://${baseUrl.removePrefix("http://")}/arena/ws"
            else -> "$baseUrl/arena/ws"
        }
    }
}

data class ArenaPlayerProfile(
    val userId: String,
    val name: String,
    val rating: Int,
    val languages: List<String>,
    val avatarId: String?,
) {
    fun toFindMatchJson(): JSONObject {
        return JSONObject()
            .put("type", "find_match")
            .put("userId", userId)
            .put("name", name)
            .put("rating", rating)
            .put("languages", JSONArray(languages))
            .put("avatarId", avatarId)
    }
}

sealed class ArenaEvent {
    data class MatchmakingStarted(val timeoutMs: Long) : ArenaEvent()
    data class MatchFound(
        val matchId: String,
        val players: List<ArenaRemotePlayer>,
        val questionCount: Int,
    ) : ArenaEvent()
    data class Question(
        val matchId: String,
        val questionIndex: Int,
        val questionCount: Int,
        val timeLimitMs: Long,
        val question: ArenaRemoteQuestion,
    ) : ArenaEvent()
    data class AnswerResult(
        val playerId: String,
        val correct: Boolean,
        val elapsedMs: Long,
        val delta: Int,
        val scores: Map<String, Int>,
    ) : ArenaEvent()
    data class QuestionFinished(
        val correctIndex: Int,
        val scores: Map<String, Int>,
    ) : ArenaEvent()
    data class MatchFinished(
        val winnerId: String?,
        val scores: Map<String, Int>,
        val forfeitBy: String?,
    ) : ArenaEvent()
    data class Error(val message: String) : ArenaEvent()
    data object MatchmakingCancelled : ArenaEvent()

    companion object {
        fun fromJson(json: JSONObject): ArenaEvent {
            return when (val type = json.optString("type")) {
                "matchmaking_started" -> MatchmakingStarted(json.optLong("timeoutMs"))
                "match_found" -> MatchFound(
                    matchId = json.getString("matchId"),
                    players = json.getJSONArray("players").toObjectList { ArenaRemotePlayer.fromJson(it) },
                    questionCount = json.getInt("questionCount"),
                )
                "question" -> Question(
                    matchId = json.getString("matchId"),
                    questionIndex = json.getInt("questionIndex"),
                    questionCount = json.getInt("questionCount"),
                    timeLimitMs = json.getLong("timeLimitMs"),
                    question = ArenaRemoteQuestion.fromJson(json.getJSONObject("question")),
                )
                "answer_result" -> AnswerResult(
                    playerId = json.getString("playerId"),
                    correct = json.getBoolean("correct"),
                    elapsedMs = json.getLong("elapsedMs"),
                    delta = json.getInt("delta"),
                    scores = json.getJSONObject("scores").toIntMap(),
                )
                "question_finished" -> QuestionFinished(
                    correctIndex = json.getInt("correctIndex"),
                    scores = json.getJSONObject("scores").toIntMap(),
                )
                "match_finished" -> MatchFinished(
                    winnerId = json.optString("winnerId").takeIf { it.isNotBlank() },
                    scores = json.getJSONObject("scores").toIntMap(),
                    forfeitBy = json.optString("forfeitBy").takeIf { it.isNotBlank() },
                )
                "matchmaking_cancelled" -> MatchmakingCancelled
                "error" -> Error(json.optString("message", "Arena error"))
                else -> Error("Unknown arena event: $type")
            }
        }
    }
}

data class ArenaRemotePlayer(
    val id: String,
    val name: String,
    val rating: Int,
    val languages: List<String>,
    val avatarId: String?,
    val isBot: Boolean,
) {
    companion object {
        fun fromJson(json: JSONObject): ArenaRemotePlayer {
            return ArenaRemotePlayer(
                id = json.getString("id"),
                name = json.getString("name"),
                rating = json.getInt("rating"),
                languages = json.optJSONArray("languages")?.toStringList().orEmpty(),
                avatarId = json.optString("avatarId").takeIf { it.isNotBlank() },
                isBot = json.optBoolean("isBot"),
            )
        }
    }
}

data class ArenaRemoteQuestion(
    val id: String,
    val language: String,
    val course: String,
    val prompt: String,
    val options: List<String>,
) {
    companion object {
        fun fromJson(json: JSONObject): ArenaRemoteQuestion {
            return ArenaRemoteQuestion(
                id = json.getString("id"),
                language = json.getString("language"),
                course = json.getString("course"),
                prompt = json.getString("prompt"),
                options = json.getJSONArray("options").toStringList(),
            )
        }
    }
}

private fun JSONArray.toStringList(): List<String> {
    return (0 until length()).map { index -> getString(index) }
}

private fun <T> JSONArray.toObjectList(transform: (JSONObject) -> T): List<T> {
    return (0 until length()).map { index -> transform(getJSONObject(index)) }
}

private fun JSONObject.toIntMap(): Map<String, Int> {
    return keys().asSequence().associateWith { key -> getInt(key) }
}
