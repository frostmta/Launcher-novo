package io.github.frostmta.zadrinth.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.frostmta.zadrinth.client.module.impl.FreelookModule;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Freelook: a câmera usa o ângulo livre em vez do ângulo do personagem. */
@Mixin(Camera.class)
public abstract class CameraMixin {
	@WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"))
	private float zadrinth$yaw(Entity entity, float partialTick, Operation<Float> original) {
		return FreelookModule.active() ? FreelookModule.cameraYaw() : original.call(entity, partialTick);
	}

	@WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"))
	private float zadrinth$pitch(Entity entity, float partialTick, Operation<Float> original) {
		return FreelookModule.active() ? FreelookModule.cameraPitch() : original.call(entity, partialTick);
	}
}
