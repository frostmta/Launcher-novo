package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.module.impl.FpsBoostModule;
import net.minecraft.client.render.CameraView;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** FPS Boost: não desenha entidades longe da câmera. */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void zadrinth$cull(Entity entity, CameraView camera, double x, double y, double z,
							   CallbackInfoReturnable<Boolean> cir) {
		if (!FpsBoostModule.shouldRender(entity, x, y, z)) cir.setReturnValue(false);
	}
}
