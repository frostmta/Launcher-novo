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

package com.movtery.zalithlauncher.ui.screens.content.zadrinth

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.petlib.PetAnimation
import com.movtery.petlib.PetTapReaction
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.wardrobe.WardrobeImageException
import com.movtery.zalithlauncher.game.account.wardrobe.ZadrinthWardrobe
import com.movtery.zalithlauncher.game.account.wardrobe.toArmModel
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.PetModelView
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthBrandButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthCard
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthFilterChip
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthSecondaryButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTag
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import com.movtery.zalithlauncher.game.account.wardrobe.OfficialCapes
import com.movtery.zalithlauncher.game.account.wardrobe.OfficialCape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect

private const val TAG = "WardrobeScreen"

private val CapeTileShape = RoundedCornerShape(12.dp)

/**
 * Armário: prévia 3D do jogador, capas originais do Zadrinth (ou PNG importado)
 * e skin para contas offline.
 */
@Composable
fun WardrobeScreen(
    backStackViewModel: ScreenBackStackViewModel,
    onOpenAccounts: () -> Unit,
) {
    BaseScreen(
        screenKey = NormalNavKey.Wardrobe,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            WardrobeContent(
                modifier = Modifier.fillMaxSize(),
                onOpenAccounts = onOpenAccounts
            )
        }
    }
}

/** O que fazer com a capa escolhida no grid. */
private sealed interface CapeChoice {
    data object None : CapeChoice
    data class BuiltIn(val cape: ZadrinthWardrobe.BuiltInCape) : CapeChoice
    data class Official(val hash: String) : CapeChoice
    data object Custom : CapeChoice
}

/** Abas de capas do Armário. */
private enum class CapeSource { OFFICIAL, ZADRINTH }

@Composable
private fun WardrobeContent(
    modifier: Modifier = Modifier,
    onOpenAccounts: () -> Unit,
) {
    val context = LocalContext.current
    val colors = ModrinthTheme.colors
    val scope = rememberCoroutineScope()

    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()

    val builtInCapes by produceState<Map<ZadrinthWardrobe.BuiltInCape, ByteArray>>(emptyMap()) {
        value = ZadrinthWardrobe.loadBuiltInCapes(context)
    }
    val skinBytes by produceState<ByteArray?>(null, account, refreshWardrobe) {
        value = account?.let { ZadrinthWardrobe.readSkin(it) }
    }
    val capeBytes by produceState<ByteArray?>(null, account, refreshWardrobe) {
        value = account?.let { ZadrinthWardrobe.readCape(it) }
    }
    val skinModel = remember(account, refreshWardrobe) { account?.skinModelType ?: SkinModelType.NONE }

    val selectedCape by produceState<CapeChoice>(CapeChoice.None, capeBytes, builtInCapes) {
        val current = capeBytes
        value = when {
            current == null -> CapeChoice.None
            else -> builtInCapes.entries
                .firstOrNull { it.value.contentEquals(current) }
                ?.let { CapeChoice.BuiltIn(it.key) }
                ?: OfficialCapes.findDownloaded(current)?.let { CapeChoice.Official(it) }
                ?: CapeChoice.Custom
        }
    }

    //Capas oficiais: lista da API pública do MineSkin, texturas dos servidores da Mojang
    var capeSource by rememberSaveable { mutableStateOf(CapeSource.OFFICIAL) }
    var officialCapes by remember { mutableStateOf(OfficialCapes.peekCatalog()) }
    LaunchedEffect(capeSource) {
        if (capeSource == CapeSource.OFFICIAL && officialCapes == null) {
            officialCapes = runCatching { OfficialCapes.catalog() }.getOrDefault(emptyList())
        }
    }

    var busy by remember { mutableStateOf(false) }
    var errorRes by remember { mutableStateOf<Int?>(null) }

    fun runWardrobeAction(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                action()
            } catch (e: WardrobeImageException) {
                errorRes = e.messageRes
            } catch (th: Throwable) {
                Logger.warning(TAG, "Wardrobe action failed", th)
                errorRes = R.string.wardrobe_error_read
            } finally {
                busy = false
            }
        }
    }

    val capePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val target = account ?: return@rememberLauncherForActivityResult
        uri ?: return@rememberLauncherForActivityResult
        runWardrobeAction {
            val cape = ZadrinthWardrobe.decodeCape(context, uri)
            ZadrinthWardrobe.applyCape(target, cape)
        }
    }
    val skinPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val target = account ?: return@rememberLauncherForActivityResult
        uri ?: return@rememberLauncherForActivityResult
        runWardrobeAction { ZadrinthWardrobe.importSkin(context, target, uri) }
    }

    errorRes?.let { res ->
        SimpleAlertDialog(
            title = stringResource(R.string.wardrobe_error_title),
            text = stringResource(res),
            onConfirm = { errorRes = null },
            onDismiss = { errorRes = null }
        )
    }

    val current = account
    if (current == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            ZadrinthEmptyState(
                iconRes = R.drawable.ic_checkroom,
                title = stringResource(R.string.wardrobe_title),
                message = stringResource(R.string.wardrobe_no_account),
                actionText = stringResource(R.string.wardrobe_open_accounts),
                onAction = onOpenAccounts
            )
        }
        return
    }

    val isLocal = current.isLocalAccount()

    Row(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Prévia do jogador + skin
        PreviewPanel(
            modifier = Modifier
                .width(250.dp)
                .fillMaxHeight(),
            account = current,
            isLocal = isLocal,
            skinBytes = skinBytes,
            capeBytes = capeBytes,
            skinModel = skinModel,
            busy = busy,
            onImportSkin = { skinPicker.launch(arrayOf("image/png")) },
            onResetSkin = { runWardrobeAction { ZadrinthWardrobe.resetSkin(current) } },
            onChangeModel = { model -> runWardrobeAction { ZadrinthWardrobe.setSkinModel(current, model) } }
        )

        //Capas
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.wardrobe_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.wardrobe_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
                ModrinthSecondaryButton(
                    enabled = isLocal && !busy,
                    onClick = { capePicker.launch(arrayOf("image/png")) }
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.drawable.ic_upload),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = stringResource(R.string.wardrobe_import_cape))
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ModrinthFilterChip(
                    selected = capeSource == CapeSource.OFFICIAL,
                    text = stringResource(R.string.wardrobe_tab_official),
                    onClick = { capeSource = CapeSource.OFFICIAL }
                )
                ModrinthFilterChip(
                    selected = capeSource == CapeSource.ZADRINTH,
                    text = stringResource(R.string.wardrobe_tab_zadrinth),
                    onClick = { capeSource = CapeSource.ZADRINTH }
                )
            }

            Spacer(Modifier.height(12.dp))

            if (!isLocal) {
                NoticeCard(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.wardrobe_offline_only),
                    actionText = stringResource(R.string.wardrobe_open_accounts),
                    onAction = onOpenAccounts
                )
                Spacer(Modifier.height(12.dp))
            }

            LazyVerticalGrid(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                columns = GridCells.Adaptive(minSize = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                item(key = "none") {
                    CapeTile(
                        name = stringResource(R.string.wardrobe_no_cape),
                        image = null,
                        selected = selectedCape == CapeChoice.None,
                        enabled = isLocal && !busy,
                        onClick = { runWardrobeAction { ZadrinthWardrobe.removeCape(current) } }
                    )
                }
                if (selectedCape == CapeChoice.Custom) {
                    item(key = "custom") {
                        CapeTile(
                            name = stringResource(R.string.wardrobe_custom_cape),
                            image = rememberCapeImage(capeBytes),
                            selected = true,
                            enabled = isLocal && !busy,
                            onClick = {}
                        )
                    }
                }
                when (capeSource) {
                    CapeSource.OFFICIAL -> {
                        val capes = officialCapes
                        if (capes == null) {
                            item(key = "loading", span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = stringResource(R.string.wardrobe_loading_capes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        } else {
                            items(
                                items = capes,
                                key = { "official_" + it.hash }
                            ) { cape ->
                                OfficialCapeTile(
                                    cape = cape,
                                    reloadKey = refreshWardrobe,
                                    selected = (selectedCape as? CapeChoice.Official)?.hash == cape.hash,
                                    enabled = isLocal && !busy,
                                    onClick = {
                                        runWardrobeAction {
                                            val bytes = try {
                                                OfficialCapes.texture(cape)
                                            } catch (e: CancellationException) {
                                                throw e
                                            } catch (th: Throwable) {
                                                Logger.warning(TAG, "Failed to download cape ${cape.name}", th)
                                                throw WardrobeImageException(R.string.wardrobe_error_download)
                                            }
                                            ZadrinthWardrobe.applyCape(current, bytes)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    CapeSource.ZADRINTH -> {
                        items(
                            items = builtInCapes.entries.toList(),
                            key = { it.key.id }
                        ) { (cape, bytes) ->
                            CapeTile(
                                name = stringResource(cape.nameRes),
                                image = rememberCapeImage(bytes),
                                selected = (selectedCape as? CapeChoice.BuiltIn)?.cape == cape,
                                enabled = isLocal && !busy,
                                onClick = { runWardrobeAction { ZadrinthWardrobe.applyCape(current, bytes) } }
                            )
                        }
                    }
                }
                item(key = "note", span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.wardrobe_visibility_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        if (capeSource == CapeSource.OFFICIAL) {
                            Text(
                                text = stringResource(R.string.wardrobe_official_source),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewPanel(
    modifier: Modifier,
    account: Account,
    isLocal: Boolean,
    skinBytes: ByteArray?,
    capeBytes: ByteArray?,
    skinModel: SkinModelType,
    busy: Boolean,
    onImportSkin: () -> Unit,
    onResetSkin: () -> Unit,
    onChangeModel: (SkinModelType) -> Unit,
) {
    val colors = ModrinthTheme.colors
    ModrinthCard(modifier = modifier) {
        //Palco com um brilho suave da cor da marca
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(colors.brandHighlight.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
        ) {
            PetModelView(
                modifier = Modifier.fillMaxSize(),
                skin = skinBytes,
                cape = capeBytes,
                arm = skinModel.toArmModel(),
                //Levemente de costas para a capa aparecer
                yaw = Math.toRadians(150.0).toFloat(),
                animation = PetAnimation.Idle,
                tapReaction = PetTapReaction.Interact,
                dragYaw = true,
                gazeEnabled = false
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = account.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ModrinthTag(
                    text = stringResource(
                        if (isLocal) R.string.wardrobe_badge_offline else R.string.wardrobe_badge_online
                    )
                )
            }

            if (isLocal) {
                val hasSkin = skinBytes != null
                Text(
                    text = if (hasSkin) stringResource(R.string.wardrobe_skin)
                    else stringResource(R.string.wardrobe_default_skin),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary
                )
                if (hasSkin) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ModrinthFilterChip(
                            selected = skinModel != SkinModelType.ALEX,
                            text = stringResource(R.string.wardrobe_model_classic),
                            onClick = { if (!busy) onChangeModel(SkinModelType.STEVE) }
                        )
                        ModrinthFilterChip(
                            selected = skinModel == SkinModelType.ALEX,
                            text = stringResource(R.string.wardrobe_model_slim),
                            onClick = { if (!busy) onChangeModel(SkinModelType.ALEX) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModrinthBrandButton(
                        modifier = Modifier.weight(1f),
                        enabled = !busy,
                        onClick = onImportSkin,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.wardrobe_import_skin),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (hasSkin) {
                        ModrinthSecondaryButton(
                            enabled = !busy,
                            onClick = onResetSkin,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(text = stringResource(R.string.wardrobe_reset_skin), maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticeCard(
    modifier: Modifier,
    text: String,
    actionText: String,
    onAction: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    ModrinthCard(
        modifier = modifier,
        color = colors.brandHighlight.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, colors.brand.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.ic_checkroom),
                contentDescription = null,
                tint = colors.brand
            )
            Text(
                modifier = Modifier.weight(1f),
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary
            )
            ModrinthSecondaryButton(onClick = onAction) {
                Text(text = actionText, maxLines = 1)
            }
        }
    }
}

@Composable
private fun rememberCapeImage(bytes: ByteArray?): ImageBitmap? = remember(bytes) {
    bytes?.let {
        runCatching {
            BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
        }.getOrNull()
    }
}

/** Capa oficial: baixa a textura quando o cartão aparece na tela (e guarda no aparelho). */
@Composable
private fun OfficialCapeTile(
    cape: OfficialCape,
    reloadKey: Any?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val result by produceState<Result<ByteArray>?>(
        OfficialCapes.peekTexture(cape.hash)?.let { Result.success(it) },
        cape.hash,
        reloadKey
    ) {
        if (value?.isSuccess != true) {
            value = OfficialCapes.peekTexture(cape.hash)?.let { Result.success(it) }
                ?: try {
                    Result.success(OfficialCapes.texture(cape))
                } catch (e: CancellationException) {
                    throw e
                } catch (th: Throwable) {
                    Result.failure(th)
                }
        }
    }
    CapeTile(
        name = cape.name,
        image = rememberCapeImage(result?.getOrNull()),
        selected = selected,
        enabled = enabled,
        loading = result == null,
        failed = result?.isFailure == true,
        onClick = onClick
    )
}

@Composable
private fun CapeTile(
    name: String,
    image: ImageBitmap?,
    selected: Boolean,
    enabled: Boolean,
    loading: Boolean = false,
    failed: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.brand else colors.border,
        label = "CapeTileBorder"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CapeTileShape)
            .background(if (selected) colors.brandHighlight.copy(alpha = 0.45f) else colors.surface)
            .border(if (selected) 2.dp else 1.dp, borderColor, CapeTileShape)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(width = 50.dp, height = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                CapeFront(
                    modifier = Modifier.fillMaxSize(),
                    image = image
                )
            } else if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(
                            if (failed) R.drawable.ic_refresh else R.drawable.ic_close
                        ),
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
            }
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) colors.textPrimary else colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Desenha a face de fora da capa (a que aparece de costas), em pixel art nítida. */
@Composable
private fun CapeFront(
    modifier: Modifier,
    image: ImageBitmap,
) {
    Canvas(modifier = modifier) {
        val scale = (image.width / 64).coerceAtLeast(1)
        drawImage(
            image = image,
            srcOffset = IntOffset(scale, scale),
            srcSize = IntSize(10 * scale, 16 * scale),
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            filterQuality = FilterQuality.None
        )
    }
}
