package com.zenyte.game.content.grandexchange;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.near_reality.tools.logging.GameLogMessage;
import com.near_reality.tools.logging.GameLogger;
import com.zenyte.game.GameConstants;
import com.zenyte.game.content.grandexchange.GrandExchange;
import com.zenyte.game.GameInterface;
import com.zenyte.game.content.preset.Preset;
import com.zenyte.game.content.preset.PresetManager;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemId;
import com.zenyte.game.model.ui.GameTab;
import com.zenyte.game.model.ui.InterfaceHandler;
import com.zenyte.game.model.ui.InterfacePosition;
import com.zenyte.game.packet.PacketDispatcher;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.util.Colour;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.VarManager;
import com.zenyte.game.world.entity.player.container.ContainerResult;
import com.zenyte.game.world.entity.player.container.RequestResult;
import com.zenyte.game.world.entity.player.privilege.PlayerPrivilege;
import com.zenyte.game.world.region.area.plugins.TempPlayerStatePlugin;
import com.zenyte.utils.TimeUnit;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import kotlinx.datetime.Instant;
import mgi.types.config.items.ItemDefinitions;
import mgi.utilities.StringFormatUtil;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Tommeh | 26 nov. 2017 : 18:58:13
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server
 * profile</a>}
 */
public class GrandExchange {

    public static boolean ENABLED = true;

    public static final int GRAND_EXCHANGE_CURRENCY_ITEM = ItemId.BLOOD_MONEY;

    public static final int INTERFACE = 465;
    public static final int INVENTORY_INTERFACE = 467;
    public static final int TYPE_VARPBIT = 4397;
    public static final int ITEM_VARP = 1151;
    //static final Object LOCK = new Object();
    public static final int SLOT_VARPBIT = 4439;
    private static final int QUANTITY_VARPBIT = 4396;
    private static final int PRICE_VARP = 1043;
    public static final long OFFER_TIMEOUT_DELAY = TimeUnit.DAYS.toMillis(7);
    private final transient Player player;
    @Expose
    private LinkedList<ExchangeHistory> history = new LinkedList<>();
    private static final String JSON_PATH = "C:\\Users\\vboxuser\\Downloads\\near-reality-server-main\\near-reality-server-main\\data\\grandexchange\\offers.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final Set<Integer> BLOCKED_SELL_ITEMS = new HashSet<>();


    public static void addBlockedItemWithNotes(Item item) {
        if (item == null) return;

        int id = item.getId();
        BLOCKED_SELL_ITEMS.add(id);

        var defs = item.getDefinitions();

        int notedId = defs.getNotedId();
        if (notedId > 0 && notedId != id) {
            BLOCKED_SELL_ITEMS.add(notedId);
        }

        if (defs.isNoted()) {
            ItemDefinitions notedDef = ItemDefinitions.getOrThrow(id);
            int unnotedId = notedDef.getNotedId();
            if (unnotedId > 0 && unnotedId != id) {
                BLOCKED_SELL_ITEMS.add(unnotedId);
            }
        }
    }




    public GrandExchange(final Player player) {
        this.player = player;
    }

    public final void initialize(final GrandExchange exchange) {
        if (exchange == null) {
            return;
        }
        if (exchange.history != null) {
            history = exchange.history;
        }
        cleanHistory();
    }

    private final void cleanHistory() {
        while (history.size() > 20) {
            history.poll();
        }
    }

    public void updateOffers() {
        for (final ExchangeOffer offer : getOffers().values()) {
            if (offer == null) {
                continue;
            }
            player.getPacketDispatcher().sendGrandExchangeOffer(offer);
        }
    }

    public void openOffersInterface() {
        if (!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }

        if (TempPlayerStatePlugin.enableTempState(player, TempPlayerStatePlugin.StateType.INVENTORY)) {
            player.sendMessage("Cannot open the GE right now.");
            return;
        }

        if (player.getPrivilege().is(PlayerPrivilege.FORUM_MODERATOR)) {
            player.sendMessage("The clerk refuses to talk to you about any issues they're having.");
            return;
        }

        if (player.isIronman()) {
            player.sendMessage("As an Iron Man, you cannot use the Grand Exchange.");
            return;
        }

        removeExpiredOffersFromJSON();

        final InterfaceHandler handler = player.getInterfaceHandler();
        final PacketDispatcher dispatcher = player.getPacketDispatcher();

        reset();
        player.getTemporaryAttributes().put("GrandExchange", true);
        player.getVarManager().sendBit(SLOT_VARPBIT, 0);
        dispatcher.sendClientScript(828, 1);
        resetGEVars();

        handler.closeInput();
        handler.sendInterface(InterfacePosition.CENTRAL, INTERFACE);
        handler.sendInterface(InterfacePosition.INVENTORY_TAB, INVENTORY_INTERFACE);
        handler.openGameTab(GameTab.INVENTORY_TAB);

        for (int i = 7; i <= 14; i++) {
            dispatcher.sendComponentSettings(INTERFACE, i, 2, 2, AccessMask.CLICK_OP1, AccessMask.CLICK_OP2);
            dispatcher.sendComponentSettings(INTERFACE, i, 3, 4, AccessMask.CLICK_OP1);
        }
        dispatcher.sendComponentSettings(INTERFACE, 22, 0, 0, AccessMask.CLICK_OP1);
        dispatcher.sendComponentSettings(INTERFACE, 23, 2, 3, AccessMask.CLICK_OP1, AccessMask.CLICK_OP2, AccessMask.CLICK_OP3, AccessMask.CLICK_OP10);
        dispatcher.sendComponentSettings(INTERFACE, 6, 0, 0, AccessMask.CLICK_OP1, AccessMask.CLICK_OP2);
        dispatcher.sendComponentSettings(INTERFACE, 24, 0, 13, AccessMask.CLICK_OP1);
        dispatcher.sendComponentSettings(INVENTORY_INTERFACE, 0, 0, 27, AccessMask.CLICK_OP1, AccessMask.CLICK_OP10);

        refreshOffers();

        player.setCloseInterfacesEvent(() -> {
            GameInterface.INVENTORY_TAB.open(player);
            handler.closeInput();
            player.getTemporaryAttributes().remove("GrandExchange");
            reset();
        });
    }

    private void removeExpiredOffersFromJSON() {
        File file = new File(JSON_PATH);
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            Iterator<JsonElement> iterator = jsonArray.iterator();
            long currentTime = System.currentTimeMillis();

            while (iterator.hasNext()) {
                JsonElement element = iterator.next();
                JsonElement lastUpdateTimeElement = element.getAsJsonObject().get("lastUpdateTime");
                JsonElement abortedElement = element.getAsJsonObject().get("aborted");
                JsonElement cancelledElement = element.getAsJsonObject().get("cancelled");

                if (lastUpdateTimeElement != null) {
                    long lastUpdateTime = lastUpdateTimeElement.getAsLong();
                    boolean isAborted = abortedElement != null && abortedElement.getAsBoolean();
                    boolean isCancelled = cancelledElement != null && cancelledElement.getAsBoolean();

                    if (isAborted || isCancelled || (currentTime - lastUpdateTime > OFFER_TIMEOUT_DELAY)) {
                        iterator.remove();
                    }
                }
            }

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(jsonArray, writer);
            }
            //remove if annoys u
            player.sendMessage("[DEBUG] Json Purged");
            //stacktrace exception
        } catch (IOException e) {
            player.sendMessage("[ERROR] Failed to clean up offers.json: " + e.getMessage());
        }
    }


    public void resetGEVars() {
        player.getVarManager().sendVar(3204, -1);
        player.getVarManager().sendVar(3206, -1);
        player.getVarManager().sendVar(3208, -1);
        player.getVarManager().sendVar(3210, -1);
        player.getVarManager().sendVar(3212, -1);
        player.getVarManager().sendVar(3214, -1);
        player.getVarManager().sendVar(3216, -1);
        player.getVarManager().sendVar(3218, -1);
    }

    public void refreshOffers() {
        for (final ExchangeOffer offer : getOffers().values()) {
            if (offer == null) {
                continue;
            }
            offer.refreshItems();
        }
    }

    public void openHistoryInterface() {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }

        GameInterface.GRAND_EXCHANGE_HISTORY.open(player);
    }

    public void openItemSetsInterface() {
        GameInterface.ITEM_SETS.open(player);
    }

    public void sell(final int slot, final boolean dialogue) {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }
        reset();
        viewOffer(slot);
        final PacketDispatcher dispatcher = player.getPacketDispatcher();
        player.getVarManager().sendBit(TYPE_VARPBIT, 1);
        if (dialogue) {
            dispatcher.sendComponentText(INTERFACE, 25, "Choose an item from your inventory to sell.");
            dispatcher.sendComponentText(INTERFACE, 26, "");
            dispatcher.sendComponentText(INTERFACE, 16, "");
            dispatcher.sendComponentText(INTERFACE, 17, "");
        }
        player.getInterfaceHandler().openGameTab(GameTab.INVENTORY_TAB);
    }

    public boolean sell(Item item) {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return false;
        }
        if (BLOCKED_SELL_ITEMS.contains(item.getId())) {
            player.sendMessage("You cannot sell this item on the Grand Exchange.");
            player.getVarManager().sendBit(SLOT_VARPBIT, 1);
            player.getVarManager().sendBit(SLOT_VARPBIT, -1);
            return false;
        }
        if (!item.isTradable()) {
            player.sendMessage("This item is untradeable.");
            player.getVarManager().sendBit(SLOT_VARPBIT, 1);
            player.getVarManager().sendBit(SLOT_VARPBIT, -1);
            return false;
        }
        if (GameConstants.RESTRICTED_TRADE_ITEMS.contains(item.getId())
                || item.getId() == GRAND_EXCHANGE_CURRENCY_ITEM
                || item.getId() == ItemId.BLOOD_MONEY || !item.getDefinitions().isGrandExchange()) {
            player.sendMessage("This item cannot be sold.");
            player.getVarManager().sendBit(SLOT_VARPBIT, 1);
            player.getVarManager().sendBit(SLOT_VARPBIT, -1);
            return false;
        }
        item = new Item(item);
        final PacketDispatcher dispatcher = player.getPacketDispatcher();
        if (item.getDefinitions().isNoted()) {
            item.setId(item.getDefinitions().getNotedId());
        }
        reset();
        setQuantity(item.getAmount());
        setItem(item.getId());
        setPrice(item.getSellPrice());
        player.getVarManager().sendBit(TYPE_VARPBIT, 1);
        dispatcher.sendComponentText(INTERFACE, 25, item.getDefinitions().getExamine());
        dispatcher.sendComponentText(INTERFACE, 26, item.getDefinitions().getName());
        return true;
    }

    public void buy(final int slot, final boolean dialogue) {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }
        final PacketDispatcher dispatcher = player.getPacketDispatcher();
        reset();
        viewOffer(slot);
        player.getVarManager().sendBit(TYPE_VARPBIT, 0);
        if (dialogue) {
            dispatcher.sendComponentText(INTERFACE, 25, "Click the icon on the left to search for items.");
            dispatcher.sendComponentText(INTERFACE, 16, "");
            dispatcher.sendComponentText(INTERFACE, 17, "");
            player.sendInputItem("What would you like to buy?", this::buy);
        }
    }

    public void buy(final Item item) {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }
        final PacketDispatcher dispatcher = player.getPacketDispatcher();
        setItem(item.getId());
        setPrice(item.getSellPrice());
        setQuantity(1);
        player.getInterfaceHandler().closeInput();
        player.getVarManager().sendBit(TYPE_VARPBIT, 0);
        dispatcher.sendComponentText(INTERFACE, 25, item.getDefinitions().getExamine());
        dispatcher.sendComponentText(INTERFACE, 26, item.getDefinitions().getName());
    }

    public void viewOffer(final int slot) {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }
        final ExchangeOffer offer = getOffers().get(slot);
        if (offer != null) {
            offer.refreshItems();
            final Item item = offer.getItem();
            final PacketDispatcher dispatcher = player.getPacketDispatcher();
            final long offerExpirationTime = System.currentTimeMillis() - OFFER_TIMEOUT_DELAY;
            final long offerUpdateTime = offer.getLastUpdateTime();
            final StringBuilder builder = new StringBuilder();
            if (!offer.isCompleted() && !offer.isCancelled() && !offer.isAborted()) {
                if (offerUpdateTime < offerExpirationTime) {
                    builder.append(Colour.RED.wrap("Offer has expired."));
                } else {
                    final long validDelay = offerUpdateTime - offerExpirationTime;
                    final long days = TimeUnit.MILLISECONDS.toDays(validDelay);
                    final long hours = TimeUnit.MILLISECONDS.toHours(validDelay) % 24;
                    final long minutes = TimeUnit.MILLISECONDS.toMinutes(validDelay) % 60;
                    builder.append(Colour.GREEN).append("Offer valid for ");
                    if (days > 0) {
                        builder.append(days).append(" day").append(days == 1 ? "" : "s");
                        if (hours > 0 && minutes <= 0) {
                            builder.append(" and ");
                        } else {
                            builder.append(", ");
                        }
                    }
                    if (hours > 0) {
                        builder.append(hours).append(" hour").append(hours == 1 ? "" : "s");
                        if (minutes > 0) {
                            builder.append(" and ");
                        } else {
                            builder.append(", ");
                        }
                    }
                    if (minutes > 0) {
                        builder.append(minutes).append(" minute").append(minutes == 1 ? "" : "s").append(", ");
                    }
                    if (builder.length() >= 2) {
                        builder.delete(builder.length() - 2, builder.length());
                    }
                    builder.append('.');
                    builder.append("<col>");
                }
            }
            dispatcher.sendComponentText(INTERFACE, 16, item.getDefinitions().getExamine() + "<br>" + builder);
            dispatcher.sendComponentText(INTERFACE, 17, item.getDefinitions().getName());
        }
        player.getVarManager().sendBit(GrandExchange.SLOT_VARPBIT, slot + 1);
    }

    public void abortOffer(final int slot) {
        if (!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }
        if (slot <= -1) {
            return;
        }
        final ExchangeOffer offer = getOffers().get(slot);
        if (offer == null || offer.isAborted()) {
            return;
        }
        player.sendMessage("Sending Abort request...");
        offer.cancel();
        offer.update();

        removeOfferFromJSON(offer.getItem().getId());
    }


    public void abortOffer() {
        abortOffer(player.getVarManager().getBitValue(GrandExchange.SLOT_VARPBIT) - 1);
    }

    public void collectItems(final int option, int slotId) {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }
        final int slot = player.getVarManager().getBitValue(GrandExchange.SLOT_VARPBIT) - 1;
        if (slot <= -1) {
            return;
        }
        collectFromBox(true, slot, option, slotId - 2);
    }

    public void collectFromBox(final boolean offersInterface, final int slot, final int option, final int slotId) {
        if (!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }

        final ExchangeOffer offer = getOffers().get(slot);
        if (offer == null) {
            return;
        }

        final Item item = offer.getContainer().get(slotId);
        if (item == null) {
            return;
        }

        final int op = (option == 1 || option == 2) ?
                (item.getAmount() > 1 ? (option == 1 ? 2 : 1) : option)
                : option;

        final Item offerItem = (op == 2) ? item.toNote() : item;
        boolean removeItem = false;

        if (option == 1 || option == 2) {
            final ContainerResult result = player.getInventory().addItem(offerItem);
            if (result.getResult().equals(RequestResult.SUCCESS)) {
                offer.getContainer().remove(item);
                removeItem = true;
            } else {
                if (result.getSucceededAmount() > 0) {
                    offer.getContainer().set(slotId, new Item(item.getId(), item.getAmount() - result.getSucceededAmount()));
                }
                player.sendMessage("Not enough space in your inventory.");
            }
        } else if (option == 3) {
            final ContainerResult result = player.getBank().add(offerItem);
            if (result.getResult().equals(RequestResult.SUCCESS)) {
                offer.getContainer().remove(item);
                removeItem = true;
            } else {
                if (result.getSucceededAmount() > 0) {
                    offer.getContainer().set(slotId, new Item(item.getId(), item.getAmount() - result.getSucceededAmount()));
                }
                player.sendMessage("Not enough space in your bank.");
            }
        }

        offer.refreshItems();

        if (offer.getContainer().getFreeSlotsSize() == 2 && (offer.isCompleted() || offer.isCancelled() || offer.isAborted())) {
            offer.setCancelled(true);
            player.getPacketDispatcher().sendGrandExchangeOffer(offer);
            GrandExchangeHandler.remove(player.getUsername(), offer.getSlot());

            if (offersInterface) {
                openOffersInterface();
            }

            if (offer.getAmount() != 0) {
                history.add(new ExchangeHistory(offer.getItem().getId(), offer.getAmount(), offer.getTotalPrice(), offer.getType()));
            }

            cleanHistory();

            removeOfferFromJSON(offer.getItem().getId());
        }
    }
    private void removeOfferFromJSON(int itemId) {
        File file = new File(JSON_PATH);
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            Iterator<JsonElement> iterator = jsonArray.iterator();

            while (iterator.hasNext()) {
                JsonElement element = iterator.next();
                if (element.getAsJsonObject().get("item").getAsJsonObject().get("id").getAsInt() == itemId) {
                    iterator.remove();
                }
            }

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(jsonArray, writer);
            }

            player.sendMessage("[DEBUG] Successfully removed item from JSON: " + itemId);

        } catch (IOException e) {
            player.sendMessage("[ERROR] Failed to update offers.json: " + e.getMessage());
        }
    }


    public void collectAll(final boolean inventory, final boolean openOffers) {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }
        final ObjectCollection<ExchangeOffer> offers = getOffers().values();
        if (offers.isEmpty()) {
            return;
        }
        final MutableBoolean notEnoughSpace = new MutableBoolean(false);
        offers.forEach(offer -> offer.getContainer().getItems().int2ObjectEntrySet().fastForEach(entry -> {
            final Item item = entry.getValue();
            final int slot = entry.getIntKey();
            final Item toAdd = new Item(item.getDefinitions().getNotedOrDefault(), item.getAmount());
            ContainerResult result = inventory ? player.getInventory().addItem(toAdd) : player.getBank().add(toAdd);
            boolean remove = result.getResult().equals(RequestResult.SUCCESS);
            if (!remove) {
                notEnoughSpace.setTrue();
                if (result.getSucceededAmount() > 0) {
                    offer.getContainer().set(slot, new Item(item.getId(), item.getAmount() - result.getSucceededAmount()));
                }
            } else {
                offer.getContainer().remove(item);
            }
            offer.refreshItems();
            if (offer.getContainer().getFreeSlotsSize() == 2 && (offer.isCompleted() || offer.isCancelled() || offer.isAborted())) {
                offer.setCancelled(true);
                player.getPacketDispatcher().sendGrandExchangeOffer(offer);
                GrandExchangeHandler.remove(player.getUsername(), offer.getSlot());
                if (openOffers) {
                    openOffersInterface();
                }
                if (offer.getAmount() != 0) {
                    history.add(new ExchangeHistory(offer.getItem().getId(), offer.getAmount(), offer.getTotalPrice(), offer.getType()));
                }
                cleanHistory();
            }
        }));
        if (notEnoughSpace.isTrue()) {
            player.sendMessage("Not enough space in your " + (inventory ? "inventory" : "bank") + ".");
        }
    }

    public void setItem(final int id) {
        player.getVarManager().sendVar(ITEM_VARP, id);
    }

    public void modifyPrice(int price) {
        if (price < 1) {
            price = 1;
        }
        setPrice(price);
    }

    public void modifyQuantity(int quantity) {
        if (quantity < 1) {
            quantity = 1;
        }
        setQuantity(quantity);
    }

    public int getPrice() {
        return player.getVarManager().getValue(PRICE_VARP);
    }

    public void setPrice(final int price) {
        player.getVarManager().sendVar(PRICE_VARP, price);
    }

    public int getQuantity() {
        return player.getVarManager().getBitValue(QUANTITY_VARPBIT);
    }

    public void setQuantity(final int quantity) {
        player.getVarManager().sendBit(QUANTITY_VARPBIT, quantity);
    }

    public void reset() {
        final PacketDispatcher dispatcher = player.getPacketDispatcher();
        setQuantity(1);
        setPrice(0);
        setItem(-1);
        dispatcher.sendComponentText(INTERFACE, 25, "");
        dispatcher.sendComponentText(INTERFACE, 26, "");
        dispatcher.sendComponentText(INTERFACE, 16, "");
        dispatcher.sendComponentText(INTERFACE, 17, "");
    }

    public void createOffer() {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }
        final VarManager manager = player.getVarManager();
        final int price = manager.getValue(GrandExchange.PRICE_VARP);
        int quantity = manager.getBitValue(GrandExchange.QUANTITY_VARPBIT);
        if (quantity <= 0) {
            player.sendMessage("You must set the quantity to a positive number.");
            return;
        }
        final int id = manager.getValue(GrandExchange.ITEM_VARP);
        if (id <= 0) {
            player.sendMessage("You must choose an item.");
            return;
        }
        if (!ItemDefinitions.getOrThrow(id).isGrandExchange()) {
            return;
        }
        final Item item = new Item(id);
        final int slot = manager.getBitValue(SLOT_VARPBIT) - 1;
        if (slot == -1) {
            return;
        }
        final ExchangeType type = manager.getBitValue(TYPE_VARPBIT) == 0 ? ExchangeType.BUYING : ExchangeType.SELLING;
        final long total = (long) price * (long) quantity;
        if (total > Integer.MAX_VALUE) {
            player.sendMessage("Too much money!");
            return;
        }
        if (type.equals(ExchangeType.BUYING)) {
            if (player.getInventory().getAmountOf(GRAND_EXCHANGE_CURRENCY_ITEM) < total) {
                player.sendMessage("That offer costs "
                        + StringFormatUtil.format(total) + " "
                        + ItemDefinitions.get(GRAND_EXCHANGE_CURRENCY_ITEM).getName()
                        + ". You haven't got enough.");
                return;
            }
            player.getInventory().deleteItem(new Item(GRAND_EXCHANGE_CURRENCY_ITEM, (int) total));
        } else {
            final int regularAmount = player.getInventory().getAmountOf(item.getId());
            final int notedAmount = player.getInventory().getAmountOf(item.getDefinitions().getNotedId());
            final int notedId = item.getDefinitions().getNotedId();
            if (quantity > regularAmount + notedAmount) {
                player.sendMessage("You don't have enough of this item to put in an offer.");
                return;
            }
            final int success = player.getInventory().deleteItem(notedId, quantity).getSucceededAmount();
            int q = quantity;
            q -= success;
            if (q > 0) {
                final int deleted = player.getInventory().deleteItem(item.getId(), q).getSucceededAmount();
                q -= deleted;
                if (q > 0) {
                    quantity -= q;
                }
            }
        }
        if (quantity < 1 || price < 1) {
            return;
        }
        final int marketPrice = GrandExchangePriceManager.getCurrentPrice(id);
        if (marketPrice > 0) {
            final int minPrice = Math.max(1, marketPrice / 5);
            final int maxPrice = marketPrice * 5;
            if (price < minPrice || price > maxPrice) {
                player.sendMessage("Your offer price must be within 5x of the current market price ("
                        + StringFormatUtil.format(marketPrice) + " blood money each).");
                return;
            }
        }
        final ExchangeOffer offer = new ExchangeOffer(player.getUsername(), new Item(item.getId(), quantity), price, slot, type);
        player.getPacketDispatcher().sendGrandExchangeOffer(offer);
        addOffer(offer);
        getOffers().put(slot, offer);
        manager.sendBit(SLOT_VARPBIT, 0);
    }

    public void resetExistingOffers() {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }
        history.clear();
        final Int2ObjectOpenHashMap<ExchangeOffer> offers = GrandExchangeHandler.getAllOffers().get(player.getUsername());
        offers.forEach((id, offer) -> {
            offer.setCancelled(true);
            player.getPacketDispatcher().sendGrandExchangeOffer(offer);
        });
        GrandExchangeHandler.getAllOffers().remove(player.getUsername());
        reset();
        player.getInterfaceHandler().closeInterfaces();
        refreshOffers();
    }

    private void addOffer(final ExchangeOffer offer) {
        if(!ENABLED) {
            player.sendMessage("GE is currently disabled.");
            return;
        }
        GameLogger.log(Level.INFO, () -> new GameLogMessage.GrandExchangeOffer(Instant.Companion.now(), player.getUsername(), offer));
        GrandExchangeHandler.addOffer(player.getUsername(), offer);
        GrandExchangeOfferExecutor.refresh(offer);
    }

    @NotNull
    private Int2ObjectOpenHashMap<ExchangeOffer> getOffers() {
        return GrandExchangeHandler.getOffers(player.getUsername());
    }

    public int getFreeSlot() {
        for (int i = 0; i < 8; i++) {
            final ExchangeOffer offer = getOffers().get(i);
            if (offer == null) {
                return i;
            }
        }
        return 0;
    }

    public LinkedList<ExchangeHistory> getHistory() {
        return history;
    }
}
