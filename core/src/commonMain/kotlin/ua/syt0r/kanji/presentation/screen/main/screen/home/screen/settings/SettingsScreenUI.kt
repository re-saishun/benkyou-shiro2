package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.general_dashboard_tutorial
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.appCardBorder
import ua.syt0r.kanji.presentation.common.theme.appColors
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.ui.TutorialDialog
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum
import kotlin.enums.EnumEntries

private enum class SettingsSection {
    Root,
    Account,
    System
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenUI(
    state: State<ScreenState>,
    onNavigateBack: () -> Unit,
    onBackupButtonClick: () -> Unit,
    onAccountButtonClick: () -> Unit,
    onSyncButtonClick: () -> Unit,
    onAboutButtonClick: () -> Unit,
    systemContent: @Composable ColumnScope.(ScreenState.Loaded) -> Unit
) {

    var currentSection by rememberSaveable { mutableStateOf(SettingsSection.Root) }

    val handleBack: () -> Unit = {
        if (currentSection != SettingsSection.Root) {
            currentSection = SettingsSection.Root
        } else {
            onNavigateBack()
        }
    }

    MultiplatformBackHandler(onBack = handleBack)

    var showTutorialDialog by remember { mutableStateOf(false) }
    if (showTutorialDialog) {
        TutorialDialog { showTutorialDialog = false }
    }

    val title = when (currentSection) {
        SettingsSection.Root -> resolveString { home.settingsTabLabel }
        SettingsSection.Account -> resolveString { settings.account }
        SettingsSection.System -> resolveString { settings.systemTitle }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = title
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.value to currentSection,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { (screenState, section) ->

            when (screenState) {
                ScreenState.Loading -> {
                    CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
                }

                is ScreenState.Loaded -> {
                    when (section) {
                        SettingsSection.Root -> {
                            SettingsContent(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SettingsCategoryCard(
                                    icon = Icons.Outlined.AccountCircle,
                                    title = resolveString { settings.account },
                                    subtitle = resolveString { settings.accountSectionDescription },
                                    testTag = "settings_category_account",
                                    onClick = { currentSection = SettingsSection.Account }
                                )

                                SettingsCategoryCard(
                                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                                    title = stringResource(Res.string.general_dashboard_tutorial),
                                    subtitle = resolveString { settings.tutorialDescription },
                                    testTag = "settings_category_tutorial",
                                    onClick = { showTutorialDialog = true }
                                )

                                SettingsCategoryCard(
                                    icon = Icons.Outlined.Settings,
                                    title = resolveString { settings.systemTitle },
                                    subtitle = resolveString { settings.systemDescription },
                                    testTag = "settings_category_system",
                                    onClick = { currentSection = SettingsSection.System }
                                )

                                SettingsCategoryCard(
                                    icon = Icons.Outlined.Info,
                                    title = resolveString { settings.aboutTitle },
                                    subtitle = resolveString { settings.aboutDescription },
                                    testTag = "settings_category_about",
                                    onClick = onAboutButtonClick
                                )

                                Spacer(Modifier.height(20.dp))
                            }
                        }

                        SettingsSection.Account -> {
                            SettingsContent(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SettingsCategoryCard(
                                    icon = Icons.Outlined.AccountCircle,
                                    title = resolveString { settings.account },
                                    subtitle = resolveString { account.supabaseSectionDescription },
                                    testTag = "settings_account_button",
                                    onClick = onAccountButtonClick
                                )
                                SettingsCategoryCard(
                                    icon = Icons.Outlined.Backup,
                                    title = resolveString { settings.backupTitle },
                                    subtitle = resolveString { backup.title },
                                    testTag = "settings_backup_button",
                                    onClick = onBackupButtonClick
                                )
                                SettingsCategoryCard(
                                    icon = Icons.Outlined.Sync,
                                    title = resolveString { settings.sync },
                                    subtitle = resolveString { sync.guideTitle },
                                    testTag = "settings_sync_button",
                                    onClick = onSyncButtonClick
                                )
                                Spacer(Modifier.height(20.dp))
                            }
                        }

                        SettingsSection.System -> {
                            SettingsContent {
                                systemContent(screenState)
                                Spacer(Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    val appColors = MaterialTheme.appColors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = appColors.card,
        border = appCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = appColors.accent
                )
            },
            headlineContent = {
                Text(
                    text = title,
                    color = appColors.textPrimary
                )
            },
            supportingContent = {
                Text(
                    text = subtitle,
                    color = appColors.textMuted
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = appColors.textMuted
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun SettingsContent(
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentWidth()
            .verticalScroll(rememberScrollState())
            .widthIn(max = 440.dp)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = verticalArrangement
    ) {

        val orientation = LocalOrientation.current
        if (orientation == Orientation.Landscape) {
            Spacer(Modifier.height(12.dp))
        }

        content()

    }

}

@Composable
fun SettingsSwitchRow(
    title: String,
    message: String,
    isEnabled: Boolean,
    onToggled: () -> Unit
) {

    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(message) },
        trailingContent = {
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggled() },
                colors = SwitchDefaults.colors(
                    uncheckedTrackColor = MaterialTheme.colorScheme.background
                )
            )
        }
    )

}

@Composable
fun SettingsTextButton(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = when {
            subtitle != null -> {
                { Text(subtitle) }
            }

            else -> null
        },
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}

@Composable
fun <T> SettingsPreferencePickerDialog(
    onDismissRequest: () -> Unit,
    title: String,
    options: EnumEntries<T>,
    defaultSelected: T,
    onSelected: (T) -> Unit
) where T : DisplayableEnum, T : Enum<T> {

    var selected by remember { mutableStateOf(defaultSelected) }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        content = {
            val appColors = MaterialTheme.appColors
            options.forEach {
                val isOptionSelected = selected == it
                ListItem(
                    headlineContent = {
                        Text(resolveString(it.titleResolver))
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
                        containerColor = when {
                            isOptionSelected -> appColors.progressTrack
                            else -> Color.Transparent
                        },
                        headlineColor = appColors.textPrimary
                    ),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .clickable { selected = it }
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
            ) { Text(resolveString { settings.pickerDialogApply }) }
        }
    )

}
