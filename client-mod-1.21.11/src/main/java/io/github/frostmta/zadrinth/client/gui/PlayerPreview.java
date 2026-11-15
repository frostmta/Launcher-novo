package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.cosmetics.Loadout;
import io.github.frostmta.zadrinth.client.cosmetics.Wardrobe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Desenha o seu jogador em 3D dentro de uma tela (como o inventário faz), mas
 * virado para o ângulo que quisermos e com o visual de prévia da loja.
 */
public final class PlayerPreview {
	private PlayerPreview() {
	}

	/**
	 * @param scale   pixels por bloco
	 * @param centerY altura do jogador (blocos, a partir dos pés) que fica no meio do retângulo
	 * @param yaw     0 = de frente, 180 = de costas
	 * @param pitch   câmera olhando de cima (graus)
	 */
	public static void draw(int x0, int y0, int x1, int y1, float scale, float centerY, float yaw, float pitch, Loadout loadout) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || x1 <= x0 || y1 <= y0) return;
		EntityRenderer<? super LocalPlayer, ?> renderer = client.getEntityRenderDispatcher().getRenderer(player);
		EntityRenderState state;
		Wardrobe.setPreview(player, loadout);
		try {
			state = renderer.createRenderState(player, 1.0F);
		} finally {
			Wardrobe.clearPreview();
		}
		state.lightCoords = 15728880;
		state.shadowPieces.clear();
		state.outlineColor = 0;
		if (state instanceof LivingEntityRenderState living) {
			living.bodyRot = 180.0F + yaw;
			living.yRot = 0.0F;
			living.xRot = 0.0F;
			living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
			living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
			living.scale = 1.0F;
		}
		Quaternionf camera = new Quaternionf().rotateX(pitch * (float) (Math.PI / 180.0));
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(camera);
		Ui.g().submitEntityRenderState(state, scale, new Vector3f(0.0F, centerY, 0.0F), rotation, camera, x0, y0, x1, y1);
	}
}
