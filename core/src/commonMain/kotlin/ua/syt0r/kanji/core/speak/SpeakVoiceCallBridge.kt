package ua.syt0r.kanji.core.speak

import kotlinx.coroutines.flow.StateFlow

enum class SpeakCallConnectionState {
    Idle,
    Connecting,
    Live,
    Error
}

data class SpeakVoiceCallStatus(
    val connectionState: SpeakCallConnectionState = SpeakCallConnectionState.Idle,
    val isMuted: Boolean = false,
    val errorMessage: String? = null
)

interface SpeakVoiceCallBridge {
    val isSupported: Boolean
    val status: StateFlow<SpeakVoiceCallStatus>
    suspend fun startCall(systemPrompt: String, voiceName: String = "Aoede")
    suspend fun stopCall()
    suspend fun toggleMute()
}
