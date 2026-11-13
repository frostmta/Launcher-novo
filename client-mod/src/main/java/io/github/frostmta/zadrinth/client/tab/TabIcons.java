package io.github.frostmta.zadrinth.client.tab;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.presence.Presence;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

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

	private static Identifier texture;
	private static boolean textureFailed;

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
		Identifier id = texture();
		if (id == null) return;
		MinecraftClient.getInstance().getTextureManager().bindTexture(id);
		GlStateManager.enableBlend();
		GlStateManager.color(1f, 1f, 1f, 1f);
		DrawableHelper.drawTexture(x + 1, y, 0f, 0f, 16, 16, ICON_SIZE, ICON_SIZE, 16f, 16f);
	}

	/**
	 * Carrega o ícone direto do .jar (sem depender da Legacy Fabric API para
	 * registrar os recursos do mod).
	 */
	private static Identifier texture() {
		if (texture != null || textureFailed) return texture;
		try (InputStream in = TabIcons.class.getResourceAsStream("/assets/zadrinth-client/tab_icon.png")) {
			if (in == null) throw new IllegalStateException("tab_icon.png não encontrado");
			BufferedImage image = ImageIO.read(in);
			texture = MinecraftClient.getInstance().getTextureManager()
					.registerDynamicTexture("zadrinth_tab_icon", new NativeImageBackedTexture(image));
		} catch (Exception e) {
			textureFailed = true;
			ZadrinthClient.LOGGER.warn("Não foi possível carregar o ícone do TAB", e);
		}
		return texture;
	}
}
