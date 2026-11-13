"""Pré-visualização 2D: jogador de costas com capa/asas e de frente com bandana."""
import sys
from PIL import Image, ImageDraw
ART = sys.argv[1]; SKIN = sys.argv[2]; OUT = sys.argv[3]
S = 12  # px de tela por pixel do skin
skin = Image.open(SKIN).convert('RGBA')
def part(box, w, h):
    return skin.crop(box).resize((w * S, h * S), Image.NEAREST)
def back_player():
    img = Image.new('RGBA', (16 * S, 32 * S), (0, 0, 0, 0))
    img.alpha_composite(part((24, 8, 32, 16), 8, 8), (4 * S, 0))
    img.alpha_composite(part((32, 20, 40, 32), 8, 12), (4 * S, 8 * S))
    img.alpha_composite(part((44, 52, 48, 64), 4, 12), (0, 8 * S))       # braço esq. (aparece à esquerda de costas)
    img.alpha_composite(part((52, 20, 56, 32), 4, 12), (12 * S, 8 * S))  # braço dir.
    img.alpha_composite(part((28, 52, 32, 64), 4, 12), (4 * S, 20 * S))
    img.alpha_composite(part((12, 20, 16, 32), 4, 12), (8 * S, 20 * S))
    return img
def front_head():
    img = Image.new('RGBA', (8 * S, 8 * S), (0, 0, 0, 0))
    img.alpha_composite(part((8, 8, 16, 16), 8, 8), (0, 0))
    return img
import os
capes = sorted(os.listdir(f'{ART}/capes')); wings = sorted(os.listdir(f'{ART}/wings')); bands = sorted(os.listdir(f'{ART}/bandanas'))
cw, ch = 40 * S, 40 * S
sheet = Image.new('RGBA', (cw * 6, ch * 2 + 14 * S), (18, 16, 26, 255))
d = ImageDraw.Draw(sheet)
for i, c in enumerate(capes):
    tex = Image.open(f'{ART}/capes/{c}').convert('RGBA')
    face = tex.crop((4, 4, 44, 68)).resize((10 * S, 16 * S), Image.LANCZOS)
    p = back_player(); p.alpha_composite(face, (3 * S, 8 * S))
    sheet.alpha_composite(p, (i * cw + 12 * S, 4 * S))
for i, w in enumerate(wings):
    tex = Image.open(f'{ART}/wings/{w}').convert('RGBA')
    W = 22 * S
    wing = tex.resize((W, W), Image.LANCZOS)
    cell = Image.new('RGBA', (cw, ch), (0, 0, 0, 0))
    ax, ay = cw // 2, 8 * S + 4 * S + 4 * S
    jy = int(0.36 * W)
    cell.alpha_composite(wing, (ax + S // 2, ay - jy))
    cell.alpha_composite(wing.transpose(Image.FLIP_LEFT_RIGHT), (ax - W - S // 2, ay - jy))
    cell.alpha_composite(back_player(), (cw // 2 - 8 * S, 4 * S + 4 * S))
    sheet.alpha_composite(cell, (i * cw, ch - 2 * S))
y0 = ch * 2 - 2 * S
for i, b in enumerate(bands):
    tex = Image.open(f'{ART}/bandanas/{b}').convert('RGBA')
    band = tex.crop((0, 0, 72, 24)).resize((8 * S + 2, 3 * S), Image.LANCZOS)
    h = front_head(); h.alpha_composite(band.crop((1, 0, 8 * S + 1, 3 * S)), (0, int(1.4 * S)))
    sheet.alpha_composite(h.resize((h.width * 3 // 2, h.height * 3 // 2), Image.NEAREST), (i * cw + 10 * S, y0 + S))
sheet.save(OUT)
