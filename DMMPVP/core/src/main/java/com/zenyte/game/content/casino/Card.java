package com.zenyte.game.content.casino;

public class Card {
    private final Suit suit;
    private final Rank rank;
    private final CardSpriteImg spriteImg;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.spriteImg = CardSpriteImg.valueOf(rank.name() + "_OF_" + suit.name()); // Create a CardSpriteImg from the card's rank and suit
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public CardSpriteImg getSpriteImg() {
        return spriteImg;
    }

    // Get the card's image ID from the CardSpriteImg enum
    public int getImageId() {
        return spriteImg.getImageId();
    }

    // Get the point value for this card
    public int getPointValue(boolean useAlt) {
        return spriteImg.getPointValue(useAlt);
    }

    @Override
    public String toString() {
        return rank.name() + " of " + suit.name();
    }
}

