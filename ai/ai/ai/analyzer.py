import base64
import json
import os
from typing import Dict, Any

from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()

client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))


def analyze_product(image_bytes: bytes, content_type: str) -> Dict[str, Any]:
    """
    Analyze an artisan product image using OpenAI vision.
    """

    encoded_image = base64.b64encode(image_bytes).decode("utf-8")

    prompt = """
You are an AI cataloging assistant for marginalized artisans.

Analyze the uploaded artisan product image and return ONLY valid JSON.

Identify:
- category
- product_type
- material
- style
- color
- features
- keywords
- confidence

Rules:
- Do not invent information that cannot reasonably be seen.
- material, color, features and keywords must be JSON arrays.
- confidence must be a number between 0 and 1.
- Keep the values concise.
"""

    response = client.responses.create(
        model="gpt-5.5",
        input=[
            {
                "role": "user",
                "content": [
                    {
                        "type": "input_text",
                        "text": prompt
                    },
                    {
                        "type": "input_image",
                        "image_url": (
                            f"data:{content_type};base64,{encoded_image}"
                        )
                    }
                ]
            }
        ]
    )

    result = response.output_text.strip()

    # Remove markdown code fences if the model adds them
    if result.startswith("```"):
        result = result.replace("```json", "")
        result = result.replace("```", "")
        result = result.strip()

    return json.loads(result)