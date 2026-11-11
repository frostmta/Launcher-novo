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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthCardShape
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthDimens
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme

/**
 * Destinos principais da barra lateral (como no Modrinth App).
 */
enum class MainNavDestination(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    HOME(R.string.nav_home, R.drawable.ic_home_filled),
    EXPLORE(R.string.nav_explore, R.drawable.ic_explore_filled),
    LIBRARY(R.string.nav_library, R.drawable.ic_library_filled),
    SETTINGS(R.string.nav_settings, R.drawable.ic_settings_filled),
}

/**
 * Qual item da barra lateral fica destacado para a tela atual.
 * Telas "filhas" acendem o destino a que pertencem (ex.: detalhes da
 * instância acendem a Biblioteca).
 */
fun TitledNavKey?.toMainNavDestination(): MainNavDestination? = when (this) {
    null, is NormalNavKey.LauncherMain -> MainNavDestination.HOME
    is NestedNavKey.Download, is NestedNavKey.AddonInfo -> MainNavDestination.EXPLORE
    is NormalNavKey.Library,
    is NormalNavKey.VersionsManager,
    is NestedNavKey.VersionSettings,
    is NestedNavKey.VersionExport -> MainNavDestination.LIBRARY
    is NestedNavKey.Settings, is NormalNavKey.License -> MainNavDestination.SETTINGS
    else -> null
}

/**
 * Navigation Rail compacta e fixa, no estilo do Modrinth App:
 * Início / Explorar / Biblioteca, botão "+" para criar instância e,
 * embaixo, Contas e Configurações.
 */
@Composable
fun ModrinthNavRail(
    selected: MainNavDestination?,
    accountsSelected: Boolean,
    onNavigate: (MainNavDestination) -> Unit,
    onCreateInstance: () -> Unit,
    onOpenAccounts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors

    Column(
        modifier = modifier
            .width(ModrinthDimens.NavRailWidth)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(
            MainNavDestination.HOME,
            MainNavDestination.EXPLORE,
            MainNavDestination.LIBRARY
        ).forEach { destination ->
            RailButton(
                selected = selected == destination,
                iconRes = destination.iconRes,
                label = stringResource(destination.labelRes),
                onClick = { onNavigate(destination) }
            )
        }

        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .width(28.dp)
                .height(1.dp)
                .background(colors.border)
        )

        CreateInstanceButton(onClick = onCreateInstance)

        Spacer(Modifier.weight(1f))

        RailButton(
            selected = accountsSelected,
            iconRes = R.drawable.ic_person_filled,
            label = stringResource(R.string.nav_accounts),
            onClick = onOpenAccounts
        )
        RailButton(
            selected = selected == MainNavDestination.SETTINGS,
            iconRes = MainNavDestination.SETTINGS.iconRes,
            label = stringResource(MainNavDestination.SETTINGS.labelRes),
            onClick = { onNavigate(MainNavDestination.SETTINGS) }
        )
    }
}

@Composable
private fun RailButton(
    selected: Boolean,
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val containerColor by animateColorAsState(
        targetValue = if (selected) colors.brandHighlight else Color.Transparent,
        label = "RailButtonContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.brand else colors.textSecondary,
        label = "RailButtonContent"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(ModrinthCardShape)
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
private fun CreateInstanceButton(onClick: () -> Unit) {
    val colors = ModrinthTheme.colors
    val label = stringResource(R.string.nav_create_instance)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(ModrinthCardShape)
            .background(colors.surfaceRaised)
            .border(ModrinthDimens.BorderWidth, colors.border, ModrinthCardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            tint = colors.brand
        )
    }
}
