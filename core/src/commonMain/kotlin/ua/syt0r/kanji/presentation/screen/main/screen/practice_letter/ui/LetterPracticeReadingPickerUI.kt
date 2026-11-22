package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiReadingsContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.ExpandablePracticeAnswerButtonsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.ExpandableVocabPracticeAnswersRowState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.SelectedReadingAnswer

@Composable
fun LetterPracticeReadingPickerUI(
    reviewState: LetterPracticeReviewState.ReadingPicker,
    onNextClick: (PracticeAnswer) -> Unit,
    speakKana: (KanaReading) -> Unit,
) {
    val selectedAnswer by reviewState.selectedAnswer

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {

        Text(
            text = reviewState.itemData.character,
            fontSize = 80.textDp,
            modifier = Modifier.padding(top = 12.dp)
        )

        when (val data = reviewState.itemData) {
            is LetterPracticeItemData.KanaReadingPickerData -> {
                AnimatedVisibility(visible = selectedAnswer != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        LetterPracticeKanaInfo(
                            kanaSystem = data.kanaSystem,
                            reading = data.reading
                        )
                        KanaVoiceMenu(
                            autoPlayEnabled = reviewState.layout.kanaAutoPlay,
                            clickable = selectedAnswer != null,
                            onAutoPlayToggleClick = {
                                reviewState.layout.kanaAutoPlay.run { value = !value }
                            },
                            onSpeakClick = { speakKana(data.reading) }
                        )
                    }
                }
            }

            is LetterPracticeItemData.KanjiReadingPickerData -> {
                if ((selectedAnswer != null || reviewState.showMeaning) && data.meanings.isNotEmpty()) {
                    Text(
                        text = data.meanings.joinToString(),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .widthIn(max = 420.dp)
                            .padding(top = 8.dp)
                    )
                }

                AnimatedVisibility(visible = selectedAnswer != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .widthIn(max = 420.dp)
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        KanjiReadingsContainer(
                            on = data.on,
                            kun = data.kun,
                            modifier = Modifier.fillMaxWidth()
                        )
                        data.variants?.takeIf { it.isNotBlank() }?.let { variants ->
                            KanjiVariantsRow(variants = variants)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val maxItemsInEachRow = when (LocalOrientation.current) {
            Orientation.Portrait -> 2
            Orientation.Landscape -> 4
        }
        val answerRows = reviewState.itemData.answers.chunked(maxItemsInEachRow)

        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            answerRows.forEachIndexed { rowIndex, rowAnswers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowAnswers.forEachIndexed { colIndex, answer ->
                        val optionIndex = rowIndex * maxItemsInEachRow + colIndex
                        LetterReadingAnswerButton(
                            answer = answer,
                            selectedAnswer = selectedAnswer,
                            enabled = selectedAnswer == null,
                            onClick = {
                                reviewState.selectedAnswer.value = SelectedReadingAnswer(
                                    selected = answer,
                                    correct = reviewState.itemData.correctAnswer
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("letter_reading_picker_option_$optionIndex")
                        )
                    }

                    val missingSpots = maxItemsInEachRow - rowAnswers.size
                    if (missingSpots != 0) {
                        Spacer(Modifier.weight(missingSpots.toFloat()))
                    }
                }
            }
        }

        val updatedState = rememberUpdatedState(reviewState to reviewState.answers)

        ExpandablePracticeAnswerButtonsRow(
            state = remember {
                derivedStateOf {
                    val (state, updatedAnswers) = updatedState.value
                    ExpandableVocabPracticeAnswersRowState(
                        answers = updatedAnswers,
                        showButton = state.selectedAnswer.value != null
                    )
                }
            },
            onClick = {
                val selected = selectedAnswer ?: return@ExpandablePracticeAnswerButtonsRow
                val updatedAnswer = it.copy(
                    mistakes = if (selected.isCorrect && it != reviewState.answers.again) 0 else 1
                )
                onNextClick(updatedAnswer)
            },
            modifier = Modifier.width(IntrinsicSize.Max)
        )
    }
}

@Composable
private fun LetterReadingAnswerButton(
    answer: String,
    selectedAnswer: SelectedReadingAnswer?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (textColor, containerColor) = MaterialTheme.run {
        when {
            answer == selectedAnswer?.correct -> {
                colorScheme.surface to extraColorScheme.success
            }

            answer == selectedAnswer?.selected && !selectedAnswer.isCorrect -> {
                colorScheme.surface to colorScheme.error
            }

            else -> {
                colorScheme.onSurface to colorScheme.surfaceVariant
            }
        }
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            contentColor = textColor,
            disabledContentColor = textColor,
            containerColor = containerColor,
            disabledContainerColor = containerColor
        ),
        enabled = enabled,
        modifier = modifier
    ) {
        Text(
            text = answer,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
