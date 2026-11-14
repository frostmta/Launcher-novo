"""Atlas de ícones (brancos) do menu do Zadrinth Client: 256x256, grade 8x8 de 32px."""
import math, sys
from PIL import Image, ImageDraw
OUT = sys.argv[1]
S = 128  # desenho em 4x
W = (255, 255, 255, 255)
LW = 10

def arc(d, box, a0, a1, w=LW):
    d.arc(box, a0, a1, fill=W, width=w)

def icon(i):
    im = Image.new('RGBA', (S, S), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    if i == 0:  # teclado WASD
        k = 30; g = 6
        d.rounded_rectangle([49, 20, 49 + k, 20 + k], 6, outline=W, width=8)
        for j in range(3):
            x = 12 + j * (k + g + 1)
            d.rounded_rectangle([x, 20 + k + g, x + k, 20 + 2 * k + g], 6, outline=W, width=8)
        d.rounded_rectangle([12, 20 + 2 * k + 2 * g, 116, 20 + 2 * k + 2 * g + 20], 6, outline=W, width=8)
    elif i == 1:  # mouse
        d.rounded_rectangle([34, 14, 94, 114], 30, outline=W, width=LW)
        d.line([64, 14, 64, 56], fill=W, width=LW); d.line([34, 56, 94, 56], fill=W, width=LW)
        d.rounded_rectangle([40, 20, 62, 54], 10, fill=W)
    elif i == 2:  # barras
        for j, h in enumerate((40, 70, 100)):
            x = 18 + j * 34; d.rounded_rectangle([x, 112 - h, x + 22, 112], 5, fill=W)
    elif i == 3:  # wifi
        for r in (52, 36, 20):
            arc(d, [64 - r, 92 - r, 64 + r, 92 + r], 225, 315, 11)
        d.ellipse([56, 86, 72, 102], fill=W)
    elif i == 4:  # pino
        d.ellipse([32, 12, 96, 76], outline=W, width=LW)
        d.polygon([(38, 60), (90, 60), (64, 114)], fill=W)
        d.ellipse([42, 22, 86, 66], fill=(0, 0, 0, 0)); d.ellipse([52, 32, 76, 56], fill=W)
    elif i == 5:  # bússola
        d.ellipse([12, 12, 116, 116], outline=W, width=LW)
        d.polygon([(64, 26), (78, 64), (64, 102), (50, 64)], outline=W, width=7)
        d.polygon([(64, 26), (78, 64), (50, 64)], fill=W)
    elif i == 6:  # poção
        d.rectangle([50, 10, 78, 20], fill=W)
        d.line([56, 20, 56, 44], fill=W, width=LW); d.line([72, 20, 72, 44], fill=W, width=LW)
        d.ellipse([22, 40, 106, 118], outline=W, width=LW)
        d.chord([30, 48, 98, 110], 10, 170, fill=W)
    elif i == 7:  # escudo
        d.polygon([(64, 10), (108, 26), (102, 74), (64, 118), (26, 74), (20, 26)], outline=W, width=LW)
        d.polygon([(64, 30), (88, 40), (84, 70), (64, 96)], fill=W)
    elif i == 8:  # placar
        d.rounded_rectangle([18, 12, 110, 116], 8, outline=W, width=LW)
        d.rectangle([30, 26, 98, 38], fill=W)
        for y in (54, 72, 90):
            d.line([32, y, 72, y], fill=W, width=7); d.line([84, y, 96, y], fill=W, width=7)
    elif i == 9:  # globo
        d.ellipse([12, 12, 116, 116], outline=W, width=LW)
        d.ellipse([40, 12, 88, 116], outline=W, width=8)
        d.line([14, 64, 114, 64], fill=W, width=8); d.line([22, 38, 106, 38], fill=W, width=6); d.line([22, 90, 106, 90], fill=W, width=6)
    elif i == 10:  # relógio
        d.ellipse([12, 12, 116, 116], outline=W, width=LW)
        d.line([64, 64, 64, 32], fill=W, width=LW); d.line([64, 64, 88, 76], fill=W, width=LW)
    elif i == 11:  # chip
        d.rounded_rectangle([32, 32, 96, 96], 8, outline=W, width=LW)
        d.rectangle([50, 50, 78, 78], fill=W)
        for t in (44, 64, 84):
            for a, b in (((t, 12), (t, 30)), ((t, 98), (t, 116)), ((12, t), (30, t)), ((98, t), (116, t))):
                d.line([a, b], fill=W, width=7)
    elif i == 12:  # velocímetro
        arc(d, [12, 20, 116, 124], 180, 360, 11)
        d.line([64, 72, 92, 40], fill=W, width=LW); d.ellipse([54, 62, 74, 82], fill=W)
        for a in range(180, 361, 45):
            r = math.radians(a); d.line([64 + 40 * math.cos(r), 72 + 40 * math.sin(r), 64 + 30 * math.cos(r), 72 + 30 * math.sin(r)], fill=W, width=6)
    elif i == 13:  # alcance ↔
        d.line([18, 64, 110, 64], fill=W, width=LW)
        d.polygon([(10, 64), (34, 44), (34, 84)], fill=W); d.polygon([(118, 64), (94, 44), (94, 84)], fill=W)
        d.line([18, 30, 18, 98], fill=W, width=7); d.line([110, 30, 110, 98], fill=W, width=7)
    elif i == 14:  # raio
        d.polygon([(74, 8), (28, 72), (60, 72), (50, 120), (100, 52), (68, 52), (82, 8)], fill=W)
    elif i == 15:  # >> corrida
        for x in (18, 58):
            d.line([x, 24, x + 36, 64, x, 104], fill=W, width=14, joint='curve')
    elif i == 16:  # lupa
        d.ellipse([14, 14, 82, 82], outline=W, width=12)
        d.line([74, 74, 112, 112], fill=W, width=16)
    elif i == 17:  # sol
        d.ellipse([40, 40, 88, 88], fill=W)
        for k in range(8):
            a = k * math.pi / 4
            d.line([64 + 34 * math.cos(a), 64 + 34 * math.sin(a), 64 + 54 * math.cos(a), 64 + 54 * math.sin(a)], fill=W, width=9)
    elif i == 18:  # lua
        d.ellipse([14, 14, 114, 114], fill=W); d.ellipse([40, 2, 130, 92], fill=(0, 0, 0, 0))
        d.polygon([(96, 84), (100, 94), (110, 98), (100, 102), (96, 112), (92, 102), (82, 98), (92, 94)], fill=W)
    elif i == 19:  # cubo
        p = [(64, 12), (110, 36), (110, 90), (64, 116), (18, 90), (18, 36)]
        d.polygon(p, outline=W, width=8)
        d.line([18, 36, 64, 60, 110, 36], fill=W, width=8); d.line([64, 60, 64, 116], fill=W, width=8)
    elif i == 20:  # mira
        d.ellipse([22, 22, 106, 106], outline=W, width=9)
        for a, b in (((64, 6), (64, 40)), ((64, 88), (64, 122)), ((6, 64), (40, 64)), ((88, 64), (122, 64))):
            d.line([a, b], fill=W, width=9)
        d.ellipse([58, 58, 70, 70], fill=W)
    elif i == 21:  # lista (tab)
        for y in (26, 64, 102):
            d.rectangle([14, y - 9, 32, y + 9], fill=W); d.line([44, y, 114, y], fill=W, width=14)
    elif i == 22:  # camiseta (cosméticos)
        d.polygon([(40, 14), (24, 20), (6, 46), (24, 60), (32, 50), (32, 116), (96, 116), (96, 50), (104, 60), (122, 46), (104, 20), (88, 14), (80, 26), (64, 30), (48, 26)], outline=W, width=9)
    elif i == 23:  # lápis
        d.polygon([(86, 14), (114, 42), (46, 110), (18, 110), (18, 82)], outline=W, width=9)
        d.line([72, 28, 100, 56], fill=W, width=9)
    elif i == 24:  # engrenagem
        for k in range(8):
            a = k * math.pi / 4
            d.polygon([(64 + 46 * math.cos(a - 0.22), 64 + 46 * math.sin(a - 0.22)), (64 + 58 * math.cos(a - 0.15), 64 + 58 * math.sin(a - 0.15)),
                       (64 + 58 * math.cos(a + 0.15), 64 + 58 * math.sin(a + 0.15)), (64 + 46 * math.cos(a + 0.22), 64 + 46 * math.sin(a + 0.22))], fill=W)
        d.ellipse([20, 20, 108, 108], fill=W); d.ellipse([44, 44, 84, 84], fill=(0, 0, 0, 0))
    elif i == 25:  # fechar
        d.ellipse([10, 10, 118, 118], outline=W, width=LW)
        d.line([42, 42, 86, 86], fill=W, width=LW); d.line([86, 42, 42, 86], fill=W, width=LW)
    elif i == 26:  # casa
        d.polygon([(64, 14), (116, 60), (102, 60), (102, 114), (26, 114), (26, 60), (12, 60)], outline=W, width=9)
        d.rectangle([52, 78, 76, 114], fill=W)
    elif i == 27:  # sacola (loja)
        d.rounded_rectangle([18, 40, 110, 118], 10, outline=W, width=LW)
        arc(d, [40, 10, 88, 66], 180, 360, LW)
        d.line([40, 38, 40, 54], fill=W, width=LW); d.line([88, 38, 88, 54], fill=W, width=LW)
    elif i == 28:  # info
        d.ellipse([10, 10, 118, 118], outline=W, width=LW)
        d.ellipse([56, 28, 72, 44], fill=W); d.rectangle([57, 54, 71, 98], fill=W)
    elif i == 29:  # seta voltar
        d.line([100, 64, 30, 64], fill=W, width=14)
        d.line([62, 28, 26, 64, 62, 100], fill=W, width=14, joint='curve')
    elif i == 30:  # resetar
        arc(d, [18, 18, 110, 110], 40, 330, 12)
        d.polygon([(102, 22), (110, 60), (74, 48)], fill=W)
    elif i == 31:  # visto
        d.line([20, 66, 52, 98, 110, 32], fill=W, width=16, joint='curve')
    elif i == 32:  # bandeira/bossbar
        d.rounded_rectangle([10, 46, 118, 82], 8, outline=W, width=9); d.rectangle([22, 56, 80, 72], fill=W)
    elif i == 33:  # olho (freelook)
        arc(d, [4, 30, 124, 170], 206, 334, LW)
        arc(d, [4, -42, 124, 98], 26, 154, LW)
        d.ellipse([42, 42, 86, 86], fill=W); d.ellipse([56, 56, 72, 72], fill=(0, 0, 0, 0))
    return im.resize((32, 32), Image.LANCZOS)

atlas = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
for i in range(34):
    atlas.alpha_composite(icon(i), ((i % 8) * 32, (i // 8) * 32))
atlas.save(OUT)
prev = Image.new('RGBA', (512, 512), (40, 36, 56, 255)); prev.alpha_composite(atlas.resize((512, 512), Image.LANCZOS))
prev.save(OUT.replace('.png', '_prev.png'))
