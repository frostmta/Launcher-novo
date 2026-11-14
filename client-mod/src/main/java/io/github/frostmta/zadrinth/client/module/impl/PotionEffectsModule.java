package io.github.frostmta.zadrinth.client.module.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Efeitos de poção ativos, com ícone, nível e tempo. */
public final class PotionEffectsModule extends HudModule {
	private static final Identifier INVENTORY = new Identifier("textures/gui/container/inventory.png");
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

	private static final class Row {
		final StatusEffect effect;
		final int amplifier;
		final int duration;

		Row(StatusEffect effect, int amplifier, int duration) {
			this.effect = effect;
			this.amplifier = amplifier;
			this.duration = duration;
		}
	}

	private List<Row> rows(MinecraftClient client, boolean editing) {
		List<Row> rows = new ArrayList<>();
		if (client.player != null) {
			Collection<?> effects = client.player.getStatusEffectInstances();
			for (Object object : effects) {
				StatusEffectInstance instance = (StatusEffectInstance) object;
				int id = instance.getEffectId();
				if (id < 0 || id >= StatusEffect.STATUS_EFFECTS.length || StatusEffect.STATUS_EFFECTS[id] == null) continue;
				rows.add(new Row(StatusEffect.STATUS_EFFECTS[id], instance.getAmplifier(), instance.getDuration()));
			}
		}
		if (rows.isEmpty() && editing) {
			if (StatusEffect.STATUS_EFFECTS[1] != null) rows.add(new Row(StatusEffect.STATUS_EFFECTS[1], 1, 1800));
			if (StatusEffect.STATUS_EFFECTS[5] != null) rows.add(new Row(StatusEffect.STATUS_EFFECTS[5], 0, 900));
		}
		return rows;
	}

	@Override
	public boolean shouldRender(MinecraftClient client) {
		return !rows(client, false).isEmpty();
	}

	@Override
	public int width(MinecraftClient client, boolean editing) {
		return WIDTH;
	}

	@Override
	public int height(MinecraftClient client, boolean editing) {
		return Math.max(1, rows(client, editing).size()) * ROW;
	}

	@Override
	public void render(MinecraftClient client, boolean editing) {
		List<Row> rows = rows(client, editing);
		TextRenderer text = client.textRenderer;
		if (background.on()) DrawableHelper.fill(0, 0, WIDTH, rows.size() * ROW, backgroundColor.argb());
		int y = 0;
		for (Row row : rows) {
			int textX = 4;
			if (icons.on() && row.effect.hasIcon()) {
				int index = row.effect.getIconLevel();
				client.getTextureManager().bindTexture(INVENTORY);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.enableBlend();
				DrawableHelper.drawTexture(2, y + 2, index % 8 * 18, 198 + index / 8 * 18, 18, 18, 256.0F, 256.0F);
				textX = 23;
			}
			String name = I18n.translate(row.effect.getTranslationKey());
			if (row.amplifier > 0) name += " " + I18n.translate("enchantment.level." + (row.amplifier + 1));
			text.drawWithShadow(name, (float) textX, (float) (y + 3), nameColor.argb());
			boolean ending = row.duration < 200 && blink.on();
			if (!ending || (System.currentTimeMillis() / 400L) % 2 == 0) {
				text.drawWithShadow(time(row.duration), (float) textX, (float) (y + 12), timeColor.argb());
			}
			y += ROW;
		}
	}

	private static String time(int ticks) {
		if (ticks > 32767 * 2) return "**:**";
		int seconds = ticks / 20;
		return seconds / 60 + ":" + String.format("%02d", seconds % 60);
	}
}
