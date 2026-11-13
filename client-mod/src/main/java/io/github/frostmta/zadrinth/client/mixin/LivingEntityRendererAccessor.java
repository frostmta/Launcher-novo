package io.github.frostmta.zadrinth.client.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Acesso à lista de camadas (features) do renderizador. */
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {
	@SuppressWarnings("rawtypes")
	@Accessor("features")
	List zadrinth$getFeatures();
}
