package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.use_case

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.logger.runWithTimeLog
import ua.syt0r.kanji.core.mergeSharedFlows
import ua.syt0r.kanji.core.refreshableDataProducerFlow
import ua.syt0r.kanji.core.srs.GrammarPracticeType
import ua.syt0r.kanji.core.srs.GrammarSrsDeck
import ua.syt0r.kanji.core.srs.GrammarSrsManager
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.LetterSrsDeck
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.srs.SrsDecksData
import ua.syt0r.kanji.core.srs.VocabPracticeType
import ua.syt0r.kanji.core.srs.VocabSrsDeck
import ua.syt0r.kanji.core.srs.VocabSrsManager
import ua.syt0r.kanji.core.streak.DailyStreakTracker
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.database.StreakData
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.common.ScreenGrammarPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GrammarStudyTargetState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.StreakCalendarItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.StudyTarget
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.StudyTargetPracticeOptions
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.StudyTargetProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.StudyTargetState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration
import kotlin.time.Duration.Companion.days

interface SubscribeOnGeneralDashboardScreenDataUseCase {

    operator fun invoke(
        coroutineScope: CoroutineScope, lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<ScreenState.Loaded>>

}

class DefaultSubscribeOnGeneralDashboardScreenDataUseCase(
    private val letterSrsManager: LetterSrsManager,
    private val vocabSrsManager: VocabSrsManager,
    private val grammarSrsManager: GrammarSrsManager,
    private val preferencesRepository: PreferencesContract.AppPreferences,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val timeUtils: TimeUtils,
    private val dailyStreakTracker: DailyStreakTracker
) : SubscribeOnGeneralDashboardScreenDataUseCase {

    override fun invoke(
        coroutineScope: CoroutineScope,
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<ScreenState.Loaded>> = refreshableDataProducerFlow(
        dataChangeFlow = mergeSharedFlows(
            coroutineScope,
            letterSrsManager.dataChangeFlow,
            vocabSrsManager.dataChangeFlow,
            grammarSrsManager.dataChangeFlow,
            preferencesRepository.dailyStreak.onModified.map { }
        ),
        lifecycleState = lifecycleState,
        producer = { withContext(Dispatchers.IO) { produceState() } }
    )

    private suspend fun ProducerScope<ScreenState.Loaded>.produceState() {
        Logger.d("produceState")

        val deferredLettersDecks = async {
            runWithTimeLog("letterDecksData") { letterSrsManager.getDecks() }
        }

        val deferredVocabDecks = async {
            runWithTimeLog("vocabDecksData") { vocabSrsManager.getDecks() }
        }

        val deferredGrammarDecks = async {
            runWithTimeLog("grammarDecksData") { grammarSrsManager.getDecks() }
        }

        val deferredStreakData = async {
            runWithTimeLog("streakData") { getStats() }
        }

        val preferencesStudyTargets = preferencesRepository.generalDashboardStudyTargets.get()
            .mapNotNull { (name, enabled) ->
                val target = StudyTarget.entries.find { it.name == name } ?: return@mapNotNull null
                target to enabled
            }

        val missingStudyTargets = StudyTarget.entries
            .minus(preferencesStudyTargets.map { it.first })
            .map { it to false }

        val studyTargets = mutableStateOf(
            preferencesStudyTargets.plus(missingStudyTargets).map { (studyTarget, enabled) ->
                async {
                    StudyTargetState(
                        studyTarget = studyTarget,
                        enabled = enabled,
                        progress = when (studyTarget.practiceType) {
                            is LetterPracticeType -> getPracticeTypeProgress(
                                decksData = deferredLettersDecks.await(),
                                practiceType = studyTarget.practiceType
                            )

                            is VocabPracticeType -> getPracticeTypeProgress(
                                decksData = deferredVocabDecks.await(),
                                practiceType = studyTarget.practiceType
                            )

                            // Grammar is displayed in its own row, not as a configurable target
                            is GrammarPracticeType -> getPracticeTypeProgress(
                                decksData = deferredGrammarDecks.await(),
                                practiceType = studyTarget.practiceType
                            )
                        }
                    )
                }
            }.awaitAll()
        )

        val grammarDecks = deferredGrammarDecks.await()
        val grammarTargets = GrammarPracticeType.entries.map { practiceType ->
            GrammarStudyTargetState(
                practiceType = ScreenGrammarPracticeType.from(practiceType),
                progress = getPracticeTypeProgress(grammarDecks, practiceType)
            )
        }

        val state = ScreenState.Loaded(
            studyTargets = studyTargets,
            grammarTargets = grammarTargets,
            stats = deferredStreakData.await()
        )
        send(state)

        snapshotFlow { studyTargets.value }
            .drop(1)
            .onEach {
                val updatedMap = it.associate { it.studyTarget.name to it.enabled }
                preferencesRepository.generalDashboardStudyTargets.set(updatedMap)
            }
            .launchIn(this)

    }

    private fun CoroutineScope.getPracticeTypeProgress(
        decksData: SrsDecksData<LetterSrsDeck, LetterPracticeType>,
        practiceType: LetterPracticeType
    ): StudyTargetProgress {
        if (decksData.decks.isEmpty()) return StudyTargetProgress.NoDecks

        val combinedDailyNew = mutableMapOf<String, Long>()
        val combinedDailyDue = mutableMapOf<String, Long>()
        val combinedNotNew = mutableSetOf<String>()

        decksData.decks
            .map { it.id to it.progressMap.getValue(practiceType) }
            .forEach { (deckId, srsProgress) ->
                srsProgress.dailyNew.associateWith { deckId }
                    .forEach { combinedDailyNew[it.key] = it.value }
                srsProgress.dailyDue.associateWith { deckId }
                    .forEach { combinedDailyDue[it.key] = it.value }

                combinedNotNew.addAll(srsProgress.due)
                combinedNotNew.addAll(srsProgress.done)
            }

        val leftover = decksData.dailyProgress.leftoversByPracticeTypeMap.getValue(practiceType)

        fun Pair<String, Long>.mapToCard() = LetterPracticeScreenConfiguration.Card(first, second)

        val resultNew = combinedDailyNew.toList().take(leftover.new).map { it.mapToCard() }
        val dueCards = combinedDailyDue.toList().take(leftover.due).map { it.mapToCard() }
        val combinedCards = resultNew.plus(dueCards)

        return StudyTargetProgress.WithDecks(
            options = StudyTargetPracticeOptions(
                newCards = resultNew,
                dueCards = dueCards,
                combinedCards = combinedCards
            ),
            totalProgress = getProgress(decksData.uniqueCardsCount, combinedNotNew.size),
            decksCount = decksData.decks.size
        )
    }

    private fun CoroutineScope.getPracticeTypeProgress(
        decksData: SrsDecksData<VocabSrsDeck, VocabPracticeType>,
        practiceType: VocabPracticeType
    ): StudyTargetProgress {
        if (decksData.decks.isEmpty()) return StudyTargetProgress.NoDecks

        val combinedDailyNew = mutableMapOf<Long, Long>()
        val combinedDailyDue = mutableMapOf<Long, Long>()
        val combinedNotNew = mutableSetOf<Long>()

        decksData.decks
            .map { it.id to it.progressMap.getValue(practiceType) }
            .forEach { (deckId, srsProgress) ->
                srsProgress.dailyNew.associateWith { deckId }
                    .forEach { combinedDailyNew[it.key] = it.value }
                srsProgress.dailyDue.associateWith { deckId }
                    .forEach { combinedDailyDue[it.key] = it.value }

                combinedNotNew.addAll(srsProgress.due)
                combinedNotNew.addAll(srsProgress.done)
            }

        val leftover = decksData.dailyProgress.leftoversByPracticeTypeMap.getValue(practiceType)

        fun Pair<Long, Long>.mapToCard() = VocabPracticeScreenConfiguration.Card(first, second)

        val resultNew = combinedDailyNew.toList().take(leftover.new).map { it.mapToCard() }
        val dueCards = combinedDailyDue.toList().take(leftover.due).map { it.mapToCard() }
        val combinedCards = resultNew.plus(dueCards)

        return StudyTargetProgress.WithDecks(
            options = StudyTargetPracticeOptions(
                newCards = resultNew,
                dueCards = dueCards,
                combinedCards = combinedCards
            ),
            totalProgress = getProgress(decksData.uniqueCardsCount, combinedNotNew.size),
            decksCount = decksData.decks.size
        )
    }

    private fun getPracticeTypeProgress(
        decksData: SrsDecksData<GrammarSrsDeck, GrammarPracticeType>,
        practiceType: GrammarPracticeType
    ): StudyTargetProgress {
        if (decksData.decks.isEmpty()) return StudyTargetProgress.NoDecks

        val combinedDailyNew = mutableMapOf<String, Long>()
        val combinedDailyDue = mutableMapOf<String, Long>()
        val combinedNotNew = mutableSetOf<String>()

        decksData.decks
            .map { it.id to it.progressMap.getValue(practiceType) }
            .forEach { (deckId, srsProgress) ->
                srsProgress.dailyNew.associateWith { deckId }
                    .forEach { combinedDailyNew[it.key] = it.value }
                srsProgress.dailyDue.associateWith { deckId }
                    .forEach { combinedDailyDue[it.key] = it.value }

                combinedNotNew.addAll(srsProgress.due)
                combinedNotNew.addAll(srsProgress.done)
            }

        val leftover = decksData.dailyProgress.leftoversByPracticeTypeMap.getValue(practiceType)

        fun Pair<String, Long>.mapToCard() = GrammarPracticeScreenConfiguration.Card(first, second)

        val resultNew = combinedDailyNew.toList().take(leftover.new).map { it.mapToCard() }
        val dueCards = combinedDailyDue.toList().take(leftover.due).map { it.mapToCard() }
        val combinedCards = resultNew.plus(dueCards)

        return StudyTargetProgress.WithDecks(
            options = StudyTargetPracticeOptions(
                newCards = resultNew,
                dueCards = dueCards,
                combinedCards = combinedCards
            ),
            totalProgress = getProgress(decksData.uniqueCardsCount, combinedNotNew.size),
            decksCount = decksData.decks.size
        )
    }

    private suspend fun getStats(): GeneralDashboardStats {
        val streakData = dailyStreakTracker.getStreakData()
        val motivation = dailyStreakTracker.getMotivation()

        val todayStart = timeUtils.getCurrentDate()
            .atStartOfDayIn(TimeZone.currentSystemDefault())
        val todayEnd = todayStart.plus(1.days)

        val reviewsToday = reviewHistoryRepository
            .getReviews(start = todayStart, end = todayEnd)
            .size

        val newItemsToday = reviewHistoryRepository
            .getNewReviewItemsCount(start = todayStart, end = todayEnd)
            .toInt()

        return GeneralDashboardStats(
            currentStreak = streakData.currentStreak,
            longestStreak = streakData.longestStreak,
            reviewsToday = reviewsToday,
            newItemsToday = newItemsToday,
            totalPracticeDays = streakData.totalPracticeDays,
            streakStatus = motivation.status,
            motivationTitle = motivation.title,
            motivationMessage = motivation.message,
            isMilestone = motivation.isMilestone
        )
    }

    private fun getProgress(uniqueCardsCount: Int, notNewCardsCount: Int): Float = uniqueCardsCount
        .takeIf { it != 0 }
        ?.let { notNewCardsCount.toFloat() / it }
        ?: 0f

    companion object {
        private const val STREAK_CALENDAR_DAYS = 7
    }

}