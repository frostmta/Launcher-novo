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
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

/**
 * Mod do jogo do Zadrinth Client (client-mod/, Minecraft 1.8.9 + Legacy Fabric).
 *
 * O CI compila o mod e coloca o .jar dentro do APK (assets/client). Antes de
 * abrir uma instância 1.8.9 com Legacy Fabric, o launcher copia (ou atualiza)
 * o mod na pasta mods da instância. Se o jogador desativar o mod (arquivo
 * ".disabled"), o launcher respeita e não instala de novo.
 */
object ZadrinthClientMod {
    private const val TAG = "ZadrinthClientMod"
    private const val ASSET_PATH = "client/zadrinth-client.jar"
    const val FILE_NAME = "zadrinth-client.jar"
    const val MINECRAFT_VERSION = "1.8.9"

    /** A instância roda o Zadrinth Client? (1.8.9 + Legacy Fabric) */
    fun isSupported(version: Version): Boolean {
        val info = version.getVersionInfo() ?: return false
        return info.minecraftVersion == MINECRAFT_VERSION && info.hasLoader(ModLoader.LEGACY_FABRIC)
    }

    /** Copia/atualiza o mod na instância. Seguro de chamar em toda abertura do jogo. */
    fun install(context: Context, version: Version) {
        if (!isSupported(version)) return
        runCatching {
            val bytes = context.assets.open(ASSET_PATH).use { it.readBytes() }
            val modsDir = File(version.getGameDir(), "mods")
            if (File(modsDir, "$FILE_NAME.disabled").exists()) {
                Logger.info(TAG, "Zadrinth Client disabled by the player, skipping install")
                return
            }
            if (!modsDir.isDirectory && !modsDir.mkdirs()) {
                Logger.warning(TAG, "Could not create $modsDir")
                return
            }
            val target = File(modsDir, FILE_NAME)
            if (target.isFile && target.length() == bytes.size.toLong() && target.readBytes().contentEquals(bytes)) {
                return
            }
            val temp = File(modsDir, "$FILE_NAME.tmp")
            temp.writeBytes(bytes)
            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }
            Logger.info(TAG, "Zadrinth Client installed into ${version.getVersionName()}")
        }.onFailure { th ->
            Logger.warning(TAG, "Could not install the Zadrinth Client mod", th)
        }
    }
}
