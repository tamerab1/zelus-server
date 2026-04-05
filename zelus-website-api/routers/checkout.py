"""
Checkout endpoints — create payment sessions with Stripe and PayPal.

Security contract:
  - Package price is ALWAYS looked up from store_catalog, never taken from the request.
  - A Transaction row is created BEFORE the player is redirected, so we can reconcile
    webhook callbacks even if the browser never returns to our success URL.
"""
import os
import logging

import httpx
import stripe
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

import models
from database import get_db
from store_catalog import get_item

log = logging.getLogger("zelus.checkout")

router = APIRouter(prefix="/api/checkout", tags=["checkout"])

# ── Stripe ─────────────────────────────────────────────────────────────────────
stripe.api_key = os.getenv("STRIPE_SECRET_KEY", "")

# ── PayPal ─────────────────────────────────────────────────────────────────────
PAYPAL_CLIENT_ID     = os.getenv("PAYPAL_CLIENT_ID", "")
PAYPAL_CLIENT_SECRET = os.getenv("PAYPAL_CLIENT_SECRET", "")
# Use the sandbox URL during development; switch to https://api-m.paypal.com for live.
PAYPAL_BASE_URL      = os.getenv("PAYPAL_BASE_URL", "https://api-m.sandbox.paypal.com")

# ── Shared ─────────────────────────────────────────────────────────────────────
SITE_URL = os.getenv("SITE_URL", "http://localhost:5173")


# ── Request schema ─────────────────────────────────────────────────────────────
class CheckoutRequest(BaseModel):
    username:   str = Field(..., min_length=1, max_length=12)
    package_id: str = Field(..., min_length=1, max_length=50)


# ── PayPal helper ──────────────────────────────────────────────────────────────
async def _paypal_access_token() -> str:
    """Exchange client_id/secret for a short-lived access token."""
    async with httpx.AsyncClient(timeout=10) as client:
        resp = await client.post(
            f"{PAYPAL_BASE_URL}/v1/oauth2/token",
            auth=(PAYPAL_CLIENT_ID, PAYPAL_CLIENT_SECRET),
            data={"grant_type": "client_credentials"},
        )
        resp.raise_for_status()
        return resp.json()["access_token"]


# ── Stripe checkout ────────────────────────────────────────────────────────────
@router.post("/stripe")
async def create_stripe_checkout(req: CheckoutRequest, db: Session = Depends(get_db)):
    log.info("[Stripe] Checkout — user='%s' pkg='%s'", req.username, req.package_id)

    if not stripe.api_key:
        raise HTTPException(503, "Stripe payments are not configured.")

    # ── Price validation (never trust the frontend) ────────────────────────────
    item = get_item(req.package_id)
    if not item:
        log.warning("[Stripe] Unknown package_id='%s' from user='%s'", req.package_id, req.username)
        raise HTTPException(400, f"Unknown package: '{req.package_id}'")

    # ── Create pending transaction row BEFORE contacting Stripe ───────────────
    txn = models.Transaction(
        username=req.username,
        package_id=item.slug,
        package_name=item.name,
        amount_usd=item.price_usd,
        provider=models.PaymentProvider.STRIPE,
        status=models.TransactionStatus.PENDING,
    )
    db.add(txn)
    db.flush()  # assign txn.id without committing

    # ── Create Stripe Checkout Session ────────────────────────────────────────
    try:
        session = stripe.checkout.Session.create(
            payment_method_types=["card"],
            line_items=[{
                "price_data": {
                    "currency": "usd",
                    "unit_amount": int(item.price_usd * 100),  # Stripe uses cents
                    "product_data": {
                        "name": f"Zelus — {item.name}",
                        "description": f"+{item.tokens:,} tokens · delivered to: {req.username}",
                    },
                },
                "quantity": 1,
            }],
            mode="payment",
            # {CHECKOUT_SESSION_ID} is a Stripe template literal — not Python formatting
            success_url=f"{SITE_URL}/?payment=success&session_id={{CHECKOUT_SESSION_ID}}",
            cancel_url=f"{SITE_URL}/?payment=cancelled",
            metadata={
                "username":       req.username,
                "package_id":     item.slug,
                "transaction_id": str(txn.id),
            },
            client_reference_id=str(txn.id),
        )
    except stripe.error.StripeError as exc:
        db.rollback()
        log.error("[Stripe] API error for user='%s': %s", req.username, exc)
        raise HTTPException(502, exc.user_message or "Stripe payment could not be initiated.")

    # ── Persist the Stripe session ID so the webhook can locate this txn ──────
    txn.provider_session_id = session.id
    db.commit()

    log.info("[Stripe] Session created: %s  txn_id=%d  user='%s'",
             session.id, txn.id, req.username)
    return {"checkout_url": session.url, "session_id": session.id}


# ── PayPal checkout ────────────────────────────────────────────────────────────
@router.post("/paypal")
async def create_paypal_checkout(req: CheckoutRequest, db: Session = Depends(get_db)):
    log.info("[PayPal] Checkout — user='%s' pkg='%s'", req.username, req.package_id)

    if not PAYPAL_CLIENT_ID or not PAYPAL_CLIENT_SECRET:
        raise HTTPException(503, "PayPal payments are not configured.")

    # ── Price validation ───────────────────────────────────────────────────────
    item = get_item(req.package_id)
    if not item:
        log.warning("[PayPal] Unknown package_id='%s' from user='%s'", req.package_id, req.username)
        raise HTTPException(400, f"Unknown package: '{req.package_id}'")

    # ── Create pending transaction row BEFORE contacting PayPal ───────────────
    txn = models.Transaction(
        username=req.username,
        package_id=item.slug,
        package_name=item.name,
        amount_usd=item.price_usd,
        provider=models.PaymentProvider.PAYPAL,
        status=models.TransactionStatus.PENDING,
    )
    db.add(txn)
    db.flush()

    # ── Create PayPal Order ────────────────────────────────────────────────────
    try:
        access_token = await _paypal_access_token()

        async with httpx.AsyncClient(timeout=10) as client:
            resp = await client.post(
                f"{PAYPAL_BASE_URL}/v2/checkout/orders",
                headers={
                    "Authorization":  f"Bearer {access_token}",
                    "Content-Type":   "application/json",
                    # Idempotency key — prevents duplicate orders on retry
                    "PayPal-Request-Id": f"zelus-txn-{txn.id}",
                },
                json={
                    "intent": "CAPTURE",
                    "purchase_units": [{
                        # custom_id is echoed back in capture webhooks for lookup
                        "custom_id":    str(txn.id),
                        "reference_id": str(txn.id),
                        "description":  (
                            f"Zelus — {item.name} "
                            f"(+{item.tokens:,} tokens for {req.username})"
                        ),
                        "amount": {
                            "currency_code": "USD",
                            "value": f"{item.price_usd:.2f}",
                        },
                    }],
                    "application_context": {
                        "brand_name":  "Zelus",
                        "user_action": "PAY_NOW",
                        "return_url":  f"{SITE_URL}/?payment=success",
                        "cancel_url":  f"{SITE_URL}/?payment=cancelled",
                    },
                },
            )
            resp.raise_for_status()
            order = resp.json()

    except httpx.HTTPStatusError as exc:
        db.rollback()
        log.error("[PayPal] HTTP %d for user='%s': %s",
                  exc.response.status_code, req.username, exc.response.text)
        raise HTTPException(502, "PayPal payment could not be initiated. Please try again.")
    except httpx.TimeoutException:
        db.rollback()
        log.error("[PayPal] Timeout creating order for user='%s'", req.username)
        raise HTTPException(504, "PayPal is taking too long to respond. Please try again.")
    except Exception as exc:
        db.rollback()
        log.error("[PayPal] Unexpected error for user='%s': %s", req.username, exc)
        raise HTTPException(502, "PayPal payment could not be initiated.")

    # ── Extract approval URL ───────────────────────────────────────────────────
    approval_url = next(
        (link["href"] for link in order.get("links", []) if link.get("rel") == "approve"),
        None,
    )
    if not approval_url:
        db.rollback()
        log.error("[PayPal] No approval URL in response for user='%s': %s", req.username, order)
        raise HTTPException(502, "PayPal did not return a payment URL.")

    txn.provider_session_id = order["id"]
    db.commit()

    log.info("[PayPal] Order created: %s  txn_id=%d  user='%s'",
             order["id"], txn.id, req.username)
    return {"checkout_url": approval_url, "order_id": order["id"]}
