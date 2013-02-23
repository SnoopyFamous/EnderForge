package com.github.DarkSeraphim.EnderForge;

import org.bukkit.enchantments.Enchantment;

/**
 *
 * @author DarkSeraphim
 */
public enum EnderEnchantment
{
    PROTECTION(Enchantment.PROTECTION_ENVIRONMENTAL, "protection", "Protection"),
    FIRE_PROTECTION(Enchantment.PROTECTION_FIRE, "fireprotection", "Fire Protection"),
    FALL_PROTECTION(Enchantment.PROTECTION_FALL, "fallprotection", "Feather Falling"),
    EXPLOSION_PROTECTION(Enchantment.PROTECTION_EXPLOSIONS, "explosionprotection", "Blast Protection"),
    PROJECTILE_PROTECTION(Enchantment.PROTECTION_PROJECTILE, "projectileprotection", "Projectile Protection"),
    WATERBREATHING(Enchantment.OXYGEN, "waterbreathing", "Respiration"),
    WATER_WORKER(Enchantment.WATER_WORKER,"waterworker", "Aqua Affinity"),
    THORNS(Enchantment.THORNS, "thorns", "Thorns"),
    SHARPNESS(Enchantment.DAMAGE_ALL, "sharpness", "Sharpness"),
    SMITE(Enchantment.DAMAGE_UNDEAD, "smite", "Smite"),
    BANE_OF_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS, "baneofarthropods", "Bane of Arthropods"),
    KNOCKBACK(Enchantment.KNOCKBACK, "knockback", "Knockback"),
    FIRE_ASPECT(Enchantment.FIRE_ASPECT, "fireaspect", "Fire Aspect"),
    LOOT(Enchantment.LOOT_BONUS_MOBS, "loot", "Looting"),
    EFFICIENCY(Enchantment.DIG_SPEED, "efficiency", "Efficiency"),
    SILK_TOUCH(Enchantment.SILK_TOUCH, "silktouch", "Silk Touch"),
    DURABILITY(Enchantment.DURABILITY, "durability", "Unbreaking"),
    FORTUNE(Enchantment.LOOT_BONUS_BLOCKS, "fortune", "Fortune"),
    POWER(Enchantment.ARROW_DAMAGE, "power", "Power"),
    ARROW_KNOCKBACK(Enchantment.ARROW_KNOCKBACK, "arrowknockback", "Punch"),
    FLAME(Enchantment.ARROW_FIRE, "firearrows", "Flame"),
    INFINITE(Enchantment.ARROW_INFINITE, "infinite", "Infinity");
    
    private EnderEnchantment(Enchantment e, String s, String name)
    {
        this.cfgName = s;
        this.name = name;
        this.ench = e;
    }
    
    private final String cfgName;
    
    private final String name;
    
    private final Enchantment ench;
    
    public String getName()
    {
        return this.name;
    }
    
    public String getCfgName()
    {
        return this.cfgName;
    }
    
    public Enchantment getEnchantment()
    {
        return this.ench;
    }
    
    public static Enchantment getEnchantment(String s)
    {
        for(EnderEnchantment se : values())
        {
            if(se.getCfgName().equals(s))
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
