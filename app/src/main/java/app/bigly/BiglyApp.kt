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

import android.app.Application
import android.content.Context
import app.bigly.data.HistoryRepository
import app.bigly.data.SettingsRepository
import app.bigly.widget.BiglyWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class BiglyApp : Application() {

    /** Outlives screens, so writes started from the UI are never cancelled halfway. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settingsRepository by lazy { SettingsRepository(this) }
    val historyRepository by lazy { HistoryRepository(File(filesDir, "history.txt")) }

    override fun onCreate() {
        super.onCreate()
        // Keep the home screen widget in sync with the latest message.
        appScope.launch {
            historyRepository.entries
                .map { it.firstOrNull()?.text }
                .distinctUntilChanged()
                .drop(1)
                .collect { BiglyWidgetProvider.updateAll(this@BiglyApp) }
        }
        // Apply a smaller history limit as soon as it is chosen.
        appScope.launch {
            settingsRepository.settings
                .map { it.historySize }
                .distinctUntilChanged()
                .collect { historyRepository.trim(it) }
        }
    }
}

val Context.bigly: BiglyApp get() = applicationContext as BiglyApp
