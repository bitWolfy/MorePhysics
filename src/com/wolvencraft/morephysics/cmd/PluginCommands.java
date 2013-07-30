/*
 * PluginCommands.java
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

package com.wolvencraft.morephysics.cmd;

import java.util.List;

import org.bukkit.ChatColor;

import com.wolvencraft.morephysics.CommandManager;
import com.wolvencraft.morephysics.CommandManager.Command;
import com.wolvencraft.morephysics.CommandManager.CommandPair;
import com.wolvencraft.morephysics.Configuration;
import com.wolvencraft.morephysics.MorePhysics;
import com.wolvencraft.morephysics.util.Message;

public class PluginCommands {
    
    @Command(
            alias = "sync",
            minArgs = 0,
            maxArgs = 0,
            permission = "morephysics.cmd.reload",
            allowConsole = true,
            usage = "/morephysics reload",
            description = "Reloads plugin configuration from the database"
            )
    public static boolean reload(List<String> args) {
        MorePhysics.getComponentManager().disable();
        MorePhysics.getComponentManager().enable();
        Configuration.clearCache();
        Message.send("Plugin configuration has been reloaded");
        return false;
    }
    
    @Command(
            alias = "help",
            minArgs = 0,
            maxArgs = 0,
            permission = "stats.cmd.help",
            allowConsole = true,
            usage = "/stats help",
            description = "Full command help listing"
            )
    public static boolean help(List<String> args) {
        Message.formatHeader(20, "Statistics Help");
        for(CommandPair command : CommandManager.getCommands()) {
            Command cmd = command.getProperties();
            Message.send(ChatColor.GREEN + cmd.usage() + " " + ChatColor.GRAY + cmd.description());
        }
        return true;
    }
    
}
