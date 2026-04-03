package com.zenyte.game.content.casino;

import com.zenyte.game.GameInterface;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.plugins.interfaces.BlackjackInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.zenyte.plugins.interfaces.BlackjackInterface.HOUSE_CARD_DISPLAY;

public class BlackjackGame {

    private Player player;
    private boolean playerPassedTurn = false;
    private boolean isGameReady = false;
    private boolean dealerTurn = false;
    private int betAmount;
    private List<CardSpriteImg> deck;
    private List<CardSpriteImg> playerHand;
    private List<CardSpriteImg> dealerHand;
    private int playerScore;
    private int dealerScore;
    private static final CardSpriteImg BACK_SIDE = CardSpriteImg.BACK_SIDE;

    private List<CardSpriteImg> getDeck() {
        return deck;
    }

    public boolean isDealerTurn() {
        dealerTurn = true; // Ensure dealerTurn is set to true
        return dealerTurn;
    }

    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }

    public int getBetAmount() {
        return this.betAmount;
    }

    public List<CardSpriteImg> getDealerHand() {
        return dealerHand;
    }

    public List<CardSpriteImg> getPlayerHand() {
        return playerHand;
    }

    public boolean isGameReady() {
        return isGameReady;
    }

    public boolean isPlayerPassedTurn() {
        return playerPassedTurn;
    }

    public void setGameReady(boolean gameReady) {
        isGameReady = gameReady;
    }

    public void playerStand() {
        playerPassedTurn = true;
        dealerHit(player);
    }

    public void handlePlayerStand() {
        playerStand();
    }

    private BlackjackGame(Player player) {
        this.player = player;
        this.deck = new ArrayList<>();
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        this.betAmount = 0;
        initializeDeck();
        shuffleDeck();
    }

    public static BlackjackGame createBlackjackGame(Player player) {
        return new BlackjackGame(player);
    }

    public void initializeBlackjackGame(Player player, int bet) {
        if (player.getBlackjackGame() == null) {
            player.setBlackjackGame(BlackjackGame.createBlackjackGame(player)); // Create the game
        }

        player.getBlackjackGame().setBetAmount(bet); // Set the bet after creation
        player.getBlackjackGame().startNewGame(bet);
        isGameReady = true;
    }

    private void initializeDeck() {
        for (CardSpriteImg card : CardSpriteImg.values()) {
            if (!card.equals(BACK_SIDE)) {
                deck.add(card);
            }
        }
    }

    private void shuffleDeck() {
        Collections.shuffle(deck);
    }

    public void startNewGame(int bet) {
        if (bet <= 0) {
            throw new IllegalArgumentException("Bet must be greater than zero.");
        }
        this.betAmount = bet;
        deck.clear();
        initializeDeck();
        shuffleDeck();
        playerHand.clear();
        dealerHand.clear();
        playerScore = 0;
        dealerScore = 0;
        playerHand.add(deck.remove(0));
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
        updateScores();
        BlackjackInterface blackjackInterface = this.player.getBlackjackInterface();
        blackjackInterface.updatePlayerHandDisplay(player);
        blackjackInterface.updateDealerHandDisplay(player, false);
    }

    public void playerHit() {
        BlackjackGame game = player.getBlackjackGame();
        if (game == null) return; // No game exists for this player

        if (game.playerScore < 21 && !game.deck.isEmpty()) {
            game.playerHand.add(game.deck.remove(0));
            game.updateScores();
        }
    }

    public void dealerHit(Player player) {
        BlackjackGame game = player.getBlackjackGame();
        if (game == null) return; // No game exists for this player
        if (!playerPassedTurn) {
            System.out.println("You need to place your bet first.");
            return;
        }
        if (dealerTurn) {
            if (dealerScore >= 17) {
                System.out.println("Dealer has 17 or more points, no need to draw a card.");
                return;
            }
            CardSpriteImg card = deck.remove(0);
            dealerHand.add(card);
            updateScores();
            int componentId = HOUSE_CARD_DISPLAY[dealerHand.size() - 1];
            player.getPacketDispatcher().sendComponentText(
                    GameInterface.BLACKJACK_INTERFACE.getId(),
                    componentId, "<img=" + card.getImageId() + ">"
            );
            System.out.println("Sending card to interface: " + card.getCardName() + " to component: " + componentId);
            while (dealerScore < 17 && !deck.isEmpty()) {
                card = deck.remove(0);
                dealerHand.add(card);
                updateScores();
                componentId = HOUSE_CARD_DISPLAY[dealerHand.size() - 1];
                player.getPacketDispatcher().sendComponentText(
                        GameInterface.BLACKJACK_INTERFACE.getId(),
                        componentId, "<img=" + card.getImageId() + ">"
                );
                System.out.println("Sending card to interface: " + card.getCardName() + " to component: " + componentId);
            }
        }
    }

    public void updateScores() {
        playerScore = calculateScore(playerHand);
        dealerScore = calculateScore(dealerHand);
        player.getPacketDispatcher().sendComponentText(GameInterface.BLACKJACK_INTERFACE, 59, Integer.toString(dealerScore));
    }

    public int getDealerScore() {
        return calculateScore(dealerHand);
    }

    public int getPlayerScore() {
        return calculateScore(playerHand);
    }

    private int calculateScore(List<CardSpriteImg> hand) {
        int score = 0;
        int aceCount = 0;
        for (CardSpriteImg card : hand) {
            int cardValue = card.getPointValue(true);
            score += cardValue;
            if (cardValue == 11) {
                aceCount++;
            }
        }
        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }
        return score;
    }

    public boolean isGameOver() {
        return playerScore >= 21 || dealerScore >= 21;
    }

    public String getWinner() {
        // Ensure dealer already hit before this method is called
        if (getPlayerScore() == 21) {
            player.getInventory().addItem(995, betAmount * 3);
            return "Blackjack! You win with a 21!";
        }
        if (getPlayerScore() > 21) {
            return "You busted! The house wins.";
        }
        if (dealerScore > 21) {
            player.getInventory().addItem(995, betAmount * 2);
            return "Dealer busted! You win!";
        }
        if (getPlayerScore() > dealerScore) {
            player.getInventory().addItem(995, betAmount * 2);
            return "You win!";
        }
        if (getPlayerScore() < dealerScore) {
            return "The house wins!";
        }
        player.getInventory().addItem(995, betAmount);
        return "It's a tie!";
    }


    public void endGame() {
        playerHand.clear();
        dealerHand.clear();
        playerScore = 0;
        dealerScore = 0;
        deck.clear();
        initializeDeck();
        shuffleDeck();
    }
}
