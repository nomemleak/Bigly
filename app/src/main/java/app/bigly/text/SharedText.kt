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

package app.bigly.text

/** Cleans up text received from the Android share menu before it fills the input field. */
object SharedText {

    /** Longer shares are cut: nobody reads more than this on a phone held at arm's length. */
    const val MAX_LENGTH = 2000

    /**
     * Returns the text to put in the input field, or null when there is nothing usable.
     *
     * Line endings become "\n", invisible control characters are dropped, spaces at
     * the ends of every line are removed, runs of blank lines shrink to one, and the
     * result is cut at [MAX_LENGTH] without splitting an emoji or a surrogate pair.
     * When only [subject] is given (some apps share a title alone), it is used instead.
     */
    fun process(text: CharSequence?, subject: CharSequence? = null): String? {
        val body = clean(text)
        if (body != null) return body
        return clean(subject)
    }

    private fun clean(input: CharSequence?): String? {
        if (input == null) return null
        val normalized = input.toString()
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .filter { it == '\n' || it == '\t' || !it.isISOControl() }
            .replace('\t', ' ')
        val lines = normalized.split('\n').map { it.trim() }
        val compact = buildList {
            var blank = 0
            for (line in lines) {
                if (line.isEmpty()) blank++ else blank = 0
                if (blank <= 1) add(line)
            }
        }.joinToString("\n").trim()
        if (compact.isEmpty()) return null
        return truncate(compact, MAX_LENGTH)
    }

    private fun truncate(text: String, max: Int): String {
        if (text.length <= max) return text
        var length = 0
        for (grapheme in Emoji.graphemes(text)) {
            if (length + grapheme.length > max) break
            length += grapheme.length
        }
        return text.substring(0, length).trimEnd()
    }
}
