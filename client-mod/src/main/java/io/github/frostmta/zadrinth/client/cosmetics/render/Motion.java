package io.github.frostmta.zadrinth.client.cosmetics.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * O "vento" que o jogador sente, calculado como a capa do Minecraft faz: a
 * diferença entre a posição do jogador e uma posição que o segue com atraso.
 */
final class Motion {
	/** Subida/queda + balanço da caminhada (graus) */
	float lift;
	/** Quanto o movimento para frente levanta a capa (graus, 0..75) */
	float swing;
	/** Movimento lateral (graus) */
	float side;

	static Motion of(AbstractClientPlayerEntity player, float tickDelta) {
		Motion motion = new Motion();
		double dx = lerp(tickDelta, player.prevCapeX, player.capeX) - lerp(tickDelta, player.prevX, player.x);
		double dy = lerp(tickDelta, player.prevCapeY, player.capeY) - lerp(tickDelta, player.prevY, player.y);
		double dz = lerp(tickDelta, player.prevCapeZ, player.capeZ) - lerp(tickDelta, player.prevZ, player.z);
		float bodyYaw = player.prevBodyYaw + (player.bodyYaw - player.prevBodyYaw) * tickDelta;
		double sin = MathHelper.sin(bodyYaw * (float) Math.PI / 180.0F);
		double cos = -MathHelper.cos(bodyYaw * (float) Math.PI / 180.0F);

		float lift = MathHelper.clamp((float) dy * 10.0F, -6.0F, 32.0F);
		float forward = MathHelper.clamp((float) (dx * sin + dz * cos) * 100.0F, 0.0F, 150.0F);
		float side = MathHelper.clamp((float) (dx * cos - dz * sin) * 100.0F, -40.0F, 40.0F);

		float stride = player.prevStrideDistance + (player.strideDistance - player.prevStrideDistance) * tickDelta;
		float walked = player.prevHorizontalSpeed + (player.horizontalSpeed - player.prevHorizontalSpeed) * tickDelta;
		lift += MathHelper.sin(walked * 6.0F) * 32.0F * stride;
		if (player.isSneaking()) lift += 25.0F;

		motion.lift = lift;
		motion.swing = forward / 2.0F;
		motion.side = side;
		return motion;
	}

	private static double lerp(float delta, double start, double end) {
		return start + (end - start) * delta;
	}
}
