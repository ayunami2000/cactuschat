// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import org.apache.logging.log4j.status.StatusLogger;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Layout;
import java.io.Serializable;

public abstract class AbstractLayout<T extends Serializable> implements Layout<T>, Serializable
{
    protected static final Logger LOGGER;
    private static final long serialVersionUID = 1L;
    protected final byte[] header;
    protected final byte[] footer;
    
    public AbstractLayout(final byte[] header, final byte[] footer) {
        this.header = header;
        this.footer = footer;
    }
    
    @Override
    public Map<String, String> getContentFormat() {
        return new HashMap<String, String>();
    }
    
    @Override
    public byte[] getFooter() {
        return this.footer;
    }
    
    @Override
    public byte[] getHeader() {
        return this.header;
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
}
