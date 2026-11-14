package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.module.impl.ZoomModule;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zoom: diminui o campo de visão. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void zadrinth$zoom(float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
		float multiplier = ZoomModule.fovMultiplier();
		if (multiplier != 1.0F) cir.setReturnValue(cir.getReturnValueF() * multiplier);
	}
}
