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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.bigly.R
import app.bigly.data.TextEffect
import app.bigly.ui.settings.labelRes

enum class OrientationLock { AUTO, PORTRAIT, LANDSCAPE }

fun OrientationLock.next(): OrientationLock = when (this) {
    OrientationLock.AUTO -> OrientationLock.PORTRAIT
    OrientationLock.PORTRAIT -> OrientationLock.LANDSCAPE
    OrientationLock.LANDSCAPE -> OrientationLock.AUTO
}

/** Small bar shown on top of the text: back, effect, orientation lock and, when scrolling, pause. */
@Composable
fun DisplayControls(
    visible: Boolean,
    effect: TextEffect,
    onEffectChange: (TextEffect) -> Unit,
    orientationLock: OrientationLock,
    onOrientationLockChange: (OrientationLock) -> Unit,
    showPause: Boolean,
    paused: Boolean,
    onPausedChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        // Dark translucent bar with white icons: readable on any text and background colors.
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.Black.copy(alpha = 0.72f),
            contentColor = Color.White,
            modifier = Modifier.padding(16.dp),
        ) {
            Row(Modifier.padding(horizontal = 4.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }

                var menuOpen by rememberSaveable { mutableStateOf(false) }
                Box {
                    val effectName = stringResource(effect.labelRes())
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            painterResource(R.drawable.ic_effect),
                            contentDescription = stringResource(R.string.control_effect, effectName),
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        TextEffect.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.labelRes())) },
                                leadingIcon = {
                                    if (option == effect) Icon(Icons.Filled.Check, contentDescription = null)
                                },
                                onClick = {
                                    menuOpen = false
                                    onEffectChange(option)
                                },
                            )
                        }
                    }
                }

                IconButton(onClick = { onOrientationLockChange(orientationLock.next()) }) {
                    val (icon, description) = when (orientationLock) {
                        OrientationLock.AUTO -> R.drawable.ic_orientation_auto to R.string.control_orientation_auto
                        OrientationLock.PORTRAIT -> R.drawable.ic_orientation_portrait to R.string.control_orientation_portrait
                        OrientationLock.LANDSCAPE -> R.drawable.ic_orientation_landscape to R.string.control_orientation_landscape
                    }
                    Icon(painterResource(icon), contentDescription = stringResource(description))
                }

                if (showPause) {
                    IconButton(onClick = { onPausedChange(!paused) }) {
                        if (paused) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.control_resume))
                        } else {
                            Icon(painterResource(R.drawable.ic_pause), contentDescription = stringResource(R.string.control_pause))
                        }
                    }
                }
            }
        }
    }
}
