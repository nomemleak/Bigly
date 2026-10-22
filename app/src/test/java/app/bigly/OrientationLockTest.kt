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
package app.bigly

import app.bigly.ui.display.OrientationLock
import app.bigly.ui.display.OrientationLock.AUTO
import app.bigly.ui.display.OrientationLock.LANDSCAPE
import app.bigly.ui.display.OrientationLock.PORTRAIT
import app.bigly.ui.display.next
import org.junit.Assert.assertEquals
import org.junit.Test

class OrientationLockTest {

    /** Taps the button [taps] times, the screen following each lock. */
    private fun cycle(startLandscape: Boolean, taps: Int): List<OrientationLock> {
        var lock = AUTO
        var first = AUTO
        var landscape = startLandscape
        return List(taps) {
            val next = lock.next(landscape, first)
            if (lock == AUTO) first = next
            lock = next
            if (lock != AUTO) landscape = lock == LANDSCAPE
            lock
        }
    }

    @Test
    fun firstTapFromPortraitTurnsToLandscape() {
        assertEquals(listOf(LANDSCAPE, PORTRAIT, AUTO, LANDSCAPE), cycle(startLandscape = false, taps = 4))
    }

    @Test
    fun firstTapFromLandscapeTurnsToPortrait() {
        assertEquals(listOf(PORTRAIT, LANDSCAPE, AUTO, PORTRAIT), cycle(startLandscape = true, taps = 4))
    }
}
