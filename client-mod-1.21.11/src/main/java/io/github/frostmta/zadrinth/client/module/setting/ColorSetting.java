package io.github.frostmta.zadrinth.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.frostmta.zadrinth.client.cosmetics.CosmeticStyle;

/** Cor ARGB, com opção de arco-íris (RGB animado). */
public class ColorSetting extends Setting<Integer> {
	/** Cores rápidas da tela de opções */
	public static final int[] PALETTE = {
			0xFFFFFF, 0xAAAAAA, 0x2A2A2A, 0xFF4B4B, 0xFF9F2E, 0xFFD84A,
			0x3BE07A, 0x34D7E6, 0x5B8CFF, 0x9D7BFF, 0xE07BFF, 0xFF6AD5
	};

	public final boolean allowAlpha;
	private boolean chroma;
	private final boolean defaultChroma;

	public ColorSetting(String key, String name, int defaultArgb, boolean allowAlpha) {
		this(key, name, defaultArgb, allowAlpha, false);
	}

	public ColorSetting(String key, String name, int defaultArgb, boolean allowAlpha, boolean defaultChroma) {
		super(key, name, defaultArgb);
		this.allowAlpha = allowAlpha;
		this.chroma = defaultChroma;
		this.defaultChroma = defaultChroma;
	}

	public boolean chroma() {
		return chroma;
	}

	public void setChroma(boolean chroma) {
		this.chroma = chroma;
	}

	public int alpha() {
		return allowAlpha ? get() >>> 24 : 255;
	}

	public void setRgb(int rgb) {
		set((alpha() << 24) | (rgb & 0xFFFFFF));
		chroma = false;
	}

	public void setAlpha(int alpha) {
		set((Math.max(0, Math.min(255, alpha)) << 24) | (get() & 0xFFFFFF));
	}

	/** Cor para desenhar agora (o RGB anda com o tempo e com a posição x). */
	public int argb(float offset) {
		if (!chroma) return allowAlpha ? get() : 0xFF000000 | get();
		float hue = (System.currentTimeMillis() % 4000L) / 4000.0F + offset;
		return (alpha() << 24) | CosmeticStyle.hsv(hue, 0.7F, 1.0F);
	}

	public int argb() {
		return argb(0);
	}

	@Override
	public void reset() {
		super.reset();
		chroma = defaultChroma;
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("argb", get());
		json.addProperty("chroma", chroma);
		return json;
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json == null || !json.isJsonObject()) return;
		JsonObject object = json.getAsJsonObject();
		if (object.has("argb")) set(object.get("argb").getAsInt());
		if (object.has("chroma")) chroma = object.get("chroma").getAsBoolean();
	}
}
