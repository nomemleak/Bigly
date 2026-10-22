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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.bigly.R
import app.bigly.data.AppSettings
import app.bigly.data.ColorPreset
import app.bigly.data.DisplayMode
import app.bigly.data.TextEffect
import app.bigly.ui.theme.DisplayTextStyle
import kotlin.math.max

private val TextPadding = 12.dp

/**
 * The full screen message. A tap anywhere shows or hides the controls.
 * All transient state is saveable, so it survives rotation and process death.
 */
@Composable
fun DisplayScreen(
    text: String,
    settings: AppSettings,
    reduceMotion: Boolean,
    orientationLock: OrientationLock,
    onOrientationClick: () -> Unit,
    onEffectChange: (TextEffect) -> Unit,
    onPresetChange: (ColorPreset) -> Unit,
    onExport: (ImageBitmap) -> Unit,
    onBack: () -> Unit,
) {
    var controlsVisible by rememberSaveable { mutableStateOf(false) }
    var paused by rememberSaveable { mutableStateOf(false) }
    var contentSize by remember { mutableStateOf(IntSize.Zero) }

    val scrolling = settings.displayMode == DisplayMode.SCROLLING
    val animateEffect = !reduceMotion && settings.effect != TextEffect.NONE
    val effectClock = rememberAnimationClock(running = animateEffect)
    val scrollClock = rememberAnimationClock(running = scrolling && !paused)
    val tempo = AppSettings.effectTempo(settings.effectSpeed)

    val style = DisplayTextStyle.copy(color = Color(settings.textColor))
    val brush: (() -> Brush?)? = if (settings.effect == TextEffect.RAINBOW) {
        { rainbowBrush(effectClock.value, tempo, animate = !reduceMotion) }
    } else {
        null
    }

    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    // The camera cutout is kept clear on both sides (and top and bottom), so the
    // text stays centered on the physical screen instead of drifting away from the notch.
    val cutout = WindowInsets.displayCutout
    val cutoutHorizontal = with(density) {
        max(cutout.getLeft(density, layoutDirection), cutout.getRight(density, layoutDirection)).toDp()
    }
    val cutoutVertical = with(density) { max(cutout.getTop(density), cutout.getBottom(density)).toDp() }

    val toggleLabel = stringResource(if (controlsVisible) R.string.display_hide_controls else R.string.display_show_controls)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(settings.backgroundColor))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = toggleLabel,
            ) { controlsVisible = !controlsVisible },
    ) {
        val content = Modifier
            .fillMaxSize()
            .padding(horizontal = cutoutHorizontal, vertical = cutoutVertical)
            .onSizeChanged { contentSize = it }
            .textEffect(
                effect = settings.effect,
                clock = effectClock,
                tempo = tempo,
                animate = !reduceMotion,
            )

        if (scrolling) {
            MarqueeText(
                text = text,
                style = style,
                speed = settings.scrollSpeed,
                direction = settings.scrollDirection,
                clock = scrollClock,
                modifier = content,
                brush = brush,
            )
        } else {
            FitText(text = text, style = style, modifier = content.padding(TextPadding), brush = brush)
        }

        DisplayControls(
            visible = controlsVisible,
            effect = settings.effect,
            onEffectChange = onEffectChange,
            settings = settings,
            onPresetChange = onPresetChange,
            orientationLock = orientationLock,
            onOrientationClick = onOrientationClick,
            showPause = scrolling,
            paused = paused,
            onPausedChange = { paused = it },
            onExport = {
                val image = MessageImage.render(
                    text = text,
                    settings = settings,
                    widthPx = contentSize.width,
                    heightPx = contentSize.height,
                    paddingPx = with(density) { TextPadding.roundToPx() },
                    density = density,
                    layoutDirection = layoutDirection,
                    measurer = measurer,
                    style = style,
                )
                onExport(image)
            },
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.displayCutout),
        )
    }
}
