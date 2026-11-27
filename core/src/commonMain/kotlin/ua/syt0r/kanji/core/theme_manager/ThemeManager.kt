package ua.syt0r.kanji.core.theme_manager

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTheme
import ua.syt0r.kanji.core.user_data.preferences.PreferencesThemePalette


open class ThemeManager(
    private val appPreferences: PreferencesContract.AppPreferences,
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
) {

    protected val coroutineScope = CoroutineScope(dispatcher)

    private val _currentTheme = mutableStateOf(value = runBlocking { appPreferences.theme.get() })
    val currentTheme: State<PreferencesTheme> = _currentTheme

    private val _currentPalette = mutableStateOf(value = runBlocking { appPreferences.themePalette.get() })
    val currentPalette: State<PreferencesThemePalette> = _currentPalette

    init {
        appPreferences.theme
            .onModified
            .onEach { invalidate() }
            .launchIn(coroutineScope)
        appPreferences.themePalette
            .onModified
            .onEach { invalidate() }
            .launchIn(coroutineScope)
    }

    fun changeTheme(theme: PreferencesTheme) {
        coroutineScope.launch { appPreferences.theme.set(theme) }
    }

    fun changePalette(palette: PreferencesThemePalette) {
        coroutineScope.launch { appPreferences.themePalette.set(palette) }
    }

    fun invalidate() {
        Logger.logMethod()
        coroutineScope.launch {
            val currentTheme = appPreferences.theme.get()
            val currentPalette = appPreferences.themePalette.get()
            _currentTheme.value = currentTheme
            _currentPalette.value = currentPalette
            platformInvalidate(currentTheme)
        }
    }

    open fun platformInvalidate(theme: PreferencesTheme) {}

    val isDarkTheme: Boolean
        @Composable
        get() = when (currentTheme.value) {
            PreferencesTheme.System -> isSystemInDarkTheme()
            PreferencesTheme.Light -> false
            PreferencesTheme.Dark, PreferencesTheme.Amoled -> true
        }

    val isAmoledTheme: Boolean
        get() = currentTheme.value == PreferencesTheme.Amoled

}

val LocalThemeManager = compositionLocalOf<ThemeManager> {
    throw IllegalStateException("ThemeManager is not initialized")
}