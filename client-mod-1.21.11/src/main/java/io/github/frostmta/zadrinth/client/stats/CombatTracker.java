package io.github.frostmta.zadrinth.client.stats;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || target == null) return;
		Vec3 eye = client.player.getEyePosition(1.0F);
		HitResult result = client.hitResult;
		double distance;
		if (result instanceof EntityHitResult hit && hit.getEntity() == target) distance = eye.distanceTo(hit.getLocation());
		else distance = client.player.distanceTo(target);
		lastReach = (float) distance;
		lastReachTime = System.currentTimeMillis();
		if (target instanceof LivingEntity) {
			pendingTarget = (LivingEntity) target;
			pendingTicks = 8;
		}
	}

	/** Tick: confirma o golpe quando o alvo mostra a animação de dano. */
	public static void tick(Minecraft client) {
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
