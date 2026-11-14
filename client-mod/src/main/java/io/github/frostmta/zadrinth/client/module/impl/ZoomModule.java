package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ModeSetting;
import io.github.frostmta.zadrinth.client.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Zoom (tecla C, como no CMClient/Lunar). Segure para aproximar; a rolagem do
 * mouse muda o zoom enquanto ele está ativo.
 */
public final class ZoomModule extends Module {
	private static ZoomModule instance;

	private final NumberSetting factor = add(new NumberSetting("factor", "Aproximação", 4.0F, 1.5F, 10.0F, 0.5F, "x"));
	private final ModeSetting mode = add(new ModeSetting("mode", "Ativar", 0, "Segurando a tecla", "Apertar para ligar/desligar"));
	private final BoolSetting smooth = add(new BoolSetting("smooth", "Zoom suave", true));
	private final BoolSetting scroll = add(new BoolSetting("scroll", "Rolagem muda o zoom", true));
	private final BoolSetting cinematic = add(new BoolSetting("cinematic", "Câmera suave no zoom", false));

	private boolean toggled;
	private boolean zooming;
	private float target = 1.0F;
	private float current = 1.0F;
	private float scrollFactor = 1.0F;
	private long lastFrame;
	private boolean changedCamera;

	public ZoomModule() {
		super("zoom", "Zoom", "Aproxima a visão (tecla C). Use a rolagem para mais ou menos zoom.", Category.VISUAL, Icon.ZOOM, true);
		instance = this;
	}

	@Override
	public void tick(MinecraftClient client) {
		boolean active = false;
		if (isEnabled() && client.currentScreen == null && client.player != null) {
			if (mode.index() == 1) {
				while (ZadrinthKeys.ZOOM.wasPressed()) toggled = !toggled;
				active = toggled;
			} else {
				active = ZadrinthKeys.ZOOM.isPressed();
			}
		} else {
			while (ZadrinthKeys.ZOOM.wasPressed()) {
				//descarta os toques enquanto o zoom está desligado
			}
		}
		if (active && !zooming) scrollFactor = 1.0F;
		zooming = active;
		target = zooming ? factor.value() * scrollFactor : 1.0F;

		boolean wantCamera = zooming && cinematic.on();
		if (wantCamera != changedCamera) {
			client.options.smoothCameraEnabled = wantCamera;
			changedCamera = wantCamera;
		}
	}

	/** Multiplicador do campo de visão (chamado pelo GameRendererMixin a cada quadro). */
	public static float fovMultiplier() {
		ZoomModule zoom = instance;
		if (zoom == null) return 1.0F;
		long now = System.currentTimeMillis();
		float dt = zoom.lastFrame == 0 ? 0 : Math.min(0.1F, (now - zoom.lastFrame) / 1000.0F);
		zoom.lastFrame = now;
		if (zoom.smooth.on()) zoom.current += (zoom.target - zoom.current) * Math.min(1.0F, dt * 14.0F);
		else zoom.current = zoom.target;
		if (Math.abs(zoom.current - 1.0F) < 0.001F) zoom.current = 1.0F;
		return 1.0F / zoom.current;
	}

	public static boolean isZooming() {
		return instance != null && instance.zooming;
	}

	/** Rolagem durante o zoom (PlayerInventoryMixin). true = consumiu. */
	public static boolean onScroll(int amount) {
		ZoomModule zoom = instance;
		if (zoom == null || !zoom.zooming || !zoom.scroll.on() || amount == 0) return false;
		zoom.scrollFactor *= amount > 0 ? 1.25F : 0.8F;
		zoom.scrollFactor = Math.max(0.3F, Math.min(5.0F, zoom.scrollFactor));
		return true;
	}

	@Override
	public void onDisable() {
		toggled = false;
	}
}
