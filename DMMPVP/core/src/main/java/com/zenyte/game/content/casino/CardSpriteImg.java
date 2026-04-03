package com.zenyte.game.content.casino;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum CardSpriteImg {
    //Back Side of cards
    BACK_SIDE(3335, 345, "Back Side", 0),

    // Spades
    ACE_OF_SPADES(3334, 344, "Ace of Spades", 1, 11),
    TWO_OF_SPADES(3333, 343, "Two of Spades", 2),
    THREE_OF_SPADES(3332, 342, "Three of Spades", 3),
    FOUR_OF_SPADES(3331, 341, "Four of Spades", 4),
    FIVE_OF_SPADES(3330, 340, "Five of Spades", 5),
    SIX_OF_SPADES(3329, 339, "Six of Spades", 6),
    SEVEN_OF_SPADES(3328, 338, "Seven of Spades", 7),
    EIGHT_OF_SPADES(3327, 337, "Eight of Spades", 8),
    NINE_OF_SPADES(3326, 336, "Nine of Spades", 9),
    TEN_OF_SPADES(3325, 335, "Ten of Spades", 10),
    JACK_OF_SPADES(3324, 334, "Jack of Spades", 10),
    QUEEN_OF_SPADES(3323, 333, "Queen of Spades", 10),
    KING_OF_SPADES(3322, 332, "King of Spades", 10),

    // Diamonds
    ACE_OF_DIAMONDS(3321, 331, "Ace of Diamonds", 1, 11),
    TWO_OF_DIAMONDS(3320, 330, "Two of Diamonds", 2),
    THREE_OF_DIAMONDS(3319, 329, "Three of Diamonds", 3),
    FOUR_OF_DIAMONDS(3318, 328, "Four of Diamonds", 4),
    FIVE_OF_DIAMONDS(3317, 327, "Five of Diamonds", 5),
    SIX_OF_DIAMONDS(3316, 326, "Six of Diamonds", 6),
    SEVEN_OF_DIAMONDS(3315, 325, "Seven of Diamonds", 7),
    EIGHT_OF_DIAMONDS(3314, 324, "Eight of Diamonds", 8),
    NINE_OF_DIAMONDS(3313, 323, "Nine of Diamonds", 9),
    TEN_OF_DIAMONDS(3312, 322, "Ten of Diamonds", 10),
    JACK_OF_DIAMONDS(3311, 293, "Jack of Diamonds", 10),
    QUEEN_OF_DIAMONDS(3310, 321, "Queen of Diamonds", 10),
    KING_OF_DIAMONDS(3309, 320, "King of Diamonds", 10),

    // Clubs
    ACE_OF_CLUBS(3308, 319, "Ace of Clubs", 1, 11),
    TWO_OF_CLUBS(3307, 318, "Two of Clubs", 2),
    THREE_OF_CLUBS(3306, 317, "Three of Clubs", 3),
    FOUR_OF_CLUBS(3305, 316, "Four of Clubs", 4),
    FIVE_OF_CLUBS(3304, 315, "Five of Clubs", 5),
    SIX_OF_CLUBS(3303, 314, "Six of Clubs", 6),
    SEVEN_OF_CLUBS(3302, 313, "Seven of Clubs", 7),
    EIGHT_OF_CLUBS(3301, 312, "Eight of Clubs", 8),
    NINE_OF_CLUBS(3300, 311, "Nine of Clubs", 9),
    TEN_OF_CLUBS(3299, 310, "Ten of Clubs", 10),
    JACK_OF_CLUBS(3298, 309, "Jack of Clubs", 10),
    QUEEN_OF_CLUBS(3297, 308, "Queen of Clubs", 10),
    KING_OF_CLUBS(3296, 307, "King of Clubs", 10),

    // Hearts
    ACE_OF_HEARTS(3295, 306, "Ace of Hearts", 1, 11),
    TWO_OF_HEARTS(3294, 305, "Two of Hearts", 2),
    THREE_OF_HEARTS(3293, 304, "Three of Hearts", 3),
    FOUR_OF_HEARTS(3292, 303, "Four of Hearts", 4),
    FIVE_OF_HEARTS(3291, 302, "Five of Hearts", 5),
    SIX_OF_HEARTS(3290, 301, "Six of Hearts", 6),
    SEVEN_OF_HEARTS(3289, 300, "Seven of Hearts", 7),
    EIGHT_OF_HEARTS(3288, 299, "Eight of Hearts", 8),
    NINE_OF_HEARTS(3287, 298, "Nine of Hearts", 9),
    TEN_OF_HEARTS(3286, 294, "Ten of Hearts", 10),
    JACK_OF_HEARTS(3285, 297, "Jack of Hearts", 10),
    QUEEN_OF_HEARTS(3284, 296, "Queen of Hearts", 10),
    KING_OF_HEARTS(3283, 295, "King of Hearts", 10);


    private final int spriteId;
    private final int imageId;
    private final String cardName;
    private final int pointValue;
    private final int altPointValue;

    private static final List<CardSpriteImg> CARD_VALUES = Arrays.stream(values())
            .filter(card -> card != BACK_SIDE) // Exclude BACK_SIDE from selection
            .toList();

    private static final Random RANDOM = new Random();

    // Constructor for card with one point value
    CardSpriteImg(int spriteId, int imageId, String cardName, int pointValue) {
        this(spriteId, imageId, cardName, pointValue, pointValue); // Default point value is the same as the card's pointValue
    }

    // Constructor for card with two point values (e.g. Ace)
    CardSpriteImg(int spriteId, int imageId, String cardName, int pointValue, int altPointValue) {
        this.spriteId = spriteId;
        this.imageId = imageId;
        this.cardName = cardName;
        this.pointValue = pointValue;
        this.altPointValue = altPointValue;
    }

    public int getSpriteId() {
        return spriteId;
    }

    public int getImageId() {
        return imageId;
    }

    public String getCardName() {
        return cardName;
    }

    public int getPointValue(boolean useAlt) {
        return useAlt ? altPointValue : pointValue;
    }

    // New method to get a random card (excluding BACK_SIDE)
    public static CardSpriteImg getRandomCard() {
        return CARD_VALUES.get(RANDOM.nextInt(CARD_VALUES.size()));
    }

    // Get the card value
    public int getCardValue() {
        return pointValue;
    }
}
