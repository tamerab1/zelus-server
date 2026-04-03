package com.near_reality.game.world.entity.player.bot

/**
 * Represents the current state of a PK bot.
 * 
 * @author Riskers
 */
enum class BotState {
    IDLE,
    COMBAT,
    EATING,
    PRAYER_SWITCHING,
    MOVING,
    RETREATING,
    WEAPON_SWITCHING,
    RESPAWNING,
    RESTOCKING,
    RETURNING,
    CROSSING_DITCH
} 