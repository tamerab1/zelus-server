from sqlalchemy import Column, Integer, String, Boolean, Float, DateTime, Enum, ForeignKey
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
    טבלה חדשה וייעודית עבור האתר!
    מכאן שרת ה-Kotlin יקרא כשהשחקן יקליד ::claimdonate
    """
    __tablename__ = "donations"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id"), index=True)

    package_name = Column(String(100), nullable=False) # למשל "Slayer Package"
    usd_amount = Column(Float, nullable=False) # כמה השחקן שילם בפועל
    tokens_to_give = Column(Integer, nullable=False) # כמה Tokens החבילה הזו שווה

    # סטטוס ההזמנה: pending (ממתין במשחק), claimed (נאסף)
    status = Column(String(20), default="pending")

    created_at = Column(DateTime, default=func.now())
    claimed_at = Column(DateTime, nullable=True)