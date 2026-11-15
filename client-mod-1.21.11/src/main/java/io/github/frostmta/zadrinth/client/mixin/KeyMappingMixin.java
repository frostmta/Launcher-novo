package io.github.frostmta.zadrinth.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.frostmta.zadrinth.client.stats.ClickCounter;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Conta cada clique (CPS). */
@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
	@Inject(method = "click", at = @At("HEAD"))
	private static void zadrinth$countClick(InputConstants.Key key, CallbackInfo ci) {
		ClickCounter.onKey(key);
	}
}
