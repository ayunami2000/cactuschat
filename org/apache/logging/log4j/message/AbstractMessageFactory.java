// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.message;

import java.io.Serializable;

public abstract class AbstractMessageFactory implements MessageFactory, Serializable
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public Message newMessage(final Object message) {
        return new ObjectMessage(message);
    }
    
    @Override
    public Message newMessage(final String message) {
        return new SimpleMessage(message);
    }
    
    @Override
    public abstract Message newMessage(final String p0, final Object... p1);
}
