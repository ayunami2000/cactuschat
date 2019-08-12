// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.mom.kafka;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.core.util.Log4jThread;
import org.apache.logging.log4j.core.config.Property;
import org.apache.kafka.clients.producer.Producer;
import java.util.Properties;
import org.apache.logging.log4j.core.appender.AbstractManager;

public class KafkaManager extends AbstractManager
{
    public static final String DEFAULT_TIMEOUT_MILLIS = "30000";
    static KafkaProducerFactory producerFactory;
    private final Properties config;
    private Producer<byte[], byte[]> producer;
    private final int timeoutMillis;
    private final String topic;
    
    public KafkaManager(final String name, final String topic, final Property[] properties) {
        super(name);
        this.config = new Properties();
        this.producer = null;
        this.topic = topic;
        this.config.setProperty("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        this.config.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        this.config.setProperty("batch.size", "0");
        for (final Property property : properties) {
            this.config.setProperty(property.getName(), property.getValue());
        }
        this.timeoutMillis = Integer.parseInt(this.config.getProperty("timeout.ms", "30000"));
    }
    
    public void releaseSub() {
        if (this.producer != null) {
            final Thread closeThread = new Log4jThread(new Runnable() {
                @Override
                public void run() {
                    KafkaManager.this.producer.close();
                }
            });
            closeThread.setName("KafkaManager-CloseThread");
            closeThread.setDaemon(true);
            closeThread.start();
            try {
                closeThread.join(this.timeoutMillis);
            }
            catch (InterruptedException ex) {}
        }
    }
    
    public void send(final byte[] msg) throws ExecutionException, InterruptedException, TimeoutException {
        if (this.producer != null) {
            this.producer.send(new ProducerRecord(this.topic, (Object)msg)).get(this.timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }
    
    public void startup() {
        this.producer = KafkaManager.producerFactory.newKafkaProducer(this.config);
    }
    
    static {
        KafkaManager.producerFactory = new DefaultKafkaProducerFactory();
    }
}
