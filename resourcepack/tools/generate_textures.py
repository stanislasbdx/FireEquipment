import math
import sys
from pathlib import Path
from PIL import Image

SIZE = 64
LIGHT = (-0.6, -0.8)
BAYER = [[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]
OUTLINE = (42, 24, 20)

RAMPS = {
	"red": [(66, 12, 22), (116, 20, 30), (164, 30, 36), (204, 44, 42), (232, 84, 64), (252, 150, 116)],
	"brass": [(70, 42, 12), (118, 76, 20), (166, 116, 30), (210, 162, 50), (240, 206, 100), (255, 243, 176)],
	"steel": [(44, 50, 62), (80, 88, 102), (122, 132, 146), (168, 178, 190), (210, 218, 226), (248, 251, 253)],
	"rubber": [(12, 12, 16), (26, 26, 32), (44, 44, 52), (66, 66, 76), (94, 94, 106), (136, 136, 150)],
	"white": [(138, 142, 156), (180, 184, 196), (214, 217, 226), (238, 240, 244), (252, 252, 252)],
	"blue": [(12, 28, 92), (22, 50, 146), (34, 76, 194), (58, 108, 226), (108, 158, 246), (188, 218, 255)],
	"water": [(36, 96, 176), (66, 138, 220), (118, 184, 244), (182, 226, 255), (238, 250, 255)],
	"yellow": [(96, 70, 0), (150, 112, 0), (206, 160, 8), (244, 200, 20), (255, 226, 70), (255, 244, 160)],
	"green": [(14, 70, 30), (24, 112, 46), (46, 160, 70), (110, 210, 120)],
}


class Canvas:
	def __init__(self):
		self.cells = {}
		self.seq = 0

	def _put(self, x, y, ramp, level, flat=False):
		self.cells[(x, y)] = (ramp, level, flat, self.seq)

	def shape(self, inside, ramp, spec=0.0, flat_level=None):
		self.seq += 1
		for y in range(SIZE):
			for x in range(SIZE):
				n = inside(x + 0.5, y + 0.5)
				if n is None:
					continue
				if flat_level is not None:
					self._put(x, y, ramp, flat_level, True)
					continue
				nx, ny = n
				m = min(1.0, math.hypot(nx, ny))
				nz = math.sqrt(max(0.0, 1 - m * m))
				d = nx * LIGHT[0] + ny * LIGHT[1]
				level = 0.30 + 0.38 * nz + 0.42 * d
				if spec and d > 0.5 and nz > 0.45:
					level += spec * (d - 0.5) * 2
				self._put(x, y, ramp, max(0.0, min(1.0, level)))

	def tube(self, p0, p1, hw0, hw1, ramp, spec=0.1, caps="round"):
		ax, ay = p1[0] - p0[0], p1[1] - p0[1]
		length = math.hypot(ax, ay) or 1e-9
		ux, uy = ax / length, ay / length

		def inside(px, py):
			rx, ry = px - p0[0], py - p0[1]
			t = (rx * ux + ry * uy) / length
			if caps == "flat" and (t < 0 or t > 1):
				return None
			tc = max(0.0, min(1.0, t))
			hw = hw0 + (hw1 - hw0) * tc
			cx, cy = p0[0] + ax * tc, p0[1] + ay * tc
			vx, vy = px - cx, py - cy
			if math.hypot(vx, vy) > hw:
				return None
			return vx / hw, vy / hw

		self.shape(inside, ramp, spec)

	def disc(self, c, r, ramp, spec=0.15):
		def inside(px, py):
			vx, vy = px - c[0], py - c[1]
			if math.hypot(vx, vy) > r:
				return None
			return vx / r, vy / r

		self.shape(inside, ramp, spec)

	def ring(self, c, radius, hw, ramp, spec=0.1):
		def inside(px, py):
			vx, vy = px - c[0], py - c[1]
			d = math.hypot(vx, vy) or 1e-9
			u = (d - radius) / hw
			if abs(u) > 1:
				return None
			return vx / d * u, vy / d * u

		self.shape(inside, ramp, spec)

	def box(self, x0, y0, x1, y1, ramp, radius=2.0, bevel=3.0, spec=0.1):
		def inside(px, py):
			if px < x0 or px > x1 or py < y0 or py > y1:
				return None
			cx = min(max(px, x0 + radius), x1 - radius)
			cy = min(max(py, y0 + radius), y1 - radius)
			if math.hypot(px - cx, py - cy) > radius:
				return None
			nx = ny = 0.0
			for dist, nxv, nyv in ((px - x0, -1, 0), (x1 - px, 1, 0), (py - y0, 0, -1), (y1 - py, 0, 1)):
				if dist < bevel:
					k = (1 - dist / bevel) * 0.85
					nx += nxv * k
					ny += nyv * k
			return nx, ny

		self.shape(inside, ramp, spec)

	def poly(self, points, ramp, level):
		def inside(px, py):
			hit = False
			j = len(points) - 1
			for i in range(len(points)):
				xi, yi = points[i]
				xj, yj = points[j]
				if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi) + xi:
					hit = not hit
				j = i
			return (0, 0) if hit else None

		self.shape(inside, ramp, flat_level=level)

	def rect_flat(self, x0, y0, x1, y1, ramp, level):
		self.poly([(x0, y0), (x1, y0), (x1, y1), (x0, y1)], ramp, level)

	def image(self):
		img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
		for (x, y), (ramp, level, flat, seq) in self.cells.items():
			colors = RAMPS[ramp]
			if flat:
				idx = level
			else:
				value = level * (len(colors) - 1) + (BAYER[y % 4][x % 4] / 16.0 - 0.5) * 0.7
				idx = max(0, min(len(colors) - 1, int(math.floor(value + 0.5))))
			color = colors[idx]
			for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
				other = self.cells.get((x + dx, y + dy))
				if other and other[3] < seq:
					color = tuple(int(v * 0.58) for v in color)
					break
			img.putpixel((x, y), color + (255,))
		filled = set(self.cells)
		for (x, y) in filled:
			for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
				n = (x + dx, y + dy)
				if n not in filled and 0 <= n[0] < SIZE and 0 <= n[1] < SIZE:
					img.putpixel(n, OUTLINE + (255,))
		return img


def dashes(c, x0, x1, y, ramp, level, dash=2.0, gap=1.0):
	x = x0
	while x < x1:
		c.rect_flat(x, y, min(x + dash, x1), y + 0.9, ramp, level)
		x += dash + gap


def extinguisher():
	c = Canvas()
	cx = 29
	c.disc((cx, 28), 12, "red", 0.14)
	c.tube((cx, 28), (cx, 55), 12, 12, "red", 0.16, "flat")
	c.box(cx - 12, 54, cx + 12, 58.5, "rubber", 1.5, 2.0, 0.15)
	c.tube((cx, 15), (cx, 19), 4.0, 4.0, "steel", 0.3, "flat")
	c.tube((cx, 18.5), (cx, 21.5), 6.8, 6.8, "rubber", 0.2, "flat")
	c.tube((cx, 30), (cx, 52), 9.5, 9.5, "white", 0.05, "flat")
	c.rect_flat(cx - 8, 31, cx + 8, 35.5, "white", 4)
	c.rect_flat(cx - 8, 31, cx + 8, 31.8, "red", 3)
	c.rect_flat(cx - 8, 35.2, cx + 8, 36, "red", 3)
	dashes(c, cx - 6.5, cx + 6.5, 32.6, "red", 2, 2.2, 0.8)
	dashes(c, cx - 6.5, cx + 6.5, 34.0, "red", 2, 2.2, 0.8)
	for i, y in enumerate((38.2, 41.4)):
		c.disc((cx - 6.5, y + 0.6), 1.1, "blue", 0.0)
		dashes(c, cx - 4, cx + 7, y, "rubber", 3, 2.4, 0.8)
		dashes(c, cx - 4, cx + 5 - i * 1.5, y + 1.3, "rubber", 3, 2.4, 0.8)
	c.rect_flat(cx - 7, 45.5, cx - 2.5, 50.5, "white", 3)
	c.rect_flat(cx - 7, 45.5, cx - 2.5, 46.2, "red", 2)
	c.disc((cx - 4.75, 48.4), 1.1, "rubber", 0.0)
	c.rect_flat(cx - 1.5, 45.5, cx + 3, 50.5, "red", 3)
	c.poly([(cx + 0.75, 46.5), (cx + 2.1, 48.4), (cx + 0.75, 49.8), (cx - 0.6, 48.4)], "white", 4)
	c.rect_flat(cx + 4, 45.5, cx + 8.5, 50.5, "blue", 3)
	c.disc((cx + 6.25, 48.2), 1.2, "white", 0.0)
	c.box(cx - 6.5, 8.5, cx + 6.5, 16.5, "rubber", 3.0, 3.0, 0.25)
	c.tube((cx - 3, 7.5), (cx + 15, 9.5), 1.9, 1.9, "yellow", 0.3)
	c.ring((cx - 9.5, 12.5), 2.5, 0.8, "steel", 0.4)
	c.tube((cx - 7, 11.8), (cx - 4.5, 11), 0.7, 0.7, "steel", 0.3)
	c.tube((cx + 5, 13.5), (cx + 9, 13.5), 2.8, 2.8, "brass", 0.35, "flat")
	c.tube((cx + 9, 13.5), (cx + 19, 13.5), 2.0, 2.0, "rubber", 0.2)
	c.tube((cx + 19, 13.5), (cx + 23, 21), 2.0, 2.0, "rubber", 0.2)
	c.tube((cx + 23, 21), (cx + 23, 40), 2.0, 2.0, "rubber", 0.2)
	c.tube((cx + 23, 40), (cx + 19, 47), 2.0, 2.0, "rubber", 0.2)
	c.tube((cx + 19, 47), (cx + 15, 54), 3.4, 2.2, "rubber", 0.25)
	c.tube((cx + 19, 47), (cx + 17.6, 49.5), 4.0, 4.0, "steel", 0.4, "flat")
	return c.image()


def hose():
	c = Canvas()
	center = (21, 43)
	c.ring(center, 12, 3.8, "red", 0.22)
	c.ring(center, 5.8, 3.2, "red", 0.22)
	c.tube((29.5, 35), (39, 26.5), 3.9, 3.9, "red", 0.22)
	c.tube((37, 28.5), (43, 22.5), 5.4, 5.4, "brass", 0.3, "flat")
	c.tube((43, 22.5), (45.5, 20), 6.8, 6.8, "brass", 0.3, "flat")
	c.disc((41, 27), 1.3, "brass", 0.3)
	c.tube((50, 18.5), (49, 27.5), 2.2, 2.2, "red", 0.2)
	c.tube((45.5, 20), (54.5, 11.5), 4.8, 3.4, "brass", 0.3, "flat")
	c.tube((49.5, 16.5), (52.5, 13.5), 5.6, 5.6, "rubber", 0.18, "flat")
	c.tube((54.5, 11.5), (59, 7), 3.2, 2.4, "steel", 0.35, "flat")
	c.disc((61, 9), 1.9, "water", 0.2)
	c.disc((58, 13.5), 1.6, "water", 0.2)
	c.disc((61.5, 3.5), 1.6, "water", 0.2)
	return c.image()


def pump():
	c = Canvas()
	for p0, p1 in (((9, 22), (9, 55)), ((55, 22), (55, 55)), ((9, 22), (55, 22)), ((9, 55), (55, 55))):
		c.tube(p0, p1, 2.2, 2.2, "red", 0.2)
	c.box(7, 56, 15, 61, "rubber", 1.5, 2.0, 0.05)
	c.box(49, 56, 57, 61, "rubber", 1.5, 2.0, 0.05)
	c.box(34, 24, 54, 54, "red", 4.0, 4.0, 0.2)
	c.box(36, 17, 52, 25, "rubber", 2.5, 2.5, 0.2)
	c.disc((44, 40), 6.2, "steel", 0.3)
	c.disc((44, 40), 3.2, "rubber", 0.1)
	c.tube((44, 40), (49, 46), 1.1, 1.1, "rubber", 0.1)
	c.disc((50, 47), 2.3, "red", 0.3)
	c.tube((22, 29), (22, 17), 5.0, 5.0, "brass", 0.3, "flat")
	c.tube((22, 17.5), (22, 14), 6.6, 6.6, "brass", 0.3, "flat")
	c.disc((22, 41), 15, "steel", 0.45)
	c.ring((22, 41), 13, 1.4, "steel", 0.4)
	c.ring((22, 41), 6.2, 2.6, "brass", 0.35)
	c.disc((22, 41), 3.8, "rubber", 0.1)
	c.disc((33, 32), 3.6, "steel", 0.3)
	c.disc((33, 32), 2.6, "white", 0.0)
	c.tube((33, 32), (34.2, 30.4), 0.45, 0.45, "rubber", 0.0)
	c.disc((9, 41), 2.3, "rubber", 0.1)
	return c.image()


TEXTURES = {"hose": hose, "pump": pump, "extinguisher": extinguisher}


def preview(images, scale=6):
	pad = 12
	cell = SIZE * scale
	w = pad + len(images) * (cell + pad)
	h = cell + 2 * pad + SIZE * 2 + 2 * pad
	sheet = Image.new("RGBA", (w, h), (36, 38, 46, 255))
	half = Image.new("RGBA", (w // 2, h), (196, 196, 198, 255))
	sheet.alpha_composite(half, (w // 2, 0))
	for i, img in enumerate(images):
		x = pad + i * (cell + pad)
		sheet.alpha_composite(img.resize((cell, cell), Image.NEAREST), (x, pad))
		small = img.resize((SIZE // 2, SIZE // 2), Image.LANCZOS)
		sheet.alpha_composite(small, (x, cell + 2 * pad))
		sheet.alpha_composite(img.resize((SIZE, SIZE), Image.NEAREST), (x + SIZE, cell + 2 * pad - 8))
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
