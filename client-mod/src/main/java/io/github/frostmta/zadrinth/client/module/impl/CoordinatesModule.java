package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public final class CoordinatesModule extends TextHudModule {
	private static final String[] FACING = {"Sul (+Z)", "Oeste (-X)", "Norte (-Z)", "Leste (+X)"};
	private final BoolSetting direction = add(new BoolSetting("direction", "Mostrar direção", true));
	private final BoolSetting oneLine = add(new BoolSetting("oneLine", "Em uma linha", false));

	public CoordinatesModule() {
		super("coordinates", "Coordenadas", "Sua posição X, Y e Z.", Category.HUD, Icon.PIN, false, 0.005f, 0.12f);
	}

	@Override
	protected boolean centered() {
		return oneLine.on();
	}

	@Override
	protected List<String> lines(MinecraftClient client, boolean editing) {
		List<String> lines = new ArrayList<>();
		int x = 0, y = 64, z = 0;
		String facing = FACING[2];
		if (client.player != null) {
			x = MathHelper.floor(client.player.x);
			y = MathHelper.floor(client.player.getBoundingBox().minY);
			z = MathHelper.floor(client.player.z);
			facing = FACING[MathHelper.floor(client.player.yaw * 4.0F / 360.0F + 0.5D) & 3];
		}
		if (oneLine.on()) {
			lines.add(x + ", " + y + ", " + z);
		} else {
			lines.add("§7X: §r" + x);
			lines.add("§7Y: §r" + y);
			lines.add("§7Z: §r" + z);
		}
		if (direction.on()) lines.add(facing);
		return lines;
	}
}
