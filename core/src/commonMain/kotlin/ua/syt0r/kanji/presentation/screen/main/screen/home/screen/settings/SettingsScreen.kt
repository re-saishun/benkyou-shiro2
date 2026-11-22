package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalUriHandler
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic

private const val DownloadsUrl = "https://kanji-dojo.com/"

@Composable
fun SettingsScreen(
    mainNavigationState: MainNavigationState,
    viewModel: SettingsScreenContract.ViewModel = getMultiplatformViewModel()
) {
    val uriHandler = LocalUriHandler.current

    SettingsScreenUI(
        state = viewModel.state.collectAsState(),
        onNavigateBack = {
            mainNavigationState.navigateBack()
        },
        onSocialClick = {
            uriHandler.openUri(it.url)
        },
        onDownloadsClick = {
            uriHandler.openUri(DownloadsUrl)
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
        onFeedbackButtonClick = {
            mainNavigationState.navigate(MainDestination.Feedback(FeedbackTopic.General))
        },
        onAboutButtonClick = {
            mainNavigationState.navigate(MainDestination.About)
        },
        onDonateButtonClick = {
            mainNavigationState.navigate(MainDestination.Sponsor)
        },
        loadedContent = { screenState ->
            screenState.items.forEach { it.content(mainNavigationState) }
        }
    )

}