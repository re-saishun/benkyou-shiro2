package ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case

import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import ua.syt0r.kanji.core.ApiTextAnalysisResponse
import ua.syt0r.kanji.core.NetworkClients
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContentLanguage
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface GeminiTextAnalysisUseCase {

    suspend operator fun invoke(
        input: String,
        apiKey: String
    ): Result<ApiTextAnalysisResponse>

    companion object {
        const val GEMINI_MODEL_ID = "gemini-3.1-flash-lite"
        private const val GEMINI_PREVIEW_FALLBACK_MODEL_ID = "gemini-3.1-flash-lite-preview"
    }

}

class DefaultGeminiTextAnalysisUseCase(
    private val networkClients: NetworkClients,
    private val appPreferences: PreferencesContract.AppPreferences
) : GeminiTextAnalysisUseCase {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun invoke(
        input: String,
        apiKey: String
    ): Result<ApiTextAnalysisResponse> = runCatching {
        val trimmedKey = apiKey.trim()
        require(trimmedKey.isNotEmpty()) { "Gemini API key is empty" }

        val contentLanguage = appPreferences.contentLanguage.get()
        val targetLanguageName = when (contentLanguage) {
            PreferencesContentLanguage.Indonesian -> "Indonesian"
            PreferencesContentLanguage.English -> "English"
        }

        val requestPayload = buildRequestPayload(
            input = input,
            targetLanguageName = targetLanguageName
        )

        val response = executeWithFallback(trimmedKey, requestPayload)
        val jsonText = extractGeneratedJsonText(response)
        parseGeneratedJsonToResponse(input, jsonText)
    }

    internal fun parseGeneratedJsonToResponse(
        input: String,
        generatedJsonText: String
    ): ApiTextAnalysisResponse {
        val sanitizedJson = generatedJsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        Logger.d("Gemini analysis JSON length=${sanitizedJson.length}")
        val parsed = json.decodeFromString<GeminiAnalysisPayload>(sanitizedJson)
        return ApiTextAnalysisResponse(
            text = input,
            translation = parsed.translation,
            elements = parsed.tokens.toIchiranJsonArray()
        )
    }

    private suspend fun executeWithFallback(
        apiKey: String,
        requestPayload: String
    ): String {
        val primaryUrl = buildEndpointUrl(GeminiTextAnalysisUseCase.GEMINI_MODEL_ID)
        val primaryResponse = networkClients.unauthenticatedClient.post(primaryUrl) {
            header("x-goog-api-key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(requestPayload)
        }

        if (primaryResponse.status == HttpStatusCode.OK) {
            return primaryResponse.bodyAsText()
        }

        val primaryBody = primaryResponse.bodyAsText()
        if (primaryResponse.status == HttpStatusCode.NotFound) {
            val fallbackUrl = buildEndpointUrl("gemini-3.1-flash-lite-preview")
            val fallbackResponse = networkClients.unauthenticatedClient.post(fallbackUrl) {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(requestPayload)
            }
            if (fallbackResponse.status == HttpStatusCode.OK) {
                return fallbackResponse.bodyAsText()
            }
            val fallbackBody = fallbackResponse.bodyAsText()
            throw IllegalStateException(extractErrorMessage(fallbackResponse.status, fallbackBody))
        }

        throw IllegalStateException(extractErrorMessage(primaryResponse.status, primaryBody))
    }

    private fun buildEndpointUrl(modelId: String): String {
        return "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent"
    }

    private fun extractErrorMessage(status: HttpStatusCode, rawBody: String): String {
        val apiMessage = runCatching {
            val root = json.decodeFromString<JsonObject>(rawBody)
            root["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
        }.getOrNull()

        return apiMessage?.takeIf { it.isNotBlank() }
            ?: "Gemini API error (${status.value} ${status.description})"
    }

    private fun buildRequestPayload(
        input: String,
        targetLanguageName: String
    ): String {
        val prompt = """
            Analyze the following Japanese text into morphological tokens and translate it naturally into $targetLanguageName.
            Rules:
            - Concatenating all token.text fields in order must reproduce the original input text accurately.
            - For punctuation, spaces, newlines, or standalone symbols, set isPunctuation=true, kana=text, and glossaries=[].
            - For Japanese words/particles, set isPunctuation=false, provide kana in hiragana/katakana (hiragana preferred), dictionaryForm (plain/dictionary form if conjugated, or same as text), dictionaryKana (kana of dictionaryForm), a JMdict-style POS tag in pos (one of: n, v1, v5r, v5s, vs, adj-i, adj-na, prt, aux-v, aux, cop, adv, conj, exp, ctr, int, pn, num, suf, pref, unc), and concise glossaries in $targetLanguageName.
            - If the word is conjugated (e.g. past, te-form, polite -masu, negative), provide conjugationType (e.g. "Past", "Non-past", "Conjunctive (~te)", "Negative").

            Input Japanese text:
            $input
        """.trimIndent()

        val payload = buildJsonObject {
            putJsonArray("contents") {
                add(buildJsonObject {
                    putJsonArray("parts") {
                        add(buildJsonObject {
                            put("text", prompt)
                        })
                    }
                })
            }
            putJsonObject("generationConfig") {
                put("temperature", 0.1)
                put("responseMimeType", "application/json")
                putJsonObject("responseSchema") {
                    put("type", "OBJECT")
                    putJsonObject("properties") {
                        putJsonObject("translation") {
                            put("type", "STRING")
                        }
                        putJsonObject("tokens") {
                            put("type", "ARRAY")
                            putJsonObject("items") {
                                put("type", "OBJECT")
                                putJsonObject("properties") {
                                    putJsonObject("text") { put("type", "STRING") }
                                    putJsonObject("kana") { put("type", "STRING") }
                                    putJsonObject("romaji") { put("type", "STRING") }
                                    putJsonObject("dictionaryForm") { put("type", "STRING") }
                                    putJsonObject("dictionaryKana") { put("type", "STRING") }
                                    putJsonObject("pos") { put("type", "STRING") }
                                    putJsonObject("conjugationType") { put("type", "STRING") }
                                    putJsonObject("isPunctuation") { put("type", "BOOLEAN") }
                                    putJsonObject("glossaries") {
                                        put("type", "ARRAY")
                                        putJsonObject("items") { put("type", "STRING") }
                                    }
                                }
                                putJsonArray("required") {
                                    add(JsonPrimitive("text"))
                                    add(JsonPrimitive("kana"))
                                    add(JsonPrimitive("isPunctuation"))
                                    add(JsonPrimitive("glossaries"))
                                }
                            }
                        }
                    }
                    putJsonArray("required") {
                        add(JsonPrimitive("translation"))
                        add(JsonPrimitive("tokens"))
                    }
                }
            }
        }
        return payload.toString()
    }

    private fun extractGeneratedJsonText(rawBody: String): String {
        val root = json.decodeFromString<JsonObject>(rawBody)
        val candidates = root["candidates"]?.jsonArray
            ?: throw IllegalStateException("Gemini returned no candidates")
        val firstCandidate = candidates.firstOrNull()?.jsonObject
            ?: throw IllegalStateException("Gemini returned an empty response")
        val parts = firstCandidate["content"]?.jsonObject?.get("parts")?.jsonArray
            ?: throw IllegalStateException("Gemini response missing content parts")

        val generatedText = parts
            .mapNotNull { part ->
                val partObject = part.jsonObject
                val isThought = partObject["thought"]?.jsonPrimitive?.content == "true"
                if (isThought) null
                else partObject["text"]?.jsonPrimitive?.content
            }
            .joinToString(separator = "")

        if (generatedText.isBlank()) {
            throw IllegalStateException("Gemini response missing generated JSON")
        }
        return generatedText
    }

    private fun List<GeminiToken>.toIchiranJsonArray(): JsonArray {
        val sentenceParts = mutableListOf<JsonElement>()
        val currentWordsBatch = mutableListOf<JsonElement>()

        fun flushWordsBatch() {
            if (currentWordsBatch.isEmpty()) return
            sentenceParts.add(
                buildJsonArray {
                    add(
                        buildJsonArray {
                            add(
                                buildJsonArray {
                                    add(JsonArray(currentWordsBatch.toList()))
                                    add(JsonPrimitive(0))
                                }
                            )
                        }
                    )
                }
            )
            currentWordsBatch.clear()
        }

        forEach { token ->
            val tokenText = token.text
            if (tokenText.isEmpty()) return@forEach

            if (token.isPunctuation || token.glossaries.isEmpty()) {
                flushWordsBatch()
                sentenceParts.add(JsonPrimitive(tokenText))
            } else {
                val kana = token.kana.ifBlank { tokenText }
                val posTag = token.pos?.takeIf { it.isNotBlank() } ?: "n"
                val dictForm = token.dictionaryForm?.takeIf { it.isNotBlank() } ?: tokenText
                val dictKana = token.dictionaryKana?.takeIf { it.isNotBlank() } ?: kana
                val surfaceReading = formatReading(tokenText, kana)
                val dictReading = formatReading(dictForm, dictKana)
                val combinedGloss = token.glossaries
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString("; ")
                val conjType = token.conjugationType?.takeIf { it.isNotBlank() }

                val wordObj = buildJsonObject {
                    put("text", tokenText)
                    put("kana", kana)
                    put("reading", surfaceReading)

                    if (conjType != null && (dictForm != tokenText || dictKana != kana)) {
                        putJsonArray("conj") {
                            add(
                                buildJsonObject {
                                    put("reading", dictReading)
                                    putJsonArray("prop") {
                                        add(
                                            buildJsonObject {
                                                put("pos", posTag)
                                                put("type", conjType)
                                            }
                                        )
                                    }
                                    putJsonArray("gloss") {
                                        add(
                                            buildJsonObject {
                                                put("gloss", combinedGloss)
                                                put("pos", "[$posTag]")
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    } else {
                        putJsonArray("gloss") {
                            add(
                                buildJsonObject {
                                    put("gloss", combinedGloss)
                                    put("pos", "[$posTag]")
                                }
                            )
                        }
                    }
                }

                currentWordsBatch.add(
                    buildJsonArray {
                        add(JsonPrimitive(token.romaji ?: ""))
                        add(wordObj)
                        add(JsonArray(emptyList()))
                    }
                )
            }
        }

        flushWordsBatch()
        return JsonArray(sentenceParts)
    }

    private fun formatReading(text: String, kana: String): String {
        return if (text == kana) text else "$text 【$kana】"
    }

    @Serializable
    private data class GeminiAnalysisPayload(
        val translation: String,
        val tokens: List<GeminiToken>
    )

    @Serializable
    private data class GeminiToken(
        val text: String,
        val kana: String = "",
        val romaji: String? = null,
        val dictionaryForm: String? = null,
        val dictionaryKana: String? = null,
        val pos: String? = null,
        val conjugationType: String? = null,
        val isPunctuation: Boolean = false,
        val glossaries: List<String> = emptyList()
    )

}
