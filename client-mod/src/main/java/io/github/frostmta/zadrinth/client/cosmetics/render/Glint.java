package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

/** Cor e brilho de encantamento (o mesmo efeito das armaduras encantadas). */
final class Glint {
	private static final Identifier TEXTURE = new Identifier("textures/misc/enchanted_item_glint.png");

	private Glint() {
	}

	static float seconds() {
		return (System.currentTimeMillis() % 3_600_000L) / 1000.0F;
	}

	/** Aplica a cor do estilo (branco = cores originais). */
	static void tint(CosmeticStyle style) {
		int rgb = style == null ? 0xFFFFFF : style.currentColor(seconds());
		GlStateManager.color(((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, 1.0F);
	}

	/** Desenha de novo a mesma geometria com o brilho por cima. */
	static void render(Runnable draw) {
		float time = (System.currentTimeMillis() % 100_000L) / 50.0F;
		MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
		GlStateManager.enableBlend();
		GlStateManager.depthFunc(514);
		GlStateManager.depthMask(false);
		for (int i = 0; i < 2; ++i) {
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(768, 1);
			GlStateManager.color(0.5F * 0.76F, 0.25F * 0.76F, 0.8F * 0.76F, 1.0F);
			GlStateManager.matrixMode(5890);
			GlStateManager.loadIdentity();
			GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
			GlStateManager.rotate(30.0F - (float) i * 60.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0.0F, time * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);
			GlStateManager.matrixMode(5888);
			draw.run();
		}
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.depthFunc(515);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
