package io.github.frostmta.zadrinth.client.stats;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.Deque;

/** Cliques por segundo (contados no KeyBinding.onKeyPressed, que recebe cada clique). */
public final class ClickCounter {
	private static final Deque<Long> LEFT = new ArrayDeque<>();
	private static final Deque<Long> RIGHT = new ArrayDeque<>();

	private ClickCounter() {
	}

	/** Cada clique (KeyMappingMixin, no KeyMapping.click do jogo). */
	public static void onKey(InputConstants.Key key) {
		Minecraft client = Minecraft.getInstance();
		if (client.options == null || key == null) return;
		long now = System.currentTimeMillis();
		if (key.equals(KeyBindingHelper.getBoundKeyOf(client.options.keyAttack))) LEFT.addLast(now);
		else if (key.equals(KeyBindingHelper.getBoundKeyOf(client.options.keyUse))) RIGHT.addLast(now);
	}

	public static int left() {
		return count(LEFT);
	}

	public static int right() {
		return count(RIGHT);
	}

	private static int count(Deque<Long> clicks) {
		long limit = System.currentTimeMillis() - 1000L;
		while (!clicks.isEmpty() && clicks.peekFirst() < limit) clicks.pollFirst();
		return clicks.size();
	}
}
