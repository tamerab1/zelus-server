package com.near_reality.plugins.bountyboard

import com.near_reality.api.dao.Db
import com.zenyte.game.item.Item
import com.zenyte.game.world.broadcasts.BroadcastType
import com.zenyte.game.world.broadcasts.WorldBroadcasts
import com.zenyte.game.world.entity.player.MessageType
import com.zenyte.game.world.entity.player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Manages the lifecycle of player-placed bounties:
 * - [placeBounty]: Deduct currency, persist the bounty, broadcast globally.
 * - [onPvpKill]: Called from the kill-event hook; verifies eligibility and awards the bounty.
 *
 * ## Anti-farming
 * A kill does **not** award the bounty when:
 * - The killer's IP matches the poster's IP (alt-account farming prevention).
 * - The killer is the poster themselves.
 */
object BountyManager {

    private val log = LoggerFactory.getLogger(BountyManager::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    // ── Constants ────────────────────────────────────────────────────────────

    /** Default bounty currency — Blood Money. TODO: verify item ID. */
    private const val BLOOD_MONEY_ID = 13307

    /** Minimum bounty that can be placed. */
    private const val MIN_BOUNTY = 500

    /** Maximum bounty that can be placed (prevents eco damage). */
    private const val MAX_BOUNTY = 500_000

    // ── Place a bounty ───────────────────────────────────────────────────────

    /**
     * Places a bounty on [targetName].
     *
     * Steps:
     * 1. Validate amount and ensure [poster] has enough currency.
     * 2. Check [targetName] exists and is not the poster.
     * 3. Deduct [amount] Blood Money from the poster's inventory.
     * 4. Persist to [ActiveBounties].
     * 5. Broadcast globally.
     */
    fun placeBounty(poster: Player, targetName: String, amount: Int) {
        if (amount < MIN_BOUNTY) {
            poster.sendMessage("<col=ff4500>Minimum bounty is $MIN_BOUNTY Blood Money.</col>")
            return
        }
        if (amount > MAX_BOUNTY) {
            poster.sendMessage("<col=ff4500>Maximum bounty is $MAX_BOUNTY Blood Money.</col>")
            return
        }
        if (poster.name.equals(targetName, ignoreCase = true)) {
            poster.sendMessage("<col=ff4500>You cannot place a bounty on yourself.</col>")
            return
        }

        val bloodMoney = Item(BLOOD_MONEY_ID, amount)
        if (!poster.inventory.containsItem(bloodMoney)) {
            poster.sendMessage("<col=ff4500>You don't have enough Blood Money.</col>")
            return
        }

        // Check if an active bounty for this target already exists (stack it instead)
        scope.launch {
            val existing = Db.dbQueryMain {
                ActiveBounties.selectAll()
                    .where {
                        (ActiveBounties.targetName eq targetName.lowercase()) and
                        (ActiveBounties.active eq true)
                    }
                    .firstOrNull()
            }

            if (existing != null) {
                poster.sendMessage(
                    "<col=ff4500>$targetName already has an active bounty. " +
                    "It will be stacked once claimed.</col>"
                )
                return@launch
            }

            // Deduct currency (must happen on game thread — use post to main thread if needed)
            poster.inventory.deleteItem(bloodMoney)

            val posterIp = poster.hostAddress ?: "unknown"
            Db.dbQueryMain {
                ActiveBounties.insert {
                    it[posterName]     = poster.name.lowercase()
                    it[ActiveBounties.posterIp] = posterIp
                    it[ActiveBounties.targetName] = targetName.lowercase()
                    it[currencyItemId] = BLOOD_MONEY_ID
                    it[ActiveBounties.amount] = amount
                    it[placedAt]       = LocalDateTime.now()
                    it[active]         = true
                }
            }

            val broadcast = "<col=ff4500>[Bounty Board] A bounty of " +
                "<col=ffd700>${amount} Blood Money</col>" +
                "<col=ff4500> has been placed on " +
                "<col=ffffff>${capitalize(targetName)}</col>" +
                "<col=ff4500>'s head!</col>"
            WorldBroadcasts.sendMessage(broadcast, BroadcastType.WILDERNESS_EVENT, false)

            poster.sendMessage(
                "<col=ffd700>[Bounty Board] Your bounty of $amount Blood Money " +
                "on ${capitalize(targetName)} is now live.</col>",
                MessageType.UNFILTERABLE
            )
            log.info("Bounty placed: poster={} target={} amount={}", poster.name, targetName, amount)
        }
    }

    // ── PvP kill verification ────────────────────────────────────────────────

    /**
     * Called when [killer] kills [victim] in the Wilderness.
     * Checks if [victim] has an active bounty and, if so, verifies the kill is
     * legitimate before paying out the reward.
     */
    fun onPvpKill(killer: Player, victim: Player) {
        scope.launch {
            val bounty = Db.dbQueryMain {
                ActiveBounties.selectAll()
                    .where {
                        (ActiveBounties.targetName eq victim.name.lowercase()) and
                        (ActiveBounties.active eq true)
                    }
                    .firstOrNull()
            } ?: return@launch   // no active bounty on this player

            val killerIp = killer.hostAddress ?: "unknown"
            val posterIp = bounty[ActiveBounties.posterIp]

            // ── Anti-farming checks ──────────────────────────────────────────
            if (killer.name.equals(bounty[ActiveBounties.posterName], ignoreCase = true)) {
                killer.sendMessage(
                    "<col=ff4500>[Bounty Board] You cannot claim your own bounty.</col>"
                )
                return@launch
            }
            if (killerIp == posterIp) {
                killer.sendMessage(
                    "<col=ff4500>[Bounty Board] Bounty claim rejected — " +
                    "connection matches the poster's IP.</col>"
                )
                log.warn(
                    "Bounty farm attempt blocked: killer={} victim={} ip={}",
                    killer.name, victim.name, killerIp
                )
                return@launch
            }

            // ── Pay out ──────────────────────────────────────────────────────
            val reward = bounty[ActiveBounties.amount]
            val bountyId = bounty[ActiveBounties.id].value

            Db.dbQueryMain {
                ActiveBounties.update({ ActiveBounties.id eq bountyId }) {
                    it[active]    = false
                    it[claimedBy] = killer.name.lowercase()
                    it[claimedAt] = LocalDateTime.now()
                }
            }

            killer.inventory.addOrDrop(Item(BLOOD_MONEY_ID, reward))
            killer.sendMessage(
                "<col=ffd700>[Bounty Board] You claimed the bounty on " +
                "${capitalize(victim.name)}'s head! Reward: $reward Blood Money.</col>",
                MessageType.UNFILTERABLE
            )

            WorldBroadcasts.sendMessage(
                "<col=ff4500>[Bounty Board] <col=ffffff>${capitalize(killer.name)}</col>" +
                "<col=ff4500> has claimed the bounty on " +
                "<col=ffffff>${capitalize(victim.name)}</col>" +
                "<col=ff4500>'s head and earned " +
                "<col=ffd700>$reward Blood Money</col><col=ff4500>!</col>",
                BroadcastType.WILDERNESS_EVENT, false
            )
            log.info(
                "Bounty claimed: killer={} victim={} reward={}",
                killer.name, victim.name, reward
            )
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun capitalize(name: String): String =
        name.replaceFirstChar { it.uppercaseChar() }
}
