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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

/**
 * Paleta do Zadrinth Launcher.
 *
 * O layout segue o Modrinth App; as cores vêm da identidade do Zadrinth:
 * degradê azul → violeta → rosa e fundos escuros puxados para o roxo.
 * O modo escuro é o padrão. Os valores aqui alimentam o [ColorScheme] do
 * Material3 (para que todas as telas existentes herdem o visual) e os tokens
 * de [ModrinthColors] usados pelos componentes novos.
 */
object ZadrinthPalette {
    // --- Escuro (padrão) ---
    /** Fundo principal do app */
    val DarkBackground = Color(0xFF15131C)
    /** Superfícies, cards e barras laterais */
    val DarkSurface = Color(0xFF221F2C)
    /** Superfícies elevadas, estados de toque e campos de texto */
    val DarkSurfaceRaised = Color(0xFF2E2A3A)
    /** Cor de destaque da marca (violeta) */
    val Brand = Color(0xFF9D7BFF)
    /** Texto sobre a cor da marca */
    val OnBrand = Color(0xFF0E0A1A)
    /** Violeta a ~22% sobre [DarkSurface], já composto (sem alfa) */
    val DarkBrandHighlight = Color(0xFF3D335A)
    val DarkOnBrandHighlight = Color(0xFFC4B2FF)
    val DarkTextPrimary = Color(0xFFFFFFFF)
    val DarkTextSecondary = Color(0xFFB8B2C9)
    /** Bordas sutis */
    val DarkBorder = Color(0xFF3B3649)

    // --- Degradê da marca (botões Jogar / ações principais) ---
    val GradientBlue = Color(0xFF6E9BFF)
    val GradientViolet = Color(0xFF9D7BFF)
    val GradientPink = Color(0xFFE07BFF)
    /** Texto sobre o degradê (contraste ≥ 6:1 em todo o degradê) */
    val OnGradient = Color(0xFF0E0A1A)

    // --- Cores de apoio (iguais nos dois modos) ---
    val Red = Color(0xFFFF5C7C)
    val Orange = Color(0xFFFFA347)
    val Blue = GradientBlue
    val Pink = GradientPink

    // --- Claro (alternativo) ---
    val LightBackground = Color(0xFFEFECF6)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceRaised = Color(0xFFF4F1FA)
    /** Violeta mais escuro para manter contraste sobre fundo claro */
    val LightBrand = Color(0xFF6D4AE0)
    val LightOnBrand = Color(0xFFFFFFFF)
    val LightBrandHighlight = Color(0xFFE6DEFF)
    val LightOnBrandHighlight = Color(0xFF3A1F99)
    val LightTextPrimary = Color(0xFF1B1726)
    val LightTextSecondary = Color(0xFF5E5870)
    val LightBorder = Color(0xFFDCD6E8)
}

/** Degradê azul → violeta → rosa da marca */
val ZadrinthBrandGradient: Brush = Brush.horizontalGradient(
    listOf(ZadrinthPalette.GradientBlue, ZadrinthPalette.GradientViolet, ZadrinthPalette.GradientPink)
)

/** Esquema Material3 escuro do Zadrinth */
val ZadrinthDarkColorScheme: ColorScheme = darkColorScheme(
    primary = ZadrinthPalette.Brand,
    onPrimary = ZadrinthPalette.OnBrand,
    primaryContainer = ZadrinthPalette.DarkBrandHighlight,
    onPrimaryContainer = ZadrinthPalette.DarkOnBrandHighlight,
    inversePrimary = ZadrinthPalette.LightBrand,

    // Estados selecionados (rails, chips, botões tonais) usam o realce violeta
    secondary = ZadrinthPalette.DarkTextSecondary,
    onSecondary = ZadrinthPalette.DarkBackground,
    secondaryContainer = ZadrinthPalette.DarkBrandHighlight,
    onSecondaryContainer = ZadrinthPalette.DarkOnBrandHighlight,

    tertiary = ZadrinthPalette.Pink,
    onTertiary = ZadrinthPalette.OnGradient,
    tertiaryContainer = Color(0xFF4A2A5C),
    onTertiaryContainer = Color(0xFFF5D6FF),

    error = ZadrinthPalette.Red,
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF5A1A2A),
    onErrorContainer = Color(0xFFFFD9E0),

    background = ZadrinthPalette.DarkBackground,
    onBackground = ZadrinthPalette.DarkTextPrimary,
    surface = ZadrinthPalette.DarkBackground,
    onSurface = ZadrinthPalette.DarkTextPrimary,
    surfaceVariant = ZadrinthPalette.DarkSurfaceRaised,
    onSurfaceVariant = ZadrinthPalette.DarkTextSecondary,
    surfaceTint = ZadrinthPalette.Brand,

    inverseSurface = Color(0xFFE9E5F2),
    inverseOnSurface = ZadrinthPalette.DarkBackground,

    outline = Color(0xFF6A6380),
    outlineVariant = ZadrinthPalette.DarkBorder,
    scrim = Color(0xFF000000),

    surfaceDim = Color(0xFF0F0D15),
    // O Zalith usa surfaceBright como cor dos cards
    surfaceBright = ZadrinthPalette.DarkSurface,
    surfaceContainerLowest = Color(0xFF0F0D15),
    surfaceContainerLow = Color(0xFF1B1824),
    // O Zalith usa surfaceContainer como fundo geral do app
    surfaceContainer = ZadrinthPalette.DarkBackground,
    surfaceContainerHigh = ZadrinthPalette.DarkSurface,
    surfaceContainerHighest = ZadrinthPalette.DarkSurfaceRaised,
)

/** Esquema Material3 claro do Zadrinth */
val ZadrinthLightColorScheme: ColorScheme = lightColorScheme(
    primary = ZadrinthPalette.LightBrand,
    onPrimary = ZadrinthPalette.LightOnBrand,
    primaryContainer = ZadrinthPalette.LightBrandHighlight,
    onPrimaryContainer = ZadrinthPalette.LightOnBrandHighlight,
    inversePrimary = ZadrinthPalette.Brand,

    secondary = ZadrinthPalette.LightTextSecondary,
    onSecondary = ZadrinthPalette.LightSurface,
    secondaryContainer = ZadrinthPalette.LightBrandHighlight,
    onSecondaryContainer = ZadrinthPalette.LightOnBrandHighlight,

    tertiary = Color(0xFFA03DC8),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5D6FF),
    onTertiaryContainer = Color(0xFF4A1A5C),

    error = Color(0xFFC4264B),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFD9E0),
    onErrorContainer = Color(0xFF5A1A2A),

    background = ZadrinthPalette.LightBackground,
    onBackground = ZadrinthPalette.LightTextPrimary,
    surface = ZadrinthPalette.LightBackground,
    onSurface = ZadrinthPalette.LightTextPrimary,
    surfaceVariant = ZadrinthPalette.LightSurfaceRaised,
    onSurfaceVariant = ZadrinthPalette.LightTextSecondary,
    surfaceTint = ZadrinthPalette.LightBrand,

    inverseSurface = ZadrinthPalette.DarkSurface,
    inverseOnSurface = Color(0xFFFFFFFF),

    outline = Color(0xFF8C859E),
    outlineVariant = ZadrinthPalette.LightBorder,
    scrim = Color(0xFF000000),

    surfaceDim = Color(0xFFDDD8E8),
    surfaceBright = ZadrinthPalette.LightSurface,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F5FB),
    surfaceContainer = ZadrinthPalette.LightBackground,
    surfaceContainerHigh = ZadrinthPalette.LightSurface,
    surfaceContainerHighest = ZadrinthPalette.LightSurfaceRaised,
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
    /** Fundo das ações principais (degradê no tema Zadrinth, cor sólida nos outros temas) */
    val brandBrush: Brush,
    /** Texto/ícone sobre [brandBrush] */
    val onBrandBrush: Color,
    val red: Color = ZadrinthPalette.Red,
    val orange: Color = ZadrinthPalette.Orange,
    val blue: Color = ZadrinthPalette.Blue,
    val pink: Color = ZadrinthPalette.Pink,
)

fun ColorScheme.toModrinthColors(): ModrinthColors {
    val isZadrinthTheme = primary == ZadrinthPalette.Brand || primary == ZadrinthPalette.LightBrand
    return ModrinthColors(
        background = surfaceContainer,
        surface = surfaceContainerHigh,
        surfaceRaised = surfaceContainerHighest,
        brand = primary,
        onBrand = onPrimary,
        brandHighlight = primaryContainer,
        textPrimary = onSurface,
        textSecondary = onSurfaceVariant,
        border = outlineVariant,
        brandBrush = if (isZadrinthTheme) ZadrinthBrandGradient else SolidColor(primary),
        onBrandBrush = if (isZadrinthTheme) ZadrinthPalette.OnGradient else onPrimary,
        red = error,
    )
}

object ModrinthTheme {
    /** Tokens de cor do tema atual */
    val colors: ModrinthColors
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.toModrinthColors()
}
