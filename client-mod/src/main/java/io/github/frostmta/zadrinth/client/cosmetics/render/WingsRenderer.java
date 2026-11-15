package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

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

	//Asas 3D de blocos (dragão): maiores, ombro mais alto
	private static final float VOXEL_SPAN = 24.0F;
	private static final float VOXEL_HEIGHT = 24.0F;
	private static final float VOXEL_JOINT_V = 0.30F;
	private static final float VOXEL_HINGE_U = 0.42F;
	private static final Map<String, VoxelWing> MESHES = new HashMap<>();

	private WingsRenderer() {
	}

	static void render(AbstractClientPlayerEntity player, PlayerEntityModel model, AnimState anim, Cosmetic cosmetic, Identifier texture,
					   CosmeticStyle style, boolean behindCape) {
		MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
		GlStateManager.pushMatrix();
		if (player.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);
		model.body.preRender(0.0625F);
		GlStateManager.scale(0.0625F, 0.0625F, 0.0625F);
		if (cosmetic != null && cosmetic.voxel) GlStateManager.translate(0.0F, 2.0F, behindCape ? 3.9F : 2.9F);
		else GlStateManager.translate(0.0F, 3.0F, behindCape ? 3.4F : 2.2F);
		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(516, 0.1F);

		float s = (float) Math.sin(anim.wingPhase);
		float lag = (float) Math.sin(anim.wingPhase - 0.8F);
		float open = 1.0F - anim.fold;
		final float sweep = (22.0F + 26.0F * anim.flap * s) * open + 72.0F * anim.fold;
		final float bend = (14.0F + 20.0F * anim.flap * lag) * open + 38.0F * anim.fold;
		final float lift = (10.0F * anim.flap * (float) Math.sin(anim.wingPhase + 0.5F) - 4.0F) * open - 8.0F * anim.fold;

		Glint.tint(style);
		draw(cosmetic, sweep, bend, lift);
		if (style != null && style.glint) Glint.render(new Runnable() {
			@Override
			public void run() {
				draw(cosmetic, sweep, bend, lift);
			}
		});
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	/** Asas sem jogador (tela inicial): batida lenta. */
	static void renderPreview(PlayerEntityModel model, Cosmetic cosmetic, Identifier texture, CosmeticStyle style, boolean behindCape, float seconds) {
		MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
		GlStateManager.pushMatrix();
		model.body.preRender(0.0625F);
		GlStateManager.scale(0.0625F, 0.0625F, 0.0625F);
		if (cosmetic != null && cosmetic.voxel) GlStateManager.translate(0.0F, 2.0F, behindCape ? 3.9F : 2.9F);
		else GlStateManager.translate(0.0F, 3.0F, behindCape ? 3.4F : 2.2F);
		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(516, 0.1F);
		float phase = seconds * 2.4F;
		final float sweep = 22.0F + 12.0F * (float) Math.sin(phase);
		final float bend = 14.0F + 10.0F * (float) Math.sin(phase - 0.8F);
		final float lift = 5.0F * (float) Math.sin(phase + 0.5F) - 4.0F;
		Glint.tint(style);
		draw(cosmetic, sweep, bend, lift);
		if (style != null && style.glint) Glint.render(new Runnable() {
			@Override
			public void run() {
				draw(cosmetic, sweep, bend, lift);
			}
		});
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	/** Asas lisas ou 3D de blocos (dragão). */
	private static void draw(Cosmetic cosmetic, float sweep, float bend, float lift) {
		VoxelWing mesh = cosmetic != null && cosmetic.voxel ? mesh(cosmetic) : null;
		if (mesh == null) {
			if (cosmetic == null || !cosmetic.voxel) drawPair(sweep, bend, lift);
			return;
		}
		for (int side = -1; side <= 1; side += 2) {
			GlStateManager.pushMatrix();
			GlStateManager.scale((float) side, 1.0F, 1.0F);
			GlStateManager.rotate(lift - 6.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(sweep, 0.0F, 1.0F, 0.0F);
			drawFaces(mesh.inner);
			GlStateManager.translate(-mesh.innerLength, 0.0F, 0.0F);
			GlStateManager.rotate(bend, 0.0F, 1.0F, 0.0F);
			drawFaces(mesh.outer);
			GlStateManager.popMatrix();
		}
	}

	private static VoxelWing mesh(Cosmetic cosmetic) {
		VoxelWing cached = MESHES.get(cosmetic.key());
		if (cached != null) return cached;
		int[] data = CosmeticTextures.alpha(cosmetic);
		if (data == null) return null;
		int[] alpha = java.util.Arrays.copyOfRange(data, 2, data.length);
		VoxelWing mesh = VoxelWing.build(data[0], data[1], alpha, VOXEL_SPAN, VOXEL_HEIGHT, VOXEL_JOINT_V, VOXEL_HINGE_U);
		MESHES.put(cosmetic.key(), mesh);
		return mesh;
	}

	private static void drawFaces(float[] faces) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(7, VertexFormats.POSITION_TEXTURE_NORMAL);
		for (int i = 0; i + 7 < faces.length; i += 8) {
			buffer.vertex(faces[i], faces[i + 1], faces[i + 2]).texture(faces[i + 3], faces[i + 4]).normal(faces[i + 5], faces[i + 6], faces[i + 7]).next();
		}
		tessellator.draw();
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
