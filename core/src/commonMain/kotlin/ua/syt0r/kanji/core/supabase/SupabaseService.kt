package ua.syt0r.kanji.core.supabase

import io.ktor.client.request.forms.ChannelProvider
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import ua.syt0r.kanji.core.AppSecretsConfig
import ua.syt0r.kanji.core.NetworkClients
import ua.syt0r.kanji.core.backup.BackupManager
import ua.syt0r.kanji.core.file.PlatformFileHandler
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.SyncBackupFileProvider
import ua.syt0r.kanji.core.sync.use_case.ApplyRemoteSyncDataUseCase
import ua.syt0r.kanji.core.sync.use_case.ApplySyncResult
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

sealed interface SupabaseAuthState {
    data object NotConfigured : SupabaseAuthState
    data object Loading : SupabaseAuthState
    data class SignedOut(val lastError: String? = null) : SupabaseAuthState
    data class SignedIn(
        val userId: String,
        val email: String,
        val lastSyncTimestamp: Long?,
        val isSyncing: Boolean = false,
        val syncMessage: String? = null,
        val syncError: String? = null
    ) : SupabaseAuthState
}

sealed interface SupabaseGoogleLoginOutcome {
    data object SignedIn : SupabaseGoogleLoginOutcome
    data object OpenBrowserOAuth : SupabaseGoogleLoginOutcome
    data object Cancelled : SupabaseGoogleLoginOutcome
    data class Error(val message: String) : SupabaseGoogleLoginOutcome
}

interface SupabaseService {
    val state: StateFlow<SupabaseAuthState>
    val isConfigured: Boolean
    fun buildGoogleOAuthUrl(): String?
    suspend fun signInWithGoogle(): SupabaseGoogleLoginOutcome
    suspend fun handleAuthDeepLink(url: String): Result<Unit>
    suspend fun signOut()
    suspend fun uploadBackup(): Result<Unit>
    suspend fun restoreBackup(): Result<Unit>
    suspend fun migrateFromLegacyAccount(): Result<String>

    companion object {
        const val REDIRECT_URI = "kanji-dojo://supabase-auth"
        private const val BACKUP_BUCKET = "user-backups"
    }
}

class DefaultSupabaseService(
    private val appSecretsConfig: AppSecretsConfig,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val networkClients: NetworkClients,
    private val googleSignInBridge: GoogleSignInBridge,
    private val backupManager: BackupManager,
    private val syncBackupFileProvider: SyncBackupFileProvider,
    private val platformFileHandler: PlatformFileHandler,
    private val applyRemoteSyncDataUseCase: ApplyRemoteSyncDataUseCase,
    private val timeUtils: TimeUtils
) : SupabaseService {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow<SupabaseAuthState>(SupabaseAuthState.Loading)
    override val state: StateFlow<SupabaseAuthState> = _state

    override val isConfigured: Boolean
        get() = appSecretsConfig.isSupabaseConfigured

    init {
        scope.launch { refreshLocalState() }
    }

    private suspend fun refreshLocalState(lastError: String? = null) {
        if (!isConfigured) {
            _state.value = SupabaseAuthState.NotConfigured
            return
        }
        val accessToken = appPreferences.supabaseAccessToken.get()
        val userId = appPreferences.supabaseUserId.get()
        val email = appPreferences.supabaseUserEmail.get()
        val lastSync = appPreferences.supabaseLastSyncTimestamp.get()

        _state.value = if (!accessToken.isNullOrBlank() && !userId.isNullOrBlank() && !email.isNullOrBlank()) {
            SupabaseAuthState.SignedIn(
                userId = userId,
                email = email,
                lastSyncTimestamp = lastSync
            )
        } else {
            SupabaseAuthState.SignedOut(lastError = lastError)
        }
    }

    override fun buildGoogleOAuthUrl(): String? {
        if (!isConfigured) return null
        val redirect = SupabaseService.REDIRECT_URI
        return "${appSecretsConfig.supabaseUrl}/auth/v1/authorize?provider=google&redirect_to=$redirect"
    }

    override suspend fun signInWithGoogle(): SupabaseGoogleLoginOutcome {
        if (!isConfigured) {
            return SupabaseGoogleLoginOutcome.Error(
                "Supabase secrets (SUPABASE_URL & SUPABASE_ANON_KEY) are not configured yet."
            )
        }

        _state.value = SupabaseAuthState.Loading
        return when (val credResult = googleSignInBridge.signIn()) {
            is GoogleSignInResult.Success -> {
                val exchange = exchangeGoogleIdToken(credResult.idToken)
                exchange.fold(
                    onSuccess = {
                        SupabaseGoogleLoginOutcome.SignedIn
                    },
                    onFailure = { err ->
                        val msg = err.message ?: "Supabase authentication failed"
                        _state.value = SupabaseAuthState.SignedOut(msg)
                        SupabaseGoogleLoginOutcome.Error(msg)
                    }
                )
            }

            GoogleSignInResult.Cancelled -> {
                refreshLocalState()
                SupabaseGoogleLoginOutcome.Cancelled
            }

            GoogleSignInResult.NotConfigured -> {
                refreshLocalState()
                SupabaseGoogleLoginOutcome.OpenBrowserOAuth
            }

            is GoogleSignInResult.Error -> {
                // If native Google Credential Manager fails (e.g. emulator without Google Play Auth),
                // gracefully fall back to browser Google OAuth with Supabase.
                Logger.d("Native Google Sign-In unavailable (${credResult.message}), falling back to OAuth browser flow")
                refreshLocalState()
                SupabaseGoogleLoginOutcome.OpenBrowserOAuth
            }
        }
    }

    private suspend fun exchangeGoogleIdToken(idToken: String): Result<Unit> = runCatching {
        val url = "${appSecretsConfig.supabaseUrl}/auth/v1/token?grant_type=id_token"
        val payload = buildJsonObject {
            put("provider", "google")
            put("id_token", idToken)
        }
        val response = networkClients.unauthenticatedClient.post(url) {
            header("apikey", appSecretsConfig.supabaseAnonKey)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), payload))
        }
        val bodyText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException(extractSupabaseError(bodyText, response.status))
        }
        persistSessionFromAuthJson(bodyText)
    }

    override suspend fun handleAuthDeepLink(url: String): Result<Unit> = runCatching {
        if (!isConfigured) {
            throw IllegalStateException("Supabase is not configured")
        }
        _state.value = SupabaseAuthState.Loading

        val params = parseUrlAndFragmentParams(url)
        val accessToken = params["access_token"]
        val refreshToken = params["refresh_token"]
        val errorDesc = params["error_description"] ?: params["error"]

        if (!errorDesc.isNullOrBlank()) {
            throw IllegalStateException(errorDesc)
        }
        if (accessToken.isNullOrBlank()) {
            throw IllegalStateException("Missing access token in Supabase callback")
        }

        val userInfo = fetchUserInfo(accessToken)
        appPreferences.supabaseAccessToken.set(accessToken)
        appPreferences.supabaseRefreshToken.set(refreshToken)
        appPreferences.supabaseUserId.set(userInfo.first)
        appPreferences.supabaseUserEmail.set(userInfo.second)
        refreshLocalState()
    }.onFailure { err ->
        _state.value = SupabaseAuthState.SignedOut(err.message)
    }

    override suspend fun signOut() {
        appPreferences.supabaseAccessToken.set(null)
        appPreferences.supabaseRefreshToken.set(null)
        appPreferences.supabaseUserId.set(null)
        appPreferences.supabaseUserEmail.set(null)
        refreshLocalState()
    }

    override suspend fun uploadBackup(): Result<Unit> {
        val current = _state.value as? SupabaseAuthState.SignedIn
            ?: return Result.failure(IllegalStateException("Not signed in to Supabase"))

        _state.value = current.copy(isSyncing = true, syncMessage = null, syncError = null)
        val tempFile = syncBackupFileProvider()

        return runCatching {
            backupManager.backupTo(tempFile)
            val objectPath = "${appSecretsConfig.supabaseUrl}/storage/v1/object/user-backups/${current.userId}/backup.zip"

            suspend fun attemptUpload(accessToken: String): HttpResponse {
                return networkClients.unauthenticatedClient.post(objectPath) {
                    header("apikey", appSecretsConfig.supabaseAnonKey)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header("x-upsert", "true")
                    contentType(ContentType.Application.Zip)
                    setBody(ChannelProvider { platformFileHandler.read(tempFile) })
                }
            }

            var token = ensureAccessToken()
            var response = attemptUpload(token)
            if (response.status == HttpStatusCode.Unauthorized) {
                token = refreshAccessTokenOrThrow()
                response = attemptUpload(token)
            }
            if (!response.status.isSuccess()) {
                val errBody = response.bodyAsText()
                throw IllegalStateException(extractSupabaseError(errBody, response.status))
            }

            val nowMs = timeUtils.now().toEpochMilliseconds()
            appPreferences.supabaseLastSyncTimestamp.set(nowMs)
            _state.value = current.copy(
                lastSyncTimestamp = nowMs,
                isSyncing = false,
                syncMessage = "Backup uploaded to Supabase",
                syncError = null
            )
        }.also {
            platformFileHandler.delete(tempFile)
        }.onFailure { err ->
            Logger.e("Supabase upload failed: ${err.message}")
            _state.value = current.copy(
                isSyncing = false,
                syncMessage = null,
                syncError = err.message ?: "Supabase upload failed"
            )
        }
    }

    override suspend fun restoreBackup(): Result<Unit> {
        val current = _state.value as? SupabaseAuthState.SignedIn
            ?: return Result.failure(IllegalStateException("Not signed in to Supabase"))

        _state.value = current.copy(isSyncing = true, syncMessage = null, syncError = null)
        val tempFile = syncBackupFileProvider()

        return runCatching {
            val objectPath = "${appSecretsConfig.supabaseUrl}/storage/v1/object/user-backups/${current.userId}/backup.zip"

            suspend fun attemptDownload(accessToken: String): HttpResponse {
                return networkClients.unauthenticatedClient.get(objectPath) {
                    header("apikey", appSecretsConfig.supabaseAnonKey)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
            }

            var token = ensureAccessToken()
            var response = attemptDownload(token)
            if (response.status == HttpStatusCode.Unauthorized) {
                token = refreshAccessTokenOrThrow()
                response = attemptDownload(token)
            }
            if (!response.status.isSuccess()) {
                val errBody = response.bodyAsText()
                throw IllegalStateException(extractSupabaseError(errBody, response.status))
            }

            platformFileHandler.write(tempFile, response.bodyAsChannel())
            backupManager.restoreFrom(tempFile)

            val nowMs = timeUtils.now().toEpochMilliseconds()
            appPreferences.supabaseLastSyncTimestamp.set(nowMs)
            _state.value = current.copy(
                lastSyncTimestamp = nowMs,
                isSyncing = false,
                syncMessage = "Progress restored from Supabase",
                syncError = null
            )
        }.also {
            platformFileHandler.delete(tempFile)
        }.onFailure { err ->
            Logger.e("Supabase restore failed: ${err.message}")
            _state.value = current.copy(
                isSyncing = false,
                syncMessage = null,
                syncError = err.message ?: "Supabase restore failed"
            )
        }
    }

    override suspend fun migrateFromLegacyAccount(): Result<String> {
        return when (val pullResult = applyRemoteSyncDataUseCase()) {
            ApplySyncResult.Success -> {
                if (_state.value is SupabaseAuthState.SignedIn) {
                    val uploadResult = uploadBackup()
                    uploadResult.fold(
                        onSuccess = { Result.success("Legacy cloud progress pulled and synced to Supabase!") },
                        onFailure = { err ->
                            Result.failure(
                                IllegalStateException(
                                    "Legacy progress was restored locally, but Supabase upload failed: ${err.message}"
                                )
                            )
                        }
                    )
                } else {
                    Result.success("Legacy progress pulled to this device. Sign in with Google (Supabase) above to upload it!")
                }
            }

            is ApplySyncResult.Fail -> {
                Result.failure(
                    IllegalStateException("Could not download legacy cloud data (${pullResult.issue::class.simpleName})")
                )
            }
        }
    }

    private suspend fun persistSessionFromAuthJson(bodyText: String) {
        val obj = json.decodeFromString<JsonObject>(bodyText)
        val accessToken = obj["access_token"]?.jsonPrimitive?.contentOrNull
            ?: throw IllegalStateException("Supabase response missing access_token")
        val refreshToken = obj["refresh_token"]?.jsonPrimitive?.contentOrNull
        val userObj = obj["user"]?.jsonObject

        val userId: String
        val email: String
        if (userObj != null) {
            userId = userObj["id"]?.jsonPrimitive?.contentOrNull
                ?: fetchUserInfo(accessToken).first
            email = userObj["email"]?.jsonPrimitive?.contentOrNull
                ?: fetchUserInfo(accessToken).second
        } else {
            val fetched = fetchUserInfo(accessToken)
            userId = fetched.first
            email = fetched.second
        }

        appPreferences.supabaseAccessToken.set(accessToken)
        appPreferences.supabaseRefreshToken.set(refreshToken)
        appPreferences.supabaseUserId.set(userId)
        appPreferences.supabaseUserEmail.set(email)
        refreshLocalState()
    }

    private suspend fun fetchUserInfo(accessToken: String): Pair<String, String> {
        val url = "${appSecretsConfig.supabaseUrl}/auth/v1/user"
        val response = networkClients.unauthenticatedClient.get(url) {
            header("apikey", appSecretsConfig.supabaseAnonKey)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException(extractSupabaseError(body, response.status))
        }
        val obj = json.decodeFromString<JsonObject>(body)
        val id = obj["id"]?.jsonPrimitive?.contentOrNull
            ?: throw IllegalStateException("Supabase user missing id")
        val email = obj["email"]?.jsonPrimitive?.contentOrNull ?: id
        return id to email
    }

    private suspend fun ensureAccessToken(): String {
        return appPreferences.supabaseAccessToken.get()
            ?.takeIf { it.isNotBlank() }
            ?: refreshAccessTokenOrThrow()
    }

    private suspend fun refreshAccessTokenOrThrow(): String {
        val refreshToken = appPreferences.supabaseRefreshToken.get()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Supabase session expired, please sign in again")

        val url = "${appSecretsConfig.supabaseUrl}/auth/v1/token?grant_type=refresh_token"
        val payload = buildJsonObject { put("refresh_token", refreshToken) }
        val response = networkClients.unauthenticatedClient.post(url) {
            header("apikey", appSecretsConfig.supabaseAnonKey)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), payload))
        }
        val bodyText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException(extractSupabaseError(bodyText, response.status))
        }
        persistSessionFromAuthJson(bodyText)
        return appPreferences.supabaseAccessToken.get()
            ?: throw IllegalStateException("Missing refreshed access token")
    }

    private fun parseUrlAndFragmentParams(rawUrl: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        runCatching {
            val parsed = Url(rawUrl)
            parsed.parameters.forEach { key, values ->
                values.firstOrNull()?.let { result[key] = it }
            }
        }
        val fragment = rawUrl.substringAfter('#', "")
        if (fragment.isNotBlank()) {
            fragment.split('&').forEach { pair ->
                val eqIdx = pair.indexOf('=')
                if (eqIdx > 0) {
                    val key = pair.substring(0, eqIdx)
                    val value = pair.substring(eqIdx + 1)
                    result[key] = value
                }
            }
        }
        return result
    }

    private fun extractSupabaseError(body: String, status: HttpStatusCode): String {
        val parsed = runCatching {
            val obj = json.decodeFromString<JsonObject>(body)
            obj["error_description"]?.jsonPrimitive?.contentOrNull
                ?: obj["msg"]?.jsonPrimitive?.contentOrNull
                ?: obj["message"]?.jsonPrimitive?.contentOrNull
                ?: obj["error"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
        return parsed ?: "HTTP ${status.value} ${status.description}"
    }

}
