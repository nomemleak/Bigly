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
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

/**
 * Long press shortcuts on the launcher icon. They are published at run time
 * (rather than in XML) so that they also work for the ".debug" build, and so
 * their labels follow the current language.
 */
object Shortcuts {

    const val ACTION_NEW_MESSAGE = "app.bigly.action.NEW_MESSAGE"
    const val ACTION_SHOW_LAST = "app.bigly.action.SHOW_LAST_MESSAGE"
    const val ACTION_HISTORY = "app.bigly.action.HISTORY"

    fun publish(context: Context) {
        val shortcuts = listOf(
            shortcut(context, "new_message", R.string.shortcut_new, R.string.shortcut_new_long, R.drawable.ic_shortcut_new, ACTION_NEW_MESSAGE),
            shortcut(context, "last_message", R.string.shortcut_last, R.string.shortcut_last_long, R.drawable.ic_shortcut_last, ACTION_SHOW_LAST),
            shortcut(context, "history", R.string.shortcut_history, R.string.shortcut_history_long, R.drawable.ic_shortcut_history, ACTION_HISTORY),
        )
        try {
            ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
        } catch (e: IllegalStateException) {
            // Rate limited by the system: the previous shortcuts stay in place.
        }
    }

    private fun shortcut(
        context: Context,
        id: String,
        shortLabel: Int,
        longLabel: Int,
        icon: Int,
        action: String,
    ): ShortcutInfoCompat =
        ShortcutInfoCompat.Builder(context, id)
            .setShortLabel(context.getString(shortLabel))
            .setLongLabel(context.getString(longLabel))
            .setIcon(IconCompat.createWithResource(context, icon))
            .setIntent(Intent(action, null, context, MainActivity::class.java))
            .build()
}
