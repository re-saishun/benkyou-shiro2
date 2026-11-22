package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.grammar_dashboard

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val grammarDashboardScreenModule = module {

    multiplatformViewModel<GrammarDashboardScreenContract.ViewModel> {
        GrammarDashboardViewModel(
            viewModelScope = it.component1(),
            grammarSrsManager = get(),
            catalogService = get(),
            repository = get()
        )
    }

}
