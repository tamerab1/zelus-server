package com.near_reality.plugins.bountyboard

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Stores all active (and historically claimed/expired) player-placed bounties.
 *
 * ## Registration
 * Add `create(ActiveBounties)` inside [com.near_reality.api.dao.Db.initMainDatabase].
 */
object ActiveBounties : LongIdTable("active_bounties") {

    /** Username of the player who placed the bounty. */
    val posterName = varchar("poster_name", 12).index()

    /**
     * IP address of the poster at the time of placement.
     * Used to detect farming (same-IP kills).
     */
    val posterIp = varchar("poster_ip", 64)

    /** Username of the target player. */
    val targetName = varchar("target_name", 12).index()

    /** Item ID of the currency deposited (default: Blood Money 13307). */
    val currencyItemId = integer("currency_item_id").default(13307)

    /** Amount of currency locked in the Bounty Vault. */
    val amount = integer("amount")

    /** UTC timestamp when the bounty was placed. */
    val placedAt = datetime("placed_at")

    /**
     * `true`  → bounty is live and claimable.
     * `false` → bounty was claimed, expired, or cancelled.
     */
    val active = bool("active").default(true)

    /** Name of the player who claimed the bounty, null until claimed. */
    val claimedBy = varchar("claimed_by", 12).nullable().default(null)

    /** UTC timestamp when the bounty was claimed, null until claimed. */
    val claimedAt = datetime("claimed_at").nullable().default(null)
}
