// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.message;

public interface FlowMessageFactory
{
    EntryMessage newEntryMessage(final Message p0);
    
    ExitMessage newExitMessage(final Object p0, final Message p1);
    
    ExitMessage newExitMessage(final EntryMessage p0);
    
    ExitMessage newExitMessage(final Object p0, final EntryMessage p1);
}
