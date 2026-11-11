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

package com.movtery.zalithlauncher.ui.theme.modrinth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R

/**
 * Card no estilo Modrinth: cantos de 12dp, borda fina e cor de superfície.
 * @param onClick quando não for nulo, o card inteiro vira clicável
 */
@Composable
fun ModrinthCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = ModrinthCardShape,
    color: Color = ModrinthTheme.colors.surface,
    contentColor: Color = ModrinthTheme.colors.textPrimary,
    border: BorderStroke? = BorderStroke(ModrinthDimens.BorderWidth, ModrinthTheme.colors.border),
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            color = color,
            contentColor = contentColor,
            border = border,
        ) {
            Column(content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            contentColor = contentColor,
            border = border,
        ) {
            Column(content = content)
        }
    }
}

/**
 * Botão "Jogar" verde com ícone de triângulo.
 */
@Composable
fun ModrinthPlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = stringResource(R.string.modrinth_play),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
) {
    ModrinthBrandButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(R.drawable.ic_play_arrow_filled),
            contentDescription = null
        )
        Box(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}

/**
 * Botão circular verde só com o triângulo de "Jogar" (usado nos cards).
 */
@Composable
fun ModrinthPlayIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 40.dp,
) {
    val colors = ModrinthTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) colors.brand else colors.surfaceRaised,
        contentColor = if (enabled) colors.onBrand else colors.textSecondary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(size * 0.55f),
                painter = painterResource(R.drawable.ic_play_arrow_filled),
                contentDescription = null
            )
        }
    }
}

/**
 * Botão primário verde (texto escuro sobre o verde da marca).
 */
@Composable
fun ModrinthBrandButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit
) {
    val colors = ModrinthTheme.colors
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = ModrinthButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.brand,
            contentColor = colors.onBrand,
            disabledContainerColor = colors.surfaceRaised,
            disabledContentColor = colors.textSecondary,
        ),
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Botão secundário (cinza elevado), como os botões comuns do Modrinth.
 */
@Composable
fun ModrinthSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit
) {
    val colors = ModrinthTheme.colors
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = ModrinthButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.surfaceRaised,
            contentColor = colors.textPrimary,
            disabledContainerColor = colors.surfaceRaised.copy(alpha = 0.5f),
            disabledContentColor = colors.textSecondary,
        ),
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Etiqueta em formato de pílula (loader, versão, categoria).
 * @param icon ícone opcional exibido antes do texto
 */
@Composable
fun ModrinthTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = ModrinthTheme.colors.surfaceRaised,
    contentColor: Color = ModrinthTheme.colors.textSecondary,
    icon: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = ModrinthPillShape,
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon?.invoke()
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Campo de busca no estilo Modrinth (lupa à esquerda, botão de limpar).
 */
@Composable
fun ModrinthSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors
    val focusManager = LocalFocusManager.current
    val textStyle = MaterialTheme.typography.bodyMedium

    Surface(
        modifier = modifier,
        shape = ModrinthButtonShape,
        color = colors.surface,
        contentColor = colors.textPrimary,
        border = BorderStroke(ModrinthDimens.BorderWidth, colors.border),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 40.dp)
                .padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = colors.textSecondary
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        style = textStyle,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = textStyle.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.brand),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                )
            }
            if (value.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = { onValueChange("") }
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.generic_clear),
                        tint = colors.textSecondary
                    )
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

/**
 * Chip de filtro: selecionado fica com o realce verde da marca.
 */
@Composable
fun ModrinthFilterChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected },
        shape = ModrinthPillShape,
        color = if (selected) colors.brandHighlight else colors.surface,
        contentColor = if (selected) colors.brand else colors.textSecondary,
        border = if (selected) null else BorderStroke(ModrinthDimens.BorderWidth, colors.border),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}
