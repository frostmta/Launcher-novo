package io.github.frostmta.zadrinth.client.hud;

import io.github.frostmta.zadrinth.client.config.HudPosition;
import io.github.frostmta.zadrinth.client.gui.HudEditorScreen;
import io.github.frostmta.zadrinth.client.gui.Ui;
import io.github.frostmta.zadrinth.client.module.ModuleManager;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.impl.AutoClickerModule;
import io.github.frostmta.zadrinth.client.module.impl.CrosshairModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;

/** Desenha os módulos de HUD ligados (registrado no fim do HUD do jogo). */
public final class HudManager {
	private HudManager() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.options.hideGui) return;
		Ui.begin(graphics);
		AutoClickerModule.onFrame(client);
		if (client.screen instanceof HudEditorScreen) return;
		boolean debug = client.debugEntries.isOverlayVisible();
		for (HudModule module : ModuleManager.hudModules()) {
			if (!module.isEnabled() || module == ModuleManager.SCOREBOARD) continue;
			//Com o F3 aberto o HUD do client sai do caminho
			if (debug) continue;
			try {
				if (!module.shouldRender(client)) continue;
				draw(client, module, false);
			} catch (RuntimeException e) {
				//um módulo com erro não pode derrubar o HUD
			}
		}
	}

	/** A mira do client substitui a do jogo? (desenha a nossa se sim) */
	public static boolean replaceCrosshair() {
		if (!CrosshairModule.active()) return false;
		Minecraft client = Minecraft.getInstance();
		//Mesmas condições da mira do jogo: só em 1ª pessoa; espectador e mira 3D do F3 ficam com a do jogo
		if (!client.options.getCameraType().isFirstPerson()) return true;
		if (client.player == null || client.player.isSpectator()
				|| client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR)) return false;
		CrosshairModule.render(Ui.g().guiWidth(), Ui.g().guiHeight());
		return true;
	}

	public static void draw(Minecraft client, HudModule module, boolean editing) {
		int[] box = bounds(client, module, editing);
		HudPosition pos = module.position();
		Ui.push();
		Ui.translate(box[0], box[1]);
		Ui.scale(pos.scale);
		module.render(client, editing);
		Ui.pop();
	}

	/** {x, y, largura, altura} já com escala, sempre dentro da tela. */
	public static int[] bounds(Minecraft client, HudModule module, boolean editing) {
		HudPosition pos = module.position();
		int w = Math.round(module.width(client, editing) * pos.scale);
		int h = Math.round(module.height(client, editing) * pos.scale);
		int screenW = client.getWindow().getGuiScaledWidth();
		int screenH = client.getWindow().getGuiScaledHeight();
		int x = Math.round(pos.x * screenW);
		int y = Math.round(pos.y * screenH);
		x = Math.max(0, Math.min(x, screenW - w));
		y = Math.max(0, Math.min(y, screenH - h));
		return new int[]{x, y, w, h};
	}
}
