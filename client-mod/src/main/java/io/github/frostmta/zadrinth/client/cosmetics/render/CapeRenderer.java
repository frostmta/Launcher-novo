package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/**
 * Capa com física de tecido.
 *
 * A capa do Minecraft é uma placa rígida de 10x16x1 que só gira inteira. Aqui
 * ela é dividida em 16 faixas: o topo acompanha o corpo e o resto vai dobrando
 * com o vento (velocidade do jogador) e com uma onda que corre de cima para
 * baixo. Com a física desligada, fica igual à do Minecraft.
 *
 * A textura segue o molde padrão de capa (64x32, ou maior na mesma proporção).
 */
final class CapeRenderer {
	private static final int SEGMENTS = 16;
	private static final float LENGTH = 16.0F;
	private static final float HALF_WIDTH = 5.0F;
	private static final float THICKNESS = 1.0F;

	private CapeRenderer() {
	}

	static void render(AbstractClientPlayerEntity player, Motion motion, AnimState anim, Identifier texture,
					   CosmeticStyle style, boolean cloth) {
		MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 0.125F);
		if (player.getArmorSlot(2) != null) {
			//Afasta um pouco do peitoral para não atravessar a armadura
			GlStateManager.translate(0.0F, -0.02F, 0.0625F);
		}
		float swing = motion.swing;
		float base = 6.0F + motion.lift + (cloth ? swing * 0.35F : swing);
		GlStateManager.rotate(base, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(motion.side / 2.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(-motion.side / 2.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.scale(0.0625F, 0.0625F, 0.0625F);

		final float[] angles = new float[SEGMENTS + 1];
		if (cloth) {
			float bend = swing * 0.65F;
			float amplitude = 2.5F + 11.0F * anim.speed;
			for (int k = 0; k <= SEGMENTS; k++) {
				float t = (float) k / SEGMENTS;
				float wave = amplitude * t * (float) Math.sin(anim.capePhase - t * 3.4F);
				float angle = bend * (float) Math.pow(t, 1.4) + wave;
				//Nunca entra no corpo do jogador
				angles[k] = Math.max(angle, 3.0F - base);
			}
		}

		Glint.tint(style);
		draw(angles);
		if (style != null && style.glint) Glint.render(new Runnable() {
			@Override
			public void run() {
				draw(angles);
			}
		});
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	/** angles[k] = inclinação extra (graus) em cada "junta" da capa, de cima (0) para baixo. */
	private static void draw(float[] angles) {
		float[] jy = new float[SEGMENTS + 1];
		float[] jz = new float[SEGMENTS + 1];
		float[] ny = new float[SEGMENTS + 1];
		float[] nz = new float[SEGMENTS + 1];
		float step = LENGTH / SEGMENTS;
		for (int k = 0; k <= SEGMENTS; k++) {
			float a = (float) Math.toRadians(angles[k]);
			ny[k] = -(float) Math.sin(a);
			nz[k] = (float) Math.cos(a);
			if (k < SEGMENTS) {
				float mid = (float) Math.toRadians((angles[k] + angles[k + 1]) * 0.5F);
				jy[k + 1] = jy[k] + step * (float) Math.cos(mid);
				jz[k + 1] = jz[k] + step * (float) Math.sin(mid);
			}
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(7, VertexFormats.POSITION_TEXTURE_NORMAL);
		float x0 = HALF_WIDTH, x1 = -HALF_WIDTH;
		for (int k = 0; k < SEGMENTS; k++) {
			float v0 = (1.0F + LENGTH * k / SEGMENTS) / 32.0F;
			float v1 = (1.0F + LENGTH * (k + 1) / SEGMENTS) / 32.0F;
			float oy0 = jy[k] + ny[k] * THICKNESS, oz0 = jz[k] + nz[k] * THICKNESS;
			float oy1 = jy[k + 1] + ny[k + 1] * THICKNESS, oz1 = jz[k + 1] + nz[k + 1] * THICKNESS;
			float mid = (float) Math.toRadians((angles[k] + angles[k + 1]) * 0.5F);
			float fny = -(float) Math.sin(mid), fnz = (float) Math.cos(mid);

			//Lado de fora (a arte)
			vertex(buffer, x0, oy0, oz0, 1, v0, 0, fny, fnz);
			vertex(buffer, x1, oy0, oz0, 11, v0, 0, fny, fnz);
			vertex(buffer, x1, oy1, oz1, 11, v1, 0, fny, fnz);
			vertex(buffer, x0, oy1, oz1, 1, v1, 0, fny, fnz);
			//Lado de dentro (forro)
			vertex(buffer, x0, jy[k], jz[k], 22, v0, 0, -fny, -fnz);
			vertex(buffer, x1, jy[k], jz[k], 12, v0, 0, -fny, -fnz);
			vertex(buffer, x1, jy[k + 1], jz[k + 1], 12, v1, 0, -fny, -fnz);
			vertex(buffer, x0, jy[k + 1], jz[k + 1], 22, v1, 0, -fny, -fnz);
			//Bordas laterais
			vertex(buffer, x0, jy[k], jz[k], 0, v0, 1, 0, 0);
			vertex(buffer, x0, oy0, oz0, 1, v0, 1, 0, 0);
			vertex(buffer, x0, oy1, oz1, 1, v1, 1, 0, 0);
			vertex(buffer, x0, jy[k + 1], jz[k + 1], 0, v1, 1, 0, 0);
			vertex(buffer, x1, jy[k], jz[k], 12, v0, -1, 0, 0);
			vertex(buffer, x1, oy0, oz0, 11, v0, -1, 0, 0);
			vertex(buffer, x1, oy1, oz1, 11, v1, -1, 0, 0);
			vertex(buffer, x1, jy[k + 1], jz[k + 1], 12, v1, -1, 0, 0);
		}
		//Topo e barra
		float topY = jy[0] + ny[0] * THICKNESS, topZ = jz[0] + nz[0] * THICKNESS;
		vertex(buffer, x1, jy[0], jz[0], 11, 0, 0, -1, 0);
		vertex(buffer, x0, jy[0], jz[0], 1, 0, 0, -1, 0);
		vertex(buffer, x0, topY, topZ, 1, 1 / 32.0F, 0, -1, 0);
		vertex(buffer, x1, topY, topZ, 11, 1 / 32.0F, 0, -1, 0);
		int n = SEGMENTS;
		float botY = jy[n] + ny[n] * THICKNESS, botZ = jz[n] + nz[n] * THICKNESS;
		vertex(buffer, x1, botY, botZ, 21, 1 / 32.0F, 0, 1, 0);
		vertex(buffer, x0, botY, botZ, 11, 1 / 32.0F, 0, 1, 0);
		vertex(buffer, x0, jy[n], jz[n], 11, 0, 0, 1, 0);
		vertex(buffer, x1, jy[n], jz[n], 21, 0, 0, 1, 0);
		tessellator.draw();
	}

	/** u em pixels do molde (0..64); v já normalizado. */
	private static void vertex(BufferBuilder buffer, float x, float y, float z, float u, float v,
							   float nx, float ny, float nz) {
		buffer.vertex(x, y, z).texture(u / 64.0F, v).normal(nx, ny, nz).next();
	}
}
