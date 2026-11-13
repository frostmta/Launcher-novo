package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

/** Coloca a tecla do menu (Right Shift) em Opções > Controles, antes de ler o options.txt. */
@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
	@Shadow
	@Mutable
	public KeyBinding[] allKeys;

	@Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;Ljava/io/File;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;load()V"))
	private void zadrinth$addKeys(MinecraftClient client, File optionsDir, CallbackInfo ci) {
		this.allKeys = ArrayUtils.addAll(this.allKeys, ZadrinthKeys.all());
	}
}
