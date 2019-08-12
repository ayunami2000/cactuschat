// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.mom.kafka;

import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.util.StringEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.layout.SerializedLayout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.appender.AbstractAppender;

@Plugin(name = "Kafka", category = "Core", elementType = "appender", printObject = true)
public final class KafkaAppender extends AbstractAppender
{
    private static final long serialVersionUID = 1L;
    private final KafkaManager manager;
    
    @PluginFactory
    public static KafkaAppender createAppender(@PluginElement("Layout") final Layout<? extends Serializable> layout, @PluginElement("Filter") final Filter filter, @Required(message = "No name provided for KafkaAppender") @PluginAttribute("name") final String name, @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions, @Required(message = "No topic provided for KafkaAppender") @PluginAttribute("topic") final String topic, @PluginElement("Properties") final Property[] properties) {
        final KafkaManager kafkaManager = new KafkaManager(name, topic, properties);
        return new KafkaAppender(name, layout, filter, ignoreExceptions, kafkaManager);
    }
    
    private KafkaAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter, final boolean ignoreExceptions, final KafkaManager manager) {
        super(name, filter, layout, ignoreExceptions);
        this.manager = manager;
    }
    
    @Override
    public void append(final LogEvent event) {
        if (event.getLoggerName().startsWith("org.apache.kafka")) {
            KafkaAppender.LOGGER.warn("Recursive logging from [{}] for appender [{}].", new Object[] { event.getLoggerName(), this.getName() });
        }
        else {
            try {
                final Layout<? extends Serializable> layout = this.getLayout();
                byte[] data;
                if (layout != null) {
                    if (layout instanceof SerializedLayout) {
                        final byte[] header = layout.getHeader();
                        final byte[] body = layout.toByteArray(event);
                        data = new byte[header.length + body.length];
                        System.arraycopy(header, 0, data, 0, header.length);
                        System.arraycopy(body, 0, data, header.length, body.length);
                    }
                    else {
                        data = layout.toByteArray(event);
                    }
                }
                else {
                    data = StringEncoder.toBytes(event.getMessage().getFormattedMessage(), StandardCharsets.UTF_8);
                }
                this.manager.send(data);
            }
            catch (Exception e) {
                KafkaAppender.LOGGER.error("Unable to write to Kafka [{}] for appender [{}].", new Object[] { this.manager.getName(), this.getName(), e });
                throw new AppenderLoggingException("Unable to write to Kafka in appender: " + e.getMessage(), e);
            }
        }
    }
    
    @Override
    public void start() {
        super.start();
        this.manager.startup();
    }
    
    @Override
    public void stop() {
        super.stop();
        this.manager.release();
    }
}
