package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

/** Manda a geometria de um cosmético para o jogo desenhar (com cor e brilho). */
final class CosmeticDraw {
	private CosmeticDraw() {
	}

	static float seconds() {
		return (System.currentTimeMillis() % 3_600_000L) / 1000.0F;
	}

	/** O desenho acontece depois: a forma recebe tudo já calculado. */
	static void submit(PoseStack poseStack, SubmitNodeCollector queue, Identifier texture, CosmeticStyle style,
					   Cosmetic cosmetic, int light, Consumer<Geo> shape) {
		int frame = cosmetic == null ? 0 : cosmetic.frame();
		int frames = cosmetic == null ? 1 : cosmetic.frames;
		int rgb = style == null ? 0xFFFFFF : style.currentColor(seconds());
		int color = 0xFF000000 | rgb;
		queue.submitCustomGeometry(poseStack, RenderTypes.entityCutoutNoCull(texture),
				(pose, out) -> shape.accept(new Geo(pose, out, color, light, frame, frames)));
		if (style != null && style.glint) {
			queue.submitCustomGeometry(poseStack, RenderTypes.entityGlint(),
					(pose, out) -> shape.accept(new Geo(pose, out, 0xFFFFFFFF, light, frame, frames)));
		}
	}
}
