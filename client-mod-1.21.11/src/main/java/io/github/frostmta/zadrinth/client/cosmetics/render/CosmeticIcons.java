package io.github.frostmta.zadrinth.client.cosmetics.render;

import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticType;
import io.github.frostmta.zadrinth.client.gui.Ui;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;

/**
 * Desenho 2D de um cosmético (sem jogador 3D), usado na loja quando ainda
 * não há mundo carregado, como na tela inicial.
 */
public final class CosmeticIcons {
	private CosmeticIcons() {
	}

	/** Desenha centralizado dentro do retângulo (x, y, w, h). */
	public static void draw(Cosmetic cosmetic, int x, int y, int w, int h) {
		Identifier texture = CosmeticTextures.get(cosmetic, false);
		if (texture == null) return;
		float f = cosmetic.frames > 1 ? 1.0F / cosmetic.frames : 1.0F;
		float frameV = cosmetic.frame() * f;
		if (cosmetic.type == CosmeticType.CAPE) {
			int ch = Math.round(h * 0.85F), cw = ch * 10 / 16;
			if (cw > w * 0.8F) {
				cw = Math.round(w * 0.8F);
				ch = cw * 16 / 10;
			}
			int cx = x + (w - cw) / 2, cy = y + (h - ch) / 2;
			quad(texture, cx, cy, cx + cw, cy + ch, 1 / 64.0F, 11 / 64.0F, frameV + f / 32.0F, frameV + f * 17 / 32.0F);
		} else if (cosmetic.type == CosmeticType.WINGS) {
			int size = Math.min(w / 2, h);
			int cx = x + w / 2, cy = y + (h - size) / 2;
			quad(texture, cx, cy, cx + size, cy + size, 0, 1, 0, 1);
			quad(texture, cx - size, cy, cx, cy + size, 1, 0, 0, 1);
		} else {
			int size = Math.min(w, h) * 4 / 5;
			int hx = x + (w - size) / 2, hy = y + (h - size) / 2;
			quad(DefaultPlayerSkin.getDefaultTexture(), hx, hy, hx + size, hy + size, 8 / 64.0F, 16 / 64.0F, 8 / 64.0F, 16 / 64.0F);
			int top = hy + Math.round(size * 1.1F / 8), bottom = hy + Math.round(size * 3.7F / 8);
			quad(texture, hx - 1, top, hx + size + 1, bottom, 0, 0.25F, frameV, frameV + 0.375F * f);
		}
	}

	private static void quad(Identifier texture, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1) {
		Ui.g().blit(texture, x0, y0, x1, y1, u0, u1, v0, v1);
	}
}
