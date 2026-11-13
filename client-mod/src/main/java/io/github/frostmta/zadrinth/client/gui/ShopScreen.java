package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticType;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetics;
import io.github.frostmta.zadrinth.client.cosmetics.Loadout;
import io.github.frostmta.zadrinth.client.cosmetics.ShopService;
import io.github.frostmta.zadrinth.client.cosmetics.Wardrobe;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.input.Mouse;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Loja de cosméticos dentro do jogo.
 *
 * Esquerda: categorias e opções gerais. Meio: busca + grade de itens, cada um
 * com o seu jogador em 3D usando o item. Direita: prévia grande (arraste para
 * girar), descrição, Resgatar/Equipar/Remover e as opções de cor.
 */
public class ShopScreen extends Screen {
	private enum Category {
		ALL("TODOS"), CAPES("CAPAS"), WINGS("ASAS"), BANDANAS("BANDANAS"), OWNED("COLEÇÃO");

		final String label;

		Category(String label) {
			this.label = label;
		}
	}

	private static final int HEADER_H = 24;
	private static final int RAIL_W = 78;
	private static final int GAP = 5;
	private static final int SWATCH = 9;

	private final Screen parent;
	private Category category = Category.ALL;
	private boolean sortDescending;
	private Cosmetic selected;
	private List<Cosmetic> visible = new ArrayList<>();
	private TextFieldWidget search;
	private String lastQuery = "";

	//Layout
	private int px, py, pw, ph;
	private int gridX, gridY, gridW, gridH, cols, cardW, cardH, previewH;
	private int detailX, detailY, detailW, detailH;
	private int bigX, bigY, bigW, bigH;
	//Posições calculadas no desenho e usadas no clique
	private int buttonY = -1000, swatchY = -1000, pillsY = -1000;

	//Rolagem e arrasto
	private float scroll;
	private int pressX, pressY;
	private boolean pressInGrid, draggingGrid, pressInPreview;
	private float scrollAtPress;
	private float previewYaw = 200.0F;
	private float yawAtPress;
	private boolean userRotated;
	private long lastFrame;

	//Mensagem rápida ("Resgatado!")
	private String toast;
	private long toastUntil;

	public ShopScreen(Screen parent) {
		this.parent = parent;
	}

	@Override
	public void init() {
		px = 6;
		py = 6;
		pw = this.width - 12;
		ph = this.height - 12;
		int top = py + HEADER_H + 4;

		detailW = Math.max(118, Math.min(170, (int) (pw * 0.32F)));
		detailX = px + pw - GAP - detailW;
		detailY = top;
		detailH = py + ph - GAP - top;

		gridX = px + GAP + RAIL_W + GAP;
		gridW = detailX - GAP - gridX;
		gridY = top + 20;
		gridH = py + ph - GAP - gridY;
		cols = Math.max(1, (gridW + 4) / (58 + 4));
		cardW = (gridW - (cols - 1) * 4) / cols;
		previewH = Math.min(cardW, 72);
		cardH = previewH + 24;

		bigW = detailW - 12;
		//Espaço embaixo da prévia: nome, tipo, 2 linhas, botão e opções de cor
		int swatchRows = (detailW - 14) >= CosmeticStyle.PALETTE.length * (SWATCH + 3) ? 1 : 2;
		int below = 11 + 12 + 20 + 4 + 16 + 6 + 10 + swatchRows * (SWATCH + 3) + 4 + 14 + 6;
		bigH = Math.max(48, Math.min(detailH - 12 - below, bigW + 20));
		bigX = detailX + 6;
		bigY = detailY + 6;

		String query = search == null ? "" : search.getText();
		search = new TextFieldWidget(1, this.textRenderer, gridX + 3, top + 2, gridW - 48, 12);
		search.setMaxLength(32);
		search.setHasBorder(false);
		search.setText(query);
		refresh();
		if (selected == null && !visible.isEmpty()) select(visible.get(0));
	}

	// ------------------------------------------------------------------ dados
	private void refresh() {
		String query = normalize(search == null ? "" : search.getText());
		List<Cosmetic> list = new ArrayList<>();
		for (Cosmetic cosmetic : Cosmetics.all()) {
			if (!matches(cosmetic)) continue;
			if (!query.isEmpty() && !normalize(cosmetic.name + " " + cosmetic.description + " " + cosmetic.type.label).contains(query)) {
				continue;
			}
			list.add(cosmetic);
		}
		Collections.sort(list, new Comparator<Cosmetic>() {
			@Override
			public int compare(Cosmetic a, Cosmetic b) {
				return normalize(a.name).compareTo(normalize(b.name));
			}
		});
		if (sortDescending) Collections.reverse(list);
		visible = list;
		lastQuery = search == null ? "" : search.getText();
		clampScroll();
	}

	private boolean matches(Cosmetic cosmetic) {
		switch (category) {
			case CAPES: return cosmetic.type == CosmeticType.CAPE;
			case WINGS: return cosmetic.type == CosmeticType.WINGS;
			case BANDANAS: return cosmetic.type == CosmeticType.BANDANA;
			case OWNED: return ShopService.owns(cosmetic);
			default: return true;
		}
	}

	private static String normalize(String text) {
		String value = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return value.toLowerCase(Locale.ROOT).trim();
	}

	private void select(Cosmetic cosmetic) {
		selected = cosmetic;
		userRotated = false;
		previewYaw = cosmetic != null && cosmetic.type == CosmeticType.BANDANA ? 25.0F : 200.0F;
	}

	private int rows() {
		return (visible.size() + cols - 1) / cols;
	}

	private void clampScroll() {
		float max = Math.max(0, rows() * (cardH + 4) - 4 - gridH);
		if (scroll > max) scroll = max;
		if (scroll < 0) scroll = 0;
	}

	/** Só o item (para os cartões). */
	private static Loadout only(Cosmetic cosmetic) {
		Loadout loadout = new Loadout();
		loadout.set(cosmetic.type, cosmetic.id);
		return loadout;
	}

	/** O seu visual atual com o item selecionado no lugar (para a prévia grande). */
	private Loadout trying(Cosmetic cosmetic) {
		Loadout loadout = Wardrobe.local().copy();
		if (cosmetic != null && !Wardrobe.isEquipped(cosmetic)) {
			loadout.set(cosmetic.type, cosmetic.id);
			loadout.style(cosmetic.type).color = -1;
			loadout.style(cosmetic.type).rgb = false;
			loadout.style(cosmetic.type).glint = false;
		}
		return loadout;
	}

	// ------------------------------------------------------------------ desenho
	@Override
	public void render(int mouseX, int mouseY, float delta) {
		long now = System.currentTimeMillis();
		float dt = lastFrame == 0L ? 0.0F : Math.min(0.1F, (now - lastFrame) / 1000.0F);
		lastFrame = now;
		if (!userRotated && !pressInPreview) previewYaw += dt * 22.0F;
		if (!search.getText().equals(lastQuery)) refresh();

		fill(0, 0, this.width, this.height, 0xB0000000);
		Ui.box(px, py, pw, ph, Ui.PANEL, Ui.BORDER);
		Ui.brandBar(px + 1, py + 1, pw - 2, 2);
		drawHeader(mouseX, mouseY);
		drawRail(mouseX, mouseY);
		drawSearch(mouseX, mouseY);
		drawGrid(mouseX, mouseY);
		drawDetail(mouseX, mouseY);
	}

	private void drawHeader(int mouseX, int mouseY) {
		int y = py + 9;
		this.textRenderer.drawWithShadow("§lZADRINTH", px + 10, y, Ui.TEXT);
		int x = px + 12 + this.textRenderer.getStringWidth("§lZADRINTH");
		this.textRenderer.drawWithShadow("§7" + Ui.spaced("LOJA"), x + 4, y, Ui.TEXT);

		//Fechar
		int closeX = px + pw - 20;
		boolean hover = Ui.inside(mouseX, mouseY, closeX, py + 5, 15, 15);
		Ui.box(closeX, py + 5, 15, 15, hover ? 0x40EF4444 : Ui.CARD, hover ? Ui.DISABLED : Ui.BORDER);
		Ui.centered(this.textRenderer, "x", closeX + 8, py + 8, Ui.TEXT);

		//Selo de "tudo grátis" + saldo (para quando houver moedas)
		String chip = "§a● §fTUDO GRÁTIS";
		int chipW = this.textRenderer.getStringWidth(chip) + 12;
		int chipX = closeX - 6 - chipW;
		Ui.box(chipX, py + 5, chipW, 15, 0x2222C55E, 0x6622C55E);
		this.textRenderer.drawWithShadow(chip, chipX + 6, py + 9, Ui.TEXT);
		if (ShopService.balance() > 0) {
			String coins = "§e" + ShopService.balance() + " moedas";
			this.textRenderer.drawWithShadow(coins, chipX - 8 - this.textRenderer.getStringWidth(coins), py + 9, Ui.TEXT);
		}
	}

	private void drawRail(int mouseX, int mouseY) {
		int x = px + GAP, y = py + HEADER_H + 4;
		Ui.box(x, y, RAIL_W, py + ph - GAP - y, 0x10FFFFFF, Ui.BORDER);
		int itemY = y + 5;
		for (Category value : Category.values()) {
			boolean active = value == category;
			boolean hover = Ui.inside(mouseX, mouseY, x + 3, itemY, RAIL_W - 6, 16);
			if (active) {
				fill(x + 3, itemY, x + RAIL_W - 3, itemY + 16, 0x339D7BFF);
				Ui.brandBar(x + 3, itemY, 2, 16);
			} else if (hover) {
				fill(x + 3, itemY, x + RAIL_W - 3, itemY + 16, Ui.CARD_HOVER);
			}
			this.textRenderer.drawWithShadow(value.label, x + 10, itemY + 4, active ? Ui.TEXT : Ui.TEXT_MUTED);
			String count = String.valueOf(count(value));
			this.textRenderer.drawWithShadow("§8" + count, x + RAIL_W - 8 - this.textRenderer.getStringWidth(count), itemY + 4, Ui.TEXT);
			itemY += 18;
		}

		//Opções gerais (como o "Cloth Cloak" do Lunar)
		ZadrinthConfig config = ZadrinthConfig.get();
		int optionsY = py + ph - GAP - 44;
		fill(x + 6, optionsY - 6, x + RAIL_W - 6, optionsY - 5, Ui.BORDER);
		checkbox(x + 6, optionsY, "Capa de tecido", config.clothCape, mouseX, mouseY);
		checkbox(x + 6, optionsY + 18, "Ver dos outros", config.showOthersCosmetics, mouseX, mouseY);
	}

	private int count(Category value) {
		int total = 0;
		Category previous = category;
		category = value;
		for (Cosmetic cosmetic : Cosmetics.all()) if (matches(cosmetic)) total++;
		category = previous;
		return total;
	}

	private void checkbox(int x, int y, String label, boolean on, int mouseX, int mouseY) {
		boolean hover = Ui.inside(mouseX, mouseY, x, y, RAIL_W - 12, 12);
		Ui.box(x, y + 1, 10, 10, on ? Ui.BRAND : 0x22FFFFFF, hover ? Ui.TEXT : Ui.BORDER);
		if (on) fill(x + 3, y + 4, x + 7, y + 8, Ui.TEXT);
		this.textRenderer.drawWithShadow(label, x + 14, y + 2, on ? Ui.TEXT : Ui.TEXT_MUTED);
	}

	private void drawSearch(int mouseX, int mouseY) {
		int y = py + HEADER_H + 4;
		Ui.box(gridX - 2, y - 2, gridW - 38, 16, search.isFocused() ? 0x22FFFFFF : 0x14FFFFFF,
				search.isFocused() ? Ui.BRAND : Ui.BORDER);
		if (search.getText().isEmpty() && !search.isFocused()) {
			this.textRenderer.drawWithShadow("§8Buscar cosméticos...", gridX + 3, y + 2, Ui.TEXT);
		}
		search.render();

		int sortX = gridX + gridW - 34;
		boolean hover = Ui.inside(mouseX, mouseY, sortX, y - 2, 34, 16);
		Ui.box(sortX, y - 2, 34, 16, hover ? Ui.CARD_HOVER : Ui.CARD, Ui.BORDER);
		Ui.centered(this.textRenderer, sortDescending ? "Z-A" : "A-Z", sortX + 17, y + 2, Ui.TEXT);
	}

	private void drawGrid(int mouseX, int mouseY) {
		if (visible.isEmpty()) {
			String message = category == Category.OWNED && lastQuery.isEmpty()
					? "Resgate itens para montar sua coleção" : "Nada encontrado";
			Ui.centered(this.textRenderer, "§7" + message, gridX + gridW / 2, gridY + gridH / 2 - 4, Ui.TEXT);
			return;
		}
		PlayerPreview.clip(gridX, gridY, gridW, gridH);
		for (int i = 0; i < visible.size(); i++) {
			int col = i % cols, row = i / cols;
			int cx = gridX + col * (cardW + 4);
			int cy = gridY + row * (cardH + 4) - Math.round(scroll);
			if (cy + cardH < gridY || cy > gridY + gridH) continue;
			drawCard(visible.get(i), cx, cy, mouseX, mouseY);
			PlayerPreview.clip(gridX, gridY, gridW, gridH);
		}
		PlayerPreview.endClip();

		//Barrinha de rolagem
		float max = rows() * (cardH + 4) - 4;
		if (max > gridH) {
			int barH = Math.max(12, Math.round(gridH * gridH / max));
			int barY = gridY + Math.round((gridH - barH) * (scroll / (max - gridH)));
			fill(gridX + gridW + 1, barY, gridX + gridW + 3, barY + barH, 0x669D7BFF);
		}
	}

	private void drawCard(Cosmetic cosmetic, int x, int y, int mouseX, int mouseY) {
		boolean active = cosmetic == selected;
		boolean hover = !draggingGrid && Ui.inside(mouseX, mouseY, x, y, cardW, cardH)
				&& Ui.inside(mouseX, mouseY, gridX, gridY, gridW, gridH);
		boolean equipped = Wardrobe.isEquipped(cosmetic);
		boolean owned = ShopService.owns(cosmetic);

		Ui.box(x, y, cardW, cardH, active ? 0x2A9D7BFF : hover ? Ui.CARD_HOVER : Ui.CARD, active ? Ui.BRAND : Ui.BORDER);
		//Brilho da cor do item no fundo da prévia
		fillGradient(x + 1, y + 1, x + cardW - 1, y + previewH, 0x00000000, (cosmetic.accent & 0x00FFFFFF) | 0x55000000);

		//Jogador 3D com o item
		int cx = x + cardW / 2;
		int top = Math.max(y + 1, gridY), bottom = Math.min(y + previewH, gridY + gridH);
		if (bottom > top) {
			PlayerPreview.clip(x + 1, top, cardW - 2, bottom - top);
			drawPreview(cosmetic, only(cosmetic), cx, y, previewH, cardW, false);
		}
		PlayerPreview.clip(gridX, gridY, gridW, gridH);

		//Nome e selo
		String name = this.textRenderer.trimToWidth(cosmetic.name, cardW - 6);
		Ui.centered(this.textRenderer, name, cx, y + previewH + 3, Ui.TEXT);
		String tag;
		if (equipped) tag = "§d" + Ui.spaced("USANDO");
		else if (owned) tag = "§7Na coleção";
		else tag = "§a" + ShopService.priceLabel(cosmetic);
		Ui.centered(this.textRenderer, tag, cx, y + previewH + 13, Ui.TEXT);
		if (equipped) fill(x + 1, y + 1, x + cardW - 1, y + 2, Ui.BRAND_PINK);
	}

	/** Enquadra o jogador conforme o tipo: capa/asas de costas, bandana de perto. */
	private void drawPreview(Cosmetic cosmetic, Loadout loadout, int centerX, int top, int height, int width, boolean big) {
		float yaw = big ? previewYaw : cosmetic.type == CosmeticType.BANDANA ? 25.0F : 200.0F;
		int size;
		int feetY;
		if (cosmetic.type == CosmeticType.BANDANA) {
			size = Math.round(Math.min(height * 0.95F, width * 0.95F));
			feetY = top + height / 2 + Math.round(size * 1.72F);
		} else if (cosmetic.type == CosmeticType.WINGS) {
			size = Math.round(Math.min(height * 0.40F, width * 0.33F));
			feetY = top + height / 2 + Math.round(size * 0.95F);
		} else {
			size = Math.round(Math.min(height * 0.44F, width * 0.6F));
			feetY = top + height / 2 + Math.round(size * 0.92F);
		}
		PlayerPreview.draw(centerX, feetY, Math.max(4, size), yaw, big ? 6.0F : 4.0F, loadout);
	}

	private void drawDetail(int mouseX, int mouseY) {
		Ui.box(detailX, detailY, detailW, detailH, 0x10FFFFFF, Ui.BORDER);
		if (selected == null) {
			Ui.centered(this.textRenderer, "§7Escolha um item", detailX + detailW / 2, detailY + detailH / 2, Ui.TEXT);
			return;
		}
		Cosmetic cosmetic = selected;
		boolean owned = ShopService.owns(cosmetic);
		boolean equipped = Wardrobe.isEquipped(cosmetic);

		//Prévia grande (arraste para girar)
		fillGradient(bigX, bigY, bigX + bigW, bigY + bigH, 0x08FFFFFF, (cosmetic.accent & 0x00FFFFFF) | 0x66000000);
		Ui.box(bigX, bigY, bigW, bigH, 0x00000000, Ui.BORDER);
		PlayerPreview.clip(bigX + 1, bigY + 1, bigW - 2, bigH - 2);
		drawPreview(cosmetic, trying(cosmetic), bigX + bigW / 2, bigY, bigH, bigW, true);
		PlayerPreview.endClip();
		if (!userRotated) this.textRenderer.drawWithShadow("§8arraste para girar", bigX + 4, bigY + bigH - 11, Ui.TEXT);

		int y = bigY + bigH + 6;
		int x = detailX + 7;
		int textW = detailW - 14;
		this.textRenderer.drawWithShadow("§l" + this.textRenderer.trimToWidth(cosmetic.name, textW - 6), x, y, Ui.TEXT);
		y += 11;
		String price = cosmetic.isFree() ? "§aGRÁTIS" : "§e" + ShopService.priceLabel(cosmetic);
		this.textRenderer.drawWithShadow("§7" + cosmetic.type.label + " §8• " + price, x, y, Ui.TEXT);
		y += 12;
		@SuppressWarnings("unchecked")
		List<String> lines = this.textRenderer.wrapLines(cosmetic.description, textW);
		for (int i = 0; i < lines.size() && i < 2; i++) {
			this.textRenderer.drawWithShadow("§7" + lines.get(i), x, y, Ui.TEXT);
			y += 10;
		}

		//Botão principal
		y = Math.max(y, bigY + bigH + 6 + 11 + 12 + 20);
		buttonY = y + 4;
		String label = !owned ? (cosmetic.isFree() ? "RESGATAR GRÁTIS" : "COMPRAR") : equipped ? "REMOVER" : "EQUIPAR";
		boolean hover = Ui.inside(mouseX, mouseY, x, buttonY, textW, 16);
		if (equipped) {
			Ui.box(x, buttonY, textW, 16, hover ? 0x40EF4444 : 0x22EF4444, 0x88EF4444);
		} else {
			Ui.brandBar(x, buttonY, textW, 16);
			if (hover) fill(x, buttonY, x + textW, buttonY + 16, 0x30FFFFFF);
		}
		String shown = this.textRenderer.getStringWidth(Ui.spaced(label)) > textW - 6 ? label : Ui.spaced(label);
		Ui.centered(this.textRenderer, shown, x + textW / 2, buttonY + 4, Ui.TEXT);
		y = buttonY + 22;

		//Opções de cor (só do que você está usando)
		if (equipped) {
			CosmeticStyle style = Wardrobe.local().style(cosmetic.type);
			this.textRenderer.drawWithShadow("§7COR", x, y, Ui.TEXT);
			y += 10;
			swatchY = y;
			int sx = x, sy = y;
			for (int i = 0; i < CosmeticStyle.PALETTE.length; i++) {
				int color = CosmeticStyle.PALETTE[i];
				if (sx + SWATCH > x + textW) {
					sx = x;
					sy += SWATCH + 3;
				}
				boolean current = !style.rgb && style.color == color;
				if (color < 0) {
					//"original": quadradinho com o degradê da marca
					Ui.brandBar(sx, sy, SWATCH, SWATCH);
				} else {
					fill(sx, sy, sx + SWATCH, sy + SWATCH, 0xFF000000 | color);
				}
				Ui.box(sx - 1, sy - 1, SWATCH + 2, SWATCH + 2, 0x00000000, current ? Ui.TEXT : 0x44FFFFFF);
				sx += SWATCH + 3;
			}
			y = sy + SWATCH + 5;
			pillsY = y;
			int half = (textW - 4) / 2;
			pill(x, y, half, "RGB", style.rgb, mouseX, mouseY);
			pill(x + half + 4, y, half, "BRILHO", style.glint, mouseX, mouseY);
		} else if (owned) {
			Ui.centered(this.textRenderer, "§8Equipe para mudar a cor", x + textW / 2, y, Ui.TEXT);
		}

		if (toast != null && System.currentTimeMillis() < toastUntil) {
			int tw = this.textRenderer.getStringWidth(toast) + 12;
			int tx = detailX + (detailW - tw) / 2, ty = bigY + 4;
			Ui.box(tx, ty, tw, 14, 0xE6121019, Ui.BRAND);
			Ui.centered(this.textRenderer, toast, detailX + detailW / 2, ty + 3, Ui.TEXT);
		}
	}

	private void pill(int x, int y, int w, String label, boolean on, int mouseX, int mouseY) {
		boolean hover = Ui.inside(mouseX, mouseY, x, y, w, 14);
		Ui.box(x, y, w, 14, on ? 0x559D7BFF : hover ? Ui.CARD_HOVER : Ui.CARD, on ? Ui.BRAND : Ui.BORDER);
		Ui.centered(this.textRenderer, (on ? "§f" : "§7") + label, x + w / 2, y + 3, Ui.TEXT);
	}

	private void showToast(String message) {
		toast = message;
		toastUntil = System.currentTimeMillis() + 2200L;
	}

	// ------------------------------------------------------------------ entrada
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		search.mouseClicked(mouseX, mouseY, button);
		if (button != 0) return;
		pressX = mouseX;
		pressY = mouseY;
		pressInGrid = Ui.inside(mouseX, mouseY, gridX, gridY, gridW, gridH);
		pressInPreview = selected != null && Ui.inside(mouseX, mouseY, bigX, bigY, bigW, bigH);
		draggingGrid = false;
		scrollAtPress = scroll;
		yawAtPress = previewYaw;
		if (pressInGrid || pressInPreview) return; //decide no soltar (toque x arrasto)

		//Fechar
		if (Ui.inside(mouseX, mouseY, px + pw - 20, py + 5, 15, 15)) {
			close();
			return;
		}
		//Ordenar
		int y = py + HEADER_H + 4;
		if (Ui.inside(mouseX, mouseY, gridX + gridW - 34, y - 2, 34, 16)) {
			sortDescending = !sortDescending;
			refresh();
			return;
		}
		//Categorias
		int railX = px + GAP;
		int itemY = y + 5;
		for (Category value : Category.values()) {
			if (Ui.inside(mouseX, mouseY, railX + 3, itemY, RAIL_W - 6, 16)) {
				category = value;
				scroll = 0;
				refresh();
				return;
			}
			itemY += 18;
		}
		//Opções gerais
		ZadrinthConfig config = ZadrinthConfig.get();
		int optionsY = py + ph - GAP - 44;
		if (Ui.inside(mouseX, mouseY, railX + 6, optionsY, RAIL_W - 12, 12)) {
			config.clothCape = !config.clothCape;
			ZadrinthConfig.save();
			return;
		}
		if (Ui.inside(mouseX, mouseY, railX + 6, optionsY + 18, RAIL_W - 12, 12)) {
			config.showOthersCosmetics = !config.showOthersCosmetics;
			ZadrinthConfig.save();
			return;
		}
		clickDetail(mouseX, mouseY);
	}

	private void clickDetail(int mouseX, int mouseY) {
		if (selected == null) return;
		Cosmetic cosmetic = selected;
		boolean owned = ShopService.owns(cosmetic);
		boolean equipped = Wardrobe.isEquipped(cosmetic);
		int x = detailX + 7;
		int textW = detailW - 14;

		if (Ui.inside(mouseX, mouseY, x, buttonY, textW, 16)) {
			if (!owned) {
				ShopService.Result result = ShopService.acquire(cosmetic);
				if (result == ShopService.Result.OK) {
					Wardrobe.equip(cosmetic);
					showToast("§aResgatado e equipado!");
				} else if (result == ShopService.Result.NOT_ENOUGH_COINS) {
					showToast("§cMoedas insuficientes");
				}
			} else if (equipped) {
				Wardrobe.unequip(cosmetic.type);
				showToast("§7Removido");
			} else {
				Wardrobe.equip(cosmetic);
				showToast("§dEquipado!");
			}
			if (category == Category.OWNED) refresh();
			return;
		}
		if (!equipped) return;
		CosmeticStyle style = Wardrobe.local().style(cosmetic.type);
		int sx = x, sy = swatchY;
		for (int i = 0; i < CosmeticStyle.PALETTE.length; i++) {
			if (sx + SWATCH > x + textW) {
				sx = x;
				sy += SWATCH + 3;
			}
			if (Ui.inside(mouseX, mouseY, sx - 1, sy - 1, SWATCH + 2, SWATCH + 2)) {
				style.color = CosmeticStyle.PALETTE[i];
				style.rgb = false;
				Wardrobe.changed();
				return;
			}
			sx += SWATCH + 3;
		}
		int half = (textW - 4) / 2;
		if (Ui.inside(mouseX, mouseY, x, pillsY, half, 14)) {
			style.rgb = !style.rgb;
			Wardrobe.changed();
		} else if (Ui.inside(mouseX, mouseY, x + half + 4, pillsY, half, 14)) {
			style.glint = !style.glint;
			Wardrobe.changed();
		}
	}

	@Override
	protected void mouseDragged(int mouseX, int mouseY, int button, long time) {
		if (pressInGrid) {
			if (!draggingGrid && Math.abs(mouseY - pressY) > 3) draggingGrid = true;
			if (draggingGrid) {
				scroll = scrollAtPress - (mouseY - pressY);
				clampScroll();
			}
		} else if (pressInPreview) {
			if (Math.abs(mouseX - pressX) > 2) userRotated = true;
			previewYaw = yawAtPress - (mouseX - pressX) * 2.0F;
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		if (pressInGrid && !draggingGrid && button == 0) {
			Cosmetic clicked = cardAt(mouseX, mouseY);
			if (clicked != null) select(clicked);
		}
		pressInGrid = false;
		pressInPreview = false;
		draggingGrid = false;
	}

	private Cosmetic cardAt(int mouseX, int mouseY) {
		if (!Ui.inside(mouseX, mouseY, gridX, gridY, gridW, gridH)) return null;
		for (int i = 0; i < visible.size(); i++) {
			int cx = gridX + (i % cols) * (cardW + 4);
			int cy = gridY + (i / cols) * (cardH + 4) - Math.round(scroll);
			if (Ui.inside(mouseX, mouseY, cx, cy, cardW, cardH)) return visible.get(i);
		}
		return null;
	}

	@Override
	public void handleMouse() {
		try {
			super.handleMouse();
		} catch (Exception ignored) {
		}
		int wheel = Mouse.getEventDWheel();
		if (wheel != 0) {
			scroll -= Math.signum(wheel) * (cardH + 4) / 2.0F;
			clampScroll();
		}
	}

	@Override
	protected void keyPressed(char character, int code) {
		if (search.isFocused() && code != 1) {
			search.keyPressed(character, code);
			return;
		}
		if (code == 1 || code == ZadrinthKeys.MENU.getCode()) {
			close();
		}
	}

	@Override
	public void tick() {
		search.tick();
	}

	private void close() {
		this.client.setScreen(parent);
	}

	@Override
	public boolean shouldPauseGame() {
		return false;
	}
}
