/*
 * Zalith Launcher 2
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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.instance.InstanceStats
import com.movtery.zalithlauncher.game.instance.InstanceStatsStore
import com.movtery.zalithlauncher.game.instance.sortedByRecent
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthBrandButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthCard
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthPlayIconButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthSecondaryButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import com.movtery.zalithlauncher.ui.theme.zadrinth.ZadrinthHeroBackground
import com.movtery.zalithlauncher.ui.theme.zadrinth.ZadrinthGlass
import com.movtery.zalithlauncher.ui.theme.zadrinth.GlassSurface
import com.movtery.zalithlauncher.ui.theme.modrinth.ZadrinthBrandGradient
import com.movtery.zalithlauncher.ui.screens.content.zadrinth.servers.rememberQuickJoinVersion
import com.movtery.zalithlauncher.ui.screens.content.zadrinth.servers.rememberQuickJoin
import com.movtery.zalithlauncher.ui.screens.content.zadrinth.servers.rememberFeaturedServers
import com.movtery.zalithlauncher.ui.screens.content.zadrinth.servers.ServerStatusLine
import com.movtery.zalithlauncher.ui.screens.content.zadrinth.servers.ServerFavicon
import com.movtery.zalithlauncher.game.servers.FeaturedServerStatus
import com.movtery.zalithlauncher.game.servers.FeaturedServer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import com.movtery.zalithlauncher.game.zadrinth.ZadrinthClientInstaller
import com.movtery.zalithlauncher.game.zadrinth.ZadrinthClientMod

/**
 * Tela Início do Zadrinth Client: herói animado com o botão JOGAR, faixa de
 * servidores (entrar rápido), instâncias recentes e o painel de Novidades.
 */
@Composable
fun ZadrinthHomeScreen(
    backStackViewModel: ScreenBackStackViewModel,
    onPlayInstance: (Version) -> Unit,
    onOpenInstance: (Version) -> Unit,
    onOpenLibrary: () -> Unit,
    onCreateInstance: () -> Unit,
    onOpenLink: (String) -> Unit,
    onJoinServer: (Version, FeaturedServer) -> Unit,
    onOpenServers: () -> Unit,
) {
    BaseScreen(
        screenKey = NormalNavKey.LauncherMain,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                HomeMain(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onPlayInstance = onPlayInstance,
                    onOpenInstance = onOpenInstance,
                    onOpenLibrary = onOpenLibrary,
                    onCreateInstance = onCreateInstance,
                    onJoinServer = onJoinServer,
                    onOpenServers = onOpenServers
                )
                MinecraftNewsPanel(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                        .padding(top = 12.dp, end = 12.dp, bottom = 12.dp),
                    onOpenLink = onOpenLink
                )
            }
        }
    }
}

@Composable
private fun HomeMain(
    onPlayInstance: (Version) -> Unit,
    onOpenInstance: (Version) -> Unit,
    onOpenLibrary: () -> Unit,
    onCreateInstance: () -> Unit,
    onJoinServer: (Version, FeaturedServer) -> Unit,
    onOpenServers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val stats by InstanceStatsStore.stats.collectAsStateWithLifecycle()
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()

    val validVersions = remember(versions) { versions.filter { it.isValid() } }
    val recent = remember(validVersions, stats) { validVersions.sortedByRecent(stats).take(4) }
    val quickPlay = rememberQuickJoinVersion()

    val (servers, serverStatus) = rememberFeaturedServers()
    val joinServer = rememberQuickJoin(
        version = quickPlay,
        onJoin = onJoinServer,
        onCreateInstance = onCreateInstance
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HomeHero(
            modifier = Modifier.fillMaxWidth(),
            playerName = account?.username ?: stringResource(R.string.home_greeting_default_name),
            version = quickPlay,
            versions = validVersions,
            onPlay = { quickPlay?.let(onPlayInstance) ?: onCreateInstance() },
            onSelectVersion = { VersionsManager.saveVersion(it) }
        )

        ZadrinthClientCard(
            modifier = Modifier.fillMaxWidth(),
            versions = validVersions,
            onPlay = onPlayInstance
        )

        if (servers.isNotEmpty()) {
            ServerStrip(
                modifier = Modifier.fillMaxWidth(),
                servers = servers,
                status = serverStatus,
                onJoin = joinServer,
                onOpenServers = onOpenServers
            )
        }

        if (recent.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.home_your_instances)
                )
                TextButton(onClick = onOpenLibrary) {
                    Text(text = stringResource(R.string.home_see_all))
                }
            }
            recent.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { version ->
                        InstanceTile(
                            modifier = Modifier.weight(1f),
                            version = version,
                            stats = stats[version.getVersionName()],
                            onOpen = { onOpenInstance(version) },
                            onPlay = { onPlayInstance(version) }
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = ModrinthTheme.colors.textPrimary
    )
}

private val HeroShape = RoundedCornerShape(18.dp)

/**
 * Herói da tela inicial: céu animado, saudação e o botão grande JOGAR
 * (com a seta para trocar de instância).
 */
@Composable
private fun HomeHero(
    playerName: String,
    version: Version?,
    versions: List<Version>,
    onPlay: () -> Unit,
    onSelectVersion: (Version) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(220.dp)
            .clip(HeroShape)
            .border(1.dp, ZadrinthGlass.Border, HeroShape)
    ) {
        ZadrinthHeroBackground(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.home_hero_brand),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 3.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
            Text(
                text = stringResource(R.string.home_greeting, playerName),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HeroPlayButton(
                    version = version,
                    versions = versions,
                    onPlay = onPlay,
                    onSelectVersion = onSelectVersion
                )
            }
        }
    }
}

@Composable
private fun HeroPlayButton(
    version: Version?,
    versions: List<Version>,
    onPlay: () -> Unit,
    onSelectVersion: (Version) -> Unit,
) {
    val colors = ModrinthTheme.colors
    val shape = RoundedCornerShape(16.dp)
    var menuOpen by remember { mutableStateOf(false) }

    //Brilho pulsando atrás do botão
    val glow by rememberInfiniteTransition(label = "PlayGlow").animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1_600), RepeatMode.Reverse),
        label = "PlayGlowAlpha"
    )

    Box {
        Row(
            modifier = Modifier
                .drawBehind {
                    val spread = 10.dp.toPx()
                    drawRoundRect(
                        brush = ZadrinthBrandGradient,
                        topLeft = androidx.compose.ui.geometry.Offset(-spread, -spread / 2),
                        size = androidx.compose.ui.geometry.Size(size.width + spread * 2, size.height + spread),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                        alpha = glow * 0.35f
                    )
                }
                .clip(shape)
                .background(colors.brandBrush)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onPlay)
                    .padding(horizontal = 30.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (version != null) stringResource(R.string.home_hero_play)
                    else stringResource(R.string.home_hero_create).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    color = colors.onBrandBrush
                )
                Text(
                    text = version?.getVersionName() ?: stringResource(R.string.home_no_instances),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onBrandBrush.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 200.dp)
                )
            }
            if (version != null && versions.size > 1) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(colors.onBrandBrush.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable(role = Role.Button) { menuOpen = true }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                        contentDescription = stringResource(R.string.home_hero_change),
                        tint = colors.onBrandBrush
                    )
                }
            }
        }
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false }
        ) {
            versions.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.getVersionName(),
                            fontWeight = if (item == version) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = { InstanceIconBox(version = item, size = 24.dp) },
                    onClick = {
                        menuOpen = false
                        onSelectVersion(item)
                    }
                )
            }
        }
    }
}

/** Faixa de servidores logo abaixo do herói: um toque entra direto no servidor. */
@Composable
private fun ServerStrip(
    servers: List<FeaturedServer>,
    status: Map<String, FeaturedServerStatus>,
    onJoin: (FeaturedServer) -> Unit,
    onOpenServers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors
    GlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.servers_featured),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.servers_tap_to_join),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
                TextButton(onClick = onOpenServers) {
                    Text(text = stringResource(R.string.servers_see_all))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                servers.forEach { server ->
                    val online = status[server.id] as? FeaturedServerStatus.Online
                    Column(
                        modifier = Modifier
                            .width(92.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(role = Role.Button) { onJoin(server) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ServerFavicon(
                            name = server.name,
                            favicon = online?.favicon,
                            size = 48.dp
                        )
                        Text(
                            text = server.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        ServerStatusLine(status = status[server.id], showPing = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun InstanceTile(
    version: Version,
    stats: InstanceStats?,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors
    ModrinthCard(
        modifier = modifier,
        onClick = onOpen
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InstanceIconBox(version = version, size = 44.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    InstanceTags(version = version)
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.border)
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = instanceModsText(stats?.modsCount ?: 0),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    maxLines = 1
                )
                ModrinthPlayIconButton(
                    onClick = onPlay,
                    size = 32.dp,
                    contentDescription = stringResource(R.string.library_play_instance, version.getVersionName())
                )
            }
        }
    }
}


private val ClientCardShape = RoundedCornerShape(16.dp)

/**
 * Cartão do Zadrinth Client: se ainda não existe uma instância do client,
 * instala o Minecraft 1.8.9 com tudo pronto em um toque; se já existe, JOGAR.
 */
@Composable
private fun ZadrinthClientCard(
    versions: List<Version>,
    onPlay: (Version) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by ZadrinthClientInstaller.state.collectAsStateWithLifecycle()
    val clientVersion = remember(versions) {
        versions.firstOrNull { it.getVersionName() == ZadrinthClientMod.INSTANCE_NAME && ZadrinthClientMod.isSupported(it) }
            ?: versions.firstOrNull { ZadrinthClientMod.isSupported(it) }
    }

    Row(
        modifier = modifier
            .clip(ClientCardShape)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF1B1433), Color(0xFF2A1D55), Color(0xFF3A1F5C))
                )
            )
            .border(1.dp, Color(0x669D7BFF), ClientCardShape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.zadrinth_client_logo),
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ZADRINTH CLIENT 1.8.9",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.White
            )
            val subtitle = when (val current = state) {
                is ZadrinthClientInstaller.State.Working -> "Instalando o Minecraft 1.8.9 com o client... (pode levar alguns minutos)"
                is ZadrinthClientInstaller.State.Failed -> "Não deu certo: ${current.message}"
                else -> if (clientVersion != null) {
                    "Mods de PvP, HUD, zoom e cosméticos já embutidos. É só jogar."
                } else {
                    "O client de PvP do Zadrinth: instala tudo pronto, em um toque."
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (state is ZadrinthClientInstaller.State.Failed) Color(0xFFFCA5A5) else Color.White.copy(alpha = 0.72f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(12.dp))
        when {
            state is ZadrinthClientInstaller.State.Working -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFE07BFF)
                    )
                    TextButton(onClick = { ZadrinthClientInstaller.cancel() }) {
                        Text(text = "Cancelar", color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
            clientVersion != null -> ModrinthBrandButton(onClick = { onPlay(clientVersion) }) {
                Text(text = "JOGAR", fontWeight = FontWeight.Bold)
            }
            else -> ModrinthBrandButton(onClick = { ZadrinthClientInstaller.install(context) }) {
                Text(
                    text = if (state is ZadrinthClientInstaller.State.Failed) "TENTAR DE NOVO" else "INSTALAR",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
