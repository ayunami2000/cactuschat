// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.apache.logging.log4j.util.LambdaUtil;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.Supplier;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.Marker;
import java.io.Serializable;

public abstract class AbstractLogger implements ExtendedLogger, Serializable
{
    public static final Marker FLOW_MARKER;
    public static final Marker ENTRY_MARKER;
    public static final Marker EXIT_MARKER;
    public static final Marker EXCEPTION_MARKER;
    public static final Marker THROWING_MARKER;
    public static final Marker CATCHING_MARKER;
    public static final Class<? extends MessageFactory> DEFAULT_MESSAGE_FACTORY_CLASS;
    private static final long serialVersionUID = 2L;
    private static final String FQCN;
    private static final String THROWING = "throwing";
    private static final String CATCHING = "catching";
    private final String name;
    private final MessageFactory messageFactory;
    
    public AbstractLogger() {
        this.name = this.getClass().getName();
        this.messageFactory = this.createDefaultMessageFactory();
    }
    
    public AbstractLogger(final String name) {
        this.name = name;
        this.messageFactory = this.createDefaultMessageFactory();
    }
    
    public AbstractLogger(final String name, final MessageFactory messageFactory) {
        this.name = name;
        this.messageFactory = ((messageFactory == null) ? this.createDefaultMessageFactory() : messageFactory);
    }
    
    public static void checkMessageFactory(final ExtendedLogger logger, final MessageFactory messageFactory) {
        final String name = logger.getName();
        final MessageFactory loggerMessageFactory = logger.getMessageFactory();
        if (messageFactory != null && !loggerMessageFactory.equals(messageFactory)) {
            StatusLogger.getLogger().warn("The Logger {} was created with the message factory {} and is now requested with the message factory {}, which may create log events with unexpected formatting.", new Object[] { name, loggerMessageFactory, messageFactory });
        }
        else if (messageFactory == null && !loggerMessageFactory.getClass().equals(AbstractLogger.DEFAULT_MESSAGE_FACTORY_CLASS)) {
            StatusLogger.getLogger().warn("The Logger {} was created with the message factory {} and is now requested with a null message factory (defaults to {}), which may create log events with unexpected formatting.", new Object[] { name, loggerMessageFactory, AbstractLogger.DEFAULT_MESSAGE_FACTORY_CLASS.getName() });
        }
    }
    
    @Override
    public void catching(final Level level, final Throwable t) {
        this.catching(AbstractLogger.FQCN, level, t);
    }
    
    protected void catching(final String fqcn, final Level level, final Throwable t) {
        if (this.isEnabled(level, AbstractLogger.CATCHING_MARKER, (Object)null, null)) {
            this.logMessage(fqcn, level, AbstractLogger.CATCHING_MARKER, this.catchingMsg(t), t);
        }
    }
    
    @Override
    public void catching(final Throwable t) {
        if (this.isEnabled(Level.ERROR, AbstractLogger.CATCHING_MARKER, (Object)null, null)) {
            this.logMessage(AbstractLogger.FQCN, Level.ERROR, AbstractLogger.CATCHING_MARKER, this.catchingMsg(t), t);
        }
    }
    
    protected Message catchingMsg(final Throwable t) {
        return this.messageFactory.newMessage("catching");
    }
    
    private MessageFactory createDefaultMessageFactory() {
        try {
            return (MessageFactory)AbstractLogger.DEFAULT_MESSAGE_FACTORY_CLASS.newInstance();
        }
        catch (InstantiationException e) {
            throw new IllegalStateException(e);
        }
        catch (IllegalAccessException e2) {
            throw new IllegalStateException(e2);
        }
    }
    
    @Override
    public void debug(final Marker marker, final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, msg, null);
    }
    
    @Override
    public void debug(final Marker marker, final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, msg, t);
    }
    
    @Override
    public void debug(final Marker marker, final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, message, null);
    }
    
    @Override
    public void debug(final Marker marker, final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, message, t);
    }
    
    @Override
    public void debug(final Marker marker, final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, message, (Throwable)null);
    }
    
    @Override
    public void debug(final Marker marker, final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, message, params);
    }
    
    @Override
    public void debug(final Marker marker, final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, message, t);
    }
    
    @Override
    public void debug(final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, msg, null);
    }
    
    @Override
    public void debug(final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, msg, t);
    }
    
    @Override
    public void debug(final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, message, null);
    }
    
    @Override
    public void debug(final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, message, t);
    }
    
    @Override
    public void debug(final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, message, (Throwable)null);
    }
    
    @Override
    public void debug(final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, message, params);
    }
    
    @Override
    public void debug(final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, message, t);
    }
    
    @Override
    public void debug(final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, msgSupplier, null);
    }
    
    @Override
    public void debug(final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, msgSupplier, t);
    }
    
    @Override
    public void debug(final Marker marker, final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, msgSupplier, null);
    }
    
    @Override
    public void debug(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, message, paramSuppliers);
    }
    
    @Override
    public void debug(final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, msgSupplier, t);
    }
    
    @Override
    public void debug(final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, message, paramSuppliers);
    }
    
    @Override
    public void debug(final Marker marker, final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, msgSupplier, null);
    }
    
    @Override
    public void debug(final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, marker, msgSupplier, t);
    }
    
    @Override
    public void debug(final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, msgSupplier, null);
    }
    
    @Override
    public void debug(final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.DEBUG, null, msgSupplier, t);
    }
    
    @Override
    public void entry() {
        this.entry(AbstractLogger.FQCN, new Object[0]);
    }
    
    @Override
    public void entry(final Object... params) {
        this.entry(AbstractLogger.FQCN, params);
    }
    
    protected void entry(final String fqcn, final Object... params) {
        if (this.isEnabled(Level.TRACE, AbstractLogger.ENTRY_MARKER, (Object)null, null)) {
            this.logIfEnabled(fqcn, Level.TRACE, AbstractLogger.ENTRY_MARKER, this.entryMsg(params.length, params), null);
        }
    }
    
    protected Message entryMsg(final int count, final Object... params) {
        if (count == 0) {
            return this.messageFactory.newMessage("entry");
        }
        final StringBuilder sb = new StringBuilder("entry params(");
        for (int i = 0; i < params.length; ++i) {
            final Object parm = params[i];
            sb.append((parm != null) ? parm.toString() : "null");
            if (i + 1 < params.length) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return this.messageFactory.newMessage(sb.toString());
    }
    
    @Override
    public void error(final Marker marker, final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, msg, null);
    }
    
    @Override
    public void error(final Marker marker, final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, msg, t);
    }
    
    @Override
    public void error(final Marker marker, final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, message, null);
    }
    
    @Override
    public void error(final Marker marker, final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, message, t);
    }
    
    @Override
    public void error(final Marker marker, final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, message, (Throwable)null);
    }
    
    @Override
    public void error(final Marker marker, final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, message, params);
    }
    
    @Override
    public void error(final Marker marker, final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, message, t);
    }
    
    @Override
    public void error(final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, msg, null);
    }
    
    @Override
    public void error(final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, msg, t);
    }
    
    @Override
    public void error(final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, message, null);
    }
    
    @Override
    public void error(final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, message, t);
    }
    
    @Override
    public void error(final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, message, (Throwable)null);
    }
    
    @Override
    public void error(final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, message, params);
    }
    
    @Override
    public void error(final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, message, t);
    }
    
    @Override
    public void error(final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, msgSupplier, null);
    }
    
    @Override
    public void error(final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, msgSupplier, t);
    }
    
    @Override
    public void error(final Marker marker, final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, msgSupplier, null);
    }
    
    @Override
    public void error(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, message, paramSuppliers);
    }
    
    @Override
    public void error(final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, msgSupplier, t);
    }
    
    @Override
    public void error(final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, message, paramSuppliers);
    }
    
    @Override
    public void error(final Marker marker, final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, msgSupplier, null);
    }
    
    @Override
    public void error(final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, marker, msgSupplier, t);
    }
    
    @Override
    public void error(final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, msgSupplier, null);
    }
    
    @Override
    public void error(final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.ERROR, null, msgSupplier, t);
    }
    
    @Override
    public void exit() {
        this.exit(AbstractLogger.FQCN, (Object)null);
    }
    
    @Override
    public <R> R exit(final R result) {
        return this.exit(AbstractLogger.FQCN, result);
    }
    
    protected <R> R exit(final String fqcn, final R result) {
        if (this.isEnabled(Level.TRACE, AbstractLogger.EXIT_MARKER, (Object)null, null)) {
            this.logIfEnabled(fqcn, Level.TRACE, AbstractLogger.EXIT_MARKER, this.exitMsg(result), null);
        }
        return result;
    }
    
    protected Message exitMsg(final Object result) {
        if (result == null) {
            return this.messageFactory.newMessage("exit");
        }
        return this.messageFactory.newMessage("exit with(" + result + ')');
    }
    
    @Override
    public void fatal(final Marker marker, final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, msg, null);
    }
    
    @Override
    public void fatal(final Marker marker, final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, msg, t);
    }
    
    @Override
    public void fatal(final Marker marker, final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, message, null);
    }
    
    @Override
    public void fatal(final Marker marker, final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, message, t);
    }
    
    @Override
    public void fatal(final Marker marker, final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, message, (Throwable)null);
    }
    
    @Override
    public void fatal(final Marker marker, final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, message, params);
    }
    
    @Override
    public void fatal(final Marker marker, final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, message, t);
    }
    
    @Override
    public void fatal(final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, msg, null);
    }
    
    @Override
    public void fatal(final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, msg, t);
    }
    
    @Override
    public void fatal(final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, message, null);
    }
    
    @Override
    public void fatal(final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, message, t);
    }
    
    @Override
    public void fatal(final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, message, (Throwable)null);
    }
    
    @Override
    public void fatal(final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, message, params);
    }
    
    @Override
    public void fatal(final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, message, t);
    }
    
    @Override
    public void fatal(final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, msgSupplier, null);
    }
    
    @Override
    public void fatal(final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, msgSupplier, t);
    }
    
    @Override
    public void fatal(final Marker marker, final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, msgSupplier, null);
    }
    
    @Override
    public void fatal(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, message, paramSuppliers);
    }
    
    @Override
    public void fatal(final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, msgSupplier, t);
    }
    
    @Override
    public void fatal(final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, message, paramSuppliers);
    }
    
    @Override
    public void fatal(final Marker marker, final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, msgSupplier, null);
    }
    
    @Override
    public void fatal(final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, marker, msgSupplier, t);
    }
    
    @Override
    public void fatal(final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, msgSupplier, null);
    }
    
    @Override
    public void fatal(final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.FATAL, null, msgSupplier, t);
    }
    
    @Override
    public MessageFactory getMessageFactory() {
        return this.messageFactory;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public void info(final Marker marker, final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, msg, null);
    }
    
    @Override
    public void info(final Marker marker, final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, msg, t);
    }
    
    @Override
    public void info(final Marker marker, final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, message, null);
    }
    
    @Override
    public void info(final Marker marker, final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, message, t);
    }
    
    @Override
    public void info(final Marker marker, final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, message, (Throwable)null);
    }
    
    @Override
    public void info(final Marker marker, final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, message, params);
    }
    
    @Override
    public void info(final Marker marker, final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, message, t);
    }
    
    @Override
    public void info(final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, msg, null);
    }
    
    @Override
    public void info(final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, msg, t);
    }
    
    @Override
    public void info(final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, message, null);
    }
    
    @Override
    public void info(final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, message, t);
    }
    
    @Override
    public void info(final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, message, (Throwable)null);
    }
    
    @Override
    public void info(final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, message, params);
    }
    
    @Override
    public void info(final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, message, t);
    }
    
    @Override
    public void info(final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, msgSupplier, null);
    }
    
    @Override
    public void info(final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, msgSupplier, t);
    }
    
    @Override
    public void info(final Marker marker, final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, msgSupplier, null);
    }
    
    @Override
    public void info(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, message, paramSuppliers);
    }
    
    @Override
    public void info(final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, msgSupplier, t);
    }
    
    @Override
    public void info(final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, message, paramSuppliers);
    }
    
    @Override
    public void info(final Marker marker, final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, msgSupplier, null);
    }
    
    @Override
    public void info(final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, marker, msgSupplier, t);
    }
    
    @Override
    public void info(final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, msgSupplier, null);
    }
    
    @Override
    public void info(final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.INFO, null, msgSupplier, t);
    }
    
    @Override
    public boolean isDebugEnabled() {
        return this.isEnabled(Level.DEBUG, null, null);
    }
    
    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return this.isEnabled(Level.DEBUG, marker, (Object)null, null);
    }
    
    @Override
    public boolean isEnabled(final Level level) {
        return this.isEnabled(level, null, (Object)null, null);
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker) {
        return this.isEnabled(level, marker, (Object)null, null);
    }
    
    @Override
    public boolean isErrorEnabled() {
        return this.isEnabled(Level.ERROR, null, (Object)null, null);
    }
    
    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return this.isEnabled(Level.ERROR, marker, (Object)null, null);
    }
    
    @Override
    public boolean isFatalEnabled() {
        return this.isEnabled(Level.FATAL, null, (Object)null, null);
    }
    
    @Override
    public boolean isFatalEnabled(final Marker marker) {
        return this.isEnabled(Level.FATAL, marker, (Object)null, null);
    }
    
    @Override
    public boolean isInfoEnabled() {
        return this.isEnabled(Level.INFO, null, (Object)null, null);
    }
    
    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return this.isEnabled(Level.INFO, marker, (Object)null, null);
    }
    
    @Override
    public boolean isTraceEnabled() {
        return this.isEnabled(Level.TRACE, null, (Object)null, null);
    }
    
    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return this.isEnabled(Level.TRACE, marker, (Object)null, null);
    }
    
    @Override
    public boolean isWarnEnabled() {
        return this.isEnabled(Level.WARN, null, (Object)null, null);
    }
    
    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return this.isEnabled(Level.WARN, marker, (Object)null, null);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, msg, null);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, msg, t);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, message, null);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final Object message, final Throwable t) {
        if (this.isEnabled(level, marker, message, t)) {
            this.logMessage(AbstractLogger.FQCN, level, marker, message, t);
        }
    }
    
    @Override
    public void log(final Level level, final Marker marker, final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, message, (Throwable)null);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, message, params);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, message, t);
    }
    
    @Override
    public void log(final Level level, final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, msg, null);
    }
    
    @Override
    public void log(final Level level, final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, msg, t);
    }
    
    @Override
    public void log(final Level level, final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, message, null);
    }
    
    @Override
    public void log(final Level level, final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, message, t);
    }
    
    @Override
    public void log(final Level level, final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, message, (Throwable)null);
    }
    
    @Override
    public void log(final Level level, final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, message, params);
    }
    
    @Override
    public void log(final Level level, final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, message, t);
    }
    
    @Override
    public void log(final Level level, final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, msgSupplier, null);
    }
    
    @Override
    public void log(final Level level, final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, msgSupplier, t);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, msgSupplier, null);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, message, paramSuppliers);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, msgSupplier, t);
    }
    
    @Override
    public void log(final Level level, final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, message, paramSuppliers);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, msgSupplier, null);
    }
    
    @Override
    public void log(final Level level, final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, level, marker, msgSupplier, t);
    }
    
    @Override
    public void log(final Level level, final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, msgSupplier, null);
    }
    
    @Override
    public void log(final Level level, final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, level, null, msgSupplier, t);
    }
    
    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final Message msg, final Throwable t) {
        if (this.isEnabled(level, marker, msg, t)) {
            this.logMessage(fqcn, level, marker, msg, t);
        }
    }
    
    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        if (this.isEnabled(level, marker, msgSupplier, t)) {
            this.logMessage(fqcn, level, marker, msgSupplier, t);
        }
    }
    
    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final Object message, final Throwable t) {
        if (this.isEnabled(level, marker, message, t)) {
            this.logMessage(fqcn, level, marker, message, t);
        }
    }
    
    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        if (this.isEnabled(level, marker, msgSupplier, t)) {
            this.logMessage(fqcn, level, marker, msgSupplier, t);
        }
    }
    
    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message) {
        if (this.isEnabled(level, marker, message)) {
            this.logMessage(fqcn, level, marker, message);
        }
    }
    
    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        if (this.isEnabled(level, marker, message)) {
            this.logMessage(fqcn, level, marker, message, paramSuppliers);
        }
    }
    
    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object... params) {
        if (this.isEnabled(level, marker, message, params)) {
            this.logMessage(fqcn, level, marker, message, params);
        }
    }
    
    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Throwable t) {
        if (this.isEnabled(level, marker, message, t)) {
            this.logMessage(fqcn, level, marker, message, t);
        }
    }
    
    protected void logMessage(final String fqcn, final Level level, final Marker marker, final Object message, final Throwable t) {
        this.logMessage(fqcn, level, marker, this.messageFactory.newMessage(message), t);
    }
    
    protected void logMessage(final String fqcn, final Level level, final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        final Message message = LambdaUtil.get(msgSupplier);
        this.logMessage(fqcn, level, marker, message, t);
    }
    
    protected void logMessage(final String fqcn, final Level level, final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        final Object message = LambdaUtil.get(msgSupplier);
        this.logMessage(fqcn, level, marker, this.messageFactory.newMessage(message), t);
    }
    
    protected void logMessage(final String fqcn, final Level level, final Marker marker, final String message, final Throwable t) {
        this.logMessage(fqcn, level, marker, this.messageFactory.newMessage(message), t);
    }
    
    protected void logMessage(final String fqcn, final Level level, final Marker marker, final String message) {
        final Message msg = this.messageFactory.newMessage(message);
        this.logMessage(fqcn, level, marker, msg, msg.getThrowable());
    }
    
    protected void logMessage(final String fqcn, final Level level, final Marker marker, final String message, final Object... params) {
        final Message msg = this.messageFactory.newMessage(message, params);
        this.logMessage(fqcn, level, marker, msg, msg.getThrowable());
    }
    
    protected void logMessage(final String fqcn, final Level level, final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        final Message msg = this.messageFactory.newMessage(message, LambdaUtil.getAll(paramSuppliers));
        this.logMessage(fqcn, level, marker, msg, msg.getThrowable());
    }
    
    @Override
    public void printf(final Level level, final Marker marker, final String format, final Object... params) {
        if (this.isEnabled(level, marker, format, params)) {
            final Message msg = new StringFormattedMessage(format, params);
            this.logMessage(AbstractLogger.FQCN, level, marker, msg, msg.getThrowable());
        }
    }
    
    @Override
    public void printf(final Level level, final String format, final Object... params) {
        if (this.isEnabled(level, null, format, params)) {
            final Message msg = new StringFormattedMessage(format, params);
            this.logMessage(AbstractLogger.FQCN, level, null, msg, msg.getThrowable());
        }
    }
    
    @Override
    public <T extends Throwable> T throwing(final T t) {
        return this.throwing(AbstractLogger.FQCN, Level.ERROR, t);
    }
    
    @Override
    public <T extends Throwable> T throwing(final Level level, final T t) {
        return this.throwing(AbstractLogger.FQCN, level, t);
    }
    
    protected <T extends Throwable> T throwing(final String fqcn, final Level level, final T t) {
        if (this.isEnabled(level, AbstractLogger.THROWING_MARKER, (Object)null, null)) {
            this.logMessage(fqcn, level, AbstractLogger.THROWING_MARKER, this.throwingMsg(t), t);
        }
        return t;
    }
    
    protected Message throwingMsg(final Throwable t) {
        return this.messageFactory.newMessage("throwing");
    }
    
    @Override
    public void trace(final Marker marker, final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, msg, null);
    }
    
    @Override
    public void trace(final Marker marker, final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, msg, t);
    }
    
    @Override
    public void trace(final Marker marker, final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, message, null);
    }
    
    @Override
    public void trace(final Marker marker, final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, message, t);
    }
    
    @Override
    public void trace(final Marker marker, final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, message, (Throwable)null);
    }
    
    @Override
    public void trace(final Marker marker, final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, message, params);
    }
    
    @Override
    public void trace(final Marker marker, final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, message, t);
    }
    
    @Override
    public void trace(final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, msg, null);
    }
    
    @Override
    public void trace(final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, msg, t);
    }
    
    @Override
    public void trace(final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, message, null);
    }
    
    @Override
    public void trace(final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, message, t);
    }
    
    @Override
    public void trace(final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, message, (Throwable)null);
    }
    
    @Override
    public void trace(final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, message, params);
    }
    
    @Override
    public void trace(final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, message, t);
    }
    
    @Override
    public void trace(final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, msgSupplier, null);
    }
    
    @Override
    public void trace(final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, msgSupplier, t);
    }
    
    @Override
    public void trace(final Marker marker, final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, msgSupplier, null);
    }
    
    @Override
    public void trace(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, message, paramSuppliers);
    }
    
    @Override
    public void trace(final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, msgSupplier, t);
    }
    
    @Override
    public void trace(final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, message, paramSuppliers);
    }
    
    @Override
    public void trace(final Marker marker, final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, msgSupplier, null);
    }
    
    @Override
    public void trace(final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, marker, msgSupplier, t);
    }
    
    @Override
    public void trace(final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, msgSupplier, null);
    }
    
    @Override
    public void trace(final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.TRACE, null, msgSupplier, t);
    }
    
    @Override
    public void warn(final Marker marker, final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, msg, null);
    }
    
    @Override
    public void warn(final Marker marker, final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, msg, t);
    }
    
    @Override
    public void warn(final Marker marker, final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, message, null);
    }
    
    @Override
    public void warn(final Marker marker, final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, message, t);
    }
    
    @Override
    public void warn(final Marker marker, final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, message, (Throwable)null);
    }
    
    @Override
    public void warn(final Marker marker, final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, message, params);
    }
    
    @Override
    public void warn(final Marker marker, final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, message, t);
    }
    
    @Override
    public void warn(final Message msg) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, msg, null);
    }
    
    @Override
    public void warn(final Message msg, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, msg, t);
    }
    
    @Override
    public void warn(final Object message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, message, null);
    }
    
    @Override
    public void warn(final Object message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, message, t);
    }
    
    @Override
    public void warn(final String message) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, message, (Throwable)null);
    }
    
    @Override
    public void warn(final String message, final Object... params) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, message, params);
    }
    
    @Override
    public void warn(final String message, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, message, t);
    }
    
    @Override
    public void warn(final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, msgSupplier, null);
    }
    
    @Override
    public void warn(final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, msgSupplier, t);
    }
    
    @Override
    public void warn(final Marker marker, final Supplier<?> msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, msgSupplier, null);
    }
    
    @Override
    public void warn(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, message, paramSuppliers);
    }
    
    @Override
    public void warn(final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, msgSupplier, t);
    }
    
    @Override
    public void warn(final String message, final Supplier<?>... paramSuppliers) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, message, paramSuppliers);
    }
    
    @Override
    public void warn(final Marker marker, final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, msgSupplier, null);
    }
    
    @Override
    public void warn(final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, marker, msgSupplier, t);
    }
    
    @Override
    public void warn(final MessageSupplier msgSupplier) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, msgSupplier, null);
    }
    
    @Override
    public void warn(final MessageSupplier msgSupplier, final Throwable t) {
        this.logIfEnabled(AbstractLogger.FQCN, Level.WARN, null, msgSupplier, t);
    }
    
    static {
        FLOW_MARKER = MarkerManager.getMarker("FLOW");
        ENTRY_MARKER = MarkerManager.getMarker("ENTRY").setParents(AbstractLogger.FLOW_MARKER);
        EXIT_MARKER = MarkerManager.getMarker("EXIT").setParents(AbstractLogger.FLOW_MARKER);
        EXCEPTION_MARKER = MarkerManager.getMarker("EXCEPTION");
        THROWING_MARKER = MarkerManager.getMarker("THROWING").setParents(AbstractLogger.EXCEPTION_MARKER);
        CATCHING_MARKER = MarkerManager.getMarker("CATCHING").setParents(AbstractLogger.EXCEPTION_MARKER);
        DEFAULT_MESSAGE_FACTORY_CLASS = ParameterizedMessageFactory.class;
        FQCN = AbstractLogger.class.getName();
    }
}
