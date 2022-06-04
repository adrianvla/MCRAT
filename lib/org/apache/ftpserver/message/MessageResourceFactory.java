// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.message;

import org.apache.ftpserver.message.impl.DefaultMessageResource;
import java.io.File;
import java.util.List;

public class MessageResourceFactory
{
    private List<String> languages;
    private File customMessageDirectory;
    
    public MessageResource createMessageResource() {
        return new DefaultMessageResource(this.languages, this.customMessageDirectory);
    }
    
    public List<String> getLanguages() {
        return this.languages;
    }
    
    public void setLanguages(final List<String> languages) {
        this.languages = languages;
    }
    
    public File getCustomMessageDirectory() {
        return this.customMessageDirectory;
    }
    
    public void setCustomMessageDirectory(final File customMessageDirectory) {
        this.customMessageDirectory = customMessageDirectory;
    }
}
