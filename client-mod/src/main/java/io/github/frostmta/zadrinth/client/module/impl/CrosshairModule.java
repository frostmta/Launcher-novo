package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ColorSetting;
import io.github.frostmta.zadrinth.client.module.setting.ModeSetting;
import io.github.frostmta.zadrinth.client.module.setting.NumberSetting;
import net.minecraft.client.gui.DrawableHelper;

/** Mira personalizada (a do jogo é escondida pelo InGameHudMixin). */
public final class CrosshairModule extends Module {
	private static CrosshairModule instance;
	/** O jogo ia mostrar a mira neste quadro? (falso em 3ª pessoa, por exemplo) */
	public static boolean vanillaVisible;

	private final ModeSetting style = add(new ModeSetting("style", "Estilo", 0, "Cruz", "Cruz com ponto", "Ponto", "Círculo", "Seta"));
	private final ColorSetting color = add(new ColorSetting("color", "Cor", 0xFFFFFFFF, false));
	private final NumberSetting size = add(new NumberSetting("size", "Tamanho", 5, 2, 12, 1, ""));
	private final NumberSetting gap = add(new NumberSetting("gap", "Espaço no meio", 2, 0, 6, 1, ""));
	private final NumberSetting thickness = add(new NumberSetting("thickness", "Espessura", 1, 1, 3, 1, ""));
	private final BoolSetting outline = add(new BoolSetting("outline", "Contorno escuro", true));

	public CrosshairModule() {
		super("crosshair", "Mira", "Mude o formato, a cor e o tamanho da mira.", Category.VISUAL, Icon.CROSSHAIR, false);
		instance = this;
	}

	public static boolean active() {
		return instance != null && instance.isEnabled();
	}

	/** Desenha no centro da tela (coordenadas da interface). */
	public static void render(int screenW, int screenH) {
		CrosshairModule m = instance;
		if (m == null || !m.isEnabled() || !vanillaVisible) return;
		int cx = screenW / 2, cy = screenH / 2;
		int s = m.size.intValue(), g = m.gap.intValue(), t = m.thickness.intValue();
		int c = m.color.argb();
		int o = 0xC0000000;
		int half = t / 2;
		switch (m.style.index()) {
			case 2:
				dot(cx, cy, t + 1, c, m.outline.on());
				break;
			case 3:
				ring(cx, cy, s, c, m.outline.on());
				break;
			case 4:
				for (int i = 0; i < s; i++) {
					rect(cx - i, cy + i, cx - i + t, cy + i + t, c, m.outline.on(), o);
					rect(cx + i, cy + i, cx + i + t, cy + i + t, c, m.outline.on(), o);
				}
				break;
			default:
				rect(cx - half, cy - g - s, cx - half + t, cy - g, c, m.outline.on(), o);
				rect(cx - half, cy + g + 1, cx - half + t, cy + g + 1 + s, c, m.outline.on(), o);
				rect(cx - g - s, cy - half, cx - g, cy - half + t, c, m.outline.on(), o);
				rect(cx + g + 1, cy - half, cx + g + 1 + s, cy - half + t, c, m.outline.on(), o);
				if (m.style.index() == 1) dot(cx, cy, t, c, m.outline.on());
				break;
		}
	}

	private static void rect(int x0, int y0, int x1, int y1, int color, boolean outline, int outlineColor) {
		if (outline) DrawableHelper.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, outlineColor);
		DrawableHelper.fill(x0, y0, x1, y1, color);
	}

	private static void dot(int cx, int cy, int size, int color, boolean outline) {
		int h = size / 2;
		rect(cx - h, cy - h, cx - h + Math.max(1, size), cy - h + Math.max(1, size), color, outline, 0xC0000000);
	}

	private static void ring(int cx, int cy, int r, int color, boolean outline) {
		for (int i = 0; i < 48; i++) {
			double a = i * Math.PI * 2 / 48;
			int x = cx + (int) Math.round(Math.cos(a) * r);
			int y = cy + (int) Math.round(Math.sin(a) * r);
			if (outline) DrawableHelper.fill(x - 1, y - 1, x + 2, y + 2, 0x60000000);
			DrawableHelper.fill(x, y, x + 1, y + 1, color);
		}
	}
}
