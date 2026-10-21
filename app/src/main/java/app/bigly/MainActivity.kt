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

package app.bigly

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.bigly.data.AppSettings
import app.bigly.data.ColorPreset
import app.bigly.data.DisplayMode
import app.bigly.data.HistoryEntry
import app.bigly.data.MessageStyle
import app.bigly.data.ScrollDirection
import app.bigly.data.SettingsRepository
import app.bigly.data.TextEffect
import app.bigly.data.ThemeMode
import app.bigly.data.toMessageStyle
import app.bigly.data.withStyle
import app.bigly.text.SharedText
import app.bigly.ui.home.HistoryScreen
import app.bigly.ui.home.HomeActions
import app.bigly.ui.home.HomeScreen
import app.bigly.ui.settings.SettingsActions
import app.bigly.ui.settings.SettingsScreen
import app.bigly.ui.theme.BiglyTheme
import app.bigly.util.isReduceMotionEnabled
import kotlinx.coroutines.launch

private enum class Screen { HOME, HISTORY, SETTINGS }

class MainActivity : ComponentActivity() {

    private val app get() = bigly

    /** Screen asked for by a shortcut, applied once by the UI. */
    private var requestedScreen by mutableStateOf<Screen?>(null)

    /** Text received from the share menu, waiting to fill the input field. */
    private var pendingInput by mutableStateOf<String?>(null)

    /** Set by the "New message" shortcut: focus the input field. */
    private var focusInput by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Shortcuts.publish(this)
        // After a rotation the intent was already handled.
        if (savedInstanceState == null) handleIntent(intent)

        val actions = RepositorySettingsActions(app.settingsRepository)

        setContent {
            val settings by app.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = null)
            val history by app.historyRepository.entries.collectAsStateWithLifecycle(initialValue = emptyList())
            val current = settings ?: return@setContent

            BiglyTheme(themeMode = current.themeMode, dynamicColor = current.dynamicColor) {
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                // Kept here, above the screens, so the message survives a trip to the settings.
                var inputText by rememberSaveable { mutableStateOf("") }
                BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

                LaunchedEffect(requestedScreen) {
                    requestedScreen?.let {
                        screen = it
                        requestedScreen = null
                    }
                }

                when (screen) {
                    Screen.HOME -> HomeScreen(
                        settings = current,
                        history = history,
                        actions = homeActions(current) { screen = it },
                        text = inputText,
                        onTextChange = { inputText = it },
                        prefill = pendingInput,
                        focusInput = focusInput,
                        onInputRequestHandled = {
                            pendingInput = null
                            focusInput = false
                        },
                    )
                    Screen.HISTORY -> HistoryScreen(
                        history = history,
                        onShow = { entry -> replay(entry, current) },
                        onDelete = { entry -> launchInApp { app.historyRepository.remove(entry.text) } },
                        onClearHistory = { launchInApp { app.historyRepository.clear() } },
                        onBack = { screen = Screen.HOME },
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        settings = current,
                        actions = actions,
                        reduceMotion = isReduceMotionEnabled(),
                        versionName = versionName(),
                        onBack = { screen = Screen.HOME },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") != true) return
                val shared = SharedText.process(
                    intent.getCharSequenceExtra(Intent.EXTRA_TEXT) ?: intent.clipData?.textAt(0),
                    intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT),
                ) ?: return
                requestedScreen = Screen.HOME
                pendingInput = shared
            }
            Shortcuts.ACTION_NEW_MESSAGE -> {
                requestedScreen = Screen.HOME
                focusInput = true
            }
            Shortcuts.ACTION_HISTORY -> requestedScreen = Screen.HISTORY
            Shortcuts.ACTION_SHOW_LAST -> startActivity(DisplayActivity.showLastIntent(this))
        }
    }

    private fun homeActions(settings: AppSettings, navigate: (Screen) -> Unit) = object : HomeActions {
        val repo = app.settingsRepository
        override fun show(text: String) = this@MainActivity.show(text, settings)
        override fun setDisplayMode(mode: DisplayMode) = launchInApp { repo.setDisplayMode(mode) }
        override fun setEffect(effect: TextEffect) = launchInApp { repo.setEffect(effect) }
        override fun applyPreset(preset: ColorPreset) = launchInApp { repo.setColors(preset.textColor, preset.backgroundColor) }
        override fun applyStyle(style: MessageStyle) = launchInApp { repo.applyStyle(style) }
        override fun setScrollSpeed(speed: Int) = launchInApp { repo.setScrollSpeed(speed) }
        override fun setScrollDirection(direction: ScrollDirection) = launchInApp { repo.setScrollDirection(direction) }
        override fun openHistory() = navigate(Screen.HISTORY)
        override fun openSettings() = navigate(Screen.SETTINGS)
    }

    private fun ClipData.textAt(index: Int): CharSequence? =
        if (itemCount > index) getItemAt(index).text else null

    /** Saves the message with its current look, then shows it. */
    private fun show(text: String, settings: AppSettings) {
        if (text.isBlank()) return
        launchInApp { app.historyRepository.add(text, settings.historySize, settings.toMessageStyle()) }
        startActivity(DisplayActivity.intent(this, text.trim()))
    }

    /** Shows a message from the history with the colors, effect and mode it was saved with. */
    private fun replay(entry: HistoryEntry, settings: AppSettings) {
        val style = entry.style ?: settings.toMessageStyle()
        lifecycleScope.launch {
            app.settingsRepository.applyStyle(style)
            show(entry.text, settings.withStyle(style))
        }
    }

    /** For writes that must finish even if the screen goes away. */
    private fun launchInApp(block: suspend () -> Unit) {
        app.appScope.launch { block() }
    }

    private fun versionName(): String =
        packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()

    private inner class RepositorySettingsActions(private val repo: SettingsRepository) : SettingsActions {
        override fun setThemeMode(value: ThemeMode) = launchInApp { repo.setThemeMode(value) }
        override fun setDynamicColor(value: Boolean) = launchInApp { repo.setDynamicColor(value) }
        override fun setTextColor(value: Int) = launchInApp { repo.setTextColor(value) }
        override fun setBackgroundColor(value: Int) = launchInApp { repo.setBackgroundColor(value) }
        override fun setColors(text: Int, background: Int) = launchInApp { repo.setColors(text, background) }
        override fun setKeepScreenOn(value: Boolean) = launchInApp { repo.setKeepScreenOn(value) }
        override fun setMaxBrightness(value: Boolean) = launchInApp { repo.setMaxBrightness(value) }
        override fun setScrollSpeed(value: Int) = launchInApp { repo.setScrollSpeed(value) }
        override fun setScrollDirection(value: ScrollDirection) = launchInApp { repo.setScrollDirection(value) }
        override fun setEffect(value: TextEffect) = launchInApp { repo.setEffect(value) }
        override fun setEffectSpeed(value: Int) = launchInApp { repo.setEffectSpeed(value) }
        override fun setHistorySize(value: Int) = launchInApp { repo.setHistorySize(value) }
    }
}
