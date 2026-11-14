package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import io.github.frostmta.zadrinth.client.config.HudPosition;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.hud.HudManager;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.ModuleManager;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;

import java.util.List;

/**
 * Editor de HUD: arraste qualquer item para mover; arraste o quadradinho do
 * canto para aumentar/diminuir. Linhas guia aparecem no meio e nas bordas.
 * Toque num item para escolher: aparecem os botões de opções e de resetar.
 */
public class HudEditorScreen extends Screen {
	private static final int HANDLE = 7;
	private static final int SNAP = 4;
	private static final float MIN_SCALE = 0.5f;
	private static final float MAX_SCALE = 3.0f;

	private final Screen parent;
	private HudModule selected;
	private HudModule dragging;
	private boolean resizing;
	private int grabX, grabY;
	private int startW;
	private float startScale;
	private int pressX, pressY;
	private boolean moved;
	private boolean guideX, guideY;

	public HudEditorScreen(Screen parent) {
		this(parent, null);
	}

	public HudEditorScreen(Screen parent, HudModule focus) {
		this.parent = parent;
		this.selected = focus;
	}

	private List<HudModule> visible() {
		List<HudModule> list = ModuleManager.hudModules();
		list.removeIf(module -> !module.isEnabled());
		return list;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		fill(0, 0, this.width, this.height, 0x55000000);
		//Grade leve e linhas do centro
		for (int x = 0; x < this.width; x += 20) fill(x, 0, x + 1, this.height, 0x0CFFFFFF);
		for (int y = 0; y < this.height; y += 20) fill(0, y, this.width, y + 1, 0x0CFFFFFF);
		if (guideX) fill(this.width / 2, 0, this.width / 2 + 1, this.height, 0xAA9D7BFF);
		if (guideY) fill(0, this.height / 2, this.width, this.height / 2 + 1, 0xAA9D7BFF);

		Window window = new Window(this.client);
		HudModule hovered = null;
		for (HudModule module : visible()) {
			int[] b = HudManager.bounds(this.client, window, module, true);
			boolean active = module == selected || module == dragging;
			fill(b[0], b[1], b[0] + b[2], b[1] + b[3], active ? 0x309D7BFF : 0x18FFFFFF);
			HudManager.draw(this.client, window, module, true);
			Ui.box(b[0] - 1, b[1] - 1, b[2] + 2, b[3] + 2, 0x00000000, active ? Ui.BRAND : 0x66FFFFFF);
			//Alça de redimensionar
			int hx = b[0] + b[2] - HANDLE + 2, hy = b[1] + b[3] - HANDLE + 2;
			fill(hx, hy, hx + HANDLE, hy + HANDLE, active ? Ui.BRAND_PINK : 0xCCFFFFFF);
			fill(hx + 2, hy + 2, hx + HANDLE - 2, hy + HANDLE - 2, 0xFF121019);
			if (Ui.inside(mouseX, mouseY, b[0], b[1], b[2], b[3])) hovered = module;
		}

		//Barra do módulo escolhido: nome, escala, opções, resetar
		if (selected != null && selected.isEnabled()) {
			int[] b = HudManager.bounds(this.client, window, selected, true);
			String label = selected.name + " §7" + Math.round(selected.position().scale * 100) + "%";
			int barW = this.textRenderer.getStringWidth(label) + 8 + 34;
			int barX = Math.max(2, Math.min(this.width - barW - 2, b[0]));
			int barY = b[1] - 18 >= 2 ? b[1] - 18 : b[1] + b[3] + 4;
			Ui.card(barX, barY, barW, 15, 0xEE121019, Ui.BRAND);
			this.textRenderer.drawWithShadow(label, barX + 5, barY + 4, Ui.TEXT);
			int ix = barX + barW - 32;
			Ui.icon(Icon.GEAR, ix, barY + 3, 9, Ui.inside(mouseX, mouseY, ix - 2, barY, 14, 15) ? Ui.BRAND_PINK : Ui.TEXT);
			Ui.icon(Icon.RESET, ix + 16, barY + 3, 9, Ui.inside(mouseX, mouseY, ix + 14, barY, 14, 15) ? Ui.BRAND_PINK : Ui.TEXT);
		}

		//Rodapé
		String hint = "Arraste para mover  •  Canto para redimensionar  •  ESC salva";
		Ui.centered(this.textRenderer, "§7" + hint, this.width / 2, this.height - 34, Ui.TEXT);
		int bw = 70, gap = 6;
		int bx = this.width / 2 - bw - gap / 2, by = this.height - 22;
		button(bx, by, bw, "MODS", mouseX, mouseY, false);
		button(bx + bw + gap, by, bw, "CONCLUIR", mouseX, mouseY, true);
		if (hovered != null && dragging == null && hovered != selected) {
			this.textRenderer.drawWithShadow(hovered.name, mouseX + 8, mouseY - 4, Ui.TEXT);
		}
	}

	private void button(int x, int y, int w, String label, int mouseX, int mouseY, boolean primary) {
		boolean hover = Ui.inside(mouseX, mouseY, x, y, w, 16);
		if (primary) {
			Ui.round(x, y, w, 16, hover ? 0xFFB49BFF : Ui.BRAND);
		} else {
			Ui.card(x, y, w, 16, hover ? 0x40FFFFFF : 0x22FFFFFF, 0x88FFFFFF);
		}
		Ui.centered(this.textRenderer, label, x + w / 2, y + 4, Ui.TEXT);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		int bw = 70, gap = 6;
		int bx = this.width / 2 - bw - gap / 2, by = this.height - 22;
		if (Ui.inside(mouseX, mouseY, bx, by, bw, 16)) {
			close(new ZadrinthMenuScreen());
			return;
		}
		if (Ui.inside(mouseX, mouseY, bx + bw + gap, by, bw, 16)) {
			close(parent);
			return;
		}
		Window window = new Window(this.client);
		if (selected != null && selected.isEnabled()) {
			int[] b = HudManager.bounds(this.client, window, selected, true);
			String label = selected.name + " §7" + Math.round(selected.position().scale * 100) + "%";
			int barW = this.textRenderer.getStringWidth(label) + 8 + 34;
			int barX = Math.max(2, Math.min(this.width - barW - 2, b[0]));
			int barY = b[1] - 18 >= 2 ? b[1] - 18 : b[1] + b[3] + 4;
			int ix = barX + barW - 32;
			if (Ui.inside(mouseX, mouseY, ix - 2, barY, 14, 15)) {
				ZadrinthConfig.save();
				this.client.setScreen(new ModuleSettingsScreen(this, selected));
				return;
			}
			if (Ui.inside(mouseX, mouseY, ix + 14, barY, 14, 15)) {
				selected.resetPosition();
				return;
			}
		}
		if (button != 0 && button != 1) return;
		List<HudModule> modules = visible();
		//De cima para baixo (o último desenhado fica por cima)
		for (int i = modules.size() - 1; i >= 0; i--) {
			HudModule module = modules.get(i);
			int[] b = HudManager.bounds(this.client, window, module, true);
			int hx = b[0] + b[2] - HANDLE + 2, hy = b[1] + b[3] - HANDLE + 2;
			boolean onHandle = Ui.inside(mouseX, mouseY, hx - 3, hy - 3, HANDLE + 6, HANDLE + 6);
			if (onHandle || Ui.inside(mouseX, mouseY, b[0], b[1], b[2], b[3])) {
				dragging = module;
				selected = module;
				resizing = onHandle;
				grabX = mouseX - b[0];
				grabY = mouseY - b[1];
				startW = Math.max(1, b[2]);
				startScale = module.position().scale;
				pressX = mouseX;
				pressY = mouseY;
				moved = false;
				return;
			}
		}
		selected = null;
	}

	@Override
	protected void mouseDragged(int mouseX, int mouseY, int button, long time) {
		if (dragging == null) return;
		if (Math.abs(mouseX - pressX) + Math.abs(mouseY - pressY) > 1) moved = true;
		Window window = new Window(this.client);
		HudPosition pos = dragging.position();
		if (resizing) {
			float scale = startScale * (startW + (mouseX - pressX)) / (float) startW;
			pos.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.round(scale * 20f) / 20f));
			return;
		}
		int[] b = HudManager.bounds(this.client, window, dragging, true);
		int w = b[2], h = b[3];
		int screenW = window.getWidth(), screenH = window.getHeight();
		int x = Math.max(0, Math.min(mouseX - grabX, screenW - w));
		int y = Math.max(0, Math.min(mouseY - grabY, screenH - h));
		//Encaixa no meio e nas bordas
		guideX = Math.abs(x + w / 2 - screenW / 2) <= SNAP;
		guideY = Math.abs(y + h / 2 - screenH / 2) <= SNAP;
		if (guideX) x = screenW / 2 - w / 2;
		if (guideY) y = screenH / 2 - h / 2;
		if (x <= SNAP) x = 0;
		if (y <= SNAP) y = 0;
		if (x + w >= screenW - SNAP) x = screenW - w;
		if (y + h >= screenH - SNAP) y = screenH - h;
		pos.x = screenW <= 0 ? 0f : (float) x / screenW;
		pos.y = screenH <= 0 ? 0f : (float) y / screenH;
		//Encostado na direita/embaixo: guarda 1.0 para continuar encostado em outras telas
		if (x + w >= screenW) pos.x = 1.0f;
		if (y + h >= screenH) pos.y = 1.0f;
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		if (dragging != null && !moved && button == 1) {
			//Botão direito sem arrastar: alterna o tamanho (100% → 75% → 50% → 150%...)
			float[] steps = {1.0f, 0.75f, 0.5f, 1.5f, 2.0f};
			float scale = dragging.position().scale;
			int index = 0;
			for (int i = 0; i < steps.length; i++) if (Math.abs(steps[i] - scale) < 0.01f) index = i;
			dragging.position().scale = steps[(index + 1) % steps.length];
		}
		dragging = null;
		resizing = false;
		guideX = false;
		guideY = false;
	}

	@Override
	protected void keyPressed(char character, int code) {
		if (code == 1 || code == ZadrinthKeys.MENU.getCode()) close(parent);
	}

	private void close(Screen next) {
		ZadrinthConfig.save();
		this.client.setScreen(next);
	}

	@Override
	public void removed() {
		ZadrinthConfig.save();
	}

	@Override
	public boolean shouldPauseGame() {
		return false;
	}
}
