package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.user_data.database.GrammarPointData
import ua.syt0r.kanji.presentation.common.ScreenGrammarPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration
import kotlin.time.Duration

interface GrammarPracticeScreenContract {

    interface ViewModel {

        val state: StateFlow<ScreenState>

        fun initialize(configuration: GrammarPracticeScreenConfiguration)

        /** Read mode: show the answer side of the card */
        fun reveal()

        /** Read mode: grade the card (Again / Hard / Good / Easy) */
        fun submitGrade(answer: PracticeAnswer)

        /** Quiz mode: pick one of the options */
        fun selectQuizOption(index: Int)

        /** Test mode: check typed answer */
        fun submitTestInput(input: String)

        /** Quiz / Test mode: continue after the result was shown */
        fun next()

        fun finishPractice()

    }

    sealed interface ScreenState {

        data object Loading : ScreenState

        data object Empty : ScreenState

        data class Review(
            val point: GrammarPointData,
            val practiceType: ScreenGrammarPracticeType,
            val progress: PracticeQueueProgress,
            val answers: PracticeAnswers,
            val step: ReviewStep
        ) : ScreenState

        data class Summary(
            val practiceDuration: Duration,
            val reviewsCount: Int,
            val items: List<SummaryItem>
        ) : ScreenState

    }

    sealed interface ReviewStep {

        data class Read(val revealed: Boolean) : ReviewStep

        data object QuizQuestion : ReviewStep

        data class QuizResult(
            val selectedIndex: Int,
            val isCorrect: Boolean
        ) : ReviewStep

        data object TestInput : ReviewStep

        data class TestResult(
            val input: String,
            val isCorrect: Boolean
        ) : ReviewStep

    }

    data class SummaryItem(
        val title: String,
        val reviews: Int,
        val nextInterval: Duration
    )

}
