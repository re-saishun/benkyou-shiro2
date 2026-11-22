package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.VocabCardResolver
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.srs.SrsCardRepository
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesNewCardsOrder
import ua.syt0r.kanji.core.user_data.preferences.PreferencesWritingHintMode
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationCardsSelectorState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeHintMode
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.toScreenType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.MutableVocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.SelectedReadingAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeQueueDataUseCase
import kotlin.math.roundToInt

class VocabPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val practicePreferences: PreferencesContract.PracticePreferences,
    private val srsCardRepository: SrsCardRepository,
    private val vocabCardResolver: VocabCardResolver,
    private val getQueueDataUseCase: GetVocabPracticeQueueDataUseCase,
    private val practiceQueue: VocabPracticeQueue,
    private val analyticsManager: AnalyticsManager
) : VocabPracticeScreenContract.ViewModel {

    private lateinit var configuration: VocabPracticeScreenConfiguration

    private lateinit var _reviewState: MutableState<VocabPracticeQueueState.Review>
    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)

    override val state: StateFlow<ScreenState>
        get() = _state

    override fun initialize(configuration: VocabPracticeScreenConfiguration) {
        if (this::configuration.isInitialized) return
        this.configuration = configuration

        viewModelScope.launch {

            val selectorState = PracticeConfigurationCardsSelectorState(
                cardsCount = configuration.cards.size,
                shuffle = mutableStateOf(practicePreferences.shuffle.get()),
                newCardsOrder = mutableStateOf(practicePreferences.newCardsOrder.get())
            )

            val cardsCache = vocabCardResolver.cardsCache.await()
            val srsCache = srsCardRepository.getAll()

            val orderedPreviewCards = configuration.cards.map { card ->
                val srsKey = configuration.practiceType.dataType.toSrsKey(card.cardId)
                val isNew = srsCache.contains(srsKey).not()
                val displayWord = cardsCache[card.cardId]?.data?.run { kanjiReading ?: kanaReading } ?: ""
                isNew to displayWord
            }
            val newWords = orderedPreviewCards.filter { it.first }.map { it.second }
            val nonNewWords = orderedPreviewCards.filterNot { it.first }.map { it.second }
            val allWords = orderedPreviewCards.map { it.second }

            val unfilteredWordsPreview = derivedStateOf {
                val shuffle = selectorState.shuffle.value
                fun List<String>.withShuffleApplied(): List<String> = if (shuffle) shuffled() else this

                when (selectorState.newCardsOrder.value) {
                    PreferencesNewCardsOrder.First -> newWords.withShuffleApplied() + nonNewWords.withShuffleApplied()
                    PreferencesNewCardsOrder.Last -> nonNewWords.withShuffleApplied() + newWords.withShuffleApplied()
                    PreferencesNewCardsOrder.Mixed -> allWords.withShuffleApplied()
                }
            }

            _state.value = ScreenState.Configuration(
                practiceType = configuration.practiceType,
                cardsSelectorState = selectorState,
                flashcard = VocabPracticeConfiguration.Flashcard(
                    translationInFront = mutableStateOf(
                        practicePreferences.vocabFlashcardMeaningInFront.get()
                    )
                ),
                readingPicker = VocabPracticeConfiguration.ReadingPicker(
                    showMeaning = mutableStateOf(
                        practicePreferences.vocabReadingPickerShowMeaning.get()
                    )
                ),
                writing = VocabPracticeConfiguration.Writing(
                    unfilteredWordsPreview = unfilteredWordsPreview,
                    hintMode = mutableStateOf(
                        practicePreferences.vocabWritingHintMode.get().toWritingHintMode()
                    ),
                    inputMode = mutableStateOf(
                        practicePreferences.writingInputMethod.get().toScreenType()
                    ),
                    showKanaReading = mutableStateOf(
                        practicePreferences.vocabWritingShowKanaReading.get()
                    ),
                    noTranslationsLayout = mutableStateOf(
                        practicePreferences.noTranslationLayout.get()
                    ),
                    leftHandedMode = mutableStateOf(
                        practicePreferences.leftHandMode.get()
                    ),
                    altStrokeEvaluatorEnabled = mutableStateOf(
                        practicePreferences.altStrokeEvaluator.get()
                    )
                )
            )
        }
    }

    override fun configure() {
        val configurationState = _state.value as? ScreenState.Configuration ?: return
        _state.value = ScreenState.Loading

        viewModelScope.launch {
            practicePreferences.apply {
                shuffle.set(configurationState.cardsSelectorState.shuffle.value)
                newCardsOrder.set(configurationState.cardsSelectorState.newCardsOrder.value)
                vocabReadingPickerShowMeaning.set(configurationState.readingPicker.showMeaning.value)
                vocabFlashcardMeaningInFront.set(configurationState.flashcard.translationInFront.value)
                vocabWritingShowKanaReading.set(configurationState.writing.showKanaReading.value)
                vocabWritingHintMode.set(configurationState.writing.hintMode.value.toPrefType())
                writingInputMethod.set(configurationState.writing.inputMode.value.repoType)
                noTranslationLayout.set(configurationState.writing.noTranslationsLayout.value)
                leftHandMode.set(configurationState.writing.leftHandedMode.value)
                altStrokeEvaluator.set(configurationState.writing.altStrokeEvaluatorEnabled.value)
            }

            practiceQueue.initialize(
                items = getQueueDataUseCase(configuration, configurationState)
            )

            practiceQueue.state
                .onEach { applyToScreenState(it) }
                .launchIn(viewModelScope)

            analyticsManager.sendEvent("vocab_practice_configuration") {
                put("practice_type", configuration.practiceType.dataType.srsPracticeType.value)
            }
        }
    }

    override fun revealFlashcard() {
        val currentState = _reviewState.value.state as MutableVocabReviewState.Flashcard
        currentState.showAnswer.value = true
    }

    override fun submitReadingPickerAnswer(answer: String) {
        val currentState = _reviewState.value.state as MutableVocabReviewState.Reading
        currentState.apply {
            displayReading.value = currentState.revealedReading
            selectedAnswer.value = SelectedReadingAnswer(answer, currentState.correctAnswer)
        }
    }

    override fun next(answer: PracticeAnswer) {
        viewModelScope.launch { practiceQueue.submitAnswer(answer) }
    }

    override fun finishPractice() {
        practiceQueue.immediateFinish()
    }

    private fun applyToScreenState(queueState: VocabPracticeQueueState) {
        when (queueState) {
            VocabPracticeQueueState.Loading -> {
                _state.value = ScreenState.Loading
            }

            is VocabPracticeQueueState.Review -> {
                if (::_reviewState.isInitialized.not()) {
                    _reviewState = mutableStateOf(queueState)
                } else {
                    _reviewState.value = queueState
                }

                if (_state.value !is ScreenState.Review) {
                    _state.value = ScreenState.Review(
                        state = derivedStateOf { _reviewState.value.toPracticeReviewState() }
                    )
                }
            }

            is VocabPracticeQueueState.Summary -> {
                val accuracy = queueState.items
                    .takeIf { it.isNotEmpty() }
                    ?.let { items ->
                        val totalUnits = items.sumOf { it.totalUnits }
                        val totalMistakes = items.sumOf { it.mistakes }
                        if (totalUnits <= 0) null
                        else {
                            val correctUnits = (totalUnits - totalMistakes).coerceAtLeast(0)
                            (correctUnits * 1000.0 / totalUnits).roundToInt() / 10f
                        }
                    }

                _state.value = ScreenState.Summary(
                    practiceDuration = queueState.duration,
                    accuracy = accuracy,
                    results = queueState.items
                )
            }
        }
    }

    private fun VocabPracticeQueueState.Review.toPracticeReviewState(): VocabPracticeReviewState {
        return VocabPracticeReviewState(
            progress = progress,
            reviewState = state.asImmutable,
            answers = answers
        )
    }

    private fun PreferencesWritingHintMode.toWritingHintMode(): WritingPracticeHintMode = when (this) {
        PreferencesWritingHintMode.OnlyNew -> WritingPracticeHintMode.OnlyNew
        PreferencesWritingHintMode.All -> WritingPracticeHintMode.All
        PreferencesWritingHintMode.None -> WritingPracticeHintMode.None
    }

    private fun WritingPracticeHintMode.toPrefType(): PreferencesWritingHintMode = when (this) {
        WritingPracticeHintMode.OnlyNew -> PreferencesWritingHintMode.OnlyNew
        WritingPracticeHintMode.All -> PreferencesWritingHintMode.All
        WritingPracticeHintMode.None -> PreferencesWritingHintMode.None
    }

}