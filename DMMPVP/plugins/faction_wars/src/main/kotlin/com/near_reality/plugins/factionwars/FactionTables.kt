package com.near_reality.plugins.factionwars

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Global faction score tracked per calendar week.
 *
 * One row per faction per week.
 * Reset every Sunday at 20:00 by [FactionWarScheduler].
 *
 * ## Registration
 * Add the following to [com.near_reality.api.dao.Db.initMainDatabase]:
 * ```kotlin
 * create(FactionWarScores)
 * create(FactionWarHistory)
 * ```
 */
object FactionWarScores : Table("faction_war_scores") {

    /** "CORRUPTED" or "LIGHTBRINGERS" */
    val factionName = varchar("faction_name", 32).uniqueIndex()

    /** Points accumulated in the current live week. */
    val weekPoints = integer("week_points").default(0)

    /** All-time cumulative points across all weeks. */
    val totalPoints = long("total_points").default(0L)

    /** UTC start time of the current scoring week. */
    val weekStart = datetime("week_start")

    /** Number of weekly wars this faction has won. */
    val winsTotal = integer("wins_total").default(0)

    override val primaryKey = PrimaryKey(factionName)
}

/**
 * Immutable log of every weekly war result.
 * Written on each Sunday reset; never mutated.
 */
object FactionWarHistory : Table("faction_war_history") {

    val id = integer("id").autoIncrement()

    /** UTC timestamp of the reset that ended this week. */
    val resolvedAt = datetime("resolved_at")

    /** Faction with the most points that week. */
    val winnerFaction = varchar("winner_faction", 32)

    val winnerPoints = integer("winner_points")
    val loserFaction  = varchar("loser_faction", 32)
    val loserPoints   = integer("loser_points")

    override val primaryKey = PrimaryKey(id)
}
