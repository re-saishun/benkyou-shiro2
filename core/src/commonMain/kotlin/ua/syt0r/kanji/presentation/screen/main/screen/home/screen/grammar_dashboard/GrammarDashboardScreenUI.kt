package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.grammar_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.core.grammar.GrammarPackStatus
import ua.syt0r.kanji.core.grammar.RemoteGrammarPack
import ua.syt0r.kanji.presentation.common.AppDropdownMenu
import ua.syt0r.kanji.presentation.common.AppDropdownMenuItem
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.grammar_dashboard.GrammarDashboardScreenContract.CatalogState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.grammar_dashboard.GrammarDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration
import ua.syt0r.kanji.srs_status_due
import ua.syt0r.kanji.srs_status_new

@Composable
fun GrammarDashboardScreenUI(
    screenState: State<ScreenState>,
    catalogState: State<CatalogState>,
    onCatalogOpened: () -> Unit,
    onCatalogCheck: (String) -> Unit,
    onDownloadPack: (RemoteGrammarPack) -> Unit,
    onCreateEmptyDeck: (String) -> Unit,
    onDeleteDeck: (GrammarDeckItem) -> Unit,
    onStartPractice: (GrammarDeckPracticeItem, List<GrammarPracticeScreenConfiguration.Card>) -> Unit
) {

    var showCatalogDialog by rememberSaveable { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<GrammarDeckItem?>(null) }

    if (showCatalogDialog) {
        LaunchedEffect(Unit) { onCatalogOpened() }
        GrammarCatalogDialog(
            catalogState = catalogState,
            onDismissRequest = { showCatalogDialog = false },
            onCheck = onCatalogCheck,
            onDownloadPack = onDownloadPack,
            onCreateEmptyDeck = {
                onCreateEmptyDeck(it)
                showCatalogDialog = false
            }
        )
    }

    deckToDelete?.let { deck ->
        val strings = resolveString { grammar }
        MultiplatformDialog(
            onDismissRequest = { deckToDelete = null },
            title = { Text(strings.deleteDeckDialogTitle) },
            content = { Text(strings.deleteDeckDialogMessage(deck.title)) },
            buttons = {
                TextButton(onClick = { deckToDelete = null }) {
                    Text(strings.closeButton)
                }
                TextButton(
                    onClick = {
                        onDeleteDeck(deck)
                        deckToDelete = null
                    }
                ) {
                    Text(strings.deleteDeckButton)
                }
            }
        )
    }

    Box(Modifier.fillMaxSize()) {

        AnimatedContent(
            targetState = screenState.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.fillMaxSize()
        ) { state ->

            when (state) {
                ScreenState.Loading -> {
                    FancyLoading(Modifier.fillMaxSize().wrapContentSize())
                }

                is ScreenState.Loaded -> {
                    if (state.decks.isEmpty()) {
                        EmptyState(onAddClick = { showCatalogDialog = true })
                    } else {
                        DeckList(
                            decks = state.decks,
                            onDeleteRequest = { deckToDelete = it },
                            onStartPractice = onStartPractice
                        )
                    }
                }
            }

        }

        FloatingActionButton(
            onClick = { showCatalogDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("grammar_add_pack_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = resolveString { grammar.addPackButton })
        }

    }

}

@Composable
private fun EmptyState(onAddClick: () -> Unit) {
    val strings = resolveString { grammar }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
            .widthIn(max = 360.dp)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "文",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = strings.emptyTitle,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = strings.emptyMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onAddClick) {
            Text(strings.addPackButton)
        }
    }
}

@Composable
private fun DeckList(
    decks: List<GrammarDeckItem>,
    onDeleteRequest: (GrammarDeckItem) -> Unit,
    onStartPractice: (GrammarDeckPracticeItem, List<GrammarPracticeScreenConfiguration.Card>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .wrapContentWidth()
            .widthIn(max = 480.dp)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        decks.forEach { deck ->
            GrammarDeckCard(
                deck = deck,
                onDeleteRequest = { onDeleteRequest(deck) },
                onStartPractice = onStartPractice
            )
        }
        // Space for the floating button
        Spacer(Modifier.height(72.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GrammarDeckCard(
    deck: GrammarDeckItem,
    onDeleteRequest: () -> Unit,
    onStartPractice: (GrammarDeckPracticeItem, List<GrammarPracticeScreenConfiguration.Card>) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = {},
                onLongClick = { showMenu = true }
            )
            .testTag("grammar_deck_${deck.deckId}"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    if (deck.level.isNotBlank()) {
                        Text(
                            text = deck.level,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = deck.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = resolveString { grammar.catalogPointCount }(deck.pointCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    AppDropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        AppDropdownMenuItem(
                            onClick = {
                                showMenu = false
                                onDeleteRequest()
                            }
                        ) {
                            Icon(Icons.Outlined.Delete, null)
                            Text(resolveString { grammar.deleteDeckButton })
                        }
                    }
                }
            }

            if (deck.description.isNotBlank()) {
                Text(
                    text = deck.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider()

            deck.practiceItems.forEach { practiceItem ->
                PracticeRow(
                    practiceItem = practiceItem,
                    onClick = {
                        onStartPractice(practiceItem, practiceItem.combinedCards)
                    }
                )
            }

        }
    }
}

@Composable
private fun PracticeRow(
    practiceItem: GrammarDeckPracticeItem,
    onClick: () -> Unit
) {
    val enabled = practiceItem.combinedCards.isNotEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .testTag("grammar_practice_${practiceItem.practiceType.name}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = resolveString(practiceItem.practiceType.titleResolver),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        CountLabel(
            title = stringResource(Res.string.srs_status_new),
            count = practiceItem.newCards.size
        )
        CountLabel(
            title = stringResource(Res.string.srs_status_due),
            count = practiceItem.dueCards.size
        )
    }
}

@Composable
private fun CountLabel(title: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (count == 0) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GrammarCatalogDialog(
    catalogState: State<CatalogState>,
    onDismissRequest: () -> Unit,
    onCheck: (String) -> Unit,
    onDownloadPack: (RemoteGrammarPack) -> Unit,
    onCreateEmptyDeck: (String) -> Unit
) {
    val strings = resolveString { grammar }
    val state = catalogState.value

    var urlInput by rememberSaveable { mutableStateOf("") }
    var urlInitialized by rememberSaveable { mutableStateOf(false) }
    var localTitle by rememberSaveable { mutableStateOf("") }

    // Fill the input with the url the view model resolved (saved or default)
    val resolvedUrl = when (state) {
        is CatalogState.Loading -> state.url
        is CatalogState.Error -> state.url
        is CatalogState.Loaded -> state.url
        CatalogState.Idle -> null
    }
    LaunchedEffect(resolvedUrl) {
        if (!urlInitialized && resolvedUrl != null) {
            urlInput = resolvedUrl
            urlInitialized = true
        }
    }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(strings.catalogDialogTitle) },
        content = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text(strings.catalogUrlLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { onCheck(urlInput) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.catalogCheckButton)
                }

                when (state) {
                    CatalogState.Idle -> Unit

                    is CatalogState.Loading -> {
                        Text(
                            text = strings.catalogLoading,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is CatalogState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = { onCheck(urlInput) }) {
                            Text(strings.catalogRetry)
                        }
                    }

                    is CatalogState.Loaded -> {
                        state.error?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        state.packs.forEach { pack ->
                            PackRow(
                                pack = pack,
                                isDownloading = pack.entry.id in state.downloadingIds,
                                onDownload = { onDownloadPack(pack) }
                            )
                        }
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = localTitle,
                    onValueChange = { localTitle = it },
                    label = { Text(strings.catalogLocalTitleHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { onCreateEmptyDeck(localTitle) },
                    enabled = localTitle.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.catalogCreateLocalButton)
                }

            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(strings.closeButton)
            }
        }
    )
}

@Composable
private fun PackRow(
    pack: RemoteGrammarPack,
    isDownloading: Boolean,
    onDownload: () -> Unit
) {
    val strings = resolveString { grammar }
    val entry = pack.entry

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (entry.level.isNotBlank()) {
                    Text(
                        text = entry.level,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            when {
                isDownloading -> Text(
                    text = strings.catalogLoading,
                    style = MaterialTheme.typography.labelMedium
                )

                pack.status == GrammarPackStatus.Downloaded -> Text(
                    text = strings.catalogStatusDownloaded,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                else -> TextButton(onClick = onDownload) {
                    Text(
                        if (pack.status == GrammarPackStatus.UpdateAvailable) strings.catalogUpdateButton
                        else strings.catalogDownloadButton
                    )
                }
            }
        }

        if (entry.description.isNotBlank()) {
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val statusText = when (pack.status) {
            GrammarPackStatus.NotDownloaded -> strings.catalogStatusNotDownloaded
            GrammarPackStatus.Downloaded -> strings.catalogStatusDownloaded
            GrammarPackStatus.UpdateAvailable -> strings.catalogStatusUpdateAvailable
        }
        Text(
            text = listOf(strings.catalogPointCount(entry.pointCount), statusText)
                .joinToString(" · "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

    }
}
