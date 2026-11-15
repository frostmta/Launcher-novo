package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import io.github.frostmta.zadrinth.client.cosmetics.Wardrobe;
import io.github.frostmta.zadrinth.client.cosmetics.render.AnimState;
import io.github.frostmta.zadrinth.client.cosmetics.render.CosmeticsLayer;
import io.github.frostmta.zadrinth.client.module.Icon;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * Tela inicial do Zadrinth Client.
 *
 * Esquerda: o seu jogador em 3D (skin, capa e cosméticos), girando devagar;
 * arraste para girar e a cabeça acompanha o dedo. Direita: símbolo, botões e
 * "Entrar rápido" nos servidores de PvP. No canto: opções, loja, mods, idioma
 * e sair. Fundo: o panorama do jogo escurecido com partículas da marca.
 */
public class ZadrinthTitleScreen extends ZScreen {
	private static final int ICON = 14;
	private static final Icon[] CORNER = {Icon.GEAR, Icon.BAG, Icon.LIST, Icon.GLOBE, Icon.CLOSE};
	private static final String[][] SERVERS = {
			{"Mush", "jogar.mush.com.br"},
			{"Hylex", "hylex.gg"},
			{"GladMC", "jogar.gladmc.com"},
			{"Hypixel", "mc.hypixel.net"},
	};
	private static final String[] BUTTONS = {"MULTIJOGADOR", "UM JOGADOR", "LOJA", "OPÇÕES", "SAIR"};

	private long openedAt;
	private float yaw = 25.0F;
	private boolean dragging;
	private int dragStartX;
	private float yawAtDrag;
	private long lastDrag;
	private Supplier<PlayerSkin> skin;

	//Layout
	private boolean showModel;
	private int modelX, modelFeetY, modelSize;
	private int colX, colW, logoSize, logoY, buttonsY, serversY;

	public ZadrinthTitleScreen() {
		super(null);
	}

	@Override
	protected void init() {
		showModel = this.width >= 330;
		int left = showModel ? (int) (this.width * 0.40F) : 0;
		colW = Math.min(196, this.width - left - 24);
		colX = left + (this.width - left - colW) / 2;
		logoSize = Math.max(28, Math.min(56, this.height / 6));
		int block = logoSize + 22 + 3 * 22 + 2 + 30;
		logoY = Math.max(28, 24 + (this.height - 24 - 12 - block) / 2);
		buttonsY = logoY + logoSize + 22;
		serversY = buttonsY + 68;

		modelSize = Math.max(20, Math.min(62, (int) (this.height * 0.27F)));
		modelX = (int) (this.width * 0.21F);
		modelFeetY = Math.min(this.height - 26, 24 + (this.height - 24) / 2 + (int) (modelSize * 0.95F));
		if (openedAt == 0) openedAt = System.currentTimeMillis();
		if (skin == null) skin = this.minecraft.getSkinManager().createLookup(this.minecraft.getGameProfile(), false);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	/** {x, y, w} de cada botão. */
	private int[] button(int index) {
		if (index < 2) return new int[]{colX, buttonsY + index * 22, colW};
		int third = (colW - 4) / 3;
		int x = index == 2 ? colX : index == 3 ? colX + third + 2 : colX + colW - third;
		return new int[]{x, buttonsY + 44, third};
	}

	@Override
	protected void draw(int mouseX, int mouseY, float delta) {
		long now = System.currentTimeMillis();
		float seconds = (now - openedAt) / 1000.0F;

		//Fundo: panorama do jogo + véu escuro + partículas
		renderPanorama(Ui.g(), delta);
		Ui.gradient(0, 0, this.width, this.height, 0xB0080612, 0xE00E0A1C);
		particles(seconds);

		//Barra de cima
		Ui.fill(0, 0, this.width, 24, 0x80000000);
		Ui.logo(6, 3, 18);
		Ui.text("§lZADRINTH CLIENT", 28, 8, Ui.TEXT);
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
		Ui.gradient(cx - logoSize, logoY + logoSize / 3, cx + logoSize, logoY + logoSize + 2, 0x00000000, glow);
		Ui.logo(cx - logoSize / 2, logoY, logoSize);
		Ui.centered("§lZ A D R I N T H   C L I E N T", cx, logoY + logoSize + 4, Ui.TEXT);

		for (int i = 0; i < BUTTONS.length; i++) {
			int[] b = button(i);
			boolean hover = Ui.inside(mouseX, mouseY, b[0], b[1], b[2], 20);
			int fill, outline;
			if (i == 0) {
				fill = hover ? 0xE0B49BFF : 0xC89D7BFF;
				outline = 0xFFE8DCFF;
			} else {
				fill = hover ? 0x55FFFFFF : 0x30000000;
				outline = hover ? 0xFFFFFFFF : 0xB0FFFFFF;
			}
			Ui.card(b[0], b[1], b[2], 20, fill, outline);
			Ui.centered("§l" + BUTTONS[i], b[0] + b[2] / 2, b[1] + 6, 0xFFFFFFFF);
		}

		//Entrar rápido
		Ui.centered("§7ENTRAR RÁPIDO", cx, serversY, Ui.TEXT);
		int chipW = (colW - 3 * 3) / 4;
		for (int i = 0; i < SERVERS.length; i++) {
			int x = colX + i * (chipW + 3);
			int y = serversY + 11;
			boolean hover = Ui.inside(mouseX, mouseY, x, y, chipW, 16);
			Ui.card(x, y, chipW, 16, hover ? 0x449D7BFF : 0x26000000, hover ? Ui.BRAND : 0x66FFFFFF);
			String name = Ui.trim(SERVERS[i][0], chipW - 4);
			Ui.centered(name, x + chipW / 2, y + 4, hover ? Ui.TEXT : 0xFFD8D8E0);
		}

		String version = "Zadrinth Client " + ZadrinthClient.version() + " (Minecraft " + ZadrinthClient.MINECRAFT + ")";
		Ui.text("§7" + version, 4, this.height - 10, Ui.TEXT);
		String copyright = "Copyright Mojang AB. Não distribua!";
		Ui.text("§8" + copyright, this.width - Ui.width(copyright) - 4, this.height - 10, Ui.TEXT);
	}

	private void drawPlayer(int mouseX, int mouseY, float seconds) {
		//Pedestal com brilho
		int w = modelSize + 16;
		Ui.gradient(modelX - w, modelFeetY - 10, modelX + w, modelFeetY + 4, 0x00000000, 0x559D7BFF);
		Ui.fill(modelX - w / 2, modelFeetY + 3, modelX + w / 2, modelFeetY + 4, 0x88E07BFF);
		//Gira sozinho, a não ser que você tenha arrastado há pouco
		if (!dragging && System.currentTimeMillis() - lastDrag > 2500L) yaw += 0.35F;
		float headX = Math.max(-1.0F, Math.min(1.0F, (mouseX - modelX) / 120.0F));
		float headY = Math.max(-1.0F, Math.min(1.0F, (mouseY - (modelFeetY - modelSize * 1.6F)) / 120.0F));
		submitModel(seconds, headX, headY);
		String name = this.minecraft.getUser().getName();
		Ui.centered(name, modelX, modelFeetY + 8, Ui.TEXT);
		if (System.currentTimeMillis() - lastDrag > 6000L) {
			Ui.centered("§8arraste para girar", modelX, modelFeetY + 18, Ui.TEXT);
		}
	}

	/** O jogador em 3D sem mundo: um "manequim" com a sua skin e os seus cosméticos. */
	private void submitModel(float seconds, float headX, float headY) {
		AvatarRenderState state = new AvatarRenderState();
		state.entityType = EntityType.PLAYER;
		state.skin = skin != null ? skin.get() : state.skin;
		state.ageInTicks = seconds * 20.0F;
		state.bodyRot = 180.0F + yaw;
		state.yRot = headX * 30.0F;
		state.xRot = headY * 20.0F;
		state.boundingBoxWidth = 0.6F;
		state.boundingBoxHeight = 1.8F;
		state.scale = 1.0F;
		state.lightCoords = 15728880;
		state.setData(CosmeticsLayer.DATA, new CosmeticsLayer.Data(Wardrobe.local(), AnimState.idle(seconds), null));

		int top = modelFeetY - Math.round(modelSize * 2.15F);
		int x0 = modelX - modelSize - 30, x1 = modelX + modelSize + 30;
		float scale = modelSize * 1.05F;
		Quaternionf camera = new Quaternionf().rotateX(8.0F * (float) (Math.PI / 180.0));
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(camera);
		Ui.g().submitEntityRenderState(state, scale, new Vector3f(0.0F, 0.95F, 0.0F), rotation, camera, x0, top, x1, modelFeetY + 4);
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
			Ui.fill((int) x, (int) y, (int) x + size, (int) y + size, (alpha << 24) | color);
		}
	}

	private int cornerX(int i) {
		return this.width - (CORNER.length - i) * (ICON + 10) - 4;
	}

	@Override
	protected void onMouseClicked(int mouseX, int mouseY, int button) {
		if (button != 0) return;
		for (int i = 0; i < CORNER.length; i++) {
			int x = cornerX(i);
			if (!Ui.inside(mouseX, mouseY, x - 4, 2, ICON + 8, 20)) continue;
			switch (CORNER[i]) {
				case GEAR -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
				case BAG -> this.minecraft.setScreen(new ShopScreen(this));
				case LIST -> this.minecraft.setScreen(new ZadrinthMenuScreen(this));
				case GLOBE -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()));
				default -> this.minecraft.stop();
			}
			return;
		}
		for (int i = 0; i < BUTTONS.length; i++) {
			int[] b = button(i);
			if (!Ui.inside(mouseX, mouseY, b[0], b[1], b[2], 20)) continue;
			switch (i) {
				case 0 -> this.minecraft.setScreen(new JoinMultiplayerScreen(this));
				case 1 -> this.minecraft.setScreen(new SelectWorldScreen(this));
				case 2 -> this.minecraft.setScreen(new ShopScreen(this));
				case 3 -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
				default -> this.minecraft.stop();
			}
			return;
		}
		int chipW = (colW - 3 * 3) / 4;
		for (int i = 0; i < SERVERS.length; i++) {
			int x = colX + i * (chipW + 3);
			if (Ui.inside(mouseX, mouseY, x, serversY + 11, chipW, 16)) {
				ServerData info = new ServerData(SERVERS[i][0], SERVERS[i][1], ServerData.Type.OTHER);
				ConnectScreen.startConnecting(this, this.minecraft, ServerAddress.parseString(SERVERS[i][1]), info, false, null);
				return;
			}
		}
		if (showModel && Ui.inside(mouseX, mouseY, modelX - modelSize, modelFeetY - modelSize * 2, modelSize * 2, modelSize * 2 + 10)) {
			dragging = true;
			dragStartX = mouseX;
			yawAtDrag = yaw;
			lastDrag = System.currentTimeMillis();
		}
	}

	@Override
	protected void onMouseDragged(int mouseX, int mouseY, int button) {
		if (dragging) {
			yaw = yawAtDrag - (mouseX - dragStartX) * 1.8F;
			lastDrag = System.currentTimeMillis();
		}
	}

	@Override
	protected void onMouseReleased(int mouseX, int mouseY, int button) {
		dragging = false;
	}
}
