// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

import io.netty.util.internal.InternalThreadLocalMap;
import io.netty.util.internal.chmv8.ForkJoinWorkerThread;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.Locale;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.chmv8.ForkJoinPool;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import io.netty.util.internal.logging.InternalLogger;

public final class DefaultExecutorServiceFactory implements ExecutorServiceFactory
{
    private static final InternalLogger logger;
    private static final AtomicInteger executorId;
    private final String namePrefix;
    
    public DefaultExecutorServiceFactory(final Class<?> clazzNamePrefix) {
        this(toName(clazzNamePrefix));
    }
    
    public DefaultExecutorServiceFactory(final String namePrefix) {
        this.namePrefix = namePrefix;
    }
    
    @Override
    public ExecutorService newExecutorService(final int parallelism) {
        final ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory = new DefaultForkJoinWorkerThreadFactory(this.namePrefix + '-' + DefaultExecutorServiceFactory.executorId.getAndIncrement());
        return new ForkJoinPool(parallelism, threadFactory, DefaultUncaughtExceptionHandler.INSTANCE, true);
    }
    
    private static String toName(final Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        final String clazzName = StringUtil.simpleClassName(clazz);
        switch (clazzName.length()) {
            case 0: {
                return "unknown";
            }
            case 1: {
                return clazzName.toLowerCase(Locale.US);
            }
            default: {
                if (Character.isUpperCase(clazzName.charAt(0)) && Character.isLowerCase(clazzName.charAt(1))) {
                    return Character.toLowerCase(clazzName.charAt(0)) + clazzName.substring(1);
                }
                return clazzName;
            }
        }
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(DefaultExecutorServiceFactory.class);
        executorId = new AtomicInteger();
    }
    
    private static final class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        private static final DefaultUncaughtExceptionHandler INSTANCE;
        
        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            if (DefaultExecutorServiceFactory.logger.isErrorEnabled()) {
                DefaultExecutorServiceFactory.logger.error("Uncaught exception in thread: {}", t.getName(), e);
            }
        }
        
        static {
            INSTANCE = new DefaultUncaughtExceptionHandler();
        }
    }
    
    private static final class DefaultForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory
    {
        private final AtomicInteger idx;
        private final String namePrefix;
        
        DefaultForkJoinWorkerThreadFactory(final String namePrefix) {
            this.idx = new AtomicInteger();
            this.namePrefix = namePrefix;
        }
        
        @Override
        public ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
            final ForkJoinWorkerThread thread = new DefaultForkJoinWorkerThread(pool);
            thread.setName(this.namePrefix + '-' + this.idx.getAndIncrement());
            thread.setPriority(10);
            return thread;
        }
    }
    
    private static final class DefaultForkJoinWorkerThread extends ForkJoinWorkerThread implements FastThreadLocalAccess
    {
        private InternalThreadLocalMap threadLocalMap;
        
        DefaultForkJoinWorkerThread(final ForkJoinPool pool) {
            super(pool);
        }
        
        @Override
        public InternalThreadLocalMap threadLocalMap() {
            return this.threadLocalMap;
        }
        
        @Override
        public void setThreadLocalMap(final InternalThreadLocalMap threadLocalMap) {
            this.threadLocalMap = threadLocalMap;
        }
    }
}
