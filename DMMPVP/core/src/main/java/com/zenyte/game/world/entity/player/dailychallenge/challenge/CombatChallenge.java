package com.zenyte.game.world.entity.player.dailychallenge.challenge;

import com.zenyte.game.world.entity.player.dailychallenge.ChallengeCategory;
import com.zenyte.game.world.entity.player.dailychallenge.ChallengeDifficulty;
import com.zenyte.game.world.entity.player.dailychallenge.reward.ChallengeReward;
import com.zenyte.game.world.entity.player.dailychallenge.reward.impl.ItemReward;
import org.jetbrains.annotations.NotNull;

import static com.zenyte.game.world.entity.player.dailychallenge.ChallengeDifficulty.*;

/**
 * @author Tommeh | 04/05/2019 | 13:33
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public enum CombatChallenge implements DailyChallenge {

    KILL_5_PLAYERS(
            "5 player kills in wilderness",
            EASY,
            5,
            "player",
            new ItemReward(13307, 2500) // 2500 BM
    ),

    KILL_10_PLAYERS(
            "10 player kills in wilderness",
            MEDIUM,
            10,
            "player",
            new ItemReward(13307, 6000) // 6000 BM
    ),

    KILL_20_PLAYERS(
            "20 player kills in wilderness",
            HARD,
            20,
            "player",
            new ItemReward(13307, 15000) // 15k BM
    ),

    KILL_50_PLAYERS(
            "50 player kills in wilderness",
            ELITE,
            50,
            "player",
            new ItemReward(13307, 50000) // 50k BM
    );

    private final String name;
    private final ChallengeDifficulty difficulty;
    private final ChallengeReward[] rewards;
    private final int length;
    private final String npc;

    CombatChallenge(final String name,
                    final ChallengeDifficulty difficulty,
                    final int length,
                    final String npc,
                    final ChallengeReward... rewards) {
        this.name = name;
        this.difficulty = difficulty;
        this.length = length;
        this.npc = npc;
        this.rewards = rewards;
    }

    public static final CombatChallenge[] all = values();

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public ChallengeCategory getCategory() {
        return ChallengeCategory.COMBAT;
    }

    @NotNull
    @Override
    public ChallengeDifficulty getDifficulty() {
        return difficulty;
    }

    @NotNull
    @Override
    public ChallengeReward[] getRewards() {
        return rewards;
    }

    @Override
    public int getSkill() {
        return 0; // placeholder zodat UI niet crasht
    }

    @Override
    public int getLength() {
        return length;
    }

    public String getNpc() {
        return npc;
    }
}
