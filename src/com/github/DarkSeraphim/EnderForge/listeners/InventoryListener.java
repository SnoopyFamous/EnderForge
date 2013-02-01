package com.github.DarkSeraphim.EnderForge.listeners;

import com.github.DarkSeraphim.EnderForge.EnderForge;
import com.github.DarkSeraphim.EnderForge.EnderRecipe;
import com.github.DarkSeraphim.EnderForge.menu.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author DarkSeraphim
 */
public class InventoryListener implements Listener
{
    
    private EnderForge sc;
    
    public InventoryListener(EnderForge sc)
    {
        this.sc = sc;
    }
    
    @EventHandler
    public void openInventory(InventoryOpenEvent event)
    {
        if(event.getPlayer() instanceof Player == false) return;
        Player player = (Player) event.getPlayer();
        boolean isCrafting = sc.crafting.get(player.getName()) != null;
        if(isCrafting)
        {
            String mode = sc.crafting.get(player.getName());
            if(sc.getMenuManager().getMenu(mode) != null)
            {
                // In menu
                
                if(sc.getRecipeMap().get(mode) != null)
                {
                    
                }
            }
        }
    }
    
    @EventHandler
    public void closeInventory(final InventoryCloseEvent event)
    {
        if(event.getPlayer() instanceof Player == false) return;
        final Player player = (Player) event.getPlayer();
        boolean isCrafting = sc.crafting.get(player.getName()) != null;
        if(isCrafting)
        {
            event.getInventory().clear();
            new BukkitRunnable()
            {
                public void run()
                {
                    Inventory i = player.getOpenInventory().getTopInventory();
                    if(i == null || i.getType() == InventoryType.CRAFTING)
                    {
                        sc.crafting.put(player.getName(), null);
                    }
                }
            }.runTaskLater(sc, 2L);
        }
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        if(event.getWhoClicked() instanceof Player == false) return;
        Player player = (Player) event.getWhoClicked();
        boolean isCrafting = sc.crafting.get(player.getName()) != null;
        if(isCrafting)
        {
            String mode = sc.crafting.get(player.getName());
            Menu m = sc.getMenuManager().getMenu(mode);
            if(m != null)
            {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
                player.updateInventory();
                m.onClick(player, event.getRawSlot());
            }
        }
    }
    
    @EventHandler(priority=EventPriority.HIGH)
    public void addIngredient(InventoryClickEvent event)
    {
        sc.debug("click");
        if(event.getWhoClicked() instanceof Player == false) return;
        Player player = (Player) event.getWhoClicked();
        sc.debug("its in: "+sc.crafting.get(player.getName()));
        boolean isCrafting = sc.crafting.get(player.getName()) != null;
        if(isCrafting)
        {
            String mode = sc.crafting.get(player.getName());
            if(sc.getRecipeMap().get(mode) != null)
            {
                if(event.getRawSlot() > 9)
                {
                    return;
                }
                // In crafting
                CraftingInventory ci = (CraftingInventory) event.getInventory();
                ci.setMaxStackSize(1);
                // Fetch the EnderRecipe
                EnderRecipe sr = sc.getRecipeMap().get(mode);
                // Fetch the ingredients Map
                Map<String, Integer> recipeMap = sr.getIngredientsMap();
                List<String> lore = new ArrayList<String>();
                List<ItemStack> left = sr.getIngredients();
                for(ItemStack i : ci.getMatrix())
                {
                    for(ItemStack l : left)
                    {
                        if(l.isSimilar(i))
                        {
                            left.remove(l);
                            break;
                        }
                    }
                }
                List<String> loreofleft = new ArrayList<String>();
                ItemMeta meta;
                for(ItemStack i : left)
                {
                    meta = i.getItemMeta();
                    loreofleft.add(meta.hasDisplayName() ? meta.getDisplayName() : i.getType().name().replace('_', ' ').toLowerCase()
                            +":"+(i.getType().getMaxDurability() > 0 ? i.getData().getData() : i.getDurability()));
                }
                // Lore building
                String pre = "";
                boolean done = true;
                for(Entry<String, Integer> ingredient : recipeMap.entrySet())
                {
                    if(loreofleft.contains(ingredient.getKey()))
                    {
                        pre = ChatColor.RED.toString();
                        done = false;
                    }
                    else
                    {
                        pre = ChatColor.GREEN.toString();
                    }
                    lore.add(pre+ingredient.getKey()+" (x "+ingredient.getValue()+")");
                }
                ItemStack list = new ItemStack(Material.PAPER);
                ItemMeta listMeta = list.getItemMeta();
                listMeta.setDisplayName(mode);
                listMeta.setLore(lore);
                list.setItemMeta(listMeta);
                // Set resultslot to ingredients list
                ci.setResult(list);
                sc.debug(done+":"+event.getRawSlot());
                if(!done && event.getRawSlot() == 0)
                {
                    sc.debug("done apparently");
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    if(ci.getHolder() instanceof Player)
                    {
                        ((Player)ci.getHolder()).updateInventory();
                    }
                }
            }
        }
    }
    
    @EventHandler(priority=EventPriority.MONITOR)
    public void onClick2(InventoryClickEvent e)
    {
        //sc.debug("DEBUG cancel state: "+e.isCancelled());
    }
}
