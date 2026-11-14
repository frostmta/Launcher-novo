package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ColorSetting;
import io.github.frostmta.zadrinth.client.stats.ClickCounter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

/** As teclas WASD, os botões do mouse e o pulo, acendendo quando apertados. */
public final class KeystrokesModule extends HudModule {
	private static final int K = 22;
	private static final int G = 2;
	private static final int WIDTH = K * 3 + G * 2;

	private final BoolSetting mouse = add(new BoolSetting("mouse", "Botões do mouse", true));
	private final BoolSetting cps = add(new BoolSetting("cps", "CPS nos botões", true));
	private final BoolSetting space = add(new BoolSetting("space", "Barra de espaço", true));
	private final BoolSetting fade = add(new BoolSetting("fade", "Animação suave", true));
	private final ColorSetting idleBg = add(new ColorSetting("idleBg", "Fundo (solto)", 0x6F000000, true));
	private final ColorSetting pressedBg = add(new ColorSetting("pressedBg", "Fundo (apertado)", 0xD8FFFFFF, true));
	private final ColorSetting idleText = add(new ColorSetting("idleText", "Texto (solto)", 0xFFFFFFFF, false));
	private final ColorSetting pressedText = add(new ColorSetting("pressedText", "Texto (apertado)", 0xFF111111, false));
	private final BoolSetting shadow = add(new BoolSetting("shadow", "Sombra no texto", false));

	/** Brilho de cada tecla: W A S D LMB RMB espaço */
	private final float[] glow = new float[7];
	private long lastFrame;

	public KeystrokesModule() {
		super("keystrokes", "Keystrokes", "Mostra as teclas que você está apertando.", Category.HUD, Icon.KEYBOARD, true, 0.005f, 0.005f);
	}

	@Override
	public int width(MinecraftClient client, boolean editing) {
		return WIDTH;
	}

	@Override
	public int height(MinecraftClient client, boolean editing) {
		int h = K * 2 + G;
		if (mouse.on()) h += G + K;
		if (space.on()) h += G + 11;
		return h;
	}

	@Override
	public void render(MinecraftClient client, boolean editing) {
		GameOptions o = client.options;
		long now = System.currentTimeMillis();
		float dt = lastFrame == 0 ? 0 : Math.min(0.2F, (now - lastFrame) / 1000.0F);
		lastFrame = now;
		boolean[] down = {
				pressed(o.forwardKey), pressed(o.leftKey), pressed(o.backKey), pressed(o.rightKey),
				pressed(o.attackKey), pressed(o.useKey), pressed(o.jumpKey)
		};
		for (int i = 0; i < glow.length; i++) {
			float target = down[i] ? 1 : 0;
			glow[i] = fade.on() ? glow[i] + (target - glow[i]) * Math.min(1, dt * 14) : target;
		}
		TextRenderer text = client.textRenderer;
		key(text, label(o.forwardKey, "W"), K + G, 0, K, K, glow[0]);
		key(text, label(o.leftKey, "A"), 0, K + G, K, K, glow[1]);
		key(text, label(o.backKey, "S"), K + G, K + G, K, K, glow[2]);
		key(text, label(o.rightKey, "D"), (K + G) * 2, K + G, K, K, glow[3]);
		int y = K * 2 + G * 2;
		if (mouse.on()) {
			int half = (WIDTH - G) / 2;
			key(text, cps.on() ? ClickCounter.left() + " CPS" : "LMB", 0, y, half, K, glow[4]);
			key(text, cps.on() ? ClickCounter.right() + " CPS" : "RMB", half + G, y, WIDTH - half - G, K, glow[5]);
			y += K + G;
		}
		if (space.on()) {
			DrawableHelper.fill(0, y, WIDTH, y + 11, mix(idleBg.argb(), pressedBg.argb(), glow[6]));
			int bar = mix(idleText.argb(), pressedText.argb(), glow[6]);
			DrawableHelper.fill(WIDTH / 2 - 14, y + 5, WIDTH / 2 + 14, y + 6, bar);
		}
	}

	private static boolean pressed(KeyBinding key) {
		return key != null && key.isPressed();
	}

	private static String label(KeyBinding key, String fallback) {
		if (key == null) return fallback;
		String name = GameOptions.getFormattedNameForKeyCode(key.getCode());
		return name == null || name.isEmpty() || name.length() > 3 ? fallback : name;
	}

	private void key(TextRenderer text, String label, int x, int y, int w, int h, float t) {
		DrawableHelper.fill(x, y, x + w, y + h, mix(idleBg.argb(), pressedBg.argb(), t));
		int color = mix(idleText.argb(), pressedText.argb(), t);
		int tx = x + (w - text.getStringWidth(label)) / 2;
		int ty = y + (h - 8) / 2;
		if (shadow.on()) text.drawWithShadow(label, (float) tx, (float) ty, color);
		else text.draw(label, tx, ty, color);
	}

	private static int mix(int a, int b, float t) {
		t = Math.max(0, Math.min(1, t));
		int aa = a >>> 24, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
		int ba = b >>> 24, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
		return (Math.round(aa + (ba - aa) * t) << 24) | (Math.round(ar + (br - ar) * t) << 16)
				| (Math.round(ag + (bg - ag) * t) << 8) | Math.round(ab + (bb - ab) * t);
	}
}
