from PIL import Image, ImageDraw, ImageFilter
import random

def generate_void_queen_texture():
    # 64x64 Player UV
    img = Image.new('RGBA', (64, 64), color=(0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Helper to draw noise
    def draw_noise(x, y, w, h, base_color, noise_amount=30):
        for i in range(x, x+w):
            for j in range(y, y+h):
                r = max(0, min(255, base_color[0] + random.randint(-noise_amount, noise_amount)))
                g = max(0, min(255, base_color[1] + random.randint(-noise_amount, noise_amount)))
                b = max(0, min(255, base_color[2] + random.randint(-noise_amount, noise_amount)))
                draw.point((i, j), fill=(r, g, b, 255))

    # --- Head (8x8x8) ---
    # Front: (8, 8, 8, 8)
    # Even though we have a block on the head, we should still texture the base head just in case
    draw_noise(8, 8, 8, 8, (20, 0, 40)) # Dark Void Purple
    # Eyes
    draw.rectangle([10, 10, 11, 11], fill=(255, 100, 0, 255)) # Magma Eye Left
    draw.rectangle([13, 10, 14, 11], fill=(255, 100, 0, 255)) # Magma Eye Right

    # --- Body (8x12x4) ---
    # Front: (20, 20, 8, 12)
    draw_noise(20, 20, 8, 12, (10, 0, 20)) # Darker Void
    # Crystal Core
    draw.ellipse([22, 23, 25, 26], fill=(200, 0, 255, 255)) # Glowing Purple Core

    # --- Arms (4x12x4) ---
    # Right Arm Front: (44, 20, 4, 12)
    draw_noise(44, 20, 4, 12, (30, 0, 50))
    # Spikes (Orange/Magma)
    draw.line([44, 22, 47, 24], fill=(255, 69, 0, 255), width=1)
    draw.line([44, 26, 47, 28], fill=(255, 69, 0, 255), width=1)

    # Left Arm Front: (36, 52, 4, 12) - Wait, standard Alex/Steve layout
    # Left Arm is usually at bottom right of texture for 64x64
    # Let's assume standard 1.8+ layout
    # Right Arm: 40, 16 (Top), 44, 20 (Front)
    # Left Arm: 32, 48 (Top), 36, 52 (Front)
    draw_noise(36, 52, 4, 12, (30, 0, 50))
    # Spikes
    draw.line([36, 54, 39, 56], fill=(255, 69, 0, 255), width=1)
    
    # --- Legs (4x12x4) ---
    # Right Leg Front: (4, 20, 4, 12)
    draw_noise(4, 20, 4, 12, (10, 0, 20))
    # Left Leg Front: (20, 52, 4, 12)
    draw_noise(20, 52, 4, 12, (10, 0, 20))

    # Save
    img.save('src/main/resources/assets/trapplatform/textures/entity/void_queen.png')
    print("Void Queen texture generated.")

if __name__ == "__main__":
    generate_void_queen_texture()
