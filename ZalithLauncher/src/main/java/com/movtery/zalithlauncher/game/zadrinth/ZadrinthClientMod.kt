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
 * Uma versão do Zadrinth Client: cada versão do Minecraft tem o seu .jar.
 */
enum class ClientTarget(
    /** Versão do Minecraft */
    val minecraft: String,
    /** Loader que a instância precisa ter */
    val loader: ModLoader,
    /** O .jar dentro do APK (assets/) */
    val asset: String,
    /** Nome do .jar extraído */
    val fileName: String,
    /** Texto curto para o seletor ("1.8.9 • PvP clássico") */
    val tagline: String,
) {
    V1_8_9("1.8.9", ModLoader.LEGACY_FABRIC, "client/zadrinth-client.jar", "zadrinth-client.jar", "PvP clássico"),
    V1_21_11("1.21.11", ModLoader.FABRIC, "client/zadrinth-client-1.21.11.jar", "zadrinth-client-1.21.11.jar", "Versão nova");

    /** Nome da instância criada pelo botão "Instalar" */
    val instanceName: String get() = "Zadrinth Client $minecraft"
}

/**
 * O Zadrinth Client que roda dentro do jogo (client-mod/ para 1.8.9 e
 * client-mod-1.21.11/ para 1.21.11).
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
    const val FILE_NAME = "zadrinth-client.jar"
    const val MINECRAFT_VERSION = "1.8.9"

    /** Nome da instância 1.8.9 criada pelo botão "Instalar Zadrinth Client" */
    const val INSTANCE_NAME = "Zadrinth Client 1.8.9"

    /** Pasta interna do Zadrinth (client extraído + cosmetics.json) */
    val homeDir: File get() = File(PathManager.DIR_FILES_PRIVATE, "zadrinth")

    private fun clientJar(target: ClientTarget) = File(homeDir, "client/${target.fileName}")

    /** Qual versão do client roda nesta instância (null = nenhuma) */
    fun targetOf(version: Version): ClientTarget? {
        val info = version.getVersionInfo() ?: return null
        return ClientTarget.entries.firstOrNull { info.minecraftVersion == it.minecraft && info.hasLoader(it.loader) }
    }

    /** A instância roda o Zadrinth Client? */
    fun isSupported(version: Version): Boolean = targetOf(version) != null

    /** A instância do client para esta versão (a criada pelo botão, ou outra compatível) */
    fun findInstance(versions: List<Version>, target: ClientTarget): Version? =
        versions.firstOrNull { it.getVersionName() == target.instanceName && targetOf(it) == target }
            ?: versions.firstOrNull { targetOf(it) == target }

    /**
     * Prepara o client para abrir esta instância.
     * @return o .jar para injetar com -Dfabric.addMods, ou null (instância sem suporte)
     */
    fun prepare(context: Context, version: Version): File? {
        val target = targetOf(version) ?: return null
        return runCatching {
            val modsDir = File(version.getGameDir(), "mods")
            if (File(modsDir, "${target.fileName}.disabled").exists() || File(modsDir, "$FILE_NAME.disabled").exists()) {
                Logger.info(TAG, "Zadrinth Client disabled for ${version.getVersionName()}")
                return null
            }
            //Versões antigas (0.5/0.6) copiavam o client para mods/: tira para não carregar duas vezes
            File(modsDir, FILE_NAME).takeIf { it.isFile }?.let { old ->
                if (old.delete()) Logger.info(TAG, "Removed old copy from mods/")
            }

            val bytes = context.assets.open(target.asset).use { it.readBytes() }
            val jar = clientJar(target)
            jar.parentFile?.mkdirs()
            val upToDate = jar.isFile && jar.length() == bytes.size.toLong() && jar.readBytes().contentEquals(bytes)
            if (!upToDate) {
                val temp = File(jar.parentFile, "${target.fileName}.tmp")
                temp.writeBytes(bytes)
                if (!temp.renameTo(jar)) {
                    temp.copyTo(jar, overwrite = true)
                    temp.delete()
                }
                Logger.info(TAG, "Zadrinth Client ${target.minecraft} extracted (${bytes.size} bytes)")
            }
            homeDir.mkdirs()
            jar
        }.onFailure { th ->
            Logger.warning(TAG, "Could not prepare the Zadrinth Client", th)
        }.getOrNull()
    }
}
