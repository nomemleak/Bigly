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

package app.bigly.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Reads and writes [AppSettings] with DataStore. */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.applicationContext.settingsDataStore)

    val settings: Flow<AppSettings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map(::fromPreferences)

    suspend fun current(): AppSettings = settings.first()

    suspend fun setThemeMode(value: ThemeMode) = set(Keys.THEME_MODE, value.name)
    suspend fun setDynamicColor(value: Boolean) = set(Keys.DYNAMIC_COLOR, value)
    suspend fun setKeepScreenOn(value: Boolean) = set(Keys.KEEP_SCREEN_ON, value)
    suspend fun setMaxBrightness(value: Boolean) = set(Keys.MAX_BRIGHTNESS, value)
    suspend fun setDisplayMode(value: DisplayMode) = set(Keys.DISPLAY_MODE, value.name)
    suspend fun setScrollDirection(value: ScrollDirection) = set(Keys.SCROLL_DIRECTION, value.name)
    suspend fun setEffect(value: TextEffect) = set(Keys.EFFECT, value.name)
    suspend fun setHistorySize(value: Int) = set(Keys.HISTORY_SIZE, value)

    suspend fun setScrollSpeed(value: Int) =
        set(Keys.SCROLL_SPEED, value.coerceIn(AppSettings.MIN_SPEED, AppSettings.MAX_SPEED))

    suspend fun setEffectSpeed(value: Int) =
        set(Keys.EFFECT_SPEED, value.coerceIn(AppSettings.MIN_SPEED, AppSettings.MAX_SPEED))

    suspend fun setColors(text: Int, background: Int) {
        dataStore.edit {
            it[Keys.TEXT_COLOR] = text
            it[Keys.BACKGROUND_COLOR] = background
        }
    }

    suspend fun setTextColor(value: Int) = set(Keys.TEXT_COLOR, value)
    suspend fun setBackgroundColor(value: Int) = set(Keys.BACKGROUND_COLOR, value)

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    private fun fromPreferences(prefs: Preferences): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            themeMode = enumOrDefault(prefs[Keys.THEME_MODE], defaults.themeMode),
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: defaults.dynamicColor,
            textColor = prefs[Keys.TEXT_COLOR] ?: defaults.textColor,
            backgroundColor = prefs[Keys.BACKGROUND_COLOR] ?: defaults.backgroundColor,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: defaults.keepScreenOn,
            maxBrightness = prefs[Keys.MAX_BRIGHTNESS] ?: defaults.maxBrightness,
            displayMode = enumOrDefault(prefs[Keys.DISPLAY_MODE], defaults.displayMode),
            scrollSpeed = prefs[Keys.SCROLL_SPEED] ?: defaults.scrollSpeed,
            scrollDirection = enumOrDefault(prefs[Keys.SCROLL_DIRECTION], defaults.scrollDirection),
            effect = enumOrDefault(prefs[Keys.EFFECT], defaults.effect),
            effectSpeed = prefs[Keys.EFFECT_SPEED] ?: defaults.effectSpeed,
            historySize = prefs[Keys.HISTORY_SIZE] ?: defaults.historySize,
        )
    }

    private inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val TEXT_COLOR = intPreferencesKey("text_color")
        val BACKGROUND_COLOR = intPreferencesKey("background_color")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val MAX_BRIGHTNESS = booleanPreferencesKey("max_brightness")
        val DISPLAY_MODE = stringPreferencesKey("display_mode")
        val SCROLL_SPEED = intPreferencesKey("scroll_speed")
        val SCROLL_DIRECTION = stringPreferencesKey("scroll_direction")
        val EFFECT = stringPreferencesKey("effect")
        val EFFECT_SPEED = intPreferencesKey("effect_speed")
        val HISTORY_SIZE = intPreferencesKey("history_size")
    }
}
