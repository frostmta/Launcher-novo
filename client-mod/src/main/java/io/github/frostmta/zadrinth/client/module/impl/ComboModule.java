package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import io.github.frostmta.zadrinth.client.stats.CombatTracker;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComboModule extends TextHudModule {
	public ComboModule() {
		super("combo", "Combo", "Golpes seguidos sem apanhar.", Category.PVP, Icon.BOLT, false, 0.005f, 0.78f);
	}

	@Override
	protected List<String> lines(MinecraftClient client, boolean editing) {
		int combo = CombatTracker.combo();
		String text = combo > 0 ? combo + " combo" : "Sem combo";
		return new ArrayList<>(Collections.singletonList(text));
	}
}
