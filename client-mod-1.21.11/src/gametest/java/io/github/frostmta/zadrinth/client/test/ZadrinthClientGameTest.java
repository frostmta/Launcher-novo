package io.github.frostmta.zadrinth.client.test;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticType;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetics;
import io.github.frostmta.zadrinth.client.cosmetics.ShopService;
import io.github.frostmta.zadrinth.client.cosmetics.Wardrobe;
import io.github.frostmta.zadrinth.client.gui.HudEditorScreen;
import io.github.frostmta.zadrinth.client.gui.ModuleSettingsScreen;
import io.github.frostmta.zadrinth.client.gui.ShopScreen;
import io.github.frostmta.zadrinth.client.gui.ZadrinthMenuScreen;
import io.github.frostmta.zadrinth.client.gui.ZadrinthTitleScreen;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.ModuleManager;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;

/**
 * Abre o Minecraft 1.21.11 de verdade com o Zadrinth Client e tira prints
 * das telas principais (o CI mostra as imagens no log).
 */
@SuppressWarnings("UnstableApiUsage")
public class ZadrinthClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		context.waitForScreen(ZadrinthTitleScreen.class);
		context.waitTicks(40);
		context.takeScreenshot("01_tela_inicial");

		context.setScreen(() -> new ZadrinthMenuScreen(new ZadrinthTitleScreen()));
		context.waitTicks(5);
		context.takeScreenshot("02_menu");

		context.setScreen(() -> new ModuleSettingsScreen(new ZadrinthMenuScreen(), module("keystrokes")));
		context.waitTicks(5);
		context.takeScreenshot("03_opcoes_keystrokes");

		context.setScreen(() -> new ShopScreen(new ZadrinthTitleScreen()));
		context.waitTicks(5);
		context.takeScreenshot("04_loja_sem_mundo");
		context.setScreen(ZadrinthTitleScreen::new);

		//Visual completo: capa animada, asas e bandana
		context.runOnClient(client -> {
			equip(CosmeticType.CAPE, "orgulho");
			equip(CosmeticType.WINGS, "celestial");
			equip(CosmeticType.BANDANA, "chamas");
			for (String id : new String[]{"keystrokes", "fps", "cps", "coordinates", "armor", "ping", "potions", "direction", "clock"}) {
				module(id).setEnabled(true);
			}
			ZadrinthConfig.save();
		});
		context.waitTicks(5);
		context.takeScreenshot("05_tela_inicial_com_cosmeticos");

		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getClientWorld().waitForChunksRender();
			context.waitTicks(20);
			context.takeScreenshot("06_jogo_hud");

			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			context.waitTicks(10);
			context.takeScreenshot("07_cosmeticos_costas");

			context.getInput().holdKeyFor(client -> client.keyUp, 30);
			context.takeScreenshot("08_cosmeticos_andando");

			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(10);
			context.takeScreenshot("09_cosmeticos_frente");
			//Itens novos: capa de traço + asas de dragão 3D
			context.runOnClient(client -> {
				equip(CosmeticType.CAPE, "lamina_sagrada");
				equip(CosmeticType.WINGS, "dragao_gelo");
				client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			});
			context.waitTicks(10);
			context.takeScreenshot("09b_dragao_costas");
			context.getInput().holdKeyFor(client -> client.keyUp, 25);
			context.takeScreenshot("09c_dragao_andando");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(10);
			context.takeScreenshot("09d_dragao_frente");
			context.runOnClient(client -> {
				equip(CosmeticType.CAPE, "estrela_polar");
				equip(CosmeticType.WINGS, "dragao_rubi");
				client.player.setYRot(client.player.getYRot() + 35.0F);
				client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			});
			context.waitTicks(10);
			context.takeScreenshot("09e_dragao_rubi_lado");
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));

			//Zoom (C) e freelook (Alt)
			context.getInput().holdKey(ZadrinthKeys.ZOOM);
			context.waitTicks(15);
			context.takeScreenshot("10_zoom");
			context.getInput().releaseKey(ZadrinthKeys.ZOOM);
			context.getInput().holdKey(ZadrinthKeys.FREELOOK);
			context.waitTicks(10);
			context.takeScreenshot("11_freelook");
			context.getInput().releaseKey(ZadrinthKeys.FREELOOK);
			context.waitTicks(5);

			context.getInput().pressKey(ZadrinthKeys.MENU);
			context.waitTicks(5);
			context.takeScreenshot("12_menu_no_jogo");

			context.setScreen(() -> new ShopScreen(null));
			context.waitTicks(10);
			context.takeScreenshot("13_loja_3d");

			context.setScreen(() -> new HudEditorScreen(null));
			context.waitTicks(5);
			context.takeScreenshot("14_editor_hud");

			context.setScreen(() -> new PauseScreen(true));
			context.waitTicks(5);
			context.takeScreenshot("15_pausa");
			context.setScreen(() -> null);

			context.getInput().holdKey(client -> client.keyPlayerList);
			context.waitTicks(5);
			context.takeScreenshot("16_tab");
			context.getInput().releaseKey(client -> client.keyPlayerList);
		}
		context.waitForScreen(ZadrinthTitleScreen.class);
		context.takeScreenshot("17_tela_inicial_final");
		//O teste do Fabric confere se terminou na tela inicial do jogo
		context.runOnClient(client -> ZadrinthClient.vanillaTitleForTests = true);
		context.setScreen(TitleScreen::new);
	}

	private static Module module(String id) {
		for (Module module : ModuleManager.all()) if (module.id.equals(id)) return module;
		throw new AssertionError("módulo não encontrado: " + id);
	}

	private static void equip(CosmeticType type, String id) {
		Cosmetic cosmetic = Cosmetics.get(type, id);
		if (cosmetic == null) throw new AssertionError("cosmético não encontrado: " + id);
		if (!ShopService.owns(cosmetic)) ShopService.acquire(cosmetic);
		Wardrobe.equip(cosmetic);
	}
}
