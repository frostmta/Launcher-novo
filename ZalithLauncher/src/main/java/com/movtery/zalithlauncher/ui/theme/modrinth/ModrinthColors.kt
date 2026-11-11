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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Paleta base inspirada no Modrinth App (desktop).
 *
 * O modo escuro é o padrão. Os valores aqui são a única fonte de verdade:
 * eles alimentam o [ColorScheme] do Material3 (para que todas as telas
 * existentes herdem o visual) e os tokens de [ModrinthColors] usados pelos
 * componentes novos.
 */
object ModrinthPalette {
    // --- Escuro (padrão) ---
    /** Fundo principal do app */
    val DarkBackground = Color(0xFF16181C)
    /** Superfícies, cards e barras laterais */
    val DarkSurface = Color(0xFF26292F)
    /** Superfícies elevadas, estados de toque e campos de texto */
    val DarkSurfaceRaised = Color(0xFF32363E)
    /** Cor de destaque da marca */
    val Brand = Color(0xFF1BD96A)
    /** Texto sobre botões verdes */
    val OnBrand = Color(0xFF000000)
    /** Verde da marca a 25% sobre [DarkSurface], já composto (sem alfa) */
    val DarkBrandHighlight = Color(0xFF23553E)
    val DarkTextPrimary = Color(0xFFFFFFFF)
    val DarkTextSecondary = Color(0xFFB0B5BF)
    /** Bordas sutis */
    val DarkBorder = Color(0xFF3B4048)

    // --- Cores de apoio (iguais nos dois modos) ---
    val Red = Color(0xFFFF496E)
    val Orange = Color(0xFFFFA347)
    val Blue = Color(0xFF4F9CFF)
    val Purple = Color(0xFFC78AFF)

    // --- Claro (alternativo) ---
    val LightBackground = Color(0xFFEBEDF0)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceRaised = Color(0xFFF1F2F4)
    /** Verde mais escuro para manter contraste sobre fundo claro */
    val LightBrand = Color(0xFF00843F)
    val LightOnBrand = Color(0xFFFFFFFF)
    val LightBrandHighlight = Color(0xFFD3F2E0)
    val LightTextPrimary = Color(0xFF1A1C20)
    val LightTextSecondary = Color(0xFF5B616B)
    val LightBorder = Color(0xFFD5D9DE)
}

/** Esquema Material3 escuro com a paleta do Modrinth */
val ModrinthDarkColorScheme: ColorScheme = darkColorScheme(
    primary = ModrinthPalette.Brand,
    onPrimary = ModrinthPalette.OnBrand,
    primaryContainer = ModrinthPalette.DarkBrandHighlight,
    onPrimaryContainer = ModrinthPalette.Brand,
    inversePrimary = Color(0xFF00843F),

    // Estados selecionados (rails, chips, botões tonais) usam o realce verde do Modrinth
    secondary = ModrinthPalette.DarkTextSecondary,
    onSecondary = ModrinthPalette.DarkBackground,
    secondaryContainer = ModrinthPalette.DarkBrandHighlight,
    onSecondaryContainer = ModrinthPalette.Brand,

    tertiary = ModrinthPalette.Blue,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF1F3A5C),
    onTertiaryContainer = Color(0xFFD6E6FF),

    error = ModrinthPalette.Red,
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF5A1A2A),
    onErrorContainer = Color(0xFFFFD9E0),

    background = ModrinthPalette.DarkBackground,
    onBackground = ModrinthPalette.DarkTextPrimary,
    surface = ModrinthPalette.DarkBackground,
    onSurface = ModrinthPalette.DarkTextPrimary,
    surfaceVariant = ModrinthPalette.DarkSurfaceRaised,
    onSurfaceVariant = ModrinthPalette.DarkTextSecondary,
    surfaceTint = ModrinthPalette.Brand,

    inverseSurface = Color(0xFFE5E7EB),
    inverseOnSurface = ModrinthPalette.DarkBackground,

    outline = Color(0xFF5A606B),
    outlineVariant = ModrinthPalette.DarkBorder,
    scrim = Color(0xFF000000),

    surfaceDim = Color(0xFF101215),
    // O Zalith usa surfaceBright como cor dos cards
    surfaceBright = ModrinthPalette.DarkSurface,
    surfaceContainerLowest = Color(0xFF101215),
    surfaceContainerLow = Color(0xFF1C1F24),
    // O Zalith usa surfaceContainer como fundo geral do app
    surfaceContainer = ModrinthPalette.DarkBackground,
    surfaceContainerHigh = ModrinthPalette.DarkSurface,
    surfaceContainerHighest = ModrinthPalette.DarkSurfaceRaised,
)

/** Esquema Material3 claro com a paleta do Modrinth */
val ModrinthLightColorScheme: ColorScheme = lightColorScheme(
    primary = ModrinthPalette.LightBrand,
    onPrimary = ModrinthPalette.LightOnBrand,
    primaryContainer = ModrinthPalette.LightBrandHighlight,
    onPrimaryContainer = Color(0xFF00522A),
    inversePrimary = ModrinthPalette.Brand,

    secondary = ModrinthPalette.LightTextSecondary,
    onSecondary = ModrinthPalette.LightSurface,
    secondaryContainer = ModrinthPalette.LightBrandHighlight,
    onSecondaryContainer = Color(0xFF00522A),

    tertiary = Color(0xFF1F6FD1),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6E6FF),
    onTertiaryContainer = Color(0xFF0B2F5C),

    error = Color(0xFFCC2A4B),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFD9E0),
    onErrorContainer = Color(0xFF5A1A2A),

    background = ModrinthPalette.LightBackground,
    onBackground = ModrinthPalette.LightTextPrimary,
    surface = ModrinthPalette.LightBackground,
    onSurface = ModrinthPalette.LightTextPrimary,
    surfaceVariant = ModrinthPalette.LightSurfaceRaised,
    onSurfaceVariant = ModrinthPalette.LightTextSecondary,
    surfaceTint = ModrinthPalette.LightBrand,

    inverseSurface = Color(0xFF26292F),
    inverseOnSurface = Color(0xFFFFFFFF),

    outline = Color(0xFF8A9099),
    outlineVariant = ModrinthPalette.LightBorder,
    scrim = Color(0xFF000000),

    surfaceDim = Color(0xFFDCDFE3),
    surfaceBright = ModrinthPalette.LightSurface,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F6F8),
    surfaceContainer = ModrinthPalette.LightBackground,
    surfaceContainerHigh = ModrinthPalette.LightSurface,
    surfaceContainerHighest = ModrinthPalette.LightSurfaceRaised,
)

/**
 * Tokens semânticos usados pelos componentes no estilo Modrinth.
 *
 * São sempre derivados do [ColorScheme] atual (veja [ModrinthTheme.colors]),
 * então os componentes novos continuam coerentes mesmo que o usuário
 * escolha outro tema de cores nas configurações.
 */
@Immutable
data class ModrinthColors(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val brand: Color,
    val onBrand: Color,
    val brandHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val red: Color = ModrinthPalette.Red,
    val orange: Color = ModrinthPalette.Orange,
    val blue: Color = ModrinthPalette.Blue,
    val purple: Color = ModrinthPalette.Purple,
)

fun ColorScheme.toModrinthColors(): ModrinthColors = ModrinthColors(
    background = surfaceContainer,
    surface = surfaceContainerHigh,
    surfaceRaised = surfaceContainerHighest,
    brand = primary,
    onBrand = onPrimary,
    brandHighlight = primaryContainer,
    textPrimary = onSurface,
    textSecondary = onSurfaceVariant,
    border = outlineVariant,
    red = error,
)

object ModrinthTheme {
    /** Tokens de cor do tema atual */
    val colors: ModrinthColors
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.toModrinthColors()
}
