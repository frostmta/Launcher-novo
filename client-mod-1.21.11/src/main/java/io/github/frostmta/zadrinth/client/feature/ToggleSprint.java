package io.github.frostmta.zadrinth.client.feature;

import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;

/**
 * ToggleSprint / ToggleSneak no estilo do Lunar: um toque na tecla de correr
 * (ou de agachar) liga o modo contínuo; outro toque desliga.
 *
 * No 1.21 o próprio jogo tem "alternar corrida/agachar" (as teclas viram
 * interruptores). O módulo liga essas opções do jogo enquanto estiver ativo
 * e devolve como estavam ao desligar, então nada diferente vai ao servidor.
 */
public final class ToggleSprint {
	private static Boolean previousSprint;
	private static Boolean previousSneak;

	private ToggleSprint() {
	}

	private static boolean active() {
		return ModuleManager.TOGGLE_SPRINT.isEnabled();
	}

	public static void tick(Minecraft client) {
		Options options = client.options;
		if (options == null) return;
		ZadrinthConfig config = ZadrinthConfig.get();
		boolean sprint = active() && config.toggleSprintEnabled;
		boolean sneak = active() && config.toggleSneakEnabled;
		previousSprint = apply(options.toggleSprint(), sprint, previousSprint);
		previousSneak = apply(options.toggleCrouch(), sneak, previousSneak);
	}

	private static Boolean apply(net.minecraft.client.OptionInstance<Boolean> option, boolean want, Boolean previous) {
		if (want) {
			if (previous == null) previous = option.get();
			if (!option.get()) option.set(true);
			return previous;
		}
		if (previous != null) {
			option.set(previous);
			return null;
		}
		return null;
	}

	/** Texto do HUD, igual ao do Lunar. Vazio quando não há nada para mostrar. */
	public static String statusText(Minecraft client) {
		if (!active()) return "";
		LocalPlayer player = client.player;
		if (player == null) return "";
		Options options = client.options;
		if (player.getAbilities().flying) return "[Flying]";
		if (player.isShiftKeyDown()) {
			return options.toggleCrouch().get() && options.keyShift.isDown() ? "[Sneaking (Toggled)]" : "[Sneaking]";
		}
		if (options.toggleSprint().get() && options.keySprint.isDown()) return "[Sprinting (Toggled)]";
		if (player.isSprinting()) return "[Sprinting (Vanilla)]";
		return "";
	}
}
