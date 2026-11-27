package ua.syt0r.kanji.presentation.screen.main.screen.text_analysis

import io.ktor.client.HttpClient
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import ua.syt0r.kanji.core.NetworkClients
import ua.syt0r.kanji.core.streak.DailyStreakData
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContentLanguage
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesDeckDetailsLetterLayout
import ua.syt0r.kanji.core.user_data.preferences.PreferencesDefaultHomeTab
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterPracticeType
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterSortOption
import ua.syt0r.kanji.core.user_data.preferences.PreferencesSyncDataInfo
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTextAnalysisProvider
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTheme
import ua.syt0r.kanji.core.user_data.preferences.PreferencesThemePalette
import ua.syt0r.kanji.core.user_data.preferences.PreferencesUserInfo
import ua.syt0r.kanji.core.user_data.preferences.PreferencesVocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.DefaultGeminiTextAnalysisUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.use_case.DefaultParseIchiranResponseUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GeminiTextAnalysisMapperTest {

    @Test
    fun mapsStructuredGeminiJsonIntoParsableAnalysisNodes() {
        val useCase = createDummyUseCase()
        val parser = DefaultParseIchiranResponseUseCase()

        val rawJson = """
            {
              "translation": "Saya belajar bahasa Jepang.",
              "tokens": [
                {
                  "text": "日本語",
                  "kana": "にほんご",
                  "romaji": "nihongo",
                  "dictionaryForm": "日本語",
                  "dictionaryKana": "にほんご",
                  "pos": "n",
                  "isPunctuation": false,
                  "glossaries": ["bahasa Jepang"]
                },
                {
                  "text": "を",
                  "kana": "を",
                  "romaji": "wo",
                  "dictionaryForm": "を",
                  "dictionaryKana": "を",
                  "pos": "prt",
                  "isPunctuation": false,
                  "glossaries": ["penanda objek"]
                },
                {
                  "text": "勉強します",
                  "kana": "べんきょうします",
                  "romaji": "benkyōshimasu",
                  "dictionaryForm": "勉強する",
                  "dictionaryKana": "べんきょうする",
                  "pos": "vs",
                  "conjugationType": "Non-past",
                  "isPunctuation": false,
                  "glossaries": ["belajar"]
                },
                {
                  "text": "。",
                  "kana": "。",
                  "isPunctuation": true,
                  "glossaries": []
                }
              ]
            }
        """.trimIndent()

        val response = useCase.parseGeneratedJsonToResponse("日本語を勉強します。", rawJson)
        assertEquals("Saya belajar bahasa Jepang.", response.translation)

        val nodes = parser(response.elements)
        assertEquals(4, nodes.size)

        val firstWord = assertIs<TextAnalysisNode.Word>(nodes[0])
        assertEquals("日本語", firstWord.text)
        assertEquals("にほんご", firstWord.reading.kanaReading)
        assertEquals(TextAnalysisNode.PartOfSpeech.Noun, firstWord.highlightPartOfSpeech)
        assertEquals(listOf("bahasa Jepang"), firstWord.cards.first().glossary)

        val particle = assertIs<TextAnalysisNode.Word>(nodes[1])
        assertEquals(TextAnalysisNode.PartOfSpeech.Particle, particle.highlightPartOfSpeech)

        val verb = assertIs<TextAnalysisNode.Word>(nodes[2])
        assertEquals(TextAnalysisNode.PartOfSpeech.Verb, verb.highlightPartOfSpeech)
        assertTrue(verb.cards.first().notes.contains("Non-past"))
        assertEquals("勉強する", verb.cards.first().reading.kanjiReading)

        val punctuation = assertIs<TextAnalysisNode.Text>(nodes[3])
        assertEquals("。", punctuation.value)
    }

    private fun createDummyUseCase(): DefaultGeminiTextAnalysisUseCase {
        val dummyNetworkClients = object : NetworkClients {
            override val unauthenticatedClient: HttpClient get() = error("Unused")
            override val authenticatedClient: HttpClient get() = error("Unused")
            override fun invalidateTokens() {}
        }
        val dummyPrefs = object : PreferencesContract.AppPreferences {
            override val refreshToken: SuspendedProperty<String?> get() = error("Unused")
            override val idToken: SuspendedProperty<String?> get() = error("Unused")
            override val userInfo: SuspendedProperty<PreferencesUserInfo?> get() = error("Unused")
            override val subscriptionAlert: SuspendedProperty<String?> get() = error("Unused")
            override val localDataId: SuspendedProperty<String> get() = error("Unused")
            override val localDataTimestamp: SuspendedProperty<Instant?> get() = error("Unused")
            override val lastSyncedDataInfo: SuspendedProperty<PreferencesSyncDataInfo?> get() = error("Unused")
            override val analyticsEnabled: SuspendedProperty<Boolean> get() = error("Unused")
            override val practiceType: SuspendedProperty<PreferencesLetterPracticeType> get() = error("Unused")
            override val filterNew: SuspendedProperty<Boolean> get() = error("Unused")
            override val filterDue: SuspendedProperty<Boolean> get() = error("Unused")
            override val filterDone: SuspendedProperty<Boolean> get() = error("Unused")
            override val sortOption: SuspendedProperty<PreferencesLetterSortOption> get() = error("Unused")
            override val isSortDescending: SuspendedProperty<Boolean> get() = error("Unused")
            override val practicePreviewLayout: SuspendedProperty<PreferencesDeckDetailsLetterLayout> get() = error("Unused")
            override val kanaGroupsEnabled: SuspendedProperty<Boolean> get() = error("Unused")
            override val theme: SuspendedProperty<PreferencesTheme> get() = error("Unused")
            override val themePalette: SuspendedProperty<PreferencesThemePalette> get() = error("Unused")
            override val dailyLimitEnabled: SuspendedProperty<Boolean> get() = error("Unused")
            override val dailyLimitConfigurationJson: SuspendedProperty<String> get() = error("Unused")
            override val reminderEnabled: SuspendedProperty<Boolean> get() = error("Unused")
            override val reminderTime: SuspendedProperty<LocalTime> get() = error("Unused")
            override val defaultHomeTab: SuspendedProperty<PreferencesDefaultHomeTab> get() = error("Unused")
            override val lastAppVersionWhenChangesDialogShown: SuspendedProperty<String> get() = error("Unused")
            override val tutorialSeen: SuspendedProperty<Boolean> get() = error("Unused")
            override val generalDashboardStudyTargets: SuspendedProperty<Map<String, Boolean>> get() = error("Unused")
            override val letterDashboardPracticeType: SuspendedProperty<PreferencesLetterPracticeType> get() = error("Unused")
            override val letterDashboardSortByTime: SuspendedProperty<Boolean> get() = error("Unused")
            override val vocabDashboardPracticeType: SuspendedProperty<PreferencesVocabPracticeType> get() = error("Unused")
            override val vocabDashboardSortByTime: SuspendedProperty<Boolean> get() = error("Unused")
            override val dailyResetTime: SuspendedProperty<LocalTime> get() = error("Unused")
            override val contentLanguage: SuspendedProperty<PreferencesContentLanguage> get() = error("Unused")
            override val dailyStreak: SuspendedProperty<DailyStreakData> get() = error("Unused")
            override val textAnalysisProvider: SuspendedProperty<PreferencesTextAnalysisProvider> get() = error("Unused")
            override val textAnalysisGeminiApiKey: SuspendedProperty<String> get() = error("Unused")
            override val grammarCatalogUrl: SuspendedProperty<String> get() = error("Unused")
            override val supabaseAccessToken: SuspendedProperty<String?> get() = error("Unused")
            override val supabaseRefreshToken: SuspendedProperty<String?> get() = error("Unused")
            override val supabaseUserEmail: SuspendedProperty<String?> get() = error("Unused")
            override val supabaseUserId: SuspendedProperty<String?> get() = error("Unused")
            override val supabaseLastSyncTimestamp: SuspendedProperty<Long?> get() = error("Unused")
        }
        return DefaultGeminiTextAnalysisUseCase(dummyNetworkClients, dummyPrefs)
    }

}
