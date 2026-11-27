package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.speak

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.speak.SpeakCallConnectionState

enum class SpeakTopic {
    FreeTalk,
    CafeOrder,
    ConvenienceStore,
    StationTravel,
    SelfIntro,
    Custom
}

enum class SpeakLevel(val label: String) {
    N5("N5"),
    N4("N4"),
    N3("N3"),
    N2("N2"),
    N1("N1")
}

data class SpeakStarterPhrase(
    val japanese: String,
    val reading: String,
    val translation: String
)

data class SpeakUiState(
    val isSupported: Boolean = true,
    val connectionState: SpeakCallConnectionState = SpeakCallConnectionState.Idle,
    val isMuted: Boolean = false,
    val errorMessage: String? = null,
    val selectedTopic: SpeakTopic = SpeakTopic.FreeTalk,
    val selectedLevel: SpeakLevel = SpeakLevel.N4,
    val customPrompt: String = "",
    val elapsedSeconds: Int = 0
)

interface SpeakScreenContract {

    interface ViewModel {
        val state: StateFlow<SpeakUiState>
        fun selectTopic(topic: SpeakTopic)
        fun selectLevel(level: SpeakLevel)
        fun updateCustomPrompt(prompt: String)
        fun startCall()
        fun stopCall()
        fun toggleMute()
    }

}
