import asyncio
import random
import time
import uuid
from dataclasses import dataclass, field
from typing import Any, Optional

from fastapi import WebSocket, WebSocketDisconnect


QUESTION_TIME_MS = 12_000
CORRECT_ANSWER_POINTS = 100
WRONG_ANSWER_PENALTY = -35
TIMEOUT_PENALTY = -50


ARENA_QUESTIONS = [
    {
        "id": "java-vars-post-inc",
        "language": "Java",
        "course": "Variables",
        "prompt": "What is the output?\nint x = 4;\nSystem.out.println(x++);",
        "options": ["4", "5", "3", "Compilation error"],
        "correctIndex": 0,
    },
    {
        "id": "java-string-plus",
        "language": "Java",
        "course": "Strings",
        "prompt": "What is printed?\nString s = \"Go\";\nSystem.out.println(s + 2 + 3);",
        "options": ["Go5", "Go23", "5Go", "Compilation error"],
        "correctIndex": 1,
    },
    {
        "id": "java-loop-count",
        "language": "Java",
        "course": "Loops",
        "prompt": "How many times does this loop run?\nfor (int i = 0; i < 3; i++)",
        "options": ["2", "3", "4", "Infinite"],
        "correctIndex": 1,
    },
    {
        "id": "java-array-index",
        "language": "Java",
        "course": "Arrays",
        "prompt": "What is printed?\nString[] names = {\"Leo\", \"Maya\", \"Dan\"};\nSystem.out.println(names[1]);",
        "options": ["Leo", "Maya", "Dan", "1"],
        "correctIndex": 1,
    },
    {
        "id": "python-floor-div",
        "language": "Python",
        "course": "Operators",
        "prompt": "What is printed?\nprint(7 // 2)",
        "options": ["3.5", "4", "3", "2"],
        "correctIndex": 2,
    },
    {
        "id": "c-array-index",
        "language": "C",
        "course": "Arrays",
        "prompt": "What is the first index in a C array?",
        "options": ["0", "1", "-1", "Depends on compiler"],
        "correctIndex": 0,
    },
]


@dataclass
class ArenaPlayer:
    id: str
    name: str
    rating: int
    languages: list[str]
    avatar_id: str | None
    websocket: Optional[WebSocket]
    is_bot: bool = False
    skill: int = 82

    def public(self) -> dict[str, Any]:
        return {
            "id": self.id,
            "name": self.name,
            "rating": self.rating,
            "languages": self.languages,
            "avatarId": self.avatar_id,
            "isBot": self.is_bot,
        }


@dataclass
class ArenaMatch:
    id: str
    players: list[ArenaPlayer]
    questions: list[dict[str, Any]]
    question_index: int = 0
    scores: dict[str, int] = field(default_factory=dict)
    streaks: dict[str, int] = field(default_factory=dict)
    answers: dict[str, dict[str, Any]] = field(default_factory=dict)
    question_started_at: float = field(default_factory=time.monotonic)
    tasks: list[asyncio.Task] = field(default_factory=list)


class ArenaManager:
    def __init__(self) -> None:
        self.waiting: ArenaPlayer | None = None
        self.waiting_timeout_task: asyncio.Task | None = None
        self.matches: dict[str, ArenaMatch] = {}
        self.player_matches: dict[str, str] = {}
        self.lock = asyncio.Lock()

    async def connect(self, websocket: WebSocket) -> None:
        await websocket.accept()
        player: ArenaPlayer | None = None

        try:
            hello = await websocket.receive_json()
            if hello.get("type") != "find_match":
                await websocket.send_json({"type": "error", "message": "Expected find_match"})
                return

            player = ArenaPlayer(
                id=str(hello.get("userId") or uuid.uuid4()),
                name=str(hello.get("name") or "Player"),
                rating=int(hello.get("rating") or 1000),
                languages=self._normalize_languages(hello.get("languages")),
                avatar_id=hello.get("avatarId"),
                websocket=websocket,
            )
            await self.enqueue(player)

            while True:
                message = await websocket.receive_json()
                if message.get("type") == "answer":
                    await self.submit_answer(player.id, message)
                elif message.get("type") == "cancel_matchmaking":
                    await self.remove_player(player.id)
                    await websocket.send_json({"type": "matchmaking_cancelled"})
                elif message.get("type") == "forfeit":
                    await self.forfeit(player.id)

        except WebSocketDisconnect:
            if player is not None:
                await self.remove_player(player.id)

    async def enqueue(self, player: ArenaPlayer) -> None:
        async with self.lock:
            if self.waiting is None or self.waiting.id == player.id:
                self.waiting = player
                self._cancel_waiting_timeout()
                self.waiting_timeout_task = asyncio.create_task(self._match_with_bot_after_timeout(player.id))
                await player.websocket.send_json({"type": "matchmaking_started", "timeoutMs": 30_000})
                return

            opponent = self.waiting
            self.waiting = None
            self._cancel_waiting_timeout()

        await self.start_match(opponent, player)

    async def _match_with_bot_after_timeout(self, waiting_player_id: str) -> None:
        await asyncio.sleep(30)
        async with self.lock:
            if self.waiting is None or self.waiting.id != waiting_player_id:
                return
            player = self.waiting
            self.waiting = None

        await self.start_match(player, self._create_bot_for(player))

    async def start_match(self, first: ArenaPlayer, second: ArenaPlayer) -> None:
        questions = self._select_questions(first.languages, second.languages)
        match = ArenaMatch(
            id=str(uuid.uuid4()),
            players=[first, second],
            questions=questions,
            scores={first.id: 0, second.id: 0},
            streaks={first.id: 0, second.id: 0},
        )
        self.matches[match.id] = match
        self.player_matches[first.id] = match.id
        self.player_matches[second.id] = match.id

        await self.broadcast(match, {
            "type": "match_found",
            "matchId": match.id,
            "players": [first.public(), second.public()],
            "questionCount": len(questions),
        })
        await self.send_question(match)

    async def send_question(self, match: ArenaMatch) -> None:
        self._cancel_match_tasks(match)
        match.answers.clear()
        match.question_started_at = time.monotonic()
        question = match.questions[match.question_index]
        public_question = {key: value for key, value in question.items() if key != "correctIndex"}
        await self.broadcast(match, {
            "type": "question",
            "matchId": match.id,
            "questionIndex": match.question_index,
            "questionCount": len(match.questions),
            "timeLimitMs": QUESTION_TIME_MS,
            "question": public_question,
        })
        for player in match.players:
            if player.is_bot:
                match.tasks.append(asyncio.create_task(self._answer_as_bot(match.id, player.id)))
        match.tasks.append(asyncio.create_task(self._timeout_unanswered(match.id, match.question_index)))

    async def submit_answer(self, player_id: str, message: dict[str, Any]) -> None:
        match = self._match_for_player(player_id)
        if match is None or player_id in match.answers:
            return

        await self._record_answer(player_id, int(message.get("selectedIndex", -1)))

    async def _record_answer(self, player_id: str, selected_index: int) -> None:
        match = self._match_for_player(player_id)
        if match is None or player_id in match.answers:
            return

        elapsed_ms = int((time.monotonic() - match.question_started_at) * 1000)
        question = match.questions[match.question_index]
        timed_out = elapsed_ms > QUESTION_TIME_MS
        correct = (not timed_out) and selected_index == question["correctIndex"]
        delta = self._score_answer(player_id, match, correct, elapsed_ms, timed_out)

        match.answers[player_id] = {
            "selectedIndex": selected_index,
            "correct": correct,
            "elapsedMs": elapsed_ms,
            "delta": delta,
        }

        await self.broadcast(match, {
            "type": "answer_result",
            "matchId": match.id,
            "playerId": player_id,
            "correct": correct,
            "elapsedMs": elapsed_ms,
            "delta": delta,
            "scores": match.scores,
        })

        if len(match.answers) == len(match.players):
            await self.advance_or_finish(match)

    async def _answer_as_bot(self, match_id: str, bot_id: str) -> None:
        match = self.matches.get(match_id)
        bot = self._player_in_match(match, bot_id)
        if match is None or bot is None:
            return

        delay_ms = self._bot_reaction_ms(bot, match.question_index)
        await asyncio.sleep(delay_ms / 1000)
        match = self.matches.get(match_id)
        bot = self._player_in_match(match, bot_id)
        if match is None or bot is None or bot_id in match.answers:
            return

        question = match.questions[match.question_index]
        accuracy = min(max(bot.skill + random.randint(-7, 5), 55), 92) / 100
        if random.random() <= accuracy:
            selected_index = question["correctIndex"]
        else:
            wrong_options = [index for index in range(len(question["options"])) if index != question["correctIndex"]]
            selected_index = random.choice(wrong_options)
        await self._record_answer(bot_id, selected_index)

    async def _timeout_unanswered(self, match_id: str, question_index: int) -> None:
        await asyncio.sleep((QUESTION_TIME_MS + 700) / 1000)
        match = self.matches.get(match_id)
        if match is None or match.question_index != question_index:
            return
        missing_player_ids = [player.id for player in match.players if player.id not in match.answers]
        for player_id in missing_player_ids:
            await self._record_answer(player_id, -1)

    async def advance_or_finish(self, match: ArenaMatch) -> None:
        await self.broadcast(match, {
            "type": "question_finished",
            "matchId": match.id,
            "correctIndex": match.questions[match.question_index]["correctIndex"],
            "scores": match.scores,
        })
        match.question_index += 1
        if match.question_index >= len(match.questions):
            await self.finish_match(match)
            return

        await asyncio.sleep(1.2)
        await self.send_question(match)

    async def finish_match(self, match: ArenaMatch) -> None:
        self._cancel_match_tasks(match)
        winner_id = max(match.scores, key=match.scores.get)
        if len(set(match.scores.values())) == 1:
            winner_id = None
        await self.broadcast(match, {
            "type": "match_finished",
            "matchId": match.id,
            "winnerId": winner_id,
            "scores": match.scores,
        })
        self._remove_match(match.id)

    async def forfeit(self, player_id: str) -> None:
        match = self._match_for_player(player_id)
        if match is None:
            await self.remove_player(player_id)
            return

        winner = next((player for player in match.players if player.id != player_id), None)
        await self.broadcast(match, {
            "type": "match_finished",
            "matchId": match.id,
            "winnerId": winner.id if winner else None,
            "forfeitBy": player_id,
            "scores": match.scores,
        })
        self._remove_match(match.id)

    async def remove_player(self, player_id: str) -> None:
        async with self.lock:
            if self.waiting and self.waiting.id == player_id:
                self.waiting = None
                self._cancel_waiting_timeout()

        match = self._match_for_player(player_id)
        if match is not None:
            await self.forfeit(player_id)

    async def broadcast(self, match: ArenaMatch, payload: dict[str, Any]) -> None:
        for player in match.players:
            if player.websocket is not None:
                await player.websocket.send_json(payload)

    def _score_answer(
        self,
        player_id: str,
        match: ArenaMatch,
        correct: bool,
        elapsed_ms: int,
        timed_out: bool,
    ) -> int:
        if not correct:
            delta = TIMEOUT_PENALTY if timed_out else WRONG_ANSWER_PENALTY
            match.streaks[player_id] = 0
        else:
            streak = match.streaks[player_id] + 1
            speed_bonus = min(max((QUESTION_TIME_MS - elapsed_ms) // 180, 0), 60)
            streak_bonus = min(streak * 8, 32)
            delta = CORRECT_ANSWER_POINTS + speed_bonus + streak_bonus
            match.streaks[player_id] = streak
        match.scores[player_id] += int(delta)
        return int(delta)

    def _select_questions(self, first_languages: list[str], second_languages: list[str]) -> list[dict[str, Any]]:
        shared = {lang.lower() for lang in first_languages} & {lang.lower() for lang in second_languages}
        pool = [q for q in ARENA_QUESTIONS if q["language"].lower() in shared]
        if len(pool) < 5:
            pool = ARENA_QUESTIONS
        return pool[:5]

    def _normalize_languages(self, value: Any) -> list[str]:
        if not isinstance(value, list):
            return ["Java"]
        languages = [str(item).strip() for item in value if str(item).strip()]
        return languages or ["Java"]

    def _create_bot_for(self, player: ArenaPlayer) -> ArenaPlayer:
        bot_names = ["ByteRunner", "StackQueen", "LoopMage", "AlgoNinja", "NullPointer"]
        bot_rating = player.rating + random.randint(-90, 130)
        return ArenaPlayer(
            id=f"bot-{uuid.uuid4()}",
            name=random.choice(bot_names),
            rating=max(bot_rating, 700),
            languages=player.languages or ["Java"],
            avatar_id=random.choice(["robot", "ninja", "owl", "alien"]),
            websocket=None,
            is_bot=True,
            skill=random.randint(76, 88),
        )

    def _bot_reaction_ms(self, bot: ArenaPlayer, question_index: int) -> int:
        base = random.randint(3_000, 8_600)
        if bot.skill >= 85:
            base -= random.randint(350, 1_200)
        if question_index == 0:
            base += random.randint(350, 900)
        return max(1_900, min(base, 10_500))

    def _player_in_match(self, match: ArenaMatch | None, player_id: str) -> ArenaPlayer | None:
        if match is None:
            return None
        return next((player for player in match.players if player.id == player_id), None)

    def _cancel_waiting_timeout(self) -> None:
        if self.waiting_timeout_task is not None:
            self.waiting_timeout_task.cancel()
            self.waiting_timeout_task = None

    def _cancel_match_tasks(self, match: ArenaMatch) -> None:
        current_task = asyncio.current_task()
        remaining_tasks = []
        for task in match.tasks:
            if task is current_task:
                remaining_tasks.append(task)
            else:
                task.cancel()
        match.tasks = remaining_tasks

    def _match_for_player(self, player_id: str) -> ArenaMatch | None:
        match_id = self.player_matches.get(player_id)
        if not match_id:
            return None
        return self.matches.get(match_id)

    def _remove_match(self, match_id: str) -> None:
        match = self.matches.pop(match_id, None)
        if match is None:
            return
        for player in match.players:
            self.player_matches.pop(player.id, None)


arena_manager = ArenaManager()
