package io.github.frostmta.zadrinth.client.module.impl;

import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import io.github.frostmta.zadrinth.client.module.setting.BoolSetting;
import io.github.frostmta.zadrinth.client.module.setting.NumberSetting;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * FPS Boost para celular: desliga o que mais pesa (partículas, nuvens,
 * sombras, iluminação suave, transparência e folhas bonitas), tira o limite
 * de FPS, limita a distância de visão e para de desenhar entidades longe de
 * você. Ao desligar o módulo, as opções de antes voltam.
 */
public final class FpsBoostModule extends Module {
	private static FpsBoostModule instance;

	private final BoolSetting particles = add(new BoolSetting("particles", "Partículas mínimas", true));
	private final BoolSetting clouds = add(new BoolSetting("clouds", "Sem nuvens", true));
	private final BoolSetting shadows = add(new BoolSetting("shadows", "Sem sombras de entidades", true));
	private final BoolSetting fast = add(new BoolSetting("fast", "Gráficos rápidos (folhas e transparência)", true));
	private final BoolSetting smoothLight = add(new BoolSetting("smoothLight", "Sem iluminação suave", true));
	private final BoolSetting unlimited = add(new BoolSetting("unlimited", "FPS sem limite (sem VSync)", true));
	private final NumberSetting renderDistance = add(new NumberSetting("renderDistance", "Distância de visão máxima", 6, 2, 16, 1, " chunks"));
	private final BoolSetting cullEntities = add(new BoolSetting("cullEntities", "Esconder entidades longe", true));
	private final NumberSetting entityDistance = add(new NumberSetting("entityDistance", "Distância das entidades", 40, 16, 128, 4, " blocos"));
	private final NumberSetting playerDistance = add(new NumberSetting("playerDistance", "Distância dos jogadores", 64, 16, 160, 4, " blocos"));

	/** Valores das opções do jogo antes do FPS Boost (para voltar ao desligar) */
	private final Map<OptionInstance<?>, Object> saved = new LinkedHashMap<>();

	public FpsBoostModule() {
		super("fpsboost", "FPS Boost", "Mais FPS em celular fraco: desliga o que pesa e esconde o que está longe.", Category.PERFORMANCE, Icon.ROCKET, false);
		instance = this;
	}

	@Override
	public void tick(Minecraft client) {
		Options o = client.options;
		if (o == null || !isEnabled()) return;
		if (particles.on()) force(o.particles(), ParticleStatus.MINIMAL);
		if (clouds.on()) force(o.cloudStatus(), CloudStatus.OFF);
		if (shadows.on()) force(o.entityShadows(), false);
		if (fast.on()) {
			force(o.cutoutLeaves(), false);
			force(o.improvedTransparency(), false);
			force(o.biomeBlendRadius(), 0);
		}
		if (smoothLight.on()) force(o.ambientOcclusion(), false);
		if (unlimited.on()) {
			force(o.framerateLimit(), 260);
			force(o.enableVsync(), false);
		}
		int maxView = renderDistance.intValue();
		if (o.renderDistance().get() > maxView) force(o.renderDistance(), maxView);
	}

	/** Muda a opção só se for diferente (cada mudança pode recarregar o mundo). */
	private <T> void force(OptionInstance<T> option, T value) {
		T current = option.get();
		if (Objects.equals(current, value)) return;
		saved.putIfAbsent(option, current);
		option.set(value);
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void onDisable() {
		for (Map.Entry<OptionInstance<?>, Object> entry : saved.entrySet()) {
			((OptionInstance) entry.getKey()).set(entry.getValue());
		}
		saved.clear();
		Minecraft.getInstance().options.save();
	}

	/** false = não desenhar esta entidade (EntityRenderDispatcherMixin). */
	public static boolean shouldRender(Entity entity, double camX, double camY, double camZ) {
		FpsBoostModule module = instance;
		if (module == null || !module.isEnabled() || !module.cullEntities.on()) return true;
		Minecraft client = Minecraft.getInstance();
		if (entity == client.player || entity == client.getCameraEntity()) return true;
		double max = entity instanceof Player ? module.playerDistance.value() : module.entityDistance.value();
		double dx = entity.getX() - camX, dy = entity.getY() - camY, dz = entity.getZ() - camZ;
		return dx * dx + dy * dy + dz * dz <= max * max;
	}
}
