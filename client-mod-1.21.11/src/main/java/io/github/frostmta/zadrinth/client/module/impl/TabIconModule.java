package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;

/** Símbolo do Zadrinth no TAB ao lado de quem usa o client. */
public final class TabIconModule extends Module {
	public TabIconModule() {
		super("tab", "Ícone no TAB", "Mostra o símbolo do Zadrinth ao lado de quem usa o client.", Category.CLIENT, Icon.LIST, true);
		add(new BoolSetting("share", "Avisar que eu uso o Zadrinth", true,
				() -> ZadrinthConfig.get().sharePresence, v -> ZadrinthConfig.get().sharePresence = v));
	}

	@Override
	public boolean isEnabled() {
		return ZadrinthConfig.get().tabIconEnabled;
	}

	@Override
	protected void storeEnabled(boolean value) {
		ZadrinthConfig.get().tabIconEnabled = value;
	}
}
