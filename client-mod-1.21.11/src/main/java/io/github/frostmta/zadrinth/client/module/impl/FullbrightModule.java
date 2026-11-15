package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;

/** Tudo claro, mesmo em cavernas e à noite (o brilho é trocado no OptionInstanceMixin). */
public final class FullbrightModule extends Module {
	private static FullbrightModule instance;

	public FullbrightModule() {
		super("fullbright", "Full Bright", "Deixa tudo claro, mesmo no escuro.", Category.VISUAL, Icon.SUN, false);
		instance = this;
	}

	public static boolean active() {
		return instance != null && instance.isEnabled();
	}
}
