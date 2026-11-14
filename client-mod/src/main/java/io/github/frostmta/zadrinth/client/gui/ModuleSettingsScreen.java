package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.setting.ActionSetting;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ColorSetting;
import io.github.frostmta.zadrinth.client.module.setting.ModeSetting;
import io.github.frostmta.zadrinth.client.module.setting.NumberSetting;
import io.github.frostmta.zadrinth.client.module.setting.Setting;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

/** Opções de um módulo, montadas a partir das Settings dele. */
public class ModuleSettingsScreen extends Screen {
	private static final int SW = 10;

	private final Screen parent;
	private final Module module;

	private int px, py, pw, ph, listX, listY, listW, listH;
	private float scroll;
	private final List<Row> rows = new ArrayList<>();
	private NumberSetting activeSlider;
	private ColorSetting activeAlpha;
	private int sliderX, sliderW;
	private int pressY;
	private float scrollAtPress;
	private boolean pressInList, draggingList;

	private static final class Row {
		final Setting<?> setting;
		final int y;
		final int h;

		Row(Setting<?> setting, int y, int h) {
			this.setting = setting;
			this.y = y;
			this.h = h;
		}
	}

	public ModuleSettingsScreen(Screen parent, Module module) {
		this.parent = parent;
		this.module = module;
	}

	@Override
	public void init() {
		pw = Math.min(this.width - 20, 360);
		ph = Math.min(this.height - 16, 280);
		px = (this.width - pw) / 2;
		py = (this.height - ph) / 2;
		listX = px + 12;
		listW = pw - 24;
		listY = py + 44;
		listH = ph - 44 - 30;
	}

	private int rowHeight(Setting<?> setting) {
		if (setting instanceof NumberSetting) return 28;
		if (setting instanceof ColorSetting) {
			int perRow = Math.max(1, (listW - 40) / (SW + 3));
			int swatchRows = (ColorSetting.PALETTE.length + perRow - 1) / perRow;
			return 16 + swatchRows * (SW + 3) + (((ColorSetting) setting).allowAlpha ? 16 : 0) + 4;
		}
		if (setting instanceof ActionSetting) return 24;
		return 20;
	}

	private void layout() {
		rows.clear();
		int y = 0;
		for (Setting<?> setting : module.settings()) {
			if (!setting.isVisible()) continue;
			int h = rowHeight(setting);
			rows.add(new Row(setting, y, h));
			y += h + 4;
		}
	}

	private int contentHeight() {
		if (rows.isEmpty()) return 0;
		Row last = rows.get(rows.size() - 1);
		return last.y + last.h;
	}

	private void clampScroll() {
		scroll = Math.max(0, Math.min(Math.max(0, contentHeight() - listH), scroll));
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		layout();
		clampScroll();
		fill(0, 0, this.width, this.height, 0x88000000);
		Ui.card(px, py, pw, ph, 0xD20E0C16, 0x40FFFFFF);

		//Cabeçalho
		boolean backHover = Ui.inside(mouseX, mouseY, px + 6, py + 8, 20, 20);
		Ui.icon(Icon.BACK, px + 10, py + 12, 12, backHover ? Ui.BRAND_PINK : Ui.TEXT);
		Ui.icon(module.icon, px + 32, py + 10, 16, Ui.TEXT);
		this.textRenderer.drawWithShadow("§l" + module.name, px + 54, py + 10, Ui.TEXT);
		String description = this.textRenderer.trimToWidth(module.description, pw - 54 - 50);
		this.textRenderer.drawWithShadow("§7" + description, px + 54, py + 22, Ui.TEXT);
		toggle(px + pw - 38, py + 13, module.isEnabled());
		fill(px + 10, py + 38, px + pw - 10, py + 39, 0x30FFFFFF);

		if (rows.isEmpty()) {
			Ui.centered(this.textRenderer, "§7Este mod não tem opções", px + pw / 2, listY + 20, Ui.TEXT);
		}
		PlayerPreview.clip(listX - 2, listY, listW + 4, listH);
		for (Row row : rows) {
			int y = listY + row.y - Math.round(scroll);
			if (y + row.h < listY || y > listY + listH) continue;
			drawRow(row.setting, y, mouseX, mouseY);
		}
		PlayerPreview.endClip();

		//Rodapé
		int footY = py + ph - 24;
		int bw = module instanceof HudModule ? (pw - 30) / 2 : pw - 24;
		footerButton(px + 12, footY, bw, "RESETAR OPÇÕES", mouseX, mouseY, false);
		if (module instanceof HudModule) footerButton(px + 18 + bw, footY, bw, "EDITAR NO HUD", mouseX, mouseY, true);
	}

	private void footerButton(int x, int y, int w, String label, int mouseX, int mouseY, boolean primary) {
		boolean hover = Ui.inside(mouseX, mouseY, x, y, w, 16);
		if (primary) Ui.round(x, y, w, 16, hover ? 0xFFB49BFF : Ui.BRAND);
		else Ui.card(x, y, w, 16, hover ? 0x40FFFFFF : 0x18FFFFFF, 0x60FFFFFF);
		Ui.centered(this.textRenderer, label, x + w / 2, y + 4, Ui.TEXT);
	}

	private void toggle(int x, int y, boolean on) {
		Ui.round(x, y, 26, 12, on ? Ui.BRAND : 0x40FFFFFF);
		int knob = on ? x + 15 : x + 2;
		Ui.round(knob, y + 2, 9, 8, 0xFFFFFFFF);
	}

	private void drawRow(Setting<?> setting, int y, int mouseX, int mouseY) {
		Ui.round(listX, y, listW, rowHeight(setting), 0x14FFFFFF);
		int tx = listX + 7;
		if (setting instanceof BoolSetting) {
			this.textRenderer.drawWithShadow(setting.name, tx, y + 6, Ui.TEXT);
			toggle(listX + listW - 32, y + 4, ((BoolSetting) setting).on());
		} else if (setting instanceof NumberSetting) {
			NumberSetting number = (NumberSetting) setting;
			this.textRenderer.drawWithShadow(setting.name, tx, y + 5, Ui.TEXT);
			String value = number.display();
			this.textRenderer.drawWithShadow("§d" + value, listX + listW - 7 - this.textRenderer.getStringWidth(value), y + 5, Ui.TEXT);
			slider(tx, y + 18, listW - 14, number.fraction());
		} else if (setting instanceof ModeSetting) {
			ModeSetting mode = (ModeSetting) setting;
			this.textRenderer.drawWithShadow(setting.name, tx, y + 6, Ui.TEXT);
			String value = mode.current();
			int vw = this.textRenderer.getStringWidth(value);
			int right = listX + listW - 7;
			this.textRenderer.drawWithShadow("§d>", right - 6, y + 6, Ui.TEXT);
			this.textRenderer.drawWithShadow(value, right - 12 - vw, y + 6, Ui.TEXT);
			this.textRenderer.drawWithShadow("§d<", right - 20 - vw, y + 6, Ui.TEXT);
		} else if (setting instanceof ColorSetting) {
			ColorSetting color = (ColorSetting) setting;
			this.textRenderer.drawWithShadow(setting.name, tx, y + 5, Ui.TEXT);
			int preview = listX + listW - 20;
			fill(preview, y + 4, preview + 13, y + 12, 0xFF000000 | color.argb());
			Ui.box(preview - 1, y + 3, 15, 10, 0x00000000, 0x88FFFFFF);
			int perRow = Math.max(1, (listW - 40) / (SW + 3));
			for (int i = 0; i < ColorSetting.PALETTE.length; i++) {
				int sx = tx + (i % perRow) * (SW + 3), sy = y + 16 + (i / perRow) * (SW + 3);
				int c = ColorSetting.PALETTE[i];
				fill(sx, sy, sx + SW, sy + SW, 0xFF000000 | c);
				boolean current = !color.chroma() && (color.get() & 0xFFFFFF) == c;
				Ui.box(sx - 1, sy - 1, SW + 2, SW + 2, 0x00000000, current ? 0xFFFFFFFF : 0x30FFFFFF);
			}
			int rgbX = tx + perRow * (SW + 3) + 2;
			int rgbY = y + 16;
			Ui.card(rgbX, rgbY - 1, 26, SW + 2, color.chroma() ? 0x669D7BFF : 0x18FFFFFF, color.chroma() ? Ui.BRAND : 0x40FFFFFF);
			int hue = io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle.hsv((System.currentTimeMillis() % 3000L) / 3000f, 0.6f, 1f);
			Ui.centered(this.textRenderer, "RGB", rgbX + 13, rgbY + 1, 0xFF000000 | hue);
			if (color.allowAlpha) {
				int rows = (ColorSetting.PALETTE.length + perRow - 1) / perRow;
				int ay = y + 16 + rows * (SW + 3) + 2;
				this.textRenderer.drawWithShadow("§7Opacidade", tx, ay, Ui.TEXT);
				slider(tx + 56, ay + 3, listW - 14 - 56, color.alpha() / 255f);
			}
		} else if (setting instanceof ActionSetting) {
			boolean hover = Ui.inside(mouseX, mouseY, listX + 4, y + 4, listW - 8, 16);
			Ui.round(listX + 4, y + 4, listW - 8, 16, hover ? 0xFFB49BFF : Ui.BRAND);
			Ui.centered(this.textRenderer, setting.name, listX + listW / 2, y + 8, Ui.TEXT);
		}
	}

	private void slider(int x, int y, int w, float fraction) {
		fill(x, y, x + w, y + 3, 0x40FFFFFF);
		int filled = Math.round(w * Math.max(0, Math.min(1, fraction)));
		Ui.brandBar(x, y, Math.max(1, filled), 3);
		int knob = x + filled;
		Ui.round(knob - 3, y - 3, 7, 9, 0xFFFFFFFF);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if (button != 0) return;
		if (Ui.inside(mouseX, mouseY, px + 6, py + 8, 20, 20)) {
			close();
			return;
		}
		if (Ui.inside(mouseX, mouseY, px + pw - 42, py + 9, 34, 20)) {
			module.toggle();
			ZadrinthConfig.save();
			return;
		}
		int footY = py + ph - 24;
		int bw = module instanceof HudModule ? (pw - 30) / 2 : pw - 24;
		if (Ui.inside(mouseX, mouseY, px + 12, footY, bw, 16)) {
			module.resetSettings();
			ZadrinthConfig.save();
			return;
		}
		if (module instanceof HudModule && Ui.inside(mouseX, mouseY, px + 18 + bw, footY, bw, 16)) {
			if (!module.isEnabled()) module.setEnabled(true);
			ZadrinthConfig.save();
			this.client.setScreen(new HudEditorScreen(this, (HudModule) module));
			return;
		}
		pressY = mouseY;
		scrollAtPress = scroll;
		draggingList = false;
		pressInList = Ui.inside(mouseX, mouseY, listX, listY, listW, listH);
		if (!pressInList) return;
		for (Row row : rows) {
			int y = listY + row.y - Math.round(scroll);
			if (!Ui.inside(mouseX, mouseY, listX, y, listW, row.h)) continue;
			if (clickRow(row.setting, y, mouseX, mouseY)) {
				pressInList = false;
				ZadrinthConfig.save();
			}
			return;
		}
	}

	/** true = o toque foi usado pela opção (não rola a lista). */
	private boolean clickRow(Setting<?> setting, int y, int mouseX, int mouseY) {
		int tx = listX + 7;
		if (setting instanceof BoolSetting) {
			((BoolSetting) setting).toggle();
			return true;
		}
		if (setting instanceof NumberSetting) {
			if (mouseY >= y + 12) {
				activeSlider = (NumberSetting) setting;
				sliderX = tx;
				sliderW = listW - 14;
				activeSlider.setFraction((mouseX - sliderX) / (float) sliderW);
				return true;
			}
			return false;
		}
		if (setting instanceof ModeSetting) {
			((ModeSetting) setting).cycle(mouseX < listX + listW / 2 ? -1 : 1);
			return true;
		}
		if (setting instanceof ColorSetting) {
			ColorSetting color = (ColorSetting) setting;
			int perRow = Math.max(1, (listW - 40) / (SW + 3));
			for (int i = 0; i < ColorSetting.PALETTE.length; i++) {
				int sx = tx + (i % perRow) * (SW + 3), sy = y + 16 + (i / perRow) * (SW + 3);
				if (Ui.inside(mouseX, mouseY, sx - 1, sy - 1, SW + 2, SW + 2)) {
					color.setRgb(ColorSetting.PALETTE[i]);
					return true;
				}
			}
			int rgbX = tx + perRow * (SW + 3) + 2;
			if (Ui.inside(mouseX, mouseY, rgbX, y + 15, 26, SW + 2)) {
				color.setChroma(!color.chroma());
				return true;
			}
			if (color.allowAlpha) {
				int rows = (ColorSetting.PALETTE.length + perRow - 1) / perRow;
				int ay = y + 16 + rows * (SW + 3) + 2;
				if (mouseY >= ay - 3) {
					activeAlpha = color;
					sliderX = tx + 56;
					sliderW = listW - 14 - 56;
					color.setAlpha(Math.round(255 * Math.max(0, Math.min(1, (mouseX - sliderX) / (float) sliderW))));
					return true;
				}
			}
			return false;
		}
		if (setting instanceof ActionSetting) {
			ZadrinthConfig.save();
			((ActionSetting) setting).run();
			return true;
		}
		return false;
	}

	@Override
	protected void mouseDragged(int mouseX, int mouseY, int button, long time) {
		if (activeSlider != null) {
			activeSlider.setFraction((mouseX - sliderX) / (float) sliderW);
			return;
		}
		if (activeAlpha != null) {
			activeAlpha.setAlpha(Math.round(255 * Math.max(0, Math.min(1, (mouseX - sliderX) / (float) sliderW))));
			return;
		}
		if (!pressInList) return;
		if (!draggingList && Math.abs(mouseY - pressY) > 3) draggingList = true;
		if (draggingList) {
			scroll = scrollAtPress - (mouseY - pressY);
			clampScroll();
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		if (activeSlider != null || activeAlpha != null) ZadrinthConfig.save();
		activeSlider = null;
		activeAlpha = null;
		pressInList = false;
		draggingList = false;
	}

	@Override
	public void handleMouse() {
		try {
			super.handleMouse();
		} catch (Exception ignored) {
		}
		int wheel = Mouse.getEventDWheel();
		if (wheel != 0) {
			scroll -= Math.signum(wheel) * 24;
			clampScroll();
		}
	}

	@Override
	protected void keyPressed(char character, int code) {
		if (code == 1 || code == ZadrinthKeys.MENU.getCode()) close();
	}

	private void close() {
		ZadrinthConfig.save();
		this.client.setScreen(parent);
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
