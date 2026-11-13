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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.servers.FeaturedServer
import com.movtery.zalithlauncher.game.servers.FeaturedServerStatus
import com.movtery.zalithlauncher.game.servers.FeaturedServers
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.versions.DescriptionTextRender
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthBrandButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthFilterChip
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTag
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme
import com.movtery.zalithlauncher.ui.theme.zadrinth.GlassSurface
import com.movtery.zalithlauncher.ui.theme.zadrinth.ZadrinthGlass
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.launch

/**
 * Aba Servidores: servidores em destaque com status ao vivo e o botão
 * Entrar, que abre o Minecraft já conectando no servidor.
 */
@Composable
fun ServersScreen(
    backStackViewModel: ScreenBackStackViewModel,
    onJoin: (Version, FeaturedServer) -> Unit,
    onCreateInstance: () -> Unit,
    onChangeInstance: () -> Unit,
) {
    BaseScreen(
        screenKey = NormalNavKey.Servers,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ServersContent(
                modifier = Modifier.fillMaxSize(),
                onJoin = onJoin,
                onCreateInstance = onCreateInstance,
                onChangeInstance = onChangeInstance
            )
        }
    }
}

@Composable
private fun ServersContent(
    modifier: Modifier,
    onJoin: (Version, FeaturedServer) -> Unit,
    onCreateInstance: () -> Unit,
    onChangeInstance: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val scope = rememberCoroutineScope()
    val (servers, status) = rememberFeaturedServers()
    val version = rememberQuickJoinVersion()
    val join = rememberQuickJoin(version = version, onJoin = onJoin, onCreateInstance = onCreateInstance)

    var tagFilter by rememberSaveable { mutableStateOf<String?>(null) }
    val tags = remember(servers) { servers.flatMap { it.tags }.distinct() }
    val visible = remember(servers, tagFilter) {
        tagFilter?.let { tag -> servers.filter { tag in it.tags } } ?: servers
    }

    Column(modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
        //Cabeçalho
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.servers_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary
                )
                Text(
                    text = stringResource(R.string.servers_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
            IconButton(onClick = { scope.launch { FeaturedServers.refresh(force = true) } }) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = stringResource(R.string.generic_refresh),
                    tint = colors.textSecondary
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        //Instância usada + filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InstanceChip(
                text = version?.let { stringResource(R.string.servers_play_with, it.getVersionName()) }
                    ?: stringResource(R.string.servers_no_instance),
                onClick = { if (version == null) onCreateInstance() else onChangeInstance() }
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(1.dp)
                    .height(20.dp)
                    .background(ZadrinthGlass.Divider)
            )
            ModrinthFilterChip(
                selected = tagFilter == null,
                text = stringResource(R.string.servers_filter_all),
                onClick = { tagFilter = null }
            )
            tags.forEach { tag ->
                ModrinthFilterChip(
                    selected = tagFilter == tag,
                    text = tag,
                    onClick = { tagFilter = if (tagFilter == tag) null else tag }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            columns = GridCells.Adaptive(minSize = 320.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items = visible, key = { it.id }) { server ->
                ServerCard(
                    server = server,
                    status = status[server.id],
                    onJoin = { join(server) }
                )
            }
        }
    }
}

@Composable
private fun InstanceChip(
    text: String,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    GlassSurface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        strong = true
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(R.drawable.ic_sports_esports_outlined),
                contentDescription = null,
                tint = colors.brand
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary,
                maxLines = 1
            )
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                contentDescription = null,
                tint = colors.textSecondary
            )
        }
    }
}

@Composable
private fun ServerCard(
    server: FeaturedServer,
    status: FeaturedServerStatus?,
    onJoin: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val online = status as? FeaturedServerStatus.Online
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onJoin),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ServerFavicon(
                name = server.name,
                favicon = online?.favicon,
                size = 64.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f, fill = false),
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    server.region?.let { region ->
                        Text(
                            text = region,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = colors.textSecondary
                        )
                    }
                }
                Text(
                    text = server.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    maxLines = 1
                )
                //MOTD com as cores do próprio servidor
                online?.motd?.let { motd ->
                    DescriptionTextRender(
                        description = motd,
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (server.premium) {
                        ModrinthTag(
                            text = stringResource(R.string.servers_premium),
                            color = ZadrinthGlass.Warning.copy(alpha = 0.18f),
                            contentColor = ZadrinthGlass.Warning
                        )
                    }
                    server.tags.forEach { tag -> ModrinthTag(text = tag) }
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ServerStatusLine(
                        modifier = Modifier.weight(1f),
                        status = status
                    )
                    ModrinthBrandButton(
                        onClick = onJoin,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(R.drawable.ic_play_arrow_filled),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(text = stringResource(R.string.servers_join))
                    }
                }
            }
        }
    }
}
