package io.github.frostmta.zadrinth.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.config.HudPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Lista de itens do HUD e o desenho deles na tela. */
public final class HudManager {
	private static final List<HudElement> ELEMENTS = Collections.unmodifiableList(Arrays.<HudElement>asList(
			new ToggleSprintHud()
	));

	private HudManager() {
	}

	public static List<HudElement> elements() {
		return ELEMENTS;
	}

	/** Chamado pelo InGameHudMixin no fim do HUD do jogo. */
	public static void render(MinecraftClient client) {
		if (client.player == null || client.options.debugEnabled) return;
		if (client.currentScreen instanceof io.github.frostmta.zadrinth.client.gui.HudEditorScreen) return;
		Window window = new Window(client);
		for (HudElement element : ELEMENTS) {
			if (element.isEnabled()) {
				draw(client, window, element, false);
			}
		}
	}

	public static void draw(MinecraftClient client, Window window, HudElement element, boolean editing) {
		int[] box = bounds(client, window, element, editing);
		HudPosition pos = element.position();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) box[0], (float) box[1], 0f);
		GlStateManager.scale(pos.scale, pos.scale, 1f);
		GlStateManager.color(1f, 1f, 1f, 1f);
		element.render(client, editing);
		GlStateManager.popMatrix();
	}

	/** {x, y, largura, altura} já com escala, sempre dentro da tela. */
	public static int[] bounds(MinecraftClient client, Window window, HudElement element, boolean editing) {
		HudPosition pos = element.position();
		int w = Math.round(element.width(client, editing) * pos.scale);
		int h = Math.round(element.height(client, editing) * pos.scale);
		int screenW = window.getWidth();
		int screenH = window.getHeight();
		int x = Math.round(pos.x * screenW);
		int y = Math.round(pos.y * screenH);
		x = Math.max(0, Math.min(x, screenW - w));
		y = Math.max(0, Math.min(y, screenH - h));
		return new int[]{x, y, w, h};
	}
}
