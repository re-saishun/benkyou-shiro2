package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.use_case

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.japanese.isKana
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract

class SearchScreenProcessInputUseCase(
    private val appDataRepository: AppDataRepository
) : SearchScreenContract.ProcessInputUseCase {

    override suspend fun process(
        input: String
    ): SearchScreenContract.ScreenState = coroutineScope {

        val knownCharactersDeferred = async {
            val distinctStrings = input.map { it.toString() }.distinct()
            if (distinctStrings.isEmpty()) return@async emptyList()

            val strokeCounts = appDataRepository.getStrokeCounts(distinctStrings)
            val charactersWithStrokes = strokeCounts.keys
            val nonKanaWithStrokes = distinctStrings
                .filter { it.length == 1 && !it[0].isKana() && it in charactersWithStrokes }
            val charactersWithReadings = if (nonKanaWithStrokes.isEmpty()) {
                emptySet()
            } else {
                appDataRepository.getCharactersWithReadings(nonKanaWithStrokes)
            }

            val knownSet = distinctStrings.filter { ch ->
                ch in charactersWithStrokes &&
                        ((ch.length == 1 && ch[0].isKana()) || ch in charactersWithReadings)
            }.toSet()

            input.map { it.toString() }.filter { it in knownSet }
        }

        val wordsDeferred = async {
            if (input.isEmpty()) {
                0 to emptyList()
            } else {
                val wordsCount = appDataRepository.getWordsWithTextCount(input)
                val words = appDataRepository.getWordsWithText(
                    text = input,
                    limit = SearchScreenContract.InitialWordsCount
                )
                wordsCount to words
            }
        }

        val (wordsCount, words) = wordsDeferred.await()

        SearchScreenContract.ScreenState(
            isLoading = false,
            characters = knownCharactersDeferred.await(),
            words = mutableStateOf(PaginatableJapaneseWordList(wordsCount, words)),
            query = input
        )
    }

}
