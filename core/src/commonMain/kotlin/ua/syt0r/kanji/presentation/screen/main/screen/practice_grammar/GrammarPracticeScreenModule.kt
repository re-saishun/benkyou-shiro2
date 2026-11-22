package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val grammarPracticeScreenModule = module {

    multiplatformViewModel<GrammarPracticeScreenContract.ViewModel> {
        GrammarPracticeViewModel(
            viewModelScope = it.component1(),
            grammarRepository = get(),
            srsCardRepository = get(),
            srsScheduler = get(),
            reviewHistoryRepository = get(),
            timeUtils = get()
        )
    }

}
