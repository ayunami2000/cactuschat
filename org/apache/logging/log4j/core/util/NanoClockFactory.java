// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util;

import java.util.Objects;

public final class NanoClockFactory
{
    private static volatile Mode mode;
    
    private NanoClockFactory() {
    }
    
    public static NanoClock createNanoClock() {
        return NanoClockFactory.mode.createNanoClock();
    }
    
    public static Mode getMode() {
        return NanoClockFactory.mode;
    }
    
    public static void setMode(final Mode mode) {
        NanoClockFactory.mode = Objects.requireNonNull(mode, "mode must be non-null");
    }
    
    static {
        NanoClockFactory.mode = Mode.Dummy;
    }
    
    public enum Mode
    {
        Dummy {
            @Override
            public NanoClock createNanoClock() {
                return new DummyNanoClock();
            }
        }, 
        System {
            @Override
            public NanoClock createNanoClock() {
                return new SystemNanoClock();
            }
        };
        
        public abstract NanoClock createNanoClock();
    }
}
