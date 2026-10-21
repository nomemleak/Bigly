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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bigly.R
import app.bigly.data.AppSettings
import app.bigly.data.ScrollDirection
import app.bigly.data.TextEffect
import app.bigly.data.ThemeMode
import app.bigly.ui.theme.DisplayTextStyle
import app.bigly.ui.theme.supportsDynamicColor
import app.bigly.util.ColorUtils
import kotlin.math.roundToInt

/** Every change is saved right away; there is no "save" button. */
interface SettingsActions {
    fun setThemeMode(value: ThemeMode)
    fun setDynamicColor(value: Boolean)
    fun setTextColor(value: Int)
    fun setBackgroundColor(value: Int)
    fun setColors(text: Int, background: Int)
    fun setKeepScreenOn(value: Boolean)
    fun setMaxBrightness(value: Boolean)
    fun setScrollSpeed(value: Int)
    fun setScrollDirection(value: ScrollDirection)
    fun setEffect(value: TextEffect)
    fun setEffectSpeed(value: Int)
    fun setHistorySize(value: Int)
}

private enum class ColorTarget { TEXT, BACKGROUND }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    actions: SettingsActions,
    reduceMotion: Boolean,
    versionName: String,
    onBack: () -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf<ColorTarget?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            SectionHeader(stringResource(R.string.settings_section_appearance))
            ChoiceRow(
                label = stringResource(R.string.settings_theme),
                options = listOf(
                    ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                    ThemeMode.LIGHT to stringResource(R.string.theme_light),
                    ThemeMode.DARK to stringResource(R.string.theme_dark),
                ),
                selected = settings.themeMode,
                onSelect = actions::setThemeMode,
            )
            if (supportsDynamicColor) {
                SwitchRow(
                    title = stringResource(R.string.settings_dynamic_color),
                    summary = stringResource(R.string.settings_dynamic_color_summary),
                    checked = settings.dynamicColor,
                    onCheckedChange = actions::setDynamicColor,
                )
            }

            SectionHeader(stringResource(R.string.settings_section_display))
            ColorPreview(settings.textColor, settings.backgroundColor)
            ColorRow(stringResource(R.string.settings_text_color), settings.textColor) { editing = ColorTarget.TEXT }
            ColorRow(stringResource(R.string.settings_background_color), settings.backgroundColor) {
                editing = ColorTarget.BACKGROUND
            }
            Text(
                text = stringResource(R.string.settings_presets),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { actions.setColors(AppSettings.BLACK, AppSettings.WHITE) },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.preset_black_on_white)) }
                OutlinedButton(
                    onClick = { actions.setColors(AppSettings.WHITE, AppSettings.BLACK) },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.preset_white_on_black)) }
            }
            Spacer(Modifier.height(8.dp))
            SwitchRow(
                title = stringResource(R.string.settings_keep_screen_on),
                summary = stringResource(R.string.settings_keep_screen_on_summary),
                checked = settings.keepScreenOn,
                onCheckedChange = actions::setKeepScreenOn,
            )
            SwitchRow(
                title = stringResource(R.string.settings_max_brightness),
                summary = stringResource(R.string.settings_max_brightness_summary),
                checked = settings.maxBrightness,
                onCheckedChange = actions::setMaxBrightness,
            )

            SectionHeader(stringResource(R.string.settings_section_scrolling))
            SpeedRow(stringResource(R.string.settings_scroll_speed), settings.scrollSpeed, actions::setScrollSpeed)
            ChoiceRow(
                label = stringResource(R.string.settings_scroll_direction),
                options = listOf(
                    ScrollDirection.RIGHT_TO_LEFT to stringResource(R.string.direction_rtl),
                    ScrollDirection.LEFT_TO_RIGHT to stringResource(R.string.direction_ltr),
                ),
                selected = settings.scrollDirection,
                onSelect = actions::setScrollDirection,
            )

            SectionHeader(stringResource(R.string.settings_section_effects))
            EffectChips(settings.effect, actions::setEffect)
            SpeedRow(stringResource(R.string.settings_effect_speed), settings.effectSpeed, actions::setEffectSpeed)
            if (reduceMotion) {
                Text(
                    text = stringResource(R.string.reduced_motion_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            SectionHeader(stringResource(R.string.settings_section_history))
            ChoiceRow(
                label = stringResource(R.string.settings_history_size),
                options = AppSettings.HISTORY_SIZES.map { it to it.toString() },
                selected = settings.historySize,
                onSelect = actions::setHistorySize,
            )

            SectionHeader(stringResource(R.string.settings_section_about))
            Text(
                text = stringResource(R.string.about_text, versionName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }

    editing?.let { target ->
        val isText = target == ColorTarget.TEXT
        ColorPickerDialog(
            title = stringResource(if (isText) R.string.settings_text_color else R.string.settings_background_color),
            initialColor = if (isText) settings.textColor else settings.backgroundColor,
            onDismiss = { editing = null },
            onConfirm = { color ->
                if (isText) actions.setTextColor(color) else actions.setBackgroundColor(color)
                editing = null
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
            .semantics { heading() },
    )
}

@Composable
private fun SwitchRow(title: String, summary: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    )
}

@Composable
private fun <T> ChoiceRow(label: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 8.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (value, text) ->
                SegmentedButton(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                ) { Text(text, maxLines = 1) }
            }
        }
    }
}

@Composable
private fun SpeedRow(label: String, value: Int, onChange: (Int) -> Unit) {
    var current by remember(value) { mutableFloatStateOf(value.toFloat()) }
    val valueText = stringResource(R.string.speed_value, current.roundToInt(), AppSettings.MAX_SPEED)
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(valueText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value = current,
            onValueChange = { current = it },
            onValueChangeFinished = { onChange(current.roundToInt()) },
            valueRange = AppSettings.MIN_SPEED.toFloat()..AppSettings.MAX_SPEED.toFloat(),
            steps = AppSettings.MAX_SPEED - AppSettings.MIN_SPEED - 1,
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = valueText
            },
        )
    }
}

@Composable
private fun ColorRow(title: String, argb: Int, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(ColorUtils.toHex(argb)) },
        trailingContent = {
            Box(
                Modifier
                    .size(40.dp)
                    .background(Color(argb), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun ColorPreview(textColor: Int, backgroundColor: Int) {
    val sample = stringResource(R.string.settings_preview_sample)
    val description = stringResource(R.string.settings_preview_description, sample)
    val ratio = ColorUtils.contrastRatio(textColor, backgroundColor)
    Column(Modifier.padding(horizontal = 16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(Color(backgroundColor), RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .semantics(mergeDescendants = true) { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Text(sample, style = DisplayTextStyle.copy(color = Color(textColor), fontSize = 48.sp))
        }
        if (ratio < ColorUtils.MIN_LARGE_TEXT_CONTRAST) {
            Row(
                Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text(
                    text = stringResource(R.string.contrast_low, "%.1f".format(ratio)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EffectChips(selected: TextEffect, onSelect: (TextEffect) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(stringResource(R.string.settings_effect), style = MaterialTheme.typography.bodyLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextEffect.entries.forEach { effect ->
                FilterChip(
                    selected = effect == selected,
                    onClick = { onSelect(effect) },
                    label = { Text(stringResource(effect.labelRes())) },
                )
            }
        }
    }
}

fun TextEffect.labelRes(): Int = when (this) {
    TextEffect.NONE -> R.string.effect_none
    TextEffect.BLINK -> R.string.effect_blink
    TextEffect.RAINBOW -> R.string.effect_rainbow
    TextEffect.SHAKE -> R.string.effect_shake
    TextEffect.PULSE -> R.string.effect_pulse
}
