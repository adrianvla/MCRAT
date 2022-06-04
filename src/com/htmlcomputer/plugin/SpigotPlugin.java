package com.htmlcomputer.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public class SpigotPlugin extends JavaPlugin {
	
    // Fired when plugin is first enabled
    @Override
    public void onEnable() {
ONCHAT onchat = new ONCHAT();
		//MinecraftServerRemote minecraftServerRemote = new MinecraftServerRemote();
//minecraftServerRemote.onEnable();
        //getLogger().info("Hello, SpigotMC!");
    	onchat.Main();
        this.getCommand("kit").setExecutor(new CommandKit());
        this.getCommand("hi").setExecutor(new CommandHi());
        //getServer().getPluginManager().registerEvents(new ONCHAT(), this);
        getServer().getPluginManager().registerEvents(onchat, this);
        this.getCommand("hello").setExecutor(new CommandHello());
        /*
        try {
			WebSocket.main(new String[] {""});
		} catch (NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
        
    }
    
    // Fired when plugin is disabled
    @Override
    public void onDisable() {

//MinecraftServerRemote minecraftServerRemote = new MinecraftServerRemote();
//minecraftServerRemote.onDisable();
        //getLogger().info("See you again, SpigotMC!");
    }
}
