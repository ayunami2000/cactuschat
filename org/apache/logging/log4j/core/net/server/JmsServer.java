// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.net.server;

import org.apache.logging.log4j.LoggingException;
import javax.jms.JMSException;
import org.apache.logging.log4j.core.LogEvent;
import javax.jms.ObjectMessage;
import javax.jms.Message;
import org.apache.logging.log4j.core.net.JndiManager;
import javax.jms.MessageConsumer;
import org.apache.logging.log4j.core.appender.mom.JmsManager;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.core.LifeCycle;
import javax.jms.MessageListener;
import org.apache.logging.log4j.core.LogEventListener;

public class JmsServer extends LogEventListener implements MessageListener, LifeCycle
{
    private final AtomicReference<State> state;
    private final JmsManager jmsManager;
    private MessageConsumer messageConsumer;
    
    public JmsServer(final String connectionFactoryBindingName, final String destinationBindingName, final String username, final String password) {
        this.state = new AtomicReference<State>(State.INITIALIZED);
        final String managerName = JmsServer.class.getName() + '@' + JmsServer.class.hashCode();
        final JndiManager jndiManager = JndiManager.getDefaultManager(managerName);
        this.jmsManager = JmsManager.getJmsManager(managerName, jndiManager, connectionFactoryBindingName, destinationBindingName, username, password);
    }
    
    public State getState() {
        return this.state.get();
    }
    
    public void onMessage(final Message message) {
        try {
            if (message instanceof ObjectMessage) {
                final Object body = ((ObjectMessage)message).getObject();
                if (body instanceof LogEvent) {
                    this.log((LogEvent)body);
                }
                else {
                    JmsServer.LOGGER.warn("Expected ObjectMessage to contain LogEvent. Got type {} instead.", new Object[] { body.getClass() });
                }
            }
            else {
                JmsServer.LOGGER.warn("Received message of type {} and JMSType {} which cannot be handled.", new Object[] { message.getClass(), message.getJMSType() });
            }
        }
        catch (JMSException e) {
            JmsServer.LOGGER.catching((Throwable)e);
        }
    }
    
    public void initialize() {
    }
    
    public void start() {
        if (this.state.compareAndSet(State.INITIALIZED, State.STARTING)) {
            try {
                (this.messageConsumer = this.jmsManager.createMessageConsumer()).setMessageListener((MessageListener)this);
            }
            catch (JMSException e) {
                throw new LoggingException((Throwable)e);
            }
        }
    }
    
    public void stop() {
        try {
            this.messageConsumer.close();
        }
        catch (JMSException ex) {}
        this.jmsManager.release();
    }
    
    public boolean isStarted() {
        return this.state.get() == State.STARTED;
    }
    
    public boolean isStopped() {
        return this.state.get() == State.STOPPED;
    }
}
