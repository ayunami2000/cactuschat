// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import io.netty.util.internal.ObjectUtil;
import java.util.List;

public final class OpenSslNpnApplicationProtocolNegotiator implements OpenSslApplicationProtocolNegotiator
{
    private final List<String> protocols;
    
    public OpenSslNpnApplicationProtocolNegotiator(final Iterable<String> protocols) {
        this.protocols = ObjectUtil.checkNotNull(ApplicationProtocolUtil.toList(protocols), "protocols");
    }
    
    public OpenSslNpnApplicationProtocolNegotiator(final String... protocols) {
        this.protocols = ObjectUtil.checkNotNull(ApplicationProtocolUtil.toList(protocols), "protocols");
    }
    
    @Override
    public List<String> protocols() {
        return this.protocols;
    }
}
