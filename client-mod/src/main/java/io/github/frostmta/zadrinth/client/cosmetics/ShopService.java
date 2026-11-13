package io.github.frostmta.zadrinth.client.cosmetics;

import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;

/**
 * Regras da loja. Hoje tudo é grátis: "Resgatar" só adiciona o item à sua
 * coleção. A estrutura de preço/moeda/saldo já está aqui para a loja poder
 * vender itens depois (com o saldo vindo de um servidor).
 */
public final class ShopService {
	public enum Result { OK, ALREADY_OWNED, NOT_ENOUGH_COINS }

	private ShopService() {
	}

	/** Moedas do jogador (ainda não existem: sempre 0). */
	public static int balance() {
		return 0;
	}

	public static boolean owns(Cosmetic cosmetic) {
		return cosmetic != null && ZadrinthConfig.get().ownedCosmetics.contains(cosmetic.key());
	}

	public static boolean canAcquire(Cosmetic cosmetic) {
		return cosmetic.isFree() || balance() >= cosmetic.price;
	}

	/** Resgata (grátis) ou compra (futuro) um item. */
	public static Result acquire(Cosmetic cosmetic) {
		if (owns(cosmetic)) return Result.ALREADY_OWNED;
		if (!canAcquire(cosmetic)) return Result.NOT_ENOUGH_COINS;
		ZadrinthConfig.get().ownedCosmetics.add(cosmetic.key());
		ZadrinthConfig.save();
		return Result.OK;
	}

	/** Texto do preço no cartão ("GRÁTIS" ou "250 moedas"). */
	public static String priceLabel(Cosmetic cosmetic) {
		return cosmetic.isFree() ? "GRÁTIS" : cosmetic.price + " moedas";
	}
}
