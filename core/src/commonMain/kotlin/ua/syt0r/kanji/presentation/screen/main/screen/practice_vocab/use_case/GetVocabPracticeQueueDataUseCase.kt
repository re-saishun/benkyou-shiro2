package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.srs.SrsCardRepository
import ua.syt0r.kanji.core.stroke_evaluator.AltKanjiStrokeEvaluator
import ua.syt0r.kanji.core.stroke_evaluator.DefaultKanjiStrokeEvaluator
import ua.syt0r.kanji.core.user_data.preferences.PreferencesNewCardsOrder
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeHintMode
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeCardConfigurationData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration

interface GetVocabPracticeQueueDataUseCase {
    suspend operator fun invoke(
        configuration: VocabPracticeScreenConfiguration,
        configuratedState: ScreenState.Configuration
    ): List<VocabPracticeQueueItemDescriptor>
}

class DefaultGetVocabPracticeQueueDataUseCase(
    private val srsCardRepository: SrsCardRepository
) : GetVocabPracticeQueueDataUseCase {

    override suspend fun invoke(
        configuration: VocabPracticeScreenConfiguration,
        configuratedState: ScreenState.Configuration
    ): List<VocabPracticeQueueItemDescriptor> {
        val evaluator = if (configuratedState.writing.altStrokeEvaluatorEnabled.value) {
            AltKanjiStrokeEvaluator()
        } else {
            DefaultKanjiStrokeEvaluator()
        }

        return configuration.cards
            .let {
                val srsCardCache = srsCardRepository.getAll()
                val defaultCardsOrder = configuration.cards.map {
                    val srsKey = configuration.practiceType.dataType.toSrsKey(it.cardId)
                    VocabPracticeCardConfigurationData(
                        cardId = it.cardId,
                        deckId = it.deckId,
                        srsCardKey = srsKey,
                        isNew = srsCardCache.contains(srsKey).not()
                    )
                }

                val newCards = defaultCardsOrder.filter { it.isNew }
                val nonNewCards = defaultCardsOrder.filterNot { it.isNew }

                val selectorState = configuratedState.cardsSelectorState
                val shuffle = selectorState.shuffle.value
                val newCardsOrder = selectorState.newCardsOrder.value

                fun <T> List<T>.withShuffleApplied(): List<T> =
                    if (shuffle) shuffled() else this

                when (newCardsOrder) {
                    PreferencesNewCardsOrder.First -> newCards.withShuffleApplied()
                        .plus(nonNewCards.withShuffleApplied())

                    PreferencesNewCardsOrder.Last -> nonNewCards.withShuffleApplied()
                        .plus(newCards.withShuffleApplied())

                    PreferencesNewCardsOrder.Mixed -> defaultCardsOrder.withShuffleApplied()
                }
            }
            .take(configuratedState.cardsSelectorState.selectedCountIntState.value)
            .asSequence()
            .map { card ->
                when (configuratedState.practiceType) {
                    ScreenVocabPracticeType.Flashcard -> {
                        VocabPracticeQueueItemDescriptor.Flashcard(
                            cardId = card.cardId,
                            deckId = card.deckId,
                            translationInFont = configuratedState.flashcard.translationInFront.value
                        )
                    }

                    ScreenVocabPracticeType.ReadingPicker -> {
                        VocabPracticeQueueItemDescriptor.ReadingPicker(
                            cardId = card.cardId,
                            deckId = card.deckId,
                            showMeaning = configuratedState.readingPicker.showMeaning.value
                        )
                    }

                    ScreenVocabPracticeType.Writing -> {
                        val shouldStudy = when (configuratedState.writing.hintMode.value) {
                            WritingPracticeHintMode.OnlyNew -> card.isNew
                            WritingPracticeHintMode.All -> true
                            WritingPracticeHintMode.None -> false
                        }
                        VocabPracticeQueueItemDescriptor.Writing(
                            cardId = card.cardId,
                            deckId = card.deckId,
                            showKanaReading = configuratedState.writing.showKanaReading.value,
                            shouldStudy = shouldStudy,
                            inputMode = configuratedState.writing.inputMode.value,
                            evaluator = evaluator,
                            noTranslationsLayout = configuratedState.writing.noTranslationsLayout.value,
                            leftHandedMode = configuratedState.writing.leftHandedMode.value
                        )
                    }
                }
            }
            .toList()
    }

}