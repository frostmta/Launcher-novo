package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.module.impl.TimeChangerModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mudar horário: só o mundo que você vê (o do servidor não muda). */
@Mixin(World.class)
public abstract class WorldMixin {
	@Inject(method = "getSkyAngle", at = @At("HEAD"), cancellable = true)
	private void zadrinth$timeChanger(float tickDelta, CallbackInfoReturnable<Float> cir) {
		if (!((Object) this instanceof ClientWorld)) return;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.player.dimension != 0) return;
		float angle = TimeChangerModule.skyAngle(tickDelta);
		if (angle >= 0) cir.setReturnValue(angle);
	}
}
