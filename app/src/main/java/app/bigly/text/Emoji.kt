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

/**
 * Minimal emoji knowledge, enough to keep line breaking and font sizing honest.
 * Rendering itself is left to the system emoji font.
 */
object Emoji {

    private const val ZERO_WIDTH_JOINER = 0x200D
    private const val VARIATION_SELECTOR_16 = 0xFE0F
    private const val COMBINING_KEYCAP = 0x20E3

    /** True for code points that belong to an emoji sequence (pictographs, modifiers, joiners, flags). */
    fun isEmojiPart(codePoint: Int): Boolean = when (codePoint) {
        ZERO_WIDTH_JOINER, VARIATION_SELECTOR_16, COMBINING_KEYCAP -> true
        in 0x1F000..0x1FAFF -> true // pictographs, emoticons, transport, flags letters, skin tones
        in 0x2600..0x27BF -> true // miscellaneous symbols and dingbats
        in 0x2300..0x23FF -> true // technical symbols such as ⌚ and ⏰
        in 0x2B00..0x2BFF -> true // arrows and stars such as ⭐
        in 0xE0020..0xE007F -> true // tag sequences (subdivision flags)
        0x00A9, 0x00AE, 0x203C, 0x2049, 0x2122, 0x2139, 0x3030, 0x303D, 0x3297, 0x3299 -> true
        else -> false
    }

    /** True when [text] contains at least one emoji. */
    fun containsEmoji(text: String): Boolean {
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            if (isEmojiPart(cp) && cp != ZERO_WIDTH_JOINER && cp != VARIATION_SELECTOR_16) return true
            i += Character.charCount(cp)
        }
        return false
    }

    /**
     * Splits [text] into user-perceived characters, keeping emoji sequences whole:
     * skin tones, joiner sequences (👨‍👩‍👧), flags (🇫🇷), keycaps (1️⃣) and tags.
     */
    fun graphemes(text: String): List<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            val start = i
            var cp = text.codePointAt(i)
            i += Character.charCount(cp)
            if (isRegionalIndicator(cp) && i < text.length && isRegionalIndicator(text.codePointAt(i))) {
                i += Character.charCount(text.codePointAt(i))
            }
            while (i < text.length) {
                val next = text.codePointAt(i)
                when {
                    next == VARIATION_SELECTOR_16 || next == COMBINING_KEYCAP || isSkinTone(next) ||
                        next in 0xE0020..0xE007F || Character.getType(next) == Character.NON_SPACING_MARK.toInt() -> {
                        i += Character.charCount(next)
                    }
                    next == ZERO_WIDTH_JOINER && i + 1 < text.length -> {
                        i += 1
                        cp = text.codePointAt(i)
                        i += Character.charCount(cp)
                    }
                    else -> break
                }
            }
            result += text.substring(start, i)
        }
        return result
    }

    private fun isRegionalIndicator(cp: Int) = cp in 0x1F1E6..0x1F1FF
    private fun isSkinTone(cp: Int) = cp in 0x1F3FB..0x1F3FF
}
