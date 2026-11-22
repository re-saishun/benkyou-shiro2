package ua.syt0r.kanji.core.streak

import kotlinx.serialization.Serializable

@Serializable
data class DailyStreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastPracticeDate: String? = null, // "yyyy-MM-dd"
    val lastPracticeTimestamp: Long? = null, // Epoch milliseconds
    val totalPracticeDays: Int = 0,
    val streakFreezeCount: Int = 0
)

enum class StreakStatus {
    /** User has not yet practiced or has no active streak */
    NOT_STARTED,

    /** User completed practice today; streak is preserved and safe */
    COMPLETED_TODAY,

    /** User practiced yesterday, but hasn't practiced yet today; streak is at risk */
    AT_RISK,

    /** User missed a day or more; streak has expired */
    EXPIRED
}

data class StreakMotivation(
    val status: StreakStatus,
    val currentStreak: Int,
    val longestStreak: Int,
    val title: String,
    val message: String,
    val callToAction: String,
    val isMilestone: Boolean
)
