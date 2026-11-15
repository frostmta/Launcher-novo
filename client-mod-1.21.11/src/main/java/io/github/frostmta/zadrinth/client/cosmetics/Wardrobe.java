package io.github.frostmta.zadrinth.client.cosmetics;

import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.presence.Presence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

/** Quem está usando o quê: você (configuração), os outros (sincronização) e as prévias da loja. */
public final class Wardrobe {
	private static AbstractClientPlayer previewPlayer;
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
	public static void setPreview(AbstractClientPlayer player, Loadout loadout) {
		previewPlayer = player;
		previewLoadout = loadout;
	}

	public static void clearPreview() {
		previewPlayer = null;
		previewLoadout = null;
	}

	/** O visual de um jogador (null = nenhum cosmético). */
	public static Loadout forPlayer(AbstractClientPlayer player) {
		if (player == null) return null;
		if (previewLoadout != null && player == previewPlayer) return previewLoadout;
		Minecraft client = Minecraft.getInstance();
		if (client.player != null && (player == client.player || player.getUUID().equals(client.player.getUUID()))) {
			return local();
		}
		if (!ZadrinthConfig.get().showOthersCosmetics) return null;
		return Presence.cosmeticsOf(player.getGameProfile().name());
	}
}
