package io.github.frostmta.zadrinth.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Escolha entre opções fixas (setas < >). */
public class ModeSetting extends Setting<Integer> {
	public final String[] options;

	public ModeSetting(String key, String name, int defaultIndex, String... options) {
		super(key, name, defaultIndex);
		this.options = options;
	}

	public int index() {
		return get();
	}

	public String current() {
		return options[Math.max(0, Math.min(options.length - 1, get()))];
	}

	public void cycle(int direction) {
		int next = (get() + direction) % options.length;
		if (next < 0) next += options.length;
		set(next);
	}

	@Override
	public void set(Integer value) {
		super.set(Math.max(0, Math.min(options.length - 1, value)));
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(get());
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json != null && json.isJsonPrimitive()) set(json.getAsInt());
	}
}
