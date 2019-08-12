// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.mom.jeromq;

import org.apache.logging.log4j.core.util.Log4jThread;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.status.StatusLogger;
import java.util.Iterator;
import java.util.Arrays;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.util.Strings;
import java.util.ArrayList;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Filter;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZMQ;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.appender.AbstractAppender;

@Plugin(name = "JeroMQ", category = "Core", elementType = "appender", printObject = true)
public final class JeroMqAppender extends AbstractAppender
{
    static final String SYS_PROPERTY_ENABLE_SHUTDOWN_HOOK = "log4j.jeromq.enableShutdownHook";
    static final String SYS_PROPERTY_IO_THREADS = "log4j.jeromq.ioThreads";
    private static volatile ZMQ.Context context;
    private static final int DEFAULT_BACKLOG = 100;
    private static final int DEFAULT_IVL = 100;
    private static final int DEFAULT_RCV_HWM = 1000;
    private static final int DEFAULT_SND_HWM = 1000;
    private static Logger logger;
    private static ZMQ.Socket publisher;
    private static final long serialVersionUID = 1L;
    private static final String SIMPLE_NAME;
    private final long affinity;
    private final long backlog;
    private final boolean delayAttachOnConnect;
    private final List<String> endpoints;
    private final byte[] identity;
    private final int ioThreads = 1;
    private final boolean ipv4Only;
    private final long linger;
    private final long maxMsgSize;
    private final long rcvHwm;
    private final long receiveBufferSize;
    private final int receiveTimeOut;
    private final long reconnectIVL;
    private final long reconnectIVLMax;
    private final long sendBufferSize;
    private int sendRcFalse;
    private int sendRcTrue;
    private final int sendTimeOut;
    private final long sndHwm;
    private final int tcpKeepAlive;
    private final long tcpKeepAliveCount;
    private final long tcpKeepAliveIdle;
    private final long tcpKeepAliveInterval;
    private final boolean xpubVerbose;
    
    private JeroMqAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout, final boolean ignoreExceptions, final List<String> endpoints, final long affinity, final long backlog, final boolean delayAttachOnConnect, final byte[] identity, final boolean ipv4Only, final long linger, final long maxMsgSize, final long rcvHwm, final long receiveBufferSize, final int receiveTimeOut, final long reconnectIVL, final long reconnectIVLMax, final long sendBufferSize, final int sendTimeOut, final long sndHWM, final int tcpKeepAlive, final long tcpKeepAliveCount, final long tcpKeepAliveIdle, final long tcpKeepAliveInterval, final boolean xpubVerbose) {
        super(name, filter, layout, ignoreExceptions);
        this.endpoints = endpoints;
        this.affinity = affinity;
        this.backlog = backlog;
        this.delayAttachOnConnect = delayAttachOnConnect;
        this.identity = identity;
        this.ipv4Only = ipv4Only;
        this.linger = linger;
        this.maxMsgSize = maxMsgSize;
        this.rcvHwm = rcvHwm;
        this.receiveBufferSize = receiveBufferSize;
        this.receiveTimeOut = receiveTimeOut;
        this.reconnectIVL = reconnectIVL;
        this.reconnectIVLMax = reconnectIVLMax;
        this.sendBufferSize = sendBufferSize;
        this.sendTimeOut = sendTimeOut;
        this.sndHwm = sndHWM;
        this.tcpKeepAlive = tcpKeepAlive;
        this.tcpKeepAliveCount = tcpKeepAliveCount;
        this.tcpKeepAliveIdle = tcpKeepAliveIdle;
        this.tcpKeepAliveInterval = tcpKeepAliveInterval;
        this.xpubVerbose = xpubVerbose;
    }
    
    @PluginFactory
    public static JeroMqAppender createAppender(@Required(message = "No name provided for JeroMqAppender") @PluginAttribute("name") final String name, @PluginElement("Layout") Layout<?> layout, @PluginElement("Filter") final Filter filter, @PluginElement("Properties") final Property[] properties, @PluginAttribute("ignoreExceptions") final boolean ignoreExceptions, @PluginAttribute(value = "affinity", defaultLong = 0L) final long affinity, @PluginAttribute(value = "backlog", defaultLong = 100L) final long backlog, @PluginAttribute(value = "delayAttachOnConnect", defaultBoolean = false) final boolean delayAttachOnConnect, @PluginAttribute("identity") final byte[] identity, @PluginAttribute(value = "ipv4Only", defaultBoolean = true) final boolean ipv4Only, @PluginAttribute(value = "linger", defaultLong = -1L) final long linger, @PluginAttribute(value = "maxMsgSize", defaultLong = -1L) final long maxMsgSize, @PluginAttribute(value = "rcvHwm", defaultLong = 1000L) final long rcvHwm, @PluginAttribute(value = "receiveBufferSize", defaultLong = 0L) final long receiveBufferSize, @PluginAttribute(value = "receiveTimeOut", defaultLong = -1L) final int receiveTimeOut, @PluginAttribute(value = "reconnectIVL", defaultLong = 100L) final long reconnectIVL, @PluginAttribute(value = "reconnectIVLMax", defaultLong = 0L) final long reconnectIVLMax, @PluginAttribute(value = "sendBufferSize", defaultLong = 0L) final long sendBufferSize, @PluginAttribute(value = "sendTimeOut", defaultLong = -1L) final int sendTimeOut, @PluginAttribute(value = "sndHwm", defaultLong = 1000L) final long sndHwm, @PluginAttribute(value = "tcpKeepAlive", defaultInt = -1) final int tcpKeepAlive, @PluginAttribute(value = "tcpKeepAliveCount", defaultLong = -1L) final long tcpKeepAliveCount, @PluginAttribute(value = "tcpKeepAliveIdle", defaultLong = -1L) final long tcpKeepAliveIdle, @PluginAttribute(value = "tcpKeepAliveInterval", defaultLong = -1L) final long tcpKeepAliveInterval, @PluginAttribute(value = "xpubVerbose", defaultBoolean = false) final boolean xpubVerbose) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        List<String> endpoints;
        if (properties == null) {
            endpoints = new ArrayList<String>(0);
        }
        else {
            endpoints = new ArrayList<String>(properties.length);
            for (final Property property : properties) {
                if ("endpoint".equalsIgnoreCase(property.getName())) {
                    final String value = property.getValue();
                    if (Strings.isNotEmpty(value)) {
                        endpoints.add(value);
                    }
                }
            }
        }
        JeroMqAppender.logger.debug("Creating JeroMqAppender with name={}, filter={}, layout={}, ignoreExceptions={}, endpoints={}", new Object[] { name, filter, layout, ignoreExceptions, endpoints });
        return new JeroMqAppender(name, filter, (Layout<? extends Serializable>)layout, ignoreExceptions, endpoints, affinity, backlog, delayAttachOnConnect, identity, ipv4Only, linger, maxMsgSize, rcvHwm, receiveBufferSize, receiveTimeOut, reconnectIVL, reconnectIVLMax, sendBufferSize, sendTimeOut, sndHwm, tcpKeepAlive, tcpKeepAliveCount, tcpKeepAliveIdle, tcpKeepAliveInterval, xpubVerbose);
    }
    
    static ZMQ.Context getContext() {
        return JeroMqAppender.context;
    }
    
    private static ZMQ.Socket getPublisher() {
        return JeroMqAppender.publisher;
    }
    
    private static ZMQ.Socket newPublisher() {
        JeroMqAppender.logger.trace("{} creating a new ZMQ PUB socket with context {}", new Object[] { JeroMqAppender.SIMPLE_NAME, JeroMqAppender.context });
        final ZMQ.Socket socketPub = JeroMqAppender.context.socket(1);
        JeroMqAppender.logger.trace("{} created new ZMQ PUB socket {}", new Object[] { JeroMqAppender.SIMPLE_NAME, socketPub });
        return socketPub;
    }
    
    static void shutdown() {
        if (JeroMqAppender.context != null) {
            JeroMqAppender.logger.trace("{} terminating JeroMQ context {}", new Object[] { JeroMqAppender.SIMPLE_NAME, JeroMqAppender.context });
            JeroMqAppender.context.term();
            JeroMqAppender.context = null;
        }
    }
    
    @Override
    public synchronized void append(final LogEvent event) {
        final String formattedMessage = event.getMessage().getFormattedMessage();
        if (getPublisher().send(formattedMessage, 0)) {
            ++this.sendRcTrue;
        }
        else {
            ++this.sendRcFalse;
            JeroMqAppender.logger.error("Appender {} could not send message {} to JeroMQ {}", new Object[] { this.getName(), this.sendRcFalse, formattedMessage });
        }
    }
    
    int getSendRcFalse() {
        return this.sendRcFalse;
    }
    
    int getSendRcTrue() {
        return this.sendRcTrue;
    }
    
    void resetSendRcs() {
        final int n = 0;
        this.sendRcFalse = n;
        this.sendRcTrue = n;
    }
    
    @Override
    public synchronized void start() {
        super.start();
        JeroMqAppender.publisher = newPublisher();
        final String name = this.getName();
        final String prefix = "JeroMQ Appender";
        JeroMqAppender.logger.debug("Starting {} {} using ZMQ version {}", new Object[] { "JeroMQ Appender", name, ZMQ.getVersionString() });
        JeroMqAppender.logger.debug("{} {} context {} with ioThreads={}", new Object[] { "JeroMQ Appender", name, JeroMqAppender.context, 1 });
        final ZMQ.Socket socketPub = getPublisher();
        JeroMqAppender.logger.trace("{} {} setting {} publisher properties for instance {}", new Object[] { "JeroMQ Appender", name, socketPub.getClass().getName(), socketPub });
        JeroMqAppender.logger.trace("{} {} publisher setAffinity({})", new Object[] { "JeroMQ Appender", name, this.affinity });
        socketPub.setAffinity(this.affinity);
        JeroMqAppender.logger.trace("{} {} publisher setBacklog({})", new Object[] { "JeroMQ Appender", name, this.backlog });
        socketPub.setBacklog(this.backlog);
        JeroMqAppender.logger.trace("{} {} publisher setDelayAttachOnConnect({})", new Object[] { "JeroMQ Appender", name, this.delayAttachOnConnect });
        socketPub.setDelayAttachOnConnect(this.delayAttachOnConnect);
        if (this.identity != null) {
            JeroMqAppender.logger.trace("{} {} publisher setIdentity({})", new Object[] { "JeroMQ Appender", name, Arrays.toString(this.identity) });
            socketPub.setIdentity(this.identity);
        }
        JeroMqAppender.logger.trace("{} {} publisher setIPv4Only({})", new Object[] { "JeroMQ Appender", name, this.ipv4Only });
        socketPub.setIPv4Only(this.ipv4Only);
        JeroMqAppender.logger.trace("{} {} publisher setLinger({})", new Object[] { "JeroMQ Appender", name, this.linger });
        socketPub.setLinger(this.linger);
        JeroMqAppender.logger.trace("{} {} publisher setMaxMsgSize({})", new Object[] { "JeroMQ Appender", name, this.maxMsgSize });
        socketPub.setMaxMsgSize(this.maxMsgSize);
        JeroMqAppender.logger.trace("{} {} publisher setRcvHWM({})", new Object[] { "JeroMQ Appender", name, this.rcvHwm });
        socketPub.setRcvHWM(this.rcvHwm);
        JeroMqAppender.logger.trace("{} {} publisher setReceiveBufferSize({})", new Object[] { "JeroMQ Appender", name, this.receiveBufferSize });
        socketPub.setReceiveBufferSize(this.receiveBufferSize);
        JeroMqAppender.logger.trace("{} {} publisher setReceiveTimeOut({})", new Object[] { "JeroMQ Appender", name, this.receiveTimeOut });
        socketPub.setReceiveTimeOut(this.receiveTimeOut);
        JeroMqAppender.logger.trace("{} {} publisher setReconnectIVL({})", new Object[] { "JeroMQ Appender", name, this.reconnectIVL });
        socketPub.setReconnectIVL(this.reconnectIVL);
        JeroMqAppender.logger.trace("{} {} publisher setReconnectIVLMax({})", new Object[] { "JeroMQ Appender", name, this.reconnectIVLMax });
        socketPub.setReconnectIVLMax(this.reconnectIVLMax);
        JeroMqAppender.logger.trace("{} {} publisher setSendBufferSize({})", new Object[] { "JeroMQ Appender", name, this.sendBufferSize });
        socketPub.setSendBufferSize(this.sendBufferSize);
        JeroMqAppender.logger.trace("{} {} publisher setSendTimeOut({})", new Object[] { "JeroMQ Appender", name, this.sendTimeOut });
        socketPub.setSendTimeOut(this.sendTimeOut);
        JeroMqAppender.logger.trace("{} {} publisher setSndHWM({})", new Object[] { "JeroMQ Appender", name, this.sndHwm });
        socketPub.setSndHWM(this.sndHwm);
        JeroMqAppender.logger.trace("{} {} publisher setTCPKeepAlive({})", new Object[] { "JeroMQ Appender", name, this.tcpKeepAlive });
        socketPub.setTCPKeepAlive(this.tcpKeepAlive);
        JeroMqAppender.logger.trace("{} {} publisher setTCPKeepAliveCount({})", new Object[] { "JeroMQ Appender", name, this.tcpKeepAliveCount });
        socketPub.setTCPKeepAliveCount(this.tcpKeepAliveCount);
        JeroMqAppender.logger.trace("{} {} publisher setTCPKeepAliveIdle({})", new Object[] { "JeroMQ Appender", name, this.tcpKeepAliveIdle });
        socketPub.setTCPKeepAliveIdle(this.tcpKeepAliveIdle);
        JeroMqAppender.logger.trace("{} {} publisher setTCPKeepAliveInterval({})", new Object[] { "JeroMQ Appender", name, this.tcpKeepAliveInterval });
        socketPub.setTCPKeepAliveInterval(this.tcpKeepAliveInterval);
        JeroMqAppender.logger.trace("{} {} publisher setXpubVerbose({})", new Object[] { "JeroMQ Appender", name, this.xpubVerbose });
        socketPub.setXpubVerbose(this.xpubVerbose);
        if (JeroMqAppender.logger.isDebugEnabled()) {
            JeroMqAppender.logger.debug("Created JeroMQ {} publisher {} type {}, affinity={}, backlog={}, delayAttachOnConnect={}, events={}, IPv4Only={}, linger={}, maxMsgSize={}, multicastHops={}, rate={}, rcvHWM={}, receiveBufferSize={}, receiveTimeOut={}, reconnectIVL={}, reconnectIVLMax={}, recoveryInterval={}, sendBufferSize={}, sendTimeOut={}, sndHWM={}, TCPKeepAlive={}, TCPKeepAliveCount={}, TCPKeepAliveIdle={}, TCPKeepAliveInterval={}, TCPKeepAliveSetting={}", new Object[] { name, socketPub, socketPub.getType(), socketPub.getAffinity(), socketPub.getBacklog(), socketPub.getDelayAttachOnConnect(), socketPub.getEvents(), socketPub.getIPv4Only(), socketPub.getLinger(), socketPub.getMaxMsgSize(), socketPub.getMulticastHops(), socketPub.getRate(), socketPub.getRcvHWM(), socketPub.getReceiveBufferSize(), socketPub.getReceiveTimeOut(), socketPub.getReconnectIVL(), socketPub.getReconnectIVLMax(), socketPub.getRecoveryInterval(), socketPub.getSendBufferSize(), socketPub.getSendTimeOut(), socketPub.getSndHWM(), socketPub.getTCPKeepAlive(), socketPub.getTCPKeepAliveCount(), socketPub.getTCPKeepAliveIdle(), socketPub.getTCPKeepAliveInterval(), socketPub.getTCPKeepAliveSetting() });
        }
        for (final String endpoint : this.endpoints) {
            JeroMqAppender.logger.debug("Binding {} appender {} to endpoint {}", new Object[] { JeroMqAppender.SIMPLE_NAME, name, endpoint });
            socketPub.bind(endpoint);
        }
    }
    
    @Override
    public synchronized void stop() {
        super.stop();
        final ZMQ.Socket socketPub = getPublisher();
        if (socketPub != null) {
            JeroMqAppender.logger.debug("Closing {} appender {} publisher {}", new Object[] { JeroMqAppender.SIMPLE_NAME, this.getName(), socketPub });
            socketPub.close();
            JeroMqAppender.publisher = null;
        }
    }
    
    @Override
    public String toString() {
        return "JeroMqAppender [context=" + JeroMqAppender.context + ", publisher=" + JeroMqAppender.publisher + ", endpoints=" + this.endpoints + "]";
    }
    
    static {
        SIMPLE_NAME = JeroMqAppender.class.getSimpleName();
        JeroMqAppender.logger = StatusLogger.getLogger();
        final PropertiesUtil managerProps = PropertiesUtil.getProperties();
        final Integer ioThreads = managerProps.getIntegerProperty("log4j.jeromq.ioThreads", 1);
        final Boolean enableShutdownHook = managerProps.getBooleanProperty("log4j.jeromq.enableShutdownHook", true);
        final String simpleName = JeroMqAppender.SIMPLE_NAME;
        JeroMqAppender.logger.trace("{} using ZMQ version {}", new Object[] { simpleName, ZMQ.getVersionString() });
        JeroMqAppender.logger.trace("{} creating ZMQ context with ioThreads={}", new Object[] { simpleName, ioThreads });
        JeroMqAppender.context = ZMQ.context((int)ioThreads);
        JeroMqAppender.logger.trace("{} created ZMQ context {}", new Object[] { simpleName, JeroMqAppender.context });
        if (enableShutdownHook) {
            final Thread hook = new Log4jThread(simpleName + "-shutdown") {
                @Override
                public void run() {
                    JeroMqAppender.shutdown();
                }
            };
            JeroMqAppender.logger.trace("{} adding shutdown hook {}", new Object[] { simpleName, hook });
            Runtime.getRuntime().addShutdownHook(hook);
        }
    }
}
