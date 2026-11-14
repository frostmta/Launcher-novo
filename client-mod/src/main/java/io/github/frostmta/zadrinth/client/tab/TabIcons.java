package io.github.frostmta.zadrinth.client.tab;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.presence.Presence;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.PlayerListEntry;


/**
 * Ícone do Zadrinth ao lado do nome no TAB.
 *
 * O PlayerListHudMixin troca o nome de quem usa o client por
 * MARCADOR + espaços + nome. Os espaços reservam a largura do ícone (assim a
 * coluna do TAB já sai do tamanho certo) e o marcador "§z" é invisível e não
 * ocupa espaço. Na hora de desenhar, tiramos o marcador e pintamos o ícone.
 */
public final class TabIcons {
	/** "§z" não é um código de cor válido: o Minecraft ignora e não conta largura */
	private static final String MARKER = "§z";
	/** 3 espaços = 12 px: ícone de 8 px + folga */
	private static final String PAD = "   ";
	private static final int ICON_SIZE = 8;
	private static final int TEXT_OFFSET = 12;


	private TabIcons() {
	}

	/** Usado no lugar de getPlayerName() dentro do TAB. */
	public static String decorate(PlayerListEntry entry, String name) {
		if (!ZadrinthConfig.get().tabIconEnabled || entry == null || entry.getProfile() == null) return name;
		if (!Presence.isZadrinthUser(entry.getProfile().getName())) return name;
		return MARKER + PAD + name;
	}

	/** Usado no lugar de drawWithShadow() dentro do TAB. */
	public static int drawName(TextRenderer renderer, String text, float x, float y, int color) {
		int index = text == null ? -1 : text.indexOf(MARKER + PAD);
		if (index < 0) {
			return renderer.drawWithShadow(text, x, y, color);
		}
		//Pode vir um código antes do marcador (ex.: itálico de espectador)
		String prefix = text.substring(0, index);
		String rest = prefix + text.substring(index + MARKER.length() + PAD.length());
		drawIcon(Math.round(x), Math.round(y));
		return renderer.drawWithShadow(rest, x + TEXT_OFFSET, y, color);
	}

	private static void drawIcon(int x, int y) {
		//O símbolo do Zadrinth Client: versão de 32 px (lisa) ou de 16 px (pixel art) conforme a escala
		int scale = new net.minecraft.client.util.Window(MinecraftClient.getInstance()).getScaleFactor();
		boolean big = scale * ICON_SIZE >= 20;
		if (!io.github.frostmta.zadrinth.client.gui.Textures.bind(big ? "textures/tab_icon_32.png" : "textures/tab_icon_16.png", big)) return;
		GlStateManager.enableBlend();
		GlStateManager.color(1f, 1f, 1f, 1f);
		int size = big ? 32 : 16;
		DrawableHelper.drawTexture(x + 1, y, 0f, 0f, size, size, ICON_SIZE, ICON_SIZE, (float) size, (float) size);
	}
}
