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

package app.bigly.ui.settings

import androidx.annotation.StringRes
import app.bigly.R

/** A named color, so screen readers can say "Yellow" rather than a hex code. */
data class NamedColor(val argb: Int, @StringRes val name: Int)

val Palette = listOf(
    NamedColor(0xFFFFFFFF.toInt(), R.string.color_white),
    NamedColor(0xFF000000.toInt(), R.string.color_black),
    NamedColor(0xFFFFEB3B.toInt(), R.string.color_yellow),
    NamedColor(0xFFFFC107.toInt(), R.string.color_amber),
    NamedColor(0xFFFF9800.toInt(), R.string.color_orange),
    NamedColor(0xFFF44336.toInt(), R.string.color_red),
    NamedColor(0xFFE91E63.toInt(), R.string.color_pink),
    NamedColor(0xFF9C27B0.toInt(), R.string.color_purple),
    NamedColor(0xFF2196F3.toInt(), R.string.color_blue),
    NamedColor(0xFF00BCD4.toInt(), R.string.color_cyan),
    NamedColor(0xFF4CAF50.toInt(), R.string.color_green),
    NamedColor(0xFFCDDC39.toInt(), R.string.color_lime),
)
