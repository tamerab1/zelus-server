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

    // -----------------------------------------------------------------------
    // Donator shop category names — must match the ShopScript "name" exactly.
    // -----------------------------------------------------------------------
    public static final String DONATOR_BOOSTERS = "Donator Boosters";
    public static final String DONATOR_BOXES    = "Donator Boxes";
    public static final String DONATOR_OTHER    = "Donator Other";

    /**
     * Attribute key that marks the player as browsing the donator shop.
     * Set this before calling {@code GameInterface.GLOBAL_SHOP.open(player)}.
     */
    public static final String DONATOR_MODE_KEY = "DonatorShopMode";

    /**
     * Text component IDs for the first three tab button labels inside
     * interface 5117.  The exact child offsets (+1 per button) are cache-
     * dependent — adjust here if the labels do not render in-game.
     *
     * Pattern: each button group occupies 4 component slots starting at
     * component 8 (vote), 12 (loyalty), 16 (blood_money).  The text child
     * is assumed to sit at buttonComponent + 1.
     */
    private static final int TAB1_TEXT_COMPONENT = 9;   // "Vote"    → "Boosters"
    private static final int TAB2_TEXT_COMPONENT = 13;  // "Loyalty" → "Boxes"
    private static final int TAB3_TEXT_COMPONENT = 17;  // "B-Money" → "Other"

    // -----------------------------------------------------------------------

    private static Shop getShopAttr(final Player player) {
        final Object shopAttr = player.getTemporaryAttributes().get("Shop");
        if (!(shopAttr instanceof Shop)) {
            throw new RuntimeException("Unable to open the shop directly.");
        }
        return (Shop) shopAttr;
    }

    /** Returns true when this player opened the interface via the donator NPC. */
    private static boolean isDonatorMode(final Player player) {
        return Boolean.TRUE.equals(player.getTemporaryAttributes().get(DONATOR_MODE_KEY));
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
        player.getTemporaryAttributes().remove(DONATOR_MODE_KEY);
    }

    @Override
    public void open(Player player) {
        final boolean donatorMode = isDonatorMode(player);

        // Pick the default landing category based on which mode we are in.
        final Object categoryAttr = player.getTemporaryAttributes().get("GlobalShopCategory");
        final String defaultCategory = donatorMode ? DONATOR_BOOSTERS : "Vote Shop";
        final String category = (categoryAttr instanceof String) ? (String) categoryAttr : defaultCategory;

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

        // Relabel the first three tab buttons when in donator mode so the
        // client shows "Boosters", "Boxes", "Other" instead of the default
        // currency names.  Adjust TAB*_TEXT_COMPONENT constants above if
        // the text does not appear in-game (depends on cache component layout).
        if (donatorMode) {
            dispatcher.sendComponentText(getId(), TAB1_TEXT_COMPONENT, "Boosters");
            dispatcher.sendComponentText(getId(), TAB2_TEXT_COMPONENT, "Boxes");
            dispatcher.sendComponentText(getId(), TAB3_TEXT_COMPONENT, "Other");
        }
    }

    /**
     * Opens a shop category: loads the shop, populates the container, and
     * sends an informational message showing the player's relevant balance.
     */
    private void openShopCategory(Player player, String shopName) {
        player.getTemporaryAttributes().put("GlobalShopCategory", shopName);

        final Shop shop = Shop.get(shopName, player.isIronman(), player);
        player.getTemporaryAttributes().put("Shop", shop);
        final PacketDispatcher dispatcher = player.getPacketDispatcher();
        final Shop.ShopContainer container = shop.getContainer();

        // Initialize the shop container with script 1074 (shop name and container type).
        dispatcher.sendClientScript(1074, shop.getName(), container.getType().getId());
        // Display the shop name in the header.
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

        // Inform the player of their current balance for the active currency.
        if (isDonatorMode(player)) {
            player.sendMessage("You have "
                    + Colour.RED.wrap(player.getDonorPoints()) + " Donor Points.");
        } else if ("Vote Shop".equals(shopName)) {
            player.sendMessage("You have - Vote: "
                    + Colour.RED.wrap(VotePlayerAttributesKt.getTotalVoteCredits(player)) + " pts");
        } else if ("Loyalty Shop".equals(shopName)) {
            player.sendMessage("You have - Loyalty: "
                    + Colour.RED.wrap(player.getLoyaltyManager().getLoyaltyPoints()) + " pts");
        } else if ("Bounty Hunter Shop".equals(shopName)) {
            player.sendMessage("You have - Bounty Hunter: "
                    + Colour.RED.wrap(player.getBountyHunterPoints()) + " pts");
        }
    }

    @Override
    protected void build() {
        // -------------------------------------------------------------------
        // Category tab buttons.
        //
        // The first three tabs double as donator category selectors when
        // DonatorShopMode is active.  All other tabs are unaffected.
        // -------------------------------------------------------------------
        bind("vote", (player, slot, itemId, option) -> {
            if (isDonatorMode(player)) openShopCategory(player, DONATOR_BOOSTERS);
            else                       openShopCategory(player, "Vote Shop");
        });
        bind("loyalty", (player, slot, itemId, option) -> {
            if (isDonatorMode(player)) openShopCategory(player, DONATOR_BOXES);
            else                       openShopCategory(player, "Loyalty Shop");
        });
        bind("blood_money", (player, slot, itemId, option) -> {
            if (isDonatorMode(player)) openShopCategory(player, DONATOR_OTHER);
            else                       openShopCategory(player, "PVP SHOP");
        });
        bind("bh",        (player, slot, itemId, option) -> openShopCategory(player, "Bounty Hunter Shop"));
        bind("supplies",  (player, slot, itemId, option) -> openShopCategory(player, "Consumables Store"));
        bind("jewellery", (player, slot, itemId, option) -> openShopCategory(player, "Jewellery Store"));
        bind("melee",     (player, slot, itemId, option) -> openShopCategory(player, "Melee Store"));
        bind("range",     (player, slot, itemId, option) -> openShopCategory(player, "Ranged Store"));
        bind("magic",     (player, slot, itemId, option) -> openShopCategory(player, "Magic Store"));
        bind("skilling",  (player, slot, itemId, option) -> openShopCategory(player, "Tools Store"));

        // -------------------------------------------------------------------
        // Item container — handles buying interactions.
        // -------------------------------------------------------------------
        bind("container", (player, interfaceSlotId, itemId, option) -> {
            final int slotId = interfaceSlotId; // zero-based slot in the shop container
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
