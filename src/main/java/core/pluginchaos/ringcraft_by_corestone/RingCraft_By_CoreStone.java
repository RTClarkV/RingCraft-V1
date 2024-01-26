package core.pluginchaos.ringcraft_by_corestone;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class RingCraft_By_CoreStone extends JavaPlugin {
    FileConfiguration config;
    @Override
    public void onEnable() {
        // Plugin startup logic
        config = getConfig();
        config.options().copyDefaults(true);
        saveDefaultConfig();
        new RingManager(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
