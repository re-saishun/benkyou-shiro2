package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.grammar_dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration

@Composable
fun GrammarDashboardScreen(
    mainNavigationState: MainNavigationState,
    viewModel: GrammarDashboardScreenContract.ViewModel = getMultiplatformViewModel()
) {

    GrammarDashboardScreenUI(
        screenState = viewModel.screenState.collectAsState(),
        catalogState = viewModel.catalogState.collectAsState(),
        onCatalogOpened = { viewModel.loadCatalog() },
        onCatalogCheck = { viewModel.loadCatalog(it) },
        onDownloadPack = { viewModel.downloadPack(it) },
        onCreateEmptyDeck = { viewModel.createEmptyDeck(it) },
        onDeleteDeck = { viewModel.deleteDeck(it.deckId) },
        onStartPractice = { practiceItem, cards ->
            val configuration = GrammarPracticeScreenConfiguration(
                cards = cards,
                practiceType = practiceItem.practiceType
            )
            mainNavigationState.navigate(MainDestination.GrammarPractice(configuration))
        }
    )

}
