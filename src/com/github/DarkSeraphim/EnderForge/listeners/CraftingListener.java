package com.github.DarkSeraphim.EnderForge.listeners;

import com.github.DarkSeraphim.EnderForge.EnderForge;
import com.github.DarkSeraphim.EnderForge.EnderRecipe;
import java.util.List;
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
    private EnderForge ef;
    
    public CraftingListener(EnderForge sc)
    {
        this.ef = sc;
    }
    
    @EventHandler(priority=EventPriority.LOW)
    public void cancelNormalCrafting(PrepareItemCraftEvent event)
    {
        if(event.getView().getPlayer() instanceof Player == false) return;
        Player player = (Player) event.getView().getPlayer();
        if(ef.crafting.get(player.getName()) != null) return;
        ItemStack result = event.getRecipe().getResult();
        ItemMeta resultMeta = result.getItemMeta();
        if(resultMeta.hasDisplayName())
        {
            String name = resultMeta.getDisplayName();
            if(ef.getRecipeMap().containsKey(ChatColor.stripColor(name)))
            {
                int data = EnderRecipe.getIdentifier(ef.getRecipeMap().get(ChatColor.stripColor(name)).getResult());
                int resultData = EnderRecipe.getIdentifier(result);
                if(data == resultData || result == null)
                {
                    event.getInventory().setResult(null);
                    player.updateInventory();
                }
                else if(result.getType().getMaxDurability() > 0)
                {
                    // Tools are bugged: they always return an ItemStack with datavalue 0...
                    // Note to Svesken: don't use tools in recipes, might break
                    event.getInventory().setResult(null);
                    player.updateInventory();
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPrepareSuperCraft(final PrepareItemCraftEvent event)
    {
        if(event.getView().getPlayer() instanceof Player == false) return;
        final Player player = (Player) event.getView().getPlayer();
        if(ef.crafting.get(player.getName()) == null) return;
        ItemStack result = event.getRecipe().getResult();
        ItemMeta resultMeta = event.getRecipe().getResult().getItemMeta();
        if(resultMeta.hasDisplayName())
        {
            final String name = resultMeta.getDisplayName();
            if(!name.equals(ef.crafting.get(player.getName())))
            {
                event.getInventory().setResult(null);
                return;
            }
            if(ef.getRecipeMap().containsKey(name))
            {
                int data = EnderRecipe.getIdentifier(ef.getRecipeMap().get(name).getResult());
                int resultData = EnderRecipe.getIdentifier(result);
                if(data == resultData)
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            event.getInventory().setResult(ef.getRecipeMap().get(name).getResult());
                            player.updateInventory();
                        }
                    }.runTaskLater(ef, 1L);
                    
                }
            }
        }
    }
    
    @EventHandler
    public void onSuperCraft(CraftItemEvent event)
    {
        if(event.getWhoClicked() instanceof Player == false) return;
        Player player = (Player) event.getWhoClicked();
        boolean isCrafting = ef.crafting.get(player.getName()) != null;
        if(isCrafting)
        {
            String mode = ef.crafting.get(player.getName());
            if(ef.getRecipeMap().get(mode) != null)
            {
                if(event.getCursor() != null && event.getCursor().getType() != Material.AIR)
                {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
        
                CraftingInventory ci = event.getInventory();
                // Fetch the EnderRecipe
                EnderRecipe er = ef.getRecipeMap().get(mode);
                
                List<String> lore = InventoryListener.getRecipeLore(er, ci.getMatrix());
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
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
                
                event.setCursor(er.getResult());
                event.getInventory().setResult(null);
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
            }
        }
    }
}
