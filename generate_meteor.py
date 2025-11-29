from PIL import Image, ImageDraw
import random

def generate_meteor_texture():
    # 32x32 Texture for the Meteor
    img = Image.new('RGBA', (32, 32), color=(0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Helper to draw noise
    def draw_noise(x, y, w, h, base_color, noise_amount=30):
        for i in range(x, x+w):
            for j in range(y, y+h):
                r = max(0, min(255, base_color[0] + random.randint(-noise_amount, noise_amount)))
                g = max(0, min(255, base_color[1] + random.randint(-noise_amount, noise_amount)))
                b = max(0, min(255, base_color[2] + random.randint(-noise_amount, noise_amount)))
                draw.point((i, j), fill=(r, g, b, 255))

    # Base: Magma/Rock
    draw_noise(0, 0, 32, 32, (50, 20, 10)) # Dark Rock

    # Cracks (Magma)
    for _ in range(10):
        x1 = random.randint(0, 31)
        y1 = random.randint(0, 31)
        x2 = random.randint(0, 31)
        y2 = random.randint(0, 31)
        draw.line([x1, y1, x2, y2], fill=(255, 100, 0, 255), width=1)
    
    # Glowing Core
    draw.ellipse([10, 10, 22, 22], fill=(255, 50, 0, 100))

    # Save
    img.save('src/main/resources/assets/trapplatform/textures/entity/meteor.png')
    print("Meteor texture generated.")

if __name__ == "__main__":
    generate_meteor_texture()
