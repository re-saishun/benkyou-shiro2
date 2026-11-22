package ua.syt0r.kanji.presentation.screen.main.screen.text_analysis

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.core.AccountManager
import ua.syt0r.kanji.core.AccountState
import ua.syt0r.kanji.core.ApiTextAnalysisRequest
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.japanese.isKanji
import ua.syt0r.kanji.core.launcherLambda
import ua.syt0r.kanji.core.user_data.database.TextAnalysisData
import ua.syt0r.kanji.core.user_data.database.TextAnalysisRepository
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTextAnalysisProvider
import ua.syt0r.kanji.error_unknown
import ua.syt0r.kanji.presentation.common.paginateable
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.TextAnalysisContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.CreateAnalysisResultUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.GeminiTextAnalysisUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.ParseIchiranResponseUseCase
import ua.syt0r.kanji.text_analysis_preview_text
import ua.syt0r.kanji.text_analysis_preview_translation

@OptIn(ExperimentalCoroutinesApi::class)
class TextAnalysisViewModel(
    private val viewModelScope: CoroutineScope,
    private val accountManager: AccountManager,
    private val analysisRepository: TextAnalysisRepository,
    private val parseIchiranResponseUseCase: ParseIchiranResponseUseCase,
    private val createAnalysisResultUseCase: CreateAnalysisResultUseCase,
    private val networkApi: NetworkApi,
    private val geminiTextAnalysisUseCase: GeminiTextAnalysisUseCase,
    private val appPreferences: PreferencesContract.AppPreferences
) {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val state: StateFlow<ScreenState> = _state

    private val _configuration = MutableStateFlow<TextAnalysisConfigurationData?>(null)
    private val configuration: StateFlow<TextAnalysisConfigurationData> = _configuration
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
            initialValue = TextAnalysisConfigurationData(
                provider = PreferencesTextAnalysisProvider.Automatic,
                geminiApiKey = ""
            )
        )

    init {

        viewModelScope.launch {

            val initialConfig = TextAnalysisConfigurationData(
                provider = appPreferences.textAnalysisProvider.get(),
                geminiApiKey = appPreferences.textAnalysisGeminiApiKey.get()
            )
            _configuration.value = initialConfig

            combine(
                accountManager.state,
                _configuration.filterNotNull()
            ) { accountState, config ->
                accountState to config
            }
                .onEach { (accountState, config) ->
                    when {
                        config.provider == PreferencesTextAnalysisProvider.Gemini -> {
                            _state.value = ScreenState.Loading
                            _state.value = if (config.geminiApiKey.isNotBlank()) {
                                createNormalState()
                            } else {
                                createPreviewState(geminiKeyMissing = true)
                            }
                        }

                        accountState is AccountState.Loading -> {
                            _state.value = ScreenState.Loading
                        }

                        accountState is AccountState.LoggedOut || accountState is AccountState.Error -> {
                            _state.value = ScreenState.Loading
                            _state.value = createPreviewState(geminiKeyMissing = false)
                        }

                        accountState is AccountState.LoggedIn -> {
                            _state.value = ScreenState.Loading
                            _state.value = createNormalState()
                        }
                    }
                }
                .launchIn(this)

        }

    }

    private fun updateConfiguration(
        provider: PreferencesTextAnalysisProvider,
        geminiApiKey: String
    ) {
        val normalizedKey = geminiApiKey.trim()
        viewModelScope.launch {
            appPreferences.textAnalysisProvider.set(provider)
            appPreferences.textAnalysisGeminiApiKey.set(normalizedKey)
            _configuration.value = TextAnalysisConfigurationData(
                provider = provider,
                geminiApiKey = normalizedKey
            )
        }
    }

    private suspend fun createPreviewState(
        geminiKeyMissing: Boolean
    ): ScreenState.Loaded {

        val inputState: StateFlow<TextAnalysisInputState> = MutableStateFlow(
            if (geminiKeyMissing) TextAnalysisInputState.GeminiKeyMissing
            else TextAnalysisInputState.NotEligible
        )

        val historyState = analysisRepository.changesFlow
            .onStart { emit(Unit) }
            .map { getLatestHistory() }
            .stateIn(viewModelScope)

        val displayedResultInitial = if (historyState.value.total != 0) {
            null
        } else {
            createAnalysisResultUseCase(
                text = getString(Res.string.text_analysis_preview_text),
                translation = getString(Res.string.text_analysis_preview_translation),
                nodeList = Res.readBytes(TextAnalysisContract.PREVIEW_RES_PATH)
                    .decodeToString()
                    .let { parseIchiranResponseUseCase(Json.decodeFromString(it)) }
            )
        }

        val displayedResult = MutableStateFlow<TextAnalysisResult?>(displayedResultInitial)

        val contentState: StateFlow<TextAnalysisContentState> = displayedResult
            .flatMapLatest { createContentStateFlow(it) }
            .stateIn(viewModelScope)

        return ScreenState.Loaded(
            contentState = contentState,
            inputState = inputState,
            history = historyState,
            configuration = configuration,
            setContent = { displayedResult.value = it },
            updateConfiguration = ::updateConfiguration
        )
    }

    private suspend fun createNormalState(): ScreenState.Loaded {
        val displayedResult = MutableStateFlow<TextAnalysisResult?>(null)

        val contentState: StateFlow<TextAnalysisContentState> = displayedResult
            .flatMapLatest { createContentStateFlow(it) }
            .stateIn(viewModelScope)

        val requestsChannel = Channel<String>()

        val requestInputFlow = requestsChannel.consumeAsFlow().transform { input ->
            emit(TextAnalysisInputState.Loading(input))
            val analysisResult = analyzeText(input)
            displayedResult.emit(analysisResult)
            val resultInputState = when (analysisResult) {
                is TextAnalysisResult.Success -> createNewInputState(requestsChannel)
                is TextAnalysisResult.Error -> createNewInputState(requestsChannel, input)
            }
            emit(resultInputState)
        }

        val inputState: StateFlow<TextAnalysisInputState> = requestInputFlow
            .onStart { emit(createNewInputState(requestsChannel)) }
            .stateIn(viewModelScope)

        val historyState = analysisRepository.changesFlow
            .onStart { emit(Unit) }
            .map { getLatestHistory() }
            .stateIn(viewModelScope)

        return ScreenState.Loaded(
            contentState = contentState,
            inputState = inputState,
            history = historyState,
            configuration = configuration,
            setContent = { displayedResult.value = it },
            updateConfiguration = ::updateConfiguration
        )
    }

    private suspend fun analyzeText(input: String): TextAnalysisResult {
        val currentConfig = _configuration.value ?: TextAnalysisConfigurationData(
            provider = appPreferences.textAnalysisProvider.get(),
            geminiApiKey = appPreferences.textAnalysisGeminiApiKey.get()
        )

        val apiResponseResult = when (currentConfig.provider) {
            PreferencesTextAnalysisProvider.Automatic -> {
                networkApi.postTextAnalysisRequest(ApiTextAnalysisRequest(text = input))
                    .map { it.value }
            }

            PreferencesTextAnalysisProvider.Gemini -> {
                geminiTextAnalysisUseCase(
                    input = input,
                    apiKey = currentConfig.geminiApiKey
                )
            }
        }

        return apiResponseResult
            .map { response ->
                val textAnalysisData = TextAnalysisData(
                    text = input,
                    timestamp = Clock.System.now(),
                    translation = response.translation,
                    annotatedTextJson = Json.Default.encodeToString(response.elements)
                )

                val result = createAnalysisResultUseCase(
                    text = input,
                    translation = response.translation,
                    nodeList = parseIchiranResponseUseCase(response.elements)
                )

                analysisRepository.add(textAnalysisData)
                result
            }
            .getOrElse {
                TextAnalysisResult.Error(
                    message = it.message ?: getString(Res.string.error_unknown)
                )
            }
    }

    private fun createContentStateFlow(
        result: TextAnalysisResult?
    ) = channelFlow<TextAnalysisContentState> {

        if (result == null) {
            send(TextAnalysisContentState.Empty)
            return@channelFlow
        }

        val changeContentModeRequestsChannel = Channel<String>()

        val browseContentMode = TextAnalysisContentMode.Browse(
            furigana = mutableStateOf(true),
            highlight = mutableStateOf(true),
            alternativeWords = result.let { it as? TextAnalysisResult.Success }
                ?.alternativeWords
                ?: emptySet(),
            switchToSaveLettersMode = launcherLambda {
                val name = TextAnalysisContentMode.SaveLetters::class.simpleName!!
                changeContentModeRequestsChannel.send(name)
            }
        )

        val contentMode = mutableStateOf<TextAnalysisContentMode>(browseContentMode)

        val state = TextAnalysisContentState.Loaded(
            contentMode = contentMode,
            result = result
        )
        send(state)

        changeContentModeRequestsChannel.consumeAsFlow().collect { className ->
            contentMode.value = when (className) {
                TextAnalysisContentMode.Browse::class.simpleName -> browseContentMode

                TextAnalysisContentMode.SaveLetters::class.simpleName -> {
                    createSaveLettersContentMode(
                        result = result as TextAnalysisResult.Success,
                        switchToBrowseMode = launcherLambda {
                            val name = TextAnalysisContentMode.Browse::class.simpleName!!
                            changeContentModeRequestsChannel.send(name)
                        }
                    )
                }

                else -> error("Invalid content mode value[$className]")
            }

        }

    }

    private fun createSaveLettersContentMode(
        result: TextAnalysisResult.Success,
        switchToBrowseMode: () -> Unit
    ): TextAnalysisContentMode.SaveLetters {
        val lettersSet = result.letters.toSet()
        val selected = mutableStateOf(emptySet<String>())

        return TextAnalysisContentMode.SaveLetters(
            letters = result.letters,
            selected = selected,
            toggleSelection = { word ->
                selected.value = selected.value.let {
                    if (it.contains(word)) it.minus(word)
                    else it.plus(word)
                }
            },
            selectAll = { selected.value = lettersSet },
            selectNone = { selected.value = emptySet() },
            selectAllKanji = {
                selected.value = selected.value.plus(
                    lettersSet.filter { it.first().isKanji() }
                )
            },
            switchToBrowseMode = switchToBrowseMode
        )
    }

    private fun createNewInputState(
        submitRequestsChannel: Channel<String>,
        input: String = ""
    ): TextAnalysisInputState {
        val input = mutableStateOf(input)
        return TextAnalysisInputState.Typing(
            input = input,
            isInputValid = derivedStateOf {
                input.value.length <= TextAnalysisContract.INPUT_LIMIT
            },
            submit = { viewModelScope.launch { submitRequestsChannel.send(input.value) } }
        )
    }

    private suspend fun getLatestHistory() = paginateable<TextAnalysisResult.Success>(
        coroutineScope = viewModelScope,
        limit = analysisRepository.getCount().toInt(),
        loadMoreImmediately = true
    ) { offset ->
        analysisRepository.get(
            offset = offset.toLong(),
            limit = 10
        ).map {
            val nodeList = parseIchiranResponseUseCase(
                Json.Default.decodeFromString(it.annotatedTextJson)
            )
            createAnalysisResultUseCase(
                text = it.text,
                translation = it.translation,
                nodeList = nodeList
            )
        }
    }

}
