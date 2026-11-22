package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationCard
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeHintMode
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeInputMode
import kotlin.time.Duration

@Serializable
data class VocabPracticeScreenConfiguration(
    val cards: List<Card>,
    val practiceType: ScreenVocabPracticeType
) {

    @Serializable
    data class Card(
        val cardId: Long,
        val deckId: Long
    ) : PracticeConfigurationCard

}

data class VocabPracticeCardConfigurationData(
    val cardId: Long,
    val deckId: Long,
    val srsCardKey: SrsCardKey,
    val isNew: Boolean
)

sealed interface VocabPracticeConfiguration {

    data class Flashcard(
        val translationInFront: MutableState<Boolean>
    ) : VocabPracticeConfiguration

    data class ReadingPicker(
        val showMeaning: MutableState<Boolean>
    ) : VocabPracticeConfiguration

    data class Writing(
        val unfilteredWordsPreview: State<List<String>>,
        val hintMode: MutableState<WritingPracticeHintMode>,
        val inputMode: MutableState<WritingPracticeInputMode>,
        val showKanaReading: MutableState<Boolean>,
        val noTranslationsLayout: MutableState<Boolean>,
        val leftHandedMode: MutableState<Boolean>,
        val altStrokeEvaluatorEnabled: MutableState<Boolean>
    ) : VocabPracticeConfiguration

}

sealed interface VocabReviewState {

    val meaning: String
    val vocabReference: InfoScreenData.Vocab

    interface Flashcard : VocabReviewState {
        val reading: FuriganaString
        val noFuriganaReading: FuriganaString
        val exampleSentence: VocabExampleSentence?
        val showMeaningInFront: Boolean
        val showAnswer: State<Boolean>
    }

    interface Reading : VocabReviewState {
        val questionCharacter: String
        val showMeaning: Boolean
        val displayReading: State<FuriganaString>
        val answers: List<String>
        val correctAnswer: String
        val selectedAnswer: State<SelectedReadingAnswer?>
    }

    interface Writing : VocabReviewState {
        val charactersData: List<VocabCharacterWritingData>
        val selected: MutableState<VocabCharacterWritingData>
        val kanaReading: String
        val showKanaReading: Boolean
        val noTranslationsLayout: Boolean
        val leftHandedMode: Boolean
        val isStudyMode: MutableState<Boolean>
        val characterDetails: Map<String, VocabCharacterDetails>
        val radicalsHighlight: MutableState<Boolean>
    }

}

sealed interface VocabCharacterDetails {
    val character: String
    val strokes: List<Path>

    data class Kanji(
        override val character: String,
        override val strokes: List<Path>,
        val radicals: List<CharacterRadical>,
        val on: List<String>,
        val kun: List<String>,
        val meanings: List<String>,
        val variants: String?
    ) : VocabCharacterDetails

    data class Kana(
        override val character: String,
        override val strokes: List<Path>,
        val kanaSystem: CharacterClassification.Kana,
        val reading: KanaReading
    ) : VocabCharacterDetails
}

sealed interface VocabCharacterWritingData {

    val character: String

    data class NoStrokes(
        override val character: String
    ) : VocabCharacterWritingData

    data class WithStrokes(
        override val character: String,
        val studyWriterState: CharacterWriterState?,
        val reviewWriterState: CharacterWriterState,
        private val isStudyMode: State<Boolean>
    ) : VocabCharacterWritingData {
        val writerState: State<CharacterWriterState> = derivedStateOf {
            if (isStudyMode.value && studyWriterState != null) studyWriterState else reviewWriterState
        }
    }

}

data class SelectedReadingAnswer(
    val selected: String,
    val correct: String
) {
    val isCorrect = selected == correct
}

data class VocabExampleSentence(
    val text: String,
    val furigana: FuriganaString,
    val translation: String
)

data class VocabPracticeReviewState(
    val progress: PracticeQueueProgress,
    val reviewState: VocabReviewState,
    val answers: PracticeAnswers
)

data class VocabSummaryItem(
    val reading: FuriganaString,
    val vocabReference: InfoScreenData.Vocab,
    override val totalReviews: Deferred<Int>,
    override val nextInterval: Duration,
    val totalUnits: Int,
    val mistakes: Int
) : PracticeSummaryItem
