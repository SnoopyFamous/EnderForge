package com.github.DarkSeraphim.EnderForge.menu;

import com.github.DarkSeraphim.EnderForge.EnderForge;
import com.github.DarkSeraphim.EnderForge.EnderRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author DarkSeraphim
 */
public class Menu 
{
    private final String name;
    
    private ItemStack[] contents;
    
    private Slot[] slots;
    
    private Menu parent;
    
    private final EnderForge main;
    
    public Menu(EnderForge main, String name, Menu parent)
    {
        this.main = main;
        this.name = name;
        this.contents = new ItemStack[27];
        this.slots = new Slot[27];
        this.parent = parent;
    }
    
    public void setItem(int index, int id, String title, String action, List<String> lore) throws IllegalArgumentException
    {
        String[] acpar = action.split("\\|");
        if(acpar.length != 2) return;
        if(index < 0 || index > 26) throw new IllegalArgumentException("Index is "+(index < 0 ? "too small" : "too large"));
        ItemStack i = new ItemStack(id);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(lore);
        i.setItemMeta(meta);
        this.contents[index] = i;
        this.slots[index] = new Slot(acpar[0], acpar[1]);
    }
    
    /**
     * Callable method for the actual processing of the event
     * 
     * @param slot
     */
    public void onClick(final Player player, int slot)
    {
        if(slot < 0 || slot > 26) return;
        Slot s = this.slots[slot];
        if(s != null)
        {
            String action = s.getAction();
            final String parameter = s.getParameter();
            if(action == null || parameter == null)
            {
                return;
            }
            if(action.equals("menu"))
            {
                final Menu m = main.getMenuManager().getMenu(parameter);
                if(m != null)
                {
                    player.getOpenInventory().close();
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1.0f, 94f);
                            player.openInventory(m.createMenu(player));
                            main.crafting.put(player.getName(), parameter);
                        }
                    }.runTaskLater(main, 0L);
                    
                }
                else
                {
                    main.debug("Null Menu found: '"+parameter+"'");
                }
            }
            else if(action.equals("item"))
            {
                player.getOpenInventory().close();
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1.0f, 94f);
                        final CraftingInventory ci = (CraftingInventory) player.openWorkbench(null, true).getTopInventory();
                        main.crafting.put(player.getName(), parameter);
                        // Fetch the EnderRecipe
                        EnderRecipe er = main.getRecipeMap().get(parameter);
                        // Fetch the ingredients Map
                        Map<String, Integer> recipeMap = er.getIngredientsMap();
                        List<String> lore = new ArrayList<String>();
                        // Lore building
                        for(Map.Entry<String, Integer> ingredient : recipeMap.entrySet())
                        {
                            lore.add(ChatColor.RED+"(x"+ingredient.getValue()+") "+ingredient.getKey());
                        }
                        main.replaceNames(lore);
                        final ItemStack list = new ItemStack(Material.ENCHANTED_BOOK);
                        EnchantmentStorageMeta listMeta = (EnchantmentStorageMeta) list.getItemMeta();
                        for(Enchantment e : listMeta.getStoredEnchants().keySet())
                        {
                            listMeta.removeStoredEnchant(e);
                        }
                        listMeta.setDisplayName(parameter);
                        listMeta.setLore(lore);
                        list.setItemMeta(listMeta);
                        // Set resultslot to ingredients list
                        ci.setResult(list);
                        player.updateInventory();
                    }
                }.runTaskLater(main, 1L);
                // Initialized on inventory open
            }
        }
    }
    
    public boolean hasParent()
    {
        return parent != null;
    }
    
    public Inventory createMenu(Player holder)
    {
        System.out.println("name: "+this.name);
        Inventory inv = Bukkit.createInventory(holder, 27, ChatColor.translateAlternateColorCodes('&',this.name));
        inv.setContents(this.contents);
        return inv;
    }
}
