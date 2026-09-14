from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import FileResponse, HTMLResponse
from fastapi.staticfiles import StaticFiles

import traceback
import os
import tempfile
from pathlib import Path

# =========================================================
# AI IMPORTS
# =========================================================

from ai.analyzer import analyze_product as analyze_product_image
from ai.description import generate_description
from ai.pricing import recommend_price

from ai.voice import (
    transcribe_voice,
    voice_assistant,
    detect_language,
    voice_catalog
)

from ai.image_enhancer import enhance_product_image

from matching.matcher import recommend_markets


# =========================================================
# PROJECT PATHS
# =========================================================

BASE_DIR = Path(__file__).resolve().parent.parent

WEB_DIR = BASE_DIR / "web"


# =========================================================
# FASTAPI APPLICATION
# =========================================================

app = FastAPI(
    title="SIH AI Smart Catalog API",
    description=(
        "AI-powered artisan product analysis, "
        "smart cataloging, pricing, market matching "
        "and multilingual voice assistance"
    ),
    version="1.0.0"
)

import base64
from pydantic import BaseModel

class VoiceJSONRequest(BaseModel):
    filename: str
    content_type: str
    audio_base64: str


@app.post("/api/transcribe-voice-json")
async def transcribe_voice_json(request: VoiceJSONRequest):

    audio_bytes = base64.b64decode(request.audio_base64)

    if not audio_bytes:
        raise HTTPException(status_code=400, detail="Audio data is empty.")

    suffix = os.path.splitext(request.filename)[1] or ".mp3"
    temp_path = None

    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
            temp_file.write(audio_bytes)
            temp_path = temp_file.name

        result = transcribe_voice(temp_path)

        return {
            "filename": request.filename,
            "content_type": request.content_type,
            **result
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        if temp_path and os.path.exists(temp_path):
            os.unlink(temp_path)
# =========================================================
# WEB FRONTEND
# =========================================================

if WEB_DIR.exists():

    app.mount(
        "/web",
        StaticFiles(
            directory=str(WEB_DIR),
            html=True
        ),
        name="web"
    )


# =========================================================
# HOME
# =========================================================

@app.get("/")
async def home():

    return {
        "message": "SIH AI Smart Catalog API is running",
        "status": "online",

        "features": [
            "AI Product Analysis",
            "Smart Cataloging",
            "AI Description Generation",
            "Image Enhancement",
            "Dynamic Pricing",
            "Voice Transcription",
            "Multilingual Voice Assistant",
            "Multilingual AI Voice Catalog",
            "Market Matching"
        ]
    }


# =========================================================
# VOICE ASSISTANT WEB PAGE
# =========================================================

@app.get(
    "/voice",
    response_class=HTMLResponse
)
async def voice_page():

    html_path = WEB_DIR / "voice.html"

    if not html_path.exists():

        raise HTTPException(
            status_code=404,
            detail="web/voice.html not found."
        )

    try:

        with open(
            html_path,
            "r",
            encoding="utf-8"
        ) as file:

            html = file.read()

        return HTMLResponse(
            content=html
        )

    except Exception as error:

        raise HTTPException(
            status_code=500,
            detail=str(error)
        )


# =========================================================
# AI PRODUCT ANALYSIS
# =========================================================

@app.post("/api/analyze-product")
async def analyze_product(
    image: UploadFile = File(...)
):

    try:

        print(
            f"\n--- ANALYZING IMAGE: {image.filename} ---"
        )

        image_bytes = await image.read()

        if not image_bytes:

            raise HTTPException(
                status_code=400,
                detail="No image file received."
            )

        print(
            "Image received:",
            len(image_bytes),
            "bytes"
        )

        # -------------------------------------------------
        # AI IMAGE ANALYSIS
        # -------------------------------------------------

        print(
            "Running AI image analysis..."
        )

        product = analyze_product_image(
            image_bytes,
            image.content_type
        )

        print(
            "AI image analysis successful."
        )

        # -------------------------------------------------
        # DESCRIPTION
        # -------------------------------------------------

        print(
            "Generating descriptions..."
        )

        description = generate_description(
            product
        )

        print(
            "Description generation successful."
        )

        product.update(
            description
        )

        # -------------------------------------------------
        # MARKET MATCHING
        # -------------------------------------------------

        print(
            "Running market matching..."
        )

        product["recommended_markets"] = (
            recommend_markets(product)
        )

        # -------------------------------------------------
        # IMAGE INFORMATION
        # -------------------------------------------------

        product["image_filename"] = (
            image.filename
        )

        product["image_content_type"] = (
            image.content_type
        )

        print(
            "--- ANALYSIS COMPLETE ---"
        )

        return product

    except HTTPException:

        raise

    except Exception as error:

        print(
            "\n========== ANALYZE ERROR =========="
        )

        print(
            type(error).__name__
        )

        print(
            str(error)
        )

        traceback.print_exc()

        print(
            "===================================\n"
        )

        raise HTTPException(
            status_code=500,
            detail={
                "error": type(error).__name__,
                "message": str(error)
            }
        )


# =========================================================
# AI IMAGE ENHANCER
# =========================================================

@app.post(
    "/api/enhance-image",
    responses={
        200: {
            "content": {
                "image/png": {}
            },
            "description": "Enhanced product image"
        }
    }
)
async def enhance_image(
    image: UploadFile = File(...)
):

    try:

        print(
            "\n========================================"
        )

        print(
            "        AI IMAGE ENHANCER"
        )

        print(
            "========================================"
        )

        # -------------------------------------------------
        # RECEIVE IMAGE
        # -------------------------------------------------

        image_bytes = await image.read()

        if not image_bytes:

            raise HTTPException(
                status_code=400,
                detail="No image file received."
            )

        print(
            "Original image:",
            image.filename
        )

        print(
            "Original size:",
            len(image_bytes),
            "bytes"
        )

        # -------------------------------------------------
        # AI IMAGE ENHANCEMENT
        # -------------------------------------------------

        print(
            "\nRunning AI image enhancement..."
        )

        enhanced_image = enhance_product_image(
            image_bytes
        )

        # -------------------------------------------------
        # SAVE OUTPUT
        # -------------------------------------------------

        output_path = (
            Path(tempfile.gettempdir()) / "enhanced_product.png"
        )

        with open(
            output_path,
            "wb"
        ) as file:

            file.write(
                enhanced_image
            )

        print(
            "Enhanced image created successfully."
        )

        print(
            "Enhanced size:",
            len(enhanced_image),
            "bytes"
        )

        print(
            "Output:",
            output_path
        )

        print(
            "========================================"
        )

        print(
            "       ENHANCEMENT COMPLETE"
        )

        print(
            "========================================\n"
        )

        # -------------------------------------------------
        # RETURN ACTUAL PNG IMAGE
        # -------------------------------------------------

        return FileResponse(
            path=output_path,
            media_type="image/png",
            filename="enhanced_product.png"
        )

    except HTTPException:

        raise

    except Exception as error:

        print(
            "\n========== IMAGE ENHANCER ERROR =========="
        )

        print(
            type(error).__name__
        )

        print(
            str(error)
        )

        traceback.print_exc()

        print(
            "===========================================\n"
        )

        raise HTTPException(
            status_code=500,
            detail={
                "error": type(error).__name__,
                "message": str(error)
            }
        )


# =========================================================
# DYNAMIC + AI PRICE RECOMMENDATION
# =========================================================

@app.post("/api/recommend-price")
async def recommend_product_price(

    product_type: str,

    material_cost: float,

    labor_hours: float,

    labor_rate: float,

    packaging_cost: float = 0,

    overhead_percent: float = 10,

    profit_margin_percent: float = 20
):

    try:

        print(
            "\n--- PRICE RECOMMENDATION ---"
        )

        print(
            "Product:",
            product_type
        )

        # -------------------------------------------------
        # PRODUCT INFORMATION
        # -------------------------------------------------

        product = {

            "product_type":
                product_type,

            "material":
                [],

            "style":
                "Traditional"
        }

        # -------------------------------------------------
        # PRICE CALCULATION
        # -------------------------------------------------

        result = recommend_price(

            product=product,

            material_cost=material_cost,

            labor_hours=labor_hours,

            labor_rate=labor_rate,

            packaging_cost=packaging_cost,

            overhead_percent=overhead_percent,

            profit_margin_percent=profit_margin_percent
        )

        print(
            "--- PRICE CALCULATION COMPLETE ---"
        )

        return result

    except Exception as error:

        print(
            "\n========== PRICING ERROR =========="
        )

        print(
            type(error).__name__
        )

        print(
            str(error)
        )

        traceback.print_exc()

        print(
            "=================================="
        )

        raise HTTPException(
            status_code=500,
            detail={
                "error": type(error).__name__,
                "message": str(error)
            }
        )


# =========================================================
# VOICE TRANSCRIPTION ONLY
# =========================================================

@app.post("/api/transcribe-voice")
async def transcribe_voice_endpoint(
    audio: UploadFile = File(...)
):

    temp_path = None

    try:

        print(
            f"\n--- TRANSCRIBING AUDIO: {audio.filename} ---"
        )

        audio_bytes = await audio.read()

        if not audio_bytes:

            raise HTTPException(
                status_code=400,
                detail="No audio file received."
            )

        print(
            "Audio received:",
            len(audio_bytes),
            "bytes"
        )

        # -------------------------------------------------
        # TEMPORARY AUDIO FILE
        # -------------------------------------------------

        suffix = os.path.splitext(
            audio.filename or ".mp3"
        )[1] or ".mp3"

        with tempfile.NamedTemporaryFile(
            delete=False,
            suffix=suffix
        ) as temp_file:

            temp_file.write(
                audio_bytes
            )

            temp_path = temp_file.name

        # -------------------------------------------------
        # TRANSCRIPTION
        # -------------------------------------------------

        print(
            "Running voice transcription..."
        )

        result = transcribe_voice(
            temp_path
        )

        print(
            "Voice transcription successful."
        )

        return {

            "filename":
                audio.filename,

            "content_type":
                audio.content_type,

            **result
        }

    except HTTPException:

        raise

    except Exception as error:

        print(
            "\n========== VOICE ERROR =========="
        )

        print(
            type(error).__name__
        )

        print(
            str(error)
        )

        traceback.print_exc()

        print(
            "=================================\n"
        )

        raise HTTPException(
            status_code=500,
            detail={
                "error": type(error).__name__,
                "message": str(error)
            }
        )

    finally:

        if (
            temp_path
            and os.path.exists(temp_path)
        ):

            try:

                os.remove(
                    temp_path
                )

            except Exception:

                pass


# =========================================================
# COMPLETE AI VOICE ASSISTANT
# =========================================================

@app.post("/api/voice-assistant")
async def voice_assistant_endpoint(
    audio: UploadFile = File(...)
):

    temp_path = None

    try:

        print(
            "\n========================================"
        )

        print(
            "       WEB VOICE ASSISTANT"
        )

        print(
            "========================================"
        )

        # -------------------------------------------------
        # RECEIVE AUDIO
        # -------------------------------------------------

        audio_bytes = await audio.read()

        if not audio_bytes:

            raise HTTPException(
                status_code=400,
                detail="No audio file received."
            )

        print(
            "Audio received:",
            len(audio_bytes),
            "bytes"
        )

        # -------------------------------------------------
        # TEMPORARY AUDIO
        # -------------------------------------------------

        suffix = os.path.splitext(
            audio.filename or ".webm"
        )[1] or ".webm"

        with tempfile.NamedTemporaryFile(
            delete=False,
            suffix=suffix
        ) as temp_file:

            temp_file.write(
                audio_bytes
            )

            temp_path = temp_file.name

        print(
            "Temporary audio:",
            temp_path
        )

        # -------------------------------------------------
        # RUN COMPLETE ASSISTANT
        # -------------------------------------------------

        result = voice_assistant(
            temp_path
        )

        print(
            "Voice assistant completed."
        )

        # -------------------------------------------------
        # RETURN RESULT
        # -------------------------------------------------

        return {

            "success":
                True,

            "filename":
                audio.filename,

            "language":
                result["language"],

            "language_code":
                result["language_code"],

            "transcription":
                result["transcription"],

            "ai_response":
                result["response"],

            "audio_url":
                "/api/voice-assistant/audio"
        }

    except HTTPException:

        raise

    except Exception as error:

        print(
            "\n========== VOICE ASSISTANT ERROR =========="
        )

        print(
            type(error).__name__
        )

        print(
            str(error)
        )

        traceback.print_exc()

        print(
            "============================================"
        )

        raise HTTPException(
            status_code=500,
            detail={
                "error": type(error).__name__,
                "message": str(error)
            }
        )

    finally:

        if (
            temp_path
            and os.path.exists(temp_path)
        ):

            try:

                os.remove(
                    temp_path
                )

            except Exception:

                pass


# =========================================================
# GET GENERATED VOICE RESPONSE AUDIO
# =========================================================

@app.get(
    "/api/voice-assistant/audio"
)
async def get_voice_assistant_audio():

    audio_path = (
        "/tmp/voice_assistant_response.wav"
    )

    if not os.path.exists(
        audio_path
    ):

        raise HTTPException(
            status_code=404,
            detail="Voice response audio not found."
        )

    return FileResponse(

        audio_path,

        media_type="audio/wav",

        filename=
            "voice_assistant_response.wav"
    )


# =========================================================
# MULTILINGUAL AI VOICE CATALOG
# =========================================================

@app.post("/api/voice-catalog")
async def voice_catalog_endpoint(
    audio: UploadFile = File(...)
):

    temp_path = None

    try:

        print(
            "\n========================================"
        )

        print(
            "      MULTILINGUAL AI VOICE CATALOG"
        )

        print(
            "========================================"
        )

        # -------------------------------------------------
        # RECEIVE AUDIO
        # -------------------------------------------------

        audio_bytes = await audio.read()

        if not audio_bytes:

            raise HTTPException(
                status_code=400,
                detail="No audio file received."
            )

        print(
            "Audio received:",
            len(audio_bytes),
            "bytes"
        )

        # -------------------------------------------------
        # SAVE TEMP AUDIO
        # -------------------------------------------------

        suffix = os.path.splitext(
            audio.filename or ".webm"
        )[1] or ".webm"

        with tempfile.NamedTemporaryFile(
            delete=False,
            suffix=suffix
        ) as temp_file:

            temp_file.write(
                audio_bytes
            )

            temp_path = temp_file.name

        print(
            "Temporary audio:",
            temp_path
        )

        # -------------------------------------------------
        # STEP 1
        # SPEECH → TEXT
        # -------------------------------------------------

        print(
            "\n1. Transcribing voice..."
        )

        transcription_result = transcribe_voice(
            temp_path
        )

        transcription = (
            transcription_result["transcription"]
        )

        if not transcription:

            raise RuntimeError(
                "Empty transcription received."
            )

        print(
            "Transcription:"
        )

        print(
            transcription
        )

        # -------------------------------------------------
        # STEP 2
        # LANGUAGE DETECTION
        # -------------------------------------------------

        print(
            "\n2. Detecting language..."
        )

        language_result = detect_language(
            transcription
        )

        language, language_code = detect_language(
            transcription
        )

        print(
            "Language:",
            language
        )

        print(
            "Language code:",
            language_code
        )

        print(
            "Language:",
            language
        )

        print(
            "Language code:",
            language_code
        )

        # -------------------------------------------------
        # STEP 3
        # AI CATALOG GENERATION
        # -------------------------------------------------

        print(
            "\n3. Generating product catalog..."
        )

        catalog = voice_catalog(
            transcription,
            language
        )

        print(
            "Product catalog generated successfully."
        )

        # -------------------------------------------------
        # STEP 4
        # MARKET MATCHING
        # -------------------------------------------------

        print(
            "\n4. Running market matching..."
        )

        market_results = recommend_markets(
            catalog
        )

        print(
            "Market matching completed."
        )

        # -------------------------------------------------
        # COMPLETE RESULT
        # -------------------------------------------------

        return {

            "success":
                True,

            "language":
                language,

            "language_code":
                language_code,

            "transcription":
                transcription,

            "catalog":
                catalog,

            "recommended_markets":
                market_results
        }

    except HTTPException:

        raise

    except Exception as error:

        print(
            "\n========== VOICE CATALOG ERROR =========="
        )

        print(
            type(error).__name__
        )

        print(
            str(error)
        )

        traceback.print_exc()

        print(
            "=========================================="
        )

        raise HTTPException(
            status_code=500,
            detail={
                "error": type(error).__name__,
                "message": str(error)
            }
        )

    finally:

        # -------------------------------------------------
        # DELETE TEMP AUDIO
        # -------------------------------------------------

        if (
            temp_path
            and os.path.exists(temp_path)
        ):

            try:

                os.remove(
                    temp_path
                )

            except Exception:

                pass

from fastapi import Request

@app.post("/api/transcribe-voice-raw")
async def transcribe_voice_raw(request: Request):

    audio_bytes = await request.body()

    if not audio_bytes:
        raise HTTPException(status_code=400, detail="Audio data is empty.")

    filename = request.headers.get("x-filename", "audio.mp3")

    content_type = request.headers.get(
        "content-type",
        "audio/mpeg"
    )

    suffix = os.path.splitext(filename)[1] or ".mp3"

    temp_path = None

    try:
        with tempfile.NamedTemporaryFile(
            delete=False,
            suffix=suffix
        ) as temp_file:

            temp_file.write(audio_bytes)
            temp_path = temp_file.name

        result = transcribe_voice(temp_path)

        return {
            "filename": filename,
            "content_type": content_type,
            **result
        }

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

    finally:
        if temp_path and os.path.exists(temp_path):
            os.unlink(temp_path)
