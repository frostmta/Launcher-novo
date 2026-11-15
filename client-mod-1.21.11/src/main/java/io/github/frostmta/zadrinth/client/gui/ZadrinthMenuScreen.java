package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.ModuleManager;
import io.github.frostmta.zadrinth.client.presence.Presence;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Menu de mods (Right Shift, ou ESC → Zadrinth Client), no estilo dos clients
 * de PvP: cartões com ícone e nome; toque liga/desliga, a engrenagem abre as
 * opções. Na barra da esquerda: fechar, mods, editar HUD, cosméticos,
 * configurações do client e informações.
 */
public class ZadrinthMenuScreen extends ZScreen {
	private static final int RAIL = 26;
	private static final int CARD_H = 26;
	private static final int GAP = 7;

	private Category category = Category.ALL;
	private EditBox search;
	private List<Module> visible = new ArrayList<>();
	private String lastQuery = "";
	private boolean showInfo;

	private int px, py, pw, ph;
	private int contentX, contentW, gridY, gridH, cols, cardW;
	private int searchX, searchY, searchW;
	private int chipsY;

	private float scroll;
	private int pressX, pressY;
	private float scrollAtPress;
	private boolean pressInGrid, draggingGrid;

	public ZadrinthMenuScreen() {
		this(null);
	}

	public ZadrinthMenuScreen(Screen parent) {
		super(parent);
	}

	@Override
	public void init() {
		pw = Math.min(this.width - 20, 470);
		ph = Math.min(this.height - 16, 280);
		px = (this.width - pw) / 2;
		py = (this.height - ph) / 2;
		contentX = px + RAIL + 10;
		contentW = pw - RAIL - 20;
		searchW = Math.min(110, contentW / 3);
		searchX = contentX + contentW - searchW;
		searchY = py + 8;
		int chipsWidth = 0;
		for (Category value : Category.values()) chipsWidth += Ui.width(value.label) + 14 + 3;
		int titleEnd = contentX + 18 + Ui.width("§lZADRINTH CLIENT") + 10;
		chipsY = titleEnd + chipsWidth < searchX - 6 ? -1 : searchY + 20;
		gridY = (chipsY < 0 ? searchY + 22 : chipsY + 20);
		gridH = py + ph - 8 - gridY;
		cols = contentW >= 330 ? 3 : 2;
		cardW = (contentW - GAP * (cols - 1)) / cols;

		String query = search == null ? "" : search.getValue();
		search = textField(searchX + 14, searchY + 4, searchW - 18, 10, query);
		refresh();
	}

	private void refresh() {
		String query = normalize(search.getValue());
		List<Module> list = new ArrayList<>();
		for (Module module : ModuleManager.all()) {
			if (category != Category.ALL && module.category != category
					&& !(category == Category.HUD && module instanceof io.github.frostmta.zadrinth.client.module.hud.HudModule)) continue;
			if (!query.isEmpty() && !normalize(module.name + " " + module.description).contains(query)) continue;
			list.add(module);
		}
		visible = list;
		lastQuery = search.getValue();
		clampScroll();
	}

	private static String normalize(String text) {
		return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
	}

	private int contentHeight() {
		int rows = (visible.size() + cols - 1) / cols;
		return rows * (CARD_H + GAP) - GAP;
	}

	private void clampScroll() {
		float max = Math.max(0, contentHeight() - gridH);
		scroll = Math.max(0, Math.min(max, scroll));
	}

	// ------------------------------------------------------------------ desenho
	@Override
	protected void draw(int mouseX, int mouseY, float delta) {
		if (!search.getValue().equals(lastQuery)) refresh();
		Ui.fill(0, 0, this.width, this.height, 0x88000000);
		Ui.card(px, py, pw, ph, 0xD20E0C16, 0x40FFFFFF);
		drawRail(mouseX, mouseY);

		//Título
		Ui.logo(contentX, py + 7, 16);
		Ui.text("§lZADRINTH CLIENT", contentX + 20, py + 11, Ui.TEXT);
		int chipX = chipsY < 0 ? contentX + 20 + Ui.width("§lZADRINTH CLIENT") + 12 : contentX;
		int chipY = chipsY < 0 ? searchY : chipsY;
		for (Category value : Category.values()) {
			int w = Ui.width(value.label) + 14;
			boolean active = value == category;
			boolean hover = Ui.inside(mouseX, mouseY, chipX, chipY, w, 16);
			Ui.card(chipX, chipY, w, 16, active ? 0x559D7BFF : hover ? 0x22FFFFFF : 0x10FFFFFF, active ? Ui.BRAND : 0x30FFFFFF);
			Ui.centered((active ? "§f" : "§7") + value.label, chipX + w / 2, chipY + 4, Ui.TEXT);
			chipX += w + 3;
		}

		//Busca
		Ui.card(searchX, searchY, searchW, 16, search.isFocused() ? 0x30FFFFFF : 0x18FFFFFF, search.isFocused() ? Ui.BRAND : 0x40FFFFFF);
		Ui.icon(Icon.ZOOM, searchX + 4, searchY + 4, 8, 0xFFBBBBBB);
		if (search.getValue().isEmpty() && !search.isFocused()) {
			Ui.text("§7Buscar...", searchX + 15, searchY + 4, Ui.TEXT);
		}

		drawGrid(mouseX, mouseY);
		if (showInfo) drawInfo();
	}

	private void drawRail(int mouseX, int mouseY) {
		int x = px + 4, w = RAIL - 6;
		Ui.round(x, py + 4, w, ph - 8, 0x20FFFFFF);
		railIcon(Icon.CLOSE, x, py + 8, mouseX, mouseY, false);
		railIcon(Icon.HOME, x, py + 30, mouseX, mouseY, !showInfo);
		int bottom = py + ph - 8 - 22 * 4;
		railIcon(Icon.PENCIL, x, bottom, mouseX, mouseY, false);
		railIcon(Icon.SHIRT, x, bottom + 22, mouseX, mouseY, false);
		railIcon(Icon.GEAR, x, bottom + 44, mouseX, mouseY, category == Category.CLIENT);
		railIcon(Icon.INFO, x, bottom + 66, mouseX, mouseY, showInfo);
	}

	private void railIcon(Icon icon, int x, int y, int mouseX, int mouseY, boolean active) {
		boolean hover = Ui.inside(mouseX, mouseY, x, y - 3, RAIL - 6, 18);
		if (active) Ui.round(x + 2, y - 3, RAIL - 10, 18, 0x409D7BFF);
		Ui.icon(icon, x + (RAIL - 6 - 12) / 2, y, 12, hover || active ? 0xFFFFFFFF : 0xFF9A9AA6);
	}

	private void drawGrid(int mouseX, int mouseY) {
		if (visible.isEmpty()) {
			Ui.centered("§7Nenhum mod encontrado", contentX + contentW / 2, gridY + gridH / 2 - 4, Ui.TEXT);
			return;
		}
		Ui.clip(contentX - 2, gridY, contentW + 4, gridH);
		for (int i = 0; i < visible.size(); i++) {
			int x = contentX + (i % cols) * (cardW + GAP);
			int y = gridY + (i / cols) * (CARD_H + GAP) - Math.round(scroll);
			if (y + CARD_H < gridY || y > gridY + gridH) continue;
			drawCard(visible.get(i), x, y, mouseX, mouseY);
		}
		Ui.endClip();
		float max = contentHeight();
		if (max > gridH) {
			int barH = Math.max(14, Math.round(gridH * gridH / max));
			int barY = gridY + Math.round((gridH - barH) * (scroll / (max - gridH)));
			Ui.fill(px + pw - 5, barY, px + pw - 3, barY + barH, 0x88FFFFFF);
		}
	}

	private void drawCard(Module module, int x, int y, int mouseX, int mouseY) {
		boolean on = module.isEnabled();
		boolean inGrid = Ui.inside(mouseX, mouseY, contentX, gridY, contentW, gridH);
		boolean hover = inGrid && !draggingGrid && Ui.inside(mouseX, mouseY, x, y, cardW, CARD_H);
		int fillColor = hover ? 0x38FFFFFF : on ? 0x26FFFFFF : 0x30000000;
		Ui.card(x, y, cardW, CARD_H, fillColor, on ? 0xF0FFFFFF : 0x50FFFFFF);
		int text = on ? 0xFFFFFFFF : 0xFF8C8C96;
		Ui.icon(module.icon, x + 7, y + 6, 14, text);
		Ui.fill(x + 27, y + 6, x + 28, y + CARD_H - 6, on ? 0xAAFFFFFF : 0x40FFFFFF);
		String name = Ui.trim(module.name, cardW - 46);
		Ui.text(name, x + 33, y + 9, text);
		if (!module.settings().isEmpty() || module instanceof io.github.frostmta.zadrinth.client.module.hud.HudModule) {
			boolean gearHover = inGrid && Ui.inside(mouseX, mouseY, x + cardW - 15, y + CARD_H - 15, 15, 15);
			Ui.icon(Icon.GEAR, x + cardW - 11, y + CARD_H - 11, 8, gearHover ? Ui.BRAND_PINK : (on ? 0xCCFFFFFF : 0x80FFFFFF));
		}
	}

	private void drawInfo() {
		int w = Math.min(contentW, 260), h = 92;
		int x = contentX + (contentW - w) / 2, y = gridY + 6;
		Ui.card(x, y, w, h, 0xF0121019, Ui.BRAND);
		Ui.logo(x + 8, y + 8, 28);
		Ui.text("§lZadrinth Client", x + 42, y + 11, Ui.TEXT);
		Ui.text("§7v" + ZadrinthClient.version() + " • Minecraft " + ZadrinthClient.MINECRAFT, x + 42, y + 22, Ui.TEXT);
		String sync = Presence.isConfigured() ? "§a● §7Sincronização ligada" : "§c● §7Sem servidor de sincronização";
		Ui.text(sync, x + 8, y + 44, Ui.TEXT);
		Ui.text("§7Abrir o menu: Right Shift, ou ESC → Zadrinth", x + 8, y + 56, Ui.TEXT);
		Ui.text("§7Zoom: C  •  Freelook: Alt  •  Editar HUD: lápis", x + 8, y + 68, Ui.TEXT);
	}

	// ------------------------------------------------------------------ entrada
	@Override
	protected void onMouseClicked(int mouseX, int mouseY, int button) {
		if (button != 0) return;
		if (showInfo) {
			showInfo = false;
			return;
		}
		pressX = mouseX;
		pressY = mouseY;
		scrollAtPress = scroll;
		draggingGrid = false;
		pressInGrid = Ui.inside(mouseX, mouseY, contentX, gridY, contentW, gridH);
		if (pressInGrid) return;

		int x = px + 4;
		if (Ui.inside(mouseX, mouseY, x, py + 5, RAIL - 6, 18)) {
			onClose();
			return;
		}
		if (Ui.inside(mouseX, mouseY, x, py + 27, RAIL - 6, 18)) {
			category = Category.ALL;
			refresh();
			return;
		}
		int bottom = py + ph - 8 - 22 * 4;
		if (Ui.inside(mouseX, mouseY, x, bottom - 3, RAIL - 6, 18)) {
			ZadrinthConfig.save();
			this.minecraft.setScreen(new HudEditorScreen(this));
			return;
		}
		if (Ui.inside(mouseX, mouseY, x, bottom + 19, RAIL - 6, 18)) {
			ZadrinthConfig.save();
			this.minecraft.setScreen(new ShopScreen(this));
			return;
		}
		if (Ui.inside(mouseX, mouseY, x, bottom + 41, RAIL - 6, 18)) {
			category = category == Category.CLIENT ? Category.ALL : Category.CLIENT;
			scroll = 0;
			refresh();
			return;
		}
		if (Ui.inside(mouseX, mouseY, x, bottom + 63, RAIL - 6, 18)) {
			showInfo = true;
			return;
		}
		int chipX = chipsY < 0 ? contentX + 20 + Ui.width("§lZADRINTH CLIENT") + 12 : contentX;
		int chipY = chipsY < 0 ? searchY : chipsY;
		for (Category value : Category.values()) {
			int w = Ui.width(value.label) + 14;
			if (Ui.inside(mouseX, mouseY, chipX, chipY, w, 16)) {
				category = value;
				scroll = 0;
				refresh();
				return;
			}
			chipX += w + 3;
		}
	}

	@Override
	protected void onMouseDragged(int mouseX, int mouseY, int button) {
		if (!pressInGrid) return;
		if (!draggingGrid && Math.abs(mouseY - pressY) > 3) draggingGrid = true;
		if (draggingGrid) {
			scroll = scrollAtPress - (mouseY - pressY);
			clampScroll();
		}
	}

	@Override
	protected void onMouseReleased(int mouseX, int mouseY, int button) {
		if (pressInGrid && !draggingGrid && button == 0) {
			for (int i = 0; i < visible.size(); i++) {
				int x = contentX + (i % cols) * (cardW + GAP);
				int y = gridY + (i / cols) * (CARD_H + GAP) - Math.round(scroll);
				if (!Ui.inside(mouseX, mouseY, x, y, cardW, CARD_H)) continue;
				Module module = visible.get(i);
				boolean hasOptions = !module.settings().isEmpty() || module instanceof io.github.frostmta.zadrinth.client.module.hud.HudModule;
				if (hasOptions && Ui.inside(mouseX, mouseY, x + cardW - 15, y + CARD_H - 15, 15, 15)) {
					ZadrinthConfig.save();
					this.minecraft.setScreen(new ModuleSettingsScreen(this, module));
				} else {
					module.toggle();
					ZadrinthConfig.save();
				}
				break;
			}
		}
		pressInGrid = false;
		draggingGrid = false;
	}

	@Override
	protected void onScroll(int mouseX, int mouseY, int direction) {
		scroll -= direction * (CARD_H + GAP);
		clampScroll();
	}
}
