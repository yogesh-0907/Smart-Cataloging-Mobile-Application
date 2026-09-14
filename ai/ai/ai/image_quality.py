import cv2
import numpy as np
from PIL import Image
from io import BytesIO


# =========================================================
# SETTINGS
# =========================================================

MIN_WIDTH = 600
MIN_HEIGHT = 600

# This is a starting value.
# We will calibrate it using your artisan images.
BLUR_THRESHOLD = 80.0

MIN_PRODUCT_COVERAGE = 0.10
MAX_PRODUCT_COVERAGE = 0.90


# =========================================================
# IMAGE QUALITY ANALYZER
# =========================================================

def analyze_image_quality(
    image_bytes: bytes
):
    """
    Analyze an artisan product photograph before
    sending it to the enhancement pipeline.

    Checks:

    - Resolution
    - Blur
    - Brightness
    - Overexposure
    - Product framing
    - Product coverage
    - Overall quality
    """

    # -----------------------------------------------------
    # OPEN IMAGE
    # -----------------------------------------------------

    image = Image.open(
        BytesIO(image_bytes)
    ).convert("RGB")

    width, height = image.size

    # Convert PIL → OpenCV
    image_np = np.array(image)

    # RGB → BGR
    image_bgr = cv2.cvtColor(
        image_np,
        cv2.COLOR_RGB2BGR
    )

    gray = cv2.cvtColor(
        image_bgr,
        cv2.COLOR_BGR2GRAY
    )


    # =====================================================
    # 1. RESOLUTION CHECK
    # =====================================================

    resolution_ok = (
        width >= MIN_WIDTH
        and
        height >= MIN_HEIGHT
    )


    # =====================================================
    # 2. BLUR DETECTION
    # =====================================================

    laplacian = cv2.Laplacian(
        gray,
        cv2.CV_64F
    )

    blur_score = float(
        laplacian.var()
    )

    blur_ok = (
        blur_score >= BLUR_THRESHOLD
    )


    # =====================================================
    # 3. BRIGHTNESS CHECK
    # =====================================================

    brightness = float(
        np.mean(gray)
    )

    brightness_percent = round(
        (brightness / 255) * 100,
        2
    )

    too_dark = (
        brightness < 45
    )

    too_bright = (
        brightness > 235
    )

    brightness_ok = not (
        too_dark or too_bright
    )


    # =====================================================
    # 4. CONTRAST CHECK
    # =====================================================

    contrast_score = float(
        gray.std()
    )

    contrast_ok = (
        contrast_score >= 20
    )


    # =====================================================
    # 5. FRAMING CHECK
    # =====================================================

    # Detect strong edges.
    edges = cv2.Canny(
        gray,
        50,
        150
    )

    contours, _ = cv2.findContours(
        edges,
        cv2.RETR_EXTERNAL,
        cv2.CHAIN_APPROX_SIMPLE
    )

    framing_ok = True

    product_coverage = 0.0

    if contours:

        largest_contour = max(
            contours,
            key=cv2.contourArea
        )

        x, y, w, h = cv2.boundingRect(
            largest_contour
        )

        product_area = w * h

        image_area = width * height

        product_coverage = (
            product_area / image_area
        )

        # Product should not be extremely tiny
        # or touching the entire frame.
        if (
            product_coverage
            < MIN_PRODUCT_COVERAGE
        ):
            framing_ok = False

        if (
            product_coverage
            > MAX_PRODUCT_COVERAGE
        ):
            framing_ok = False

    else:

        framing_ok = False


    # =====================================================
    # 6. OVERALL QUALITY
    # =====================================================

    checks = [
        resolution_ok,
        blur_ok,
        brightness_ok,
        contrast_ok,
        framing_ok
    ]

    passed_checks = sum(
        1 for check in checks
        if check
    )

    quality_score = round(
        (
            passed_checks
            /
            len(checks)
        ) * 100,
        2
    )


    # =====================================================
    # 7. RECOMMENDATION
    # =====================================================

    if quality_score >= 80:

        status = "GOOD"

        recommendation = (
            "Image quality is good. "
            "Ready for AI enhancement."
        )

    elif quality_score >= 60:

        status = "ACCEPTABLE"

        recommendation = (
            "Image can be enhanced, "
            "but retaking the photo may "
            "improve the final result."
        )

    else:

        status = "RETAKE"

        recommendation = (
            "Image quality is too low. "
            "Please retake the photograph."
        )


    # =====================================================
    # 8. SPECIFIC WARNINGS
    # =====================================================

    warnings = []


    if not resolution_ok:

        warnings.append(
            "Image resolution is too low."
        )


    if not blur_ok:

        warnings.append(
            "Image appears blurry. "
            "Hold the camera steady."
        )


    if too_dark:

        warnings.append(
            "Image is too dark. "
            "Move to a brighter area."
        )


    if too_bright:

        warnings.append(
            "Image is overexposed. "
            "Reduce strong lighting."
        )


    if not contrast_ok:

        warnings.append(
            "Image has low contrast."
        )


    if not framing_ok:

        warnings.append(
            "Product framing needs improvement."
        )


    # =====================================================
    # RESULT
    # =====================================================

    return {

        "status": status,

        "quality_score": quality_score,

        "resolution": {
            "width": width,
            "height": height,
            "passed": resolution_ok
        },

        "blur": {
            "score": round(
                blur_score,
                2
            ),
            "passed": blur_ok,
            "threshold": BLUR_THRESHOLD
        },

        "brightness": {
            "percentage": brightness_percent,
            "passed": brightness_ok
        },

        "contrast": {
            "score": round(
                contrast_score,
                2
            ),
            "passed": contrast_ok
        },

        "framing": {
            "product_coverage": round(
                product_coverage * 100,
                2
            ),
            "passed": framing_ok
        },

        "warnings": warnings,

        "recommendation": recommendation
    }