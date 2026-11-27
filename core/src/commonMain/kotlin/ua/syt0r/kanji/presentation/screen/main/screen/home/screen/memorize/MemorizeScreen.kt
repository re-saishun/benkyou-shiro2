package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.memorize

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.srs.VocabSrsManager
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.appCardBorder
import ua.syt0r.kanji.presentation.common.theme.appColors
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreen

private enum class MemorizeMode { Menu, Letters, Vocab }

private data class MemorizeCardSummary(
    val decks: Int,
    val new: Int,
    val due: Int
)

private data class MemorizeSummary(
    val letters: MemorizeCardSummary,
    val vocab: MemorizeCardSummary
)

/**
 * Entry point of the "Memorize" home tab. Shows two big cards (Letters, Vocab) and
 * opens the existing dashboards inside the tab. Back returns to the menu.
 */
@Composable
fun MemorizeScreen(
    mainNavigationState: MainNavigationState
) {

    var mode by rememberSaveable { mutableStateOf(MemorizeMode.Menu) }

    if (mode != MemorizeMode.Menu) {
        MultiplatformBackHandler(onBack = { mode = MemorizeMode.Menu })
    }

    AnimatedContent(
        targetState = mode,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = Modifier.fillMaxSize()
    ) { targetMode ->

        when (targetMode) {
            MemorizeMode.Menu -> MemorizeMenu(
                onLettersClick = { mode = MemorizeMode.Letters },
                onVocabClick = { mode = MemorizeMode.Vocab }
            )

            MemorizeMode.Letters -> MemorizeSubScreen(
                title = resolveString { memorize.lettersTitle },
                onBack = { mode = MemorizeMode.Menu }
            ) {
                LettersDashboardScreen(mainNavigationState = mainNavigationState)
            }

            MemorizeMode.Vocab -> MemorizeSubScreen(
                title = resolveString { memorize.vocabTitle },
                onBack = { mode = MemorizeMode.Menu }
            ) {
                VocabDashboardScreen(mainNavigationState = mainNavigationState)
            }
        }

    }

}

@Composable
private fun MemorizeSubScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("memorize_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = title)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            content()
        }

    }
}

@Composable
private fun MemorizeMenu(
    onLettersClick: () -> Unit,
    onVocabClick: () -> Unit
) {

    val letterSrsManager = koinInject<LetterSrsManager>()
    val vocabSrsManager = koinInject<VocabSrsManager>()

    val summary by produceState<MemorizeSummary?>(
        initialValue = null,
        letterSrsManager,
        vocabSrsManager
    ) {
        merge(letterSrsManager.dataChangeFlow, vocabSrsManager.dataChangeFlow)
            .onStart { emit(Unit) }
            .collect {
                value = withContext(Dispatchers.IO) {
                    val letters = letterSrsManager.getDecks()
                    val vocab = vocabSrsManager.getDecks()
                    MemorizeSummary(
                        letters = MemorizeCardSummary(
                            decks = letters.decks.size,
                            new = letters.dailyProgress.totalLeftover.new,
                            due = letters.dailyProgress.totalLeftover.due
                        ),
                        vocab = MemorizeCardSummary(
                            decks = vocab.decks.size,
                            new = vocab.dailyProgress.totalLeftover.new,
                            due = vocab.dailyProgress.totalLeftover.due
                        )
                    )
                }
            }
    }

    val isLandscape = LocalOrientation.current == Orientation.Landscape

    val cards: @Composable (Modifier) -> Unit = { cardModifier ->
        MemorizeCard(
            glyph = "字",
            title = resolveString { memorize.lettersTitle },
            subtitle = resolveString { memorize.lettersSubtitle },
            summary = summary?.letters,
            onClick = onLettersClick,
            testTagValue = "memorize_letters_button",
            modifier = cardModifier
        )
        MemorizeCard(
            glyph = "語",
            title = resolveString { memorize.vocabTitle },
            subtitle = resolveString { memorize.vocabSubtitle },
            summary = summary?.vocab,
            onClick = onVocabClick,
            testTagValue = "memorize_vocab_button",
            modifier = cardModifier
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentWidth()
            .widthIn(max = if (isLandscape) 720.dp else 480.dp)
            .padding(20.dp)
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                cards(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                cards(Modifier.weight(1f).fillMaxWidth())
            }
        }
    }

}

@Composable
private fun MemorizeCard(
    glyph: String,
    title: String,
    subtitle: String,
    summary: MemorizeCardSummary?,
    onClick: () -> Unit,
    testTagValue: String,
    modifier: Modifier = Modifier
) {

    val appColors = MaterialTheme.appColors

    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .testTag(testTagValue),
        shape = MaterialTheme.shapes.extraLarge,
        color = appColors.card,
        contentColor = appColors.textPrimary,
        border = appCardBorder(),
        tonalElevation = if (appColors.useCardBorder) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = glyph,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.accent
            )

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textMuted
            )

            Box(Modifier.weight(1f))

            if (summary != null) {
                val strings = resolveString { memorize }
                Text(
                    text = listOf(
                        strings.decksCountLabel(summary.decks),
                        strings.newCountLabel(summary.new),
                        strings.dueCountLabel(summary.due)
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelLarge,
                    color = appColors.textMuted
                )
            }

        }
    }

}
