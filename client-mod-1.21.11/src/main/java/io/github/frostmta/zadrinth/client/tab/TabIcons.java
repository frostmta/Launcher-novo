package io.github.frostmta.zadrinth.client.tab;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.presence.Presence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Ícone do Zadrinth ao lado do nome no TAB.
 *
 * O nome de quem usa o client ganha 3 espaços na frente (a coluna do TAB já
 * sai do tamanho certo) marcados com uma "inserção" invisível. Na hora de
 * escrever o nome, o ícone é pintado em cima desses espaços.
 */
public final class TabIcons {
	private static final String MARKER = "zadrinth-client:tab";
	private static final String PAD = "   ";
	private static final int ICON_SIZE = 8;
	private static final Identifier ICON_SMALL = ZadrinthClient.id("textures/tab_icon_16.png");
	private static final Identifier ICON_BIG = ZadrinthClient.id("textures/tab_icon_32.png");

	private TabIcons() {
	}

	public static Component decorate(PlayerInfo info, Component name) {
		if (!ZadrinthConfig.get().tabIconEnabled || info == null || info.getProfile() == null) return name;
		if (!Presence.isZadrinthUser(info.getProfile().name())) return name;
		return Component.literal(PAD).withStyle(style -> style.withInsertion(MARKER)).append(name);
	}

	public static void drawIcon(GuiGraphics graphics, Component text, int x, int y) {
		if (text == null || !MARKER.equals(text.getStyle().getInsertion())) return;
		//Versão de 32 px (lisa) ou de 16 px (pixel art) conforme a escala da interface
		int scale = (int) Minecraft.getInstance().getWindow().getGuiScale();
		boolean big = scale * ICON_SIZE >= 20;
		int size = big ? 32 : 16;
		graphics.blit(RenderPipelines.GUI_TEXTURED, big ? ICON_BIG : ICON_SMALL, x + 1, y, 0f, 0f, ICON_SIZE, ICON_SIZE, size, size, size, size);
	}
}
