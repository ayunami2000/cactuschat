// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.message;

public final class StringFormatterMessageFactory extends AbstractMessageFactory
{
    public static final StringFormatterMessageFactory INSTANCE;
    private static final long serialVersionUID = 1L;
    
    @Override
    public Message newMessage(final String message, final Object... params) {
        return new StringFormattedMessage(message, params);
    }
    
    static {
        INSTANCE = new StringFormatterMessageFactory();
    }
}
