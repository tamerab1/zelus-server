package com.zenyte.game.util;

import com.zenyte.game.world.entity.player.Player;

import java.sql.*;
/**
 Edited by Xander
 */
public class Votes {


    public static final String HOST = ""; // website ip address
    public static final String USER = "";
    public static final String PASS = "";
    public static final String DATABASE = "";
    private static Player player;
    private static Connection conn;
    private static Statement stmt;

    /**
     * The constructor
     *
     * @param player
     */
    public Votes(Player player) {
        this.player = player;
    }

    public static void run(Player player) {
        try {
            player.lock();
            if (!connect(HOST, DATABASE, USER, PASS, player)) {
                return;
            }

            String name = player.getUsername().replace(" ", "_");
            ResultSet rs = executeQuery("SELECT * FROM fx_votes WHERE username='" + name + "' AND claimed=0");
            player.sendMessage("Checking for votes...");
            int count = 0;

            while (rs.next()) {
                String ipAddress = rs.getString("ip_address");
                int siteId = rs.getInt("site_id");

                // Reward logic
                int itemId = 32148; // vote shard reward
                int quantity = 3;

                if (player.getInventory().hasSpaceFor(itemId, quantity)) {
                    player.getInventory().addItem(itemId, quantity);
                    player.sendMessage("Received vote from site: " + siteId);
                } else {
                    // If inventory is full, send items to bank
                    player.getBank().addItem(itemId, quantity);
                    player.sendMessage("Your inventory was full. The items have been sent to your bank.");
                }

                // Mark the vote as claimed in the database
                rs.updateInt("claimed", 1);
                rs.updateRow();
                count++;
            }

            // Additional rewards for voting on 2 or more sites
            if (count >= 2) {
                int mysteryBoxId = 6199;
                int mysteryBoxQuantity = 2;

                if (player.getInventory().hasSpaceFor(mysteryBoxId, mysteryBoxQuantity)) {
                    player.getInventory().addItem(mysteryBoxId, mysteryBoxQuantity);
                    player.sendMessage("You've voted on " + count + " sites and earned a mystery box!");
                } else {
                    // If inventory is full, send items to bank
                    player.getBank().addItem(mysteryBoxId, mysteryBoxQuantity);
                    player.sendMessage("Your inventory was full. The mystery boxes have been sent to your bank.");
                }
            }

            if (count == 0) {
                player.sendMessage("You have no votes to claim.");
            } else {
                player.sendMessage("Your rewards have been added to your inventory.");
            }

            player.unlock();
            destroy();
        } catch (Exception e) {
            e.printStackTrace();
            player.unlock();
            player.sendMessage("Something went wrong. Please try again. If it continues, contact staff.");
        } finally {
            // Ensure resources are closed
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

