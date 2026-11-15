/*
 * Zadrinth Client — fork não oficial do Zalith Launcher 2
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

package com.movtery.zalithlauncher.game.zadrinth

import com.google.gson.JsonParser
import com.movtery.zalithlauncher.game.download.addon.platform.modrinth.MODRINTH_API
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.utils.network.downloadFile
import com.movtery.zalithlauncher.utils.network.fetchStringFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Sodium (licença Polyform Shield, distribuído pelo Modrinth) no Zadrinth
 * Client 1.21.11: o launcher baixa a versão certa direto do Modrinth para a
 * pasta mods da instância. É opcional porque nem todo renderizador de celular
 * aceita o Sodium; se o jogo não abrir, é só remover.
 */
object SodiumSupport {
    private const val PROJECT = "AANobbMI"

    private fun modsDir(version: Version) = File(version.getGameDir(), "mods")

    private fun jars(version: Version): List<File> =
        modsDir(version).listFiles()?.filter { it.isFile && it.name.endsWith(".jar", ignoreCase = true) && it.name.contains("sodium", ignoreCase = true) }
            ?: emptyList()

    fun isInstalled(version: Version) = jars(version).isNotEmpty()

    fun remove(version: Version) {
        jars(version).forEach { it.delete() }
    }

    /** Baixa o Sodium para a versão da instância. Devolve o nome do arquivo. */
    suspend fun install(version: Version): String = withContext(Dispatchers.IO) {
        val mc = version.getVersionInfo()?.minecraftVersion ?: ClientTarget.V1_21_11.minecraft
        val json = fetchStringFromUrl("$MODRINTH_API/project/$PROJECT/version")
        val match = JsonParser.parseString(json).asJsonArray.map { it.asJsonObject }.firstOrNull { item ->
            val loaders = item.getAsJsonArray("loaders").map { it.asString }
            val games = item.getAsJsonArray("game_versions").map { it.asString }
            "fabric" in loaders && mc in games
        } ?: error("Não achei o Sodium para $mc no Modrinth")
        val files = match.getAsJsonArray("files").map { it.asJsonObject }
        val file = files.firstOrNull { it.get("primary")?.asBoolean == true } ?: files.first()
        val name = file.get("filename").asString
        val target = File(modsDir(version), name)
        target.parentFile?.mkdirs()
        remove(version)
        downloadFile(file.get("url").asString, target)
        name
    }
}
