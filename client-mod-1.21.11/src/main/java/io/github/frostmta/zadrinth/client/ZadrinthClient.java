package io.github.frostmta.zadrinth.client;

import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.cosmetics.render.CosmeticsLayer;
import io.github.frostmta.zadrinth.client.feature.ToggleSprint;
import io.github.frostmta.zadrinth.client.gui.ZadrinthMenuScreen;
import io.github.frostmta.zadrinth.client.hud.HudManager;
import io.github.frostmta.zadrinth.client.module.ModuleManager;
import io.github.frostmta.zadrinth.client.module.impl.ScoreboardModule;
import io.github.frostmta.zadrinth.client.presence.Presence;
import io.github.frostmta.zadrinth.client.stats.CombatTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Zadrinth Client — Minecraft 1.21.11 (Fabric).
 * Ponto de entrada: teclas, HUD, camada de cosméticos e o "tick" do cliente.
 */
public final class ZadrinthClient implements ClientModInitializer {
	public static final String MOD_ID = "zadrinth-client";
	public static final String MINECRAFT = "1.21.11";
	public static final Logger LOGGER = LoggerFactory.getLogger("ZadrinthClient");
	public static final Properties BUILD = loadBuildProperties();

	@Override
	public void onInitializeClient() {
		ZadrinthKeys.register();
		ClientTickEvents.END_CLIENT_TICK.register(ZadrinthClient::onClientTick);

		//HUD do client depois de tudo; a mira própria no lugar da do jogo; o placar pode mudar de lugar/tamanho
		HudElementRegistry.addLast(id("hud"), HudManager::render);
		HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, tracker) -> {
			if (!HudManager.replaceCrosshair()) original.render(graphics, tracker);
		});
		HudElementRegistry.replaceElement(VanillaHudElements.SCOREBOARD, original -> (graphics, tracker) ->
				ScoreboardModule.render(graphics, () -> original.render(graphics, tracker)));

		//Capa, asas e bandana
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if (renderer instanceof AvatarRenderer<?> avatar) helper.register(new CosmeticsLayer(avatar));
		});

		//Botão do client no menu de pausa (ESC)
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (!(screen instanceof PauseScreen pause) || !pause.showsPauseMenu()) return;
			Screens.getButtons(screen).add(Button.builder(Component.literal("Zadrinth Client"),
					button -> client.setScreen(new ZadrinthMenuScreen(screen))).bounds(width / 2 - 102, 6, 204, 20).build());
		});

		ZadrinthConfig.load();
		LOGGER.info("Zadrinth Client {} para Minecraft {} carregado (sincronização: {})", version(), MINECRAFT,
				Presence.isConfigured() ? "ligada" : "sem servidor configurado");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static String version() {
		return BUILD.getProperty("version", "dev");
	}

	/** Fim de cada tick do Minecraft (20x por segundo). */
	private static void onClientTick(Minecraft client) {
		while (ZadrinthKeys.MENU.consumeClick()) {
			if (client.screen == null) client.setScreen(new ZadrinthMenuScreen());
		}
		ToggleSprint.tick(client);
		ModuleManager.tick(client);
		CombatTracker.tick(client);
		Presence.tick(client);
	}

	/** Usar a tela inicial do Zadrinth no lugar da do Minecraft. */
	public static boolean customTitleScreen() {
		return true;
	}

	private static Properties loadBuildProperties() {
		Properties properties = new Properties();
		try (InputStream in = ZadrinthClient.class.getResourceAsStream("/zadrinth-client.properties")) {
			if (in != null) properties.load(in);
		} catch (Exception e) {
			LoggerFactory.getLogger("ZadrinthClient").warn("Não foi possível ler zadrinth-client.properties", e);
		}
		return properties;
	}
}
