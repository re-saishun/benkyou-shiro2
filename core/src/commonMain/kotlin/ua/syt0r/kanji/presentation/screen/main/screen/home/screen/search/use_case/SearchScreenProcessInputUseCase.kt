package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.use_case

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

        // Characters and words are independent queries, run them concurrently.
        // Each distinct character is checked only once, original order is preserved.
        val knownCharactersDeferred = async {
            val distinctCharacters = input.toSet()
            val knownSet = distinctCharacters.map { char ->
                async { char to isKnownCharacter(char) }
            }.awaitAll().filter { it.second }.map { it.first }.toSet()

            input.filter { it in knownSet }.map { it.toString() }
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

    private suspend fun isKnownCharacter(char: Char): Boolean {
        val charString = char.toString()
        val areStrokesAvailable = appDataRepository.getStrokes(charString).isNotEmpty()
        return areStrokesAvailable && when {
            char.isKana() -> true
            else -> appDataRepository.getReadings(charString).isNotEmpty()
        }
    }

}
