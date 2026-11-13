package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.config.HudPosition;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.hud.HudElement;
import io.github.frostmta.zadrinth.client.hud.HudManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;

/**
 * Editor de HUD: arraste os itens para mudar o lugar; clique com o botão
 * direito (ou segure e toque com outro dedo, no celular) para mudar o tamanho.
 */
public class HudEditorScreen extends Screen {
	private static final float[] SCALES = {0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};

	private final Screen parent;
	private HudElement dragging;
	private int dragOffsetX;
	private int dragOffsetY;

	public HudEditorScreen(Screen parent) {
		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		fill(0, 0, this.width, this.height, 0x55000000);
		//Linhas guia no meio da tela
		fill(this.width / 2, 0, this.width / 2 + 1, this.height, 0x22FFFFFF);
		fill(0, this.height / 2, this.width, this.height / 2 + 1, 0x22FFFFFF);

		Window window = new Window(this.client);
		for (HudElement element : HudManager.elements()) {
			if (!element.isEnabled()) continue;
			int[] box = HudManager.bounds(this.client, window, element, true);
			boolean active = element == dragging || Ui.inside(mouseX, mouseY, box[0], box[1], box[2], box[3]);
			Ui.box(box[0] - 1, box[1] - 1, box[2] + 2, box[3] + 2, active ? 0x339D7BFF : 0x18FFFFFF,
					active ? Ui.BRAND : 0x66FFFFFF);
			HudManager.draw(this.client, window, element, true);
		}

		Ui.centered(this.textRenderer, "Arraste para mover §8•§f Clique direito: tamanho §8•§f ESC: salvar",
				this.width / 2, 6, Ui.TEXT);
		super.render(mouseX, mouseY, delta);
	}

	private HudElement elementAt(int mouseX, int mouseY) {
		Window window = new Window(this.client);
		for (HudElement element : HudManager.elements()) {
			if (!element.isEnabled()) continue;
			int[] box = HudManager.bounds(this.client, window, element, true);
			if (Ui.inside(mouseX, mouseY, box[0] - 2, box[1] - 2, box[2] + 4, box[3] + 4)) return element;
		}
		return null;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		HudElement element = elementAt(mouseX, mouseY);
		if (element == null) return;
		if (button == 1) {
			HudPosition pos = element.position();
			int next = 0;
			for (int i = 0; i < SCALES.length; i++) {
				if (Math.abs(SCALES[i] - pos.scale) < 0.01f) next = (i + 1) % SCALES.length;
			}
			pos.scale = SCALES[next];
			ZadrinthConfig.save();
			return;
		}
		Window window = new Window(this.client);
		int[] box = HudManager.bounds(this.client, window, element, true);
		dragging = element;
		dragOffsetX = mouseX - box[0];
		dragOffsetY = mouseY - box[1];
	}

	@Override
	protected void mouseDragged(int mouseX, int mouseY, int button, long time) {
		if (dragging == null) return;
		Window window = new Window(this.client);
		int[] box = HudManager.bounds(this.client, window, dragging, true);
		int x = Math.max(0, Math.min(mouseX - dragOffsetX, window.getWidth() - box[2]));
		int y = Math.max(0, Math.min(mouseY - dragOffsetY, window.getHeight() - box[3]));
		//Gruda no centro da tela
		int centerX = (window.getWidth() - box[2]) / 2;
		if (Math.abs(x - centerX) < 4) x = centerX;
		HudPosition pos = dragging.position();
		pos.x = window.getWidth() == 0 ? 0f : (float) x / window.getWidth();
		pos.y = window.getHeight() == 0 ? 0f : (float) y / window.getHeight();
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		if (dragging != null) {
			dragging = null;
			ZadrinthConfig.save();
		}
	}

	@Override
	protected void keyPressed(char character, int code) {
		if (code == 1) { //ESC
			ZadrinthConfig.save();
			this.client.setScreen(parent);
			return;
		}
		try {
			super.keyPressed(character, code);
		} catch (Exception ignored) {
		}
	}

	@Override
	public boolean shouldPauseGame() {
		return false;
	}
}
