package com.github.DarkSeraphim.EnderForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author DarkSeraphim
 */
public class EnderRecipe 
{

    List<ItemStack> ingredients;
    
    ItemStack result;
    
    ItemMeta meta;
    
    public EnderRecipe(ItemStack result)
    {
        this.ingredients = new ArrayList<ItemStack>();
        this.result = result;
        this.meta = this.result.getItemMeta();
    }
    
    public EnderRecipe setName(String name)
    {
        this.meta.setDisplayName(ChatColor.RESET+ChatColor.translateAlternateColorCodes('&', name));
        this.result.setItemMeta(this.meta);
        return this;
    }
    
    public EnderRecipe setLore(String...lore)
    {
        List<String> lores = new ArrayList<String>();
        for(int i = 0; i < lore.length; i++)
        {
            lores.add(ChatColor.RESET+lore[i]);
        }
        this.meta.setLore(lores);
        this.result.setItemMeta(this.meta);
        return this;
    }
    
    public EnderRecipe addEnchantments(Map<String, Integer> enchs)
    {
        for(Entry<String, Integer> ench : enchs.entrySet())
        {
            addEnchantment(ench.getKey(), ench.getValue());
        }
        return this;
    }
    
    private void addEnchantment(String name, int level)
    {
        if(EnderEnchantment.getEnchantment(name) != null)
        {
            this.meta.addEnchant(EnderEnchantment.getEnchantment(name), level, true);
        }
        this.result.setItemMeta(this.meta);
    }
    
    public EnderRecipe addIngredients(ItemStack...iss)
    {
        for(ItemStack i : iss)
        {
            System.out.println("Adding ingredient for '"+this.meta.getDisplayName()+"': "+(i.getItemMeta().hasDisplayName() ? i.getItemMeta().getDisplayName() : i.getType().name()));
            addIngredient(i);
        }
        return this;
    }
    
    private void addIngredient(ItemStack i) throws IllegalArgumentException
    {
        if(ingredients.size() == 9) throw new IllegalArgumentException("super recipes cannot have more than 9 ingredients");
        this.ingredients.add(i);
    }
    
    public void registerRecipe()
    {
        ShapelessRecipe sr = new ShapelessRecipe(this.result);
        System.out.println("Data registered for recipe: "+getIdentifier(this.result));
        for(ItemStack i : ingredients)
        {
            sr.addIngredient(i.getAmount(), i.getType(), (i.getType().getMaxDurability() > 0 ? (int)i.getData().getData() : i.getDurability()));
        }
        Bukkit.addRecipe(sr);
    }
    
    public ItemStack getResult()
    {
        return this.result;
    }
    
    public List<ItemStack> getIngredients()
    {
        List<ItemStack> copy = new ArrayList<ItemStack>();
        copy.addAll(this.ingredients);
        return copy;
    }
    
    public Map<String, Integer> getIngredientsMap()
    {
        Map<String, Integer> loreMap = new HashMap<String, Integer>();
        ItemMeta meta;
        for(ItemStack ing : this.ingredients)
        {
            String name;
            name = ""+ing.getTypeId();
            int data;
            if(ing.getType().getMaxDurability() > 0)
            {
                data = (int)ing.getData().getData();
            }
            else
            {
                data = ing.getDurability();
            }
            name += ":"+data;
            if(loreMap.containsKey(name))
            {
                loreMap.put(name, loreMap.get(name)+1);
            }
            else
            {
                loreMap.put(name, 1);
            }
        }
        return loreMap;
    }
    
    public static int getIdentifier(ItemStack stack)
    {
        return (int)stack.getType().getMaxDurability() > 0 ? stack.getData().getData() : stack.getDurability();
    }
    
}
