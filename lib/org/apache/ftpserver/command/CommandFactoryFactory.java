// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command;

import org.apache.ftpserver.command.impl.USER;
import org.apache.ftpserver.command.impl.TYPE;
import org.apache.ftpserver.command.impl.SYST;
import org.apache.ftpserver.command.impl.STRU;
import org.apache.ftpserver.command.impl.STOU;
import org.apache.ftpserver.command.impl.STOR;
import org.apache.ftpserver.command.impl.STAT;
import org.apache.ftpserver.command.impl.SITE_ZONE;
import org.apache.ftpserver.command.impl.SITE_WHO;
import org.apache.ftpserver.command.impl.SITE_STAT;
import org.apache.ftpserver.command.impl.SITE_HELP;
import org.apache.ftpserver.command.impl.SITE_DESCUSER;
import org.apache.ftpserver.command.impl.SIZE;
import org.apache.ftpserver.command.impl.SITE;
import org.apache.ftpserver.command.impl.RNTO;
import org.apache.ftpserver.command.impl.RNFR;
import org.apache.ftpserver.command.impl.RMD;
import org.apache.ftpserver.command.impl.RETR;
import org.apache.ftpserver.command.impl.REST;
import org.apache.ftpserver.command.impl.REIN;
import org.apache.ftpserver.command.impl.QUIT;
import org.apache.ftpserver.command.impl.PWD;
import org.apache.ftpserver.command.impl.PROT;
import org.apache.ftpserver.command.impl.PORT;
import org.apache.ftpserver.command.impl.PBSZ;
import org.apache.ftpserver.command.impl.PASV;
import org.apache.ftpserver.command.impl.PASS;
import org.apache.ftpserver.command.impl.OPTS;
import org.apache.ftpserver.command.impl.NOOP;
import org.apache.ftpserver.command.impl.NLST;
import org.apache.ftpserver.command.impl.MODE;
import org.apache.ftpserver.command.impl.MLSD;
import org.apache.ftpserver.command.impl.MKD;
import org.apache.ftpserver.command.impl.MLST;
import org.apache.ftpserver.command.impl.MDTM;
import org.apache.ftpserver.command.impl.MFMT;
import org.apache.ftpserver.command.impl.MD5;
import org.apache.ftpserver.command.impl.LIST;
import org.apache.ftpserver.command.impl.LANG;
import org.apache.ftpserver.command.impl.HELP;
import org.apache.ftpserver.command.impl.FEAT;
import org.apache.ftpserver.command.impl.EPSV;
import org.apache.ftpserver.command.impl.EPRT;
import org.apache.ftpserver.command.impl.DELE;
import org.apache.ftpserver.command.impl.CWD;
import org.apache.ftpserver.command.impl.CDUP;
import org.apache.ftpserver.command.impl.AUTH;
import org.apache.ftpserver.command.impl.APPE;
import org.apache.ftpserver.command.impl.ACCT;
import org.apache.ftpserver.command.impl.ABOR;
import java.util.Iterator;
import org.apache.ftpserver.command.impl.DefaultCommandFactory;
import java.util.Map;
import java.util.HashMap;

public class CommandFactoryFactory
{
    private static final HashMap<String, Command> DEFAULT_COMMAND_MAP;
    private Map<String, Command> commandMap;
    private boolean useDefaultCommands;
    
    public CommandFactoryFactory() {
        this.commandMap = new HashMap<String, Command>();
        this.useDefaultCommands = true;
    }
    
    public CommandFactory createCommandFactory() {
        final Map<String, Command> mergedCommands = new HashMap<String, Command>();
        if (this.useDefaultCommands) {
            mergedCommands.putAll(CommandFactoryFactory.DEFAULT_COMMAND_MAP);
        }
        mergedCommands.putAll(this.commandMap);
        return new DefaultCommandFactory(mergedCommands);
    }
    
    public boolean isUseDefaultCommands() {
        return this.useDefaultCommands;
    }
    
    public void setUseDefaultCommands(final boolean useDefaultCommands) {
        this.useDefaultCommands = useDefaultCommands;
    }
    
    public Map<String, Command> getCommandMap() {
        return this.commandMap;
    }
    
    public void addCommand(final String commandName, final Command command) {
        if (commandName == null) {
            throw new NullPointerException("commandName can not be null");
        }
        if (command == null) {
            throw new NullPointerException("command can not be null");
        }
        this.commandMap.put(commandName.toUpperCase(), command);
    }
    
    public void setCommandMap(final Map<String, Command> commandMap) {
        if (commandMap == null) {
            throw new NullPointerException("commandMap can not be null");
        }
        this.commandMap.clear();
        for (final Map.Entry<String, Command> entry : commandMap.entrySet()) {
            this.commandMap.put(entry.getKey().toUpperCase(), entry.getValue());
        }
    }
    
    static {
        (DEFAULT_COMMAND_MAP = new HashMap<String, Command>()).put("ABOR", new ABOR());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("ACCT", new ACCT());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("APPE", new APPE());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("AUTH", new AUTH());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("CDUP", new CDUP());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("CWD", new CWD());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("DELE", new DELE());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("EPRT", new EPRT());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("EPSV", new EPSV());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("FEAT", new FEAT());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("HELP", new HELP());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("LANG", new LANG());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("LIST", new LIST());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("MD5", new MD5());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("MFMT", new MFMT());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("MMD5", new MD5());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("MDTM", new MDTM());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("MLST", new MLST());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("MKD", new MKD());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("MLSD", new MLSD());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("MODE", new MODE());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("NLST", new NLST());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("NOOP", new NOOP());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("OPTS", new OPTS());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("PASS", new PASS());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("PASV", new PASV());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("PBSZ", new PBSZ());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("PORT", new PORT());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("PROT", new PROT());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("PWD", new PWD());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("QUIT", new QUIT());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("REIN", new REIN());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("REST", new REST());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("RETR", new RETR());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("RMD", new RMD());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("RNFR", new RNFR());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("RNTO", new RNTO());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("SITE", new SITE());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("SIZE", new SIZE());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("SITE_DESCUSER", new SITE_DESCUSER());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("SITE_HELP", new SITE_HELP());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("SITE_STAT", new SITE_STAT());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("SITE_WHO", new SITE_WHO());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("SITE_ZONE", new SITE_ZONE());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("STAT", new STAT());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("STOR", new STOR());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("STOU", new STOU());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("STRU", new STRU());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("SYST", new SYST());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("TYPE", new TYPE());
        CommandFactoryFactory.DEFAULT_COMMAND_MAP.put("USER", new USER());
    }
}
