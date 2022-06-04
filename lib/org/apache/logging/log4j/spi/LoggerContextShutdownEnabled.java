// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.spi;

import java.util.List;

public interface LoggerContextShutdownEnabled
{
    void addShutdownListener(final LoggerContextShutdownAware p0);
    
    List<LoggerContextShutdownAware> getListeners();
}
