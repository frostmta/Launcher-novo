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

package com.movtery.zalithlauncher.ui.screens.main

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.instance.InstanceStatsStore
import com.movtery.zalithlauncher.game.instance.sortedByRecent
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.screens.BackStackNavKey
import com.movtery.zalithlauncher.ui.screens.NavStateHost
import com.movtery.zalithlauncher.ui.screens.NestedNavDisplay
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.AccountManageScreen
import com.movtery.zalithlauncher.ui.screens.content.DownloadScreen
import com.movtery.zalithlauncher.ui.screens.content.FileSelectorScreen
import com.movtery.zalithlauncher.ui.screens.content.LibraryScreen
import com.movtery.zalithlauncher.ui.screens.content.zadrinth.WardrobeScreen
import com.movtery.zalithlauncher.ui.screens.content.zadrinth.CosmeticShopScreen
import com.movtery.zalithlauncher.ui.screens.content.zadrinth.servers.ServersScreen
import com.movtery.zalithlauncher.game.servers.FeaturedServer
import com.movtery.zalithlauncher.ui.screens.content.LicenseScreen
import com.movtery.zalithlauncher.ui.screens.content.LogViewScreen
import com.movtery.zalithlauncher.ui.screens.content.MultiplayerScreen
import com.movtery.zalithlauncher.ui.screens.content.SettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionExportScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionsManageScreen
import com.movtery.zalithlauncher.ui.screens.content.WebViewScreen
import com.movtery.zalithlauncher.ui.screens.content.addoninfo.AddonInfoScreen
import com.movtery.zalithlauncher.ui.screens.content.zadrinth.ZadrinthHomeScreen
import com.movtery.zalithlauncher.ui.screens.content.zadrinth.ZadrinthInstanceScreen
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ModifyVersionViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen
import java.io.File

/** Faixa superior com degradê, como a barra de título do SKlauncher */
private val TitleBarBrush = Brush.horizontalGradient(
    listOf(
        Color(0xFF15162B),
        Color(0xFF2B3B8A),
        Color(0xFF47389C),
        Color(0xFF1C1631)
    )
)

/**
 * Ações de navegação compartilhadas entre a barra lateral e as telas.
 */
private class MainNavigator(
    private val model: ScreenBackStackViewModel,
) {
    /** Volta ao Início */
    fun toHome() {
        model.mainScreen.clearWith(NormalNavKey.LauncherMain)
    }

    /** Destino principal: a pilha vira [Início, destino] */
    fun topLevel(key: TitledNavKey) {
        val main = model.mainScreen
        main.clearWith(NormalNavKey.LauncherMain)
        if (key !is NormalNavKey.LauncherMain) {
            main.navigateTo(key, useClassEquality = key !is NormalNavKey.Instance)
        }
    }

    /** Abre a tela de download na aba indicada */
    fun downloadTab(tab: TitledNavKey) {
        model.downloadScreen.clearWith(tab)
        topLevel(model.downloadScreen)
    }

    fun createInstance() = downloadTab(model.downloadGameScreen)

    fun exploreContent() = downloadTab(model.downloadModScreen)

    fun exploreWorlds() = downloadTab(model.downloadSavesScreen)

    /** Abre a página de uma instância por cima da tela atual */
    fun openInstance(version: Version) {
        model.mainScreen.navigateTo(NormalNavKey.Instance(version.getVersionName()))
    }

    /** Abre a página de uma instância a partir da barra lateral */
    fun openInstanceTopLevel(version: Version) {
        topLevel(NormalNavKey.Instance(version.getVersionName()))
    }

    /** Gerenciamento avançado da versão (telas do Zalith) */
    fun openAdvanced(version: Version) {
        model.mainScreen.navigateTo(
            screenKey = NestedNavKey.VersionSettings(version),
            useClassEquality = true
        )
    }

    /** Configurações da instância (RAM, renderizador, argumentos JVM...) */
    fun openInstanceSettings(version: Version) {
        model.mainScreen.navigateTo(
            screenKey = NestedNavKey.VersionSettings(version).apply {
                navigateTo(NormalNavKey.Versions.Config)
            },
            useClassEquality = true
        )
    }

    fun openExport(version: Version) {
        model.mainScreen.removeAndNavigateTo(
            remove = NestedNavKey.VersionSettings::class,
            screenKey = NestedNavKey.VersionExport(version),
            useClassEquality = true
        )
    }
}

@Composable
fun MainScreen(
    screenBackStackModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    modifyVersionViewModel: ModifyVersionViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    //监控当前是否有任务正在进行
    var noTasks by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        TaskSystem.tasksFlow.collect { tasks ->
            val empty = tasks.isEmpty()
            eventViewModel.sendKeepScreen(!empty)
            noTasks = empty
        }
    }

    val isTaskMenuExpanded = AllSettings.launcherTaskMenuExpanded.state

    fun changeTasksExpandedState() {
        AllSettings.launcherTaskMenuExpanded.save(!isTaskMenuExpanded)
    }

    val navigator = remember(screenBackStackModel) { MainNavigator(screenBackStackModel) }

    val mainScreenKey = screenBackStackModel.mainScreen.currentKey
    val inLauncherScreen = mainScreenKey == null || mainScreenKey is NormalNavKey.LauncherMain

    //Dados para a barra lateral (conta e instâncias recentes)
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val stats by InstanceStatsStore.stats.collectAsStateWithLifecycle()
    LaunchedEffect(versions) {
        InstanceStatsStore.refresh(versions)
    }
    val recentInstances = remember(versions, stats) {
        versions.filter { it.isValid() }.sortedByRecent(stats).take(4)
    }

    val isBackgroundValid = LocalBackgroundViewModel.current?.isValid == true
    val launcherBackgroundOpacity = AllSettings.launcherBackgroundOpacity.state.toFloat() / 100f

    val colors = ModrinthTheme.colors
    val shellColor = if (isBackgroundValid) {
        colors.shell.copy(alpha = launcherBackgroundOpacity)
    } else colors.shell
    val panelColor = if (isBackgroundValid) {
        backgroundColor().copy(alpha = launcherBackgroundOpacity)
    } else backgroundColor()

    //As telas novas do Zadrinth têm o próprio cabeçalho; as herdadas do Zalith usam a linha "Voltar"
    val showContentHeader = !inLauncherScreen &&
            mainScreenKey !is NormalNavKey.Library &&
            mainScreenKey !is NormalNavKey.Wardrobe &&
            mainScreenKey !is NormalNavKey.Shop &&
            mainScreenKey !is NormalNavKey.Servers &&
            mainScreenKey !is NormalNavKey.Instance &&
            //Explorar e as configurações têm o próprio cabeçalho (abas/menu lateral)
            mainScreenKey !is NestedNavKey.Download &&
            mainScreenKey !is NestedNavKey.Settings &&
            mainScreenKey !is NestedNavKey.VersionSettings

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = shellColor,
        contentColor = onBackgroundColor()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ZadrinthTitleBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                showTasks = !(isTaskMenuExpanded || noTasks),
                onToggleTasks = { changeTasksExpandedState() },
                onOpenFiles = {
                    eventViewModel.sendEvent(
                        EventViewModel.Event.OpenFileManager(
                            rootPath = PathManager.DIR_FILES_EXTERNAL.absolutePath
                        )
                    )
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ZadrinthSidebar(
                    account = account,
                    accountsSelected = mainScreenKey is NormalNavKey.AccountManager,
                    selected = mainScreenKey.toMainNavDestination(),
                    recentInstances = recentInstances,
                    selectedInstanceName = (mainScreenKey as? NormalNavKey.Instance)?.versionName,
                    onNavigate = { destination ->
                        when (destination) {
                            MainNavDestination.HOME -> {
                                if (!inLauncherScreen) navigator.toHome()
                            }
                            MainNavDestination.LIBRARY -> {
                                if (mainScreenKey !is NormalNavKey.Library) {
                                    navigator.topLevel(NormalNavKey.Library)
                                }
                            }
                            MainNavDestination.EXPLORE -> {
                                if (mainScreenKey !is NestedNavKey.Download) {
                                    navigator.downloadTab(screenBackStackModel.downloadModPackScreen)
                                }
                            }
                            MainNavDestination.SERVERS -> {
                                if (mainScreenKey !is NormalNavKey.Servers) {
                                    navigator.topLevel(NormalNavKey.Servers)
                                }
                            }
                            MainNavDestination.WARDROBE -> {
                                if (mainScreenKey !is NormalNavKey.Wardrobe) {
                                    navigator.topLevel(NormalNavKey.Wardrobe)
                                }
                            }
                            MainNavDestination.SHOP -> {
                                if (mainScreenKey !is NormalNavKey.Shop) {
                                    navigator.topLevel(NormalNavKey.Shop)
                                }
                            }
                            MainNavDestination.MULTIPLAYER -> {
                                if (mainScreenKey !is NormalNavKey.Multiplayer) {
                                    navigator.topLevel(NormalNavKey.Multiplayer)
                                }
                            }
                            MainNavDestination.SETTINGS -> {
                                if (mainScreenKey !is NestedNavKey.Settings) {
                                    navigator.topLevel(screenBackStackModel.settingsScreen)
                                }
                            }
                        }
                    },
                    onCreateInstance = { navigator.createInstance() },
                    onOpenAccounts = {
                        if (mainScreenKey !is NormalNavKey.AccountManager) {
                            navigator.topLevel(NormalNavKey.AccountManager())
                        }
                    },
                    onOpenInstance = { version -> navigator.openInstanceTopLevel(version) }
                )

                //Painel de conteúdo flutuante
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(ZadrinthPanelShape)
                        .background(panelColor)
                        .border(1.dp, colors.border, ZadrinthPanelShape)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(visible = showContentHeader) {
                            ContentHeader(
                                mainScreenKey = mainScreenKey,
                                onBack = {
                                    screenBackStackModel.mainScreen.backStack.removeLastOrNull()
                                }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            NavStateHost(rootKey = screenBackStackModel.mainScreen) {
                                NavigationUI(
                                    modifier = Modifier.fillMaxSize(),
                                    screenBackStackModel = screenBackStackModel,
                                    navigator = navigator,
                                    eventViewModel = eventViewModel,
                                    modpackImportViewModel = modpackImportViewModel,
                                    modifyVersionViewModel = modifyVersionViewModel,
                                    submitError = submitError
                                )
                            }
                        }
                    }

                    TaskMenu(
                        isExpanded = isTaskMenuExpanded,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.3f)
                            .align(Alignment.CenterStart)
                            .padding(all = 6.dp)
                    ) {
                        changeTasksExpandedState()
                    }
                }
            }
        }
    }
}

/**
 * Faixa superior: nome do launcher no centro (com o aviso de versão não oficial,
 * exigido pela licença do Zalith Launcher 2) e atalhos à direita.
 */
@Composable
private fun ZadrinthTitleBar(
    showTasks: Boolean,
    onToggleTasks: () -> Unit,
    onOpenFiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onBar = Color.White
    Box(modifier = modifier.background(TitleBarBrush)) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.img_launcher),
                contentDescription = null
            )
            Text(
                text = "${BuildKeys.LAUNCHER_NAME} ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.titleSmall,
                color = onBar,
                maxLines = 1
            )
            //Exigido pelos termos adicionais da licença do Zalith Launcher 2 (GPLv3, seção 7)
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(onBar.copy(alpha = 0.16f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                text = stringResource(R.string.unofficial_modified_version),
                style = MaterialTheme.typography.labelSmall,
                color = onBar,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = showTasks,
                enter = slideInVertically(initialOffsetY = { -50 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -50 }) + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onToggleTasks)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.width(72.dp),
                        color = onBar,
                        trackColor = onBar.copy(alpha = 0.25f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.drawable.ic_assignment_filled),
                        contentDescription = stringResource(R.string.main_task_menu),
                        tint = onBar
                    )
                }
            }
            IconButton(onClick = onOpenFiles) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_folder_outlined),
                    contentDescription = stringResource(R.string.nav_files),
                    tint = onBar
                )
            }
        }
    }
}

/**
 * Linha "← Voltar · Título" mostrada nas telas herdadas do Zalith.
 */
@Composable
private fun ContentHeader(
    mainScreenKey: TitledNavKey?,
    onBack: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val parentRes = mainScreenKey?.title
    val childRes = (mainScreenKey as? BackStackNavKey<*>)?.currentKey?.title

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button) {
                        backDispatcher?.onBackPressed() ?: onBack()
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
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

            Crossfade(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                targetState = parentRes to childRes
            ) { (parent, child) ->
                if (parent != null) {
                    val titleText = if (child != null) {
                        androidText(parent, androidText(" · "), child)
                    } else {
                        parent
                    }
                    AndroidStringText(
                        text = titleText,
                        style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary),
                        softWrap = false,
                        maxLines = 1
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

@Composable
private fun NavigationUI(
    modifier: Modifier = Modifier,
    screenBackStackModel: ScreenBackStackViewModel,
    navigator: MainNavigator,
    eventViewModel: EventViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    modifyVersionViewModel: ModifyVersionViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
) {
    val backStack = screenBackStackModel.mainScreen.backStack
    val currentKey = backStack.lastOrNull()

    LaunchedEffect(currentKey) {
        screenBackStackModel.mainScreen.currentKey = currentKey
    }

    if (backStack.isNotEmpty()) {
        val toMainScreen: () -> Unit = { navigator.toHome() }
        val navigateToVersions: (Version) -> Unit = { version -> navigator.openAdvanced(version) }
        val navigateToExport: (Version) -> Unit = { version -> navigator.openExport(version) }
        /** Joga uma instância; ela passa a ser a selecionada (aparece no Início) */
        val playInstance: (Version) -> Unit = { version ->
            VersionsManager.saveVersion(version)
            eventViewModel.sendEvent(EventViewModel.Event.Launch.Game(version))
        }
        val openLink: (String) -> Unit = { url ->
            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
        }
        /** Entrar rápido: abre o Minecraft já conectando no servidor (quick play) */
        val joinServer: (Version, FeaturedServer) -> Unit = { version, server ->
            VersionsManager.saveVersion(version)
            eventViewModel.sendEvent(EventViewModel.Event.Launch.PlayServer(version, server.address))
        }

        NestedNavDisplay(
            ownerKey = screenBackStackModel.mainScreen,
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NormalNavKey.LauncherMain> {
                    ZadrinthHomeScreen(
                        backStackViewModel = screenBackStackModel,
                        onPlayInstance = playInstance,
                        onOpenInstance = { version -> navigator.openInstance(version) },
                        onOpenLibrary = { navigator.topLevel(NormalNavKey.Library) },
                        onCreateInstance = { navigator.createInstance() },
                        onOpenLink = openLink,
                        onJoinServer = joinServer,
                        onOpenServers = { navigator.topLevel(NormalNavKey.Servers) }
                    )
                }
                entry<NormalNavKey.Servers> {
                    ServersScreen(
                        backStackViewModel = screenBackStackModel,
                        onJoin = joinServer,
                        onCreateInstance = { navigator.createInstance() },
                        onChangeInstance = { navigator.topLevel(NormalNavKey.Library) }
                    )
                }
                entry<NormalNavKey.Library> {
                    LibraryScreen(
                        backStackViewModel = screenBackStackModel,
                        onOpenInstance = { version -> navigator.openInstance(version) },
                        onPlayInstance = playInstance,
                        onCreateInstance = { navigator.createInstance() },
                        onManageInvalid = {
                            screenBackStackModel.mainScreen.navigateTo(NormalNavKey.VersionsManager)
                        }
                    )
                }
                entry<NormalNavKey.Shop> {
                    CosmeticShopScreen(backStackViewModel = screenBackStackModel)
                }
                entry<NormalNavKey.Wardrobe> {
                    WardrobeScreen(
                        backStackViewModel = screenBackStackModel,
                        onOpenAccounts = { navigator.topLevel(NormalNavKey.AccountManager()) }
                    )
                }
                entry<NormalNavKey.Instance> { key ->
                    ZadrinthInstanceScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                        onBack = { onBack(backStack) },
                        onPlay = playInstance,
                        onOpenSettings = { version -> navigator.openInstanceSettings(version) },
                        onOpenAdvanced = navigateToVersions,
                        onExport = navigateToExport,
                        onBrowseContent = { navigator.exploreContent() },
                        onBrowseWorlds = { navigator.exploreWorlds() },
                        onOpenFolder = { root: File, current: File ->
                            eventViewModel.sendEvent(
                                EventViewModel.Event.OpenFileManager(
                                    rootPath = root.absolutePath,
                                    currentPath = current.absolutePath
                                )
                            )
                        }
                    )
                }
                entry<NestedNavKey.Settings> { key ->
                    SettingsScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                        openLicenseScreen = { raw ->
                            backStack.navigateTo(NormalNavKey.License(raw))
                        },
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.License> { key ->
                    LicenseScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel
                    )
                }
                entry<NormalNavKey.AccountManager> { key ->
                    AccountManageScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                        backToMainScreen = toMainScreen,
                        openLink = { url ->
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                        },
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.WebScreen> { key ->
                    WebViewScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NormalNavKey.VersionsManager> {
                    VersionsManageScreen(
                        backScreenViewModel = screenBackStackModel,
                        navigateToVersions = navigateToVersions,
                        navigateToExport = navigateToExport,
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.FileSelector> { key ->
                    FileSelectorScreen(
                        key = key,
                        backScreenViewModel = screenBackStackModel
                    ) {
                        backStack.removeLastOrNull()
                    }
                }
                entry<NestedNavKey.VersionSettings> { key ->
                    VersionSettingsScreen(
                        key = key,
                        modifyViewModel = modifyVersionViewModel,
                        backScreenViewModel = screenBackStackModel,
                        backToMainScreen = toMainScreen,
                        onExportModpack = {
                            navigateToExport(key.version)
                        },
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NestedNavKey.VersionExport> { key ->
                    VersionExportScreen(
                        key = key,
                        backScreenViewModel = screenBackStackModel,
                        eventViewModel = eventViewModel,
                        backToMainScreen = toMainScreen
                    )
                }
                entry<NestedNavKey.Download> { key ->
                    DownloadScreen(
                        key = key,
                        backScreenViewModel = screenBackStackModel,
                        eventViewModel = eventViewModel,
                        modpackImportViewModel = modpackImportViewModel,
                        submitError = submitError
                    )
                }
                entry<NestedNavKey.AddonInfo> { key ->
                    AddonInfoScreen(
                        key = key,
                        mainScreenKey = screenBackStackModel.mainScreen.currentKey,
                        addonInfoScreenKey = key.currentKey,
                        eventViewModel = eventViewModel,
                        submitError = submitError,
                    )
                }
                entry<NormalNavKey.Multiplayer> {
                    MultiplayerScreen(
                        backScreenViewModel = screenBackStackModel,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NormalNavKey.LogView> { key ->
                    LogViewScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}

@Composable
private fun TaskMenu(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    changeExpandedState: () -> Unit = {}
) {
    val tasks by TaskSystem.tasksFlow.collectAsStateWithLifecycle()
    val show = isExpanded && tasks.isNotEmpty()

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    AnimatedVisibility(
        modifier = modifier,
        enter = slideInHorizontally(
            initialOffsetX = { if (isRtl) it else -it },
            animationSpec = getAnimateTween()
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { if (isRtl) it else -it },
            animationSpec = getAnimateTween()
        ) + fadeOut(),
        visible = show
    ) {
        BackgroundCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 6.dp),
            influencedByBackground = false,
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor(),
                contentColor = onBackgroundColor()
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
        ) {
            Column {
                CardTitleLayout(blur = 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        IconButton(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.CenterStart),
                            onClick = changeExpandedState
                        ) {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                painter = painterResource(R.drawable.ic_arrow_left_rounded),
                                contentDescription = stringResource(R.string.generic_collapse)
                            )
                        }

                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.main_task_menu)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        val taskProgress by task.progress.collectAsStateWithLifecycle()
                        val taskMessage by task.message.collectAsStateWithLifecycle()
                        val rateBytesPerSec by task.rateBytesPerSec.collectAsStateWithLifecycle()

                        TaskItem(
                            taskProgress = taskProgress,
                            taskMessage = taskMessage,
                            rateBytesPerSec = rateBytesPerSec,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            //取消任务
                            TaskSystem.cancelTask(task.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    taskProgress: Float,
    taskMessage: AndroidStringText?,
    rateBytesPerSec: Long?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = cardColor(false),
    contentColor: Color = onCardColor(),
    onCancelClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
                onClick = onCancelClick
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.generic_cancel)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                taskMessage?.let { message ->
                    AndroidStringText(
                        text = message,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                if (taskProgress < 0) { //负数则代表不确定
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LinearProgressIndicator(
                        progress = { taskProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    taskProgress.takeIf { it >= 0f }?.let { progress ->
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    rateBytesPerSec?.let { bytes ->
                        val text = remember(bytes) { "${formatFileSize(bytes)}/s" }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}