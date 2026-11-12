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

/**
 * Tela Início no estilo do SKlauncher: saudação, "Jogar rápido" com a
 * instância selecionada, instâncias recentes e o painel de Novidades.
 */
@Composable
fun ZadrinthHomeScreen(
    backStackViewModel: ScreenBackStackViewModel,
    onPlayInstance: (Version) -> Unit,
    onOpenInstance: (Version) -> Unit,
    onOpenLibrary: () -> Unit,
    onCreateInstance: () -> Unit,
    onOpenLink: (String) -> Unit,
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
                    onCreateInstance = onCreateInstance
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
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val stats by InstanceStatsStore.stats.collectAsStateWithLifecycle()
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()

    val recent = remember(versions, stats) {
        versions.filter { it.isValid() }.sortedByRecent(stats).take(4)
    }
    val quickPlay = currentVersion?.takeIf { it.isValid() } ?: recent.firstOrNull()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = stringResource(
                    R.string.home_greeting,
                    account?.username ?: stringResource(R.string.home_greeting_default_name)
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }

        SectionTitle(text = stringResource(R.string.home_quick_play))
        QuickPlayCard(
            version = quickPlay,
            stats = quickPlay?.let { stats[it.getVersionName()] },
            onPlay = { quickPlay?.let(onPlayInstance) },
            onOpen = { quickPlay?.let(onOpenInstance) },
            onChange = onOpenLibrary,
            onCreateInstance = onCreateInstance
        )

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

@Composable
private fun QuickPlayCard(
    version: Version?,
    stats: InstanceStats?,
    onPlay: () -> Unit,
    onOpen: () -> Unit,
    onChange: () -> Unit,
    onCreateInstance: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    ModrinthCard(modifier = Modifier.fillMaxWidth()) {
        if (version == null) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.home_no_instances),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                ModrinthBrandButton(onClick = onCreateInstance) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = stringResource(R.string.nav_create_instance))
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Botão grande "Jogar" com o degradê da marca
                val playShape = RoundedCornerShape(14.dp)
                Row(
                    modifier = Modifier
                        .widthIn(min = 180.dp, max = 260.dp)
                        .heightIn(min = 60.dp)
                        .clip(playShape)
                        .background(colors.brandBrush)
                        .clickable(role = Role.Button, onClick = onPlay)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(26.dp),
                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                        contentDescription = null,
                        tint = colors.onBrandBrush
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.modrinth_play),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onBrandBrush
                        )
                        Text(
                            text = version.getVersionName(),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onBrandBrush.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(colors.border)
                )
                Spacer(Modifier.width(14.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpen)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InstanceIconBox(version = version, size = 40.dp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.home_selected_instance),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                        Text(
                            text = version.getVersionName(),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(
                                R.string.instance_last_played,
                                instanceLastPlayedText(stats?.lastPlayed ?: 0L)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))
                ModrinthSecondaryButton(onClick = onChange) {
                    Text(text = stringResource(R.string.home_change_instance))
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
