package io.github.frostmta.zadrinth.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting<Float> {
	public final float min;
	public final float max;
	public final float step;
	/** Texto depois do número (ex.: "x", "%") */
	public final String suffix;

	public NumberSetting(String key, String name, float defaultValue, float min, float max, float step, String suffix) {
		super(key, name, defaultValue);
		this.min = min;
		this.max = max;
		this.step = step;
		this.suffix = suffix;
	}

	@Override
	public void set(Float value) {
		float v = Math.max(min, Math.min(max, value));
		if (step > 0) v = Math.round((v - min) / step) * step + min;
		this.value = Math.max(min, Math.min(max, v));
	}

	public float value() {
		return get();
	}

	public int intValue() {
		return Math.round(get());
	}

	public String display() {
		float v = get();
		String text = step >= 1 ? String.valueOf(Math.round(v)) : String.format(java.util.Locale.ROOT, "%.1f", v);
		return text + suffix;
	}

	/** 0..1 para a barra */
	public float fraction() {
		return max > min ? (get() - min) / (max - min) : 0;
	}

	public void setFraction(float fraction) {
		set(min + (max - min) * Math.max(0, Math.min(1, fraction)));
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(get());
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json != null && json.isJsonPrimitive()) set(json.getAsFloat());
	}
}
