"""Arte dos cosméticos do Zadrinth Client (originais, em alta resolução).

Gera em <out>/:
  capes/<id>.png     256x128 (layout de capa do Minecraft em 4x)
  wings/<id>.png     192x192 (uma asa; a outra é espelhada no jogo)
  bandanas/<id>.png  288x64  (faixa 36x3 px em 8x + nó e pontas)
  icons/<cat>_<id>.png  miniaturas para a loja

Uso: python3 cosmetics_art.py <out> [<preview.png>]
"""
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

OUT = sys.argv[1]
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
RNG = np.random.default_rng(1234)


# ------------------------------------------------------------------ util
def hexrgb(h):
    h = h.lstrip('#')
    return np.array([int(h[i:i + 2], 16) for i in (0, 2, 4)], dtype=np.float32)


def lerp(a, b, t):
    return a + (b - a) * t


def ramp(stops, t):
    """stops: [(pos, '#hex'), ...]; t: array 0..1 -> (...,3)"""
    t = np.clip(t, 0, 1)
    out = np.zeros(t.shape + (3,), np.float32)
    for (p0, c0), (p1, c1) in zip(stops[:-1], stops[1:]):
        m = (t >= p0) & (t <= p1)
        k = ((t - p0) / max(p1 - p0, 1e-6))[..., None]
        out = np.where(m[..., None], lerp(hexrgb(c0), hexrgb(c1), k), out)
    return out


def value_noise(w, h, scale, seed, octaves=4, persistence=0.5):
    rng = np.random.default_rng(seed)
    total = np.zeros((h, w), np.float32)
    amp, norm = 1.0, 0.0
    for o in range(octaves):
        s = scale * (2 ** o)
        gw, gh = int(w / s) + 3, int(h / s) + 3
        grid = rng.random((gh, gw)).astype(np.float32)
        ys, xs = np.mgrid[0:h, 0:w].astype(np.float32)
        gx, gy = xs / s, ys / s
        x0, y0 = gx.astype(int), gy.astype(int)
        fx, fy = gx - x0, gy - y0
        fx = fx * fx * (3 - 2 * fx)
        fy = fy * fy * (3 - 2 * fy)
        a = grid[y0, x0]
        b = grid[y0, x0 + 1]
        c = grid[y0 + 1, x0]
        d = grid[y0 + 1, x0 + 1]
        total += amp * lerp(lerp(a, b, fx), lerp(c, d, fx), fy)
        norm += amp
        amp *= persistence
    return total / norm


def to_img(rgb, alpha=None):
    rgb = np.clip(rgb, 0, 255).astype(np.uint8)
    if alpha is None:
        alpha = np.full(rgb.shape[:2], 255, np.uint8)
    else:
        alpha = np.clip(alpha, 0, 255).astype(np.uint8)
    return Image.fromarray(np.dstack([rgb, alpha]), 'RGBA')


def add_glow(rgb, cx, cy, radius, color, strength):
    h, w = rgb.shape[:2]
    ys, xs = np.mgrid[0:h, 0:w].astype(np.float32)
    d = np.sqrt((xs - cx) ** 2 + (ys - cy) ** 2) / radius
    g = np.clip(1 - d, 0, 1) ** 2 * (strength / 100.0)
    return rgb + hexrgb(color) * g[..., None]


def stars(rgb, count, seed, max_y=None, bright=1.0):
    rng = np.random.default_rng(seed)
    h, w = rgb.shape[:2]
    max_y = max_y or h
    for _ in range(count):
        x, y = rng.integers(0, w), rng.integers(0, max_y)
        b = rng.random() ** 2 * bright
        c = 255 * (0.55 + 0.45 * b)
        tint = rng.choice([np.array([1, 1, 1]), np.array([0.85, 0.9, 1]), np.array([1, 0.95, 0.85])])
        rgb[y, x] = np.maximum(rgb[y, x], c * tint)
        if b > 0.7:  # brilho em cruz
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                xx, yy = x + dx, y + dy
                if 0 <= xx < w and 0 <= yy < h:
                    rgb[yy, xx] = np.maximum(rgb[yy, xx], c * 0.55 * tint)
    return rgb


def ridge(w, base, amp, seed, rough=0.5):
    """Linha de horizonte (montanhas): array de y por x."""
    n = value_noise(w, 1, w / 6, seed, octaves=5, persistence=rough)[0]
    return base - n * amp


def shade_mask(rgb, mask, color):
    rgb[mask] = color
    return rgb


# ------------------------------------------------------------------ capes
FW, FH = 40, 64  # face da capa em 4x (10x16 px)


def cape_texture(face, inner, edge):
    """Monta o PNG 256x128 a partir da face externa (40x64)."""
    img = Image.new('RGBA', (256, 128), (0, 0, 0, 0))
    face_img = to_img(face)
    inner_img = to_img(inner)
    img.paste(face_img, (4, 4))                      # externa (1,1)-(11,17)
    img.paste(inner_img.transpose(Image.FLIP_LEFT_RIGHT), (48, 4))  # interna (12,1)-(22,17)
    e = tuple(int(v) for v in edge) + (255,)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 4, 3, 67], fill=e)        # lado (0,1)-(1,17)
    d.rectangle([44, 4, 47, 67], fill=e)      # lado (11,1)-(12,17)
    d.rectangle([4, 0, 43, 3], fill=e)        # topo
    d.rectangle([44, 0, 83, 3], fill=e)       # base
    # élitros: repete a face
    for x in range(88, 184, 40):
        img.paste(face_img.crop((0, 0, min(40, 184 - x), 64)), (x, 0))
    return img


def lining(base_hex, seed):
    n = value_noise(FW, FH, 6, seed, 3)
    base = hexrgb(base_hex)
    weave = ((np.indices((FH, FW)).sum(0) % 4) == 0) * 6
    return base[None, None, :] * (0.85 + 0.25 * n[..., None]) + weave[..., None]


def trim(face, color_hex, width=2):
    c = hexrgb(color_hex)
    face[:width, :] = c
    face[-width:, :] = c * 0.8
    face[:, :width] = c * 0.9
    face[:, -width:] = c * 0.9
    # brilho no canto de cima
    face[:width, :FW // 2] = np.minimum(c * 1.25, 255)
    return face


def cape_nebula():
    ys = np.linspace(0, 1, FH)[:, None] * np.ones((1, FW))
    sky = ramp([(0, '#05030f'), (0.5, '#120a35'), (1, '#1a0b2e')], ys)
    n1 = value_noise(FW, FH, 10, 11, 5)
    n2 = value_noise(FW, FH, 7, 12, 5)
    cloud = np.clip((n1 - 0.45) * 2.6, 0, 1)
    cloud2 = np.clip((n2 - 0.5) * 3.0, 0, 1)
    sky += cloud[..., None] * hexrgb('#c03ad6') * 0.75
    sky += cloud2[..., None] * hexrgb('#3aa8e8') * 0.6
    sky += (cloud * cloud2)[..., None] * hexrgb('#ffe0ff') * 0.6
    sky = stars(sky, 70, 13)
    sky = add_glow(sky, 26, 20, 14, '#d9a8ff', 55)
    sky = add_glow(sky, 26, 20, 4, '#fff0ff', 110)
    for d in range(1, 7):  # raios da estrela principal
        for (x, y) in ((26 + d, 20), (26 - d, 20), (26, 20 + d), (26, 20 - d)):
            sky[y, x] = np.maximum(sky[y, x], 255 - d * 26)
    sky[20, 26] = 255
    return trim(sky, '#6b4bd6'), lining('#1b1238', 14), hexrgb('#2a1d55')


def cape_aurora():
    ys = np.linspace(0, 1, FH)[:, None] * np.ones((1, FW))
    sky = ramp([(0, '#02040d'), (0.55, '#0b1a33'), (1, '#10273f')], ys)
    sky = stars(sky, 45, 21, max_y=40)
    xs = np.arange(FW)
    # cortinas de aurora: faixas senoidais com riscos verticais
    streak = value_noise(FW, 1, 1.5, 22, 2)[0]
    for k, (col, off, amp) in enumerate((('#38f2a0', 14, 5), ('#36c9e6', 20, 4), ('#9d6bff', 9, 3))):
        center = off + amp * np.sin(xs / 6.0 + k * 1.7)
        for x in range(FW):
            top = int(center[x]) - 9
            for y in range(max(0, top), min(FH, int(center[x]) + 2)):
                t = (y - top) / 11
                a = (t ** 1.6) * (0.55 + 0.45 * streak[x]) * (0.9 if k == 0 else 0.6)
                sky[y, x] += hexrgb(col) * a
    # montanhas com neve só nos picos
    far = ridge(FW, 47, 15, 23, 0.55)
    near = ridge(FW, 57, 9, 24, 0.5)
    for x in range(FW):
        top = int(far[x])
        for y in range(top, FH):
            depth = (y - top) / 18.0
            col = lerp(hexrgb('#2c4166'), hexrgb('#15233b'), min(1.0, depth))
            if y < 39 and y < top + 3 + (x % 3 == 0):
                col = hexrgb('#e6eefa') if y < top + 2 else hexrgb('#a9bcd8')
            sky[y, x] = col
        topn = int(near[x])
        for y in range(topn, FH):
            sky[y, x] = hexrgb('#0d1828') if y > topn else hexrgb('#22344f')
    # pinheiros
    rng = np.random.default_rng(25)
    for _ in range(9):
        cx, h = rng.integers(0, FW), rng.integers(6, 11)
        base = FH - 1
        for i in range(h):
            half = int(i * 0.45) + (i % 3 == 2)
            y = base - h + i
            sky[y, max(0, cx - half):min(FW, cx + half + 1)] = hexrgb('#060d16')
    return trim(sky, '#2fd3a0'), lining('#0b1626', 26), hexrgb('#173a4a')


def cape_blood_moon():
    ys = np.linspace(0, 1, FH)[:, None] * np.ones((1, FW))
    sky = ramp([(0, '#12030a'), (0.45, '#4a0b18'), (0.75, '#b5301e'), (1, '#f08a3a')], ys)
    cx, cy, r = 20, 22, 10
    yy, xx = np.mgrid[0:FH, 0:FW]
    d = np.sqrt((xx - cx) ** 2 + (yy - cy) ** 2)
    sky = add_glow(sky, cx, cy, 24, '#ff4b2a', 60)
    moon = d <= r
    craters = value_noise(FW, FH, 3, 31, 3)
    mcol = ramp([(0, '#7a0f12'), (0.5, '#d22a1f'), (1, '#ff7a4a')], 1 - (d / r) * 0.6 + (craters - 0.5) * 0.5)
    sky[moon] = mcol[moon]
    rim = (d > r - 1.2) & (d <= r)
    sky[rim] = np.minimum(sky[rim] * 1.3, 255)
    # nuvens finas
    for y0, ph in ((18, 0), (29, 2.1)):
        for x in range(FW):
            y = int(y0 + 1.5 * math.sin(x / 4 + ph))
            sky[y:y + 2, x] = sky[y:y + 2, x] * 0.45 + hexrgb('#2a0610') * 0.55
    # silhueta de montanha e floresta
    hill = ridge(FW, 52, 12, 32, 0.6)
    for x in range(FW):
        sky[int(hill[x]):, x] = hexrgb('#0c0306')
    rng = np.random.default_rng(33)
    for _ in range(12):
        cx2, h = rng.integers(0, FW), rng.integers(5, 12)
        for i in range(h):
            half = int(i * 0.4)
            y = FH - 1 - h + i
            sky[y, max(0, cx2 - half):min(FW, cx2 + half + 1)] = hexrgb('#050103')
    # brasas
    for _ in range(14):
        x, y = rng.integers(0, FW), rng.integers(30, 58)
        sky[y, x] = hexrgb('#ffb15a')
    return trim(sky, '#c23a2a'), lining('#24070c', 34), hexrgb('#5c1414')


def crystal_polygon(cx, cy, s):
    top, right, bottom, left = (cx, cy - 1.6 * s), (cx + s, cy - 0.2 * s), (cx, cy + 1.7 * s), (cx - s, cy - 0.2 * s)
    return top, right, bottom, left


def draw_crystal(img_rgb, cx, cy, s, ss=4):
    """Cristal facetado (logo do Zadrinth) com supersampling."""
    h, w = img_rgb.shape[:2]
    big = Image.new('RGBA', (w * ss, h * ss), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    S = lambda p: (p[0] * ss, p[1] * ss)
    top, right, bottom, left = crystal_polygon(cx, cy, s)
    mid = (cx, cy - 0.2 * s)
    facets = [
        ((top, left, mid), '#9fc0ff'), ((top, mid, right), '#c6a8ff'),
        ((left, bottom, mid), '#6e7dff'), ((mid, bottom, right), '#d07bff'),
    ]
    for pts, col in facets:
        d.polygon([S(p) for p in pts], fill=col)
    d.line([S(top), S(left), S(bottom), S(right), S(top)], fill='#1b1433', width=ss)
    d.line([S(top), S(mid), S(bottom)], fill=(255, 255, 255, 140), width=max(1, ss // 2))
    d.line([S(left), S(mid), S(right)], fill=(255, 255, 255, 90), width=max(1, ss // 2))
    hl = [(cx - 0.45 * s, cy - 0.65 * s), (cx - 0.1 * s, cy - 1.25 * s), (cx + 0.05 * s, cy - 1.05 * s), (cx - 0.3 * s, cy - 0.45 * s)]
    d.polygon([S(p) for p in hl], fill=(255, 255, 255, 200))
    small = big.resize((w, h), Image.LANCZOS)
    a = np.asarray(small).astype(np.float32)
    alpha = a[..., 3:4] / 255
    return img_rgb * (1 - alpha) + a[..., :3] * alpha


def cape_crystal():
    yy, xx = np.mgrid[0:FH, 0:FW].astype(np.float32)
    t = np.clip((yy / FH) * 0.8 + (xx / FW) * 0.2, 0, 1)
    sky = ramp([(0, '#0c0a1d'), (1, '#1c1338')], t)
    diag = (((xx + yy) % 8) < 1) * 10 + (((xx - yy) % 8) < 1) * 6
    sky += diag[..., None]
    sky = add_glow(sky, 20, 30, 20, '#7a5cff', 70)
    sky = add_glow(sky, 20, 30, 12, '#e07bff', 45)
    sky = draw_crystal(sky, 20, 30, 9)
    rng = np.random.default_rng(41)
    for _ in range(16):  # faíscas
        x, y = rng.integers(3, FW - 3), rng.integers(3, FH - 3)
        sky[y, x] = np.maximum(sky[y, x], hexrgb('#e8dcff'))
    face = trim(sky, '#9d7bff', 2)
    face[2:3, 2:-2] = hexrgb('#e8dcff')  # filete claro
    return face, lining('#120d26', 42), hexrgb('#3a2a77')


def cape_abyss():
    ys = np.linspace(0, 1, FH)[:, None] * np.ones((1, FW))
    sea = ramp([(0, '#1fb7c9'), (0.25, '#0b6f99'), (0.7, '#062a52'), (1, '#020a1c')], ys)
    xs = np.arange(FW)
    # raios de luz
    for k, x0 in enumerate((6, 15, 27, 34)):
        for y in range(FH):
            x = int(x0 + y * 0.18)
            for dx in range(-1, 2):
                if 0 <= x + dx < FW:
                    sea[y, x + dx] += 18 * (1 - y / FH) * (1 - abs(dx) * 0.4)
    # ondas no topo
    for x in range(FW):
        y = int(3 + 1.5 * math.sin(x / 3.0))
        sea[:y, x] = hexrgb('#8ff3ff')
        sea[y, x] = hexrgb('#d8fbff')
    # água-viva brilhante
    cx, cy = 24, 34
    yy, xx = np.mgrid[0:FH, 0:FW]
    bell = (((xx - cx) / 6.0) ** 2 + ((yy - cy) / 4.0) ** 2 <= 1) & (yy <= cy + 1)
    sea = add_glow(sea, cx, cy, 14, '#ff6ad5', 55)
    sea[bell] = ramp([(0, '#ffd0f4'), (1, '#c13ac6')], ((yy - (cy - 4)) / 6.0))[bell]
    for i, ox in enumerate((-4, -2, 0, 2, 4)):
        for y in range(cy + 2, cy + 14):
            x = int(cx + ox + math.sin(y / 2.0 + i) * 0.8)
            if 0 <= x < FW:
                sea[y, x] = hexrgb('#f59ae6') * (1 - (y - cy) / 18)
    rng = np.random.default_rng(51)
    for _ in range(18):  # bolhas
        x, y = rng.integers(1, FW - 1), rng.integers(8, FH - 2)
        sea[y, x] = np.maximum(sea[y, x], hexrgb('#bff7ff'))
    # areia/rochas
    floor = ridge(FW, 60, 4, 52)
    for x in range(FW):
        sea[int(floor[x]):, x] = hexrgb('#0a1424')
    return trim(sea, '#36c9e6'), lining('#04182a', 53), hexrgb('#0c3b58')


def cape_spirit_flames():
    yy, xx = np.mgrid[0:FH, 0:FW].astype(np.float32)
    sky = ramp([(0, '#04030b'), (1, '#120a2a')], yy / FH)
    fine = value_noise(FW, FH, 3, 61, 3, 0.6)
    # altura de cada coluna: várias línguas (picos) somadas
    heights = np.zeros(FW, np.float32)
    rng = np.random.default_rng(64)
    for _ in range(7):
        c, wdt, hh = rng.uniform(0, FW), rng.uniform(3, 7), rng.uniform(26, 50)
        heights = np.maximum(heights, hh * np.exp(-((np.arange(FW) - c) / wdt) ** 2))
    heights = np.maximum(heights, 16)
    base = FH - heights[None, :]
    t = np.clip((yy - base) / heights[None, :], 0, 1)  # 0 = ponta, 1 = base
    t = np.clip(t + (fine - 0.5) * 0.35, 0, 1)
    inside = (yy > base + (fine - 0.5) * 6)
    col = ramp([(0, '#3b1a8f'), (0.25, '#6a3cff'), (0.55, '#4fb8ff'), (0.82, '#bfe8ff'), (1, '#ffffff')], t)
    sky = np.where(inside[..., None], col, sky)
    glow = np.clip(1 - (base - yy) / 6.0, 0, 1) * (~inside)
    sky += glow[..., None] * hexrgb('#4a2aa0') * 0.5
    for _ in range(26):  # faíscas subindo
        x, y = rng.integers(0, FW), rng.integers(4, 44)
        if not inside[y, x]:
            sky[y, x] = np.maximum(sky[y, x], hexrgb('#9fd8ff'))
    return trim(sky, '#4fb8ff'), lining('#0a0618', 63), hexrgb('#24125a')


CAPES = [
    ('nebulosa', cape_nebula), ('aurora', cape_aurora), ('lua_carmesim', cape_blood_moon),
    ('cristal', cape_crystal), ('abismo', cape_abyss), ('chamas_etereas', cape_spirit_flames),
]


# ------------------------------------------------------------------ wings
WS = 192  # textura da asa (a asa tem 24x24 px no jogo -> 8 texels por pixel)
JOINT = np.array([0.02, 0.36])  # ombro (u, v)
WRIST = np.array([0.40, 0.14])  # dobra fica em u = 10/24 ~ 0.416


def P(p):
    return (p[0] * WS * 2, p[1] * WS * 2)  # supersampling 2x


def membrane_wing(palette, seed, scales=False, spikes=False):
    big = Image.new('RGBA', (WS * 2, WS * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    tips = [np.array(p) for p in ((0.98, 0.04), (0.97, 0.40), (0.82, 0.78), (0.52, 0.96))]
    # membrana: entre o braço e os dedos, com borda de fuga recortada
    poly = [JOINT, WRIST, tips[0]]
    for a, b in zip(tips[:-1], tips[1:]):
        mid = (a + b) / 2
        inward = WRIST + (mid - WRIST) * 0.80
        for t in np.linspace(0, 1, 7)[1:-1]:  # arco côncavo
            p = (1 - t) ** 2 * a + 2 * (1 - t) * t * inward + t ** 2 * b
            poly.append(p)
        poly.append(b)
    poly += [np.array((0.30, 0.86)), np.array((0.10, 0.62)), JOINT]
    d.polygon([P(p) for p in poly], fill=palette['membrane_dark'])

    # gradiente e veias da membrana (por pixel)
    m = np.asarray(big).astype(np.float32)
    alpha = m[..., 3] > 0
    H = WS * 2
    yy, xx = np.mgrid[0:H, 0:H].astype(np.float32) / H
    dist = np.sqrt((xx - WRIST[0]) ** 2 + (yy - WRIST[1]) ** 2)
    n = value_noise(H, H, 18, seed, 4)
    t = np.clip(dist * 1.1 + (n - 0.5) * 0.25, 0, 1)
    col = ramp([(0, palette['membrane_light']), (0.55, palette['membrane_mid']), (1, palette['membrane_dark'])], t)
    veins = value_noise(H, H, 6, seed + 1, 3)
    vmask = (np.abs(veins - 0.5) < 0.018) & alpha
    col[vmask] = col[vmask] * 0.55 + hexrgb(palette['vein']) * 0.45
    if scales:
        sc = ((np.floor(xx * 40) + np.floor(yy * 40)) % 2) * 10
        col += sc[..., None] * alpha[..., None]
    m[..., :3] = np.where(alpha[..., None], col, m[..., :3])
    big = Image.fromarray(np.clip(m, 0, 255).astype(np.uint8), 'RGBA')
    d = ImageDraw.Draw(big)

    # ossos (braço + dedos) com contorno e brilho
    bones = [(JOINT, WRIST, 7)] + [(WRIST, tp, 5 - i) for i, tp in enumerate(tips)]
    for a, b, wdt in bones:
        d.line([P(a), P(b)], fill=palette['bone_dark'], width=int(wdt * 2.2))
    for a, b, wdt in bones:
        d.line([P(a), P(b)], fill=palette['bone'], width=int(wdt * 1.3))
        off = np.array((0, -0.004))
        d.line([P(a + off), P(b + off)], fill=palette['bone_light'], width=max(2, int(wdt * 0.5)))
    # garras
    for tip in [tips[0]]:
        claw = [tip, tip + np.array((0.03, -0.035)), tip + np.array((0.0, 0.03))]
        d.polygon([P(p) for p in claw], fill=palette['claw'])
    thumb = [WRIST, WRIST + np.array((0.02, -0.11)), WRIST + np.array((0.045, -0.01))]
    d.polygon([P(p) for p in thumb], fill=palette['claw'])
    d.ellipse([P(WRIST - 0.022)[0], P(WRIST - 0.022)[1], P(WRIST + 0.022)[0], P(WRIST + 0.022)[1]], fill=palette['bone'])
    if spikes:
        for t in np.linspace(0.1, 0.9, 6):
            base = JOINT + (WRIST - JOINT) * t
            sp = [base + np.array((-0.015, 0)), base + np.array((0.0, -0.05)), base + np.array((0.015, 0))]
            d.polygon([P(p) for p in sp], fill=palette['claw'])
    # contorno externo
    small = big.resize((WS, WS), Image.LANCZOS)
    arr = np.asarray(small).astype(np.float32)
    a = arr[..., 3]
    solid = a > 110
    edge = solid & ~np.roll(solid, 1, 0) | solid & ~np.roll(solid, -1, 0) | solid & ~np.roll(solid, 1, 1) | solid & ~np.roll(solid, -1, 1)
    arr[..., :3][edge] = arr[..., :3][edge] * 0.45
    arr[..., 3] = np.where(solid, 255, 0)
    return Image.fromarray(arr.astype(np.uint8), 'RGBA')


def feather(d, base, direction, length, width, colors, ss):
    """Pena: duas metades (luz/sombra), haste central e contorno."""
    direction = direction / np.linalg.norm(direction)
    normal = np.array((-direction[1], direction[0]))
    prof = lambda t: math.sin(math.pi * min(1.0, 0.12 + t * 0.95)) ** 0.7 * (1 - 0.25 * t)
    ts = np.linspace(0, 1, 12)
    spine = [base + direction * length * t for t in ts]
    left = [spine[i] + normal * width * prof(t) for i, t in enumerate(ts)]
    right = [spine[i] - normal * width * 0.85 * prof(t) for i, t in enumerate(ts)]
    S = lambda p: (p[0] * ss, p[1] * ss)
    d.polygon([S(p) for p in left + spine[::-1]], fill=colors[0])
    d.polygon([S(p) for p in spine + right[::-1]], fill=colors[1])
    d.line([S(p) for p in left], fill=colors[2], width=ss)
    d.line([S(p) for p in right], fill=colors[2], width=ss)
    d.line([S(spine[0]), S(spine[-1])], fill=colors[3], width=max(1, ss // 2 + 1))


def feather_wing(pal, seed):
    ss = 4
    W = WS
    big = Image.new('RGBA', (W * ss, W * ss), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    U = lambda u, v: np.array((u * W, v * W))
    rng = np.random.default_rng(seed)
    S0, WR, HD = U(0.03, 0.40), U(0.46, 0.15), U(0.74, 0.07)
    along = lambda a, b, t: a + (b - a) * t
    dirv = lambda deg: np.array((math.cos(math.radians(deg)), math.sin(math.radians(deg))))
    # secundárias (atrás): penduradas no braço
    for i in range(13):
        t = i / 12
        feather(d, along(S0, WR, t), dirv(97 - 22 * t + rng.normal(0, 2)), W * (0.44 + 0.08 * t), W * 0.075, pal['secondary'], ss)
    # primárias: abrem em leque a partir da mão (de fora para dentro)
    for i in range(12, -1, -1):
        t = i / 12
        ln = W * (0.42 + 0.16 * math.sin(math.pi * (0.25 + 0.6 * t)))
        feather(d, along(WR, HD, t), dirv(78 - 66 * t + rng.normal(0, 2)), ln, W * 0.07, pal['primary'], ss)
    # coberteiras grandes e pequenas por cima
    for row, (ln, wd, drop) in enumerate(((0.22, 0.065, 0.035), (0.13, 0.055, 0.0))):
        for i in range(14, -1, -1):
            t = i / 14
            base = along(S0, HD, t) + np.array((0, W * drop))
            feather(d, base, dirv(100 - 62 * t + rng.normal(0, 3)), W * ln * (1 - 0.25 * t), W * wd, pal['covert'], ss)
    # braço (borda de ataque)
    d.line([tuple(S0 * ss), tuple(WR * ss), tuple(HD * ss)], fill=pal['arm'], width=int(W * 0.04 * ss))
    small = big.resize((W, W), Image.LANCZOS)
    arr = np.asarray(small).astype(np.float32)
    solid = arr[..., 3] > 110
    yy = np.linspace(0, 1, W)[:, None] * np.ones((1, W))
    xx = np.linspace(0, 1, W)[None, :] * np.ones((W, 1))
    dist = np.sqrt((xx - 0.05) ** 2 + (yy - 0.40) ** 2)
    arr[..., :3] = arr[..., :3] * (1.12 - 0.35 * np.clip(dist, 0, 1))[..., None]
    if pal.get('tip'):
        k = np.clip((dist - 0.55) / 0.35, 0, 1)[..., None]
        arr[..., :3] = arr[..., :3] * (1 - 0.6 * k) + hexrgb(pal['tip']) * 0.6 * k
    edge = solid & (~np.roll(solid, 1, 0) | ~np.roll(solid, -1, 0) | ~np.roll(solid, 1, 1) | ~np.roll(solid, -1, 1))
    arr[..., :3][edge] = arr[..., :3][edge] * 0.45
    arr[..., 3] = np.where(solid, 255, 0)
    return Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), 'RGBA')


def crystal_wing(seed):
    ss = 4
    W = WS
    big = Image.new('RGBA', (W * ss, W * ss), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    rng = np.random.default_rng(seed)
    U = lambda u, v: np.array((u * W, v * W)) * ss
    cols = ['#6e9bff', '#8a86ff', '#9d7bff', '#b77bff', '#e07bff']
    shards = []
    for i in range(17):
        t = i / 16
        base = np.array((0.04 + 0.62 * t, 0.38 - 0.30 * t))
        ang = math.radians(98 - 88 * t + rng.normal(0, 3))
        ln = 0.40 + 0.40 * math.sin(math.pi * (0.2 + 0.65 * t))
        wdt = 0.075 + 0.025 * rng.random()
        tip = base + np.array((math.cos(ang), math.sin(ang))) * ln
        nrm = np.array((-math.sin(ang), math.cos(ang)))
        shards.append((base, tip, nrm, wdt, cols[min(4, int(t * 5))]))
    for base, tip, nrm, wdt, col in shards:
        mid = base + (tip - base) * 0.6
        left = mid + nrm * wdt
        right = mid - nrm * wdt
        d.polygon([tuple(U(*base)), tuple(U(*left)), tuple(U(*tip)), tuple(U(*right))], fill=col, outline='#1b1433')
        d.polygon([tuple(U(*base)), tuple(U(*left)), tuple(U(*tip))], fill=col)
        lighter = tuple(min(255, int(c * 1.35) + 30) for c in hexrgb(col).astype(int))
        d.polygon([tuple(U(*base)), tuple(U(*tip)), tuple(U(*right))], fill=lighter)
        d.line([tuple(U(*base)), tuple(U(*tip))], fill=(255, 255, 255, 170), width=ss)
    d.line([tuple(U(0.03, 0.40)), tuple(U(0.69, 0.10))], fill='#b9a6ff', width=int(W * 0.09 * ss))
    for i in range(12):  # segunda camada, curta, por cima
        t = i / 11
        base = np.array((0.04 + 0.6 * t, 0.38 - 0.29 * t))
        ang = math.radians(95 - 70 * t)
        tip = base + np.array((math.cos(ang), math.sin(ang))) * (0.20 + 0.06 * math.sin(math.pi * t))
        nrm = np.array((-math.sin(ang), math.cos(ang))) * 0.065
        mid = base + (tip - base) * 0.55
        d.polygon([tuple(U(*base)), tuple(U(*(mid + nrm))), tuple(U(*tip)), tuple(U(*(mid - nrm)))], fill='#c9b8ff')
        d.polygon([tuple(U(*base)), tuple(U(*(mid + nrm))), tuple(U(*tip))], fill='#e8dcff')
    d.line([tuple(U(0.02, 0.38)), tuple(U(0.68, 0.07))], fill='#f3ecff', width=int(W * 0.03 * ss))
    small = big.resize((W, W), Image.LANCZOS)
    arr = np.asarray(small).astype(np.float32)
    solid = arr[..., 3] > 110
    edge = solid & (~np.roll(solid, 1, 0) | ~np.roll(solid, -1, 0) | ~np.roll(solid, 1, 1) | ~np.roll(solid, -1, 1))
    arr[..., :3][edge] = hexrgb('#1b1433')
    arr[..., 3] = np.where(solid, 255, 0)
    return Image.fromarray(arr.astype(np.uint8), 'RGBA')


WINGS = [
    ('demonio', lambda: membrane_wing(dict(
        membrane_dark='#3a0610', membrane_mid='#8f1320', membrane_light='#e2384a', vein='#ff7a6a',
        bone='#2a1a1e', bone_dark='#0c0507', bone_light='#5e3f45', claw='#e9e1d2'), 71)),
    ('dragao_esmeralda', lambda: membrane_wing(dict(
        membrane_dark='#06331f', membrane_mid='#13804a', membrane_light='#5fe3a0', vein='#b9ffd8',
        bone='#1d5a2c', bone_dark='#061a0d', bone_light='#59b56a', claw='#f2e6b8'), 72, scales=True, spikes=True)),
    ('celestial', lambda: feather_wing(dict(
        primary=('#ffffff', '#dfe6f7', '#8f9bbd', '#c9d3ee'), secondary=('#f6f8ff', '#d5ddf2', '#8793b6', '#c2cbe6'),
        covert=('#ffffff', '#e8edf9', '#9aa6c8', '#d8def2'), arm='#fff6d8', tip='#f2c65a'), 73)),
    ('sombra', lambda: feather_wing(dict(
        primary=('#3a3156', '#251f3a', '#07060d', '#6a58a8'), secondary=('#332b4d', '#201a33', '#07060d', '#6a58a8'),
        covert=('#4a3f70', '#2f2750', '#08070f', '#8a76d0'), arm='#5b4b8f', tip='#b48cff'), 74)),
    ('cristal', lambda: crystal_wing(75)),
]


# ------------------------------------------------------------------ bandanas
BW, BH = 288, 64  # faixa 36x3 px (8x) em cima; nó e pontas embaixo


def bandana_base(color_a, color_b):
    img = np.zeros((BH, BW, 3), np.float32)
    yy = np.linspace(0, 1, 24)[:, None] * np.ones((1, BW))
    img[:24] = ramp([(0, color_a), (1, color_b)], yy)
    # dobras do tecido
    folds = (np.sin(np.arange(BW) / 5.0)[None, :] * 10)
    img[:24] += folds[..., None]
    img[0:2] *= 0.7
    img[22:24] *= 0.65
    return img


def bandana_tails(img, color_a, color_b):
    yy = np.linspace(0, 1, 40)[:, None] * np.ones((1, BW))
    img[24:] = ramp([(0, color_a), (1, color_b)], yy)
    folds = np.sin(np.arange(BW) / 3.0)[None, :] * 12
    img[24:] += folds[..., None]
    return img


def paisley(img, seed, ink, dot):
    big = Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), 'RGB').resize((BW * 2, BH * 2), Image.NEAREST)
    d = ImageDraw.Draw(big)
    rng = np.random.default_rng(seed)
    for x0 in range(0, BW * 2, 56):
        for y0, flip in ((10, 1), (30, -1)):
            cx, cy = x0 + rng.integers(4, 20), y0
            # gota de paisley
            pts = []
            for t in np.linspace(0, 2 * math.pi, 24):
                r = 7 + 3 * math.cos(t)
                pts.append((cx + r * math.cos(t) * 1.2, cy + flip * r * math.sin(t) * 0.7 - flip * 2 * math.sin(t / 2) ** 4))
            d.polygon(pts, outline=ink, fill=None)
            d.ellipse([cx - 3, cy - 2, cx + 3, cy + 2], outline=ink)
            d.point([(cx, cy)], fill=dot)
    for x in range(0, BW * 2, 8):  # pontinhos nas bordas
        d.point([(x, 5), (x + 4, 42)], fill=dot)
    # pontas e nó com o mesmo padrão
    return np.asarray(big.resize((BW, BH), Image.LANCZOS)).astype(np.float32)


def bandana_emblem(img):
    """Cristal do Zadrinth na frente da faixa (frente = primeiro quarto)."""
    face = img[:24, :72].copy()
    face = draw_crystal(face, 36, 12, 6)
    img[:24, :72] = face
    return img


def bandana_ninja():
    img = bandana_base('#1a1626', '#0c0a14')
    img[9:15] = img[9:15] * 0.3 + hexrgb('#9d7bff') * 0.7
    img[10:11] = hexrgb('#e8dcff')
    img = bandana_tails(img, '#1a1626', '#0a0810')
    img[40:44] = img[40:44] * 0.3 + hexrgb('#9d7bff') * 0.7
    return bandana_emblem(img)


def bandana_paisley():
    img = bandana_base('#d0262f', '#8e1119')
    img = bandana_tails(img, '#c2222b', '#7a0d14')
    return paisley(img, 81, (255, 238, 225), (255, 255, 255))


def bandana_flames():
    img = bandana_base('#0d0a10', '#050407')
    img = bandana_tails(img, '#0d0a10', '#050407')
    n = value_noise(BW, BH, 4, 82, 4, 0.6)
    yy = np.mgrid[0:BH, 0:BW][0].astype(np.float32)
    band_t = np.where(yy < 24, (yy / 24), (yy - 24) / 40)
    f = np.clip(band_t + (n - 0.5) * 0.7 - 0.25, 0, 1)
    col = ramp([(0, '#5a0a00'), (0.4, '#e83a0c'), (0.75, '#ffb21e'), (1, '#fff3c0')], f)
    return np.where((f > 0.15)[..., None], col, img)


def bandana_galaxy():
    img = np.zeros((BH, BW, 3), np.float32)
    n1 = value_noise(BW, BH, 9, 83, 5)
    n2 = value_noise(BW, BH, 6, 84, 5)
    img += hexrgb('#0a0620')
    img += np.clip((n1 - 0.45) * 2.5, 0, 1)[..., None] * hexrgb('#b23ad6') * 0.8
    img += np.clip((n2 - 0.5) * 3.0, 0, 1)[..., None] * hexrgb('#3aa8e8') * 0.7
    img = stars(img, 120, 85)
    img[0:2] *= 0.6
    img[22:24] *= 0.6
    return img


def bandana_zadrinth():
    img = np.zeros((BH, BW, 3), np.float32)
    xs = np.linspace(0, 1, BW)[None, :] * np.ones((BH, 1))
    img[:] = ramp([(0, '#6e9bff'), (0.5, '#9d7bff'), (1, '#e07bff')], (xs * 2) % 1.0)
    img[:24] += np.sin(np.arange(BW) / 5.0)[None, :, None] * 10
    img[0:2] *= 0.7
    img[22:24] *= 0.65
    return bandana_emblem(img)


BANDANAS = [
    ('ninja', bandana_ninja), ('paisley', bandana_paisley), ('chamas', bandana_flames),
    ('galaxia', bandana_galaxy), ('zadrinth', bandana_zadrinth),
]


# ------------------------------------------------------------------ main
def main():
    for sub in ('capes', 'wings', 'bandanas'):
        os.makedirs(os.path.join(OUT, sub), exist_ok=True)
    out_capes, out_wings, out_bandanas = [], [], []
    for cid, fn in CAPES:
        face, inner, edge = fn()
        tex = cape_texture(face, inner, edge)
        tex.save(os.path.join(OUT, 'capes', f'{cid}.png'), optimize=True)
        out_capes.append((cid, to_img(face)))
    for wid, fn in WINGS:
        tex = fn()
        tex.save(os.path.join(OUT, 'wings', f'{wid}.png'), optimize=True)
        out_wings.append((wid, tex))
    for bid, fn in BANDANAS:
        arr = fn()
        tex = to_img(arr)
        tex.save(os.path.join(OUT, 'bandanas', f'{bid}.png'), optimize=True)
        out_bandanas.append((bid, tex))

    if PREVIEW:
        sheet = Image.new('RGBA', (1500, 1000), (22, 20, 30, 255))
        x = 20
        for cid, face in out_capes:
            sheet.alpha_composite(face.resize((160, 256), Image.NEAREST), (x, 20))
            x += 180
        x = 20
        for wid, tex in out_wings:
            pair = Image.new('RGBA', (2 * 140 + 40, 140), (0, 0, 0, 0))
            w = tex.resize((140, 140), Image.LANCZOS)
            pair.alpha_composite(w, (160, 0))
            pair.alpha_composite(w.transpose(Image.FLIP_LEFT_RIGHT), (20, 0))
            ImageDraw.Draw(pair).rectangle([150, 40, 170, 110], fill=(60, 160, 200, 255))
            sheet.alpha_composite(pair.resize((280, 122)), (x, 300))
            x += 290
        y = 450
        for bid, tex in out_bandanas:
            sheet.alpha_composite(tex.resize((1152, 256 // 2), Image.NEAREST).crop((0, 0, 1152, 96)), (20, y))
            y += 106
        sheet.save(PREVIEW)
    print('capes', len(out_capes), 'wings', len(out_wings), 'bandanas', len(out_bandanas))


main()
