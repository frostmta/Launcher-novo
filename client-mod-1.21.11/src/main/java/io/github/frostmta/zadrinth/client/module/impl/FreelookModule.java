package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ModeSetting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Freelook (olhar em volta): segure Alt para a câmera ir para trás de você e
 * girar livre, enquanto o personagem continua olhando/andando para a frente.
 * O movimento do mouse vai só para a câmera (EntityMixin) e a câmera usa o
 * ângulo do freelook (CameraMixin).
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
	private CameraType previous = CameraType.FIRST_PERSON;

	public FreelookModule() {
		super("freelook", "Freelook", "Olhe em volta sem virar o personagem (tecla Alt).", Category.PVP, Icon.EYE, true);
		instance = this;
	}

	@Override
	public void tick(Minecraft client) {
		boolean want = false;
		if (isEnabled() && client.player != null && client.screen == null) {
			if (mode.index() == 1) {
				if (ZadrinthKeys.consume(ZadrinthKeys.FREELOOK) % 2 == 1) toggled = !toggled;
				want = toggled;
			} else {
				ZadrinthKeys.consume(ZadrinthKeys.FREELOOK);
				want = ZadrinthKeys.isDown(ZadrinthKeys.FREELOOK);
			}
		} else {
			ZadrinthKeys.consume(ZadrinthKeys.FREELOOK);
			if (client.player == null) toggled = false;
		}
		if (want && !active) start(client);
		else if (!want && active) stop(client);
	}

	private void start(Minecraft client) {
		active = true;
		cameraYaw = client.player.getYRot();
		cameraPitch = client.player.getXRot();
		previous = client.options.getCameraType();
		client.options.setCameraType(view.index() == 1 ? CameraType.THIRD_PERSON_FRONT : CameraType.THIRD_PERSON_BACK);
	}

	private void stop(Minecraft client) {
		active = false;
		client.options.setCameraType(previous);
	}

	@Override
	public void onDisable() {
		toggled = false;
		if (active) stop(Minecraft.getInstance());
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
	public static boolean onLook(Entity entity, double yaw, double pitch) {
		FreelookModule module = instance;
		if (module == null || !module.active || entity != Minecraft.getInstance().player) return false;
		module.cameraYaw += (float) yaw * 0.15F;
		float dy = (float) pitch * 0.15F;
		module.cameraPitch += module.invert.on() ? -dy : dy;
		module.cameraPitch = Math.max(-90.0F, Math.min(90.0F, module.cameraPitch));
		return true;
	}
}
