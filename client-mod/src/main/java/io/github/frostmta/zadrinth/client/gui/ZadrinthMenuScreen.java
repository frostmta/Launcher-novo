package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.presence.Presence;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu do Zadrinth Client (Right Shift): cartões de mods com ATIVADO/DESATIVADO
 * e o botão para editar o HUD.
 */
public class ZadrinthMenuScreen extends Screen {
	private static final int CARD_W = 118;
	private static final int CARD_H = 62;
	private static final int GAP = 8;

	private interface Toggle {
		boolean get(ZadrinthConfig config);

		void set(ZadrinthConfig config, boolean value);
	}

	private static final class ModCard {
		final String name;
		final String description;
		final Toggle toggle;

		ModCard(String name, String description, Toggle toggle) {
			this.name = name;
			this.description = description;
			this.toggle = toggle;
		}
	}

	private final List<ModCard> cards = new ArrayList<>();
	private int panelX, panelY, panelW, panelH;

	public ZadrinthMenuScreen() {
		cards.add(new ModCard("ToggleSprint", "Corrida contínua", new Toggle() {
			public boolean get(ZadrinthConfig c) { return c.toggleSprintEnabled; }
			public void set(ZadrinthConfig c, boolean v) { c.toggleSprintEnabled = v; if (!v) c.sprintToggled = false; }
		}));
		cards.add(new ModCard("ToggleSneak", "Agachar contínuo", new Toggle() {
			public boolean get(ZadrinthConfig c) { return c.toggleSneakEnabled; }
			public void set(ZadrinthConfig c, boolean v) { c.toggleSneakEnabled = v; if (!v) c.sneakToggled = false; }
		}));
		cards.add(new ModCard("Status na tela", "[Sprinting (Toggled)]", new Toggle() {
			public boolean get(ZadrinthConfig c) { return c.toggleSprintHud; }
			public void set(ZadrinthConfig c, boolean v) { c.toggleSprintHud = v; }
		}));
		cards.add(new ModCard("Ícone no TAB", "Ver quem usa o client", new Toggle() {
			public boolean get(ZadrinthConfig c) { return c.tabIconEnabled; }
			public void set(ZadrinthConfig c, boolean v) { c.tabIconEnabled = v; }
		}));
		cards.add(new ModCard("Aparecer no TAB", "Outros te veem com o ícone", new Toggle() {
			public boolean get(ZadrinthConfig c) { return c.sharePresence; }
			public void set(ZadrinthConfig c, boolean v) { c.sharePresence = v; }
		}));
	}

	@Override
	public void init() {
		int columns = Math.max(1, Math.min(4, (this.width - 40) / (CARD_W + GAP)));
		int rows = (cards.size() + columns - 1) / columns;
		panelW = columns * CARD_W + (columns - 1) * GAP + 24;
		panelH = 40 + rows * CARD_H + (rows - 1) * GAP + 52;
		panelX = (this.width - panelW) / 2;
		panelY = Math.max(6, (this.height - panelH) / 2);
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		fill(0, 0, this.width, this.height, Ui.OVERLAY);
		Ui.box(panelX, panelY, panelW, panelH, Ui.PANEL, Ui.BORDER);
		Ui.brandBar(panelX + 1, panelY + 1, panelW - 2, 2);

		//Cabeçalho: marca + aba MODS
		this.textRenderer.drawWithShadow("§lZADRINTH§r CLIENT", panelX + 12, panelY + 12, Ui.TEXT);
		String tab = Ui.spaced("MODS");
		int tabW = this.textRenderer.getStringWidth(tab) + 16;
		int tabX = panelX + panelW - tabW - 12;
		Ui.box(tabX, panelY + 8, tabW, 16, 0x339D7BFF, 0x669D7BFF);
		Ui.centered(this.textRenderer, tab, tabX + tabW / 2, panelY + 12, Ui.TEXT);

		//Cartões
		int columns = Math.max(1, (panelW - 24 + GAP) / (CARD_W + GAP));
		ZadrinthConfig config = ZadrinthConfig.get();
		for (int i = 0; i < cards.size(); i++) {
			ModCard card = cards.get(i);
			int cx = panelX + 12 + (i % columns) * (CARD_W + GAP);
			int cy = panelY + 34 + (i / columns) * (CARD_H + GAP);
			boolean hover = Ui.inside(mouseX, mouseY, cx, cy, CARD_W, CARD_H);
			Ui.box(cx, cy, CARD_W, CARD_H, hover ? Ui.CARD_HOVER : Ui.CARD, Ui.BORDER);
			Ui.centered(this.textRenderer, card.name, cx + CARD_W / 2, cy + 8, Ui.TEXT);
			Ui.centered(this.textRenderer, trim(card.description, CARD_W - 8), cx + CARD_W / 2, cy + 22, Ui.TEXT_MUTED);
			boolean on = card.toggle.get(config);
			int buttonY = cy + CARD_H - 18;
			fill(cx + 4, buttonY, cx + CARD_W - 4, buttonY + 14, on ? Ui.ENABLED : Ui.DISABLED);
			Ui.centered(this.textRenderer, on ? Ui.spaced("ATIVADO") : Ui.spaced("DESATIVADO"),
					cx + CARD_W / 2, buttonY + 3, Ui.TEXT);
		}

		//Rodapé: editar HUD + status da sincronização
		int footerY = panelY + panelH - 26;
		int editW = this.textRenderer.getStringWidth(Ui.spaced("EDITAR HUD")) + 20;
		boolean editHover = Ui.inside(mouseX, mouseY, panelX + 12, footerY, editW, 18);
		Ui.brandBar(panelX + 12, footerY, editW, 18);
		if (editHover) fill(panelX + 12, footerY, panelX + 12 + editW, footerY + 18, 0x22FFFFFF);
		Ui.centered(this.textRenderer, Ui.spaced("EDITAR HUD"), panelX + 12 + editW / 2, footerY + 5, Ui.TEXT);

		int shopX = panelX + 12 + editW + 6;
		int shopW = shopWidth();
		boolean shopHover = Ui.inside(mouseX, mouseY, shopX, footerY, shopW, 18);
		Ui.box(shopX, footerY, shopW, 18, shopHover ? 0x40E07BFF : 0x22E07BFF, 0x88E07BFF);
		Ui.centered(this.textRenderer, Ui.spaced("COSMÉTICOS"), shopX + shopW / 2, footerY + 5, Ui.TEXT);

		String sync = Presence.isConfigured() ? "§a●§7 Sincronização ligada" : "§c●§7 Sem servidor de sincronização";
		String info = sync + "  §8v" + ZadrinthClient.version();
		int infoX = panelX + panelW - 12 - this.textRenderer.getStringWidth(info);
		if (infoX < shopX + shopW + 6) {
			//Tela estreita: mostra a sincronização acima dos botões
			infoX = panelX + panelW - 12 - this.textRenderer.getStringWidth(info);
			this.textRenderer.drawWithShadow(info, infoX, footerY - 11, Ui.TEXT_MUTED);
		} else {
			this.textRenderer.drawWithShadow(info, infoX, footerY + 5, Ui.TEXT_MUTED);
		}

		super.render(mouseX, mouseY, delta);
	}

	private String trim(String text, int maxWidth) {
		if (this.textRenderer.getStringWidth(text) <= maxWidth) return text;
		String value = text;
		while (value.length() > 1 && this.textRenderer.getStringWidth(value + "…") > maxWidth) {
			value = value.substring(0, value.length() - 1);
		}
		return value + "…";
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if (button != 0) return;
		int columns = Math.max(1, (panelW - 24 + GAP) / (CARD_W + GAP));
		ZadrinthConfig config = ZadrinthConfig.get();
		for (int i = 0; i < cards.size(); i++) {
			int cx = panelX + 12 + (i % columns) * (CARD_W + GAP);
			int cy = panelY + 34 + (i / columns) * (CARD_H + GAP);
			if (Ui.inside(mouseX, mouseY, cx, cy, CARD_W, CARD_H)) {
				ModCard card = cards.get(i);
				card.toggle.set(config, !card.toggle.get(config));
				ZadrinthConfig.save();
				return;
			}
		}
		int footerY = panelY + panelH - 26;
		int editW = this.textRenderer.getStringWidth(Ui.spaced("EDITAR HUD")) + 20;
		if (Ui.inside(mouseX, mouseY, panelX + 12, footerY, editW, 18)) {
			this.client.setScreen(new HudEditorScreen(this));
			return;
		}
		if (Ui.inside(mouseX, mouseY, panelX + 12 + editW + 6, footerY, shopWidth(), 18)) {
			this.client.setScreen(new ShopScreen(this));
		}
	}

	private int shopWidth() {
		return this.textRenderer.getStringWidth(Ui.spaced("COSMÉTICOS")) + 20;
	}

	@Override
	protected void keyPressed(char character, int code) {
		if (code == ZadrinthKeys.MENU.getCode()) {
			this.client.setScreen(null);
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
