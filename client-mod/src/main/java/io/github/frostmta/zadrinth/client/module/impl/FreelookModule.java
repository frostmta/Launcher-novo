package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

/**
 * Freelook (olhar em volta): segure F para a câmera ir para trás de você e
 * girar livre, enquanto o personagem continua olhando/andando para a frente.
 * O movimento do mouse vai só para a câmera (EntityMixin) e a câmera usa o
 * ângulo do freelook (GameRendererMixin).
 */
public final class FreelookModule extends Module {
	private static FreelookModule instance;

	private final ModeSetting mode = add(new ModeSetting("mode", "Ativar", 0, "Segurando a tecla", "Apertar para ligar/desligar"));
	private final ModeSetting view = add(new ModeSetting("view", "Câmera", 0, "Atrás de você", "Na sua frente"));
	private final BoolSetting invert = add(new BoolSetting("invert", "Inverter vertical", false));

	private boolean toggled;
	private boolean active;
	private float cameraYaw;
	private float cameraPitch;
	private int previousPerspective;

	public FreelookModule() {
		super("freelook", "Freelook", "Olhe em volta sem virar o personagem (tecla F).", Category.PVP, Icon.EYE, true);
		instance = this;
	}

	@Override
	public void tick(MinecraftClient client) {
		boolean want = false;
		if (isEnabled() && client.player != null && client.currentScreen == null) {
			if (mode.index() == 1) {
				while (ZadrinthKeys.FREELOOK.wasPressed()) toggled = !toggled;
				want = toggled;
			} else {
				want = ZadrinthKeys.FREELOOK.isPressed();
			}
		} else {
			while (ZadrinthKeys.FREELOOK.wasPressed()) {
				//descarta os toques enquanto o freelook está desligado
			}
			if (client.player == null) toggled = false;
		}
		if (want && !active) start(client);
		else if (!want && active) stop(client);
	}

	private void start(MinecraftClient client) {
		active = true;
		cameraYaw = client.player.yaw;
		cameraPitch = client.player.pitch;
		previousPerspective = client.options.perspective;
		client.options.perspective = view.index() == 1 ? 2 : 1;
	}

	private void stop(MinecraftClient client) {
		active = false;
		client.options.perspective = previousPerspective;
	}

	@Override
	public void onDisable() {
		toggled = false;
		if (active) stop(MinecraftClient.getInstance());
	}

	public static boolean active() {
		return instance != null && instance.active;
	}

	public static float cameraYaw() {
		return instance.cameraYaw;
	}

	public static float cameraPitch() {
		return instance.cameraPitch;
	}

	/** Movimento do mouse (true = foi para a câmera, o personagem não gira). */
	public static boolean onLook(Entity entity, float yaw, float pitch) {
		FreelookModule module = instance;
		if (module == null || !module.active || entity != MinecraftClient.getInstance().player) return false;
		module.cameraYaw += yaw * 0.15F;
		float dy = pitch * 0.15F;
		module.cameraPitch += module.invert.on() ? dy : -dy;
		module.cameraPitch = Math.max(-90.0F, Math.min(90.0F, module.cameraPitch));
		return true;
	}
}
