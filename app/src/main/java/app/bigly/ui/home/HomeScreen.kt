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

package app.bigly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bigly.R
import app.bigly.data.AppSettings
import app.bigly.data.ColorPreset
import app.bigly.data.DisplayMode
import app.bigly.data.HistoryEntry
import app.bigly.data.MessageStyle
import app.bigly.data.ScrollDirection
import app.bigly.data.TextEffect
import app.bigly.ui.display.rainbowBrush
import app.bigly.ui.settings.labelRes
import app.bigly.ui.theme.DisplayTextStyle
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** What the home screen can ask for. Option changes are saved right away. */
interface HomeActions {
    fun show(text: String)
    fun setDisplayMode(mode: DisplayMode)
    fun setEffect(effect: TextEffect)
    fun applyPreset(preset: ColorPreset)
    fun applyStyle(style: MessageStyle)
    fun setScrollSpeed(speed: Int)
    fun setScrollDirection(direction: ScrollDirection)
    fun openHistory()
    fun openSettings()
}

/**
 * Home: a big input area that previews the message in its final colors and font,
 * then, one tap away, the color themes, the display mode, the effect and "Show".
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    settings: AppSettings,
    history: List<HistoryEntry>,
    actions: HomeActions,
    text: String,
    onTextChange: (String) -> Unit,
    prefill: String? = null,
    focusInput: Boolean = false,
    onInputRequestHandled: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    // Text shared from another app, or the "New message" shortcut.
    LaunchedEffect(prefill, focusInput) {
        if (prefill == null && !focusInput) return@LaunchedEffect
        if (prefill != null) onTextChange(prefill)
        focusRequester.requestFocus()
        onInputRequestHandled()
    }

    val submit = { if (text.isNotBlank()) actions.show(text) }
    val typing = WindowInsets.isImeVisible

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = actions::openHistory) {
                        Icon(painterResource(R.drawable.ic_history), contentDescription = stringResource(R.string.history_screen_title))
                    }
                    IconButton(onClick = actions::openSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(PaddingValues(top = padding.calculateTopPadding()))
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PreviewInput(
                text = text,
                onTextChange = onTextChange,
                settings = settings,
                focusRequester = focusRequester,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 120.dp)
                    .fillMaxWidth(),
            )

            if (!typing && history.isNotEmpty()) {
                RecentRow(history) { entry ->
                    onTextChange(entry.text)
                    entry.style?.let(actions::applyStyle)
                }
            }

            ThemeRow(settings = settings, onSelect = actions::applyPreset, onMore = actions::openSettings)

            ModeRow(settings.displayMode, actions::setDisplayMode)
            EffectRow(settings.effect, actions::setEffect)
            if (!typing && settings.displayMode == DisplayMode.SCROLLING) {
                ScrollRow(settings, actions)
            }

            Button(
                onClick = submit,
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_show), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.size(4.dp))
        }
    }
}

/**
 * The input field doubles as a preview: chosen background, text color, heavy
 * font, and a size that shrinks as the message grows.
 */
@Composable
private fun PreviewInput(
    text: String,
    onTextChange: (String) -> Unit,
    settings: AppSettings,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val textColor = Color(settings.textColor)
    val shape = RoundedCornerShape(24.dp)
    val label = stringResource(R.string.home_input_label)

    BoxWithConstraints(
        modifier
            .background(Color(settings.backgroundColor), shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        val fontSize = previewFontSize(text, maxWidth.value, maxHeight.value)
        val style = DisplayTextStyle.copy(color = textColor, fontSize = fontSize.sp, lineHeight = (fontSize * 1.15f).sp)
        val brush = if (settings.effect == TextEffect.RAINBOW) rainbowBrush(0f, 1f, animate = false) else null

        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            textStyle = if (brush != null) style.copy(brush = brush) else style,
            cursorBrush = SolidColor(textColor),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .semantics { contentDescription = label },
            decorationBox = { inner ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.home_input_placeholder),
                            style = style.copy(color = textColor.copy(alpha = 0.45f)),
                        )
                    }
                    inner()
                }
            },
        )

        if (text.isNotEmpty()) {
            IconButton(
                onClick = { onTextChange("") },
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = stringResource(R.string.action_clear_text),
                    tint = textColor,
                )
            }
        }
    }
}

/** Rough fit for the preview: big for a word, smaller for a sentence. */
private fun previewFontSize(text: String, widthDp: Float, heightDp: Float): Float {
    val lines = text.ifEmpty { "Hello" }.lines()
    val longest = max(1, lines.maxOf { it.length })
    val byWidth = (widthDp - 40f) / (0.62f * longest)
    val wrapped = max(lines.size, (longest * 0.62f * 56f / (widthDp - 40f)).toInt())
    val byHeight = (heightDp - 32f) / (1.2f * max(1, min(wrapped, 6)))
    return min(72f, min(byWidth, byHeight)).coerceAtLeast(24f)
}

@Composable
private fun RecentRow(history: List<HistoryEntry>, onPick: (HistoryEntry) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(history.take(10), key = { it.text }) { entry ->
            SuggestionChip(
                onClick = { onPick(entry) },
                icon = entry.style?.let { style -> @Composable { StyleSwatch(style, Modifier.size(20.dp)) } },
                label = {
                    Text(
                        text = entry.text.replace('\n', ' '),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 160.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun ThemeRow(settings: AppSettings, onSelect: (ColorPreset) -> Unit, onMore: () -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(ColorPreset.entries) { preset ->
            val selected = preset.matches(settings)
            val name = stringResource(preset.labelRes())
            Box(
                Modifier
                    .size(48.dp)
                    .background(Color(preset.backgroundColor), CircleShape)
                    .border(
                        width = if (selected) 3.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                    )
                    .selectable(selected = selected, role = Role.RadioButton) { onSelect(preset) }
                    .semantics { contentDescription = name },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Aa",
                    style = DisplayTextStyle.copy(color = Color(preset.textColor), fontSize = 16.sp),
                    modifier = Modifier.clearAndSetSemantics { },
                )
            }
        }
        item {
            IconButton(onClick = onMore, modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.ic_palette), contentDescription = stringResource(R.string.colors_more))
            }
        }
    }
}

@Composable
private fun ModeRow(mode: DisplayMode, onChange: (DisplayMode) -> Unit) {
    val modes = listOf(
        DisplayMode.FIXED to stringResource(R.string.mode_fixed),
        DisplayMode.SCROLLING to stringResource(R.string.mode_scrolling),
    )
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = mode == value,
                onClick = { onChange(value) },
                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
            ) { Text(label) }
        }
    }
}

@Composable
private fun EffectRow(effect: TextEffect, onChange: (TextEffect) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(TextEffect.entries) { option ->
            FilterChip(
                selected = option == effect,
                onClick = { onChange(option) },
                label = { Text(stringResource(option.labelRes())) },
            )
        }
    }
}

@Composable
private fun ScrollRow(settings: AppSettings, actions: HomeActions) {
    var speed by remember(settings.scrollSpeed) { mutableFloatStateOf(settings.scrollSpeed.toFloat()) }
    val speedLabel = stringResource(R.string.settings_scroll_speed)
    val speedValue = stringResource(R.string.speed_value, speed.roundToInt(), AppSettings.MAX_SPEED)
    val rtl = settings.scrollDirection == ScrollDirection.RIGHT_TO_LEFT
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = {
                actions.setScrollDirection(if (rtl) ScrollDirection.LEFT_TO_RIGHT else ScrollDirection.RIGHT_TO_LEFT)
            },
        ) {
            Icon(
                painterResource(if (rtl) R.drawable.ic_arrow_left else R.drawable.ic_arrow_right),
                contentDescription = stringResource(R.string.settings_scroll_direction) + ": " +
                    stringResource(if (rtl) R.string.direction_rtl else R.string.direction_ltr),
            )
        }
        Slider(
            value = speed,
            onValueChange = { speed = it },
            onValueChangeFinished = { actions.setScrollSpeed(speed.roundToInt()) },
            valueRange = AppSettings.MIN_SPEED.toFloat()..AppSettings.MAX_SPEED.toFloat(),
            steps = AppSettings.MAX_SPEED - AppSettings.MIN_SPEED - 1,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = speedLabel
                    stateDescription = speedValue
                },
        )
    }
}
