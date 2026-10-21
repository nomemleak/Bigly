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

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class DisplayMode { FIXED, SCROLLING }

enum class ScrollDirection { RIGHT_TO_LEFT, LEFT_TO_RIGHT }

enum class TextEffect { NONE, BLINK, RAINBOW, SHAKE, PULSE }

/** Every user preference. Colors are opaque ARGB integers. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val textColor: Int = WHITE,
    val backgroundColor: Int = BLACK,
    val keepScreenOn: Boolean = true,
    val maxBrightness: Boolean = false,
    val displayMode: DisplayMode = DisplayMode.FIXED,
    val scrollSpeed: Int = DEFAULT_SPEED,
    val scrollDirection: ScrollDirection = ScrollDirection.RIGHT_TO_LEFT,
    val effect: TextEffect = TextEffect.NONE,
    val effectSpeed: Int = DEFAULT_SPEED,
    val historySize: Int = HistoryList.DEFAULT_MAX_SIZE,
) {
    companion object {
        const val WHITE = 0xFFFFFFFF.toInt()
        const val BLACK = 0xFF000000.toInt()

        const val MIN_SPEED = 1
        const val MAX_SPEED = 10
        const val DEFAULT_SPEED = 5

        val HISTORY_SIZES = listOf(10, 20, 50, 100)

        /** Scrolling speed, in screen widths per second. */
        fun screensPerSecond(speed: Int): Float =
            0.08f * speed.coerceIn(MIN_SPEED, MAX_SPEED)

        /** Multiplier applied to the base tempo of every effect (1 = normal). */
        fun effectTempo(speed: Int): Float =
            speed.coerceIn(MIN_SPEED, MAX_SPEED) / DEFAULT_SPEED.toFloat()
    }
}
