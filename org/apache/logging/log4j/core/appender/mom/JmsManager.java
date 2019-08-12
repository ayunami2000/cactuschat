// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.mom;

import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.status.StatusLogger;
import javax.jms.Message;
import java.io.Serializable;
import javax.jms.MessageProducer;
import javax.jms.MessageConsumer;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.Connection;
import org.apache.logging.log4j.core.net.JndiManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.AbstractManager;

public class JmsManager extends AbstractManager
{
    private static final Logger LOGGER;
    private static final JmsManagerFactory FACTORY;
    private final JndiManager jndiManager;
    private final Connection connection;
    private final Session session;
    private final Destination destination;
    
    private JmsManager(final String name, final JndiManager jndiManager, final String connectionFactoryName, final String destinationName, final String username, final String password) throws NamingException, JMSException {
        super(name);
        this.jndiManager = jndiManager;
        final ConnectionFactory connectionFactory = this.jndiManager.lookup(connectionFactoryName);
        if (username != null && password != null) {
            this.connection = connectionFactory.createConnection(username, password);
        }
        else {
            this.connection = connectionFactory.createConnection();
        }
        this.session = this.connection.createSession(false, 1);
        this.destination = this.jndiManager.lookup(destinationName);
        this.connection.start();
    }
    
    public static JmsManager getJmsManager(final String name, final JndiManager jndiManager, final String connectionFactoryName, final String destinationName, final String username, final String password) {
        final JmsConfiguration configuration = new JmsConfiguration(jndiManager, connectionFactoryName, destinationName, username, password);
        return JmsManager.FACTORY.createManager(name, configuration);
    }
    
    public MessageConsumer createMessageConsumer() throws JMSException {
        return this.session.createConsumer(this.destination);
    }
    
    public MessageProducer createMessageProducer() throws JMSException {
        return this.session.createProducer(this.destination);
    }
    
    public Message createMessage(final Serializable object) throws JMSException {
        if (object instanceof String) {
            return (Message)this.session.createTextMessage((String)object);
        }
        return (Message)this.session.createObjectMessage(object);
    }
    
    @Override
    protected void releaseSub() {
        try {
            this.session.close();
        }
        catch (JMSException ex) {}
        try {
            this.connection.close();
        }
        catch (JMSException ex2) {}
        this.jndiManager.release();
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
        FACTORY = new JmsManagerFactory();
    }
    
    private static class JmsConfiguration
    {
        private final JndiManager jndiManager;
        private final String connectionFactoryName;
        private final String destinationName;
        private final String username;
        private final String password;
        
        private JmsConfiguration(final JndiManager jndiManager, final String connectionFactoryName, final String destinationName, final String username, final String password) {
            this.jndiManager = jndiManager;
            this.connectionFactoryName = connectionFactoryName;
            this.destinationName = destinationName;
            this.username = username;
            this.password = password;
        }
    }
    
    private static class JmsManagerFactory implements ManagerFactory<JmsManager, JmsConfiguration>
    {
        @Override
        public JmsManager createManager(final String name, final JmsConfiguration data) {
            try {
                return new JmsManager(name, data.jndiManager, data.connectionFactoryName, data.destinationName, data.username, data.password, null);
            }
            catch (Exception e) {
                JmsManager.LOGGER.error("Error creating JmsManager using ConnectionFactory [{}] and Destination [{}].", new Object[] { data.connectionFactoryName, data.destinationName, e });
                return null;
            }
        }
    }
}
