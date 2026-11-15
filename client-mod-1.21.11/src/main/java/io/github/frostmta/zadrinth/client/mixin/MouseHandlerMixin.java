package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.module.impl.ZoomModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Rolagem do mouse durante o zoom muda o zoom (e não troca o item da mão). */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void zadrinth$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (this.minecraft.screen != null || this.minecraft.player == null || vertical == 0) return;
		if (ZoomModule.onScroll(vertical > 0 ? 1 : -1)) ci.cancel();
	}
}
