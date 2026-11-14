package io.github.frostmta.zadrinth.client.module;

import com.google.gson.JsonObject;
import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.impl.ArmorModule;
import io.github.frostmta.zadrinth.client.module.impl.ClockModule;
import io.github.frostmta.zadrinth.client.module.impl.ComboModule;
import io.github.frostmta.zadrinth.client.module.impl.CoordinatesModule;
import io.github.frostmta.zadrinth.client.module.impl.CosmeticsModule;
import io.github.frostmta.zadrinth.client.module.impl.CpsModule;
import io.github.frostmta.zadrinth.client.module.impl.CrosshairModule;
import io.github.frostmta.zadrinth.client.module.impl.DirectionModule;
import io.github.frostmta.zadrinth.client.module.impl.FpsModule;
import io.github.frostmta.zadrinth.client.module.impl.FullbrightModule;
import io.github.frostmta.zadrinth.client.module.impl.HitboxModule;
import io.github.frostmta.zadrinth.client.module.impl.KeystrokesModule;
import io.github.frostmta.zadrinth.client.module.impl.MemoryModule;
import io.github.frostmta.zadrinth.client.module.impl.PingModule;
import io.github.frostmta.zadrinth.client.module.impl.PotionEffectsModule;
import io.github.frostmta.zadrinth.client.module.impl.ReachModule;
import io.github.frostmta.zadrinth.client.module.impl.ScoreboardModule;
import io.github.frostmta.zadrinth.client.module.impl.ServerAddressModule;
import io.github.frostmta.zadrinth.client.module.impl.SpeedModule;
import io.github.frostmta.zadrinth.client.module.impl.TabIconModule;
import io.github.frostmta.zadrinth.client.module.impl.TimeChangerModule;
import io.github.frostmta.zadrinth.client.module.impl.ToggleSprintModule;
import io.github.frostmta.zadrinth.client.module.impl.ZoomModule;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Todos os mods do Zadrinth Client, na ordem do menu. */
public final class ModuleManager {
	public static final ToggleSprintModule TOGGLE_SPRINT = new ToggleSprintModule();
	public static final ScoreboardModule SCOREBOARD = new ScoreboardModule();

	private static final List<Module> MODULES = Collections.unmodifiableList(Arrays.asList(
			new ArmorModule(),
			new DirectionModule(),
			new ComboModule(),
			new CoordinatesModule(),
			new CosmeticsModule(),
			new CpsModule(),
			new ServerAddressModule(),
			new FpsModule(),
			new FullbrightModule(),
			new HitboxModule(),
			new TabIconModule(),
			new KeystrokesModule(),
			new MemoryModule(),
			new CrosshairModule(),
			new TimeChangerModule(),
			new PotionEffectsModule(),
			new PingModule(),
			SCOREBOARD,
			new ReachModule(),
			new ClockModule(),
			TOGGLE_SPRINT,
			new SpeedModule(),
			new ZoomModule()
	));

	private ModuleManager() {
	}

	public static List<Module> all() {
		return MODULES;
	}

	public static List<HudModule> hudModules() {
		List<HudModule> list = new ArrayList<>();
		for (Module module : MODULES) if (module instanceof HudModule) list.add((HudModule) module);
		return list;
	}

	/** Lê o estado salvo de cada módulo (chamado depois de carregar a configuração). */
	public static void load(ZadrinthConfig config) {
		for (Module module : MODULES) {
			try {
				module.load(config.modules.get(module.id));
			} catch (RuntimeException e) {
				ZadrinthClient.LOGGER.warn("Configuração inválida do módulo {}: {}", module.id, e.toString());
			}
		}
	}

	/** Copia o estado de cada módulo para a configuração (antes de salvar). */
	public static void store(ZadrinthConfig config) {
		for (Module module : MODULES) {
			JsonObject json = module.save();
			config.modules.put(module.id, json);
		}
	}

	public static void tick(MinecraftClient client) {
		for (Module module : MODULES) {
			try {
				module.tick(client);
			} catch (RuntimeException e) {
				ZadrinthClient.LOGGER.warn("Erro no módulo {}: {}", module.id, e.toString());
			}
		}
	}
}
