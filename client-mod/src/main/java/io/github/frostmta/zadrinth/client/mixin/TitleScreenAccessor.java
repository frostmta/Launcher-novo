package io.github.frostmta.zadrinth.client.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Acesso ao panorama da tela inicial (usado pela tela do Zadrinth). */
@Mixin(TitleScreen.class)
public interface TitleScreenAccessor {
	@Invoker("renderBackground")
	void zadrinth$renderPanorama(int mouseX, int mouseY, float delta);
}
