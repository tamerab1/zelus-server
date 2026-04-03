package com.near_reality.game.world.entity.player.bot

import com.zenyte.game.item.Item
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot

/**
 * Complete loadout configuration for a bot type.
 * 
 * @author Riskers
 */
data class BotLoadout(
    val type: BotType,
    val name: String,
    val equipment: Map<EquipmentSlot, Item>,
    val inventory: List<Item>,
    val specialAttackConfig: SpecialAttackConfig,
    val combatConfig: CombatConfig
) 