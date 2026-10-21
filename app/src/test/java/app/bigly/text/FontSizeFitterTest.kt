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
import kotlin.math.ceil

class FontSizeFitterTest {

    /**
     * Fake monospace layout: every character is 0.6 x size wide, every line is
     * 1.2 x size tall, and lines wrap on spaces only (no word breaking here).
     */
    private fun fitsMonospace(text: String, width: Float, height: Float): (Float) -> Boolean = { size ->
        val charWidth = 0.6f * size
        val lineHeight = 1.2f * size
        val maxChars = (width / charWidth).toInt()
        var lines = 0
        var fits = true
        for (paragraph in text.split('\n')) {
            var current = 0
            lines++
            for (word in paragraph.split(' ')) {
                if (word.length > maxChars) fits = false
                val needed = if (current == 0) word.length else current + 1 + word.length
                if (needed <= maxChars) {
                    current = needed
                } else {
                    lines++
                    current = word.length
                }
            }
        }
        fits && lines * lineHeight <= height
    }

    @Test
    fun returnsMaxWhenEverythingFits() {
        assertEquals(500f, FontSizeFitter.largestFitting(1f, 500f) { true }, 0f)
    }

    @Test
    fun returnsMinWhenNothingFits() {
        assertEquals(4f, FontSizeFitter.largestFitting(4f, 500f) { false }, 0f)
    }

    @Test
    fun findsThresholdWithinPrecision() {
        val result = FontSizeFitter.largestFitting(1f, 1000f, precision = 0.25f) { it <= 123.4f }
        assertTrue(result <= 123.4f)
        assertTrue(123.4f - result <= 0.25f)
    }

    @Test
    fun singleWordFillsTheWidth() {
        // "HELLO" = 5 chars, 0.6 * size each -> size = 1000 / 3 = 333.3
        val result = FontSizeFitter.largestFitting(1f, 2000f, precision = 0.1f, fits = fitsMonospace("HELLO", 1000f, 2000f))
        assertEquals(333.3f, result, 0.2f)
    }

    @Test
    fun singleWordLimitedByHeight() {
        // Width would allow 333, but 1.2 * size must stay below 120 -> 100.
        val result = FontSizeFitter.largestFitting(1f, 2000f, precision = 0.1f, fits = fitsMonospace("HELLO", 1000f, 120f))
        assertEquals(100f, result, 0.2f)
    }

    @Test
    fun multilineTextFitsBothDimensions() {
        val text = "HI\nTHERE"
        val width = 600f
        val height = 1000f
        val result = FontSizeFitter.largestFitting(1f, 2000f, precision = 0.1f, fits = fitsMonospace(text, width, height))
        // Longest line has 5 chars -> 200; two lines -> 1.2 * 2 * size <= 1000 -> 416.
        assertEquals(200f, result, 0.2f)
        assertTrue(ceil(result * 0.6f * 5) <= width + 1)
    }

    @Test
    fun longerTextGetsSmallerFont() {
        val short = FontSizeFitter.largestFitting(1f, 2000f, fits = fitsMonospace("OK", 1080f, 1920f))
        val long = FontSizeFitter.largestFitting(1f, 2000f, fits = fitsMonospace("This is a much longer message", 1080f, 1920f))
        assertTrue(long < short)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidRange() {
        FontSizeFitter.largestFitting(10f, 5f) { true }
    }

    @Test
    fun breakOnSpaceIsAccepted() {
        // "HELLO " | "WORLD"
        assertFalse(FontSizeFitter.breaksInsideWord("HELLO WORLD", listOf(6, 11)))
    }

    @Test
    fun breakOnNewlineIsAccepted() {
        assertFalse(FontSizeFitter.breaksInsideWord("HELLO\nWORLD", listOf(6, 11)))
    }

    @Test
    fun breakInsideWordIsDetected() {
        // "HEL" | "LO"
        assertTrue(FontSizeFitter.breaksInsideWord("HELLO", listOf(3, 5)))
    }

    @Test
    fun breakAfterHyphenIsAccepted() {
        assertFalse(FontSizeFitter.breaksInsideWord("WELL-KNOWN", listOf(5, 10)))
    }

    @Test
    fun singleLineNeverBreaks() {
        assertFalse(FontSizeFitter.breaksInsideWord("HELLO", listOf(5)))
    }

    @Test
    fun singleLineJoinsParagraphs() {
        assertEquals("HELLO   WORLD", FontSizeFitter.toSingleLine(" HELLO \n\n WORLD\n"))
    }
}
