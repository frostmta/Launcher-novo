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

package com.movtery.zalithlauncher.ui.theme.zadrinth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ui.theme.modrinth.ZadrinthPalette
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Tokens do visual "vidro" (glassmorphism) do Zadrinth Client. */
object ZadrinthGlass {
    /** Preenchimento do vidro: branco bem transparente sobre o fundo escuro */
    val Fill = Color.White.copy(alpha = 0.06f)
    val FillStrong = Color.White.copy(alpha = 0.10f)
    /** Borda com brilho em cima e quase sumindo embaixo */
    val Border: Brush = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
    )
    val Divider = Color.White.copy(alpha = 0.08f)
    val SpaceTop = Color(0xFF0B0A1A)
    val SpaceBottom = Color(0xFF16112F)
    val Online = Color(0xFF4ADE80)
    val Warning = Color(0xFFFBBF24)
}

/**
 * Superfície de vidro: fundo translúcido + borda fina com brilho.
 * Fica bonito por cima do fundo animado do herói ou do painel escuro.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    strong: Boolean = false,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (strong) ZadrinthGlass.FillStrong else ZadrinthGlass.Fill)
            .border(borderWidth, ZadrinthGlass.Border, shape),
        content = content
    )
}

private data class Star(val x: Float, val y: Float, val radius: Float, val phase: Float, val speed: Float)

/**
 * Fundo do herói da tela inicial: céu escuro com brilhos nas cores da marca
 * e estrelas piscando devagar. Desenhado em código (sem imagem), então fica
 * nítido em qualquer tela.
 */
@Composable
fun ZadrinthHeroBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 70,
) {
    val stars = remember(starCount) {
        val random = Random(42)
        List(starCount) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat(),
                radius = 0.6f + random.nextFloat() * 1.6f,
                phase = random.nextFloat() * 2f * PI.toFloat(),
                speed = 0.6f + random.nextFloat() * 1.4f
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "HeroBackground")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(durationMillis = 9_000, easing = LinearEasing), RepeatMode.Restart),
        label = "HeroTime"
    )

    Canvas(modifier = modifier) {
        //Céu
        drawRect(Brush.verticalGradient(listOf(ZadrinthGlass.SpaceTop, ZadrinthGlass.SpaceBottom)))

        //Brilhos da marca, que se mexem bem devagar
        val drift = sin(time) * size.width * 0.03f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ZadrinthPalette.GradientBlue.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(size.width * 0.15f + drift, size.height * 0.9f),
                radius = size.width * 0.45f
            ),
            radius = size.width * 0.45f,
            center = Offset(size.width * 0.15f + drift, size.height * 0.9f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ZadrinthPalette.GradientViolet.copy(alpha = 0.40f), Color.Transparent),
                center = Offset(size.width * 0.55f - drift, size.height * 0.15f),
                radius = size.width * 0.40f
            ),
            radius = size.width * 0.40f,
            center = Offset(size.width * 0.55f - drift, size.height * 0.15f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ZadrinthPalette.GradientPink.copy(alpha = 0.30f), Color.Transparent),
                center = Offset(size.width * 0.92f, size.height * 0.75f + drift * 0.5f),
                radius = size.width * 0.32f
            ),
            radius = size.width * 0.32f,
            center = Offset(size.width * 0.92f, size.height * 0.75f + drift * 0.5f)
        )

        //Estrelas
        stars.forEach { star ->
            val twinkle = 0.35f + 0.65f * ((sin(time * star.speed + star.phase) + 1f) / 2f)
            drawCircle(
                color = Color.White.copy(alpha = 0.85f * twinkle),
                radius = star.radius * density,
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }

        //Escurece a base para o texto e a faixa de servidores ficarem legíveis
        drawRect(
            Brush.verticalGradient(
                0.55f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.45f)
            )
        )
    }
}
