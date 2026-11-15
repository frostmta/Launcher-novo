package io.github.frostmta.zadrinth.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.cosmetics.Loadout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Cosméticos divididos com o launcher. O launcher abre o jogo com
 * -Dzadrinth.home=&lt;pasta&gt; e a Loja do launcher grava o mesmo cosmetics.json,
 * então o que você resgata/equipa lá aparece no jogo (e vice-versa), em
 * qualquer versão do client.
 */
public final class SharedCosmetics {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private SharedCosmetics() {
	}

	private static final class Data {
		Set<String> owned = new LinkedHashSet<>();
		Loadout loadout = new Loadout();
		Boolean clothCape;
		Boolean showOthersCosmetics;
	}

	private static File file() {
		String home = System.getProperty("zadrinth.home");
		if (home == null || home.isEmpty()) return null;
		return new File(home, "cosmetics.json");
	}

	/** Lê o arquivo do launcher (se existir) por cima da configuração. */
	public static void load(ZadrinthConfig config) {
		File file = file();
		if (file == null || !file.isFile()) return;
		try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			Data data = GSON.fromJson(reader, Data.class);
			if (data == null) return;
			if (data.owned != null) {
				//Junta: nada que você já tinha é perdido
				config.ownedCosmetics.addAll(data.owned);
			}
			if (data.loadout != null) config.loadout = data.loadout;
			if (data.clothCape != null) config.clothCape = data.clothCape;
			if (data.showOthersCosmetics != null) config.showOthersCosmetics = data.showOthersCosmetics;
		} catch (Exception e) {
			ZadrinthClient.LOGGER.warn("Não foi possível ler {}: {}", file, e.toString());
		}
	}

	public static void save(ZadrinthConfig config) {
		File file = file();
		if (file == null) return;
		Data data = new Data();
		data.owned = new LinkedHashSet<>(config.ownedCosmetics);
		data.loadout = config.loadout;
		data.clothCape = config.clothCape;
		data.showOthersCosmetics = config.showOthersCosmetics;
		try {
			File parent = file.getParentFile();
			if (parent != null && !parent.isDirectory()) parent.mkdirs();
			File temp = new File(file.getPath() + ".tmp");
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8)) {
				GSON.toJson(data, writer);
			}
			if (!temp.renameTo(file)) {
				file.delete();
				temp.renameTo(file);
			}
		} catch (Exception e) {
			ZadrinthClient.LOGGER.warn("Não foi possível salvar {}: {}", file, e.toString());
		}
	}
}
