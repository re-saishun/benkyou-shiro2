package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data

import kotlinx.serialization.Serializable
import ua.syt0r.kanji.presentation.common.ScreenGrammarPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationCard

@Serializable
data class GrammarPracticeScreenConfiguration(
    val cards: List<Card>,
    val practiceType: ScreenGrammarPracticeType
) {

    @Serializable
    data class Card(
        val itemKey: String,
        val deckId: Long
    ) : PracticeConfigurationCard

}
