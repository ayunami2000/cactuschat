// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import org.apache.logging.log4j.core.net.SslSocketManager;
import org.apache.logging.log4j.core.net.DatagramSocketManager;
import org.apache.logging.log4j.core.net.TcpSocketManager;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.SerializedLayout;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.net.Protocol;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import java.util.Map;
import java.util.HashMap;
import org.apache.logging.log4j.core.Filter;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.net.AbstractSocketManager;

@Plugin(name = "Socket", category = "Core", elementType = "appender", printObject = true)
public class SocketAppender extends AbstractOutputStreamAppender<AbstractSocketManager>
{
    private static final long serialVersionUID = 1L;
    private Object advertisement;
    private final Advertiser advertiser;
    
    protected SocketAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter, final AbstractSocketManager manager, final boolean ignoreExceptions, final boolean immediateFlush, final Advertiser advertiser) {
        super(name, layout, filter, ignoreExceptions, immediateFlush, manager);
        if (advertiser != null) {
            final Map<String, String> configuration = new HashMap<String, String>(layout.getContentFormat());
            configuration.putAll(manager.getContentFormat());
            configuration.put("contentType", layout.getContentType());
            configuration.put("name", name);
            this.advertisement = advertiser.advertise(configuration);
        }
        this.advertiser = advertiser;
    }
    
    @Override
    public void stop() {
        super.stop();
        if (this.advertiser != null) {
            this.advertiser.unadvertise(this.advertisement);
        }
    }
    
    @PluginFactory
    public static SocketAppender createAppender(@PluginAttribute("host") final String host, @PluginAttribute(value = "port", defaultInt = 0) final int port, @PluginAttribute("protocol") final Protocol protocol, @PluginElement("SSL") final SslConfiguration sslConfig, @PluginAttribute(value = "connectTimeoutMillis", defaultInt = 0) final int connectTimeoutMillis, @PluginAliases({ "reconnectionDelay" }) @PluginAttribute(value = "reconnectionDelayMillis", defaultInt = 0) final int reconnectDelayMillis, @PluginAttribute(value = "immediateFail", defaultBoolean = true) final boolean immediateFail, @PluginAttribute("name") final String name, @PluginAttribute(value = "immediateFlush", defaultBoolean = true) boolean immediateFlush, @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions, @PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filter") final Filter filter, @PluginAttribute(value = "advertise", defaultBoolean = false) final boolean advertise, @PluginConfiguration final Configuration config) {
        if (layout == null) {
            layout = SerializedLayout.createLayout();
        }
        if (name == null) {
            SocketAppender.LOGGER.error("No name provided for SocketAppender");
            return null;
        }
        final Protocol actualProtocol = (protocol != null) ? protocol : Protocol.TCP;
        if (actualProtocol == Protocol.UDP) {
            immediateFlush = true;
        }
        final AbstractSocketManager manager = createSocketManager(name, actualProtocol, host, port, connectTimeoutMillis, sslConfig, reconnectDelayMillis, immediateFail, layout);
        return new SocketAppender(name, layout, filter, manager, ignoreExceptions, immediateFlush, advertise ? config.getAdvertiser() : null);
    }
    
    @Deprecated
    public static SocketAppender createAppender(final String host, final String portNum, final String protocolIn, final SslConfiguration sslConfig, final int connectTimeoutMillis, final String delayMillis, final String immediateFail, final String name, final String immediateFlush, final String ignore, final Layout<? extends Serializable> layout, final Filter filter, final String advertise, final Configuration config) {
        final boolean isFlush = Booleans.parseBoolean(immediateFlush, true);
        final boolean isAdvertise = Boolean.parseBoolean(advertise);
        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        final boolean fail = Booleans.parseBoolean(immediateFail, true);
        final int reconnectDelayMillis = AbstractAppender.parseInt(delayMillis, 0);
        final int port = AbstractAppender.parseInt(portNum, 0);
        final Protocol p = (protocolIn == null) ? Protocol.UDP : Protocol.valueOf(protocolIn);
        return createAppender(host, port, p, sslConfig, connectTimeoutMillis, reconnectDelayMillis, fail, name, isFlush, ignoreExceptions, layout, filter, isAdvertise, config);
    }
    
    protected static AbstractSocketManager createSocketManager(final String name, Protocol protocol, final String host, final int port, final int connectTimeoutMillis, final SslConfiguration sslConfig, final int delayMillis, final boolean immediateFail, final Layout<? extends Serializable> layout) {
        if (protocol == Protocol.TCP && sslConfig != null) {
            protocol = Protocol.SSL;
        }
        if (protocol != Protocol.SSL && sslConfig != null) {
            SocketAppender.LOGGER.info("Appender {} ignoring SSL configuration for {} protocol", new Object[] { name, protocol });
        }
        switch (protocol) {
            case TCP: {
                return TcpSocketManager.getSocketManager(host, port, connectTimeoutMillis, delayMillis, immediateFail, layout);
            }
            case UDP: {
                return DatagramSocketManager.getSocketManager(host, port, layout);
            }
            case SSL: {
                return SslSocketManager.getSocketManager(sslConfig, host, port, connectTimeoutMillis, delayMillis, immediateFail, layout);
            }
            default: {
                throw new IllegalArgumentException(protocol.toString());
            }
        }
    }
}
