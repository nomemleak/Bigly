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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class HistoryRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private var now = 1000L

    private fun repository(file: File = File(folder.root, "history.txt")) =
        HistoryRepository(file, StandardTestDispatcher(), clock = { now++ })

    @Test
    fun startsEmptyWhenNoFile() = runTest {
        val repo = HistoryRepository(File(folder.root, "history.txt"), StandardTestDispatcher(testScheduler))
        assertEquals(emptyList<HistoryEntry>(), repo.entries.first())
        assertNull(repo.latest())
    }

    @Test
    fun addedEntriesArePersistedAcrossInstances() = runTest {
        val file = File(folder.root, "history.txt")
        val first = HistoryRepository(file, StandardTestDispatcher(testScheduler), clock = { now++ })
        first.add("hello", 20)
        first.add("world", 20)

        val second = HistoryRepository(file, StandardTestDispatcher(testScheduler))
        assertEquals(listOf("world", "hello"), second.entries.first().map { it.text })
        assertEquals("world", second.latest()?.text)
    }

    @Test
    fun removeAndClearArePersisted() = runTest {
        val file = File(folder.root, "history.txt")
        val repo = HistoryRepository(file, StandardTestDispatcher(testScheduler), clock = { now++ })
        repo.add("a", 20)
        repo.add("b", 20)
        repo.add("c", 20)
        repo.remove("b")
        assertEquals(listOf("c", "a"), HistoryRepository(file, StandardTestDispatcher(testScheduler)).load().map { it.text })

        repo.clear()
        assertTrue(HistoryRepository(file, StandardTestDispatcher(testScheduler)).load().isEmpty())
    }

    @Test
    fun respectsMaxSizeAndTrim() = runTest {
        val file = File(folder.root, "history.txt")
        val repo = HistoryRepository(file, StandardTestDispatcher(testScheduler), clock = { now++ })
        repeat(30) { repo.add("m$it", 20) }
        assertEquals(20, repo.load().size)

        repo.trim(5)
        assertEquals(listOf("m29", "m28", "m27", "m26", "m25"), HistoryRepository(file, StandardTestDispatcher(testScheduler)).load().map { it.text })
    }

    @Test
    fun flowEmitsUpdates() = runTest {
        val repo = HistoryRepository(File(folder.root, "history.txt"), StandardTestDispatcher(testScheduler), clock = { now++ })
        repo.add("x", 20)
        assertEquals(listOf("x"), repo.entries.first().map { it.text })
        repo.add("y", 20)
        assertEquals(listOf("y", "x"), repo.entries.first().map { it.text })
    }

    @Test
    fun noTemporaryFileLeftBehind() = runTest {
        val file = File(folder.root, "history.txt")
        val repo = HistoryRepository(file, StandardTestDispatcher(testScheduler), clock = { now++ })
        repo.add("x", 20)
        assertTrue(file.exists())
        assertFalse(File(folder.root, "history.txt.tmp").exists())
    }

    @Test
    fun corruptedFileGivesEmptyHistory() = runTest {
        val file = File(folder.root, "history.txt")
        file.writeText("garbage")
        val repo = HistoryRepository(file, StandardTestDispatcher(testScheduler), clock = { now++ })
        assertTrue(repo.load().isEmpty())
        repo.add("fresh", 20)
        assertEquals(listOf("fresh"), HistoryRepository(file, StandardTestDispatcher(testScheduler)).load().map { it.text })
    }

    @Test
    fun createsMissingParentDirectory() = runTest {
        val file = File(folder.root, "nested/dir/history.txt")
        val repo = HistoryRepository(file, StandardTestDispatcher(testScheduler), clock = { now++ })
        repo.add("x", 20)
        assertTrue(file.exists())
    }
}
