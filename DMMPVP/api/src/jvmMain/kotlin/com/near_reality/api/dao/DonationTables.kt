package com.near_reality.api.dao

import com.near_reality.api.responses.ClaimedDonation
import com.near_reality.api.responses.UserClaimDonationsResponse
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.selectAll

/**
 * Mirrors the `donations` table created by the FastAPI website.
 * Schema must match: id, user_id, package_name, tokens_to_give, usd_amount, status
 */
object Donations : LongIdTable("donations") {
    val userId      = reference("user_id", Users)
    val packageName = varchar("package_name", 128)
    val tokensToGive = integer("tokens_to_give")
    val usdAmount   = float("usd_amount")
    val status      = varchar("status", 32).default("pending")
}

object DonationDao {

    /**
     * Finds all pending donations for [userId], marks each one as 'claimed',
     * updates the user's tokens and total_spent, then returns the updated user
     * and the list of claimed donations.
     *
     * Runs inside a single transaction so a crash mid-way rolls back everything.
     */
    fun claimPendingDonations(userId: Long): UserClaimDonationsResponse {
        return transaction(Db.mainDatabase) {
            val userEntity = UserEntity.findById(userId)
                ?: return@transaction UserClaimDonationsResponse.UserNotExist

            val pendingRows = Donations
                .selectAll()
                .where { (Donations.userId eq userId) and (Donations.status eq "pending") }
                .toList()

            if (pendingRows.isEmpty()) {
                return@transaction UserClaimDonationsResponse.NoPendingDonations
            }

            val claimed = mutableListOf<ClaimedDonation>()

            for (row in pendingRows) {
                val tokens  = row[Donations.tokensToGive]
                val usd     = row[Donations.usdAmount]
                val name    = row[Donations.packageName]
                val donationId = row[Donations.id]

                // Mark as claimed first — prevents double-grant if a crash occurs later
                Donations.update({ Donations.id eq donationId }) {
                    it[status] = "claimed"
                }

                userEntity.storeCredits += tokens
                userEntity.totalSpent   += usd

                claimed += ClaimedDonation(
                    packageName  = name,
                    tokensGiven  = tokens,
                    usdAmount    = usd
                )
            }

            UserClaimDonationsResponse.Success(
                user    = userEntity.toApiModel(),
                claimed = claimed
            )
        }
    }
}
