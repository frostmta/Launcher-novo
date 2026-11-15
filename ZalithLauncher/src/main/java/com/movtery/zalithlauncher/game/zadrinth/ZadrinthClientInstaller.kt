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
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.legacyfabric.LegacyFabricVersions
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Instala o Zadrinth Client com um toque: o Minecraft da versão escolhida +
 * o loader certo (Legacy Fabric no 1.8.9, Fabric no 1.21.11), numa instância
 * chamada "Zadrinth Client <versão>". O client em si é injetado na hora de
 * jogar (a Fabric API do 1.21.11 já vem dentro dele).
 */
object ZadrinthClientInstaller {
    private const val TAG = "ZadrinthClientInstaller"

    sealed interface State {
        data object Idle : State
        data object Working : State
        data class Failed(val message: String) : State
        data object Done : State
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var installer: GameInstaller? = null

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    /** Versão sendo instalada agora */
    var installing: ClientTarget? = null
        private set

    fun install(context: Context, target: ClientTarget = ClientTarget.V1_8_9) {
        if (_state.value is State.Working) return
        _state.value = State.Working
        installing = target
        val appContext = context.applicationContext
        scope.launch {
            var info: GameDownloadInfo? = null
            var name = target.instanceName
            var index = 2
            while (VersionsManager.isVersionExists(name, checkJson = true)) {
                name = "${target.instanceName} ($index)"
                index++
            }
            when (target.loader) {
                ModLoader.LEGACY_FABRIC -> {
                    val loader = withContext(Dispatchers.IO) {
                        runCatching { LegacyFabricVersions.fetchFabricLoaderList(target.minecraft, force = false) }
                            .onFailure { Logger.warning(TAG, "Could not fetch Legacy Fabric loaders", it) }.getOrNull()
                    }?.let { list -> list.firstOrNull { it.stable } ?: list.firstOrNull() }
                    if (loader != null) info = GameDownloadInfo(gameVersion = target.minecraft, customVersionName = name, legacyFabric = loader)
                }
                else -> {
                    val loader = withContext(Dispatchers.IO) {
                        runCatching { FabricVersions.fetchFabricLoaderList(target.minecraft, force = false) }
                            .onFailure { Logger.warning(TAG, "Could not fetch Fabric loaders", it) }.getOrNull()
                    }?.let { list -> list.firstOrNull { it.stable } ?: list.firstOrNull() }
                    if (loader != null) info = GameDownloadInfo(gameVersion = target.minecraft, customVersionName = name, fabric = loader)
                }
            }
            val download = info
            if (download == null) {
                _state.value = State.Failed("Não foi possível baixar a lista do Fabric. Verifique a internet e tente de novo.")
                return@launch
            }

            installer = GameInstaller(appContext, download, scope).also { gameInstaller ->
                gameInstaller.installGame(
                    onInstalled = { version ->
                        installer = null
                        VersionsManager.refresh("[ZadrinthClient] installed", version)
                        _state.value = State.Done
                    },
                    onError = { th ->
                        installer = null
                        Logger.warning(TAG, "Install failed", th)
                        _state.value = State.Failed(th.message ?: th.toString())
                    },
                    onGameAlreadyInstalled = {
                        installer = null
                        VersionsManager.refresh("[ZadrinthClient] already installed", name)
                        _state.value = State.Done
                    }
                )
            }
        }
    }

    fun cancel() {
        installer?.cancelInstall()
        installer = null
        _state.value = State.Idle
    }

    fun dismissError() {
        if (_state.value is State.Failed || _state.value is State.Done) _state.value = State.Idle
    }
}
