import os
import json
from typing import Dict, Any

from dotenv import load_dotenv
from google import genai
from google.genai import types


load_dotenv(".env")

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

client = None

if GEMINI_API_KEY:
    client = genai.Client(
        api_key=GEMINI_API_KEY,
        http_options=types.HttpOptions(
            client_args={"trust_env": False}
        )
    )


def calculate_production_cost(
    material_cost: float,
    labor_hours: float,
    labor_rate: float,
    packaging_cost: float = 0,
    overhead_percent: float = 10
) -> Dict[str, float]:

    labor_cost = labor_hours * labor_rate

    subtotal = (
        material_cost
        + labor_cost
        + packaging_cost
    )

    overhead_cost = (
        subtotal * overhead_percent / 100
    )

    total_cost = (
        subtotal
        + overhead_cost
    )

    return {
        "material_cost": round(material_cost, 2),
        "labor_cost": round(labor_cost, 2),
        "packaging_cost": round(packaging_cost, 2),
        "overhead_cost": round(overhead_cost, 2),
        "total_production_cost": round(total_cost, 2)
    }


def ai_price_recommendation(
    product: Dict[str, Any],
    production_cost: float,
    profit_margin_percent: float
) -> Dict[str, Any]:

    """
    Ask Gemini to estimate a market-oriented price adjustment.

    IMPORTANT:
    The AI does not replace the actual cost calculation.
    It only recommends a selling price based on product
    characteristics and the calculated production cost.
    """

    if client is None:
        return {
            "ai_available": False,
            "market_price_estimate": round(
                production_cost * 1.25,
                2
            ),
            "ai_recommended_price": round(
                production_cost * (
                    1 + profit_margin_percent / 100
                ),
                2
            ),
            "ai_minimum_price": round(
                production_cost * 1.10,
                2
            ),
            "ai_maximum_price": round(
                production_cost * 1.50,
                2
            ),
            "ai_confidence": 0.50,
            "ai_reason": "AI unavailable. Rule-based fallback used."
        }

    prompt = f"""
You are an AI pricing assistant for Indian marginalized artisans.

Calculate a realistic selling-price recommendation for this handmade
product.

PRODUCT INFORMATION:

Product type:
{product.get("product_type", "Unknown")}

Category:
{product.get("category", "Unknown")}

Material:
{product.get("material", [])}

Style:
{product.get("style", "Unknown")}

Color:
{product.get("color", [])}

Features:
{product.get("features", [])}

Quality:
{product.get("quality", "Standard")}

Production cost:
₹{production_cost}

Requested minimum profit margin:
{profit_margin_percent}%

PRICING RULES:

1. The production cost is the minimum economic base.
2. Consider craftsmanship and uniqueness.
3. Consider material quality.
4. Consider product category and positioning.
5. Consider whether the product is traditional, premium,
   eco-friendly or decorative.
6. Do not recommend a price below production cost.
7. Do not produce an extremely high or unrealistic price.
8. The artisan has the final decision.
9. This is an AI recommendation, not a guaranteed market price.

Return ONLY valid JSON:

{{
    "market_price_estimate": 0,
    "ai_recommended_price": 0,
    "ai_minimum_price": 0,
    "ai_maximum_price": 0,
    "ai_confidence": 0.0,
    "ai_reason": "short explanation"
}}
"""

    try:

        response = client.models.generate_content(
            model="gemini-3.6-flash",
            contents=prompt,
            config={
                "response_mime_type": "application/json"
            }
        )

        result = json.loads(response.text)

        # Validate and protect against bad AI values
        recommended = float(
            result.get(
                "ai_recommended_price",
                production_cost * 1.20
            )
        )

        minimum = float(
            result.get(
                "ai_minimum_price",
                production_cost * 1.10
            )
        )

        maximum = float(
            result.get(
                "ai_maximum_price",
                production_cost * 1.50
            )
        )

        market_estimate = float(
            result.get(
                "market_price_estimate",
                recommended
            )
        )

        confidence = float(
            result.get(
                "ai_confidence",
                0.70
            )
        )

        # Never allow AI to recommend below production cost
        minimum = max(
            minimum,
            production_cost
        )

        recommended = max(
            recommended,
            production_cost
        )

        maximum = max(
            maximum,
            recommended
        )

        return {
            "ai_available": True,
            "market_price_estimate": round(
                market_estimate,
                2
            ),
            "ai_recommended_price": round(
                recommended,
                2
            ),
            "ai_minimum_price": round(
                minimum,
                2
            ),
            "ai_maximum_price": round(
                maximum,
                2
            ),
            "ai_confidence": round(
                confidence,
                2
            ),
            "ai_reason": result.get(
                "ai_reason",
                "AI-assisted market pricing recommendation."
            )
        }

    except Exception as error:

        print(
            "AI pricing unavailable:",
            type(error).__name__,
            str(error)
        )

        # Safe fallback
        recommended = production_cost * (
            1 + profit_margin_percent / 100
        )

        return {
            "ai_available": False,
            "market_price_estimate": round(
                recommended,
                2
            ),
            "ai_recommended_price": round(
                recommended,
                2
            ),
            "ai_minimum_price": round(
                production_cost * 1.10,
                2
            ),
            "ai_maximum_price": round(
                production_cost * 1.50,
                2
            ),
            "ai_confidence": 0.50,
            "ai_reason": (
                "Gemini was unavailable. "
                "Rule-based pricing fallback used."
            )
        }


def recommend_price(
    product: Dict[str, Any],
    material_cost: float,
    labor_hours: float,
    labor_rate: float,
    packaging_cost: float = 0,
    overhead_percent: float = 10,
    profit_margin_percent: float = 20
) -> Dict[str, Any]:

    # -----------------------------------------
    # STEP 1: Actual production cost
    # -----------------------------------------

    costs = calculate_production_cost(
        material_cost=material_cost,
        labor_hours=labor_hours,
        labor_rate=labor_rate,
        packaging_cost=packaging_cost,
        overhead_percent=overhead_percent
    )

    production_cost = costs[
        "total_production_cost"
    ]

    # -----------------------------------------
    # STEP 2: Normal rule-based price
    # -----------------------------------------

    normal_price = production_cost * (
        1 + profit_margin_percent / 100
    )

    # -----------------------------------------
    # STEP 3: AI market recommendation
    # -----------------------------------------

    ai_result = ai_price_recommendation(
        product=product,
        production_cost=production_cost,
        profit_margin_percent=profit_margin_percent
    )

    # -----------------------------------------
    # STEP 4: Final result
    # -----------------------------------------

    return {
        "product": {
            "product_type": product.get(
                "product_type",
                "Unknown"
            ),
            "category": product.get(
                "category",
                "Unknown"
            ),
            "material": product.get(
                "material",
                []
            ),
            "style": product.get(
                "style",
                "Unknown"
            )
        },

        "cost_breakdown": costs,

        "rule_based_price": round(
            normal_price,
            2
        ),

        "ai_pricing": ai_result,

        "currency": "INR",

        "pricing_method": (
            "Production cost + AI-assisted "
            "market pricing"
        ),

        "artisan_final_decision": True,

        "message": (
            "AI provides a recommended price. "
            "The artisan has the final decision "
            "on the selling price."
        )
    }
