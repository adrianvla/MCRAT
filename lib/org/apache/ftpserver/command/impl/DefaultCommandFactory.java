// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.util.HashMap;
import org.apache.ftpserver.command.Command;
import java.util.Map;
import org.apache.ftpserver.command.CommandFactory;

public class DefaultCommandFactory implements CommandFactory
{
    private final Map<String, Command> commandMap;
    
    public DefaultCommandFactory() {
        this(new HashMap<String, Command>());
    }
    
    public DefaultCommandFactory(final Map<String, Command> commandMap) {
        this.commandMap = commandMap;
    }
    
    @Override
    public Command getCommand(final String cmdName) {
        if (cmdName == null || cmdName.equals("")) {
            return null;
        }
        final String upperCaseCmdName = cmdName.toUpperCase();
        return this.commandMap.get(upperCaseCmdName);
    }
}
