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
    private EnderForge ef;
    
    public InteractionListener(EnderForge sc)
    {
        this.ef = sc;
    }

    @EventHandler(ignoreCancelled=true)
    public void onEnderForgeClick(PlayerInteractEvent e)
    {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block b = e.getClickedBlock();
        String loc = new StringBuilder(b.getWorld().getName()).append(",").append(b.getX()).append(",").append(b.getY()).append(",").append(b.getZ()).toString();
        if(ef.getForgeLocation().equals(loc))
        {
            Menu m = ef.getMenuManager().getMenu(ef.getMenuManager().MainMenu);
            if(m != null)
            {
                e.getPlayer().playSound(e.getClickedBlock().getLocation(), Sound.IRONGOLEM_HIT, 1f, 31f);
                Inventory inv = m.createMenu(e.getPlayer());
                e.getPlayer().openInventory(inv);
                ef.crafting.put(e.getPlayer().getName(), ef.getMenuManager().MainMenu);
            }
            else
            {
                ef.debug("Menu '"+ef.getMenuManager().MainMenu+"' found, but null");
            }
            e.setCancelled(true);
        }
    }
    
}