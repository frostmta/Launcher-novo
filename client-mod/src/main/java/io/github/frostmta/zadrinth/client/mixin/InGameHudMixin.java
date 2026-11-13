package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.hud.HudManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Inject(method = "render(F)V", at = @At("TAIL"))
	private void zadrinth$renderHud(float tickDelta, CallbackInfo ci) {
		HudManager.render(MinecraftClient.getInstance());
	}
}
