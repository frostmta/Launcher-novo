/*
 * Zadrinth Launcher — fork não oficial do Zalith Launcher 2
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

package com.movtery.zalithlauncher.game.account.wardrobe

import android.graphics.BitmapFactory
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsText
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/** Uma capa oficial do Minecraft, identificada pelo hash da textura em textures.minecraft.net. */
data class OfficialCape(
    val name: String,
    val hash: String,
) {
    val url: String get() = "https://textures.minecraft.net/texture/$hash"
}

/**
 * Catálogo das capas oficiais do Minecraft para o Armário.
 *
 * Nenhuma textura da Mojang vem dentro do APK: a lista vem da API pública do MineSkin
 * e cada textura é baixada de textures.minecraft.net quando o jogador abre o Armário,
 * ficando salva no aparelho para funcionar offline depois.
 */
object OfficialCapes {
    private const val TAG = "OfficialCapes"
    private const val CATALOG_URL = "https://api.mineskin.org/v2/capes"
    private const val TEXTURE_HOST = "textures.minecraft.net"
    private const val MAX_TEXTURE_BYTES = 1024 * 1024

    private val HASH_REGEX = Regex("^[0-9a-f]{32,64}$")

    /**
     * Usada quando a API não responde e não há cache.
     * Hashes conferidos na Minecraft Wiki (campo "Texture ID") e no Craftdex.
     */
    private val FALLBACK = listOf(
        OfficialCape("Migrator", "2340c0e03dd24a11b15a8b33c2a7e9e32abb2051b2481d0ba7defd635ca7a933"),
        OfficialCape("Vanilla", "f9a76537647989f9a0b6d001e320dac591c359e9e61a31f4ce11c88f207f0ad4"),
        OfficialCape("Cherry Blossom", "afd553b39358a24edfe3b8a9a939fa5fa4faa4d9a9c3d6af8eafb377fa05c2bb"),
        OfficialCape("15th Anniversary", "cd9d82ab17fd92022dbd4a86cde4c382a7540e117fae7b9a2853658505a80625"),
        OfficialCape("Follower's", "569b7f2a1d00d26f30efe3f9ab9ac817b1e6d35f4f3cfb0324ef2d328223d350"),
        OfficialCape("Minecraft Experience", "7658c5025c77cfac7574aab3af94a46a8886e3b7722a895255fbf22ab8652434"),
        OfficialCape("Mojang Office", "5c29410057e32abec02d870ecb52ec25fb45ea81e785a7854ae8429d7236ca26"),
        OfficialCape("Pan", "28de4a81688ad18b49e735a273e086c18f1e3966956123ccb574034c06f5d336"),
        OfficialCape("Home", "1de21419009db483900da6298a1e6cbf9f1bc1523a0dcdc16263fab150693edd"),
        OfficialCape("Founder's", "99aba02ef05ec6aa4d42db8ee43796d6cd50e4b2954ab29f0caeb85f96bf52a1"),
        OfficialCape("Hero", "bb384a1079b9a6f2811520c7991b6e8150d02e5a96457e44509e30822f72f38e"),
        OfficialCape("Aurora", "7c418dfbd37412a55e9f1425c9807a591ad10cf71b1edb576a587be9114c277f"),
        OfficialCape("MINECON 2016", "e7dfea16dc83c97df01a12fabbd1216359c0cd0ea42f9999b6e97c584963e980"),
    )

    private val dir: File get() = File(PathManager.DIR_FILES_PRIVATE, "official_capes")
    private val catalogFile: File get() = File(dir, "catalog.json")

    private val catalogMutex = Mutex()
    @Volatile private var catalogMemory: List<OfficialCape>? = null
    private val textures = ConcurrentHashMap<String, ByteArray>()

    /** Catálogo já carregado nesta sessão, se houver. */
    fun peekCatalog(): List<OfficialCape>? = catalogMemory

    /** Textura já carregada na memória, se houver. */
    fun peekTexture(hash: String): ByteArray? = textures[hash]

    /**
     * Lista de capas oficiais: tenta a API; se falhar, usa a última lista salva;
     * em todo caso completa com a lista embutida de hashes conhecidos.
     */
    suspend fun catalog(force: Boolean = false): List<OfficialCape> = catalogMutex.withLock {
        if (!force) catalogMemory?.let { return@withLock it }

        val remote = try {
            withContext(Dispatchers.IO) {
                parseCatalog(GLOBAL_CLIENT.get(CATALOG_URL).safeBodyAsText())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (th: Throwable) {
            Logger.warning(TAG, "Failed to fetch the official cape list", th)
            null
        }

        if (!remote.isNullOrEmpty()) {
            withContext(Dispatchers.IO) { saveCatalog(remote) }
        }
        val base = remote?.takeIf { it.isNotEmpty() } ?: withContext(Dispatchers.IO) { readSavedCatalog() }
        val merged = merge(base.orEmpty(), FALLBACK)
        //Só guarda na memória se a lista veio da API, para tentar de novo na próxima abertura
        if (!remote.isNullOrEmpty()) catalogMemory = merged
        merged
    }

    /** Textura da capa (64x32): memória → arquivo salvo → download da Mojang. */
    suspend fun texture(cape: OfficialCape): ByteArray {
        textures[cape.hash]?.let { return it }
        val bytes = withContext(Dispatchers.IO) {
            val file = File(dir, "${cape.hash}.png")
            file.takeIf { it.isFile }?.readBytes()?.takeIf { isCapeTexture(it) }
                ?: download(cape).also { downloaded ->
                    dir.mkdirs()
                    val temp = File(dir, "${cape.hash}.png.tmp")
                    temp.writeBytes(downloaded)
                    if (!temp.renameTo(file)) {
                        temp.copyTo(file, overwrite = true)
                        temp.delete()
                    }
                }
        }
        textures[cape.hash] = bytes
        return bytes
    }

    /** Se [bytes] é uma capa oficial já baixada, devolve o hash dela. */
    suspend fun findDownloaded(bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        textures.entries.firstOrNull { it.value.contentEquals(bytes) }?.let { return@withContext it.key }
        dir.listFiles { file -> file.isFile && file.name.endsWith(".png") && file.length() == bytes.size.toLong() }
            ?.firstOrNull { runCatching { it.readBytes().contentEquals(bytes) }.getOrDefault(false) }
            ?.name?.removeSuffix(".png")
    }

    private suspend fun download(cape: OfficialCape): ByteArray {
        val bytes = GLOBAL_CLIENT.get(cape.url).bodyAsChannel().toInputStream().use { input ->
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(8 * 1024)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                if (total > MAX_TEXTURE_BYTES) throw IllegalStateException("Cape texture is too large")
                out.write(buffer, 0, read)
            }
            out.toByteArray()
        }
        check(isCapeTexture(bytes)) { "Downloaded file is not a cape texture: ${cape.hash}" }
        return bytes
    }

    /** Capa válida: PNG com proporção 2:1, largura múltipla de 64 (até 1024). */
    private fun isCapeTexture(bytes: ByteArray): Boolean {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        val w = options.outWidth
        val h = options.outHeight
        return w in 64..1024 && w % 64 == 0 && h * 2 == w
    }

    private fun parseCatalog(text: String): List<OfficialCape> {
        val root = JsonParser.parseString(text)
        val array: JsonArray = when {
            root.isJsonArray -> root.asJsonArray
            root.isJsonObject -> root.asJsonObject.getAsJsonArray("capes") ?: return emptyList()
            else -> return emptyList()
        }
        return array.mapNotNull { element ->
            runCatching {
                val obj = element.asJsonObject
                val name = obj.string("alias") ?: obj.string("name") ?: return@runCatching null
                val hash = obj.string("url")?.let(::hashFromUrl) ?: obj.string("hash")
                    ?.lowercase()?.takeIf { HASH_REGEX.matches(it) }
                    ?: return@runCatching null
                OfficialCape(name = name.trim(), hash = hash)
            }.getOrNull()
        }.distinctBy { it.hash }
    }

    /** Só aceita texturas hospedadas pela própria Mojang. */
    private fun hashFromUrl(url: String): String? {
        val clean = url.trim()
        val host = clean.substringAfter("://", "").substringBefore('/')
        if (!host.equals(TEXTURE_HOST, ignoreCase = true)) return null
        val hash = clean.substringAfterLast('/').lowercase()
        return hash.takeIf { HASH_REGEX.matches(it) }
    }

    private fun merge(primary: List<OfficialCape>, extra: List<OfficialCape>): List<OfficialCape> {
        val seen = primary.mapTo(HashSet()) { it.hash }
        return primary + extra.filter { seen.add(it.hash) }
    }

    private fun saveCatalog(capes: List<OfficialCape>) {
        runCatching {
            dir.mkdirs()
            val array = JsonArray()
            capes.forEach { cape ->
                array.add(JsonObject().apply {
                    addProperty("name", cape.name)
                    addProperty("hash", cape.hash)
                })
            }
            catalogFile.writeText(array.toString())
        }.onFailure { th ->
            Logger.warning(TAG, "Failed to save the official cape list", th)
        }
    }

    private fun readSavedCatalog(): List<OfficialCape>? = runCatching {
        catalogFile.takeIf { it.isFile }?.readText()?.let(::parseCatalog)
    }.getOrNull()

    private fun JsonObject.string(key: String): String? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
}
