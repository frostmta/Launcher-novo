package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import io.github.frostmta.zadrinth.client.stats.CombatTracker;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComboModule extends TextHudModule {
	public ComboModule() {
		super("combo", "Combo", "Golpes seguidos sem apanhar.", Category.PVP, Icon.BOLT, false, 0.175f, 0.32f);
	}

	@Override
	protected List<String> lines(Minecraft client, boolean editing) {
		int combo = CombatTracker.combo();
		String text = combo > 0 ? combo + " combo" : "Sem combo";
		return new ArrayList<>(Collections.singletonList(text));
	}
}
