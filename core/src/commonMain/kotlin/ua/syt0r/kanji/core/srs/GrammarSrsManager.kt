package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.database.GrammarDeck
import ua.syt0r.kanji.core.user_data.database.GrammarPracticeRepository
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface GrammarSrsManager {
    val dataChangeFlow: SharedFlow<Unit>
    suspend fun getDecks(): GrammarSrsDecksData
    suspend fun getDeck(deckId: Long): GrammarSrsDeck
}

typealias GrammarSrsDecksData = SrsDecksData<GrammarSrsDeck, GrammarPracticeType>
typealias GrammarSrsDeckDescriptor = SrsDeckDescriptor<String, GrammarPracticeType>
typealias GrammarSrsDeckProgress = SrsDeckProgress<String>

data class GrammarSrsDeck(
    override val id: Long,
    override val title: String,
    override val position: Int,
    override val items: List<String>,
    override val lastReview: Instant?,
    override val progressMap: Map<GrammarPracticeType, GrammarSrsDeckProgress>,
    val deck: GrammarDeck
) : SrsDeckData<GrammarPracticeType, String>

class DefaultGrammarSrsManager(
    private val practiceRepository: GrammarPracticeRepository,
    private val srsCardRepository: SrsCardRepository,
    dailyLimitManager: DailyLimitManager,
    timeUtils: TimeUtils,
    appPreferences: PreferencesContract.AppPreferences,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    coroutineScope: CoroutineScope
) : SrsManager<String, GrammarPracticeType, GrammarSrsDeck>(
    deckChangesFlow = practiceRepository.changesFlow,
    srsChangesFlow = srsCardRepository.changesFlow,
    dailyLimitManager = dailyLimitManager,
    timeUtils = timeUtils,
    appPreferences = appPreferences,
    coroutineScope = coroutineScope
), GrammarSrsManager {

    private val decksByIdCache = mutableMapOf<Long, GrammarDeck>()

    override val practiceTypes: List<GrammarPracticeType> = GrammarPracticeType.entries

    override suspend fun getDecks(): GrammarSrsDecksData {
        return getDecksInternal()
    }

    override suspend fun getDeck(deckId: Long): GrammarSrsDeck {
        return getDecksInternal().decks.first { it.id == deckId }
    }

    override suspend fun getDeckDescriptors(): List<GrammarSrsDeckDescriptor> {
        val decks = practiceRepository.getDecks()
        decksByIdCache.clear()
        decks.forEach { decksByIdCache[it.id] = it }

        return decks.map { deck ->
            val items = practiceRepository.getPoints(deck.id).map { it.itemKey }
            val historyStats = reviewHistoryRepository
                .getReviewHistoryStatsForKeys(keys = items)

            val itemsDataMap = GrammarPracticeType.entries.associateWith { practiceType ->
                val itemsData: Map<String, SrsCardData> = items.associateWith { itemKey ->
                    val key = practiceType.toSrsKey(itemKey)
                    val card = srsCardRepository.get(key)
                    val practiceTypeStat = historyStats[itemKey]
                        ?.practiceTypeToDataMap
                        ?.get(practiceType.srsPracticeType.value)
                    SrsCardData(
                        key = key,
                        card = card,
                        status = getSrsStatus(card),
                        lapses = card?.fsrsCard?.lapses ?: 0,
                        repeats = card?.fsrsCard?.repeats ?: 0,
                        firstReview = practiceTypeStat?.firstReview,
                        firstReviewSrsDate = practiceTypeStat?.firstReview?.toSrsDate(),
                        lastReview = card?.lastReview,
                        lastReviewSrsDate = card?.lastReview?.toSrsDate(),
                        expectedReviewDate = card?.expectedReview?.toSrsDate()
                    )
                }
                PracticeTypeDeckData(itemsData = itemsData)
            }

            GrammarSrsDeckDescriptor(
                id = deck.id,
                title = deck.title,
                position = deck.position,
                lastReview = historyStats
                    .flatMap { it.value.practiceTypeToDataMap.values }
                    .maxOfOrNull { it.lastReview },
                items = items,
                itemsData = itemsDataMap
            )
        }
    }

    override suspend fun getDeckSortConfiguration(): DeckSortConfiguration {
        return DeckSortConfiguration(sortByReviewDate = false)
    }

    override suspend fun getDeckLimit(
        configuration: DailyLimitConfiguration,
        newDoneToday: Int,
        dueDoneToday: Int
    ): DeckLimit.EnabledDeckLimit {
        return when {
            configuration.isGrammarLimitCombined -> DeckLimit.Combined(
                limit = configuration.grammarCombinedLimit,
                newDone = newDoneToday,
                dueDone = dueDoneToday
            )

            else -> DeckLimit.Separate(
                limitsMap = configuration.grammarSeparatedLimit
            )
        }
    }

    override suspend fun createDeck(
        deckDescriptor: GrammarSrsDeckDescriptor,
        deckLimit: DeckLimit,
        currentSrsDate: LocalDate
    ): GrammarSrsDeck {
        return GrammarSrsDeck(
            id = deckDescriptor.id,
            title = deckDescriptor.title,
            position = deckDescriptor.position,
            lastReview = deckDescriptor.lastReview,
            items = deckDescriptor.items,
            progressMap = deckDescriptor.itemsData.mapValues { (practiceType, deckData) ->
                deckData.toProgress(deckLimit, practiceType, currentSrsDate)
            },
            deck = decksByIdCache.getValue(deckDescriptor.id)
        )
    }

}
