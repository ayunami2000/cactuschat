// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.Recycler;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.PlatformDependent;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.logging.InternalLogger;

final class ThreadLocalPooledDirectByteBuf
{
    private static final InternalLogger logger;
    public static final int threadLocalDirectBufferSize;
    
    public static ByteBuf newInstance() {
        if (PlatformDependent.hasUnsafe()) {
            return ThreadLocalUnsafeDirectByteBuf.newInstance();
        }
        return ThreadLocalDirectByteBuf.newInstance();
    }
    
    private ThreadLocalPooledDirectByteBuf() {
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(ThreadLocalPooledDirectByteBuf.class);
        threadLocalDirectBufferSize = SystemPropertyUtil.getInt("io.netty.threadLocalDirectBufferSize", 65536);
        ThreadLocalPooledDirectByteBuf.logger.debug("-Dio.netty.threadLocalDirectBufferSize: {}", (Object)ThreadLocalPooledDirectByteBuf.threadLocalDirectBufferSize);
    }
    
    static final class ThreadLocalUnsafeDirectByteBuf extends UnpooledUnsafeDirectByteBuf
    {
        private static final Recycler<ThreadLocalUnsafeDirectByteBuf> RECYCLER;
        private final Recycler.Handle<ThreadLocalUnsafeDirectByteBuf> handle;
        
        static ThreadLocalUnsafeDirectByteBuf newInstance() {
            final ThreadLocalUnsafeDirectByteBuf buf = ThreadLocalUnsafeDirectByteBuf.RECYCLER.get();
            buf.setRefCnt(1);
            return buf;
        }
        
        private ThreadLocalUnsafeDirectByteBuf(final Recycler.Handle<ThreadLocalUnsafeDirectByteBuf> handle) {
            super(UnpooledByteBufAllocator.DEFAULT, 256, Integer.MAX_VALUE);
            this.handle = handle;
        }
        
        @Override
        protected void deallocate() {
            if (this.capacity() > ThreadLocalPooledDirectByteBuf.threadLocalDirectBufferSize) {
                super.deallocate();
            }
            else {
                this.clear();
                ThreadLocalUnsafeDirectByteBuf.RECYCLER.recycle(this, this.handle);
            }
        }
        
        static {
            RECYCLER = new Recycler<ThreadLocalUnsafeDirectByteBuf>() {
                @Override
                protected ThreadLocalUnsafeDirectByteBuf newObject(final Handle<ThreadLocalUnsafeDirectByteBuf> handle) {
                    return new ThreadLocalUnsafeDirectByteBuf((Handle)handle);
                }
            };
        }
    }
    
    static final class ThreadLocalDirectByteBuf extends UnpooledDirectByteBuf
    {
        private static final Recycler<ThreadLocalDirectByteBuf> RECYCLER;
        private final Recycler.Handle<ThreadLocalDirectByteBuf> handle;
        
        static ThreadLocalDirectByteBuf newInstance() {
            final ThreadLocalDirectByteBuf buf = ThreadLocalDirectByteBuf.RECYCLER.get();
            buf.setRefCnt(1);
            return buf;
        }
        
        private ThreadLocalDirectByteBuf(final Recycler.Handle<ThreadLocalDirectByteBuf> handle) {
            super(UnpooledByteBufAllocator.DEFAULT, 256, Integer.MAX_VALUE);
            this.handle = handle;
        }
        
        @Override
        protected void deallocate() {
            if (this.capacity() > ThreadLocalPooledDirectByteBuf.threadLocalDirectBufferSize) {
                super.deallocate();
            }
            else {
                this.clear();
                ThreadLocalDirectByteBuf.RECYCLER.recycle(this, this.handle);
            }
        }
        
        static {
            RECYCLER = new Recycler<ThreadLocalDirectByteBuf>() {
                @Override
                protected ThreadLocalDirectByteBuf newObject(final Handle<ThreadLocalDirectByteBuf> handle) {
                    return new ThreadLocalDirectByteBuf((Handle)handle);
                }
            };
        }
    }
}
