"""Ícone do TAB: o símbolo do Zadrinth Client (anel de 8 cristais) em pixel art."""
import colorsys, math, sys
from PIL import Image
OUT = sys.argv[1]
# cores dos cristais no sentido horário, começando no topo (como no logo)
HUES = ['#ffb04a', '#ff6a3a', '#ff9f2e', '#4be07a', '#34e0d0', '#4b8cff', '#a07bff', '#ff5ad0']
def hexrgb(h): return tuple(int(h[i:i+2], 16) for i in (1, 3, 5))
def render(n):
    img = Image.new('RGBA', (n, n), (0, 0, 0, 0))
    px = img.load()
    c = n / 2
    s = n / 16.0
    for y in range(n):
        for x in range(n):
            dx, dy = x + 0.5 - c, y + 0.5 - c
            r = math.hypot(dx, dy) / s
            if r > 7.7: continue
            if r > 6.8:
                px[x, y] = (27, 20, 51, 255); continue
            col = (40 + int(10 * (1 - r / 7)), 29, 84, 255)
            th = math.atan2(dx, -dy)  # 0 = topo, horário
            for k in range(8):
                a = k * math.pi / 4
                u = (dx * math.sin(a) - dy * math.cos(a)) / s   # radial
                v = (dx * math.cos(a) + dy * math.sin(a)) / s   # tangencial
                d = abs(u - 3.9) / 2.9 + abs(v) / 1.75
                if d <= 1.0:
                    base = hexrgb(HUES[k])
                    light = 1.25 if v < 0 else 0.92
                    if d < 0.35 and u > 3.4: light = 1.8
                    col = tuple(min(255, int(ch * light)) for ch in base) + (255,)
            px[x, y] = col
    return img
for n in (16, 32):
    render(n).save(f'{OUT}/tab_icon_{n}.png')
