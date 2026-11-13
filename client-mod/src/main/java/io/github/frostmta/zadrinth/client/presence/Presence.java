package io.github.frostmta.zadrinth.client.presence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.cosmetics.Loadout;
import io.github.frostmta.zadrinth.client.cosmetics.Wardrobe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sincronização entre jogadores do Zadrinth Client (ícone no TAB e cosméticos).
 *
 * Como funciona:
 * 1. Enquanto você está num servidor, o client avisa a cada 60 s:
 *    "o jogador NOME está no servidor ENDEREÇO usando o Zadrinth" (heartbeat).
 * 2. A cada 30 s (10 s com o TAB aberto), ele pergunta quais nomes da lista
 *    de jogadores daquele servidor também mandaram esse aviso recentemente.
 * 3. Quem estiver na resposta ganha o ícone do Zadrinth no TAB.
 * 4. Junto com o aviso vai o que você está vestindo (capa, asas, bandana e as
 *    cores); a resposta traz o visual de cada um, para todos se verem iguais.
 *
 * O servidor é um projeto Supabase (veja docs/supabase.sql). Só vão para lá
 * o endereço do servidor, o seu nome, os nomes da lista do TAB e os ids dos
 * cosméticos equipados. Se o projeto ainda não tiver as funções novas (_v2),
 * o client volta para as antigas: o TAB continua funcionando, só sem cosméticos.
 */
public final class Presence {
	private static final long HEARTBEAT_MS = 60_000L;
	private static final long QUERY_MS = 30_000L;
	private static final long QUERY_TAB_OPEN_MS = 10_000L;
	private static final int MAX_NAMES = 200;

	private static final String URL = ZadrinthClient.BUILD.getProperty("supabase.url", "").trim();
	private static final String KEY = ZadrinthClient.BUILD.getProperty("supabase.key", "").trim();

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "Zadrinth-Presence");
		thread.setDaemon(true);
		return thread;
	});
	private static final AtomicBoolean QUERY_RUNNING = new AtomicBoolean(false);

	private static volatile Set<String> online = Collections.emptySet();
	private static volatile Map<String, Loadout> cosmetics = Collections.emptyMap();
	/** O Supabase tem as funções _v2 (cosméticos)? Começa tentando que sim. */
	private static volatile boolean v2Available = true;
	private static String currentServer;
	private static long lastHeartbeat;
	private static long lastQuery;

	private Presence() {
	}

	public static boolean isConfigured() {
		return URL.startsWith("https://") && !KEY.isEmpty();
	}

	/** Este jogador está usando o Zadrinth Client? (você sempre está) */
	public static boolean isZadrinthUser(String name) {
		if (name == null) return false;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null && name.equalsIgnoreCase(client.player.getGameProfile().getName())) {
			return true;
		}
		return online.contains(name.toLowerCase(Locale.ROOT));
	}

	/** O visual de outro jogador do Zadrinth neste servidor (null se não usa o client). */
	public static Loadout cosmeticsOf(String name) {
		return name == null ? null : cosmetics.get(name.toLowerCase(Locale.ROOT));
	}

	/** Manda o próximo aviso logo (ex.: você trocou de capa). */
	public static void requestHeartbeat() {
		lastHeartbeat = 0L;
	}

	public static void tick(MinecraftClient client) {
		ServerInfo info = client.getCurrentServerEntry();
		if (client.world == null || client.player == null || info == null || info.address == null) {
			reset(null);
			return;
		}
		String server = normalize(info.address);
		if (!server.equals(currentServer)) reset(server);
		ZadrinthConfig config = ZadrinthConfig.get();
		if (!isConfigured()) return;

		long now = System.currentTimeMillis();
		if (config.sharePresence && now - lastHeartbeat > HEARTBEAT_MS) {
			lastHeartbeat = now;
			final String name = client.player.getGameProfile().getName();
			final JsonObject outfit = Wardrobe.local().toJson();
			EXECUTOR.execute(() -> heartbeat(server, name, outfit));
		}

		if (!config.tabIconEnabled && !config.showOthersCosmetics) return;
		boolean tabOpen = client.options.playerListKey.isPressed();
		if (now - lastQuery > (tabOpen ? QUERY_TAB_OPEN_MS : QUERY_MS) && !QUERY_RUNNING.get()) {
			ClientPlayNetworkHandler handler = client.getNetworkHandler();
			if (handler == null) return;
			lastQuery = now;
			final List<String> names = new ArrayList<>();
			for (PlayerListEntry entry : handler.getPlayerList()) {
				if (entry.getProfile() != null && entry.getProfile().getName() != null) {
					names.add(entry.getProfile().getName());
					if (names.size() >= MAX_NAMES) break;
				}
			}
			if (names.isEmpty()) return;
			QUERY_RUNNING.set(true);
			EXECUTOR.execute(() -> {
				try {
					query(server, names);
				} finally {
					QUERY_RUNNING.set(false);
				}
			});
		}
	}

	private static void reset(String server) {
		currentServer = server;
		online = Collections.emptySet();
		cosmetics = Collections.emptyMap();
		lastHeartbeat = 0L;
		lastQuery = 0L;
	}

	/** "Mc.Hypixel.net:25565" → "mc.hypixel.net" */
	static String normalize(String address) {
		String value = address.trim().toLowerCase(Locale.ROOT);
		if (value.endsWith(":25565")) value = value.substring(0, value.length() - 6);
		if (value.endsWith(".")) value = value.substring(0, value.length() - 1);
		return value;
	}

	private static void heartbeat(String server, String name, JsonObject outfit) {
		JsonObject body = new JsonObject();
		body.addProperty("p_server", server);
		body.addProperty("p_name", name);
		body.addProperty("p_version", ZadrinthClient.version());
		try {
			if (v2Available) {
				body.add("p_cosmetics", outfit);
				try {
					rpc("zadrinth_heartbeat_v2", body);
					return;
				} catch (MissingFunctionException e) {
					markV1Only();
					body.remove("p_cosmetics");
				}
			}
			rpc("zadrinth_heartbeat", body);
		} catch (Exception e) {
			ZadrinthClient.LOGGER.warn("Falha ao avisar presença: {}", e.toString());
		}
	}

	private static void markV1Only() {
		if (v2Available) {
			v2Available = false;
			ZadrinthClient.LOGGER.warn("O Supabase ainda não tem as funções de cosméticos (_v2): "
					+ "rode de novo o docs/supabase.sql. O TAB continua funcionando.");
		}
	}

	private static void query(String server, List<String> names) {
		JsonObject body = new JsonObject();
		body.addProperty("p_server", server);
		JsonArray array = new JsonArray();
		for (String name : names) array.add(new com.google.gson.JsonPrimitive(name));
		body.add("p_names", array);
		try {
			String response = null;
			if (v2Available) {
				try {
					response = rpc("zadrinth_online_v2", body);
				} catch (MissingFunctionException e) {
					markV1Only();
				}
			}
			if (response == null) response = rpc("zadrinth_online", body);
			JsonElement parsed = new JsonParser().parse(response);
			Set<String> result = new HashSet<>();
			Map<String, Loadout> outfits = new HashMap<>();
			if (parsed.isJsonArray()) {
				//v1: só os nomes
				for (JsonElement element : parsed.getAsJsonArray()) {
					if (element.isJsonPrimitive()) result.add(element.getAsString().toLowerCase(Locale.ROOT));
				}
			} else if (parsed.isJsonObject()) {
				//v2: { "nome": { cosméticos } }
				for (Map.Entry<String, JsonElement> entry : parsed.getAsJsonObject().entrySet()) {
					String player = entry.getKey().toLowerCase(Locale.ROOT);
					result.add(player);
					Loadout loadout = Loadout.fromJson(entry.getValue());
					if (!loadout.isEmpty()) outfits.put(player, loadout);
				}
			}
			//Só aplica se ainda estivermos no mesmo servidor
			if (server.equals(currentServer)) {
				online = Collections.unmodifiableSet(result);
				cosmetics = Collections.unmodifiableMap(outfits);
			}
		} catch (Exception e) {
			ZadrinthClient.LOGGER.warn("Falha ao buscar jogadores do Zadrinth: {}", e.toString());
		}
	}

	private static String rpc(String function, JsonObject body) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) new URL(URL + "/rest/v1/rpc/" + function).openConnection();
		connection.setRequestMethod("POST");
		connection.setConnectTimeout(5_000);
		connection.setReadTimeout(5_000);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("apikey", KEY);
		connection.setRequestProperty("Authorization", "Bearer " + KEY);
		connection.setRequestProperty("User-Agent", "ZadrinthClient/" + ZadrinthClient.version());
		byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
		try (OutputStream out = connection.getOutputStream()) {
			out.write(payload);
		}
		int code = connection.getResponseCode();
		InputStream in = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
		String text = "";
		if (in != null) {
			try (InputStream stream = in) {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				byte[] chunk = new byte[4096];
				int read;
				while ((read = stream.read(chunk)) != -1 && buffer.size() < 256 * 1024) {
					buffer.write(chunk, 0, read);
				}
				text = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
			}
		}
		connection.disconnect();
		//PostgREST responde 404 (PGRST202) quando a função não existe
		if (code == 404 || (code >= 400 && text.contains("PGRST202"))) throw new MissingFunctionException(function);
		if (code >= 400) throw new IllegalStateException("HTTP " + code + ": " + text);
		return text;
	}

	private static final class MissingFunctionException extends Exception {
		MissingFunctionException(String function) {
			super("função " + function + " não existe no Supabase");
		}
	}
}
