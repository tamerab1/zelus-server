package com.zenyte.game.content.preset;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.zenyte.game.content.skills.magic.Spellbook;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemId;
import com.zenyte.game.model.item.UnmodifiableItem;
import com.zenyte.game.model.item.degradableitems.DegradableItem;
import com.zenyte.game.parser.impl.ItemRequirements;
import com.zenyte.game.util.Colour;
import com.zenyte.game.world.entity.masks.UpdateFlag;
import com.zenyte.game.world.entity.player.CombatDefinitions;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.Skills;
import com.zenyte.game.world.entity.player.container.Container;
import com.zenyte.game.world.entity.player.container.impl.Inventory;
import com.zenyte.game.world.entity.player.container.impl.RunePouch;
import com.zenyte.game.world.entity.player.container.impl.bank.Bank;
import com.zenyte.game.world.entity.player.container.impl.bank.BankContainer;
import com.zenyte.game.world.entity.player.container.impl.bank.BankSetting;
import com.zenyte.game.world.entity.player.container.impl.equipment.Equipment;
import com.zenyte.game.world.entity.player.privilege.GameMode;
import com.zenyte.game.world.entity.player.privilege.MemberRank;
import com.zenyte.game.world.region.RegionArea;
import com.zenyte.game.world.region.area.plugins.EquipmentPlugin;
import com.zenyte.plugins.equipment.equip.EquipPlugin;
import com.zenyte.plugins.equipment.equip.EquipPluginLoader;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mgi.types.config.items.ItemDefinitions;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;

import static com.zenyte.game.content.preset.PresetLoadResponse.*;

/**
 * @author Paxton | 7/05/2025
 */
@SuppressWarnings("DuplicatedCode")
public class PresetAdapter implements JsonDeserializer<Preset>, JsonSerializer<Preset> {

    @Override
    public Preset deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        String name = obj.get("name").getAsString();
        Spellbook spellbook = context.deserialize(obj.get("spellbook"), Spellbook.class);
        Map<Integer, UnmodifiableItem> inventory = context.deserialize(obj.get("inventory"), new TypeToken<Map<Integer, UnmodifiableItem>>() {}.getType());
        Map<Integer, UnmodifiableItem> equipment = context.deserialize(obj.get("equipment"), new TypeToken<Map<Integer, UnmodifiableItem>>() {}.getType());
        Map<Integer, UnmodifiableItem> runePouch = obj.has("runePouch") && !obj.get("runePouch").isJsonNull() ?
                context.deserialize(obj.get("runePouch"), new TypeToken<Map<Integer, UnmodifiableItem>>() {}.getType()) : null;
        Map<Integer, Integer> skillLevels = obj.has("skillLevels") && !obj.get("skillLevels").isJsonNull() ?
                context.deserialize(obj.get("skillLevels"), new TypeToken<Map<Integer, Integer>>() {}.getType()) : null;

        Map<Integer, Item> inventoryItems = new HashMap<>();
        for (Map.Entry<Integer, UnmodifiableItem> entry : inventory.entrySet()) {
            inventoryItems.put(entry.getKey(), entry.getValue().toItem());
        }

        Map<Integer, Item> equipmentItems = new HashMap<>();
        for (Map.Entry<Integer, UnmodifiableItem> entry : equipment.entrySet()) {
            equipmentItems.put(entry.getKey(), entry.getValue().toItem());
        }

        Preset preset = new Preset(name, equipmentItems, inventoryItems, spellbook, skillLevels);

        if (obj.has("locked") && obj.get("locked").getAsBoolean()) {
            preset.setLocked(true);
        }
        return preset;
    }

    @Override
    public JsonElement serialize(Preset src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", src.getName());
        obj.add("spellbook", context.serialize(src.getSpellbook()));
        obj.add("inventory", context.serialize(src.getInventory()));
        obj.add("equipment", context.serialize(src.getEquipment()));
        obj.add("runePouch", context.serialize(src.getRunePouch()));
        obj.add("skillLevels", context.serialize(src.getSkillLevels()));
        obj.addProperty("locked", src.isLocked());
        return obj;
    }
}
