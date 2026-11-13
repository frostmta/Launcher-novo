package io.github.frostmta.zadrinth.client.hud;

import io.github.frostmta.zadrinth.client.config.HudPosition;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import io.github.frostmta.zadrinth.client.feature.ToggleSprint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;

/** Mostra "[Sprinting (Toggled)]" e companhia na tela. */
public final class ToggleSprintHud implements HudElement {
	private static final String SAMPLE = "[Sprinting (Toggled)]";
	private static final int PADDING = 2;

	@Override
	public String name() {
		return "ToggleSprint";
	}

	@Override
	public boolean isEnabled() {
		ZadrinthConfig config = ZadrinthConfig.get();
		return config.toggleSprintHud && (config.toggleSprintEnabled || config.toggleSneakEnabled);
	}

	@Override
	public HudPosition position() {
		return ZadrinthConfig.get().toggleSprintHudPosition;
	}

	private String text(MinecraftClient client, boolean editing) {
		String text = ToggleSprint.statusText(client);
		return text.isEmpty() && editing ? SAMPLE : text;
	}

	@Override
	public int width(MinecraftClient client, boolean editing) {
		String text = text(client, editing);
		return text.isEmpty() ? 0 : client.textRenderer.getStringWidth(text) + PADDING * 2;
	}

	@Override
	public int height(MinecraftClient client, boolean editing) {
		return client.textRenderer.fontHeight + PADDING * 2;
	}

	@Override
	public void render(MinecraftClient client, boolean editing) {
		String text = text(client, editing);
		if (text.isEmpty()) return;
		ZadrinthConfig config = ZadrinthConfig.get();
		if (config.toggleSprintBackground) {
			DrawableHelper.fill(0, 0, width(client, editing), height(client, editing), 0x66000000);
		}
		int color = 0xFF000000 | config.toggleSprintTextColor;
		if (config.toggleSprintTextShadow) {
			client.textRenderer.drawWithShadow(text, PADDING, PADDING + 1, color);
		} else {
			client.textRenderer.draw(text, PADDING, PADDING + 1, color);
		}
	}
}
