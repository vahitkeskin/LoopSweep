import cairosvg
from PIL import Image
import io

def create_gif():
    with open("shared/src/commonMain/composeResources/drawable/robot_icon.svg", "r") as f:
        svg_template = f.read()

    # Expand viewBox so the brush tip doesn't get clipped
    svg_template = svg_template.replace('viewBox="0 0 1024 1024"', 'viewBox="-100 -100 1224 1224"')

    # 1. Generate Static White PNG
    print("Generating static image...")
    static_svg = svg_template
    png_data_static = cairosvg.svg2png(bytestring=static_svg.encode('utf-8'))
    image_static = Image.open(io.BytesIO(png_data_static)).convert("RGBA")
    image_static.save("shared/src/commonMain/composeResources/files/robot_static.png")

    # 2. Generate Animated Green GIF
    print("Generating animated frames...")
    # Change Power Icon color to green (#10B981)
    # The Power icon is drawn with: stroke="#ffffff" stroke-width="3"
    # To be safe, we will replace the specific power button line:
    green_svg_template = svg_template.replace(
        '<path d="M 512 205 L 512 220 M 500 215 A 14 14 0 1 0 524 215" fill="none" stroke="#ffffff"',
        '<path d="M 512 205 L 512 220 M 500 215 A 14 14 0 1 0 524 215" fill="none" stroke="#10B981"'
    )

    frames = []
    num_frames = 60

    for i in range(num_frames):
        angle = (i / num_frames) * 360
        target_string = 'transform="translate(850, 150) scale(2.2)"'
        replacement_string = f'transform="translate(850, 150) scale(2.2) rotate({angle})"'
        
        modified_svg = green_svg_template.replace(target_string, replacement_string)
        
        png_data = cairosvg.svg2png(bytestring=modified_svg.encode('utf-8'))
        image = Image.open(io.BytesIO(png_data)).convert("RGBA")
        frames.append(image)

    print("Saving GIF...")
    frames[0].save(
        "shared/src/commonMain/composeResources/files/robot_animated.gif",
        save_all=True,
        append_images=frames[1:],
        duration=30,
        loop=0,
        disposal=2
    )
    print("Done! Saved robot_static.png and robot_animated.gif")

if __name__ == "__main__":
    create_gif()
