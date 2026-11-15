package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.util.ArrayList;
import java.util.List;

public final class ServerAddressModule extends TextHudModule {
	public ServerAddressModule() {
		super("server", "Endereço do servidor", "Mostra em qual servidor você está.", Category.HUD, Icon.GLOBE, false, 0.005f, 0.48f);
	}

	@Override
	protected List<String> lines(Minecraft client, boolean editing) {
		List<String> lines = new ArrayList<>();
		ServerData info = client.getCurrentServer();
		if (info != null && info.ip != null) lines.add(info.ip);
		else if (client.hasSingleplayerServer()) lines.add("Um jogador");
		else if (editing) lines.add("mush.com.br");
		return lines;
	}
}
