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

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

/**
 * Metadados de uma instância isolada, salvos em `instance.json` na pasta raiz
 * da instância (a mesma pasta da versão, onde ficam mods/, config/, saves/...).
 *
 * O launcher grava o início de cada sessão (last_played) e soma o tempo
 * jogado a cada minuto enquanto o jogo está aberto (time_played).
 */
@Keep
data class InstanceMetadata(
    @SerializedName("name")
    val name: String? = null,
    /** Caminho do ícone, relativo à pasta da instância */
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("game_version")
    val gameVersion: String? = null,
    /** vanilla, fabric, forge, neoforge ou quilt */
    @SerializedName("loader")
    val loader: String? = null,
    @SerializedName("loader_version")
    val loaderVersion: String? = null,
    /** Tempo total jogado, em segundos */
    @SerializedName("time_played")
    val timePlayedSeconds: Long = 0L,
    /** Início da última sessão (epoch em milissegundos) */
    @SerializedName("last_played")
    val lastPlayed: Long? = null,
)

object InstanceMetadataStore {
    private const val TAG = "InstanceMetadata"
    const val FILE_NAME = "instance.json"

    private val lock = Any()

    fun getFile(version: Version): File = File(version.getVersionPath(), FILE_NAME)

    /**
     * Lê o `instance.json` da instância.
     * @return null se o arquivo não existir ou estiver inválido
     */
    fun read(version: Version): InstanceMetadata? {
        val file = getFile(version)
        if (!file.isFile) return null
        return runCatching {
            GSON.fromJson(file.readText(), InstanceMetadata::class.java)
        }.onFailure { e ->
            Logger.warning(TAG, "Unable to read ${file.absolutePath}", e)
        }.getOrNull()
    }

    /**
     * Atualiza o `instance.json` (cria se não existir), preenchendo nome,
     * versão do jogo e loader a partir da versão instalada.
     * Deve ser chamado fora da thread principal.
     */
    fun update(version: Version, transform: (InstanceMetadata) -> InstanceMetadata) {
        synchronized(lock) {
            runCatching {
                val current = read(version) ?: InstanceMetadata()
                val updated = transform(current.withVersionInfo(version))
                val file = getFile(version)
                file.parentFile?.mkdirs()
                val temp = File(file.parentFile, "$FILE_NAME.tmp")
                temp.writeText(GSON.toJson(updated))
                if (!temp.renameTo(file)) {
                    file.writeText(GSON.toJson(updated))
                    temp.delete()
                }
            }.onFailure { e ->
                Logger.warning(TAG, "Unable to write instance metadata for ${version.getVersionName()}", e)
            }
        }
    }

    /** Marca o início de uma sessão de jogo */
    fun markLaunched(version: Version) = update(version) {
        it.copy(lastPlayed = System.currentTimeMillis())
    }

    /** Soma tempo jogado (em segundos) */
    fun addPlayTime(version: Version, seconds: Long) = update(version) {
        it.copy(timePlayedSeconds = it.timePlayedSeconds + seconds)
    }

    private fun InstanceMetadata.withVersionInfo(version: Version): InstanceMetadata {
        val info = version.getVersionInfo()
        val primary = info?.primaryLoader
        return copy(
            name = this.name ?: version.getVersionName(),
            gameVersion = info?.minecraftVersion ?: this.gameVersion,
            loader = when {
                primary != null -> primary.loader.name.lowercase()
                info != null -> "vanilla"
                else -> this.loader
            },
            loaderVersion = primary?.version ?: this.loaderVersion,
        )
    }
}
