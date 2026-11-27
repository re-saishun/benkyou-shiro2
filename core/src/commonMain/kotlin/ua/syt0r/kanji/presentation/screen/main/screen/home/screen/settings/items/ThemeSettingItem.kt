package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.theme_manager.ThemeManager
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTheme
import ua.syt0r.kanji.core.user_data.preferences.PreferencesThemePalette
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.appCardBorder
import ua.syt0r.kanji.presentation.common.theme.appColors
import ua.syt0r.kanji.presentation.common.theme.resolveTokens
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum

enum class DisplayableTheme(
    val prefType: PreferencesTheme,
    override val titleResolver: StringResolveScope<String>
) : DisplayableEnum {

    System(PreferencesTheme.System, { settings.themeSystem }),
    Light(PreferencesTheme.Light, { settings.themeLight }),
    Dark(PreferencesTheme.Dark, { settings.themeDark });

    companion object {
        fun from(prefType: PreferencesTheme): DisplayableTheme = when (prefType) {
            PreferencesTheme.System -> System
            PreferencesTheme.Light -> Light
            PreferencesTheme.Dark, PreferencesTheme.Amoled -> Dark
        }
    }

}

enum class DisplayableThemePalette(
    val prefType: PreferencesThemePalette,
    override val titleResolver: StringResolveScope<String>
) : DisplayableEnum {

    Shu(PreferencesThemePalette.Shu, { settings.themePaletteShu }),
    Sumi(PreferencesThemePalette.Sumi, { settings.themePaletteSumi }),
    Matcha(PreferencesThemePalette.Matcha, { settings.themePaletteMatcha }),
    Sakura(PreferencesThemePalette.Sakura, { settings.themePaletteSakura }),
    Ai(PreferencesThemePalette.Ai, { settings.themePaletteAi }),
    Suna(PreferencesThemePalette.Suna, { settings.themePaletteSuna });

    companion object {
        fun from(prefType: PreferencesThemePalette): DisplayableThemePalette =
            entries.first { it.prefType == prefType }
    }

}

class ThemeSettingItem(
    private val themeManager: ThemeManager
) : SettingsScreenContract.ListItem {

    @Composable
    override fun content(mainNavigationState: MainNavigationState) {

        var showPalettePicker by rememberSaveable { mutableStateOf(false) }

        val currentMode = DisplayableTheme.from(themeManager.currentTheme.value)
        val currentPalette = DisplayableThemePalette.from(themeManager.currentPalette.value)
        val isDark = themeManager.isDarkTheme || themeManager.isAmoledTheme

        Column {
            ListItem(
                headlineContent = { Text(resolveString { settings.themeTitle }) },
                supportingContent = { Text(resolveString(currentPalette.titleResolver)) },
                trailingContent = {
                    PaletteSwatchRow(
                        palette = currentPalette.prefType,
                        isDark = isDark
                    )
                },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .fillMaxWidth()
                    .clickable { showPalettePicker = true }
                    .testTag("settings_theme_palette_button")
            )

            ThemeModeSegmentedRow(
                selectedMode = currentMode,
                onModeSelected = { themeManager.changeTheme(it.prefType) }
            )
        }

        if (showPalettePicker) {
            ThemePalettePickerDialog(
                defaultSelected = currentPalette,
                isDark = isDark,
                onDismissRequest = { showPalettePicker = false },
                onSelected = { themeManager.changePalette(it.prefType) }
            )
        }

    }

}

@Composable
private fun ThemeModeSegmentedRow(
    selectedMode: DisplayableTheme,
    onModeSelected: (DisplayableTheme) -> Unit
) {
    val appColors = MaterialTheme.appColors
    val modes = listOf(DisplayableTheme.Dark, DisplayableTheme.Light, DisplayableTheme.System)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = resolveString { settings.themeModeTitle },
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.textMuted
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = appColors.card,
            border = appCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                modes.forEach { mode ->
                    val isSelected = mode == selectedMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) appColors.accent else appColors.card)
                            .clickable { onModeSelected(mode) }
                            .testTag("settings_theme_mode_${mode.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = resolveString(mode.titleResolver),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) appColors.onAccent else appColors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteSwatchRow(
    palette: PreferencesThemePalette,
    isDark: Boolean
) {
    val tokens = palette.resolveTokens(useDarkTheme = isDark)
    val borderColor = MaterialTheme.appColors.progressTrack

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(tokens.background, tokens.card, tokens.accent).forEach { swatchColor ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(swatchColor)
                    .border(0.5.dp, borderColor, CircleShape)
            )
        }
    }
}

@Composable
private fun ThemePalettePickerDialog(
    defaultSelected: DisplayableThemePalette,
    isDark: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (DisplayableThemePalette) -> Unit
) {
    var selected by remember { mutableStateOf(defaultSelected) }
    val appColors = MaterialTheme.appColors

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(resolveString { settings.themeTitle }) },
        content = {
            DisplayableThemePalette.entries.forEach { palette ->
                val isOptionSelected = selected == palette
                ListItem(
                    leadingContent = {
                        PaletteSwatchRow(
                            palette = palette.prefType,
                            isDark = isDark
                        )
                    },
                    headlineContent = {
                        Text(resolveString(palette.titleResolver))
                    },
                    trailingContent = {
                        if (isOptionSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = appColors.accent
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = if (isOptionSelected) appColors.progressTrack else Color.Transparent,
                        headlineColor = appColors.textPrimary
                    ),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .clickable { selected = palette }
                        .testTag("settings_theme_palette_option_${palette.name}")
                )
            }
        },
        buttons = {
            TextButton(onDismissRequest) {
                Text(resolveString { settings.pickerDialogCancel })
            }
            TextButton(
                onClick = {
                    onSelected(selected)
                    onDismissRequest()
                }
            ) {
                Text(resolveString { settings.pickerDialogApply })
            }
        }
    )
}