package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.List;

public final class PingModule extends TextHudModule {
	public PingModule() {
		super("ping", "Ping", "Seu ping no servidor.", Category.HUD, Icon.WIFI, true, 0.005f, 0.42f);
	}

	@Override
	protected List<String> lines(MinecraftClient client, boolean editing) {
		List<String> lines = new ArrayList<>();
		int ping = -1;
		if (client.player != null && client.getNetworkHandler() != null && !client.isIntegratedServerRunning()) {
			PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
			if (entry != null) ping = entry.getLatency();
		}
		if (ping >= 0) lines.add(ping + " ms");
		else if (editing) lines.add("42 ms");
		return lines;
	}
}
