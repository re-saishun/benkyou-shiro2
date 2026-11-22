package ua.syt0r.kanji.presentation.screen.main.screen.vocab_card

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberJsonFilePicker(
    onFileLoaded: (content: String) -> Unit,
    onError: (message: String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }
                if (content != null) {
                    onFileLoaded(content)
                } else {
                    onError("Failed to read file content")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to open file")
            }
        }
    }
    return {
        launcher.launch(arrayOf("application/json", "text/*", "*/*"))
    }
}
