/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.instance

import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Resumo de uma instância para as telas (Início, Biblioteca, barra lateral).
 * @param metadata conteúdo do instance.json, se existir
 * @param modsCount quantidade de mods ativos (.jar) na pasta mods/
 */
data class InstanceStats(
    val metadata: InstanceMetadata?,
    val modsCount: Int,
) {
    val lastPlayed: Long get() = metadata?.lastPlayed ?: 0L
    val timePlayedSeconds: Long get() = metadata?.timePlayedSeconds ?: 0L
}

/**
 * Cache em memória com o resumo de todas as instâncias, lido fora da thread principal.
 */
object InstanceStatsStore {
    private val _stats = MutableStateFlow<Map<String, InstanceStats>>(emptyMap())
    val stats = _stats.asStateFlow()

    suspend fun refresh(versions: List<Version>) {
        val result = withContext(Dispatchers.IO) {
            versions.associate { version ->
                version.getVersionName() to InstanceStats(
                    metadata = InstanceMetadataStore.read(version),
                    modsCount = countMods(version)
                )
            }
        }
        _stats.value = result
    }

    /** Atualiza só uma instância (depois de ativar/desativar mods, por exemplo) */
    suspend fun refresh(version: Version) {
        val stats = withContext(Dispatchers.IO) {
            InstanceStats(
                metadata = InstanceMetadataStore.read(version),
                modsCount = countMods(version)
            )
        }
        _stats.value = _stats.value + (version.getVersionName() to stats)
    }

    private fun countMods(version: Version): Int {
        return runCatching {
            VersionFolders.MOD.getDir(version.getGameDir())
                .listFiles()
                ?.count { it.isFile && it.name.endsWith(".jar", ignoreCase = true) }
                ?: 0
        }.getOrDefault(0)
    }
}

/**
 * Instâncias ordenadas como "recentes": fixadas primeiro, depois a última jogada.
 */
fun List<Version>.sortedByRecent(stats: Map<String, InstanceStats>): List<Version> =
    sortedWith(
        compareByDescending<Version> { it.pinnedState }
            .thenByDescending { stats[it.getVersionName()]?.lastPlayed ?: 0L }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.getVersionName() }
    )
