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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme

/** Forma dos painéis flutuantes (barra lateral, conteúdo, notícias) */
val ZadrinthPanelShape = RoundedCornerShape(16.dp)

private val SidebarItemShape = RoundedCornerShape(12.dp)

/**
 * Destinos principais da barra lateral.
 */
enum class MainNavDestination(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    HOME(R.string.nav_home, R.drawable.ic_dashboard_outlined),
    LIBRARY(R.string.nav_library, R.drawable.ic_style_outlined),
    EXPLORE(R.string.nav_explore, R.drawable.ic_package_2_outlined),
    MULTIPLAYER(R.string.nav_multiplayer, R.drawable.ic_public),
    SETTINGS(R.string.nav_settings, R.drawable.ic_settings_filled),
}

/**
 * Qual item da barra lateral fica destacado para a tela atual.
 * Telas "filhas" acendem o destino a que pertencem.
 */
fun TitledNavKey?.toMainNavDestination(): MainNavDestination? = when (this) {
    null, is NormalNavKey.LauncherMain -> MainNavDestination.HOME
    is NestedNavKey.Download, is NestedNavKey.AddonInfo -> MainNavDestination.EXPLORE
    is NormalNavKey.Library,
    is NormalNavKey.Instance,
    is NormalNavKey.VersionsManager,
    is NestedNavKey.VersionSettings,
    is NestedNavKey.VersionExport -> MainNavDestination.LIBRARY
    is NormalNavKey.Multiplayer -> MainNavDestination.MULTIPLAYER
    is NestedNavKey.Settings, is NormalNavKey.License -> MainNavDestination.SETTINGS
    else -> null
}

/**
 * Barra lateral flutuante no estilo do SKlauncher: avatar da conta no topo,
 * destinos principais, botão "+", instâncias recentes e Configurações embaixo.
 */
@Composable
fun ZadrinthSidebar(
    account: Account?,
    accountsSelected: Boolean,
    selected: MainNavDestination?,
    recentInstances: List<Version>,
    selectedInstanceName: String?,
    onNavigate: (MainNavDestination) -> Unit,
    onCreateInstance: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenInstance: (Version) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors

    Column(
        modifier = modifier
            .width(64.dp)
            .fillMaxHeight()
            .clip(ZadrinthPanelShape)
            .background(colors.background)
            .border(1.dp, colors.border, ZadrinthPanelShape)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AccountButton(
            account = account,
            selected = accountsSelected,
            onClick = onOpenAccounts
        )

        SidebarDivider()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                MainNavDestination.HOME,
                MainNavDestination.LIBRARY,
                MainNavDestination.EXPLORE,
                MainNavDestination.MULTIPLAYER
            ).forEach { destination ->
                SidebarButton(
                    selected = selected == destination,
                    iconRes = destination.iconRes,
                    label = stringResource(destination.labelRes),
                    onClick = { onNavigate(destination) }
                )
            }

            SidebarDivider()

            SidebarButton(
                selected = false,
                iconRes = R.drawable.ic_add,
                label = stringResource(R.string.nav_create_instance),
                onClick = onCreateInstance
            )

            if (recentInstances.isNotEmpty()) {
                SidebarDivider()
                recentInstances.forEach { version ->
                    InstanceButton(
                        version = version,
                        selected = version.getVersionName() == selectedInstanceName,
                        onClick = { onOpenInstance(version) }
                    )
                }
            }
        }

        SidebarDivider()

        SidebarButton(
            selected = selected == MainNavDestination.SETTINGS,
            iconRes = MainNavDestination.SETTINGS.iconRes,
            label = stringResource(MainNavDestination.SETTINGS.labelRes),
            onClick = { onNavigate(MainNavDestination.SETTINGS) }
        )
    }
}

@Composable
private fun SidebarDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .width(28.dp)
            .height(1.dp)
            .background(ModrinthTheme.colors.border)
    )
}

@Composable
private fun SidebarButton(
    selected: Boolean,
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val containerColor by animateColorAsState(
        targetValue = if (selected) colors.brandHighlight else Color.Transparent,
        label = "SidebarButtonContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.brand else colors.textSecondary,
        label = "SidebarButtonContent"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(SidebarItemShape)
            .background(containerColor)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = contentColor
        )
    }
}

@Composable
private fun AccountButton(
    account: Account?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val label = account?.username ?: stringResource(R.string.nav_accounts)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(SidebarItemShape)
            .background(colors.surfaceRaised)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.brand else colors.border,
                shape = SidebarItemShape
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        if (account != null) {
            PlayerFace(
                account = account,
                avatarSize = 34.dp
            )
        } else {
            Icon(
                modifier = Modifier.size(22.dp),
                painter = painterResource(R.drawable.ic_person_outlined),
                contentDescription = null,
                tint = colors.textSecondary
            )
        }
    }
}

@Composable
private fun InstanceButton(
    version: Version,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val name = version.getVersionName()
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(SidebarItemShape)
            .background(if (selected) colors.brandBrush else SolidColor(Color.Transparent))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = name },
        contentAlignment = Alignment.Center
    ) {
        VersionIconImage(
            version = version,
            modifier = Modifier.size(30.dp)
        )
    }
}
