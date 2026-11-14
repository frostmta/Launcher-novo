package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.cosmetics.render.CosmeticsFeatureRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

/**
 * Troca a camada da capa do Minecraft pela do Zadrinth (capa com física,
 * asas e bandana).
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Inject(method = "<init>(Lnet/minecraft/client/render/entity/EntityRenderDispatcher;Z)V", at = @At("TAIL"))
	private void zadrinth$addCosmetics(EntityRenderDispatcher dispatcher, boolean slim, CallbackInfo ci) {
		PlayerEntityRenderer self = (PlayerEntityRenderer) (Object) this;
		List features = ((LivingEntityRendererAccessor) self).zadrinth$getFeatures();
		Iterator iterator = features.iterator();
		while (iterator.hasNext()) {
			if (iterator.next() instanceof CapeFeatureRenderer) iterator.remove();
		}
		features.add(new CosmeticsFeatureRenderer(self));
	}
}
