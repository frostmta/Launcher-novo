package io.github.frostmta.zadrinth.client.cosmetics.render;

import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticType;
import io.github.frostmta.zadrinth.client.cosmetics.Loadout;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/**
 * Cosméticos num modelo de jogador solto, sem entidade no mundo (a tela
 * inicial ainda não tem mundo). As posições já devem estar no espaço do
 * modelo (o mesmo das camadas do jogador).
 */
public final class PreviewCosmetics {
	private PreviewCosmetics() {
	}

	/**
	 * @param officialCape capa da conta (oficial ou do Armário), usada se não houver capa do Zadrinth
	 */
	public static void render(PlayerEntityModel model, Loadout loadout, Identifier officialCape, float seconds) {
		boolean capeDrawn = false;
		Cosmetic cape = loadout == null ? null : loadout.equipped(CosmeticType.CAPE);
		if (cape != null) {
			CosmeticStyle style = loadout.style(CosmeticType.CAPE);
			Identifier texture = CosmeticTextures.get(cape, style.tinted());
			if (texture != null) {
				Frames.begin(cape);
				CapeRenderer.renderPreview(texture, style, seconds);
				Frames.end(cape);
				capeDrawn = true;
			}
		}
		if (!capeDrawn && officialCape != null) {
			CapeRenderer.renderPreview(officialCape, null, seconds);
			capeDrawn = true;
		}
		if (loadout == null) return;
		Cosmetic wings = loadout.equipped(CosmeticType.WINGS);
		if (wings != null) {
			CosmeticStyle style = loadout.style(CosmeticType.WINGS);
			Identifier texture = CosmeticTextures.get(wings, style.tinted());
			if (texture != null) {
				Frames.begin(wings);
				WingsRenderer.renderPreview(model, texture, style, capeDrawn, seconds);
				Frames.end(wings);
			}
		}
		Cosmetic bandana = loadout.equipped(CosmeticType.BANDANA);
		if (bandana != null) {
			CosmeticStyle style = loadout.style(CosmeticType.BANDANA);
			Identifier texture = CosmeticTextures.get(bandana, style.tinted());
			if (texture != null) {
				Frames.begin(bandana);
				BandanaRenderer.renderPreview(model, texture, style, seconds);
				Frames.end(bandana);
			}
		}
	}
}
