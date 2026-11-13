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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.ByteArrayOutputStream
import java.io.File

private const val TAG = "ZadrinthWardrobe"

/** Falha ao importar uma imagem para o Armário; [messageRes] explica o motivo ao jogador. */
class WardrobeImageException(@StringRes val messageRes: Int) : Exception()

/**
 * Armário do Zadrinth: capas e skins para contas offline (locais).
 *
 * A capa escolhida é salva em [Account.getCapeFile]; ao iniciar o jogo, o servidor
 * yggdrasil local (authlib-injector) entrega skin e capa ao Minecraft.
 * Como o servidor é local, só o próprio jogador enxerga a capa.
 */
object ZadrinthWardrobe {
    /** Capas originais do Zadrinth, em `assets/capes/<id>.png` (64x32). */
    enum class BuiltInCape(val id: String, @StringRes val nameRes: Int) {
        ZADRINTH("zadrinth", R.string.wardrobe_cape_zadrinth),
        CRISTAL("cristal", R.string.wardrobe_cape_cristal),
        NOITE("noite", R.string.wardrobe_cape_noite),
        CHAMAS("chamas", R.string.wardrobe_cape_chamas),
        OCEANO("oceano", R.string.wardrobe_cape_oceano),
        FLORESTA("floresta", R.string.wardrobe_cape_floresta),
        ARCO_IRIS("arco_iris", R.string.wardrobe_cape_arco_iris),
        DOURADA("dourada", R.string.wardrobe_cape_dourada),
        OBSIDIANA("obsidiana", R.string.wardrobe_cape_obsidiana),
        GELO("gelo", R.string.wardrobe_cape_gelo);

        val assetPath: String get() = "capes/$id.png"
    }

    /** Lê todas as capas embutidas (arquivos pequenos, ~1 KB cada). */
    suspend fun loadBuiltInCapes(context: Context): Map<BuiltInCape, ByteArray> =
        withContext(Dispatchers.IO) {
            BuiltInCape.entries.mapNotNull { cape ->
                runCatching {
                    cape to context.assets.open(cape.assetPath).use { it.readBytes() }
                }.onFailure { th ->
                    Logger.warning(TAG, "Failed to load built-in cape ${cape.id}", th)
                }.getOrNull()
            }.toMap()
        }

    suspend fun readSkin(account: Account): ByteArray? = withContext(Dispatchers.IO) {
        account.getSkinFile().takeIf { it.exists() }?.readBytes()
    }

    suspend fun readCape(account: Account): ByteArray? = withContext(Dispatchers.IO) {
        account.getCapeFile().takeIf { it.exists() }?.readBytes()
    }

    /** Veste a capa na conta (substitui a anterior). */
    suspend fun applyCape(account: Account, cape: ByteArray) {
        withContext(Dispatchers.IO) {
            val target = account.getCapeFile()
            target.parentFile?.mkdirs()
            val temp = File(target.parentFile, "${target.name}.tmp")
            temp.writeBytes(cape)
            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                FileUtils.deleteQuietly(temp)
            }
        }
        AccountsManager.refreshWardrobe()
    }

    /** Tira a capa da conta. */
    suspend fun removeCape(account: Account) {
        withContext(Dispatchers.IO) {
            FileUtils.deleteQuietly(account.getCapeFile())
        }
        AccountsManager.refreshWardrobe()
    }

    /**
     * Lê um PNG escolhido pelo jogador e o converte em textura de capa válida:
     * - 64x32 (ou HD proporcional até 1024x512): usada como está;
     * - 22x17 (só a capa) ou 46x22 (capa + élitros), e seus múltiplos HD:
     *   colocada no canto de uma textura 64x32 transparente.
     */
    suspend fun decodeCape(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val bytes = readUri(context, uri)
        val bitmap = decodeBitmap(bytes)
        try {
            val w = bitmap.width
            val h = bitmap.height
            val scale = when {
                w % 64 == 0 && h * 2 == w -> 0
                w % 46 == 0 && h * 46 == w * 22 -> w / 46
                w % 22 == 0 && h * 22 == w * 17 -> w / 22
                else -> throw WardrobeImageException(R.string.wardrobe_error_cape_size)
            }
            when {
                scale == 0 && w <= 1024 -> bitmap.toPng()
                scale in 1..16 -> {
                    val canvasBitmap = Bitmap.createBitmap(64 * scale, 32 * scale, Bitmap.Config.ARGB_8888)
                    try {
                        Canvas(canvasBitmap).drawBitmap(bitmap, 0f, 0f, null)
                        canvasBitmap.toPng()
                    } finally {
                        canvasBitmap.recycle()
                    }
                }
                else -> throw WardrobeImageException(R.string.wardrobe_error_cape_size)
            }
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Importa uma skin para uma conta offline. O modelo (clássico/fino) é detectado
     * pela transparência dos braços; o jogador pode trocar depois com [setSkinModel].
     */
    suspend fun importSkin(context: Context, account: Account, uri: Uri): SkinModelType {
        val cacheFile = File(PathManager.DIR_CACHE, "wardrobe_skin_${account.uniqueUUID}.png")
        try {
            withContext(Dispatchers.IO) {
                cacheFile.parentFile?.mkdirs()
                cacheFile.writeBytes(readUri(context, uri))
            }
            if (!validateSkinFile(cacheFile).isValid()) {
                throw WardrobeImageException(R.string.wardrobe_error_skin_size)
            }
            val model = if (cacheFile.isSlimModel()) SkinModelType.ALEX else SkinModelType.STEVE
            withContext(Dispatchers.IO) {
                val target = account.getSkinFile()
                target.parentFile?.mkdirs()
                cacheFile.copyTo(target, overwrite = true)
                account.skinModelType = model
                account.profileId = getLocalUUIDWithSkinModel(account.username, model)
                AccountsManager.suspendSaveAccount(account)
            }
            AccountsManager.refreshWardrobe()
            return model
        } finally {
            withContext(Dispatchers.IO) { FileUtils.deleteQuietly(cacheFile) }
        }
    }

    /** Troca o modelo dos braços (Steve = clássico, Alex = fino) da skin offline. */
    suspend fun setSkinModel(account: Account, model: SkinModelType) {
        if (account.skinModelType == model) return
        withContext(Dispatchers.IO) {
            account.skinModelType = model
            account.profileId = getLocalUUIDWithSkinModel(account.username, model)
            AccountsManager.suspendSaveAccount(account)
        }
        AccountsManager.refreshWardrobe()
    }

    /** Volta para a skin padrão (Steve/Alex conforme o UUID). */
    suspend fun resetSkin(account: Account) {
        withContext(Dispatchers.IO) {
            FileUtils.deleteQuietly(account.getSkinFile())
            account.skinModelType = SkinModelType.NONE
            account.profileId = getLocalUUIDWithSkinModel(account.username, SkinModelType.NONE)
            AccountsManager.suspendSaveAccount(account)
        }
        AccountsManager.refreshWardrobe()
    }

    private fun readUri(context: Context, uri: Uri): ByteArray {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            //Texturas são pequenas; 8 MB é o mesmo limite do visualizador 3D
            val buffer = ByteArrayOutputStream()
            val chunk = ByteArray(16 * 1024)
            var total = 0
            while (true) {
                val read = input.read(chunk)
                if (read < 0) break
                total += read
                if (total > 8 * 1024 * 1024) throw WardrobeImageException(R.string.wardrobe_error_too_big)
                buffer.write(chunk, 0, read)
            }
            buffer.toByteArray()
        }
        return bytes ?: throw WardrobeImageException(R.string.wardrobe_error_read)
    }

    private fun decodeBitmap(bytes: ByteArray): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw WardrobeImageException(R.string.wardrobe_error_read)
        }
        //Recusa imagens enormes antes de decodificar (evita estourar a memória)
        if (bounds.outWidth > 2048 || bounds.outHeight > 2048) {
            throw WardrobeImageException(R.string.wardrobe_error_cape_size)
        }
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inScaled = false
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: throw WardrobeImageException(R.string.wardrobe_error_read)
    }

    private fun Bitmap.toPng(): ByteArray = ByteArrayOutputStream().use { out ->
        compress(Bitmap.CompressFormat.PNG, 100, out)
        out.toByteArray()
    }
}
