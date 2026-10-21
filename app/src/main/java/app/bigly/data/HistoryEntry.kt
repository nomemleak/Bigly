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

/**
 * A message that was shown on screen. [timestamp] is in milliseconds since the epoch.
 * [style] is how it looked (null for entries saved by Bigly 1.0 and 1.1).
 */
data class HistoryEntry(val text: String, val timestamp: Long, val style: MessageStyle? = null)

/** The look of one message, saved with it in the history and restored when it is replayed. */
data class MessageStyle(
    val textColor: Int,
    val backgroundColor: Int,
    val effect: TextEffect,
    val displayMode: DisplayMode,
)

fun AppSettings.toMessageStyle() = MessageStyle(textColor, backgroundColor, effect, displayMode)

fun AppSettings.withStyle(style: MessageStyle?): AppSettings =
    if (style == null) {
        this
    } else {
        copy(
            textColor = style.textColor,
            backgroundColor = style.backgroundColor,
            effect = style.effect,
            displayMode = style.displayMode,
        )
    }
