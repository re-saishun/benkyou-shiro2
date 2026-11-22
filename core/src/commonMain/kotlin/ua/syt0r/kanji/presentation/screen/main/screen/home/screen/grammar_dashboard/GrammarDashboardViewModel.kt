package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.grammar_dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.grammar.GrammarCatalogService
import ua.syt0r.kanji.core.grammar.RemoteGrammarPack
import ua.syt0r.kanji.core.refreshableDataProducerFlow
import ua.syt0r.kanji.core.srs.GrammarPracticeType
import ua.syt0r.kanji.core.srs.GrammarSrsManager
import ua.syt0r.kanji.core.user_data.database.GrammarPracticeRepository
import ua.syt0r.kanji.presentation.LifecycleAwareViewModel
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.common.ScreenGrammarPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.grammar_dashboard.GrammarDashboardScreenContract.CatalogState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.grammar_dashboard.GrammarDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration

class GrammarDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    private val grammarSrsManager: GrammarSrsManager,
    private val catalogService: GrammarCatalogService,
    private val repository: GrammarPracticeRepository
) : GrammarDashboardScreenContract.ViewModel,
    LifecycleAwareViewModel {

    override val lifecycleState = MutableStateFlow(LifecycleState.Hidden)

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val screenState: StateFlow<ScreenState> = _screenState

    private val _catalogState = MutableStateFlow<CatalogState>(CatalogState.Idle)
    override val catalogState: StateFlow<CatalogState> = _catalogState

    private var catalogJob: Job? = null

    init {
        refreshableDataProducerFlow<ScreenState.Loaded>(
            dataChangeFlow = grammarSrsManager.dataChangeFlow,
            lifecycleState = lifecycleState,
            producer = {
                val state = withContext(Dispatchers.IO) { produceState() }
                send(state)
            }
        )
            .onEach {
                when (it) {
                    is RefreshableData.Loading -> _screenState.value = ScreenState.Loading
                    is RefreshableData.Loaded -> _screenState.value = it.value
                }
            }
            .launchIn(viewModelScope)
    }

    override fun loadCatalog(url: String?) {
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            val targetUrl = url?.takeIf { it.isNotBlank() } ?: catalogService.getSavedCatalogUrl()
            _catalogState.value = CatalogState.Loading(targetUrl)
            catalogService.fetchCatalog(targetUrl)
                .onSuccess { _catalogState.value = CatalogState.Loaded(targetUrl, it) }
                .onFailure {
                    _catalogState.value = CatalogState.Error(targetUrl, it.message ?: "Error")
                }
        }
    }

    override fun downloadPack(pack: RemoteGrammarPack) {
        val current = _catalogState.value as? CatalogState.Loaded ?: return
        val packId = pack.entry.id
        if (packId in current.downloadingIds) return

        _catalogState.value = current.copy(
            downloadingIds = current.downloadingIds + packId,
            error = null
        )

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { catalogService.downloadPack(pack) }

            if (result.isSuccess) {
                // Refresh pack statuses (Downloaded / UpdateAvailable)
                val refreshed = withContext(Dispatchers.IO) {
                    catalogService.fetchCatalog(current.url)
                }
                _catalogState.update { state ->
                    if (state !is CatalogState.Loaded) return@update state
                    state.copy(
                        packs = refreshed.getOrDefault(state.packs),
                        downloadingIds = state.downloadingIds - packId
                    )
                }
            } else {
                val message = result.exceptionOrNull()?.message ?: "Error"
                _catalogState.update { state ->
                    if (state !is CatalogState.Loaded) return@update state
                    state.copy(
                        downloadingIds = state.downloadingIds - packId,
                        error = message
                    )
                }
            }
        }
    }

    override fun createEmptyDeck(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.createEmptyDeck(trimmed)
        }
    }

    override fun deleteDeck(deckId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDeck(deckId)
        }
    }

    private suspend fun produceState(): ScreenState.Loaded {
        val decksData = grammarSrsManager.getDecks()

        val items = decksData.decks.map { srsDeck ->
            GrammarDeckItem(
                deckId = srsDeck.id,
                title = srsDeck.title,
                level = srsDeck.deck.level,
                description = srsDeck.deck.description,
                pointCount = srsDeck.items.size,
                practiceItems = GrammarPracticeType.entries.map { practiceType ->
                    val progress = srsDeck.progressMap.getValue(practiceType)
                    GrammarDeckPracticeItem(
                        practiceType = ScreenGrammarPracticeType.from(practiceType),
                        newCards = progress.dailyNew.map {
                            GrammarPracticeScreenConfiguration.Card(it, srsDeck.id)
                        },
                        dueCards = progress.dailyDue.map {
                            GrammarPracticeScreenConfiguration.Card(it, srsDeck.id)
                        }
                    )
                }
            )
        }

        return ScreenState.Loaded(items)
    }

}
