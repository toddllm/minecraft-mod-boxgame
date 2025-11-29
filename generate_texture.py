from PIL import Image, ImageDraw
import random

def create_fear_texture():
    # Create a 64x64 image (Standard Player/Steve Layout)
    img = Image.new('RGBA', (64, 64), color=(0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # --- Colors ---
    SKIN_COLOR = (100, 100, 110, 255) # Sickly grey
    ROBE_BASE = (5, 5, 5, 255)
    TRIM_COLOR = (100, 0, 0, 255)
    EYE_OUTER = (50, 0, 0, 255)
    EYE_INNER = (200, 0, 0, 255)
    FANG_COLOR = (240, 240, 240, 255)

    # --- Helper to add noise ---
    def add_noise(x1, y1, x2, y2, color, intensity=20):
        for x in range(x1, x2):
            for y in range(y1, y2):
                r, g, b, a = color
                noise = random.randint(-intensity, intensity)
                r = max(0, min(255, r + noise))
                g = max(0, min(255, g + noise))
                b = max(0, min(255, b + noise))
                draw.point((x, y), fill=(r, g, b, a))

    # --- HEAD (Standard Steve) ---
    # Top: [8, 0, 16, 8]
    add_noise(8, 0, 16, 8, ROBE_BASE)
    # Bottom: [16, 0, 24, 8]
    add_noise(16, 0, 24, 8, SKIN_COLOR)
    # Right: [0, 8, 8, 16]
    add_noise(0, 8, 8, 16, ROBE_BASE)
    # Front (Face): [8, 8, 16, 16]
    add_noise(8, 8, 16, 16, SKIN_COLOR)
    # Left: [16, 8, 24, 16]
    add_noise(16, 8, 24, 16, ROBE_BASE)
    # Back: [24, 8, 32, 16]
    add_noise(24, 8, 32, 16, ROBE_BASE)

    # Face Details (on Front [8,8,16,16])
    # Eyes
    draw.rectangle([9, 11, 11, 12], fill=EYE_OUTER)
    draw.point((10, 11), fill=EYE_INNER)
    draw.rectangle([13, 11, 15, 12], fill=EYE_OUTER)
    draw.point((14, 11), fill=EYE_INNER)
    
    # Eyebrows
    draw.line([9, 10, 11, 11], fill=ROBE_BASE, width=1)
    draw.line([15, 10, 13, 11], fill=ROBE_BASE, width=1)
    draw.point((12, 11), fill=ROBE_BASE)

    # Fangs
    draw.line([10, 14, 14, 14], fill=(30, 30, 30, 255), width=1)
    draw.point((10, 15), fill=FANG_COLOR)
    draw.point((14, 15), fill=FANG_COLOR)

    # Scar
    draw.line([12, 9, 12, 13], fill=(80, 0, 0, 180), width=1)

    # --- BODY ---
    # Front: [20, 20, 28, 32]
    add_noise(20, 20, 28, 32, ROBE_BASE)
    # Back: [32, 20, 40, 32]
    add_noise(32, 20, 40, 32, ROBE_BASE)
    # Top/Bot/Sides...
    add_noise(16, 16, 40, 20, ROBE_BASE) # Top area
    add_noise(16, 20, 20, 32, ROBE_BASE) # Right side
    add_noise(28, 20, 32, 32, ROBE_BASE) # Left side

    # Body Details (Red Symbol)
    draw.line([22, 22, 26, 30], fill=TRIM_COLOR, width=1)
    draw.line([26, 22, 22, 30], fill=TRIM_COLOR, width=1)

    # --- ARMS (Right) ---
    # [40, 16, 56, 32] -> [44,20,48,32] is front
    add_noise(40, 16, 56, 32, ROBE_BASE)

    # --- ARMS (Left) ---
    # [32, 48, 48, 64]
    add_noise(32, 48, 48, 64, ROBE_BASE)

    # --- LEGS (Right) ---
    # [0, 16, 16, 32]
    add_noise(0, 16, 16, 32, ROBE_BASE)

    # --- LEGS (Left) ---
    # [16, 48, 32, 64]
    add_noise(16, 48, 32, 64, ROBE_BASE)

    # Save
    img.save('src/main/resources/assets/trapplatform/textures/entity/fear.png')
    print("Texture generated successfully.")

if __name__ == "__main__":
    create_fear_texture()
