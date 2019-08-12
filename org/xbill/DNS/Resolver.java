// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.util.List;

public interface Resolver
{
    void setPort(final int p0);
    
    void setTCP(final boolean p0);
    
    void setIgnoreTruncation(final boolean p0);
    
    void setEDNS(final int p0);
    
    void setEDNS(final int p0, final int p1, final int p2, final List p3);
    
    void setTSIGKey(final TSIG p0);
    
    void setTimeout(final int p0, final int p1);
    
    void setTimeout(final int p0);
    
    Message send(final Message p0) throws IOException;
    
    Object sendAsync(final Message p0, final ResolverListener p1);
}
