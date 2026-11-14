package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MemoryModule extends TextHudModule {
	public MemoryModule() {
		super("memory", "Memória", "Quanto da memória do jogo está em uso.", Category.HUD, Icon.CHIP, false, 0.005f, 0.60f);
	}

	@Override
	protected List<String> lines(MinecraftClient client, boolean editing) {
		Runtime runtime = Runtime.getRuntime();
		long used = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
		long max = runtime.maxMemory() / 1048576L;
		int percent = max > 0 ? (int) (used * 100 / max) : 0;
		return new ArrayList<>(Collections.singletonList("Mem: " + percent + "% (" + used + "MB)"));
	}
}
