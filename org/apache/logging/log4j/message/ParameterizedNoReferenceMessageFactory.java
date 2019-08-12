// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.message;

public final class ParameterizedNoReferenceMessageFactory extends AbstractMessageFactory
{
    public static final ParameterizedNoReferenceMessageFactory INSTANCE;
    private static final long serialVersionUID = 1L;
    
    @Override
    public Message newMessage(final String message, final Object... params) {
        if (params == null) {
            return new SimpleMessage(message);
        }
        final String formatted = new ParameterizedMessage(message, params).getFormattedMessage();
        return new SimpleMessage(formatted);
    }
    
    static {
        INSTANCE = new ParameterizedNoReferenceMessageFactory();
    }
}
