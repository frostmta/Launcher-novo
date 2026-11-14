package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.module.impl.ZoomModule;
import io.github.frostmta.zadrinth.client.module.impl.FreelookModule;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zoom (campo de visão) e Freelook (ângulo da câmera). */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void zadrinth$zoom(float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
		float multiplier = ZoomModule.fovMultiplier();
		if (multiplier != 1.0F) cir.setReturnValue(cir.getReturnValueF() * multiplier);
	}

	// Freelook: a câmera usa o ângulo do freelook em vez do ângulo do personagem
	@Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;yaw:F", opcode = 180))
	private float zadrinth$cameraYaw(Entity entity) {
		return FreelookModule.active() ? FreelookModule.cameraYaw() : entity.yaw;
	}

	@Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevYaw:F", opcode = 180))
	private float zadrinth$cameraPrevYaw(Entity entity) {
		return FreelookModule.active() ? FreelookModule.cameraYaw() : entity.prevYaw;
	}

	@Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;pitch:F", opcode = 180))
	private float zadrinth$cameraPitch(Entity entity) {
		return FreelookModule.active() ? FreelookModule.cameraPitch() : entity.pitch;
	}

	@Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevPitch:F", opcode = 180))
	private float zadrinth$cameraPrevPitch(Entity entity) {
		return FreelookModule.active() ? FreelookModule.cameraPitch() : entity.prevPitch;
	}
}
