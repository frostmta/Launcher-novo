package io.github.frostmta.zadrinth.client.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.frostmta.zadrinth.client.module.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Um mod do Zadrinth Client (um cartão no menu). Pode ser ligado/desligado e
 * ter opções. Fica salvo em config/zadrinth-client.json → "modules".
 */
public abstract class Module {
	public final String id;
	public final String name;
	public final String description;
	public final Category category;
	public final Icon icon;
	private final boolean defaultEnabled;
	private boolean enabled;
	private final List<Setting<?>> settings = new ArrayList<>();

	protected Module(String id, String name, String description, Category category, Icon icon, boolean defaultEnabled) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.icon = icon;
		this.defaultEnabled = defaultEnabled;
		this.enabled = defaultEnabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean value) {
		if (value == isEnabled()) return;
		storeEnabled(value);
		if (value) onEnable();
		else onDisable();
	}

	/** Onde o "ligado" fica guardado (alguns módulos usam campos antigos da configuração). */
	protected void storeEnabled(boolean value) {
		this.enabled = value;
	}

	public void toggle() {
		setEnabled(!isEnabled());
	}

	protected <S extends Setting<?>> S add(S setting) {
		settings.add(setting);
		return setting;
	}

	public List<Setting<?>> settings() {
		return Collections.unmodifiableList(settings);
	}

	public void onEnable() {
	}

	public void onDisable() {
	}

	/** Chamado a cada tick do jogo (20x por segundo), ligado ou não. */
	public void tick(MinecraftClient client) {
	}

	public void resetSettings() {
		for (Setting<?> setting : settings) setting.reset();
	}

	public JsonObject save() {
		JsonObject json = new JsonObject();
		json.addProperty("enabled", isEnabled());
		JsonObject values = new JsonObject();
		for (Setting<?> setting : settings) {
			JsonElement value = setting.toJson();
			if (value != null && !value.isJsonNull()) values.add(setting.key, value);
		}
		json.add("settings", values);
		return json;
	}

	public void load(JsonObject json) {
		if (json == null) {
			storeEnabled(defaultEnabled);
			return;
		}
		if (json.has("enabled")) storeEnabled(json.get("enabled").getAsBoolean());
		if (json.has("settings") && json.get("settings").isJsonObject()) {
			JsonObject values = json.getAsJsonObject("settings");
			for (Setting<?> setting : settings) {
				try {
					setting.fromJson(values.get(setting.key));
				} catch (RuntimeException ignored) {
					//valor antigo/inválido: fica o padrão
				}
			}
		}
	}
}
