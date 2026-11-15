package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.gui.Ui;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ColorSetting;
import io.github.frostmta.zadrinth.client.module.setting.ModeSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Armadura e item na mão, com a durabilidade. */
public final class ArmorModule extends HudModule {
	private static final int ROW = 17;

	private final BoolSetting held = add(new BoolSetting("held", "Item na mão", true));
	private final ModeSetting durability = add(new ModeSetting("durability", "Durabilidade", 0, "Valor", "Porcentagem", "Esconder"));
	private final ModeSetting layout = add(new ModeSetting("layout", "Formato", 0, "Vertical", "Horizontal"));
	private final BoolSetting background = add(new BoolSetting("background", "Fundo", false));
	private final ColorSetting backgroundColor = add(new ColorSetting("backgroundColor", "Cor do fundo", 0x6F000000, true));
	private final ColorSetting textColor = add(new ColorSetting("textColor", "Cor do texto", 0xFFFFFFFF, false));
	private final BoolSetting colorByDamage = add(new BoolSetting("colorByDamage", "Cor pela durabilidade", true));

	public ArmorModule() {
		super("armor", "Armor HUD", "Sua armadura e a durabilidade dela.", Category.HUD, Icon.SHIELD, true, 1.0f, 1.0f);
	}

	private List<ItemStack> items(Minecraft client, boolean editing) {
		List<ItemStack> list = new ArrayList<>();
		if (client.player != null) {
			for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
				ItemStack stack = client.player.getItemBySlot(slot);
				if (!stack.isEmpty()) list.add(stack);
			}
			if (held.on()) {
				ItemStack hand = client.player.getMainHandItem();
				if (!hand.isEmpty()) list.add(hand);
			}
		}
		if (list.isEmpty() && editing) {
			list.add(new ItemStack(Items.DIAMOND_HELMET));
			list.add(new ItemStack(Items.DIAMOND_CHESTPLATE));
			list.add(new ItemStack(Items.DIAMOND_LEGGINGS));
			list.add(new ItemStack(Items.DIAMOND_BOOTS));
			if (held.on()) list.add(new ItemStack(Items.DIAMOND_SWORD));
		}
		return list;
	}

	private String label(ItemStack stack) {
		if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
			int left = stack.getMaxDamage() - stack.getDamageValue();
			if (durability.index() == 0) return String.valueOf(left);
			if (durability.index() == 1) return (left * 100 / stack.getMaxDamage()) + "%";
			return "";
		}
		return stack.getCount() > 1 ? String.valueOf(stack.getCount()) : "";
	}

	private int labelWidth(Minecraft client, List<ItemStack> items) {
		int max = 0;
		for (ItemStack stack : items) max = Math.max(max, client.font.width(label(stack)));
		return max;
	}

	@Override
	public boolean shouldRender(Minecraft client) {
		return !items(client, false).isEmpty();
	}

	@Override
	public int width(Minecraft client, boolean editing) {
		List<ItemStack> items = items(client, editing);
		if (layout.index() == 1) {
			int w = 0;
			for (ItemStack stack : items) {
				String text = label(stack);
				w += 17 + (text.isEmpty() ? 0 : client.font.width(text) + 2);
			}
			return Math.max(17, w + 2);
		}
		int text = labelWidth(client, items);
		return 20 + (text > 0 ? text + 4 : 0);
	}

	@Override
	public int height(Minecraft client, boolean editing) {
		if (layout.index() == 1) return ROW + 2;
		return Math.max(1, items(client, editing).size()) * ROW + 2;
	}

	@Override
	public void render(Minecraft client, boolean editing) {
		List<ItemStack> items = items(client, editing);
		int w = width(client, editing), h = height(client, editing);
		if (background.on()) Ui.fill(0, 0, w, h, backgroundColor.argb());
		int x = 2, y = 1;
		for (ItemStack stack : items) {
			Ui.g().renderItem(stack, x, y);
			Ui.g().renderItemDecorations(client.font, stack, x, y);
			String label = label(stack);
			int color = textColor.argb();
			if (colorByDamage.on() && stack.isDamageableItem() && stack.getMaxDamage() > 0) {
				float left = 1.0F - (float) stack.getDamageValue() / stack.getMaxDamage();
				color = left > 0.6F ? 0xFF55FF55 : left > 0.3F ? 0xFFFFFF55 : 0xFFFF5555;
			}
			if (layout.index() == 1) {
				if (!label.isEmpty()) Ui.text(label, x + 18, y + 5, color);
				x += 17 + (label.isEmpty() ? 0 : Ui.width(label) + 2);
			} else {
				if (!label.isEmpty()) Ui.text(label, 21, y + 5, color);
				y += ROW;
			}
		}
	}
}
