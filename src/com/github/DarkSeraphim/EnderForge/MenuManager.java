package com.github.DarkSeraphim.EnderForge;

import com.github.DarkSeraphim.EnderForge.menu.Menu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author DarkSeraphim
 */
public class MenuManager 
{
    private EnderForge sc;
    
    private Map<String, Menu> menus = new HashMap<String, Menu>();

    public final String MainMenu;
    
    protected MenuManager(EnderForge sc)
    {
        this.sc = sc;
        this.MainMenu = sc.getConfig().getString("main-menu", "");
        MenuManager.this.initializeMenus();
    }
    
    public Menu getMenu(String name)
    {
        return menus.get(name);
    }
    
    public void initializeMenus()
    {
        ConfigurationSection menusSection = sc.getConfig().getConfigurationSection("menus");
        if(menusSection == null)
        {
            menusSection = sc.getConfig().createSection("menus");
        }
        if(!menusSection.contains(MainMenu))
        {
            sc.debug("Failed to fetch main menu");
            return;
        }

        //Menu main = new Menu(sc, MainMenu, null);
        //this.menus.put(MainMenu, main);
        for(String menu : menusSection.getKeys(false))
        {
            initializeMenu(menusSection, menu);
        }
    }
    
    private void initializeMenu(ConfigurationSection menus, String menu)
    {
        if(this.menus.containsKey(menu)) return;
        String parent = menus.getString(menu+".parent", this.MainMenu);
        if(!this.menus.containsKey(parent) && !menu.equals(this.MainMenu))
        {
            initializeMenu(menus, parent);
        }
        Menu m = new Menu(sc, menu, this.menus.get(parent));
        ConfigurationSection items = menus.getConfigurationSection(menu+".items");
        if(items == null)
        {
            items = menus.createSection("items");
        }
        for(String item : items.getKeys(false))
        {
            int index;
            try
            {
                index = Integer.parseInt(item);
            }
            catch(NumberFormatException ex)
            {
                continue;
            }
            int icon = items.getInt(item+".icon", -1);
            String name = items.getString(item+".icon-name", "");
            String action = items.getString(item+".link", "");
            if(icon < 1 || name.isEmpty() || action.isEmpty())
            {
                continue;
            }
            List<String> ing = null;
            try
            {
                String[] acpar = action.split("\\|");
                String linkage = acpar[0];
                String rec = acpar[1];
                if(linkage.equals("item"))
                {
                    EnderRecipe er = sc.getRecipeMap().get(rec);
                    ItemMeta recmet = er.getResult().getItemMeta();
                    ing = new ArrayList<String>();
                    for(Entry<Enchantment, Integer> ench : recmet.getEnchants().entrySet())
                    {
                        String en = EnderEnchantment.getEName(ench.getKey());
                        en = en.substring(0,1).toUpperCase()+en.substring(1);
                        ing.add(ChatColor.GRAY+en+" "+EnderEnchantment.toRoman(ench.getValue()));
                    }
                    if(ing.size() > 0) ing.add("");
                    ing.addAll(recmet.getLore());
                    if(ing.size() > 0) ing.add("");
                    ing.add("Recipe: ");
                    for(Entry<String, Integer> e : er.getIngredientsMap().entrySet())
                    {
                        ing.add(ChatColor.WHITE+"("+e.getValue()+"x) "+ChatColor.RESET+ChatColor.GRAY+e.getKey());
                    }
                    sc.replaceNames(ing);
                }
                else if(linkage.equals("menu") && rec.equals("back") && !menu.equals(this.MainMenu))
                {
                    action = linkage+"|"+parent;
                }
            }
            catch(Exception ex){/*Ignore*/}
            try
            {
                m.setItem(index, icon, ChatColor.translateAlternateColorCodes('&', name), action, ing);
            }
            catch(IllegalArgumentException ex)
            {
                sc.debug("Failed to add item '"+item+"' to Menu '"+menu+"'");
            }
        }
        this.menus.put(menu, m);
    }
}
