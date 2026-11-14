package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.stats.ClickCounter;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CpsModule extends TextHudModule {
	private final BoolSetting right = add(new BoolSetting("right", "Mostrar botão direito", true));

	public CpsModule() {
		super("cps", "CPS", "Cliques por segundo.", Category.HUD, Icon.MOUSE, true, 0.005f, 0.36f);
	}

	@Override
	protected List<String> lines(MinecraftClient client, boolean editing) {
		String text = right.on() ? ClickCounter.left() + " | " + ClickCounter.right() + " CPS" : ClickCounter.left() + " CPS";
		return new ArrayList<>(Collections.singletonList(text));
	}
}
