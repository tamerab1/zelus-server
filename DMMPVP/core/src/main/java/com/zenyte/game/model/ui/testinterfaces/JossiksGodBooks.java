package com.zenyte.game.model.ui.testinterfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.content.GodBooks;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kris | 12/06/2019 08:06
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class JossiksGodBooks extends Interface {
    @Override
    protected void attach() {
        put(3, "Fury Kit");
        put(4, "AGS Kit");
        put(5, "Dragon claws Kit");
        put(6, "Torture Kit");
        put(7, "Holy Kit");
        put(8, "Sanguine Kit");
    }


    @Override
    public void open(final Player player) {
        player.getInterfaceHandler().sendInterface(this);
        for (final GodBooks.GodBook book : GodBooks.GodBook.values) {
            refreshBook(player, book);
        }
    }

    private void refreshBook(@NotNull final Player player, @NotNull final GodBooks.GodBook book) {
        int value = 0;
        final GodBooks books = player.getGodBooks();
        if (books.getClaimedBooks().contains(book)) {
            value = 2;
            if (!player.containsItem(book.getCompletedBookId()) && !player.containsItem(book.getDamagedBookId())) {
                value = 1;
            }
        }
        // kits hebben geen pages: als het een kit is of compleet → 3
        if (book.getPages().length == 0) {
            if (player.containsItem(book.getDamagedBookId()) || player.containsItem(book.getCompletedBookId())) {
                value = 3; // Heeft de kit echt in inventory → complete
            } else if (books.getClaimedBooks().contains(book)) {
                value = 1; // Kit ooit gekocht, maar niet op inventory → missing
            } else {
                value = 0; // Niet gekocht → not owned
            }
        } else if (books.getPages(book).size() >= 4) {
            value = 3;
        }

        player.getVarManager().sendVar(261 + book.ordinal(), value);
    }


    @Override
    protected void build() {
        bind("Fury Kit", player -> manageBook(player, GodBooks.GodBook.FURY_KIT));
        bind("AGS Kit", player -> manageBook(player, GodBooks.GodBook.AGS_KIT));
        bind("Dragon claws Kit", player -> manageBook(player, GodBooks.GodBook.DCLAWS_KIT));
        bind("Torture Kit", player -> manageBook(player, GodBooks.GodBook.TORTURE_KIT));
        bind("Holy Kit", player -> manageBook(player, GodBooks.GodBook.HOLY_KIT));
        bind("Sanguine Kit", player -> manageBook(player, GodBooks.GodBook.SANGUINE_KIT));
    }


    private final void manageBook(@NotNull final Player player, @NotNull final GodBooks.GodBook book) {
        if (!player.getInventory().hasFreeSlots()) {
            player.sendMessage("You need some free inventory space to do that.");
            return;
        }

        final GodBooks books = player.getGodBooks();
        final boolean ownsBook = player.containsItem(book.getDamagedBookId()) || player.containsItem(book.getCompletedBookId());

        // Als hij het al eerder gekocht heeft
        if (books.getClaimedBooks().contains(book)) {
            if (ownsBook) {
                player.sendMessage("You already own this kit.");
                return;
            }

            // Niet meer in bezit → heraankoop met kosten
            player.getDialogueManager().start(new Dialogue(player) {
                @Override
                public void buildDialogue() {
                    item(new Item(book.getCompletedBookId()), "You’ve already unlocked this kit.<br>Pay 25.000 blood money to reclaim it?");
                    options("Reclaim the kit?", new DialogueOption("Reclaim it.", () -> {
                        if (!player.getInventory().hasFreeSlots()) {
                            player.sendMessage("You need some free inventory space to do that.");
                            return;
                        }
                        if (!player.getInventory().containsItem(13307, 25000)) {
                            player.sendMessage("You need at least 25000 blood money to reclaim the kit.");
                            return;
                        }
                        player.getInventory().deleteItem(13307, 25000);
                        player.getInventory().addOrDrop(new Item(book.getDamagedBookId()));
                        refreshBook(player, book); // Var update
                        //player.getInterfaceHandler().closeInterfaces();
                        //GameInterface.JOSSIKS_SALVAGED_GODBOOKS.open(player);
                    }), new DialogueOption("Cancel."));
                }
            });
            return;
        }

        // Nog niet gekocht → normale unlock
        player.getDialogueManager().start(new Dialogue(player) {
            @Override
            public void buildDialogue() {
                item(new Item(book.getCompletedBookId()), "Unlock this ornament kit for 25.000 blood money?");
                options("Unlock the kit?", new DialogueOption("Unlock it.", () -> {
                    if (!player.getInventory().hasFreeSlots()) {
                        player.sendMessage("You need some free inventory space to do that.");
                        return;
                    }
                    if (!player.getInventory().containsItem(13307, 25000)) {
                        player.sendMessage("You need at least 25000 Blood money to unlock a kit.");
                        return;
                    }
                    player.getInventory().deleteItem(13307, 25000);
                    player.getInventory().addOrDrop(new Item(book.getDamagedBookId()));
                    books.getClaimedBooks().add(book);
                    refreshBook(player, book); // voor directe update van varps
                    //player.getInterfaceHandler().closeInterfaces();
                    //GameInterface.JOSSIKS_SALVAGED_GODBOOKS.open(player);
                }), new DialogueOption("Cancel."));
            }
        });
    }


    @Override
    public GameInterface getInterface() {
        return GameInterface.JOSSIKS_SALVAGED_GODBOOKS;
    }
}
