package com.zenyte.plugins.interfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.content.casino.CardSpriteImg;
import com.zenyte.game.content.casino.BlackjackGame;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.packet.PacketDispatcher;
import com.zenyte.game.task.WorldTask;
import com.zenyte.game.task.WorldTasksManager;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.container.impl.GameShop;
import com.zenyte.plugins.item.Pack;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class BlackjackInterface extends Interface {

    // Buttons
    private static final int HIT = 8;
    private static final int STAND = 30;
    private static final int PLACE = 27;
    private static final int CLEAR = 48;
    private static final int RESTART = 51;
    private static final int SPLIT_PAIRS = 54;
    private Player player;

    public static final int TOTAL_BET_DISPLAY = 6;
    public static final int TIMER_COMPONENT_ID = 89;

    // Components for player cards
    public static final int[] CARD_DISPLAY_PLAYER = {38, 40, 42, 44, 46, 71};
    public static final int[] SPLIT_DISPLAY_PLAYER = {73, 75, 77, 79, 81, 83};
    // Components for dealer cards
    public static final int[] HOUSE_CARD_DISPLAY = {34, 36, 65, 67, 69, 85};

    // Components for scores
    public static final int HOUSE_POINTS_DISPLAY = 59;
    public static final int PLAYER_POINTS_DISPLAY = 63;

    // Components for chips
    private static final int CHIP_100K = 12;
    private static final int CHIP_500K = 15;
    private static final int CHIP_1M = 18;
    private static final int CHIP_10M = 21;
    private static final int CHIP_100M = 24;

    @Override
    protected void attach() {
        put(HIT, "Hit");
        put(PLACE, "Place");
        put(STAND, "Stand");
        put(SPLIT_PAIRS, "Split Pairs");
        put(CLEAR, "Clear Bet");
        put(RESTART, "Restart");
        put(CHIP_100K, "Select 100k Chip");
        put(CHIP_500K, "Select 500k Chip");
        put(CHIP_1M, "Select 1m Chip");
        put(CHIP_10M, "Select 10m Chip");
        put(CHIP_100M, "Select 100m Chip");
    }
    //private boolean isGameReady = false;


    @Override
    public void open(Player player) {
        player.getInterfaceHandler().sendInterface(getInterface());
        if (player.getBlackjackGame() == null) {
            player.setBlackjackGame(BlackjackGame.createBlackjackGame(player));
        }
    }





    @Override
    protected void build() {
        bind("Hit", this::hitAction);
        bind("Place", this::placeAction);
        bind("Stand", this::standAction);
        bind("Split Pairs", this::splitAction);
        bind("Clear Bet", this::clearAction);
        bind("Restart", this::restartAction);
        bind("Select 100k Chip", player -> selectChip(player, 100_000));
        bind("Select 500k Chip", player -> selectChip(player, 500_000));
        bind("Select 1m Chip", player -> selectChip(player, 1_000_000));
        bind("Select 10m Chip", player -> selectChip(player, 10_000_000));
        bind("Select 100m Chip", player -> selectChip(player, 100_000_000));
    }

    private void hitAction(Player player) {

        if (player.getBlackjackGame() == null || player.getBlackjackGame().isGameOver() || !player.getBlackjackGame().isGameReady()) {
            player.sendMessage("You must place a bet first!");
            return;
        }
        if (player.getBlackjackGame().isPlayerPassedTurn()) {
            player.sendMessage("You have already passed your turn. The dealer is now playing.");
            return;  // Return early, preventing further action
        }

        player.getBlackjackGame().playerHit();  // Add card to player hand
        player.getBlackjackGame().updateScores();  // Update player and dealer scores
        if (player.getBlackjackGame().getPlayerScore() == 21) {
            player.getBlackjackGame().isGameOver();  // Mark the game as over
            player.sendMessage(player.getBlackjackGame().getWinner());  // Notify the player of the result
        }
        // Check if player has gone over 21 points (busted)
        if (player.getBlackjackGame().getPlayerScore() > 21) {
            player.sendMessage("You busted! The house wins.");
            player.getBlackjackGame().isGameOver();  // End the game
            updatePlayerHand(player);  // Update player hand after bust
            updatePlayerHandDisplay(player);
        } else {
            // Update player hand and continue the game
            updatePlayerHand(player);
            updatePlayerHandDisplay(player);
            if (player.getBlackjackGame().isGameOver()) {
                player.sendMessage(player.getBlackjackGame().getWinner());
            }
        }
    }
    private void placeAction(Player player) {
        if (player.getBlackjackGame() == null) {
            player.setBlackjackGame(BlackjackGame.createBlackjackGame(player));
        }
        BlackjackGame blackjackGame = player.getBlackjackGame();
        if (blackjackGame.isGameReady()) {
            player.sendMessage("The game is already in progress.");
            return;
        }
        if (!validateBet(player)) {
            return;
        }
        int betAmount = blackjackGame.getBetAmount();
        blackjackGame.initializeBlackjackGame(player, betAmount);
        updatePlayerHandDisplay(player);
        player.getPacketDispatcher().sendComponentText(GameInterface.BLACKJACK_INTERFACE, HOUSE_CARD_DISPLAY[1], "<img=345>");
        updateBetAndScores(player);
        if (player.getBlackjackGame().getPlayerScore() == 21) {
            player.getBlackjackGame().isGameOver();
            player.sendMessage(player.getBlackjackGame().getWinner());
            player.getBlackjackGame().updateScores();
            updateDealerHand(player, true);
            updateDealerHandDisplay(player,true);
        }
    }


    private void standAction(Player player) {

        if (player.getBlackjackGame() == null || player.getBlackjackGame().isGameOver()) {
            player.sendMessage("You are not in a game.");
            return;
        }
        // Check if the player has already passed their turn (stood)
        if (player.getBlackjackGame().isPlayerPassedTurn()) {
            player.sendMessage("It's the dealer's turn.");
            return;
        }
        player.getBlackjackGame().isDealerTurn();
        player.getBlackjackGame().handlePlayerStand();  // Set the flag to true indicating the player has stood
        player.getBlackjackGame().dealerHit(player);  // Dealer keeps hitting until their score is at least 17
        player.getBlackjackGame().isGameOver();  // Mark the game as over
        player.sendMessage(player.getBlackjackGame().getWinner());  // Notify the player of the result
    }

    private void splitAction(Player player) {

    }

    private void clearAction(Player player) {
        BlackjackGame blackjackGame = player.getBlackjackGame();

        if (blackjackGame == null || blackjackGame.isGameOver() || blackjackGame.isGameReady()) {
            player.sendMessage("You can't clear your bet anymore.");
            return;
        }
        int betAmount = blackjackGame.getBetAmount();

        if (betAmount > 0) {
            player.getInventory().addItem(995, betAmount);
        }
        blackjackGame.setBetAmount(0);
        updateTotalBetDisplay(player);
        updateDealerHand(player, true);
        player.sendMessage("Bet cleared. The game has been reset.");
        blackjackGame.setGameReady(false);
    }
    private void restartAction(Player player) {
        BlackjackGame blackjackGame = player.getBlackjackGame();
        if (player.getBlackjackGame() == null || !(player.getBlackjackGame().isGameReady())) {
            player.sendMessage("You cannot restart now.");
            return;
        }
        for (int i = 0; i < 6; i++) {
            clearCardDisplay(player, getCardDisplayComponent(i));
            clearCardDisplay(player, getHouseCardDisplayComponent(i));
        }
        player.getPacketDispatcher().sendComponentText(
                GameInterface.BLACKJACK_INTERFACE.getId(),
                PLAYER_POINTS_DISPLAY, "0"
        );
        player.getPacketDispatcher().sendComponentText(
                GameInterface.BLACKJACK_INTERFACE.getId(),
                HOUSE_POINTS_DISPLAY, "0"
        );
        //isGameReady = false;
        blackjackGame.setBetAmount(0);
        updateTotalBetDisplay(player);
        player.getBlackjackGame().endGame();
        player.setBlackjackGame(BlackjackGame.createBlackjackGame(player));
        player.sendMessage("The game has been restarted. Please place your bets");
    }
    private boolean validateBet(Player player) {
        BlackjackGame blackjackGame = player.getBlackjackGame();
        if (blackjackGame == null) {
            player.sendMessage("You are not currently in a game.");
            return false;
        }

        // Only check if betAmount is greater than 0 (inventory was already checked when placing the bet)
        if (blackjackGame.getBetAmount() <= 0) {
            player.sendMessage("You must place a bet first!");
            return false;
        }

        return true;
    }

    private void updateBetAndScores(Player player) {
        updateTotalBetDisplay(player);
        player.getBlackjackGame().updateScores();
    }
    private void selectChip(Player player, int chipValue) {
        BlackjackGame blackjackGame = player.getBlackjackGame();
        if (blackjackGame == null) {
            player.sendMessage("You are not currently in a game.");
            return;
        }
        if (blackjackGame.isGameReady()) {
            player.sendMessage("You cannot place a bet while the game is in progress!");
            return;
        }
        if (!player.getInventory().containsItem(995, chipValue)) {
            player.sendMessage("You don't have enough coins to place this bet.");
            return;
        }

        blackjackGame.setBetAmount(blackjackGame.getBetAmount() + chipValue);
        updateTotalBetDisplay(player);
        player.getInventory().deleteItem(995, chipValue);
        player.sendMessage("You added " + chipValue + " to your bet. Total Bet: " + blackjackGame.getBetAmount());
    }



    public void updatePlayerHands(Player player) {
        player.getPacketDispatcher().sendComponentText(GameInterface.BLACKJACK_INTERFACE,
                CARD_DISPLAY_PLAYER[player.getBlackjackGame().getPlayerHand().size()],
                player.getBlackjackGame().getPlayerHand().size());
    }

    private void updateTotalBetDisplay(Player player) {
        BlackjackGame blackjackGame = player.getBlackjackGame();
        if (blackjackGame == null) {
            player.sendMessage("You must start a game first.");
            return;
        }
        String formattedAmount = formatLargeNumber(blackjackGame.getBetAmount());
        player.getPacketDispatcher().sendComponentText(
                GameInterface.BLACKJACK_INTERFACE.getId(), TOTAL_BET_DISPLAY, formattedAmount
        );
    }

    private String formatLargeNumber(int number) {
        if (number >= 1_000_000_000) {
            return String.format("%dB", number / 1_000_000_000); // For billions
        } else if (number >= 1_000_000) {
            return String.format("%dM", number / 1_000_000); // For millions
        } else if (number >= 1_000) {
            return String.format("%dK", number / 1_000); // For thousands
        } else {
            return String.valueOf(number);
        }
    }
    public void updatePlayerHandDisplay(Player player) {
        if (player.getBlackjackGame() == null) {
            player.sendMessage("Blackjack game is not initialized.");
            return;
        }

        // Call the new method to update player hand
        updatePlayerHand(player);

        // Update the player's score display
        player.getPacketDispatcher().sendComponentText(
                GameInterface.BLACKJACK_INTERFACE.getId(),
                PLAYER_POINTS_DISPLAY,
                String.valueOf(player.getBlackjackGame().getPlayerScore())
        );
    }
    public void updateDealerHandDisplay(Player player, boolean isClearing) {
        if (player.getBlackjackGame() == null) {
            player.sendMessage("Blackjack game is not initialized.");
            return;
        }
        // Call the new method to update dealer hand
        updateDealerHand(player, isClearing);
        // Update the dealer's score display
        player.getPacketDispatcher().sendComponentText(
                GameInterface.BLACKJACK_INTERFACE.getId(),
                HOUSE_POINTS_DISPLAY,
                String.valueOf(player.getBlackjackGame().getDealerScore())
        );
    }

    public void updatePlayerHand(Player player) {
        // Check if blackjackGame is initialized
        if (player.getBlackjackGame() == null) {
            player.sendMessage("Blackjack game is not initialized.");
            return;
        }

        // Get the player's hand size
        int playerHandSize = player.getBlackjackGame().getPlayerHand() != null ? player.getBlackjackGame().getPlayerHand().size() : 0;

        // Update player's hand (up to 6 cards)
        for (int i = 0; i < Math.min(playerHandSize, 6); i++) {
            CardSpriteImg card = player.getBlackjackGame().getPlayerHand().get(i);
            if (card != null) {
                updateCardDisplay(player, getCardDisplayComponent(i), card); // Update the player's card display
            }
        }
    }
    public void updateDealerHand(Player player, boolean isClearing) {
        // Check if blackjackGame is initialized
        if (player.getBlackjackGame() == null) {
            player.sendMessage("Blackjack game is not initialized.");
            return;
        }
        // Get the dealer's hand size
        int houseHandSize = player.getBlackjackGame().getDealerHand() != null ? player.getBlackjackGame().getDealerHand().size() : 0;
        // Update dealer's hand (up to 6 cards)
        for (int i = 0; i < Math.min(houseHandSize, 6); i++) {
            CardSpriteImg houseCard = player.getBlackjackGame().getDealerHand().get(i);
            if (houseCard != null) {
                int componentId = getHouseCardDisplayComponent(i);

                // If clearing the action, don't update the second dealer card to the backside
                if (i == 1 && !player.getBlackjackGame().isGameOver() && !isClearing) {
                    if (player.getBlackjackGame().isPlayerPassedTurn()) {
                        CardSpriteImg randomCard = getRandomCardSprite();
                        updateCardDisplay(player, componentId, randomCard);
                    } else {
                        updateCardDisplay(player, componentId, CardSpriteImg.BACK_SIDE);
                    }
                } else {
                    updateCardDisplay(player, componentId, houseCard); // Otherwise, show the dealer's actual card
                }
            }
        }
    }


    /**
     * Generates a random card sprite ID for the dealer's hidden card.
     */
    private CardSpriteImg getRandomCardSprite() {
        CardSpriteImg[] allCards = CardSpriteImg.values();
        return allCards[(int) (Math.random() * allCards.length)];
    }

    private void updateCardDisplay(Player player, int componentId, CardSpriteImg card) {
        if (componentId != -1) {
            player.getPacketDispatcher().sendComponentSprite(
                    GameInterface.BLACKJACK_INTERFACE.getId(),
                    componentId,
                    card.getSpriteId()   // use spriteId, not imageId
            );
        }
    }

    private void clearCardDisplay(Player player, int componentId) {
        if (componentId != -1) {
            player.getPacketDispatcher().sendComponentSprite(
                    GameInterface.BLACKJACK_INTERFACE.getId(),
                    componentId,
                    -1   // removes the sprite from that component
            );
        }
    }

    private int getCardDisplayComponent(int index) {
        return index >= 0 && index < CARD_DISPLAY_PLAYER.length ? CARD_DISPLAY_PLAYER[index] : -1;
    }
    private int getHouseCardDisplayComponent(int index) {
        return index >= 0 && index < HOUSE_CARD_DISPLAY.length ? HOUSE_CARD_DISPLAY[index] : -1;
    }
    @Override
    public GameInterface getInterface() {
        return GameInterface.BLACKJACK_INTERFACE;
    }
    @Override
    public void close(Player player, Optional<GameInterface> replacement) {
        BlackjackGame game = player.getBlackjackGame();
        if (game != null) {
            // Refund ONLY if the game never started (bet placed but no cards dealt)
            if (!game.isGameReady() && game.getBetAmount() > 0) {
                player.getInventory().addItem(995, game.getBetAmount());
                player.sendMessage("Your bet has been refunded.");
            }

            // Reset the game
            game.setBetAmount(0);
            game.endGame();
            player.setBlackjackGame(null);
        }

        // Clear interface visuals
        for (int i = 0; i < 6; i++) {
            clearCardDisplay(player, getCardDisplayComponent(i));
            clearCardDisplay(player, getHouseCardDisplayComponent(i));
        }
        player.getPacketDispatcher().sendComponentText(GameInterface.BLACKJACK_INTERFACE.getId(), PLAYER_POINTS_DISPLAY, "0");
        player.getPacketDispatcher().sendComponentText(GameInterface.BLACKJACK_INTERFACE.getId(), HOUSE_POINTS_DISPLAY, "0");
        updateTotalBetDisplay(player);

        player.sendMessage("Your Blackjack session has been reset.");
    }


}