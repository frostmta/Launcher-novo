package io.github.frostmta.zadrinth.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoolSetting extends Setting<Boolean> {
	private final Supplier<Boolean> getter;
	private final Consumer<Boolean> setter;

	public BoolSetting(String key, String name, boolean defaultValue) {
		this(key, name, defaultValue, null, null);
	}

	/** Opção ligada a um campo que já existe na configuração (ex.: ToggleSprint). */
	public BoolSetting(String key, String name, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		super(key, name, defaultValue);
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public Boolean get() {
		return getter != null ? getter.get() : value;
	}

	@Override
	public void set(Boolean value) {
		if (setter != null) setter.accept(value);
		else this.value = value;
	}

	public boolean on() {
		return get();
	}

	public void toggle() {
		set(!get());
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(get());
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json != null && json.isJsonPrimitive()) set(json.getAsBoolean());
	}
}
