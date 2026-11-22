package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration

@Composable
fun GrammarPracticeScreen(
    configuration: GrammarPracticeScreenConfiguration,
    mainNavigationState: MainNavigationState,
    viewModel: GrammarPracticeScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.initialize(configuration)
    }

    GrammarPracticeScreenUI(
        state = viewModel.state.collectAsState(),
        onReveal = { viewModel.reveal() },
        onGrade = { viewModel.submitGrade(it) },
        onQuizOptionSelected = { viewModel.selectQuizOption(it) },
        onTestSubmit = { viewModel.submitTestInput(it) },
        onNext = { viewModel.next() },
        onFinishPractice = { viewModel.finishPractice() },
        navigateBack = { mainNavigationState.navigateBack() }
    )

}
