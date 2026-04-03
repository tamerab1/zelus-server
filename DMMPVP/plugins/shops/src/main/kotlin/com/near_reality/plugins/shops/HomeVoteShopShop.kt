package com.near_reality.plugins.shops

import com.near_reality.game.item.CustomItemId

import com.near_reality.scripts.shops.ShopScript
import com.zenyte.game.model.shop.*
import com.zenyte.game.model.shop.ShopPolicy
import com.zenyte.game.model.shop.ShopPolicy.*
import com.zenyte.game.model.shop.ShopCurrency
import com.zenyte.game.model.shop.ShopCurrency.*
import com.zenyte.game.item.ItemId
import com.zenyte.game.item.ItemId.*
import com.near_reality.game.content.universalshop.*
import com.near_reality.game.content.universalshop.UnivShopItem
import com.near_reality.game.content.universalshop.UnivShopItem.*

class HomeVoteShopShop : ShopScript() {
    init {
        "Vote Shop"(1009, VOTE_POINTS, STOCK_ONLY) {
            UnivShopItem(CustomItemId.VOTE_GEM, buyPrice = 1, sellPrice = 1).invoke()
            UnivShopItem(989, buyPrice = 1).invoke() // crystal key
            UnivShopItem(23083, buyPrice = 5).invoke() // Brimstone key
            UnivShopItem(6199, buyPrice = 25).invoke() //mystery box
            UnivShopItem(32164, buyPrice = 75).invoke() // SMystery box
            UnivShopItem(32165, buyPrice = 105).invoke() // UMystery box
            UnivShopItem(32231, buyPrice = 90).invoke() // RMystery box
            UnivShopItem(32163, buyPrice = 65).invoke() // CMystery box
            UnivShopItem(32060, buyPrice = 50).invoke() // Lime whip
            UnivShopItem(32064, buyPrice = 50).invoke() // Lava whip

        }
    }
}