    package com.zenyte.plugins.renewednpc;

    import com.near_reality.api.service.user.UserPlayerHandler;
    import com.near_reality.game.item.CustomItemId;
    import com.near_reality.game.world.entity.player.PlayerAttributesKt;
    import com.zenyte.ContentConstants;
    import com.zenyte.game.GameInterface;
    import com.zenyte.game.content.achievementdiary.AdventurersLogIcon;
    import com.zenyte.game.item.Item;
    import com.zenyte.game.item.ItemId;
    import com.zenyte.game.model.item.degradableitems.DegradableItem;
    import com.zenyte.game.model.ui.InterfacePosition;
    import com.zenyte.game.util.Colour;
    import com.zenyte.game.world.World;
    import com.zenyte.game.world.broadcasts.BroadcastType;
    import com.zenyte.game.world.broadcasts.WorldBroadcasts;
    import com.zenyte.game.world.entity.Location;
    import com.zenyte.game.world.entity.npc.NPC;
    import com.zenyte.game.world.entity.npc.NpcId;
    import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
    import com.zenyte.game.world.entity.player.LogLevel;
    import com.zenyte.game.world.entity.player.Player;
    import com.zenyte.game.world.entity.player.cutscene.impl.EdgevilleCutscene;
    import com.zenyte.game.world.entity.player.dailychallenge.challenge.DailyChallenge;
    import com.zenyte.game.world.entity.player.dialogue.Dialogue;
    import com.zenyte.game.world.entity.player.privilege.ExpConfiguration;
    import com.zenyte.game.world.entity.player.privilege.ExpConfigurations;
    import com.zenyte.game.world.entity.player.privilege.GameMode;
    import kotlin.Unit;
    import mgi.utilities.StringFormatUtil;
    import org.jetbrains.annotations.NotNull;

    import java.util.*;
    import java.util.concurrent.ConcurrentHashMap;

    public class ZenyteGuide extends NPCPlugin {
        private static final String GLOBAL_STARTER_REWARD_TRACKER = "ipStarterRewardMap";
        private static final String STARTER_REWARD_CLAIMED_ATTR = "starterRewardClaimed";

        public static final int NPC_ID = NpcId.NEARREALITY_GUIDE;

        public static final Item[][] STARTER_ITEMS = {
                { // normal (niet meer gebruikt)
                        new Item(995, 50000)
                },
                { // ironman
                        new Item(12810), new Item(12811), new Item(12812)
                },
                { // ultimate ironman
                        new Item(12813), new Item(12814), new Item(12815)
                },
                { // hardcore ironman
                        new Item(20792), new Item(20794), new Item(20796)
                },
                { // group ironman
                        new Item(ItemId.GROUP_IRON_HELM),
                        new Item(ItemId.GROUP_IRON_PLATEBODY),
                        new Item(ItemId.GROUP_IRON_PLATELEGS),
                        new Item(ItemId.GROUP_IRON_BRACERS),
                },
                { // hardcore group ironman
                        new Item(ItemId.HARDCORE_GROUP_IRON_HELM),
                        new Item(ItemId.HARDCORE_GROUP_IRON_PLATEBODY),
                        new Item(ItemId.HARDCORE_GROUP_IRON_PLATELEGS),
                        new Item(ItemId.HARDCORE_GROUP_IRON_BRACERS),
                }
        };

        private static final Item[] PVP_STARTER_ITEMS = {
                new Item(ItemId.ARMADYL_GODSWORD),
                new Item(ItemId.BLOOD_MONEY, 1000),
                new Item(ItemId.NR_VOTE_SHARD, 10),
        };

        private static final Item EXTRA_TUTORIAL_GOLD = new Item(13307, 1000);
        public static final Location HOME_ZENYTE_GUIDE = new Location(3086, 3491);
        public static final Location SPAWN_LOCATION = new Location(3087, 3502);
        public static boolean disableJoinAnnouncement = false;

        public static void finishAppearance(final Player player) {
            player.lock();

            // Alleen ironman varianten krijgen starter sword/bow/staff
            GameMode gm = PlayerAttributesKt.getSelectedGameMode(player);
            if (gm == GameMode.STANDARD_IRON_MAN
                    || gm == GameMode.HARDCORE_IRON_MAN
                    || gm == GameMode.ULTIMATE_IRON_MAN
                    || gm == GameMode.GROUP_IRON_MAN) {

                player.sendMessage("You've received a set of starter weapons.");
                takeWeapon(player, ItemId.STARTER_SWORD);
                takeWeapon(player, ItemId.STARTER_BOW);
                takeWeapon(player, ItemId.STARTER_STAFF);
            }

            // Tutorial dialoog
            player.getDialogueManager().start(new Dialogue(player, NPC_ID) {
                @Override
                public void buildDialogue() {
                    npc("Would you like to view the tutorial so I can show you around our home area or do you want to skip it?");
                    options("Would you like to view the tutorial?",
                            "Yes. <col=00080>(Receive extra " + StringFormatUtil.format(EXTRA_TUTORIAL_GOLD.getAmount()) + " Blood money)</col>",
                            "No, I want to skip it.")
                            .onOptionOne(() -> {
                                player.putBooleanTemporaryAttribute("viewed_tutorial", true);
                                player.getPacketDispatcher().sendClientScript(10700, 1);
                                player.getPacketDispatcher().sendClientScript(42, 0, 200);
                                player.setLocation(SPAWN_LOCATION);
                                player.lock();
                                player.getCutsceneManager().play(new EdgevilleCutscene());
                            })
                            .onOptionTwo(() -> {
                                player.putBooleanTemporaryAttribute("viewed_tutorial", false);
                                finishTutorial(player);
                            });
                }
            });
        }

        private static void takeWeapon(@NotNull final Player player, final int weaponId) {
            final Item weapon = new Item(weaponId, 1, DegradableItem.getFullCharges(weaponId));
            player.getInventory().addOrDrop(weapon);
        }

        public static void finishTutorial(final Player player) {
            player.getAppearance().setInvisible(false);
            player.unlock();

            if (player.getBooleanTemporaryAttribute("tutorial_rewatch")) {
                player.setLocation(HOME_ZENYTE_GUIDE);
                player.getInterfaceHandler().closeInterface(InterfacePosition.DIALOGUE);
                player.addTemporaryAttribute("tutorial_rewatch", 0);
                return;
            }

            if (!disableJoinAnnouncement) {
                WorldBroadcasts.broadcast(player, BroadcastType.NEW_PLAYER);
            }
            World.findNPC(NPC_ID, HOME_ZENYTE_GUIDE).ifPresent(npc ->
                    npc.setForceTalk("Welcome " + player.getName() + " to " + ContentConstants.SERVER_NAME + "!")
            );

            player.getDialogueManager().start(new Dialogue(player, NPC_ID) {
                @Override
                public void buildDialogue() {
                    npc("That should be everything regarding our home area.");
                    npc("Before i forget. Because this is a PVP server, to find your PVP spawn presets click the equipment tab and the preset button will show on the right top!");
                    npc("To make the fastest money i suggest you to enter wilderness and kill players for Blood money, Bounty hunter points and achievement rewards.");
                    npc("If I went a bit too fast for you, you can always come and visit me in the achievement hall and I'll show you around whenever you want.");
                    npc("Good luck on your adventure.");
                }
            });

            final GameMode gameMode = PlayerAttributesKt.getSelectedGameMode(player);

            UserPlayerHandler.INSTANCE.updateGameMode(player, gameMode, (success) -> {
                if (!success) {
                    player.log(LogLevel.ERROR, "Failed to update game-mode " + gameMode + " in API, setting anyways.");
                    player.setGameMode(gameMode);
                }

                if (alreadyClaimedStarter(player)) {
                    player.sendMessage("You have already claimed a starter reward on this IP/MAC.");
                } else {
                    if (player.getGameMode() == GameMode.REGULAR) {
                        player.getInventory().addItem(new Item(ItemId.BLOOD_MONEY, 1000));
                        player.getInventory().addItem(new Item(ItemId.ARMADYL_GODSWORD, 1));
                    }

                    if (player.isIronman()) {
                        final Item[] items = STARTER_ITEMS[player.getGameMode().ordinal()];
                        for (final Item item : items) {
                            final Item it = new Item(item.getId(), item.getAmount(),
                                    DegradableItem.getDefaultCharges(item.getId(), 0));
                            player.getInventory().addItem(it);
                        }
                    }

                    /*if (player.getGameMode() == GameMode.PVP) {
                        for (final Item item : PVP_STARTER_ITEMS) {
                            final Item it = new Item(item.getId(), item.getAmount(),
                                    DegradableItem.getDefaultCharges(item.getId(), 0));
                            player.getInventory().addItem(it);
                        }
                    }*/

                    markStarterClaimed(player);
                }

                // Founder's Cape + tutorial bonus
                if (!player.getBooleanAttribute("claimedFounders")
                        && Calendar.getInstance().get(Calendar.YEAR) == 2024
                        && Calendar.getInstance().get(Calendar.MONTH) == Calendar.MARCH
                        && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) < 19) {
                    player.sendMessage(Colour.RS_GREEN.wrap("Thank you for joining " + ContentConstants.SERVER_NAME + " on our launch weekend!"));
                    player.sendMessage(Colour.RS_GREEN.wrap("The powerful Founder's Cape has been added to your inventory."));
                    player.getInventory().addItem(new Item(CustomItemId.FOUNDERS_CAPE));
                    player.putBooleanAttribute("claimedFounders", true);
                }
                if (player.getBooleanTemporaryAttribute("viewed_tutorial")) {
                    player.getInventory().addItem(EXTRA_TUTORIAL_GOLD);
                }

                player.getTemporaryAttributes().remove("registration");
                player.getTemporaryAttributes().remove("viewed_tutorial");
                player.putBooleanAttribute("registered", true);

                final DailyChallenge challenge = player.getDailyChallengeManager().getRandomChallenge();
                if (challenge != null) player.getDailyChallengeManager().assignChallenge(challenge);

                return Unit.INSTANCE;
            });
        }

        // -----------------------------
        // Starter reward IP/MAC limiter
        // -----------------------------
        @SuppressWarnings("unchecked")
        private static boolean alreadyClaimedStarter(Player player) {
            if (player.getBooleanAttribute(STARTER_REWARD_CLAIMED_ATTR)) {
                return true;
            }
            String ip = player.getIP();
            if (ip != null) {
                Object obj = World.getTemporaryAttributes().get(GLOBAL_STARTER_REWARD_TRACKER);
                if (!(obj instanceof Set)) {
                    obj = ConcurrentHashMap.newKeySet();
                    World.getTemporaryAttributes().put(GLOBAL_STARTER_REWARD_TRACKER, obj);
                }
                Set<String> ipSet = (Set<String>) obj;
                if (ipSet.contains(ip)) {
                    return true;
                }
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        private static void markStarterClaimed(Player player) {
            player.putBooleanAttribute(STARTER_REWARD_CLAIMED_ATTR, true);
            String ip = player.getIP();
            if (ip != null) {
                Object obj = World.getTemporaryAttributes().get(GLOBAL_STARTER_REWARD_TRACKER);
                if (!(obj instanceof Set)) {
                    obj = ConcurrentHashMap.newKeySet();
                    World.getTemporaryAttributes().put(GLOBAL_STARTER_REWARD_TRACKER, obj);
                }
                Set<String> ipSet = (Set<String>) obj;
                ipSet.add(ip);
            }
        }

        @Override
        public void handle() {
            bind("Talk-to", new OptionHandler() {
                @Override
                public void handle(Player player, NPC npc) {
                    player.stopAll();
                    player.faceEntity(npc);
                    if (!player.getBooleanAttribute("registered")) {
                        player.getDialogueManager().start(new Dialogue(player, npc) {
                            @Override
                            public void buildDialogue() {
                                npc("Greetings! I see you are a new arrival to this world.");
                                npc("Before you continue your adventure, could you please select a game mode you see fit?")
                                        .executeAction(() -> {
                                            player.sendMessage("When you are done selecting a game mode, simply close the interface to finalise your decision.");
                                            player.getTemporaryAttributes().put("ironman_setup", "register");
                                            player.getVarManager().sendVar(281, 0);
                                            player.getVarManager().sendBit(1777, 0);
                                            player.setExperienceMultiplier(250, 80);
                                            GameInterface.CREATEACCOUNT.open(player);
                                        });
                            }
                        });
                    } else {
                        player.getDialogueManager().start(new Dialogue(player, npc) {
                            @Override
                            public void buildDialogue() {
                                npc("Hey! It's good to see you again, " + player.getName() + ". What can I do for you?");
                                options(TITLE, "Talk about my experience mode.", "Rewatch the tutorial.")
                                        .onOptionOne(() -> setKey(5))
                                        .onOptionTwo(() -> {
                                            player.lock();
                                            player.getPacketDispatcher().sendClientScript(10700, 1);
                                            player.getPacketDispatcher().sendClientScript(42, 0, 200);
                                            player.setLocation(SPAWN_LOCATION);
                                            player.addTemporaryAttribute("tutorial_rewatch", 1);
                                            player.getCutsceneManager().play(new EdgevilleCutscene());
                                        });
                                ExpConfigurations config = ExpConfigurations.of(player.getGameMode());
                                if (config != null) {
                                    int currentIndex = config.getExpConfigurationIndex(player.getCombatXPRate(), player.getSkillingXPRate());
                                    ExpConfiguration currentExp = config.getConfigurations()[currentIndex];
                                    ExpConfiguration[] easier = config.getEasier(currentIndex);
                                    if (easier != null) {
                                        String[] options = Arrays.stream(easier).map(ExpConfiguration::getString).toArray(String[]::new);
                                        npc(5, "I see that you're on the <col=00080>" + currentExp.getString() + " mode</col>. Would you like to change to an easier mode?");
                                        options("Select the experience mode", options)
                                                .onOptionOne(() -> setKey(selectConfigurationMode(player, easier, 0)))
                                                .onOptionTwo(() -> setKey(selectConfigurationMode(player, easier, 1)))
                                                .onOptionThree(() -> setKey(selectConfigurationMode(player, easier, 2)))
                                                .onOptionFour(() -> setKey(selectConfigurationMode(player, easier, 3)))
                                                .onOptionFive(() -> setKey(selectConfigurationMode(player, easier, 4)));
                                    } else {
                                        npc(5, "There are no easier modes available for you.");
                                    }
                                } else {
                                    npc(5, "There are no easier modes available for you.");
                                }
                                npc(35, "Please be aware you cannot revert this change once accepted.").executeAction(() -> confirmDialogue(player, npc));
                            }
                        });
                    }
                }
            });
        }

        private static void confirmDialogue(Player player, NPC npc) {
            player.getDialogueManager().start(new Dialogue(player, npc) {
                @Override
                public void buildDialogue() {
                    options("Choose " + (((ExpConfiguration) player.getTemporaryAttributes().get("review_exp")).getString()) + "?",
                            "Yes please", "No thanks")
                            .onOptionOne(() -> {
                                setMode(player, (ExpConfiguration) player.getTemporaryAttributes().get("review_exp"));
                                showNewMode(player, npc);
                            })
                            .onOptionTwo(() -> setKey(15));
                    npc(15, "That's fine, come back if you change your mind!");
                }
            });
        }

        private static void showNewMode(Player player, NPC npc) {
            player.getDialogueManager().start(new Dialogue(player, npc) {
                @Override
                public void buildDialogue() {
                    npc("Your new exp mode is " + player.getCombatXPRate() + "x combat, " + player.getSkillingXPRate() + "x skilling.");
                }
            });
        }

        private static int selectConfigurationMode(Player player, ExpConfiguration[] easier, int index) {
            if (index >= easier.length) {
                return 25;
            } else {
                player.addTemporaryAttribute("review_exp", easier[index]);
                return 35;
            }
        }

        private static void setMode(Player player, ExpConfiguration expConfiguration) {
            player.setExperienceMultiplier(expConfiguration);
            player.sendAdventurersEntry(AdventurersLogIcon.OVERALL_SKILLING,
                    player.getName() + " has just changed exp mode - now playing under " + expConfiguration + "!");
        }

        @Override
        public int[] getNPCs() {
            return new int[]{NPC_ID};
        }
    }
