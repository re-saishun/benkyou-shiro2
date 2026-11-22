package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.VocabCardResolver
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.app_data.data.formattedVocabReading
import ua.syt0r.kanji.core.app_data.data.toFurigana
import ua.syt0r.kanji.core.app_data.data.withoutAnnotations
import ua.syt0r.kanji.core.japanese.getKanaInfo
import ua.syt0r.kanji.core.japanese.isKana
import ua.syt0r.kanji.core.japanese.isKanji
import ua.syt0r.kanji.core.toInfoScreenData
import ua.syt0r.kanji.presentation.common.ui.kanji.parseKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeInputMode
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.CharacterWriterData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabCharacterDetails
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItemDescriptor

interface GetVocabPracticeWritingDataUseCase {
    suspend operator fun invoke(
        descriptor: VocabPracticeQueueItemDescriptor.Writing
    ): VocabPracticeItemData.Writing
}

class DefaultGetVocabPracticeWritingDataUseCase(
    private val vocabCardResolver: VocabCardResolver,
    private val appDataRepository: AppDataRepository
) : GetVocabPracticeWritingDataUseCase {

    override suspend fun invoke(
        descriptor: VocabPracticeQueueItemDescriptor.Writing
    ): VocabPracticeItemData.Writing {
        val card = vocabCardResolver.resolveUserCard(descriptor.cardId)
        val strokeEvaluator = descriptor.evaluator

        val letters: String
        val summaryReading: FuriganaString

        when {
            card.furigana != null -> {
                letters = card.furigana.withoutAnnotations()
                summaryReading = card.furigana
            }

            card.kanjiReading != null -> {
                letters = card.kanjiReading
                summaryReading = formattedVocabReading(
                    kanaReading = card.kanaReading,
                    kanjiReading = card.kanjiReading
                )
            }

            else -> {
                letters = card.kanaReading
                summaryReading = card.kanaReading.toFurigana()
            }
        }

        val reviewConfiguration = when (descriptor.inputMode) {
            WritingPracticeInputMode.Stroke -> CharacterWriterConfiguration.StrokeInput(isStudyMode = false)
            WritingPracticeInputMode.Character -> CharacterWriterConfiguration.CharacterInput
        }

        val characterDetailsMap = mutableMapOf<String, VocabCharacterDetails>()

        val writerData = letters
            .map { it.toString() }
            .map { character ->
                val rawStrokes = appDataRepository.getStrokes(character)
                val parsedStrokes = parseKanjiStrokes(rawStrokes)

                if (!characterDetailsMap.containsKey(character) && parsedStrokes.isNotEmpty()) {
                    val firstChar = character.first()
                    when {
                        firstChar.isKanji() -> {
                            val readings = appDataRepository.getReadings(character)
                            val meanings = appDataRepository.getMeanings(character)
                            val radicals = appDataRepository.getRadicalsInCharacter(character)
                            val variants = appDataRepository.getData(character)
                                ?.variantFamily
                                ?.replace(character, "")
                                ?.takeIf { it.isNotEmpty() }

                            characterDetailsMap[character] = VocabCharacterDetails.Kanji(
                                character = character,
                                strokes = parsedStrokes,
                                radicals = radicals,
                                on = readings.filter { it.value == ReadingType.ON }.keys.toList(),
                                kun = readings.filter { it.value == ReadingType.KUN }.keys.toList(),
                                meanings = meanings,
                                variants = variants
                            )
                        }

                        firstChar.isKana() -> {
                            val kanaInfo = runCatching { getKanaInfo(firstChar) }.getOrNull()
                            if (kanaInfo != null) {
                                characterDetailsMap[character] = VocabCharacterDetails.Kana(
                                    character = character,
                                    strokes = parsedStrokes,
                                    kanaSystem = kanaInfo.classification,
                                    reading = kanaInfo.reading
                                )
                            }
                        }
                    }
                }

                val characterWriterData = when {
                    parsedStrokes.isEmpty() -> null
                    else -> CharacterWriterData(
                        character = character,
                        strokeEvaluator = strokeEvaluator,
                        strokes = parsedStrokes,
                        shouldStudy = descriptor.shouldStudy,
                        reviewConfiguration = reviewConfiguration
                    )
                }
                character to characterWriterData
            }

        return VocabPracticeItemData.Writing(
            meaning = card.meaning,
            summaryReading = summaryReading,
            writerData = writerData,
            vocabReference = card.toInfoScreenData(),
            showKanaReading = descriptor.showKanaReading,
            kanaReading = card.kanaReading,
            shouldStudy = descriptor.shouldStudy,
            noTranslationsLayout = descriptor.noTranslationsLayout,
            leftHandedMode = descriptor.leftHandedMode,
            characterDetails = characterDetailsMap
        )
    }

}