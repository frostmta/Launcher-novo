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

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.screens.content.elements.ModLoaderIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthBrandButton
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthIconShape
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTag
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme

/** Ícone da instância dentro de um quadrado arredondado */
@Composable
fun InstanceIconBox(
    version: Version,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(ModrinthIconShape)
            .background(ModrinthTheme.colors.surfaceRaised),
        contentAlignment = Alignment.Center
    ) {
        VersionIconImage(
            version = version,
            modifier = Modifier.size(size * 0.78f)
        )
    }
}

/** Etiquetas de versão do jogo e loader (ex.: [1.21.4] [Fabric]) */
@Composable
fun InstanceTags(
    version: Version,
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors
    val info = version.getVersionInfo()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!version.isValid() || info == null) {
            ModrinthTag(
                text = stringResource(R.string.library_invalid),
                color = colors.red.copy(alpha = 0.16f),
                contentColor = colors.red
            )
        } else {
            info.minecraftVersion.takeIf { it.isNotBlank() }?.let { mcVersion ->
                ModrinthTag(
                    modifier = Modifier.weight(1f, fill = false),
                    text = mcVersion
                )
            }
            val loader = info.primaryLoader
            if (loader != null) {
                ModrinthTag(
                    modifier = Modifier.weight(1f, fill = false),
                    text = loader.loader.displayName,
                    icon = {
                        ModLoaderIcon(
                            modifier = Modifier.size(14.dp),
                            modloader = loader.loader,
                            defaultIcon = R.drawable.img_minecraft
                        )
                    }
                )
            } else {
                ModrinthTag(
                    modifier = Modifier.weight(1f, fill = false),
                    text = stringResource(R.string.library_filter_vanilla)
                )
            }
        }
    }
}

/** "12h 30min", "45 min" ou "Nunca jogado" */
@Composable
fun instancePlayTimeText(seconds: Long): String {
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

/** "Hoje", "Ontem", "Há 3 dias"... ou "Nunca" */
@Composable
fun instanceLastPlayedText(timestamp: Long): String {
    if (timestamp <= 0L) return stringResource(R.string.instance_never)
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.DAY_IN_MILLIS
    ).toString()
}

/** "14 mods", "1 mod" ou "Sem mods" */
@Composable
fun instanceModsText(count: Int): String = when (count) {
    0 -> stringResource(R.string.instance_no_mods)
    1 -> stringResource(R.string.instance_mods_one)
    else -> stringResource(R.string.instance_mods_count, count)
}

/** Estado vazio no estilo do SKlauncher: ícone num círculo, título, texto e ação */
@Composable
fun ZadrinthEmptyState(
    iconRes: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    val colors = ModrinthTheme.colors
    Column(
        modifier = modifier
            .widthIn(max = 360.dp)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(ModrinthIconShape)
                .background(colors.surfaceRaised),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = colors.textSecondary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        if (actionText != null) {
            Spacer(Modifier.height(12.dp))
            ModrinthBrandButton(onClick = onAction) {
                Text(text = actionText)
            }
        }
    }
}
