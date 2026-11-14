package io.github.frostmta.zadrinth.client.stats;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayDeque;
import java.util.Deque;

/** Cliques por segundo (contados no KeyBinding.onKeyPressed, que recebe cada clique). */
public final class ClickCounter {
	private static final Deque<Long> LEFT = new ArrayDeque<>();
	private static final Deque<Long> RIGHT = new ArrayDeque<>();

	private ClickCounter() {
	}

	public static void onKey(int code) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.options == null) return;
		long now = System.currentTimeMillis();
		if (code == client.options.attackKey.getCode()) LEFT.addLast(now);
		else if (code == client.options.useKey.getCode()) RIGHT.addLast(now);
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
