package io.github.frostmta.zadrinth.client.module.hud;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;

import java.util.List;

/**
 * Módulo de HUD feito de linhas de texto dentro de uma caixinha (FPS, CPS,
 * Ping...). Todos ganham as mesmas opções de aparência.
 */
public abstract class TextHudModule extends HudModule {
	protected static final int PAD_X = 5;
	protected static final int PAD_Y = 4;
	protected static final int LINE_GAP = 2;

	protected final BoolSetting background = add(new BoolSetting("background", "Fundo", defaultBackground()));
	protected final ColorSetting backgroundColor = add(new ColorSetting("backgroundColor", "Cor do fundo", 0x6F000000, true))
			.visibleWhen(() -> this.background.on());
	protected final BoolSetting border = add(new BoolSetting("border", "Borda", false));
	protected final ColorSetting borderColor = add(new ColorSetting("borderColor", "Cor da borda", 0xCCFFFFFF, true))
			.visibleWhen(() -> this.border.on());
	protected final ColorSetting textColor = add(new ColorSetting("textColor", "Cor do texto", 0xFFFFFFFF, false));
	protected final BoolSetting shadow = add(new BoolSetting("shadow", "Sombra no texto", true));
	protected final BoolSetting brackets = add(new BoolSetting("brackets", "Colchetes [ ]", false));

	protected TextHudModule(String id, String name, String description, Category category, Icon icon,
							boolean defaultEnabled, float x, float y) {
		super(id, name, description, category, icon, defaultEnabled, x, y);
	}

	/** As linhas a mostrar (no editor, mostre um exemplo se não houver nada). */
	protected abstract List<String> lines(MinecraftClient client, boolean editing);

	private List<String> shown(MinecraftClient client, boolean editing) {
		List<String> lines = lines(client, editing);
		if (brackets.on()) {
			for (int i = 0; i < lines.size(); i++) lines.set(i, "[" + lines.get(i) + "]");
		}
		return lines;
	}

	@Override
	public boolean shouldRender(MinecraftClient client) {
		return !lines(client, false).isEmpty();
	}

	@Override
	public int width(MinecraftClient client, boolean editing) {
		int max = 0;
		for (String line : shown(client, editing)) max = Math.max(max, client.textRenderer.getStringWidth(line));
		return max + PAD_X * 2;
	}

	@Override
	public int height(MinecraftClient client, boolean editing) {
		int count = Math.max(1, shown(client, editing).size());
		return count * client.textRenderer.fontHeight + (count - 1) * LINE_GAP + PAD_Y * 2 - 1;
	}

	@Override
	public void render(MinecraftClient client, boolean editing) {
		List<String> lines = shown(client, editing);
		int w = width(client, editing), h = height(client, editing);
		drawFrame(w, h);
		TextRenderer text = client.textRenderer;
		int y = PAD_Y;
		for (String line : lines) {
			int x = centered() ? (w - text.getStringWidth(line)) / 2 : PAD_X;
			drawText(text, line, x, y);
			y += text.fontHeight + LINE_GAP;
		}
	}

	/** Começa com fundo? (chamado antes do construtor da subclasse: só devolva uma constante) */
	protected boolean defaultBackground() {
		return true;
	}

	/** Texto centralizado na caixa (falso = alinhado à esquerda). */
	protected boolean centered() {
		return true;
	}

	protected void drawFrame(int w, int h) {
		if (background.on()) DrawableHelper.fill(0, 0, w, h, backgroundColor.argb());
		if (border.on()) {
			int c = borderColor.argb();
			DrawableHelper.fill(0, 0, w, 1, c);
			DrawableHelper.fill(0, h - 1, w, h, c);
			DrawableHelper.fill(0, 1, 1, h - 1, c);
			DrawableHelper.fill(w - 1, 1, w, h - 1, c);
		}
	}

	protected void drawText(TextRenderer text, String line, int x, int y) {
		int color = textColor.argb(x * 0.003F);
		if (shadow.on()) text.drawWithShadow(line, (float) x, (float) y, color);
		else text.draw(line, x, y, color);
	}
}
