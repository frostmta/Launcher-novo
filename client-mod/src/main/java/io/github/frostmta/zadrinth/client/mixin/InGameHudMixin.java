package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.hud.HudManager;
import io.github.frostmta.zadrinth.client.module.ModuleManager;
import io.github.frostmta.zadrinth.client.module.impl.CrosshairModule;
import io.github.frostmta.zadrinth.client.module.impl.ScoreboardModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Inject(method = "render(F)V", at = @At("HEAD"))
	private void zadrinth$beforeHud(float tickDelta, CallbackInfo ci) {
		ScoreboardModule.current = null;
		CrosshairModule.vanillaVisible = false;
	}

	@Inject(method = "render(F)V", at = @At("TAIL"))
	private void zadrinth$renderHud(float tickDelta, CallbackInfo ci) {
		HudManager.render(MinecraftClient.getInstance());
	}

	/** O placar passa a ser desenhado pelo módulo Placar (que pode ser movido e redimensionado). */
	@Inject(method = "renderScoreboardObjective", at = @At("HEAD"), cancellable = true)
	private void zadrinth$scoreboard(ScoreboardObjective objective, Window window, CallbackInfo ci) {
		if (ModuleManager.SCOREBOARD.isEnabled()) {
			ScoreboardModule.current = objective;
			ci.cancel();
		}
	}

	/** Mira personalizada: guarda se o jogo ia mostrar a mira e esconde a dele. */
	@Inject(method = "showCrosshair", at = @At("RETURN"), cancellable = true)
	private void zadrinth$crosshair(CallbackInfoReturnable<Boolean> cir) {
		CrosshairModule.vanillaVisible = cir.getReturnValueZ();
		if (CrosshairModule.active() && cir.getReturnValueZ()) cir.setReturnValue(false);
	}
}
