/*
 * Bigly - show text in huge letters on your screen.
 * Copyright (C) 2026 Bigly contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package app.bigly.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Font sizing with emojis. The fake layout below gives an emoji 1.25 times the
 * width of a letter, as color emoji fonts do, and wraps on spaces only.
 */
class EmojiFontSizeTest {

    private fun widthUnits(word: String): Float =
        Emoji.graphemes(word).sumOf { g -> if (Emoji.containsEmoji(g)) 1.25 else 0.6 }.toFloat()

    private fun fits(text: String, width: Float, height: Float): (Float) -> Boolean = { size ->
        var lines = 0
        var ok = true
        for (paragraph in text.split('\n')) {
            lines++
            var current = 0f
            for (word in paragraph.split(' ')) {
                val w = widthUnits(word) * size
                if (w > width) ok = false
                val space = if (current == 0f) 0f else 0.3f * size
                if (current + space + w <= width) {
                    current += space + w
                } else {
                    lines++
                    current = w
                }
            }
        }
        ok && lines * 1.2f * size <= height
    }

    @Test
    fun graphemesKeepEmojiSequencesWhole() {
        assertEquals(listOf("👍🏽"), Emoji.graphemes("👍🏽"))
        assertEquals(listOf("👨‍👩‍👧"), Emoji.graphemes("👨‍👩‍👧"))
        assertEquals(listOf("🇫🇷", "🇧🇪"), Emoji.graphemes("🇫🇷🇧🇪"))
        assertEquals(listOf("1️⃣"), Emoji.graphemes("1️⃣"))
        assertEquals(listOf("❤️"), Emoji.graphemes("❤️"))
        assertEquals(listOf("H", "i", " ", "🎉"), Emoji.graphemes("Hi 🎉"))
        assertEquals(listOf("e\u0301"), Emoji.graphemes("e\u0301"))
    }

    @Test
    fun detectsEmoji() {
        assertTrue(Emoji.containsEmoji("Bravo 🎉"))
        assertTrue(Emoji.containsEmoji("⭐"))
        assertFalse(Emoji.containsEmoji("Bravo !"))
        assertFalse(Emoji.containsEmoji("日本語"))
    }

    @Test
    fun emojiOnlyMessageFillsTheWidth() {
        // One emoji = 1.25 units -> size = 1000 / 1.25 = 800, limited by height 1.2 * 800 = 960 <= 2000.
        val size = FontSizeFitter.largestFitting(1f, 2000f, 0.1f, fits("🎉", 1000f, 2000f))
        assertEquals(800f, size, 0.2f)
    }

    @Test
    fun emojisMakeTheFontSmallerThanLettersOfSameCount() {
        val letters = FontSizeFitter.largestFitting(1f, 2000f, fits = fits("ABC", 1000f, 2000f))
        val emojis = FontSizeFitter.largestFitting(1f, 2000f, fits = fits("🎉🎉🎉", 1000f, 2000f))
        assertTrue(emojis < letters)
    }

    @Test
    fun skinToneAndJoinerSequencesCountAsOneCharacter() {
        val simple = FontSizeFitter.largestFitting(1f, 2000f, fits = fits("👍", 1000f, 2000f))
        val toned = FontSizeFitter.largestFitting(1f, 2000f, fits = fits("👍🏽", 1000f, 2000f))
        val family = FontSizeFitter.largestFitting(1f, 2000f, fits = fits("👨‍👩‍👧", 1000f, 2000f))
        assertEquals(simple, toned, 0.5f)
        assertEquals(simple, family, 0.5f)
    }

    @Test
    fun mixedTextAndEmojiNeverOverflows() {
        val text = "Joyeux anniversaire 🎂🎉\nMarie 💖"
        val width = 1080f
        val height = 1920f
        val size = FontSizeFitter.largestFitting(1f, 2000f, fits = fits(text, width, height))
        assertTrue(fits(text, width, height)(size))
        assertFalse(fits(text, width, height)(size + 2f))
    }

    @Test
    fun lineBreakBetweenEmojisIsAccepted() {
        // "🎉🎉" | "🎉": each emoji is 2 chars long, the break is at offset 4.
        assertFalse(FontSizeFitter.breaksInsideWord("🎉🎉🎉", listOf(4, 6)))
    }

    @Test
    fun lineBreakBetweenWordAndEmojiIsAccepted() {
        assertFalse(FontSizeFitter.breaksInsideWord("Bravo🎉", listOf(5, 7)))
    }

    @Test
    fun lineBreakInsideWordNextToEmojiIsStillDetected() {
        // "🎉Bra" | "vo"
        assertTrue(FontSizeFitter.breaksInsideWord("🎉Bravo", listOf(5, 7)))
    }

    @Test
    fun lineBreakBetweenIdeographsIsAccepted() {
        assertFalse(FontSizeFitter.breaksInsideWord("你好世界", listOf(2, 4)))
    }

    @Test
    fun scrollingLineKeepsEmojis() {
        assertEquals("Go 🚀   ⭐⭐", FontSizeFitter.toSingleLine("Go 🚀\n⭐⭐"))
    }
}
