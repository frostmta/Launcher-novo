package io.github.frostmta.zadrinth.client.cosmetics.render;

import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Texturas dos cosméticos, lidas direto do .jar (sem depender da Legacy
 * Fabric API). Para as opções de cor existe também uma versão em tons de cinza
 * da mesma arte, que o jogo "tinge" com a cor escolhida mantendo as sombras e
 * os detalhes.
 */
public final class CosmeticTextures {
	private static final Map<String, Identifier> CACHE = new HashMap<>();
	private static final Set<String> FAILED = new HashSet<>();
	/** Alfa dos pixels das asas 3D: {largura, altura, alfa...} */
	private static final Map<String, int[]> ALPHA = new HashMap<>();

	private CosmeticTextures() {
	}

	public static Identifier get(Cosmetic cosmetic, boolean gray) {
		String key = cosmetic.key() + (gray ? "#gray" : "");
		Identifier cached = CACHE.get(key);
		if (cached != null || FAILED.contains(key)) return cached;
		try (InputStream in = CosmeticTextures.class.getResourceAsStream(cosmetic.texturePath())) {
			if (in == null) throw new IllegalStateException("textura não encontrada: " + cosmetic.texturePath());
			BufferedImage image = ImageIO.read(in);
			if (image == null) throw new IllegalStateException("textura inválida: " + cosmetic.texturePath());
			if (cosmetic.voxel && !ALPHA.containsKey(cosmetic.key())) ALPHA.put(cosmetic.key(), alphaOf(image));
			if (gray) image = grayscale(image);
			String name = "zadrinth_" + cosmetic.type.folder + "_" + cosmetic.id + (gray ? "_gray" : "");
			Identifier id = MinecraftClient.getInstance().getTextureManager()
					.registerDynamicTexture(name, new NativeImageBackedTexture(image));
			CACHE.put(key, id);
			return id;
		} catch (Exception e) {
			FAILED.add(key);
			ZadrinthClient.LOGGER.warn("Não foi possível carregar o cosmético {}: {}", cosmetic.key(), e.toString());
			return null;
		}
	}

	/** Luminância um pouco clareada (o tingimento multiplica e escureceria demais). */
	/** {largura, altura, alfa de cada pixel...} da asa 3D (depois de carregar a textura). */
	public static int[] alpha(Cosmetic cosmetic) {
		return ALPHA.get(cosmetic.key());
	}

	private static int[] alphaOf(BufferedImage image) {
		int w = image.getWidth(), h = image.getHeight();
		int[] out = new int[2 + w * h];
		out[0] = w;
		out[1] = h;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) out[2 + y * w + x] = image.getRGB(x, y) >>> 24;
		}
		return out;
	}

	private static BufferedImage grayscale(BufferedImage source) {
		int w = source.getWidth();
		int h = source.getHeight();
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int argb = source.getRGB(x, y);
				int a = argb >>> 24;
				int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
				float lum = 0.299f * r + 0.587f * g + 0.114f * b;
				int v = Math.min(255, Math.round(lum * 1.15f + 28f));
				out.setRGB(x, y, (a << 24) | (v << 16) | (v << 8) | v);
			}
		}
		return out;
	}
}
