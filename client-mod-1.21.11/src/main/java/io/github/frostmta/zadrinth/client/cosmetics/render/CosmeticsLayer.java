package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticType;
import io.github.frostmta.zadrinth.client.cosmetics.Loadout;
import io.github.frostmta.zadrinth.client.cosmetics.Wardrobe;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * Camada do jogador que desenha a capa (com física de tecido), as asas e a
 * bandana. Os dados vêm do AvatarRendererMixin, que guarda no "render state"
 * o visual e a animação de cada jogador.
 */
public final class CosmeticsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	public static final RenderStateDataKey<Data> DATA = RenderStateDataKey.create(() -> "zadrinth-client:cosmetics");
	private static boolean loggedError;

	/**
	 * @param vanillaCape capa da conta para desenhar com física (null = deixa a do jogo)
	 */
	public record Data(@Nullable Loadout loadout, AnimState anim, @Nullable Identifier vanillaCape) {
	}

	public CosmeticsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	/** Chamado ao montar o estado de cada jogador (antes de desenhar). */
	public static void extract(Avatar avatar, AvatarRenderState state) {
		if (!(avatar instanceof AbstractClientPlayer player)) {
			state.setData(DATA, null);
			return;
		}
		Loadout loadout = Wardrobe.forPlayer(player);
		AnimState anim = AnimState.of(player);
		anim.update(player, state.capeLean / 2.0F);
		Identifier vanilla = null;
		boolean ownCape = loadout != null && loadout.equipped(CosmeticType.CAPE) != null;
		if (ownCape) {
			//A capa do Zadrinth fica no lugar da do jogo
			state.showCape = false;
		} else if (ZadrinthConfig.get().clothCape && state.showCape && state.skin.cape() != null) {
			//Capa da conta com física de tecido
			vanilla = state.skin.cape().texturePath();
			state.showCape = false;
		}
		if (loadout == null && vanilla == null) {
			state.setData(DATA, null);
			return;
		}
		state.setData(DATA, new Data(loadout, anim.copy(), vanilla));
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector queue, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
		Data data = state.getData(DATA);
		if (data == null || state.isInvisible) return;
		try {
			submitCosmetics(poseStack, queue, light, state, data);
		} catch (RuntimeException e) {
			//Um erro aqui não pode derrubar o jogo: só deixa de desenhar
			if (!loggedError) {
				loggedError = true;
				ZadrinthClient.LOGGER.error("Erro ao desenhar cosméticos", e);
			}
		}
	}

	private void submitCosmetics(PoseStack poseStack, SubmitNodeCollector queue, int light, AvatarRenderState state, Data data) {
		PlayerModel model = getParentModel();
		Loadout loadout = data.loadout();
		boolean cloth = ZadrinthConfig.get().clothCape;
		boolean elytra = state.chestEquipment.is(Items.ELYTRA);
		boolean chestplate = !state.chestEquipment.isEmpty() && !elytra;
		float lift = state.capeFlap;
		float swing = state.capeLean / 2.0F;
		float side = state.capeLean2;

		boolean capeDrawn = false;
		Cosmetic cape = loadout == null ? null : loadout.equipped(CosmeticType.CAPE);
		if (!elytra) {
			Identifier texture = null;
			CosmeticStyle style = null;
			if (cape != null) {
				style = loadout.style(CosmeticType.CAPE);
				texture = CosmeticTextures.get(cape, style.tinted());
			} else if (data.vanillaCape() != null) {
				texture = data.vanillaCape();
			}
			if (texture != null) {
				poseStack.pushPose();
				model.body.translateAndRotate(poseStack);
				CapeRenderer.submit(poseStack, queue, light, cape, texture, style, data.anim(), lift, swing, side, cloth, chestplate);
				poseStack.popPose();
				capeDrawn = true;
			}
		}
		if (loadout == null) return;

		Cosmetic wings = loadout.equipped(CosmeticType.WINGS);
		if (wings != null && !elytra) {
			CosmeticStyle style = loadout.style(CosmeticType.WINGS);
			Identifier texture = CosmeticTextures.get(wings, style.tinted());
			if (texture != null) {
				poseStack.pushPose();
				model.body.translateAndRotate(poseStack);
				WingsRenderer.submit(poseStack, queue, light, wings, texture, style, data.anim(), capeDrawn);
				poseStack.popPose();
			}
		}
		Cosmetic bandana = loadout.equipped(CosmeticType.BANDANA);
		if (bandana != null) {
			CosmeticStyle style = loadout.style(CosmeticType.BANDANA);
			Identifier texture = CosmeticTextures.get(bandana, style.tinted());
			if (texture != null) {
				poseStack.pushPose();
				model.head.translateAndRotate(poseStack);
				float headPitch = (float) Math.toDegrees(model.head.xRot);
				float wind = 16.0F + swing * 0.9F + Math.max(0.0F, -lift) * 0.5F;
				BandanaRenderer.submit(poseStack, queue, light, bandana, texture, style, data.anim(), headPitch, wind);
				poseStack.popPose();
			}
		}
	}
}
