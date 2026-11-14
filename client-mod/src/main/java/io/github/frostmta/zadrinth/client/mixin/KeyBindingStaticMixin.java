package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.stats.ClickCounter;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cada clique (e tecla) passa por aqui: conta os cliques por segundo. */
@Mixin(KeyBinding.class)
public abstract class KeyBindingStaticMixin {
	@Inject(method = "onKeyPressed", at = @At("HEAD"))
	private static void zadrinth$countClick(int code, CallbackInfo ci) {
		ClickCounter.onKey(code);
	}
}
