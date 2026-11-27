package ua.syt0r.kanji.speak

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import ua.syt0r.kanji.core.speak.SpeakCallConnectionState
import ua.syt0r.kanji.core.speak.SpeakVoiceCallBridge
import ua.syt0r.kanji.core.speak.SpeakVoiceCallStatus

private const val TAG = "FirebaseLiveVoiceCall"
private const val LIVE_MODEL_NAME = "gemini-2.5-flash-native-audio-preview-12-2025"

@OptIn(PublicPreviewAPI::class)
class FirebaseLiveVoiceCallBridge(
    private val context: Context
) : SpeakVoiceCallBridge {

    override val isSupported: Boolean = true

    private val mutex = Mutex()
    private val _status = MutableStateFlow(SpeakVoiceCallStatus())
    override val status: StateFlow<SpeakVoiceCallStatus> = _status

    private var activeSession: LiveSession? = null

    @SuppressLint("MissingPermission")
    override suspend fun startCall(systemPrompt: String, voiceName: String) {
        mutex.withLock {
            val hasMicPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasMicPermission) {
                _status.value = SpeakVoiceCallStatus(
                    connectionState = SpeakCallConnectionState.Error,
                    errorMessage = "Microphone permission is required for live voice conversation."
                )
                return
            }

            runCatching { closeExistingSessionInternal() }
            _status.value = SpeakVoiceCallStatus(
                connectionState = SpeakCallConnectionState.Connecting,
                isMuted = false,
                errorMessage = null
            )

            try {
                withTimeout(30_000L) {
                    val model = Firebase.ai(backend = GenerativeBackend.googleAI()).liveModel(
                        modelName = LIVE_MODEL_NAME,
                        generationConfig = liveGenerationConfig {
                            responseModality = ResponseModality.AUDIO
                            speechConfig = SpeechConfig(voice = Voice(voiceName))
                        },
                        systemInstruction = content {
                            text(systemPrompt)
                        }
                    )
                    val session = model.connect()
                    activeSession = session
                    session.startAudioConversation()
                }
                _status.value = SpeakVoiceCallStatus(
                    connectionState = SpeakCallConnectionState.Live,
                    isMuted = false,
                    errorMessage = null
                )
            } catch (e: CancellationException) {
                if (e is TimeoutCancellationException) {
                    Log.e(TAG, "Timed out connecting Live AI voice session", e)
                    runCatching { closeExistingSessionInternal() }
                    _status.value = SpeakVoiceCallStatus(
                        connectionState = SpeakCallConnectionState.Error,
                        isMuted = false,
                        errorMessage = "Voice call connection timed out. Please try again."
                    )
                } else {
                    runCatching { closeExistingSessionInternal() }
                    throw e
                }
            } catch (throwable: Exception) {
                Log.e(TAG, "Failed to start Live AI voice session", throwable)
                runCatching { closeExistingSessionInternal() }
                _status.value = SpeakVoiceCallStatus(
                    connectionState = SpeakCallConnectionState.Error,
                    isMuted = false,
                    errorMessage = throwable.localizedMessage ?: "Failed to connect live voice call"
                )
            }
        }
    }

    override suspend fun stopCall() {
        mutex.withLock {
            closeExistingSessionInternal()
            _status.value = SpeakVoiceCallStatus(
                connectionState = SpeakCallConnectionState.Idle,
                isMuted = false,
                errorMessage = null
            )
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun toggleMute() {
        mutex.withLock {
            val session = activeSession ?: return
            val current = _status.value
            if (current.connectionState != SpeakCallConnectionState.Live) return

            val nextMuted = !current.isMuted
            runCatching {
                if (nextMuted) {
                    session.stopAudioConversation()
                } else {
                    val hasMicPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasMicPermission) {
                        throw IllegalStateException("Microphone permission missing")
                    }
                    session.startAudioConversation()
                }
                _status.value = current.copy(isMuted = nextMuted)
            }.onFailure { throwable ->
                Log.e(TAG, "Failed to toggle mute state", throwable)
                _status.value = current.copy(
                    connectionState = SpeakCallConnectionState.Error,
                    errorMessage = throwable.localizedMessage ?: "Microphone toggle failed"
                )
            }
        }
    }

    private suspend fun closeExistingSessionInternal() {
        val session = activeSession ?: return
        activeSession = null
        runCatching { session.stopAudioConversation() }
        runCatching { session.close() }
    }

}
