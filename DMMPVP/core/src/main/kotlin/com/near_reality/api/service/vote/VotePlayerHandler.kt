package com.near_reality.api.service.vote

import com.near_reality.api.APIClient
import com.near_reality.api.model.User
import com.near_reality.api.model.Vote
import com.near_reality.api.model.VoteSite
import com.near_reality.api.model.VoteSiteStatus
import com.near_reality.api.service.store.notify
import com.near_reality.api.service.user.updateVoteStatisticOnPlayerDetailsTab
import com.near_reality.tools.logging.GameLogMessage
import com.near_reality.tools.logging.GameLogger
import com.zenyte.cores.CoresManager
import com.zenyte.game.content.skills.afk.AfkSkilling
import com.zenyte.game.content.vote.VoteHandler
import com.zenyte.game.content.xamphur.XamphurHandler
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.zenyte.game.item.Item
import com.zenyte.game.item.ItemId
import com.zenyte.game.task.WorldTasksManager
import com.zenyte.game.util.Colour
import com.zenyte.game.world.World
import com.zenyte.game.world.broadcasts.BroadcastType
import com.zenyte.game.world.broadcasts.WorldBroadcasts
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.entity.player.dialogue.dialogue
import com.zenyte.plugins.dialogue.WiseOldManD
import com.zenyte.utils.TimeUnit
import io.ktor.http.*
import io.ktor.server.util.*
import mgi.utilities.StringFormatUtil

object VotePlayerHandler {

    private val logger = LoggerFactory.getLogger(VotePlayerHandler::class.java)
    internal var peopleVoted = 0

    private const val WEBSITE_API_URL = "http://localhost:8000"
    private val http: HttpClient = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class PendingVote(
        val vote_id:     Int,
        val site_name:   String,
        val vote_points: Int,
    )

    fun startVote(player: Player, site: VoteSite) {
        ifEnabled(
            player,
            action = { user ->
                player.notify("Awaiting vote confirmation...", loading = true)
                val voteUrl = when(site) {
                    VoteSite.RUNELOCUS -> url {
                        protocol = URLProtocol.HTTPS
                        host = "www.runelocus.com"
                        path("top-rsps-list/near-reality-is-back/vote")
                        parameters.append("callback", user.id.toString())
                    }
                    VoteSite.RSPS_LIST -> url {
                        protocol = URLProtocol.HTTPS
                        host = "www.rsps-list.com"
                        path("index.php")
                        parameters.append("a", "in")
                        parameters.append("u", "nrpk")
                        parameters.append("id", user.id.toString())
                    }
                }
                player.packetDispatcher.sendURL(voteUrl)
            }
        )
    }

    fun requestStatuses(player: Player, onResponse: (Map<VoteSite, VoteSiteStatus>) -> Unit) {
        ifEnabled(player) { user ->
            VoteAPIService.requestStatuses(user) {
                WorldTasksManager.schedule {
                    onResponse(it)
                }
            }
        }
    }

    fun claimWebsiteVotes(player: Player) {
        val user = player.user
        if (user == null) {
            player.sendMessage("You must have a linked website account to use ::claimvote.")
            return
        }

        player.sendMessage("Checking for unclaimed votes…")

        Thread {
            try {
                val getResponse = http.send(
                    HttpRequest.newBuilder()
                        .uri(URI.create("$WEBSITE_API_URL/votes/pending/${user.id}"))
                        .GET()
                        .build(),
                    HttpResponse.BodyHandlers.ofString(),
                )

                if (getResponse.statusCode() == 503) {
                    WorldTasksManager.schedule {
                        player.sendMessage("The vote service is temporarily unavailable. Please try again later.")
                    }
                    return@Thread
                }

                if (getResponse.statusCode() != 200) {
                    WorldTasksManager.schedule {
                        player.sendMessage("Could not reach the vote service (HTTP ${getResponse.statusCode()}).")
                    }
                    return@Thread
                }

                val pendingVotes: List<PendingVote> = json.decodeFromString(getResponse.body())

                if (pendingVotes.isEmpty()) {
                    WorldTasksManager.schedule {
                        player.sendMessage("You have no unclaimed votes. Type ::vote to visit the vote page!")
                    }
                    return@Thread
                }

                val totalPoints = pendingVotes.sumOf { it.vote_points }
                val siteList    = pendingVotes.joinToString(" & ") { it.site_name }

                WorldTasksManager.schedule {
                    XamphurHandler.get().addVotes(player, totalPoints)
                    player.totalVoteCredits += totalPoints
                    player.lastVoteClaimTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12)
                    player.dialogue {
                        plain(
                            "Your vote${if (pendingVotes.size > 1) "s" else ""} on " +
                            "${Colour.DARK_BLUE.wrap(siteList)} have been confirmed!<br>" +
                            "You received ${Colour.DARK_BLUE.wrap("$totalPoints vote point${if (totalPoints != 1) "s" else ""}")}.<br>" +
                            "Thank you for supporting Zelus! <3"
                        )
                    }
                }

                for (vote in pendingVotes) {
                    try {
                        val claimResponse = http.send(
                            HttpRequest.newBuilder()
                                .uri(URI.create("$WEBSITE_API_URL/votes/claim/${vote.vote_id}"))
                                .POST(HttpRequest.BodyPublishers.noBody())
                                .build(),
                            HttpResponse.BodyHandlers.ofString(),
                        )
                        if (claimResponse.statusCode() != 200) {
                            logger.warn(
                                "::claimvote — could not mark vote ${vote.vote_id} as claimed " +
                                "(HTTP ${claimResponse.statusCode()}): ${claimResponse.body()}"
                            )
                        }
                    } catch (e: Exception) {
                        logger.error("::claimvote — exception marking vote ${vote.vote_id} as claimed", e)
                    }
                }
            } catch (e: Exception) {
                logger.error("::claimvote failed for player '${player.username}'", e)
                WorldTasksManager.schedule {
                    player.sendMessage("The vote service is currently unavailable. Please try again shortly.")
                }
            }
        }.start()
    }

    internal fun onVoteReceived(vote: Vote) {
        fun tryClaimVote(player: Player, vote: Vote, offline: Boolean) {
            WorldTasksManager.schedule {
                val votePoints = vote.votePointReward
                val votePointsWithBonus = (votePoints * VoteHandler.getVotePointsModifier(player)).toInt()
                val bonusVotePoints = votePointsWithBonus - votePoints
                GameLogger.log {
                    GameLogMessage.ClaimedVotes(username = player.username, votesClaimed = votePoints, votesBonus = bonusVotePoints)
                }
                if (votePointsWithBonus > 0) {
                    XamphurHandler.get().addVotes(player, votePoints)
                    AfkSkilling.addAfkTime(player, TimeUnit.HOURS.toMillis(12))
                    WiseOldManD.rollClues(player, votePointsWithBonus)
                    val coinRewardAmount = votePointsWithBonus * (if (player.authenticator.isEnabled) 150_000 else 75_000)
                    val coinRewardItem = Item(ItemId.COINS_995, coinRewardAmount)
                    player.inventory.addOrDrop(ItemId.TOME_OF_EXPERIENCE_30215, 2)
                    player.inventory.addOrDrop(coinRewardItem)
                    player.totalVoteCredits += votePointsWithBonus
                    player.lastVoteClaimTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12)
                    if (!offline) {
                        player.updateVoteStatisticOnPlayerDetailsTab()
                        player.dialogue {
                            plain(buildString {
                                append("Your vote on ${vote.siteType.fancyName} has been confirmed!")
                                append("<br>As a reward you received ${Colour.DARK_BLUE.wrap("$votePoints vote points${if (votePointsWithBonus > 1) "s" else ""}")}")
                                if (bonusVotePoints > 0)
                                    append(" (+${Colour.DARK_BLUE.wrap("$bonusVotePoints")} bonus)")
                                append("<br>and ${Colour.DARK_BLUE.wrap(StringFormatUtil.format(coinRewardAmount))} gold pieces.")
                                append("<br>Thank you for voting <3")
                            })
                        }
                    }
                    peopleVoted++
                    if (peopleVoted % 10 == 0) {
                        WorldBroadcasts.broadcast(null, BroadcastType.HELPFUL_TIP, "Another 10 people have just voted, type ::vote and get rewards!")
                    }
                }
            }
        }
        val user = vote.user
        val onlinePlayer = World.getPlayer(user.name)
        if (onlinePlayer.isPresent) {
            tryClaimVote(onlinePlayer.get(), vote, offline = false)
        } else {
            CoresManager.getLoginManager().load(true, vote.user.name) { offlinePlayer ->
                if (offlinePlayer.isPresent)
                    tryClaimVote(offlinePlayer.get(), vote, offline = true)
                else
                    logger.error("Failed to claim vote ${vote.id} for user: ${vote.user.name}, user not found.")
            }
        }
    }

    private fun ifEnabled(player: Player, action: (User) -> Unit) {
        when {
            !APIClient.enabled -> player.notify("You cannot vote on this world.", schedule = false)
            !VoteAPIService.enabled -> player.notify( "The voting service is currently unavailable.", schedule = false)
            World.isUpdating() -> player.notify("You cannot vote while the world is updating.", schedule = false)
            else -> {
                val user = player.user
                if (user != null)
                    action(user)
                else
                    player.notify("You must be logged in to vote.", schedule = false)
            }
        }
    }
}
