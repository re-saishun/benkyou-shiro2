package ua.syt0r.kanji.core.user_data.database

import kotlinx.serialization.Serializable

data class GrammarDeck(
    val id: Long,
    val remoteId: String,
    val title: String,
    val level: String,
    val description: String,
    val sourceUrl: String,
    val version: Int,
    val position: Int
)

@Serializable
data class GrammarExample(
    val japanese: String = "",
    val reading: String = "",
    val translation: String = ""
)

data class GrammarQuiz(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

/**
 * Data used to create or update a grammar point
 */
data class GrammarPointData(
    val key: String,
    val title: String,
    val structure: String,
    val meaning: String,
    val nuance: String,
    val examples: List<GrammarExample>,
    val quiz: GrammarQuiz?
)

/**
 * Saved grammar point. [itemKey] is globally unique and used as the SRS / review history key
 */
data class GrammarPoint(
    val itemKey: String,
    val deckId: Long,
    val data: GrammarPointData
)

interface GrammarPracticeRepository : ObservableRepository {
    suspend fun getDecks(): List<GrammarDeck>
    suspend fun getDeck(id: Long): GrammarDeck?
    suspend fun getPoints(deckId: Long): List<GrammarPoint>
    suspend fun getAllPoints(): List<GrammarPoint>
    suspend fun createEmptyDeck(title: String): Long
    suspend fun savePack(
        remoteId: String,
        title: String,
        level: String,
        description: String,
        sourceUrl: String,
        version: Int,
        points: List<GrammarPointData>
    ): Long

    suspend fun deleteDeck(id: Long)
    suspend fun updateDeckPositions(deckIdToPositionMap: Map<Long, Int>)
}

fun buildGrammarItemKey(deckRemoteId: String, pointKey: String): String {
    return "$deckRemoteId/$pointKey"
}
