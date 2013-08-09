/*
 * ArrowComponent.java
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

package com.shackledmc.physics.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.Physics;
import com.shackledmc.physics.api.ProjectileCritEvent;
import com.shackledmc.physics.util.Experimental;
import com.shackledmc.physics.util.Experimental.ParticleEffectType;
import com.shackledmc.physics.util.Message;

/**
 * Arrow damage component.
 * 
 * Handles critical hits done by arrows.
 * @author bitWolfy
 *
 */
public class ArrowComponent extends Component implements Listener {
    
    private static final EntityType[] VALID_ENTITIES = {
        EntityType.BLAZE,
        EntityType.CREEPER,
        EntityType.IRON_GOLEM,
        EntityType.PIG_ZOMBIE,
        EntityType.PLAYER,
        EntityType.SKELETON,
        EntityType.SNOWMAN,
        EntityType.VILLAGER,
        EntityType.WITCH,
        EntityType.WITHER
    };
    
    private boolean playersOnly;
    private boolean effects;
    
    public ArrowComponent() {
        super(ComponentType.ARROW);
        
        if(!enabled) return;
        
        HitArea.clearCache();
        
        FileConfiguration configFile = Physics.getInstance().getConfig();
        effects = configFile.getBoolean("arrows.effects");
        playersOnly = configFile.getBoolean("arrows.players-only");
    }
    
    @Override
    public void onEnable() {
        if(effects && !Physics.isCraftBukkitCompatible()) {
            Message.log(
                    "|  |- Particle effects are not compatible with  |",
                    "|     your CraftBukkit version. Disabling...    |"
                    );
            effects = false;
        }
        
        Bukkit.getServer().getPluginManager().registerEvents(this, Physics.getInstance());
    }
    
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        
        // Check whether the damaging entity is a projectile
        if(!(event.getDamager() instanceof Projectile)) return;
        
        // Check for invalid worlds
        if(exemptWorlds.contains(event.getEntity().getWorld().getName())) return;
        
        // Check whether the damaged entity type is valid (i.e. humanoid-ish)
        if((playersOnly && !(event.getEntity() instanceof Player))
                || !isTypeValid(event.getEntityType()))  return;
        
        // If the damaged entity is a player, check for permissions
        if(event.getEntity() instanceof Player && !((Player) event.getEntity()).hasPermission(type.getPermission())) return;
        
        HitArea hitArea = HitArea.get(event.getDamager().getLocation().getY() - event.getEntity().getLocation().getY());
        
        double damageMultiplyer = hitArea.getDamage();
        
        ProjectileCritEvent apiEvent = new ProjectileCritEvent(event, hitArea, damageMultiplyer);
        Bukkit.getServer().getPluginManager().callEvent(apiEvent);
        if(apiEvent.isCancelled()) return;
        
        if(effects && (hitArea.equals(HitArea.HEAD) || hitArea.equals(HitArea.NECK) || hitArea.equals(HitArea.CROTCH))) {
            Experimental.createEffect(ParticleEffectType.CRIT, "", event.getEntity().getLocation(), 1f, 1f, 1f, 20);
        }
        
        event.setDamage(event.getDamage() * damageMultiplyer);
        
        hitArea.applyEffects((LivingEntity) event.getEntity());
    }
    
    /**
     * Checks whether the specified entity type is valid
     * @param type Entity type to check
     * @return <b>true</b> if the type is valid, <b>false</b> otherwise
     */
    private static boolean isTypeValid(EntityType type) {
        for(EntityType testType : VALID_ENTITIES) {
            if(testType.equals(type)) return true;
        }
        return false;
    }
    
    /**
     * Represents the area that was hit by a projectile
     * @author bitWolfy
     *
     */
    public enum HitArea {
        
        HEAD    ("head"),
        NECK    ("neck"),
        TORSO   ("torso"),
        CROTCH  ("crotch"),
        LEGS    ("legs"),
        TOE     ("toe"),
        
        UNKNOWN ("unknown"),
        ;
        
        private static Random random = new Random();
        
        private String key;
        private double damage;
        private double damageRange;
        private List<PotionEffectType> effects;
        
        HitArea(String key) {
            this.key = key;
            refresh();
        }
        
        private void refresh() {
            FileConfiguration configFile = Physics.getInstance().getConfig();
            damage = configFile.getDouble("arrows.modifiers." + key + ".damage");
            damageRange = configFile.getDouble("arrows.modifiers." + key + ".damage-range");
            
            List<String> effectsList = configFile.getStringList("arrows.modifiers." + key + ".effects");
            effects = new ArrayList<PotionEffectType>();
            for(String str : effectsList) {
                PotionEffectType effectType = PotionEffectType.getByName(str);
                if(effectType != null) effects.add(effectType);
            }
        }
        
        /**
         * Returns the randomized damage
         * @return Randomized damage within the pre-defined range
         */
        private double getDamage() {
            return damage + (random.nextDouble() * damageRange);
        }
        
        /**
         * Applies the potion effects to the entity
         * @param player Entity to apply the effects to
         */
        private void applyEffects(LivingEntity entity) {
            for(PotionEffectType effect : effects) { 
                entity.addPotionEffect(new PotionEffect(effect, 1000, 50));
            }
        }
        
        public static void clearCache() {
            for(HitArea area : HitArea.values()) area.refresh();
        }
        
        /**
         * Returns the hit area based on the height
         * @param height Height to look for
         */
        public static HitArea get(double height) {
            if(height <= 1.85 && height > 1.38) return HitArea.HEAD;
            else if(height <= 1.38 && height > 1.26) return HitArea.NECK;
            else if(height <= 1.26 && height > 0.74) return HitArea.TORSO;
            else if(height <= 0.74 && height > 0.46) return HitArea.CROTCH;
            else if(height <= 0.46 && height > 0.17) return HitArea.LEGS;
            else if(height <= 0.17 && height > 0) return HitArea.TOE;
            
            return HitArea.UNKNOWN;
        }
        
    }
    
}
