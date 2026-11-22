package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContentLanguage
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsPreferencePickerDialog
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum

enum class DisplayableContentLanguage(
    val prefType: PreferencesContentLanguage,
    override val titleResolver: StringResolveScope<String>
) : DisplayableEnum {

    English(PreferencesContentLanguage.English, { settings.contentLanguageEnglish }),
    Indonesian(PreferencesContentLanguage.Indonesian, { settings.contentLanguageIndonesian });

    companion object {
        fun from(prefType: PreferencesContentLanguage): DisplayableContentLanguage =
            entries.firstOrNull { it.prefType == prefType } ?: English
    }

}

class ContentLanguageSettingItem(
    private val appPreferences: PreferencesContract.AppPreferences
) : SettingsScreenContract.ConfigurableListItem {

    private lateinit var configuration: MutableState<DisplayableContentLanguage>

    override suspend fun prepare(coroutineScope: CoroutineScope) {
        configuration = mutableStateOf(
            DisplayableContentLanguage.from(appPreferences.contentLanguage.get())
        )

        snapshotFlow { configuration.value }
            .drop(1)
            .map { it.prefType }
            .onEach { appPreferences.contentLanguage.set(it) }
            .launchIn(coroutineScope)
    }

    @Composable
    override fun content(mainNavigationState: MainNavigationState) {
        val label = resolveString { settings.contentLanguageTitle }
        var showPicker by rememberSaveable { mutableStateOf(false) }

        ListItem(
            modifier = Modifier.clip(MaterialTheme.shapes.medium)
                .fillMaxWidth()
                .clickable { showPicker = true },
            headlineContent = { Text(label) },
            supportingContent = { Text(resolveString(configuration.value.titleResolver)) }
        )

        if (showPicker) {
            SettingsPreferencePickerDialog(
                onDismissRequest = { showPicker = false },
                title = label,
                options = DisplayableContentLanguage.entries,
                defaultSelected = configuration.value,
                onSelected = { configuration.value = it }
            )
        }
    }

}
