package com.near_reality.plugins.area.warmarea

import com.zenyte.game.content.skills.prayer.Prayer
import com.zenyte.game.task.WorldTasksManager.schedule
import com.zenyte.game.world.entity.Entity
import com.zenyte.game.world.entity.Location
import com.zenyte.game.world.entity.masks.Animation
import com.zenyte.game.world.entity.masks.Hit
import com.zenyte.game.world.entity.masks.HitType
import com.zenyte.game.world.entity.npc.NPC
import com.zenyte.game.world.entity.npc.combat.CombatScript
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot
import kotlin.math.min
import kotlin.random.Random

/**
 * AI training bot for the Warmarea zone.
 *
 * ## Combat behaviour
 * - **Overhead prayer**: On each attack the bot inspects the player's equipped weapon
 *   and activates the matching protection prayer. Acolyte reacts slowly ([WarmareaBotTier.prayerAccuracy]),
 *   Zelus Master reacts every tick.
 * - **Gear switch / special attack**: Every [WarmareaBotTier.specThreshold] attacks the
 *   bot fires a high-damage "special" hit simulating an AGS spec.
 * - **Combo-eat**: When HP falls below [WarmareaBotTier.eatThreshold] × maxHP the bot
 *   heals [WarmareaBotTier.eatAmount] HP and plays an eat animation, then immediately
 *   throws a fast follow-up hit (simulating a combo-eat scenario).
 *
 * ## Safe death
 * Item protection on death is enforced by [WarmareaPlugin] via the [PlayerEvent.Died] hook —
 * this NPC class only handles combat AI.
 */
class WarmareaBot(
    val tier: WarmareaBotTier,
    spawnLocation: Location,
) : NPC(tier.npcId, spawnLocation, true), CombatScript {

    // ── State ────────────────────────────────────────────────────────────────

    private enum class BotState { NORMAL, EATING, SPEC_WIND_UP }

    private var state: BotState = BotState.NORMAL
    private var attackCounter: Int = 0

    // Simulated spec-energy: regenerates 10 units/tick; spec costs 55 (AGS)
    private var specEnergy: Int = 100

    init {
        aggressionDistance = 16
        isForceAggressive = true
    }

    // ── Main attack loop ─────────────────────────────────────────────────────

    override fun attack(target: Entity?): Int {
        val player = target as? Player ?: return attackSpeed
        if (state == BotState.EATING) return attackSpeed  // busy eating

        attackCounter++

        // React to player's combat style with overhead prayer (tier-gated accuracy)
        updateOverheadPrayer(player)

        // Check if we should eat before attacking
        if (shouldEat()) {
            performComboEat(player)
            return attackSpeed
        }

        // Special attack window: simulates AGS gear-switch + spec
        if (attackCounter % tier.specThreshold == 0 && specEnergy >= 55) {
            performSpecialAttack(player)
            specEnergy -= 55
            return attackSpeed
        }

        // Standard melee attack
        performMeleeAttack(player)
        return attackSpeed
    }

    // ── Tick processing ──────────────────────────────────────────────────────

    override fun processNPC() {
        super.processNPC()
        // Regenerate spec energy (10 units per tick, capped at 100)
        if (specEnergy < 100) specEnergy = min(100, specEnergy + 10)
    }

    // ── Combat actions ───────────────────────────────────────────────────────

    private fun performMeleeAttack(target: Player) {
        animation = Animation(423) // TODO: swap for correct melee swing anim
        val damage = Random.nextInt(0, tier.maxHit + 1)
        schedule(1) {
            target.applyHit(Hit(this@WarmareaBot, damage, HitType.DEFAULT))
        }
    }

    /**
     * Simulates an AGS special attack: plays a wind-up animation on tick 0,
     * then delivers a doubled max-hit on tick 1.
     */
    private fun performSpecialAttack(target: Player) {
        state = BotState.SPEC_WIND_UP
        animation = Animation(7054) // AGS spec anim — TODO: verify ID
        val damage = Random.nextInt(tier.maxHit, tier.specMaxHit + 1)
        schedule(1) {
            target.applyHit(Hit(this@WarmareaBot, damage, HitType.DEFAULT))
            state = BotState.NORMAL
        }
    }

    /**
     * Heals the bot for [WarmareaBotTier.eatAmount] HP, plays an eat animation,
     * then immediately fires a follow-up hit — the classic OSRS combo-eat pattern.
     */
    private fun performComboEat(target: Player) {
        state = BotState.EATING
        animation = Animation(829) // eat animation
        hitpoints = min(maxHitpoints, hitpoints + tier.eatAmount)
        // Combo-eat: deliver a fast follow-up hit on the very next tick
        schedule(1) {
            val damage = Random.nextInt(0, tier.maxHit + 1)
            target.applyHit(Hit(this@WarmareaBot, damage, HitType.DEFAULT))
            state = BotState.NORMAL
        }
    }

    // ── Incoming hit — overhead prayer damage reduction ──────────────────────

    /**
     * Reduces incoming damage by 40 % when the bot has the correct overhead prayer
     * active for the player's attack style (mirrors OSRS prayer protection mechanic).
     */
    override fun handleIngoingHit(hit: Hit?) {
        if (hit == null) return
        val attacker = hit.source as? Player ?: run { super.handleIngoingHit(hit); return }

        val style = detectPlayerCombatStyle(attacker)
        if (isPrayingAgainst(style)) {
            hit.damage = (hit.damage * 0.60).toInt()  // 40 % damage reduction
        }
        super.handleIngoingHit(hit)
    }

    // ── Prayer logic ─────────────────────────────────────────────────────────

    /**
     * Each attack the bot "reacts" to the player's weapon type and updates its
     * overhead prayer. The chance of a correct reaction is [WarmareaBotTier.prayerAccuracy].
     *
     * TODO: Call the NPC head-icon API here to visually show the prayer overhead
     *       (e.g. setHeadIconPk(HeadIcon.MELEE_PROTECTION)) once confirmed.
     */
    private fun updateOverheadPrayer(player: Player) {
        if (Random.nextDouble() > tier.prayerAccuracy) return  // bot "delays" reaction
        currentPrayedStyle = detectPlayerCombatStyle(player)
    }

    /** The combat style the bot is currently praying against. Null = no prayer active. */
    private var currentPrayedStyle: CombatStyle? = null

    private fun isPrayingAgainst(style: CombatStyle): Boolean = currentPrayedStyle == style

    // ── Helpers ──────────────────────────────────────────────────────────────

    private val attackSpeed: Int get() = when (tier) {
        WarmareaBotTier.ACOLYTE      -> 5
        WarmareaBotTier.KNIGHT       -> 5
        WarmareaBotTier.ZELUS_MASTER -> 4
    }

    private fun shouldEat(): Boolean =
        hitpoints < (maxHitpoints * tier.eatThreshold).toInt() && state != BotState.EATING

    /**
     * Detects the player's current combat style by inspecting their equipped weapon name.
     * Bows / crossbows / throwing weapons → RANGED; staves / wands / tridents → MAGIC;
     * everything else → MELEE.
     */
    private fun detectPlayerCombatStyle(player: Player): CombatStyle {
        val weapon = player.equipment.getItem(EquipmentSlot.WEAPON) ?: return CombatStyle.MELEE
        val name = weapon.name.lowercase()
        return when {
            name.contains("bow")      || name.contains("crossbow") ||
            name.contains("ballista") || name.contains("thrownaxe") ||
            name.contains("dart")     || name.contains("knife")     -> CombatStyle.RANGED

            name.contains("staff")    || name.contains("wand")      ||
            name.contains("trident")  || name.contains("tome")      -> CombatStyle.MAGIC

            else -> CombatStyle.MELEE
        }
    }

    enum class CombatStyle { MELEE, RANGED, MAGIC }
}
