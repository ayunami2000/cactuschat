// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import javax.net.ssl.SSLException;
import io.netty.util.internal.PlatformDependent;
import java.util.List;
import org.eclipse.jetty.alpn.ALPN;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import io.netty.util.internal.ObjectUtil;
import javax.net.ssl.SSLEngine;

final class JdkAlpnSslEngine extends JdkSslEngine
{
    private static boolean available;
    
    static boolean isAvailable() {
        updateAvailability();
        return JdkAlpnSslEngine.available;
    }
    
    private static void updateAvailability() {
        if (JdkAlpnSslEngine.available) {
            return;
        }
        try {
            ClassLoader bootloader = ClassLoader.getSystemClassLoader().getParent();
            if (bootloader == null) {
                bootloader = ClassLoader.getSystemClassLoader();
            }
            Class.forName("sun.security.ssl.ALPNExtension", true, bootloader);
            JdkAlpnSslEngine.available = true;
        }
        catch (Exception ex) {}
    }
    
    JdkAlpnSslEngine(final SSLEngine engine, final JdkApplicationProtocolNegotiator applicationNegotiator, final boolean server) {
        super(engine);
        ObjectUtil.checkNotNull(applicationNegotiator, "applicationNegotiator");
        if (server) {
            final JdkApplicationProtocolNegotiator.ProtocolSelector protocolSelector = ObjectUtil.checkNotNull(applicationNegotiator.protocolSelectorFactory().newSelector(this, new HashSet<String>(applicationNegotiator.protocols())), "protocolSelector");
            ALPN.put(engine, (ALPN.Provider)new ALPN.ServerProvider() {
                public String select(final List<String> protocols) {
                    try {
                        return protocolSelector.select(protocols);
                    }
                    catch (Throwable t) {
                        PlatformDependent.throwException(t);
                        return null;
                    }
                }
                
                public void unsupported() {
                    protocolSelector.unsupported();
                }
            });
        }
        else {
            final JdkApplicationProtocolNegotiator.ProtocolSelectionListener protocolListener = ObjectUtil.checkNotNull(applicationNegotiator.protocolListenerFactory().newListener(this, applicationNegotiator.protocols()), "protocolListener");
            ALPN.put(engine, (ALPN.Provider)new ALPN.ClientProvider() {
                public List<String> protocols() {
                    return applicationNegotiator.protocols();
                }
                
                public void selected(final String protocol) {
                    try {
                        protocolListener.selected(protocol);
                    }
                    catch (Throwable t) {
                        PlatformDependent.throwException(t);
                    }
                }
                
                public void unsupported() {
                    protocolListener.unsupported();
                }
            });
        }
    }
    
    @Override
    public void closeInbound() throws SSLException {
        ALPN.remove(this.getWrappedEngine());
        super.closeInbound();
    }
    
    @Override
    public void closeOutbound() {
        ALPN.remove(this.getWrappedEngine());
        super.closeOutbound();
    }
}
