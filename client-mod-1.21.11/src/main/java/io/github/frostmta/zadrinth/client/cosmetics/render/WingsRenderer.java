package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;

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

	/** Malha 3D da asa (montada uma vez, depois que a textura foi carregada). */
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

	private static void emit(Geo geo, float[] faces) {
		for (int i = 0; i + 7 < faces.length; i += 8) {
			geo.vertex(faces[i], faces[i + 1], faces[i + 2], faces[i + 3], faces[i + 4], faces[i + 5], faces[i + 6], faces[i + 7]);
		}
	}

	/** Já no espaço do corpo do jogador. */
	static void submit(PoseStack poseStack, SubmitNodeCollector queue, int light, Cosmetic cosmetic, Identifier texture,
					   CosmeticStyle style, AnimState anim, boolean behindCape) {
		float s = (float) Math.sin(anim.wingPhase);
		float lag = (float) Math.sin(anim.wingPhase - 0.8F);
		float open = 1.0F - anim.fold;
		final float sweep = (22.0F + 26.0F * anim.flap * s) * open + 72.0F * anim.fold;
		final float bend = (14.0F + 20.0F * anim.flap * lag) * open + 38.0F * anim.fold;
		final float lift = (10.0F * anim.flap * (float) Math.sin(anim.wingPhase + 0.5F) - 4.0F) * open - 8.0F * anim.fold;

		if (cosmetic != null && cosmetic.voxel) {
			VoxelWing mesh = mesh(cosmetic);
			if (mesh == null) return;
			poseStack.pushPose();
			poseStack.scale(0.0625F, 0.0625F, 0.0625F);
			poseStack.translate(0.0F, 2.0F, behindCape ? 3.9F : 2.9F);
			for (int side = -1; side <= 1; side += 2) {
				poseStack.pushPose();
				poseStack.scale((float) side, 1.0F, 1.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(lift - 6.0F));
				poseStack.mulPose(Axis.YP.rotationDegrees(sweep));
				CosmeticDraw.submit(poseStack, queue, texture, style, cosmetic, light, geo -> emit(geo, mesh.inner));
				poseStack.translate(-mesh.innerLength, 0.0F, 0.0F);
				poseStack.mulPose(Axis.YP.rotationDegrees(bend));
				CosmeticDraw.submit(poseStack, queue, texture, style, cosmetic, light, geo -> emit(geo, mesh.outer));
				poseStack.popPose();
			}
			poseStack.popPose();
			return;
		}
		poseStack.pushPose();
		poseStack.scale(0.0625F, 0.0625F, 0.0625F);
		poseStack.translate(0.0F, 3.0F, behindCape ? 3.4F : 2.2F);
		for (int side = -1; side <= 1; side += 2) {
			poseStack.pushPose();
			poseStack.scale((float) side, 1.0F, 1.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(lift));
			CosmeticDraw.submit(poseStack, queue, texture, style, cosmetic, light, geo -> drawWing(geo, sweep, bend));
			poseStack.popPose();
		}
		poseStack.popPose();
	}

	private static void drawWing(Geo geo, float sweepDeg, float bendDeg) {
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

		geo.vertex(0, top, 0, 0, 0, n1x, 0, n1z);
		geo.vertex(hx, top, hz, HINGE_U, 0, n1x, 0, n1z);
		geo.vertex(hx, bottom, hz, HINGE_U, 1, n1x, 0, n1z);
		geo.vertex(0, bottom, 0, 0, 1, n1x, 0, n1z);

		geo.vertex(hx, top, hz, HINGE_U, 0, n2x, 0, n2z);
		geo.vertex(tx, top, tz, 1, 0, n2x, 0, n2z);
		geo.vertex(tx, bottom, tz, 1, 1, n2x, 0, n2z);
		geo.vertex(hx, bottom, hz, HINGE_U, 1, n2x, 0, n2z);
	}
}
