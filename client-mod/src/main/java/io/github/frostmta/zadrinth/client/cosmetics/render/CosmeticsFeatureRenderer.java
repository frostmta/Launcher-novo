package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticType;
import io.github.frostmta.zadrinth.client.cosmetics.Loadout;
import io.github.frostmta.zadrinth.client.cosmetics.Wardrobe;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * Camada de render do jogador que desenha a capa (substitui a do Minecraft,
 * com física de tecido), as asas e a bandana.
 */
@SuppressWarnings("rawtypes")
public final class CosmeticsFeatureRenderer implements FeatureRenderer {
	private static PlayerModelPart capePart;
	private static boolean loggedError;

	private final PlayerEntityRenderer renderer;

	public CosmeticsFeatureRenderer(PlayerEntityRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void render(LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float age,
					   float headYaw, float headPitch, float scale) {
		if (!(entity instanceof AbstractClientPlayerEntity)) return;
		AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) entity;
		if (player.isInvisible()) return;
		try {
			renderPlayer(player, tickDelta);
		} catch (RuntimeException e) {
			//Um erro aqui não pode derrubar o jogo: só deixa de desenhar
			if (!loggedError) {
				loggedError = true;
				ZadrinthClient.LOGGER.error("Erro ao desenhar cosméticos", e);
			}
		} finally {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	private void renderPlayer(AbstractClientPlayerEntity player, float tickDelta) {
		Loadout loadout = Wardrobe.forPlayer(player);
		PlayerEntityModel model = (PlayerEntityModel) renderer.getModel();
		Motion motion = Motion.of(player, tickDelta);
		AnimState anim = AnimState.of(player);
		anim.update(player, motion);
		boolean cloth = ZadrinthConfig.get().clothCape;

		boolean capeDrawn = false;
		Cosmetic cape = loadout == null ? null : loadout.equipped(CosmeticType.CAPE);
		if (cape != null) {
			CosmeticStyle style = loadout.style(CosmeticType.CAPE);
			Identifier texture = CosmeticTextures.get(cape, style.tinted());
			if (texture != null) {
				Frames.begin(cape);
				CapeRenderer.render(player, motion, anim, texture, style, cloth);
				Frames.end(cape);
				capeDrawn = true;
			}
		}
		if (!capeDrawn && hasVanillaCape(player)) {
			CapeRenderer.render(player, motion, anim, player.getCapeId(), null, cloth);
			capeDrawn = true;
		}
		if (loadout == null) return;

		Cosmetic wings = loadout.equipped(CosmeticType.WINGS);
		if (wings != null) {
			CosmeticStyle style = loadout.style(CosmeticType.WINGS);
			Identifier texture = CosmeticTextures.get(wings, style.tinted());
			if (texture != null) {
				Frames.begin(wings);
				WingsRenderer.render(player, model, anim, texture, style, capeDrawn);
				Frames.end(wings);
			}
		}
		Cosmetic bandana = loadout.equipped(CosmeticType.BANDANA);
		if (bandana != null) {
			CosmeticStyle style = loadout.style(CosmeticType.BANDANA);
			Identifier texture = CosmeticTextures.get(bandana, style.tinted());
			if (texture != null) {
				Frames.begin(bandana);
				BandanaRenderer.render(player, model, motion, anim, texture, style);
				Frames.end(bandana);
			}
		}
	}

	/** Mesmas condições da capa do Minecraft. */
	private static boolean hasVanillaCape(AbstractClientPlayerEntity player) {
		if (!player.canRenderCapeTexture() || player.getCapeId() == null) return false;
		PlayerModelPart part = capePart();
		return part == null || player.isPartVisible(part);
	}

	private static PlayerModelPart capePart() {
		if (capePart == null) {
			for (PlayerModelPart part : PlayerModelPart.values()) {
				if ("cape".equals(part.getName())) capePart = part;
			}
		}
		return capePart;
	}

	@Override
	public boolean combineTextures() {
		return false;
	}
}
