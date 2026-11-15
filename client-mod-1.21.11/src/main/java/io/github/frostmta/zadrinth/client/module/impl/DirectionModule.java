package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.gui.Ui;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ColorSetting;
import net.minecraft.client.Minecraft;

/** Bússola em faixa no topo da tela (N, NE, L...). */
public final class DirectionModule extends HudModule {
	private static final int W = 150;
	private static final int H = 18;
	private static final String[] CARDINAL = {"N", "NE", "L", "SE", "S", "SO", "O", "NO"};

	private final BoolSetting background = add(new BoolSetting("background", "Fundo", true));
	private final ColorSetting backgroundColor = add(new ColorSetting("backgroundColor", "Cor do fundo", 0x6F000000, true));
	private final ColorSetting textColor = add(new ColorSetting("textColor", "Cor das letras", 0xFFFFFFFF, false));
	private final ColorSetting markerColor = add(new ColorSetting("markerColor", "Cor do marcador", 0xFFE07BFF, false));
	private final BoolSetting degrees = add(new BoolSetting("degrees", "Mostrar graus", true));

	public DirectionModule() {
		super("direction", "Bússola", "Para onde você está olhando.", Category.HUD, Icon.COMPASS, false, 0.40f, 0.005f);
	}

	@Override
	public int width(Minecraft client, boolean editing) {
		return W;
	}

	@Override
	public int height(Minecraft client, boolean editing) {
		return H;
	}

	@Override
	public void render(Minecraft client, boolean editing) {
		float yaw = client.player != null ? client.player.getYRot() : 180.0F;
		float heading = ((yaw + 180.0F) % 360.0F + 360.0F) % 360.0F;
		if (background.on()) Ui.fill(0, 0, W, H, backgroundColor.argb());
		int center = W / 2;
		int color = textColor.argb();
		int dim = (color & 0x00FFFFFF) | 0x99000000;
		for (int deg = -90; deg <= 450; deg += 15) {
			float diff = deg - heading;
			if (diff < -180) diff += 360;
			if (diff > 180) diff -= 360;
			int x = Math.round(center + diff);
			if (x < 3 || x > W - 3) continue;
			int d = ((deg % 360) + 360) % 360;
			if (d % 45 == 0) {
				String label = CARDINAL[d / 45];
				Ui.text(label, x - Ui.width(label) / 2.0F, 5, color, true);
			} else if (degrees.on()) {
				String label = String.valueOf(d);
				drawSmall(label, x, 6, dim);
			} else {
				Ui.fill(x, 8, x + 1, 11, dim);
			}
		}
		int marker = markerColor.argb();
		Ui.fill(center - 1, 0, center + 1, 3, marker);
		Ui.fill(center, 3, center + 1, 4, marker);
		Ui.fill(center - 1, H - 2, center + 1, H, marker);
	}

	/** Texto pequeno centralizado em x. */
	private static void drawSmall(String label, int x, int y, int color) {
		Ui.push();
		Ui.translate(x, y);
		Ui.scale(0.6F);
		Ui.text(label, -Ui.width(label) / 2.0F, 0.0F, color, true);
		Ui.pop();
	}
}
