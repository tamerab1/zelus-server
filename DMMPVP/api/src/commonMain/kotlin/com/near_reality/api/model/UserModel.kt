package com.near_reality.api.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val name: String,
    val passwordHash: String? = null,
    val passwordAtRisk: Boolean,
    val email: String,
    val gameMode: ApiGameMode,
    val memberRank: ApiMemberRank,
    val privilege: ApiPrivilege,
    val twoFactorEnabled: Boolean,
    val twoFactorSecret: String? = null,
    var totalSpent: Int,
    var storeCredits: Int,
    val totalVotes: Int,
    val joinDate: LocalDateTime,
    val sanctions: List<Sanction>
) {
    fun stripCredentials() = copy(passwordHash = null, twoFactorSecret = null)
}
@Serializable
enum class Bond(val id: Int, val credits: Int, val amount: Int) {
    DONATOR_PIN_10(32070, 130, 10),
    DONATOR_PIN_25(32071, 325, 25),
    DONATOR_PIN_50(32072, 650, 50),
    DONATOR_PIN_100(32073, 1300, 100);

    fun isValid(): Boolean {
        return this.amount > 0 && this.credits > 0
    }

    companion object {
        // Retrieves the Bond by its ID
        operator fun get(id: Int): Bond? = entries.find { it.id == id }

        fun item(id: Int): Bond? = get(id)
    }
}




@Serializable
enum class ApiGameMode {
    REGULAR,
    STANDARD_IRON_MAN,
    ULTIMATE_IRON_MAN,
    HARDCORE_IRON_MAN,
    GROUP_IRON_MAN,
    GROUP_HARDCORE_IRON_MAN;

    val isIronMan: Boolean
        get() = this != REGULAR

    companion object {
        fun forId(id: Number) = when(id.toInt()) {
            1 -> REGULAR
            2 -> STANDARD_IRON_MAN
            3 -> ULTIMATE_IRON_MAN
            4 -> HARDCORE_IRON_MAN
            5 -> GROUP_IRON_MAN
            else -> error("Did not find ApiGameMode for id $id")
        }
    }
}

@Serializable
enum class ApiMemberRank(val formattedName: String, val requiredDonatedAmount: Int) {
    NONE("None", 0),
    PREMIUM("Premium", 25),
    EXPANSION("Expansion Premium", 50),
    EXTREME("Extreme Premium", 100),
    RESPECTED("Respected Premium", 150),
    LEGENDARY("Legendary Premium", 250),
    MYTHICAL("Mythical Premium", 5000),
    UBER("Uber Premium", 1000),

    /**
     * Amascut was a special rank added by Will that is only obtainable
     * for 2 members `speckle` and `gim`.
     */
    AMASCUT("Amascut", 1500);

    companion object {
        val obtainableRanks = entries.filter { it != AMASCUT }
        fun findForUserWithAmountSpent(username: String? = null, totalSpent: Int) =
            if (username == "mr_boolt")
                AMASCUT
            else
                obtainableRanks
                    .filter { it.requiredDonatedAmount <= totalSpent }
                    .maxBy { it.requiredDonatedAmount }
    }
}

@Serializable
enum class ApiPrivilege(val requires2FA: Boolean = false) {
    PLAYER(false),
    YOUTUBER(false),
    MEMBER(false),
    FORUM_MODERATOR(false),
    SUPPORT(false),
    MODERATOR(false),
    SENIOR_MODERATOR(false),
    ADMINISTRATOR(false),
    DEVELOPER(false),
    HIDDEN_ADMINISTRATOR(false),
    TRUE_DEVELOPER(false)
}

