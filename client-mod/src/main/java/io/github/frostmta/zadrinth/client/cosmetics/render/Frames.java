package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;

/** Cosméticos animados: escolhe o quadro certo pela matriz de textura. */
final class Frames {
	private Frames() {
	}

	static void begin(Cosmetic cosmetic) {
		if (cosmetic == null || cosmetic.frames <= 1) return;
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.translate(0.0F, cosmetic.frame() / (float) cosmetic.frames, 0.0F);
		GlStateManager.scale(1.0F, 1.0F / cosmetic.frames, 1.0F);
		GlStateManager.matrixMode(5888);
	}

	static void end(Cosmetic cosmetic) {
		if (cosmetic == null || cosmetic.frames <= 1) return;
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
	}
}
