package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FpsModule extends TextHudModule {
	public FpsModule() {
		super("fps", "FPS", "Quadros por segundo.", Category.HUD, Icon.BARS, true, 0.005f, 0.30f);
	}

	@Override
	protected List<String> lines(Minecraft client, boolean editing) {
		return new ArrayList<>(Collections.singletonList(client.getFps() + " FPS"));
	}
}
