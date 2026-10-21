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

package app.bigly.util

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/** Color helpers working on plain ARGB integers, so they run in unit tests. */
object ColorUtils {

    /** WCAG 2 recommends at least 4.5:1 for text; 3:1 is the floor for very large text. */
    const val MIN_LARGE_TEXT_CONTRAST = 3.0

    fun red(argb: Int) = (argb shr 16) and 0xFF
    fun green(argb: Int) = (argb shr 8) and 0xFF
    fun blue(argb: Int) = argb and 0xFF

    fun rgb(red: Int, green: Int, blue: Int): Int =
        (0xFF shl 24) or
            (red.coerceIn(0, 255) shl 16) or
            (green.coerceIn(0, 255) shl 8) or
            blue.coerceIn(0, 255)

    /** Relative luminance as defined by WCAG 2, between 0 (black) and 1 (white). */
    fun relativeLuminance(argb: Int): Double {
        fun channel(value: Int): Double {
            val c = value / 255.0
            return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(red(argb)) +
            0.7152 * channel(green(argb)) +
            0.0722 * channel(blue(argb))
    }

    /** Contrast ratio between two opaque colors, from 1 to 21. */
    fun contrastRatio(first: Int, second: Int): Double {
        val a = relativeLuminance(first)
        val b = relativeLuminance(second)
        return (max(a, b) + 0.05) / (min(a, b) + 0.05)
    }

    /** "#RRGGBB", alpha is ignored. */
    fun toHex(argb: Int): String = "#%06X".format(argb and 0xFFFFFF)

    /** Accepts "#RGB", "#RRGGBB" or the same without "#". Returns an opaque color or null. */
    fun parseHex(input: String): Int? {
        val hex = input.trim().removePrefix("#")
        if (hex.isEmpty() || hex.any { it.digitToIntOrNull(16) == null }) return null
        val full = when (hex.length) {
            3 -> hex.map { "$it$it" }.joinToString("")
            6 -> hex
            else -> return null
        }
        return (0xFF000000.toInt()) or full.toInt(16)
    }

    /** [hue] in degrees (0..360), [saturation] and [value] in 0..1. */
    fun hsvToColor(hue: Float, saturation: Float, value: Float): Int {
        val h = ((hue % 360f) + 360f) % 360f
        val s = saturation.coerceIn(0f, 1f)
        val v = value.coerceIn(0f, 1f)
        val c = v * s
        val x = c * (1 - abs((h / 60f) % 2f - 1))
        val m = v - c
        val (r, g, b) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return rgb(
            ((r + m) * 255f).roundToInt(),
            ((g + m) * 255f).roundToInt(),
            ((b + m) * 255f).roundToInt(),
        )
    }

    /** Returns hue (0..360), saturation (0..1) and value (0..1). */
    fun colorToHsv(argb: Int): FloatArray {
        val r = red(argb) / 255f
        val g = green(argb) / 255f
        val b = blue(argb) / 255f
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val delta = maxC - minC
        val hue = when {
            delta == 0f -> 0f
            maxC == r -> 60f * (((g - b) / delta) % 6f)
            maxC == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0f) it + 360f else it }
        val saturation = if (maxC == 0f) 0f else delta / maxC
        return floatArrayOf(hue, saturation, maxC)
    }
}
