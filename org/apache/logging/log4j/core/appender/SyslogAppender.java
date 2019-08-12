// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.SyslogLayout;
import org.apache.logging.log4j.core.layout.Rfc5424Layout;
import org.apache.logging.log4j.util.EnglishEnums;
import org.apache.logging.log4j.core.net.Protocol;
import org.apache.logging.log4j.core.layout.LoggerFields;
import java.nio.charset.Charset;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.net.Facility;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.net.AbstractSocketManager;
import org.apache.logging.log4j.core.Filter;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "Syslog", category = "Core", elementType = "appender", printObject = true)
public class SyslogAppender extends SocketAppender
{
    private static final long serialVersionUID = 1L;
    protected static final String RFC5424 = "RFC5424";
    
    protected SyslogAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter, final boolean ignoreExceptions, final boolean immediateFlush, final AbstractSocketManager manager, final Advertiser advertiser) {
        super(name, layout, filter, manager, ignoreExceptions, immediateFlush, advertiser);
    }
    
    @PluginFactory
    public static SyslogAppender createAppender(@PluginAttribute("host") final String host, @PluginAttribute(value = "port", defaultInt = 0) final int port, @PluginAttribute("protocol") final String protocolStr, @PluginElement("SSL") final SslConfiguration sslConfig, @PluginAttribute(value = "connectTimeoutMillis", defaultInt = 0) final int connectTimeoutMillis, @PluginAliases({ "reconnectionDelay" }) @PluginAttribute(value = "reconnectionDelayMillis", defaultInt = 0) final int reconnectionDelayMillis, @PluginAttribute(value = "immediateFail", defaultBoolean = true) final boolean immediateFail, @PluginAttribute("name") final String name, @PluginAttribute(value = "immediateFlush", defaultBoolean = true) final boolean immediateFlush, @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions, @PluginAttribute(value = "facility", defaultString = "LOCAL0") final Facility facility, @PluginAttribute("id") final String id, @PluginAttribute(value = "enterpriseNumber", defaultInt = 18060) final int enterpriseNumber, @PluginAttribute(value = "includeMdc", defaultBoolean = true) final boolean includeMdc, @PluginAttribute("mdcId") final String mdcId, @PluginAttribute("mdcPrefix") final String mdcPrefix, @PluginAttribute("eventPrefix") final String eventPrefix, @PluginAttribute(value = "newLine", defaultBoolean = false) final boolean newLine, @PluginAttribute("newLineEscape") final String escapeNL, @PluginAttribute("appName") final String appName, @PluginAttribute("messageId") final String msgId, @PluginAttribute("mdcExcludes") final String excludes, @PluginAttribute("mdcIncludes") final String includes, @PluginAttribute("mdcRequired") final String required, @PluginAttribute("format") final String format, @PluginElement("Filter") final Filter filter, @PluginConfiguration final Configuration config, @PluginAttribute(value = "charset", defaultString = "UTF-8") final Charset charsetName, @PluginAttribute("exceptionPattern") final String exceptionPattern, @PluginElement("LoggerFields") final LoggerFields[] loggerFields, @PluginAttribute(value = "advertise", defaultBoolean = false) final boolean advertise) {
        final Protocol protocol = EnglishEnums.valueOf(Protocol.class, protocolStr);
        final boolean useTlsMessageFormat = sslConfig != null || protocol == Protocol.SSL;
        final Layout<? extends Serializable> layout = "RFC5424".equalsIgnoreCase(format) ? Rfc5424Layout.createLayout(facility, id, enterpriseNumber, includeMdc, mdcId, mdcPrefix, eventPrefix, newLine, escapeNL, appName, msgId, excludes, includes, required, exceptionPattern, useTlsMessageFormat, loggerFields, config) : SyslogLayout.createLayout(facility, newLine, escapeNL, charsetName);
        if (name == null) {
            SyslogAppender.LOGGER.error("No name provided for SyslogAppender");
            return null;
        }
        final AbstractSocketManager manager = SocketAppender.createSocketManager(name, protocol, host, port, connectTimeoutMillis, sslConfig, reconnectionDelayMillis, immediateFail, layout);
        return new SyslogAppender(name, layout, filter, ignoreExceptions, immediateFlush, manager, advertise ? config.getAdvertiser() : null);
    }
}
