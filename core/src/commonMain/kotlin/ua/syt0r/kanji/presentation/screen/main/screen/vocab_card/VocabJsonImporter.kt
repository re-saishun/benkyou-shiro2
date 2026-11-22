package ua.syt0r.kanji.presentation.screen.main.screen.vocab_card

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import ua.syt0r.kanji.core.user_data.database.VocabCardData

object VocabJsonImporter {

    val SampleJson = """[
  {
    "kanji": "日本語",
    "kana": "にほんご",
    "meaning": "Japanese language"
  },
  {
    "kanji": "勉強",
    "kana": "べんきょう",
    "meaning": "Study"
  },
  {
    "kanji": "猫",
    "kana": "ねこ",
    "meaning": "Cat"
  },
  {
    "kana": "ありがとう",
    "meaning": "Thank you"
  }
]""".trimIndent()

    sealed interface ParseResult {
        data class Success(val cards: List<VocabCardData>) : ParseResult
        data class Error(val message: String) : ParseResult
    }

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parse(rawJson: String): ParseResult {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) {
            return ParseResult.Error("JSON is empty")
        }

        val jsonElement: JsonElement = try {
            jsonParser.parseToJsonElement(trimmed)
        } catch (e: Exception) {
            val firstLine = e.message?.lines()?.firstOrNull() ?: "parse error"
            return ParseResult.Error("Invalid JSON syntax: $firstLine")
        }

        val cardsArray: JsonArray = when (jsonElement) {
            is JsonArray -> jsonElement
            is JsonObject -> {
                val arrayProperty = jsonElement["cards"]?.takeIf { it is JsonArray }
                    ?: jsonElement["vocab"]?.takeIf { it is JsonArray }
                    ?: jsonElement["words"]?.takeIf { it is JsonArray }
                    ?: jsonElement["items"]?.takeIf { it is JsonArray }
                    ?: jsonElement["vocabulary"]?.takeIf { it is JsonArray }

                if (arrayProperty != null) {
                    arrayProperty.jsonArray
                } else {
                    JsonArray(listOf(jsonElement))
                }
            }
            else -> return ParseResult.Error("JSON must be an array of cards or an object containing a cards list")
        }

        if (cardsArray.isEmpty()) {
            return ParseResult.Error("No cards found in JSON")
        }

        val parsedCards = mutableListOf<VocabCardData>()
        val errors = mutableListOf<String>()

        cardsArray.forEachIndexed { index, element ->
            if (element !is JsonObject) {
                errors.add("Card #${index + 1} is not a JSON object")
                return@forEachIndexed
            }

            val cardObj = element.jsonObject

            val kanji = cardObj.findString("kanji", "kanjiReading", "word")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

            val kana = cardObj.findString("kana", "kanaReading", "reading", "furigana")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: kanji

            val meaning = cardObj.findString("meaning", "translation", "english", "arti", "terjemahan", "glossary")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

            val dictId = cardObj.findLong("dictionaryId", "jmDictId", "id")

            if (kana.isNullOrEmpty()) {
                errors.add("Card #${index + 1}: Missing kana or kanji reading")
                return@forEachIndexed
            }

            parsedCards.add(
                VocabCardData(
                    kanjiReading = kanji,
                    kanaReading = kana,
                    meaning = meaning ?: "",
                    dictionaryId = dictId
                )
            )
        }

        if (parsedCards.isEmpty()) {
            val errorSummary = errors.take(3).joinToString("; ")
            return ParseResult.Error("No valid cards found. $errorSummary")
        }

        return ParseResult.Success(parsedCards)
    }

    private fun JsonObject.findString(vararg keys: String): String? {
        for (key in keys) {
            val element = this[key] ?: continue
            try {
                val str = element.jsonPrimitive.content
                if (str.isNotBlank()) return str
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun JsonObject.findLong(vararg keys: String): Long? {
        for (key in keys) {
            val element = this[key] ?: continue
            try {
                val num = element.jsonPrimitive.longOrNull
                if (num != null) return num
            } catch (_: Exception) {
            }
        }
        return null
    }

}
