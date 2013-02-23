package com.github.DarkSeraphim.EnderForge.listeners;

import com.github.DarkSeraphim.EnderForge.EnderForge;
import com.github.DarkSeraphim.EnderForge.EnderRecipe;
import com.github.DarkSeraphim.EnderForge.menu.Menu;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author DarkSeraphim
 */
public class InventoryListener implements Listener
{
    
    private EnderForge ef;
    
    public InventoryListener(EnderForge sc)
    {
        this.ef = sc;
    }
    
    @EventHandler
    public void openInventory(InventoryOpenEvent event)
    {
        if(event.getPlayer() instanceof Player == false) return;
        Player player = (Player) event.getPlayer();
        boolean isCrafting = ef.crafting.get(player.getName()) != null;
        if(isCrafting)
        {
            String mode = ef.crafting.get(player.getName());
            if(ef.getMenuManager().getMenu(mode) != null)
            {
                // In menu
                
                if(ef.getRecipeMap().get(mode) != null)
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
        boolean isCrafting = ef.crafting.get(player.getName()) != null;
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
                        ef.crafting.put(player.getName(), null);
                    }
                }
            }.runTaskLater(ef, 2L);
        }
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        if(event.getWhoClicked() instanceof Player == false) return;
        Player player = (Player) event.getWhoClicked();
        boolean isCrafting = ef.crafting.get(player.getName()) != null;
        if(isCrafting)
        {
            String mode = ef.crafting.get(player.getName());
            Menu m = ef.getMenuManager().getMenu(mode);
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
    public void addIngredient(final InventoryClickEvent event)
    {
        ef.debug("click");
        if(event.getWhoClicked() instanceof Player == false) return;
        final Player player = (Player) event.getWhoClicked();
        ef.debug("its in: "+ef.crafting.get(player.getName()));
        boolean isCrafting = ef.crafting.get(player.getName()) != null;
        if(isCrafting)
        {
            String mode = ef.crafting.get(player.getName());
            if(ef.getRecipeMap().get(mode) != null)
            {
                if(event.getRawSlot() > 9)
                {
                    return;
                }
                // In crafting
                final CraftingInventory ci = (CraftingInventory) event.getInventory();
                //ci.setMaxStackSize(1);
                // Fetch the EnderRecipe
                final EnderRecipe er = ef.getRecipeMap().get(mode);
                // Fetch the ingredients Map
                
                List<String> lore = getRecipeLore(er, ci.getMatrix());
                
                boolean done = true;
                for(String s : lore)
                {
                    if(s.startsWith(ChatColor.RED.toString()))
                    {
                        done = false;
                        break;
                    }
                }
                ef.replaceNames(lore);
                final ItemStack list = new ItemStack(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta listMeta = (EnchantmentStorageMeta) list.getItemMeta();
                for(Enchantment e : listMeta.getStoredEnchants().keySet())
                {
                    listMeta.removeStoredEnchant(e);
                }
                listMeta.setDisplayName(mode);
                listMeta.setLore(lore);
                list.setItemMeta(listMeta);
                
                ItemStack current = event.getCursor();
                ef.debug(current.toString());
                ItemStack[] aftermatrix = (ItemStack[])ci.getMatrix().clone();
                for(int i = 0; i < aftermatrix.length; i++)
                {
                    if(aftermatrix[i] == null || aftermatrix[i].getType() == Material.AIR)
                    {
                        aftermatrix[i] = current;
                        break;
                    }
                }
                List<String> before = getLoreOfLeft(er, ci.getMatrix());
                List<String> after = getLoreOfLeft(er, aftermatrix);
                int beforeN = Collections.frequency(before, current.getTypeId()+":"+EnderRecipe.getIdentifier(current));
                int afterN = Collections.frequency(after, current.getTypeId()+":"+EnderRecipe.getIdentifier(current));
                ef.debug(beforeN+":"+afterN);
                if(afterN < beforeN)
                {
                    ef.debug("new item");
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 31f);
                    if(event.getCursor().getType().getMaxDurability() > 0)
                    {
                        ItemStack cursor = event.getCursor();
                        String name = cursor.getItemMeta().hasDisplayName() ? cursor.getItemMeta().getDisplayName() : "";
                        if(!name.isEmpty())
                        {
                            short data = ef.getActualData(ChatColor.stripColor(name));
                            MaterialData mData = cursor.getData();
                            mData.setData((byte)data);
                            cursor.setData(mData);
                        }
                    }
                }
                // Set resultslot to ingredients list
                ci.setResult(list);
                ef.debug(done+":"+event.getRawSlot());
                ef.debug("before");
                for(String s : before)
                {
                    ef.debug("* "+s);
                }
                ef.debug("after");
                for(String s : after)
                {
                    ef.debug("* "+s);
                }
                if(!done && event.getRawSlot() == 0)
                {
                    ef.debug("done apparently");
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    player.updateInventory();
                }
                else if(event.getRawSlot() == 0)
                {
                    CraftItemEvent cie = new CraftItemEvent(er.getRecipe(), player.getOpenInventory(), InventoryType.SlotType.RESULT, 0, false, false);
                    Bukkit.getPluginManager().callEvent(cie);
                }
                else
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            List<String> lore = getRecipeLore(er, ci.getMatrix());
                            boolean done = true;
                            for(String s : lore)
                            {
                                if(s.startsWith(ChatColor.RED.toString()))
                                {
                                    done = false;
                                    break;
                                }
                            }
                            if(!done)
                            {
                                ItemMeta meta = list.getItemMeta();
                                ef.replaceNames(lore);
                                meta.setLore(lore);
                                list.setItemMeta(meta);
                                ef.debug("Debug much?");
                                ci.setResult(list);
                            }
                            else
                            {
                                ci.setResult(er.getResult());
                            }
                            player.updateInventory();
                        }
                    }.runTaskLater(ef, 3L);
                }
            }
        }
    }
    
    public List<String> getRecipeLore(EnderRecipe er, ItemStack[] matrix)
    {
        Map<String, Integer> recipeMap = er.getIngredientsMap();
        List<String> lore = new ArrayList<String>();
        // Lore building
        List<String> loreofleft = getLoreOfLeft(er, matrix);
        String pre = "";
        for(Entry<String, Integer> ingredient : recipeMap.entrySet())
        {
            if(loreofleft.contains(ingredient.getKey()))
            {
                pre = ChatColor.RED.toString();
            }
            else
            {
                pre = ChatColor.GREEN.toString();
            }
            lore.add(pre+"("+ingredient.getValue()+" x )"+ingredient.getKey());
        }
        return lore;
    }
    
    public List<String> getLoreOfLeft(EnderRecipe er, ItemStack[] matrix)
    {
        List<ItemStack> left = er.getIngredients();
        ItemMeta iMeta;
        ItemMeta lMeta;
        for(ItemStack i : matrix)
        {
            for(ItemStack l : left)
            {
                if(i == null) continue;
                //if(l.isSimilar(i))
                if(l.getType() == i.getType() && EnderRecipe.getIdentifier(l) == EnderRecipe.getIdentifier(i))
                {
                    left.remove(l);
                    break;
                }
                else if(i.getType().getMaxDurability() > 0 && l.getType().getMaxDurability() > 0)
                {
                    iMeta = i.getItemMeta();
                    lMeta = l.getItemMeta();
                    List<String> ident = Arrays.asList(l.getType().getId()+":"+EnderRecipe.getIdentifier(l));
                    ef.replaceNames(ident);
                    String lName = ident.get(0);
                    String iName = ChatColor.stripColor(iMeta.getDisplayName());
                    if(lName.equals(iName) && iName.length() < iMeta.getDisplayName().length())
                    {
                        left.remove(l);
                        break;
                    }
                }
            }
        }
        List<String> loreofleft = new ArrayList<String>();
        for(ItemStack i : left)
        {
            loreofleft.add(i.getTypeId()+":"+EnderRecipe.getIdentifier(i));
        }
        return loreofleft;
    }
}
