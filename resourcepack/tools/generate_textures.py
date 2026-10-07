import sys
from pathlib import Path
from PIL import Image

SIZE = 16

PALETTE = {
	"K": (38, 24, 28),
	"R": (214, 48, 44),
	"H": (245, 106, 78),
	"D": (140, 28, 34),
	"Y": (234, 184, 36),
	"G": (255, 230, 128),
	"O": (156, 112, 20),
	"S": (160, 168, 178),
	"L": (205, 212, 220),
	"T": (96, 104, 116),
	"W": (244, 244, 240),
	"B": (60, 128, 220),
	"C": (150, 204, 255),
	"N": (46, 46, 54),
	"M": (82, 82, 94),
}


class Canvas:
	def __init__(self):
		self.pixels = {}

	def px(self, x, y, c):
		if 0 <= x < SIZE and 0 <= y < SIZE:
			self.pixels[(x, y)] = c

	def rect(self, x0, y0, x1, y1, c):
		for y in range(y0, y1 + 1):
			for x in range(x0, x1 + 1):
				self.px(x, y, c)

	def line(self, x0, y0, x1, y1, c, width=1):
		steps = max(abs(x1 - x0), abs(y1 - y0), 1)
		for i in range(steps + 1):
			x = round(x0 + (x1 - x0) * i / steps)
			y = round(y0 + (y1 - y0) * i / steps)
			for dx in range(width):
				for dy in range(width):
					self.px(x + dx, y + dy, c)

	def ring(self, cx, cy, r_out, r_in, c):
		for y in range(SIZE):
			for x in range(SIZE):
				d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
				if r_in <= d <= r_out:
					self.px(x, y, c)

	def disc(self, cx, cy, r, c):
		self.ring(cx, cy, r, 0, c)

	def outline(self, c="K"):
		filled = set(self.pixels)
		for (x, y) in filled:
			for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
				n = (x + dx, y + dy)
				if n not in filled and 0 <= n[0] < SIZE and 0 <= n[1] < SIZE:
					self.pixels[n] = c

	def image(self):
		img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
		for (x, y), c in self.pixels.items():
			img.putpixel((x, y), PALETTE[c] + (255,))
		return img


def extinguisher():
	c = Canvas()
	c.rect(4, 1, 10, 1, "L")
	c.rect(5, 2, 9, 2, "T")
	c.rect(7, 3, 8, 4, "S")
	c.rect(6, 5, 9, 5, "R")
	c.rect(5, 6, 10, 14, "R")
	c.rect(6, 6, 6, 14, "H")
	c.rect(10, 6, 10, 14, "D")
	c.rect(5, 14, 10, 14, "D")
	c.rect(5, 9, 10, 10, "W")
	c.rect(10, 9, 10, 10, "S")
	c.rect(5, 12, 10, 12, "N")
	c.line(9, 3, 12, 3, "N")
	c.line(12, 3, 12, 8, "N")
	c.rect(11, 9, 13, 10, "T")
	c.px(11, 9, "L")
	c.px(14, 12, "C")
	c.px(13, 14, "B")
	c.outline()
	return c.image()


def hose():
	c = Canvas()
	c.ring(5.5, 10.5, 5.2, 2.0, "R")
	c.ring(5.5, 10.5, 5.2, 4.2, "D")
	c.ring(5.5, 10.5, 3.2, 2.0, "H")
	c.ring(5.5, 10.5, 4.2, 3.2, "R")
	c.line(9, 6, 12, 3, "Y", 2)
	c.line(9, 6, 11, 4, "G")
	c.line(12, 4, 13, 3, "O")
	c.rect(13, 1, 14, 2, "T")
	c.px(13, 1, "L")
	c.px(15, 0, "C")
	c.px(15, 2, "B")
	c.px(15, 4, "C")
	c.outline()
	return c.image()


def pump():
	c = Canvas()
	c.rect(1, 14, 12, 14, "T")
	c.rect(2, 7, 6, 13, "T")
	c.rect(3, 7, 3, 13, "S")
	c.rect(7, 5, 10, 13, "R")
	c.rect(8, 5, 8, 13, "H")
	c.rect(10, 5, 10, 13, "D")
	c.rect(7, 3, 10, 4, "S")
	c.rect(3, 4, 9, 4, "N")
	c.rect(3, 5, 3, 6, "N")
	c.disc(4.5, 10, 1.9, "W")
	c.px(4, 10, "N")
	c.px(5, 9, "N")
	c.rect(11, 9, 13, 10, "T")
	c.px(11, 9, "L")
	c.rect(0, 9, 1, 12, "N")
	c.px(0, 13, "M")
	c.px(14, 12, "C")
	c.px(13, 14, "B")
	c.outline()
	return c.image()


TEXTURES = {"hose": hose, "pump": pump, "extinguisher": extinguisher}


def preview(images, scale=16):
	pad = 4
	w = (SIZE * scale + pad * scale) * len(images) + pad * scale
	h = SIZE * scale + 2 * pad * scale
	sheet = Image.new("RGBA", (w, h), (36, 38, 46, 255))
	light = Image.new("RGBA", (w, h // 2), (198, 198, 198, 255))
	sheet.paste(light, (0, h // 2))
	for i, img in enumerate(images):
		big = img.resize((SIZE * scale, SIZE * scale), Image.NEAREST)
		x = pad * scale // 2 + i * (SIZE * scale + pad * scale)
		sheet.alpha_composite(big, (x + pad * scale // 2, pad * scale))
	return sheet


def main():
	out = Path(sys.argv[1])
	out.mkdir(parents=True, exist_ok=True)
	images = []
	for name, fn in TEXTURES.items():
		img = fn()
		img.save(out / f"{name}.png")
		images.append(img)
	if len(sys.argv) > 2:
		preview(images).save(sys.argv[2])


if __name__ == "__main__":
	main()
