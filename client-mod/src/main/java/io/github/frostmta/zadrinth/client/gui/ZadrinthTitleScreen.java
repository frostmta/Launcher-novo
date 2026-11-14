package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.mixin.TitleScreenAccessor;
import io.github.frostmta.zadrinth.client.module.Icon;
import net.minecraft.client.gui.screen.TitleScreen;

/**
 * Tela inicial do Zadrinth Client: o panorama do jogo ao fundo, o símbolo e o
 * nome do client no meio, os botões no nosso visual e atalhos no canto.
 */
public class ZadrinthTitleScreen extends TitleScreen {
	private static final int ICON = 14;
	private static final Icon[] CORNER = {Icon.GEAR, Icon.SHIRT, Icon.LIST, Icon.GLOBE, Icon.CLOSE};
	private long openedAt;

	@Override
	public void init() {
		super.init();
		this.buttons.clear();
		int w = Math.min(190, this.width - 40);
		int x = this.width / 2 - w / 2;
		int top = buttonsTop();
		this.buttons.add(new ZadrinthButton(1, x, top, w, 20, "UM JOGADOR", false));
		this.buttons.add(new ZadrinthButton(2, x, top + 24, w, 20, "MULTIJOGADOR", true));
		int half = (w - 4) / 2;
		this.buttons.add(new ZadrinthButton(0, x, top + 48, half, 20, "OPÇÕES", false));
		this.buttons.add(new ZadrinthButton(4, x + w - half, top + 48, half, 20, "SAIR", false));
		if (openedAt == 0) openedAt = System.currentTimeMillis();
	}

	private int logoSize() {
		return Math.max(40, Math.min(84, this.height / 4));
	}

	private int buttonsTop() {
		int block = logoSize() + 6 + 12 + 10 + 68;
		int top = Math.max(6, (this.height - block) / 2);
		return top + logoSize() + 6 + 12 + 10;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		//Panorama do Minecraft (já desfocado pelo próprio jogo)
		((TitleScreenAccessor) this).zadrinth$renderPanorama(mouseX, mouseY, delta);
		fillGradient(0, 0, this.width, this.height, 0x99080612, 0xCC0E0A1C);
		fill(0, 0, this.width, 24, 0x66000000);

		//Barra de cima: símbolo + nome
		Ui.logo(6, 3, 18);
		this.textRenderer.drawWithShadow("§lZADRINTH CLIENT", 28, 8, Ui.TEXT);
		Ui.brandBar(0, 24, this.width, 1);

		//Atalhos no canto direito
		for (int i = 0; i < CORNER.length; i++) {
			int x = this.width - (CORNER.length - i) * (ICON + 10) - 4;
			boolean hover = Ui.inside(mouseX, mouseY, x - 4, 2, ICON + 8, 20);
			Ui.card(x - 4, 3, ICON + 8, 18, hover ? 0x40FFFFFF : 0x22000000, hover ? 0xFFFFFFFF : 0x70FFFFFF);
			Ui.icon(CORNER[i], x, 5, ICON, hover ? Ui.BRAND_PINK : Ui.TEXT);
		}

		//Símbolo e nome no meio, com um brilho que "respira"
		int size = logoSize();
		int top = buttonsTop() - 10 - 12 - 6 - size;
		float pulse = (float) (Math.sin((System.currentTimeMillis() - openedAt) / 700.0) * 0.5 + 0.5);
		int glow = (int) (0x30 + 0x30 * pulse) << 24 | 0x9D7BFF;
		fillGradient(this.width / 2 - size, top + size / 2, this.width / 2 + size, top + size + 4, 0x00000000, glow);
		Ui.logo(this.width / 2 - size / 2, top, size);
		String title = "§lZ A D R I N T H   C L I E N T";
		Ui.centered(this.textRenderer, title, this.width / 2, top + size + 6, Ui.TEXT);

		for (Object button : this.buttons) {
			((net.minecraft.client.gui.widget.ButtonWidget) button).render(this.client, mouseX, mouseY);
		}

		String version = "Zadrinth Client " + ZadrinthClient.version() + " (Minecraft 1.8.9)";
		this.textRenderer.drawWithShadow("§7" + version, 4, this.height - 10, Ui.TEXT);
		String copyright = "Copyright Mojang AB. Não distribua!";
		this.textRenderer.drawWithShadow("§7" + copyright, this.width - this.textRenderer.getStringWidth(copyright) - 4, this.height - 10, Ui.TEXT);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if (button == 0) {
			for (int i = 0; i < CORNER.length; i++) {
				int x = this.width - (CORNER.length - i) * (ICON + 10) - 4;
				if (!Ui.inside(mouseX, mouseY, x - 4, 2, ICON + 8, 20)) continue;
				switch (CORNER[i]) {
					case GEAR:
						this.client.setScreen(new net.minecraft.client.gui.screen.SettingsScreen(this, this.client.options));
						return;
					case SHIRT:
						this.client.setScreen(new ShopScreen(this));
						return;
					case LIST:
						this.client.setScreen(new ZadrinthMenuScreen(this));
						return;
					case GLOBE:
						this.client.setScreen(new net.minecraft.client.gui.screen.options.LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager()));
						return;
					default:
						this.client.scheduleStop();
						return;
				}
			}
		}
		try {
			super.mouseClicked(mouseX, mouseY, button);
		} catch (Exception ignored) {
		}
	}
}
