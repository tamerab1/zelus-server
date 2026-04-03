package com.near_reality.plugins.factionwars

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Schedules the weekly Faction War reset every **Sunday at 20:00** server time.
 *
 * Uses a single-thread [java.util.concurrent.ScheduledExecutorService] with:
 * - Initial delay  = milliseconds until the next Sunday 20:00
 * - Repeat period  = exactly 7 days (604 800 000 ms)
 *
 * The online player list is captured at fire time via [onlinePlayers].
 * Wire this lambda in [FactionPlugin.onServerLaunch].
 */
object FactionWarScheduler {

    private val log = LoggerFactory.getLogger(FactionWarScheduler::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val executor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "faction-war-scheduler").also { it.isDaemon = true }
    }

    /** Lambda that returns the current list of online players at the time of the reset. */
    var onlinePlayers: () -> List<com.zenyte.game.world.entity.player.Player> = { emptyList() }

    /**
     * Starts the scheduler.
     * Safe to call multiple times — subsequent calls are no-ops.
     */
    fun start() {
        val delayMs = millisUntilNextReset()
        val periodMs = TimeUnit.DAYS.toMillis(7)

        executor.scheduleAtFixedRate(::fireReset, delayMs, periodMs, TimeUnit.MILLISECONDS)

        val nextReset = LocalDateTime.now().plusNanos(delayMs * 1_000_000L)
        log.info(
            "FactionWarScheduler started. Next reset: {} (in {} ms)",
            nextReset, delayMs
        )
    }

    fun stop() {
        executor.shutdownNow()
    }

    // ── Reset execution ──────────────────────────────────────────────────────

    private fun fireReset() {
        log.info("Faction War weekly reset firing…")
        val snapshot = onlinePlayers()
        scope.launch {
            runCatching { FactionManager.resolveWeeklyWar(snapshot) }
                .onFailure { log.error("Error during faction war reset", it) }
        }
    }

    // ── Timing ──────────────────────────────────────────────────────────────

    /**
     * Returns the number of milliseconds from now until next Sunday at 20:00.
     * If it is already Sunday ≥ 20:00, schedules for the following Sunday.
     */
    private fun millisUntilNextReset(): Long {
        val now = LocalDateTime.now()
        var nextSunday = now
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .withHour(20).withMinute(0).withSecond(0).withNano(0)

        // If we're past this week's reset time, advance to next Sunday
        if (!nextSunday.isAfter(now)) {
            nextSunday = nextSunday.plusWeeks(1)
        }

        val nowMillis  = System.currentTimeMillis()
        val fireMillis = nextSunday
            .toInstant(java.time.ZoneOffset.UTC)
            .toEpochMilli()

        return (fireMillis - nowMillis).coerceAtLeast(0)
    }
}
