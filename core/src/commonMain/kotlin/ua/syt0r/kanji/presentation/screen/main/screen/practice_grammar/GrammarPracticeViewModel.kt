package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.SrsAnswer
import ua.syt0r.kanji.core.srs.SrsAnswers
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.SrsCardRepository
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.database.GrammarPoint
import ua.syt0r.kanji.core.user_data.database.GrammarPracticeRepository
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryItem
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import ua.syt0r.kanji.presentation.common.ScreenGrammarPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.GrammarPracticeScreenContract.ReviewStep
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.GrammarPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.GrammarPracticeScreenContract.SummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration
import kotlin.math.min
import kotlin.time.Duration.Companion.days

class GrammarPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val grammarRepository: GrammarPracticeRepository,
    private val srsCardRepository: SrsCardRepository,
    private val srsScheduler: SrsScheduler,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val timeUtils: TimeUtils
) : GrammarPracticeScreenContract.ViewModel {

    private class QueueItem(
        val point: GrammarPoint,
        val srsKey: SrsCardKey,
        val card: SrsCard,
        val repeats: Int = 0
    ) {
        fun copyAfterAnswer(answer: SrsAnswer) = QueueItem(
            point = point,
            srsKey = srsKey,
            card = answer.card,
            repeats = repeats + 1
        )
    }

    private class SummaryEntry(
        val title: String,
        val reviews: Int,
        val card: SrsCard
    )

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    private var initialized = false
    private lateinit var practiceType: ScreenGrammarPracticeType
    private lateinit var practiceStart: Instant
    private lateinit var reviewStart: Instant

    private val queue = mutableListOf<QueueItem>()
    private val summary = linkedMapOf<String, SummaryEntry>()
    private var totalReviews = 0
    private var currentAnswers: SrsAnswers? = null

    override fun initialize(configuration: GrammarPracticeScreenConfiguration) {
        if (initialized) return
        initialized = true
        practiceType = configuration.practiceType
        practiceStart = timeUtils.now()

        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) { loadQueue(configuration) }
            queue.addAll(items)
            if (queue.isEmpty()) _state.value = ScreenState.Empty
            else presentCurrent()
        }
    }

    private suspend fun loadQueue(
        configuration: GrammarPracticeScreenConfiguration
    ): List<QueueItem> {
        val pointsByKey = grammarRepository.getAllPoints().associateBy { it.itemKey }
        return configuration.cards.mapNotNull { card ->
            val point = pointsByKey[card.itemKey] ?: return@mapNotNull null
            // Quiz mode can't be done for points without quiz data
            if (practiceType == ScreenGrammarPracticeType.Quiz && point.data.quiz == null) {
                return@mapNotNull null
            }
            val key = practiceType.dataType.toSrsKey(point.itemKey)
            QueueItem(
                point = point,
                srsKey = key,
                card = srsCardRepository.get(key) ?: srsScheduler.newCard()
            )
        }
    }

    override fun reveal() {
        val review = _state.value as? ScreenState.Review ?: return
        val step = review.step as? ReviewStep.Read ?: return
        if (step.revealed) return
        _state.value = review.copy(step = ReviewStep.Read(revealed = true))
    }

    override fun submitGrade(answer: PracticeAnswer) {
        val review = _state.value as? ScreenState.Review ?: return
        val step = review.step as? ReviewStep.Read ?: return
        if (!step.revealed) return
        recordAnswer(answer.srsAnswer, mistakes = if (answer.srsAnswer == currentAnswers?.again) 1 else 0)
    }

    override fun selectQuizOption(index: Int) {
        val review = _state.value as? ScreenState.Review ?: return
        if (review.step != ReviewStep.QuizQuestion) return
        val quiz = review.point.quiz ?: return
        _state.value = review.copy(
            step = ReviewStep.QuizResult(
                selectedIndex = index,
                isCorrect = index == quiz.correctIndex
            )
        )
    }

    override fun submitTestInput(input: String) {
        val review = _state.value as? ScreenState.Review ?: return
        if (review.step != ReviewStep.TestInput) return
        val data = review.point
        val isCorrect = isTestAnswerCorrect(
            input = input,
            candidates = listOf(data.meaning, data.structure)
        )
        _state.value = review.copy(step = ReviewStep.TestResult(input, isCorrect))
    }

    override fun next() {
        val review = _state.value as? ScreenState.Review ?: return
        val answers = currentAnswers ?: return
        val isCorrect = when (val step = review.step) {
            is ReviewStep.QuizResult -> step.isCorrect
            is ReviewStep.TestResult -> step.isCorrect
            else -> return
        }
        if (isCorrect) recordAnswer(answers.good, mistakes = 0)
        else recordAnswer(answers.again, mistakes = 1)
    }

    override fun finishPractice() {
        _state.value = buildSummary()
    }

    private fun recordAnswer(answer: SrsAnswer, mistakes: Int) {
        val item = queue.removeFirstOrNull() ?: return
        val now = timeUtils.now()
        val duration = now - reviewStart
        val updated = item.copyAfterAnswer(answer)

        totalReviews++
        val title = item.point.data.title
        val previous = summary[item.srsKey.itemKey]
        summary[item.srsKey.itemKey] = SummaryEntry(
            title = title,
            reviews = (previous?.reviews ?: 0) + 1,
            card = answer.card
        )

        // Items that are still "short term" come back in this session
        if (answer.card.interval < 1.days) {
            val position = min(queue.size, REPEAT_QUEUE_POSITION)
            queue.add(position, updated)
        }

        // Persist outside of the UI flow
        val historyItem = ReviewHistoryItem(
            key = item.srsKey.itemKey,
            practiceType = item.srsKey.practiceType,
            timestamp = reviewStart,
            duration = duration,
            grade = answer.grade,
            mistakes = mistakes,
            deckId = item.point.deckId
        )
        viewModelScope.launch(Dispatchers.IO) {
            srsCardRepository.update(item.srsKey, answer.card)
            reviewHistoryRepository.addReview(historyItem)
        }

        presentCurrent()
    }

    private fun presentCurrent() {
        val item = queue.firstOrNull()
        if (item == null) {
            _state.value = buildSummary()
            return
        }

        val now = timeUtils.now()
        val answers = srsScheduler.answers(item.card, now)
        currentAnswers = answers
        reviewStart = now

        _state.value = ScreenState.Review(
            point = item.point.data,
            practiceType = practiceType,
            progress = PracticeQueueProgress(
                pending = queue.count { it.repeats == 0 },
                repeats = queue.count { it.repeats > 0 },
                completed = summary.values.count { it.card.interval >= 1.days }
            ),
            answers = PracticeAnswers(
                again = PracticeAnswer(answers.again),
                hard = PracticeAnswer(answers.hard),
                good = PracticeAnswer(answers.good),
                easy = PracticeAnswer(answers.easy)
            ),
            step = when (practiceType) {
                ScreenGrammarPracticeType.Read -> ReviewStep.Read(revealed = false)
                ScreenGrammarPracticeType.Quiz -> ReviewStep.QuizQuestion
                ScreenGrammarPracticeType.Test -> ReviewStep.TestInput
            }
        )
    }

    private fun buildSummary(): ScreenState.Summary {
        return ScreenState.Summary(
            practiceDuration = timeUtils.now() - practiceStart,
            reviewsCount = totalReviews,
            items = summary.values.map {
                SummaryItem(
                    title = it.title,
                    reviews = it.reviews,
                    nextInterval = it.card.interval
                )
            }
        )
    }

    companion object {

        private const val REPEAT_QUEUE_POSITION = 3

        private val IgnoredCharacters = Regex("[\\s.,;:!?'\"()\\[\\]{}\\-_+*/\\\\|<>=~`@#$%^&、。・「」『』（）〜～！？，．]")
        private val AlternativesSeparator = Regex("[;/／、,]")

        /**
         * Typed answer is correct if it matches (ignoring case, spaces and punctuation)
         * one of the candidates or one of the candidate's alternatives separated by ; / , 、
         */
        internal fun isTestAnswerCorrect(input: String, candidates: List<String>): Boolean {
            val normalizedInput = input.normalizeForComparison()
            if (normalizedInput.isEmpty()) return false
            return candidates.any { candidate ->
                val alternatives = candidate.split(AlternativesSeparator) + candidate
                alternatives.any { it.normalizeForComparison() == normalizedInput }
            }
        }

        private fun String.normalizeForComparison(): String {
            return lowercase().replace(IgnoredCharacters, "")
        }

    }

}
