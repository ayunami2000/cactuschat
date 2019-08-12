// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.message;

public final class SimpleMessageFactory extends AbstractMessageFactory
{
    public static final SimpleMessageFactory INSTANCE;
    private static final long serialVersionUID = 1L;
    
    @Override
    public Message newMessage(final String message, final Object... params) {
        return new SimpleMessage(message);
    }
    
    static {
        INSTANCE = new SimpleMessageFactory();
    }
}
