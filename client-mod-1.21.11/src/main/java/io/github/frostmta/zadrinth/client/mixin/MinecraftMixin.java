package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.gui.ZadrinthTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	/** A tela inicial do Zadrinth no lugar da do Minecraft. */
	@ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
	private Screen zadrinth$titleScreen(Screen screen) {
		if (screen instanceof TitleScreen && ZadrinthClient.customTitleScreen()) return new ZadrinthTitleScreen();
		return screen;
	}

	/** Nome da janela. */
	@Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
	private void zadrinth$title(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue("Zadrinth Client " + ZadrinthClient.version() + " (Minecraft " + ZadrinthClient.MINECRAFT + ")");
	}
}
