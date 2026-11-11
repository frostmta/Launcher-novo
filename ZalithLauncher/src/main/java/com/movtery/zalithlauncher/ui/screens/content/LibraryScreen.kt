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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.instance.InstanceMetadata
import com.movtery.zalithlauncher.game.instance.InstanceMetadataStore
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.ModLoaderIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthBrandButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthCard
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthDimens
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthFilterChip
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthIconShape
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthPlayIconButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthSearchField
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTag
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "LibraryScreen"

private enum class LibraryFilter(@StringRes val textRes: Int) {
    ALL(R.string.library_filter_all),
    VANILLA(R.string.library_filter_vanilla),
    MODDED(R.string.library_filter_modded),
}

/**
 * Biblioteca de instâncias no estilo Modrinth App: grade de cards com ícone,
 * nome, loader + versão do jogo, tempo jogado e botão rápido de "Jogar".
 *
 * Cada instância é uma [Version] do Zalith (com sua própria pasta de jogo),
 * então abrir e jogar usam exatamente o mesmo fluxo de antes.
 */
@Composable
fun LibraryScreen(
    backStackViewModel: ScreenBackStackViewModel,
    onOpenInstance: (Version) -> Unit,
    onPlayInstance: (Version) -> Unit,
    onCreateInstance: () -> Unit,
    onManageInvalid: () -> Unit,
) {
    BaseScreen(
        screenKey = NormalNavKey.Library,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LibraryContent(
                modifier = Modifier.fillMaxSize(),
                onOpenInstance = onOpenInstance,
                onPlayInstance = onPlayInstance,
                onCreateInstance = onCreateInstance,
                onManageInvalid = onManageInvalid
            )
        }
    }
}

@Composable
private fun LibraryContent(
    modifier: Modifier = Modifier,
    onOpenInstance: (Version) -> Unit,
    onPlayInstance: (Version) -> Unit,
    onCreateInstance: () -> Unit,
    onManageInvalid: () -> Unit,
) {
    val colors = ModrinthTheme.colors

    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(LibraryFilter.ALL) }

    //instance.json de cada instância, lido fora da thread principal
    val metadata by produceState(initialValue = emptyMap<String, InstanceMetadata>(), versions) {
        value = withContext(Dispatchers.IO) {
            versions.mapNotNull { version ->
                InstanceMetadataStore.read(version)?.let { version.getVersionName() to it }
            }.toMap()
        }
    }

    val shown = remember(versions, query, filter, metadata) {
        val search = query.trim()
        versions
            .filter { version ->
                search.isEmpty() || version.getVersionName().contains(search, ignoreCase = true)
            }
            .filter { version ->
                val hasLoader = version.getVersionInfo()?.primaryLoader != null
                when (filter) {
                    LibraryFilter.ALL -> true
                    LibraryFilter.VANILLA -> version.isValid() && !hasLoader
                    LibraryFilter.MODDED -> hasLoader
                }
            }
            .sortedWith(
                compareByDescending<Version> { it.pinnedState }
                    .thenByDescending { metadata[it.getVersionName()]?.lastPlayed ?: 0L }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.getVersionName() }
            )
    }

    Column(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
    ) {
        //Cabeçalho
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.library_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary
            )
            if (versions.isNotEmpty()) {
                ModrinthTag(text = versions.size.toString())
            }
            Spacer(Modifier.weight(1f))
            IconButton(
                enabled = !isRefreshing,
                onClick = { VersionsManager.refresh(TAG) }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = stringResource(R.string.library_refresh),
                    tint = colors.textSecondary
                )
            }
            ModrinthBrandButton(onClick = onCreateInstance) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.nav_create_instance),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        //Busca + filtros
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModrinthSearchField(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(min = 160.dp, max = 360.dp),
                value = query,
                onValueChange = { query = it },
                hint = stringResource(R.string.library_search_hint)
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LibraryFilter.entries.forEach { entry ->
                    ModrinthFilterChip(
                        selected = filter == entry,
                        text = stringResource(entry.textRes),
                        onClick = { filter = entry }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                versions.isEmpty() && isRefreshing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colors.brand
                    )
                }
                versions.isEmpty() -> {
                    LibraryEmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        onCreateInstance = onCreateInstance
                    )
                }
                shown.isEmpty() -> {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        text = stringResource(R.string.library_no_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = shown,
                            key = { it.getVersionName() }
                        ) { version ->
                            InstanceCard(
                                version = version,
                                metadata = metadata[version.getVersionName()],
                                isCurrent = version.getVersionName() == currentVersion?.getVersionName(),
                                onOpen = {
                                    if (version.isValid()) onOpenInstance(version) else onManageInvalid()
                                },
                                onPlay = { onPlayInstance(version) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstanceCard(
    version: Version,
    metadata: InstanceMetadata?,
    isCurrent: Boolean,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors
    val isValid = version.isValid()
    val versionInfo = version.getVersionInfo()
    val primaryLoader = versionInfo?.primaryLoader

    ModrinthCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onOpen,
        border = BorderStroke(
            width = ModrinthDimens.BorderWidth,
            color = if (isCurrent) colors.brand.copy(alpha = 0.6f) else colors.border
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(ModrinthIconShape)
                        .background(colors.surfaceRaised),
                    contentAlignment = Alignment.Center
                ) {
                    VersionIconImage(
                        version = version,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            !isValid || versionInfo == null -> {
                                ModrinthTag(
                                    text = stringResource(R.string.library_invalid),
                                    color = colors.red.copy(alpha = 0.16f),
                                    contentColor = colors.red
                                )
                            }
                            primaryLoader != null -> {
                                LoaderTag(
                                    modifier = Modifier.weight(1f, fill = false),
                                    loader = primaryLoader.loader
                                )
                            }
                            else -> {
                                ModrinthTag(
                                    modifier = Modifier.weight(1f, fill = false),
                                    text = stringResource(R.string.library_filter_vanilla)
                                )
                            }
                        }
                        versionInfo?.minecraftVersion?.takeIf { it.isNotBlank() }?.let { mcVersion ->
                            ModrinthTag(
                                modifier = Modifier.weight(1f, fill = false),
                                text = mcVersion
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_schedule_outlined),
                    contentDescription = null,
                    tint = colors.textSecondary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = formatPlayTime(metadata?.timePlayedSeconds ?: 0L),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ModrinthPlayIconButton(
                    onClick = onPlay,
                    enabled = isValid,
                    contentDescription = stringResource(R.string.library_play_instance, version.getVersionName())
                )
            }
        }
    }
}

@Composable
private fun LoaderTag(
    loader: ModLoader,
    modifier: Modifier = Modifier,
) {
    ModrinthTag(
        modifier = modifier,
        text = loader.displayName,
        icon = {
            ModLoaderIcon(
                modifier = Modifier.size(14.dp),
                modloader = loader,
                defaultIcon = R.drawable.img_minecraft
            )
        }
    )
}

@Composable
private fun LibraryEmptyState(
    onCreateInstance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors
    Column(
        modifier = modifier
            .widthIn(max = 360.dp)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(R.drawable.ic_library_filled),
            contentDescription = null,
            tint = colors.textSecondary
        )
        Text(
            text = stringResource(R.string.library_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.library_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        ModrinthBrandButton(onClick = onCreateInstance) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.nav_create_instance),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/** Formata o tempo jogado como no Modrinth ("12h 30min", "45 min") */
@Composable
private fun formatPlayTime(seconds: Long): String {
    return when {
        seconds <= 0L -> stringResource(R.string.library_never_played)
        seconds < 60L -> stringResource(R.string.library_time_less_minute)
        seconds < 3600L -> stringResource(R.string.library_time_minutes, seconds / 60L)
        else -> stringResource(
            R.string.library_time_hours_minutes,
            seconds / 3600L,
            (seconds % 3600L) / 60L
        )
    }
}
