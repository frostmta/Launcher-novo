package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.stats.CombatTracker;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reach e Combo: cada golpe seu. */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Inject(method = "attack", at = @At("HEAD"))
	private void zadrinth$attack(Player player, Entity target, CallbackInfo ci) {
		CombatTracker.onAttack(target);
	}
}
