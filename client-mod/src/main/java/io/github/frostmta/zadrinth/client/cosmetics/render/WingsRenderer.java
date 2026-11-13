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
 * Asas presas nas costas. Cada asa tem duas partes com uma "dobra" no meio
 * (como o punho de uma asa de verdade), então ela bate e a ponta acompanha com
 * um pequeno atraso. Bate devagar parado, mais rápido correndo e forte no ar;
 * agachado, as asas se recolhem.
 *
 * Textura: uma asa só (quadrada); a outra é espelhada. O ombro fica em
 * u≈0, v=0.38 e a dobra em u=0.44.
 */
final class WingsRenderer {
	private static final float SPAN = 20.0F;
	private static final float HEIGHT = 20.0F;
	private static final float JOINT_V = 0.38F;
	private static final float HINGE_U = 0.44F;

	private WingsRenderer() {
	}

	static void render(AbstractClientPlayerEntity player, PlayerEntityModel model, AnimState anim, Identifier texture,
					   CosmeticStyle style, boolean behindCape) {
		MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
		GlStateManager.pushMatrix();
		if (player.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);
		model.body.preRender(0.0625F);
		GlStateManager.scale(0.0625F, 0.0625F, 0.0625F);
		GlStateManager.translate(0.0F, 3.0F, behindCape ? 3.4F : 2.2F);
		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(516, 0.1F);

		float s = (float) Math.sin(anim.wingPhase);
		float lag = (float) Math.sin(anim.wingPhase - 0.8F);
		float open = 1.0F - anim.fold;
		final float sweep = (22.0F + 26.0F * anim.flap * s) * open + 72.0F * anim.fold;
		final float bend = (14.0F + 20.0F * anim.flap * lag) * open + 38.0F * anim.fold;
		final float lift = (10.0F * anim.flap * (float) Math.sin(anim.wingPhase + 0.5F) - 4.0F) * open - 8.0F * anim.fold;

		Glint.tint(style);
		drawPair(sweep, bend, lift);
		if (style != null && style.glint) Glint.render(new Runnable() {
			@Override
			public void run() {
				drawPair(sweep, bend, lift);
			}
		});
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	private static void drawPair(float sweep, float bend, float lift) {
		for (int side = -1; side <= 1; side += 2) {
			GlStateManager.pushMatrix();
			GlStateManager.scale((float) side, 1.0F, 1.0F);
			GlStateManager.rotate(lift, 0.0F, 0.0F, 1.0F);
			drawWing(sweep, bend);
			GlStateManager.popMatrix();
		}
	}

	private static void drawWing(float sweepDeg, float bendDeg) {
		float a = (float) Math.toRadians(sweepDeg);
		float b = (float) Math.toRadians(sweepDeg + bendDeg);
		float inner = SPAN * HINGE_U;
		float outer = SPAN - inner;
		//Para fora (-x) e para trás (+z)
		float hx = -(float) Math.cos(a) * inner, hz = (float) Math.sin(a) * inner;
		float tx = hx - (float) Math.cos(b) * outer, tz = hz + (float) Math.sin(b) * outer;
		float top = -JOINT_V * HEIGHT, bottom = (1.0F - JOINT_V) * HEIGHT;
		float n1x = (float) Math.sin(a), n1z = (float) Math.cos(a);
		float n2x = (float) Math.sin(b), n2z = (float) Math.cos(b);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(7, VertexFormats.POSITION_TEXTURE_NORMAL);
		buffer.vertex(0, top, 0).texture(0, 0).normal(n1x, 0, n1z).next();
		buffer.vertex(hx, top, hz).texture(HINGE_U, 0).normal(n1x, 0, n1z).next();
		buffer.vertex(hx, bottom, hz).texture(HINGE_U, 1).normal(n1x, 0, n1z).next();
		buffer.vertex(0, bottom, 0).texture(0, 1).normal(n1x, 0, n1z).next();

		buffer.vertex(hx, top, hz).texture(HINGE_U, 0).normal(n2x, 0, n2z).next();
		buffer.vertex(tx, top, tz).texture(1, 0).normal(n2x, 0, n2z).next();
		buffer.vertex(tx, bottom, tz).texture(1, 1).normal(n2x, 0, n2z).next();
		buffer.vertex(hx, bottom, hz).texture(HINGE_U, 1).normal(n2x, 0, n2z).next();
		tessellator.draw();
	}
}
