package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.setting.NumberSetting;

/** Muda a hora do dia só para você (o céu e a luz). */
public final class TimeChangerModule extends Module {
	private static TimeChangerModule instance;
	private final NumberSetting hour = add(new NumberSetting("hour", "Hora", 12.0F, 0.0F, 23.5F, 0.5F, "h"));

	public TimeChangerModule() {
		super("timechanger", "Mudar horário", "Escolha a hora do dia que você vê.", Category.VISUAL, Icon.MOON, false);
		instance = this;
	}

	/** A hora do jogo trocada pela escolhida (LevelMixin), mantendo o dia (fase da lua). */
	public static long dayTime(long real) {
		TimeChangerModule module = instance;
		if (module == null || !module.isEnabled()) return real;
		long ticks = ((long) (module.hour.value() * 1000.0F) + 18000L) % 24000L;
		return Math.floorDiv(real, 24000L) * 24000L + ticks;
	}

	public static boolean active() {
		return instance != null && instance.isEnabled();
	}
}
