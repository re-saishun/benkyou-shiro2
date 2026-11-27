package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.core.launchOnInvoke
import ua.syt0r.kanji.core.srs.GrammarPracticeType
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.VocabPracticeType
import ua.syt0r.kanji.core.streak.StreakStatus
import ua.syt0r.kanji.general_dashboard_header_streak_current
import ua.syt0r.kanji.general_dashboard_header_streak_longest
import ua.syt0r.kanji.general_dashboard_statistic_accordion
import ua.syt0r.kanji.general_dashboard_study_target_daily_limit
import ua.syt0r.kanji.general_dashboard_study_target_no_decks
import ua.syt0r.kanji.general_dashboard_study_target_nothing_left
import ua.syt0r.kanji.general_dashboard_study_target_title
import ua.syt0r.kanji.general_dashboard_study_target_today_badge
import ua.syt0r.kanji.general_dashboard_text_analysis
import ua.syt0r.kanji.practice_type_flashcard
import ua.syt0r.kanji.practice_type_reading_picker
import ua.syt0r.kanji.practice_type_writing
import ua.syt0r.kanji.presentation.common.AppDropdownMenu
import ua.syt0r.kanji.presentation.common.AppDropdownMenuItem
import ua.syt0r.kanji.presentation.common.AppListItem
import ua.syt0r.kanji.presentation.common.AppListItemDefaults
import ua.syt0r.kanji.presentation.common.ScreenGrammarPracticeType
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.copyCentered
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.theme.appCardBorder
import ua.syt0r.kanji.presentation.common.theme.appColors
import ua.syt0r.kanji.presentation.common.theme.snapSizeTransform
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.home.HomeScreenTab
import ua.syt0r.kanji.presentation.screen.main.screen.home.LocalHomeNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationCard
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration
import ua.syt0r.kanji.srs_status_due
import ua.syt0r.kanji.srs_status_new
import ua.syt0r.kanji.stats_today_new
import ua.syt0r.kanji.stats_today_review
import ua.syt0r.kanji.stats_today_time_spent
import kotlin.math.roundToInt
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsSectionUI

@Composable
fun GeneralDashboardScreenUI(
    state: State<ScreenState>,
    statsState: State<StatsScreenContract.ScreenState>,
    navigateToDailyLimitConfiguration: () -> Unit,
    navigateToCreateLetterDeck: () -> Unit,
    navigateToCreateVocabDeck: () -> Unit,
    navigateToLetterPractice: (MainDestination.LetterPractice) -> Unit,
    navigateToVocabPractice: (MainDestination.VocabPractice) -> Unit,
    navigateToGrammarPractice: (MainDestination.GrammarPractice) -> Unit,
    textAnalysisClick: () -> Unit
) {

    val homeNavigationState = LocalHomeNavigationState.current
    val openGrammarTab = { homeNavigationState?.navigate(HomeScreenTab.GrammarDashboard); Unit }

    ScreenLayout(state) { screenState, snackbarHostState ->

        val coroutineScope = rememberCoroutineScope()

        Header(screenState)

        ScreenDivider()

        val notifyNothingLeftToStudy = coroutineScope.launchOnInvoke {
            val message = getString(Res.string.general_dashboard_study_target_nothing_left)
            snackbarHostState.showSnackbar(message, withDismissAction = true)
        }

        StudyTargets(
            state = screenState,
            statsState = statsState,
            navigateToDailyLimitConfiguration = navigateToDailyLimitConfiguration,
            navigateToCreateLetterDeck = navigateToCreateLetterDeck,
            navigateToCreateVocabDeck = navigateToCreateVocabDeck,
            navigateToCreateGrammarDeck = openGrammarTab,
            navigateToLetterPractice = navigateToLetterPractice,
            navigateToVocabPractice = navigateToVocabPractice,
            navigateToGrammarPractice = navigateToGrammarPractice,
            notifyNothingLeftToStudy = notifyNothingLeftToStudy
        )

        ScreenDivider()

        StatsAccordionSection(statsState = statsState)

        ScreenDivider()

        AppListItem(
            onClick = textAnalysisClick,
            headlineContent = { Text(stringResource(Res.string.general_dashboard_text_analysis)) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
            modifier = Modifier.testTag("home_text_analysis_button")
        )

    }

}

@Composable
private fun StatsAccordionSection(
    statsState: State<StatsScreenContract.ScreenState>
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if (isExpanded) 180f else 0f)

    Column(modifier = Modifier.fillMaxWidth()) {
        AppListItem(
            onClick = { isExpanded = !isExpanded },
            headlineContent = {
                Text(
                    text = stringResource(Res.string.general_dashboard_statistic_accordion),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(Res.string.general_dashboard_statistic_accordion),
                    modifier = Modifier.rotate(arrowRotation)
                )
            },
            modifier = Modifier.testTag("home_stats_accordion")
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            StatsSectionUI(
                state = statsState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun ScreenDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        color = MaterialTheme.appColors.progressTrack
    )
}

private data class BookSpineStripItem(
    val id: String,
    val groupGlyph: String,
    val shortLabel: String,
    val fullTitle: String,
    val progress: StudyTargetProgress,
    val testTag: String,
    val onCreateDeck: () -> Unit,
    val onStartPractice: (List<PracticeConfigurationCard>) -> Unit
)

@Composable
private fun StudyTargets(
    state: ScreenState.Loaded,
    statsState: State<StatsScreenContract.ScreenState>,
    navigateToDailyLimitConfiguration: () -> Unit,
    navigateToCreateLetterDeck: () -> Unit,
    navigateToCreateVocabDeck: () -> Unit,
    navigateToCreateGrammarDeck: () -> Unit,
    navigateToLetterPractice: (MainDestination.LetterPractice) -> Unit,
    navigateToVocabPractice: (MainDestination.VocabPractice) -> Unit,
    navigateToGrammarPractice: (MainDestination.GrammarPractice) -> Unit,
    notifyNothingLeftToStudy: () -> Unit
) {
    val appColors = MaterialTheme.appColors

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = stringResource(Res.string.general_dashboard_study_target_title),
                style = MaterialTheme.typography.titleLarge.copyCentered(),
                color = appColors.textPrimary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.general_dashboard_study_target_today_badge),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = appColors.onAccent,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(appColors.accent)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            var showPopup by remember { mutableStateOf(false) }

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = { showPopup = true },
                modifier = Modifier.testTag("study_targets_menu_button")
            ) {

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(Res.string.general_dashboard_study_target_daily_limit)
                )

                AppDropdownMenu(
                    expanded = showPopup,
                    onDismissRequest = { showPopup = false }
                ) {
                    AppDropdownMenuItem(
                        onClick = {
                            navigateToDailyLimitConfiguration()
                            showPopup = false
                        }
                    ) {
                        Icon(Icons.Outlined.Settings, null)
                        Text(stringResource(Res.string.general_dashboard_study_target_daily_limit))
                    }
                }

            }

        }

        TodayStatsRow(
            newItems = state.stats.newItemsToday,
            reviews = state.stats.reviewsToday,
            statsState = statsState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        val grammarProgressByType = state.grammarTargets.associateBy { it.practiceType }
        val studyProgressByTarget = state.studyTargets.value.associateBy { it.studyTarget }

        fun launchGrammar(
            type: ScreenGrammarPracticeType,
            cards: List<PracticeConfigurationCard>
        ) {
            if (cards.isEmpty()) {
                notifyNothingLeftToStudy()
                return
            }
            val configuration = GrammarPracticeScreenConfiguration(
                cards = cards.filterIsInstance<GrammarPracticeScreenConfiguration.Card>(),
                practiceType = type
            )
            navigateToGrammarPractice(MainDestination.GrammarPractice(configuration))
        }

        fun launchLetter(
            type: LetterPracticeType,
            cards: List<PracticeConfigurationCard>
        ) {
            if (cards.isEmpty()) {
                notifyNothingLeftToStudy()
                return
            }
            val configuration = LetterPracticeScreenConfiguration(
                cards = cards.filterIsInstance<LetterPracticeScreenConfiguration.Card>(),
                practiceType = ScreenLetterPracticeType.from(type)
            )
            navigateToLetterPractice(MainDestination.LetterPractice(configuration))
        }

        fun launchVocab(
            type: VocabPracticeType,
            cards: List<PracticeConfigurationCard>
        ) {
            if (cards.isEmpty()) {
                notifyNothingLeftToStudy()
                return
            }
            val configuration = VocabPracticeScreenConfiguration(
                cards = cards.filterIsInstance<VocabPracticeScreenConfiguration.Card>(),
                practiceType = ScreenVocabPracticeType.from(type)
            )
            navigateToVocabPractice(MainDestination.VocabPractice(configuration))
        }

        val grammarGroup = listOf(
            BookSpineStripItem(
                id = "grammar_read",
                groupGlyph = "文",
                shortLabel = "Read",
                fullTitle = resolveString(ScreenGrammarPracticeType.Read.titleResolver),
                progress = grammarProgressByType[ScreenGrammarPracticeType.Read]?.progress
                    ?: StudyTargetProgress.NoDecks,
                testTag = "grammar_target_Read",
                onCreateDeck = navigateToCreateGrammarDeck,
                onStartPractice = { launchGrammar(ScreenGrammarPracticeType.Read, it) }
            ),
            BookSpineStripItem(
                id = "grammar_quiz",
                groupGlyph = "文",
                shortLabel = "Quiz",
                fullTitle = resolveString(ScreenGrammarPracticeType.Quiz.titleResolver),
                progress = grammarProgressByType[ScreenGrammarPracticeType.Quiz]?.progress
                    ?: StudyTargetProgress.NoDecks,
                testTag = "grammar_target_Quiz",
                onCreateDeck = navigateToCreateGrammarDeck,
                onStartPractice = { launchGrammar(ScreenGrammarPracticeType.Quiz, it) }
            ),
            BookSpineStripItem(
                id = "grammar_test",
                groupGlyph = "文",
                shortLabel = "Test",
                fullTitle = resolveString(ScreenGrammarPracticeType.Test.titleResolver),
                progress = grammarProgressByType[ScreenGrammarPracticeType.Test]?.progress
                    ?: StudyTargetProgress.NoDecks,
                testTag = "grammar_target_Test",
                onCreateDeck = navigateToCreateGrammarDeck,
                onStartPractice = { launchGrammar(ScreenGrammarPracticeType.Test, it) }
            )
        )

        val letterGroup = listOf(
            BookSpineStripItem(
                id = "letter_writing",
                groupGlyph = "字",
                shortLabel = "Write",
                fullTitle = stringResource(Res.string.practice_type_writing),
                progress = studyProgressByTarget[StudyTarget.LetterWriting]?.progress
                    ?: StudyTargetProgress.NoDecks,
                testTag = "study_target_LetterWriting",
                onCreateDeck = navigateToCreateLetterDeck,
                onStartPractice = { launchLetter(LetterPracticeType.Writing, it) }
            ),
            BookSpineStripItem(
                id = "letter_flashcards",
                groupGlyph = "字",
                shortLabel = "Cards",
                fullTitle = stringResource(Res.string.practice_type_flashcard),
                progress = studyProgressByTarget[StudyTarget.LetterFlashcards]?.progress
                    ?: StudyTargetProgress.NoDecks,
                testTag = "study_target_LetterFlashcards",
                onCreateDeck = navigateToCreateLetterDeck,
                onStartPractice = { launchLetter(LetterPracticeType.Reading, it) }
            ),
            BookSpineStripItem(
                id = "letter_reading_picker",
                groupGlyph = "字",
                shortLabel = "Pick",
                fullTitle = stringResource(Res.string.practice_type_reading_picker),
                progress = studyProgressByTarget[StudyTarget.LetterReadingPicker]?.progress
                    ?: StudyTargetProgress.NoDecks,
                testTag = "study_target_LetterReadingPicker",
                onCreateDeck = navigateToCreateLetterDeck,
                onStartPractice = { launchLetter(LetterPracticeType.ReadingPicker, it) }
            )
        )

        val vocabGroup = listOf(
            BookSpineStripItem(
                id = "vocab_flashcard",
                groupGlyph = "語",
                shortLabel = "Cards",
                fullTitle = stringResource(Res.string.practice_type_flashcard),
                progress = studyProgressByTarget[StudyTarget.VocabFlashcard]?.progress
                    ?: StudyTargetProgress.NoDecks,
                testTag = "study_target_VocabFlashcard",
                onCreateDeck = navigateToCreateVocabDeck,
                onStartPractice = { launchVocab(VocabPracticeType.Flashcard, it) }
            ),
            BookSpineStripItem(
                id = "vocab_writing",
                groupGlyph = "語",
                shortLabel = "Write",
                fullTitle = stringResource(Res.string.practice_type_writing),
                progress = studyProgressByTarget[StudyTarget.VocabWriting]?.progress
                    ?: StudyTargetProgress.NoDecks,
                testTag = "study_target_VocabWriting",
                onCreateDeck = navigateToCreateVocabDeck,
                onStartPractice = { launchVocab(VocabPracticeType.Writing, it) }
            ),
            BookSpineStripItem(
                id = "vocab_reading_picker",
                groupGlyph = "語",
                shortLabel = "Pick",
                fullTitle = stringResource(Res.string.practice_type_reading_picker),
                progress = studyProgressByTarget[StudyTarget.VocabReadingPicker]?.progress
                    ?: StudyTargetProgress.NoDecks,
                testTag = "study_target_VocabReadingPicker",
                onCreateDeck = navigateToCreateVocabDeck,
                onStartPractice = { launchVocab(VocabPracticeType.ReadingPicker, it) }
            )
        )

        val groups = listOf(grammarGroup, letterGroup, vocabGroup)
        var expandedStripId by rememberSaveable { mutableStateOf<String?>(null) }
        val anyExpanded = expandedStripId != null

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(160.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            groups.forEachIndexed { groupIndex, groupItems ->
                if (groupIndex > 0) {
                    Spacer(Modifier.width(8.dp))
                }
                groupItems.forEachIndexed { itemIndex, item ->
                    if (itemIndex > 0) {
                        Spacer(Modifier.width(3.dp))
                    }
                    val isExpanded = expandedStripId == item.id
                    val animatedWeight by animateFloatAsState(
                        targetValue = if (isExpanded) 3.2f else 1f,
                        animationSpec = tween(durationMillis = 250)
                    )
                    BookSpineStripCard(
                        item = item,
                        isExpanded = isExpanded,
                        anyExpanded = anyExpanded,
                        onExpand = { expandedStripId = item.id },
                        onCollapse = { expandedStripId = null },
                        modifier = Modifier
                            .weight(animatedWeight)
                            .fillMaxHeight()
                    )
                }
            }
        }

    }

}

@Composable
private fun BookSpineStripCard(
    item: BookSpineStripItem,
    isExpanded: Boolean,
    anyExpanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = MaterialTheme.appColors
    val shape = RoundedCornerShape(12.dp)

    val launchAction: () -> Unit = {
        when (val progress = item.progress) {
            StudyTargetProgress.NoDecks -> item.onCreateDeck()
            is StudyTargetProgress.WithDecks -> item.onStartPractice(progress.options.combinedCards)
        }
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .then(
                if (!isExpanded) Modifier.clickable(onClick = onExpand)
                else Modifier
            )
            .testTag(item.testTag),
        shape = shape,
        color = appColors.card,
        contentColor = appColors.textPrimary,
        border = appCardBorder(),
        tonalElevation = if (appColors.useCardBorder) 0.dp else 1.dp
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(tween(durationMillis = 180)) togetherWith
                    fadeOut(tween(durationMillis = 120))
            }
        ) { expanded ->
            if (!expanded) {
                CollapsedStripContent(
                    item = item,
                    isShrunk = anyExpanded
                )
            } else {
                ExpandedStripContent(
                    item = item,
                    onCollapse = onCollapse,
                    onActionClick = launchAction
                )
            }
        }
    }
}

@Composable
private fun CollapsedStripContent(
    item: BookSpineStripItem,
    isShrunk: Boolean
) {
    val appColors = MaterialTheme.appColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = item.groupGlyph,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.accent,
            maxLines = 1,
            softWrap = false
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = item.shortLabel,
            fontSize = if (isShrunk) 9.sp else 10.sp,
            lineHeight = 12.sp,
            color = appColors.textMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )

        Spacer(Modifier.weight(1f))

        when (val progress = item.progress) {
            StudyTargetProgress.NoDecks -> {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.general_dashboard_study_target_no_decks),
                    tint = appColors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }

            is StudyTargetProgress.WithDecks -> {
                val fraction = progress.totalProgress.coerceIn(0f, 1f)
                val percent = (fraction * 100f).roundToInt()

                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(appColors.progressTrack)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction)
                            .align(Alignment.BottomCenter)
                            .background(appColors.accent)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "$percent%",
                    fontSize = if (isShrunk) 9.sp else 10.sp,
                    lineHeight = 12.sp,
                    color = appColors.textMuted,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun ExpandedStripContent(
    item: BookSpineStripItem,
    onCollapse: () -> Unit,
    onActionClick: () -> Unit
) {
    val appColors = MaterialTheme.appColors

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCollapse)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .testTag("${item.testTag}_collapse"),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.groupGlyph,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.accent,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = item.fullTitle,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = appColors.progressTrack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(onClick = onActionClick)
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("${item.testTag}_action"),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when (val progress = item.progress) {
                StudyTargetProgress.NoDecks -> {
                    Text(
                        text = stringResource(Res.string.general_dashboard_study_target_no_decks),
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        color = appColors.textMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.general_dashboard_study_target_no_decks),
                            tint = appColors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                is StudyTargetProgress.WithDecks -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = null,
                                tint = appColors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = progress.decksCount.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = appColors.textPrimary,
                                maxLines = 1
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = appColors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            val dueCount = progress.options.dueCards.size
                            if (dueCount == 0) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = appColors.accent,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = dueCount.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = appColors.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    val fraction = progress.totalProgress.coerceIn(0f, 1f)
                    val percent = (fraction * 100f).roundToInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(appColors.progressTrack)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction)
                                    .align(Alignment.CenterStart)
                                    .background(appColors.accent)
                            )
                        }
                        Text(
                            text = "$percent%",
                            fontSize = 10.sp,
                            color = appColors.textMuted,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayStatsRow(
    newItems: Int,
    reviews: Int,
    statsState: State<StatsScreenContract.ScreenState>,
    modifier: Modifier = Modifier
) {
    val timeSpentText = when (val screenState = statsState.value) {
        StatsScreenContract.ScreenState.Loading -> "-"
        is StatsScreenContract.ScreenState.Loaded -> {
            resolveString { stats }.formattedDuration(screenState.stats.todayStats.timeSpent)
        }
    }
    Row(
        modifier = modifier.height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TodayStatItem(
            title = stringResource(Res.string.stats_today_new),
            value = newItems.toString(),
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
        TodayStatItem(
            title = stringResource(Res.string.stats_today_time_spent),
            value = timeSpentText,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
        TodayStatItem(
            title = stringResource(Res.string.stats_today_review),
            value = reviews.toString(),
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

@Composable
private fun TodayStatItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val appColors = MaterialTheme.appColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = appColors.card,
        contentColor = appColors.textPrimary,
        border = appCardBorder(),
        tonalElevation = if (appColors.useCardBorder) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = appColors.textMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyTargetItem(
    studyTargetState: StudyTargetState,
    createDeck: () -> Unit,
    startPractice: (List<PracticeConfigurationCard>) -> Unit
) {

    val appColors = MaterialTheme.appColors
    val studyTarget = studyTargetState.studyTarget
    val studyProgress = studyTargetState.progress

    AppListItem(
        onClick = {
            when (studyProgress) {
                StudyTargetProgress.NoDecks -> createDeck()
                is StudyTargetProgress.WithDecks -> {
                    startPractice(studyProgress.options.combinedCards)
                }
            }
        },
        headlineContent = {
            Text(
                stringResource(studyTarget.categoryTitle) + "・" + stringResource(studyTarget.typeTitleRes)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = null
            )
        },
        supportingContent = {
            if (studyProgress is StudyTargetProgress.NoDecks) {
                Text(stringResource(Res.string.general_dashboard_study_target_no_decks))
                return@AppListItem
            }

            studyProgress as StudyTargetProgress.WithDecks

            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingTiny)
            ) {

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(-Dimens.SpacingMid)
                ) {

                    ClickableStudyRow(
                        imageVector = Icons.Outlined.School,
                        title = stringResource(Res.string.srs_status_new),
                        count = studyProgress.options.newCards.size,
                        onClick = { startPractice(studyProgress.options.newCards) }
                    )

                    ClickableStudyRow(
                        imageVector = Icons.Outlined.Schedule,
                        title = stringResource(Res.string.srs_status_due),
                        count = studyProgress.options.dueCards.size,
                        onClick = { startPractice(studyProgress.options.dueCards) }
                    )

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMid)
                ) {

                    LinearProgressIndicator(
                        progress = studyProgress.totalProgress,
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                        color = appColors.accent,
                        backgroundColor = appColors.progressTrack,
                        strokeCap = StrokeCap.Round
                    )

                    Text(
                        text = studyProgress.totalProgress.times(100).roundToInt().toString() + "%",
                        style = LocalTextStyle.current.copyCentered()
                    )

                }
            }
        }
    )
}

@Composable
private fun ClickableStudyRow(
    imageVector: ImageVector,
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    val appColors = MaterialTheme.appColors
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpacingMid, vertical = Dimens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)
    ) {
        val iconSize = 18.dp
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )

        val textStyle = LocalTextStyle.current.copyCentered()

        Text(
            text = title,
            style = textStyle
        )

        if (count == 0) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = appColors.accent,
                modifier = Modifier.size(iconSize)
            )
        } else {
            Text(
                text = count.toString(),
                style = textStyle
            )
        }
    }
}

@Composable
private fun Header(state: ScreenState.Loaded) {
    val appColors = MaterialTheme.appColors
    val stats = state.stats
    val streakStrings = resolveString { dailyStreak }

    val statusLabel = when (stats.streakStatus) {
        StreakStatus.COMPLETED_TODAY -> streakStrings.statusCompleted
        StreakStatus.AT_RISK -> streakStrings.statusAtRisk
        StreakStatus.EXPIRED -> streakStrings.statusExpired
        StreakStatus.NOT_STARTED -> streakStrings.statusStart
    }

    val badgeIcon = when {
        stats.isMilestone -> Icons.Filled.EmojiEvents
        stats.streakStatus == StreakStatus.AT_RISK -> Icons.Filled.Bolt
        stats.currentStreak > 0 -> Icons.Filled.LocalFireDepartment
        else -> Icons.Outlined.Timeline
    }

    val badgeTint = when (stats.streakStatus) {
        StreakStatus.COMPLETED_TODAY -> appColors.accent
        StreakStatus.AT_RISK -> appColors.accent
        StreakStatus.EXPIRED -> appColors.textMuted
        StreakStatus.NOT_STARTED -> appColors.textMuted
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = appColors.card,
        border = appCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("daily_streak_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = appColors.progressTrack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = statusLabel,
                            tint = badgeTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stats.motivationTitle ?: streakStrings.statusStart,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    stats.motivationMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.textMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (stats.streakStatus == StreakStatus.COMPLETED_TODAY) {
                        appColors.accent.copy(alpha = 0.14f)
                    } else {
                        appColors.progressTrack
                    }
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (stats.streakStatus == StreakStatus.COMPLETED_TODAY) {
                            appColors.accent
                        } else {
                            appColors.textMuted
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = appColors.progressTrack)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                fun Int.numberOrDash(): String = if (this == 0) "-" else toString()
                HeaderStatItem(
                    title = streakStrings.currentLabel,
                    text = stats.currentStreak.numberOrDash(),
                    highlight = stats.currentStreak > 0,
                    modifier = Modifier.weight(1f)
                )
                HeaderStatItem(
                    title = streakStrings.bestLabel,
                    text = stats.longestStreak.numberOrDash(),
                    highlight = false,
                    modifier = Modifier.weight(1f)
                )
                HeaderStatItem(
                    title = streakStrings.totalDaysLabel,
                    text = stats.totalPracticeDays.numberOrDash(),
                    highlight = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeaderStatItem(
    title: String,
    text: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val appColors = MaterialTheme.appColors
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = appColors.textMuted,
            textAlign = TextAlign.Center
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (highlight) appColors.accent else appColors.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun ScreenLayout(
    state: State<ScreenState>,
    content: @Composable ColumnScope.(ScreenState.Loaded, SnackbarHostState) -> Unit
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val appColors = MaterialTheme.appColors

    Box {
        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() using snapSizeTransform() }
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> {
                    FancyLoading(Modifier.fillMaxSize().wrapContentSize())
                }

                is ScreenState.Loaded -> Column(
                    modifier = Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .wrapContentWidth()
                        .widthIn(max = 480.dp)
                ) {

                    if (LocalOrientation.current == Orientation.Landscape) {
                        Spacer(Modifier.height(20.dp))
                    }

                    content(screenState, snackbarHostState)

                    Spacer(Modifier.height(20.dp))

                }

            }

        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            snackbar = {
                Snackbar(
                    snackbarData = it,
                    containerColor = appColors.dialogSurface,
                    contentColor = appColors.textPrimary,
                    actionContentColor = appColors.accent,
                    dismissActionContentColor = appColors.textPrimary
                )
            }
        )

    }

}
