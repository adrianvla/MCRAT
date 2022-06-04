package com.htmlcomputer.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class CommandHello implements CommandExecutor {

    // This method is called, when somebody uses our command
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Bukkit.getOnlinePlayers().forEach(p -> {
            	p.sendMessage("Hello World!");
            });
            
        }

        // If the player (or console) uses our command correct, we can return true
        return true;
    }
}