package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.gui.ShopScreen;
import io.github.frostmta.zadrinth.client.gui.ZadrinthMenuScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Botões "Zadrinth Client" e "Cosméticos" no menu de pausa (ESC), na linha
 * vazia entre Conquistas/Estatísticas e Opções. Assim dá para abrir o menu no
 * celular sem ter uma tecla Right Shift.
 */
@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
	private static final int ZADRINTH_BUTTON_ID = 0x5A44;
	private static final int SHOP_BUTTON_ID = 0x5A45;

	@Inject(method = "init", at = @At("TAIL"))
	private void zadrinth$addButton(CallbackInfo ci) {
		this.buttons.add(new ButtonWidget(ZADRINTH_BUTTON_ID, this.width / 2 - 100, this.height / 4 + 56, 98, 20,
				"§dZadrinth Client"));
		this.buttons.add(new ButtonWidget(SHOP_BUTTON_ID, this.width / 2 + 2, this.height / 4 + 56, 98, 20,
				"§dCosméticos"));
	}

	@Inject(method = "buttonClicked", at = @At("HEAD"), cancellable = true)
	private void zadrinth$onButton(ButtonWidget button, CallbackInfo ci) {
		if (button.id == ZADRINTH_BUTTON_ID) {
			this.client.setScreen(new ZadrinthMenuScreen());
			ci.cancel();
		} else if (button.id == SHOP_BUTTON_ID) {
			this.client.setScreen(new ShopScreen(null));
			ci.cancel();
		}
	}
}
