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

/** Ready-made text and background pairs. Every pair has a contrast of at least 4.5:1 (checked by tests). */
enum class ColorPreset(val textColor: Int, val backgroundColor: Int) {
    BLACK_ON_WHITE(0xFF000000.toInt(), 0xFFFFFFFF.toInt()),
    WHITE_ON_BLACK(0xFFFFFFFF.toInt(), 0xFF000000.toInt()),
    YELLOW_ON_BLACK(0xFFFFEB3B.toInt(), 0xFF000000.toInt()),
    GREEN_ON_BLACK(0xFF00E676.toInt(), 0xFF000000.toInt()),
    ORANGE_ON_BLACK(0xFFFF9100.toInt(), 0xFF000000.toInt()),
    CYAN_ON_BLACK(0xFF00E5FF.toInt(), 0xFF000000.toInt()),
    PINK_ON_BLACK(0xFFFF4081.toInt(), 0xFF000000.toInt()),
    RED_ON_WHITE(0xFFD50000.toInt(), 0xFFFFFFFF.toInt()),
    BLUE_ON_WHITE(0xFF0D47A1.toInt(), 0xFFFFFFFF.toInt()),
    BLACK_ON_YELLOW(0xFF000000.toInt(), 0xFFFFD600.toInt()),
    WHITE_ON_RED(0xFFFFFFFF.toInt(), 0xFFB71C1C.toInt()),
    WHITE_ON_BLUE(0xFFFFFFFF.toInt(), 0xFF1565C0.toInt()),
    WHITE_ON_GREEN(0xFFFFFFFF.toInt(), 0xFF1B5E20.toInt()),
    ;

    fun matches(settings: AppSettings): Boolean =
        settings.textColor == textColor && settings.backgroundColor == backgroundColor
}
