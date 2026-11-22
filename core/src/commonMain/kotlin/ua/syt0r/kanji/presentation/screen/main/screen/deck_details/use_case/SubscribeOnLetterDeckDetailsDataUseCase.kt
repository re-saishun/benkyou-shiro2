package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.LetterSrsDeck
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.srs.SrsCardData
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.PracticeItemSummary
import kotlin.coroutines.CoroutineContext
import kotlin.time.measureTime

interface SubscribeOnDeckDetailsDataUseCase {
    operator fun invoke(
        configuration: DeckDetailsScreenConfiguration.LetterDeck,
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<DeckDetailsData.LetterDeckData>>
}

class DefaultSubscribeOnDeckDetailsDataUseCase(
    private val letterSrsManager: LetterSrsManager,
    private val appDataRepository: AppDataRepository,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : SubscribeOnDeckDetailsDataUseCase {

    override operator fun invoke(
        configuration: DeckDetailsScreenConfiguration.LetterDeck,
        lifecycleState: StateFlow<LifecycleState>,
    ): Flow<RefreshableData<DeckDetailsData.LetterDeckData>> {
        val deckId = configuration.deckId
        return refreshableDataFlow(
            dataChangeFlow = letterSrsManager.dataChangeFlow,
            lifecycleState = lifecycleState,
            valueProvider = {
                var data: DeckDetailsData.LetterDeckData
                val timeToRefreshData = measureTime { data = getUpdatedData(deckId) }
                Logger.d("timeToRefreshData[$timeToRefreshData]")
                data
            }
        )
    }

    private suspend fun getUpdatedData(
        deckId: Long
    ): DeckDetailsData.LetterDeckData = withContext(coroutineContext) {
        Logger.logMethod()

        val deck = letterSrsManager.getDeck(deckId)

        val progressMaps = ScreenLetterPracticeType.entries.associateWith {
            deck.progressMap.getValue(it.dataType).itemsData
        }

        val timeZone = TimeZone.currentSystemDefault()

        val items = deck.items.mapIndexed { index, character ->

            val summaryMap = progressMaps.mapValues { (_, itemsData) ->
                val cardData = itemsData.getValue(character)
                PracticeItemSummary(
                    firstReviewDate = cardData.firstReview?.toLocalDateTime(timeZone),
                    lastReviewDate = cardData.lastReview?.toLocalDateTime(timeZone),
                    expectedReviewDate = cardData.expectedReviewDate,
                    lapses = cardData.lapses,
                    repeats = cardData.repeats,
                    srsItemStatus = cardData.status
                )
            }

            DeckDetailsItemData.LetterData(
                character = character,
                positionInPractice = index,
                frequency = appDataRepository.getData(character)?.frequency,
                summaryMap = summaryMap
            )
        }

        DeckDetailsData.LetterDeckData(
            deckTitle = deck.title,
            items = items,
            sharableDeckData = items.joinToString("") { it.character }
        )

    }

}