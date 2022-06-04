// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.message.impl;

import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import org.apache.ftpserver.util.IoUtils;
import java.io.IOException;
import org.apache.ftpserver.FtpServerConfigurationException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import org.apache.ftpserver.message.MessageResource;

public class DefaultMessageResource implements MessageResource
{
    private final Logger LOG;
    private static final String RESOURCE_PATH = "org/apache/ftpserver/message/";
    private final List<String> languages;
    private final Map<String, PropertiesPair> messages;
    
    public DefaultMessageResource(final List<String> languages, final File customMessageDirectory) {
        this.LOG = LoggerFactory.getLogger(DefaultMessageResource.class);
        if (languages != null) {
            this.languages = Collections.unmodifiableList((List<? extends String>)languages);
        }
        else {
            this.languages = null;
        }
        this.messages = new HashMap<String, PropertiesPair>();
        if (languages != null) {
            for (final String language : languages) {
                final PropertiesPair pair = this.createPropertiesPair(language, customMessageDirectory);
                this.messages.put(language, pair);
            }
        }
        final PropertiesPair pair2 = this.createPropertiesPair(null, customMessageDirectory);
        this.messages.put(null, pair2);
    }
    
    private PropertiesPair createPropertiesPair(final String lang, final File customMessageDirectory) {
        final PropertiesPair pair = new PropertiesPair();
        String defaultResourceName;
        if (lang == null) {
            defaultResourceName = "org/apache/ftpserver/message/FtpStatus.properties";
        }
        else {
            defaultResourceName = "org/apache/ftpserver/message/FtpStatus_" + lang + ".properties";
        }
        InputStream in = null;
        Label_0166: {
            try {
                in = this.getClass().getClassLoader().getResourceAsStream(defaultResourceName);
                if (in != null) {
                    Label_0148: {
                        try {
                            pair.defaultProperties.load(in);
                            break Label_0148;
                        }
                        catch (IOException e) {
                            throw new FtpServerConfigurationException("Failed to load messages from \"" + defaultResourceName + "\", file not found in classpath");
                        }
                        throw new FtpServerConfigurationException("Failed to load messages from \"" + defaultResourceName + "\", file not found in classpath");
                    }
                    break Label_0166;
                }
                throw new FtpServerConfigurationException("Failed to load messages from \"" + defaultResourceName + "\", file not found in classpath");
            }
            finally {
                IoUtils.close(in);
            }
        }
        File resourceFile = null;
        if (lang == null) {
            resourceFile = new File(customMessageDirectory, "FtpStatus.gen");
        }
        else {
            resourceFile = new File(customMessageDirectory, "FtpStatus_" + lang + ".gen");
        }
        in = null;
        try {
            if (resourceFile.exists()) {
                in = new FileInputStream(resourceFile);
                pair.customProperties.load(in);
            }
        }
        catch (Exception ex) {
            this.LOG.warn("MessageResourceImpl.createPropertiesPair()", ex);
            throw new FtpServerConfigurationException("MessageResourceImpl.createPropertiesPair()", ex);
        }
        finally {
            IoUtils.close(in);
        }
        return pair;
    }
    
    @Override
    public List<String> getAvailableLanguages() {
        if (this.languages == null) {
            return null;
        }
        return Collections.unmodifiableList((List<? extends String>)this.languages);
    }
    
    @Override
    public String getMessage(final int code, final String subId, String language) {
        String key = String.valueOf(code);
        if (subId != null) {
            key = key + '.' + subId;
        }
        String value = null;
        PropertiesPair pair = null;
        if (language != null) {
            language = language.toLowerCase();
            pair = this.messages.get(language);
            if (pair != null) {
                value = pair.customProperties.getProperty(key);
                if (value == null) {
                    value = pair.defaultProperties.getProperty(key);
                }
            }
        }
        if (value == null) {
            pair = this.messages.get(null);
            if (pair != null) {
                value = pair.customProperties.getProperty(key);
                if (value == null) {
                    value = pair.defaultProperties.getProperty(key);
                }
            }
        }
        return value;
    }
    
    @Override
    public Map<String, String> getMessages(String language) {
        final Properties messages = new Properties();
        PropertiesPair pair = this.messages.get(null);
        if (pair != null) {
            messages.putAll(pair.defaultProperties);
            messages.putAll(pair.customProperties);
        }
        if (language != null) {
            language = language.toLowerCase();
            pair = this.messages.get(language);
            if (pair != null) {
                messages.putAll(pair.defaultProperties);
                messages.putAll(pair.customProperties);
            }
        }
        final Map<String, String> result = new HashMap<String, String>();
        for (final Object key : messages.keySet()) {
            result.put(key.toString(), messages.getProperty(key.toString()));
        }
        return Collections.unmodifiableMap((Map<? extends String, ? extends String>)result);
    }
    
    public void dispose() {
        for (final String language : this.messages.keySet()) {
            final PropertiesPair pair = this.messages.get(language);
            pair.customProperties.clear();
            pair.defaultProperties.clear();
        }
        this.messages.clear();
    }
    
    private static class PropertiesPair
    {
        public Properties defaultProperties;
        public Properties customProperties;
        
        private PropertiesPair() {
            this.defaultProperties = new Properties();
            this.customProperties = new Properties();
        }
    }
}
