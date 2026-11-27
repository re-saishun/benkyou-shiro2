package ua.syt0r.kanji.auth

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import ua.syt0r.kanji.core.AppSecretsConfig
import ua.syt0r.kanji.core.supabase.GoogleSignInBridge
import ua.syt0r.kanji.core.supabase.GoogleSignInResult
import java.lang.ref.WeakReference

class CurrentActivityTracker : Application.ActivityLifecycleCallbacks {

    private var currentActivityRef: WeakReference<Activity>? = null

    val currentActivity: Activity?
        get() = currentActivityRef?.get()?.takeIf { !it.isFinishing && !it.isDestroyed }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivityRef = WeakReference(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivityRef = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivityRef = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivityRef?.get() === activity) {
            currentActivityRef = null
        }
    }

}

class AndroidGoogleSignInBridge(
    private val activityTracker: CurrentActivityTracker,
    private val appSecretsConfig: AppSecretsConfig
) : GoogleSignInBridge {

    override suspend fun signIn(): GoogleSignInResult {
        val webClientId = appSecretsConfig.googleWebClientId
        if (webClientId.isBlank()) {
            return GoogleSignInResult.NotConfigured
        }

        val activity = activityTracker.currentActivity
            ?: return GoogleSignInResult.Error("No active window available for Google Sign-In")

        return try {
            val credentialManager = CredentialManager.create(activity)
            val option = GetSignInWithGoogleOption.Builder(webClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val response = credentialManager.getCredential(
                context = activity,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInResult.Success(
                    idToken = googleCred.idToken,
                    email = googleCred.id
                )
            } else {
                GoogleSignInResult.Error("Unexpected credential type returned")
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (_: NoCredentialException) {
            GoogleSignInResult.Error("No Google account available on device")
        } catch (t: Throwable) {
            GoogleSignInResult.Error(t.localizedMessage ?: "Google Sign-In failed")
        }
    }

}
