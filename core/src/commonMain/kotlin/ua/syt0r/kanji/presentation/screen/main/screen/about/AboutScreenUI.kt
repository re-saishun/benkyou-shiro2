package ua.syt0r.kanji.presentation.screen.main.screen.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ua.syt0r.kanji.BuildConfig
import ua.syt0r.kanji.PlatformFeature
import ua.syt0r.kanji.presentation.common.AppDropdownMenu
import ua.syt0r.kanji.presentation.common.AppDropdownMenuItem
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.appCardBorder
import ua.syt0r.kanji.presentation.common.theme.appColors
import ua.syt0r.kanji.presentation.dialog.VersionChangeDialog
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.SocialButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreenUI(
    onUpButtonClick: () -> Unit,
    openLink: (String) -> Unit,
    navigateToCredits: () -> Unit,
    onFeedbackClick: () -> Unit = {},
    onDonateClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    appGithubUrl: String = "https://github.com/re-saishun/benkyou"
) {
    MultiplatformBackHandler(onBack = onUpButtonClick)

    val appColors = MaterialTheme.appColors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = resolveString { about.title }) },
                navigationIcon = {
                    IconButton(onClick = onUpButtonClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .wrapContentWidth()
                .widthIn(max = 460.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = appColors.card,
                border = appCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = resolveString { appName },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                    Text(
                        text = resolveString { about.appSubtitle },
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textMuted
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = resolveString { about.version(BuildConfig.versionName) },
                        style = MaterialTheme.typography.labelLarge,
                        color = appColors.accent
                    )
                    Text(
                        text = "${resolveString { about.forkAttributionTitle }} • ${resolveString { about.forkAttributionDescription }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textMuted
                    )
                }
            }

            AboutActionRow(
                icon = Icons.Outlined.Code,
                title = resolveString { about.githubTitle },
                subtitle = appGithubUrl.removePrefix("https://"),
                onClick = { openLink(appGithubUrl) }
            )

            AboutActionRow(
                icon = Icons.Outlined.Source,
                title = resolveString { about.kanjiDojoGithubTitle },
                subtitle = resolveString { about.kanjiDojoGithubDescription },
                onClick = { openLink(KanjiDojoGithubLink) }
            )

            var showSocialDropdown by rememberSaveable { mutableStateOf(false) }
            Box {
                AboutActionRow(
                    icon = Icons.Outlined.Forum,
                    title = resolveString { about.socialTitle },
                    subtitle = resolveString { about.socialDescription },
                    onClick = { showSocialDropdown = true }
                )
                AppDropdownMenu(
                    expanded = showSocialDropdown,
                    onDismissRequest = { showSocialDropdown = false }
                ) {
                    SocialButton.entries.forEach { social ->
                        AppDropdownMenuItem(
                            onClick = {
                                showSocialDropdown = false
                                openLink(social.url)
                            },
                            content = {
                                Icon(painterResource(social.icon), null)
                                Text(stringResource(social.title))
                            }
                        )
                    }
                }
            }

            AboutActionRow(
                icon = Icons.Outlined.Download,
                title = resolveString { about.downloadsTitle },
                subtitle = resolveString { about.downloadsDescription },
                onClick = onDownloadsClick
            )

            AboutActionRow(
                icon = Icons.Outlined.Feedback,
                title = resolveString { about.feedbackTitle },
                subtitle = resolveString { about.feedbackDescription },
                onClick = onFeedbackClick
            )

            if (PlatformFeature.supported) {
                AboutActionRow(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = resolveString { about.donateTitle },
                    subtitle = resolveString { about.donateDescription },
                    onClick = onDonateClick
                )
            }

            var shouldShowVersionChangeDialog by remember { mutableStateOf(false) }
            if (shouldShowVersionChangeDialog) {
                VersionChangeDialog { shouldShowVersionChangeDialog = false }
            }

            AboutActionRow(
                icon = Icons.Outlined.History,
                title = resolveString { about.versionChangesTitle },
                subtitle = resolveString { about.versionChangesDescription },
                onClick = { shouldShowVersionChangeDialog = true }
            )

            AboutActionRow(
                icon = Icons.Outlined.Info,
                title = resolveString { about.creditsTitle },
                subtitle = resolveString { about.creditsDescription },
                onClick = navigateToCredits
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AboutActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
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
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.textPrimary
                )
            },
            supportingContent = {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
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
