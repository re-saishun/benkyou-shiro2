package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.practice_summary_header_accuracy
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.snapToBiggerContainerCrossfadeTransitionSpec
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationEnumSelector
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationItemsSelector
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationOption
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeEarlyFinishDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryEmptyList
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryInfoLabel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeToolbar
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeToolbarState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeHintMode
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeInputMode
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui.VocabPracticeFlashcardUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui.VocabPracticeReadingPickerUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui.VocabPracticeWritingUI

@Composable
fun VocabPracticeScreenUI(
    state: State<ScreenState>,
    onConfigured: () -> Unit,
    onFlashcardAnswerRevealClick: () -> Unit,
    onReadingPickerAnswerSelected: (String) -> Unit,
    onNext: (PracticeAnswer) -> Unit,
    onInfoClick: (VocabReviewState) -> Unit,
    onCharacterInfoClick: (String) -> Unit,
    onSummaryItemClick: (VocabSummaryItem) -> Unit,
    onFeedback: (JapaneseWord) -> Unit,
    navigateBack: () -> Unit,
    finishPractice: () -> Unit
) {

    var showPracticeFinishDialog by rememberSaveable { mutableStateOf(false) }
    if (showPracticeFinishDialog) {
        PracticeEarlyFinishDialog(
            onDismissRequest = { showPracticeFinishDialog = false },
            onConfirmClick = {
                finishPractice()
                showPracticeFinishDialog = false
            }
        )
    }

    val tryNavigateBack = {
        val isSafeToLeave = state.value.let {
            it is ScreenState.Configuration || it is ScreenState.Summary
        }

        if (isSafeToLeave) navigateBack()
        else showPracticeFinishDialog = true
    }

    MultiplatformBackHandler(onBack = tryNavigateBack)

    Scaffold(
        topBar = {
            PracticeToolbar(
                state = state.toPracticeToolbarState(),
                onUpButtonClick = tryNavigateBack
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.value,
            transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec(),
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {

            when (it) {
                ScreenState.Loading -> {
                    FancyLoading(Modifier.fillMaxSize().wrapContentSize())
                }

                is ScreenState.Configuration -> {
                    ScreenConfiguration(
                        screenState = it,
                        onConfigured = onConfigured
                    )
                }

                is ScreenState.Review -> {
                    ScreenReview(
                        screenState = it,
                        onFlashcardAnswerRevealClick = onFlashcardAnswerRevealClick,
                        onAnswerSelected = onReadingPickerAnswerSelected,
                        onNextClick = onNext,
                        onInfoClick = onInfoClick,
                        onCharacterInfoClick = onCharacterInfoClick,
                        onFeedbackClick = onFeedback
                    )
                }

                is ScreenState.Summary -> {
                    ScreenSummary(
                        screenState = it,
                        onVocabClick = onSummaryItemClick,
                        onFinishClick = navigateBack
                    )
                }
            }

        }

    }

}

@Composable
private fun State<ScreenState>.toPracticeToolbarState(): State<PracticeToolbarState> {
    return remember {
        derivedStateOf {
            when (val currentValue = value) {
                ScreenState.Loading,
                is ScreenState.Summary -> PracticeToolbarState.Idle

                is ScreenState.Configuration -> PracticeToolbarState.Configuration

                is ScreenState.Review -> PracticeToolbarState.Review(
                    practiceQueueProgress = currentValue.state.value.progress
                )
            }
        }
    }
}

@Composable
private fun ScreenConfiguration(
    screenState: ScreenState.Configuration,
    onConfigured: () -> Unit
) {

    val practiceTypeTitle = resolveString(screenState.practiceType.titleResolver)

    PracticeConfigurationContainer(
        onClick = onConfigured,
        practiceTypeMessage = resolveString { vocabPractice.configurationTitle(practiceTypeTitle) }
    ) {

        PracticeConfigurationItemsSelector(
            state = screenState.cardsSelectorState
        )

        when (screenState.practiceType) {
            ScreenVocabPracticeType.Flashcard -> {
                var translationInFront by screenState.flashcard.translationInFront
                PracticeConfigurationOption(
                    title = resolveString { vocabPractice.translationInFrontConfigurationTitle },
                    subtitle = resolveString { vocabPractice.translationInFrontConfigurationMessage },
                    checked = translationInFront,
                    onChange = { translationInFront = it }
                )
            }

            ScreenVocabPracticeType.ReadingPicker -> {
                var showMeaning by screenState.readingPicker.showMeaning
                PracticeConfigurationOption(
                    title = resolveString { vocabPractice.readingMeaningConfigurationTitle },
                    subtitle = resolveString { vocabPractice.readingMeaningConfigurationMessage },
                    checked = showMeaning,
                    onChange = { showMeaning = it }
                )
            }

            ScreenVocabPracticeType.Writing -> {
                val letterStrings = resolveString { letterPractice }
                val vocabStrings = resolveString { vocabPractice }

                var selectedHintMode by screenState.writing.hintMode
                var selectedInputMode by screenState.writing.inputMode
                var showKanaReading by screenState.writing.showKanaReading
                var noTranslationLayout by screenState.writing.noTranslationsLayout
                var leftHandedMode by screenState.writing.leftHandedMode
                var altStrokeEvaluatorEnabled by screenState.writing.altStrokeEvaluatorEnabled

                WordsPreview(
                    words = screenState.writing.unfilteredWordsPreview.value,
                    selectedCount = screenState.cardsSelectorState.selectedCountIntState
                )

                PracticeConfigurationEnumSelector(
                    title = letterStrings.hintStrokesTitle,
                    subtitle = vocabStrings.writingHintStrokesMessage,
                    values = WritingPracticeHintMode.entries,
                    selected = selectedHintMode,
                    onSelected = { selectedHintMode = it }
                )

                PracticeConfigurationEnumSelector(
                    title = letterStrings.inputModeTitle,
                    subtitle = vocabStrings.writingInputModeMessage,
                    values = WritingPracticeInputMode.entries,
                    selected = selectedInputMode,
                    onSelected = { selectedInputMode = it }
                )

                PracticeConfigurationOption(
                    title = vocabStrings.writingKanaReadingConfigurationTitle,
                    subtitle = vocabStrings.writingKanaReadingConfigurationMessage,
                    checked = showKanaReading,
                    onChange = { showKanaReading = it }
                )

                PracticeConfigurationOption(
                    title = letterStrings.noTranslationLayoutTitle,
                    subtitle = vocabStrings.writingNoTranslationLayoutMessage,
                    checked = noTranslationLayout,
                    onChange = { noTranslationLayout = it }
                )

                PracticeConfigurationOption(
                    title = letterStrings.leftHandedModeTitle,
                    subtitle = letterStrings.leftHandedModeMessage,
                    checked = leftHandedMode,
                    onChange = { leftHandedMode = it }
                )

                PracticeConfigurationOption(
                    title = letterStrings.altStrokeEvaluatorTitle,
                    subtitle = letterStrings.altStrokeEvaluatorMessage,
                    checked = altStrokeEvaluatorEnabled,
                    onChange = { altStrokeEvaluatorEnabled = it }
                )
            }
        }

    }

}

@Composable
private fun ColumnScope.WordsPreview(
    words: List<String>,
    selectedCount: State<Int>
) {
    var previewExpanded by remember { mutableStateOf(false) }

    Row(
        Modifier.clip(MaterialTheme.shapes.medium)
            .clickable(onClick = { previewExpanded = !previewExpanded })
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(start = 20.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = resolveString { vocabPractice.writingWordsPreviewTitle },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { previewExpanded = !previewExpanded }) {
            val icon = if (previewExpanded) Icons.Default.KeyboardArrowUp
            else Icons.Default.KeyboardArrowDown
            Icon(imageVector = icon, contentDescription = null)
        }
    }

    if (previewExpanded) {
        val count = selectedCount.value
        Text(
            text = buildAnnotatedString {
                words.forEachIndexed { index, word ->
                    if (index > 0) append("・")
                    if (index < count) {
                        append(word)
                    } else {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.surfaceVariant)) {
                            append(word)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ScreenReview(
    screenState: ScreenState.Review,
    onFlashcardAnswerRevealClick: () -> Unit,
    onAnswerSelected: (String) -> Unit,
    onNextClick: (PracticeAnswer) -> Unit,
    onInfoClick: (VocabReviewState) -> Unit,
    onCharacterInfoClick: (String) -> Unit,
    onFeedbackClick: (JapaneseWord) -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val reviewState = screenState.state.value
        when (val currentState = reviewState.reviewState) {
            is VocabReviewState.Flashcard -> {
                VocabPracticeFlashcardUI(
                    reviewState = currentState,
                    answers = reviewState.answers,
                    onRevealAnswerClick = onFlashcardAnswerRevealClick,
                    onNextClick = onNextClick,
                    onInfoClick = { onInfoClick(currentState) },
                )
            }

            is VocabReviewState.Reading -> {
                VocabPracticeReadingPickerUI(
                    reviewState = currentState,
                    answers = reviewState.answers,
                    onAnswerSelected = onAnswerSelected,
                    onNextClick = onNextClick,
                    onInfoClick = { onInfoClick(currentState) },
                    onFeedbackClick = onFeedbackClick
                )
            }

            is VocabReviewState.Writing -> {
                VocabPracticeWritingUI(
                    reviewState = currentState,
                    answers = reviewState.answers,
                    answerSelected = onNextClick,
                    onInfoClick = { onInfoClick(currentState) },
                    onCharacterInfoClick = onCharacterInfoClick,
                    onFeedbackClick = onFeedbackClick
                )
            }
        }

    }

}

@Composable
private fun ScreenSummary(
    screenState: ScreenState.Summary,
    onVocabClick: (VocabSummaryItem) -> Unit,
    onFinishClick: () -> Unit
) {

    PracticeSummaryContainer(
        practiceDuration = screenState.practiceDuration,
        summaryItemsCount = screenState.results.size,
        onFinishClick = onFinishClick,
        extraHeaderContent = {
            if (screenState.accuracy != null) {
                PracticeSummaryInfoLabel(
                    title = stringResource(Res.string.practice_summary_header_accuracy),
                    data = "${screenState.accuracy}%"
                )
            }
        }
    ) {

        screenState.results.takeIf { it.isNotEmpty() }
            ?.forEachIndexed { index, item ->
                PracticeSummaryItem(
                    index = index,
                    header = {
                        FuriganaText(
                            furiganaString = item.reading,
                            modifier = Modifier
                        )
                    },
                    totalReviews = item.totalReviews,
                    nextInterval = item.nextInterval,
                    onClick = { onVocabClick(item) }
                )
            }
            ?: PracticeSummaryEmptyList()

    }

}
