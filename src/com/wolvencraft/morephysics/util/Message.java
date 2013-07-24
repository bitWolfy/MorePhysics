/*
 * Util.java
 * 
 * MorePhysics
 * Copyright (C) 2013 FriedTaco, bitWolfy, and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.wolvencraft.morephysics.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wolvencraft.morephysics.Configuration;
import com.wolvencraft.morephysics.MorePhysics;

public class Message {
    private static Logger logger = MorePhysics.getInstance().getLogger();
    
    private Message() { }
    
    /**
     * Send a message to the specified CommandSender.<br />
     * Using CommandSender allows to easily send messages both to Player and ConsoleCommandSender.
     * @param sender CommandSender to forward the message to
     * @param message Message to be sent
     */
    public static void send(CommandSender sender, String message) {
        if(sender == null) sender = Bukkit.getServer().getConsoleSender();
        if(message == null) return;
        message = parseChatColors(message);
        sender.sendMessage(message);
    }
    
    /**
     * Builds and sends a message with an attached title.<br />
     * Using CommandSender allows to easily send messages both to Player and ConsoleCommandSender.
     * @param sender CommandSender to forward the message to
     * @param titleColor Color of the title
     * @param title Title to attach to the message
     * @param message Message to be sent
     */
    public static void sendFormatted(CommandSender sender, ChatColor titleColor, String title, String message) {
        if(message == null) return;
        message = titleColor + "[" + title + "] " + ChatColor.WHITE + message;
        send(sender, message);
    }
    
    /**
     * Builds and sends a message with a red-colored title.
     * @param sender CommandSender to forward the message to
     * @param message Message to be sent
     */
    public static void sendFormattedError(CommandSender sender, String message) {
        sendFormatted(sender, ChatColor.DARK_RED, Configuration.LogPrefix.toString(), message);
    }
    
    /**
     * Broadcasts a message to all players on the server
     * @param message Message to be sent
     */
    public static void broadcast(String message) {
        for (Player p : Bukkit.getServer().getOnlinePlayers())
            sendFormatted(p, ChatColor.DARK_GREEN, Configuration.LogPrefix.toString(), message);
        log(parseChatColors(message));
    }
    
    /**
     * Sends a message into the server log
     * @param messages Messages to be sent
     */
    public static void log(String... messages) {
        for(String message : messages) logger.info(message);
    }
    
    /**
     * Sends a message into the server log
     * @param level Severity level
     * @param messages Messages to be sent
     */
    public static void log(Level level, String... messages) {
        for(String message : messages) logger.log(level, message);
    }
    
    /**
     * Sends a message into the server log if debug is enabled in the configuration.<br />
     * Should not be used if there is more then one line to be sent to the console.
     * @param message Message to be sent
     */
    public static void debug(String... message) {
        if (Configuration.Debug.toBoolean()) log(message);
    }
    
    /**
     * Sends a message into the server log if debug is enabled in the configuration.<br />
     * Should not be used if there is more then one line to be sent to the console.
     * @param level Severity level
     * @param message Message to be sent
     */
    public static void debug(Level level, String... message) {
        if (Configuration.Debug.toBoolean()) log(level, message);
    }
    
    /**
     * Centers the string to take up the specified number of characters
     * @param str String to center
     * @param length New String length
     * @return New String
     */
    public static String centerString(String str, int length) {
        while(str.length() < length) str = " " + str + " ";
        if(str.length() > length) str = str.substring(1);
        return str;
    }
    
    /**
     * Fills the string with spaces to match the specified length
     * @param str String to fill
     * @param length New String length
     * @return New String
     */
    public static String fillString(String str, int length) {
        while(str.length() < length) str += " ";
        return str;
    }
    
    /**
     * Parses color codes in the String and replaces them with ChatColors
     * @param str String to parse
     * @return Result string
     */
    private static String parseChatColors(String str) {
        if(str == null) return "";
        for(ChatColor color : ChatColor.values()) str = str.replaceAll("&" + color.getChar(), color + "");
        return str;
    }
}
