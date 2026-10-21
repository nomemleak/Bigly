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
 * bigly-history 2
 * <timestamp>\t<style>\t<escaped text>
 * ```
 *
 * `<style>` is `-` or `TEXTCOLOR,BACKGROUND,EFFECT,MODE` (colors as 8 hex digits).
 * Backslashes, tabs and line breaks inside a message are escaped, so every
 * entry stays on one line. Files written by version 1 (`<timestamp>\t<text>`)
 * are still read. Damaged lines are skipped instead of failing.
 */
object HistoryCodec {

    private const val HEADER_V1 = "bigly-history 1"
    private const val HEADER = "bigly-history 2"
    private const val NO_STYLE = "-"

    fun encode(entries: List<HistoryEntry>): String = buildString {
        append(HEADER).append('\n')
        for (entry in entries) {
            append(entry.timestamp).append('\t')
            append(encodeStyle(entry.style)).append('\t')
            append(escape(entry.text)).append('\n')
        }
    }

    fun decode(data: String): List<HistoryEntry> {
        val lines = data.split('\n')
        return when (lines.firstOrNull()?.trimEnd('\r')) {
            HEADER -> lines.drop(1).mapNotNull(::decodeLine)
            HEADER_V1 -> lines.drop(1).mapNotNull(::decodeLineV1)
            else -> emptyList()
        }
    }

    private fun decodeLine(line: String): HistoryEntry? {
        val parts = line.split('\t', limit = 3)
        if (parts.size != 3) return null
        val timestamp = parts[0].toLongOrNull() ?: return null
        val text = unescape(parts[2]) ?: return null
        if (text.isEmpty()) return null
        return HistoryEntry(text, timestamp, decodeStyle(parts[1]))
    }

    private fun decodeLineV1(line: String): HistoryEntry? {
        val tab = line.indexOf('\t')
        if (tab <= 0) return null
        val timestamp = line.substring(0, tab).toLongOrNull() ?: return null
        val text = unescape(line.substring(tab + 1)) ?: return null
        if (text.isEmpty()) return null
        return HistoryEntry(text, timestamp)
    }

    private fun encodeStyle(style: MessageStyle?): String {
        if (style == null) return NO_STYLE
        return "%08X,%08X,%s,%s".format(style.textColor, style.backgroundColor, style.effect.name, style.displayMode.name)
    }

    /** An unreadable style is dropped; the message itself is kept. */
    private fun decodeStyle(value: String): MessageStyle? {
        val parts = value.split(',')
        if (parts.size != 4) return null
        val text = parts[0].toLongOrNull(16)?.toInt() ?: return null
        val background = parts[1].toLongOrNull(16)?.toInt() ?: return null
        val effect = TextEffect.entries.firstOrNull { it.name == parts[2] } ?: return null
        val mode = DisplayMode.entries.firstOrNull { it.name == parts[3] } ?: return null
        return MessageStyle(text, background, effect, mode)
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
