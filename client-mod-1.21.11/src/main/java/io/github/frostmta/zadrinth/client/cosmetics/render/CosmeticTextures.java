package io.github.frostmta.zadrinth.client.cosmetics.render;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.frostmta.zadrinth.client.ZadrinthClient;
import io.github.frostmta.zadrinth.client.cosmetics.Cosmetic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Texturas dos cosméticos, lidas direto do .jar. Para as opções de cor existe
 * também uma versão em tons de cinza da mesma arte, que o jogo "tinge" com a
 * cor escolhida mantendo as sombras e os detalhes.
 */
public final class CosmeticTextures {
	private static final Map<String, Identifier> CACHE = new HashMap<>();
	private static final Set<String> FAILED = new HashSet<>();
	private static final Map<Identifier, int[]> SIZES = new HashMap<>();
	/** Alfa dos pixels das asas 3D: {largura, altura, alfa...} */
	private static final Map<String, int[]> ALPHA = new HashMap<>();

	private CosmeticTextures() {
	}

	/** Precisa ser chamado na thread de render (cria a textura na GPU). */
	public static Identifier get(Cosmetic cosmetic, boolean gray) {
		String key = cosmetic.key() + (gray ? "#gray" : "");
		Identifier cached = CACHE.get(key);
		if (cached != null || FAILED.contains(key)) return cached;
		try (InputStream in = CosmeticTextures.class.getResourceAsStream(cosmetic.texturePath())) {
			if (in == null) throw new IllegalStateException("textura não encontrada: " + cosmetic.texturePath());
			NativeImage image = NativeImage.read(in);
			if (cosmetic.voxel && !ALPHA.containsKey(cosmetic.key())) ALPHA.put(cosmetic.key(), alphaOf(image));
			if (gray) grayscale(image);
			String name = "zadrinth_" + cosmetic.type.folder + "_" + cosmetic.id + (gray ? "_gray" : "");
			Identifier id = ZadrinthClient.id("dynamic/" + cosmetic.type.folder + "/" + cosmetic.id + (gray ? "_gray" : ""));
			SIZES.put(id, new int[]{image.getWidth(), image.getHeight()});
			Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> name, image));
			CACHE.put(key, id);
			return id;
		} catch (Exception e) {
			FAILED.add(key);
			ZadrinthClient.LOGGER.warn("Não foi possível carregar o cosmético {}: {}", cosmetic.key(), e.toString());
			return null;
		}
	}

	/** {largura, altura, alfa de cada pixel...} da asa 3D (depois de carregar a textura). */
	public static int[] alpha(Cosmetic cosmetic) {
		return ALPHA.get(cosmetic.key());
	}

	private static int[] alphaOf(NativeImage image) {
		int w = image.getWidth(), h = image.getHeight();
		int[] out = new int[2 + w * h];
		out[0] = w;
		out[1] = h;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) out[2 + y * w + x] = image.getPixel(x, y) >>> 24;
		}
		return out;
	}

	/** Largura e altura da textura (para desenhar partes dela na tela). */
	public static int[] size(Identifier id) {
		int[] size = SIZES.get(id);
		return size == null ? new int[]{64, 32} : size;
	}

	/** Luminância um pouco clareada (o tingimento multiplica e escureceria demais). */
	private static void grayscale(NativeImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getPixel(x, y);
				int a = argb >>> 24;
				int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
				float lum = 0.299f * r + 0.587f * g + 0.114f * b;
				int v = Math.min(255, Math.round(lum * 1.15f + 28f));
				image.setPixel(x, y, (a << 24) | (v << 16) | (v << 8) | v);
			}
		}
	}
}
