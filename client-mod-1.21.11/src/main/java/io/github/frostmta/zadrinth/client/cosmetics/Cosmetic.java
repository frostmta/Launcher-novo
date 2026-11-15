package io.github.frostmta.zadrinth.client.cosmetics;

/**
 * Um item da loja.
 *
 * Preço e moeda já existem para a loja poder vender itens no futuro; por
 * enquanto tudo custa 0 (grátis).
 */
public final class Cosmetic {
	public enum Currency { FREE, COINS }

	public final String id;
	public final CosmeticType type;
	public final String name;
	public final String description;
	public final int price;
	public final Currency currency;
	/** Cor de destaque do cartão na loja (ARGB) */
	public final int accent;
	/** Quadros da animação (empilhados de cima para baixo na textura); 1 = parada */
	public int frames = 1;
	/** Duração de cada quadro (ms) */
	public int frameMillis = 70;

	Cosmetic(String id, CosmeticType type, String name, String description, int accent) {
		this(id, type, name, description, 0, Currency.FREE, accent);
	}

	Cosmetic(String id, CosmeticType type, String name, String description, int price, Currency currency, int accent) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.description = description;
		this.price = price;
		this.currency = currency;
		this.accent = accent;
	}

	/** Identificador único entre todas as categorias, ex.: "capes/nebulosa". */
	public String key() {
		return type.folder + "/" + id;
	}

	public boolean isFree() {
		return currency == Currency.FREE || price <= 0;
	}

	/** Deixa o item animado (a textura tem N quadros um embaixo do outro). */
	Cosmetic animated(int frames, int frameMillis) {
		this.frames = Math.max(1, frames);
		this.frameMillis = Math.max(16, frameMillis);
		return this;
	}

	/** Quadro atual da animação. */
	public int frame() {
		return frames <= 1 ? 0 : (int) ((System.currentTimeMillis() / frameMillis) % frames);
	}

	public String texturePath() {
		return "/assets/zadrinth-client/cosmetics/" + type.folder + "/" + id + ".png";
	}
}
