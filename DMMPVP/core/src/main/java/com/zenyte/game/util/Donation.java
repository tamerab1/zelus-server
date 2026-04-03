package com.zenyte.game.util;
import com.zenyte.game.world.entity.player.Player;

import java.sql.*;



/**
Edited by xander
 */
public class Donation {

    public static final String HOST = ""; // website ip address
    public static final String USER = "";
    public static final String PASS = "";
    public static final String DATABASE = "";
    private static Player player;
    private static Connection conn;
    private static Statement stmt;

    /**
     * The constructor
     * @param player
     */
    public Donation(Player player) {
        this.player = player;
    }


    public static void run(Player player) {
        try {
            player.lock();
            if (!connect(HOST, DATABASE, USER, PASS, player)) {
                return;
            }

            String name = player.getUsername().replace("_", " ");
            ResultSet rs = executeQuery("SELECT * FROM payments WHERE player_name='"+name+"' AND status='Completed' AND claimed=0");
            player.sendMessage("Checking items...");
            while (rs.next()) {
                int item_number = rs.getInt("item_number");
                double paid = rs.getDouble("amount");
                int quantity = rs.getInt("quantity");
                switch (item_number) {// add products according to their ID in the ACP
                    /**
                     * Boosters
                     *
                     */
                    case 56: // Larrans booster
                        player.getInventory().addItem(32149, 1 * quantity); //test deze ook moment ff db aanpassen
                        break;
                    case 57: // Gano booster
                        player.getInventory().addItem(32150, 1 * quantity);
//                        player.incrementCredits(50);

                        break;
                    case 58: // Slayer booster
                        player.getInventory().addItem(32151, 1 * quantity);
                        break;
                    case 59: // Pet booster
                        player.getInventory().addItem(32152, 1 * quantity);
                        break;
                    case 60: // Gauntlet booster
                        player.getInventory().addItem(32153, 1 * quantity);
                        break;
                    case 61: // Blood money booster
                        player.getInventory().addItem(32154, 1 * quantity);
                        break;
                    case 62: // Clue booster
                        player.getInventory().addItem(32155, 1 * quantity);
                        break;
                    case 63: // ToB booster
                        player.getInventory().addItem(32156, 1 * quantity);
                        break;
                    case 64: // Revenant booster
                        player.getInventory().addItem(32166, 1 * quantity);
                        break;
                    case 65: // Nex booster
                        player.getInventory().addItem(32167, 1 * quantity);
                        break; //ik stuur je zofilmpje dat je ziet dat het werkt ok
                    /**
                     * Mystery Boxes
                     */
                    case 67: // reg Mystery Box
                        player.getInventory().addItem(6199, 1 * quantity);
                        break;
                    case 68: // Pet Mystery Box
                        player.getInventory().addItem(30031, 1 * quantity);
                        break;
                    case 69: // Cosmetic Mystery Box
                        player.getInventory().addItem(32163, 1 * quantity);
                        break;
                    case 70: // Super Mystery Box
                        player.getInventory().addItem(32164, 1 * quantity);
                        break;
                    case 71: // Ultimate Mystery Box
                        player.getInventory().addItem(32165, 1 * quantity);
                        break;
                    case 72: // PvP Mystery Box
                        player.getInventory().addItem(32205, 1 * quantity);
                        break;
                    case 73: // Ultimate Mystery Box e
                        player.getInventory().addItem(32206, 1 * quantity);
                        break;
                    case 74: // 3rd Age Mystery Box
                        player.getInventory().addItem(32209, 1 * quantity);
                        break;
                    case 75: // Skilling Mystery Box
                        player.getInventory().addItem(32212, 1 * quantity);
                        break;
                    case 76: // 10 Ultimate 2 Ultimate e
                        player.getInventory().addItem(32215, 1 * quantity);
                        break;
                    case 77: // Regal Mystery Box
                        player.getInventory().addItem(32231, 1 * quantity);
                        break;
                    case 78: // Easter Mystery Box
                        player.getInventory().addItem(32357, 1 * quantity);
                        break;
                    /**
                     * Misc boosters
                     */
                        //drop rate boster
                    case 79:
                        player.getInventory().addItem(32201, 1 * quantity);
                        break;
                    /**
                     * Claimable bonds
                     */
                    case 80: //10 donation pin
                        player.getInventory().addItem(32070, 1 * quantity);
                        break;
                    case 81: //25 donation pin
                        player.getInventory().addItem(32071, 1 * quantity);
                        break;
                    case 82: //50 donation pin
                        player.getInventory().addItem(32072, 1 * quantity);
                        break;
                    case 83: //100 donation pin
                        player.getInventory().addItem(32073, 1 * quantity);
                        break;



                }
                player.sendMessage("Adding x" + quantity + " " + "...</col>");
                player.sendMessage("Thank you for supporting Zelus!");
                player.sendMessage("Your items have been added to your bank.");
                player.getPacketDispatcher().sendGlobalBroadcast("Another player donated towards the server!");

                rs.updateInt("claimed", 1); // do not delete otherwise they can reclaim!
                rs.updateRow();
            }
            if (!rs.first()) {
                player.sendMessage("You have no items to claim.");
            }
            player.unlock();
            destroy();
        } catch (Exception e) {
            player.unlock();
            player.sendMessage("Something went wrong, please try again. If it continues contact staff");
            e.printStackTrace();
        }
    }

    /**
     *
     * @param host the host ip address or url
     * @param database the name of the database
     * @param user the user attached to the database
     * @param pass the users password
     * @return true if connected
     */
    public static boolean connect(String host, String database, String user, String pass, Player player) {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+host+":3306"+"/"+database, user, pass);
            return true;
        } catch (SQLException e) {
            player.unlock();
            e.printStackTrace();
            System.out.println("Failing connecting to database! " + e.getErrorCode() + " state: " + e.getSQLState());
            return false;
        }
    }

    /**
     * Disconnects from the MySQL server and destroy the connection
     * and statement instances
     */
    public static void destroy() {
        try {
            conn.close();
            conn = null;
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes an update query on the database
     * @param query
     * @see {@link Statement#executeUpdate}
     */
    public int executeUpdate(String query) {
        try {
            stmt = conn.createStatement(1005, 1008);
            int results = stmt.executeUpdate(query);
            return results;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * Executres a query on the database
     * @param query
     * @see {@link Statement#executeQuery(String)}
     * @return the results, never null
     */
    public static ResultSet executeQuery(String query) {
        try {
            stmt = conn.createStatement(1005, 1008);
            ResultSet results = stmt.executeQuery(query);
            return results;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
