package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.gui.ZadrinthTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;

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

	/**
	 * Tela de carregamento: no lugar do logo da Mojang, o jogo carrega a nossa
	 * imagem (fundo escuro + símbolo do Zadrinth Client). O fundo da tela inteira
	 * usa a cor do canto da imagem, então ele também fica escuro.
	 */
	@Redirect(method = "loadLogo", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/resource/DefaultResourcePack;open(Lnet/minecraft/util/Identifier;)Ljava/io/InputStream;"))
	private InputStream zadrinth$splash(DefaultResourcePack pack, Identifier id) throws IOException {
		InputStream ours = MinecraftClientMixin.class.getResourceAsStream("/assets/zadrinth-client/textures/splash.png");
		return ours != null ? ours : pack.open(id);
	}
}
