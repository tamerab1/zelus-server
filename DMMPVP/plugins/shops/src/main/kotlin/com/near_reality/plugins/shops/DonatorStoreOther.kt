package com.near_reality.plugins.shops

import com.near_reality.scripts.shops.ShopScript
import com.zenyte.game.model.shop.ShopCurrency
import com.zenyte.game.model.shop.ShopPolicy.NO_SELLING

class DonatorStoreOther : ShopScript() {
    init {
        "Donator Other"(30, ShopCurrency.DONOR_POINTS, NO_SELLING) {
            28526(10, 85, 390)  // Sigil of Ninja
            26141(10, 85, 390)  // Sigil of Remote Storage
            28523(10, 85, 390)  // Sigil of Titanium
            32001(10, 85, 650)  // Korasi
            32060(10, 85, 1000) // Lime Whip
            11730(100, 85, 2)   // Overload
        }
    }
}
