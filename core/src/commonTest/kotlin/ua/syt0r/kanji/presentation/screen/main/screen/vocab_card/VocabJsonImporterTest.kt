package ua.syt0r.kanji.presentation.screen.main.screen.vocab_card

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VocabJsonImporterTest {

    @Test
    fun testParseStandardArray() {
        val json = """
        [
            {"kanji": "日本", "kana": "にほん", "meaning": "Japan"},
            {"kanji": "猫", "kana": "ねこ", "meaning": "Cat"}
        ]
        """.trimIndent()

        val result = VocabJsonImporter.parse(json)
        assertTrue(result is VocabJsonImporter.ParseResult.Success)
        assertEquals(2, result.cards.size)
        assertEquals("日本", result.cards[0].kanjiReading)
        assertEquals("にほん", result.cards[0].kanaReading)
        assertEquals("Japan", result.cards[0].meaning)

        assertEquals("猫", result.cards[1].kanjiReading)
        assertEquals("ねこ", result.cards[1].kanaReading)
        assertEquals("Cat", result.cards[1].meaning)
    }

    @Test
    fun testParseWrappedObject() {
        val json = """
        {
            "deckName": "N5 Vocabulary",
            "cards": [
                {"word": "勉強", "reading": "べんきょう", "translation": "Study", "id": 12345},
                {"kana": "ありがとう", "arti": "Terima kasih"}
            ]
        }
        """.trimIndent()

        val result = VocabJsonImporter.parse(json)
        assertTrue(result is VocabJsonImporter.ParseResult.Success)
        assertEquals(2, result.cards.size)

        assertEquals("勉強", result.cards[0].kanjiReading)
        assertEquals("べんきょう", result.cards[0].kanaReading)
        assertEquals("Study", result.cards[0].meaning)
        assertEquals(12345L, result.cards[0].dictionaryId)

        assertEquals(null, result.cards[1].kanjiReading)
        assertEquals("ありがとう", result.cards[1].kanaReading)
        assertEquals("Terima kasih", result.cards[1].meaning)
    }

    @Test
    fun testParseSampleJson() {
        val result = VocabJsonImporter.parse(VocabJsonImporter.SampleJson)
        assertTrue(result is VocabJsonImporter.ParseResult.Success)
        assertEquals(4, result.cards.size)
    }

    @Test
    fun testParseEmptyOrInvalid() {
        val emptyResult = VocabJsonImporter.parse("")
        assertTrue(emptyResult is VocabJsonImporter.ParseResult.Error)

        val invalidResult = VocabJsonImporter.parse("{ invalid json }")
        assertTrue(invalidResult is VocabJsonImporter.ParseResult.Error)
    }

}
