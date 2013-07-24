package com.wolvencraft.morephysics.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class PhysicsUtil {
    
    public static boolean isEntityNearby(Entity entity, Location location) {
        Location entityLoc = entity.getLocation();
        Location entityHeadLoc = entityLoc.clone().add(0, 1, 0);
        return entityLoc.distanceSquared(location) <= 1 || entityHeadLoc.distanceSquared(location) <= 1;
    }
    
}
