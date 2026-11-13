package io.github.frostmta.zadrinth.client.presence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sincronização entre jogadores do Zadrinth Client (para o ícone no TAB).
 *
 * Como funciona:
 * 1. Enquanto você está num servidor, o client avisa a cada 60 s:
 *    "o jogador NOME está no servidor ENDEREÇO usando o Zadrinth" (heartbeat).
 * 2. A cada 30 s (10 s com o TAB aberto), ele pergunta quais nomes da lista
 *    de jogadores daquele servidor também mandaram esse aviso recentemente.
 * 3. Quem estiver na resposta ganha o ícone do Zadrinth no TAB.
 *
 * O servidor é um projeto Supabase (veja docs/supabase.sql). Só vão para lá
 * o endereço do servidor, o seu nome e os nomes da lista do TAB.
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
			EXECUTOR.execute(() -> heartbeat(server, name));
		}

		if (!config.tabIconEnabled) return;
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

	private static void heartbeat(String server, String name) {
		JsonObject body = new JsonObject();
		body.addProperty("p_server", server);
		body.addProperty("p_name", name);
		body.addProperty("p_version", ZadrinthClient.version());
		try {
			rpc("zadrinth_heartbeat", body);
		} catch (Exception e) {
			ZadrinthClient.LOGGER.warn("Falha ao avisar presença: {}", e.toString());
		}
	}

	private static void query(String server, List<String> names) {
		JsonObject body = new JsonObject();
		body.addProperty("p_server", server);
		JsonArray array = new JsonArray();
		for (String name : names) array.add(new com.google.gson.JsonPrimitive(name));
		body.add("p_names", array);
		try {
			String response = rpc("zadrinth_online", body);
			JsonElement parsed = new JsonParser().parse(response);
			Set<String> result = new HashSet<>();
			if (parsed.isJsonArray()) {
				for (JsonElement element : parsed.getAsJsonArray()) {
					if (element.isJsonPrimitive()) result.add(element.getAsString().toLowerCase(Locale.ROOT));
				}
			}
			//Só aplica se ainda estivermos no mesmo servidor
			if (server.equals(currentServer)) {
				online = Collections.unmodifiableSet(result);
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
		if (code >= 400) throw new IllegalStateException("HTTP " + code + ": " + text);
		return text;
	}
}
