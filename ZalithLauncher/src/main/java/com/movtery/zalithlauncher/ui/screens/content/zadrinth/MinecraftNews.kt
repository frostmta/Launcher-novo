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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.ui.theme.modrinth.ModrinthTheme
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsText
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Uma notícia do Minecraft (mesma fonte usada pelo launcher oficial).
 */
data class MinecraftNewsItem(
    val title: String,
    val date: LocalDate?,
    val imageUrl: String?,
    val link: String?,
)

/**
 * Busca as notícias públicas do Minecraft e guarda em memória durante a sessão.
 */
object MinecraftNewsRepository {
    private const val TAG = "MinecraftNews"
    private const val CONTENT_HOST = "https://launchercontent.mojang.com"
    private const val NEWS_URL = "$CONTENT_HOST/news.json"
    private const val MAX_ITEMS = 20

    private val mutex = Mutex()
    private var cache: List<MinecraftNewsItem>? = null

    suspend fun load(force: Boolean = false): List<MinecraftNewsItem> = mutex.withLock {
        if (!force) cache?.let { return@withLock it }
        val text = withContext(Dispatchers.IO) {
            GLOBAL_CLIENT.get(NEWS_URL).safeBodyAsText()
        }
        val items = withContext(Dispatchers.Default) { parse(text) }
        cache = items
        items
    }

    private fun parse(text: String): List<MinecraftNewsItem> {
        val entries = JsonParser.parseString(text).asJsonObject.getAsJsonArray("entries")
            ?: return emptyList()

        val parsed = entries.mapNotNull { element ->
            runCatching {
                val obj = element.asJsonObject
                val title = obj.string("title")?.trim()?.takeIf { it.isNotEmpty() } ?: return@runCatching null
                val types = obj.getAsJsonArray("newsType")
                    ?.mapNotNull { runCatching { it.asString }.getOrNull() }
                    .orEmpty()
                val isJava = types.any { it.contains("Java", ignoreCase = true) } ||
                        obj.string("category")?.contains("Java", ignoreCase = true) == true
                val image = obj.getAsJsonObject("newsPageImage")?.string("url")
                    ?: obj.getAsJsonObject("playPageImage")?.string("url")
                isJava to MinecraftNewsItem(
                    title = title,
                    date = obj.string("date")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
                    imageUrl = image?.let { if (it.startsWith("/")) CONTENT_HOST + it else it },
                    link = obj.string("readMoreLink")?.trim()
                )
            }.onFailure { e ->
                Logger.warning(TAG, "Skipping a news entry that could not be parsed", e)
            }.getOrNull()
        }

        //Prefere as notícias da Java Edition; se não houver nenhuma marcada, mostra todas
        val java = parsed.filter { it.first }.map { it.second }
        val list = java.ifEmpty { parsed.map { it.second } }
        return list.sortedByDescending { it.date }.take(MAX_ITEMS)
    }

    private fun JsonObject.string(key: String): String? {
        val element = get(key) ?: return null
        if (element.isJsonNull || !element.isJsonPrimitive) return null
        return element.asString
    }
}

private sealed interface NewsState {
    data object Loading : NewsState
    data object Error : NewsState
    data class Loaded(val items: List<MinecraftNewsItem>) : NewsState
}

/**
 * Painel "Novidades" (como o "News" do SKlauncher).
 */
@Composable
fun MinecraftNewsPanel(
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ModrinthTheme.colors
    var state by remember { mutableStateOf<NewsState>(NewsState.Loading) }
    var reloadCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadCount) {
        state = NewsState.Loading
        state = try {
            NewsState.Loaded(MinecraftNewsRepository.load(force = reloadCount > 0))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.warning("MinecraftNews", "Unable to load Minecraft news", e)
            NewsState.Error
        }
    }

    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_article_outlined),
                contentDescription = null,
                tint = colors.textSecondary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.news_title),
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            IconButton(onClick = { reloadCount++ }) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = stringResource(R.string.library_refresh),
                    tint = colors.textSecondary
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.border)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val current = state) {
                NewsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.Center),
                        color = colors.brand,
                        strokeWidth = 3.dp
                    )
                }
                NewsState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.news_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = { reloadCount++ }) {
                            Text(text = stringResource(R.string.generic_retry))
                        }
                    }
                }
                is NewsState.Loaded -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(current.items) { item ->
                            NewsRow(
                                item = item,
                                onClick = { item.link?.let(onOpenLink) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsRow(
    item: MinecraftNewsItem,
    onClick: () -> Unit,
) {
    val colors = ModrinthTheme.colors
    val dateText = remember(item.date) {
        item.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = item.link != null, onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier
                .size(width = 72.dp, height = 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceRaised),
            model = item.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            dateText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
