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


package com.movtery.zalithlauncher.ui.screens.content.zadrinth

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.zadrinth.CosmeticSlot
import com.movtery.zalithlauncher.game.zadrinth.ShopCosmetic
import com.movtery.zalithlauncher.game.zadrinth.ZadrinthCosmetics
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthBrandButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthCard
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthFilterChip
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthSecondaryButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTag
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

private val PreviewShape = RoundedCornerShape(12.dp)

private val Filters = listOf("Todos", "Capas", "Asas", "Bandanas", "Minha coleção")

/**
 * Loja de cosméticos do launcher. Tudo grátis por enquanto: "Resgatar" põe o
 * item na sua coleção e já equipa. O jogo lê a mesma coleção (cosmetics.json),
 * então o que você escolhe aqui aparece no Zadrinth Client.
 */
@Composable
fun CosmeticShopScreen(
    backStackViewModel: ScreenBackStackViewModel,
) {
    BaseScreen(
        screenKey = NormalNavKey.Shop,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ShopContent(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ShopContent(modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { ZadrinthCosmetics.reload() }
    val state by ZadrinthCosmetics.state.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val colors = ModrinthTheme.colors

    val items = remember(filter, state) {
        ZadrinthCosmetics.catalog.filter { item ->
            when (filter) {
                1 -> item.slot == CosmeticSlot.CAPE
                2 -> item.slot == CosmeticSlot.WINGS
                3 -> item.slot == CosmeticSlot.BANDANA
                4 -> state.owns(item)
                else -> true
            }
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_shopping_bag),
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Loja de cosméticos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Capas com física, asas e bandanas. Aparecem no jogo e para os outros jogadores do Zadrinth.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            ModrinthTag(
                text = "TUDO GRÁTIS",
                color = Color(0x3322C55E),
                contentColor = Color(0xFF4ADE80)
            )
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Filters.forEachIndexed { index, label ->
                ModrinthFilterChip(
                    selected = filter == index,
                    text = label,
                    onClick = { filter = index }
                )
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Resgate itens para montar a sua coleção.",
                    color = colors.textSecondary
                )
            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.key }) { item ->
                    ShopCard(
                        item = item,
                        owned = state.owns(item),
                        equipped = state.isEquipped(item)
                    )
                }
            }
        }

        Text(
            text = "Cores, RGB e brilho de cada item: dentro do jogo, em ESC → Cosméticos.",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun ShopCard(
    item: ShopCosmetic,
    owned: Boolean,
    equipped: Boolean,
) {
    val colors = ModrinthTheme.colors
    val accent = Color(item.accent)
    ModrinthCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(8.dp)
                .clip(PreviewShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x14FFFFFF), accent.copy(alpha = 0.45f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = rememberAssetBitmap(item.previewAsset)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None
                )
            }
            if (item.animated) {
                ModrinthTag(
                    text = "ANIMADO",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    color = Color(0x99000000),
                    contentColor = Color(0xFFFFB21E)
                )
            }
        }
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        equipped -> "USANDO"
                        owned -> "Na coleção"
                        item.isFree -> "GRÁTIS"
                        else -> "${item.price} moedas"
                    },
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        equipped -> colors.brand
                        owned -> colors.textSecondary
                        else -> Color(0xFF4ADE80)
                    }
                )
                when {
                    !owned -> ModrinthBrandButton(
                        onClick = { ZadrinthCosmetics.claim(item) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text(text = "Resgatar", fontSize = 12.sp) }
                    equipped -> ModrinthSecondaryButton(
                        onClick = { ZadrinthCosmetics.unequip(item.slot) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text(text = "Remover", fontSize = 12.sp) }
                    else -> ModrinthSecondaryButton(
                        onClick = { ZadrinthCosmetics.equip(item) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text(text = "Equipar", fontSize = 12.sp) }
                }
            }
        }
    }
}

/** Lê uma imagem de assets/ uma vez só. */
@Composable
private fun rememberAssetBitmap(path: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(path) {
        runCatching {
            context.assets.open(path).use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
        }.getOrNull()
    }
}
