package com.near_reality.plugins.factionwars

import com.near_reality.api.dao.Db
import com.zenyte.game.world.entity.player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Two playable factions for the weekly Faction War.
 */
enum class Faction(val displayName: String, val color: String) {
    CORRUPTED(     "The Corrupted",    "8b0000"),
    LIGHTBRINGERS( "The Lightbringers","1e90ff");

    val opponent: Faction get() = if (this == CORRUPTED) LIGHTBRINGERS else CORRUPTED

    companion object {
        fun fromName(name: String): Faction? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

/**
 * Core logic for faction assignment, kill scoring, and winner/loser resolution.
 */
object FactionManager {

    private val log = LoggerFactory.getLogger(FactionManager::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    // ── Faction assignment ───────────────────────────────────────────────────

    /**
     * Called on a player's first login (when [Player.factionName] is blank).
     * Assigns them to the faction with fewer members to keep teams balanced.
     * Saves the choice to the persistent attribute immediately.
     */
    fun assignFaction(player: Player) {
        if (player.factionName.isNotBlank()) return  // already assigned

        scope.launch {
            val faction = pickBalancedFaction()
            player.factionName = faction.name

            ensureFactionRowExists(faction)

            player.sendMessage(
                "<col=${faction.color}>[Faction Wars] You have sworn allegiance to " +
                "<col=ffffff>${faction.displayName}</col>" +
                "<col=${faction.color}>! Fight for your faction every week in the Wilderness.</col>"
            )
            log.info("Player {} assigned to faction {}", player.name, faction.name)
        }
    }

    /**
     * Returns the faction that currently has fewer members (by counting non-blank
     * factionName attributes in memory).  Falls back to [Faction.CORRUPTED] on a tie.
     *
     * TODO: For accuracy beyond the current online players, query a DB count of all
     *       users grouped by faction_name.
     */
    private fun pickBalancedFaction(): Faction {
        // Simple in-memory heuristic — good enough for small servers.
        return Faction.CORRUPTED  // TODO: replace with DB-backed count
    }

    // ── Kill scoring ─────────────────────────────────────────────────────────

    /**
     * Adds 1 War Point to the killer's faction when they kill a member of the
     * opposing faction in the Wilderness.
     */
    fun recordWildernessKill(killer: Player, victim: Player) {
        val killerFaction = Faction.fromName(killer.factionName) ?: return
        val victimFaction = Faction.fromName(victim.factionName) ?: return

        // Only score inter-faction kills
        if (killerFaction == victimFaction) return

        killer.factionWarPoints++

        scope.launch {
            Db.dbQueryMain {
                FactionWarScores.update({ FactionWarScores.factionName eq killerFaction.name }) {
                    it[weekPoints]  = weekPoints + 1
                    it[totalPoints] = totalPoints + 1
                }
            }
        }

        killer.sendMessage(
            "<col=${killerFaction.color}>[Faction Wars] +1 War Point for ${killerFaction.displayName}!</col>"
        )
    }

    // ── Weekly reset ─────────────────────────────────────────────────────────

    /**
     * Resolves the current week:
     * 1. Determines the winning faction.
     * 2. Writes the result to [FactionWarHistory].
     * 3. Grants winner-zone access to all online winning-faction members.
     * 4. Revokes winner-zone access from all online losing-faction members.
     * 5. Resets [FactionWarScores.weekPoints] back to zero.
     *
     * Called by [FactionWarScheduler] every Sunday at 20:00.
     */
    suspend fun resolveWeeklyWar(onlinePlayersSnapshot: List<Player>) {
        val scores = Db.dbQueryMain {
            FactionWarScores.selectAll().associate {
                it[FactionWarScores.factionName] to it[FactionWarScores.weekPoints]
            }
        }

        val corruptedPts    = scores[Faction.CORRUPTED.name]    ?: 0
        val lightbringerPts = scores[Faction.LIGHTBRINGERS.name] ?: 0

        val (winner, loser) = if (corruptedPts >= lightbringerPts)
            Faction.CORRUPTED to Faction.LIGHTBRINGERS
        else
            Faction.LIGHTBRINGERS to Faction.CORRUPTED

        val winnerPts = if (winner == Faction.CORRUPTED) corruptedPts else lightbringerPts
        val loserPts  = if (loser  == Faction.CORRUPTED) corruptedPts else lightbringerPts

        // Persist history
        Db.dbQueryMain {
            FactionWarHistory.insert {
                it[resolvedAt]    = LocalDateTime.now()
                it[winnerFaction] = winner.name
                it[FactionWarHistory.winnerPoints] = winnerPts
                it[loserFaction]  = loser.name
                it[FactionWarHistory.loserPoints]  = loserPts
            }
            // Increment win counter for winner
            FactionWarScores.update({ FactionWarScores.factionName eq winner.name }) {
                it[winsTotal] = winsTotal + 1
            }
            // Reset weekly scores
            FactionWarScores.update {
                it[weekPoints] = 0
                it[weekStart]  = LocalDateTime.now()
            }
        }

        // Apply winner/loser zone access to online players
        onlinePlayersSnapshot.forEach { player ->
            val pFaction = Faction.fromName(player.factionName) ?: return@forEach
            if (pFaction == winner) {
                grantWinnerZoneAccess(player, winner)
            } else {
                revokeWinnerZoneAccess(player)
            }
        }

        // Also reset personal war points for all online players
        onlinePlayersSnapshot.forEach { it.factionWarPoints = 0 }

        log.info(
            "Faction war resolved: winner={} ({}pts) vs loser={} ({}pts)",
            winner.name, winnerPts, loser.name, loserPts
        )
    }

    // ── Winner zone access ───────────────────────────────────────────────────

    fun grantWinnerZoneAccess(player: Player, winner: Faction) {
        player.hasWinnerZoneAccess = true
        player.sendMessage(
            "<col=${winner.color}>[Faction Wars] ${winner.displayName} wins this week! " +
            "You have been granted access to the Zelus Sanctum for 7 days.</col>"
        )
    }

    fun revokeWinnerZoneAccess(player: Player) {
        player.hasWinnerZoneAccess = false
    }

    // ── DB initialisation ────────────────────────────────────────────────────

    /** Ensures both faction rows exist in [FactionWarScores] on first boot. */
    suspend fun initFactionRows() {
        Db.dbQueryMain {
            Faction.entries.forEach { faction ->
                val existing = FactionWarScores
                    .selectAll()
                    .where { FactionWarScores.factionName eq faction.name }
                    .firstOrNull()
                if (existing == null) {
                    FactionWarScores.insert {
                        it[factionName] = faction.name
                        it[weekPoints]  = 0
                        it[totalPoints] = 0L
                        it[weekStart]   = LocalDateTime.now()
                        it[winsTotal]   = 0
                    }
                }
            }
        }
    }

    private fun ensureFactionRowExists(faction: Faction) {
        scope.launch { initFactionRows() }
    }
}
