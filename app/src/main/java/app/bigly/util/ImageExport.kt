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

package app.bigly.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Writes exported images to the app's private cache and shares them through a
 * FileProvider: no storage permission is needed, and only the app receiving the
 * share gets temporary read access.
 */
object ImageExport {

    private const val DIRECTORY = "exports"

    suspend fun save(context: Context, image: ImageBitmap): Uri = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, DIRECTORY).apply { mkdirs() }
        // Keep only the latest export.
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "bigly-${System.currentTimeMillis()}.png")
        file.outputStream().use { out ->
            check(image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)) { "PNG encoding failed" }
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
