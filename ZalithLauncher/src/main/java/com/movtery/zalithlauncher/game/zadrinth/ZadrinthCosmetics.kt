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

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/** Categoria de cosmético (igual à do client do jogo). */
enum class CosmeticSlot(val folder: String, val loadoutKey: String) {
    CAPE("capes", "cape"),
    WINGS("wings", "wings"),
    BANDANA("bandanas", "bandana"),
}

/** Item da loja. Preço 0 = grátis (a estrutura já aceita preços no futuro). */
data class ShopCosmetic(
    val id: String,
    val slot: CosmeticSlot,
    val name: String,
    val description: String,
    val accent: Long,
    val price: Int = 0,
    val animated: Boolean = false,
) {
    /** Mesmo formato do client: "capes/nebulosa" */
    val key: String get() = "${slot.folder}/$id"

    /** Imagem de prévia em assets/cosmetics */
    val previewAsset: String get() = "cosmetics/${slot.folder}_$id.png"

    val isFree: Boolean get() = price <= 0
}

/**
 * Catálogo e coleção de cosméticos do Zadrinth Client.
 *
 * A coleção e o que está equipado ficam em files/zadrinth/cosmetics.json. O
 * launcher abre o jogo com -Dzadrinth.home apontando para essa pasta, então o
 * client lê/grava o mesmo arquivo: o que você resgata aqui aparece no jogo e
 * vice-versa.
 */
object ZadrinthCosmetics {
    private const val TAG = "ZadrinthCosmetics"
    private val GSON = GsonBuilder().setPrettyPrinting().create()

    val catalog: List<ShopCosmetic> = listOf(
        ShopCosmetic("nebulosa", CosmeticSlot.CAPE, "Nebulosa", "Uma estrela nascendo entre nuvens de poeira cósmica.", 0xFF9D7BFF),
        ShopCosmetic("aurora", CosmeticSlot.CAPE, "Aurora Boreal", "Luzes do norte dançando sobre montanhas com neve.", 0xFF2FD3A0),
        ShopCosmetic("lua_carmesim", CosmeticSlot.CAPE, "Lua Carmesim", "Uma lua de sangue sobre a floresta em brasas.", 0xFFE0453A),
        ShopCosmetic("cristal", CosmeticSlot.CAPE, "Cristal Zadrinth", "O cristal da marca, lapidado em violeta.", 0xFFB77BFF),
        ShopCosmetic("abismo", CosmeticSlot.CAPE, "Abismo", "Uma água-viva brilhando no fundo do oceano.", 0xFF36C9E6),
        ShopCosmetic("chamas_etereas", CosmeticSlot.CAPE, "Chamas Etéreas", "Fogo espiritual azul e violeta.", 0xFF4FB8FF),
        ShopCosmetic("demonio", CosmeticSlot.WINGS, "Asas de Demônio", "Membrana carmesim com garras de osso.", 0xFFE2384A),
        ShopCosmetic("dragao_esmeralda", CosmeticSlot.WINGS, "Dragão Esmeralda", "Escamas verdes e espinhos ao longo do braço.", 0xFF2FD37A),
        ShopCosmetic("celestial", CosmeticSlot.WINGS, "Asas Celestiais", "Penas brancas com pontas douradas.", 0xFFF2C65A),
        ShopCosmetic("sombra", CosmeticSlot.WINGS, "Asas da Sombra", "Penas escuras com brilho violeta.", 0xFF8A76D0),
        ShopCosmetic("cristal", CosmeticSlot.WINGS, "Asas de Cristal", "Lâminas de cristal que refletem a luz.", 0xFFC9B8FF),
        ShopCosmetic("ninja", CosmeticSlot.BANDANA, "Ninja Violeta", "Faixa preta com o cristal do Zadrinth.", 0xFF9D7BFF),
        ShopCosmetic("paisley", CosmeticSlot.BANDANA, "Paisley Vermelha", "A estampa clássica, em vermelho.", 0xFFD0262F),
        ShopCosmetic("chamas", CosmeticSlot.BANDANA, "Chamas", "Fogo de verdade: as chamas sobem e tremulam.", 0xFFFF8A1E, animated = true),
        ShopCosmetic("galaxia", CosmeticSlot.BANDANA, "Galáxia", "Nebulosas e estrelas em volta da cabeça.", 0xFF6E9BFF),
        ShopCosmetic("zadrinth", CosmeticSlot.BANDANA, "Zadrinth", "O degradê da marca com o cristal na testa.", 0xFFE07BFF),
    )

    /** O que o jogador tem e o que está usando. */
    data class State(
        val owned: Set<String> = emptySet(),
        val equipped: Map<CosmeticSlot, String> = emptyMap(),
    ) {
        fun owns(item: ShopCosmetic) = item.key in owned
        fun isEquipped(item: ShopCosmetic) = equipped[item.slot] == item.id
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private fun file(): File = File(ZadrinthClientMod.homeDir, "cosmetics.json")

    private fun readJson(): JsonObject = runCatching {
        val file = file()
        if (file.isFile) JsonParser.parseString(file.readText()).asJsonObject else JsonObject()
    }.getOrElse {
        Logger.warning(TAG, "Invalid cosmetics.json, starting over", it)
        JsonObject()
    }

    private fun writeJson(json: JsonObject) {
        runCatching {
            val file = file()
            file.parentFile?.mkdirs()
            val temp = File(file.parentFile, "${file.name}.tmp")
            temp.writeText(GSON.toJson(json))
            if (!temp.renameTo(file)) {
                temp.copyTo(file, overwrite = true)
                temp.delete()
            }
        }.onFailure { Logger.warning(TAG, "Could not save cosmetics.json", it) }
    }

    private fun parse(json: JsonObject): State {
        val owned = json.getAsJsonArray("owned")?.mapNotNull { runCatching { it.asString }.getOrNull() }?.toSet() ?: emptySet()
        val loadout = json.getAsJsonObject("loadout")
        val equipped = buildMap {
            CosmeticSlot.entries.forEach { slot ->
                val id = loadout?.get(slot.loadoutKey)?.takeIf { it.isJsonPrimitive }?.asString
                if (!id.isNullOrEmpty()) put(slot, id)
            }
        }
        return State(owned, equipped)
    }

    /** Relê o arquivo (o jogo pode ter mudado alguma coisa). */
    fun reload() {
        _state.value = parse(readJson())
    }

    private fun edit(block: (JsonObject) -> Unit) {
        val json = readJson()
        block(json)
        writeJson(json)
        _state.value = parse(json)
    }

    /** Resgata (grátis) e já equipa. */
    fun claim(item: ShopCosmetic) = edit { json ->
        val owned = json.getAsJsonArray("owned") ?: JsonArray().also { json.add("owned", it) }
        if (owned.none { runCatching { it.asString }.getOrNull() == item.key }) owned.add(item.key)
        loadout(json).addProperty(item.slot.loadoutKey, item.id)
    }

    fun equip(item: ShopCosmetic) = edit { json ->
        loadout(json).addProperty(item.slot.loadoutKey, item.id)
    }

    fun unequip(slot: CosmeticSlot) = edit { json ->
        loadout(json).remove(slot.loadoutKey)
    }

    private fun loadout(json: JsonObject): JsonObject =
        json.getAsJsonObject("loadout") ?: JsonObject().also { json.add("loadout", it) }
}
