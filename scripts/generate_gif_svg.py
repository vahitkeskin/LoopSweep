import cairosvg
from PIL import Image
import io

def create_gif():
    # Read the original SVG
    with open("shared/src/commonMain/composeResources/drawable/robot_icon.svg", "r") as f:
        svg_template = f.read()

    frames = []
    num_frames = 60

    print("Generating frames...")
    for i in range(num_frames):
        angle = (i / num_frames) * 360
        target_string = 'transform="translate(850, 150) scale(2.2)"'
        replacement_string = f'transform="translate(850, 150) scale(2.2) rotate({angle})"'
        modified_svg = svg_template.replace(target_string, replacement_string)
        
        # Expand viewBox so the brush tip doesn't get clipped
        modified_svg = modified_svg.replace('viewBox="0 0 1024 1024"', 'viewBox="-100 -100 1224 1224"')
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
    print("Done! Saved robot_animated.gif")

if __name__ == "__main__":
    create_gif()
