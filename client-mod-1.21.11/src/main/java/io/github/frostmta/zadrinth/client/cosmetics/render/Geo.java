package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Escreve vértices de quadriláteros (4 por face) já com cor, luz e o quadro
 * da animação (as texturas animadas têm os quadros um embaixo do outro).
 */
final class Geo {
	private final PoseStack.Pose pose;
	private final VertexConsumer out;
	private final int color;
	private final int light;
	private final float frameV;
	private final float frameScale;

	Geo(PoseStack.Pose pose, VertexConsumer out, int color, int light, int frame, int frames) {
		this.pose = pose;
		this.out = out;
		this.color = color;
		this.light = light;
		this.frameScale = frames > 1 ? 1.0F / frames : 1.0F;
		this.frameV = frame * this.frameScale;
	}

	/** u e v de 0 a 1 dentro de um quadro. */
	void vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
		out.addVertex(pose, x, y, z)
				.setColor(color)
				.setUv(u, frameV + v * frameScale)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.setNormal(pose, nx, ny, nz);
	}
}
