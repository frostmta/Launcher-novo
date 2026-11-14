package io.github.frostmta.zadrinth.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.config.HudPosition;
import io.github.frostmta.zadrinth.client.gui.HudEditorScreen;
import io.github.frostmta.zadrinth.client.module.ModuleManager;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.impl.CrosshairModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

/** Desenha os módulos de HUD ligados (chamado no fim do HUD do jogo). */
public final class HudManager {
	private HudManager() {
	}

	public static void render(MinecraftClient client) {
		if (client.player == null) return;
		Window window = new Window(client);
		CrosshairModule.render(window.getWidth(), window.getHeight());
		if (client.currentScreen instanceof HudEditorScreen) return;
		for (HudModule module : ModuleManager.hudModules()) {
			if (!module.isEnabled()) continue;
			//No F3 só o placar continua (como no jogo normal)
			if (client.options.debugEnabled && module != ModuleManager.SCOREBOARD) continue;
			try {
				if (!module.shouldRender(client)) continue;
				draw(client, window, module, false);
			} catch (RuntimeException e) {
				//um módulo com erro não pode derrubar o HUD
			}
		}
		GlStateManager.color(1f, 1f, 1f, 1f);
	}

	public static void draw(MinecraftClient client, Window window, HudModule module, boolean editing) {
		int[] box = bounds(client, window, module, editing);
		HudPosition pos = module.position();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) box[0], (float) box[1], 0f);
		GlStateManager.scale(pos.scale, pos.scale, 1f);
		GlStateManager.color(1f, 1f, 1f, 1f);
		module.render(client, editing);
		GlStateManager.popMatrix();
	}

	/** {x, y, largura, altura} já com escala, sempre dentro da tela. */
	public static int[] bounds(MinecraftClient client, Window window, HudModule module, boolean editing) {
		HudPosition pos = module.position();
		int w = Math.round(module.width(client, editing) * pos.scale);
		int h = Math.round(module.height(client, editing) * pos.scale);
		int screenW = window.getWidth();
		int screenH = window.getHeight();
		int x = Math.round(pos.x * screenW);
		int y = Math.round(pos.y * screenH);
		x = Math.max(0, Math.min(x, screenW - w));
		y = Math.max(0, Math.min(y, screenH - h));
		return new int[]{x, y, w, h};
	}
}
