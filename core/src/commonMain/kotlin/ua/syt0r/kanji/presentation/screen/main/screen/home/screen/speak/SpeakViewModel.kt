package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.speak

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.speak.SpeakCallConnectionState
import ua.syt0r.kanji.core.speak.SpeakVoiceCallBridge
import ua.syt0r.kanji.core.streak.DailyStreakTracker

class SpeakViewModel(
    private val coroutineScope: CoroutineScope,
    private val speakVoiceCallBridge: SpeakVoiceCallBridge,
    private val dailyStreakTracker: DailyStreakTracker
) : SpeakScreenContract.ViewModel {

    private val _state = MutableStateFlow(
        SpeakUiState(isSupported = speakVoiceCallBridge.isSupported)
    )
    override val state: StateFlow<SpeakUiState> = _state

    private var timerJob: Job? = null

    init {
        speakVoiceCallBridge.status
            .onEach { status ->
                val wasLive = _state.value.connectionState == SpeakCallConnectionState.Live
                val isNowLive = status.connectionState == SpeakCallConnectionState.Live

                _state.value = _state.value.copy(
                    connectionState = status.connectionState,
                    isMuted = status.isMuted,
                    errorMessage = status.errorMessage,
                    elapsedSeconds = if (status.connectionState == SpeakCallConnectionState.Idle ||
                        status.connectionState == SpeakCallConnectionState.Error
                    ) 0 else _state.value.elapsedSeconds
                )

                if (!wasLive && isNowLive) {
                    startElapsedTimer()
                    coroutineScope.launch {
                        dailyStreakTracker.recordPractice()
                    }
                } else if (wasLive && !isNowLive) {
                    stopElapsedTimer()
                }
            }
            .launchIn(coroutineScope)
    }

    override fun selectTopic(topic: SpeakTopic) {
        _state.value = _state.value.copy(selectedTopic = topic)
    }

    override fun selectLevel(level: SpeakLevel) {
        _state.value = _state.value.copy(selectedLevel = level)
    }

    override fun updateCustomPrompt(prompt: String) {
        _state.value = _state.value.copy(customPrompt = prompt)
    }

    override fun startCall() {
        val current = _state.value
        val systemPrompt = buildSystemPrompt(
            topic = current.selectedTopic,
            level = current.selectedLevel,
            customPrompt = current.customPrompt
        )
        coroutineScope.launch {
            speakVoiceCallBridge.startCall(systemPrompt = systemPrompt)
        }
    }

    override fun stopCall() {
        stopElapsedTimer()
        coroutineScope.launch {
            speakVoiceCallBridge.stopCall()
        }
    }

    override fun toggleMute() {
        coroutineScope.launch {
            speakVoiceCallBridge.toggleMute()
        }
    }

    private fun startElapsedTimer() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            _state.value = _state.value.copy(elapsedSeconds = 0)
            while (true) {
                delay(1000L)
                _state.value = _state.value.copy(
                    elapsedSeconds = _state.value.elapsedSeconds + 1
                )
            }
        }
    }

    private fun stopElapsedTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun buildSystemPrompt(
        topic: SpeakTopic,
        level: SpeakLevel,
        customPrompt: String
    ): String {
        val scenario = when (topic) {
            SpeakTopic.FreeTalk -> "Casual everyday free conversation in Japanese about the learner's day, hobbies, and interests."
            SpeakTopic.CafeOrder -> "Roleplay as a friendly barista in a Tokyo cafe taking the learner's coffee/pastry order."
            SpeakTopic.ConvenienceStore -> "Roleplay as a Japanese convenience store (konbini) clerk helping with checkout, bento warming, and bags."
            SpeakTopic.StationTravel -> "Roleplay as a Japanese train station staff helping the learner buy a ticket, transfer trains, or find an exit."
            SpeakTopic.SelfIntro -> "Roleplay as a new Japanese friend meeting the learner for the first time (jikoshoukai)."
            SpeakTopic.Custom -> customPrompt.ifBlank { "Natural everyday Japanese conversation roleplay." }
        }

        return """
            You are Shiro, a warm, encouraging native Japanese speaking partner and tutor inside the Shiro Japanese study app.
            Learner target level: JLPT ${level.label}.
            Scenario: $scenario

            Guidelines:
            - Speak primarily in natural spoken Japanese suited to JLPT ${level.label}.
            - Keep each reply concise (1 to 3 short sentences) so the conversation feels brisk like a real phone call.
            - End most turns with an inviting follow-up question to keep the learner speaking.
            - If the learner makes a grammar or vocabulary mistake, gently model the natural phrasing and encourage them.
            - If the learner asks in English or seems stuck, briefly clarify in simple English and switch back to Japanese.
            - Start the conversation right away by greeting the learner warmly in Japanese for this scenario.
        """.trimIndent()
    }

}
