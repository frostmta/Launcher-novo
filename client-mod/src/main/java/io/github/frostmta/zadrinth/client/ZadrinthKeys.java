package io.github.frostmta.zadrinth.client;

import net.minecraft.client.option.KeyBinding;

/** Teclas do Zadrinth Client (aparecem em Opções > Controles). */
public final class ZadrinthKeys {
	/** LWJGL 2: KEY_RSHIFT = 54 */
	public static final KeyBinding MENU = new KeyBinding("Zadrinth Client - Menu", 54, "Zadrinth Client");
	/** LWJGL 2: KEY_C = 46 (igual ao CMClient/Lunar) */
	public static final KeyBinding ZOOM = new KeyBinding("Zadrinth Client - Zoom", 46, "Zadrinth Client");
	/** LWJGL 2: KEY_F = 33 (o F não faz nada no 1.8.9 normal) */
	public static final KeyBinding FREELOOK = new KeyBinding("Zadrinth Client - Freelook", 33, "Zadrinth Client");

	private ZadrinthKeys() {
	}

	public static KeyBinding[] all() {
		return new KeyBinding[]{MENU, ZOOM, FREELOOK};
	}
}
