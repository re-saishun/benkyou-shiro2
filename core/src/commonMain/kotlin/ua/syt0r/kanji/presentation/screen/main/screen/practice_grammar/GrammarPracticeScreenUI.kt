package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.user_data.database.GrammarExample
import ua.syt0r.kanji.core.user_data.database.GrammarPointData
import ua.syt0r.kanji.presentation.common.AppListItem
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.FlashcardPracticeAnswerButtonsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswerButtonsContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeEarlyFinishDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryEmptyList
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeToolbar
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeToolbarState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.GrammarPracticeScreenContract.ReviewStep
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.GrammarPracticeScreenContract.ScreenState

@Composable
fun GrammarPracticeScreenUI(
    state: State<ScreenState>,
    onReveal: () -> Unit,
    onGrade: (PracticeAnswer) -> Unit,
    onQuizOptionSelected: (Int) -> Unit,
    onTestSubmit: (String) -> Unit,
    onNext: () -> Unit,
    onFinishPractice: () -> Unit,
    navigateBack: () -> Unit
) {

    var showEarlyFinishDialog by remember { mutableStateOf(false) }

    val tryNavigateBack: () -> Unit = {
        when (state.value) {
            is ScreenState.Review -> showEarlyFinishDialog = true
            else -> navigateBack()
        }
    }

    if (showEarlyFinishDialog) {
        PracticeEarlyFinishDialog(
            onDismissRequest = { showEarlyFinishDialog = false },
            onConfirmClick = {
                showEarlyFinishDialog = false
                onFinishPractice()
            }
        )
    }

    val toolbarState = derivedToolbarState(state)

    Scaffold(
        topBar = {
            PracticeToolbar(
                state = toolbarState,
                onUpButtonClick = tryNavigateBack
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val screenState = state.value) {
                ScreenState.Loading -> {
                    // Intentionally empty, data loads from local database almost instantly
                }

                ScreenState.Empty -> EmptyContent(onCloseClick = navigateBack)

                is ScreenState.Review -> ReviewContent(
                    state = screenState,
                    onReveal = onReveal,
                    onGrade = onGrade,
                    onQuizOptionSelected = onQuizOptionSelected,
                    onTestSubmit = onTestSubmit,
                    onNext = onNext
                )

                is ScreenState.Summary -> SummaryContent(
                    state = screenState,
                    onFinishClick = navigateBack
                )
            }
        }

    }

}

@Composable
private fun derivedToolbarState(
    state: State<ScreenState>
): State<PracticeToolbarState> {
    return remember(state) {
        derivedStateOf {
            when (val value = state.value) {
                is ScreenState.Review -> PracticeToolbarState.Review(value.progress)
                else -> PracticeToolbarState.Idle
            }
        }
    }
}

@Composable
private fun EmptyContent(onCloseClick: () -> Unit) {
    val strings = resolveString { grammar }
    Column(
        modifier = Modifier.fillMaxSize().wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.practiceEmptyQueue,
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = onCloseClick) {
            Text(strings.closeButton)
        }
    }
}

@Composable
private fun ReviewContent(
    state: ScreenState.Review,
    onReveal: () -> Unit,
    onGrade: (PracticeAnswer) -> Unit,
    onQuizOptionSelected: (Int) -> Unit,
    onTestSubmit: (String) -> Unit,
    onNext: () -> Unit
) {

    Column(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .wrapContentSize(Alignment.TopCenter)
                .widthIn(max = 500.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = resolveString(state.practiceType.titleResolver),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )

            when (val step = state.step) {
                is ReviewStep.Read -> ReadBody(state.point, step.revealed)

                ReviewStep.QuizQuestion -> QuizBody(
                    point = state.point,
                    selectedIndex = null,
                    onOptionSelected = onQuizOptionSelected
                )

                is ReviewStep.QuizResult -> QuizBody(
                    point = state.point,
                    selectedIndex = step.selectedIndex,
                    onOptionSelected = {}
                )

                ReviewStep.TestInput -> TestBody(
                    point = state.point,
                    result = null,
                    onSubmit = onTestSubmit
                )

                is ReviewStep.TestResult -> TestBody(
                    point = state.point,
                    result = step,
                    onSubmit = {}
                )
            }

        }

        when (val step = state.step) {
            is ReviewStep.Read -> {
                val revealed = remember(step.revealed) { mutableStateOf(step.revealed) }
                FlashcardPracticeAnswerButtonsRow(
                    answers = state.answers,
                    showAnswer = revealed,
                    onRevealAnswerClick = onReveal,
                    onAnswerClick = onGrade
                )
            }

            is ReviewStep.QuizResult,
            is ReviewStep.TestResult -> NextButton(onNext)

            else -> {}
        }

    }

}

@Composable
private fun NextButton(onClick: () -> Unit) {
    PracticeAnswerButtonsContainer {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(horizontal = 20.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(resolveString { grammar.practiceNextButton })
        }
    }
}

// region Read

@Composable
private fun ColumnScope.ReadBody(
    point: GrammarPointData,
    revealed: Boolean
) {

    Text(
        text = point.title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )

    if (!revealed) return

    val strings = resolveString { grammar }

    HorizontalDivider()

    LabeledSection(strings.practiceStructureLabel, point.structure)
    LabeledSection(strings.practiceMeaningLabel, point.meaning)
    if (point.nuance.isNotBlank()) {
        LabeledSection(strings.practiceNuanceLabel, point.nuance)
    }

    if (point.examples.isNotEmpty()) {
        Text(
            text = strings.practiceExamplesLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        point.examples.forEach { ExampleCard(it) }
    }

}

@Composable
private fun LabeledSection(label: String, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ExampleCard(example: GrammarExample) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = example.japanese,
                style = MaterialTheme.typography.titleMedium
            )
            if (example.reading.isNotBlank()) {
                Text(
                    text = example.reading,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (example.translation.isNotBlank()) {
                Text(
                    text = example.translation,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// endregion

// region Quiz

@Composable
private fun ColumnScope.QuizBody(
    point: GrammarPointData,
    selectedIndex: Int?,
    onOptionSelected: (Int) -> Unit
) {
    val quiz = point.quiz ?: return
    val strings = resolveString { grammar }
    val success = MaterialTheme.extraColorScheme.success
    val error = MaterialTheme.colorScheme.error

    Text(
        text = point.title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.outline
    )

    Text(
        text = quiz.question,
        style = MaterialTheme.typography.headlineSmall
    )

    quiz.options.forEachIndexed { index, option ->

        val answered = selectedIndex != null
        val isCorrectOption = index == quiz.correctIndex
        val isWrongSelection = answered && index == selectedIndex && !isCorrectOption
        val highlighted = answered && (isCorrectOption || isWrongSelection)
        val borderColor: Color = when {
            answered && isCorrectOption -> success
            isWrongSelection -> error
            else -> MaterialTheme.colorScheme.outlineVariant
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !answered) { onOptionSelected(index) },
            border = BorderStroke(
                width = if (highlighted) 2.dp else 1.dp,
                color = borderColor
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

    }

    if (selectedIndex != null) {
        val isCorrect = selectedIndex == quiz.correctIndex
        ResultLabel(isCorrect)
        if (quiz.explanation.isNotBlank()) {
            Text(
                text = quiz.explanation,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (!isCorrect) {
            LabeledSection(strings.practiceMeaningLabel, point.meaning)
        }
    }
}

@Composable
private fun ResultLabel(isCorrect: Boolean) {
    val strings = resolveString { grammar }
    Text(
        text = if (isCorrect) strings.practiceCorrectAnswerLabel
        else strings.practiceIncorrectAnswerLabel,
        style = MaterialTheme.typography.titleMedium,
        color = if (isCorrect) MaterialTheme.extraColorScheme.success
        else MaterialTheme.colorScheme.error
    )
}

// endregion

// region Test

@Composable
private fun ColumnScope.TestBody(
    point: GrammarPointData,
    result: ReviewStep.TestResult?,
    onSubmit: (String) -> Unit
) {
    val strings = resolveString { grammar }
    var input by remember(point.key) { mutableStateOf("") }

    Text(
        text = point.title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )

    OutlinedTextField(
        value = if (result != null) result.input else input,
        onValueChange = { if (result == null) input = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(strings.practiceTestInputHint) },
        readOnly = result != null,
        singleLine = true
    )

    if (result == null) {
        Button(
            onClick = { onSubmit(input) },
            modifier = Modifier.fillMaxWidth(),
            enabled = input.isNotBlank(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(strings.practiceTestCheckButton)
        }
    } else {
        ResultLabel(result.isCorrect)
        LabeledSection(strings.practiceMeaningLabel, point.meaning)
        LabeledSection(strings.practiceStructureLabel, point.structure)
    }
}

// endregion

// region Summary

@Composable
private fun SummaryContent(
    state: ScreenState.Summary,
    onFinishClick: () -> Unit
) {
    PracticeSummaryContainer(
        practiceDuration = state.practiceDuration,
        summaryItemsCount = state.reviewsCount,
        onFinishClick = onFinishClick
    ) {
        if (state.items.isEmpty()) {
            PracticeSummaryEmptyList()
        } else {
            state.items.forEachIndexed { index, item ->
                AppListItem(
                    leadingContent = { Text((index + 1).toString()) },
                    headlineContent = { Text(item.title) },
                    supportingContent = {
                        Text(
                            resolveString { commonPractice.formattedSrsInterval(item.nextInterval) }
                        )
                    }
                )
            }
        }
    }
}

// endregion
