package com.github.DarkSeraphim.EnderForge.listeners;

import com.github.DarkSeraphim.EnderForge.EnderForge;
import com.github.DarkSeraphim.EnderForge.EnderRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author DarkSeraphim
 */
public class CraftingListener implements Listener
{
    private EnderForge sc;
    
    public CraftingListener(EnderForge sc)
    {
        this.sc = sc;
    }
    
    @EventHandler(priority=EventPriority.LOW)
    public void cancelNormalCrafting(PrepareItemCraftEvent event)
    {
        if(event.getView().getPlayer() instanceof Player == false) return;
        Player player = (Player) event.getView().getPlayer();
        if(sc.crafting.get(player.getName()) != null) return;
        ItemStack result = event.getRecipe().getResult();
        ItemMeta resultMeta = event.getRecipe().getResult().getItemMeta();
        if(resultMeta.hasDisplayName())
        {
            String name = resultMeta.getDisplayName();
            if(sc.getRecipeMap().containsKey(name))
            {
                int data = EnderRecipe.getIdentifier(sc.getRecipeMap().get(name).getResult());
                int resultData = EnderRecipe.getIdentifier(result);
                if(data == resultData)
                {
                    event.getInventory().setResult(null);
                    player.updateInventory();
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPrepareSuperCraft(final PrepareItemCraftEvent event)
    {
        sc.debug("prepare");
        if(event.getView().getPlayer() instanceof Player == false) return;
        final Player player = (Player) event.getView().getPlayer();
        if(sc.crafting.get(player.getName()) == null) return;
        ItemStack result = event.getRecipe().getResult();
        ItemMeta resultMeta = event.getRecipe().getResult().getItemMeta();
        if(resultMeta.hasDisplayName())
        {
            final String name = resultMeta.getDisplayName();
            if(sc.getRecipeMap().containsKey(name))
            {
                int data = EnderRecipe.getIdentifier(sc.getRecipeMap().get(name).getResult());
                int resultData = EnderRecipe.getIdentifier(result);
                if(data == resultData)
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            event.getInventory().setResult(sc.getRecipeMap().get(name).getResult());
                            player.updateInventory();
                        }
                    }.runTaskLater(sc, 1L);
                    
                }
            }
        }
    }
    
    @EventHandler
    public void onSuperCraft(CraftItemEvent event)
    {
        sc.debug("craft");
        if(event.getWhoClicked() instanceof Player == false) return;
        Player player = (Player) event.getWhoClicked();
        boolean isCrafting = sc.crafting.get(player.getName()) != null;
        if(isCrafting)
        {
            String mode = sc.crafting.get(player.getName());
            if(sc.getRecipeMap().get(mode) != null)
            {
                if(event.getCursor() != null && event.getCursor().getType() != Material.AIR)
                {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
        
                CraftingInventory ci = event.getInventory();
                // Fetch the EnderRecipe
                EnderRecipe sr = sc.getRecipeMap().get(mode);
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
                boolean done = true;
                for(Map.Entry<String, Integer> ingredient : recipeMap.entrySet())
                {

                    if(loreofleft.contains(ingredient.getKey()))
                    {
                        done = false;
                    }
                }
                
                if(!done)
                {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
                
                event.setCursor(event.getRecipe().getResult());
                event.getInventory().setResult(null);
                //event.setCancelled(true);
                //event.setResult(Event.Result.DENY);
                ItemStack[] matrix = event.getInventory().getMatrix();
                for(int i = 0; i < matrix.length; i++)
                {
                    ItemStack x = matrix[i];
                    if(x != null)
                    {
                        if(x.getAmount() > 1)
                        {
                            x.setAmount(x.getAmount() - 1);
                        }
                        else
                        {
                            x = null;
                        }
                        matrix[i] = x;
                    }
                }
                event.getInventory().setMatrix(matrix);
                player.updateInventory();
                player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 20);
                player.getWorld().playSound(player.getLocation(), Sound.ANVIL_USE, 1f, 63f);
                //sc.crafting.put(player.getName(), null);*/
            }
        }
    }
}
