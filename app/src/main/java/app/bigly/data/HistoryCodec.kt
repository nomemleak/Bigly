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
 * Plain text format of the history file:
 *
 * ```
 * bigly-history 1
 * <timestamp>\t<escaped text>
 * ```
 *
 * Backslashes, tabs and line breaks inside a message are escaped, so every
 * entry stays on one line. Damaged lines are skipped instead of failing.
 */
object HistoryCodec {

    private const val HEADER = "bigly-history 1"

    fun encode(entries: List<HistoryEntry>): String = buildString {
        append(HEADER).append('\n')
        for (entry in entries) {
            append(entry.timestamp).append('\t').append(escape(entry.text)).append('\n')
        }
    }

    fun decode(data: String): List<HistoryEntry> {
        val lines = data.split('\n')
        if (lines.firstOrNull()?.trimEnd('\r') != HEADER) return emptyList()
        return lines.drop(1).mapNotNull(::decodeLine)
    }

    private fun decodeLine(line: String): HistoryEntry? {
        val tab = line.indexOf('\t')
        if (tab <= 0) return null
        val timestamp = line.substring(0, tab).toLongOrNull() ?: return null
        val text = unescape(line.substring(tab + 1)) ?: return null
        if (text.isEmpty()) return null
        return HistoryEntry(text, timestamp)
    }

    internal fun escape(text: String): String = buildString(text.length) {
        for (c in text) {
            when (c) {
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
    }

    /** Returns null when [text] contains an invalid escape sequence. */
    internal fun unescape(text: String): String? {
        val out = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c != '\\') {
                out.append(c)
                i++
                continue
            }
            if (i + 1 >= text.length) return null
            when (text[i + 1]) {
                '\\' -> out.append('\\')
                'n' -> out.append('\n')
                'r' -> out.append('\r')
                't' -> out.append('\t')
                else -> return null
            }
            i += 2
        }
        return out.toString()
    }
}
