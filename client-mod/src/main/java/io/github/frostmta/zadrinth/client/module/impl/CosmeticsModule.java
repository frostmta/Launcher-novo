package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.gui.ShopScreen;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.setting.ActionSetting;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;

/** Cosméticos: ver os dos outros, capa de tecido e atalho para a loja. */
public final class CosmeticsModule extends Module {
	public CosmeticsModule() {
		super("cosmetics", "Cosméticos", "Capas, asas e bandanas. Ligado: você vê os cosméticos dos outros.", Category.CLIENT, Icon.SHIRT, true);
		add(new ActionSetting("shop", "Abrir a loja de cosméticos", () -> {
			MinecraftClient client = MinecraftClient.getInstance();
			client.setScreen(new ShopScreen(client.currentScreen));
		}));
		add(new BoolSetting("cloth", "Capa de tecido (física)", true,
				() -> ZadrinthConfig.get().clothCape, v -> ZadrinthConfig.get().clothCape = v));
	}

	@Override
	public boolean isEnabled() {
		return ZadrinthConfig.get().showOthersCosmetics;
	}

	@Override
	protected void storeEnabled(boolean value) {
		ZadrinthConfig.get().showOthersCosmetics = value;
	}
}
