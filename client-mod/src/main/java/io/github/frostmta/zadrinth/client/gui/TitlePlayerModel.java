package io.github.frostmta.zadrinth.client.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.cosmetics.Wardrobe;
import io.github.frostmta.zadrinth.client.cosmetics.render.PreviewCosmetics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.ModelPart;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;

/**
 * O seu jogador em 3D na tela inicial (ainda não existe mundo, então o modelo
 * é desenhado direto, com a sua skin, a sua capa e os cosméticos equipados).
 */
public final class TitlePlayerModel {
	private static final Identifier STEVE = new Identifier("textures/entity/steve.png");

	private static boolean started;
	private static volatile Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> pending;
	private static Identifier skin;
	private static Identifier cape;
	private static boolean slim;
	private static PlayerEntityModel model;
	private static boolean modelSlim;

	private TitlePlayerModel() {
	}

	/** Começa a buscar a skin (uma vez) e aplica quando chegar. Chame a cada quadro. */
	private static void loadSkin(MinecraftClient client) {
		if (!started) {
			started = true;
			skin = STEVE;
			try {
				final GameProfile profile = client.getSession().getProfile();
				UUID id = profile.getId();
				if (id != null) {
					skin = DefaultSkinHelper.getTexture(id);
					slim = "slim".equals(DefaultSkinHelper.getModel(id));
				}
				Thread thread = new Thread(() -> {
					try {
						GameProfile filled = client.getSessionService().fillProfileProperties(profile, false);
						pending = client.getSessionService().getTextures(filled, false);
					} catch (Throwable e) {
						ZadrinthClient.LOGGER.info("Skin da tela inicial indisponível: {}", e.toString());
					}
				}, "Zadrinth skin");
				thread.setDaemon(true);
				thread.start();
			} catch (Throwable e) {
				ZadrinthClient.LOGGER.info("Sem perfil para a tela inicial: {}", e.toString());
			}
		}
		Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = pending;
		if (textures != null) {
			pending = null;
			try {
				MinecraftProfileTexture skinTexture = textures.get(MinecraftProfileTexture.Type.SKIN);
				if (skinTexture != null) {
					skin = client.getSkinProvider().loadSkin(skinTexture, MinecraftProfileTexture.Type.SKIN);
					slim = "slim".equals(skinTexture.getMetadata("model"));
				}
				MinecraftProfileTexture capeTexture = textures.get(MinecraftProfileTexture.Type.CAPE);
				if (capeTexture != null) cape = client.getSkinProvider().loadSkin(capeTexture, MinecraftProfileTexture.Type.CAPE);
			} catch (Throwable e) {
				ZadrinthClient.LOGGER.info("Falha ao carregar a skin: {}", e.toString());
			}
		}
		if (model == null || modelSlim != slim) {
			model = new PlayerEntityModel(0.0F, slim);
			modelSlim = slim;
		}
	}

	/**
	 * @param x    centro horizontal
	 * @param y    pés
	 * @param size escala (altura ≈ 1.8 × size)
	 * @param yaw  giro do corpo (0 = de frente)
	 * @param lookX quanto a cabeça vira para o lado (-1..1)
	 */
	public static void draw(int x, int y, int size, float yaw, float lookX, float lookY) {
		MinecraftClient client = MinecraftClient.getInstance();
		loadSkin(client);
		float seconds = (System.currentTimeMillis() % 3_600_000L) / 1000.0F;
		pose(seconds, lookX, lookY);

		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, 100.0F);
		GlStateManager.scale((float) -size, (float) size, (float) size);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		DiffuseLighting.enableNormally();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-6.0F, 1.0F, 0.0F, 0.0F);
		//O mesmo que o renderizador de entidades faz
		GlStateManager.rotate(180.0F - yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);
		GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.disableCull();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		try {
			client.getTextureManager().bindTexture(skin);
			float s = 0.0625F;
			model.head.render(s);
			model.body.render(s);
			model.rightArm.render(s);
			model.leftArm.render(s);
			model.rightLeg.render(s);
			model.leftLeg.render(s);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);
			model.hat.render(s);
			model.jacket.render(s);
			model.leftSleeve.render(s);
			model.rightSleeve.render(s);
			model.leftPants.render(s);
			model.rightPants.render(s);
			GlStateManager.disableBlend();
			PreviewCosmetics.render(model, Wardrobe.local(), cape, seconds);
		} catch (RuntimeException e) {
			ZadrinthClient.LOGGER.warn("Erro no modelo da tela inicial: {}", e.toString());
		}
		GlStateManager.enableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		DiffuseLighting.disable();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	/** Respiração, braços balançando e a cabeça olhando para o dedo/mouse. */
	private static void pose(float seconds, float lookX, float lookY) {
		float t = seconds * 20.0F;
		model.head.posY = lookX * 0.7F;
		model.head.posX = lookY * 0.4F;
		copy(model.head, model.hat);
		model.body.posX = 0;
		model.rightArm.posZ = (float) Math.cos(t * 0.09F) * 0.05F + 0.05F;
		model.leftArm.posZ = -((float) Math.cos(t * 0.09F) * 0.05F + 0.05F);
		model.rightArm.posX = (float) Math.sin(t * 0.067F) * 0.05F;
		model.leftArm.posX = -(float) Math.sin(t * 0.067F) * 0.05F;
		model.rightLeg.posX = 0;
		model.leftLeg.posX = 0;
		//Mangas e calças seguem o braço/perna do mesmo lado (mesmo pivô)
		follow(model.rightArm, model.leftArm, model.rightSleeve, model.leftSleeve);
		follow(model.rightLeg, model.leftLeg, model.rightPants, model.leftPants);
		copy(model.body, model.jacket);
	}

	private static void follow(ModelPart a, ModelPart b, ModelPart overlay1, ModelPart overlay2) {
		copy(Math.signum(overlay1.pivotX) == Math.signum(a.pivotX) ? a : b, overlay1);
		copy(Math.signum(overlay2.pivotX) == Math.signum(a.pivotX) ? a : b, overlay2);
	}

	private static void copy(ModelPart from, ModelPart to) {
		to.posX = from.posX;
		to.posY = from.posY;
		to.posZ = from.posZ;
	}
}
