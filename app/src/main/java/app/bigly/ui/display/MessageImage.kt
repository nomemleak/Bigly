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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import app.bigly.data.AppSettings
import app.bigly.data.DisplayMode
import app.bigly.data.TextEffect
import app.bigly.text.FontSizeFitter
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

/** Draws the message as shown on screen into a bitmap, for the "Export" button. */
object MessageImage {

    /** Bitmaps wider than this are scaled down (a long scrolling line can be very wide). */
    private const val MAX_SIDE_PX = 8192

    /**
     * Fixed mode: an image of the screen size, with the same colors, font and size.
     * Scrolling mode: the whole line at its on-screen height, so nothing is cut.
     * Animated effects are drawn at rest; the rainbow keeps its colors.
     */
    fun render(
        text: String,
        settings: AppSettings,
        widthPx: Int,
        heightPx: Int,
        paddingPx: Int,
        density: Density,
        layoutDirection: LayoutDirection,
        measurer: TextMeasurer,
        style: TextStyle,
    ): ImageBitmap = when (settings.displayMode) {
        DisplayMode.FIXED -> renderFixed(text, settings, widthPx, heightPx, paddingPx, density, layoutDirection, measurer, style)
        DisplayMode.SCROLLING -> renderLine(text, settings, heightPx, paddingPx, density, layoutDirection, measurer, style)
    }

    private fun renderFixed(
        text: String,
        settings: AppSettings,
        widthPx: Int,
        heightPx: Int,
        paddingPx: Int,
        density: Density,
        layoutDirection: LayoutDirection,
        measurer: TextMeasurer,
        style: TextStyle,
    ): ImageBitmap {
        val width = widthPx.coerceAtLeast(1)
        val height = heightPx.coerceAtLeast(1)
        val innerWidth = (width - 2 * paddingPx).coerceAtLeast(1)
        val innerHeight = (height - 2 * paddingPx).coerceAtLeast(1)
        val fontSize = fitMultilineFontSize(measurer, text, style, innerWidth, innerHeight, density)
        val layout = measurer.measure(
            text = text,
            style = style.copy(fontSize = fontSize),
            softWrap = true,
            constraints = Constraints(maxWidth = innerWidth),
            layoutDirection = layoutDirection,
            density = density,
        )
        return draw(width, height, settings, density, layoutDirection) {
            Offset(
                (width - layout.size.width) / 2f,
                (height - layout.size.height) / 2f,
            ) to layout
        }
    }

    private fun renderLine(
        text: String,
        settings: AppSettings,
        heightPx: Int,
        paddingPx: Int,
        density: Density,
        layoutDirection: LayoutDirection,
        measurer: TextMeasurer,
        style: TextStyle,
    ): ImageBitmap {
        val line = FontSizeFitter.toSingleLine(text)
        val base = style.forSingleLine()
        var fontSize = fitSingleLineFontSize(measurer, line, base, heightPx.coerceAtLeast(1), density)
        var layout = measurer.measure(line, base.copy(fontSize = fontSize), softWrap = false, maxLines = 1, density = density)
        var height = heightPx.coerceAtLeast(1)
        val fullWidth = layout.size.width + 2 * paddingPx
        if (fullWidth > MAX_SIDE_PX) {
            val scale = MAX_SIDE_PX.toFloat() / fullWidth
            fontSize *= scale
            height = (height * scale).roundToInt().coerceAtLeast(1)
            layout = measurer.measure(line, base.copy(fontSize = fontSize), softWrap = false, maxLines = 1, density = density)
        }
        val width = min(MAX_SIDE_PX, ceil(layout.size.width + 2f * paddingPx).toInt()).coerceAtLeast(1)
        return draw(width, height, settings, density, layoutDirection) {
            Offset((width - layout.size.width) / 2f, (height - layout.size.height) / 2f) to layout
        }
    }

    private fun draw(
        width: Int,
        height: Int,
        settings: AppSettings,
        density: Density,
        layoutDirection: LayoutDirection,
        placement: () -> Pair<Offset, TextLayoutResult>,
    ): ImageBitmap {
        val bitmap = ImageBitmap(width, height)
        val (topLeft, layout) = placement()
        CanvasDrawScope().draw(density, layoutDirection, Canvas(bitmap), Size(width.toFloat(), height.toFloat())) {
            drawRect(Color(settings.backgroundColor))
            if (settings.effect == TextEffect.RAINBOW) {
                drawText(layout, brush = rainbowBrush(0f, 1f, animate = false), topLeft = topLeft)
            } else {
                drawText(layout, color = Color(settings.textColor), topLeft = topLeft)
            }
        }
        return bitmap
    }
}
