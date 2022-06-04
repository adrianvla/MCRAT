// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

public enum IoEventType
{
    SESSION_CREATED, 
    SESSION_OPENED, 
    SESSION_CLOSED, 
    MESSAGE_RECEIVED, 
    MESSAGE_SENT, 
    SESSION_IDLE, 
    EXCEPTION_CAUGHT, 
    WRITE, 
    CLOSE;
}
