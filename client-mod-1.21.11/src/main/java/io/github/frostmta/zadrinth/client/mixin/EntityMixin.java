package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.module.impl.FreelookModule;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freelook: o mouse gira só a câmera, o personagem continua olhando para a frente. */
@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "turn", at = @At("HEAD"), cancellable = true)
	private void zadrinth$freelook(double yaw, double pitch, CallbackInfo ci) {
		if (FreelookModule.onLook((Entity) (Object) this, yaw, pitch)) ci.cancel();
	}
}
