package io.github.frostmta.zadrinth.client.config;

/**
 * Posição de um elemento do HUD em fração da tela (0..1), para continuar no
 * mesmo lugar quando a escala da interface ou o tamanho da tela mudam.
 */
public final class HudPosition {
	public float x;
	public float y;
	public float scale;

	public HudPosition() {
		this(0.01f, 0.01f, 1.0f);
	}

	public HudPosition(float x, float y, float scale) {
		this.x = x;
		this.y = y;
		this.scale = scale;
	}
}
