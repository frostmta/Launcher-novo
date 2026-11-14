package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Texturas do client lidas direto do .jar (sem depender da Legacy Fabric API). */
public final class Textures {
	private static final Map<String, Identifier> CACHE = new HashMap<>();
	private static final Set<String> FAILED = new HashSet<>();

	private Textures() {
	}

	/** path relativo a assets/zadrinth-client/, ex.: "textures/icons.png" */
	public static Identifier get(String path) {
		Identifier cached = CACHE.get(path);
		if (cached != null || FAILED.contains(path)) return cached;
		try (InputStream in = Textures.class.getResourceAsStream("/assets/zadrinth-client/" + path)) {
			if (in == null) throw new IllegalStateException("não encontrado");
			BufferedImage image = ImageIO.read(in);
			String name = "zadrinth_" + path.replaceAll("[^a-z0-9_]", "_");
			Identifier id = MinecraftClient.getInstance().getTextureManager()
					.registerDynamicTexture(name, new NativeImageBackedTexture(image));
			CACHE.put(path, id);
			return id;
		} catch (Exception e) {
			FAILED.add(path);
			ZadrinthClient.LOGGER.warn("Não foi possível carregar {}: {}", path, e.toString());
			return null;
		}
	}

	/** Liga a textura; smooth = filtro linear (ícones reduzidos ficam lisos). */
	public static boolean bind(String path, boolean smooth) {
		Identifier id = get(path);
		if (id == null) return false;
		MinecraftClient.getInstance().getTextureManager().bindTexture(id);
		int filter = smooth ? GL11.GL_LINEAR : GL11.GL_NEAREST;
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
		return true;
	}
}
