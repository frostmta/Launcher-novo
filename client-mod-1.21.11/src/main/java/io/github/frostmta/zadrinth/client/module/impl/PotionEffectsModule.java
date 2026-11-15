package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.gui.Ui;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ColorSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.List;

/** Efeitos de poção ativos, com ícone, nível e tempo. */
public final class PotionEffectsModule extends HudModule {
	private static final int ROW = 22;
	private static final int WIDTH = 110;

	private final BoolSetting icons = add(new BoolSetting("icons", "Ícones", true));
	private final BoolSetting background = add(new BoolSetting("background", "Fundo", true));
	private final ColorSetting backgroundColor = add(new ColorSetting("backgroundColor", "Cor do fundo", 0x6F000000, true));
	private final ColorSetting nameColor = add(new ColorSetting("nameColor", "Cor do nome", 0xFFFFFFFF, false));
	private final ColorSetting timeColor = add(new ColorSetting("timeColor", "Cor do tempo", 0xFFAAAAAA, false));
	private final BoolSetting blink = add(new BoolSetting("blink", "Piscar no fim (10s)", true));

	public PotionEffectsModule() {
		super("potions", "Efeitos de poção", "Seus efeitos ativos e quanto tempo falta.", Category.HUD, Icon.POTION, true, 0.005f, 0.50f);
	}

	private record Row(Holder<MobEffect> effect, int amplifier, int duration, boolean infinite) {
	}

	private List<Row> rows(Minecraft client, boolean editing) {
		List<Row> rows = new ArrayList<>();
		if (client.player != null) {
			for (MobEffectInstance instance : client.player.getActiveEffects()) {
				if (!instance.showIcon()) continue;
				rows.add(new Row(instance.getEffect(), instance.getAmplifier(), instance.getDuration(), instance.isInfiniteDuration()));
			}
		}
		if (rows.isEmpty() && editing) {
			rows.add(new Row(MobEffects.SPEED, 1, 1800, false));
			rows.add(new Row(MobEffects.STRENGTH, 0, 900, false));
		}
		return rows;
	}

	@Override
	public boolean shouldRender(Minecraft client) {
		return !rows(client, false).isEmpty();
	}

	@Override
	public int width(Minecraft client, boolean editing) {
		return WIDTH;
	}

	@Override
	public int height(Minecraft client, boolean editing) {
		return Math.max(1, rows(client, editing).size()) * ROW;
	}

	@Override
	public void render(Minecraft client, boolean editing) {
		List<Row> rows = rows(client, editing);
		if (background.on()) Ui.fill(0, 0, WIDTH, rows.size() * ROW, backgroundColor.argb());
		int y = 0;
		for (Row row : rows) {
			int textX = 4;
			if (icons.on()) {
				Ui.g().blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(row.effect()), 2, y + 2, 18, 18);
				textX = 23;
			}
			String name = row.effect().value().getDisplayName().getString();
			if (row.amplifier() > 0) name += " " + Component.translatable("enchantment.level." + (row.amplifier() + 1)).getString();
			name = Ui.trim(name, WIDTH - textX - 2);
			Ui.text(name, textX, y + 3, nameColor.argb());
			boolean ending = !row.infinite() && row.duration() < 200 && blink.on();
			if (!ending || (System.currentTimeMillis() / 400L) % 2 == 0) {
				Ui.text(row.infinite() ? "**:**" : time(row.duration()), textX, y + 12, timeColor.argb());
			}
			y += ROW;
		}
	}

	private static String time(int ticks) {
		int seconds = ticks / 20;
		return seconds / 60 + ":" + String.format("%02d", seconds % 60);
	}
}
