package com.civrev.breakspawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class BreakSpawn extends JavaPlugin implements Listener {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        // save the default config if it doesn't exist
        saveDefaultConfig();
        // register the listener
        getServer().getPluginManager().registerEvents(this, this);
        // load the config
        loadConfig();
    }

    private void loadConfig() {
        // get the config file
        File configFile = new File(getDataFolder(), "config.yml");
        // create the data folder if it doesn't exist
        if (!configFile.exists()) {
            getDataFolder().mkdir();
        }
        // create the config file if it doesn't exist
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
        // load the config
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // check if block is in config
        Material material = block.getType();
        if (!config.isConfigurationSection("spawns." + material)) {
            return;
        }
        Location location = player.getLocation();
        for (String mobName : config.getConfigurationSection("spawns." + material).getKeys(false)) {
            double chance = config.getDouble("spawns." + material + "." + mobName + ".chance");
            if (Math.random() >= chance) {
                //getLogger().info(String.format("Block break did not meet chance requirement to spawn %s", mobName));
                continue;
            }
            String worldName = config.getString("spawns." + material + "." + mobName + ".world");
            if (worldName != null && !worldName.equals(location.getWorld().getName())) {
                //getLogger().info(String.format("Block break did not occur in correct world to spawn %s", mobName));
                continue;
            }
            int level = config.getInt("spawns." + material + "." + mobName + ".level");
            String command = String.format("mm mobs spawn %s:%d 1 %s,%.2f,%.2f,%.2f,%.1f,%.1f",
                    mobName, level, location.getWorld().getName(),
                    location.getX(), location.getY() + 1, location.getZ(),
                    player.getLocation().getYaw(), player.getLocation().getPitch());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            getLogger().info(String.format("Spawned %s (level %d) at %s",
                    mobName, level, location.toString()));
            break;
        }
    }


    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        // update the config with example data
        config = getConfig();
        config.options().copyDefaults(true);
        config.addDefault("spawns.STONE.gazelle.material", "STONE");
        config.addDefault("spawns.STONE.gazelle.chance", 1);
        config.addDefault("spawns.STONE.gazelle.world", "world");
        config.addDefault("spawns.STONE.gazelle.level", 1);
        saveConfig();
    }

    @Override
    public void saveConfig() {
        // create the data folder if it doesn't exist
        getDataFolder().mkdir();
        // get the config file
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            // save the config file
            config.save(configFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error saving config file", e);
        }
    }
}

