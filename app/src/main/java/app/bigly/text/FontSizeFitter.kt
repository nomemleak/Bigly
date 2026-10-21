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
 * Pure helpers used to pick the biggest font size that still fits on screen.
 *
 * The actual text measurement lives in the UI layer; this object only knows how
 * to search sizes and how to judge a line layout, so it can be unit tested.
 */
object FontSizeFitter {

    /**
     * Returns the largest size in [min]..[max] for which [fits] is true.
     *
     * [fits] must be monotonic: if a size fits, every smaller size fits too.
     * When even [min] does not fit, [min] is returned so the caller always gets
     * a usable size.
     */
    fun largestFitting(
        min: Float,
        max: Float,
        precision: Float = 0.5f,
        fits: (Float) -> Boolean,
    ): Float {
        require(min > 0f) { "min must be positive" }
        require(max >= min) { "max must be >= min" }
        require(precision > 0f) { "precision must be positive" }

        if (fits(max)) return max
        if (!fits(min)) return min

        var low = min // always fits
        var high = max // never fits
        while (high - low > precision) {
            val mid = (low + high) / 2f
            if (fits(mid)) low = mid else high = mid
        }
        return low
    }

    /**
     * Tells whether a soft line break falls in the middle of a word.
     *
     * [lineEnds] holds the exclusive end offset of every visual line, as reported
     * by the text layout. A break is fine when it happens next to whitespace or
     * right after a hyphen or a slash.
     */
    fun breaksInsideWord(text: String, lineEnds: List<Int>): Boolean {
        for (end in lineEnds.dropLast(1)) {
            if (end <= 0 || end >= text.length) continue
            val before = text[end - 1]
            val after = text[end]
            if (before.isWhitespace() || after.isWhitespace()) continue
            if (before in BREAKABLE_PUNCTUATION) continue
            return true
        }
        return false
    }

    /** Text shown in the scrolling (LED banner) mode: always a single line. */
    fun toSingleLine(text: String): String =
        text.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("   ")

    private const val BREAKABLE_PUNCTUATION = "-‐–—/"
}
