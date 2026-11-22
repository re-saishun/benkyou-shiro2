package ua.syt0r.kanji.presentation.screen.main.screen.text_analysis

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.CreateAnalysisResultUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.DefaultCreateAnalysisResultUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.DefaultGeminiTextAnalysisUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.DefaultParseIchiranResponseUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.GeminiTextAnalysisUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.ParseIchiranResponseUseCase

val textAnalysisScreenModule = module {

    multiplatformViewModel {
        TextAnalysisViewModel(
            viewModelScope = it.component1(),
            accountManager = get(),
            parseIchiranResponseUseCase = get(),
            createAnalysisResultUseCase = get(),
            analysisRepository = get(),
            networkApi = get(),
            geminiTextAnalysisUseCase = get(),
            appPreferences = get()
        )
    }

    factory<CreateAnalysisResultUseCase> {
        DefaultCreateAnalysisResultUseCase(
            appDataRepository = get()
        )
    }

    factory<ParseIchiranResponseUseCase> {
        DefaultParseIchiranResponseUseCase()
    }

    factory<GeminiTextAnalysisUseCase> {
        DefaultGeminiTextAnalysisUseCase(
            networkClients = get(),
            appPreferences = get()
        )
    }

}