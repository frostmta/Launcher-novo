package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import net.minecraft.client.MinecraftClient;

/** Tudo claro, mesmo em cavernas e à noite. */
public final class FullbrightModule extends Module {
	private static final float BRIGHT = 16.0F;
	private float previous = -1;

	public FullbrightModule() {
		super("fullbright", "Full Bright", "Deixa tudo claro, mesmo no escuro.", Category.VISUAL, Icon.SUN, false);
	}

	@Override
	public void tick(MinecraftClient client) {
		if (client.options == null) return;
		if (isEnabled()) {
			if (client.options.gamma < BRIGHT) {
				if (previous < 0) previous = client.options.gamma;
				client.options.gamma = BRIGHT;
			}
		} else if (previous >= 0) {
			client.options.gamma = previous;
			previous = -1;
		} else if (client.options.gamma > 1.0F) {
			//sobrou de uma sessão anterior
			client.options.gamma = 1.0F;
		}
	}
}
