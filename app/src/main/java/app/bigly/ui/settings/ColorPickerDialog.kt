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

package app.bigly.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.bigly.R
import app.bigly.util.ColorUtils

/** Palette plus free choice (hue, saturation, brightness sliders and a hex field). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    title: String,
    initialColor: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val initialHsv = remember(initialColor) { ColorUtils.colorToHsv(initialColor) }
    var hue by rememberSaveable { mutableFloatStateOf(initialHsv[0]) }
    var saturation by rememberSaveable { mutableFloatStateOf(initialHsv[1]) }
    var brightness by rememberSaveable { mutableFloatStateOf(initialHsv[2]) }
    var hexText by rememberSaveable { mutableStateOf(ColorUtils.toHex(initialColor)) }

    val color = ColorUtils.hsvToColor(hue, saturation, brightness)
    val hexValid = ColorUtils.parseHex(hexText) != null

    fun select(argb: Int) {
        val hsv = ColorUtils.colorToHsv(argb)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
        hexText = ColorUtils.toHex(argb)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val hex = ColorUtils.toHex(color)
                val currentDescription = stringResource(R.string.color_current, hex)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(color))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .semantics { contentDescription = currentDescription },
                )

                Text(stringResource(R.string.color_palette), style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Palette.forEach { named ->
                        Swatch(
                            argb = named.argb,
                            label = stringResource(named.name),
                            selected = named.argb == color,
                            onClick = { select(named.argb) },
                        )
                    }
                }

                Text(stringResource(R.string.color_custom), style = MaterialTheme.typography.labelLarge)
                LabeledSlider(stringResource(R.string.color_hue), hue, 0f..360f) {
                    hue = it
                    hexText = ColorUtils.toHex(ColorUtils.hsvToColor(hue, saturation, brightness))
                }
                LabeledSlider(stringResource(R.string.color_saturation), saturation, 0f..1f) {
                    saturation = it
                    hexText = ColorUtils.toHex(ColorUtils.hsvToColor(hue, saturation, brightness))
                }
                LabeledSlider(stringResource(R.string.color_brightness), brightness, 0f..1f) {
                    brightness = it
                    hexText = ColorUtils.toHex(ColorUtils.hsvToColor(hue, saturation, brightness))
                }
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { input ->
                        hexText = input.take(7)
                        ColorUtils.parseHex(hexText)?.let { parsed ->
                            val hsv = ColorUtils.colorToHsv(parsed)
                            hue = hsv[0]
                            saturation = hsv[1]
                            brightness = hsv[2]
                        }
                    },
                    label = { Text(stringResource(R.string.color_hex)) },
                    isError = !hexValid,
                    supportingText = if (hexValid) null else {
                        { Text(stringResource(R.string.color_hex_invalid)) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(color) }) { Text(stringResource(R.string.action_apply)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun Swatch(argb: Int, label: String, selected: Boolean, onClick: () -> Unit) {
    val checkColor = if (ColorUtils.contrastRatio(argb, 0xFFFFFFFF.toInt()) >= 3.0) Color.White else Color.Black
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(argb))
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = checkColor)
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.semantics { contentDescription = label },
        )
    }
}
