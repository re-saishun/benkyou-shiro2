package ua.syt0r.kanji.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ua.syt0r.kanji.AndroidAppSecretsConfig
import ua.syt0r.kanji.auth.AndroidGoogleSignInBridge
import ua.syt0r.kanji.auth.CurrentActivityTracker
import ua.syt0r.kanji.core.AppSecretsConfig
import ua.syt0r.kanji.core.speak.SpeakVoiceCallBridge
import ua.syt0r.kanji.core.supabase.GoogleSignInBridge
import ua.syt0r.kanji.presentation.screen.AndroidGetCreditLibrariesUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.credits.GetCreditLibrariesUseCase
import ua.syt0r.kanji.speak.FirebaseLiveVoiceCallBridge

val appComponentsModule = module {

    single<AppSecretsConfig> { AndroidAppSecretsConfig() }

    single { CurrentActivityTracker() }

    single<GoogleSignInBridge> {
        AndroidGoogleSignInBridge(
            activityTracker = get(),
            appSecretsConfig = get()
        )
    }

    single<SpeakVoiceCallBridge> {
        FirebaseLiveVoiceCallBridge(
            context = androidContext()
        )
    }

    factory<GetCreditLibrariesUseCase> {
        AndroidGetCreditLibrariesUseCase(
            context = androidContext()
        )
    }

}