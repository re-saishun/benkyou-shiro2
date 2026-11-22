package ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import org.jetbrains.compose.resources.stringResource
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTextAnalysisProvider
import ua.syt0r.kanji.dialog_cancel
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.TextAnalysisConfigurationData
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.GeminiTextAnalysisUseCase
import ua.syt0r.kanji.text_analysis_configuration_analysis_provider
import ua.syt0r.kanji.text_analysis_configuration_auto_description
import ua.syt0r.kanji.text_analysis_configuration_auto_title
import ua.syt0r.kanji.text_analysis_configuration_gemini_api_key_hint
import ua.syt0r.kanji.text_analysis_configuration_gemini_api_key_label
import ua.syt0r.kanji.text_analysis_configuration_gemini_description
import ua.syt0r.kanji.text_analysis_configuration_gemini_model_label
import ua.syt0r.kanji.text_analysis_configuration_gemini_title
import ua.syt0r.kanji.text_analysis_configuration_provider_mode
import ua.syt0r.kanji.text_analysis_configuration_save
import ua.syt0r.kanji.text_analysis_configuration_title
import ua.syt0r.kanji.text_analysis_configuration_translation_provider
import ua.syt0r.kanji.text_analysis_ichiran_description

@Composable
fun TextAnalysisConfigurationDialog(
    configuration: TextAnalysisConfigurationData,
    onSave: (PreferencesTextAnalysisProvider, String) -> Unit,
    onDismissRequest: () -> Unit
) {

    var selectedProvider by remember(configuration.provider) {
        mutableStateOf(configuration.provider)
    }
    var apiKeyInput by remember(configuration.geminiApiKey) {
        mutableStateOf(configuration.geminiApiKey)
    }
    var showApiKey by remember { mutableStateOf(false) }

    val categoryRow: @Composable (String) -> Unit = { title ->
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = Dimens.SpacingBig, bottom = Dimens.SpacingSmall)
        )
    }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.text_analysis_configuration_title)) },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)) {
                categoryRow(stringResource(Res.string.text_analysis_configuration_provider_mode))

                ProviderOptionRow(
                    title = stringResource(Res.string.text_analysis_configuration_auto_title),
                    subtitle = stringResource(Res.string.text_analysis_configuration_auto_description),
                    selected = selectedProvider == PreferencesTextAnalysisProvider.Automatic,
                    testTag = "text_analysis_config_auto_option",
                    onClick = { selectedProvider = PreferencesTextAnalysisProvider.Automatic }
                )

                ProviderOptionRow(
                    title = stringResource(Res.string.text_analysis_configuration_gemini_title),
                    subtitle = stringResource(
                        Res.string.text_analysis_configuration_gemini_description,
                        GeminiTextAnalysisUseCase.GEMINI_MODEL_ID
                    ),
                    selected = selectedProvider == PreferencesTextAnalysisProvider.Gemini,
                    testTag = "text_analysis_config_gemini_option",
                    onClick = { selectedProvider = PreferencesTextAnalysisProvider.Gemini }
                )

                AnimatedVisibility(visible = selectedProvider == PreferencesTextAnalysisProvider.Gemini) {
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = {
                            Text(stringResource(Res.string.text_analysis_configuration_gemini_api_key_label))
                        },
                        placeholder = {
                            Text(stringResource(Res.string.text_analysis_configuration_gemini_api_key_hint))
                        },
                        supportingText = {
                            Text(
                                stringResource(
                                    Res.string.text_analysis_configuration_gemini_model_label,
                                    GeminiTextAnalysisUseCase.GEMINI_MODEL_ID
                                )
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (showApiKey) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (apiKeyInput.isNotEmpty()) {
                                    IconButton(
                                        onClick = { apiKeyInput = "" },
                                        modifier = Modifier.testTag("text_analysis_config_clear_key")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Clear API key"
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { showApiKey = !showApiKey },
                                    modifier = Modifier.testTag("text_analysis_config_toggle_key_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (showApiKey) {
                                            Icons.Outlined.VisibilityOff
                                        } else {
                                            Icons.Outlined.Visibility
                                        },
                                        contentDescription = "Toggle API key visibility"
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.SpacingSmall)
                            .testTag("text_analysis_config_gemini_key_input")
                    )
                }

                categoryRow(stringResource(Res.string.text_analysis_configuration_analysis_provider))
                if (selectedProvider == PreferencesTextAnalysisProvider.Automatic) {
                    SummaryRow("Ichiran", stringResource(Res.string.text_analysis_ichiran_description))
                } else {
                    SummaryRow(
                        "Gemini 3.1 Flash-Lite",
                        GeminiTextAnalysisUseCase.GEMINI_MODEL_ID
                    )
                }

                categoryRow(stringResource(Res.string.text_analysis_configuration_translation_provider))
                if (selectedProvider == PreferencesTextAnalysisProvider.Automatic) {
                    SummaryRow("Gemini 2.0 Flash", null)
                } else {
                    SummaryRow(
                        "Gemini 3.1 Flash-Lite",
                        GeminiTextAnalysisUseCase.GEMINI_MODEL_ID
                    )
                }
            }
        },
        buttons = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("text_analysis_config_cancel_button")
            ) {
                Text(stringResource(Res.string.dialog_cancel))
            }
            TextButton(
                onClick = {
                    onSave(selectedProvider, apiKeyInput)
                    onDismissRequest()
                },
                modifier = Modifier.testTag("text_analysis_config_apply_button")
            ) {
                Text(stringResource(Res.string.text_analysis_configuration_save))
            }
        }
    )

}

@Composable
private fun ProviderOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMid),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.surfaceDim
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpacingMid, vertical = Dimens.SpacingMid)
            .testTag(testTag)
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryRow(
    title: String,
    subtitle: String?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMid),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceDim)
            .padding(horizontal = Dimens.SpacingBig, vertical = Dimens.SpacingMid)
    ) {
        Icon(Icons.Outlined.Check, null)

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
            subtitle?.also {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}