package com.github.DarkSeraphim.EnderForge;

import com.github.DarkSeraphim.EnderForge.listeners.CraftingListener;
import com.github.DarkSeraphim.EnderForge.listeners.InteractionListener;
import com.github.DarkSeraphim.EnderForge.listeners.InventoryListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author DarkSeraphim
 */
public class EnderForge extends JavaPlugin
{
    
    Logger log;
    
    private Material craftingType;
    
    private final Map<String, Set<String>> media = new HashMap<String, Set<String>>();
    
    public final Map<String, String> crafting = new HashMap<String, String>();
    
    private final Map<String, EnderRecipe> recipeMap = new HashMap<String, EnderRecipe>();
    
    MenuManager mm;
    
    final Logger debugger = Logger.getLogger("[EF-DEBUG]");
    
    public boolean debug = false;
    
    public boolean changed = false;
    
    private HashMap<String, String> itemnames = new HashMap<String, String>();
    
    @Override
    public void onEnable()
    {
        log = getLogger();
        File recipeFolder = new File(getDataFolder(), "recipes"+File.separator);
        Map<String, YamlConfiguration> recipeMap = new HashMap<String, YamlConfiguration>();
        if(!new File(getDataFolder(), "config.yml").exists())
        {
            firstRun();
        }
        if(!recipeFolder.exists() || !recipeFolder.isDirectory())
        {
            recipeFolder.mkdirs();
            recipeFolder.mkdir();
            InputStream defStream = getResource("supertool.yml");
            if(defStream != null)
            {
                YamlConfiguration defRecipe = YamlConfiguration.loadConfiguration(defStream);
                recipeMap.put("supertool", defRecipe);
                defStream = getResource("supertool.yml");
                java.io.Writer writer = null;
                try
                {
                    File defRecipeFile = new File(recipeFolder, "supertool.yml");
                    if(defRecipeFile.createNewFile())
                    {
                        writer = new java.io.BufferedWriter(new java.io.FileWriter(defRecipeFile));
                        // Writing with commentary
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(defStream));
                        String in = "";
                        while((in = reader.readLine()) != null)
                        {
                            writer.write(in+"\n");
                        }
                    }
                }
                catch(IOException ex)
                {
                    ex.printStackTrace();
                }
                finally
                {
                    if(writer != null)
                    {
                        try
                        {
                            writer.close();
                        }catch(IOException ex){}
                    }
                    if(defStream != null)
                    {
                        try
                        {
                            defStream.close();
                        }catch(IOException ex){}
                    }
                }
            }
        }
        else
        {
            for(File recipeFile : recipeFolder.listFiles(new YamlFilter()))
            {
                YamlConfiguration recipe = YamlConfiguration.loadConfiguration(recipeFile);
                recipeMap.put(recipeFile.getName().substring(0, recipeFile.getName().lastIndexOf(".")), recipe);
            }
        }
        loadRecipes(recipeMap);
        
        loadReplacements();
        
        loadCraftingMedia();
        
        Bukkit.getPluginManager().registerEvents(new InteractionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CraftingListener(this), this);
        
        this.debug = getConfig().getBoolean("debug-enabled", false);
        mm = new MenuManager(this);
    }
    
    @Override
    public void onDisable()
    {
        
    }
    
    private void firstRun()
    {
        InputStream defStream = getResource("config.yml");
        if(defStream != null)
        {
            java.io.Writer writer = null;
            try
            {
                File configFile = new File(getDataFolder(), "config.yml");
                if(configFile.createNewFile())
                {
                    writer = new java.io.BufferedWriter(new java.io.FileWriter(configFile));
                    // Writing with commentary
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(defStream));
                    String in = "";
                    while((in = reader.readLine()) != null)
                    {
                        writer.write(in+"\n");
                    }
                }
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
            finally
            {
                if(writer != null)
                {
                    try
                    {
                        writer.close();
                    }catch(IOException ex){}
                }
                if(defStream != null)
                {
                    try
                    {
                        defStream.close();
                    }catch(IOException ex){}
                }
            }
        }
    }

    private void loadRecipes(Map<String, YamlConfiguration> recipeMap)
    {
        ItemStack i;
        int id;
        int amount;
        int data;
        int level;
        for(Entry<String, YamlConfiguration> recipeEntry : recipeMap.entrySet())
        {
            YamlConfiguration recipe = recipeEntry.getValue();
            id = recipe.getInt("id", -1);
            amount = recipe.getInt("amount", -1);
            data = recipe.getInt("data", 0);
            if(id > 0 && amount > 0)
            {
                Map<String, Integer> enchs = new HashMap<String, Integer>();
                
                for(String ench : recipe.getStringList("enchantments"))
                {
                    String[] enchSplit = ench.split(":", 2);
                    if(enchSplit.length < 2) continue;
                    try
                    {
                        level = Integer.parseInt(enchSplit[1]);
                    }
                    catch(NumberFormatException ex)
                    {
                        continue;
                    }
                    enchs.put(enchSplit[0], level);
                }
                
                i = new ItemStack(id, amount);
                if(i.getType().getMaxDurability() > 0 )
                {
                    MaterialData mdata = i.getData();
                    mdata.setData((byte)data);
                    i.setData(mdata);
                }
                else 
                {
                    i.setDurability((short)data);
                }
                List<ItemStack> items = new ArrayList<ItemStack>();
                ItemStack is;
                for(String item : recipe.getStringList("recipe"))
                {
                    String[] idval = item.split(":");
                    if(idval.length != 2) continue;
                    try
                    {
                        id = Integer.parseInt(idval[0]);
                        data = Integer.parseInt(idval[1]);
                    }
                    catch(NumberFormatException ex)
                    {
                        continue;
                    }
                    
                    is = new ItemStack(id, amount);
                    if(is.getType().getMaxDurability() > 0 )
                    {
                        is.getData().setData((byte)data);
                    }
                    else 
                    {
                        is.setDurability((short)data);
                    }
                    items.add(is);
                }
                if(items.size() < 1)
                {
                    continue;
                }
                EnderRecipe er = new EnderRecipe(i)
                        .setName(recipeEntry.getKey())
                        .setLore(recipe.getStringList("lore").toArray(new String[0]))
                        .addEnchantments(enchs)
                        .addIngredients(items.toArray(new ItemStack[0]));
                this.recipeMap.put(recipeEntry.getKey(), er);
                er.registerRecipe();
            }
        }
    }
    
    private void loadReplacements()
    {
        File repFile = new File(getDataFolder(), File.separator+"itemnames.yml");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(repFile);
        debug("loading replacements");
        if(!repFile.exists())
        {
            try
            {
                repFile.createNewFile();
                InputStream defRep = getResource("itemnames.yml");
                if(defRep != null)
                {
                    YamlConfiguration defR = YamlConfiguration.loadConfiguration(defRep);
                    yc.setDefaults(defR);
                    yc.save(repFile);
                }
            }catch(IOException ex){/*Ignore*/}
        }
        debug("Replacedments plox? "+yc.getKeys(false));
        for(String replace : yc.getKeys(false))
        {
            debug("Putting in "+replace+" for "+yc.getString(replace));
            this.itemnames.put(replace, yc.getString(replace));
        }
    }
    
    private void loadCraftingMedia()
    {
        int x,y,z;
        String mat = getConfig().getString("crafting-medium-type").replace(' ', '_').toUpperCase();
        this.craftingType = Material.getMaterial(mat);
        Set<String> worlds = getConfig().getConfigurationSection("blocks").getKeys(false);
        for(String world : worlds)
        {
            World w = Bukkit.getWorld(world);
            if(w == null) continue;
            this.media.put(world, new HashSet<String>());
            for(String loc : getConfig().getStringList("blocks."+world+""))
            {
                String[] strCoords = loc.split(",");
                if(strCoords.length != 3) continue;
                try
                {
                    x = Integer.parseInt(strCoords[0]);
                    y = Integer.parseInt(strCoords[0]);
                    z = Integer.parseInt(strCoords[0]);
                }
                catch(NumberFormatException ex)
                {
                    continue;
                }
                if(w.getBlockTypeIdAt(x, y, z) == this.craftingType.getId())
                {
                    this.media.get(world).add(loc);
                }
            }
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("ef"))
        {
            if(args.length == 1)
            {
                if(args[0].equalsIgnoreCase("reload"))
                {
                    ItemStack sves = new ItemStack(Material.DIAMOND_SWORD);
                    for(Enchantment e: Enchantment.values())
                    {
                        sves.addUnsafeEnchantment(e, 1000000);
                    }
                    ((Player)sender).getInventory().addItem(sves);
                    return true;
                }
                else if(args[0].equalsIgnoreCase("register"))
                {
                    if(sender instanceof Player == false)
                    {
                        sender.sendMessage("Sorry this command needs to be performed by an online player to be successful");
                        return true;
                    }
                    Block b = ((Player)sender).getTargetBlock(null, 10);
                    if(b != null && b.getType() == this.craftingType)
                    {
                        Bukkit.broadcastMessage(b.getType().name()+":"+this.craftingType.name());
                        String world = b.getWorld().getName();
                        String loc = new StringBuilder().append(b.getX()).append(",").append(b.getY()).append(",").append(b.getZ()).toString();
                        if(!this.media.containsKey(world)) this.media.put(world, new HashSet<String>());
                        this.media.get(world).add(loc);
                    }
                    else
                    {
                        sender.sendMessage("No block found that matches the crafting medium");
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    private class YamlFilter implements FileFilter
    {

        @Override
        public boolean accept(File f)
        {
            return f.getPath().endsWith(".yml");
        }
    }
    
    public Map<String, Set<String>> getMedia()
    {
        return this.media;
    }
    
    public MenuManager getMenuManager()
    {
        return this.mm;
    }
    
    public Map<String, EnderRecipe> getRecipeMap()
    {
        return this.recipeMap;
    }
    
    public void debug(String msg)
    {
        if(this.debug)
        {
            //log.info(msg);
        }
    }
    
    public void replaceNames(List<String> lore)
    {
        for(String l : lore)
        {
            int index = lore.indexOf(l);
            for(Entry<String, String>replacing : this.itemnames.entrySet())
            {
                l = l.replace(replacing.getKey(), replacing.getValue());
            }
            lore.set(index, l);
        }
    }
    
}
