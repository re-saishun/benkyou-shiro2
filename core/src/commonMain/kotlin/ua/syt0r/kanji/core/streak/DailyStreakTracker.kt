package ua.syt0r.kanji.core.streak

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import kotlin.math.max

interface DailyStreakTracker {
    val streakData: Flow<DailyStreakData>
    suspend fun getStreakData(): DailyStreakData
    suspend fun getStreakStatus(): StreakStatus
    suspend fun getMotivation(): StreakMotivation
    suspend fun recordPractice(timestamp: Instant = timeUtils.now()): DailyStreakData
    suspend fun checkAndUpdateStreakExpiration(): DailyStreakData
    val timeUtils: TimeUtils
}

class DefaultDailyStreakTracker(
    private val appPreferences: PreferencesContract.AppPreferences,
    override val timeUtils: TimeUtils,
    private val reviewHistoryRepository: ReviewHistoryRepository
) : DailyStreakTracker {

    private val mutex = Mutex()

    init {
        reviewHistoryRepository.setOnReviewAddedListener { review ->
            recordPractice(review.timestamp)
        }
    }

    override val streakData: Flow<DailyStreakData> = flow {
        emit(getStreakData())
        emitAll(appPreferences.dailyStreak.onModified)
    }

    override suspend fun getStreakData(): DailyStreakData = mutex.withLock {
        val current = appPreferences.dailyStreak.get()
        if (current.lastPracticeDate == null) {
            // Seed from existing database reviews if available
            val seeded = seedFromReviewHistory(current)
            if (seeded != null) {
                appPreferences.dailyStreak.set(seeded)
                return@withLock checkExpirationInternal(seeded)
            }
        }
        return@withLock checkExpirationInternal(current)
    }

    override suspend fun getStreakStatus(): StreakStatus {
        val data = getStreakData()
        val resetTime = appPreferences.dailyResetTime.get()
        val today = getEffectiveDate(timeUtils.now(), resetTime)
        return calculateStreakStatus(data, today)
    }

    override suspend fun getMotivation(): StreakMotivation {
        val data = getStreakData()
        val resetTime = appPreferences.dailyResetTime.get()
        val today = getEffectiveDate(timeUtils.now(), resetTime)
        val status = calculateStreakStatus(data, today)
        return getMotivationalInfo(data, status)
    }

    override suspend fun recordPractice(timestamp: Instant): DailyStreakData = mutex.withLock {
        val resetTime = appPreferences.dailyResetTime.get()
        val effectiveDate = getEffectiveDate(timestamp, resetTime)
        val effectiveDateStr = effectiveDate.toString()

        var currentData = appPreferences.dailyStreak.get()
        if (currentData.lastPracticeDate == null) {
            val seeded = seedFromReviewHistory(currentData)
            if (seeded != null) {
                currentData = seeded
            }
        }

        val lastDate = currentData.lastPracticeDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

        val (newCurrentStreak, newLongestStreak, newTotalDays) = when {
            lastDate == null -> {
                // First recorded practice
                Triple(1, max(currentData.longestStreak, 1), 1)
            }
            lastDate == effectiveDate -> {
                // Already practiced on this logical day
                Triple(
                    max(currentData.currentStreak, 1),
                    max(currentData.longestStreak, max(currentData.currentStreak, 1)),
                    max(currentData.totalPracticeDays, 1)
                )
            }
            effectiveDate.toEpochDays() - lastDate.toEpochDays() == 1 -> {
                // Consecutive day! Streak increases
                val updatedStreak = currentData.currentStreak + 1
                Triple(
                    updatedStreak,
                    max(currentData.longestStreak, updatedStreak),
                    currentData.totalPracticeDays + 1
                )
            }
            effectiveDate.toEpochDays() - lastDate.toEpochDays() > 1 -> {
                // Streak was broken, start new streak
                val updatedStreak = 1
                Triple(
                    updatedStreak,
                    max(currentData.longestStreak, updatedStreak),
                    currentData.totalPracticeDays + 1
                )
            }
            else -> {
                // effectiveDate < lastDate (e.g. clock change or syncing older review)
                Triple(
                    max(currentData.currentStreak, 1),
                    currentData.longestStreak,
                    currentData.totalPracticeDays
                )
            }
        }

        val updatedData = currentData.copy(
            currentStreak = newCurrentStreak,
            longestStreak = newLongestStreak,
            lastPracticeDate = effectiveDateStr,
            lastPracticeTimestamp = timestamp.toEpochMilliseconds(),
            totalPracticeDays = newTotalDays
        )

        Logger.d("DailyStreakTracker: recordPractice date=$effectiveDateStr streak=$newCurrentStreak longest=$newLongestStreak")
        appPreferences.dailyStreak.set(updatedData)
        updatedData
    }

    override suspend fun checkAndUpdateStreakExpiration(): DailyStreakData = mutex.withLock {
        val current = appPreferences.dailyStreak.get()
        checkExpirationInternal(current)
    }

    private suspend fun checkExpirationInternal(current: DailyStreakData): DailyStreakData {
        val lastDateStr = current.lastPracticeDate ?: return current
        val lastDate = runCatching { LocalDate.parse(lastDateStr) }.getOrNull() ?: return current

        val resetTime = appPreferences.dailyResetTime.get()
        val today = getEffectiveDate(timeUtils.now(), resetTime)
        val daysDiff = today.toEpochDays() - lastDate.toEpochDays()

        if (daysDiff > 1 && current.currentStreak > 0) {
            // Missed at least one day
            val expired = current.copy(currentStreak = 0)
            Logger.d("DailyStreakTracker: Streak expired. lastDate=$lastDate today=$today")
            appPreferences.dailyStreak.set(expired)
            return expired
        }

        return current
    }

    private suspend fun seedFromReviewHistory(current: DailyStreakData): DailyStreakData? {
        return runCatching {
            val resetTime = appPreferences.dailyResetTime.get()
            val streaks = reviewHistoryRepository.getStreaks(resetTime)
            if (streaks.isEmpty()) return null

            val longest = streaks.maxByOrNull { it.length }?.length ?: 0
            val today = getEffectiveDate(timeUtils.now(), resetTime)
            val yesterday = today.minus(1, DateTimeUnit.DAY)

            val activeStreak = streaks.find { streak ->
                today in streak.start..streak.end || yesterday in streak.start..streak.end
            }

            val currentStreak = activeStreak?.length ?: 0
            val latestEnd = streaks.maxByOrNull { it.end }?.end?.toString()

            val seeded = current.copy(
                currentStreak = currentStreak,
                longestStreak = max(current.longestStreak, longest),
                lastPracticeDate = latestEnd,
                totalPracticeDays = streaks.sumOf { it.length }
            )
            Logger.d("DailyStreakTracker: Seeded from review history: current=$currentStreak longest=$longest")
            seeded
        }.getOrNull()
    }

    companion object {

        fun getEffectiveDate(
            instant: Instant,
            resetTime: LocalTime,
            timeZone: TimeZone = TimeZone.currentSystemDefault()
        ): LocalDate {
            val offsetMillis = resetTime.toMillisecondOfDay().toLong()
            val adjustedEpochMillis = instant.toEpochMilliseconds() - offsetMillis
            val adjustedInstant = Instant.fromEpochMilliseconds(adjustedEpochMillis)
            return adjustedInstant.toLocalDateTime(timeZone).date
        }

        fun calculateStreakStatus(
            streakData: DailyStreakData,
            currentEffectiveDate: LocalDate
        ): StreakStatus {
            val lastDateStr = streakData.lastPracticeDate ?: return StreakStatus.NOT_STARTED
            val lastDate = runCatching { LocalDate.parse(lastDateStr) }.getOrNull()
                ?: return StreakStatus.NOT_STARTED

            val daysDiff = currentEffectiveDate.toEpochDays() - lastDate.toEpochDays()
            return when {
                daysDiff <= 0 -> {
                    if (streakData.currentStreak > 0) StreakStatus.COMPLETED_TODAY
                    else StreakStatus.NOT_STARTED
                }
                daysDiff == 1 -> {
                    if (streakData.currentStreak > 0) StreakStatus.AT_RISK
                    else StreakStatus.NOT_STARTED
                }
                else -> {
                    StreakStatus.EXPIRED
                }
            }
        }

        fun getMotivationalInfo(
            streakData: DailyStreakData,
            status: StreakStatus
        ): StreakMotivation {
            val streak = streakData.currentStreak
            val isMilestone = streak in setOf(3, 7, 14, 21, 30, 50, 60, 90, 100, 150, 200, 365)

            return when (status) {
                StreakStatus.COMPLETED_TODAY -> {
                    when {
                        streak == 1 -> StreakMotivation(
                            status = status,
                            currentStreak = streak,
                            longestStreak = streakData.longestStreak,
                            title = "Day 1 Complete!",
                            message = "Great start! Practice tomorrow to build your streak.",
                            callToAction = "Streak Secured",
                            isMilestone = false
                        )
                        isMilestone -> StreakMotivation(
                            status = status,
                            currentStreak = streak,
                            longestStreak = streakData.longestStreak,
                            title = "🎉 Milestone: $streak Days!",
                            message = "Incredible dedication! Your consistent practice is mastering Kanji.",
                            callToAction = "Milestone Achieved",
                            isMilestone = true
                        )
                        streak >= 30 -> StreakMotivation(
                            status = status,
                            currentStreak = streak,
                            longestStreak = streakData.longestStreak,
                            title = "🔥 $streak-Day Streak!",
                            message = "You're an unstoppable Kanji powerhouse! The habit is locked in.",
                            callToAction = "Streak Secured",
                            isMilestone = false
                        )
                        streak >= 7 -> StreakMotivation(
                            status = status,
                            currentStreak = streak,
                            longestStreak = streakData.longestStreak,
                            title = "🔥 $streak-Day Streak!",
                            message = "Fantastic consistency! Daily practice guarantees strong retention.",
                            callToAction = "Streak Secured",
                            isMilestone = false
                        )
                        else -> StreakMotivation(
                            status = status,
                            currentStreak = streak,
                            longestStreak = streakData.longestStreak,
                            title = "🔥 $streak-Day Streak!",
                            message = "Streak safe for today! See you tomorrow.",
                            callToAction = "Streak Secured",
                            isMilestone = false
                        )
                    }
                }
                StreakStatus.AT_RISK -> {
                    val streakText = if (streak > 1) "$streak-day streak" else "streak"
                    StreakMotivation(
                        status = status,
                        currentStreak = streak,
                        longestStreak = streakData.longestStreak,
                        title = "⚡ Keep Your Streak Alive!",
                        message = "You practiced yesterday! Complete a session today to reach ${streak + 1} days.",
                        callToAction = "Practice Now",
                        isMilestone = false
                    )
                }
                StreakStatus.EXPIRED -> {
                    StreakMotivation(
                        status = status,
                        currentStreak = 0,
                        longestStreak = streakData.longestStreak,
                        title = "🎯 Start Fresh Today",
                        message = if (streakData.longestStreak > 0) {
                            "Your personal best is ${streakData.longestStreak} days. Practice today to start beating your record!"
                        } else {
                            "Consistency is the secret to mastering Kanji. Start your streak today!"
                        },
                        callToAction = "Start Practice",
                        isMilestone = false
                    )
                }
                StreakStatus.NOT_STARTED -> {
                    StreakMotivation(
                        status = status,
                        currentStreak = 0,
                        longestStreak = streakData.longestStreak,
                        title = "🚀 Begin Your Kanji Streak",
                        message = "Practice at least once daily to build muscle memory and retention.",
                        callToAction = "Start Practice",
                        isMilestone = false
                    )
                }
            }
        }

    }
}
