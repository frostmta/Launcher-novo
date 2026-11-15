package io.github.frostmta.zadrinth.client.module.setting;

import com.google.gson.JsonElement;

/** Uma opção de um módulo (aparece na tela de opções do módulo). */
public abstract class Setting<T> {
	public final String key;
	public final String name;
	protected final T defaultValue;
	protected T value;
	/** Esconde a opção quando ela não faz sentido (ex.: cor da borda sem borda). */
	private java.util.function.BooleanSupplier visibleWhen;

	protected Setting(String key, String name, T defaultValue) {
		this.key = key;
		this.name = name;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}

	public void reset() {
		set(defaultValue);
	}

	public boolean isVisible() {
		return visibleWhen == null || visibleWhen.getAsBoolean();
	}

	@SuppressWarnings("unchecked")
	public <S extends Setting<T>> S visibleWhen(java.util.function.BooleanSupplier condition) {
		this.visibleWhen = condition;
		return (S) this;
	}

	/** Salvo no config/zadrinth-client.json */
	public abstract JsonElement toJson();

	public abstract void fromJson(JsonElement json);
}
