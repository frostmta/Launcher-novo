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

package com.movtery.zalithlauncher.ui.screens.content.zadrinth.servers

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.servers.FeaturedServer
import com.movtery.zalithlauncher.game.servers.FeaturedServerStatus
import com.movtery.zalithlauncher.game.servers.FeaturedServers
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme
import com.movtery.zalithlauncher.ui.theme.zadrinth.GlassSurface
import com.movtery.zalithlauncher.ui.theme.zadrinth.ZadrinthGlass
import java.text.NumberFormat

/** Carrega o catálogo e pinga os servidores enquanto a tela estiver aberta. */
@Composable
fun rememberFeaturedServers(): Pair<List<FeaturedServer>, Map<String, FeaturedServerStatus>> {
    val context = LocalContext.current
    val servers by FeaturedServers.servers.collectAsStateWithLifecycle()
    val status by FeaturedServers.status.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        FeaturedServers.loadAndRefresh(context.applicationContext)
    }
    return servers to status
}

/** Instância usada no Entrar rápido: a selecionada ou a primeira válida. */
@Composable
fun rememberQuickJoinVersion(): Version? {
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val current by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    return current?.takeIf { it.isValid() } ?: versions.firstOrNull { it.isValid() }
}

/**
 * Entrar rápido: devolve a ação de entrar num servidor. Avisa antes quando o
 * servidor exige conta original e o jogador está com conta offline.
 */
@Composable
fun rememberQuickJoin(
    version: Version?,
    onJoin: (Version, FeaturedServer) -> Unit,
    onCreateInstance: () -> Unit,
): (FeaturedServer) -> Unit {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    var pendingPremium by remember { mutableStateOf<FeaturedServer?>(null) }

    pendingPremium?.let { server ->
        SimpleAlertDialog(
            title = stringResource(R.string.servers_premium_title, server.name),
            text = stringResource(R.string.servers_premium_text),
            confirmText = stringResource(R.string.servers_join_anyway),
            onConfirm = {
                pendingPremium = null
                version?.let { onJoin(it, server) }
            },
            onDismiss = { pendingPremium = null }
        )
    }

    return { server ->
        when {
            version == null -> onCreateInstance()
            server.premium && account?.isLocalAccount() != false -> pendingPremium = server
            else -> onJoin(version, server)
        }
    }
}

/** Ícone do servidor (o mesmo da lista de servidores do Minecraft), em pixel art nítida. */
@Composable
fun ServerFavicon(
    name: String,
    favicon: ByteArray?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val image: ImageBitmap? = remember(favicon) {
        favicon?.let { bytes ->
            runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }.getOrNull()
        }
    }
    val shape = RoundedCornerShape(size * 0.22f)
    if (image != null) {
        Image(
            modifier = modifier
                .size(size)
                .clip(shape),
            bitmap = image,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.None
        )
    } else {
        GlassSurface(
            modifier = modifier.size(size),
            shape = shape,
            strong = true
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/** "● 1.234 online · 45 ms" ou "Offline". */
@Composable
fun ServerStatusLine(
    status: FeaturedServerStatus?,
    modifier: Modifier = Modifier,
    showPing: Boolean = true,
) {
    val colors = ModrinthTheme.colors
    val (dotColor, text) = when (status) {
        is FeaturedServerStatus.Online -> {
            val players = NumberFormat.getIntegerInstance().format(status.playersOnline)
            val base = stringResource(R.string.servers_online, players)
            ZadrinthGlass.Online to if (showPing) {
                base + "  ·  " + stringResource(R.string.servers_ping, status.pingMs.toInt())
            } else base
        }
        FeaturedServerStatus.Offline -> colors.red to stringResource(R.string.servers_offline)
        else -> colors.textSecondary to stringResource(R.string.servers_checking)
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            maxLines = 1
        )
    }
}
