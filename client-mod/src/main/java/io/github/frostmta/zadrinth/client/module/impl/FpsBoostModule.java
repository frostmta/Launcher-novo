package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * FPS Boost para celular: desliga o que mais pesa (partículas, nuvens,
 * sombras, iluminação suave, gráficos bonitos), tira o limite de FPS, limita a
 * distância de visão e para de desenhar entidades longe de você. Ao desligar o
 * módulo, as opções de antes voltam.
 */
public final class FpsBoostModule extends Module {
	private static FpsBoostModule instance;

	private final BoolSetting particles = add(new BoolSetting("particles", "Partículas mínimas", true));
	private final BoolSetting clouds = add(new BoolSetting("clouds", "Sem nuvens", true));
	private final BoolSetting shadows = add(new BoolSetting("shadows", "Sem sombras de entidades", true));
	private final BoolSetting fast = add(new BoolSetting("fast", "Gráficos rápidos", true));
	private final BoolSetting smoothLight = add(new BoolSetting("smoothLight", "Sem iluminação suave", true));
	private final BoolSetting unlimited = add(new BoolSetting("unlimited", "FPS sem limite (sem VSync)", true));
	private final NumberSetting renderDistance = add(new NumberSetting("renderDistance", "Distância de visão máxima", 6, 2, 16, 1, " chunks"));
	private final BoolSetting cullEntities = add(new BoolSetting("cullEntities", "Esconder entidades longe", true));
	private final NumberSetting entityDistance = add(new NumberSetting("entityDistance", "Distância das entidades", 40, 16, 128, 4, " blocos"));
	private final NumberSetting playerDistance = add(new NumberSetting("playerDistance", "Distância dos jogadores", 64, 16, 160, 4, " blocos"));

	/** Opções do jogo antes do FPS Boost (para voltar ao desligar) */
	private int[] saved;

	public FpsBoostModule() {
		super("fpsboost", "FPS Boost", "Mais FPS em celular fraco: desliga o que pesa e esconde o que está longe.", Category.PERFORMANCE, Icon.ROCKET, false);
		instance = this;
	}

	@Override
	public void tick(MinecraftClient client) {
		GameOptions o = client.options;
		if (o == null || !isEnabled()) return;
		if (saved == null) {
			saved = new int[]{o.particle, o.cloudMode, o.entityShadows ? 1 : 0, o.fancyGraphics ? 1 : 0, o.ao,
					o.maxFramerate, o.vsync ? 1 : 0, o.viewDistance};
		}
		boolean reload = false;
		if (particles.on()) o.particle = 2;
		if (clouds.on()) o.cloudMode = 0;
		if (shadows.on()) o.entityShadows = false;
		if (fast.on() && o.fancyGraphics) {
			o.fancyGraphics = false;
			reload = true;
		}
		if (smoothLight.on() && o.ao != 0) {
			o.ao = 0;
			reload = true;
		}
		if (unlimited.on()) {
			o.maxFramerate = 260;
			o.vsync = false;
		}
		int maxView = renderDistance.intValue();
		if (o.viewDistance > maxView) o.viewDistance = maxView;
		if (reload && client.worldRenderer != null && client.world != null) client.worldRenderer.reload();
	}

	@Override
	public void onDisable() {
		MinecraftClient client = MinecraftClient.getInstance();
		GameOptions o = client.options;
		if (o == null || saved == null) return;
		boolean reload = o.fancyGraphics != (saved[3] == 1) || o.ao != saved[4];
		o.particle = saved[0];
		o.cloudMode = saved[1];
		o.entityShadows = saved[2] == 1;
		o.fancyGraphics = saved[3] == 1;
		o.ao = saved[4];
		o.maxFramerate = saved[5];
		o.vsync = saved[6] == 1;
		o.viewDistance = saved[7];
		saved = null;
		if (reload && client.worldRenderer != null && client.world != null) client.worldRenderer.reload();
	}

	/** false = não desenhar esta entidade (EntityRenderDispatcherMixin). */
	public static boolean shouldRender(Entity entity, double camX, double camY, double camZ) {
		FpsBoostModule module = instance;
		if (module == null || !module.isEnabled() || !module.cullEntities.on()) return true;
		MinecraftClient client = MinecraftClient.getInstance();
		if (entity == client.player || entity == client.getCameraEntity()) return true;
		double max = entity instanceof PlayerEntity ? module.playerDistance.value() : module.entityDistance.value();
		double dx = entity.x - camX, dy = entity.y - camY, dz = entity.z - camZ;
		return dx * dx + dy * dy + dz * dz <= max * max;
	}
}
