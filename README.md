# MCRAT
### A minecraft remote access tool.
###### Mainly used to access the console and moderate the server when you don't have the ssh command installed

For Paper 1.20.1 (also compatible with other versions if you manage to compile it)

The rat.java file Is The remote access tool itself

It's going to be hooked up to chat commands in the main files

To change who can execute it, change it in the array:
```java

public String onlyrunners[] = new String[]{YOUR_PLAYER_NAME};
```

##  Usage

#### NEW!
### :ssh username password
  Opens an Apache SSHD server
### :stopssh
  Stops the ssh server
### :sh command
  Runs a shell command on the server
### :stopsh
  Stops the shell command and shows you the output in a book (commands like ping need to be stopped)
### :invincible
  Toggles invincibility (also called god mode, this cancels damage for the player. When this is enabled, /kill doesn't work on the player)
### :download <url> DOWNLOAD_TO:<filename>
  Downloads a given file to the servers directory
### :cd <directory>
  Lists all avaible folders in blue and avaible files in black in a book like GUI (to test it on linux servers, do :cd /)

----------------------------------------------------------------
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
  Forces a player to say the chatmessage. If the chat message'd be something like /..., it'll be executed as a command



# Disclaimer
If you plan on using it, you agree you will install this plugin to servers you own or on servers you have permission to install plugins on. Only use this to access the servers files if you can't do this with the servers panel.

### By using, downloading, adapting in any shape or form this project, you agree to [CC By Sa](https://creativecommons.org/licenses/by-sa/4.0/) . This work is licensed under the same license (excluding every file that's in the libs/ folder).
