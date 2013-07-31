/*
 * Util.java
 * 
 * Physics
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

package com.shackledmc.physics.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * A set of methods used to perform operations with blocks
 * @author bitWolfy
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class Util {
    
    /**
     * Takes in <b>MaterialData</b> and returns a user-friendly name.<br />
     * Inverse method to <i>getBlockMaterial(String blockName)</i>
     * @param material MaterialData of the block
     * @return User-friendly block name
     */
    public static String getBlockName(MaterialData material) {
        String str[] = {material.getItemTypeId() + "", material.getData() + ""};
        String name = material.getItemType().toString().toLowerCase().replace("_", " ");
        String meta = parseMetadata(str, true);
        if(!meta.equalsIgnoreCase("0")) name = meta + " " + name;
        return name;
    }
    
    /**
     * Takes in a block name and returns the corresponding <b>MaterialData</b><br />
     * Accepts values with metadata, separated by a colon (`:`).<br />
     * Inverse method to <i>getBlockName(MaterialData material)</i>
     * @param blockName Name of the block
     * @return <b>MaterialData</b> if the block is valid, <b>null</b> otherwise
     */
    public static MaterialData getBlockMaterial(String blockName) {
        String[] parts = blockName.split(":");
        if(parts.length > 2) return null;
        
        MaterialData block = null;
        
        try {
            if(isNumeric(parts[0])) block = new MaterialData(Material.getMaterial(Integer.parseInt(parts[0])));
            else {
                MaterialHook ore = MaterialHook.match(parts[0]);
                if(ore != null) parts[0] = ore.getMaterial();
                block = new MaterialData(Material.getMaterial(parts[0].toUpperCase()));
            }
            
            parts[0] = block.getItemTypeId() + "";
            
            if(parts.length == 2) {
                if(!isNumeric(parts[1])) parts[1] = parseMetadata(parts, false);
                block.setData(Byte.parseByte(parts[1]));
            }
        } catch(Throwable t) { return null; }
        
        return block;
    }
    
    /**
     * Returns the data of the block specified
     * @param parts Block name
     * @return metadata of a block
     */
    public static String parseMetadata(String[] parts, boolean recursive) {
        if(recursive) {
            int data = Integer.parseInt(parts[1]);
            switch(Integer.parseInt(parts[0])) {
                case 5:
                case 6:
                case 17:
                case 18:
                {
                    if(data == 1) parts[1] = "pine";
                    else if(data == 2) parts[1] = "birch";
                    else if(data == 3) parts[1] = "jungle";
                    else parts[1] = "oak";

                    break;
                }
                case 24:
                {
                    if(data == 1) parts[1] = "chiseled";
                    else if(data == 2) parts[1] = "smooth";
                    else parts[1] = "";

                    break;
                }
                case 33:
                case 34:
                {
                    if(data == 1) parts[1] = "sandstone";
                    else if(data == 2) parts[1] = "wooden";
                    else if(data == 3) parts[1] = "cobblestone";
                    else if(data == 4) parts[1] = "brick";
                    else if(data == 5) parts[1] = "stone brick";
                    else if(data == 6) parts[1] = "smooth";
                    else parts[1] = "stone";

                    break;
                }
                case 35:
                {
                    if(data == 1) parts[1] = "orange";
                    else if(data == 2) parts[1] = "magenta";
                    else if(data == 3) parts[1] = "lightblue";
                    else if(data == 4) parts[1] = "yellow";
                    else if(data == 5) parts[1] = "lime";
                    else if(data == 6) parts[1] = "pink";
                    else if(data == 7) parts[1] = "gray";
                    else if(data == 8) parts[1] = "lightgray";
                    else if(data == 9) parts[1] = "cyan";
                    else if(data == 10) parts[1] = "purple";
                    else if(data == 11) parts[1] = "blue";
                    else if(data == 12) parts[1] = "brown";
                    else if(data == 13) parts[1] = "green";
                    else if(data == 14) parts[1] = "red";
                    else if(data == 15) parts[1] = "black";
                    else parts[1] = "white";

                    break;
                }
                case 84:
                {
                    if(data == 1) parts[1] = "gold disk";
                    else if(data == 2) parts[1] = "green disk";
                    else if(data == 3) parts[1] = "orange disk";
                    else if(data == 4) parts[1] = "red disk";
                    else if(data == 5) parts[1] = "lime disk";
                    else if(data == 6) parts[1] = "purple disk";
                    else if(data == 7) parts[1] = "violet disk";
                    else if(data == 8) parts[1] = "black disk";
                    else if(data == 9) parts[1] = "white disk";
                    else if(data == 10) parts[1] = "sea green disk";
                    else if(data == 11) parts[1] = "broken disk";
                    else parts[1] = "";

                    break;
                }
                case 98:
                {
                    if(data == 1) parts[1] = "mossy";
                    else if(data == 2) parts[1] = "cracked";
                    else if(data == 3) parts[1] = "chiseled";
                    else parts[1] = "";
                }
                default:
                {
                    if(data == 0) parts[1] = "";
                }
            }
        } else {
            if(parts[0].equalsIgnoreCase("5") || parts[0].equalsIgnoreCase("6") || parts[0].equalsIgnoreCase("17") || parts[0].equalsIgnoreCase("18")) {
                if(parts[1].equalsIgnoreCase("dark") || parts[1].equalsIgnoreCase("pine") || parts[1].equalsIgnoreCase("spruce")) parts[1] = 1 + "";
                else if(parts[1].equalsIgnoreCase("birch")) parts[1] = 2 + "";
                else if(parts[1].equalsIgnoreCase("jungle")) parts[1] = 3 + "";
                else parts[1] = 0 + "";
            } else if(parts[0].equalsIgnoreCase("24")) {
                if(parts[1].equalsIgnoreCase("chiseled") || parts[1].equalsIgnoreCase("creeper")) parts[1] = 1 + "";
                else if(parts[1].equalsIgnoreCase("smooth")) parts[1] = 2 + "";
                else parts[1] = 0 + "";
            } else if(parts[0].equalsIgnoreCase("33") || parts[0].equalsIgnoreCase("34")) {
                if(parts[1].equalsIgnoreCase("sandstone")) parts[1] = 1 + "";
                else if(parts[1].equalsIgnoreCase("wooden") || parts[1].equalsIgnoreCase("wood") || parts[1].equalsIgnoreCase("plank")) parts[1] = 2+ "";
                else if(parts[1].equalsIgnoreCase("cobblestone") || parts[1].equalsIgnoreCase("cobble")) parts[1] = 3 + "";
                else if(parts[1].equalsIgnoreCase("brick")) parts[1] = 4 + "";
                else if(parts[1].equalsIgnoreCase("stonebrick") || parts[1].equalsIgnoreCase("stone_brick")) parts[1] = 5 + "";
                else if(parts[1].equalsIgnoreCase("smoothstone") || parts[1].equalsIgnoreCase("smooth")) parts[1] = 6 + "";
            } else if(parts[0].equalsIgnoreCase("35")) {
                if(parts[1].equalsIgnoreCase("orange")) parts[1] = 1 + "";
                else if(parts[1].equalsIgnoreCase("magenta")) parts[1] = 2 + "";
                else if(parts[1].equalsIgnoreCase("lightblue")) parts[1] = 3 + "";
                else if(parts[1].equalsIgnoreCase("yellow")) parts[1] = 4 + "";
                else if(parts[1].equalsIgnoreCase("lime")) parts[1] = 5 + "";
                else if(parts[1].equalsIgnoreCase("pink")) parts[1] = 6 + "";
                else if(parts[1].equalsIgnoreCase("gray")) parts[1] = 7 + "";
                else if(parts[1].equalsIgnoreCase("lightgray")) parts[1] = 8 + "";
                else if(parts[1].equalsIgnoreCase("cyan")) parts[1] = 9 + "";
                else if(parts[1].equalsIgnoreCase("purple")) parts[1] = 10 + "";
                else if(parts[1].equalsIgnoreCase("blue")) parts[1] = 11 + "";
                else if(parts[1].equalsIgnoreCase("brown")) parts[1] = 12 + "";
                else if(parts[1].equalsIgnoreCase("green")) parts[1] = 13 + "";
                else if(parts[1].equalsIgnoreCase("red")) parts[1] = 14 + "";
                else if(parts[1].equalsIgnoreCase("black")) parts[1] = 15 + "";
                else parts[1] = 0 + "";
            } else if(parts[0].equalsIgnoreCase("84")) {
                if(parts[1].equalsIgnoreCase("gold")) parts[1] = 1 + "";
                else if(parts[1].equalsIgnoreCase("green")) parts[1] = 2 + "";
                else if(parts[1].equalsIgnoreCase("orange")) parts[1] = 3 + "";
                else if(parts[1].equalsIgnoreCase("red")) parts[1] = 4 + "";
                else if(parts[1].equalsIgnoreCase("lime")) parts[1] = 5 + "";
                else if(parts[1].equalsIgnoreCase("purple")) parts[1] = 6 + "";
                else if(parts[1].equalsIgnoreCase("violet")) parts[1] = 7 + "";
                else if(parts[1].equalsIgnoreCase("black")) parts[1] = 8 + "";
                else if(parts[1].equalsIgnoreCase("white")) parts[1] = 9 + "";
                else if(parts[1].equalsIgnoreCase("seagreen")) parts[1] = 10 + "";
                else if(parts[1].equalsIgnoreCase("broken")) parts[1] = 11 + "";
                else parts[1] = 0 + "";
            } else if(parts[0].equalsIgnoreCase("98")) {
                if(parts[1].equalsIgnoreCase("mossy")) parts[1] = 1 + "";
                else if(parts[1].equalsIgnoreCase("cracked")) parts[1] = 2 + "";
                else if(parts[1].equalsIgnoreCase("chiseled")) parts[1] = 3 + "";
                else parts[1] = 0 + "";
            }
        }
        return parts[1];
    }
    
    /**
     * Checks if a string is numeric
     * @param str String String to be checked
     * @return boolean True if a string is numeric
     */
    private static boolean isNumeric(String str) {  
      try { Double.parseDouble(str); }
      catch(NumberFormatException nfe) { return false; }  
      return true;  
    }
}