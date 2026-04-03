package com.near_reality.game.content.universalshop

import com.zenyte.game.model.shop.ShopCurrency

enum class UnivShopCategory(
    val index: Int,
    val dbTable: Int,
    val prettyName: String,
    val currency: ShopCurrency = ShopCurrency.COINS,
    val canBuy: Boolean = true,
    val canSell: Boolean = false,
) {
    Melee(1, 1001, "Melee"),
    Ranged(2, 1008, "Ranged"),
    Magic(3, 1007, "Magic"),
    Supplies(4, 1011, "Supplies"),
    Skilling(5, 1010, "Skilling"),
    Jewelry(6, 1012, "Jewelry"),
    General(7, 1003, "General"),
    Slayer(8, 1004, "Slayer", currency = ShopCurrency.SLAYER_POINTS),
    BountyHunter(9, 1005, "Bounty Hunter", currency = ShopCurrency.BH_POINTS),
    Capes(10, 1006, "Capes"),
    BloodMoney(11, 1009, "Blood Money", currency = ShopCurrency.BLOOD_MONEY),
    Loyalty(12, 1013, "Loyalty", currency = ShopCurrency.LOYALTY_POINTS),
    Vote(13, 1014, "Vote", currency = ShopCurrency.VOTE_POINTS),
    Donator(14, 1015, "Donator", currency = ShopCurrency.DONOR_POINTS) // New Donator shop category
    ;

    val categoriesToIds = mapOf(
        1 to 1001, //Melee
        2 to 1008, //Ranged
        3 to 1007, //Magic
        4 to 1011, //Supplies
        5 to 1010, //Skilling
        6 to 1012, //Jewelry
        7 to 1003, //General
        8 to 1004, //Slayer
        9 to 1005, //BountyHunter
        10 to 1006, //Capes
        11 to 1009, //Blood Money
        12 to 1013, //Loyalty
        13 to 1014, //Vote
        14 to 1015, //Donator (matching dbTable ID for Donator shop)
    )
}