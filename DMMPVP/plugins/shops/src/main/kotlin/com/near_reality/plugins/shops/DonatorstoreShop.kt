package com.near_reality.plugins.shops

import com.near_reality.scripts.shops.ShopScript
import com.zenyte.game.model.shop.ShopCurrency
import com.zenyte.game.model.shop.ShopPolicy.NO_SELLING
import com.zenyte.game.model.shop.ShopPolicy.STOCK_ONLY

class Donatorstore : ShopScript() {
    init {
        "Donator store"(290, ShopCurrency.DONOR_POINTS, NO_SELLING) {
            32215(10, 85, 2400) //mbox bundle
            32209(10, 85, 325) //3rd age box
            32357(10, 85, 195) //easter box
            30031(10, 85, 195) //pet box
            32163(10, 85, 235) //cosmetic box
            32206(10, 85, 290) //ultimate box e
            32203(10, 85, 150) //pvp box
            32231(10, 85, 230) //regal box
            32164(10, 85, 180) //super m box
            32165(10, 85, 260) // ultimate m box
            32212(10, 85, 65) //skilling box
            6199(10, 85, 130) // mbox
            32149(10, 10, 100) //Larrrans key booster
            32201(10, 85, 130) //drop rate pin
            32150(10, 85, 100) //gano boost
            32151(10, 85, 50) //slayer boost
            32152(10, 85, 100) //pet booster
            32153(10, 85, 100) //gauntlet booster
            32154(10, 85, 100) //blood money booster
            32155(10, 85, 100) //clue booster
            32156(10, 85, 100) //tob booster
            32166(10, 85, 100) //rev booster
            32167(10, 85, 100) //nex booster
            28526(10, 85, 390) // sigil of ninja
            26141(10, 85, 390) // sigil remote storage
            28523(10, 85, 390) //sigil of titanium
            32001(10, 85, 650) //korasi
            32060(10, 85, 1000) //Lime whip
            11730(100, 85, 2) // Overload
        }
    }
}