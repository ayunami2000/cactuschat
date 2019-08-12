// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util;

import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.util.PropertiesUtil;
import java.nio.charset.Charset;

public final class Constants
{
    public static final String LOG4J_LOG_EVENT_FACTORY = "Log4jLogEventFactory";
    public static final String LOG4J_CONTEXT_SELECTOR = "Log4jContextSelector";
    public static final String LOG4J_DEFAULT_STATUS_LEVEL = "Log4jDefaultStatusLevel";
    public static final String JNDI_CONTEXT_NAME = "java:comp/env/log4j/context-name";
    public static final String LINE_SEPARATOR;
    public static final int MILLIS_IN_SECONDS = 1000;
    @Deprecated
    public static final Charset UTF_8;
    public static final boolean FORMAT_MESSAGES_IN_BACKGROUND;
    
    private Constants() {
    }
    
    static {
        LINE_SEPARATOR = PropertiesUtil.getProperties().getStringProperty("line.separator", "\n");
        UTF_8 = StandardCharsets.UTF_8;
        FORMAT_MESSAGES_IN_BACKGROUND = PropertiesUtil.getProperties().getBooleanProperty("log4j.format.msg.async", false);
    }
}
