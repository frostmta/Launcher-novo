package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.module.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Cores e desenhos do visual escuro de "vidro" do Zadrinth Client.
 *
 * No 1.21.11 tudo na tela passa por um GuiGraphics: as telas e o HUD chamam
 * {@link #begin(GuiGraphics)} antes de desenhar e os métodos daqui usam ele.
 */
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

	public static final Identifier ICONS = ZadrinthClient.id("textures/icons.png");
	public static final Identifier LOGO = ZadrinthClient.id("textures/logo.png");

	private static GuiGraphics graphics;

	private Ui() {
	}

	public static void begin(GuiGraphics g) {
		graphics = g;
	}

	public static GuiGraphics g() {
		return graphics;
	}

	public static Font font() {
		return Minecraft.getInstance().font;
	}

	// ------------------------------------------------------------------ básico
	public static void fill(int x1, int y1, int x2, int y2, int color) {
		graphics.fill(x1, y1, x2, y2, color);
	}

	public static void gradient(int x1, int y1, int x2, int y2, int top, int bottom) {
		graphics.fillGradient(x1, y1, x2, y2, top, bottom);
	}

	public static int width(String text) {
		return font().width(text);
	}

	/** Texto com sombra (aceita os códigos de cor com §). */
	public static void text(String text, int x, int y, int color) {
		graphics.drawString(font(), text, x, y, opaque(color), true);
	}

	public static void text(String text, int x, int y, int color, boolean shadow) {
		graphics.drawString(font(), text, x, y, opaque(color), shadow);
	}

	/** Texto em posição fracionária (ex.: centralizado em meio pixel). */
	public static void text(String text, float x, float y, int color, boolean shadow) {
		int ix = (int) Math.floor(x), iy = (int) Math.floor(y);
		if (x == ix && y == iy) {
			text(text, ix, iy, color, shadow);
			return;
		}
		push();
		translate(x, y);
		text(text, 0, 0, color, shadow);
		pop();
	}

	/** No 1.21 texto com alfa 0 some: quem passa só RGB ganha alfa cheio. */
	private static int opaque(int color) {
		return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
	}

	public static void centered(String text, int centerX, int y, int color) {
		text(text, centerX - width(text) / 2, y, color);
	}

	public static String trim(String text, int maxWidth) {
		return font().plainSubstrByWidth(text, maxWidth);
	}

	public static void push() {
		graphics.pose().pushMatrix();
	}

	public static void pop() {
		graphics.pose().popMatrix();
	}

	public static void translate(float x, float y) {
		graphics.pose().translate(x, y);
	}

	public static void scale(float s) {
		graphics.pose().scale(s, s);
	}

	/** Recorta o que for desenhado a seguir a um retângulo da tela. */
	public static void clip(int x, int y, int w, int h) {
		graphics.enableScissor(x, y, x + Math.max(0, w), y + Math.max(0, h));
	}

	public static void endClip() {
		graphics.disableScissor();
	}

	// ------------------------------------------------------------------ visual do client
	/** Retângulo com borda de 1 px. */
	public static void box(int x, int y, int w, int h, int fill, int border) {
		fill(x, y, x + w, y + h, fill);
		fill(x, y, x + w, y + 1, border);
		fill(x, y + h - 1, x + w, y + h, border);
		fill(x, y + 1, x + 1, y + h - 1, border);
		fill(x + w - 1, y + 1, x + w, y + h - 1, border);
	}

	/** Faixa com o degradê da marca (azul → violeta → rosa), feita em colunas. */
	public static void brandBar(int x, int y, int w, int h) {
		for (int i = 0; i < w; i++) {
			float t = w <= 1 ? 0f : (float) i / (w - 1);
			int color = t < 0.5f ? mix(BRAND_BLUE, BRAND, t * 2f) : mix(BRAND, BRAND_PINK, (t - 0.5f) * 2f);
			fill(x + i, y, x + i + 1, y + h, color);
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

	/** Mistura com alfa também. */
	public static int mixArgb(int a, int b, float t) {
		t = Math.max(0, Math.min(1, t));
		int aa = a >>> 24, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
		int ba = b >>> 24, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
		return (Math.round(aa + (ba - aa) * t) << 24) | (Math.round(ar + (br - ar) * t) << 16)
				| (Math.round(ag + (bg - ag) * t) << 8) | Math.round(ab + (bb - ab) * t);
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

	/** Retângulo com cantos arredondados (raio de 2 px, estilo pixel). */
	public static void round(int x, int y, int w, int h, int color) {
		if (w < 4 || h < 4) {
			fill(x, y, x + w, y + h, color);
			return;
		}
		fill(x + 2, y, x + w - 2, y + 1, color);
		fill(x + 1, y + 1, x + w - 1, y + 2, color);
		fill(x, y + 2, x + w, y + h - 2, color);
		fill(x + 1, y + h - 2, x + w - 1, y + h - 1, color);
		fill(x + 2, y + h - 1, x + w - 2, y + h, color);
	}

	/** Contorno de 1 px com cantos arredondados. */
	public static void roundOutline(int x, int y, int w, int h, int color) {
		fill(x + 2, y, x + w - 2, y + 1, color);
		fill(x + 2, y + h - 1, x + w - 2, y + h, color);
		fill(x, y + 2, x + 1, y + h - 2, color);
		fill(x + w - 1, y + 2, x + w, y + h - 2, color);
		fill(x + 1, y + 1, x + 2, y + 2, color);
		fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
		fill(x + 1, y + h - 2, x + 2, y + h - 1, color);
		fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
	}

	/** Cartão: fundo + contorno arredondados. */
	public static void card(int x, int y, int w, int h, int fill, int outline) {
		fill(x + 2, y + 1, x + w - 2, y + 2, fill);
		fill(x + 1, y + 2, x + w - 1, y + h - 2, fill);
		fill(x + 2, y + h - 2, x + w - 2, y + h - 1, fill);
		roundOutline(x, y, w, h, outline);
	}

	/** Ícone branco do atlas, tingido com a cor. */
	public static void icon(Icon icon, int x, int y, int size, int argb) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, x, y, (float) icon.u(), (float) icon.v(), size, size, 32, 32, 256, 256, argb);
	}

	/** O símbolo do Zadrinth Client. */
	public static void logo(int x, int y, int size) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO, x, y, 0f, 0f, size, size, 256, 256, 256, 256);
	}

	/** Uma imagem inteira esticada num retângulo. */
	public static void image(Identifier texture, int x, int y, int w, int h, int texW, int texH) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0f, 0f, w, h, texW, texH, texW, texH);
	}

	public static boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h;
	}
}
