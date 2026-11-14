package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.module.impl.ZoomModule;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Durante o zoom, a rolagem do mouse muda o zoom em vez do item da mão. */
@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
	@Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
	private void zadrinth$zoomScroll(int amount, CallbackInfo ci) {
		if (ZoomModule.onScroll(amount)) ci.cancel();
	}
}
