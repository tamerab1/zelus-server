package com.near_reality.plugins.shops

import com.near_reality.scripts.shops.ShopScript
import com.zenyte.game.model.shop.ShopCurrency
import com.zenyte.game.model.shop.ShopPolicy.NO_SELLING

class DonatorStoreBoosters : ShopScript() {
    init {
        "Donator Boosters"(50, ShopCurrency.DONOR_POINTS, NO_SELLING) {
            32149(10, 85, 100) // Larrran's key booster
            32201(10, 85, 130) // Drop rate pin
            32150(10, 85, 100) // Gano boost
            32151(10, 85, 50)  // Slayer boost
            32152(10, 85, 100) // Pet booster
            32153(10, 85, 100) // Gauntlet booster
            32154(10, 85, 100) // Blood money booster
            32155(10, 85, 100) // Clue booster
            32156(10, 85, 100) // ToB booster
            32166(10, 85, 100) // Rev booster
            32167(10, 85, 100) // Nex booster
        }
    }
}
