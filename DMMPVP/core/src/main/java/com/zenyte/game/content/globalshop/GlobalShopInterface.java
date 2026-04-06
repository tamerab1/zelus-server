package com.zenyte.game.content.globalshop;

import com.near_reality.api.service.vote.VotePlayerAttributesKt;
import com.zenyte.game.GameInterface;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.shop.Shop;
import com.zenyte.game.model.shop.ShopInterface; // contains the public enum
import com.zenyte.game.model.shop.ShopInterface.ItemOption; // use this enum
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.packet.PacketDispatcher;
import com.zenyte.game.util.ItemUtil;
import com.zenyte.game.util.Colour;
import com.zenyte.game.world.entity.player.Player;
import mgi.types.config.items.ItemDefinitions;
import mgi.utilities.StringFormatUtil;

import static com.zenyte.game.util.AccessMask.*;
import static com.zenyte.game.util.AccessMask.CLICK_OP10;

public class GlobalShopInterface extends Interface {

    /**
     * Retrieves the Shop instance from the player's temporary attributes.
     */
    private static Shop getShopAttr(final Player player) {
        final Object shopAttr = player.getTemporaryAttributes().get("Shop");
        if (!(shopAttr instanceof Shop)) {
            throw new RuntimeException("Unable to open the shop directly.");
        }
        return (Shop) shopAttr;
    }

    @Override
    protected void attach() {
        // Bind the store container component (for buying)
        put(6, "container");
        put(8, "vote");
        put(12, "loyalty");
        put(16, "blood_money");
        put(20, "bh");
        put(24, "supplies");
        put(28, "jewellery");
        put(32, "melee");
        put(36, "range");
        put(40, "magic");
        put(44, "skilling");
    }

    @Override
    protected void close(Player player) {
        player.getTemporaryAttributes().remove("GlobalShopCategory");
    }

    @Override
    public void open(Player player) {
        final Object categoryAttr = player.getTemporaryAttributes().get("GlobalShopCategory");
        final String category = (categoryAttr instanceof String) ? (String) categoryAttr : "Vote Shop";
        openShopCategory(player, category);
        GameInterface.SHOP_INVENTORY.open(player);
        player.getInterfaceHandler().sendInterface(getInterface());

        final Shop shop = Shop.get(category, player.isIronman(), player);
        player.getTemporaryAttributes().put("Shop", shop);
        final PacketDispatcher dispatcher = player.getPacketDispatcher();
        final Shop.ShopContainer container = shop.getContainer();

        dispatcher.sendComponentSettings(
                getInterface(),
                getComponent("container"),
                0,
                container.getContainerSize(),
                CLICK_OP1, CLICK_OP2, CLICK_OP3, CLICK_OP4, CLICK_OP5, CLICK_OP6, CLICK_OP7, CLICK_OP10);

    }

    /**
     * Opens a shop category (sets up the store container for buying)
     * and sends the shop name to component 2.
     * If the shop is Vote or Loyalty, it sends a message showing only the respective points.
     */
    private void openShopCategory(Player player, String shopName) {

        player.getTemporaryAttributes().put("GlobalShopCategory", shopName);

        final Shop shop = Shop.get(shopName, player.isIronman(), player);
        player.getTemporaryAttributes().put("Shop", shop);
        final PacketDispatcher dispatcher = player.getPacketDispatcher();
        final Shop.ShopContainer container = shop.getContainer();

        // Initialize the shop container with script 1074 (shop name and container type).
        dispatcher.sendClientScript(1074, shop.getName(), container.getType().getId());
        // Display the regular shop name.
        dispatcher.sendComponentText(getId(), 2, shop.getName());
        // Send the grid parameters for buying.
        player.getPacketDispatcher().sendClientScript(149,
                getId() << 16 | getComponent("container"), container.getType().getId(), 7,
                (container.getContainerSize() / 7) + 1, 0, -1, "Value", "Buy-1", "Buy-5", "Buy-10", "Buy-X");

        dispatcher.sendComponentSettings(
                getInterface(),
                getComponent("container"),
                0,
                container.getContainerSize(),
                CLICK_OP1, CLICK_OP2, CLICK_OP3, CLICK_OP4, CLICK_OP5, CLICK_OP6, CLICK_OP7, CLICK_OP10);

        shop.getPlayers().add(player);
        container.setFullUpdate(true);
        container.refresh(player);

        if ("Vote Shop".equals(shopName)) {
            player.sendMessage("You have - Vote: " +
                    Colour.RED.wrap(VotePlayerAttributesKt.getTotalVoteCredits(player)) + " pts");
        } else if ("Loyalty Shop".equals(shopName)) {
            player.sendMessage("You have - Loyalty: " +
                    Colour.RED.wrap(player.getLoyaltyManager().getLoyaltyPoints()) + " pts");
        } else if ("Bounty Hunter Shop".equals(shopName)) {
            player.sendMessage("You have - Bounty Hunter: " +
                    Colour.RED.wrap(player.getBountyHunterPoints()) + " pts");
        }
    }

    @Override
    protected void build() {
        // Bind category buttons.
        bind("vote", (player, slot, itemId, option) -> openShopCategory(player, "Vote Shop"));
        bind("loyalty", (player, slot, itemId, option) -> openShopCategory(player, "Loyalty Shop"));
        bind("blood_money", (player, slot, itemId, option) -> openShopCategory(player, "PVP SHOP"));
        bind("bh", (player, slot, itemId, option) -> openShopCategory(player, "Bounty Hunter Shop"));
        bind("supplies", (player, slot, itemId, option) -> openShopCategory(player, "Consumables Store"));
        bind("jewellery", (player, slot, itemId, option) -> openShopCategory(player, "Jewellery Store"));
        bind("melee", (player, slot, itemId, option) -> openShopCategory(player, "Melee Store"));
        bind("range", (player, slot, itemId, option) -> openShopCategory(player, "Ranged Store"));
        bind("magic", (player, slot, itemId, option) -> openShopCategory(player, "Magic Store"));
        bind("skilling", (player, slot, itemId, option) -> openShopCategory(player, "Tools Store"));

        // Buying interactions for items in the store container.
        bind("container", (player, interfaceSlotId, itemId, option) -> {
            final int slotId = interfaceSlotId; // adjust if necessary (zero-based)
            final Shop shop = getShopAttr(player);
            final Item item = shop.getContainer().get(slotId);
            if (ItemDefinitions.isInvalid(itemId) || item == null || item.getId() != itemId)
                return;
            final int mobileBuyAmount = player.getNumericTemporaryAttribute("mobile buy amount").intValue();
            if (mobileBuyAmount != 0 && option == ShopInterface.ItemOption.VALUE.optionId) {
                option = mobileBuyAmount;
            }
            final ShopInterface.ItemOption op = ItemOption.of(option);
            if (op.is(ShopInterface.ItemOption.EXAMINE)) {
                ItemUtil.sendItemExamine(player, item);
                return;
            }
            if (op.is(ShopInterface.ItemOption.VALUE)) {
                final int price = shop.getBuyPrice(player, item.getId());
                if (price <= -1) {
                    player.sendMessage(item.getName() + ": currently unavailable.");
                } else if (price == 0) {
                    player.sendMessage(item.getName() + ": currently costs nothing.");
                } else {
                    player.sendMessage(item.getName() + ": currently costs " +
                            StringFormatUtil.format(price) + " " + shop.getCurrency() + ".");
                }
                return;
            }
            if (op.is(ShopInterface.ItemOption.BUY_X)) {
                player.sendInputInt("Enter amount:", amt -> shop.purchase(player, amt, slotId));
                return;
            }
            shop.purchase(player, op, slotId);
        });
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.GLOBAL_SHOP;
    }
}
