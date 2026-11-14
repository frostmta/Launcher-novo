package io.github.frostmta.zadrinth.client.cosmetics.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Estado da animação de cada jogador (fases das ondas e intensidades
 * suavizadas), para a capa e as asas não "pularem" quando o jogador muda de
 * parado para correndo ou voando.
 */
final class AnimState {
	private static final Map<AbstractClientPlayerEntity, AnimState> STATES = new WeakHashMap<>();

	float capePhase;
	float wingPhase;
	float tailPhase;
	/** 0..1: quanto o jogador está se movendo */
	float speed;
	/** 0..1: força das batidas de asa */
	float flap = 0.2F;
	/** 0..1: asas recolhidas (agachado) */
	float fold;
	private long lastNanos;

	static AnimState of(AbstractClientPlayerEntity player) {
		AnimState state = STATES.get(player);
		if (state == null) {
			state = new AnimState();
			STATES.put(player, state);
		}
		return state;
	}

	/** Avança a animação pelo tempo real (funciona mesmo com o jogo pausado, na loja). */
	void update(AbstractClientPlayerEntity player, Motion motion) {
		long now = System.nanoTime();
		float dt = lastNanos == 0L ? 0.0F : (now - lastNanos) / 50_000_000.0F; //em ticks
		lastNanos = now;
		if (dt <= 0.0F) return;
		if (dt > 4.0F) dt = 4.0F;

		float targetSpeed = Math.min(1.0F, motion.swing / 35.0F);
		speed += (targetSpeed - speed) * Math.min(1.0F, dt * 0.15F);

		boolean airborne = !player.onGround || player.abilities.flying;
		float targetFlap = airborne ? 1.0F : player.isSprinting() ? 0.65F : speed > 0.1F ? 0.42F : 0.2F;
		flap += (targetFlap - flap) * Math.min(1.0F, dt * 0.08F);

		float targetFold = player.isSneaking() ? 1.0F : 0.0F;
		fold += (targetFold - fold) * Math.min(1.0F, dt * 0.25F);

		capePhase = wrap(capePhase + dt * (0.11F + 0.34F * speed));
		wingPhase = wrap(wingPhase + dt * (0.07F + 0.38F * flap) * (1.0F - 0.7F * fold));
		tailPhase = wrap(tailPhase + dt * (0.15F + 0.35F * speed));
	}

	private static float wrap(float phase) {
		float tau = (float) (Math.PI * 2.0);
		return phase > tau * 64 ? phase - tau * 64 : phase;
	}
}
