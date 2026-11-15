package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.module.impl.TimeChangerModule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mudar horário: só o mundo do seu cliente vê a hora escolhida (céu e luz). */
@Mixin(Level.class)
public abstract class LevelMixin {
	@Inject(method = "getDayTime", at = @At("RETURN"), cancellable = true)
	private void zadrinth$timeChanger(CallbackInfoReturnable<Long> cir) {
		if (TimeChangerModule.active() && (Object) this instanceof ClientLevel) {
			cir.setReturnValue(TimeChangerModule.dayTime(cir.getReturnValueJ()));
		}
	}
}
