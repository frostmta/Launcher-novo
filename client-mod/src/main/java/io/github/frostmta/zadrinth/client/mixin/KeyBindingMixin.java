package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.feature.ToggleSprint;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** ToggleSprint/ToggleSneak: a tecla "continua apertada" enquanto o modo estiver ligado. */
@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
	@Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
	private void zadrinth$togglePressed(CallbackInfoReturnable<Boolean> cir) {
		if (ToggleSprint.forcePressed((KeyBinding) (Object) this)) {
			cir.setReturnValue(true);
		}
	}
}
