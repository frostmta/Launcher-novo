package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/**
 * Bandana: uma faixa em volta da testa (por fora do chapéu da skin), com um nó
 * atrás e duas pontas que balançam com o vento.
 *
 * Textura 288x64: a faixa ocupa as 24 linhas de cima (frente, lado, trás,
 * lado) e as pontas usam a parte de baixo.
 */
final class BandanaRenderer {
	private static final float R = 4.62F;
	private static final float TOP = -6.9F;
	private static final float BOTTOM = -4.3F;
	private static final float BAND_V = 24.0F / 64.0F;
	private static final int TAIL_SEGMENTS = 3;
	private static final float TAIL_LENGTH = 7.0F;
	private static final float TAIL_HALF_WIDTH = 0.9F;

	private BandanaRenderer() {
	}

	static void render(AbstractClientPlayerEntity player, PlayerEntityModel model, Motion motion, AnimState anim,
					   Identifier texture, CosmeticStyle style) {
		MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
		GlStateManager.pushMatrix();
		if (player.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);
		model.head.preRender(0.0625F);
		GlStateManager.scale(0.0625F, 0.0625F, 0.0625F);

		//As pontas caem "para baixo" de verdade: desconta a inclinação da cabeça
		float headPitch = (float) Math.toDegrees(model.head.posX);
		final float[] tailAngles = new float[TAIL_SEGMENTS];
		final float[] tailAngles2 = new float[TAIL_SEGMENTS];
		float wind = 16.0F + motion.swing * 0.9F + Math.max(0.0F, -motion.lift) * 0.5F;
		float amplitude = 4.0F + 10.0F * anim.speed;
		for (int j = 0; j < TAIL_SEGMENTS; j++) {
			tailAngles[j] = wind + j * 6.0F + amplitude * (float) Math.sin(anim.tailPhase + j * 0.9F) - headPitch;
			tailAngles2[j] = wind + j * 6.0F + amplitude * (float) Math.sin(anim.tailPhase + 1.3F + j * 0.9F) - headPitch;
		}

		Glint.tint(style);
		draw(tailAngles, tailAngles2);
		if (style != null && style.glint) Glint.render(new Runnable() {
			@Override
			public void run() {
				draw(tailAngles, tailAngles2);
			}
		});
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	private static void draw(float[] tail1, float[] tail2) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(7, VertexFormats.POSITION_TEXTURE_NORMAL);
		//Faixa: frente (z = -R), lado +x, trás (z = +R), lado -x — a textura dá a volta
		quad(buffer, -R, -R, R, -R, 0.00F, 0.25F, 0, 0, -1);
		quad(buffer, R, -R, R, R, 0.25F, 0.50F, 1, 0, 0);
		quad(buffer, R, R, -R, R, 0.50F, 0.75F, 0, 0, 1);
		quad(buffer, -R, R, -R, -R, 0.75F, 1.00F, -1, 0, 0);

		//Nó atrás da cabeça
		float kz0 = R, kz1 = R + 1.3F, ky0 = -6.5F, ky1 = -4.6F, kx = 1.1F;
		float ku0 = 0.60F, ku1 = 0.64F;
		box(buffer, -kx, ky0, kz0, kx, ky1, kz1, ku0, ku1);

		//Pontas
		tail(buffer, tail1, -0.55F, -0.25F, 0.03F, 0.13F);
		tail(buffer, tail2, 0.55F, 0.25F, 0.20F, 0.30F);
		tessellator.draw();
	}

	private static void quad(BufferBuilder buffer, float xa, float za, float xb, float zb, float u0, float u1,
							 float nx, float ny, float nz) {
		buffer.vertex(xa, TOP, za).texture(u0, 0).normal(nx, ny, nz).next();
		buffer.vertex(xb, TOP, zb).texture(u1, 0).normal(nx, ny, nz).next();
		buffer.vertex(xb, BOTTOM, zb).texture(u1, BAND_V).normal(nx, ny, nz).next();
		buffer.vertex(xa, BOTTOM, za).texture(u0, BAND_V).normal(nx, ny, nz).next();
	}

	private static void box(BufferBuilder b, float x0, float y0, float z0, float x1, float y1, float z1,
							float u0, float u1) {
		float v0 = 0.05F, v1 = BAND_V - 0.05F;
		//trás
		b.vertex(x0, y0, z1).texture(u0, v0).normal(0, 0, 1).next();
		b.vertex(x1, y0, z1).texture(u1, v0).normal(0, 0, 1).next();
		b.vertex(x1, y1, z1).texture(u1, v1).normal(0, 0, 1).next();
		b.vertex(x0, y1, z1).texture(u0, v1).normal(0, 0, 1).next();
		//lados
		b.vertex(x1, y0, z0).texture(u0, v0).normal(1, 0, 0).next();
		b.vertex(x1, y0, z1).texture(u1, v0).normal(1, 0, 0).next();
		b.vertex(x1, y1, z1).texture(u1, v1).normal(1, 0, 0).next();
		b.vertex(x1, y1, z0).texture(u0, v1).normal(1, 0, 0).next();
		b.vertex(x0, y0, z1).texture(u0, v0).normal(-1, 0, 0).next();
		b.vertex(x0, y0, z0).texture(u1, v0).normal(-1, 0, 0).next();
		b.vertex(x0, y1, z0).texture(u1, v1).normal(-1, 0, 0).next();
		b.vertex(x0, y1, z1).texture(u0, v1).normal(-1, 0, 0).next();
		//cima e baixo
		b.vertex(x0, y0, z0).texture(u0, v0).normal(0, -1, 0).next();
		b.vertex(x1, y0, z0).texture(u1, v0).normal(0, -1, 0).next();
		b.vertex(x1, y0, z1).texture(u1, v1).normal(0, -1, 0).next();
		b.vertex(x0, y0, z1).texture(u0, v1).normal(0, -1, 0).next();
		b.vertex(x0, y1, z1).texture(u0, v0).normal(0, 1, 0).next();
		b.vertex(x1, y1, z1).texture(u1, v0).normal(0, 1, 0).next();
		b.vertex(x1, y1, z0).texture(u1, v1).normal(0, 1, 0).next();
		b.vertex(x0, y1, z0).texture(u0, v1).normal(0, 1, 0).next();
	}

	/** Uma ponta: fita em segmentos, presa no nó e caindo para baixo/trás. */
	private static void tail(BufferBuilder b, float[] angles, float startX, float splay, float u0, float u1) {
		float x = startX, y = -5.0F, z = R + 1.1F;
		float segment = TAIL_LENGTH / TAIL_SEGMENTS;
		float vStep = (1.0F - BAND_V) / TAIL_SEGMENTS;
		for (int j = 0; j < TAIL_SEGMENTS; j++) {
			float a = (float) Math.toRadians(angles[j]);
			float dy = (float) Math.cos(a), dz = (float) Math.sin(a);
			float nx = x + splay * segment * 0.35F;
			float ny = y + dy * segment;
			float nz = z + dz * segment;
			float v0 = BAND_V + vStep * j, v1 = BAND_V + vStep * (j + 1);
			float normalY = -dz, normalZ = dy;
			b.vertex(x - TAIL_HALF_WIDTH, y, z).texture(u0, v0).normal(0, normalY, normalZ).next();
			b.vertex(x + TAIL_HALF_WIDTH, y, z).texture(u1, v0).normal(0, normalY, normalZ).next();
			b.vertex(nx + TAIL_HALF_WIDTH, ny, nz).texture(u1, v1).normal(0, normalY, normalZ).next();
			b.vertex(nx - TAIL_HALF_WIDTH, ny, nz).texture(u0, v1).normal(0, normalY, normalZ).next();
			x = nx;
			y = ny;
			z = nz;
		}
	}
}
