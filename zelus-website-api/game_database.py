from sqlalchemy import create_engine, Column, BigInteger, Integer, String
from sqlalchemy.orm import sessionmaker, declarative_base

import os
# When running on the game server, use localhost. Set GAME_DB_HOST env var to override.
_host = os.getenv("GAME_DB_HOST", "localhost")
GAME_DB_URL = f"postgresql://nr:SQ6UMey4YwFNcqLsdbDkXmp9@{_host}:5432/nr"
game_engine = create_engine(GAME_DB_URL, pool_pre_ping=True)
GameSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=game_engine)
GameBase = declarative_base()

class GameUser(GameBase):
    __tablename__ = "users"
    id       = Column(BigInteger, primary_key=True)
    username = Column(String(12))

class GameUserSkillStat(GameBase):
    __tablename__ = "user_skill_stats"
    id               = Column(Integer, primary_key=True)
    account          = Column(BigInteger)  # FK → users.id (named "account" in game DB)
    total_level      = Column(Integer)
    total_experience = Column(Integer)
    attack_xp        = Column(Integer)
    defence_xp       = Column(Integer)
    strength_xp      = Column(Integer)
    hitpoints_xp     = Column(Integer)
    ranged_xp        = Column(Integer)
    prayer_xp        = Column(Integer)
    magic_xp         = Column(Integer)
    cooking_xp       = Column(Integer)
    woodcutting_xp   = Column(Integer)
    fletching_xp     = Column(Integer)
    fishing_xp       = Column(Integer)
    firemaking_xp    = Column(Integer)
    crafting_xp      = Column(Integer)
    smithing_xp      = Column(Integer)
    mining_xp        = Column(Integer)
    herblore_xp      = Column(Integer)
    agility_xp       = Column(Integer)
    thieving_xp      = Column(Integer)
    slayer_xp        = Column(Integer)
    farming_xp       = Column(Integer)
    runecrafting_xp  = Column(Integer)
    hunter_xp        = Column(Integer)
    construction_xp  = Column(Integer)

def get_game_db():
    db = GameSessionLocal()
    try:
        yield db
    finally:
        db.close()
