package io.github.frostmta.zadrinth.client.cosmetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catálogo de cosméticos do Zadrinth Client (artes originais). */
public final class Cosmetics {
	private static final Map<String, Cosmetic> BY_KEY = new LinkedHashMap<>();

	static {
		//Capas
		add(new Cosmetic("nebulosa", CosmeticType.CAPE, "Nebulosa",
				"Uma estrela nascendo entre nuvens de poeira cósmica.", 0xFF9D7BFF));
		add(new Cosmetic("aurora", CosmeticType.CAPE, "Aurora Boreal",
				"Luzes do norte dançando sobre montanhas com neve.", 0xFF2FD3A0));
		add(new Cosmetic("lua_carmesim", CosmeticType.CAPE, "Lua Carmesim",
				"Uma lua de sangue sobre a floresta em brasas.", 0xFFE0453A));
		add(new Cosmetic("cristal", CosmeticType.CAPE, "Cristal Zadrinth",
				"O cristal da marca, lapidado em violeta.", 0xFFB77BFF));
		add(new Cosmetic("abismo", CosmeticType.CAPE, "Abismo",
				"Uma água-viva brilhando no fundo do oceano.", 0xFF36C9E6));
		add(new Cosmetic("chamas_etereas", CosmeticType.CAPE, "Chamas Etéreas",
				"Fogo espiritual azul e violeta.", 0xFF4FB8FF));

		//Asas
		add(new Cosmetic("demonio", CosmeticType.WINGS, "Asas de Demônio",
				"Membrana carmesim com garras de osso.", 0xFFE2384A));
		add(new Cosmetic("dragao_esmeralda", CosmeticType.WINGS, "Dragão Esmeralda",
				"Escamas verdes e espinhos ao longo do braço.", 0xFF2FD37A));
		add(new Cosmetic("celestial", CosmeticType.WINGS, "Asas Celestiais",
				"Penas brancas com pontas douradas.", 0xFFF2C65A));
		add(new Cosmetic("sombra", CosmeticType.WINGS, "Asas da Sombra",
				"Penas escuras com brilho violeta.", 0xFF8A76D0));
		add(new Cosmetic("cristal", CosmeticType.WINGS, "Asas de Cristal",
				"Lâminas de cristal que refletem a luz.", 0xFFC9B8FF));

		//Bandanas
		add(new Cosmetic("ninja", CosmeticType.BANDANA, "Ninja Violeta",
				"Faixa preta com o cristal do Zadrinth.", 0xFF9D7BFF));
		add(new Cosmetic("paisley", CosmeticType.BANDANA, "Paisley Vermelha",
				"A estampa clássica, em vermelho.", 0xFFD0262F));
		add(new Cosmetic("chamas", CosmeticType.BANDANA, "Chamas",
				"Fogo laranja sobre tecido preto.", 0xFFFF8A1E));
		add(new Cosmetic("galaxia", CosmeticType.BANDANA, "Galáxia",
				"Nebulosas e estrelas em volta da cabeça.", 0xFF6E9BFF));
		add(new Cosmetic("zadrinth", CosmeticType.BANDANA, "Zadrinth",
				"O degradê da marca com o cristal na testa.", 0xFFE07BFF));
	}

	private Cosmetics() {
	}

	private static void add(Cosmetic cosmetic) {
		BY_KEY.put(cosmetic.key(), cosmetic);
	}

	public static List<Cosmetic> all() {
		return Collections.unmodifiableList(new ArrayList<>(BY_KEY.values()));
	}

	public static Cosmetic get(CosmeticType type, String id) {
		if (type == null || id == null || id.isEmpty()) return null;
		return BY_KEY.get(type.folder + "/" + id);
	}

	public static Cosmetic byKey(String key) {
		return key == null ? null : BY_KEY.get(key);
	}
}
