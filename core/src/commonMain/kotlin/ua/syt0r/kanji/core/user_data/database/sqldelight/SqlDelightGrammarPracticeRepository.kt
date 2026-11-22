package ua.syt0r.kanji.core.user_data.database.sqldelight

import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.user_data.database.GrammarDeck
import ua.syt0r.kanji.core.user_data.database.GrammarExample
import ua.syt0r.kanji.core.user_data.database.GrammarPoint
import ua.syt0r.kanji.core.user_data.database.GrammarPointData
import ua.syt0r.kanji.core.user_data.database.GrammarPracticeRepository
import ua.syt0r.kanji.core.user_data.database.GrammarQuiz
import ua.syt0r.kanji.core.user_data.database.ObservableRepository
import ua.syt0r.kanji.core.user_data.database.ObservableUserDataRepository
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseContract
import ua.syt0r.kanji.core.user_data.database.buildGrammarItemKey
import ua.syt0r.kanji.core.userdata.db.Grammar_deck
import ua.syt0r.kanji.core.userdata.db.Grammar_point

class SqlDelightGrammarPracticeRepository(
    observableRepository: ObservableUserDataRepository
) : GrammarPracticeRepository,
    ObservableRepository by observableRepository,
    UserDataDatabaseContract.TransactionScope by observableRepository {

    constructor(
        databaseManager: UserDataDatabaseContract.Manager
    ) : this(ObservableUserDataRepository(databaseManager))

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun getDecks(): List<GrammarDeck> = readTransaction {
        getAllGrammarDecks().executeAsList().map { it.toDeck() }
    }

    override suspend fun getDeck(id: Long): GrammarDeck? = readTransaction {
        getGrammarDeckById(id).executeAsOneOrNull()?.toDeck()
    }

    override suspend fun getPoints(deckId: Long): List<GrammarPoint> = readTransaction {
        val deck = getGrammarDeckById(deckId).executeAsOneOrNull()
            ?: return@readTransaction emptyList<GrammarPoint>()
        getGrammarPointsForDeck(deckId).executeAsList().map { it.toPoint(deck.remote_id) }
    }

    override suspend fun getAllPoints(): List<GrammarPoint> = readTransaction {
        val remoteIds = getAllGrammarDecks().executeAsList().associate { it.id to it.remote_id }
        getAllGrammarPoints().executeAsList().mapNotNull { row ->
            val remoteId = remoteIds[row.deck_id] ?: return@mapNotNull null
            row.toPoint(remoteId)
        }
    }

    override suspend fun createEmptyDeck(title: String): Long = writeTransaction {
        val remoteId = "local-${Clock.System.now().toEpochMilliseconds()}"
        insertGrammarDeck(remoteId, title, "", "", "", 1L)
        getLastInsertRowId().executeAsOne()
    }

    override suspend fun savePack(
        remoteId: String,
        title: String,
        level: String,
        description: String,
        sourceUrl: String,
        version: Int,
        points: List<GrammarPointData>
    ): Long = writeTransaction {
        val existing = getGrammarDeckByRemoteId(remoteId).executeAsOneOrNull()
        val deckId: Long
        if (existing == null) {
            insertGrammarDeck(remoteId, title, level, description, sourceUrl, version.toLong())
            deckId = getLastInsertRowId().executeAsOne()
        } else {
            deckId = existing.id
            updateGrammarDeckMetadata(title, level, description, sourceUrl, version.toLong(), deckId)
        }

        points.forEach { point ->
            val quiz = point.quiz
            insertOrReplaceGrammarPoint(
                deckId,
                point.key,
                point.title,
                point.structure,
                point.meaning,
                point.nuance,
                json.encodeToString(point.examples),
                quiz?.question ?: "",
                json.encodeToString(quiz?.options ?: emptyList<String>()),
                (quiz?.correctIndex ?: 0).toLong(),
                quiz?.explanation ?: ""
            )
        }
        deleteGrammarPointsNotInKeys(deckId, points.map { it.key })

        deckId
    }

    override suspend fun deleteDeck(id: Long) {
        writeTransaction { deleteGrammarDeck(id) }
    }

    override suspend fun updateDeckPositions(deckIdToPositionMap: Map<Long, Int>) {
        writeTransaction {
            deckIdToPositionMap.forEach { (deckId, position) ->
                updateGrammarDeckPosition(position.toLong(), deckId)
            }
        }
    }

    private fun Grammar_deck.toDeck() = GrammarDeck(
        id = id,
        remoteId = remote_id,
        title = title,
        level = level,
        description = description,
        sourceUrl = source_url,
        version = version.toInt(),
        position = position.toInt()
    )

    private fun Grammar_point.toPoint(deckRemoteId: String): GrammarPoint {
        val examples = runCatching {
            json.decodeFromString<List<GrammarExample>>(examples_json)
        }.getOrDefault(emptyList())

        val options = runCatching {
            json.decodeFromString<List<String>>(quiz_options_json)
        }.getOrDefault(emptyList())

        val quiz = if (quiz_question.isNotBlank() && options.size >= 2) {
            GrammarQuiz(
                question = quiz_question,
                options = options,
                correctIndex = quiz_correct_index.toInt().coerceIn(0, options.lastIndex),
                explanation = quiz_explanation
            )
        } else {
            null
        }

        return GrammarPoint(
            itemKey = buildGrammarItemKey(deckRemoteId, point_key),
            deckId = deck_id,
            data = GrammarPointData(
                key = point_key,
                title = title,
                structure = structure,
                meaning = meaning,
                nuance = nuance,
                examples = examples,
                quiz = quiz
            )
        )
    }

}
