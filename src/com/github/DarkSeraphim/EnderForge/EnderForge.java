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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
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
            
    private String forgeLoc;
    
    public final Map<String, String> crafting = new HashMap<String, String>();
    
    private final Map<String, EnderRecipe> recipeMap = new HashMap<String, EnderRecipe>();
    
    MenuManager mm;
    
    final Logger debugger = Logger.getLogger("[EF-DEBUG]");
    
    public boolean debug = false;
    
    public boolean changed = false;
    
    private HashMap<String, String> itemnames = new HashMap<String, String>();
    
    private InventoryListener il;
    
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
                        String in;
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

        mm = new MenuManager(this);
        
        this.il = new InventoryListener(this);
        Bukkit.getPluginManager().registerEvents(new InteractionListener(this), this);
        Bukkit.getPluginManager().registerEvents(this.il, this);
        Bukkit.getPluginManager().registerEvents(new CraftingListener(this), this);
        
        this.debug = getConfig().getBoolean("debug-enabled", false);
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
                    String in;
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
            debug("Starting creating "+recipeEntry.getKey());
            YamlConfiguration recipe = recipeEntry.getValue();
            id = recipe.getInt("id", -1);
            amount = recipe.getInt("amount", -1);
            data = recipe.getInt("data", 0);
            if(id > 0 && amount > 0)
            {
                debug("Adding "+recipeEntry.getKey());
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
                        debug("Failed to add enchantment");
                        continue;
                    }
                    debug("Added enchantment" + ench);
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
                    if(idval.length != 2)
                    {
                        debug("Not a valid item idval: "+item);
                        continue;
                    }
                    try
                    {
                        id = Integer.parseInt(idval[0]);
                        data = Integer.parseInt(idval[1]);
                    }
                    catch(NumberFormatException ex)
                    {
                        debug("Not a valid item: "+item);
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
                    debug("Ingredient added");
                    items.add(is);
                }
                if(items.size() < 1)
                {
                    debug("no recipe found");
                    continue;
                }
                EnderRecipe er = new EnderRecipe(i)
                        .setName(recipeEntry.getKey())
                        .setLore(recipe.getStringList("lore").toArray(new String[0]))
                        .addEnchantments(enchs)
                        .addIngredients(items.toArray(new ItemStack[0]));
                debug("Adding "+recipeEntry.getKey());
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
        String world = getConfig().getString("forge.world", "world");
        int x = getConfig().getInt("forge.x", 0);
        int y = getConfig().getInt("forge.y", 0);
        int z = getConfig().getInt("forge.z", 0);
        this.forgeLoc = world+","+x+","+y+","+z;        
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("ef"))
        {
            if(args.length == 1)
            {
                
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
    
    public String getForgeLocation()
    {
        return this.forgeLoc;
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
    
    public short getActualData(String name)
    {
        for(Entry<String, String> replacement : this.itemnames.entrySet())
        {
            if(replacement.getValue().equals(name))
            {
                try
                {
                    String[] idval = replacement.getKey().split(":");
                    return Short.parseShort(idval[1]);
                }
                catch(ArrayIndexOutOfBoundsException ex)
                {
                    continue;
                }
                catch(NumberFormatException ex)
                {
                    continue;
                }
            }
        }
        return 0;
    }
    
    public List<String> getRecipeLore(EnderRecipe er, ItemStack[] matrix)
    {
        return il.getRecipeLore(er, matrix);
    }
    
     public static String translateAlternateColorCodesBackwards(char altColorChar, String textToTranslate) 
     {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) 
        {
            if (b[i] == ChatColor.COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) 
            {
                b[i] = altColorChar;
                //b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }
    
}
