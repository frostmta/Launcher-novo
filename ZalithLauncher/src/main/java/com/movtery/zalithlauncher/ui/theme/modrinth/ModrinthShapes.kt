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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Medidas do Design System no estilo Modrinth */
object ModrinthDimens {
    /** Raio dos cards */
    val CardRadius = 12.dp
    /** Raio dos botões */
    val ButtonRadius = 10.dp
    /** Raio dos ícones de projeto/instância */
    val IconRadius = 10.dp
    /** Espessura das bordas finas */
    val BorderWidth = 1.dp
    /** Largura da barra de navegação lateral */
    val NavRailWidth = 64.dp
}

val ModrinthCardShape = RoundedCornerShape(ModrinthDimens.CardRadius)
val ModrinthButtonShape = RoundedCornerShape(ModrinthDimens.ButtonRadius)
val ModrinthIconShape = RoundedCornerShape(ModrinthDimens.IconRadius)
val ModrinthPillShape = RoundedCornerShape(percent = 50)

/**
 * Formas do Material3. "large" é a forma padrão dos cards do Zalith, então
 * ela recebe os 12dp do Modrinth.
 */
val ModrinthShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(ModrinthDimens.CardRadius),
    extraLarge = RoundedCornerShape(16.dp),
)
