package io.github.frostmta.zadrinth.client.cosmetics;

/** Opções visuais de um cosmético equipado. */
public final class CosmeticStyle {
	/** Cores da paleta (RGB). -1 = cores originais da arte. */
	public static final int[] PALETTE = {
			-1, 0xFF4B4B, 0xFF9F2E, 0xFFD84A, 0x3BE07A, 0x34D7E6, 0x5B8CFF, 0x9D7BFF, 0xFF6AD5, 0xF2F2F2, 0x2A2A2A
	};

	/** Cor escolhida (RGB) ou -1 para as cores originais */
	public int color = -1;
	/** Arco-íris animado */
	public boolean rgb = false;
	/** Brilho de encantamento por cima */
	public boolean glint = false;

	public CosmeticStyle copy() {
		CosmeticStyle style = new CosmeticStyle();
		style.color = color;
		style.rgb = rgb;
		style.glint = glint;
		return style;
	}

	/** Usa a versão em tons de cinza da textura (tingida pela cor)? */
	public boolean tinted() {
		return rgb || color >= 0;
	}

	/** Cor atual (RGB) — muda com o tempo no modo RGB. */
	public int currentColor(float seconds) {
		if (rgb) return hsv((seconds * 0.25f) % 1f, 0.75f, 1f);
		return color < 0 ? 0xFFFFFF : color & 0xFFFFFF;
	}

	/** HSV (0..1) → RGB. */
	public static int hsv(float h, float s, float v) {
		float hh = (h - (float) Math.floor(h)) * 6f;
		int sector = (int) hh;
		float f = hh - sector;
		float p = v * (1f - s);
		float q = v * (1f - s * f);
		float t = v * (1f - s * (1f - f));
		float r, g, b;
		switch (sector) {
			case 0: r = v; g = t; b = p; break;
			case 1: r = q; g = v; b = p; break;
			case 2: r = p; g = v; b = t; break;
			case 3: r = p; g = q; b = v; break;
			case 4: r = t; g = p; b = v; break;
			default: r = v; g = p; b = q; break;
		}
		return ((int) (r * 255f + 0.5f) << 16) | ((int) (g * 255f + 0.5f) << 8) | (int) (b * 255f + 0.5f);
	}
}
