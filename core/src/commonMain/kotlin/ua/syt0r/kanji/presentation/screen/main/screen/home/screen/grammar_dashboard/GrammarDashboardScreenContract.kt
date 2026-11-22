package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.grammar_dashboard

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.grammar.RemoteGrammarPack
import ua.syt0r.kanji.presentation.common.ScreenGrammarPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration

interface GrammarDashboardScreenContract {

    interface ViewModel {
        val screenState: StateFlow<ScreenState>
        val catalogState: StateFlow<CatalogState>

        /** [url] null means the saved (or default) catalog url */
        fun loadCatalog(url: String? = null)
        fun downloadPack(pack: RemoteGrammarPack)
        fun createEmptyDeck(title: String)
        fun deleteDeck(deckId: Long)
    }

    sealed interface ScreenState {

        data object Loading : ScreenState

        data class Loaded(
            val decks: List<GrammarDeckItem>
        ) : ScreenState

    }

    sealed interface CatalogState {

        data object Idle : CatalogState

        data class Loading(val url: String) : CatalogState

        data class Error(val url: String, val message: String) : CatalogState

        data class Loaded(
            val url: String,
            val packs: List<RemoteGrammarPack>,
            val downloadingIds: Set<String> = emptySet(),
            val error: String? = null
        ) : CatalogState

    }

}

data class GrammarDeckItem(
    val deckId: Long,
    val title: String,
    val level: String,
    val description: String,
    val pointCount: Int,
    val practiceItems: List<GrammarDeckPracticeItem>
)

data class GrammarDeckPracticeItem(
    val practiceType: ScreenGrammarPracticeType,
    val newCards: List<GrammarPracticeScreenConfiguration.Card>,
    val dueCards: List<GrammarPracticeScreenConfiguration.Card>
) {
    val combinedCards: List<GrammarPracticeScreenConfiguration.Card>
        get() = newCards + dueCards
}
