package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import io.github.frostmta.zadrinth.client.stats.CombatTracker;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ReachModule extends TextHudModule {
	public ReachModule() {
		super("reach", "Reach", "Distância do seu último golpe.", Category.PVP, Icon.REACH, false, 0.005f, 0.72f);
	}

	@Override
	protected List<String> lines(MinecraftClient client, boolean editing) {
		float reach = CombatTracker.lastReach();
		boolean recent = reach >= 0 && System.currentTimeMillis() - CombatTracker.lastReachTime() < 3000L;
		String text = recent ? String.format(Locale.ROOT, "%.2f blocos", reach) : "Sem golpes";
		return new ArrayList<>(Collections.singletonList(text));
	}
}
