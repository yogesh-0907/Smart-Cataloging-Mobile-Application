import json
import os
from typing import Dict, Any

from dotenv import load_dotenv
from google import genai
from google.genai import types

load_dotenv(".env")

client = genai.Client(
    api_key=os.getenv("GEMINI_API_KEY"),
    http_options=types.HttpOptions(
        client_args={"trust_env": False}
    )
)

DESCRIPTION_MODELS = [
    "gemini-3.6-flash",
    "gemini-3.5-flash",
    "gemini-3.5-flash-lite",
]


def generate_description(product: Dict[str, Any]) -> Dict[str, Any]:

    prompt = f"""
You are an expert e-commerce catalog writer helping marginalized artisans.

Create professional product content from the following AI-analyzed product information.

Product information:
Category: {product.get("category", "Unknown")}
Product Type: {product.get("product_type", "Unknown")}
Material: {", ".join(product.get("material", []))}
Style: {product.get("style", "Unknown")}
Color: {", ".join(product.get("color", []))}
Features: {", ".join(product.get("features", []))}
Keywords: {", ".join(product.get("keywords", []))}

Return ONLY JSON with exactly these fields:

{{
    "description_en": "Professional English product description",
    "description_hi": "Professional Hindi product description",
    "seo_keywords": [
        "keyword1",
        "keyword2",
        "keyword3",
        "keyword4",
        "keyword5"
    ]
}}

Rules:
- Write a professional e-commerce description.
- Do not invent facts.
- Mention craftsmanship naturally.
- English must be suitable for online marketplaces.
- Hindi must be natural and professional.
- Generate 5 to 10 useful SEO keywords.
- Keep descriptions concise and attractive.
"""

    last_error = None

    for model in DESCRIPTION_MODELS:

        try:
            print(f"Trying description model: {model}")

            response = client.models.generate_content(
                model=model,
                contents=prompt,
                config=types.GenerateContentConfig(
                    response_mime_type="application/json"
                )
            )

            result = response.text.strip()

            description = json.loads(result)

            print(f"Description generated successfully with: {model}")

            return description

        except Exception as error:
            print(f"Description model failed: {model}")
            print(error)
            last_error = error

    raise RuntimeError(
        f"All description models failed. Last error: {last_error}"
    )
