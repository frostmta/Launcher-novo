package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.module.impl.FreelookModule;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freelook: o movimento do mouse vira só a câmera. */
@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "increaseTransforms", at = @At("HEAD"), cancellable = true)
	private void zadrinth$freelook(float yaw, float pitch, CallbackInfo ci) {
		if (FreelookModule.onLook((Entity) (Object) this, yaw, pitch)) ci.cancel();
	}
}
