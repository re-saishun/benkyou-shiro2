package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.speak

import androidx.compose.runtime.Composable

data class AudioPermissionState(
    val isGranted: Boolean,
    val requestPermission: () -> Unit
)

@Composable
expect fun rememberAudioPermissionState(
    onPermissionGranted: () -> Unit = {}
): AudioPermissionState
