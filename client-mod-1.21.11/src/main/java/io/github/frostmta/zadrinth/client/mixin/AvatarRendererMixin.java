package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.cosmetics.render.CosmeticsLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Guarda o visual (capa, asas, bandana) e a animação de cada jogador para a CosmeticsLayer. */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
			at = @At("TAIL"))
	private void zadrinth$cosmetics(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
		CosmeticsLayer.extract(avatar, state);
	}
}
