package com.near_reality.plugins.area.warmarea

/**
 * Defines the three difficulty tiers for Warmarea AI bots.
 *
 * @param npcId          NPC ID to spawn. Add these entries to NpcId / the cache.
 * @param trainingReward Training Points awarded to the player who kills this bot.
 * @param eatThreshold   Bot heals when HP drops below this fraction of max HP.
 * @param eatAmount      HP restored per eat action.
 * @param specThreshold  Attack-counter interval at which the bot uses its special attack.
 * @param prayerAccuracy Probability (0.0–1.0) that the bot correctly switches prayer each attack.
 *                       Acolyte is forgiving, Zelus Master reacts instantly.
 * @param maxHit         Regular attack max hit.
 * @param specMaxHit     Special-attack max hit (AGS-style high hit).
 */
enum class WarmareaBotTier(
    val npcId: Int,
    val trainingReward: Int,
    val eatThreshold: Double,
    val eatAmount: Int,
    val specThreshold: Int,
    val prayerAccuracy: Double,
    val maxHit: Int,
    val specMaxHit: Int,
) {
    // TODO: Replace placeholder npcIds with real cache-defined IDs for each tier.
    ACOLYTE(
        npcId         = 14000,  // TODO: define in NpcId
        trainingReward = 1,
        eatThreshold  = 0.30,
        eatAmount     = 15,
        specThreshold = 10,     // uses spec every 10 attacks
        prayerAccuracy = 0.40,  // only correctly prays 40% of the time
        maxHit        = 16,
        specMaxHit    = 28,
    ),
    KNIGHT(
        npcId         = 14001,
        trainingReward = 3,
        eatThreshold  = 0.40,
        eatAmount     = 20,
        specThreshold = 6,
        prayerAccuracy = 0.70,
        maxHit        = 22,
        specMaxHit    = 38,
    ),
    ZELUS_MASTER(
        npcId         = 14002,
        trainingReward = 7,
        eatThreshold  = 0.50,
        eatAmount     = 26,
        specThreshold = 4,      // uses spec every 4 attacks
        prayerAccuracy = 1.00,  // always prays correctly
        maxHit        = 30,
        specMaxHit    = 52,
    );

    companion object {
        fun fromNpcId(id: Int): WarmareaBotTier? = entries.firstOrNull { it.npcId == id }
    }
}
