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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.bigly.data.AppSettings
import app.bigly.data.DisplayMode
import app.bigly.data.ScrollDirection
import app.bigly.data.SettingsRepository
import app.bigly.data.TextEffect
import app.bigly.data.ThemeMode
import app.bigly.ui.home.HomeScreen
import app.bigly.ui.settings.SettingsActions
import app.bigly.ui.settings.SettingsScreen
import app.bigly.ui.theme.BiglyTheme
import app.bigly.util.isReduceMotionEnabled
import kotlinx.coroutines.launch

private enum class Screen { HOME, SETTINGS }

class MainActivity : ComponentActivity() {

    private val app get() = bigly

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val actions = RepositorySettingsActions(app.settingsRepository)

        setContent {
            val settings by app.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = null)
            val history by app.historyRepository.entries.collectAsStateWithLifecycle(initialValue = emptyList())
            val current = settings ?: return@setContent

            BiglyTheme(themeMode = current.themeMode, dynamicColor = current.dynamicColor) {
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                BackHandler(enabled = screen == Screen.SETTINGS) { screen = Screen.HOME }

                when (screen) {
                    Screen.HOME -> HomeScreen(
                        history = history,
                        displayMode = current.displayMode,
                        onDisplayModeChange = { mode -> launchInApp { app.settingsRepository.setDisplayMode(mode) } },
                        onShow = { text -> show(text, current) },
                        onDelete = { entry -> launchInApp { app.historyRepository.remove(entry.text) } },
                        onClearHistory = { launchInApp { app.historyRepository.clear() } },
                        onOpenSettings = { screen = Screen.SETTINGS },
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

    private fun show(text: String, settings: AppSettings) {
        if (text.isBlank()) return
        launchInApp { app.historyRepository.add(text, settings.historySize) }
        startActivity(DisplayActivity.intent(this, text.trim()))
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
