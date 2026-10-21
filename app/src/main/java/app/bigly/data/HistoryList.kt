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

/** Rules of the history list, kept free of any storage concern. Newest entry first. */
object HistoryList {

    const val DEFAULT_MAX_SIZE = 20

    /** Removes leading and trailing blank characters; inner line breaks are kept. */
    fun normalize(text: String): String = text.trim()

    /**
     * Puts [text] at the top of the list. An identical message already in the list
     * moves to the top instead of being duplicated. The list never grows past [maxSize].
     */
    fun add(
        entries: List<HistoryEntry>,
        text: String,
        timestamp: Long,
        maxSize: Int,
        style: MessageStyle? = null,
    ): List<HistoryEntry> {
        val limit = maxSize.coerceAtLeast(1)
        val clean = normalize(text)
        if (clean.isEmpty()) return entries.take(limit)
        val others = entries.filterNot { it.text == clean }
        return (listOf(HistoryEntry(clean, timestamp, style)) + others).take(limit)
    }

    /** Changes the look saved with [text], if that message is in the list. Order is kept. */
    fun updateStyle(entries: List<HistoryEntry>, text: String, style: MessageStyle): List<HistoryEntry> =
        entries.map { if (it.text == text) it.copy(style = style) else it }

    fun remove(entries: List<HistoryEntry>, text: String): List<HistoryEntry> =
        entries.filterNot { it.text == text }

    fun trim(entries: List<HistoryEntry>, maxSize: Int): List<HistoryEntry> =
        entries.take(maxSize.coerceAtLeast(1))
}
