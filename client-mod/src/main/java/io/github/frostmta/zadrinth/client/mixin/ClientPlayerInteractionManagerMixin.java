package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.stats.CombatTracker;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Golpes: para Reach e Combo. */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
	@Inject(method = "attackEntity", at = @At("HEAD"))
	private void zadrinth$onAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
		CombatTracker.onAttack(target);
	}
}
