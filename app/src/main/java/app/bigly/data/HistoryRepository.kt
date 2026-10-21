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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Stores the history in a private file of the app. Nothing ever leaves the device.
 *
 * Writes go to a temporary file first and are then renamed over the real one, so
 * a crash in the middle of a write never leaves a half-written history behind.
 */
class HistoryRepository(
    private val file: File,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val mutex = Mutex()
    private val state = MutableStateFlow<List<HistoryEntry>?>(null)

    /** Newest entry first. Loads the file on first collection. */
    val entries: Flow<List<HistoryEntry>> = state
        .onStart { load() }
        .filterNotNull()

    suspend fun load(): List<HistoryEntry> {
        state.value?.let { return it }
        return mutex.withLock {
            state.value ?: read().also { state.value = it }
        }
    }

    suspend fun latest(): HistoryEntry? = load().firstOrNull()

    suspend fun add(text: String, maxSize: Int, style: MessageStyle? = null): List<HistoryEntry> =
        update { HistoryList.add(it, text, clock(), maxSize, style) }

    suspend fun updateStyle(text: String, style: MessageStyle): List<HistoryEntry> =
        update { HistoryList.updateStyle(it, text, style) }

    suspend fun remove(text: String): List<HistoryEntry> =
        update { HistoryList.remove(it, text) }

    suspend fun clear(): List<HistoryEntry> = update { emptyList() }

    suspend fun trim(maxSize: Int): List<HistoryEntry> =
        update { HistoryList.trim(it, maxSize) }

    private suspend fun update(
        transform: (List<HistoryEntry>) -> List<HistoryEntry>,
    ): List<HistoryEntry> = mutex.withLock {
        val current = state.value ?: read()
        val next = transform(current)
        if (next != current) write(next)
        state.value = next
        next
    }

    private suspend fun read(): List<HistoryEntry> = withContext(ioDispatcher) {
        try {
            if (file.exists()) HistoryCodec.decode(file.readText()) else emptyList()
        } catch (e: IOException) {
            emptyList()
        }
    }

    private suspend fun write(entries: List<HistoryEntry>) = withContext(ioDispatcher) {
        val dir = file.absoluteFile.parentFile
        dir?.mkdirs()
        val tmp = File(dir, file.name + ".tmp")
        tmp.writeText(HistoryCodec.encode(entries))
        if (!tmp.renameTo(file)) {
            tmp.copyTo(file, overwrite = true)
            tmp.delete()
        }
    }
}
