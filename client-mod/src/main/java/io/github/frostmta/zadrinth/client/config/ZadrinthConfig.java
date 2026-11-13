package io.github.frostmta.zadrinth.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.frostmta.zadrinth.client.ZadrinthClient;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/** Configuração do Zadrinth Client, salva em config/zadrinth-client.json. */
public final class ZadrinthConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ZadrinthConfig instance = new ZadrinthConfig();

	// --- ToggleSprint / ToggleSneak ---
	public boolean toggleSprintEnabled = true;
	public boolean toggleSneakEnabled = false;
	/** Corrida contínua ligada (fica salva entre as sessões) */
	public boolean sprintToggled = false;
	/** Agachar contínuo (não é salvo: sempre começa desligado) */
	public transient boolean sneakToggled = false;
	public boolean toggleSprintHud = true;
	public HudPosition toggleSprintHudPosition = new HudPosition(0.005f, 0.96f, 1.0f);
	public int toggleSprintTextColor = 0xFFFFFF;
	public boolean toggleSprintTextShadow = true;
	public boolean toggleSprintBackground = false;

	// --- Ícone no TAB e sincronização ---
	public boolean tabIconEnabled = true;
	/** Avisa ao servidor do Zadrinth que você está jogando (para outros verem o ícone) */
	public boolean sharePresence = true;

	public static ZadrinthConfig get() {
		return instance;
	}

	private static File file() {
		return new File(new File(MinecraftClient.getInstance().runDirectory, "config"), "zadrinth-client.json");
	}

	public static void load() {
		File file = file();
		if (!file.isFile()) {
			save();
			return;
		}
		try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			ZadrinthConfig loaded = GSON.fromJson(reader, ZadrinthConfig.class);
			if (loaded != null) {
				if (loaded.toggleSprintHudPosition == null) loaded.toggleSprintHudPosition = new HudPosition(0.005f, 0.96f, 1.0f);
				instance = loaded;
			}
		} catch (Exception e) {
			ZadrinthClient.LOGGER.warn("Configuração inválida, usando o padrão", e);
		}
	}

	public static void save() {
		File file = file();
		try {
			File parent = file.getParentFile();
			if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
				ZadrinthClient.LOGGER.warn("Não foi possível criar {}", parent);
			}
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
				GSON.toJson(instance, writer);
			}
		} catch (Exception e) {
			ZadrinthClient.LOGGER.warn("Não foi possível salvar a configuração", e);
		}
	}
}
