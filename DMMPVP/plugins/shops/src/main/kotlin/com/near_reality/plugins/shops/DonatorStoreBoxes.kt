package com.near_reality.plugins.shops

import com.near_reality.scripts.shops.ShopScript
import com.zenyte.game.model.shop.ShopCurrency
import com.zenyte.game.model.shop.ShopPolicy.NO_SELLING

class DonatorStoreBoxes : ShopScript() {
    init {
        "Donator Boxes"(60, ShopCurrency.DONOR_POINTS, NO_SELLING) {
            32215(10, 85, 2400) // Mbox bundle
            32209(10, 85, 325)  // 3rd Age Box
            32357(10, 85, 195)  // Easter Box
            30031(10, 85, 195)  // Pet Box
            32163(10, 85, 235)  // Cosmetic Box
            32206(10, 85, 290)  // Ultimate Box E
            32203(10, 85, 150)  // PvP Box
            32231(10, 85, 230)  // Regal Box
            32164(10, 85, 180)  // Super M Box
            32165(10, 85, 260)  // Ultimate M Box
            32212(10, 85, 65)   // Skilling Box
            6199(10, 85, 130)   // Mystery Box
        }
    }
}
