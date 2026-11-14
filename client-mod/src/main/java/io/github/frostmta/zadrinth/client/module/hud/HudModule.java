package io.github.frostmta.zadrinth.client.module.hud;

import com.google.gson.JsonObject;
import io.github.frostmta.zadrinth.client.config.HudPosition;
import io.github.frostmta.zadrinth.client.module.Category;
import io.github.frostmta.zadrinth.client.module.Icon;
import io.github.frostmta.zadrinth.client.module.Module;
import net.minecraft.client.MinecraftClient;

/** Módulo que aparece na tela e pode ser movido/redimensionado no editor de HUD. */
public abstract class HudModule extends Module {
	private final HudPosition defaultPosition;
	private HudPosition position;

	protected HudModule(String id, String name, String description, Category category, Icon icon,
						boolean defaultEnabled, float x, float y) {
		super(id, name, description, category, icon, defaultEnabled);
		this.defaultPosition = new HudPosition(x, y, 1.0f);
		this.position = new HudPosition(x, y, 1.0f);
	}

	public HudPosition position() {
		return position;
	}

	public void resetPosition() {
		position = new HudPosition(defaultPosition.x, defaultPosition.y, defaultPosition.scale);
	}

	/** Desenhar agora? (alguns só aparecem quando há algo para mostrar) */
	public boolean shouldRender(MinecraftClient client) {
		return true;
	}

	/** Tamanho sem escala, em pixels da interface. */
	public abstract int width(MinecraftClient client, boolean editing);

	public abstract int height(MinecraftClient client, boolean editing);

	/** Desenha em (0, 0); posição e escala já foram aplicadas. */
	public abstract void render(MinecraftClient client, boolean editing);

	@Override
	public JsonObject save() {
		JsonObject json = super.save();
		JsonObject hud = new JsonObject();
		hud.addProperty("x", position.x);
		hud.addProperty("y", position.y);
		hud.addProperty("scale", position.scale);
		json.add("hud", hud);
		return json;
	}

	@Override
	public void load(JsonObject json) {
		super.load(json);
		if (json != null && json.has("hud") && json.get("hud").isJsonObject()) {
			JsonObject hud = json.getAsJsonObject("hud");
			try {
				position = new HudPosition(hud.get("x").getAsFloat(), hud.get("y").getAsFloat(), hud.get("scale").getAsFloat());
			} catch (RuntimeException e) {
				resetPosition();
			}
		}
	}

	/** Usado para migrar a posição antiga do ToggleSprint. */
	protected void setPosition(HudPosition value) {
		if (value != null) position = new HudPosition(value.x, value.y, value.scale);
	}
}
