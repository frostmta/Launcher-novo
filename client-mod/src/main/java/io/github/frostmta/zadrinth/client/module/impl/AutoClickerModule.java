package io.github.frostmta.zadrinth.client.module.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.hit.BlockHitResult;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Auto Clicker: enquanto você segura o ataque, o client clica sozinho no
 * ritmo escolhido (com variação, como um clique humano).
 *
 * Só funciona em mundo singleplayer e nos servidores da lista
 * assets/zadrinth-client/autoclicker-servers.json (servidores que liberam).
 * Em servidores como Mush, Hylex, GladMC e Hypixel isso é proibido e dá ban,
 * então lá ele fica desligado sozinho. Mirando num bloco ele também não clica,
 * para não atrapalhar a quebra.
 */
public final class AutoClickerModule extends Module {
	private static AutoClickerModule instance;
	private static Set<String> allowed;

	private final NumberSetting minCps = add(new NumberSetting("minCps", "CPS mínimo", 9, 4, 20, 1, ""));
	private final NumberSetting maxCps = add(new NumberSetting("maxCps", "CPS máximo", 13, 4, 20, 1, ""));

	private final Random random = new Random();
	private long nextClick;

	public AutoClickerModule() {
		super("autoclicker", "Auto Clicker", "Clica sozinho segurando o ataque. Só no singleplayer e nos servidores liberados.", Category.PVP, Icon.CLICK, false);
		instance = this;
	}

	/** Pode usar aqui? (singleplayer ou servidor da lista) */
	public static boolean allowedHere(MinecraftClient client) {
		if (client.isIntegratedServerRunning()) return true;
		ServerInfo info = client.getCurrentServerEntry();
		if (info == null || info.address == null) return false;
		String address = info.address.trim().toLowerCase(Locale.ROOT);
		Set<String> list = allowedServers();
		if (list.contains(address)) return true;
		int colon = address.lastIndexOf(':');
		return colon > 0 && address.endsWith(":25565") && list.contains(address.substring(0, colon));
	}

	private static Set<String> allowedServers() {
		if (allowed == null) {
			Set<String> set = new HashSet<>();
			try (InputStream in = AutoClickerModule.class.getResourceAsStream("/assets/zadrinth-client/autoclicker-servers.json")) {
				if (in != null) {
					JsonObject json = new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
					for (JsonElement element : json.getAsJsonArray("servers")) {
						set.add(element.getAsString().trim().toLowerCase(Locale.ROOT));
					}
				}
			} catch (Exception ignored) {
			}
			allowed = set;
		}
		return allowed;
	}

	/** Chamado a cada quadro do HUD. */
	public static void onFrame(MinecraftClient client) {
		AutoClickerModule module = instance;
		if (module == null || !module.isEnabled()) return;
		if (client.player == null || client.currentScreen != null || !allowedHere(client)) {
			module.nextClick = 0;
			return;
		}
		KeyBinding attack = client.options.attackKey;
		if (!attack.isPressed()) {
			module.nextClick = 0;
			return;
		}
		//Mirando num bloco: deixa o jogo quebrar normalmente
		BlockHitResult result = client.result;
		if (result != null && result.type != null && result.type.ordinal() == 1) return;
		long now = System.nanoTime();
		if (module.nextClick == 0) {
			module.nextClick = now + module.interval();
			return;
		}
		if (now >= module.nextClick) {
			KeyBinding.onKeyPressed(attack.getCode());
			module.nextClick = now + module.interval();
		}
	}

	/** Intervalo até o próximo clique, variando entre o CPS mínimo e o máximo. */
	private long interval() {
		float min = Math.min(minCps.value(), maxCps.value());
		float max = Math.max(minCps.value(), maxCps.value());
		float cps = min + random.nextFloat() * (max - min);
		return (long) (1_000_000_000L / Math.max(1.0F, cps));
	}
}
