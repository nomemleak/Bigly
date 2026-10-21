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

package app.bigly.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ColorUtilsTest {

    private val black = 0xFF000000.toInt()
    private val white = 0xFFFFFFFF.toInt()

    @Test
    fun blackOnWhiteIsMaximumContrast() {
        assertEquals(21.0, ColorUtils.contrastRatio(black, white), 0.01)
        assertEquals(21.0, ColorUtils.contrastRatio(white, black), 0.01)
    }

    @Test
    fun sameColorIsMinimumContrast() {
        assertEquals(1.0, ColorUtils.contrastRatio(0xFF336699.toInt(), 0xFF336699.toInt()), 0.0001)
    }

    @Test
    fun knownContrastValue() {
        // #777777 on white is about 4.48:1.
        assertEquals(4.48, ColorUtils.contrastRatio(0xFF777777.toInt(), white), 0.01)
    }

    @Test
    fun hexFormatting() {
        assertEquals("#FF8000", ColorUtils.toHex(0xFFFF8000.toInt()))
        assertEquals("#000000", ColorUtils.toHex(black))
    }

    @Test
    fun hexParsing() {
        assertEquals(0xFFFF8000.toInt(), ColorUtils.parseHex("#ff8000"))
        assertEquals(0xFFFF8000.toInt(), ColorUtils.parseHex("FF8000"))
        assertEquals(0xFFFFAA00.toInt(), ColorUtils.parseHex("#fa0"))
        assertNull(ColorUtils.parseHex("#12345"))
        assertNull(ColorUtils.parseHex("#GGGGGG"))
        assertNull(ColorUtils.parseHex(""))
    }

    @Test
    fun hsvConversions() {
        assertEquals(0xFFFF0000.toInt(), ColorUtils.hsvToColor(0f, 1f, 1f))
        assertEquals(0xFF00FF00.toInt(), ColorUtils.hsvToColor(120f, 1f, 1f))
        assertEquals(0xFF0000FF.toInt(), ColorUtils.hsvToColor(240f, 1f, 1f))
        assertEquals(white, ColorUtils.hsvToColor(42f, 0f, 1f))
        assertArrayEquals(floatArrayOf(240f, 1f, 1f), ColorUtils.colorToHsv(0xFF0000FF.toInt()), 0.001f)
    }

    @Test
    fun hsvRoundTrip() {
        for (color in listOf(0xFF123456, 0xFFABCDEF, 0xFFFF8000, 0xFF808080).map { it.toInt() }) {
            val hsv = ColorUtils.colorToHsv(color)
            assertEquals(color, ColorUtils.hsvToColor(hsv[0], hsv[1], hsv[2]))
        }
    }
}
