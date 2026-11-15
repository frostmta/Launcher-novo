package io.github.frostmta.zadrinth.client.cosmetics;

/** Categorias de cosméticos (cada uma ocupa um "espaço" no jogador). */
public enum CosmeticType {
	CAPE("capes", "Capas", "c"),
	WINGS("wings", "Asas", "w"),
	BANDANA("bandanas", "Bandanas", "b");

	/** Pasta das texturas em assets/zadrinth-client/cosmetics/ */
	public final String folder;
	public final String label;
	/** Chave curta usada na sincronização */
	public final String shortKey;

	CosmeticType(String folder, String label, String shortKey) {
		this.folder = folder;
		this.label = label;
		this.shortKey = shortKey;
	}
}
