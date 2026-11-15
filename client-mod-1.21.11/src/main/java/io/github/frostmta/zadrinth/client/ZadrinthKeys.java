package io.github.frostmta.zadrinth.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/** Teclas do Zadrinth Client (aparecem em Opções > Controles > Zadrinth Client). */
public final class ZadrinthKeys {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("zadrinth-client", "main"));

	public static KeyMapping MENU;
	public static KeyMapping ZOOM;
	public static KeyMapping FREELOOK;

	private ZadrinthKeys() {
	}

	public static void register() {
		MENU = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.zadrinth-client.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));
		ZOOM = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.zadrinth-client.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY));
		//No 1.21 o F troca o item de mão: o freelook fica no Alt esquerdo (como no Lunar)
		FREELOOK = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.zadrinth-client.freelook", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY));
	}

	/** A tecla do menu (para fechar as telas do client com ela também). */
	public static boolean isMenuKey(int glfwKey) {
		return MENU != null && KeyBindingHelper.getBoundKeyOf(MENU).getValue() == glfwKey;
	}

	public static boolean isDown(KeyMapping key) {
		return key != null && key.isDown();
	}

	/** Descarta os toques acumulados; devolve quantos eram. */
	public static int consume(KeyMapping key) {
		int count = 0;
		if (key == null) return 0;
		while (key.consumeClick()) count++;
		return count;
	}
}
