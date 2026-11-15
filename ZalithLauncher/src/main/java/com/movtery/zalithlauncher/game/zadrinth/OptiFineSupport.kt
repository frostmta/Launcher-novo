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

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.gson.JsonParser
import com.movtery.zalithlauncher.game.download.addon.platform.modrinth.MODRINTH_API
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.utils.network.downloadFile
import com.movtery.zalithlauncher.utils.network.fetchStringFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * OptiFine junto com o Zadrinth Client.
 *
 * O Legacy OptiFabric (licença MPL-2.0) faz o OptiFine funcionar no Legacy
 * Fabric: o launcher baixa ele sozinho do Modrinth. O OptiFine em si não pode
 * vir dentro do app (a licença dele não deixa redistribuir), então o jogador
 * baixa no site oficial e escolhe o arquivo aqui; o launcher copia para a
 * pasta mods da instância do client.
 */
object OptiFineSupport {
    private const val OPTIFABRIC_PROJECT = "FCvC00y3"
    const val DOWNLOAD_PAGE = "https://optifine.net/downloads"

    private fun modsDir(version: Version) = File(version.getGameDir(), "mods")

    private fun jars(version: Version): List<File> =
        modsDir(version).listFiles()?.filter { it.isFile && it.name.endsWith(".jar", ignoreCase = true) } ?: emptyList()

    fun hasOptiFabric(version: Version) = jars(version).any { it.name.contains("optifabric", ignoreCase = true) }

    fun hasOptiFine(version: Version) = jars(version).any {
        it.name.contains("optifine", ignoreCase = true) && !it.name.contains("optifabric", ignoreCase = true)
    }

    /** Baixa o Legacy OptiFabric para 1.8.9 (Legacy Fabric). Devolve o nome do arquivo. */
    suspend fun installOptiFabric(version: Version): String = withContext(Dispatchers.IO) {
        val json = fetchStringFromUrl("$MODRINTH_API/project/$OPTIFABRIC_PROJECT/version")
        val match = JsonParser.parseString(json).asJsonArray.map { it.asJsonObject }.firstOrNull { item ->
            val loaders = item.getAsJsonArray("loaders").map { it.asString }
            val games = item.getAsJsonArray("game_versions").map { it.asString }
            "legacy-fabric" in loaders && ZadrinthClientMod.MINECRAFT_VERSION in games
        } ?: error("Não achei o OptiFabric para 1.8.9 no Modrinth")
        val files = match.getAsJsonArray("files").map { it.asJsonObject }
        val file = files.firstOrNull { it.get("primary")?.asBoolean == true } ?: files.first()
        val name = file.get("filename").asString
        val target = File(modsDir(version), name)
        target.parentFile?.mkdirs()
        //Tira versões antigas do OptiFabric para não carregar duas
        jars(version).filter { it.name.contains("optifabric", ignoreCase = true) && it.name != name }.forEach { it.delete() }
        downloadFile(file.get("url").asString, target)
        name
    }

    /** Copia o .jar do OptiFine escolhido pelo jogador para a pasta mods. */
    suspend fun importOptiFine(context: Context, uri: Uri, version: Version): String = withContext(Dispatchers.IO) {
        var name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: "OptiFine_1.8.9.jar"
        if (!name.endsWith(".jar", ignoreCase = true)) name += ".jar"
        if (!name.contains("optifine", ignoreCase = true)) name = "OptiFine_$name"
        val target = File(modsDir(version), name)
        target.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Não foi possível ler o arquivo")
        name
    }
}
