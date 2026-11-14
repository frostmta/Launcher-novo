package io.github.frostmta.zadrinth.client.cosmetics;

import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.presence.Presence;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;

/** Quem está usando o quê: você (configuração), os outros (sincronização) e as prévias da loja. */
public final class Wardrobe {
	private static AbstractClientPlayerEntity previewPlayer;
	private static Loadout previewLoadout;

	private Wardrobe() {
	}

	public static Loadout local() {
		ZadrinthConfig config = ZadrinthConfig.get();
		if (config.loadout == null) config.loadout = new Loadout();
		return config.loadout;
	}

	public static boolean isEquipped(Cosmetic cosmetic) {
		return cosmetic != null && cosmetic.id.equals(local().get(cosmetic.type));
	}

	public static void equip(Cosmetic cosmetic) {
		if (!ShopService.owns(cosmetic)) return;
		local().set(cosmetic.type, cosmetic.id);
		changed();
	}

	public static void unequip(CosmeticType type) {
		local().set(type, null);
		changed();
	}

	/** Salva e avisa os outros jogadores o quanto antes. */
	public static void changed() {
		ZadrinthConfig.save();
		Presence.requestHeartbeat();
	}

	/** Durante a prévia da loja, este jogador aparece com outro visual. */
	public static void setPreview(AbstractClientPlayerEntity player, Loadout loadout) {
		previewPlayer = player;
		previewLoadout = loadout;
	}

	public static void clearPreview() {
		previewPlayer = null;
		previewLoadout = null;
	}

	/** O visual de um jogador (null = nenhum cosmético). */
	public static Loadout forPlayer(AbstractClientPlayerEntity player) {
		if (player == null) return null;
		if (previewLoadout != null && player == previewPlayer) return previewLoadout;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null && (player == client.player || player.getUuid().equals(client.player.getUuid()))) {
			return local();
		}
		if (!ZadrinthConfig.get().showOthersCosmetics) return null;
		return Presence.cosmeticsOf(player.getGameProfile().getName());
	}
}
