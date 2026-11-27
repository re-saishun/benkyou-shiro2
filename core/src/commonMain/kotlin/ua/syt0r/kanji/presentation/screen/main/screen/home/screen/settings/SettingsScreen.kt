package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import androidx.compose.runtime.collectAsState

@Composable
fun SettingsScreen(
    mainNavigationState: MainNavigationState,
    viewModel: SettingsScreenContract.ViewModel = getMultiplatformViewModel()
) {
    SettingsScreenUI(
        state = viewModel.state.collectAsState(),
        onNavigateBack = {
            mainNavigationState.navigateBack()
        },
        onBackupButtonClick = {
            mainNavigationState.navigate(MainDestination.Backup)
        },
        onAccountButtonClick = {
            mainNavigationState.navigate(MainDestination.Account())
        },
        onSyncButtonClick = {
            mainNavigationState.navigate(MainDestination.Sync)
        },
        onAboutButtonClick = {
            mainNavigationState.navigate(MainDestination.About)
        },
        systemContent = { screenState ->
            screenState.items.forEach { it.content(mainNavigationState) }
        }
    )
}