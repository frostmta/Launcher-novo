package io.github.frostmta.zadrinth.client.hud;

import io.github.frostmta.zadrinth.client.config.HudPosition;
import net.minecraft.client.MinecraftClient;

/** Um item do HUD que pode ser arrastado e redimensionado no editor. */
public interface HudElement {
	String name();

	boolean isEnabled();

	HudPosition position();

	/** Tamanho sem escala, em pixels da interface. */
	int width(MinecraftClient client, boolean editing);

	int height(MinecraftClient client, boolean editing);

	/** Desenha em (0, 0): o HudManager já aplica posição e escala. */
	void render(MinecraftClient client, boolean editing);
}
