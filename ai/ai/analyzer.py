import json
import os
from typing import Dict, Any

from dotenv import load_dotenv
from google import genai
from google.genai import types
from google.genai import errors

load_dotenv()

client = genai.Client(
    api_key=os.getenv("GEMINI_API_KEY"),
    http_options=types.HttpOptions(
        client_args={"trust_env": False}
    )
)

MODELS = [
    "gemini-3.5-flash",
    "gemini-3.5-flash",
    "gemini-3.7-flash"
]


def analyze_product(
    image_bytes: bytes,
    content_type: str
) -> Dict[str, Any]:

    prompt = """
You are an AI cataloging assistant for marginalized artisans.

Analyze the uploaded product image carefully.

Identify the actual product shown in the image.

Return ONLY JSON with exactly these fields:

{
    "category": "string",
    "product_type": "string",
    "material": ["string"],
    "style": "string",
    "color": ["string"],
    "features": ["string"],
    "keywords": ["string"],
    "confidence": 0.0
}

Rules:
- Identify the actual product visible in the image.
- Do NOT assume every product is a bamboo basket.
- Do not invent information that cannot reasonably be seen.
- material, color, features and keywords must be arrays.
- confidence must be between 0 and 1.
- If something cannot be determined, use "Unknown".
- Keep values concise.
"""

    last_error = None

    for model in MODELS:
        try:
            print(f"Trying Gemini model: {model}")

            response = client.models.generate_content(
                model=model,
                contents=[
                    types.Part.from_text(text=prompt),
                    types.Part.from_bytes(
                        data=image_bytes,
                        mime_type=content_type
                    )
                ],
                config=types.GenerateContentConfig(
                    response_mime_type="application/json"
                )
            )

            result = response.text.strip()

            print(f"Success with model: {model}")

            return json.loads(result)

        except errors.ServerError as error:
            print(f"{model} unavailable: {error}")
            last_error = error
            continue

    raise last_error
