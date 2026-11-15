package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.gui.Ui;
import io.github.frostmta.zadrinth.client.hud.HudManager;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.ModuleManager;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ColorSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * O placar lateral dos servidores, que você pode mover, aumentar/diminuir e
 * deixar sem os números vermelhos. Desligado, o placar do jogo aparece normal.
 */
public final class ScoreboardModule extends HudModule {
	private static final Comparator<PlayerScoreEntry> ORDER = Comparator.comparing(PlayerScoreEntry::value)
			.reversed()
			.thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);

	private final BoolSetting numbers = add(new BoolSetting("numbers", "Números vermelhos", false));
	private final BoolSetting background = add(new BoolSetting("background", "Fundo", true));
	private final ColorSetting backgroundColor = add(new ColorSetting("backgroundColor", "Cor do fundo", 0x50000000, true));
	private final ColorSetting titleColor = add(new ColorSetting("titleColor", "Fundo do título", 0x66000000, true));
	private final BoolSetting shadow = add(new BoolSetting("shadow", "Sombra no texto", false));

	public ScoreboardModule() {
		super("scoreboard", "Placar", "O placar do servidor: mova, mude o tamanho e esconda os números.", Category.HUD, Icon.SCOREBOARD, true, 1.0f, 0.32f);
	}

	private record Line(Component name, Component score) {
	}

	/** No lugar do placar do jogo (HudElementRegistry). */
	public static void render(GuiGraphics graphics, Runnable vanilla) {
		Minecraft client = Minecraft.getInstance();
		ScoreboardModule module = ModuleManager.SCOREBOARD;
		if (!module.isEnabled()) {
			vanilla.run();
			return;
		}
		if (current(client) == null || client.screen instanceof io.github.frostmta.zadrinth.client.gui.HudEditorScreen) return;
		Ui.begin(graphics);
		graphics.nextStratum();
		HudManager.draw(client, module, false);
	}

	/** O placar que o jogo mostraria agora (o da cor do seu time tem prioridade). */
	public static @Nullable Objective current(Minecraft client) {
		if (client.level == null || client.player == null) return null;
		Scoreboard scoreboard = client.level.getScoreboard();
		Objective objective = null;
		PlayerTeam team = scoreboard.getPlayersTeam(client.player.getScoreboardName());
		if (team != null) {
			DisplaySlot slot = DisplaySlot.teamColorToSlot(team.getColor());
			if (slot != null) objective = scoreboard.getDisplayObjective(slot);
		}
		return objective != null ? objective : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
	}

	private Component title(Minecraft client, boolean editing) {
		Objective objective = current(client);
		if (objective != null) return objective.getDisplayName();
		return Component.literal(editing ? "§d§lZADRINTH" : "");
	}

	private List<Line> lines(Minecraft client, boolean editing) {
		List<Line> lines = new ArrayList<>();
		Objective objective = current(client);
		if (objective != null) {
			Scoreboard scoreboard = objective.getScoreboard();
			NumberFormat format = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);
			scoreboard.listPlayerScores(objective).stream()
					.filter(entry -> !entry.isHidden())
					.sorted(ORDER)
					.limit(15L)
					.forEach(entry -> {
						PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
						lines.add(new Line(PlayerTeam.formatNameForTeam(team, entry.ownerName()), entry.formatValue(format)));
					});
		} else if (editing) {
			lines.add(new Line(Component.literal("Rank: §dMembro"), Component.literal("§c4")));
			lines.add(new Line(Component.literal("Créditos: §e0"), Component.literal("§c3")));
			lines.add(new Line(Component.literal(""), Component.literal("§c2")));
			lines.add(new Line(Component.literal("§7zadrinth.client"), Component.literal("§c1")));
		}
		return lines;
	}

	@Override
	public boolean shouldRender(Minecraft client) {
		return current(client) != null;
	}

	@Override
	public int width(Minecraft client, boolean editing) {
		int max = client.font.width(title(client, editing));
		int colon = client.font.width(": ");
		for (Line line : lines(client, editing)) {
			int w = client.font.width(line.name());
			int score = client.font.width(line.score());
			if (numbers.on() && score > 0) w += colon + score;
			max = Math.max(max, w);
		}
		return max + 4;
	}

	@Override
	public int height(Minecraft client, boolean editing) {
		return (lines(client, editing).size() + 1) * client.font.lineHeight + 1;
	}

	@Override
	public void render(Minecraft client, boolean editing) {
		int w = width(client, editing);
		int fh = client.font.lineHeight;
		Component title = title(client, editing);
		if (background.on()) {
			Ui.fill(0, 0, w, fh, titleColor.argb());
			Ui.fill(0, fh, w, fh + 1, backgroundColor.argb());
		}
		draw(client, title, (w - client.font.width(title)) / 2, 1);
		int y = fh + 1;
		for (Line line : lines(client, editing)) {
			if (background.on()) Ui.fill(0, y, w, y + fh, backgroundColor.argb());
			draw(client, line.name(), 2, y);
			if (numbers.on()) draw(client, line.score(), w - 2 - client.font.width(line.score()), y);
			y += fh;
		}
	}

	private void draw(Minecraft client, Component value, int x, int y) {
		Ui.g().drawString(client.font, value, x, y, 0xFFFFFFFF, shadow.on());
	}
}
