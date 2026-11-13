package io.github.frostmta.zadrinth.client;

import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.feature.ToggleSprint;
import io.github.frostmta.zadrinth.client.gui.ZadrinthMenuScreen;
import io.github.frostmta.zadrinth.client.presence.Presence;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * Zadrinth Client — mod do jogo (Minecraft 1.8.9, Legacy Fabric).
 * Ponto de entrada: carrega a configuração e recebe o "tick" do cliente.
 */
public final class ZadrinthClient implements ClientModInitializer {
	public static final String MOD_ID = "zadrinth-client";
	public static final Logger LOGGER = LogManager.getLogger("ZadrinthClient");
	public static final Properties BUILD = loadBuildProperties();

	@Override
	public void onInitializeClient() {
		ZadrinthConfig.load();
		LOGGER.info("Zadrinth Client {} carregado (sincronização: {})", version(),
				Presence.isConfigured() ? "ligada" : "sem servidor configurado");
	}

	public static String version() {
		return BUILD.getProperty("version", "dev");
	}

	/** Chamado no fim de cada tick do Minecraft (20x por segundo). */
	public static void onClientTick(MinecraftClient client) {
		while (ZadrinthKeys.MENU.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new ZadrinthMenuScreen());
			}
		}
		ToggleSprint.tick(client);
		Presence.tick(client);
	}

	private static Properties loadBuildProperties() {
		Properties properties = new Properties();
		try (InputStream in = ZadrinthClient.class.getResourceAsStream("/zadrinth-client.properties")) {
			if (in != null) properties.load(in);
		} catch (Exception e) {
			LogManager.getLogger("ZadrinthClient").warn("Não foi possível ler zadrinth-client.properties", e);
		}
		return properties;
	}
}
