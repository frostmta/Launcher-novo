package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;

/** Caixas de colisão das entidades (o mesmo do F3+B). */
public final class HitboxModule extends Module {
	public HitboxModule() {
		super("hitbox", "Hitbox", "Mostra a caixa de acerto de jogadores e mobs.", Category.PVP, Icon.CUBE, false);
	}

	@Override
	public void tick(MinecraftClient client) {
		EntityRenderDispatcher dispatcher = client.getEntityRenderManager();
		if (dispatcher != null && isEnabled() && !dispatcher.getRenderHitboxes()) dispatcher.setRenderHitboxes(true);
	}

	@Override
	public void onDisable() {
		EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderManager();
		if (dispatcher != null) dispatcher.setRenderHitboxes(false);
	}
}
