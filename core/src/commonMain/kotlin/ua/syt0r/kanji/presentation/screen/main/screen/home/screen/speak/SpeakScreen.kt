package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.speak

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.speak.SpeakCallConnectionState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.appCardBorder
import ua.syt0r.kanji.presentation.common.theme.appColors
import ua.syt0r.kanji.presentation.getMultiplatformViewModel

@Composable
fun SpeakScreen(
    viewModel: SpeakScreenContract.ViewModel = getMultiplatformViewModel()
) {
    val audioPermissionState = rememberAudioPermissionState(
        onPermissionGranted = { viewModel.startCall() }
    )

    SpeakScreenUI(
        state = viewModel.state.collectAsState(),
        hasAudioPermission = audioPermissionState.isGranted,
        onRequestAudioPermission = audioPermissionState.requestPermission,
        onSelectTopic = viewModel::selectTopic,
        onSelectLevel = viewModel::selectLevel,
        onCustomPromptChange = viewModel::updateCustomPrompt,
        onStartCall = viewModel::startCall,
        onStopCall = viewModel::stopCall,
        onToggleMute = viewModel::toggleMute
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeakScreenUI(
    state: State<SpeakUiState>,
    hasAudioPermission: Boolean,
    onRequestAudioPermission: () -> Unit,
    onSelectTopic: (SpeakTopic) -> Unit,
    onSelectLevel: (SpeakLevel) -> Unit,
    onCustomPromptChange: (String) -> Unit,
    onStartCall: () -> Unit,
    onStopCall: () -> Unit,
    onToggleMute: () -> Unit
) {
    val uiState = state.value
    val appColors = MaterialTheme.appColors
    val speakStrings = resolveString { speak }
    val cardBorder = appCardBorder()

    val isCallActive = uiState.connectionState == SpeakCallConnectionState.Live ||
            uiState.connectionState == SpeakCallConnectionState.Connecting

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .wrapContentWidth()
            .widthIn(max = 520.dp)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = speakStrings.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )
            Text(
                text = speakStrings.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textMuted
            )
        }

        // Mic permission prompt card if not granted
        if (uiState.isSupported && !hasAudioPermission) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = appColors.card,
                border = cardBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = speakStrings.micPermissionNeededTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.textPrimary
                    )
                    Text(
                        text = speakStrings.micPermissionNeededMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textMuted
                    )
                    OutlinedButton(
                        onClick = onRequestAudioPermission,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("speak_grant_mic_button")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(speakStrings.grantMicPermissionButton)
                    }
                }
            }
        }

        // Live Voice Call Hero Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.card,
            border = cardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "voice_orb")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (uiState.connectionState == SpeakCallConnectionState.Live) 1.18f else 1.06f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 900),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                val orbContainerColor by animateColorAsState(
                    targetValue = when (uiState.connectionState) {
                        SpeakCallConnectionState.Live -> appColors.accent
                        SpeakCallConnectionState.Connecting -> appColors.progressTrack
                        SpeakCallConnectionState.Error -> MaterialTheme.colorScheme.errorContainer
                        SpeakCallConnectionState.Idle -> appColors.progressTrack
                    },
                    label = "orb_color"
                )

                val orbContentColor = when (uiState.connectionState) {
                    SpeakCallConnectionState.Live -> appColors.onAccent
                    SpeakCallConnectionState.Error -> MaterialTheme.colorScheme.onErrorContainer
                    else -> appColors.accent
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(116.dp)
                ) {
                    if (isCallActive) {
                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(appColors.accent.copy(alpha = 0.16f))
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = orbContainerColor,
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            when (uiState.connectionState) {
                                SpeakCallConnectionState.Connecting -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = appColors.accent,
                                        strokeWidth = 3.dp
                                    )
                                }

                                SpeakCallConnectionState.Live -> {
                                    Icon(
                                        imageVector = if (uiState.isMuted) Icons.Default.MicOff else Icons.Default.GraphicEq,
                                        contentDescription = speakStrings.liveStatusLabel,
                                        tint = orbContentColor,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                else -> {
                                    Icon(
                                        imageVector = Icons.Outlined.RecordVoiceOver,
                                        contentDescription = speakStrings.title,
                                        tint = orbContentColor,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Status & Timer
                when (uiState.connectionState) {
                    SpeakCallConnectionState.Connecting -> {
                        Text(
                            text = speakStrings.connectingLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.textPrimary
                        )
                    }

                    SpeakCallConnectionState.Live -> {
                        Text(
                            text = "${speakStrings.liveStatusLabel} • ${formatElapsedSeconds(uiState.elapsedSeconds)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.accent
                        )
                    }

                    SpeakCallConnectionState.Error -> {
                        Text(
                            text = uiState.errorMessage ?: "Call error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    SpeakCallConnectionState.Idle -> {
                        Text(
                            text = speakStrings.liveCallTips,
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (!uiState.isSupported) {
                    Text(
                        text = speakStrings.unsupportedMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textMuted,
                        textAlign = TextAlign.Center
                    )
                } else if (!isCallActive) {
                    Button(
                        onClick = {
                            if (hasAudioPermission) onStartCall() else onRequestAudioPermission()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.accent,
                            contentColor = appColors.onAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("speak_start_call_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = speakStrings.startCallButton,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onToggleMute,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp)
                                .testTag("speak_mute_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isMuted) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = null
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(if (uiState.isMuted) speakStrings.unmuteMicButton else speakStrings.muteMicButton)
                        }

                        Button(
                            onClick = onStopCall,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp)
                                .testTag("speak_end_call_button")
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text(speakStrings.endCallButton)
                        }
                    }
                }
            }
        }

        // Topic Selection Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = appColors.card,
            border = cardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = speakStrings.topicSectionTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpeakTopic.entries.forEach { topic ->
                        val selected = uiState.selectedTopic == topic
                        val label = when (topic) {
                            SpeakTopic.FreeTalk -> speakStrings.topicFreeTalk
                            SpeakTopic.CafeOrder -> speakStrings.topicCafeOrder
                            SpeakTopic.ConvenienceStore -> speakStrings.topicConvenienceStore
                            SpeakTopic.StationTravel -> speakStrings.topicStationTravel
                            SpeakTopic.SelfIntro -> speakStrings.topicSelfIntro
                            SpeakTopic.Custom -> speakStrings.topicCustom
                        }
                        SelectablePill(
                            label = label,
                            selected = selected,
                            enabled = !isCallActive,
                            onClick = { onSelectTopic(topic) }
                        )
                    }
                }

                if (uiState.selectedTopic == SpeakTopic.Custom) {
                    OutlinedTextField(
                        value = uiState.customPrompt,
                        onValueChange = onCustomPromptChange,
                        enabled = !isCallActive,
                        label = { Text(speakStrings.customPromptLabel) },
                        placeholder = { Text(speakStrings.customPromptPlaceholder) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )
                }

                Text(
                    text = speakStrings.levelSectionTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SpeakLevel.entries.forEach { level ->
                        SelectablePill(
                            label = level.label,
                            selected = uiState.selectedLevel == level,
                            enabled = !isCallActive,
                            onClick = { onSelectLevel(level) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Starter Phrases for quick confidence boost
        val starterPhrases = starterPhrasesFor(uiState.selectedTopic)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = appColors.card,
            border = cardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = speakStrings.starterPhrasesTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                )
                starterPhrases.forEach { phrase ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = phrase.japanese,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = appColors.textPrimary
                        )
                        Text(
                            text = "${phrase.reading} — ${phrase.translation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SelectablePill(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = MaterialTheme.appColors
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) appColors.accent else appColors.progressTrack,
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) appColors.onAccent else appColors.textPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatElapsedSeconds(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    val minStr = mins.toString().padStart(2, '0')
    val secStr = secs.toString().padStart(2, '0')
    return "$minStr:$secStr"
}

private fun starterPhrasesFor(topic: SpeakTopic): List<SpeakStarterPhrase> = when (topic) {
    SpeakTopic.FreeTalk -> listOf(
        SpeakStarterPhrase("こんにちは！今日はどうですか？", "Konnichiwa! Kyō wa dō desu ka?", "Hello! How is today going?"),
        SpeakStarterPhrase("日本語を少し勉強しています。", "Nihongo o sukoshi benkyō shite imasu.", "I am studying a little Japanese.")
    )
    SpeakTopic.CafeOrder -> listOf(
        SpeakStarterPhrase("アイスラテを一つお願いします。", "Aisu rate o hitotsu onegaishimasu.", "One iced latte, please."),
        SpeakStarterPhrase("店内で飲みます。", "Tennai de nomimasu.", "I'll drink it here.")
    )
    SpeakTopic.ConvenienceStore -> listOf(
        SpeakStarterPhrase("すみません、お弁当を温めてください。", "Sumimasen, obentō o atatamete kudasai.", "Excuse me, please warm up this bento."),
        SpeakStarterPhrase("袋はいりません。", "Fukuro wa irimasen.", "I don't need a bag.")
    )
    SpeakTopic.StationTravel -> listOf(
        SpeakStarterPhrase("渋谷駅までどうやって行きますか？", "Shibuya-eki made dō yatte ikimasu ka?", "How do I get to Shibuya Station?"),
        SpeakStarterPhrase("何番線ですか？", "Nan-bansen desu ka?", "Which platform number is it?")
    )
    SpeakTopic.SelfIntro -> listOf(
        SpeakStarterPhrase("はじめまして！よろしくお願いします。", "Hajimemashite! Yoroshiku onegaishimasu.", "Nice to meet you!"),
        SpeakStarterPhrase("趣味は旅行と音楽です。", "Shumi wa ryokō to ongaku desu.", "My hobbies are travel and music.")
    )
    SpeakTopic.Custom -> listOf(
        SpeakStarterPhrase("ゆっくり話してください。", "Yukkuri hanashite kudasai.", "Please speak slowly."),
        SpeakStarterPhrase("もう一度言ってください。", "Mō ichido itte kudasai.", "Please say that one more time.")
    )
}
