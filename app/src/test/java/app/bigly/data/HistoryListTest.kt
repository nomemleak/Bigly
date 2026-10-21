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

package app.bigly.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryListTest {

    @Test
    fun newEntryGoesFirst() {
        var list = emptyList<HistoryEntry>()
        list = HistoryList.add(list, "one", 1, 20)
        list = HistoryList.add(list, "two", 2, 20)
        assertEquals(listOf("two", "one"), list.map { it.text })
    }

    @Test
    fun duplicateMovesToTopWithNewTimestamp() {
        var list = emptyList<HistoryEntry>()
        list = HistoryList.add(list, "one", 1, 20)
        list = HistoryList.add(list, "two", 2, 20)
        list = HistoryList.add(list, "one", 3, 20)
        assertEquals(listOf(HistoryEntry("one", 3), HistoryEntry("two", 2)), list)
    }

    @Test
    fun textIsTrimmedAndBlankIgnored() {
        var list = HistoryList.add(emptyList(), "  hello \n", 1, 20)
        list = HistoryList.add(list, "   \n ", 2, 20)
        assertEquals(listOf(HistoryEntry("hello", 1)), list)
    }

    @Test
    fun innerLineBreaksAreKept() {
        val list = HistoryList.add(emptyList(), "a\nb", 1, 20)
        assertEquals("a\nb", list.single().text)
    }

    @Test
    fun sizeIsCappedAndOldestDropped() {
        var list = emptyList<HistoryEntry>()
        for (i in 1..25) list = HistoryList.add(list, "m$i", i.toLong(), HistoryList.DEFAULT_MAX_SIZE)
        assertEquals(20, list.size)
        assertEquals("m25", list.first().text)
        assertEquals("m6", list.last().text)
    }

    @Test
    fun removeDeletesOnlyThatEntry() {
        var list = emptyList<HistoryEntry>()
        list = HistoryList.add(list, "one", 1, 20)
        list = HistoryList.add(list, "two", 2, 20)
        assertEquals(listOf("one"), HistoryList.remove(list, "two").map { it.text })
    }

    @Test
    fun trimKeepsNewest() {
        var list = emptyList<HistoryEntry>()
        for (i in 1..5) list = HistoryList.add(list, "m$i", i.toLong(), 20)
        assertEquals(listOf("m5", "m4"), HistoryList.trim(list, 2).map { it.text })
        assertTrue(HistoryList.trim(list, 0).size == 1)
    }

    private val yellow = MessageStyle(0xFFFFEB3B.toInt(), 0xFF000000.toInt(), TextEffect.BLINK, DisplayMode.FIXED)
    private val blue = MessageStyle(0xFFFFFFFF.toInt(), 0xFF1565C0.toInt(), TextEffect.NONE, DisplayMode.SCROLLING)

    @Test
    fun styleIsStoredWithTheEntry() {
        val list = HistoryList.add(emptyList(), "hi", 1, 20, yellow)
        assertEquals(yellow, list.single().style)
    }

    @Test
    fun showingAgainWithAnotherStyleReplacesIt() {
        var list = HistoryList.add(emptyList(), "hi", 1, 20, yellow)
        list = HistoryList.add(list, "hi", 2, 20, blue)
        assertEquals(listOf(HistoryEntry("hi", 2, blue)), list)
    }

    @Test
    fun updateStyleKeepsOrderAndIgnoresUnknownText() {
        var list = HistoryList.add(emptyList(), "a", 1, 20, yellow)
        list = HistoryList.add(list, "b", 2, 20, yellow)
        val updated = HistoryList.updateStyle(list, "a", blue)
        assertEquals(listOf("b", "a"), updated.map { it.text })
        assertEquals(blue, updated[1].style)
        assertEquals(list, HistoryList.updateStyle(list, "missing", blue))
    }
}
