package io.github.frostmta.zadrinth.client.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;

/** Cores e desenhos do visual escuro de "vidro" do Zadrinth Client. */
public final class Ui {
	public static final int OVERLAY = 0x99000000;
	public static final int PANEL = 0xE6121019;
	public static final int CARD = 0x14FFFFFF;
	public static final int CARD_HOVER = 0x24FFFFFF;
	public static final int BORDER = 0x2EFFFFFF;
	public static final int TEXT = 0xFFFFFFFF;
	public static final int TEXT_MUTED = 0xFFA1A1AA;
	public static final int BRAND = 0xFF9D7BFF;
	public static final int BRAND_BLUE = 0xFF6E9BFF;
	public static final int BRAND_PINK = 0xFFE07BFF;
	public static final int ENABLED = 0xFF22C55E;
	public static final int DISABLED = 0xFFEF4444;

	private Ui() {
	}

	/** Retângulo com borda de 1 px. */
	public static void box(int x, int y, int w, int h, int fill, int border) {
		DrawableHelper.fill(x, y, x + w, y + h, fill);
		DrawableHelper.fill(x, y, x + w, y + 1, border);
		DrawableHelper.fill(x, y + h - 1, x + w, y + h, border);
		DrawableHelper.fill(x, y + 1, x + 1, y + h - 1, border);
		DrawableHelper.fill(x + w - 1, y + 1, x + w, y + h - 1, border);
	}

	/** Faixa com o degradê da marca (azul → violeta → rosa), feita em colunas. */
	public static void brandBar(int x, int y, int w, int h) {
		for (int i = 0; i < w; i++) {
			float t = w <= 1 ? 0f : (float) i / (w - 1);
			int color = t < 0.5f ? mix(BRAND_BLUE, BRAND, t * 2f) : mix(BRAND, BRAND_PINK, (t - 0.5f) * 2f);
			DrawableHelper.fill(x + i, y, x + i + 1, y + h, color);
		}
	}

	public static int mix(int a, int b, float t) {
		int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
		int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
		int r = Math.round(ar + (br - ar) * t);
		int g = Math.round(ag + (bg - ag) * t);
		int bl = Math.round(ab + (bb - ab) * t);
		return 0xFF000000 | (r << 16) | (g << 8) | bl;
	}

	public static void centered(TextRenderer font, String text, int centerX, int y, int color) {
		font.drawWithShadow(text, centerX - font.getStringWidth(text) / 2f, y, color);
	}

	/** Texto com letras espaçadas, como nos menus do Lunar ("M O D S"). */
	public static String spaced(String text) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (i > 0) builder.append(' ');
			builder.append(text.charAt(i));
		}
		return builder.toString();
	}

	public static boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h;
	}
}
