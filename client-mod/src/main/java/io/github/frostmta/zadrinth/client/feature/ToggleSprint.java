package io.github.frostmta.zadrinth.client.feature;

import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.mixin.KeyBindingAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.ClientPlayerEntity;

/**
 * ToggleSprint / ToggleSneak no estilo do Lunar: um toque na tecla de correr
 * (ou de agachar) liga o modo contínuo; outro toque desliga.
 *
 * Funciona fazendo o jogo "achar" que a tecla continua apertada
 * (veja KeyBindingMixin), então o próprio Minecraft decide quando dá para
 * correr (fome, parede, item em uso...). Nada é enviado ao servidor além do
 * que um jogador segurando a tecla enviaria.
 */
public final class ToggleSprint {
	private ToggleSprint() {
	}

	private static boolean active() {
		return io.github.frostmta.zadrinth.client.module.ModuleManager.TOGGLE_SPRINT.isEnabled();
	}

	public static void tick(MinecraftClient client) {
		GameOptions options = client.options;
		if (options == null) return;
		ZadrinthConfig config = ZadrinthConfig.get();
		if (!active()) {
			config.sneakToggled = false;
			return;
		}

		boolean changed = false;
		//wasPressed() consome cada toque; o jogo usa isPressed() (tecla segurada) para correr
		while (options.sprintKey.wasPressed()) {
			if (config.toggleSprintEnabled && client.currentScreen == null) {
				config.sprintToggled = !config.sprintToggled;
				changed = true;
			}
		}
		while (options.sneakKey.wasPressed()) {
			if (config.toggleSneakEnabled && client.currentScreen == null) {
				config.sneakToggled = !config.sneakToggled;
			}
		}
		if (!config.toggleSneakEnabled) config.sneakToggled = false;
		if (changed) ZadrinthConfig.save();
	}

	/** Diz ao KeyBindingMixin se a tecla deve contar como apertada. */
	public static boolean forcePressed(KeyBinding key) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.options == null || client.player == null || !active()) return false;
		ZadrinthConfig config = ZadrinthConfig.get();
		if (key == client.options.sprintKey) {
			return config.toggleSprintEnabled && config.sprintToggled;
		}
		if (key == client.options.sneakKey) {
			return config.toggleSneakEnabled && config.sneakToggled && client.currentScreen == null;
		}
		return false;
	}

	/** Texto do HUD, igual ao do Lunar. Vazio quando não há nada para mostrar. */
	public static String statusText(MinecraftClient client) {
		if (!active()) return "";
		ClientPlayerEntity player = client.player;
		if (player == null) return "";
		ZadrinthConfig config = ZadrinthConfig.get();
		boolean sneakHeld = ((KeyBindingAccessor) client.options.sneakKey).zadrinth$isPhysicallyPressed();
		boolean sprintHeld = ((KeyBindingAccessor) client.options.sprintKey).zadrinth$isPhysicallyPressed();

		if (player.abilities.flying) {
			return "[Flying]";
		}
		if (config.toggleSneakEnabled && config.sneakToggled) {
			return "[Sneaking (Toggled)]";
		}
		if (player.isSneaking()) {
			return sneakHeld ? "[Sneaking (Key Held)]" : "[Sneaking]";
		}
		if (config.toggleSprintEnabled && config.sprintToggled) {
			return "[Sprinting (Toggled)]";
		}
		if (player.isSprinting()) {
			return sprintHeld ? "[Sprinting (Key Held)]" : "[Sprinting (Vanilla)]";
		}
		return "";
	}
}
