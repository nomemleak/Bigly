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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import app.bigly.text.FontSizeFitter

/** Smallest size tried, in pixels. Very long messages end up at this size. */
private const val MIN_FONT_PX = 8f

/**
 * Shows [text] with the largest font that fits the available space.
 * Explicit line breaks are kept; lines may also wrap between words, never inside one.
 */
@Composable
fun FitText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val maxWidth = constraints.maxWidth
        val maxHeight = constraints.maxHeight

        val fontSize = remember(text, style, maxWidth, maxHeight, density) {
            val px = FontSizeFitter.largestFitting(
                min = MIN_FONT_PX,
                max = maxHeight.toFloat().coerceAtLeast(MIN_FONT_PX),
                precision = 1f,
            ) { sizePx ->
                val layout = measurer.measure(
                    text = text,
                    style = style.copy(fontSize = with(density) { sizePx.toSp() }),
                    softWrap = true,
                    constraints = Constraints(maxWidth = maxWidth),
                    skipCache = true,
                )
                val lineEnds = List(layout.lineCount) { layout.getLineEnd(it) }
                layout.size.height <= maxHeight &&
                    !layout.hasVisualOverflow &&
                    !FontSizeFitter.breaksInsideWord(text, lineEnds)
            }
            with(density) { px.toSp() }
        }

        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = style.copy(fontSize = fontSize), softWrap = true)
        }
    }
}
