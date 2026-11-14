package io.github.frostmta.zadrinth.client.module.impl;

import com.google.gson.JsonObject;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.feature.ToggleSprint;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.TextHudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/** ToggleSprint/ToggleSneak (a lógica fica em feature/ToggleSprint). */
public final class ToggleSprintModule extends TextHudModule {
	private static final String SAMPLE = "[Sprinting (Toggled)]";

	private final BoolSetting sprint = add(new BoolSetting("sprint", "Correr automático (toggle)", true,
			() -> ZadrinthConfig.get().toggleSprintEnabled, v -> ZadrinthConfig.get().toggleSprintEnabled = v));
	private final BoolSetting sneak = add(new BoolSetting("sneak", "Agachar automático (toggle)", false,
			() -> ZadrinthConfig.get().toggleSneakEnabled, v -> ZadrinthConfig.get().toggleSneakEnabled = v));
	private final BoolSetting showText = add(new BoolSetting("showText", "Mostrar texto na tela", true,
			() -> ZadrinthConfig.get().toggleSprintHud, v -> ZadrinthConfig.get().toggleSprintHud = v));

	public ToggleSprintModule() {
		super("togglesprint", "Toggle Sprint", "Corre e agacha sem segurar a tecla.", Category.PVP, Icon.SPRINT, true, 0.005f, 0.96f);
	}

	@Override
	protected boolean defaultBackground() {
		return false;
	}

	@Override
	protected List<String> lines(MinecraftClient client, boolean editing) {
		List<String> lines = new ArrayList<>();
		if (!showText.on() && !editing) return lines;
		String text = ToggleSprint.statusText(client);
		if (!text.isEmpty()) lines.add(text);
		else if (editing) lines.add(SAMPLE);
		return lines;
	}

	@Override
	public void load(JsonObject json) {
		super.load(json);
		//Primeira vez: usa a posição que já estava salva na versão anterior
		if (json == null) setPosition(ZadrinthConfig.get().toggleSprintHudPosition);
	}
}
