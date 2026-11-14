package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import io.github.frostmta.zadrinth.client.module.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public final class ClockModule extends TextHudModule {
	private final ModeSetting format = add(new ModeSetting("format", "Formato", 0, "24 horas", "12 horas"));

	public ClockModule() {
		super("clock", "Relógio", "A hora do seu celular.", Category.HUD, Icon.CLOCK, false, 0.005f, 0.54f);
	}

	@Override
	protected List<String> lines(MinecraftClient client, boolean editing) {
		String pattern = format.index() == 0 ? "HH:mm" : "h:mm a";
		return new ArrayList<>(Collections.singletonList(new SimpleDateFormat(pattern).format(new Date())));
	}
}
