package ua.syt0r.kanji.presentation.screen.main.screen.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.AppSecretsConfig
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic

private const val DownloadsUrl = "https://kanji-dojo.com/"

@Composable
fun AboutScreen(
    mainNavigationState: MainNavigationState,
    viewModel: AboutScreenContract.ViewModel = getMultiplatformViewModel()
) {
    val uriHandler = LocalUriHandler.current
    val secretsConfig = koinInject<AppSecretsConfig>()

    AboutScreenUI(
        onUpButtonClick = { mainNavigationState.navigateBack() },
        openLink = { url ->
            uriHandler.openUri(url)
            viewModel.reportUrlClick(url)
        },
        navigateToCredits = { mainNavigationState.navigate(MainDestination.Credits) },
        onFeedbackClick = { mainNavigationState.navigate(MainDestination.Feedback(FeedbackTopic.General)) },
        onDonateClick = { mainNavigationState.navigate(MainDestination.Sponsor) },
        onDownloadsClick = { uriHandler.openUri(DownloadsUrl) },
        appGithubUrl = secretsConfig.resolvedAppGithubUrl
    )
}
