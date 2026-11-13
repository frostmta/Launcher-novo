package io.github.frostmta.zadrinth.client.mixin;

import io.github.frostmta.zadrinth.client.tab.TabIcons;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Ícone do Zadrinth no TAB: reserva espaço no nome (para a coluna sair do
 * tamanho certo) e desenha o ícone na hora de escrever o nome.
 */
@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
	@Redirect(method = "render",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/PlayerListHud;getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Ljava/lang/String;"))
	private String zadrinth$decorateName(PlayerListHud hud, PlayerListEntry entry) {
		return TabIcons.decorate(entry, hud.getPlayerName(entry));
	}

	@Redirect(method = "render",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I"))
	private int zadrinth$drawName(TextRenderer renderer, String text, float x, float y, int color) {
		return TabIcons.drawName(renderer, text, x, y, color);
	}
}
