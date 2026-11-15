package io.github.frostmta.zadrinth.client.cosmetics.render;

/**
 * Asa 3D "de blocos": cada pixel da textura vira um bloquinho com espessura,
 * como os itens do Minecraft na mão, mas com espessuras diferentes (o osso é
 * mais grosso que a membrana). A espessura vem do alfa da textura:
 * 255 = osso, 254 = garra/espinho, 253 = membrana.
 *
 * A malha é montada uma vez por asa, em duas partes (do ombro até a dobra e
 * da dobra até a ponta), já nas coordenadas locais de cada parte:
 * x para fora (negativo), y para baixo, z = espessura. Cada face tem 4
 * vértices {x, y, z, u, v, nx, ny, nz}.
 */
public final class VoxelWing {
	/** Espessuras (em pixels do modelo) */
	private static final float BONE = 1.5F;
	private static final float CLAW = 1.1F;
	private static final float MEMBRANE = 0.6F;

	/** Faces da parte de dentro (ombro → dobra) e de fora (dobra → ponta) */
	public final float[] inner;
	public final float[] outer;
	/** Comprimento da parte de dentro (para posicionar a dobra) */
	public final float innerLength;

	private VoxelWing(float[] inner, float[] outer, float innerLength) {
		this.inner = inner;
		this.outer = outer;
		this.innerLength = innerLength;
	}

	public static float thickness(int alpha) {
		if (alpha <= 25) return 0.0F;
		if (alpha >= 255) return BONE;
		if (alpha == 254) return CLAW;
		return MEMBRANE;
	}

	/**
	 * @param alpha  alfa de cada pixel (w*h, linha por linha)
	 * @param span   largura da asa (pixels do modelo)
	 * @param height altura da asa (pixels do modelo)
	 * @param jointV altura do ombro na textura (0..1)
	 * @param hingeU posição da dobra na textura (0..1)
	 */
	public static VoxelWing build(int w, int h, int[] alpha, float span, float height, float jointV, float hingeU) {
		float[] t = new float[w * h];
		for (int i = 0; i < t.length; i++) t[i] = thickness(alpha[i]);
		int hinge = Math.max(1, Math.min(w - 1, Math.round(hingeU * w)));
		float cell = span / w;
		float rowH = height / h;
		float top = -jointV * height;
		Builder in = new Builder(), out = new Builder();
		for (int r = 0; r < h; r++) {
			//Frente e verso: junta os pixels vizinhos de mesma espessura numa face só
			int c = 0;
			while (c < w) {
				float th = t[r * w + c];
				if (th <= 0) {
					c++;
					continue;
				}
				boolean innerPart = c < hinge;
				int end = c;
				while (end + 1 < w && t[r * w + end + 1] == th && (end + 1 < hinge) == innerPart) end++;
				Builder b = innerPart ? in : out;
				int origin = innerPart ? 0 : hinge;
				float x0 = -(c - origin) * cell, x1 = -(end + 1 - origin) * cell;
				float y0 = top + r * rowH, y1 = y0 + rowH;
				float u0 = (float) c / w, u1 = (float) (end + 1) / w;
				float v0 = (float) r / h, v1 = (float) (r + 1) / h;
				float z = th / 2.0F;
				b.quad(x0, y0, z, u0, v0, x1, y0, z, u1, v0, x1, y1, z, u1, v1, x0, y1, z, u0, v1, 0, 0, 1);
				b.quad(x0, y0, -z, u0, v0, x0, y1, -z, u0, v1, x1, y1, -z, u1, v1, x1, y0, -z, u1, v0, 0, 0, -1);
				c = end + 1;
			}
			//Laterais: onde o vizinho é mais fino (ou vazio), aparece a "parede" do bloquinho
			for (c = 0; c < w; c++) {
				float th = t[r * w + c];
				if (th <= 0) continue;
				boolean innerPart = c < hinge;
				Builder b = innerPart ? in : out;
				int origin = innerPart ? 0 : hinge;
				float xa = -(c - origin) * cell, xb = -(c + 1 - origin) * cell;
				float ya = top + r * rowH, yb = ya + rowH;
				//UV no meio do pixel (a parede usa a cor dele)
				float uc = (c + 0.5F) / w, vc = (r + 0.5F) / h;
				float du = 0.25F / w, dv = 0.25F / h;
				float zl = sideNeighbor(t, w, h, c - 1, r, hinge, innerPart);
				float zr = sideNeighbor(t, w, h, c + 1, r, hinge, innerPart);
				float zu = sideNeighbor(t, w, h, c, r - 1, hinge, innerPart);
				float zd = sideNeighbor(t, w, h, c, r + 1, hinge, innerPart);
				//Lado da coluna anterior (perto do ombro): x = xa
				walls(b, th, zl, xa, ya, xa, yb, uc, vc, du, dv, 1, 0, 0);
				//Lado da próxima coluna (para fora): x = xb
				walls(b, th, zr, xb, yb, xb, ya, uc, vc, du, dv, -1, 0, 0);
				//Em cima
				walls(b, th, zu, xb, ya, xa, ya, uc, vc, du, dv, 0, -1, 0);
				//Embaixo
				walls(b, th, zd, xa, yb, xb, yb, uc, vc, du, dv, 0, 1, 0);
			}
		}
		return new VoxelWing(in.toArray(), out.toArray(), hinge * cell);
	}

	/** Espessura do vizinho (a dobra conta como borda: cada parte fecha a sua). */
	private static float sideNeighbor(float[] t, int w, int h, int c, int r, int hinge, boolean innerPart) {
		if (c < 0 || r < 0 || c >= w || r >= h) return 0.0F;
		if ((c < hinge) != innerPart) return 0.0F;
		return t[r * w + c];
	}

	/** As duas faixas de parede (frente e verso) entre a espessura do vizinho e a deste pixel. */
	private static void walls(Builder b, float th, float neighbor, float xa, float ya, float xb, float yb,
							  float uc, float vc, float du, float dv, float nx, float ny, float nz) {
		if (neighbor >= th) return;
		float zIn = neighbor / 2.0F, zOut = th / 2.0F;
		b.quad(xa, ya, zIn, uc - du, vc - dv, xb, yb, zIn, uc + du, vc - dv, xb, yb, zOut, uc + du, vc + dv, xa, ya, zOut, uc - du, vc + dv, nx, ny, nz);
		b.quad(xa, ya, -zOut, uc - du, vc - dv, xb, yb, -zOut, uc + du, vc - dv, xb, yb, -zIn, uc + du, vc + dv, xa, ya, -zIn, uc - du, vc + dv, nx, ny, nz);
	}

	private static final class Builder {
		private float[] data = new float[4096];
		private int size;

		void quad(float x0, float y0, float z0, float u0, float v0,
				  float x1, float y1, float z1, float u1, float v1,
				  float x2, float y2, float z2, float u2, float v2,
				  float x3, float y3, float z3, float u3, float v3,
				  float nx, float ny, float nz) {
			vertex(x0, y0, z0, u0, v0, nx, ny, nz);
			vertex(x1, y1, z1, u1, v1, nx, ny, nz);
			vertex(x2, y2, z2, u2, v2, nx, ny, nz);
			vertex(x3, y3, z3, u3, v3, nx, ny, nz);
		}

		private void vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
			if (size + 8 > data.length) data = java.util.Arrays.copyOf(data, data.length * 2);
			data[size++] = x;
			data[size++] = y;
			data[size++] = z;
			data[size++] = u;
			data[size++] = v;
			data[size++] = nx;
			data[size++] = ny;
			data[size++] = nz;
		}

		float[] toArray() {
			return java.util.Arrays.copyOf(data, size);
		}
	}
}
