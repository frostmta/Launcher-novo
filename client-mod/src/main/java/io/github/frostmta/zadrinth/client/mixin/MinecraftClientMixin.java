package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.gui.ZadrinthTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Shadow
	public ClientWorld world;

	@Inject(method = "tick", at = @At("TAIL"))
	private void zadrinth$onTick(CallbackInfo ci) {
		ZadrinthClient.onClientTick((MinecraftClient) (Object) this);
	}

	/** Troca a tela inicial do Minecraft pela do Zadrinth Client. */
	@ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
	private Screen zadrinth$titleScreen(Screen screen) {
		boolean vanillaTitle = screen != null && screen.getClass() == TitleScreen.class;
		boolean willBeTitle = screen == null && this.world == null;
		if (ZadrinthClient.customTitleScreen() && (vanillaTitle || willBeTitle)) {
			return new ZadrinthTitleScreen();
		}
		return screen;
	}
}
