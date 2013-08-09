## MorePhysics 2.3.0 ##

* General changes
    * Ground-up rewrite of the plugin
    * Implement several techniques that will prevent the plugin from completely breaking in case of a CraftBukkit update
    * Changed the permissions to start with _physics.*_ in case of the hit-and-run scenario
    * Added the _enable_, _effects_, and _exempt-world_ options to (most) components
* ArrowComponent
    * Change the height locator to separate the body into 6 parts instead of 4
        * Head, neck, torso, crotch, legs, toes
        * Add an option to set the default damage (if the body part cannot be determined)
    * Apply the critical damage to all humanoid-ish entities
        * Blaze, creeper, iron golem, pigman, player, skeleton, snowman, villager, witch, wither
    * Add an option to apply potion effects to players when hit in specific areas
* BlocksComponent
    * Brand new component: apply different effects to players when they walk on top of specific blocks
    * Very much alpha and not optimized; might cause lag
    * Speed-up block
        * Boosts the player in the direction he is walking
        * Minor bug: moves the player even if he is not actively walking
    * Slow-down block
        * Slows the player down
        * Works by applying a vector with the direction opposite to the one in which the player is moving
        * Might cause the player to move slightly backwards even when not actively walking
    * Bounce block
        * Similar to the piston launcher
        * Might not launch the player if he is perfectly still
* BloodComponent
    * Brand new component: add particle effects to players, monsters, and animals when receiving damage
        * Depends on native Minecraft code and therefore will break when CraftBukkit updates
    * Add 11 pre-defined colors
        * white, white_alt, red, red_alt, blue, blue_alt, yellow, yellow_alt, green, green_alt, glitter
        * Particle effects are based on blocks that are being broken; therefore, custom colors can be used by specifying the ID of the block
            * Only basic block IDs are supported, no damage values are allowed
* BoatComponent
    * Re-write the component to use the entity metadata instead of storing a list of all sinking boats
    * Add a setting to customize the rate at which the boat is being damaged
* MinecartComponent
    * Re-write the component to use the entity metadata instead of storing a list of all hit players
    * Better death cause handling
    * Add a setting to customize the minimum detected speed
* PistonComponent
    * Add an option to cancel the falling damage
    * Add an option to calculate the player's inventory weight into the velocity
        * Requires WeightComponent to be enabled
    * Add an option to have sign-controlled pistons: specify the velocity on the sign
        * Only pistons with signs on them will launch entities
    * Launch any blocks (not just sand and gravel)
        * Add an option to customize the launcheable blocks in the configuration
        * Add an option to treat the pushed blocks whitelist as a blacklist
* WeightComponent
    * Calculate the weight of the entire player inventory, not just armor
        * Add an option to only calculate armor, just like in old times
    * Add an option to specify default player speed and player speed multiplyers
        * Both of the values are multiplyers, so that the server owners will not be confused by the numbers like 0.2 and 0.0001
    * Add an option to exempt creative-mode players from weight calculations

## MorePhysics 2.2.1 ##

No older changelog available