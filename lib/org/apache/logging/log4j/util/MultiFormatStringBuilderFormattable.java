// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.util;

import org.apache.logging.log4j.message.MultiformatMessage;

public interface MultiFormatStringBuilderFormattable extends MultiformatMessage, StringBuilderFormattable
{
    void formatTo(final String[] p0, final StringBuilder p1);
}
