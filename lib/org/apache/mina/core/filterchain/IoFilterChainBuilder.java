// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.filterchain;

public interface IoFilterChainBuilder
{
    public static final IoFilterChainBuilder NOOP = new IoFilterChainBuilder() {
        @Override
        public void buildFilterChain(final IoFilterChain chain) throws Exception {
        }
        
        @Override
        public String toString() {
            return "NOOP";
        }
    };
    
    void buildFilterChain(final IoFilterChain p0) throws Exception;
}
