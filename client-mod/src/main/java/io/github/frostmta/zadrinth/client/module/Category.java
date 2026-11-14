package io.github.frostmta.zadrinth.client.module;

public enum Category {
	ALL("Todos"), HUD("HUD"), PVP("PvP"), VISUAL("Visual"), CLIENT("Cliente");

	public final String label;

	Category(String label) {
		this.label = label;
	}
}
