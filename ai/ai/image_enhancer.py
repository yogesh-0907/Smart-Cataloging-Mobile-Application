from __future__ import annotations

from io import BytesIO
from pathlib import Path
from typing import Dict, Optional, Tuple

import colorsys
import math

from PIL import (
    Image,
    ImageDraw,
    ImageEnhance,
    ImageFilter,
    ImageOps,
    ImageStat,
)

from rembg import remove, new_session


# ---------------------------------------------------------
# Configuration
# ---------------------------------------------------------

CANVAS_SIZE = 2000

# Reuse the model session instead of loading it for every image.
REMBG_SESSION = new_session("u2netp")


# ---------------------------------------------------------
# Background palettes
# ---------------------------------------------------------

CRAFT_PALETTES = {
    "bamboo": [
        (244, 237, 220),   # warm ivory
        (231, 238, 226),   # muted sage
        (238, 228, 210),   # pale sand
    ],

    "cane": [
        (244, 237, 220),
        (232, 239, 228),
        (239, 230, 214),
    ],

    "wood": [
        (244, 238, 226),
        (231, 236, 226),
        (235, 228, 215),
    ],

    "pottery": [
        (244, 235, 220),
        (232, 225, 213),
        (228, 237, 231),
    ],

    "clay": [
        (245, 235, 218),
        (236, 226, 214),
        (229, 237, 231),
    ],

    "ceramic": [
        (245, 243, 236),
        (231, 237, 238),
        (239, 231, 220),
    ],

    "textile": [
        (245, 239, 229),
        (233, 237, 231),
        (237, 231, 240),
    ],

    "jewelry": [
        (247, 243, 235),
        (236, 237, 233),
        (240, 232, 220),
    ],

    "metal": [
        (238, 241, 242),
        (231, 235, 237),
        (238, 233, 226),
    ],

    "handicraft": [
        (244, 238, 226),
        (233, 238, 230),
        (237, 231, 220),
    ],

    "default": [
        (245, 240, 231),
        (234, 238, 232),
        (238, 232, 221),
    ],
}


# ---------------------------------------------------------
# Utility functions
# ---------------------------------------------------------

def clamp(value: float, low: float, high: float) -> float:
    return max(low, min(high, value))


def hex_to_rgb(hex_color: str) -> Tuple[int, int, int]:
    hex_color = hex_color.lstrip("#")

    return tuple(
        int(hex_color[i:i + 2], 16)
        for i in (0, 2, 4)
    )


def normalize_text(value) -> str:
    if isinstance(value, list):
        return " ".join(str(x) for x in value).lower()

    return str(value or "").lower()


# ---------------------------------------------------------
# Product-aware background selection
# ---------------------------------------------------------

def choose_background_color(
    product_info: Optional[Dict] = None,
) -> Tuple[int, int, int]:

    product_info = product_info or {}

    text = " ".join([
        normalize_text(product_info.get("category")),
        normalize_text(product_info.get("product_type")),
        normalize_text(product_info.get("material")),
        normalize_text(product_info.get("style")),
    ])

    for keyword, palette in CRAFT_PALETTES.items():

        if keyword in text:

            # Pick the first safe color from the palette.
            return palette[0]

    return CRAFT_PALETTES["default"][0]


# ---------------------------------------------------------
# Automatic complementary background
# ---------------------------------------------------------

def estimate_foreground_color(
    image: Image.Image,
) -> Tuple[int, int, int]:

    rgba = image.convert("RGBA")

    pixels = []

    # Sample pixels instead of processing every pixel.
    step = max(1, min(rgba.size) // 100)

    for y in range(0, rgba.height, step):

        for x in range(0, rgba.width, step):

            r, g, b, a = rgba.getpixel((x, y))

            if a > 220:
                pixels.append((r, g, b))

    if not pixels:
        return (150, 130, 110)

    # Average a limited sample.
    pixels = pixels[:5000]

    r = sum(p[0] for p in pixels) / len(pixels)
    g = sum(p[1] for p in pixels) / len(pixels)
    b = sum(p[2] for p in pixels) / len(pixels)

    return int(r), int(g), int(b)


def complementary_soft_color(
    rgb: Tuple[int, int, int],
) -> Tuple[int, int, int]:

    r, g, b = [value / 255 for value in rgb]

    h, s, l = colorsys.rgb_to_hls(r, g, b)

    # Complementary hue.
    h = (h + 0.5) % 1.0

    # Strongly reduce saturation so the background
    # does not compete with the craft.
    s = clamp(s * 0.35, 0.05, 0.18)

    # Keep background bright and soft.
    l = clamp(0.90 - (s * 0.15), 0.82, 0.94)

    nr, ng, nb = colorsys.hls_to_rgb(h, l, s)

    return (
        int(nr * 255),
        int(ng * 255),
        int(nb * 255),
    )


# ---------------------------------------------------------
# Background generation
# ---------------------------------------------------------

def create_soft_gradient(
    size: Tuple[int, int],
    base_color: Tuple[int, int, int],
) -> Image.Image:

    width, height = size

    image = Image.new(
        "RGB",
        size,
        base_color,
    )

    pixels = image.load()

    r, g, b = base_color

    for y in range(height):

        vertical = y / max(1, height - 1)

        for x in range(width):

            horizontal = x / max(1, width - 1)

            # Very subtle brightness variation.
            factor = (
                1.03
                - 0.035 * vertical
                + 0.015 * (1 - horizontal)
            )

            pixels[x, y] = (
                int(clamp(r * factor, 0, 255)),
                int(clamp(g * factor, 0, 255)),
                int(clamp(b * factor, 0, 255)),
            )

    return image


# ---------------------------------------------------------
# Product mask cleanup
# ---------------------------------------------------------

def clean_alpha(
    alpha: Image.Image,
) -> Image.Image:

    alpha = alpha.convert("L")

    # Remove tiny isolated noise.
    alpha = alpha.filter(
        ImageFilter.MedianFilter(size=3)
    )

    # Slightly soften jagged edges.
    alpha = alpha.filter(
        ImageFilter.GaussianBlur(radius=0.35)
    )

    return alpha


# ---------------------------------------------------------
# Product enhancement
# ---------------------------------------------------------

def enhance_foreground(
    image: Image.Image,
) -> Image.Image:

    rgba = image.convert("RGBA")

    rgb = rgba.convert("RGB")

    # Gentle corrections.
    rgb = ImageEnhance.Brightness(
        rgb
    ).enhance(1.05)

    rgb = ImageEnhance.Contrast(
        rgb
    ).enhance(1.06)

    rgb = ImageEnhance.Color(
        rgb
    ).enhance(1.03)

    rgb = ImageEnhance.Sharpness(
        rgb
    ).enhance(1.10)

    # Very mild unsharp mask.
    rgb = rgb.filter(
        ImageFilter.UnsharpMask(
            radius=1,
            percent=60,
            threshold=3,
        )
    )

    rgb.putalpha(rgba.getchannel("A"))

    return rgb


# ---------------------------------------------------------
# Resize and center product
# ---------------------------------------------------------

def place_product(
    foreground: Image.Image,
    canvas_size: int = CANVAS_SIZE,
    target_ratio: float = 0.82,
) -> Tuple[Image.Image, Tuple[int, int]]:

    foreground = foreground.convert("RGBA")

    bbox = foreground.getbbox()

    if bbox is None:
        raise ValueError(
            "No visible product was detected."
        )

    cropped = foreground.crop(bbox)

    max_dimension = canvas_size * target_ratio

    scale = min(
        max_dimension / cropped.width,
        max_dimension / cropped.height,
    )

    new_width = max(
        1,
        int(cropped.width * scale),
    )

    new_height = max(
        1,
        int(cropped.height * scale),
    )

    cropped = cropped.resize(
        (new_width, new_height),
        Image.Resampling.LANCZOS,
    )

    x = (canvas_size - new_width) // 2
    y = (canvas_size - new_height) // 2

    return cropped, (x, y)


# ---------------------------------------------------------
# Realistic soft shadow
# ---------------------------------------------------------

def create_shadow(
    product: Image.Image,
    position: Tuple[int, int],
    canvas_size: int = CANVAS_SIZE,
) -> Image.Image:

    shadow = Image.new(
        "RGBA",
        (canvas_size, canvas_size),
        (0, 0, 0, 0),
    )

    alpha = product.getchannel("A")

    # Make shadow from product mask.
    shadow_alpha = alpha.filter(
        ImageFilter.GaussianBlur(radius=28)
    )

    # Reduce shadow intensity.
    shadow_alpha = shadow_alpha.point(
        lambda value: int(value * 0.16)
    )

    shadow_layer = Image.new(
        "RGBA",
        product.size,
        (70, 60, 50, 0),
    )

    shadow_layer.putalpha(shadow_alpha)

    shadow.alpha_composite(
        shadow_layer,
        dest=(
            position[0] + 10,
            position[1] + 25,
        ),
    )

    return shadow


# ---------------------------------------------------------
# Main image generation
# ---------------------------------------------------------

def generate_product_image(
    image_bytes: bytes,
    content_type: str = "image/jpeg",
    mode: str = "craft_studio",
    product_info: Optional[Dict] = None,
) -> bytes:

    try:

        original = Image.open(
            BytesIO(image_bytes)
        ).convert("RGBA")

    except Exception as error:

        raise ValueError(
            f"Invalid image file: {error}"
        )

    # -----------------------------------------------------
    # Resize original if excessively large
    # -----------------------------------------------------

    max_input_size = 2400

    if max(original.size) > max_input_size:

        ratio = (
            max_input_size
            / max(original.size)
        )

        original = original.resize(
            (
                int(original.width * ratio),
                int(original.height * ratio),
            ),
            Image.Resampling.LANCZOS,
        )

    # -----------------------------------------------------
    # Background removal
    # -----------------------------------------------------

    cutout = remove(
        original,
        session=REMBG_SESSION,
    )

    cutout = cutout.convert("RGBA")

    alpha = clean_alpha(
        cutout.getchannel("A")
    )

    cutout.putalpha(alpha)

    # -----------------------------------------------------
    # Product enhancement
    # -----------------------------------------------------

    cutout = enhance_foreground(
        cutout
    )

    # -----------------------------------------------------
    # Determine background
    # -----------------------------------------------------

    if mode == "marketplace_main":

        background_color = (
            255,
            255,
            255,
        )

    elif mode == "complementary":

        product_color = (
            estimate_foreground_color(
                cutout
            )
        )

        background_color = (
            complementary_soft_color(
                product_color
            )
        )

    elif mode == "craft_studio":

        background_color = (
            choose_background_color(
                product_info
            )
        )

    elif mode == "transparent":

        background_color = None

    else:

        background_color = (
            choose_background_color(
                product_info
            )
        )

    # -----------------------------------------------------
    # Transparent output
    # -----------------------------------------------------

    if mode == "transparent":

        output = BytesIO()

        cutout.save(
            output,
            format="PNG",
            optimize=True,
        )

        return output.getvalue()

    # -----------------------------------------------------
    # Create canvas
    # -----------------------------------------------------

    canvas = create_soft_gradient(
        (CANVAS_SIZE, CANVAS_SIZE),
        background_color,
    ).convert("RGBA")

    # -----------------------------------------------------
    # Place product
    # -----------------------------------------------------

    product, position = place_product(
        cutout
    )

    # -----------------------------------------------------
    # Add shadow only for studio/lifestyle modes
    # -----------------------------------------------------

    if mode in {
        "craft_studio",
        "complementary",
    }:

        shadow = create_shadow(
            product,
            position,
        )

        canvas.alpha_composite(
            shadow
        )

    # -----------------------------------------------------
    # Place foreground
    # -----------------------------------------------------

    canvas.alpha_composite(
        product,
        dest=position,
    )

    # -----------------------------------------------------
    # Final output
    # -----------------------------------------------------

    output = BytesIO()

    canvas.convert("RGB").save(
        output,
        format="JPEG",
        quality=94,
        optimize=True,
        progressive=True,
    )

    return output.getvalue()


# ---------------------------------------------------------
# Backwards-compatible function
# ---------------------------------------------------------

def enhance_product_image(
    image_bytes: bytes,
) -> bytes:

    return generate_product_image(
        image_bytes=image_bytes,
        mode="craft_studio",
    )