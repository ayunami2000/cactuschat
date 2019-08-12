// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import java.util.Set;

public interface Cookie extends Comparable<Cookie>
{
    String name();
    
    String value();
    
    void setValue(final String p0);
    
    String rawValue();
    
    void setRawValue(final String p0);
    
    String domain();
    
    void setDomain(final String p0);
    
    String path();
    
    void setPath(final String p0);
    
    String comment();
    
    void setComment(final String p0);
    
    long maxAge();
    
    void setMaxAge(final long p0);
    
    int version();
    
    void setVersion(final int p0);
    
    boolean isSecure();
    
    void setSecure(final boolean p0);
    
    boolean isHttpOnly();
    
    void setHttpOnly(final boolean p0);
    
    String commentUrl();
    
    void setCommentUrl(final String p0);
    
    boolean isDiscard();
    
    void setDiscard(final boolean p0);
    
    Set<Integer> ports();
    
    void setPorts(final int... p0);
    
    void setPorts(final Iterable<Integer> p0);
}
