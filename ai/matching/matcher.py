import json
from pathlib import Path
from typing import Dict, Any, List


MARKET_FILE = Path(__file__).parent.parent / "data" / "markets.json"


def load_markets() -> List[Dict[str, Any]]:
    with open(MARKET_FILE, "r", encoding="utf-8") as file:
        return json.load(file)


def calculate_match(
    product: Dict[str, Any],
    market: Dict[str, Any]
) -> int:

    score = 0

    category = product.get("category", "")
    material_list = product.get("material", [])
    style = product.get("style", "")

    market_categories = market.get("categories", [])
    market_materials = market.get("materials", [])
    market_styles = market.get("styles", [])

    # Category match
    if category in market_categories:
        score += 40

    # Material match
    if set(material_list) & set(market_materials):
        score += 35

    # Style match
    if style in market_styles:
        score += 25

    return score


def recommend_markets(
    product: Dict[str, Any]
) -> List[Dict[str, Any]]:

    markets = load_markets()

    results = []

    for market in markets:

        score = calculate_match(
            product,
            market
        )

        results.append({
            "market": market["name"],
            "match_score": score
        })

    results.sort(
        key=lambda item: item["match_score"],
        reverse=True
    )

    return results