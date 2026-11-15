package io.github.frostmta.zadrinth.client.cosmetics.render;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Estado da animação de cada jogador (fases das ondas e intensidades
 * suavizadas), para a capa e as asas não "pularem" quando o jogador muda de
 * parado para correndo ou voando.
 */
public final class AnimState {
	private static final Map<Player, AnimState> STATES = new WeakHashMap<>();

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

	public static AnimState of(Player player) {
		return STATES.computeIfAbsent(player, p -> new AnimState());
	}

	/** Avança a animação pelo tempo real. swing = inclinação da capa pela velocidade (graus). */
	public void update(Player player, float swing) {
		long now = System.nanoTime();
		float dt = lastNanos == 0L ? 0.0F : (now - lastNanos) / 50_000_000.0F; //em ticks
		lastNanos = now;
		if (dt <= 0.0F) return;
		if (dt > 4.0F) dt = 4.0F;

		float targetSpeed = Math.min(1.0F, swing / 35.0F);
		speed += (targetSpeed - speed) * Math.min(1.0F, dt * 0.15F);

		boolean airborne = !player.onGround() || player.getAbilities().flying;
		float targetFlap = airborne ? 1.0F : player.isSprinting() ? 0.65F : speed > 0.1F ? 0.42F : 0.2F;
		flap += (targetFlap - flap) * Math.min(1.0F, dt * 0.08F);

		float targetFold = player.isCrouching() ? 1.0F : 0.0F;
		fold += (targetFold - fold) * Math.min(1.0F, dt * 0.25F);

		capePhase = wrap(capePhase + dt * (0.11F + 0.34F * speed));
		wingPhase = wrap(wingPhase + dt * (0.07F + 0.38F * flap) * (1.0F - 0.7F * fold));
		tailPhase = wrap(tailPhase + dt * (0.15F + 0.35F * speed));
	}

	/** Animação sem jogador (prévias e tela inicial): balanço lento pelo relógio. */
	public static AnimState idle(float seconds) {
		AnimState state = new AnimState();
		state.capePhase = seconds * 2.2F;
		state.wingPhase = seconds * 2.4F;
		state.tailPhase = seconds * 2.6F;
		state.speed = 0.0F;
		state.flap = 0.35F;
		return state;
	}

	public AnimState copy() {
		AnimState state = new AnimState();
		state.capePhase = capePhase;
		state.wingPhase = wingPhase;
		state.tailPhase = tailPhase;
		state.speed = speed;
		state.flap = flap;
		state.fold = fold;
		return state;
	}

	private static float wrap(float phase) {
		float tau = (float) (Math.PI * 2.0);
		return phase > tau * 64 ? phase - tau * 64 : phase;
	}
}
