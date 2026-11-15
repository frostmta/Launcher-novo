package io.github.frostmta.zadrinth.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

/** Um botão na tela de opções (ex.: "Abrir loja"). Não é salvo. */
public class ActionSetting extends Setting<Runnable> {
	public ActionSetting(String key, String name, Runnable action) {
		super(key, name, action);
	}

	public void run() {
		get().run();
	}

	@Override
	public JsonElement toJson() {
		return JsonNull.INSTANCE;
	}

	@Override
	public void fromJson(JsonElement json) {
	}
}
