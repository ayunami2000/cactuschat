// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.message;

public class FormattedMessageFactory extends AbstractMessageFactory
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public Message newMessage(final String message, final Object... params) {
        return new FormattedMessage(message, params);
    }
}
