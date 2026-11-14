package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import io.github.frostmta.zadrinth.client.mixin.TitleScreenAccessor;
import io.github.frostmta.zadrinth.client.module.Icon;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;

/**
 * Tela inicial do Zadrinth Client.
 *
 * Esquerda: o seu jogador em 3D (skin, capa e cosméticos), girando devagar;
 * arraste para girar e a cabeça acompanha o dedo. Direita: símbolo, botões e
 * "Entrar rápido" nos servidores de PvP. No canto: opções, loja, mods, idioma
 * e sair. Fundo: o panorama do jogo escurecido com partículas da marca.
 */
public class ZadrinthTitleScreen extends TitleScreen {
	private static final int ICON = 14;
	private static final Icon[] CORNER = {Icon.GEAR, Icon.BAG, Icon.LIST, Icon.GLOBE, Icon.CLOSE};
	private static final String[][] SERVERS = {
			{"Mush", "jogar.mush.com.br"},
			{"Hylex", "hylex.gg"},
			{"GladMC", "jogar.gladmc.com"},
			{"Hypixel", "mc.hypixel.net"},
	};
	private static final int SHOP_ID = 100;

	private long openedAt;
	private float yaw = 25.0F;
	private boolean dragging;
	private int dragStartX;
	private float yawAtDrag;
	private long lastDrag;

	//Layout
	private boolean showModel;
	private int modelX, modelFeetY, modelSize;
	private int colX, colW, logoSize, logoY, buttonsY, serversY;

	@Override
	public void init() {
		super.init();
		this.buttons.clear();
		showModel = this.width >= 330;
		int left = showModel ? (int) (this.width * 0.40F) : 0;
		colW = Math.min(196, this.width - left - 24);
		colX = left + (this.width - left - colW) / 2;
		logoSize = Math.max(28, Math.min(56, this.height / 6));
		int block = logoSize + 22 + 3 * 22 + 2 + 30;
		logoY = Math.max(28, 24 + (this.height - 24 - 12 - block) / 2);
		buttonsY = logoY + logoSize + 22;

		this.buttons.add(new ZadrinthButton(2, colX, buttonsY, colW, 20, "MULTIJOGADOR", true));
		this.buttons.add(new ZadrinthButton(1, colX, buttonsY + 22, colW, 20, "UM JOGADOR", false));
		int third = (colW - 4) / 3;
		this.buttons.add(new ZadrinthButton(SHOP_ID, colX, buttonsY + 44, third, 20, "LOJA", false));
		this.buttons.add(new ZadrinthButton(0, colX + third + 2, buttonsY + 44, third, 20, "OPÇÕES", false));
		this.buttons.add(new ZadrinthButton(4, colX + colW - third, buttonsY + 44, third, 20, "SAIR", false));
		serversY = buttonsY + 68;

		modelSize = Math.max(20, Math.min(62, (int) (this.height * 0.27F)));
		modelX = (int) (this.width * 0.21F);
		modelFeetY = Math.min(this.height - 26, 24 + (this.height - 24) / 2 + (int) (modelSize * 0.95F));
		if (openedAt == 0) openedAt = System.currentTimeMillis();
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		long now = System.currentTimeMillis();
		float seconds = (now - openedAt) / 1000.0F;

		//Fundo: panorama do jogo + véu escuro + partículas
		((TitleScreenAccessor) this).zadrinth$renderPanorama(mouseX, mouseY, delta);
		fillGradient(0, 0, this.width, this.height, 0xB0080612, 0xE00E0A1C);
		particles(seconds);

		//Barra de cima
		fill(0, 0, this.width, 24, 0x80000000);
		Ui.logo(6, 3, 18);
		this.textRenderer.drawWithShadow("§lZADRINTH CLIENT", 28, 8, Ui.TEXT);
		Ui.brandBar(0, 24, this.width, 1);
		for (int i = 0; i < CORNER.length; i++) {
			int x = cornerX(i);
			boolean hover = Ui.inside(mouseX, mouseY, x - 4, 2, ICON + 8, 20);
			Ui.card(x - 4, 3, ICON + 8, 18, hover ? 0x40FFFFFF : 0x22000000, hover ? 0xFFFFFFFF : 0x70FFFFFF);
			Ui.icon(CORNER[i], x, 5, ICON, hover ? Ui.BRAND_PINK : Ui.TEXT);
		}

		if (showModel) drawPlayer(mouseX, mouseY, seconds);

		//Símbolo e nome
		float pulse = (float) (Math.sin(seconds * 1.4) * 0.5 + 0.5);
		int cx = colX + colW / 2;
		int glow = ((int) (0x28 + 0x28 * pulse) << 24) | 0x9D7BFF;
		fillGradient(cx - logoSize, logoY + logoSize / 3, cx + logoSize, logoY + logoSize + 2, 0x00000000, glow);
		Ui.logo(cx - logoSize / 2, logoY, logoSize);
		Ui.centered(this.textRenderer, "§lZ A D R I N T H   C L I E N T", cx, logoY + logoSize + 4, Ui.TEXT);

		for (Object button : this.buttons) ((ButtonWidget) button).render(this.client, mouseX, mouseY);

		//Entrar rápido
		Ui.centered(this.textRenderer, "§7ENTRAR RÁPIDO", cx, serversY, Ui.TEXT);
		int chipW = (colW - 3 * 3) / 4;
		for (int i = 0; i < SERVERS.length; i++) {
			int x = colX + i * (chipW + 3);
			int y = serversY + 11;
			boolean hover = Ui.inside(mouseX, mouseY, x, y, chipW, 16);
			Ui.card(x, y, chipW, 16, hover ? 0x449D7BFF : 0x26000000, hover ? Ui.BRAND : 0x66FFFFFF);
			String name = this.textRenderer.trimToWidth(SERVERS[i][0], chipW - 4);
			Ui.centered(this.textRenderer, name, x + chipW / 2, y + 4, hover ? Ui.TEXT : 0xFFD8D8E0);
		}

		String version = "Zadrinth Client " + ZadrinthClient.version() + " (Minecraft 1.8.9)";
		this.textRenderer.drawWithShadow("§7" + version, 4, this.height - 10, Ui.TEXT);
		String copyright = "Copyright Mojang AB. Não distribua!";
		this.textRenderer.drawWithShadow("§8" + copyright, this.width - this.textRenderer.getStringWidth(copyright) - 4, this.height - 10, Ui.TEXT);
	}

	private void drawPlayer(int mouseX, int mouseY, float seconds) {
		//Pedestal com brilho
		int w = modelSize + 16;
		fillGradient(modelX - w, modelFeetY - 10, modelX + w, modelFeetY + 4, 0x00000000, 0x559D7BFF);
		fill(modelX - w / 2, modelFeetY + 3, modelX + w / 2, modelFeetY + 4, 0x88E07BFF);
		//Gira sozinho, a não ser que você tenha arrastado há pouco
		if (!dragging && System.currentTimeMillis() - lastDrag > 2500L) yaw += 0.35F;
		float headX = Math.max(-1.0F, Math.min(1.0F, (mouseX - modelX) / 120.0F));
		float headY = Math.max(-1.0F, Math.min(1.0F, (mouseY - (modelFeetY - modelSize * 1.6F)) / 120.0F));
		TitlePlayerModel.draw(modelX, modelFeetY, modelSize, yaw, -headX, headY);
		String name = this.client.getSession().getUsername();
		Ui.centered(this.textRenderer, name, modelX, modelFeetY + 8, Ui.TEXT);
		if (System.currentTimeMillis() - lastDrag > 6000L) {
			Ui.centered(this.textRenderer, "§8arraste para girar", modelX, modelFeetY + 18, Ui.TEXT);
		}
	}

	/** Pontinhos nas cores da marca subindo devagar (sem estado: só o tempo). */
	private void particles(float seconds) {
		for (int i = 0; i < 46; i++) {
			float seed = (i * 0.618034F) % 1.0F;
			float speed = 6.0F + (i % 7) * 2.5F;
			float span = this.height + 20;
			float y = this.height + 10 - ((seconds * speed + seed * span) % span);
			float x = seed * this.width + (float) Math.sin(seconds * 0.6F + i) * 8.0F;
			float twinkle = (float) (Math.sin(seconds * 2.0F + i * 1.7F) * 0.5 + 0.5);
			int alpha = (int) (60 + 150 * twinkle);
			int color = CosmeticStyle.hsv(0.72F + (i % 5) * 0.05F, 0.45F, 1.0F);
			int size = i % 9 == 0 ? 2 : 1;
			fill((int) x, (int) y, (int) x + size, (int) y + size, (alpha << 24) | color);
		}
	}

	private int cornerX(int i) {
		return this.width - (CORNER.length - i) * (ICON + 10) - 4;
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		if (button.id == SHOP_ID) {
			this.client.setScreen(new ShopScreen(this));
			return;
		}
		try {
			super.buttonClicked(button);
		} catch (Exception ignored) {
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if (button == 0) {
			for (int i = 0; i < CORNER.length; i++) {
				int x = cornerX(i);
				if (!Ui.inside(mouseX, mouseY, x - 4, 2, ICON + 8, 20)) continue;
				switch (CORNER[i]) {
					case GEAR:
						this.client.setScreen(new net.minecraft.client.gui.screen.SettingsScreen(this, this.client.options));
						return;
					case BAG:
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
			int chipW = (colW - 3 * 3) / 4;
			for (int i = 0; i < SERVERS.length; i++) {
				int x = colX + i * (chipW + 3);
				if (Ui.inside(mouseX, mouseY, x, serversY + 11, chipW, 16)) {
					ServerInfo info = new ServerInfo(SERVERS[i][0], SERVERS[i][1], false);
					this.client.setScreen(new ConnectScreen(this, this.client, info));
					return;
				}
			}
			if (showModel && Ui.inside(mouseX, mouseY, modelX - modelSize, modelFeetY - modelSize * 2, modelSize * 2, modelSize * 2 + 10)) {
				dragging = true;
				dragStartX = mouseX;
				yawAtDrag = yaw;
				lastDrag = System.currentTimeMillis();
				return;
			}
		}
		try {
			super.mouseClicked(mouseX, mouseY, button);
		} catch (Exception ignored) {
		}
	}

	@Override
	protected void mouseDragged(int mouseX, int mouseY, int button, long time) {
		if (dragging) {
			yaw = yawAtDrag - (mouseX - dragStartX) * 1.8F;
			lastDrag = System.currentTimeMillis();
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		dragging = false;
		try {
			super.mouseReleased(mouseX, mouseY, button);
		} catch (Exception ignored) {
		}
	}
}
