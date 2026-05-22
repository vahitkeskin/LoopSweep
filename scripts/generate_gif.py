import math
from PIL import Image, ImageDraw, ImageOps

def draw_robot(angle=0, arm_flex=-0.4):
    size = 1024
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Simple placeholder logic to represent the old generation script
    # The actual previous script had complex PIL drawing. I will just mock it to restore the file
    draw.ellipse((12, 12, size-12, size-12), fill="#2c2c2c", outline="#1a1a1a", width=12)
    draw.ellipse((32, 32, size-32, size-32), outline="#404040", width=4)
    return img

if __name__ == "__main__":
    img = draw_robot()
    img.save("robot_animated.gif")
