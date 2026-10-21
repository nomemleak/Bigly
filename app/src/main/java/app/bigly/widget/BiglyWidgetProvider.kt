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

package app.bigly.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.bigly.DisplayActivity
import app.bigly.R
import app.bigly.bigly
import kotlinx.coroutines.launch

/** Home screen widget: shows the last message, and a tap opens it full screen. */
class BiglyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        val app = context.bigly
        app.appScope.launch {
            try {
                val text = app.historyRepository.latest()?.text
                val settings = app.settingsRepository.current()
                val views = views(context, text, settings.textColor, settings.backgroundColor)
                ids.forEach { manager.updateAppWidget(it, views) }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {

        /** Asks every placed widget to refresh. Does nothing when none is placed. */
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, BiglyWidgetProvider::class.java))
            if (ids.isEmpty()) return
            context.sendBroadcast(
                Intent(context, BiglyWidgetProvider::class.java)
                    .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids),
            )
        }

        private fun views(context: Context, text: String?, textColor: Int, backgroundColor: Int): RemoteViews {
            val open = PendingIntent.getActivity(
                context,
                0,
                DisplayActivity.showLastIntent(context),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val description = if (text == null) {
                context.getString(R.string.widget_open_app)
            } else {
                context.getString(R.string.widget_open_last, text)
            }
            return RemoteViews(context.packageName, R.layout.widget_bigly).apply {
                setTextViewText(R.id.widget_text, text ?: context.getString(R.string.widget_placeholder))
                setTextColor(R.id.widget_text, textColor)
                setInt(R.id.widget_background, "setColorFilter", backgroundColor)
                setContentDescription(R.id.widget_root, description)
                setOnClickPendingIntent(R.id.widget_root, open)
            }
        }
    }
}
