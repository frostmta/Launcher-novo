package io.github.frostmta.zadrinth.client.gui;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.cosmetics.Loadout;
import io.github.frostmta.zadrinth.client.cosmetics.Wardrobe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL11;

/**
 * Desenha o seu jogador em 3D dentro de uma tela (como o inventário faz), mas
 * virado para o ângulo que quisermos e com um visual de prévia.
 */
public final class PlayerPreview {
	private PlayerPreview() {
	}

	/**
	 * @param x    centro horizontal
	 * @param y    posição dos pés
	 * @param size escala (≈ altura do jogador / 1.8)
	 * @param yaw  0 = de frente, 180 = de costas
	 */
	public static void draw(int x, int y, int size, float yaw, float pitch, Loadout loadout) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!(client.player instanceof AbstractClientPlayerEntity)) return;
		AbstractClientPlayerEntity player = client.player;

		float bodyYaw = player.bodyYaw, prevBodyYaw = player.prevBodyYaw;
		float entityYaw = player.yaw, prevEntityYaw = player.prevYaw;
		float entityPitch = player.pitch, prevEntityPitch = player.prevPitch;
		float headYaw = player.headYaw, prevHeadYaw = player.prevHeadYaw;

		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, 50.0F);
		GlStateManager.scale((float) -size, (float) size, (float) size);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		DiffuseLighting.enableNormally();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);

		player.bodyYaw = yaw;
		player.prevBodyYaw = yaw;
		player.yaw = yaw;
		player.prevYaw = yaw;
		player.headYaw = yaw;
		player.prevHeadYaw = yaw;
		player.pitch = 0.0F;
		player.prevPitch = 0.0F;

		EntityRenderDispatcher dispatcher = client.getEntityRenderManager();
		dispatcher.setYaw(180.0F);
		dispatcher.setRenderShadows(false);
		Wardrobe.setPreview(player, loadout);
		try {
			dispatcher.render(player, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
		} finally {
			Wardrobe.clearPreview();
			dispatcher.setRenderShadows(true);
			player.bodyYaw = bodyYaw;
			player.prevBodyYaw = prevBodyYaw;
			player.yaw = entityYaw;
			player.prevYaw = prevEntityYaw;
			player.pitch = entityPitch;
			player.prevPitch = prevEntityPitch;
			player.headYaw = headYaw;
			player.prevHeadYaw = prevHeadYaw;
			GlStateManager.popMatrix();
			DiffuseLighting.disable();
			GlStateManager.disableRescaleNormal();
			GlStateManager.activeTexture(GLX.lightmapTextureUnit);
			GlStateManager.disableTexture();
			GlStateManager.activeTexture(GLX.textureUnit);
			GlStateManager.enableTexture();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	/** Recorta o que for desenhado a seguir a um retângulo da tela (coordenadas da GUI). */
	public static void clip(int x, int y, int w, int h) {
		MinecraftClient client = MinecraftClient.getInstance();
		int scale = new Window(client).getScaleFactor();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x * scale, client.height - (y + h) * scale, Math.max(0, w * scale), Math.max(0, h * scale));
	}

	public static void endClip() {
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
}
