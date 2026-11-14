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

	/** Ângulo do sol para a hora escolhida, ou -1 se desligado (WorldMixin). */
	public static float skyAngle(float tickDelta) {
		TimeChangerModule module = instance;
		if (module == null || !module.isEnabled()) return -1;
		long ticks = ((long) (module.hour.value() * 1000.0F) + 18000L) % 24000L;
		float f = ((float) ticks + tickDelta) / 24000.0F - 0.25F;
		if (f < 0.0F) ++f;
		if (f > 1.0F) --f;
		float base = f;
		f = 1.0F - (float) ((Math.cos(f * Math.PI) + 1.0D) / 2.0D);
		return base + (f - base) / 3.0F;
	}
}
