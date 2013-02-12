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
    
    public static String getEName(Enchantment e)
    {
        for(EnderEnchantment se : values())
        {
            if(se.getEnchantment().equals(e))
            {
                return se.getName();
            }
        }
        return null;
    }
    
    /**
     *  Thanks to this stackoverflow page for the conversion ;D
     *  http://stackoverflow.com/questions/3921866/how-do-you-find-a-roman-numeral-equivalent-of-an-integer
     */
    
    private static final char[] R = {'ↂ', 'ↁ', 'M', 'D', 'C', 'L', 'X', 'V', 'I'};
    // or, as suggested by Andrei Fierbinteanu
    // private static final String[] R = {"X\u0305", "V\u0305", "M", "D", "C", "L", "X", "V", "I"};
    private static final int MAX = 10000; // value of R[0], must be a power of 10

    private static final int[][] DIGITS = {
        {},{0},{0,0},{0,0,0},{0,1},{1},
        {1,0},{1,0,0},{1,0,0,0},{0,2}};

   
    public static String toRoman(int number) 
    {
        if (number < 0 || number >= MAX*4) 
        {
            throw new IllegalArgumentException("toRoman: " + number + " is not between 0 and " + (MAX*4-1));
        }
        if (number == 0) return "N";
        StringBuilder sb = new StringBuilder();
        int i = 0, m = MAX;
        while (number > 0) 
        {
            int[] d = DIGITS[number / m];
            for (int n: d) sb.append(R[i-n]);
            number %= m;
            m /= 10;
            i += 2;
        }
        
        return sb.toString();
    }
    
}
