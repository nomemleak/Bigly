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

package app.bigly.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Fallback palette for Android 8 to 11, or when Material You is turned off:
// near-monochrome surfaces with one calm blue accent.
private val Blue40 = Color(0xFF3E5BA9)
private val Blue80 = Color(0xFFB3C5FF)
private val Blue90 = Color(0xFFDBE1FF)
private val Blue20 = Color(0xFF002A77)
private val Blue30 = Color(0xFF244290)
private val Slate40 = Color(0xFF585E71)
private val Slate80 = Color(0xFFC0C6DC)
private val Slate90 = Color(0xFFDCE2F9)
private val Slate20 = Color(0xFF2A3042)
private val Slate30 = Color(0xFF414659)

val LightColors = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue20,
    secondary = Slate40,
    onSecondary = Color.White,
    secondaryContainer = Slate90,
    onSecondaryContainer = Slate20,
    background = Color(0xFFFBFBFD),
    onBackground = Color(0xFF1A1B20),
    surface = Color(0xFFFBFBFD),
    onSurface = Color(0xFF1A1B20),
    surfaceVariant = Color(0xFFE2E2EC),
    onSurfaceVariant = Color(0xFF45464F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F5F9),
    surfaceContainer = Color(0xFFEFEFF4),
    surfaceContainerHigh = Color(0xFFE9E9EE),
    surfaceContainerHighest = Color(0xFFE3E3E8),
    outline = Color(0xFF757680),
    outlineVariant = Color(0xFFC5C6D0),
)

val DarkColors = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Slate80,
    onSecondary = Slate20,
    secondaryContainer = Slate30,
    onSecondaryContainer = Slate90,
    background = Color(0xFF121316),
    onBackground = Color(0xFFE3E2E7),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE3E2E7),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    surfaceContainerLowest = Color(0xFF0D0E11),
    surfaceContainerLow = Color(0xFF1A1B1F),
    surfaceContainer = Color(0xFF1E1F23),
    surfaceContainerHigh = Color(0xFF292A2D),
    surfaceContainerHighest = Color(0xFF343538),
    outline = Color(0xFF8F909A),
    outlineVariant = Color(0xFF45464F),
)
