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

package com.movtery.zalithlauncher.game.servers

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.movtery.zalithlauncher.game.version.multiplayer.description.ServerDescription
import com.movtery.zalithlauncher.game.version.multiplayer.pingServer
import com.movtery.zalithlauncher.game.version.multiplayer.resolve
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.ServerAddress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Um servidor em destaque na tela inicial / aba Servidores. */
data class FeaturedServer(
    val id: String,
    val name: String,
    val address: String,
    val region: String?,
    val tags: List<String>,
    /** Só aceita conta original (Microsoft) */
    val premium: Boolean,
)

/** Estado ao vivo de um servidor, obtido pelo "ping" do Minecraft (lista de servidores). */
sealed interface FeaturedServerStatus {
    data object Loading : FeaturedServerStatus
    data class Online(
        val playersOnline: Int,
        val playersMax: Int,
        val pingMs: Long,
        val favicon: ByteArray?,
        val motd: ServerDescription?,
        val versionName: String,
    ) : FeaturedServerStatus {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Online) return false
            return playersOnline == other.playersOnline && playersMax == other.playersMax &&
                    pingMs == other.pingMs && versionName == other.versionName &&
                    motd == other.motd && favicon.contentEqualsNullable(other.favicon)
        }

        override fun hashCode(): Int {
            var result = playersOnline
            result = 31 * result + playersMax
            result = 31 * result + pingMs.hashCode()
            result = 31 * result + (favicon?.contentHashCode() ?: 0)
            result = 31 * result + versionName.hashCode()
            return result
        }
    }
    data object Offline : FeaturedServerStatus
}

private fun ByteArray?.contentEqualsNullable(other: ByteArray?): Boolean =
    if (this == null) other == null else other != null && contentEquals(other)

/**
 * Catálogo de servidores em destaque (assets/servers/featured.json) e o status
 * ao vivo de cada um (ícone, jogadores online, ping), igual à lista de
 * servidores do próprio Minecraft.
 */
object FeaturedServers {
    private const val TAG = "FeaturedServers"
    private const val ASSET_PATH = "servers/featured.json"
    /** Não pinga o mesmo servidor de novo antes disso */
    private const val PING_COOLDOWN_MS = 45_000L

    private val _servers = MutableStateFlow<List<FeaturedServer>>(emptyList())
    val servers: StateFlow<List<FeaturedServer>> = _servers.asStateFlow()

    private val _status = MutableStateFlow<Map<String, FeaturedServerStatus>>(emptyMap())
    val status: StateFlow<Map<String, FeaturedServerStatus>> = _status.asStateFlow()

    private val loadMutex = Mutex()
    private val refreshMutex = Mutex()
    private val lastPing = HashMap<String, Long>()

    /** Lê o catálogo (uma vez) e atualiza o status de todos os servidores. */
    suspend fun loadAndRefresh(context: Context, force: Boolean = false) {
        load(context)
        refresh(force)
    }

    suspend fun load(context: Context) = loadMutex.withLock {
        if (_servers.value.isNotEmpty()) return@withLock
        val list = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open(ASSET_PATH).bufferedReader().use { parse(it.readText()) }
            }.onFailure { th ->
                Logger.warning(TAG, "Failed to read the featured server list", th)
            }.getOrDefault(emptyList())
        }
        _servers.value = list
    }

    /** Pinga todos os servidores ao mesmo tempo (respeitando um intervalo mínimo). */
    suspend fun refresh(force: Boolean = false) = refreshMutex.withLock {
        val now = System.currentTimeMillis()
        val targets = _servers.value.filter { server ->
            force || now - (lastPing[server.id] ?: 0L) > PING_COOLDOWN_MS
        }
        if (targets.isEmpty()) return@withLock

        _status.update { current ->
            current + targets
                .filter { current[it.id] !is FeaturedServerStatus.Online }
                .associate { it.id to FeaturedServerStatus.Loading }
        }

        coroutineScope {
            targets.map { server ->
                async {
                    val result = ping(server)
                    synchronized(lastPing) { lastPing[server.id] = System.currentTimeMillis() }
                    _status.update { it + (server.id to result) }
                }
            }.awaitAll()
        }
    }

    private suspend fun ping(server: FeaturedServer): FeaturedServerStatus {
        return try {
            val result = pingServer(
                resolved = ServerAddress.parse(server.address).resolve(),
                timeoutMillis = 8_000
            )
            val status = result.status
            FeaturedServerStatus.Online(
                playersOnline = status.players.online,
                playersMax = status.players.max,
                pingMs = result.pingMs,
                favicon = status.favicon?.icon,
                motd = status.description,
                versionName = status.version.name
            )
        } catch (e: CancellationException) {
            throw e
        } catch (th: Throwable) {
            Logger.warning(TAG, "Unable to ping ${server.address}", th)
            FeaturedServerStatus.Offline
        }
    }

    private fun parse(text: String): List<FeaturedServer> {
        val root = JsonParser.parseString(text).asJsonObject
        val array = root.getAsJsonArray("servers") ?: return emptyList()
        return array.mapNotNull { element ->
            runCatching {
                val obj = element.asJsonObject
                val address = obj.string("address") ?: return@runCatching null
                FeaturedServer(
                    id = obj.string("id") ?: address,
                    name = obj.string("name") ?: address,
                    address = address,
                    region = obj.string("region"),
                    tags = obj.getAsJsonArray("tags")
                        ?.mapNotNull { tag -> runCatching { tag.asString }.getOrNull() }
                        .orEmpty(),
                    premium = obj.get("premium")?.takeIf { it.isJsonPrimitive }?.asBoolean ?: false
                )
            }.getOrNull()
        }.distinctBy { it.id }
    }

    private fun JsonObject.string(key: String): String? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString?.trim()?.takeIf { it.isNotEmpty() }
}
