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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedTextTest {

    @Test
    fun plainTextIsKept() {
        assertEquals("Hello world", SharedText.process("Hello world"))
    }

    @Test
    fun nullOrBlankGivesNull() {
        assertNull(SharedText.process(null))
        assertNull(SharedText.process("   \n\t \r\n "))
    }

    @Test
    fun subjectIsUsedWhenTextIsMissing() {
        assertEquals("Title", SharedText.process(null, "  Title "))
        assertEquals("Body", SharedText.process("Body", "Title"))
    }

    @Test
    fun lineEndingsAreNormalized() {
        assertEquals("a\nb\nc", SharedText.process("a\r\nb\rc"))
    }

    @Test
    fun linesAreTrimmedAndBlankRunsCollapsed() {
        assertEquals("one\n\ntwo", SharedText.process("  one  \n\n\n\n   two \n\n"))
    }

    @Test
    fun controlCharactersAreRemovedAndTabsBecomeSpaces() {
        assertEquals("ab c", SharedText.process("a\u0000b\tc\u0007"))
    }

    @Test
    fun emojisAreKept() {
        assertEquals("Bravo 🎉👨‍👩‍👧", SharedText.process("Bravo 🎉👨‍👩‍👧"))
    }

    @Test
    fun richTextIsFlattened() {
        val styled = StringBuilder("Styled text")
        assertEquals("Styled text", SharedText.process(styled))
    }

    @Test
    fun longTextIsCut() {
        val result = SharedText.process("x".repeat(5000))!!
        assertEquals(SharedText.MAX_LENGTH, result.length)
    }

    @Test
    fun cutNeverSplitsAnEmoji() {
        val text = "x".repeat(SharedText.MAX_LENGTH - 1) + "👨‍👩‍👧" + "yyy"
        val result = SharedText.process(text)!!
        assertTrue(result.length <= SharedText.MAX_LENGTH)
        assertEquals("x".repeat(SharedText.MAX_LENGTH - 1), result)
        assertTrue(!Character.isHighSurrogate(result.last()))
    }
}
