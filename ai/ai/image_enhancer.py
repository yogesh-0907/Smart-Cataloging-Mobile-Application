from PIL import Image, ImageEnhance
from rembg import remove, new_session
from io import BytesIO


# =========================================================
# LIGHTWEIGHT BACKGROUND REMOVAL MODEL
# =========================================================

rembg_session = new_session("u2netp")


# =========================================================
# AI PRODUCT IMAGE ENHANCER
# =========================================================

def enhance_product_image(image_bytes: bytes) -> bytes:

    print("Opening image...")

    # -----------------------------------------------------
    # OPEN IMAGE
    # -----------------------------------------------------

    image = Image.open(
        BytesIO(image_bytes)
    ).convert("RGBA")

    # -----------------------------------------------------
    # RESIZE LARGE IMAGES
    # -----------------------------------------------------

    max_size = 1600

    if max(image.size) > max_size:

        ratio = max_size / max(image.size)

        new_size = (
            int(image.width * ratio),
            int(image.height * ratio)
        )

        print(
            f"Resizing image to {new_size}..."
        )

        image = image.resize(
            new_size,
            Image.Resampling.LANCZOS
        )

    # -----------------------------------------------------
    # QUALITY CHECK
    # -----------------------------------------------------

    print("Checking image quality...")

    rgb = image.convert("RGB")

    # Brightness analysis
    brightness = ImageEnhance.Brightness(rgb)

    # Slight brightness correction
    rgb = brightness.enhance(1.08)

    print("Brightness: Improved")

    # -----------------------------------------------------
    # BACKGROUND REMOVAL
    # -----------------------------------------------------

    print("Removing background...")

    output = remove(
        rgb,
        session=rembg_session
    )

    # IMPORTANT:
    # Keep RGBA so transparent background is preserved.

    output = output.convert("RGBA")

    print(
        "Background removed successfully."
    )

    # -----------------------------------------------------
    # CONTRAST
    # -----------------------------------------------------

    print("Improving contrast...")

    alpha = output.getchannel("A")

    rgb_output = output.convert("RGB")

    contrast = ImageEnhance.Contrast(
        rgb_output
    )

    rgb_output = contrast.enhance(
        1.08
    )

    # -----------------------------------------------------
    # SHARPNESS
    # -----------------------------------------------------

    print("Improving sharpness...")

    sharpness = ImageEnhance.Sharpness(
        rgb_output
    )

    rgb_output = sharpness.enhance(
        1.15
    )

    # -----------------------------------------------------
    # RESTORE TRANSPARENCY
    # -----------------------------------------------------

    final_image = rgb_output.convert(
        "RGBA"
    )

    final_image.putalpha(
        alpha
    )

    # -----------------------------------------------------
    # SAVE PNG
    # -----------------------------------------------------

    print("Saving enhanced image...")

    output_buffer = BytesIO()

    final_image.save(
        output_buffer,
        format="PNG",
        optimize=True
    )

    print(
        "Image enhancement completed."
    )

    return output_buffer.getvalue()