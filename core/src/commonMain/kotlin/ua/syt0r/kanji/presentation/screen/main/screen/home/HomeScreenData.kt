package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.HomeOutline
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.grammar_dashboard.GrammarDashboardScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.memorize.MemorizeScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreen

enum class HomeScreenTab(
    val analyticsName: String,
    val iconContent: @Composable () -> Unit,
    val titleResolver: StringResolveScope<String>,
    val content: @Composable (MainNavigationState) -> Unit
) {

    GeneralDashboard(
        analyticsName = "general_dashboard",
        iconContent = { Icon(ExtraIcons.HomeOutline, null) },
        titleResolver = { home.generalDashboardTabLabel },
        content = { GeneralDashboardScreen(it) }
    ),
    GrammarDashboard(
        analyticsName = "grammar_dashboard",
        iconContent = {
            Text(
                text = "文",
                fontSize = 18.textDp,
                fontWeight = FontWeight.Bold
            )
        },
        titleResolver = { home.grammarDashboardTabLabel },
        content = { GrammarDashboardScreen(mainNavigationState = it) }
    ),
    Memorize(
        analyticsName = "memorize",
        iconContent = { Icon(Icons.Outlined.Psychology, null) },
        titleResolver = { home.memorizeDashboardTabLabel },
        content = { MemorizeScreen(mainNavigationState = it) }
    ),
    Search(
        analyticsName = "search",
        iconContent = { Icon(Icons.Default.Search, null) },
        titleResolver = { home.searchTabLabel },
        content = { SearchScreen(mainNavigationState = it) }
    );

    val buttonTestTag = name

    companion object {
        val VisibleTabs: List<HomeScreenTab> = entries
    }

}

data class SyncIconState(
    val loading: Boolean = false,
    val indicator: SyncIconIndicator = SyncIconIndicator.Disabled
)

enum class SyncIconIndicator {
    Disabled,
    PendingUpload,
    UpToDate,
    Canceled,
    Error,
    Conflict
}
