package com.github.DarkSeraphim.EnderForge.listeners;

import com.github.DarkSeraphim.EnderForge.EnderForge;
import com.github.DarkSeraphim.EnderForge.menu.Menu;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

/**
 *
 * @author DarkSeraphim
 */
public class InteractionListener implements Listener
{
    private EnderForge sc;
    
    public InteractionListener(EnderForge sc)
    {
        this.sc = sc;
    }

    @EventHandler(ignoreCancelled=true)
    public void onSuperAnvilClick(PlayerInteractEvent e)
    {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block b = e.getClickedBlock();
        String loc = new StringBuilder().append(b.getX()).append(",").append(b.getY()).append(",").append(b.getZ()).toString();
        String world = b.getWorld().getName();
        if(sc.getMedia().containsKey(world))
        {
            if(sc.getMedia().get(world).contains(loc))
            {
                Menu m = sc.getMenuManager().getMenu(sc.getMenuManager().MainMenu);
                if(m != null)
                {
                    e.getPlayer().playSound(e.getClickedBlock().getLocation(), Sound.IRONGOLEM_HIT, 1f, 31f);
                    Inventory inv = m.createMenu(e.getPlayer());
                    e.getPlayer().openInventory(inv);
                    sc.crafting.put(e.getPlayer().getName(), sc.getMenuManager().MainMenu);
                }
                else
                {
                    sc.debug("Menu '"+sc.getMenuManager().MainMenu+"' found, but null");
                }
                e.setCancelled(true);
            }
        }
    }
    
}