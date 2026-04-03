package com.near_reality.plugins.bountyboard

import com.near_reality.api.dao.Db
import com.zenyte.game.GameInterface
import com.zenyte.game.model.ui.Interface
import com.zenyte.game.util.AccessMask
import com.zenyte.game.world.entity.attribute
import com.zenyte.game.world.entity.player.Player
import com.zenyte.plugins.dialogue.StringDialogue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sortedBy
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

// ── Per-player interface state ───────────────────────────────────────────────

private var Player.bbTab: BountyBoardTab by attribute("bb_tab", BountyBoardTab.ACTIVE)
private var Player.bbPage: Int            by attribute("bb_page", 0)
private var Player.bbPendingTarget: String by attribute("bb_pending_target", "")
private var Player.bbPendingAmount: Int   by attribute("bb_pending_amount", 0)

internal enum class BountyBoardTab { ACTIVE, MINE, PLACE }

// ── Component ID constants ───────────────────────────────────────────────────
// These match the cache definition for interface 5200.
// See docs/bounty_board_interface.md for the full layout spec.

private object C {
    // ── Fixed / always-visible ───────────────────────────────────────────
    const val TITLE          = 1   // Text: "Bounty Board"
    const val CLOSE          = 2   // Button: close interface
    const val TAB_ACTIVE     = 3   // Button: "Active Bounties" tab
    const val TAB_MINE       = 4   // Button: "My Bounties" tab
    const val TAB_PLACE      = 5   // Button: "Place Bounty" tab
    const val PAGE_INFO      = 6   // Text: "Page 1/3  •  47 active bounties"
    const val BTN_PREV       = 7   // Button: previous page
    const val BTN_NEXT       = 8   // Button: next page

    // ── List view column headers ─────────────────────────────────────────
    const val HDR_TARGET     = 9
    const val HDR_POSTER     = 10
    const val HDR_AMOUNT     = 11
    const val HDR_TIME       = 12

    // ── List rows  (ROW_STRIDE components each, ROWS_PER_PAGE rows) ──────
    // Layout per row starting at ROW_BASE + (rowIndex * ROW_STRIDE):
    //   +0  select / click area
    //   +1  target name
    //   +2  poster name
    //   +3  amount text
    //   +4  time-ago text
    const val ROW_BASE       = 13
    const val ROW_STRIDE     = 5
    const val ROWS_PER_PAGE  = 15

    // ── Place-bounty form ────────────────────────────────────────────────
    const val FORM_TARGET_LBL  = 88   // Text: "Target:"
    const val FORM_TARGET_VAL  = 89   // Text: current pending target (updates live)
    const val FORM_TARGET_BTN  = 90   // Button: "Set Target"
    const val FORM_AMOUNT_LBL  = 91   // Text: "Amount (Blood Money):"
    const val FORM_AMOUNT_VAL  = 92   // Text: current pending amount
    const val FORM_AMOUNT_BTN  = 93   // Button: "Set Amount"
    const val FORM_CONFIRM     = 94   // Button: "Place Bounty  ▶"
    const val FORM_CLEAR       = 95   // Button: "Clear"
    const val FORM_MIN_INFO    = 96   // Text: "Min 500 / Max 500,000 Blood Money"

    // Convenience: total component span used for CLICK_OP1 on list rows
    val ROW_CLICK_START = ROW_BASE
    val ROW_CLICK_END   = ROW_BASE + (ROWS_PER_PAGE * ROW_STRIDE)
}

// ── Data class used when rendering rows ─────────────────────────────────────

private data class BountyRow(
    val targetName: String,
    val posterName: String,
    val amount: Int,
    val placedAt: LocalDateTime,
)

// ── Interface class ──────────────────────────────────────────────────────────

/**
 * Custom Bounty Board interface (interface ID 5200).
 *
 * ## Tabs
 * | Tab | Shows |
 * |-----|-------|
 * | Active Bounties | Paginated table of all live bounties; click a row to see detail |
 * | My Bounties     | Same table filtered to bounties *you* placed |
 * | Place Bounty    | Two-field form: enter target name + amount, then confirm |
 *
 * ## Cache requirement
 * Interface 5200 must be defined in the cache before this code is useful.
 * See `docs/bounty_board_interface.md` for the complete component layout.
 */
class BountyBoardInterface : Interface() {

    private val scope = CoroutineScope(Dispatchers.IO)

    // ── attach() — map component IDs to logical names ────────────────────────

    override fun attach() {
        put(C.CLOSE,         "close")
        put(C.TAB_ACTIVE,    "tab_active")
        put(C.TAB_MINE,      "tab_mine")
        put(C.TAB_PLACE,     "tab_place")
        put(C.BTN_PREV,      "prev_page")
        put(C.BTN_NEXT,      "next_page")
        put(C.ROW_BASE,      "list_rows")     // container — slots 0..(ROWS_PER_PAGE*ROW_STRIDE)
        put(C.FORM_TARGET_BTN, "set_target")
        put(C.FORM_AMOUNT_BTN, "set_amount")
        put(C.FORM_CONFIRM,  "confirm_bounty")
        put(C.FORM_CLEAR,    "clear_form")
    }

    // ── build() — bind click handlers ───────────────────────────────────────

    override fun build() {

        bind("close") { player ->
            player.interfaceHandler.closeInterface(this)
        }

        bind("tab_active") { player, _, _, _ ->
            player.bbTab  = BountyBoardTab.ACTIVE
            player.bbPage = 0
            refreshList(player)
        }

        bind("tab_mine") { player, _, _, _ ->
            player.bbTab  = BountyBoardTab.MINE
            player.bbPage = 0
            refreshList(player)
        }

        bind("tab_place") { player, _, _, _ ->
            player.bbTab = BountyBoardTab.PLACE
            refreshForm(player)
        }

        bind("prev_page") { player, _, _, _ ->
            if (player.bbPage > 0) {
                player.bbPage--
                refreshList(player)
            }
        }

        bind("next_page") { player, _, _, _ ->
            player.bbPage++
            refreshList(player)
        }

        bind("list_rows") { player, slotId, _, _ ->
            val rowIndex = slotId / C.ROW_STRIDE
            if (rowIndex < 0 || rowIndex >= C.ROWS_PER_PAGE) return@bind
            onRowClicked(player, rowIndex)
        }

        bind("set_target") { player ->
            player.awaitInputString(TargetNameDialogue())
        }

        bind("set_amount") { player ->
            player.awaitInputString(AmountDialogue())
        }

        bind("confirm_bounty") { player ->
            val target = player.bbPendingTarget
            val amount = player.bbPendingAmount
            if (target.isBlank()) {
                player.sendMessage("<col=ff4500>[Bounty Board] Please enter a target name first.</col>")
                return@bind
            }
            if (amount <= 0) {
                player.sendMessage("<col=ff4500>[Bounty Board] Please enter an amount first.</col>")
                return@bind
            }
            player.interfaceHandler.closeInterface(this)
            BountyManager.placeBounty(player, target, amount)
            // Reset form
            player.bbPendingTarget = ""
            player.bbPendingAmount = 0
        }

        bind("clear_form") { player ->
            player.bbPendingTarget = ""
            player.bbPendingAmount = 0
            refreshForm(player)
        }
    }

    // ── open() — initial load ────────────────────────────────────────────────

    override fun open(player: Player) {
        super.open(player)

        // Enable clicks on list row container
        player.packetDispatcher.sendComponentSettings(
            getId(), getComponent("list_rows"),
            0, C.ROWS_PER_PAGE * C.ROW_STRIDE,
            AccessMask.CLICK_OP1
        )

        // "Place-Bounty" right-click sets this flag from BountyBoardObject (no cross-module import needed)
        val prePlaceTab = player.temporaryAttributes.remove("bb_pre_place") == true
        player.bbPage = 0
        if (prePlaceTab) {
            player.bbTab = BountyBoardTab.PLACE
            refreshForm(player)
        } else {
            player.bbTab = BountyBoardTab.ACTIVE
            refreshList(player)
        }
    }

    override fun getInterface(): GameInterface = GameInterface.BOUNTY_BOARD


    // ── List rendering ───────────────────────────────────────────────────────

    /**
     * Fetches bounties from the DB and populates the 15 list rows via
     * [sendComponentText].  Runs the DB query on IO then jumps back to the
     * game thread to send packets.
     */
    private fun refreshList(player: Player) {
        scope.launch {
            val tab      = player.bbTab
            val page     = player.bbPage
            val offset   = page * C.ROWS_PER_PAGE

            val (rows, total) = Db.dbQueryMain {
                val query = ActiveBounties.selectAll().where {
                    when (tab) {
                        BountyBoardTab.MINE   ->
                            (ActiveBounties.posterName eq player.name.lowercase()) and
                            (ActiveBounties.active eq true)
                        else ->
                            ActiveBounties.active eq true
                    }
                }.sortedBy { ActiveBounties.amount }.reversed()

                val total = query.count()
                val rows = query.drop(offset).take(C.ROWS_PER_PAGE).map { row ->
                    BountyRow(
                        targetName = capitalize(row[ActiveBounties.targetName]),
                        posterName = capitalize(row[ActiveBounties.posterName]),
                        amount     = row[ActiveBounties.amount],
                        placedAt   = row[ActiveBounties.placedAt],
                    )
                }
                rows to total
            }

            // Jump to game thread for packet sending
            withContext(Dispatchers.Default) {
                if (!player.isOnline) return@withContext

                val totalPages = ((total - 1) / C.ROWS_PER_PAGE + 1).coerceAtLeast(1)

                // Page info
                player.packetDispatcher.sendComponentText(
                    getId(), C.PAGE_INFO,
                    "Page ${page + 1}/$totalPages  •  $total active bounties"
                )

                // Populate rows
                for (i in 0 until C.ROWS_PER_PAGE) {
                    val base = C.ROW_BASE + i * C.ROW_STRIDE
                    if (i < rows.size) {
                        val b = rows[i]
                        player.packetDispatcher.sendComponentText(getId(), base + 1, b.targetName)
                        player.packetDispatcher.sendComponentText(getId(), base + 2, b.posterName)
                        player.packetDispatcher.sendComponentText(
                            getId(), base + 3,
                            formatAmount(b.amount)
                        )
                        player.packetDispatcher.sendComponentText(
                            getId(), base + 4,
                            timeAgo(b.placedAt)
                        )
                    } else {
                        // Clear empty rows
                        for (offset in 1..4) {
                            player.packetDispatcher.sendComponentText(getId(), base + offset, "")
                        }
                    }
                }
            }
        }
    }

    // ── Form rendering ───────────────────────────────────────────────────────

    private fun refreshForm(player: Player) {
        val targetDisplay = if (player.bbPendingTarget.isBlank()) "<col=aaaaaa>Not set</col>"
                            else "<col=ffffff>${capitalize(player.bbPendingTarget)}</col>"
        val amountDisplay = if (player.bbPendingAmount <= 0) "<col=aaaaaa>Not set</col>"
                            else "<col=ffd700>${formatAmount(player.bbPendingAmount)} Blood Money</col>"

        with(player.packetDispatcher) {
            sendComponentText(getId(), C.FORM_TARGET_VAL, targetDisplay)
            sendComponentText(getId(), C.FORM_AMOUNT_VAL, amountDisplay)
            sendComponentText(getId(), C.FORM_MIN_INFO,
                "<col=aaaaaa>Min: 500  |  Max: 500,000 Blood Money</col>")
        }
    }

    // ── Row click handler ────────────────────────────────────────────────────

    private fun onRowClicked(player: Player, rowIndex: Int) {
        scope.launch {
            val offset = player.bbPage * C.ROWS_PER_PAGE + rowIndex
            val row = Db.dbQueryMain {
                ActiveBounties.selectAll()
                    .where { ActiveBounties.active eq true }
                    .sortedBy { ActiveBounties.amount }.reversed()
                    .drop(offset).firstOrNull()
            } ?: return@launch

            val target = capitalize(row[ActiveBounties.targetName])
            val amount = row[ActiveBounties.amount]
            val poster = capitalize(row[ActiveBounties.posterName])

            withContext(Dispatchers.Default) {
                player.sendMessage(
                    "<col=ffd700>[Bounty Board] Target: <col=ffffff>$target</col>" +
                    "<col=ffd700>  |  Reward: <col=ff4500>${formatAmount(amount)} Blood Money</col>" +
                    "<col=ffd700>  |  Posted by: <col=ffffff>$poster</col>"
                )
            }
        }
    }

    // ── Input dialogue classes ───────────────────────────────────────────────

    private inner class TargetNameDialogue : StringDialogue {
        override fun run(input: String) {}
        override fun execute(player: Player, input: String) {
            if (input.isBlank() || input.length > 12) {
                player.sendMessage("<col=ff4500>[Bounty Board] Invalid player name.</col>")
            } else {
                player.bbPendingTarget = input.trim()
            }
            refreshForm(player)
            GameInterface.BOUNTY_BOARD.open(player)
        }
    }

    private inner class AmountDialogue : StringDialogue {
        override fun run(input: String) {}
        override fun execute(player: Player, input: String) {
            val amount = input.trim().toIntOrNull()
            if (amount == null || amount <= 0) {
                player.sendMessage("<col=ff4500>[Bounty Board] Please enter a valid number.</col>")
            } else {
                player.bbPendingAmount = amount
            }
            refreshForm(player)
            GameInterface.BOUNTY_BOARD.open(player)
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun capitalize(name: String) = name.replaceFirstChar { it.uppercaseChar() }

    private fun formatAmount(amount: Int): String = when {
        amount >= 1_000_000 -> "${amount / 1_000_000}M"
        amount >= 1_000     -> "${amount / 1_000}K"
        else                -> amount.toString()
    }

    private fun timeAgo(dt: LocalDateTime): String {
        val now = LocalDateTime.now()
        val minutes = ChronoUnit.MINUTES.between(dt, now)
        return when {
            minutes < 1    -> "Just now"
            minutes < 60   -> "${minutes}m ago"
            minutes < 1440 -> "${minutes / 60}h ago"
            else           -> "${minutes / 1440}d ago"
        }
    }
}
