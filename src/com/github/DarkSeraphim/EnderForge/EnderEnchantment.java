package com.github.DarkSeraphim.EnderForge;

import org.bukkit.enchantments.Enchantment;

/**
 *
 * @author DarkSeraphim
 */
public enum EnderEnchantment
{
    PROTECTION(Enchantment.PROTECTION_ENVIRONMENTAL, "protection"),
    FIRE_PROTECTION(Enchantment.PROTECTION_FIRE, "fireprotection"),
    FALL_PROTECTION(Enchantment.PROTECTION_FALL, "fallprotection"),
    EXPLOSION_PROTECTION(Enchantment.PROTECTION_EXPLOSIONS, "explosionprotection"),
    PROJECTILE_PROTECTION(Enchantment.PROTECTION_PROJECTILE, "projectileprotection"),
    WATERBREATHING(Enchantment.OXYGEN, "waterbreathing"),
    WATER_WORKER(Enchantment.WATER_WORKER,"waterworker"),
    THORNS(Enchantment.THORNS, "thorns"),
    SHARPNESS(Enchantment.DAMAGE_ALL, "sharpness"),
    SMITE(Enchantment.DAMAGE_UNDEAD, "smite"),
    BANE_OF_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS, "baneofarthropods"),
    KNOCKBACK(Enchantment.KNOCKBACK, "knockback"),
    FIRE_ASPECT(Enchantment.FIRE_ASPECT, "fireaspect"),
    LOOT(Enchantment.LOOT_BONUS_MOBS, "loot"),
    EFFICIENCY(Enchantment.DIG_SPEED, "efficiency"),
    SILK_TOUCH(Enchantment.SILK_TOUCH, "silktouch"),
    DURABILITY(Enchantment.DURABILITY, "durability"),
    FORTUNE(Enchantment.LOOT_BONUS_BLOCKS, "fortune"),
    POWER(Enchantment.ARROW_DAMAGE, "power"),
    ARROW_KNOCKBACK(Enchantment.ARROW_KNOCKBACK, "arrowknockback"),
    FLAME(Enchantment.ARROW_FIRE, "firearrows"),
    INFINITE(Enchantment.ARROW_INFINITE, "infinite");
    
    private EnderEnchantment(Enchantment e, String s)
    {
        this.name = s;
        this.ench = e;
    }
    
    private final String name;
    
    private final Enchantment ench;
    
    public String getName()
    {
        return this.name;
    }
    
    public Enchantment getEnchantment()
    {
        return this.ench;
    }
    
    public static Enchantment getEnchantment(String s)
    {
        for(EnderEnchantment se : values())
        {
            if(se.getName().equals(s))
            {
                return se.getEnchantment();
            }
        }
        return null;
    }
    
}
