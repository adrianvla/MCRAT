// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.util.List;
import java.util.ArrayList;
import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import java.util.StringTokenizer;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.command.AbstractCommand;

public class OPTS_MLST extends AbstractCommand
{
    private static final String[] AVAILABLE_TYPES;
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        final String argument = request.getArgument();
        final int spIndex = argument.indexOf(32);
        String[] types;
        String listTypes;
        if (spIndex == -1) {
            types = new String[0];
            listTypes = "";
        }
        else {
            listTypes = argument.substring(spIndex + 1);
            final StringTokenizer st = new StringTokenizer(listTypes, ";");
            types = new String[st.countTokens()];
            for (int i = 0; i < types.length; ++i) {
                types[i] = st.nextToken();
            }
        }
        final String[] validatedTypes = this.validateSelectedTypes(types);
        if (validatedTypes != null) {
            session.setAttribute("MLST.types", validatedTypes);
            session.write(LocalizedFtpReply.translate(session, request, context, 200, "OPTS.MLST", listTypes));
        }
        else {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "OPTS.MLST", listTypes));
        }
    }
    
    private String[] validateSelectedTypes(final String[] types) {
        if (types == null) {
            return new String[0];
        }
        final List<String> selectedTypes = new ArrayList<String>();
        for (int i = 0; i < types.length; ++i) {
            for (int j = 0; j < OPTS_MLST.AVAILABLE_TYPES.length; ++j) {
                if (OPTS_MLST.AVAILABLE_TYPES[j].equalsIgnoreCase(types[i])) {
                    selectedTypes.add(OPTS_MLST.AVAILABLE_TYPES[j]);
                    break;
                }
            }
        }
        return selectedTypes.toArray(new String[0]);
    }
    
    static {
        AVAILABLE_TYPES = new String[] { "Size", "Modify", "Type", "Perm" };
    }
}
