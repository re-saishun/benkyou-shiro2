package ua.syt0r.kanji.core.supabase

sealed interface GoogleSignInResult {
    data class Success(val idToken: String, val email: String?) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data object NotConfigured : GoogleSignInResult
    data class Error(val message: String) : GoogleSignInResult
}

interface GoogleSignInBridge {
    suspend fun signIn(): GoogleSignInResult
}
