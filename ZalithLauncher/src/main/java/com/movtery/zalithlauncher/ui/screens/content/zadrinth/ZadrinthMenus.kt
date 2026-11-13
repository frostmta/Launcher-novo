/*
 * Zadrinth Launcher — fork não oficial do Zalith Launcher 2
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

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryItem
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthPillShape
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme

private val SideItemShape = RoundedCornerShape(10.dp)

/** Botão "voltar" redondo; usa o despachante do sistema para respeitar as pilhas internas. */
@Composable
fun ZadrinthBackButton(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val colors = ModrinthTheme.colors
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val label = stringResource(R.string.generic_back)
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.surfaceRaised)
            .clickable(role = Role.Button) {
                onBack?.invoke() ?: backDispatcher?.onBackPressed()
            }
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = null,
            tint = colors.textPrimary
        )
    }
}

/**
 * Menu lateral no estilo do SKlauncher: título no topo e uma lista de categorias
 * (ícone + nome), com a selecionada destacada por uma pílula da cor da marca.
 */
@Composable
fun ZadrinthSideMenu(
    title: String,
    items: List<CategoryItem>,
    isSelected: (CategoryItem) -> Boolean,
    onSelect: (CategoryItem) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    width: Dp = 196.dp,
    showBack: Boolean = true,
    onBack: (() -> Unit)? = null,
) {
    val colors = ModrinthTheme.colors
    val scrollState = rememberScrollState()

    Row(modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .width(width)
                .fillMaxHeight()
                .background(colors.shell.copy(alpha = 0.35f))
                .padding(horizontal = 10.dp)
        ) {
            //Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showBack) ZadrinthBackButton(onBack = onBack)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items.forEach { item ->
                    if (item.division) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 6.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.border)
                        )
                    }
                    SideMenuItem(
                        item = item,
                        selected = isSelected(item),
                        onClick = { onSelect(item) }
                    )
                }
            }
        }
        //Linha que separa o menu do conteúdo
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(colors.border)
        )
    }
}

@Composable
private fun SideMenuItem(
    item: CategoryItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val container by animateColorAsState(
        targetValue = if (selected) colors.brandHighlight else Color.Transparent,
        label = "SideMenuItemContainer"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) colors.brand else colors.textSecondary,
        label = "SideMenuItemIcon"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(SideItemShape)
            .background(container)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides iconTint) {
                item.icon()
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(item.textRes),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) colors.textPrimary else colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Cabeçalho de página com título e abas em pílula (estilo Modrinth/SKlauncher),
 * usado no Explorar.
 */
@Composable
fun ZadrinthTopTabs(
    title: String,
    items: List<CategoryItem>,
    isSelected: (CategoryItem) -> Boolean,
    onSelect: (CategoryItem) -> Unit,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
) {
    val colors = ModrinthTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showBack) ZadrinthBackButton()
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEachIndexed { index, item ->
                if (item.division && index > 0) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(1.dp)
                            .height(20.dp)
                            .background(colors.border)
                    )
                }
                TopTab(
                    item = item,
                    selected = isSelected(item),
                    onClick = { onSelect(item) }
                )
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
private fun TopTab(
    item: CategoryItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val container by animateColorAsState(
        targetValue = if (selected) colors.brandHighlight else colors.surface,
        label = "TopTabContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.brand else colors.textSecondary,
        label = "TopTabContent"
    )
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(ModrinthPillShape)
            .background(container)
            .border(1.dp, if (selected) colors.brand.copy(alpha = 0.5f) else colors.border, ModrinthPillShape)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides content) {
                item.icon()
            }
        }
        Text(
            text = stringResource(item.textRes),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) colors.textPrimary else colors.textSecondary,
            maxLines = 1
        )
    }
}
