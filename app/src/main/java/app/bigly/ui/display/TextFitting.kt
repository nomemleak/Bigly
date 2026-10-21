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

package app.bigly.ui.display

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import app.bigly.text.FontSizeFitter

/** Smallest size tried, in pixels. Very long messages end up at this size. */
private const val MIN_FONT_PX = 8f

/**
 * Largest font size at which [text] fits in [maxWidth] x [maxHeight] pixels.
 * Lines may wrap between words (or between emojis and ideographs), never inside a word.
 * Emojis are measured with the system emoji font, like any other glyph.
 */
fun fitMultilineFontSize(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    maxWidth: Int,
    maxHeight: Int,
    density: Density,
): TextUnit {
    val px = FontSizeFitter.largestFitting(
        min = MIN_FONT_PX,
        max = maxHeight.toFloat().coerceAtLeast(MIN_FONT_PX),
        precision = 1f,
    ) { sizePx ->
        val layout = measurer.measure(
            text = text,
            style = style.copy(fontSize = with(density) { sizePx.toSp() }),
            softWrap = true,
            constraints = Constraints(maxWidth = maxWidth.coerceAtLeast(1)),
            density = density,
            skipCache = true,
        )
        val lineEnds = List(layout.lineCount) { layout.getLineEnd(it) }
        layout.size.height <= maxHeight &&
            !layout.hasVisualOverflow &&
            !FontSizeFitter.breaksInsideWord(text, lineEnds)
    }
    return with(density) { px.toSp() }
}

/** Largest font size at which the single [line] is at most [maxHeight] pixels tall. */
fun fitSingleLineFontSize(
    measurer: TextMeasurer,
    line: String,
    style: TextStyle,
    maxHeight: Int,
    density: Density,
): TextUnit {
    val px = FontSizeFitter.largestFitting(
        min = MIN_FONT_PX,
        max = maxHeight.toFloat().coerceAtLeast(MIN_FONT_PX),
        precision = 1f,
    ) { sizePx ->
        val layout = measurer.measure(
            text = line,
            style = style.copy(fontSize = with(density) { sizePx.toSp() }),
            softWrap = false,
            maxLines = 1,
            density = density,
            skipCache = true,
        )
        layout.size.height <= maxHeight
    }
    return with(density) { px.toSp() }
}

/** Style used for the scrolling line: left aligned so its measured width is exact. */
fun TextStyle.forSingleLine(): TextStyle = copy(textAlign = TextAlign.Start)
