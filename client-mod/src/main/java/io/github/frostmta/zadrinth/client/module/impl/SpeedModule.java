package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SpeedModule extends TextHudModule {
	private float speed;

	public SpeedModule() {
		super("speed", "Velocímetro", "Sua velocidade em blocos por segundo.", Category.HUD, Icon.GAUGE, false, 0.005f, 0.66f);
	}

	@Override
	public void tick(MinecraftClient client) {
		if (client.player == null) {
			speed = 0;
			return;
		}
		double dx = client.player.x - client.player.prevX;
		double dz = client.player.z - client.player.prevZ;
		float now = (float) Math.sqrt(dx * dx + dz * dz) * 20.0F;
		speed += (now - speed) * 0.5F;
	}

	@Override
	protected List<String> lines(MinecraftClient client, boolean editing) {
		return new ArrayList<>(Collections.singletonList(String.format(Locale.ROOT, "%.2f m/s", speed)));
	}
}
