package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.speak

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val speakScreenModule = module {

    multiplatformViewModel<SpeakScreenContract.ViewModel> {
        SpeakViewModel(
            coroutineScope = it.component1(),
            speakVoiceCallBridge = get(),
            dailyStreakTracker = get()
        )
    }

}
