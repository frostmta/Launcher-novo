package io.github.frostmta.zadrinth.client.stats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

/** Alcance do último golpe e combo de golpes seguidos (para os módulos Reach e Combo). */
public final class CombatTracker {
	private static float lastReach = -1;
	private static long lastReachTime;
	private static int combo;
	private static long lastComboHit;
	private static LivingEntity pendingTarget;
	private static int pendingTicks;
	private static int lastPlayerHurt;

	private CombatTracker() {
	}

	/** Chamado quando você ataca uma entidade. */
	public static void onAttack(Entity target) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || target == null) return;
		Vec3d eye = client.player.getCameraPosVec(1.0F);
		BlockHitResult result = client.result;
		double distance;
		if (result != null && result.entity == target && result.pos != null) distance = eye.distanceTo(result.pos);
		else distance = client.player.distanceTo(target);
		lastReach = (float) distance;
		lastReachTime = System.currentTimeMillis();
		if (target instanceof LivingEntity) {
			pendingTarget = (LivingEntity) target;
			pendingTicks = 8;
		}
	}

	/** Tick: confirma o golpe quando o alvo mostra a animação de dano. */
	public static void tick(MinecraftClient client) {
		if (client.player == null) {
			combo = 0;
			return;
		}
		if (pendingTarget != null) {
			if (pendingTarget.hurtTime >= 9) {
				combo++;
				lastComboHit = System.currentTimeMillis();
				pendingTarget = null;
			} else if (--pendingTicks <= 0) {
				pendingTarget = null;
			}
		}
		int hurt = client.player.hurtTime;
		if (hurt > lastPlayerHurt && hurt >= 9) combo = 0; //apanhou: perdeu o combo
		lastPlayerHurt = hurt;
		if (combo > 0 && System.currentTimeMillis() - lastComboHit > 2500L) combo = 0;
	}

	public static float lastReach() {
		return lastReach;
	}

	public static long lastReachTime() {
		return lastReachTime;
	}

	public static int combo() {
		return combo;
	}
}
