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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.instance.InstanceStatsStore
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.saves.SaveData
import com.movtery.zalithlauncher.game.version.saves.parseLevelDatFile
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthBrandButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthCard
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthFilterChip
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthPlayButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthSearchField
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "InstanceScreen"
private const val DISABLED_SUFFIX = ".disabled"

private enum class InstanceTab(@StringRes val labelRes: Int, @DrawableRes val iconRes: Int) {
    CONTENT(R.string.instance_tab_content, R.drawable.ic_extension_outlined),
    WORLDS(R.string.instance_tab_worlds, R.drawable.ic_public),
    SCREENSHOTS(R.string.instance_tab_screenshots, R.drawable.ic_image_outlined),
    FILES(R.string.instance_tab_files, R.drawable.ic_folder_outlined),
}

private enum class ContentType(val folder: VersionFolders, @StringRes val labelRes: Int) {
    MODS(VersionFolders.MOD, R.string.instance_content_mods),
    RESOURCE_PACKS(VersionFolders.RESOURCE_PACK, R.string.instance_content_resource_packs),
    SHADERS(VersionFolders.SHADERS, R.string.instance_content_shaders),
}

/** Um mod, resource pack ou shader instalado */
private data class ContentEntry(
    val file: File,
    val displayName: String,
    val enabled: Boolean,
    val sizeBytes: Long,
) {
    /** Pastas não podem ser desativadas renomeando (o jogo carrega qualquer pasta) */
    val canToggle: Boolean get() = file.isFile
}

/**
 * Página de uma instância no estilo do SKlauncher: cabeçalho com Jogar,
 * status / último jogo / tempo jogado e abas Conteúdo, Mundos, Capturas e Arquivos.
 */
@Composable
fun ZadrinthInstanceScreen(
    key: NormalNavKey.Instance,
    backStackViewModel: ScreenBackStackViewModel,
    onBack: () -> Unit,
    onPlay: (Version) -> Unit,
    onOpenSettings: (Version) -> Unit,
    onOpenAdvanced: (Version) -> Unit,
    onExport: (Version) -> Unit,
    onBrowseContent: () -> Unit,
    onBrowseWorlds: () -> Unit,
    onOpenFolder: (root: File, current: File) -> Unit,
) {
    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val versions by VersionsManager.versions.collectAsStateWithLifecycle()
            val version = remember(versions, key) {
                versions.firstOrNull { it.getVersionName() == key.versionName }
            }
            if (version == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ZadrinthEmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        iconRes = R.drawable.ic_warning_outlined,
                        title = stringResource(R.string.instance_not_found),
                        message = key.versionName,
                        actionText = stringResource(R.string.generic_back),
                        onAction = onBack
                    )
                }
            } else {
                InstanceContent(
                    version = version,
                    onBack = onBack,
                    onPlay = { onPlay(version) },
                    onOpenSettings = { onOpenSettings(version) },
                    onOpenAdvanced = { onOpenAdvanced(version) },
                    onExport = { onExport(version) },
                    onBrowseContent = onBrowseContent,
                    onBrowseWorlds = onBrowseWorlds,
                    onOpenFolder = onOpenFolder
                )
            }
        }
    }
}

@Composable
private fun InstanceContent(
    version: Version,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdvanced: () -> Unit,
    onExport: () -> Unit,
    onBrowseContent: () -> Unit,
    onBrowseWorlds: () -> Unit,
    onOpenFolder: (root: File, current: File) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val stats by InstanceStatsStore.stats.collectAsStateWithLifecycle()
    val instanceStats = stats[version.getVersionName()]
    val gameDir = remember(version) { version.getGameDir() }

    var tab by rememberSaveable { mutableStateOf(InstanceTab.CONTENT) }
    var contentType by rememberSaveable { mutableStateOf(ContentType.MODS) }
    var filter by rememberSaveable { mutableStateOf("") }
    //Incrementar força a releitura da aba atual
    var reloadKey by remember { mutableIntStateOf(0) }
    var pendingDelete by remember { mutableStateOf<ContentEntry?>(null) }

    //Dados de cada aba, lidos fora da thread principal
    var contentEntries by remember { mutableStateOf<List<ContentEntry>?>(null) }
    var worlds by remember { mutableStateOf<List<SaveData>?>(null) }
    var screenshots by remember { mutableStateOf<List<File>?>(null) }

    LaunchedEffect(version, tab, contentType, reloadKey) {
        try {
            when (tab) {
                InstanceTab.CONTENT -> {
                    contentEntries = null
                    contentEntries = loadContent(contentType.folder.getDir(gameDir), contentType)
                }
                InstanceTab.WORLDS -> {
                    worlds = null
                    worlds = loadWorlds(VersionFolders.SAVES.getDir(gameDir))
                }
                InstanceTab.SCREENSHOTS -> {
                    screenshots = null
                    screenshots = loadScreenshots(VersionFolders.SCREENSHOTS.getDir(gameDir))
                }
                InstanceTab.FILES -> {}
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.warning(TAG, "Unable to load tab $tab for ${version.getVersionName()}", e)
            contentEntries = contentEntries ?: emptyList()
            worlds = worlds ?: emptyList()
            screenshots = screenshots ?: emptyList()
        }
    }

    fun refresh() {
        reloadKey++
        scope.launch { InstanceStatsStore.refresh(version) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "back") {
            BackLink(onClick = onBack)
        }
        item(key = "header") {
            InstanceHeader(
                version = version,
                lastPlayed = instanceStats?.lastPlayed ?: 0L,
                playTimeSeconds = instanceStats?.timePlayedSeconds ?: 0L,
                onPlay = onPlay,
                onOpenSettings = onOpenSettings,
                onOpenAdvanced = onOpenAdvanced,
                onExport = onExport,
                onOpenFolder = { onOpenFolder(gameDir, gameDir) }
            )
        }
        item(key = "tabs") {
            InstanceTabs(
                selected = tab,
                onSelect = {
                    tab = it
                    filter = ""
                }
            )
        }

        when (tab) {
            InstanceTab.CONTENT -> contentTab(
                entries = contentEntries,
                contentType = contentType,
                filter = filter,
                onFilterChange = { filter = it },
                onTypeChange = {
                    contentType = it
                    filter = ""
                },
                onOpenFolder = {
                    val dir = contentType.folder.getDir(gameDir)
                    dir.mkdirs()
                    onOpenFolder(gameDir, dir)
                },
                onRefresh = ::refresh,
                onBrowse = onBrowseContent,
                onToggle = { entry ->
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) { toggleEntry(entry) }
                        if (!ok) Logger.warning(TAG, "Unable to rename ${entry.file.absolutePath}")
                        refresh()
                    }
                },
                onDelete = { pendingDelete = it }
            )
            InstanceTab.WORLDS -> worldsTab(
                worlds = worlds,
                filter = filter,
                onFilterChange = { filter = it },
                onOpenFolder = {
                    val dir = VersionFolders.SAVES.getDir(gameDir)
                    dir.mkdirs()
                    onOpenFolder(gameDir, dir)
                },
                onOpenWorld = { save -> onOpenFolder(gameDir, save.saveFile) },
                onRefresh = ::refresh,
                onBrowse = onBrowseWorlds
            )
            InstanceTab.SCREENSHOTS -> screenshotsTab(
                screenshots = screenshots,
                onOpenFolder = {
                    val dir = VersionFolders.SCREENSHOTS.getDir(gameDir)
                    dir.mkdirs()
                    onOpenFolder(gameDir, dir)
                },
                onRefresh = ::refresh
            )
            InstanceTab.FILES -> filesTab(
                gameDir = gameDir,
                onOpenFolder = { dir ->
                    dir.mkdirs()
                    onOpenFolder(gameDir, dir)
                }
            )
        }
    }

    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(text = stringResource(R.string.instance_delete_title, entry.displayName)) },
            text = { Text(text = stringResource(R.string.instance_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        scope.launch {
                            withContext(Dispatchers.IO) { entry.file.deleteRecursively() }
                            refresh()
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.generic_delete),
                        color = ModrinthTheme.colors.red
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(text = stringResource(R.string.generic_cancel))
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Cabeçalho
// ---------------------------------------------------------------------------

@Composable
private fun BackLink(onClick: () -> Unit) {
    val colors = ModrinthTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = null,
            tint = colors.textSecondary
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.generic_back),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun InstanceHeader(
    version: Version,
    lastPlayed: Long,
    playTimeSeconds: Long,
    onPlay: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdvanced: () -> Unit,
    onExport: () -> Unit,
    onOpenFolder: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val isValid = version.isValid()

    ModrinthCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InstanceIconBox(version = version, size = 60.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                InstanceTags(version = version)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = version.getVersionName(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(12.dp))
            ModrinthPlayButton(
                onClick = onPlay,
                enabled = isValid,
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.border)
        )

        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            StatCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.instance_stat_status),
                value = if (isValid) {
                    stringResource(R.string.instance_status_ready)
                } else {
                    stringResource(R.string.library_invalid)
                }
            )
            StatDivider()
            StatCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.instance_stat_last_played),
                value = instanceLastPlayedText(lastPlayed)
            )
            StatDivider()
            StatCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.instance_stat_playtime),
                value = instancePlayTimeText(playTimeSeconds),
                iconRes = R.drawable.ic_schedule_outlined
            )
            StatDivider()
            ActionCell(
                iconRes = R.drawable.ic_settings_filled,
                label = stringResource(R.string.instance_settings),
                enabled = isValid,
                onClick = onOpenSettings
            )
            StatDivider()
            MoreCell(
                enabled = isValid,
                onOpenFolder = onOpenFolder,
                onExport = onExport,
                onOpenAdvanced = onOpenAdvanced
            )
        }
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(ModrinthTheme.colors.border)
    )
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
) {
    val colors = ModrinthTheme.colors
    Column(modifier = modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconRes != null) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = colors.textSecondary
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActionCell(
    @DrawableRes iconRes: Int,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (enabled) colors.textPrimary else colors.textSecondary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun MoreCell(
    enabled: Boolean,
    onOpenFolder: () -> Unit,
    onExport: () -> Unit,
    onOpenAdvanced: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ActionCell(
            iconRes = R.drawable.ic_more_vert,
            label = stringResource(R.string.instance_more),
            enabled = true,
            onClick = { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.instance_more_open_folder)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_folder_outlined),
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    onOpenFolder()
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.instance_more_export)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_file_export_outlined),
                        contentDescription = null
                    )
                },
                enabled = enabled,
                onClick = {
                    expanded = false
                    onExport()
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.instance_more_advanced)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_build_outlined),
                        contentDescription = null
                    )
                },
                enabled = enabled,
                onClick = {
                    expanded = false
                    onOpenAdvanced()
                }
            )
        }
    }
}

@Composable
private fun InstanceTabs(
    selected: InstanceTab,
    onSelect: (InstanceTab) -> Unit,
) {
    val colors = ModrinthTheme.colors
    Column {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            InstanceTab.entries.forEach { entry ->
                val isSelected = entry == selected
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .clickable(role = Role.Tab, onClick = { onSelect(entry) })
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(entry.iconRes),
                            contentDescription = null,
                            tint = if (isSelected) colors.brand else colors.textSecondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(entry.labelRes),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) colors.brand else colors.textSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (isSelected) colors.brand else colors.border.copy(alpha = 0f))
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.border)
        )
    }
}

// ---------------------------------------------------------------------------
// Abas
// ---------------------------------------------------------------------------

@Composable
private fun TabToolbar(
    filter: String?,
    filterHint: String,
    onFilterChange: (String) -> Unit,
    onOpenFolder: () -> Unit,
    onRefresh: () -> Unit,
    browseText: String?,
    onBrowse: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (filter != null) {
            ModrinthSearchField(
                modifier = Modifier.weight(1f),
                value = filter,
                onValueChange = onFilterChange,
                hint = filterHint
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        IconButton(onClick = onOpenFolder) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.ic_folder_outlined),
                contentDescription = stringResource(R.string.instance_open_folder),
                tint = colors.textSecondary
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = stringResource(R.string.library_refresh),
                tint = colors.textSecondary
            )
        }
        if (browseText != null) {
            Spacer(Modifier.width(4.dp))
            ModrinthBrandButton(onClick = onBrowse) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_explore_filled),
                    contentDescription = null
                )
                Spacer(Modifier.width(6.dp))
                Text(text = browseText, maxLines = 1)
            }
        }
    }
}

@Composable
private fun LoadingRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = ModrinthTheme.colors.brand,
            strokeWidth = 3.dp
        )
    }
}

private fun LazyListScope.contentTab(
    entries: List<ContentEntry>?,
    contentType: ContentType,
    filter: String,
    onFilterChange: (String) -> Unit,
    onTypeChange: (ContentType) -> Unit,
    onOpenFolder: () -> Unit,
    onRefresh: () -> Unit,
    onBrowse: () -> Unit,
    onToggle: (ContentEntry) -> Unit,
    onDelete: (ContentEntry) -> Unit,
) {
    item(key = "content_types") {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContentType.entries.forEach { type ->
                ModrinthFilterChip(
                    selected = type == contentType,
                    text = stringResource(type.labelRes),
                    onClick = { onTypeChange(type) }
                )
            }
        }
    }
    item(key = "content_toolbar") {
        TabToolbar(
            filter = filter,
            filterHint = stringResource(R.string.instance_filter_content),
            onFilterChange = onFilterChange,
            onOpenFolder = onOpenFolder,
            onRefresh = onRefresh,
            browseText = stringResource(R.string.instance_browse_content),
            onBrowse = onBrowse
        )
    }
    when {
        entries == null -> item(key = "content_loading") { LoadingRow() }
        entries.isEmpty() -> item(key = "content_empty") {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ZadrinthEmptyState(
                    iconRes = R.drawable.ic_extension_outlined,
                    title = stringResource(R.string.instance_content_empty_title),
                    message = stringResource(R.string.instance_content_empty_message),
                    actionText = stringResource(R.string.instance_browse_content),
                    onAction = onBrowse
                )
            }
        }
        else -> {
            val shown = entries.filter {
                filter.isBlank() || it.displayName.contains(filter.trim(), ignoreCase = true)
            }
            items(items = shown, key = { "content_" + it.file.absolutePath }) { entry ->
                ContentRow(
                    entry = entry,
                    onToggle = { onToggle(entry) },
                    onDelete = { onDelete(entry) }
                )
            }
        }
    }
}

@Composable
private fun ContentRow(
    entry: ContentEntry,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    ModrinthCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceRaised),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(
                        if (entry.file.isDirectory) R.drawable.ic_folder_outlined else R.drawable.ic_extension_outlined
                    ),
                    contentDescription = null,
                    tint = if (entry.enabled) colors.brand else colors.textSecondary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (entry.enabled) colors.textPrimary else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.file.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (entry.sizeBytes > 0) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = formatFileSize(entry.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
            if (entry.canToggle) {
                Switch(
                    checked = entry.enabled,
                    onCheckedChange = { onToggle() }
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.ic_delete_outlined),
                    contentDescription = stringResource(R.string.generic_delete),
                    tint = colors.textSecondary
                )
            }
        }
    }
}

private fun LazyListScope.worldsTab(
    worlds: List<SaveData>?,
    filter: String,
    onFilterChange: (String) -> Unit,
    onOpenFolder: () -> Unit,
    onOpenWorld: (SaveData) -> Unit,
    onRefresh: () -> Unit,
    onBrowse: () -> Unit,
) {
    item(key = "worlds_toolbar") {
        TabToolbar(
            filter = filter,
            filterHint = stringResource(R.string.instance_filter_worlds),
            onFilterChange = onFilterChange,
            onOpenFolder = onOpenFolder,
            onRefresh = onRefresh,
            browseText = stringResource(R.string.instance_browse_worlds),
            onBrowse = onBrowse
        )
    }
    when {
        worlds == null -> item(key = "worlds_loading") { LoadingRow() }
        worlds.isEmpty() -> item(key = "worlds_empty") {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ZadrinthEmptyState(
                    iconRes = R.drawable.ic_public,
                    title = stringResource(R.string.instance_worlds_empty_title),
                    message = stringResource(R.string.instance_worlds_empty_message),
                    actionText = stringResource(R.string.instance_browse_worlds),
                    onAction = onBrowse
                )
            }
        }
        else -> {
            item(key = "worlds_header") {
                val colors = ModrinthTheme.colors
                Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(
                        modifier = Modifier.weight(2f),
                        text = stringResource(R.string.instance_worlds_col_world).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.instance_worlds_col_mode).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.instance_stat_last_played).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.brand
                    )
                }
            }
            val shown = worlds.filter { save ->
                filter.isBlank() ||
                        (save.levelName ?: save.saveFile.name).contains(filter.trim(), ignoreCase = true)
            }
            items(items = shown, key = { "world_" + it.saveFile.absolutePath }) { save ->
                WorldRow(save = save, onClick = { onOpenWorld(save) })
            }
        }
    }
}

@Composable
private fun WorldRow(
    save: SaveData,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val iconFile = remember(save) { File(save.saveFile, "icon.png").takeIf { it.isFile } }
    ModrinthCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceRaised),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconFile != null) {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = iconFile,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(R.drawable.ic_public),
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = save.levelName ?: save.saveFile.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    save.levelMCVersion?.let { mcVersion ->
                        Text(
                            text = mcVersion,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
            Text(
                modifier = Modifier.weight(1f),
                text = when {
                    save.hardcoreMode == true -> stringResource(R.string.instance_worlds_hardcore)
                    save.gameMode != null -> stringResource(save.gameMode.nameRes)
                    else -> "—"
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1
            )
            Text(
                modifier = Modifier.weight(1f),
                text = instanceLastPlayedText(save.lastPlayed ?: save.saveFile.lastModified()),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1
            )
        }
    }
}

private fun LazyListScope.screenshotsTab(
    screenshots: List<File>?,
    onOpenFolder: () -> Unit,
    onRefresh: () -> Unit,
) {
    item(key = "shots_toolbar") {
        TabToolbar(
            filter = null,
            filterHint = "",
            onFilterChange = {},
            onOpenFolder = onOpenFolder,
            onRefresh = onRefresh,
            browseText = null,
            onBrowse = {}
        )
    }
    when {
        screenshots == null -> item(key = "shots_loading") { LoadingRow() }
        screenshots.isEmpty() -> item(key = "shots_empty") {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ZadrinthEmptyState(
                    iconRes = R.drawable.ic_image_outlined,
                    title = stringResource(R.string.instance_screenshots_empty_title),
                    message = stringResource(R.string.instance_screenshots_empty_message)
                )
            }
        }
        else -> {
            val rows = screenshots.chunked(3)
            items(items = rows, key = { row -> "shots_" + row.first().absolutePath }) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { file ->
                        AsyncImage(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ModrinthTheme.colors.surfaceRaised)
                                .clickable(onClick = onOpenFolder),
                            model = file,
                            contentDescription = file.name,
                            contentScale = ContentScale.Crop
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun LazyListScope.filesTab(
    gameDir: File,
    onOpenFolder: (File) -> Unit,
) {
    val folders = listOf(
        gameDir,
        File(gameDir, "mods"),
        File(gameDir, "config"),
        File(gameDir, "resourcepacks"),
        File(gameDir, "shaderpacks"),
        File(gameDir, "saves"),
        File(gameDir, "screenshots"),
        File(gameDir, "logs"),
    )
    items(items = folders, key = { "folder_" + it.absolutePath }) { dir ->
        FolderRow(
            dir = dir,
            isRoot = dir == gameDir,
            onClick = { onOpenFolder(dir) }
        )
    }
}

@Composable
private fun FolderRow(
    dir: File,
    isRoot: Boolean,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val count = remember(dir) { dir.listFiles()?.size ?: 0 }
    ModrinthCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(22.dp),
                painter = painterResource(R.drawable.ic_folder_outlined),
                contentDescription = null,
                tint = if (isRoot) colors.brand else colors.textSecondary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = if (isRoot) stringResource(R.string.instance_files_root) else dir.name + "/",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.instance_files_items, count),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_keyboard_arrow_right),
                contentDescription = null,
                tint = colors.textSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Leitura de arquivos
// ---------------------------------------------------------------------------

private suspend fun loadContent(dir: File, type: ContentType): List<ContentEntry> = withContext(Dispatchers.IO) {
    val files = dir.listFiles() ?: return@withContext emptyList()
    files.filter { file ->
        val base = file.name.removeSuffix(DISABLED_SUFFIX).lowercase()
        when (type) {
            ContentType.MODS -> file.isFile && base.endsWith(".jar")
            ContentType.RESOURCE_PACKS, ContentType.SHADERS ->
                file.isDirectory || base.endsWith(".zip")
        }
    }.map { file ->
        val enabled = !file.name.endsWith(DISABLED_SUFFIX)
        val baseName = file.name.removeSuffix(DISABLED_SUFFIX)
        ContentEntry(
            file = file,
            displayName = baseName.substringBeforeLast('.').takeIf { file.isFile && it.isNotEmpty() } ?: baseName,
            enabled = enabled,
            sizeBytes = if (file.isFile) file.length() else 0L
        )
    }.sortedWith(compareByDescending<ContentEntry> { it.enabled }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayName })
}

private suspend fun loadWorlds(savesDir: File): List<SaveData> = withContext(Dispatchers.IO) {
    val dirs = savesDir.listFiles()?.filter { it.isDirectory } ?: return@withContext emptyList()
    dirs.map { dir ->
        parseLevelDatFile(
            saveFile = dir,
            levelDatFile = File(dir, "level.dat"),
            worldGenDatFile = File(dir, "data/minecraft/world_gen_settings.dat").takeIf { it.isFile }
        )
    }.sortedByDescending { it.lastPlayed ?: it.saveFile.lastModified() }
}

private suspend fun loadScreenshots(dir: File): List<File> = withContext(Dispatchers.IO) {
    dir.listFiles()
        ?.filter { it.isFile && it.extension.lowercase() in setOf("png", "jpg", "jpeg") }
        ?.sortedByDescending { it.lastModified() }
        ?: emptyList()
}

/** Ativa/desativa renomeando para .disabled (como o Modrinth App faz) */
private fun toggleEntry(entry: ContentEntry): Boolean {
    val file = entry.file
    val target = if (entry.enabled) {
        File(file.parentFile, file.name + DISABLED_SUFFIX)
    } else {
        File(file.parentFile, file.name.removeSuffix(DISABLED_SUFFIX))
    }
    if (target.exists()) return false
    return file.renameTo(target)
}
