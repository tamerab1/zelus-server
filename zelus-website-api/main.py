import hashlib
import logging
import math
import os
import json
import secrets
import smtplib
from dotenv import load_dotenv
load_dotenv()
from datetime import datetime, timedelta, timezone
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from pathlib import Path
from typing import Optional

import bcrypt
import requests as _requests
import stripe as _stripe
from fastapi import FastAPI, Depends, HTTPException, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse, JSONResponse
from pydantic import BaseModel, EmailStr, Field
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from slowapi.util import get_remote_address
from sqlalchemy.exc import OperationalError
from sqlalchemy.orm import Session

import models
from database import engine, get_db
from game_database import get_game_db, GameUser, GameUserSkillStat
from routers import checkout as checkout_router_module
from routers import webhooks as webhooks_router_module

# ── Logging ───────────────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)-8s %(name)s — %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger("zelus.main")

# ── Environment config ────────────────────────────────────────────────────────
_TURNSTILE_SECRET   = os.getenv("TURNSTILE_SECRET_KEY", "")
_SITE_URL           = os.getenv("SITE_URL", "http://localhost:5173")
_SMTP_HOST          = os.getenv("SMTP_HOST", "smtp.gmail.com")
_SMTP_PORT          = int(os.getenv("SMTP_PORT", "587"))
_SMTP_USER          = os.getenv("SMTP_USER", "")
_SMTP_PASS          = os.getenv("SMTP_PASS", "")
_CORS_ORIGINS_RAW   = os.getenv(
    "CORS_ORIGINS",
    "http://localhost:5173,http://127.0.0.1:5173,http://localhost:5174,http://127.0.0.1:5174"
)
_CORS_ORIGINS = [o.strip() for o in _CORS_ORIGINS_RAW.split(",") if o.strip()]

# ── Cloudflare Turnstile verification ─────────────────────────────────────────
def _verify_turnstile(token: str, client_ip: str = "") -> bool:
    if not _TURNSTILE_SECRET:
        return True
    try:
        resp = _requests.post(
            "https://challenges.cloudflare.com/turnstile/v0/siteverify",
            data={"secret": _TURNSTILE_SECRET, "response": token, "remoteip": client_ip},
            timeout=5,
        )
        return resp.json().get("success", False)
    except Exception:
        return False

# ── Token helpers ─────────────────────────────────────────────────────────────

def _generate_token() -> tuple[str, str]:
    """
    Returns (raw_token, token_hash).
    raw_token  → sent to the user in the email link, never stored.
    token_hash → SHA-256 hex digest stored in auth_tokens.token_hash.
    """
    raw    = secrets.token_urlsafe(32)
    hashed = hashlib.sha256(raw.encode()).hexdigest()
    return raw, hashed


def _create_auth_token(db: Session, user_id: int, purpose: models.TokenPurpose,
                       expires_hours: int) -> str:
    """
    Deletes any existing token for this user+purpose, creates a new one,
    and returns the raw token to embed in the email link.
    """
    db.query(models.AuthToken).filter(
        models.AuthToken.user_id == user_id,
        models.AuthToken.purpose == purpose,
    ).delete()

    raw, hashed = _generate_token()
    token = models.AuthToken(
        user_id    = user_id,
        purpose    = purpose,
        token_hash = hashed,
        expires_at = datetime.now(timezone.utc) + timedelta(hours=expires_hours),
    )
    db.add(token)
    db.flush()
    return raw


def _consume_auth_token(db: Session, raw_token: str,
                        purpose: models.TokenPurpose) -> models.AuthToken | None:
    """
    Looks up the token by hash, verifies it is unused and not expired,
    marks it used, and returns the AuthToken row. Returns None if invalid.
    Uses SELECT FOR UPDATE to prevent concurrent reuse.
    """
    hashed = hashlib.sha256(raw_token.encode()).hexdigest()
    now    = datetime.now(timezone.utc)
    token  = (
        db.query(models.AuthToken)
        .filter(
            models.AuthToken.token_hash == hashed,
            models.AuthToken.purpose    == purpose,
            models.AuthToken.used_at    == None,
            models.AuthToken.expires_at >  now,
        )
        .with_for_update()
        .first()
    )
    if not token:
        return None
    token.used_at = now
    return token


# ── Email sending ─────────────────────────────────────────────────────────────

def _send_email(to_email: str, subject: str, html: str) -> None:
    """
    Sends an HTML email. If SMTP is not configured, logs a WARNING and
    prints the full content to the console so local dev still works.
    """
    if not _SMTP_USER or not _SMTP_PASS:
        log.warning(
            "SMTP is not configured (SMTP_USER/SMTP_PASS empty). "
            "Email NOT sent to %s — printing to console instead.", to_email
        )
        print(f"\n{'='*60}\nDEV EMAIL → {to_email}\nSubject: {subject}\n{html}\n{'='*60}\n")
        return
    msg = MIMEMultipart("alternative")
    msg["Subject"] = subject
    msg["From"]    = _SMTP_USER
    msg["To"]      = to_email
    msg.attach(MIMEText(html, "html"))
    try:
        with smtplib.SMTP(_SMTP_HOST, _SMTP_PORT) as s:
            s.starttls()
            s.login(_SMTP_USER, _SMTP_PASS)
            s.sendmail(_SMTP_USER, to_email, msg.as_string())
        log.info("Email sent to %s — subject: %s", to_email, subject)
    except Exception as e:
        log.error("Failed to send email to %s: %s", to_email, e)


def _send_verification_email(to_email: str, username: str, raw_token: str) -> None:
    url  = f"{_SITE_URL}/verify-email?token={raw_token}"
    html = f"""
    <p>Hi <b>{username}</b>,</p>
    <p>Click below to verify your Zelus account:</p>
    <p><a href="{url}">{url}</a></p>
    <p>This link expires in <b>24 hours</b>.</p>
    """
    _send_email(to_email, "Verify your Zelus account", html)


def _send_password_reset_email(to_email: str, username: str, raw_token: str) -> None:
    url  = f"{_SITE_URL}/reset-password?token={raw_token}"
    html = f"""
    <p>Hi <b>{username}</b>,</p>
    <p>Click below to reset your Zelus password:</p>
    <p><a href="{url}">{url}</a></p>
    <p>This link expires in <b>1 hour</b>.</p>
    <p>If you did not request this, you can safely ignore this email.</p>
    """
    _send_email(to_email, "Reset your Zelus password", html)


# ── Game username validator ───────────────────────────────────────────────────
def _game_username_exists(username: str) -> bool:
    norm = username.strip().lower()
    try:
        gdb = next(get_game_db())
        from sqlalchemy import func as sa_func
        exists = gdb.query(GameUser).filter(
            sa_func.lower(GameUser.username) == norm
        ).first()
        if exists:
            return True
    except Exception:
        pass
    if _CHARACTERS_DIR.exists():
        for path in _CHARACTERS_DIR.glob("*.json"):
            try:
                data = json.loads(path.read_text(encoding="utf-8"))
                info = data.get("playerInformation", {})
                dn   = (info.get("displayname") or info.get("username") or path.stem or "").lower()
                if dn == norm:
                    return True
            except Exception:
                continue
    return False

# ── Character file reader ─────────────────────────────────────────────────────
_SKILL_ORDER = [
    'attack_xp', 'defence_xp', 'strength_xp', 'hitpoints_xp', 'ranged_xp',
    'prayer_xp', 'magic_xp', 'cooking_xp', 'woodcutting_xp', 'fletching_xp',
    'fishing_xp', 'firemaking_xp', 'crafting_xp', 'smithing_xp', 'mining_xp',
    'herblore_xp', 'agility_xp', 'thieving_xp', 'slayer_xp', 'farming_xp',
    'runecrafting_xp', 'hunter_xp', 'construction_xp',
]

_CHARACTERS_DIR = Path(os.getenv("CHARACTERS_DIR") or "") or (
    Path(__file__).parent.parent / "DMMPVP" / "data" / "characters"
)

def _xp_to_level(xp: float) -> int:
    if xp <= 0:
        return 1
    total = 0
    for lvl in range(1, 99):
        total += math.floor(lvl + 300 * (2 ** (lvl / 7)))
        if math.floor(total / 4) > xp:
            return lvl
    return 99

def _count_online_players() -> int | None:
    if not _CHARACTERS_DIR.exists():
        return None
    count = 0
    for path in _CHARACTERS_DIR.glob("*.json"):
        try:
            data = json.loads(path.read_text(encoding='utf-8'))
            sessions = data.get('loyaltyManager', {}).get('sessions', [])
            if sessions:
                last = sessions[-1]
                if 'login' in last and 'logout' not in last:
                    count += 1
        except Exception:
            continue
    return count


def _read_character_files(limit: int = 200) -> list[dict] | None:
    if not _CHARACTERS_DIR.exists():
        return None
    rows = []
    for path in _CHARACTERS_DIR.glob("*.json"):
        try:
            data = json.loads(path.read_text(encoding='utf-8'))
            info       = data.get('playerInformation', {})
            skills     = data.get('skills', {})
            xp_arr     = skills.get('experience', [])
            attrs = data.get('attributes', {})
            if not xp_arr:
                continue
            username    = info.get('displayname') or info.get('username') or path.stem
            skill_xp    = {_SKILL_ORDER[i]: int(xp_arr[i]) for i in range(min(len(xp_arr), 23))}
            total_xp    = sum(skill_xp.values())
            total_level = sum(_xp_to_level(v) for v in skill_xp.values())
            kills       = int(attrs.get('pvp-kills', 0))
            deaths      = int(attrs.get('pvp-deaths', 0))
            killstreak  = int(attrs.get('current-killstreak', 0))
            kdr         = round(kills / deaths, 2) if deaths > 0 else float(kills)
            rows.append({
                "username": username, "total_level": total_level,
                "total_experience": total_xp,
                "kills": kills, "deaths": deaths,
                "kdr": kdr, "killstreak": killstreak,
                **skill_xp,
            })
        except Exception:
            continue
    return rows

# ── DB migrations (run on every startup — all idempotent) ─────────────────────
models.Base.metadata.create_all(bind=engine)

_SKILL_COLUMNS = [
    "cooking_xp", "woodcutting_xp", "fletching_xp", "fishing_xp",
    "firemaking_xp", "crafting_xp", "smithing_xp", "mining_xp",
    "herblore_xp", "agility_xp", "thieving_xp", "slayer_xp",
    "farming_xp", "runecrafting_xp", "hunter_xp", "construction_xp",
]
with engine.connect() as _conn:
    for _col in _SKILL_COLUMNS:
        try:
            _conn.execute(
                __import__("sqlalchemy").text(
                    f"ALTER TABLE user_skill_stats ADD COLUMN IF NOT EXISTS {_col} INTEGER DEFAULT 0"
                )
            )
        except Exception:
            pass
    _new_user_cols = [
        "ALTER TABLE users ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT FALSE",
        "ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_token VARCHAR(255)",
        "ALTER TABLE votes ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45)",
        "ALTER TABLE votes ADD COLUMN IF NOT EXISTS game_username VARCHAR(12)",
    ]
    for _stmt in _new_user_cols:
        try:
            _conn.execute(__import__("sqlalchemy").text(_stmt))
        except Exception:
            pass
    # pending_claims unique constraint
    try:
        _conn.execute(__import__("sqlalchemy").text(
            "ALTER TABLE pending_claims ADD CONSTRAINT uq_pending_claims_transaction_id "
            "UNIQUE (transaction_id)"
        ))
    except Exception:
        pass
    # auth_tokens table
    try:
        _conn.execute(__import__("sqlalchemy").text("""
            CREATE TABLE IF NOT EXISTS auth_tokens (
                id         SERIAL PRIMARY KEY,
                user_id    INTEGER NOT NULL REFERENCES users(id),
                purpose    VARCHAR(30) NOT NULL,
                token_hash VARCHAR(64) NOT NULL UNIQUE,
                expires_at TIMESTAMP NOT NULL,
                created_at TIMESTAMP DEFAULT NOW(),
                used_at    TIMESTAMP
            )
        """))
        _conn.execute(__import__("sqlalchemy").text(
            "CREATE INDEX IF NOT EXISTS ix_auth_tokens_user_id ON auth_tokens(user_id)"
        ))
        _conn.execute(__import__("sqlalchemy").text(
            "CREATE INDEX IF NOT EXISTS ix_auth_tokens_token_hash ON auth_tokens(token_hash)"
        ))
    except Exception:
        pass
    _conn.commit()

# ── App setup ─────────────────────────────────────────────────────────────────
limiter = Limiter(key_func=get_remote_address)
app = FastAPI(title="Zelus Website API", description="API for Zelus RSPS")
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

app.add_middleware(
    CORSMiddleware,
    allow_origins=_CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["Content-Type", "Authorization"],
)

app.include_router(checkout_router_module.router)
app.include_router(webhooks_router_module.router)

@app.exception_handler(_stripe.error.StripeError)
async def stripe_error_handler(request: Request, exc: _stripe.error.StripeError):
    log.error("Unhandled Stripe error: %s", exc)
    return JSONResponse(
        status_code=502,
        content={"detail": exc.user_message or "A payment processing error occurred."},
    )

@app.exception_handler(Exception)
async def generic_error_handler(request: Request, exc: Exception):
    if isinstance(exc, HTTPException):
        raise exc
    log.error("Unhandled exception on %s %s: %s", request.method, request.url.path, exc,
              exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"detail": "An unexpected server error occurred. Please try again."},
    )

# ── Pydantic schemas ──────────────────────────────────────────────────────────
class UserRegister(BaseModel):
    username: str = Field(..., min_length=1, max_length=12)
    email: EmailStr
    password: str = Field(..., min_length=6)
    turnstile_token: str = Field(default="")

class UserLogin(BaseModel):
    username: str
    password: str

class CheckoutRequest(BaseModel):
    username: str
    package_name: str
    usd_amount: float
    tokens_to_give: int

class VoteSubmitRequest(BaseModel):
    user_id: int
    site_name: str
    game_username: str = Field(..., min_length=1, max_length=12)

class ForgotPasswordRequest(BaseModel):
    email: EmailStr

class ResetPasswordRequest(BaseModel):
    token: str
    new_password: str = Field(..., min_length=6)

class ResendVerificationRequest(BaseModel):
    email: EmailStr

# ── Vote cooldown ─────────────────────────────────────────────────────────────
VOTE_COOLDOWN_HOURS = 12

def _get_vote_state(vote: Optional[models.Vote]) -> dict:
    if not vote:
        return {"state": "idle", "seconds_remaining": None, "vote_id": None}
    now = datetime.now(timezone.utc)
    created = vote.created_at
    if created.tzinfo is None:
        created = created.replace(tzinfo=timezone.utc)
    elapsed   = (now - created).total_seconds()
    remaining = int(VOTE_COOLDOWN_HOURS * 3600 - elapsed)
    if remaining > 0:
        if vote.status == "pending":
            return {"state": "pending",  "seconds_remaining": remaining, "vote_id": vote.id}
        else:
            return {"state": "cooldown", "seconds_remaining": remaining, "vote_id": None}
    else:
        return {"state": "idle", "seconds_remaining": None, "vote_id": None}

# ── Live feed ─────────────────────────────────────────────────────────────────
import random
_LIVEFEED_FILE = _CHARACTERS_DIR / "livefeed.json"

def _generate_synthetic_feed(rows: list[dict], limit: int) -> list[dict]:
    events = []
    now = datetime.now(timezone.utc)
    killers = sorted(rows, key=lambda r: r.get('kills', 0), reverse=True)
    victims = sorted(rows, key=lambda r: r.get('deaths', 0), reverse=True)
    for i, k in enumerate(killers[:5]):
        if k.get('kills', 0) == 0:
            break
        victim = victims[i % len(victims)] if victims else {'username': 'an adventurer'}
        if victim['username'] == k['username']:
            continue
        seed = int(hashlib.md5(f"{k['username']}{i}".encode()).hexdigest(), 16) % 3600
        ts = (now - timedelta(seconds=seed)).isoformat()
        events.append({
            "id": f"kill_{k['username']}_{i}",
            "type": "pvp_kill",
            "timestamp": ts,
            "message": f"{k['username']} slew {victim['username']} in the Wilderness.",
        })
    streakers = sorted(rows, key=lambda r: r.get('killstreak', 0), reverse=True)
    for i, p in enumerate(streakers[:3]):
        ks = p.get('killstreak', 0)
        if ks < 1:
            break
        seed = int(hashlib.md5(f"ks_{p['username']}".encode()).hexdigest(), 16) % 1800
        ts = (now - timedelta(seconds=seed)).isoformat()
        events.append({
            "id": f"streak_{p['username']}",
            "type": "killstreak",
            "timestamp": ts,
            "message": f"{p['username']} is on a {ks} killstreak in the Wilderness!",
        })
    levelers = sorted(rows, key=lambda r: r.get('total_level', 0), reverse=True)
    for i, p in enumerate(levelers[:3]):
        lvl = p.get('total_level', 0)
        if lvl < 24:
            break
        seed = int(hashlib.md5(f"lvl_{p['username']}".encode()).hexdigest(), 16) % 7200
        ts = (now - timedelta(seconds=seed)).isoformat()
        events.append({
            "id": f"lvl_{p['username']}",
            "type": "level_up",
            "timestamp": ts,
            "message": f"{p['username']} reached total level {lvl}.",
        })
    events.sort(key=lambda e: e['timestamp'], reverse=True)
    return events[:limit]

# ── API endpoints ─────────────────────────────────────────────────────────────

@app.get("/")
def read_root():
    return {"status": "Zelus API is running!"}

@app.get("/livefeed")
def livefeed(limit: int = 12):
    if _LIVEFEED_FILE.exists():
        try:
            data = json.loads(_LIVEFEED_FILE.read_text(encoding='utf-8'))
            events = data if isinstance(data, list) else data.get('events', [])
            if events:
                events.sort(key=lambda e: e.get('timestamp', ''), reverse=True)
                return {"events": events[:limit], "source": "server"}
        except Exception:
            pass
    rows = _read_character_files(200) or []
    if not rows:
        return {"events": [], "source": "empty"}
    return {"events": _generate_synthetic_feed(rows, limit), "source": "synthetic"}

@app.get("/debug/hiscores")
def debug_hiscores(db: Session = Depends(get_db)):
    game_status = "ok"
    game_rows = 0
    try:
        gdb = next(get_game_db())
        game_rows = gdb.query(GameUserSkillStat).count()
    except Exception as e:
        game_status = str(e)
    local_status = "ok"
    local_rows = 0
    local_users = 0
    local_skill_rows = 0
    try:
        local_users = db.query(models.User).count()
        local_skill_rows = db.query(models.UserSkillStat).count()
        local_rows = (
            db.query(models.User.username, models.UserSkillStat)
            .join(models.UserSkillStat, models.User.id == models.UserSkillStat.user_id)
            .count()
        )
    except Exception as e:
        local_status = str(e)
    return {
        "game_db":   {"status": game_status, "skill_stat_rows": game_rows},
        "local_db":  {"status": local_status, "users": local_users, "skill_stats": local_skill_rows, "join_rows": local_rows},
    }

def _build_hiscore_row(username, stat):
    return {
        "username":         username,
        "total_level":      stat.total_level      or 0,
        "total_experience": stat.total_experience or 0,
        "attack_xp":        stat.attack_xp        or 0,
        "defence_xp":       stat.defence_xp       or 0,
        "strength_xp":      stat.strength_xp      or 0,
        "hitpoints_xp":     stat.hitpoints_xp     or 0,
        "ranged_xp":        stat.ranged_xp        or 0,
        "prayer_xp":        stat.prayer_xp        or 0,
        "magic_xp":         stat.magic_xp         or 0,
        "cooking_xp":       stat.cooking_xp       or 0,
        "woodcutting_xp":   stat.woodcutting_xp   or 0,
        "fletching_xp":     stat.fletching_xp     or 0,
        "fishing_xp":       stat.fishing_xp       or 0,
        "firemaking_xp":    stat.firemaking_xp    or 0,
        "crafting_xp":      stat.crafting_xp      or 0,
        "smithing_xp":      stat.smithing_xp      or 0,
        "mining_xp":        stat.mining_xp        or 0,
        "herblore_xp":      stat.herblore_xp      or 0,
        "agility_xp":       stat.agility_xp       or 0,
        "thieving_xp":      stat.thieving_xp      or 0,
        "slayer_xp":        stat.slayer_xp        or 0,
        "farming_xp":       stat.farming_xp       or 0,
        "runecrafting_xp":  stat.runecrafting_xp  or 0,
        "hunter_xp":        stat.hunter_xp        or 0,
        "construction_xp":  stat.construction_xp  or 0,
    }

@app.get("/players/online")
def get_online_players():
    count = _count_online_players()
    if count is not None:
        return {"online": count}
    try:
        game_db = next(get_game_db())
        from sqlalchemy import text as sa_text
        result = game_db.execute(
            sa_text("SELECT COUNT(*) FROM user_logins WHERE logged_out IS NULL")
        ).scalar()
        return {"online": result or 0}
    except Exception:
        pass
    return {"online": 0}


@app.get("/hiscores")
def get_hiscores(limit: int = 50, sort: str = "total_level"):
    try:
        game_db = next(get_game_db())
        rows = (
            game_db.query(GameUser.username, GameUserSkillStat)
            .join(GameUserSkillStat, GameUser.id == GameUserSkillStat.account)
            .order_by(GameUserSkillStat.total_level.desc())
            .limit(limit)
            .all()
        )
        if rows:
            return [_build_hiscore_row(u, s) for u, s in rows]
    except Exception:
        pass
    all_rows = _read_character_files()
    if all_rows is not None:
        valid_sort = sort if sort in (
            'total_level', 'total_experience', 'kills', 'deaths', 'kdr', 'killstreak',
            *_SKILL_ORDER
        ) else 'total_level'
        all_rows.sort(key=lambda r: r.get(valid_sort, 0), reverse=True)
        return all_rows[:limit]
    try:
        local_db = next(get_db())
        rows = (
            local_db.query(models.User.username, models.UserSkillStat)
            .join(models.UserSkillStat, models.User.id == models.UserSkillStat.user_id)
            .order_by(models.UserSkillStat.total_level.desc())
            .limit(limit)
            .all()
        )
        return [_build_hiscore_row(u, s) for u, s in rows]
    except Exception:
        raise HTTPException(status_code=503, detail="Could not reach the game database.")


# ── Auth endpoints ────────────────────────────────────────────────────────────

@app.post("/register", status_code=status.HTTP_201_CREATED)
@limiter.limit("5/minute")
def register_user(request: Request, user: UserRegister, db: Session = Depends(get_db)):
    client_ip = get_remote_address(request)
    if not _verify_turnstile(user.turnstile_token, client_ip):
        raise HTTPException(status_code=400, detail="CAPTCHA verification failed. Please try again.")

    existing_user = db.query(models.User).filter(
        (models.User.username == user.username) | (models.User.email == user.email)
    ).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="Username or email already taken.")

    hashed_password = bcrypt.hashpw(user.password.encode("utf-8"), bcrypt.gensalt(rounds=12)).decode("utf-8")

    new_user = models.User(
        username=user.username,
        email=user.email,
        password=hashed_password,
        is_verified=False,
    )
    db.add(new_user)
    db.flush()

    skill_stat = models.UserSkillStat(user_id=new_user.id)
    db.add(skill_stat)

    raw_token = _create_auth_token(db, new_user.id, models.TokenPurpose.EMAIL_VERIFICATION, expires_hours=24)
    db.commit()

    _send_verification_email(user.email, user.username, raw_token)

    return {"message": "Account created! Please check your email to verify your account before logging in."}


@app.get("/verify-email")
def verify_email(token: str, db: Session = Depends(get_db)):
    """Called when the player clicks the verification link in their email."""
    auth_token = _consume_auth_token(db, token, models.TokenPurpose.EMAIL_VERIFICATION)
    if not auth_token:
        raise HTTPException(status_code=400, detail="Invalid or expired verification link.")

    user = db.query(models.User).filter(models.User.id == auth_token.user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found.")

    user.is_verified        = True
    user.verification_token = None  # clear legacy field
    db.commit()

    return {"message": f"Email verified! Welcome to Zelus, {user.username}. You can now log in."}


@app.post("/resend-verification")
@limiter.limit("3/hour")
def resend_verification(request: Request, req: ResendVerificationRequest, db: Session = Depends(get_db)):
    """
    Resends the verification email. Rate limited to 3/hour per IP to prevent abuse.
    Always returns the same message whether or not the email exists (prevents enumeration).
    """
    user = db.query(models.User).filter(models.User.email == req.email).first()
    if user and not user.is_verified:
        raw_token = _create_auth_token(db, user.id, models.TokenPurpose.EMAIL_VERIFICATION, expires_hours=24)
        db.commit()
        _send_verification_email(user.email, user.username, raw_token)

    return {"message": "If that email is registered and unverified, a new link has been sent."}


@app.post("/forgot-password")
@limiter.limit("3/hour")
def forgot_password(request: Request, req: ForgotPasswordRequest, db: Session = Depends(get_db)):
    """
    Sends a password reset email. Rate limited to 3/hour per IP.
    Always returns the same message whether or not the email exists (prevents enumeration).
    """
    user = db.query(models.User).filter(models.User.email == req.email).first()
    if user:
        raw_token = _create_auth_token(db, user.id, models.TokenPurpose.PASSWORD_RESET, expires_hours=1)
        db.commit()
        _send_password_reset_email(user.email, user.username, raw_token)

    return {"message": "If that email is registered, a password reset link has been sent."}


@app.post("/reset-password")
@limiter.limit("5/hour")
def reset_password(request: Request, req: ResetPasswordRequest, db: Session = Depends(get_db)):
    """
    Consumes a reset token and sets the new password.
    Token is one-time use — reuse after consumption returns 400.
    """
    auth_token = _consume_auth_token(db, req.token, models.TokenPurpose.PASSWORD_RESET)
    if not auth_token:
        raise HTTPException(status_code=400, detail="Invalid or expired reset link. Please request a new one.")

    user = db.query(models.User).filter(models.User.id == auth_token.user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found.")

    user.password = bcrypt.hashpw(req.new_password.encode("utf-8"), bcrypt.gensalt(rounds=12)).decode("utf-8")
    db.commit()

    return {"message": "Password updated successfully. You can now log in."}


@app.post("/login")
@limiter.limit("10/minute")
def login_user(request: Request, user: UserLogin, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.username == user.username).first()

    if not db_user:
        raise HTTPException(status_code=400, detail="Invalid username or password.")

    if not bcrypt.checkpw(user.password.encode("utf-8"), db_user.password.encode("utf-8")):
        raise HTTPException(status_code=400, detail="Invalid username or password.")

    if not db_user.is_verified:
        raise HTTPException(
            status_code=403,
            detail="Please verify your email address before logging in. Check your inbox."
        )

    return {
        "message": "Login successful",
        "user": {
            "id": db_user.id,
            "username": db_user.username,
            "email": db_user.email,
            "privilege": db_user.privilege.value,
            "tokens": db_user.tokens,
            "total_spent": db_user.total_spent
        }
    }


# ── Store (legacy) ────────────────────────────────────────────────────────────
@app.post("/store/checkout")
def store_checkout(req: CheckoutRequest, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.username == req.username).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found.")
    new_donation = models.Donation(
        user_id=user.id,
        package_name=req.package_name,
        usd_amount=req.usd_amount,
        tokens_to_give=req.tokens_to_give,
        status="pending"
    )
    db.add(new_donation)
    db.commit()
    return {"message": "Order placed! Type ::claimdonate in-game to receive your items."}


# ── Game username validation ───────────────────────────────────────────────────
@app.get("/game/check-username/{username}")
@limiter.limit("20/minute")
def check_game_username(request: Request, username: str):
    if not username or len(username) > 12:
        raise HTTPException(status_code=400, detail="Invalid username.")
    return {"exists": _game_username_exists(username)}


# ── Vote endpoints ─────────────────────────────────────────────────────────────
VALID_SITES = {"RUNELOCUS", "RSPS_LIST"}
VOTE_POINTS_BY_SITE = {"RUNELOCUS": 2, "RSPS_LIST": 2}

@app.post("/vote/submit", status_code=status.HTTP_201_CREATED)
@limiter.limit("10/minute")
def submit_vote(request: Request, req: VoteSubmitRequest, db: Session = Depends(get_db)):
    try:
        if req.site_name not in VALID_SITES:
            raise HTTPException(status_code=400, detail=f"Unknown voting site: {req.site_name}")

        user = db.query(models.User).filter(models.User.id == req.user_id).first()
        if not user:
            raise HTTPException(status_code=404, detail="User not found.")

        if not _game_username_exists(req.game_username):
            raise HTTPException(
                status_code=400,
                detail=f"The character '{req.game_username}' does not exist in-game. "
                       "Please enter your exact in-game character name."
            )

        client_ip = get_remote_address(request)
        cutoff    = datetime.utcnow() - timedelta(hours=VOTE_COOLDOWN_HOURS)

        recent_by_user = (
            db.query(models.Vote)
            .filter(
                models.Vote.user_id   == req.user_id,
                models.Vote.site_name == req.site_name,
                models.Vote.created_at >= cutoff,
            )
            .order_by(models.Vote.created_at.desc())
            .first()
        )
        if recent_by_user:
            elapsed   = (datetime.utcnow() - recent_by_user.created_at).total_seconds()
            remaining = int(VOTE_COOLDOWN_HOURS * 3600 - elapsed)
            raise HTTPException(
                status_code=429,
                detail=f"You already voted on {req.site_name}. "
                       f"Cooldown: {remaining} seconds remaining."
            )

        if client_ip:
            recent_by_ip = (
                db.query(models.Vote)
                .filter(
                    models.Vote.ip_address == client_ip,
                    models.Vote.site_name  == req.site_name,
                    models.Vote.created_at >= cutoff,
                )
                .order_by(models.Vote.created_at.desc())
                .first()
            )
            if recent_by_ip:
                elapsed   = (datetime.utcnow() - recent_by_ip.created_at).total_seconds()
                remaining = int(VOTE_COOLDOWN_HOURS * 3600 - elapsed)
                raise HTTPException(
                    status_code=429,
                    detail=f"A vote from your connection was already registered for {req.site_name}. "
                           f"Cooldown: {remaining} seconds remaining."
                )

        new_vote = models.Vote(
            user_id       = req.user_id,
            site_name     = req.site_name,
            vote_points   = VOTE_POINTS_BY_SITE.get(req.site_name, 2),
            status        = "pending",
            ip_address    = client_ip,
            game_username = req.game_username.strip(),
        )
        db.add(new_vote)
        db.commit()
        db.refresh(new_vote)

        return {
            "message": "Vote registered! Type ::claimvote in-game to receive your reward.",
            "vote_id": new_vote.id,
        }

    except HTTPException:
        raise
    except OperationalError:
        raise HTTPException(status_code=503, detail="Database connection failed. Please try again later.")


@app.get("/votes/status/{user_id}")
def get_vote_status(user_id: int, db: Session = Depends(get_db)):
    try:
        result = []
        for site_name in VALID_SITES:
            cutoff = datetime.utcnow() - timedelta(hours=VOTE_COOLDOWN_HOURS)
            recent = (
                db.query(models.Vote)
                .filter(
                    models.Vote.user_id   == user_id,
                    models.Vote.site_name == site_name,
                    models.Vote.created_at >= cutoff,
                )
                .order_by(models.Vote.created_at.desc())
                .first()
            )
            state_info = _get_vote_state(recent)
            result.append({"site_name": site_name, **state_info})
        return result
    except OperationalError:
        raise HTTPException(status_code=503, detail="Database connection failed. Please try again later.")


@app.get("/votes/pending/{user_id}")
def get_pending_votes(user_id: int, db: Session = Depends(get_db)):
    try:
        pending = (
            db.query(models.Vote)
            .filter(
                models.Vote.user_id == user_id,
                models.Vote.status  == "pending",
            )
            .all()
        )
        return [
            {"vote_id": v.id, "site_name": v.site_name, "vote_points": v.vote_points}
            for v in pending
        ]
    except OperationalError:
        raise HTTPException(status_code=503, detail="Database connection failed.")


@app.post("/votes/claim/{vote_id}")
def claim_vote(vote_id: int, db: Session = Depends(get_db)):
    try:
        vote = db.query(models.Vote).filter(models.Vote.id == vote_id).first()
        if not vote:
            raise HTTPException(status_code=404, detail="Vote not found.")
        if vote.status == "claimed":
            raise HTTPException(status_code=400, detail="Vote already claimed.")
        vote.status     = "claimed"
        vote.claimed_at = datetime.utcnow()
        db.commit()
        return {"message": f"Vote {vote_id} marked as claimed."}
    except HTTPException:
        raise
    except OperationalError:
        raise HTTPException(status_code=503, detail="Database connection failed.")


# ── Admin endpoints ────────────────────────────────────────────────────────────
@app.get("/admin/users")
def get_all_users(db: Session = Depends(get_db)):
    users = db.query(models.User).order_by(models.User.id.desc()).all()
    return [
        {
            "id": u.id,
            "username": u.username,
            "privilege": u.privilege.value if hasattr(u.privilege, 'value') else str(u.privilege),
            "total_spent": u.total_spent
        }
        for u in users
    ]

@app.get("/admin/donations")
def get_all_donations(db: Session = Depends(get_db)):
    donations = db.query(models.Donation).order_by(models.Donation.id.desc()).all()
    return [
        {
            "id": d.id,
            "package_name": d.package_name,
            "usd_amount": d.usd_amount,
            "status": d.status
        }
        for d in donations
    ]
