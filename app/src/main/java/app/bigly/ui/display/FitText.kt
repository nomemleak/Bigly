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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer

/**
 * Shows [text] with the largest font that fits the available space.
 * Explicit line breaks are kept; lines may also wrap between words, never inside one.
 *
 * [brush], when given, paints the letters (rainbow effect). It is read during
 * composition only by the inner text, so changing it never measures the text again.
 */
@Composable
fun FitText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    brush: (() -> Brush?)? = null,
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val maxWidth = constraints.maxWidth
        val maxHeight = constraints.maxHeight

        val sized = remember(text, style, maxWidth, maxHeight, density) {
            style.copy(fontSize = fitMultilineFontSize(measurer, text, style, maxWidth, maxHeight, density))
        }

        Box(contentAlignment = Alignment.Center) {
            val currentBrush = brush?.invoke()
            Text(
                text = text,
                style = if (currentBrush != null) sized.copy(brush = currentBrush) else sized,
                softWrap = true,
            )
        }
    }
}
