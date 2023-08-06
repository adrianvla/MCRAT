package org.keke.mcrat;

import org.bukkit.plugin.java.JavaPlugin;

public final class MCRAT extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new rat(), this);
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
