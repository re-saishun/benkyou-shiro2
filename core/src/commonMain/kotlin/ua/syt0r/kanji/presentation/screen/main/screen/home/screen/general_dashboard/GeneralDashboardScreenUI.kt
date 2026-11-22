package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableColumn
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.core.launchOnInvoke
import ua.syt0r.kanji.core.srs.GrammarPracticeType
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.VocabPracticeType
import ua.syt0r.kanji.dialog_apply
import ua.syt0r.kanji.dialog_cancel
import ua.syt0r.kanji.general_dashboard_header_streak_current
import ua.syt0r.kanji.general_dashboard_header_streak_longest
import ua.syt0r.kanji.general_dashboard_study_target_daily_limit
import ua.syt0r.kanji.general_dashboard_study_target_edit
import ua.syt0r.kanji.general_dashboard_study_target_empty
import ua.syt0r.kanji.general_dashboard_study_target_no_decks
import ua.syt0r.kanji.general_dashboard_study_target_nothing_left
import ua.syt0r.kanji.general_dashboard_statistic_accordion
import ua.syt0r.kanji.general_dashboard_study_target_title
import ua.syt0r.kanji.general_dashboard_study_target_today_badge
import ua.syt0r.kanji.stats_today_new
import ua.syt0r.kanji.stats_today_review
import ua.syt0r.kanji.stats_today_time_spent
import ua.syt0r.kanji.study_category_grammar
import ua.syt0r.kanji.general_dashboard_text_analysis
import ua.syt0r.kanji.presentation.common.AppDropdownMenu
import ua.syt0r.kanji.presentation.common.AppDropdownMenuItem
import ua.syt0r.kanji.presentation.common.AppListItem
import ua.syt0r.kanji.presentation.common.AppListItemDefaults
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.ScreenGrammarPracticeType
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.copyCentered
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.theme.snapSizeTransform
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsSectionUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationCard
import ua.syt0r.kanji.presentation.screen.main.screen.home.HomeScreenTab
import ua.syt0r.kanji.presentation.screen.main.screen.home.LocalHomeNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration
import ua.syt0r.kanji.srs_status_due
import ua.syt0r.kanji.srs_status_new
import kotlin.math.roundToInt

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
        var showStudyTargetsEditDialog by rememberSaveable { mutableStateOf(false) }
        if (showStudyTargetsEditDialog) {
            StudyTargetsEditDialog(
                onDismissRequest = { showStudyTargetsEditDialog = false },
                state = screenState
            )
        }


        Header(screenState)

        ScreenDivider()

        val notifyNothingLeftToStudy = coroutineScope.launchOnInvoke {
            val message = getString(Res.string.general_dashboard_study_target_nothing_left)
            snackbarHostState.showSnackbar(message, withDismissAction = true)
        }

        StudyTargets(
            state = screenState,
            statsState = statsState,
            showEditDialog = { showStudyTargetsEditDialog = true },
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
    HorizontalDivider(Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
}

@Composable
private fun StudyTargetsEditDialog(
    onDismissRequest: () -> Unit,
    state: ScreenState.Loaded
) {

    var states by remember {
        mutableStateOf(state.studyTargets.value)
    }

    val toggleEnabledAtIndex = { index: Int ->
        states = states.toMutableList().apply {
            val itemState = get(index).run { copy(enabled = !enabled) }
            set(index, itemState)
        }
    }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.general_dashboard_study_target_title)) },
        paddedContent = false,
        content = {
            ReorderableColumn(
                list = states.toList(),
                onSettle = { fromIndex, toIndex ->
                    states = states.toList()
                        .toMutableList()
                        .apply { add(toIndex, removeAt(fromIndex)) }
                }
            ) { index, item, _ ->
                val studyTarget = item.studyTarget
                key(studyTarget) {
                    AppListItem(
                        onClick = { toggleEnabledAtIndex(index) },
                        leadingContent = {
                            Icon(Icons.Outlined.DragIndicator, null, Modifier.draggableHandle())
                        },
                        overlineContent = { Text(stringResource(studyTarget.categoryTitle)) },
                        headlineContent = { Text(stringResource(studyTarget.typeTitleRes)) },
                        trailingContent = {
                            Switch(
                                checked = item.enabled,
                                onCheckedChange = { toggleEnabledAtIndex(index) }
                            )
                        }
                    )
                }
            }
        },
        buttons = {
            TextButton(onDismissRequest) {
                Text(stringResource(Res.string.dialog_cancel))
            }
            TextButton(
                onClick = {
                    state.studyTargets.value = states
                    onDismissRequest()
                }
            ) {
                Text(stringResource(Res.string.dialog_apply))
            }
        }
    )
}

@Composable
private fun StudyTargets(
    state: ScreenState.Loaded,
    statsState: State<StatsScreenContract.ScreenState>,
    showEditDialog: () -> Unit,
    navigateToDailyLimitConfiguration: () -> Unit,
    navigateToCreateLetterDeck: () -> Unit,
    navigateToCreateVocabDeck: () -> Unit,
    navigateToCreateGrammarDeck: () -> Unit,
    navigateToLetterPractice: (MainDestination.LetterPractice) -> Unit,
    navigateToVocabPractice: (MainDestination.VocabPractice) -> Unit,
    navigateToGrammarPractice: (MainDestination.GrammarPractice) -> Unit,
    notifyNothingLeftToStudy: () -> Unit
) {

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
                style = MaterialTheme.typography.titleLarge.copyCentered()
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.general_dashboard_study_target_today_badge),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MaterialTheme.colorScheme.primary)
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
                    contentDescription = stringResource(Res.string.general_dashboard_study_target_edit)
                )

                AppDropdownMenu(
                    expanded = showPopup,
                    onDismissRequest = { showPopup = false }
                ) {
                    AppDropdownMenuItem(
                        onClick = {
                            showEditDialog()
                            showPopup = false
                        }
                    ) {
                        Icon(Icons.Outlined.Edit, null)
                        Text(stringResource(Res.string.general_dashboard_study_target_edit))
                    }
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

        GrammarTargetsRow(
            targets = state.grammarTargets,
            createDeck = navigateToCreateGrammarDeck,
            startPractice = { target, cards ->
                if (cards.isEmpty()) {
                    notifyNothingLeftToStudy()
                    return@GrammarTargetsRow
                }
                val configuration = GrammarPracticeScreenConfiguration(
                    cards = cards.filterIsInstance<GrammarPracticeScreenConfiguration.Card>(),
                    practiceType = target.practiceType
                )
                navigateToGrammarPractice(MainDestination.GrammarPractice(configuration))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        val displayList = state.studyTargets.value.filter { it.enabled }

        if (displayList.isEmpty()) {
            AppListItem(
                onClick = showEditDialog,
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.general_dashboard_study_target_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                displayList.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { targetState ->
                            StudyTargetGridCard(
                                studyTargetState = targetState,
                                createDeck = {
                                    when (targetState.studyTarget.practiceType) {
                                        is LetterPracticeType -> navigateToCreateLetterDeck()
                                        is VocabPracticeType -> navigateToCreateVocabDeck()
                                        is GrammarPracticeType -> navigateToCreateGrammarDeck()
                                    }
                                },
                                startPractice = { cards ->
                                    if (cards.isEmpty()) {
                                        notifyNothingLeftToStudy()
                                        return@StudyTargetGridCard
                                    }
                                    when (val practiceType = targetState.studyTarget.practiceType) {
                                        is LetterPracticeType -> {
                                            val configuration = LetterPracticeScreenConfiguration(
                                                cards = cards as List<LetterPracticeScreenConfiguration.Card>,
                                                practiceType = ScreenLetterPracticeType.from(practiceType)
                                            )
                                            val destination = MainDestination.LetterPractice(configuration)
                                            navigateToLetterPractice(destination)
                                        }

                                        is VocabPracticeType -> {
                                            val configuration = VocabPracticeScreenConfiguration(
                                                cards = cards as List<VocabPracticeScreenConfiguration.Card>,
                                                practiceType = ScreenVocabPracticeType.from(practiceType)
                                            )
                                            val destination = MainDestination.VocabPractice(configuration)
                                            navigateToVocabPractice(destination)
                                        }

                                        is GrammarPracticeType -> {
                                            val configuration = GrammarPracticeScreenConfiguration(
                                                cards = cards as List<GrammarPracticeScreenConfiguration.Card>,
                                                practiceType = ScreenGrammarPracticeType.from(practiceType)
                                            )
                                            val destination = MainDestination.GrammarPractice(configuration)
                                            navigateToGrammarPractice(destination)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

    }

}

@Composable
private fun StudyTargetGridCard(
    studyTargetState: StudyTargetState,
    createDeck: () -> Unit,
    startPractice: (List<PracticeConfigurationCard>) -> Unit,
    modifier: Modifier = Modifier
) {
    val studyTarget = studyTargetState.studyTarget
    TargetGridCard(
        category = stringResource(studyTarget.categoryTitle),
        title = stringResource(studyTarget.typeTitleRes),
        studyProgress = studyTargetState.progress,
        testTagValue = "study_target_${studyTarget.name}",
        createDeck = createDeck,
        startPractice = startPractice,
        modifier = modifier
    )
}

@Composable
private fun GrammarTargetsRow(
    targets: List<GrammarStudyTargetState>,
    createDeck: () -> Unit,
    startPractice: (GrammarStudyTargetState, List<PracticeConfigurationCard>) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        targets.forEach { target ->
            TargetGridCard(
                category = stringResource(Res.string.study_category_grammar),
                title = resolveString(target.practiceType.titleResolver),
                studyProgress = target.progress,
                testTagValue = "grammar_target_${target.practiceType.name}",
                createDeck = createDeck,
                startPractice = { cards -> startPractice(target, cards) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
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
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TargetGridCard(
    category: String,
    title: String,
    studyProgress: StudyTargetProgress,
    testTagValue: String,
    createDeck: () -> Unit,
    startPractice: (List<PracticeConfigurationCard>) -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier
            .heightIn(min = 120.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                when (studyProgress) {
                    StudyTargetProgress.NoDecks -> createDeck()
                    is StudyTargetProgress.WithDecks -> startPractice(studyProgress.options.combinedCards)
                }
            }
            .testTag(testTagValue),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            when (studyProgress) {
                StudyTargetProgress.NoDecks -> {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = stringResource(Res.string.general_dashboard_study_target_no_decks),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                is StudyTargetProgress.WithDecks -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactTargetCountBadge(
                            icon = Icons.Outlined.School,
                            count = studyProgress.options.newCards.size,
                            onClick = { startPractice(studyProgress.options.newCards) }
                        )
                        CompactTargetCountBadge(
                            icon = Icons.Outlined.Schedule,
                            count = studyProgress.options.dueCards.size,
                            onClick = { startPractice(studyProgress.options.dueCards) }
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = studyProgress.totalProgress,
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp),
                            color = MaterialTheme.colorScheme.primary,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${studyProgress.totalProgress.times(100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall.copyCentered()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTargetCountBadge(
    icon: ImageVector,
    count: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        if (count == 0) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        } else {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium.copyCentered()
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
                        color = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
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
                tint = MaterialTheme.colorScheme.primary,
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(AppListItemDefaults.ExtraPaddings),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        fun Int.numberOrDash(): String = if (this == 0) "-" else toString()
        HeaderStatItem(
            title = stringResource(Res.string.general_dashboard_header_streak_current),
            text = state.stats.currentStreak.numberOrDash(),
            modifier = Modifier.weight(1f)
        )
        HeaderStatItem(
            title = stringResource(Res.string.general_dashboard_header_streak_longest),
            text = state.stats.longestStreak.numberOrDash(),
            modifier = Modifier.weight(1f)
        )
    }

}

@Composable
private fun HeaderStatItem(title: String, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
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
                    containerColor = MaterialTheme.colorScheme.surfaceDim,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionContentColor = MaterialTheme.colorScheme.primary,
                    dismissActionContentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        )

    }

}
