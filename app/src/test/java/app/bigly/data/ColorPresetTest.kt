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

import app.bigly.util.ColorUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorPresetTest {

    @Test
    fun everyPresetHasEnoughContrast() {
        for (preset in ColorPreset.entries) {
            val ratio = ColorUtils.contrastRatio(preset.textColor, preset.backgroundColor)
            assertTrue("$preset has contrast $ratio", ratio >= 4.5)
        }
    }

    @Test
    fun thereAreAboutTenPresetsBesideBlackAndWhite() {
        assertTrue(ColorPreset.entries.size - 2 >= 10)
    }

    @Test
    fun presetsAreDistinct() {
        val pairs = ColorPreset.entries.map { it.textColor to it.backgroundColor }
        assertEquals(pairs.size, pairs.toSet().size)
    }

    @Test
    fun matchesCurrentSettings() {
        val settings = AppSettings(textColor = 0xFFFFEB3B.toInt(), backgroundColor = 0xFF000000.toInt())
        assertEquals(listOf(ColorPreset.YELLOW_ON_BLACK), ColorPreset.entries.filter { it.matches(settings) })
    }
}
