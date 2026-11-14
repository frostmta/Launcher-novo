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
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

/**
 * O Zadrinth Client que roda dentro do jogo (client-mod/, Minecraft 1.8.9).
 *
 * O CI compila o client e coloca o .jar dentro do APK (assets/client). Ele
 * NÃO vai para a pasta mods: ao abrir uma instância compatível, o launcher
 * extrai o .jar para a pasta interna do app e injeta no jogo com
 * -Dfabric.addMods, junto com -Dzadrinth.home (pasta dos cosméticos, dividida
 * com a Loja do launcher). Assim o client faz parte do launcher, como no
 * Lunar, e não aparece como um mod.
 */
object ZadrinthClientMod {
    private const val TAG = "ZadrinthClientMod"
    private const val ASSET_PATH = "client/zadrinth-client.jar"
    const val FILE_NAME = "zadrinth-client.jar"
    const val MINECRAFT_VERSION = "1.8.9"

    /** Nome da instância criada pelo botão "Instalar Zadrinth Client" */
    const val INSTANCE_NAME = "Zadrinth Client 1.8.9"

    /** Pasta interna do Zadrinth (client extraído + cosmetics.json) */
    val homeDir: File get() = File(PathManager.DIR_FILES_PRIVATE, "zadrinth")

    private val clientJar: File get() = File(homeDir, "client/$FILE_NAME")

    /** A instância roda o Zadrinth Client? (1.8.9 + Legacy Fabric) */
    fun isSupported(version: Version): Boolean {
        val info = version.getVersionInfo() ?: return false
        return info.minecraftVersion == MINECRAFT_VERSION && info.hasLoader(ModLoader.LEGACY_FABRIC)
    }

    /**
     * Prepara o client para abrir esta instância.
     * @return o .jar para injetar com -Dfabric.addMods, ou null (instância sem suporte)
     */
    fun prepare(context: Context, version: Version): File? {
        if (!isSupported(version)) return null
        return runCatching {
            val modsDir = File(version.getGameDir(), "mods")
            if (File(modsDir, "$FILE_NAME.disabled").exists()) {
                Logger.info(TAG, "Zadrinth Client disabled for ${version.getVersionName()}")
                return null
            }
            //Versões antigas (0.5/0.6) copiavam o client para mods/: tira para não carregar duas vezes
            File(modsDir, FILE_NAME).takeIf { it.isFile }?.let { old ->
                if (old.delete()) Logger.info(TAG, "Removed old copy from mods/")
            }

            val bytes = context.assets.open(ASSET_PATH).use { it.readBytes() }
            val target = clientJar
            target.parentFile?.mkdirs()
            val upToDate = target.isFile && target.length() == bytes.size.toLong() && target.readBytes().contentEquals(bytes)
            if (!upToDate) {
                val temp = File(target.parentFile, "$FILE_NAME.tmp")
                temp.writeBytes(bytes)
                if (!temp.renameTo(target)) {
                    temp.copyTo(target, overwrite = true)
                    temp.delete()
                }
                Logger.info(TAG, "Zadrinth Client extracted (${bytes.size} bytes)")
            }
            homeDir.mkdirs()
            target
        }.onFailure { th ->
            Logger.warning(TAG, "Could not prepare the Zadrinth Client", th)
        }.getOrNull()
    }
}
