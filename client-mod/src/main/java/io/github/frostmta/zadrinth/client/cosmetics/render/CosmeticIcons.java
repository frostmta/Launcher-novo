package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/**
 * Desenho 2D de um cosmético (sem jogador 3D), usado na loja quando ainda
 * não há mundo carregado, como na tela inicial.
 */
public final class CosmeticIcons {
	private static final Identifier STEVE = new Identifier("textures/entity/steve.png");

	private CosmeticIcons() {
	}

	/** Desenha centralizado dentro do retângulo (x, y, w, h). */
	public static void draw(Cosmetic cosmetic, int x, int y, int w, int h) {
		Identifier texture = CosmeticTextures.get(cosmetic, false);
		if (texture == null) return;
		MinecraftClient client = MinecraftClient.getInstance();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableAlphaTest();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float f = cosmetic.frames > 1 ? 1.0F / cosmetic.frames : 1.0F;
		float frameV = cosmetic.frame() * f;
		if (cosmetic.type == CosmeticType.CAPE) {
			int ch = Math.round(h * 0.85F), cw = ch * 10 / 16;
			if (cw > w * 0.8F) {
				cw = Math.round(w * 0.8F);
				ch = cw * 16 / 10;
			}
			int cx = x + (w - cw) / 2, cy = y + (h - ch) / 2;
			client.getTextureManager().bindTexture(texture);
			quad(cx, cy, cx + cw, cy + ch, 1 / 64.0F, 1 / 32.0F, 11 / 64.0F, 17 / 32.0F);
		} else if (cosmetic.type == CosmeticType.WINGS) {
			int size = Math.min(w / 2, h);
			int cx = x + w / 2, cy = y + (h - size) / 2;
			client.getTextureManager().bindTexture(texture);
			quad(cx, cy, cx + size, cy + size, 0, 0, 1, 1);
			quad(cx - size, cy, cx, cy + size, 1, 0, 0, 1);
		} else {
			int size = Math.min(w, h) * 4 / 5;
			int hx = x + (w - size) / 2, hy = y + (h - size) / 2;
			client.getTextureManager().bindTexture(STEVE);
			quad(hx, hy, hx + size, hy + size, 8 / 64.0F, 8 / 64.0F, 16 / 64.0F, 16 / 64.0F);
			client.getTextureManager().bindTexture(texture);
			int top = hy + Math.round(size * 1.1F / 8), bottom = hy + Math.round(size * 3.7F / 8);
			quad(hx - 1, top, hx + size + 1, bottom, 0, frameV, 0.25F, frameV + 0.375F * f);
		}
		GlStateManager.disableBlend();
	}

	private static void quad(int x0, int y0, int x1, int y1, float u0, float v0, float u1, float v1) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(7, VertexFormats.POSITION_TEXTURE);
		buffer.vertex(x0, y1, 0).texture(u0, v1).next();
		buffer.vertex(x1, y1, 0).texture(u1, v1).next();
		buffer.vertex(x1, y0, 0).texture(u1, v0).next();
		buffer.vertex(x0, y0, 0).texture(u0, v0).next();
		tessellator.draw();
	}
}
