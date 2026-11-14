package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.hud.HudModule;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * O placar lateral dos servidores, que você pode mover, aumentar/diminuir e
 * deixar sem os números vermelhos. O do jogo é cancelado no InGameHudMixin.
 */
public final class ScoreboardModule extends HudModule {
	/** Placar que o jogo ia desenhar neste quadro (null = nenhum) */
	public static ScoreboardObjective current;

	private final BoolSetting numbers = add(new BoolSetting("numbers", "Números vermelhos", false));
	private final BoolSetting background = add(new BoolSetting("background", "Fundo", true));
	private final ColorSetting backgroundColor = add(new ColorSetting("backgroundColor", "Cor do fundo", 0x50000000, true));
	private final ColorSetting titleColor = add(new ColorSetting("titleColor", "Fundo do título", 0x66000000, true));
	private final BoolSetting shadow = add(new BoolSetting("shadow", "Sombra no texto", false));

	public ScoreboardModule() {
		super("scoreboard", "Placar", "O placar do servidor: mova, mude o tamanho e esconda os números.", Category.HUD, Icon.SCOREBOARD, true, 1.0f, 0.32f);
	}

	private static final class Line {
		final String name;
		final String score;

		Line(String name, String score) {
			this.name = name;
			this.score = score;
		}
	}

	private String title(boolean editing) {
		if (current != null) return current.getDisplayName();
		return editing ? "§d§lZADRINTH" : "";
	}

	private List<Line> lines(boolean editing) {
		List<Line> lines = new ArrayList<>();
		if (current != null) {
			Scoreboard scoreboard = current.getScoreboard();
			Collection<?> scores = scoreboard.getAllPlayerScores(current);
			List<ScoreboardPlayerScore> list = new ArrayList<>();
			for (Object object : scores) {
				ScoreboardPlayerScore score = (ScoreboardPlayerScore) object;
				if (score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) list.add(score);
			}
			if (list.size() > 15) list = new ArrayList<>(list.subList(list.size() - 15, list.size()));
			Collections.reverse(list);
			for (ScoreboardPlayerScore score : list) {
				Team team = scoreboard.getPlayerTeam(score.getPlayerName());
				lines.add(new Line(Team.decorateName(team, score.getPlayerName()), "§c" + score.getScore()));
			}
		} else if (editing) {
			lines.add(new Line("Rank: §dMembro", "§c4"));
			lines.add(new Line("Créditos: §e0", "§c3"));
			lines.add(new Line("", "§c2"));
			lines.add(new Line("§7zadrinth.client", "§c1"));
		}
		return lines;
	}

	@Override
	public boolean shouldRender(MinecraftClient client) {
		return current != null;
	}

	@Override
	public int width(MinecraftClient client, boolean editing) {
		TextRenderer text = client.textRenderer;
		int max = text.getStringWidth(title(editing));
		for (Line line : lines(editing)) {
			int w = text.getStringWidth(line.name);
			if (numbers.on()) w += text.getStringWidth(": " + line.score);
			max = Math.max(max, w);
		}
		return max + 4;
	}

	@Override
	public int height(MinecraftClient client, boolean editing) {
		return (lines(editing).size() + 1) * client.textRenderer.fontHeight + 1;
	}

	@Override
	public void render(MinecraftClient client, boolean editing) {
		TextRenderer text = client.textRenderer;
		int w = width(client, editing);
		int fh = text.fontHeight;
		String title = title(editing);
		if (background.on()) {
			DrawableHelper.fill(0, 0, w, fh, titleColor.argb());
			DrawableHelper.fill(0, fh, w, fh + 1, backgroundColor.argb());
		}
		draw(text, title, (w - text.getStringWidth(title)) / 2, 1);
		int y = fh + 1;
		for (Line line : lines(editing)) {
			if (background.on()) DrawableHelper.fill(0, y, w, y + fh, backgroundColor.argb());
			draw(text, line.name, 2, y);
			if (numbers.on()) draw(text, line.score, w - 2 - text.getStringWidth(line.score), y);
			y += fh;
		}
	}

	private void draw(TextRenderer text, String value, int x, int y) {
		if (shadow.on()) text.drawWithShadow(value, (float) x, (float) y, 0xFFFFFFFF);
		else text.draw(value, x, y, 0xFFFFFFFF);
	}
}
