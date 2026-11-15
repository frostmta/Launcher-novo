package io.github.frostmta.zadrinth.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.frostmta.zadrinth.client.tab.TabIcons;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Ícone do Zadrinth no TAB ao lado de quem usa o client. */
@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
	@ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
	private Component zadrinth$reserveIcon(Component name, PlayerInfo info) {
		return TabIcons.decorate(info, name);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
	private void zadrinth$drawIcon(GuiGraphics graphics, Font font, Component text, int x, int y, int color, Operation<Void> original) {
		TabIcons.drawIcon(graphics, text, x, y);
		original.call(graphics, font, text, x, y, color);
	}
}
