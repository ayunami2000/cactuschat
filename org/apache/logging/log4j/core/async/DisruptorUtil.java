// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.status.StatusLogger;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import com.lmax.disruptor.ExceptionHandler;
import org.apache.logging.log4j.core.util.Integers;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import org.apache.logging.log4j.util.PropertiesUtil;
import com.lmax.disruptor.WaitStrategy;
import org.apache.logging.log4j.Logger;

final class DisruptorUtil
{
    private static final Logger LOGGER;
    private static final int RINGBUFFER_MIN_SIZE = 128;
    private static final int RINGBUFFER_DEFAULT_SIZE = 262144;
    
    private DisruptorUtil() {
    }
    
    static WaitStrategy createWaitStrategy(final String propertyName) {
        final String strategy = PropertiesUtil.getProperties().getStringProperty(propertyName);
        if (strategy != null) {
            DisruptorUtil.LOGGER.trace("property {}={}", new Object[] { propertyName, strategy });
            if ("Sleep".equalsIgnoreCase(strategy)) {
                return (WaitStrategy)new SleepingWaitStrategy();
            }
            if ("Yield".equalsIgnoreCase(strategy)) {
                return (WaitStrategy)new YieldingWaitStrategy();
            }
            if ("Block".equalsIgnoreCase(strategy)) {
                return (WaitStrategy)new BlockingWaitStrategy();
            }
        }
        return (WaitStrategy)new BlockingWaitStrategy();
    }
    
    static int calculateRingBufferSize(final String propertyName) {
        int ringBufferSize = 262144;
        final String userPreferredRBSize = PropertiesUtil.getProperties().getStringProperty(propertyName, String.valueOf(ringBufferSize));
        try {
            int size = Integer.parseInt(userPreferredRBSize);
            if (size < 128) {
                size = 128;
                DisruptorUtil.LOGGER.warn("Invalid RingBufferSize {}, using minimum size {}.", new Object[] { userPreferredRBSize, 128 });
            }
            ringBufferSize = size;
        }
        catch (Exception ex) {
            DisruptorUtil.LOGGER.warn("Invalid RingBufferSize {}, using default size {}.", new Object[] { userPreferredRBSize, ringBufferSize });
        }
        return Integers.ceilingNextPowerOfTwo(ringBufferSize);
    }
    
    static <T> ExceptionHandler<T> getExceptionHandler(final String propertyName, final Class<T> type) {
        final String cls = PropertiesUtil.getProperties().getStringProperty(propertyName);
        if (cls == null) {
            return null;
        }
        try {
            final Class<? extends ExceptionHandler<T>> klass = (Class<? extends ExceptionHandler<T>>)Class.forName(cls);
            return (ExceptionHandler<T>)klass.newInstance();
        }
        catch (Exception ignored) {
            DisruptorUtil.LOGGER.debug("Invalid {} value: error creating {}: ", new Object[] { propertyName, cls, ignored });
            return null;
        }
    }
    
    public static long getExecutorThreadId(final ExecutorService executor) {
        final Future<Long> result = executor.submit((Callable<Long>)new Callable<Long>() {
            @Override
            public Long call() {
                return Thread.currentThread().getId();
            }
        });
        try {
            return result.get();
        }
        catch (Exception ex) {
            final String msg = "Could not obtain executor thread Id. Giving up to avoid the risk of application deadlock.";
            throw new IllegalStateException("Could not obtain executor thread Id. Giving up to avoid the risk of application deadlock.", ex);
        }
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
}
