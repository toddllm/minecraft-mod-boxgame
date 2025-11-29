from PIL import Image, ImageDraw

def create_amulet_texture():
    # Create a 16x16 item texture
    img = Image.new('RGBA', (16, 16), color=(0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Gold Chain
    draw.line([4, 0, 11, 0], fill=(255, 215, 0, 255))
    draw.line([3, 1, 3, 4], fill=(255, 215, 0, 255))
    draw.line([12, 1, 12, 4], fill=(255, 215, 0, 255))

    # Gem/Amulet Body (Purple/Black Void)
    draw.ellipse([4, 4, 11, 13], fill=(20, 0, 40, 255), outline=(50, 0, 100, 255))
    
    # Glowing Center (Red Eye)
    draw.ellipse([6, 7, 9, 10], fill=(255, 0, 0, 255))
    draw.point((7, 8), fill=(255, 200, 200, 255)) # Glint

    # Save
    img.save('src/main/resources/assets/trapplatform/textures/item/fear_amulet.png')
    print("Amulet texture generated.")

if __name__ == "__main__":
    create_amulet_texture()
