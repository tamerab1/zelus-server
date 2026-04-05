"""
Server-side store catalog — the single source of truth for pricing.
Frontend prices are NEVER trusted; every checkout is validated against this file.
"""
from dataclasses import dataclass
from typing import Optional


@dataclass(frozen=True)
class StoreItem:
    slug: str
    name: str
    price_usd: float
    tokens: int


# ── Catalog ────────────────────────────────────────────────────────────────────
# slug keys MUST match the `slug` / `id` fields sent by the React frontend.
CATALOG: dict[str, StoreItem] = {
    # ── Donator Ranks ──────────────────────────────────────────────────────────
    "donator":         StoreItem("donator",         "Donator",           10.00,  100),
    "super_donator":   StoreItem("super_donator",   "Super Donator",     25.00,  275),
    "extreme_donator": StoreItem("extreme_donator", "Extreme Donator",   50.00,  600),
    "legendary":       StoreItem("legendary",       "Legendary",        100.00, 1250),
    "sponsor":         StoreItem("sponsor",         "Sponsor",          250.00, 3500),
    # ── Store Packs ────────────────────────────────────────────────────────────
    "starter":         StoreItem("starter",         "Starter Pack",       5.00,  500),
    "pvp":             StoreItem("pvp",             "PvP Pack",          15.00,  750),
    "skilling":        StoreItem("skilling",        "Skilling Pack",     20.00, 1000),
    "boss":            StoreItem("boss",            "Boss Pack",         30.00, 1500),
    "mystery":         StoreItem("mystery",         "Mystery Bundle",    10.00,  200),
    "ultimate":        StoreItem("ultimate",        "Ultimate Pack",     75.00, 5000),
}


def get_item(slug: str) -> Optional[StoreItem]:
    """Case-insensitive lookup by slug. Returns None if not found."""
    return CATALOG.get(slug.lower().strip())
