package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class CoordinatesModule extends TextHudModule {
	private static final String[] FACING = {"Sul (+Z)", "Oeste (-X)", "Norte (-Z)", "Leste (+X)"};
	private final BoolSetting direction = add(new BoolSetting("direction", "Mostrar direção", true));
	private final BoolSetting oneLine = add(new BoolSetting("oneLine", "Em uma linha", false));

	public CoordinatesModule() {
		super("coordinates", "Coordenadas", "Sua posição X, Y e Z.", Category.HUD, Icon.PIN, false, 0.175f, 0.005f);
	}

	@Override
	protected boolean centered() {
		return oneLine.on();
	}

	@Override
	protected List<String> lines(Minecraft client, boolean editing) {
		List<String> lines = new ArrayList<>();
		int x = 0, y = 64, z = 0;
		String facing = FACING[2];
		if (client.player != null) {
			x = Mth.floor(client.player.getX());
			y = Mth.floor(client.player.getBoundingBox().minY);
			z = Mth.floor(client.player.getZ());
			facing = FACING[Mth.floor(client.player.getYRot() * 4.0F / 360.0F + 0.5D) & 3];
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
