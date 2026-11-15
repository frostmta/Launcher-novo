package io.github.frostmta.zadrinth.client.cosmetics;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * O que um jogador está usando: um cosmético por categoria + opções de cada um.
 * É salvo na configuração (você) e enviado na sincronização (os outros).
 */
public final class Loadout {
	public String cape;
	public String wings;
	public String bandana;
	public CosmeticStyle capeStyle = new CosmeticStyle();
	public CosmeticStyle wingsStyle = new CosmeticStyle();
	public CosmeticStyle bandanaStyle = new CosmeticStyle();

	public String get(CosmeticType type) {
		switch (type) {
			case CAPE: return cape;
			case WINGS: return wings;
			default: return bandana;
		}
	}

	public void set(CosmeticType type, String id) {
		switch (type) {
			case CAPE: cape = id; break;
			case WINGS: wings = id; break;
			default: bandana = id; break;
		}
	}

	public CosmeticStyle style(CosmeticType type) {
		CosmeticStyle style;
		switch (type) {
			case CAPE: style = capeStyle; break;
			case WINGS: style = wingsStyle; break;
			default: style = bandanaStyle; break;
		}
		if (style == null) {
			style = new CosmeticStyle();
			setStyle(type, style);
		}
		return style;
	}

	private void setStyle(CosmeticType type, CosmeticStyle style) {
		switch (type) {
			case CAPE: capeStyle = style; break;
			case WINGS: wingsStyle = style; break;
			default: bandanaStyle = style; break;
		}
	}

	/** O cosmético equipado nesta categoria (null se nenhum ou se não existir mais). */
	public Cosmetic equipped(CosmeticType type) {
		return Cosmetics.get(type, get(type));
	}

	public boolean isEmpty() {
		for (CosmeticType type : CosmeticType.values()) {
			if (equipped(type) != null) return false;
		}
		return true;
	}

	public Loadout copy() {
		Loadout loadout = new Loadout();
		for (CosmeticType type : CosmeticType.values()) {
			loadout.set(type, get(type));
			loadout.setStyle(type, style(type).copy());
		}
		return loadout;
	}

	/** Versão compacta para a sincronização. Ex.: {"c":{"id":"nebulosa","k":-1,"r":false,"g":true}} */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		for (CosmeticType type : CosmeticType.values()) {
			Cosmetic cosmetic = equipped(type);
			if (cosmetic == null) continue;
			CosmeticStyle style = style(type);
			JsonObject item = new JsonObject();
			item.addProperty("id", cosmetic.id);
			item.addProperty("k", style.color);
			item.addProperty("r", style.rgb);
			item.addProperty("g", style.glint);
			json.add(type.shortKey, item);
		}
		return json;
	}

	/** Lê o que veio de outro jogador, ignorando qualquer coisa desconhecida. */
	public static Loadout fromJson(JsonElement element) {
		Loadout loadout = new Loadout();
		if (element == null || !element.isJsonObject()) return loadout;
		JsonObject json = element.getAsJsonObject();
		for (CosmeticType type : CosmeticType.values()) {
			try {
				JsonElement value = json.get(type.shortKey);
				if (value == null || !value.isJsonObject()) continue;
				JsonObject item = value.getAsJsonObject();
				String id = item.has("id") ? item.get("id").getAsString() : null;
				if (Cosmetics.get(type, id) == null) continue;
				loadout.set(type, id);
				CosmeticStyle style = loadout.style(type);
				if (item.has("k")) {
					int color = item.get("k").getAsInt();
					style.color = color < 0 ? -1 : color & 0xFFFFFF;
				}
				style.rgb = item.has("r") && item.get("r").getAsBoolean();
				style.glint = item.has("g") && item.get("g").getAsBoolean();
			} catch (RuntimeException ignored) {
				//item mal formado: só ignora esta categoria
			}
		}
		return loadout;
	}
}
