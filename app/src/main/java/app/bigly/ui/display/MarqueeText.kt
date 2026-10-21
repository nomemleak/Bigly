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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import app.bigly.data.AppSettings
import app.bigly.data.ScrollDirection
import app.bigly.text.FontSizeFitter
import kotlin.math.roundToInt

/**
 * LED banner: one line as tall as the screen, crossing it in a continuous loop.
 * The text enters from one edge as soon as it has left from the other.
 */
@Composable
fun MarqueeText(
    text: String,
    style: TextStyle,
    speed: Int,
    direction: ScrollDirection,
    clock: State<Float>,
    modifier: Modifier = Modifier,
    brush: (() -> Brush?)? = null,
) {
    BoxWithConstraints(modifier.clipToBounds(), contentAlignment = AbsoluteAlignment.CenterLeft) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val line = remember(text) { FontSizeFitter.toSingleLine(text) }
        val screenWidth = constraints.maxWidth.toFloat()
        val maxHeight = constraints.maxHeight

        val lineStyle = remember(line, style, maxHeight, density) {
            val base = style.forSingleLine()
            base.copy(fontSize = fitSingleLineFontSize(measurer, line, base, maxHeight, density))
        }
        val textWidth = remember(line, lineStyle, density) {
            measurer.measure(line, lineStyle, softWrap = false, maxLines = 1, density = density).size.width.toFloat()
        }

        val pixelsPerSecond = screenWidth * AppSettings.screensPerSecond(speed)
        val cycle = screenWidth + textWidth
        val currentBrush = brush?.invoke()

        Text(
            text = line,
            style = if (currentBrush != null) lineStyle.copy(brush = currentBrush) else lineStyle,
            softWrap = false,
            maxLines = 1,
            modifier = Modifier
                .wrapContentWidth(AbsoluteAlignment.Left, unbounded = true)
                .absoluteOffset {
                    val travelled = if (cycle > 0f) (clock.value * pixelsPerSecond) % cycle else 0f
                    val x = when (direction) {
                        ScrollDirection.RIGHT_TO_LEFT -> screenWidth - travelled
                        ScrollDirection.LEFT_TO_RIGHT -> travelled - textWidth
                    }
                    IntOffset(x.roundToInt(), 0)
                },
        )
    }
}
