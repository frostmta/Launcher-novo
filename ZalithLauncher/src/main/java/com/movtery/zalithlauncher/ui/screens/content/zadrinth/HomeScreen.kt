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
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.rememberCoroutineScope
import com.movtery.zalithlauncher.game.zadrinth.OptiFineSupport
import com.movtery.zalithlauncher.setting.AllSettings
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val installState by ZadrinthClientInstaller.state.collectAsStateWithLifecycle()
    //O Zadrinth Client é o jogo principal do launcher
    val clientVersion = remember(validVersions) {
        validVersions.firstOrNull { it.getVersionName() == ZadrinthClientMod.INSTANCE_NAME && ZadrinthClientMod.isSupported(it) }
            ?: validVersions.firstOrNull { ZadrinthClientMod.isSupported(it) }
    }

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
            clientVersion = clientVersion,
            installState = installState,
            otherVersions = validVersions.filter { it != clientVersion },
            onPlay = { if (clientVersion != null) onPlayInstance(clientVersion) else ZadrinthClientInstaller.install(context) },
            onPlayOther = onPlayInstance,
            onCancelInstall = { ZadrinthClientInstaller.cancel() }
        )

        ZadrinthToolsRow(
            modifier = Modifier.fillMaxWidth(),
            clientVersion = clientVersion
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
    clientVersion: Version?,
    installState: ZadrinthClientInstaller.State,
    otherVersions: List<Version>,
    onPlay: () -> Unit,
    onPlayOther: (Version) -> Unit,
    onCancelInstall: () -> Unit,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.zadrinth_client_logo),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
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
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val working = installState is ZadrinthClientInstaller.State.Working
                val failed = installState as? ZadrinthClientInstaller.State.Failed
                HeroPlayButton(
                    title = when {
                        working -> "INSTALANDO..."
                        clientVersion != null -> stringResource(R.string.home_hero_play)
                        failed != null -> "TENTAR DE NOVO"
                        else -> "INSTALAR"
                    },
                    subtitle = when {
                        working -> "Baixando o Minecraft 1.8.9 com o client"
                        failed != null -> failed.message
                        else -> "Zadrinth Client 1.8.9"
                    },
                    busy = working,
                    onPlay = onPlay,
                    onCancel = onCancelInstall,
                    otherVersions = otherVersions,
                    onPlayOther = onPlayOther
                )
            }
        }
    }
}

/**
 * Botão principal: joga (ou instala) o Zadrinth Client. A setinha ao lado
 * abre as outras instâncias para jogar uma delas.
 */
@Composable
private fun HeroPlayButton(
    title: String,
    subtitle: String,
    busy: Boolean,
    onPlay: () -> Unit,
    onCancel: () -> Unit,
    otherVersions: List<Version>,
    onPlayOther: (Version) -> Unit,
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
            Row(
                modifier = Modifier
                    .clickable(role = Role.Button, enabled = !busy, onClick = onPlay)
                    .padding(horizontal = 30.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.onBrandBrush
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp,
                        color = colors.onBrandBrush
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onBrandBrush.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 220.dp)
                    )
                }
            }
            if (busy || otherVersions.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(colors.onBrandBrush.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable(role = Role.Button) { if (busy) onCancel() else menuOpen = true }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (busy) {
                        Text(text = "✕", color = colors.onBrandBrush, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                            contentDescription = stringResource(R.string.home_hero_change),
                            tint = colors.onBrandBrush
                        )
                    }
                }
            }
        }
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false }
        ) {
            otherVersions.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = "Jogar ${item.getVersionName()}") },
                    leadingIcon = { InstanceIconBox(version = item, size = 24.dp) },
                    onClick = {
                        menuOpen = false
                        onPlayOther(item)
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


private val ToolChipShape = RoundedCornerShape(12.dp)

/** Atalhos do client: modo celular fraco (resolução menor) e OptiFine. */
@Composable
private fun ZadrinthToolsRow(
    clientVersion: Version?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val ratio = AllSettings.resolutionRatio.state
    val lowEnd = ratio <= LOW_END_RATIO
    var optiFineOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolChip(
            text = if (lowEnd) "⚡ Modo celular fraco: LIGADO" else "⚡ Modo celular fraco",
            active = lowEnd,
            onClick = {
                val value = if (lowEnd) 100 else LOW_END_RATIO
                AllSettings.resolutionRatio.updateState(value)
                AllSettings.resolutionRatio.save(value)
                Toast.makeText(
                    context,
                    if (lowEnd) "Resolução normal (100%)" else "Resolução em $LOW_END_RATIO%: mais FPS. Ligue também o FPS Boost no menu do client.",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
        ToolChip(
            text = if (clientVersion != null && OptiFineSupport.hasOptiFine(clientVersion) && OptiFineSupport.hasOptiFabric(clientVersion)) "OptiFine: instalado" else "OptiFine",
            active = false,
            onClick = {
                if (clientVersion == null) {
                    Toast.makeText(context, "Instale o Zadrinth Client primeiro", Toast.LENGTH_SHORT).show()
                } else {
                    optiFineOpen = true
                }
            }
        )
    }

    if (optiFineOpen && clientVersion != null) {
        OptiFineDialog(version = clientVersion, onDismiss = { optiFineOpen = false })
    }
}

private const val LOW_END_RATIO = 60

@Composable
private fun ToolChip(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(ToolChipShape)
            .background(if (active) Color(0x559D7BFF) else Color(0x22FFFFFF))
            .border(1.dp, if (active) Color(0xFF9D7BFF) else Color(0x33FFFFFF), ToolChipShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OptiFineDialog(version: Version, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(0) }
    val hasFabric = remember(refresh) { OptiFineSupport.hasOptiFabric(version) }
    val hasOptiFine = remember(refresh) { OptiFineSupport.hasOptiFine(version) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = true
        scope.launch {
            status = runCatching { "Copiado: " + OptiFineSupport.importOptiFine(context, uri, version) }
                .getOrElse { "Erro ao copiar: ${it.message}" }
            busy = false
            refresh++
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
        title = { Text("OptiFine no Zadrinth Client") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("O OptiFine roda junto com o client. São 3 passos:")
                OptiFineStep(
                    done = hasFabric,
                    text = "1. Baixar o OptiFabric (faz o OptiFine funcionar no client)",
                    button = "Baixar",
                    enabled = !busy
                ) {
                    busy = true
                    scope.launch {
                        status = runCatching { "Baixado: " + OptiFineSupport.installOptiFabric(version) }
                            .getOrElse { "Erro ao baixar: ${it.message}" }
                        busy = false
                        refresh++
                    }
                }
                OptiFineStep(
                    done = hasOptiFine,
                    text = "2. No site oficial, baixe o \"OptiFine 1.8.9 HD U M5\" (o OptiFine não pode vir dentro do app)",
                    button = "Abrir site",
                    enabled = true
                ) {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OptiFineSupport.DOWNLOAD_PAGE)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
                OptiFineStep(
                    done = hasOptiFine,
                    text = "3. Escolha o arquivo .jar que você baixou",
                    button = "Escolher",
                    enabled = !busy
                ) { picker.launch("*/*") }
                if (status.isNotEmpty()) Text(text = status, fontSize = 12.sp)
                if (hasFabric && hasOptiFine) {
                    Text(
                        text = "Pronto! O OptiFine vai abrir junto na próxima vez que você jogar. O zoom do OptiFine fica na tecla C.",
                        fontSize = 12.sp,
                        color = Color(0xFF4ADE80)
                    )
                }
            }
        }
    )
}

@Composable
private fun OptiFineStep(done: Boolean, text: String, button: String, enabled: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = (if (done) "✔ " else "") + text,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onClick, enabled = enabled) { Text(button) }
    }
}
