package io.github.frostmta.zadrinth.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.frostmta.zadrinth.client.module.impl.FullbrightModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Full Bright: o brilho da luz vai lá em cima (só para desenhar; a opção salva não muda). */
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
	@WrapOperation(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
	private Object zadrinth$fullbright(OptionInstance<?> option, Operation<Object> original) {
		if (FullbrightModule.active() && option == Minecraft.getInstance().options.gamma()) return 15.0D;
		return original.call(option);
	}
}
