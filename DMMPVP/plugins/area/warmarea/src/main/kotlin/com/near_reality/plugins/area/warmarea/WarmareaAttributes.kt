package com.near_reality.plugins.area.warmarea

import com.zenyte.game.world.entity.persistentAttribute
import com.zenyte.game.world.entity.player.Player

/** Persistent training points earned by killing Warmarea bots. */
var Player.trainingPoints: Int by persistentAttribute("warmarea-training-points", 0)
