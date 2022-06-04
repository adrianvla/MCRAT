// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import java.util.List;
import org.apache.ftpserver.message.MessageResource;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.command.AbstractCommand;

public class LANG extends AbstractCommand
{
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        String language = request.getArgument();
        if (language == null) {
            session.setLanguage(null);
            session.write(LocalizedFtpReply.translate(session, request, context, 200, "LANG", null));
            return;
        }
        language = language.toLowerCase();
        final MessageResource msgResource = context.getMessageResource();
        final List<String> availableLanguages = msgResource.getAvailableLanguages();
        if (availableLanguages != null) {
            for (int i = 0; i < availableLanguages.size(); ++i) {
                if (availableLanguages.get(i).equals(language)) {
                    session.setLanguage(language);
                    session.write(LocalizedFtpReply.translate(session, request, context, 200, "LANG", null));
                    return;
                }
            }
        }
        session.write(LocalizedFtpReply.translate(session, request, context, 504, "LANG", null));
    }
}
