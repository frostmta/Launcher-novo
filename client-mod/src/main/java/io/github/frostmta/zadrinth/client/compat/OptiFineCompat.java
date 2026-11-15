package io.github.frostmta.zadrinth.client.compat;

import net.fabricmc.loader.api.FabricLoader;

/**
 * OptiFine junto com o Zadrinth Client (via Legacy OptiFabric).
 *
 * O OptiFine tem zoom próprio na mesma tecla C; quando ele está presente, o
 * zoom do Zadrinth sai do caminho (a não ser que você mude a tecla do nosso).
 * As capas do OptiFine continuam aparecendo: a nossa camada de capa usa o
 * mesmo método que o OptiFine modifica.
 */
public final class OptiFineCompat {
	private static Boolean present;

	private OptiFineCompat() {
	}

	public static boolean present() {
		if (present == null) {
			boolean found = false;
			try {
				found = FabricLoader.getInstance().isModLoaded("optifabric");
			} catch (Throwable ignored) {
			}
			if (!found) {
				for (String name : new String[]{"Config", "net.optifine.Config", "optifine.OptiFineClassTransformer"}) {
					try {
						Class.forName(name, false, OptiFineCompat.class.getClassLoader());
						found = true;
						break;
					} catch (Throwable ignored) {
					}
				}
			}
			present = found;
		}
		return present;
	}
}
