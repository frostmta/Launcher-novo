package io.github.frostmta.zadrinth.client;

import net.minecraft.client.option.KeyBinding;

/** Teclas do Zadrinth Client (aparecem em Opções > Controles). */
public final class ZadrinthKeys {
	/** LWJGL 2: KEY_RSHIFT = 54 */
	public static final KeyBinding MENU = new KeyBinding("Zadrinth Client - Menu", 54, "Zadrinth Client");

	private ZadrinthKeys() {
	}

	public static KeyBinding[] all() {
		return new KeyBinding[]{MENU};
	}
}
