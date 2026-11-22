package ua.syt0r.kanji.presentation.screen.main.screen.vocab_card

import androidx.compose.runtime.Composable

@Composable
expect fun rememberJsonFilePicker(
    onFileLoaded: (content: String) -> Unit,
    onError: (message: String) -> Unit
): () -> Unit
