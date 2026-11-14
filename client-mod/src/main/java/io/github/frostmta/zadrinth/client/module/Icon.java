package io.github.frostmta.zadrinth.client.module;

/** Ícones do atlas assets/zadrinth-client/textures/icons.png (grade 8x8 de 32px). */
public enum Icon {
	KEYBOARD, MOUSE, BARS, WIFI, PIN, COMPASS, POTION, SHIELD,
	SCOREBOARD, GLOBE, CLOCK, CHIP, GAUGE, REACH, BOLT, SPRINT,
	ZOOM, SUN, MOON, CUBE, CROSSHAIR, LIST, SHIRT, PENCIL,
	GEAR, CLOSE, HOME, BAG, INFO, BACK, RESET, CHECK,
	BOSSBAR, EYE;

	public int u() {
		return (ordinal() % 8) * 32;
	}

	public int v() {
		return (ordinal() / 8) * 32;
	}
}
