from sqlalchemy import Column, Integer, String, Boolean, Float, DateTime, Enum, ForeignKey, Text, UniqueConstraint
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.sql import func
import enum

Base = declarative_base()

# הגדרת דרגות השחקנים (תואם ל- ApiPrivilege ב-Kotlin)
class ApiPrivilege(enum.Enum):
    PLAYER = "PLAYER"
    YOUTUBER = "YOUTUBER"
    MEMBER = "MEMBER"
    FORUM_MODERATOR = "FORUM_MODERATOR"
    SUPPORT = "SUPPORT"
    MODERATOR = "MODERATOR"
    SENIOR_MODERATOR = "SENIOR_MODERATOR"
    ADMINISTRATOR = "ADMINISTRATOR"
    DEVELOPER = "DEVELOPER"
    HIDDEN_ADMINISTRATOR = "HIDDEN_ADMINISTRATOR"
    TRUE_DEVELOPER = "TRUE_DEVELOPER"

# הגדרת מצב המשחק (Normal, Ironman וכו' - הוספתי בסיס שתוכל להרחיב)
class GameMode(enum.Enum):
    NORMAL = "NORMAL"
    IRONMAN = "IRONMAN"
    HARDCORE_IRONMAN = "HARDCORE_IRONMAN"
    ULTIMATE_IRONMAN = "ULTIMATE_IRONMAN"

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    username = Column(String(12), unique=True, index=True, nullable=False)
    password = Column(String(128), nullable=False) # כאן נשמור את סיסמת ה-Bcrypt
    password_at_risk = Column(Boolean, default=False)
    email = Column(String(255), unique=True, index=True, nullable=True)

    privilege = Column(Enum(ApiPrivilege), default=ApiPrivilege.PLAYER)
    tokens = Column(Integer, default=0) # המטבעות לחנות
    votes = Column(Integer, default=0)
    total_spent = Column(Float, default=0.0) # לפיו נחשב את דרגת התורם (Premium, Legendary...)

    last_login = Column(DateTime, nullable=True)
    join_date = Column(DateTime, default=func.now())

    two_factor_secret = Column(String(50), nullable=True)
    two_factor_activated = Column(Boolean, default=False)
    game_mode = Column(Enum(GameMode), default=GameMode.NORMAL)

    is_verified        = Column(Boolean, default=False)
    verification_token = Column(String(255), nullable=True, index=True)


class UserSkillStat(Base):
    __tablename__ = "user_skill_stats"

    # ה-ID של השחקן הוא גם המפתח הראשי כאן, כי לכל שחקן יש שורת סטטוס אחת
    user_id = Column(Integer, ForeignKey("users.id"), primary_key=True)

    total_level = Column(Integer, default=32) # רמה התחלתית ברונאסקייפ (1 בכל סקיל, 10 ב-HP)
    total_experience = Column(Integer, default=1154) # XP התחלתי (של רמה 10 ב-HP)

    attack_xp       = Column(Integer, default=0)
    defence_xp      = Column(Integer, default=0)
    strength_xp     = Column(Integer, default=0)
    hitpoints_xp    = Column(Integer, default=1154)
    ranged_xp       = Column(Integer, default=0)
    prayer_xp       = Column(Integer, default=0)
    magic_xp        = Column(Integer, default=0)
    cooking_xp      = Column(Integer, default=0)
    woodcutting_xp  = Column(Integer, default=0)
    fletching_xp    = Column(Integer, default=0)
    fishing_xp      = Column(Integer, default=0)
    firemaking_xp   = Column(Integer, default=0)
    crafting_xp     = Column(Integer, default=0)
    smithing_xp     = Column(Integer, default=0)
    mining_xp       = Column(Integer, default=0)
    herblore_xp     = Column(Integer, default=0)
    agility_xp      = Column(Integer, default=0)
    thieving_xp     = Column(Integer, default=0)
    slayer_xp       = Column(Integer, default=0)
    farming_xp      = Column(Integer, default=0)
    runecrafting_xp = Column(Integer, default=0)
    hunter_xp       = Column(Integer, default=0)
    construction_xp = Column(Integer, default=0)


class Vote(Base):
    """
    Tracks votes submitted through the website portal.
    The Kotlin game server reads this table via ::claimvote to award in-game rewards.
    """
    __tablename__ = "votes"

    id            = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id       = Column(Integer, ForeignKey("users.id"), index=True, nullable=False)
    site_name     = Column(String(50), nullable=False)   # e.g. RUNELOCUS, RSPS_LIST
    vote_points   = Column(Integer, default=2)            # points to award when claimed
    status        = Column(String(20), default="pending") # pending | claimed
    created_at    = Column(DateTime, default=func.now())
    claimed_at    = Column(DateTime, nullable=True)
    ip_address    = Column(String(45), nullable=True)     # IPv4 or IPv6 of voter
    game_username = Column(String(12), nullable=True)     # validated in-game character name


class Donation(Base):
    """
    Legacy donation table — kept for backward compatibility with existing ::claimdonate command.
    New purchases go through Transaction + PendingClaim instead.
    """
    __tablename__ = "donations"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id"), index=True)

    package_name = Column(String(100), nullable=False)
    usd_amount = Column(Float, nullable=False)
    tokens_to_give = Column(Integer, nullable=False)

    status = Column(String(20), default="pending")

    created_at = Column(DateTime, default=func.now())
    claimed_at = Column(DateTime, nullable=True)


# ── Payment system models ──────────────────────────────────────────────────────

class TransactionStatus(enum.Enum):
    PENDING   = "pending"
    COMPLETED = "completed"
    FAILED    = "failed"
    REFUNDED  = "refunded"


class PaymentProvider(enum.Enum):
    STRIPE = "stripe"
    PAYPAL = "paypal"


class Transaction(Base):
    """
    Audit log for every payment attempt (pending, completed, failed, refunded).
    Created before the player is redirected to Stripe/PayPal; updated by the webhook.
    """
    __tablename__ = "transactions"

    id                  = Column(Integer, primary_key=True, index=True, autoincrement=True)
    username            = Column(String(12), nullable=False, index=True)
    package_id          = Column(String(50), nullable=False)
    package_name        = Column(String(100), nullable=False)
    amount_usd          = Column(Float, nullable=False)
    provider            = Column(Enum(PaymentProvider), nullable=False)
    # Stripe session ID or PayPal order ID — set after provider session is created
    provider_session_id = Column(String(255), unique=True, index=True, nullable=True)
    status              = Column(Enum(TransactionStatus), default=TransactionStatus.PENDING, nullable=False)
    created_at          = Column(DateTime, default=func.now())
    completed_at        = Column(DateTime, nullable=True)
    # Raw webhook payload stored for debugging and customer support
    raw_webhook_payload = Column(Text, nullable=True)


class PendingClaim(Base):
    """
    Written by the webhook handler after a payment is verified.
    The in-game ::claim command reads this table to deliver items to the player.
    claimed_status: 'unclaimed' → 'claimed'
    """
    __tablename__ = "pending_claims"

    id             = Column(Integer, primary_key=True, index=True, autoincrement=True)
    username       = Column(String(12), nullable=False, index=True)
    package_id     = Column(String(50), nullable=False)
    package_name   = Column(String(100), nullable=False)
    tokens_to_give = Column(Integer, nullable=False)
    transaction_id = Column(Integer, ForeignKey("transactions.id"), nullable=True, index=True)
    claimed_status = Column(String(20), default="unclaimed", nullable=False)
    created_at     = Column(DateTime, default=func.now())
    claimed_at     = Column(DateTime, nullable=True)

    # DB-level safety net: one claim per transaction, prevents duplicate fulfillment
    # even if two webhook deliveries race past the application-level idempotency check.
    __table_args__ = (
        UniqueConstraint("transaction_id", name="uq_pending_claims_transaction_id"),
    )

class TokenPurpose(enum.Enum):
      EMAIL_VERIFICATION = "email_verification"
      PASSWORD_RESET     = "password_reset"


class AuthToken(Base):
      """
      Secure, single-use, expiring tokens for email verification and password reset.

      Security properties:
      - token_hash: we store a SHA-256 hash of the token, never the raw value.
        Even if the DB is dumped, tokens can't be used.
      - expires_at: hard expiry enforced in the DB query (1 hour for reset,
        24 hours for verification).
      - used_at: set when consumed — any reuse attempt fails the `used_at IS NULL`
        check, making tokens strictly one-time.
      - One active token per user per purpose: old tokens are deleted before
        inserting a new one, so "resend" can't pile up infinite valid tokens.
      """
      __tablename__ = "auth_tokens"

      id         = Column(Integer, primary_key=True, autoincrement=True)
      user_id    = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
      purpose    = Column(Enum(TokenPurpose), nullable=False)
      token_hash = Column(String(64), nullable=False, unique=True, index=True)
      expires_at = Column(DateTime, nullable=False)
      created_at = Column(DateTime, default=func.now())
      used_at    = Column(DateTime, nullable=True)