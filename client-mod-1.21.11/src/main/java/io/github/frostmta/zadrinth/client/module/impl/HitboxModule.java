package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;

/** Caixas de colisão das entidades (o mesmo do F3+B). */
public final class HitboxModule extends Module {
	public HitboxModule() {
		super("hitbox", "Hitbox", "Mostra a caixa de acerto de jogadores e mobs.", Category.PVP, Icon.CUBE, false);
	}

	@Override
	public void tick(Minecraft client) {
		if (isEnabled() && !client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
			client.debugEntries.toggleStatus(DebugScreenEntries.ENTITY_HITBOXES);
		}
	}

	@Override
	public void onDisable() {
		Minecraft client = Minecraft.getInstance();
		if (client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
			client.debugEntries.toggleStatus(DebugScreenEntries.ENTITY_HITBOXES);
		}
	}
}
