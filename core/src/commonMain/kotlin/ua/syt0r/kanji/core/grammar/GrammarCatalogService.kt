package ua.syt0r.kanji.core.grammar

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.AppSecretsConfig
import ua.syt0r.kanji.core.NetworkClients
import ua.syt0r.kanji.core.user_data.database.GrammarExample
import ua.syt0r.kanji.core.user_data.database.GrammarPointData
import ua.syt0r.kanji.core.user_data.database.GrammarPracticeRepository
import ua.syt0r.kanji.core.user_data.database.GrammarQuiz
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

/**
 * Change this to the raw URL of catalog.json in your GitHub repository.
 * Users can also type another URL in the "Grammar Packs" dialog, it is remembered.
 *
 * Example:
 * https://raw.githubusercontent.com/<username>/<repository>/main/catalog.json
 */
const val DefaultGrammarCatalogUrl: String =
    "https://raw.githubusercontent.com/benkyou-shiro/grammar-packs/main/catalog.json"

@Serializable
data class GrammarCatalogFile(
    val packs: List<GrammarCatalogEntry> = emptyList()
)

@Serializable
data class GrammarCatalogEntry(
    val id: String,
    val title: String,
    val level: String = "",
    val description: String = "",
    val version: Int = 1,
    val url: String,
    val pointCount: Int = 0
)

@Serializable
data class GrammarPackFile(
    val id: String = "",
    val title: String = "",
    val level: String = "",
    val description: String = "",
    val version: Int = 1,
    val points: List<GrammarPackPoint> = emptyList()
)

@Serializable
data class GrammarPackPoint(
    val key: String = "",
    val title: String = "",
    val structure: String = "",
    val meaning: String = "",
    val nuance: String = "",
    val examples: List<GrammarExample> = emptyList(),
    val quiz: GrammarPackQuiz? = null
)

@Serializable
data class GrammarPackQuiz(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val explanation: String = ""
)

enum class GrammarPackStatus { NotDownloaded, Downloaded, UpdateAvailable }

data class RemoteGrammarPack(
    val entry: GrammarCatalogEntry,
    val absoluteUrl: String,
    val status: GrammarPackStatus
)

interface GrammarCatalogService {
    suspend fun getSavedCatalogUrl(): String
    suspend fun fetchCatalog(catalogUrl: String): Result<List<RemoteGrammarPack>>
    suspend fun downloadPack(pack: RemoteGrammarPack): Result<Unit>
}

class DefaultGrammarCatalogService(
    private val networkClients: NetworkClients,
    private val repository: GrammarPracticeRepository,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val appSecretsConfig: AppSecretsConfig
) : GrammarCatalogService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override suspend fun getSavedCatalogUrl(): String {
        val saved = appPreferences.grammarCatalogUrl.get().trim()
        val secretUrl = appSecretsConfig.resolvedGithubCatalogUrl
        return when {
            !secretUrl.isNullOrBlank() && (saved.isBlank() || saved == DefaultGrammarCatalogUrl) -> secretUrl
            saved.isNotBlank() -> saved
            !secretUrl.isNullOrBlank() -> secretUrl
            else -> DefaultGrammarCatalogUrl
        }
    }

    override suspend fun fetchCatalog(
        catalogUrl: String
    ): Result<List<RemoteGrammarPack>> = runCatching {
        val normalizedUrl = normalizeUrl(catalogUrl)
        require(normalizedUrl.isNotEmpty()) { "URL is empty" }

        val body = download(normalizedUrl)
        val catalog = json.decodeFromString<GrammarCatalogFile>(body)

        appPreferences.grammarCatalogUrl.set(normalizedUrl)

        val localDecks = repository.getDecks().associateBy { it.remoteId }

        catalog.packs.map { entry ->
            val local = localDecks[entry.id]
            RemoteGrammarPack(
                entry = entry,
                absoluteUrl = resolveUrl(normalizedUrl, entry.url),
                status = when {
                    local == null -> GrammarPackStatus.NotDownloaded
                    entry.version > local.version -> GrammarPackStatus.UpdateAvailable
                    else -> GrammarPackStatus.Downloaded
                }
            )
        }
    }

    override suspend fun downloadPack(pack: RemoteGrammarPack): Result<Unit> = runCatching {
        val body = download(pack.absoluteUrl)
        val file = json.decodeFromString<GrammarPackFile>(body)

        val points = file.points
            .filter { it.title.isNotBlank() }
            .mapIndexed { index, point ->
                val key = point.key.ifBlank { "point-${index + 1}" }
                val quiz = point.quiz
                    ?.takeIf { it.question.isNotBlank() && it.options.size >= 2 }
                    ?.let {
                        GrammarQuiz(
                            question = it.question,
                            options = it.options,
                            correctIndex = it.correctIndex.coerceIn(0, it.options.lastIndex),
                            explanation = it.explanation
                        )
                    }
                GrammarPointData(
                    key = key,
                    title = point.title,
                    structure = point.structure,
                    meaning = point.meaning,
                    nuance = point.nuance,
                    examples = point.examples,
                    quiz = quiz
                )
            }
            .distinctBy { it.key }

        require(points.isNotEmpty()) { "Pack has no grammar points" }

        repository.savePack(
            remoteId = pack.entry.id,
            title = pack.entry.title.ifBlank { file.title },
            level = pack.entry.level.ifBlank { file.level },
            description = pack.entry.description.ifBlank { file.description },
            sourceUrl = pack.absoluteUrl,
            version = maxOf(pack.entry.version, file.version),
            points = points
        )
        Unit
    }

    private suspend fun download(url: String): String {
        val token = appSecretsConfig.githubToken
        val isGithubHost = url.contains("github.com", ignoreCase = true) ||
                url.contains("githubusercontent.com", ignoreCase = true)
        val response = networkClients.unauthenticatedClient.get(url) {
            if (isGithubHost && token.isNotBlank()) {
                header("Authorization", "Bearer $token")
            }
            header("Accept", "application/vnd.github.raw+json, application/json")
        }
        if (!response.status.isSuccess()) {
            throw IllegalStateException("HTTP ${response.status.value} ${response.status.description}")
        }
        return response.bodyAsText()
    }

    /**
     * Converts github.com/.../blob/... links into raw.githubusercontent.com links
     */
    private fun normalizeUrl(input: String): String {
        val url = input.trim()
        val blobRegex = Regex("^https?://github\\.com/([^/]+)/([^/]+)/blob/(.+)$")
        val match = blobRegex.find(url) ?: return url
        val (user, repo, rest) = match.destructured
        return "https://raw.githubusercontent.com/$user/$repo/$rest"
    }

    private fun resolveUrl(catalogUrl: String, packUrl: String): String {
        if (packUrl.startsWith("http://") || packUrl.startsWith("https://")) {
            return normalizeUrl(packUrl)
        }
        val base = catalogUrl.substringBeforeLast('/', missingDelimiterValue = catalogUrl)
        return "$base/${packUrl.trimStart('/')}"
    }

}
