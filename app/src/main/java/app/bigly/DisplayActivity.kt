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

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.bigly.data.AppSettings
import app.bigly.ui.display.DisplayScreen
import app.bigly.ui.display.OrientationLock
import app.bigly.ui.theme.BiglyTheme
import app.bigly.util.isReduceMotionEnabled
import kotlinx.coroutines.launch

/**
 * Full screen display of one message. Rotation is handled without recreating the
 * activity (see the manifest), and the remaining state is saveable.
 */
class DisplayActivity : ComponentActivity() {

    private var reduceMotion by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        hideSystemBars()

        val app = bigly
        val initialText = intent.getStringExtra(EXTRA_TEXT)

        setContent {
            val settings by app.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = null)
            val text by produceState<String?>(initialValue = initialText) {
                if (value == null) {
                    val latest = app.historyRepository.latest()?.text
                    if (latest == null) openHomeInstead() else value = latest
                }
            }
            var orientationLock by rememberSaveable { mutableStateOf(OrientationLock.AUTO) }

            LaunchedEffect(orientationLock) { applyOrientation(orientationLock) }

            val current = settings ?: return@setContent
            val message = text ?: return@setContent

            LaunchedEffect(current.keepScreenOn, current.maxBrightness) { applyWindowSettings(current) }

            BiglyTheme(themeMode = current.themeMode, dynamicColor = current.dynamicColor) {
                DisplayScreen(
                    text = message,
                    settings = current,
                    reduceMotion = reduceMotion,
                    orientationLock = orientationLock,
                    onOrientationLockChange = { orientationLock = it },
                    onEffectChange = { effect -> app.appScope.launch { app.settingsRepository.setEffect(effect) } },
                    onBack = { finish() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        reduceMotion = isReduceMotionEnabled()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Bars that were swiped in, or shown by a dialog, go away again.
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun applyWindowSettings(settings: AppSettings) {
        if (settings.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        window.attributes = window.attributes.apply {
            screenBrightness = if (settings.maxBrightness) {
                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            } else {
                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
    }

    private fun applyOrientation(lock: OrientationLock) {
        requestedOrientation = when (lock) {
            // Follows the sensor, unless the user locked rotation in the system.
            OrientationLock.AUTO -> ActivityInfo.SCREEN_ORIENTATION_FULL_USER
            OrientationLock.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
            OrientationLock.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        }
    }

    private fun openHomeInstead() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val EXTRA_TEXT = "app.bigly.extra.TEXT"

        /** Shows [text] full screen. */
        fun intent(context: Context, text: String): Intent =
            Intent(context, DisplayActivity::class.java).putExtra(EXTRA_TEXT, text)

        /** Shows the most recent message from the history (used by the widget). */
        fun showLastIntent(context: Context): Intent =
            Intent(context, DisplayActivity::class.java)
                .setAction(ACTION_SHOW_LAST)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        const val ACTION_SHOW_LAST = "app.bigly.action.SHOW_LAST"
    }
}
