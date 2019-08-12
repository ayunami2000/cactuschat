// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import java.util.Collections;
import java.util.List;

final class OpenSslDefaultApplicationProtocolNegotiator implements OpenSslApplicationProtocolNegotiator
{
    static final OpenSslDefaultApplicationProtocolNegotiator INSTANCE;
    
    private OpenSslDefaultApplicationProtocolNegotiator() {
    }
    
    @Override
    public List<String> protocols() {
        return Collections.emptyList();
    }
    
    static {
        INSTANCE = new OpenSslDefaultApplicationProtocolNegotiator();
    }
}
