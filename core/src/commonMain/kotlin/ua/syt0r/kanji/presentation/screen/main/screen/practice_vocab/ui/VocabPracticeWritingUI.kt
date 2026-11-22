package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.AppListItemDefaults
import ua.syt0r.kanji.presentation.common.AutopaddedScrollableColumn
import ua.syt0r.kanji.presentation.common.copyCentered
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.CenteredBoxWithSide
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.common.ui.kanji.Kanji
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiReadingsContainer
import ua.syt0r.kanji.presentation.common.ui.kanji.RadicalKanji
import ua.syt0r.kanji.presentation.common.ui.kanji.getColoredKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterDecorations
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWritingProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswerButtonsContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswerButtonsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui.KanjiVariantsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui.LetterPracticeKanaInfo
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabCharacterDetails
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabCharacterWritingData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewState

@Composable
fun VocabPracticeWritingUI(
    reviewState: VocabReviewState.Writing,
    answers: PracticeAnswers,
    answerSelected: (PracticeAnswer) -> Unit,
    onInfoClick: () -> Unit,
    onCharacterInfoClick: (String) -> Unit,
    onFeedbackClick: (JapaneseWord) -> Unit
) {

    AutoSwitchSelectedItemLaunchedEffect(reviewState)

    val handleAnswer = { answer: PracticeAnswer ->
        val strokeMistakes = reviewState.charactersData
            .filterIsInstance<VocabCharacterWritingData.WithStrokes>()
            .map { it.reviewWriterState.progress.value }
            .filterIsInstance<CharacterWritingProgress.Completed>()
            .sumOf { it.mistakes }
        val updatedAnswer = answer.copy(
            mistakes = if (answer == answers.again) strokeMistakes.coerceAtLeast(1) else strokeMistakes
        )
        answerSelected(updatedAnswer)
    }

    val revealAnswer = remember(reviewState) {
        derivedStateOf {
            !reviewState.isStudyMode.value &&
                    reviewState.charactersData.filterIsInstance<VocabCharacterWritingData.WithStrokes>()
                        .all { it.reviewWriterState.progress.value is CharacterWritingProgress.Completed }
        }
    }

    val updatedState = rememberUpdatedState(reviewState to answers)
    val buttonsState = remember(reviewState) {
        derivedStateOf {
            val (state, currentAnswers) = updatedState.value
            val allCompletedIdle = state.charactersData
                .filterIsInstance<VocabCharacterWritingData.WithStrokes>()
                .all { it.writerState.value.progress.value is CharacterWritingProgress.Completed.Idle }

            when {
                !allCompletedIdle -> VocabWritingButtonsState.Hidden
                state.isStudyMode.value -> VocabWritingButtonsState.StudyButtons
                else -> VocabWritingButtonsState.AnswerButtons(currentAnswers)
            }
        }
    }

    val onStudyCompleted: () -> Unit = {
        reviewState.isStudyMode.value = false
        reviewState.selected.value = reviewState.charactersData
            .firstOrNull { it is VocabCharacterWritingData.WithStrokes }
            ?: reviewState.charactersData.first()
    }

    val selectedReviewState = rememberUpdatedState(reviewState.selected.value)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when (LocalOrientation.current) {
            Orientation.Portrait -> {
                AutopaddedScrollableColumn(
                    modifier = Modifier.fillMaxSize(),
                    bottomOverlayContent = {
                        Input(
                            state = selectedReviewState,
                            modifier = Modifier.fillMaxWidth()
                                .padding(20.dp)
                                .wrapContentSize()
                                .widthIn(max = 400.dp)
                                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                        )
                    }
                ) {

                    Progress(
                        reviewState = reviewState,
                        revealAnswer = revealAnswer,
                        onInfoClick = onInfoClick,
                        onCharacterInfoClick = onCharacterInfoClick,
                        modifier = Modifier.fillMaxSize().padding(20.dp)
                    )

                }
            }

            Orientation.Landscape -> {
                val progressPane: @Composable RowScope.() -> Unit = {
                    Progress(
                        reviewState = reviewState,
                        revealAnswer = revealAnswer,
                        onInfoClick = onInfoClick,
                        onCharacterInfoClick = onCharacterInfoClick,
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    )
                }
                val inputPane: @Composable RowScope.() -> Unit = {
                    Input(
                        state = selectedReviewState,
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight()
                            .padding(20.dp)
                            .wrapContentSize()
                            .widthIn(max = 400.dp)
                            .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    )
                }

                val (firstPane, secondPane) = if (reviewState.leftHandedMode) {
                    inputPane to progressPane
                } else {
                    progressPane to inputPane
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    firstPane()
                    secondPane()
                }
            }
        }

        VocabWritingAnswerButtons(
            buttonsState = buttonsState,
            studyCompleted = onStudyCompleted,
            answerSelected = handleAnswer,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

    }

}

private sealed interface VocabWritingButtonsState {
    object Hidden : VocabWritingButtonsState
    object StudyButtons : VocabWritingButtonsState
    data class AnswerButtons(val answers: PracticeAnswers) : VocabWritingButtonsState
}

@Composable
private fun VocabWritingAnswerButtons(
    buttonsState: State<VocabWritingButtonsState>,
    studyCompleted: () -> Unit,
    answerSelected: (PracticeAnswer) -> Unit,
    modifier: Modifier
) {
    val transition = updateTransition(buttonsState.value, label = "VocabWritingAnswerButtons")
    transition.AnimatedContent(
        transitionSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeOut()
        },
        contentKey = { it !is VocabWritingButtonsState.Hidden },
        modifier = modifier
    ) { state ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            when (state) {
                VocabWritingButtonsState.Hidden -> {}

                VocabWritingButtonsState.StudyButtons -> {
                    PracticeAnswerButtonsContainer {
                        Row(
                            modifier = Modifier
                                .padding(AppListItemDefaults.ListItemDefaultPaddings)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(onClick = studyCompleted)
                                .width(400.dp)
                                .padding(Dimens.SpacingBig)
                                .testTag("vocab_writing_review_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMid)
                        ) {
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = resolveString { letterPractice.studyFinishedButton },
                                style = MaterialTheme.typography.bodyLarge.copyCentered(),
                                textAlign = TextAlign.Center
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.NavigateNext,
                                contentDescription = resolveString { letterPractice.studyFinishedButton },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(24.dp, 24.dp)
                                    .wrapContentSize(Alignment.CenterStart)
                            )
                        }
                    }
                }

                is VocabWritingButtonsState.AnswerButtons -> {
                    PracticeAnswerButtonsRow(
                        answers = state.answers,
                        onClick = answerSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun Progress(
    reviewState: VocabReviewState.Writing,
    revealAnswer: State<Boolean>,
    onInfoClick: () -> Unit,
    onCharacterInfoClick: (String) -> Unit,
    modifier: Modifier,
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        val showMeaning = !reviewState.noTranslationsLayout || reviewState.isStudyMode.value || revealAnswer.value

        CenteredBoxWithSide(
            modifier = Modifier,
            placeSideContentAtStart = false,
            centerContent = {
                Text(
                    text = if (showMeaning) {
                        reviewState.meaning
                    } else {
                        resolveString { letterPractice.noKanjiTranslationsLabel }
                    },
                    style = MaterialTheme.typography.displaySmall,
                    color = if (showMeaning) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center
                )
            },
            sideContent = {
                IconButton(
                    enabled = revealAnswer.value,
                    onClick = onInfoClick,
                    modifier = Modifier.testTag("vocab_writing_vocab_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowOutward,
                        contentDescription = resolveString { vocabPractice.detailsButton }
                    )
                }
            },
        )

        if (reviewState.showKanaReading) {
            Text(
                text = reviewState.kanaReading,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        val lazyListState = rememberLazyListState()
        AutoscrollCharacterIndicatorRowLaunchedEffect(reviewState, lazyListState)

        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            state = lazyListState
        ) {

            items(reviewState.charactersData) {
                CharacterStateIndicator(
                    characterData = it,
                    selectedState = reviewState.selected,
                    isStudyMode = reviewState.isStudyMode.value
                )
            }

        }

        SelectedCharacterInfoSection(
            reviewState = reviewState,
            onCharacterInfoClick = onCharacterInfoClick
        )
    }

}

@Composable
private fun SelectedCharacterInfoSection(
    reviewState: VocabReviewState.Writing,
    onCharacterInfoClick: (String) -> Unit
) {
    val selectedChar = reviewState.selected.value
    val details = reviewState.characterDetails[selectedChar.character] ?: return
    val isSelectedCompleted = (selectedChar as? VocabCharacterWritingData.WithStrokes)
        ?.writerState?.value?.progress?.value !is CharacterWritingProgress.Writing
    val revealCharacter = reviewState.isStudyMode.value || isSelectedCompleted

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = resolveString { vocabPractice.writingKanjiInfoTitle },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = resolveString { letterPractice.strokeCountTitle(details.strokes.size) },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    enabled = revealCharacter,
                    onClick = { onCharacterInfoClick(details.character) },
                    modifier = Modifier.size(36.dp).testTag("vocab_writing_character_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowOutward,
                        contentDescription = resolveString { vocabPractice.writingCharacterDetailsButton },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            when (details) {
                is VocabCharacterDetails.Kanji -> {
                    val showMeanings = (!reviewState.noTranslationsLayout || revealCharacter) &&
                            details.meanings.isNotEmpty()

                    if (revealCharacter || showMeanings) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (revealCharacter) {
                                VocabAnimatedKanjiPreview(
                                    strokes = details.strokes,
                                    radicals = details.radicals,
                                    shouldHighlightRadicals = reviewState.radicalsHighlight,
                                    toggleRadicalsHighlight = {
                                        reviewState.radicalsHighlight.value = !reviewState.radicalsHighlight.value
                                    }
                                )
                            }
                            if (showMeanings) {
                                Text(
                                    text = details.meanings.joinToString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    KanjiReadingsContainer(
                        on = details.on,
                        kun = details.kun,
                        modifier = Modifier.fillMaxWidth()
                    )

                    details.variants?.let {
                        KanjiVariantsRow(variants = it)
                    }
                }

                is VocabCharacterDetails.Kana -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (revealCharacter) {
                            Kanji(
                                strokes = details.strokes,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        LetterPracticeKanaInfo(
                            kanaSystem = details.kanaSystem,
                            reading = details.reading,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabAnimatedKanjiPreview(
    strokes: List<Path>,
    radicals: List<CharacterRadical>,
    shouldHighlightRadicals: State<Boolean>,
    toggleRadicalsHighlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val radicalsTransition = updateTransition(
        targetState = shouldHighlightRadicals.value,
        label = "Vocab radical highlight transition"
    )

    radicalsTransition.AnimatedContent(
        modifier = modifier
            .size(64.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = toggleRadicalsHighlight),
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { shouldHighlight ->
        when (shouldHighlight) {
            true -> RadicalKanji(
                strokes = getColoredKanjiStrokes(
                    strokes = strokes,
                    radicalToStrokeRangeList = radicals.map {
                        val range = it.startPosition until (it.startPosition + it.strokesCount)
                        it.radical to range
                    }
                ),
                modifier = Modifier.fillMaxSize()
            )

            false -> Kanji(
                strokes = strokes,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun Input(
    state: State<VocabCharacterWritingData>,
    modifier: Modifier
) {

    CharacterWriterDecorations(
        modifier = modifier,
        state = remember {
            derivedStateOf {
                state.value.let { it as? VocabCharacterWritingData.WithStrokes }?.writerState?.value
            }
        }
    ) {

        Crossfade(
            targetState = state.value
        ) { writingData ->

            when (writingData) {
                is VocabCharacterWritingData.NoStrokes -> {}
                is VocabCharacterWritingData.WithStrokes -> CharacterWriter(
                    state = writingData.writerState.value,
                    modifier = Modifier.fillMaxSize()
                )
            }

        }

    }

}

private enum class CharacterWritingDisplayState { NoWritingData, Writing, Correct, Failed }

@Composable
private fun CharacterStateIndicator(
    characterData: VocabCharacterWritingData,
    selectedState: MutableState<VocabCharacterWritingData>,
    isStudyMode: Boolean
) {

    val state = when (characterData) {
        is VocabCharacterWritingData.NoStrokes -> {
            CharacterWritingDisplayState.NoWritingData
        }

        is VocabCharacterWritingData.WithStrokes -> {
            when (val status = characterData.writerState.value.progress.value) {
                CharacterWritingProgress.Writing -> {
                    CharacterWritingDisplayState.Writing
                }

                is CharacterWritingProgress.Completed -> {
                    if (status.isCorrect) CharacterWritingDisplayState.Correct
                    else CharacterWritingDisplayState.Failed
                }

            }
        }
    }

    val borderColor = when (state) {
        CharacterWritingDisplayState.NoWritingData,
        CharacterWritingDisplayState.Writing -> MaterialTheme.colorScheme.surfaceVariant

        CharacterWritingDisplayState.Failed -> MaterialTheme.colorScheme.error
        CharacterWritingDisplayState.Correct -> MaterialTheme.extraColorScheme.success
    }

    val textColor = when {
        state == CharacterWritingDisplayState.NoWritingData -> MaterialTheme.colorScheme
            .onSurface.copy(alpha = 0.6f)

        state == CharacterWritingDisplayState.Writing && isStudyMode -> MaterialTheme.colorScheme.onSurfaceVariant
        state == CharacterWritingDisplayState.Writing -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = characterData.character,
            color = textColor,
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable(
                    enabled = state != CharacterWritingDisplayState.NoWritingData
                ) { selectedState.value = characterData }
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, borderColor, MaterialTheme.shapes.small)
                .wrapContentSize()
        )

        Box(
            Modifier.fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 4.dp)
                .clip(CircleShape)
                .background(
                    when (characterData == selectedState.value) {
                        true -> MaterialTheme.colorScheme.onSurface
                        false -> MaterialTheme.colorScheme.surface
                    }
                )
        )

    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun AutoSwitchSelectedItemLaunchedEffect(reviewState: VocabReviewState.Writing) {
    LaunchedEffect(reviewState, reviewState.isStudyMode.value) {
        val completedItemIndexes = mutableSetOf<Int>()
        val selectedStateToIndexFlow = snapshotFlow {
            val selected = reviewState.selected.value
            val index = reviewState.charactersData.indexOf(selected)
            selected to index
        }

        selectedStateToIndexFlow.filter { (_, index) -> !completedItemIndexes.contains(index) }
            .flatMapLatest { (state: VocabCharacterWritingData, index: Int) ->
                if (state !is VocabCharacterWritingData.WithStrokes)
                    return@flatMapLatest flow<Int> { }

                val writingStatusFlow = snapshotFlow { state.writerState.value.progress.value }
                writingStatusFlow.filterIsInstance<CharacterWritingProgress.Completed>()
                    .map { index }
            }
            .onEach { index ->
                completedItemIndexes.add(index)
                delay(800)
                val nextState = reviewState.charactersData
                    .filterIsInstance<VocabCharacterWritingData.WithStrokes>()
                    .find { it.writerState.value.progress.value !is CharacterWritingProgress.Completed }
                    ?: return@onEach
                reviewState.selected.value = nextState
            }
            .collect()
    }
}

@Composable
fun AutoscrollCharacterIndicatorRowLaunchedEffect(
    reviewState: VocabReviewState.Writing,
    lazyListState: LazyListState
) {
    LaunchedEffect(reviewState) {
        snapshotFlow { reviewState.selected.value }
            .collectLatest {
                val index = reviewState.charactersData.indexOf(it)

                val offset = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()
                    ?.size?.times(3) ?: return@collectLatest

                val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
                if (index < firstVisibleItemIndex + 3)
                    lazyListState.animateScrollToItem(index, -offset)

                val lastVisibleItemIndex = lazyListState.layoutInfo.visibleItemsInfo.last().index
                if (index > lastVisibleItemIndex - 3)
                    lazyListState.animateScrollToItem(index, offset)
            }
    }
}
