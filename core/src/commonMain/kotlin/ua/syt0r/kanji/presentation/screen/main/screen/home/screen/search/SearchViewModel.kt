package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchState

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val viewModelScope: CoroutineScope,
    private val appDataRepository: AppDataRepository,
    private val processInputUseCase: SearchScreenContract.ProcessInputUseCase,
    private val loadMoreWordsUseCase: SearchScreenContract.LoadMoreWordsUseCase,
    private val loadRadicalsUseCase: SearchScreenContract.LoadRadicalsUseCase,
    private val searchByRadicalsUseCase: SearchScreenContract.SearchByRadicalsUseCase,
    private val updateEnabledRadicalsUseCase: SearchScreenContract.UpdateEnabledRadicalsUseCase,
    private val analyticsManager: AnalyticsManager
) : SearchScreenContract.ViewModel {

    private val searchQueriesChannel = Channel<String>(Channel.BUFFERED)
    private val loadMoreWordsChannel = Channel<Int>(Channel.RENDEZVOUS, BufferOverflow.DROP_LATEST)

    private val radicalsDataInitialLoadChannel = Channel<Unit>(Channel.BUFFERED)
    private val radicalsLoadedCompletable = CompletableDeferred<Unit>()
    private val radicalsSearchQueriesChannel = Channel<Set<String>>(Channel.BUFFERED)

    override val state = mutableStateOf(
        ScreenState(
            isLoading = false,
            characters = emptyList(),
            words = mutableStateOf(PaginatableJapaneseWordList(0, emptyList())),
            query = ""
        )
    )

    override val radicalsState = mutableStateOf(
        RadicalSearchState(
            radicalsListItems = emptyList(),
            characterListItems = emptyList(),
            isLoading = true
        )
    )


    init {
        prewarmAppDatabase()
        handleSearchQueries()
        handleRadicalsLoading(radicalsLoadedCompletable)
        handleRadicalSearchQueries(radicalsLoadedCompletable)
        handleLoadMoreWordsRequests()
    }

    private fun prewarmAppDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            // Touch both characters and words tables so first real query hits warm caches
            runCatching { appDataRepository.getStrokes("あ") }
            runCatching { appDataRepository.getWordsWithText(text = "あ", limit = 1) }
        }
    }

    override fun search(input: String) {
        Logger.d("sending input $input")
        searchQueriesChannel.trySend(input)
    }

    override fun loadMoreWords() {
        Logger.d(">>")
        val currentState = state.value.words.value
        loadMoreWordsChannel.trySend(currentState.items.size)
    }

    override fun loadRadicalsData() {
        radicalsDataInitialLoadChannel.trySend(Unit)
    }

    override fun radicalsSearch(radicals: Set<String>) {
        radicalsSearchQueriesChannel.trySend(radicals)
    }

    private fun handleSearchQueries() = viewModelScope.launch {
        searchQueriesChannel.consumeAsFlow()
            .distinctUntilChanged()
            .onEach { input ->
                if (input.isBlank()) {
                    state.value = ScreenState(
                        isLoading = false,
                        characters = emptyList(),
                        words = mutableStateOf(PaginatableJapaneseWordList(0, emptyList())),
                        query = input
                    )
                } else {
                    state.value = state.value.copy(isLoading = true)
                }
            }
            .debounce { input -> if (input.isBlank()) 0L else 75L }
            .collectLatest { input ->
                if (input.isBlank()) return@collectLatest
                kotlin.runCatching {
                    Logger.d("start searching for $input")
                    val result = withContext(Dispatchers.IO) {
                        Logger.d("processing input $input in background")
                        processInputUseCase.process(input)
                    }
                    Logger.d("applying new state for $input")
                    state.value = result
                }.onFailure { Logger.d("search for $input was interrupted, reason[$it]") }
            }
    }

    private fun handleRadicalsLoading(radicalsLoadedCompletable: CompletableDeferred<Unit>) {
        radicalsDataInitialLoadChannel.consumeAsFlow()
            .take(1)
            .onEach {
                val updatedState = withContext(Dispatchers.IO) {
                    RadicalSearchState(
                        radicalsListItems = loadRadicalsUseCase.load(),
                        characterListItems = emptyList(),
                        isLoading = false
                    )
                }
                radicalsState.value = updatedState
                radicalsLoadedCompletable.complete(Unit)
            }
            .launchIn(viewModelScope)
    }

    private fun handleRadicalSearchQueries(
        radicalsLoadedCompletable: CompletableDeferred<Unit>
    ) = viewModelScope.launch {
        radicalsSearchQueriesChannel.consumeAsFlow()
            .distinctUntilChanged()
            .onStart { radicalsLoadedCompletable.await() }
            .onEach { radicalsState.value = radicalsState.value.copy(isLoading = true) }
            .collectLatest { radicals ->
                val currentState = radicalsState.value

                val updatedState = async(Dispatchers.IO) {
                    val searchResult = searchByRadicalsUseCase.search(radicals)
                    val updatedRadicals = updateEnabledRadicalsUseCase.update(
                        allRadicals = currentState.radicalsListItems,
                        selectedRadicals = radicals
                    )
                    RadicalSearchState(
                        radicalsListItems = updatedRadicals,
                        characterListItems = searchResult.listData,
                        isLoading = false
                    )
                }

                radicalsState.value = updatedState.await()
            }
    }

    private fun handleLoadMoreWordsRequests() = viewModelScope.launch {
        loadMoreWordsChannel.consumeAsFlow()
            .collect { loadMoreWordsUseCase.loadMore(state.value) }
    }

}