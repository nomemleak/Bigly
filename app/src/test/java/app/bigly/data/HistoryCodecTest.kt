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
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryCodecTest {

    @Test
    fun roundTripKeepsSpecialCharacters() {
        val entries = listOf(
            HistoryEntry("Bonjour à tous ! 🎉", 1_700_000_000_000),
            HistoryEntry("line 1\nline 2\r\nwith\ttab and \\ backslash \\n literal", 42),
            HistoryEntry("‮مرحبا", 7),
        )
        assertEquals(entries, HistoryCodec.decode(HistoryCodec.encode(entries)))
    }

    @Test
    fun emptyListRoundTrip() {
        assertEquals(emptyList<HistoryEntry>(), HistoryCodec.decode(HistoryCodec.encode(emptyList())))
    }

    @Test
    fun unknownFormatGivesEmptyList() {
        assertEquals(emptyList<HistoryEntry>(), HistoryCodec.decode("something else\n1\thello\n"))
        assertEquals(emptyList<HistoryEntry>(), HistoryCodec.decode(""))
    }

    @Test
    fun damagedLinesAreSkipped() {
        val data = "bigly-history 1\n1\tgood\nnot a line\nabc\tbad timestamp\n2\tbad \\x escape\n3\t\n4\tlast\n"
        assertEquals(listOf(HistoryEntry("good", 1), HistoryEntry("last", 4)), HistoryCodec.decode(data))
    }

    @Test
    fun unescapeRejectsTrailingBackslash() {
        assertNull(HistoryCodec.unescape("abc\\"))
    }
}
