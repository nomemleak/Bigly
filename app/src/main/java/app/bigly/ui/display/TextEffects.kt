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

import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import app.bigly.data.TextEffect
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val RainbowColors = listOf(
    Color(0xFFFF3B30),
    Color(0xFFFF9500),
    Color(0xFFFFCC00),
    Color(0xFF34C759),
    Color(0xFF00C7BE),
    Color(0xFF007AFF),
    Color(0xFFAF52DE),
    Color(0xFFFF3B30),
)

/** Width, in pixels, of one full rainbow cycle. */
private const val RAINBOW_PERIOD_PX = 1200f

/**
 * Rainbow gradient for the letters. It is set as the text brush rather than drawn
 * over the text, so color emojis keep their own colors. [seconds] moves the colors.
 */
fun rainbowBrush(seconds: Float, tempo: Float, animate: Boolean): Brush {
    val shift = if (animate) (seconds * 0.35f * tempo % 1f) * RAINBOW_PERIOD_PX else 0f
    return Brush.linearGradient(
        colors = RainbowColors,
        start = Offset(shift, 0f),
        end = Offset(shift + RAINBOW_PERIOD_PX, RAINBOW_PERIOD_PX * 0.25f),
        tileMode = TileMode.Repeated,
    )
}

/**
 * Applies one animated effect to the content.
 *
 * [clock] is in seconds and is only read inside layer and draw blocks, so the
 * text is never measured again while an effect runs. [tempo] scales the speed
 * (1 = normal). When [animate] is false every effect is shown still, which is
 * what "remove animations" in the system settings asks for.
 */
fun Modifier.textEffect(
    effect: TextEffect,
    clock: State<Float>,
    tempo: Float,
    animate: Boolean,
): Modifier = when (effect) {
    TextEffect.NONE -> this

    // Never faster than 2.5 flashes per second, below the 3 Hz photosensitivity threshold.
    TextEffect.BLINK -> graphicsLayer {
        alpha = if (!animate) 1f else {
            val phase = (clock.value * 1.25f * tempo) % 1f
            if (phase < 0.5f) 1f else 0f
        }
    }

    TextEffect.PULSE -> graphicsLayer {
        val scale = if (!animate) 1f else {
            0.9f + 0.1f * cos(2f * PI.toFloat() * 0.6f * tempo * clock.value)
        }
        scaleX = scale
        scaleY = scale
    }

    TextEffect.SHAKE -> graphicsLayer {
        if (animate) {
            val t = clock.value * tempo
            val amplitude = 0.012f * size.minDimension
            translationX = amplitude * (sin(t * 31f) + 0.5f * sin(t * 53f)) / 1.5f
            translationY = amplitude * (sin(t * 37f) + 0.5f * sin(t * 47f)) / 1.5f
            rotationZ = 1.2f * sin(t * 29f)
        }
    }

    // Painted by the text brush, see rainbowBrush().
    TextEffect.RAINBOW -> this
}
